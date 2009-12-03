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
import it.geosolutions.geobatch.track.dao.ContactTypeDAO;
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
import java.util.Date;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.simplify.DouglasPeuckerLineSimplifier;


/**
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */

public class ContactGeoServerGenerator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	private ContactDAO contactDAO;
	private PastContactPositionDAO pastContactPositionDAO;
	private ContactTypeDAO contactTypeDAO;	

	
	public ContactGeoServerGenerator(GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	public ContactGeoServerGenerator(GeoServerActionConfiguration configuration,
			ContactDAO contactDAO, 
			PastContactPositionDAO pastContactPositionDAO, ContactTypeDAO contactTypeDAO) throws IOException {
		
		super(configuration);
		this.contactDAO = contactDAO;
		this.pastContactPositionDAO = pastContactPositionDAO;
		this.contactTypeDAO = contactTypeDAO;
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
	    	    double cog = 0.0;
	    	    
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

	    	    	PastContactPosition pastContactPosition = new PastContactPosition();
	    	    	pastContactPosition.setCog(cnt.getContactPosition().getCog());
	    	    	pastContactPosition.setContact(cnt);
	    	    	pastContactPosition.setCourse(cnt.getContactPosition().getCourse());
	    	    	pastContactPosition.setPosition(cnt.getContactPosition().getPosition());
	    	    	pastContactPosition.setTime(cnt.getContactPosition().getTime());
	    	    	
	    	    	pastContactPositionDAO.save(pastContactPosition);
	    	    	
	    	    	if(type.compareTo(ContactType.NONESSENTIAL) != 0){
	    	    		cnt.setContactType(type);
	    	    		cnt.setLink(linkCode);
	    	    	}
	    	    	
	    	    	ContactPosition contactPos = cnt.getContactPosition();
	    	    	contactPos.setTime(new Date(timestamp*1000));
	    	    	contactPos.setCog(cog);
	    	    	
			    	GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
	            	WKTReader wkt_reader = new WKTReader(geometryFactory);
	            	
	            	double longitude = Double.parseDouble(fields[1]);
	            	double latitude = Double.parseDouble(fields[2]);
	            	
	            	Point point = (Point)wkt_reader.read("POINT("+ longitude + " " + latitude + " 0)");
	                point.setSRID(4326);   
	                
	    	    	contactPos.setPosition(point);	    	    	
	    	    	
	    	    	LineString lineStr = contactPos.getCourse();
		 			int numPoints = contactPos.getCourse().getNumPoints();					 		
			 		Coordinate[] coordinate = new Coordinate[numPoints+1];
			 		
			 		for (int k=0; k<numPoints; k++)
			 			coordinate[k] = lineStr.getPointN(k).getCoordinate();
			 			
			 		coordinate[numPoints] = new Coordinate (point.getCoordinate());
					
					coordinate = DouglasPeuckerLineSimplifier.simplify(coordinate, 0.0025);
					CoordinateSequence sequence = geometryFactory.getCoordinateSequenceFactory().create(coordinate);
			 		
			 		LineString course = new LineString(sequence, geometryFactory);
			 		course.setSRID(4326);
			 		
	    	    	contactPos.setCourse(course);
	    	    	
	    	    	cnt.setContactPosition(contactPos);	    	    	
	            	
	            	contactDAO.merge(cnt);

	    	    }else{	    	
	    	    	
	    	    	// ///////////////////////////////
		    	    // Setting the ContactPosition
		    	    // ///////////////////////////////
		    	    
		    	    ContactPosition currentContact = new ContactPosition();
		    	    currentContact.setTime(new Date(timestamp*1000));
		    	    currentContact.setCog(cog);
		    	    
			    	GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
	            	WKTReader wkt_reader = new WKTReader(geometryFactory);
	            	
	            	double longitude = Double.parseDouble(fields[1]);
	            	double latitude = Double.parseDouble(fields[2]);
	            	
	            	Point point = (Point)wkt_reader.read("POINT("+ longitude + " " + latitude + " 0)");
	                point.setSRID(4326);   
	                  
		    	    currentContact.setPosition(point);
		    	    
	            	Coordinate[] coordinate = new Coordinate[2];        			
	         		coordinate[0] = new Coordinate (point.getCoordinate());
	         		coordinate[1] = new Coordinate (point.getCoordinate());

	            	coordinate = DouglasPeuckerLineSimplifier.simplify(coordinate, 0.0025);
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
			
			return events; 
			
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		} 
	}
	
	/**
	 * Pack the files received in the events into an array.
	 * 
	 *
	 * @param events The received event queue
	 * @return
	 */
	private File[] handleDataFile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
	}
}
