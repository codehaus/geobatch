/*
 * $Header: it.geosolutions.geobatch.shp2pg.util.Shp2Pg,v. 0.1 15/gen/2010 17.40.48 created by frank $
 * $Revision: 0.1 $
 * $Date: 15/gen/2010 17.40.48 $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. 
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.geobatch.shp2pg.util;

import it.geosolutions.geobatch.shp2pg.configuration.Shp2PgActionConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author frank
 * 
 */
public class Shp2Pg {

    protected final static Logger LOGGER = Logger.getLogger(Shp2Pg.class.toString());

    public void copy(File shapeFile, Shp2PgActionConfiguration configuration) {
        try {
            Map<String, Object> connect = new HashMap<String, Object>();
            connect.put("url", shapeFile.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(connect);
            String[] typeNames = dataStore.getTypeNames();
            String typeName = typeNames[0];

            LOGGER.log(Level.INFO, "Reading content " + typeName);

            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore
                    .getFeatureSource(typeName);

            SimpleFeatureType simpleFeatureType = featureSource.getSchema();

            LOGGER.log(Level.INFO, "SCHEMA HEADER: " + DataUtilities.spec(simpleFeatureType));

            DefaultQuery query = new DefaultQuery();
            query.setTypeName(typeName);

            CoordinateReferenceSystem prj = simpleFeatureType.getCoordinateReferenceSystem();

            query.setCoordinateSystem(prj);

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource
                    .getFeatures(query);

            DataStore postgisDataStore = this.createPostgisDataStore(configuration);

            // check if the schema is present in postgis
            boolean schema = false;
            if (postgisDataStore.getTypeNames().length == 0) {
                schema = true;
            } else {
                for (String tableName : postgisDataStore.getTypeNames()) {
                    if (tableName.equalsIgnoreCase(collection.getSchema().getTypeName())) {
                        schema = true;
                    }
                }
            }
            if (!schema)
                postgisDataStore.createSchema(collection.getSchema());
            
            Transaction transaction = new DefaultTransaction("create");

            FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
            featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) postgisDataStore
                    .getFeatureSource(typeName);

            featureStore.setTransaction(transaction);

            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
                postgisDataStore.dispose();
                postgisDataStore = null;
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private DataStore createPostgisDataStore(Shp2PgActionConfiguration configuration)
            throws IOException {
        DataStoreFactorySpi factoryPG = new PostgisNGDataStoreFactory();

        Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("user", configuration.getDbUID());
        map.put("database", configuration.getDbName());
        map.put("passwd", configuration.getDbPWD());
        map.put("host", configuration.getDbServerIp());
        map.put("port", configuration.getDbPort());
        map.put("dbtype", configuration.getDbType());

        return factoryPG.createDataStore(map);
    }

}
