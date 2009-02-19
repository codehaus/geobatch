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



package it.geosolutions.iengine.ftpserver.shapefile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.catalog.file.FileBaseCatalog;
import it.geosolutions.iengine.configuration.event.action.ftpserver.FtpServerEventActionConfiguration;
import it.geosolutions.iengine.flow.event.action.ftpserver.FtpServerEventAction;
import it.geosolutions.iengine.flow.event.action.ftpserver.FtpServerHelper;
import it.geosolutions.iengine.global.CatalogHolder;
import it.geosolutions.iengine.io.utils.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Queue;
import java.util.logging.Level;


/**
 * Comments here ...
 * 
 * @author Ivano Picco
 * 
 */
public class ShapeFileFtpServerEventAction extends
        FtpServerEventAction<FileSystemMonitorEvent> {

    protected ShapeFileFtpServerEventAction(FtpServerEventActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    /**
     * It just send file to remote by FTP
     * @param data
     * @throws java.net.MalformedURLException
     * @throws java.io.FileNotFoundException
     */
    public void send(File data) {

        boolean sent = false;

        if (data == null) {
            LOGGER
                    .info("ShapeFile FtpServerConfiguratorAction: cannot send file to FtpServer, input data null!");
            return;
        }

        sent = FtpServerHelper.putBinaryFileTo(ftpserverHost, data.getAbsolutePath(),
                ftpserverUSR, ftpserverPWD, ftpserverPort);

        if (sent) {
            LOGGER
                    .info("ShapeFile FtpServerConfiguratorAction: file SUCCESSFULLY sent to FtpServer!");
        } else {
            LOGGER
                    .info("ShapeFile FtpServerConfiguratorAction: file was NOT sent to FtpServer due to connection errors!");
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
                LOGGER.log(Level.SEVERE, "FtpServerDataDirectory is null or does not exist.");
                throw new IllegalStateException("FtpServerDataDirectory is null or does not exist.");
            }

            if ((ftpserverHost == null) || "".equals(ftpserverHost)) {
                LOGGER.log(Level.SEVERE, "FtpServerHost is null.");
                throw new IllegalStateException("FtpServerHost is null.");
            }

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

                     if ("shp".equalsIgnoreCase(fileSuffix)) return true;
                    
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

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to FtpServer via FTP.
            //
            // ////////////////////////////////////////////////////////////////////
            LOGGER.info("Sending ShapeFile to FtpServer ... " + ftpserverHost);
            
//            send(files[0] , 
           send(IOUtils.deflate(dataDir,dataStoreId));

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }
    }
}
