package it.geosolutions.iengine.catalog;

import it.geosolutions.iengine.catalog.dao.DAO;

public interface PersistentResource<C extends Configuration> extends Resource {

    /**
     * The Flow BaseEventConsumer Type.
     */
    public C getConfiguration();

    public void setConfiguration(C coonfiguration);

    public void persist();

    public void load();

    public boolean remove();

    public void setDAO(DAO<C, ?> dao);

    public DAO<C, ?> getDAO();

}
