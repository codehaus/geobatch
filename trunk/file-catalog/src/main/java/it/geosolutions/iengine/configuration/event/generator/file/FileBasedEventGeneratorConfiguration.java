package it.geosolutions.iengine.configuration.event.generator.file;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.iengine.catalog.impl.BaseConfiguration;
import it.geosolutions.iengine.configuration.event.generator.EventGeneratorConfiguration;

/**
 * <p>
 * Configuration for the event generators based on xml marshalled files.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class FileBasedEventGeneratorConfiguration extends BaseConfiguration implements
        EventGeneratorConfiguration {

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
     * Default Constructor.
     */
    public FileBasedEventGeneratorConfiguration() {
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
    public FileBasedEventGeneratorConfiguration(String id, String name, String description,
            boolean dirty, OsType osType, FileSystemMonitorNotifications eventType,
            String workingDirectory, String wildCard) {
        super(id, name, description, dirty);
        this.osType = osType;
        this.eventType = eventType;
        this.watchDirectory = workingDirectory;
        this.wildCard = wildCard;
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

}