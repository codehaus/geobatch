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



package it.geosolutions.iengine.geoserver.shapefile;

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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ ShapeFileGeoServerConfigurator.java $ Revision: x.x $ 19/feb/07 16:28:31
 */
public class ShapeFileGeoServerConfigurator extends
        GeoServerConfiguratorAction<FileSystemMonitorEvent> {

    protected ShapeFileGeoServerConfigurator(GeoServerActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    public void send(File inputDataDir, File data, String geoserverBaseURL, String timeStamp,
            String storeId, String storeFilePrefix, List<String> dataStyles, String configId,
            String defaultStyle, Map<String, String> queryParams) throws MalformedURLException,
            FileNotFoundException {
        // TODO: PARAMTERIZE THIS
        URL geoserverREST_URL = null;
        boolean sent = false;

        if (data == null) {
            LOGGER
                    .info("ShapeFile GeoServerConfiguratorAction: cannot send coverage to GeoServer, input data null!");
            return;
        }
        // if ("DIRECT".equals(IngestionEngineEnvironment.getDataTransferMethod())) {
        geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/" + storeId + "/layers/"
                + storeFilePrefix + "/file.shp?" + getQueryString(queryParams));
        sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL, new FileInputStream(data),
                geoserverUID, geoserverPWD);
        /*
         * } else if ("URL".equals(IngestionEngineEnvironment.getDataTransferMethod())) {
         * geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/" + storeId + "/layers/" +
         * storeFilePrefix + "/url.shp"); sent = GeoServerRESTHelper.putContent(geoserverREST_URL,
         * data.toURL().toExternalForm()); }
         */

        if (sent) {
            LOGGER
                    .info("ShapeFile GeoServerConfiguratorAction: coverage SUCCESSFULLY sent to GeoServer!");

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
                            .info("ShapeFile GeoServerConfiguratorAction: SLD SUCCESSFULLY sent to GeoServer!");
                } else {
                    LOGGER
                            .info("ShapeFile GeoServerConfiguratorAction: SLD was NOT sent to GeoServer!");
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
                    .info("ShapeFile GeoServerConfiguratorAction: coverage was NOT sent to GeoServer due to connection errors!");
        }
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {
            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
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
            final String configId = configuration.getName();

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

            /*
             * if (storeFilePrefix == null) { LOGGER.log(Level.SEVERE, "DataFilePrefix is null.");
             * throw new IllegalStateException("DataFilePrefix is null."); }
             */

            // // //
            // // looking for optional parameters.
            // // //
            // if ((destinationCrs == null) && (configCRS != null)) {
            // final String authority = configCRS.getAuthority();
            // final BigInteger code = configCRS.getCode();
            //
            // if ((authority != null) && (code != null)) {
            // try {
            // destinationCrs = org.geotools.referencing.CRS.decode(authority + ":" + code, true);
            // } catch (NoSuchAuthorityCodeException e) {
            // LOGGER.info("No right CRS ('AUTH','CODE') specified ... using the native one!");
            // destinationCrs = null;
            // } catch (FactoryException e) {
            // LOGGER.info("No right CRS ('AUTH','CODE') specified ... using the native one!");
            // destinationCrs = null;
            // }
            // } else {
            // final String WKT = configCRS.getStringValue();
            //
            // try {
            // destinationCrs = org.geotools.referencing.CRS.parseWKT(WKT);
            // } catch (FactoryException e) {
            // LOGGER.info("No right CRS ('WKT') specified ... using the native one!");
            // destinationCrs = null;
            // }
            // }
            // }
            //
            // // //
            // // the destination Envelope is acceptable only if the CRS was specified.
            // // //
            // if ((destinationEnvelope == null) && (destinationCrs != null) && (configEvnelope !=
            // null)) {
            // final int dim = configEvnelope.getDimension().intValue();
            //
            // if (dim != 2) {
            // LOGGER.info("Only 2D Envelopes are supported!");
            // LOGGER.info("No right ENVELOPE specified ... using the native one!");
            // }
            //
            // final double[] minCP = new double[2];
            // final double[] maxCP = new double[2];
            //
            // String[] pos_0 = configEvnelope.getPosArray(0).split(" ");
            // String[] pos_1 = configEvnelope.getPosArray(1).split(" ");
            //
            // try {
            // minCP[0] = Double.parseDouble(pos_0[0]);
            // minCP[1] = Double.parseDouble(pos_0[1]);
            // maxCP[0] = Double.parseDouble(pos_1[0]);
            // maxCP[1] = Double.parseDouble(pos_1[1]);
            //
            // destinationEnvelope = new GeneralEnvelope(minCP, maxCP);
            // destinationEnvelope.setCoordinateReferenceSystem(destinationCrs);
            // } catch (NumberFormatException e) {
            // LOGGER.info("No right ENVELOPE specified ... using the native one!");
            // destinationEnvelope = null;
            // }
            // }
            // ////////////////////////////////////////////////////////////////////
            //
            // Creating Shapefile dataStore.
            //
            // ////////////////////////////////////////////////////////////////////
            // //
            // looking for file
            // //
            // XXX FIX ME
            FileSystemMonitorEvent event = events.peek();
            File dataDir = new File(event.getSource().getParent());
            String shpFileName = null;
            String dataStoreId = null;
            File[] files = dataDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    final String filePrefix = name.substring(0, name.lastIndexOf("."));
                    final String fileSuffix = name
                            .substring(filePrefix.length() + 1, name.length());

                    if (storeFilePrefix != null) {
                        if ((filePrefix.equals(storeFilePrefix) || filePrefix
                                .matches(storeFilePrefix))
                                && ("shp".equalsIgnoreCase(fileSuffix))) {
                            return true;
                        }
                    } else if ("shp".equalsIgnoreCase(fileSuffix)) {
                        storeFilePrefix = filePrefix;
                        return true;
                    }

                    return false;
                }
            });

            if (files.length != 1) {
                LOGGER.log(Level.SEVERE, "No valid ShapeFile Names found for this Data Flow!");
                throw new IllegalStateException(
                        "No valid ShapeFile Names found for this Data Flow!");
            }

            String path = files[0].getAbsolutePath();
            path = path.replaceAll("\\\\", "/");
            shpFileName = path.substring(path.lastIndexOf("/") + 1, path.length());
            dataStoreId = shpFileName.substring(0, shpFileName.lastIndexOf("."));

            // //
            // creating dataStore
            // //
            DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

            // //
            // Convert Params into the kind of Map we actually need
            // //
            Map connectionParams = new HashMap(); // values used for connection

            /**
             * GeoServer url: "file:data/" + dataStoreId + "/" + shpFileName
             */
            try {
                connectionParams.put("url", files[0].toURI().toURL());
            } catch (MalformedURLException e) {
                LOGGER.log(Level.SEVERE, "No valid ShapeFile URL found for this Data Flow: "
                        + e.getLocalizedMessage());
                throw new IllegalStateException("No valid ShapeFile URL found for this Data Flow: "
                        + e.getLocalizedMessage());
            }

            connectionParams.put("namespace", defaultNamespace);

            boolean validShape = factory.canProcess(connectionParams);
            factory = null;

            if (!validShape) {
                LOGGER.log(Level.SEVERE, "No valid ShapeFile found for this Data Flow!");
                throw new IllegalStateException("No valid ShapeFiles found for this Data Flow!");
            }

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to GeoServer via REST protocol.
            //
            // ////////////////////////////////////////////////////////////////////
            // http://localhost:8080/geoserver/rest/coveragestores/test_cv_store/test/file.tiff
            LOGGER.info("Sending ShapeFile to GeoServer ... " + geoserverURL);
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("namespace", defaultNamespace);
            queryParams.put("wmspath", wmsPath);
            send(workingDir, IOUtils.deflate(dataDir, storeFilePrefix), geoserverURL, new Long(
                    event.getTimestamp()).toString(), storeFilePrefix, storeFilePrefix, styles,
                    configId, defaultStyle, queryParams);

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }
    }
}
