package it.geosolutions.iengine.flow.event.consumer.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.impl.BaseConfiguration;

import java.util.ArrayList;
import java.util.List;

public class FileEventRule extends BaseConfiguration implements Configuration, Cloneable {

    private String regex;

    private int originalOccurrencies;

    private int actualOccurrencies;

    private boolean optional;

    private List<FileSystemMonitorNotifications> acceptableNotifications;

    public FileEventRule() {
        super();
    }

    public FileEventRule(String id, String name, String description, boolean dirty) {
        super(id, name, description, dirty);
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public int getOriginalOccurrencies() {
        return originalOccurrencies;
    }

    public void setActualOccurrencies(int occurrencies) {
        this.actualOccurrencies = occurrencies;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public List<FileSystemMonitorNotifications> getAcceptableNotifications() {
        return acceptableNotifications;
    }

    public void setAcceptableNotifications(
            List<FileSystemMonitorNotifications> acceptableNotifications) {
        this.acceptableNotifications = acceptableNotifications;
    }

    public int getActualOccurrencies() {
        return actualOccurrencies;
    }

    public void setOriginalOccurrencies(int originalOccurrencies) {
        this.originalOccurrencies = originalOccurrencies;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        final FileEventRule rule = new FileEventRule();
        rule.setId(getId());
        rule.setName(getName());
        rule.setDescription(getDescription());
        if (acceptableNotifications != null)
            rule.setAcceptableNotifications(new ArrayList<FileSystemMonitorNotifications>(
                    acceptableNotifications));
        rule.setOptional(optional);
        rule.setRegex(regex);
        rule.setOriginalOccurrencies(originalOccurrencies);
        rule.setActualOccurrencies(originalOccurrencies);
        return rule;
    }

    public void setServiceID(String serviceID) {
    }

    public String getServiceID() {
        return null;
    }

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "id:" + getId()
				+ ", name:" + getName()
				+ ", regex:" + getRegex()
				+ "]";
	}
}