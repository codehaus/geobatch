package it.geosolutions.iengine.catalog.impl;

import it.geosolutions.iengine.catalog.Catalog;
import it.geosolutions.iengine.catalog.Service;

public class BaseService extends BaseResource implements Service {

    private boolean available;

    public BaseService() {
        super();
        available = true;
    }

    public BaseService(String id, String name, String description, Catalog catalog) {
        super(id, name, description, catalog);
    }

    public BaseService(boolean available) {
        super();
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * @param available
     *            the available to set
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

}