/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.nc;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.database.DataBaseActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.database.DataBaseConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.io.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.postgresql.Driver;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


/**
 * 
 * Public class to insert NetCDF data file (gliders measurements) into DB 
 *  
 */
public class NetCDFFileConfigurator extends
        DataBaseConfiguratorAction<FileSystemMonitorEvent>{
	
	
	// //////////////////////////
	// GT-PostGIS fields 
	// //////////////////////////
	
	private static PostgisDataStoreFactory factory = new PostgisDataStoreFactory();    
    private static DataStore pgDataStore = null;
    
	// //////////////////////////
	// JDBC data fields  
	// //////////////////////////
    
    private Connection conTarget = null;
    private boolean isConnected = false;   
    

    protected NetCDFFileConfigurator(DataBaseActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {

        try {
        	
        	// ///////////////////////////////////
            // Initializing input variables
            // ///////////////////////////////////
        	
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "ActionConfig is null.");
                throw new IllegalStateException("ActionConfig is null.");
            }

            // ///////////////////////////////////
            // Initializing input variables
            // ///////////////////////////////////
            
            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ///////////////////////////////////
            // Checking input files.
            // ///////////////////////////////////
            
            if (workingDir == null) {
                LOGGER.log(Level.SEVERE, "Working directory is null.");
                throw new IllegalStateException("Working directory is null.");
            }

            if ( !workingDir.exists() || !workingDir.isDirectory()) {
                LOGGER.log(Level.SEVERE, "Working directory does not exist ("+workingDir.getAbsolutePath()+").");
                throw new IllegalStateException("Working directory does not exist ("+workingDir.getAbsolutePath()+").");
            }

			File[] netcdfList;
			netcdfList = handleNetCDFfile(events);

			if(netcdfList == null)
				throw new Exception("Error while processing the netcdf file set");
			
			// /////////////////////////////////////////////
			// Look for the main netcdf file in the set
			// /////////////////////////////////////////////
			
			File netcdfFile = null;
			for (File file : netcdfList) {
				if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("nc")) {
					netcdfFile = file;
					break;
				}
			}

			if(netcdfFile == null) {
                LOGGER.log(Level.SEVERE, "netcdf file not found in fileset.");
                throw new IllegalStateException("netcdf file not found in fileset.");
			}

	        	
        	// //////////////////////////////////////
        	// Inserting data into OBSERVATON table
        	// //////////////////////////////////////
        	
        	insertHeaderData(netcdfFile);
        	
        	// ////////////////////////////////////////////////////////////
        	// Inserting data in MEASUREMENT and MEASUREMENT_VALUES tables 
        	// and closing JDBC connection.
        	// ////////////////////////////////////////////////////////////
        	
            initJDBCConnection();
            
	        if (isConnected()) {
                insertData(netcdfFile);
                closeConnections();
	        }

        	return events;        	
        	
        } catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        } finally {
			cleanup();
		}
    }

	private void cleanup() {
		try{
	        if (isConnected) {
	        	if(conTarget != null && !conTarget.isClosed()){
	                conTarget.close();
	                conTarget = null;                
	        	}
	        	
	        	isConnected = false;
	        }
		}catch(SQLException e){
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
		}  
	}

	/**
	 * Pack the files received in the events into an array.
	 * 
	 *
	 * @param events The received event queue
	 * @return
	 */
	private File[] handleNetCDFfile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
	}
	
	/**
	 * This function initialize the params HashMap for PostGIS Data Store
	 * 
	 */
    private void PGDataStoreConfig()throws IOException{
    	
    	try{
	    	Map<String, Comparable> params = new HashMap<String, Comparable>();
			params.put("dbtype", this.getConfiguration().getDbType());        //must be postgis
			params.put("charset", "");
			params.put("host", this.getConfiguration().getDbServerIp());      //the name or ip address of the machine running PostGIS
			params.put("port", this.getConfiguration().getDbPort());          //the port that PostGIS is running on (generally 5432)
			params.put("database", this.getConfiguration().getDbName());      //the name of the database to connect to.
			params.put("user", this.getConfiguration().getDbUID());           //the user to connect with
			params.put("passwd", this.getConfiguration().getDbPWD());         //the password of the user.
			
			pgDataStore = factory.createDataStore(params);			
			
    	}catch(IOException exc){
    		throw new IOException("EXCEPTION -> " + exc.getLocalizedMessage());
    	}  
    }
    
	/**
	 * Utility function to read variables from NetCDF file
	 * 
	 *  @param dataset The NetCDF dataset 
	 *  @param name The variable name to read 
	 * 
	 * 	@return array
	 */
    private static Array readVariables(NetcdfDataset dataset, String name)throws IOException{
    	Array array = null;
    	
    	try{
        	Variable v = dataset.findVariable(name);
        	
        	if(v != null)
        		array = v.read(); 
        	
        	return array;
        	
    	}catch(IOException exc){
    		throw new IOException("EXCEPTION -> " + exc.getLocalizedMessage());
    	}    	
    }
    
    /**
     * @throws SQLException 
     * 
     */
    private void closeConnections() throws SQLException {
        if (isConnected) {
        	if(conTarget != null && !conTarget.isClosed()){
        		conTarget.commit();
                conTarget.close();
                conTarget = null;                
        	}
        	
        	isConnected = false;
        }
    }

    /**
     * @throws SQLException 
     * 
     */
    private void initJDBCConnection() throws SQLException {
        DriverManager.registerDriver(new Driver());
        
        // /////////////////////////////
        // Connecting to the DataBase
        // /////////////////////////////

        StringBuffer conString = new StringBuffer("jdbc:postgresql://");
        conString.append(this.getConfiguration().getDbServerIp()).append(":").append(this.getConfiguration()
        		.getDbPort()).append("/").append(this.getConfiguration().getDbName());
        conTarget = DriverManager.getConnection(conString.toString(), this.getConfiguration()
        		.getDbUID(), this.getConfiguration().getDbPWD());

        isConnected = true;
    }

    /**
     * @return the isConnected
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Insert method for the data into OBSERVATION table (the header) 
     * 
     * @params input The NetCDF file
     * 
     */
    private void insertHeaderData(File input)throws IOException, ParseException{
    	
		// ///////////////////////////////////////////
		// Managing GT-PostGIS Connection 
		// ///////////////////////////////////////////
    	
    	Transaction transaction = null;
    	FeatureWriter<SimpleFeatureType, SimpleFeature> aWriter = null;
    	
    	try{
    		PGDataStoreConfig();
    		
    		NetcdfDataset dataset = NetcdfDataset.openDataset((input).getPath());   	        	 
        	
        	Array pTime, depth, lonValues, latValues;	        	
        	
        	pTime = readVariables(dataset, "ptime");
        	
            if(pTime == null)throw new IOException();
            else{
//            	depth = readVariables(dataset, "depth");
            	lonValues = readVariables(dataset, "lon");
            	latValues = readVariables(dataset, "lat");
            	
            	if(lonValues == null || latValues == null)throw new IOException();
            	else{
                	Attribute platform_code = dataset.findGlobalAttribute("platform_code");
                	
                	Long size = pTime.getSize();
                	
                	Point[] positions = new Point[size.intValue()];
                	GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
                	
                	for(int k=0; k<positions.length; k++){     		
                		
                		WKTReader reader = new WKTReader( geometryFactory );
                		Point point = (Point) reader.read("POINT("+ lonValues.getDouble(lonValues.getIndex().set(k)) + " " + latValues.getDouble(latValues.getIndex().set(k)) + " 0)");
                		point.setSRID(4326);     		
                		
                		positions[k] = point;
                	} 
                	
                	SimpleFeatureType newFT = pgDataStore.getSchema(this.getConfiguration().getDbTableName());
                	
                	transaction = new DefaultTransaction(this.getConfiguration().getDbTableName());
                	aWriter = pgDataStore.getFeatureWriterAppend(newFT.getTypeName(),transaction);
                	
                	for(int l=0; l<size.intValue(); l++){
                		SimpleFeature aNewFeature = (SimpleFeature)aWriter.next();	
        	    		
        	    		aNewFeature.setAttribute("ship_id", 1);
        	    		aNewFeature.setAttribute("type_id", 2);
        	    		aNewFeature.setAttribute("cruise_id", 157);
        	    		aNewFeature.setAttribute("sens_id", 0);
        	    		
        	    		if(platform_code != null)
        	    			aNewFeature.setAttribute("ext_name", platform_code.getStringValue());
        	    		else
        	    			aNewFeature.setAttribute("ext_name", "Not Available");
        	    		
        	        	Date date = new Date(pTime.getLong(pTime.getIndex().set(l))*1000);
        	        	Time time = new Time(pTime.getLong(pTime.getIndex().set(l))*1000);	        	
        	        	
        	    		aNewFeature.setAttribute("obs_date", date);	    		
        	    		aNewFeature.setAttribute("obs_time", time.toString());
        	    		aNewFeature.setAttribute("lat", latValues.getDouble(latValues.getIndex().set(l)));	    		
        	    		aNewFeature.setAttribute("lon", lonValues.getDouble(lonValues.getIndex().set(l)));
        	    		aNewFeature.setAttribute("the_geom", positions[l]);
        	    		
        	    		aWriter.write();
                	}
                	
                	aWriter.close();
                	
                    transaction.commit();
                    transaction.close();
                	pgDataStore.dispose();	
            	}
            }
	
    	}catch(IOException exc){
    		throw new IOException("EXCEPTION -> " + exc.getLocalizedMessage());
    	}catch(ParseException exc){
    		throw new ParseException("EXCEPTION -> " + exc.getLocalizedMessage());
    	}	
    }
    
    /**
     * Insert function to insert the data into MEASUREMENT and MEASUREMENT_VALUES tables
     * 
     * @params input The NetCDF file
     * @throws SQLException 
     * 
     */
    private void insertData(File input) throws SQLException {
    	
		// ///////////////////////////////////////////
		// Managing JDBC-PostGIS Connection 
		// ///////////////////////////////////////////
    	
    	PreparedStatement stat = null;        	
	    ResultSet rs_glider_ms = null;
	    
        try{
        	NetcdfDataset dataset = NetcdfDataset.openDataset((input).getPath());   	        	 

        	Array pTime, dist, pitch, inflecting, numHalfYolnSegment, cond, temperature, press, irrad412nm, irrad442nm, irrad491nm, irrad664nm, backscatterBlue,
        	backscatterGreen, backscatterRed, depth, cndr, salin, densi, pTemp, pDens, svel, prfl;	        	

        	pTime = readVariables(dataset, "ptime");
        	dist = readVariables(dataset, "dist");
        	pitch = readVariables(dataset, "pitch");
        	inflecting = readVariables(dataset, "inflecting");
        	numHalfYolnSegment = readVariables(dataset, "numHalfYosInSegment");
        	cond = readVariables(dataset, "cond");
        	temperature = readVariables(dataset, "temp");
        	press = readVariables(dataset, "press");
        	irrad412nm = readVariables(dataset, "irrad412nm");
        	irrad442nm = readVariables(dataset, "irrad442nm");
        	irrad491nm = readVariables(dataset, "irrad491nm");
        	irrad664nm = readVariables(dataset, "irrad664nm");
        	backscatterBlue = readVariables(dataset, "backscatterBlue");
        	backscatterGreen = readVariables(dataset, "backscatterGreen");
        	backscatterRed = readVariables(dataset, "backscatterRed");
        	depth = readVariables(dataset, "depth");
        	cndr = readVariables(dataset, "cndr");
        	salin = readVariables(dataset, "salin");
        	densi = readVariables(dataset, "densi");
        	pTemp = readVariables(dataset, "pTemp");
        	pDens = readVariables(dataset, "pDens");
        	svel = readVariables(dataset, "svel");
        	prfl = readVariables(dataset, "prfl");       	    	

            conTarget.setAutoCommit(false);      
            
            if(pTime == null)throw new Exception();
            else{
            	int obs_id_min = 0;
            	int obs_id_max = 0;
            	
//              	String sqlString = "SELECT MIN(obs_id) FROM observation";
//              	stat = conTarget.prepareStatement(sqlString);
//              	rs_glider_ms = stat.executeQuery();
//                if(rs_glider_ms.next())	obs_id_min = rs_glider_ms.getInt(1);
//                rs_glider_ms.close();
//                stat.close();       	

            	String sqlString = "SELECT MAX(obs_id) FROM observation";
              	stat = conTarget.prepareStatement(sqlString);
              	rs_glider_ms = stat.executeQuery();
                if(rs_glider_ms.next())	obs_id_max = rs_glider_ms.getInt(1);
                rs_glider_ms.close();
                stat.close();

                Long size = pTime.getSize();
                
                obs_id_min = obs_id_max - size.intValue();
                obs_id_min++;
                
    	    	for(int i=obs_id_min, j=0; i<=obs_id_max && j<size; j++, i++){	     	
    	    		StringBuffer zPos = new StringBuffer();	     	    		
    	    			 
    	    		Double depth_value = new Double(depth == null ? Double.NaN : depth.getDouble(depth.getIndex().set(j)));
//    	    		Double depth_value = new Double(depth.getDouble(depth.getIndex().set(j)));
    	    		
    	    		if(depth_value.isNaN())
    	    			zPos.append(0.0);
    	    		else
    	    			zPos.append(depth.getDouble(depth.getIndex().set(j)));
    	    		       	
    	        	
    	        	Date date = new Date(pTime.getLong(pTime.getIndex().set(j))*1000);
    	        	
    	          	sqlString = "insert into measurement(zpos,tpos,obs_id,depth) values(ARRAY[" + zPos.toString() + "],?,?,?)";
    	          	stat = conTarget.prepareStatement(sqlString);
    	          	stat.setTimestamp(1, new Timestamp(date.getTime()));
    	          	stat.setLong(2, i);
    	          	
    	          	if(depth_value.isNaN())	
    	          		stat.setDouble(3, 0.0);
    	          	else
    	          		stat.setDouble(3, depth_value.doubleValue()*-1);
    	          	
    	          	stat.execute();  
    	          	stat.close();
            	}	
    	    	
            	int measurement_id_min = 0;
            	int measurement_id_max = 0;
    	    	
//              	sqlString = "SELECT MIN(measurement_id) FROM measurement";
//              	stat = conTarget.prepareStatement(sqlString);
//                  rs_glider_ms = stat.executeQuery();
//                if(rs_glider_ms.next())	measurement_id_min = rs_glider_ms.getInt(1);
//                rs_glider_ms.close();
//                stat.close();
                
              	sqlString = "SELECT MAX(measurement_id) FROM measurement";
              	stat = conTarget.prepareStatement(sqlString);
              	rs_glider_ms = stat.executeQuery();
                if(rs_glider_ms.next())	measurement_id_max = rs_glider_ms.getInt(1);
                rs_glider_ms.close();
                stat.close();	          
                
                measurement_id_min = measurement_id_max - size.intValue();
                measurement_id_min++;
                
                final Integer[] param_id = {20,21,22,23,3,2,1,13,14,15,16,17,18,19,12,4,6,24,11,5,25};
                
        		for(int y=measurement_id_min, h=0; y<=measurement_id_max; y++, h++){    			
        			
    	    		final Double[] mValues = {
    	    				dist == null ? Double.NaN : dist.getDouble(dist.getIndex().set(h)),	    				
    	    				pitch == null ? Double.NaN : pitch.getDouble(pitch.getIndex().set(h)),	    				
    	    				inflecting == null ? Double.NaN : inflecting.getDouble(inflecting.getIndex().set(h)),
    	    				numHalfYolnSegment == null ? Double.NaN : numHalfYolnSegment.getDouble(numHalfYolnSegment.getIndex().set(h)),
    	    				cond == null ? Double.NaN : cond.getDouble(cond.getIndex().set(h))*100000,
    	    				temperature == null ? Double.NaN : temperature.getDouble(temperature.getIndex().set(h)),
    	    				press == null ? Double.NaN : press.getDouble(press.getIndex().set(h)),
    	    				irrad412nm == null ? Double.NaN : irrad412nm.getDouble(irrad412nm.getIndex().set(h)),
    	    				irrad442nm == null ? Double.NaN : irrad442nm.getDouble(irrad442nm.getIndex().set(h)),
    	    				irrad491nm == null ? Double.NaN : irrad491nm.getDouble(irrad491nm.getIndex().set(h)),
    	    				irrad664nm == null ? Double.NaN : irrad664nm.getDouble(irrad664nm.getIndex().set(h)),
    	    				backscatterBlue == null ? Double.NaN : backscatterBlue.getDouble(backscatterBlue.getIndex().set(h)),
    	    				backscatterGreen == null ? Double.NaN : backscatterGreen.getDouble(backscatterGreen.getIndex().set(h)),
    	    				backscatterRed == null ? Double.NaN : backscatterRed.getDouble(backscatterRed.getIndex().set(h)),
    	    				cndr == null ? Double.NaN : cndr.getDouble(cndr.getIndex().set(h)),
    	    				salin == null ? Double.NaN : salin.getDouble(salin.getIndex().set(h)),
    	    				densi == null ? Double.NaN : densi.getDouble(densi.getIndex().set(h)),
    	    				pTemp == null ? Double.NaN : pTemp.getDouble(pTemp.getIndex().set(h)),
    	    				pDens == null ? Double.NaN : pDens.getDouble(pDens.getIndex().set(h)),
    	    				svel == null ? Double.NaN : svel.getDouble(svel.getIndex().set(h)),
    	    				prfl == null ? Double.NaN : prfl.getDouble(prfl.getIndex().set(h))	    				
    	    		};
        		    
        			for(int k=0; k<mValues.length; k++){         				
        	            sqlString = "insert into measurement_values(measurement_id,values,param_id) values(" + y + ", ARRAY['" + mValues[k].doubleValue() + "']," + param_id[k].longValue() + ")";
        	            
        	          	stat = conTarget.prepareStatement(sqlString);
        	          	stat.execute();
        	          	stat.close();
        			}
        		}
            }

		}catch(Exception exc){
			throw new SQLException("EXCEPTION -> " + exc.getLocalizedMessage());
    	}finally{
	        try{
	        	if(rs_glider_ms != null){
	        		rs_glider_ms.close();
	        		rs_glider_ms = null;
        		}
        		
        		if(stat != null){
        			stat.close();
        			stat = null;
        		}
        		
            }catch(SQLException exc){
        		throw new SQLException("EXCEPTION -> " + exc.getLocalizedMessage());
            }
	 	}
    }
}

