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



package it.geosolutions.iengine.configuration.event.generator.ftp;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.configuration.event.generator.EventGeneratorConfiguration;

/**
 * <p>
 * Configuration for the event generators based on xml marshalled files.
 * </p>
 * 
 * @author Ivano Picco
 */
public class FtpBasedEventGeneratorConfiguration extends EventGeneratorConfiguration implements
        Configuration {

    /**
     * The type of OS which will be used by the embedded File System Watcher.
     */
    private OsType osType;

    /**
     * The type of File System Event accepted by the generator. The events can be of kind
     * FILE_ADDED, FILE_REMOVED, FILE_MODIFIED, etc...
     */
    private FileSystemMonitorNotifications eventType;

    /**
     * The configuring directory.
     */
    private String watchDirectory;

    /**
     * The wild-card used to catch the kind of input files.
     */
    private String wildCard;

    /**
     * FtpServer parameters
     *
     */
    private String ftpserverUSR;
    private String ftpserverPWD;
    private String ftpserverPort;


    /**
     * Default Constructor.
     */
    public FtpBasedEventGeneratorConfiguration() {
        super();
    }

    /**
     * 
     * @param id
     * @param name
     * @param description
     * @param dirty
     * @param osType
     * @param eventType
     * @param workingDirectory
     * @param wildCard
     */
    public FtpBasedEventGeneratorConfiguration(String id, String name, String description,
            boolean dirty, OsType osType, FileSystemMonitorNotifications eventType,
            String workingDirectory, String wildCard, String ftpserverUSR,String ftpserverPWD,String ftpserverPort) {
        super(id, name, description, dirty);
        this.osType = osType;
        this.eventType = eventType;
        this.watchDirectory = workingDirectory;
        this.wildCard = wildCard;
        this.ftpserverUSR = ftpserverUSR;
        this.ftpserverPWD = ftpserverPWD;
        this.ftpserverPort = ftpserverPort;
    }

    /**
     * Getter for the OS type attribute.
     * 
     * @return osType
     */
    public OsType getOsType() {
        return osType;
    }

    /**
     * Setter for the OS type attribute.
     * 
     * @param osType
     */
    public void setOsType(OsType osType) {
        this.osType = osType;
    }

    /**
     * Getter for the configuring directory attribute.
     * 
     * @return workinfDirectory
     */
    public String getWorkingDirectory() {
        return watchDirectory;
    }

    /**
     * Setter for the configuring directory attribute.
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.watchDirectory = workingDirectory;
    }

    /**
     * Getter for the wild card attribute.
     * 
     * @return wildCard
     */
    public String getWildCard() {
        return wildCard;
    }

    /**
     * Setter for the wild card attribute.
     * 
     * @param wildCard
     */
    public void setWildCard(String wildCard) {
        this.wildCard = wildCard;
    }

    /**
     * Getter for the event type attribute.
     * 
     * @return eventType
     */
    public FileSystemMonitorNotifications getEventType() {
        return eventType;
    }

    /**
     * Setter for the event type attribute.
     * 
     * @param eventType
     */
    public void setEventType(FileSystemMonitorNotifications eventType) {
        this.eventType = eventType;
    }

    /**
     * @return the ftpserverUSR
     */
    public String getFtpserverUSR() {
        return ftpserverUSR;
    }

    /**
     * @param ftpserverUSR the ftpserverUSR to set
     */
    public void setFtpserverUSR(String ftpserverUSR) {
        this.ftpserverUSR = ftpserverUSR;
    }

    /**
     * @return the ftpserverPWD
     */
    public String getFtpserverPWD() {
        return ftpserverPWD;
    }

    /**
     * @param ftpserverPWD the ftpserverPWD to set
     */
    public void setFtpserverPWD(String ftpserverPWD) {
        this.ftpserverPWD = ftpserverPWD;
    }

    /**
     * @return the ftpserverPort
     */
    public String getFtpserverPort() {
        return ftpserverPort;
    }

    /**
     * @param ftpserverPort the ftpserverPort to set
     */
    public void setFtpserverPort(String ftpserverPort) {
        this.ftpserverPort = ftpserverPort;
    }

}