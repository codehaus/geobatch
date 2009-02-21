/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



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
