package it.geosolutions.iengine.catalog.dao;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.Service;

import java.io.Serializable;

public interface DAOService<T extends Configuration, ID extends Serializable> extends Service {

    DAO<T, ID> createDAO(Class<T> clazz);

}
