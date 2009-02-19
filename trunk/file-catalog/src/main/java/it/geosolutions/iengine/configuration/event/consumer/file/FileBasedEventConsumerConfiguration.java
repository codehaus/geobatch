package it.geosolutions.iengine.configuration.event.consumer.file;

import it.geosolutions.iengine.catalog.impl.BaseConfiguration;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;
import it.geosolutions.iengine.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.iengine.flow.event.consumer.file.FileEventRule;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Configuration for the event consumers based on xml marshalled files.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class FileBasedEventConsumerConfiguration extends BaseConfiguration implements
        EventConsumerConfiguration {

    /**
     * Default Constructor.
     */
    public FileBasedEventConsumerConfiguration() {
        super();
    }

    /**
     * List of configurable actions that will be sequentially performed at the end of event
     * consumption.
     */
    private ArrayList<? extends ActionConfiguration> actions;

    /**
     * List of rules defining the consumer behavior.
     */
    private ArrayList<FileEventRule> rules;

    /**
     * The configuring directory. This is the directory where the consumer will store the input
     * data.
     */
    private String workingDirectory;

    /**
     * Do we remove input files and put them on a backup directory?
     */
    private boolean performBackup;

    /**
     * Getter for the consumer actions.
     * 
     * @return actions
     */
    public List<? extends ActionConfiguration> getActions() {
        return this.actions;
    }

    /**
     * Setter for the consumer actions.
     * 
     * @param actions
     */
    public void setActions(List<? extends ActionConfiguration> actions) {
        this.actions = new ArrayList<ActionConfiguration>(actions);
    }

    /**
     * Getter for the consumer rules.
     * 
     * @return rules
     */
    public List<FileEventRule> getRules() {
        return rules;
    }

    /**
     * Setter for the consumer rules.
     * 
     * @param rules
     */
    public void setRules(List<FileEventRule> rules) {
        this.rules = new ArrayList<FileEventRule>(rules);
    }

    /**
     * Getter for the configuring directory attribute.
     * 
     * @return workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Setter for the configuring directory attribute.
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Is the backup of the input data enabled?
     * 
     * @return performBackup
     */
    public boolean isPerformBackup() {
        return performBackup;
    }

    /**
     * Setter for the perform backup option.
     * 
     * @param performBackup
     */
    public void setPerformBackup(boolean performBackup) {
        this.performBackup = performBackup;
    }

}
