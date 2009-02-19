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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.enterprisedt.net.ftp.AdvancedFTPSettings;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FileTransferClient;
import com.enterprisedt.net.ftp.WriteMode;

/**
 * EDTFTP based utility methods.
 * 
 * @author Ivano Picco
 * 
 */
class FTPHelper {

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
    public static boolean putBinaryFileTo(String ftpserverHost, String binaryFile,
            String ftpserverUser, String ftpserverPassword,int ftpserverPort, WriteMode writeMode, FTPConnectMode connectMode, int timeout ) {

    	if(LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("[FTP::PutFileTo]: " + "start");

        boolean res = putFile(ftpserverHost, binaryFile, ftpserverUser,
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
	private static boolean putFile(String ftpserverHost, String binaryFile,
			String ftpserverUser, String ftpserverPassword, int ftpserverPort, FTPTransferType transferType, WriteMode writeMode, FTPConnectMode connectMode, int timeout) {
		boolean res = false;

        final String host = ftpserverHost;
        final String login = ftpserverUser;
        final String password = ftpserverPassword;
        final int port = ftpserverPort;

        String remoteFileName = null;

        FileTransferClient ftp = null;

        try {
            // create client
            ftp = new FileTransferClient();

            // set remote host
            ftp.setRemoteHost(host);
            ftp.setUserName(login);
            ftp.setPassword(password);
            ftp.setRemotePort(port);
			ftp.setTimeout(timeout); //millis
            
            //transfer mode (ACTIVE vs PASSIVE)
            final AdvancedFTPSettings ftpSettings = ftp.getAdvancedFTPSettings();
            ftpSettings.setConnectMode(connectMode);			

            remoteFileName = binaryFile.replaceAll("\\\\", "/");
            remoteFileName = remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());

            // connect to the server
            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::PutFileTo]: " + "Connecting to :" + host+":"+port);
            ftp.connect();

            if(LOGGER.isLoggable(Level.INFO))
            		LOGGER.info("[FTP::FileTo]: " + "sending: " + binaryFile+ " to: " + remoteFileName);
            //transfer type (BINARY vs ASCII)
            ftp.setContentType(transferType);

            
            final String remoteFileNameReturned=ftp.uploadFile(binaryFile, remoteFileName,writeMode);
            if(LOGGER.isLoggable(Level.INFO))
        		LOGGER.info("[FTP::FileTo]: " + "sent: " + binaryFile+ " to: " + remoteFileNameReturned);
            res = true;

        } catch (FTPException e) {
            LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }catch (IOException e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }
         catch (Throwable e) {
        	LOGGER.log(Level.SEVERE,"FTP ERROR: " + e.getLocalizedMessage(),e);
            res = false;
        }
        finally{
        	//disconnect
        	if(ftp!=null)
        		try{
        			ftp.disconnect();
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
public static boolean putTextFileTo(String ftpserverHost, String textFile,
            String ftpserverUser, String ftpserverPassword, int ftpserverPort, WriteMode writeMode, FTPConnectMode connectMode, int timeout) {
	if(LOGGER.isLoggable(Level.INFO))
		LOGGER.info("[FTP::PutFileTo]: " + "start");

    boolean res = putFile(ftpserverHost, textFile, ftpserverUser,
			ftpserverPassword, ftpserverPort,FTPTransferType.ASCII,writeMode,connectMode,timeout); 
     
     if(LOGGER.isLoggable(Level.INFO))
 		LOGGER.info("[FTP::PutFileTo]: " + "end");
     
     return res;
    }

}
