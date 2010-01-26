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
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.track.configuration.FusedTrackActionConfiguration;
import it.geosolutions.geobatch.track.configuration.FusedTrackConfiguratorAction;
import it.geosolutions.geobatch.track.dao.ContactDAO;
import it.geosolutions.geobatch.track.dao.PastContactPositionDAO;
import it.geosolutions.geobatch.track.datastore.Postgis;
import it.geosolutions.geobatch.track.model.Contact;
import it.geosolutions.geobatch.track.model.ContactPosition;
import it.geosolutions.geobatch.track.model.ContactType;
import it.geosolutions.geobatch.track.model.PastContactPosition;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;


/**
 * This class extends FusedTrackConfiguratorAction to
 * allow the fused-track and ais ingestion in to PostGIS database from .txt file using 
 * Hibernate, Hibernate Saptial and EhCache.
 *  
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */
public class FusedTrackGenerator extends
	FusedTrackConfiguratorAction<FileSystemMonitorEvent>{

	private ContactDAO contactDAO;
	
	private PastContactPositionDAO pastContactPositionDAO;

    private Postgis postgisDataStore;
	
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);	
	
	
	
	public FusedTrackGenerator(FusedTrackActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	public FusedTrackGenerator(FusedTrackActionConfiguration configuration,
			ContactDAO contactDAO, PastContactPositionDAO pastContactPositionDAO,
			Postgis postgisDataStore) throws IOException {
		
		super(configuration);
		this.contactDAO = contactDAO;
		this.pastContactPositionDAO = pastContactPositionDAO;
		this.postgisDataStore = postgisDataStore;
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
				LOGGER.log(Level.SEVERE, "ActionConfig is null!");
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
			
			// ////////////////////////////////
			// Getting the received .txt file 
			// ////////////////////////////////
			
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
			
			// //////////////////////////////////////////////////////
			//	Switch the ingestion type in baste to configuration 
			//  log type parameter.
			// //////////////////////////////////////////////////////
			
			if(this.configuration.getLogType().indexOf("fused") != -1)
				ingestFusedTrackFile(dataFile);
			else if(this.configuration.getLogType().indexOf("ais") != -1)
				ingestAisFile(dataFile);
			
			return events; 
			
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		} 
	}
	
	/**
	 * This function parse the ais log file to store the data in to database.
	 * 
	 *
	 * @param dataFile the data file received.
	 * 
	 * @return void
	 */	
	private void ingestAisFile(final File dataFile)throws Exception{
		
		// ///////////////
	    // Open the file
		// ///////////////
		
		FileInputStream fstream = new FileInputStream(dataFile);
	    
		// ///////////////////////////////////
	    // Get the object of DataInputStream
		// ///////////////////////////////////
	    
		DataInputStream dataInputStream = new DataInputStream(fstream);
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader(dataInputStream));
	    
	    String line = reader.readLine();
	    
	    // ///////////////////////
	    // Read File Line By Line
	    // ///////////////////////
	    while(line != null) {				      

	       if(line.indexOf("kinematic") != -1){
  	    	    line = line.replaceAll("kinematic", "");
	    	    line = line.replaceAll("\t", "&");
	    	    line = line.replaceAll("\n", "&"); 

	    	    String[] fields = line.split("&");   
	    	   
	    	    // ////////////////////////////////
	    	    // Setting the fields to ingest
	    	    // ////////////////////////////////
	    	    
	    	    long contactId = Long.parseLong(fields[2]);
	    	    long linkCode = Long.parseLong(fields[2]);
	    	    double cog = Double.parseDouble(fields[7]);
	    	    long timestamp = Long.parseLong(fields[1]);	   
		    	double longitude = Double.parseDouble(fields[5]);
		    	double latitude = Double.parseDouble(fields[6]);
	    	    
	    	    ContactType type = ContactType.MMSI;
	    	    
	        	// //////////////////
	        	// Insert the data 
	        	// //////////////////

	    	    insertData(contactId, linkCode, cog, type, longitude, latitude, timestamp);
	       } 
	      
	       line = reader.readLine();
	    } 	    
	    
    	// ////////////////////////
	    // Close the input stream
    	// ////////////////////////	    
	    
	    dataInputStream.close();
	}
	
	/**
	 * This function parse the fused track log file to store the data in to database.
	 * 
	 *
	 * @param dataFile the data file received.
	 * 
	 * @return void
	 */	
	private void ingestFusedTrackFile(final File dataFile)throws Exception{

		// ///////////////
	    // Open the file
		// ///////////////
		
		FileInputStream fstream = new FileInputStream(dataFile);
	    
		// ///////////////////////////////////
	    // Get the object of DataInputStream
		// ///////////////////////////////////
	    
		DataInputStream dataInputStream = new DataInputStream(fstream);
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader(dataInputStream));
	    
	    String line = reader.readLine();
	    
	    // ///////////////////////
	    // Read File Line By Line
	    // ///////////////////////
	    
	    if (line == null){	   
	    	
	    	// ////////////////////////
		    // Close the input stream
	    	// ////////////////////////	   
	    	
	    	dataInputStream.close();	   
	    	
	    	throw new Exception();	    	
	    }else{
			long timestamp = Long.parseLong(line);		
			line = reader.readLine();
		
		    while(line != null) {	
		    	if(line.indexOf(" ") != -1)
		    		line = line.replaceAll(" ", "&"); 
		    	
		    	String[] fields = null;
	    	    if(line.indexOf("&") != -1){
	    		    fields = line.split("&");
	    	    }
	    	    
	    	    // ////////////////////////////////
	    	    // Setting the fields to ingest
	    	    // ////////////////////////////////
	    	    
	    	    long contactId = Long.parseLong(fields[0]);
	    	    long linkCode = Long.parseLong(fields[6]);
	    	    double velocyX = Double.parseDouble(fields[3]);
	    	    double velocyY = Double.parseDouble(fields[4]);
	    	    double cog = CalcCOG(velocyX, velocyY);
	    	    
	    	    // ///////////////////////////
	    	    // Setting the ContactType
	    	    // ///////////////////////////
	    	    
	    	    ContactType type = ContactType.NONESSENTIAL;
	    	    if(fields[5].indexOf("9999") != -1)
	    	    	type = ContactType.MMSI;
	    	    else if(fields[5].indexOf("3333") != -1)
	    	    	type = ContactType.RADAR;	    	    
	    	    
	        	double longitude = Double.parseDouble(fields[1]);
	        	double latitude = Double.parseDouble(fields[2]);
	        	
	        	// //////////////////
	        	// Insert the data 
	        	// //////////////////

	        	insertData(contactId, linkCode, cog, type, longitude, latitude, timestamp);

		        line = reader.readLine();
		    }  	  
		    
	    	// ////////////////////////
		    // Close the input stream
	    	// ////////////////////////	    
		    
		    dataInputStream.close();
	    }
	}
	
	/**
	 * This function insert the log data in to database using Hibernate.
	 * 
	 *
	 * @param contactId the contact id to insert
	 * @param linkCode the link code (MMSI or RADAR)
	 * @param cog the course over ground
	 * @param type the type of the contact
	 * @param longitude the longitude of the new point received
	 * @param latitude  the latitude of the new point received
	 * @param timestamp the timestamp of the new contact
	 * 
	 * @return void
	 */
	private void insertData(final long contactId, final long linkCode, 
			final double cog, final ContactType type, final double longitude, 
			final double latitude, final long timestamp)throws Exception{
		
		// ///////////////////////////////
    	// Checking if contact is exist 
    	// ///////////////////////////////
		
	    Contact cnt = contactDAO.findIsExist(contactId);

	    if(cnt != null){	    

	    	PastContactPosition pastContactPosition = new PastContactPosition();
	    	pastContactPosition.setCog(cnt.getContactPosition().getCog());
	    	pastContactPosition.setCourse(cnt.getContactPosition().getCourse());
	    	pastContactPosition.setPosition(cnt.getContactPosition().getPosition());
	    	pastContactPosition.setTime(cnt.getContactPosition().getTime());
	    	pastContactPosition.setContact(cnt);
	    	
            // /////////////////////////
            // Shard PastContactPosition
            // /////////////////////////
	    	
            DataStore dataStore = null;
            Transaction transaction = new DefaultTransaction("create");
            FeatureIterator<SimpleFeature> iterator = null;
            
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = null;            
            FeatureWriter<SimpleFeatureType, SimpleFeature> aWriter = null;
            
            try {
                dataStore = (DataStore)DataStoreFinder.getDataStore(this.postgisDataStore.getParams());
                
                FeatureSource<SimpleFeatureType, SimpleFeature> fs = 
                	dataStore.getFeatureSource(this.postgisDataStore.getShardingSystemTable());

                Point point = cnt.getContactPosition().getPosition();                
                Filter shardingFilter = CQL.toFilter("CONTAINS(the_geom, POINT("
                        + point.getX() + " " + point.getY() + "))");

                features = fs.getFeatures(shardingFilter);
                
                iterator = features.features();

                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();

                	SimpleFeatureType newFT = dataStore.getSchema(feature.getAttribute("shard_link").toString());

                	aWriter = dataStore.getFeatureWriterAppend(newFT.getTypeName(),transaction);
                	
            		SimpleFeature aNewFeature = (SimpleFeature)aWriter.next();	           		
            		
            		//point = geometryFactory.createPoint(new Coordinate(point.getX(),
            		//		point.getY()));

                    aNewFeature.setAttribute("cog", cnt.getContactPosition().getCog());
                    aNewFeature.setAttribute("the_geom", cnt.getContactPosition().getCourse());
                    aNewFeature.setAttribute("position", point);
                    aNewFeature.setAttribute("time", cnt.getContactPosition().getTime());
                    aNewFeature.setAttribute("contact_id", cnt.getContactId());

    	    		aWriter.write();  
    	    		aWriter.close();
    	    		aWriter = null;
                }                    	
            	
                transaction.commit();
                
            } catch (IOException e) {
                transaction.rollback();
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            } catch (CQLException e) {
            	LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            } finally {
                transaction.close();	
                features.close(iterator);
//                iterator.close(); // IMPORTANT                
                if(aWriter != null )aWriter.close();
                dataStore.dispose();
                dataStore = null;
            }
            
	    	pastContactPositionDAO.save(pastContactPosition);
	    	
	    	Date now = new Date();
	    	long timeMillis = (timestamp*1000) + (now.getTime() - timestamp*1000);
	    	Date contactTimestamp = new Date(timeMillis);
	    	

	    	
	    	// ///////////////////////////////////////////////////////////
	    	// Find the past contact positions received in the last time 
	    	// ///////////////////////////////////////////////////////////
            
	    	
//	    	////////////////////////////////////////////////////////////////
//	    	
//	    	List<PastContactPosition> pastContacts = new ArrayList<PastContactPosition>();
//	    	Transaction view_transaction = new DefaultTransaction("view_transaction");
//	    	FeatureReader<?, SimpleFeature> aReader = null;
//	    	
//            try {
//            	dataStore = (DataStore)DataStoreFinder.getDataStore(this.postgisDataStore.getParams());
//            	
//            	Filter filter = CQL.toFilter("contact_id=" + cnt.getContactId());
//    			Query query = new DefaultQuery("history", filter);
//    	        aReader = dataStore.getFeatureReader(query, view_transaction);
//    	        
//    	        for(;aReader.hasNext();){
//    	        	 SimpleFeature feature = aReader.next();
//
//                  	 PastContactPosition pcp = new PastContactPosition();
//                	 pcp.setCog(((Double)feature.getAttribute("cog")).doubleValue());
//                	 pcp.setPosition((Point)feature.getDefaultGeometry());
//                	 
//                	 pastContacts.add(pcp);
//    	        }
//    	        
//            }catch (IOException e) {
//            	view_transaction.rollback();
//            	LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
//            } catch (CQLException e) {
//            	LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
//            } finally {
//            	view_transaction.close();
//            	aReader.close();
//                dataStore.dispose();
//                dataStore = null;
//            }
//
//	    	////////////////////////////////////////////////////////////////
            
	    	List<PastContactPosition> pastContacts = pastContactPositionDAO.findByPeriod(timeMillis, 
	    			this.configuration.getStepTimeSecond(), cnt.getContactId());
	    	
			if(this.configuration.getLogType().indexOf("fused") != -1){
		    	if(type.compareTo(ContactType.NONESSENTIAL) != 0){
		    		cnt.setContactType(type);
		    		cnt.setLink(linkCode);
		    	}
			}

	    	ContactPosition contactPos = cnt.getContactPosition();
	    	contactPos.setTime(contactTimestamp);

	    	WKTReader wkt_reader = new WKTReader(geometryFactory);
	    	
        	Point point = (Point)wkt_reader.read("POINT("+ longitude + " " + latitude + " 0)");
            point.setSRID(4326);   
            
	    	contactPos.setPosition(point);	 

	    	LineString lineSimplified = null;
	    	if(pastContacts != null && pastContacts.size() > 2){
	    		lineSimplified = buildHistoryLineString(pastContacts, point, cog,
	    				this.configuration.getCogDegreeThreshold());
	    	}else{
    	    	lineSimplified = lineSimplifier(contactPos.getCourse(), point, 
    	    			contactPos.getCog(), cog, this.configuration.getCogDegreeThreshold()); 
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
    	    
	    	Date now = new Date();
	    	long timeMillis = (timestamp*1000) + (now.getTime() - timestamp*1000);
	    	Date contactTimestamp = new Date(timeMillis);
    	    
    	    currentContact.setTime(contactTimestamp);
    	    currentContact.setCog(cog);
        	
    	    WKTReader wkt_reader = new WKTReader(geometryFactory);
        	
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
	}

	/**
	 * This function build the ship course from the received points in the last time.
	 * 
	 *
	 * @param pastContacts the list of the PastContactPositions in the last time.
	 * @param newPoint the new point received to add.
	 * @param newCog the course over ground of the last contact
	 * 
	 * @return course the temporal LineString of the ship course
	 */
	private LineString buildHistoryLineString(
			final List<PastContactPosition> pastContacts,
			final Point newPoint, 
			final double newCog, final double cogDegreeThreshold){

		// /////////////////////////////////
		// create the new coordinate list
		// /////////////////////////////////
		
		final List<Coordinate> coordinateList = new ArrayList<Coordinate>();
		
		// ////////////////////////////////////
		// simplify the points as we add them
		// ////////////////////////////////////
		
		double oldCog = 0.0;
		
		// /////////////////////////////////////////
		// parse the past contacts plus the new one
		// /////////////////////////////////////////
		
		final int size=pastContacts.size();
		for(int i = 0; i<size+1;i++){
			
			if(i<size){
				
				// /////////////////////////////////
				// current past contact
				// /////////////////////////////////
				
				final PastContactPosition contact=pastContacts.get(i);
				
				if(i < 2){
					coordinateList.add(contact.getPosition().getCoordinate());
					oldCog = contact.getCog();
				}else{
					final double mCheck = Math.abs(contact.getCog() - oldCog);
					
					// /////////////////////////////////
					// check for exclusion based on COG
					// /////////////////////////////////
					
					if(mCheck > cogDegreeThreshold){	
						coordinateList.add(contact.getPosition().getCoordinate());
						oldCog = contact.getCog();
					}
				}
			}else{
				// /////////////////
				// new point
				// /////////////////
				
				final double mCheck = Math.abs(newCog - oldCog);
				
				// /////////////////////////////////
				// check for exclusion based on COG
				// /////////////////////////////////
				
				if(mCheck > cogDegreeThreshold){	
					coordinateList.add(newPoint.getCoordinate());
					oldCog = newCog;
				}				
			}
		}
		
		// ////////////////////////////////////////////////
		// create a sequence with the current coordinates
		// ////////////////////////////////////////////////
		
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
	
	/**
	 * This function add or not to a LineString a new point in base to the difference 
	 * between the LineString end point COG and new point COG. 
	 * 
	 *
	 * @param line the actual contact history LineString 
	 * @param newPoint the new point to add
	 * @param oldCog the COG of the end point of teh history LineString
	 * @param newCog the COG of the last received contact.
	 * 
	 * @return course the LineString simplified.
	 */	
	private LineString lineSimplifier(final LineString line, final Point newPoint, 
			final double oldCog, final double newCog, final double cogDegreeThreshold){

		int numPoints = line.getNumPoints();
		double mCheck = Math.abs(newCog - oldCog);

		if(mCheck > cogDegreeThreshold){		

			// ////////////////////////////////////////
			// Adding a new point to linestring course 
			// ////////////////////////////////////////
			
	 		final Coordinate[] newCoordinateSequence = new Coordinate[numPoints+1];
	 		final Coordinate[] oldCoordinateSequence = line.getCoordinates();
	 		
	 		// /////////////////////////////
	 		// copy over the coordinates
	 		// /////////////////////////////
	 		System.arraycopy(oldCoordinateSequence, 0, newCoordinateSequence, 0, numPoints);
	 		newCoordinateSequence[numPoints] = newPoint.getCoordinate();

			final CoordinateSequence sequence = geometryFactory.getCoordinateSequenceFactory().create(newCoordinateSequence);
			final LineString course = new LineString(sequence, geometryFactory);
	 		course.setSRID(4326);
			return course;

		}else{

			// ////////////////////////////////////////////////////////
			// Simplifying the linestring course 
			// replacing the last point on existing string
			// ////////////////////////////////////////////////////////

			final CoordinateSequence sequence = line.getCoordinateSequence();
			sequence.setOrdinate(numPoints - 1, 0, newPoint.getX());
			sequence.setOrdinate(numPoints - 1, 1, newPoint.getY());
			return line;			
		}		
	}
	
	/**
	 * Calculate the course over ground (COG) from XSpeed and YSpeed parameters.
	 * 
	 *
	 * @param vx xspeed
	 * @param vy yspeed
	 * 
	 * @return cog the course over ground
	 */	
	public static double CalcCOG(double vx, double vy){

		double cog = 0;
	
		// ////////////////////
		// determine angle (x, y) instead of (y,x)
		// because the 0 is at north (y axis) and not on the x axis
		// ////////////////////
	
		cog = Math.atan2(vx, vy); 
	
		// /////////////////////
		// convert to degrees
		// /////////////////////
		
		return toDegree(cog);
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
	private File[] handleDataFile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
	}

}
