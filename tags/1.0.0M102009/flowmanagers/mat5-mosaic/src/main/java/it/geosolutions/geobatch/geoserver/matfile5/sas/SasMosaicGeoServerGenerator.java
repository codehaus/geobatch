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

package it.geosolutions.geobatch.geoserver.matfile5.sas;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.base.Utils;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.io.utils.IOUtils;
import it.geosolutions.geobatch.mosaic.Mosaicer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicReader;

/**
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * 
 */
public class SasMosaicGeoServerGenerator
		extends GeoServerConfiguratorAction<FileSystemMonitorEvent> {

    public final static String SAS_STYLE = "sas";
    
    public final static String SAS_RAW_STYLE = "sasraw";
    
    public final static String DEFAULT_STYLE = "raster";
    
    public final static String GEOSERVER_VERSION = "1.7.X";
       
    public SasMosaicGeoServerGenerator(GeoServerActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {

            // looking for file
//            if (events.size() != 1)
//                throw new IllegalArgumentException("Wrong number of elements for this action: "
//                        + events.size());
//            FileSystemMonitorEvent event = events.remove();
            
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            
            final String configId = configuration.getName();
            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            final String dataType = configuration.getDatatype();
            
            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if ((workingDir == null) || !workingDir.exists() || 
                    (!workingDir.isDirectory()&&dataType.equalsIgnoreCase("imagemosaic"))) {
                throw new IllegalStateException("GeoServerDataDirectory is null or does not exist.");
            }
            
            final String inputFileName = workingDir.getAbsolutePath();
            String baseFileName = null;
            final String coverageStoreId = FilenameUtils.getBaseName(inputFileName);

            if (dataType.equalsIgnoreCase("imagemosaic")){
                final ImageMosaicFormat format = new ImageMosaicFormat();
                ImageMosaicReader coverageReader = null;
    
                // //
                // Trying to read the mosaic
                // //
                try {
                    coverageReader = (ImageMosaicReader) format.getReader(workingDir);
    
                    if (coverageReader == null) {
                        LOGGER.log(Level.SEVERE, "No valid Mosaic found for this Data Flow!");
                        throw new IllegalStateException(
                                "No valid Mosaic found for this Data Flow!");
                    }
                } finally {
                    if (coverageReader != null) {
                        try {
                            coverageReader.dispose();
                        } catch (Throwable e) {
                            if (LOGGER.isLoggable(Level.FINEST))
                                LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                        }
                    }
                }
            } else if (dataType.equalsIgnoreCase("geotiff")){
                final GeoTiffFormat format = new GeoTiffFormat ();
                GeoTiffReader coverageReader = null;
    
                // //
                // Trying to read the mosaic
                // //
                try {
                    coverageReader = (GeoTiffReader) format.getReader(workingDir);
    
                    if (coverageReader == null) {
                        LOGGER.log(Level.SEVERE, "No valid Mosaic found for this Data Flow!");
                        throw new IllegalStateException(
                                "No valid Mosaic found for this Data Flow!");
                    }
                } finally {
                    if (coverageReader != null) {
                        try {
                            coverageReader.dispose();
                        } catch (Throwable e) {
                            if (LOGGER.isLoggable(Level.FINEST))
                                LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                        }
                    }
                }
            } else {
            	LOGGER.log(Level.SEVERE,"Unsupported format type" + dataType);
                return null;
            }
            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////
            final Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("namespace",	getConfiguration().getDefaultNamespace());
            queryParams.put("path",		getConfiguration().getWmsPath());
            queryParams.put("style", getConfiguration().getDefaultStyle());
            send(workingDir, 
                    workingDir, 
                    getConfiguration().getGeoserverURL(),
                    new Long(System.currentTimeMillis()).toString(),
                    coverageStoreId,
                    baseFileName,
                    configId,
                    queryParams,
                    getConfiguration().getDatatype());

            return events;
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }

    }


    /**
     * Sending data to geoserver via REST protocol
     * @throws UnsupportedEncodingException 
     *
     * 
     */
    public void send(final File inputDataDir, final File data, final String geoserverBaseURL,
            final String timeStamp, final String originalCoverageStoreId, final String storeFilePrefix,
            final String configId, final Map<String, String> queryParams, final String type) 
			throws MalformedURLException, FileNotFoundException, UnsupportedEncodingException {
        URL geoserverREST_URL = null;
        boolean sent = false;
        final String coverageStoreId = URLEncoder.encode(originalCoverageStoreId,"UTF-8"); 
        String layerName = storeFilePrefix != null ? storeFilePrefix : coverageStoreId;
        if (GEOSERVER_VERSION.equalsIgnoreCase("1.7.2")){
            if ("DIRECT".equals(getConfiguration().getDataTransferMethod())) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL).append("/rest/folders/")
                		.append(coverageStoreId).append("/layers/").append(layerName).append("/file.")
                		.append(type).append( "?" ).append(getQueryString(queryParams)).toString());
                sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL,
                        new FileInputStream(data), 
    					getConfiguration().getGeoserverUID(),
    					getConfiguration().getGeoserverPWD());
            } else if ("URL".equals(getConfiguration().getDataTransferMethod())) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL ).append("/rest/folders/")
                		.append(coverageStoreId).append("/layers/").append(layerName).append("/url.")
                		.append(type).append("?").append(getQueryString(queryParams)).toString()); 
                sent = GeoServerRESTHelper.putContent(geoserverREST_URL,
    					data.toURL().toExternalForm(),
    					getConfiguration().getGeoserverUID(),
    					getConfiguration().getGeoserverPWD());
            }else if ("EXTERNAL".equals(getConfiguration().getDataTransferMethod())) {
                geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL).append("/rest/folders/")
                		.append(coverageStoreId).append("/layers/").append(layerName).append("/external.")
                		.append(type).append("?").append(getQueryString(queryParams)).toString());
                sent = GeoServerRESTHelper.putContent(geoserverREST_URL,
                                            data.toURL().toExternalForm(),
                                            getConfiguration().getGeoserverUID(),
                                            getConfiguration().getGeoserverPWD());
            }
        }else{
        	if ("DIRECT".equals(getConfiguration().getDataTransferMethod())) {
	            geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL).append("/rest/workspaces/")
	            		.append(queryParams.get("namespace")).append("/coveragestores/").append(coverageStoreId)
	            		.append("/file.").append(type).append("?").append(getQueryString(queryParams)).toString());
	            sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL,
	                    new FileInputStream(data), 
	                                        getConfiguration().getGeoserverUID(),
	                                        getConfiguration().getGeoserverPWD());
	        } else if ("URL".equals(getConfiguration().getDataTransferMethod())) {
	            geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL).append("/rest/workspaces/")
	            		.append(queryParams.get("namespace")).append("/coveragestores/").append(coverageStoreId)
	            		.append("/url.").append(type).append("?").append(getQueryString(queryParams)).toString()); 
	            sent = GeoServerRESTHelper.putContent(geoserverREST_URL,
	                                        data.toURL().toExternalForm(),
	                                        getConfiguration().getGeoserverUID(),
	                                        getConfiguration().getGeoserverPWD());
	        } else if ("EXTERNAL".equals(getConfiguration().getDataTransferMethod())) {
	            geoserverREST_URL = new URL(new StringBuilder(geoserverBaseURL).append("/rest/workspaces/")
	            		.append(queryParams.get("namespace")).append("/coveragestores/").append(coverageStoreId)
	            		.append("/external.").append(type).append("?").append(getQueryString(queryParams)).toString());
	            sent = GeoServerRESTHelper.putContent(geoserverREST_URL,
	                                        data.toURL().toExternalForm(),
	                                        getConfiguration().getGeoserverUID(),
	                                        getConfiguration().getGeoserverPWD());
	        }
        }

        if (sent) {
        	if (LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("MOSAIC GeoServerConfiguratorAction: coverage SUCCESSFULLY sent to GeoServer!");
        } else {
        	if (LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("MOSAIC GeoServerConfiguratorAction: coverage was NOT sent to GeoServer due to connection errors!");
        }
    }

    /**
     * Setup a Geoserver Ingestion action to send data to Geoserver via REST 
     * @param mosaicToBeIngested the location of the mosaic to be ingested 
     * @param prefix one of {@link Mosaicer#BALANCED_PREFIX} or {@link Mosaicer#RAW_PREFIX}
     * @throws Exception
     */
    public static void ingest(final String dataToBeIngested, final String wmsPath,
    		final String geoserverURL, final String geoserverUID, final String geoserverPWD,
    		final String geoserverUploadMethod, final String style, final String datatype) throws Exception{
      // //
      //
      // Setting up the GeoserverActionConfiguration properties
      //
      // //
      final GeoServerActionConfiguration geoserverConfig = new GeoServerActionConfiguration();
      geoserverConfig.setGeoserverURL(geoserverURL);
      geoserverConfig.setGeoserverUID(geoserverUID);
      geoserverConfig.setGeoserverPWD(geoserverPWD);
      geoserverConfig.setDataTransferMethod(geoserverUploadMethod);
      geoserverConfig.setWorkingDirectory(dataToBeIngested);
      geoserverConfig.setDefaultNamespace("it.geosolutions");
      geoserverConfig.setWmsPath(wmsPath);
      geoserverConfig.setDatatype(datatype);
      
      //Setting styles
      
      geoserverConfig.setDefaultStyle(style);
      final List<String> styles = new ArrayList<String>();
      styles.add(style);
      geoserverConfig.setStyles(styles);
      
      final SasMosaicGeoServerGenerator geoserverIngestion  = new SasMosaicGeoServerGenerator(geoserverConfig);
      geoserverIngestion.execute(null);
    }

    /**
     * Build a WMSPath from the specified String 
     * Input names are in the form: DATE_missionXX_LegXXXX_CHANNEL
     * As an instance: DATE=090316 and CHANNEL=port
     * 
     * @param name
     * @return
     */
	public static String buildWmsPath(final String name) {
		if (name==null || name.trim().length()==0)
			return "";
		final int missionIndex = name.indexOf("_");
        final String timePrefix = name.substring(0,missionIndex);
        final int legIndex = name.indexOf(Utils.LEG_PREFIX);
        String missionPrefix = name.substring(missionIndex+1,legIndex);
//        final int indexOfMissionNumber = missionPrefix.lastIndexOf("_");
//        missionPrefix = new StringBuffer("mission").append(missionPrefix.substring(indexOfMissionNumber+1)).toString();
        final String legPath = name.substring(legIndex+1);
        final String wmsPath = new StringBuilder("/").append(timePrefix).append("/").append(missionPrefix).append("/").append(legPath.replace("_","/")).toString();
        return wmsPath;
	}
    
}
