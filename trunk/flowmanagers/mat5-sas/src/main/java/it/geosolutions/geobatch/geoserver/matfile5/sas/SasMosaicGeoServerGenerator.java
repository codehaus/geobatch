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
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.io.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicReader;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ GeoTIFFOverviewsEmbedder.java $ Revision: x.x $ 23/mar/07 11:42:25
 */
public class SasMosaicGeoServerGenerator
		extends GeoServerConfiguratorAction<FileSystemMonitorEvent> {

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
            final String configId = configuration.getName();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////
            if ((workingDir == null) || !workingDir.exists() || !workingDir.isDirectory()) {
                LOGGER.log(Level.SEVERE, "GeoServerDataDirectory is null or does not exist.");
                throw new IllegalStateException("GeoServerDataDirectory is null or does not exist.");
            }

			// checked by superclass
//            if ((geoserverURL == null) || "".equals(geoserverURL)) {
//                LOGGER.log(Level.SEVERE, "GeoServerCatalogServiceURL is null.");
//                throw new IllegalStateException("GeoServerCatalogServiceURL is null.");
//            }

            
            
            String inputFileName = workingDir.getAbsolutePath()+"/" + workingDir.getName() + ".shp";
            final File shapeFileName = new File(inputFileName);
            final String filePrefix = FilenameUtils.getBaseName(inputFileName);
            final String fileSuffix = FilenameUtils.getExtension(inputFileName);
			final String fileNameFilter = getConfiguration().getStoreFilePrefix();

			String baseFileName = null;

            if (fileNameFilter != null) {
                if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
                        && ("tif".equalsIgnoreCase(fileSuffix) || "tiff"
                                .equalsIgnoreCase(fileSuffix))) {
					// etj: are we missing something here?
					baseFileName = filePrefix;
                }
            } else if ("shp".equalsIgnoreCase(fileSuffix)) {
                baseFileName = filePrefix;
            }

			if(baseFileName == null) {
                LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
                throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
			}

            inputFileName = FilenameUtils.getName(inputFileName);
            final String coverageStoreId = FilenameUtils.getBaseName(inputFileName);

            // //
            // creating coverageStore
            // //
            final ImageMosaicFormat format = new ImageMosaicFormat();
            ImageMosaicReader coverageReader = null;

            // //
            // Trying to read the GeoTIFF
            // //
            /**
             * GeoServer url: "file:data/" + coverageStoreId + "/" + geoTIFFFileName
             */
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

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("namespace",	getConfiguration().getDefaultNamespace());
            queryParams.put("wmspath",		getConfiguration().getWmsPath());
            send(workingDir,
					shapeFileName,
					getConfiguration().getGeoserverURL(),
					new Long(System.currentTimeMillis()).toString(),
					coverageStoreId,
					baseFileName,
					getConfiguration().getStyles(),
					configId,
					getConfiguration().getDefaultStyle(),
                    queryParams);

            return events;
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }

    }


    /**
     */
    public void send(final File inputDataDir, final File data, final String geoserverBaseURL,
            final String timeStamp, final String coverageStoreId, final String storeFilePrefix,
            final List<String> dataStyles, final String configId, final String defaultStyle,
            final Map<String, String> queryParams) 
			throws MalformedURLException, FileNotFoundException {
        URL geoserverREST_URL = null;
        boolean sent = false;

		String layerName = storeFilePrefix != null ? storeFilePrefix : coverageStoreId;

        if ("DIRECT".equals(getConfiguration().getDataTransferMethod())) {
            geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/" + coverageStoreId
                    + "/layers/" + layerName
                    + "/file.shp?" + getQueryString(queryParams));
            sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL,
                    new FileInputStream(data), 
					getConfiguration().getGeoserverUID(),
					getConfiguration().getGeoserverPWD());
        } else if ("URL".equals(getConfiguration().getDataTransferMethod())) {
            geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/" + coverageStoreId
                    + "/layers/" + layerName
                    + "/url.shp");
            sent = GeoServerRESTHelper.putContent(geoserverREST_URL,
					data.toURL().toExternalForm(),
					getConfiguration().getGeoserverUID(),
					getConfiguration().getGeoserverPWD());
        }

        if (sent) {
            LOGGER.info("MOSAIC GeoServerConfiguratorAction: coverage SUCCESSFULLY sent to GeoServer!");
			boolean sldSent = configureStyles(layerName);
        } else {
            LOGGER.info("MOSAIC GeoServerConfiguratorAction: coverage was NOT sent to GeoServer due to connection errors!");
        }
    }

}
