package it.geosolutions.iengine.catalog.impl;

import it.geosolutions.iengine.catalog.Catalog;
import it.geosolutions.iengine.catalog.Resource;

public class BaseResource extends BaseIdentifiable implements Resource {

    private Catalog catalog;

    public BaseResource(Catalog catalog) {
        super();
        this.catalog = catalog;
    }

    public BaseResource() {
        super();
    }

    public BaseResource(String id, String name, String description) {
        super(id, name, description);
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;

    }

    public BaseResource(String id, String name, String description, Catalog catalog) {
        super(id, name, description);
        this.catalog = catalog;
    }
}
