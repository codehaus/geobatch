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
            String ftpserverUser, String ftpserverPassword) {

        LOGGER.info("[FTP::PutBinaryFileTo]: " + "start");

        boolean res = false;

            final String host = ftpserverHost;
            final String login = ftpserverUser;
            final String password = ftpserverPassword;

            String remoteFileName = null;

            FileTransferClient ftp = null;

            try {
            // create client
            ftp = new FileTransferClient();

            // set remote host
            ftp.setRemoteHost(host);
            ftp.setUserName(login);
            ftp.setPassword(password);

            ftp.setTimeout(3000); //millis

            remoteFileName = binaryFile.replaceAll("\\\\", "/");
            remoteFileName = remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1, remoteFileName.length());

            // connect to the server
            LOGGER.info("[FTP::PutBinaryFileTo]: " + "Connect to:" + host);
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
            String ftpserverUser, String ftpserverPassword) {
        boolean res = false;

            final String host = ftpserverHost;
            final String login = ftpserverUser;
            final String password = ftpserverPassword;

            String remoteFileName = null;
            
            FileTransferClient ftp = null;

            try {
            // create client
            ftp = new FileTransferClient();

            // set remote host
            ftp.setRemoteHost(host);
            ftp.setUserName(login);
            ftp.setPassword(password);

            ftp.setTimeout(3000); //millis
            remoteFileName = textFile.substring(textFile.lastIndexOf("/") + 1, textFile.length());

            // connect to the server
            ftp.connect();

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
