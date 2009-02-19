package it.geosolutions.iengine.catalog.impl;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.PersistentResource;
import it.geosolutions.iengine.catalog.dao.DAO;

public abstract class BasePersistentResource<C extends Configuration> extends BaseResource
        implements PersistentResource<C> {

    private C configuration;

    private DAO dao;

    private boolean removed;

    public C getConfiguration() {
        return configuration;
    }

    public DAO getDAO() {
        return dao;
    }

    public void persist() {
        if (configuration.isDirty())
            configuration = (C) dao.persist(configuration);

    }

    public void load() {
        setConfiguration((C) dao.find(this.getId(), false));
        configuration.setDirty(false);

    }

    public boolean remove() {
        removed = dao.remove(configuration);
        return removed;

    }

    public void setConfiguration(C configuration) {
        this.configuration = configuration;

    }

    public void setDAO(DAO flowConfigurationDAO) {
        this.dao = flowConfigurationDAO;

    }

}
