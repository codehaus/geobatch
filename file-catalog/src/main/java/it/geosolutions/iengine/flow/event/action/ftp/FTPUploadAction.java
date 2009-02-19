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



package it.geosolutions.iengine.flow.event.action.ftp;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.catalog.file.FileBaseCatalog;
import it.geosolutions.iengine.configuration.event.action.ftp.FTPUploadActionConfiguration;
import it.geosolutions.iengine.global.CatalogHolder;
import it.geosolutions.iengine.io.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.WriteMode;


/**
 * Comments here ...
 * 
 * @author Ivano Picco
 * 
 */
public class FTPUploadAction extends
        FTPBaseAction<FileSystemMonitorEvent> {

    private boolean zipMe;
    
	private String zipFileName;


	protected FTPUploadAction(FTPUploadActionConfiguration configuration)
            throws IOException {
        super(configuration);
        this.zipMe=configuration.isZipInput();
        this.zipFileName=configuration.getZipFileName();
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
                throw new IllegalStateException("FtpServerDataDirectory is null or does not exist.");
            }

            if ((ftpserverHost == null) || "".equals(ftpserverHost)) {
                throw new IllegalStateException("FtpServerHost is null.");
            }

            // ////////////////////////////////////////////////////////////////////
            //
            // Creating Shapefile dataStore.
            //
            // ////////////////////////////////////////////////////////////////////
            final List<File> filesToSend= new ArrayList<File>();
            for(FileSystemMonitorEvent event:events)
        	{
            	final File input=event.getSource();
            	if(input.exists()&&input.isFile()&&input.canRead())
            		filesToSend.add(input);
            	//else LOG ME
        	}

            if (filesToSend.size() <=0) {
                throw new IllegalStateException(
                        "No valid ShapeFile Names found for this Data Flow!");
            }
            
            

            // ////////////////////////////////////////////////////////////////////
            //
            // SENDING data to FtpServer via FTP.
            //
            // ////////////////////////////////////////////////////////////////////
            LOGGER.info("Sending file to FtpServer ... " + ftpserverHost);
            boolean sent=false;
            final WriteMode writeMode=configuration.getWriteMode();
            final FTPConnectMode connectMode=configuration.getConnectMode();
            final int timeout=configuration.getTimeout();
			if(zipMe){
	            sent = FTPHelper.putBinaryFileTo(ftpserverHost, IOUtils.deflate(workingDir,zipFileName,filesToSend.toArray(new File[filesToSend.size()])).getAbsolutePath(),
	                    ftpserverUSR, ftpserverPWD, ftpserverPort,WriteMode.OVERWRITE,FTPConnectMode.PASV,timeout);
            }
            else
            {
            	for(File file: filesToSend)
            	{
            		sent = FTPHelper.putBinaryFileTo(ftpserverHost, file.getAbsolutePath(),
    	                    ftpserverUSR, ftpserverPWD, ftpserverPort,WriteMode.OVERWRITE,FTPConnectMode.PASV,timeout);
            		if(!sent)
            			break;
            	}
            }
            if (sent) {
                LOGGER
                        .info("ShapeFile FtpServerConfiguratorAction: file SUCCESSFULLY sent to FtpServer!");
            } else {
                LOGGER
                        .info("ShapeFile FtpServerConfiguratorAction: file was NOT sent to FtpServer due to connection errors!");
            }

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }
    }
}
