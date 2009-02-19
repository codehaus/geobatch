/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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



package it.geosolutions.iengine.catalog.dao.file.xstream;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.dao.DAO;
import it.geosolutions.iengine.catalog.dao.DAOService;
import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.configuration.CatalogConfiguration;
import it.geosolutions.iengine.configuration.flow.FlowConfiguration;

public class XStreamDAOService<T extends Configuration> extends BaseService implements
        DAOService<T, String> {

    private String baseDirectory;

    public XStreamDAOService() {
        super(true);
    }

    public XStreamDAOService(String baseDirectory) {
        super(true);
        this.baseDirectory = baseDirectory;
    }

    public DAO createDAO(Class<T> clazz) {
        if (clazz.isAssignableFrom(FlowConfiguration.class))
            return new XStreamFlowConfigurationDAO(this.baseDirectory);
        if (clazz.isAssignableFrom(CatalogConfiguration.class))
            return new XStreamCatalogDAO(this.baseDirectory);
        return null;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

}
