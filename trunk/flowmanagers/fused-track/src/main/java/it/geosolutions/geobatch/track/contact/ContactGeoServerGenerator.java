/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geobatch.track.contact;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.track.dao.ContactDAO;
import it.geosolutions.geobatch.track.dao.PastContactPositionDAO;
import it.geosolutions.geobatch.track.model.Contact;
import it.geosolutions.geobatch.track.model.ContactPosition;
import it.geosolutions.geobatch.track.model.ContactType;
import it.geosolutions.geobatch.track.model.PastContactPosition;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.hibernate.SessionFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;


/**
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */

public class ContactGeoServerGenerator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	private ContactDAO contactDAO;
	private PastContactPositionDAO pastContactPositionDAO;
	
	private SessionFactory sessionFactory;
	
	private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	private static final WKTReader wkt_reader = new WKTReader(geometryFactory);

	
	public ContactGeoServerGenerator(GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	public ContactGeoServerGenerator(GeoServerActionConfiguration configuration,
			ContactDAO contactDAO, PastContactPositionDAO pastContactPositionDAO,
			SessionFactory sessionFactory) throws IOException {
		
		super(configuration);
		this.contactDAO = contactDAO;
		this.pastContactPositionDAO = pastContactPositionDAO;
		this.sessionFactory = sessionFactory;
	}

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {
		
		// ////////////////////////////////////////////////////////////////////
		//
		// Initializing input variables
		//
		// ////////////////////////////////////////////////////////////////////
		
		try {
			if (configuration == null) {
				LOGGER.log(Level.SEVERE, "ActionConfig is null.");
				throw new IllegalStateException("ActionConfig is null.");
			}

			final File workingDir = IOUtils.findLocation(configuration
					.getWorkingDirectory(), new File(
					((FileBaseCatalog) CatalogHolder.getCatalog())
							.getBaseDirectory()));

			if (workingDir == null) {
				LOGGER.log(Level.SEVERE, "Working directory is null.");
				throw new IllegalStateException("Working directory is null.");
			}

			if (!workingDir.exists() || !workingDir.isDirectory()) {
				LOGGER.log(Level.SEVERE, "Working directory does not exist ("
						+ workingDir.getAbsolutePath() + ").");
				throw new IllegalStateException(
						"Working directory does not exist ("
								+ workingDir.getAbsolutePath() + ").");
			}
			
			File[] dataList;
			dataList = handleDataFile(events);

			if(dataList == null)
				throw new Exception("Error while processing the layer data file set");
			
			File dataFile = null;
			for (File file : dataList) {
				if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("txt")) {
					dataFile = file;
					break;
				}
			}
			
			if(dataFile == null) {
                LOGGER.log(Level.SEVERE, "layer data file not found in fileset.");
                throw new IllegalStateException("layer data file not found in fileset.");
			}
			
			FileReader fileReader = new FileReader(dataFile);
			BufferedReader reader = new BufferedReader(fileReader);
			
			String line = reader.readLine();				
			long timestamp = Long.parseLong(line);
			line = reader.readLine();
		
		    while(line != null) {	
		    	if(line.indexOf(" ") != -1)
		    		line = line.replaceAll(" ", "&"); 
		    	
		    	String[] fields = null;
	    	    if(line.indexOf("&") != -1){
	    		    fields = line.split("&");
	    	    }
	    	    
	    	    long contactId = Long.parseLong(fields[0]);
	    	    long linkCode = Long.parseLong(fields[6]);
	    	    double velocyX = Double.parseDouble(fields[3]);
	    	    double velocyY = Double.parseDouble(fields[4]);
	    	    double cog = CalcBearing(velocyX, velocyY);
	    	    
	    	    // ///////////////////////////
	    	    // Setting the ContactType
	    	    // ///////////////////////////
	    	    
	    	    ContactType type = ContactType.NONESSENTIAL;
	    	    if(fields[5].indexOf("9999") != -1)
	    	    	type = ContactType.MMSI;
	    	    else if(fields[5].indexOf("3333") != -1)
	    	    	type = ContactType.RADAR;	    	    
	    	    
            	// //////////////////
            	// Checking
            	// //////////////////
            	
	    	    Contact cnt = contactDAO.isExist(contactId);

	    	    if(cnt != null){	    

	    	    	    	    	
	    	    	final PastContactPosition pastContactPosition = new PastContactPosition();
	    	    	pastContactPosition.setCog(cnt.getContactPosition().getCog());
	    	    	pastContactPosition.setCourse(cnt.getContactPosition().getCourse());
	    	    	pastContactPosition.setPosition(cnt.getContactPosition().getPosition());
	    	    	pastContactPosition.setTime(cnt.getContactPosition().getTime());
	    	    	pastContactPosition.setContact(cnt);
	    	    	
	    	    	pastContactPositionDAO.save(pastContactPosition);
	    	    	
	    	    	//
	    	    	// load the positions in the last hour
	    	    	// 
	    	    	final Date contactTimestamp = new Date(timestamp*1000);
	    	    	List<PastContactPosition> pastContacts = pastContactPositionDAO.findByPeriod(timestamp, 3600, cnt.getContactId());
	    	    	if(type.compareTo(ContactType.NONESSENTIAL) != 0){
	    	    		cnt.setContactType(type);
	    	    		cnt.setLink(linkCode);
	    	    	}
	    	    	
	    	    	ContactPosition contactPos = cnt.getContactPosition();
	    	    	contactPos.setTime(contactTimestamp);


	    	    	//
	    	    	// create current position
	    	    	//
	            	double longitude = Double.parseDouble(fields[1]);
	            	double latitude = Double.parseDouble(fields[2]);
	            	Point point = (Point)wkt_reader.read("POINT("+ longitude + " " + latitude + " 0)");
	                point.setSRID(4326);   
	                
	    	    	contactPos.setPosition(point);	 

	    	    	LineString lineSimplified = null;
	    	    	if(pastContacts != null && pastContacts.size() > 2){
	    	    		lineSimplified = buildHistoryLineString(pastContacts, point, cog);
	    	    	}else{
    	    	    	lineSimplified = lineSimplifier(contactPos.getCourse(), point, contactPos.getCog(), cog); 
	    	    	}        	    	    			

	    	    	contactPos.setCourse(lineSimplified);	     
	    	    	contactPos.setCog(cog);	    
	    	    	
	    	    	cnt.setContactPosition(contactPos);	    
	            	contactDAO.merge(cnt);

	    	    }else{	    	
	    	    	
	    	    	// ///////////////////////////////
		    	    // Setting the ContactPosition
		    	    // ///////////////////////////////
		    	    
		    	    ContactPosition currentContact = new ContactPosition();
		    	    currentContact.setTime(new Date(timestamp*1000));
		    	    currentContact.setCog(cog);

	            	WKTReader wkt_reader = new WKTReader(geometryFactory);
	            	
	            	double longitude = Double.parseDouble(fields[1]);
	            	double latitude = Double.parseDouble(fields[2]);
	            	
	            	Point point = (Point)wkt_reader.read("POINT("+ longitude + " " + latitude + " 0)");
	                point.setSRID(4326);   
	                  
		    	    currentContact.setPosition(point);
		    	    
	            	Coordinate[] coordinate = new Coordinate[2];        			
	         		coordinate[0] = new Coordinate (point.getCoordinate());
	         		coordinate[1] = new Coordinate (point.getCoordinate());

	            	CoordinateSequence sequence = geometryFactory.getCoordinateSequenceFactory().create(coordinate);
	            	LineString course = new LineString(sequence, geometryFactory);   
	            	course.setSRID(4326);

	            	currentContact.setCourse(course);
	            	
		    	    // ///////////////////////////////
		    	    // Saving the Contact
		    	    // ///////////////////////////////
	            	
	            	Contact contact = new Contact();
	            	contact.setContactId(contactId);
	            	contact.setContactType(type);
	            	contact.setContactPosition(currentContact);
	            	contact.setLink(linkCode);
	            	
	            	contactDAO.save(contact);
	    	    }

		        line = reader.readLine();
		    }  
			//System.out.println("::::::::::::::::::::  " + this.sessionFactory.getStatistics());
			return events; 
			
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		} 
	}

	private static LineString buildHistoryLineString(
			final List<PastContactPosition> pastContacts,
			final Point newPoint, 
			final double newCog){

		// create the new coordinate list
		final List<Coordinate> coordinateList = new ArrayList<Coordinate>();

		// simplify the points as we add them
		double oldCog = 0.0;
		
		// parse the past contacts plus the new one
		final int size=pastContacts.size();
		for(int i = 0; i<size+1;i++){
			if(i<size)
			{
				// current past contact
				final PastContactPosition contact=pastContacts.get(i);
				if(i < 2){
					coordinateList.add(contact.getPosition().getCoordinate());
					oldCog = contact.getCog();
				}else{
					final double mCheck = Math.abs(contact.getCog() - oldCog);
					// check for exclusion based on COG
					if(mCheck > 5){	
						coordinateList.add(contact.getPosition().getCoordinate());
						oldCog = contact.getCog();
					}
				}
			}
			else
			{
				// new point
				final double mCheck = Math.abs(newCog - oldCog);
				// check for exclusion based on COG
				if(mCheck > 5){	
					coordinateList.add(newPoint.getCoordinate());
					oldCog = newCog;
				}				
			}
		}
	
		
		// create a sequence with the current coordinates
		final int newSize=coordinateList.size();
		final Coordinate[] coordinate = new Coordinate[newSize];
		for(int j=0; j<newSize; j++){
			coordinate[j] = coordinateList.get(j);
		}
		
    	CoordinateSequence sequence = geometryFactory.getCoordinateSequenceFactory().create(coordinate);
    	LineString course = new LineString(sequence, geometryFactory);   
    	course.setSRID(4326);

    	return course;
	}
	
	private static LineString lineSimplifier(final LineString line, final Point newPoint, 
			final double oldCog, final double newCog){

		int numPoints = line.getNumPoints();
		double mCheck = Math.abs(newCog - oldCog);

		if(mCheck > 5){		

			// ////////////////////////////////////////
			// Adding a new point to linestring course 
			// ////////////////////////////////////////
			// simone this can be optimized, we need to use System.arraycopy !!!!
			
	 		final Coordinate[] newCoordinateSequence = new Coordinate[numPoints+1];
	 		final Coordinate[] oldCoordinateSequence = line.getCoordinates();
//
//	 		for (int k=0; k<numPoints; k++)
//	 			newCoordinateSequence[k] = line.getPointN(k).getCoordinate();
	 		
	 		// copy over the coordinates
	 		System.arraycopy(oldCoordinateSequence, 0, newCoordinateSequence, 0, numPoints);
	 		newCoordinateSequence[numPoints] = newPoint.getCoordinate();

			final CoordinateSequence sequence = geometryFactory.getCoordinateSequenceFactory().create(newCoordinateSequence);
			final LineString course = new LineString(sequence, geometryFactory);
	 		course.setSRID(4326);
			return course;

		}else{

			// ////////////////////////////////////////////////////////
			// Simplifying the linestring course replacing the end point  
			// with the new point
			// ////////////////////////////////////////////////////////
			// simone, we do not need to do this, we just need to replace the last point
//			
//	 		Coordinate[] coordinate = new Coordinate[numPoints];
//
//	 		for (int l=0; l<numPoints; l++){
//	 			if(l == (numPoints - 1))
//	 				coordinate[l] = newPoint.getCoordinate();
//	 			else
//	 				coordinate[l] = line.getPointN(l).getCoordinate();
//	 		}
//	 		
//			CoordinateSequence sequence = geometryFactory.getCoordinateSequenceFactory().create(coordinate);
//	 		course = new LineString(sequence, geometryFactory);
//	 		course.setSRID(4326);
			
			// replace last point on existing string
			final CoordinateSequence sequence = line.getCoordinateSequence();
			sequence.setOrdinate(numPoints - 1, 0, newPoint.getX());
			sequence.setOrdinate(numPoints - 1, 1, newPoint.getY());
			return line;			
		}
		
	}
	
	public static double CalcBearing(double vx, double vy){

		double bearing = 0;
	
		// ////////////////////
		// determine angle (x, y) instead of (y,x)
		// because the 0 is at north (y axis) and not on the x axis
		// ////////////////////
	
		bearing = Math.atan2(vx, vy); 
	
		// /////////////////////
		// convert to degrees
		// /////////////////////
		
		return toDegree(bearing);
	}
	
	public static double toDegree(double val){ 
		return val * (180 / Math.PI);
	}
	
	/**
	 * Pack the files received in the events into an array.
	 * 
	 *
	 * @param events The received event queue
	 * @return
	 */
	private static File[] handleDataFile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
	}

}
