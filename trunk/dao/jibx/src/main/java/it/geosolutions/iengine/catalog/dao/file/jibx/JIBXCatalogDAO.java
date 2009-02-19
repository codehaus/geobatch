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



package it.geosolutions.iengine.catalog.dao.file.jibx;

import it.geosolutions.iengine.catalog.dao.CatalogConfigurationDAO;
import it.geosolutions.iengine.configuration.CatalogConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedCatalogConfiguration;

import java.io.File;
import java.io.FileInputStream;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

public class JIBXCatalogDAO extends JIBXDAO<CatalogConfiguration> implements
        CatalogConfigurationDAO {

    public JIBXCatalogDAO(String directory) {
        super(directory);
    }

    public CatalogConfiguration find(CatalogConfiguration exampleInstance, boolean lock) {
        return find(exampleInstance.getId(), lock);
    }

    public CatalogConfiguration find(String id, boolean lock) {
        try {
            final File entityfile = new File(getBaseDirectory(), id + ".xml");
            if (entityfile.exists() && entityfile.canRead()) {
                IBindingFactory bfact = BindingDirectory.getFactory(FileBasedCatalogConfiguration.class);
                IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
                FileBasedCatalogConfiguration obj = (FileBasedCatalogConfiguration) uctx.unmarshalDocument(new FileInputStream(entityfile), null);
                return obj;

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
