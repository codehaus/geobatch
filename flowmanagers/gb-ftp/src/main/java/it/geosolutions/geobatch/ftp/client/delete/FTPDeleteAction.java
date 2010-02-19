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


package it.geosolutions.geobatch.ftp.client.delete;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.ftp.client.FTPHelper;
import it.geosolutions.geobatch.ftp.client.configuration.FTPDeleteActionConfiguration;
import it.geosolutions.geobatch.ftp.client.configuration.FTPDeleteBaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.WriteMode;


/**
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */
public class FTPDeleteAction extends
        FTPDeleteBaseAction<FileSystemMonitorEvent> {


	protected FTPDeleteAction(FTPDeleteActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)throws Exception {
    	
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
                throw new IllegalStateException("FTP client data directory is null or does not exist.");
            }

            if ((ftpserverHost == null) || "".equals(ftpserverHost)) {
                throw new IllegalStateException("FtpServerHost is null.");
            }

            // //////////////////////////////////////////////
            // Retrive the added files from flow working dir
            // //////////////////////////////////////////////
            
            final List<File> filesToDelete= new ArrayList<File>();
            for(FileSystemMonitorEvent event : events){
            	final File input = event.getSource();
            	if(input.exists() && input.isFile() && input.canRead())
            		filesToDelete.add(input);
            	else{
                    throw new IllegalStateException("No valid input file found for this data flow!");
            	} 
        	}

            if (filesToDelete.size() <= 0)
                throw new IllegalStateException("No valid file found for this data flow!");

            // /////////////////////////////////////////
            //
            // Deleting files from remote FTP Server 
            //
            // /////////////////////////////////////////
            
            if (LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("Deleting file from FtpServer ... " + ftpserverHost);
            
            boolean sent = false;
            final FTPConnectMode connectMode = configuration.getConnectMode().toString().equalsIgnoreCase(FTPConnectMode.ACTIVE.toString()) ?
            		FTPConnectMode.ACTIVE : FTPConnectMode.PASV;
            final int timeout = configuration.getTimeout();
            
            boolean zipOutput = configuration.isZipInput();
            String zipFileName = configuration.getZipFileName();
            
            String path = "path";
            
            if(zipOutput){
            	
            	// ////////////////////////////////////////////////////////////////////
            	// Build the real name of the remote zipped file before deleting this
            	// ////////////////////////////////////////////////////////////////////
            	
            	String remoteZipFile = zipFileName.concat(".zip");
            	
            	// /////////////////////////////////
            	// Deleting the remote zipped file
            	// /////////////////////////////////
            	
        		sent = FTPHelper.deleteFileOrDirectory(ftpserverHost, remoteZipFile, false, path, ftpserverUSR, ftpserverPWD, 
        				ftpserverPort, connectMode, timeout);
            }else{
            	
            	// /////////////////////////////////////////////////////////////////////////
            	// Scanning the files to delete array to distinguish files and directories
            	// /////////////////////////////////////////////////////////////////////////
            	
            	for(File file : filesToDelete){
            		if(file.isFile()){
                		sent = FTPHelper.deleteFileOrDirectory(ftpserverHost, file.getName(), false, path, ftpserverUSR, ftpserverPWD, 
                				ftpserverPort, connectMode, timeout);

                		if(!sent)
                			break;
            		}else{
//            		    sent = deleteDirectory("test", path);
            			sent = deleteDirectory(file.getName(), path);
            			
                		if(!sent)
                			break;
                		else{
//                    		sent = FTPHelper.deleteFileOrDirectory(ftpserverHost, "test", true, path, ftpserverUSR, ftpserverPWD, 
//                    				ftpserverPort, connectMode, timeout);
                    		sent = FTPHelper.deleteFileOrDirectory(ftpserverHost, file.getName(), true, path, ftpserverUSR, ftpserverPWD, 
                    				ftpserverPort, connectMode, timeout);
                    		
                    		if(!sent)
                    			break;
                		}
            		}
            	}     
            }
            
            if (sent)
            	if (LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("FTPDeleteAction: file SUCCESSFULLY deleted from FtpServer!");
            else
            	if (LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("FTPDeleteAction: file was NOT deleted from FtpServer due to connection errors!");
            
            return events;
            
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }
    }
    
    /**
     * This function perform the elimination of the remote directory recursively.
     * 
     * @param dirName The directory name to delete. 
     * @param remotePath The remote directory path to delete  
     * @return boolean If true the deleting operation has been successful
     */    
    private boolean deleteDirectory(final String dirName, final String remotePath){
    	
    	boolean sent = false;
    	final FTPConnectMode connectMode = configuration.getConnectMode().toString().equalsIgnoreCase(FTPConnectMode.ACTIVE.toString()) ?
    			FTPConnectMode.ACTIVE : FTPConnectMode.PASV;
    	final int timeout = configuration.getTimeout();

    	// ////////////////////////////////////////////////////////
    	// Get the remote directory details to delete this content
    	// ////////////////////////////////////////////////////////
    	
    	FTPFile[] ftpFiles = FTPHelper.dirDetails(ftpserverHost, dirName, remotePath, ftpserverUSR, ftpserverPWD, 
				ftpserverPort, FTPTransferType.BINARY, WriteMode.OVERWRITE, connectMode, timeout);   	
    	
    	// //////////////////////////////////////
    	// Deleting the remote directory content
    	// //////////////////////////////////////
    	
    	if(ftpFiles != null && ftpFiles.length >= 1){	
    		String dirPath = remotePath.concat("/" + dirName);
    		
		    for (int i = 0, n = ftpFiles.length; i < n; i++) {
		    	if (ftpFiles[i].isDir()) {
		    		sent = deleteDirectory(ftpFiles[i].getName(), dirPath);
		    		
	        		if(!sent)
	        			break;
	        		else{
	            		sent = FTPHelper.deleteFileOrDirectory(ftpserverHost, ftpFiles[i].getName(), true, dirPath, ftpserverUSR, ftpserverPWD, 
	            				ftpserverPort, connectMode, timeout);
			    		
		        		if(!sent)
		        			break;
	        		}
		    	}else{       	    		
            		sent = FTPHelper.deleteFileOrDirectory(ftpserverHost, ftpFiles[i].getName(), false, dirPath, ftpserverUSR, ftpserverPWD, 
            				ftpserverPort, connectMode, timeout);

		    		if(!sent)
		    			break;
		    	}
		    }
		    
		    if(sent)return true;
		    else return false;
		    
    	}else if(ftpFiles != null && ftpFiles.length < 1){
    		return true;
    	}else return false;
    }
}
