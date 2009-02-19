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



package it.geosolutions.iengine.flow.event.action.ftpserver;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FileTransferClient;
import com.enterprisedt.net.ftp.FTPTransferType;


import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Ivano Picco
 * 
 */

public class FtpServerHelper {

    private static final Logger LOGGER = Logger.getLogger(FtpServerHelper.class.toString());

    /**
     *
     * @param ftpserverHost
     * @param binaryFile
     * @param ftpserverUser
     * @param ftpserverPassword
     * @return
     */
    public static boolean putBinaryFileTo(String ftpserverHost, String binaryFile,
            String ftpserverUser, String ftpserverPassword,int ftpserverPort ) {

        LOGGER.info("[FTP::PutBinaryFileTo]: " + "start");

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

            ftp.setTimeout(3000); //millis

            remoteFileName = binaryFile.replaceAll("\\\\", "/");
            remoteFileName = remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());

            // connect to the server
            LOGGER.info("[FTP::PutBinaryFileTo]: " + "Connect to :" + host+":"+port);
            ftp.connect();

            LOGGER.info("[FTP::PutBinaryFileTo]: " + "send: " + binaryFile+ " to: " + remoteFileName);
            ftp.setContentType(FTPTransferType.BINARY);
            ftp.uploadFile(binaryFile, remoteFileName);

            ftp.disconnect();
            res = true;

        } catch (FTPException e) {
            LOGGER.info("FTP ERROR: " + e.getLocalizedMessage());
            res = false;
        }catch (IOException e) {
            LOGGER.info("FTP ERROR: " + e.getLocalizedMessage());
            res = false;
        }
         catch (Exception e) {
            LOGGER.info("FTP ERROR: " + e.getLocalizedMessage());
            res = false;
        } finally {
            LOGGER.info("[FTP::PutBinaryFileTo]: " + "end");
            return res;
        }
    }

    /**
     *
     * @param ftpserverHost
     * @param textFile
     * @param ftpserverUser
     * @param ftpserverPassword
     * @return
     */
public static boolean putTextFileTo(String ftpserverHost, String textFile,
            String ftpserverUser, String ftpserverPassword, int ftpserverPort) {
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

            ftp.setTimeout(3000); //millis
            remoteFileName = textFile.substring(textFile.lastIndexOf("/") + 1, textFile.length());

            LOGGER.info("[FTP::PutBinaryFileTo]: " + "Connect to :" + host+":"+port);
            ftp.connect();

            LOGGER.info("[FTP::PutBinaryFileTo]: " + "send: " + textFile+ " to: " + remoteFileName);
            ftp.setContentType(FTPTransferType.ASCII);
            ftp.uploadFile(textFile, remoteFileName);

            ftp.disconnect();
            res = true;

        } catch (FTPException e) {
            LOGGER.info("FTP ERROR: " + e.getLocalizedMessage());
            res = false;
        }catch (IOException e) {
            LOGGER.info("FTP ERROR: " + e.getLocalizedMessage());
            res = false;
        }
         catch (Exception e) {
            LOGGER.info("FTP ERROR: " + e.getLocalizedMessage());
            res = false;
        } finally {
            return res;
        }
    }

}
