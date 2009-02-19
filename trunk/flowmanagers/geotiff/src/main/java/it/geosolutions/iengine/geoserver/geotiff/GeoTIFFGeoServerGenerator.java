/*
 * $Header: $fileName$ $
 * $Revision: 0.1 $
 * $Date: $date$ $time.long$ $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.iengine.geoserver.geotiff;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.catalog.file.FileBaseCatalog;
import it.geosolutions.iengine.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.iengine.flow.event.action.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.iengine.flow.event.action.geoserver.GeoServerRESTHelper;
import it.geosolutions.iengine.global.CatalogHolder;
import it.geosolutions.iengine.io.utils.IOUtils;

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
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ GeoTIFFOverviewsEmbedder.java $ Revision: x.x $ 23/mar/07 11:42:25
 */
public class GeoTIFFGeoServerGenerator extends GeoServerConfiguratorAction<FileSystemMonitorEvent> {

    protected GeoTIFFGeoServerGenerator(GeoServerActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    /**
     * @param inputDataDir
     * @param geoserverBaseURL
     * @param storeFilePrefix
     * @param timeStamp
     * @param dataStyles
     * @param configId
     * @param defaultStyle
     * @param coverageStoreId
     * @param files
     * @throws MalformedURLException
     * @throws FileNotFoundException
     */
    public void send(final File inputDataDir, final File data, final String geoserverBaseURL,
            final String timeStamp, final String coverageStoreId, final String storeFilePrefix,
            final List<String> dataStyles, final String configId, final String defaultStyle,
            final Map<String, String> queryParams) throws MalformedURLException,
            FileNotFoundException {
        URL geoserverREST_URL = null;
        boolean sent = false;

        if ("DIRECT".equals(dataTransferMethod)) {
            geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/" + coverageStoreId
                    + "/layers/" + (storeFilePrefix != null ? storeFilePrefix : coverageStoreId)
                    + "/file.geotiff?" + getQueryString(queryParams));
            sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL,
                    new FileInputStream(data), geoserverUID, geoserverPWD);
        } else if ("URL".equals(dataTransferMethod)) {
            geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/" + coverageStoreId
                    + "/layers/" + (storeFilePrefix != null ? storeFilePrefix : coverageStoreId)
                    + "/url.geotiff");
            sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data.toURL().toExternalForm(),
                    geoserverUID, geoserverPWD);
        }

        if (sent) {
            LOGGER
                    .info("GeoTIFF GeoServerConfiguratorAction: coverage SUCCESSFULLY sent to GeoServer!");

            // //
            // Storing SLDs
            // //
            boolean sldsCreatedOK = true;

            for (String styleName : dataStyles) {
                File sldFile = new File(inputDataDir, "/" + configId + "/" + timeStamp + "/"
                        + styleName + ".sld");
                geoserverREST_URL = new URL(geoserverBaseURL + "/rest/styles/" + styleName);

                if (GeoServerRESTHelper.putTextFileTo(geoserverREST_URL, new FileInputStream(
                        sldFile), geoserverUID, geoserverPWD)) {
                    geoserverREST_URL = new URL(geoserverBaseURL + "/rest/sldservice/updateLayer/"
                            + storeFilePrefix);
                    GeoServerRESTHelper.putContent(geoserverREST_URL, "<LayerConfig><Style>"
                            + styleName + "</Style></LayerConfig>", geoserverUID, geoserverPWD);

                    LOGGER
                            .info("GeoTIFF GeoServerConfiguratorAction: SLD SUCCESSFULLY sent to GeoServer!");
                } else {
                    LOGGER
                            .info("GeoTIFF GeoServerConfiguratorAction: SLD was NOT sent to GeoServer!");
                    sldsCreatedOK = false;
                }
            }

            // //
            // if it's all OK, set the Default SLD
            // //
            if (sldsCreatedOK) {
                geoserverREST_URL = new URL(geoserverBaseURL + "/rest/sldservice/updateLayer/"
                        + storeFilePrefix);
                GeoServerRESTHelper.putContent(geoserverREST_URL, "<LayerConfig><DefaultStyle>"
                        + defaultStyle + "</DefaultStyle></LayerConfig>", geoserverUID,
                        geoserverPWD);
            }
        } else {
            LOGGER
                    .info("GeoTIFF GeoServerConfiguratorAction: coverage was NOT sent to GeoServer due to connection errors!");
        }
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {

            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            FileSystemMonitorEvent event = events.remove();
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

            if ((geoserverURL == null) || "".equals(geoserverURL)) {
                LOGGER.log(Level.SEVERE, "GeoServerCatalogServiceURL is null.");
                throw new IllegalStateException("GeoServerCatalogServiceURL is null.");
            }

            String inputFileName = event.getSource().getAbsolutePath();
            final String filePrefix = FilenameUtils.getBaseName(inputFileName);
            final String fileSuffix = FilenameUtils.getExtension(inputFileName);

            if (storeFilePrefix != null) {
                if ((filePrefix.equals(storeFilePrefix) || filePrefix.matches(storeFilePrefix))
                        && ("tif".equalsIgnoreCase(fileSuffix) || "tiff"
                                .equalsIgnoreCase(fileSuffix))) {
                }
            } else if ("tif".equalsIgnoreCase(fileSuffix) || "tiff".equalsIgnoreCase(fileSuffix)) {
                storeFilePrefix = filePrefix;
            }

            inputFileName = FilenameUtils.getName(inputFileName);
            final String coverageStoreId = FilenameUtils.getBaseName(inputFileName);

            // //
            // creating coverageStore
            // //
            final GeoTiffFormat format = new GeoTiffFormat();
            GeoTiffReader coverageReader = null;

            // //
            // Trying to read the GeoTIFF
            // //
            /**
             * GeoServer url: "file:data/" + coverageStoreId + "/" + geoTIFFFileName
             */
            try {
                coverageReader = (GeoTiffReader) format.getReader(event.getSource());

                if (coverageReader == null) {
                    LOGGER.log(Level.SEVERE, "No valid GeoTIFF File found for this Data Flow!");
                    throw new IllegalStateException(
                            "No valid GeoTIFF File found for this Data Flow!");
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
            queryParams.put("namespace", defaultNamespace);
            queryParams.put("wmspath", wmsPath);
            send(workingDir, event.getSource(), geoserverURL, new Long(event.getTimestamp())
                    .toString(), coverageStoreId, storeFilePrefix, styles, configId, defaultStyle,
                    queryParams);

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }

    }

}
