package it.geosolutions.iengine.catalog.impl;

import it.geosolutions.iengine.catalog.Configuration;

public class BaseConfiguration extends BaseIdentifiable implements Configuration {

    public String serviceID;
    
    private boolean dirty;

    public BaseConfiguration() {
        super();
    }

    public BaseConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    public BaseConfiguration(String id, String name, String description, boolean dirty) {
        super(id, name, description);
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * @return the serviceID
     */
    public String getServiceID() {
        return serviceID;
    }

    /**
     * @param serviceID
     *            the serviceID to set
     */
    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

}
