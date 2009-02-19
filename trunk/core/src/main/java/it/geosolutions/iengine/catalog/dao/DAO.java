package it.geosolutions.iengine.catalog.dao;

import it.geosolutions.iengine.catalog.Configuration;

import java.io.Serializable;

public interface DAO<T extends Configuration, ID extends Serializable> {
    T find(ID id, boolean lock);

    T find(T exampleInstance, boolean lock);

    T persist(T entity);

    boolean remove(T entity);

    T refresh(T entity);

}
