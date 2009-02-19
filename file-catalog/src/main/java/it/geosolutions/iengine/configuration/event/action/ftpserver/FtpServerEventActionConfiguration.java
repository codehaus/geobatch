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
