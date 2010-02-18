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

package it.geosolutions.geobatch.ftp.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPMessageCollector;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FileTransferClient;
import com.enterprisedt.net.ftp.WriteMode;


/**
 * EDTFTP based utility methods.
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * @author Ivano Picco
 */
public class FTPHelper {

    private static final Logger LOGGER = Logger.getLogger(FTPHelper.class.toString());

    /**
     *
     * @param ftpserverHost
     * @param binaryFile
     * @param ftpserverUser
     * @param ftpserverPassword
     * @param writeMode 
     * @param connectMode 
     * @param timeout 
     * @return
     */
    public static boolean putBinaryFileTo(String ftpserverHost, String binaryFile, String path,
            String ftpserverUser, String ftpserverPassword,int ftpserverPort, WriteMode writeMode, FTPConnectMode connectMode, int timeout ) {

    	if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("[FTP::PutFileTo]: " + "start");

        boolean res = putFile(ftpserverHost, binaryFile, path, ftpserverUser,
				ftpserverPassword, ftpserverPort,FTPTransferType.BINARY,writeMode,connectMode,timeout); 
         
         if(LOGGER.isLoggable(Level.INFO))
     		LOGGER.info("[FTP::PutFileTo]: " + "end");
         return res;
         
    }

	/**
	 * @param ftpserverHost
	 * @param binaryFile
	 * @param ftpserverUser
	 * @param ftpserverPassword
	 * @param ftpserverPort
	 * @param transferType 
	 * @param writeMode 
	 * @param connectMode 
	 * @param timeout 
	 * @return
	 */
	private static boolean putFile(String ftpserverHost, String binaryFile, String path, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout) {
		
		boolean res = false;

        final String host = ftpserverHost;
        final String login = ftpserverUser;
        final String password = ftpserverPassword;
        final int port = ftpserverPort;

        String remoteFileName = null;
        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.setRemoteHost(host);
            ftp.setRemotePort(port);
			ftp.setTimeout(timeout);
            final FTPMessageCollector listener = new FTPMessageCollector();
            ftp.setMessageListener(listener);

            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Connecting");
            
			ftp.connect();
			
			if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Logging in");
			
			ftp.login(login, password);
            
			// /////////////////////////////////////
            // Transfer mode (ACTIVE vs PASSIVE)
			// /////////////////////////////////////
			
			ftp.setConnectMode(connectMode);	
			
			// //////////////////////////////////
            // Transfer type (BINARY vs ASCII)
			// //////////////////////////////////
			
            ftp.setType(transferType);
            
            // ///////////////////////////////
            // Get the remote file name
            // ///////////////////////////////
            
            remoteFileName = binaryFile.replaceAll("\\\\", "/");
            remoteFileName = remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::PutFileTo]: " + "Connecting to :" + host + ":" + port);            

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::FileTo]: " + "sending: " + binaryFile + " to: " + remoteFileName);

            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(path.indexOf("/") != -1){
            	String[] pathArray = path.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.chdir(pathArray[h]);
            }
        	
            // //////////////////////////
            // Uploading the local file 
            // //////////////////////////
            
            final String remoteFileNameReturned = ftp.put(binaryFile, remoteFileName);
            
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("[FTP::FileTo]: " + "sent: " + binaryFile + " to: " + remoteFileNameReturned);
            
            res = true;
            
        } catch (FTPException e) {
            LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (IOException e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (Throwable e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp!=null&&ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
		return res;
	}

    /**
     *
     * @param ftpserverHost
     * @param textFile
     * @param ftpserverUser
     * @param ftpserverPassword
     * @param writeMode 
     * @param connectMode 
     * @param timeout 
     * @return
     */
	public static boolean putTextFileTo(String ftpserverHost, String textFile, String path,
	            String ftpserverUser, String ftpserverPassword, int ftpserverPort, WriteMode writeMode, FTPConnectMode connectMode, int timeout) {
		if(LOGGER.isLoggable(Level.INFO))
			LOGGER.info("[FTP::PutFileTo]: " + "start");
	
	    boolean res = putFile(ftpserverHost, textFile, path, ftpserverUser,
				ftpserverPassword, ftpserverPort,FTPTransferType.ASCII,writeMode,connectMode,timeout); 
	     
	     if(LOGGER.isLoggable(Level.INFO))
	 		LOGGER.info("[FTP::PutFileTo]: " + "end");
	     
	     return res;
	}

	/**
	*
	* @param ftpserverHost
	* @param textFile
	* @param ftpserverUser
	* @param ftpserverPassword
	* @param writeMode 
	* @param connectMode 
	* @param timeout 
	* @return
	*/
	public static boolean createDirectory(String ftpserverHost, String binaryFile, String path, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout){
		
		boolean res = false;

        final String host = ftpserverHost;
        final String login = ftpserverUser;
        final String password = ftpserverPassword;
        final int port = ftpserverPort;

        String remoteFileName = null;
        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.setRemoteHost(host);
            ftp.setRemotePort(port);
			ftp.setTimeout(timeout);
            final FTPMessageCollector listener = new FTPMessageCollector();
            ftp.setMessageListener(listener);

            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Connecting");
            
			ftp.connect();

			if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Logging in");
			
			ftp.login(login, password);
			
			// /////////////////////////////////////
            // Transfer mode (ACTIVE vs PASSIVE)
			// /////////////////////////////////////
			
			ftp.setConnectMode(connectMode);
			
			// //////////////////////////////////
            // Transfer type (BINARY vs ASCII)
			// //////////////////////////////////
			
            ftp.setType(transferType);
            
            // ///////////////////////////////
            // Get the remote file name
            // ///////////////////////////////
            
            remoteFileName = binaryFile.replaceAll("\\\\", "/");
            remoteFileName = remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::PutFileTo]: " + "Connecting to :" + host + ":" + port);            

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::FileTo]: " + "sending: " + binaryFile + " to: " + remoteFileName);

            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(path.indexOf("/") != -1){
            	String[] pathArray = path.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.chdir(pathArray[h]);
            }
            
            // ////////////////////////////
            // Building the remote directory
            // ////////////////////////////

            ftp.mkdir(remoteFileName); 
            
            res = true;

        }catch (FTPException e) {
            LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (IOException e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (Throwable e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
		return res;
	}
	
	public static boolean downloadFile(String ftpserverHost, String localPath, String remotePath, String remoteFile, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout){
		
		boolean res = false;

        final String host = ftpserverHost;
        final String login = ftpserverUser;
        final String password = ftpserverPassword;
        final int port = ftpserverPort;

        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.setRemoteHost(host);
            ftp.setRemotePort(port);
			ftp.setTimeout(timeout);
            final FTPMessageCollector listener = new FTPMessageCollector();
            ftp.setMessageListener(listener);

            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Connecting");
            
			ftp.connect();

			if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Logging in");
			
			ftp.login(login, password);
			
			// /////////////////////////////////////
            // Transfer mode (ACTIVE vs PASSIVE)
			// /////////////////////////////////////
			
			ftp.setConnectMode(connectMode);
			
			// //////////////////////////////////
            // Transfer type (BINARY vs ASCII)
			// //////////////////////////////////
			
            ftp.setType(transferType);

            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::PutFileTo]: " + "Connecting to :" + host + ":" + port);            

            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::FileTo]: " + "downloading: " + remoteFile + " from: " + remoteFile);

            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(remotePath.indexOf("/") != -1){
            	String[] pathArray = remotePath.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.chdir(pathArray[h]);
            }
            
            // ///////////////////////////////
            // Get the remote file 
            // ///////////////////////////////
        	
            ftp.get(localPath, remoteFile);
            
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("[FTP::FileTo]: " + "downloaded: " + remoteFile + " from: " + remoteFile);
            
            res = true;
            
        }catch (FTPException e) {
            LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (IOException e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (Throwable e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
		return res;		
	}
	
	public static FTPFile[] dirDetails(String ftpserverHost, String dirName, String remotePath, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, 
			WriteMode writeMode, FTPConnectMode connectMode, int timeout){
		
		boolean res = false;
		FTPFile[] ftpFiles = null;

        final String host = ftpserverHost;
        final String login = ftpserverUser;
        final String password = ftpserverPassword;
        final int port = ftpserverPort;
        
        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.setRemoteHost(host);
            ftp.setRemotePort(port);
			ftp.setTimeout(timeout);
            final FTPMessageCollector listener = new FTPMessageCollector();
            ftp.setMessageListener(listener);

            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Connecting");
            
			ftp.connect();

			if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Logging in");
			
			ftp.login(login, password);

			// /////////////////////////////////////
            // Transfer mode (ACTIVE vs PASSIVE)
			// /////////////////////////////////////
			
			ftp.setConnectMode(connectMode);
			
			// //////////////////////////////////
            // Transfer type (BINARY vs ASCII)
			// //////////////////////////////////
			
            ftp.setType(transferType);

            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::PutFileTo]: " + "Connecting to :" + host + ":" + port);
      
            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(remotePath.indexOf("/") != -1){
            	String[] pathArray = remotePath.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.chdir(pathArray[h]);
            }
            
            // /////////////////////////////////////
            // Getting the remote directory details 
            // /////////////////////////////////////
            
            ftpFiles = ftp.dirDetails(dirName);

            if(ftpFiles != null)res = true;
            else res = false;
            
        }catch (FTPException e) {
            LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (IOException e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (Throwable e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.connected())
        		try{
        			ftp.quitImmediately();
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
        if(res)return ftpFiles;
        else return null;
	}
	
	public static boolean deleteFileOrDirectory(String ftpserverHost, String remoteFile, boolean isDir, String remotePath, String ftpserverUser,
			String ftpserverPassword, int ftpserverPort, FTPConnectMode connectMode, int timeout){
		
		boolean res = false;

        final String host = ftpserverHost;
        final String login = ftpserverUser;
        final String password = ftpserverPassword;
        final int port = ftpserverPort;

        FileTransferClient ftp = null;

        try {
            ftp = new FileTransferClient();
            ftp.setRemoteHost(host);
            ftp.setRemotePort(port);
			ftp.setTimeout(timeout);
			ftp.setPassword(password);
			ftp.setUserName(login);
			
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("Connecting");
            
			ftp.connect();
			
            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::PutFileTo]: " + "Connecting to :" + host + ":" + port);            

            if(LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("[FTP::FileTo]: " + "removing: " + remoteFile + " from: " + remoteFile);
        	
            // /////////////////////////////////////////////
            // Checking to change remote working directory 
            // /////////////////////////////////////////////
            
            if(remotePath.indexOf("/") != -1){
            	String[] pathArray = remotePath.split("/");
            	
            	for(int h=0; h<pathArray.length; h++)
            		if(pathArray[h].indexOf("path") != -1) continue;
            		else ftp.changeDirectory(pathArray[h]);
            }
            
            // //////////////////////////////////
            // Deleting remote file or directory
            // //////////////////////////////////
            
            if(isDir)
            	ftp.deleteDirectory(remoteFile);
            else
            	ftp.deleteFile(remoteFile);
            
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("[FTP::FileTo]: " + "removed: " + remoteFile + " from: " + remoteFile);
            
            res = true;
            
        }catch (FTPException e) {
            LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (IOException e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (Throwable e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }finally{
        	
        	// ///////////////////////////////
        	// Disconnect to the FTP server 
        	// ///////////////////////////////
        	
        	if(ftp != null && ftp.isConnected())
        		try{
        			ftp.disconnect(true);
        		}catch (Throwable t) {
                    if(LOGGER.isLoggable(Level.FINE))
                		LOGGER.log(Level.FINE,t.getLocalizedMessage(),t);
				}
        }
        
		return res;		
	}
}
