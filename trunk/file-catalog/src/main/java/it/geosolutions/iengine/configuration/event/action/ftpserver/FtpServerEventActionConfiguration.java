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
package it.geosolutions.iengine.configuration.event.action.ftpserver;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;

/**
 *
 * @author Ivano Picco
 */

public class FtpServerEventActionConfiguration extends ActionConfiguration implements Configuration {

    private String ftpserverHost;

    private String ftpserverPWD;

    private String ftpserverUSR;

    private String workingDirectory;

    private String dataTransferMethod;

    public FtpServerEventActionConfiguration() {
        super();
    }

    public String getFtpserverHost() {
        return ftpserverHost;
    }

    public void setFtpserverHost(String ftpserverHost) {
        this.ftpserverHost = ftpserverHost;
    }

    public String getFtpserverUSR() {
        return ftpserverUSR;
    }

    public void setFtpserverUSR(String ftpserverUSR) {
        this.ftpserverUSR = ftpserverUSR;
    }

    public String getFtpserverPWD() {
        return ftpserverPWD;
    }

    public void setFtpserverPWD(String ftpserverPWD) {
        this.ftpserverPWD = ftpserverPWD;
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getDataTransferMethod() {
        return dataTransferMethod;
    }

    public void setDataTransferMethod(String dataTransferMethod) {
        this.dataTransferMethod = dataTransferMethod;
    }

}
