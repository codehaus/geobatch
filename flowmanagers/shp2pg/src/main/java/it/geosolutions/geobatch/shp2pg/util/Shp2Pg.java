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
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author frank
 * 
 */
public class Shp2Pg {

    protected final static Logger LOGGER = Logger.getLogger(Shp2Pg.class.toString());

    public boolean copy(File shapeFile, Shp2PgActionConfiguration configuration) {
        try {
            // connect to the shapefile
            final Map<String, Object> connect = new HashMap<String, Object>();
            connect.put("url", DataUtilities.fileToURL(shapeFile));

            final DataStore sourceDataStore = DataStoreFinder.getDataStore(connect);
            String[] typeNames = sourceDataStore.getTypeNames();
            String typeName = typeNames[0];

            LOGGER.log(Level.INFO, "Reading content " + typeName);

            SimpleFeatureType originalSchema = sourceDataStore.getSchema(typeName);
            LOGGER.log(Level.INFO, "SCHEMA HEADER: " + DataUtilities.spec(originalSchema));

            // prepare to open up a reader for the shapefile
            DefaultQuery query = new DefaultQuery();
            query.setTypeName(typeName);
            CoordinateReferenceSystem prj = originalSchema.getCoordinateReferenceSystem();
            query.setCoordinateSystem(prj);

            DataStore destinationDataSource = this.createPostgisDataStore(configuration);

            // check if the schema is present in postgis
            boolean schema = false;
            if (destinationDataSource.getTypeNames().length == 0) {
                schema = true;
            } else {
                for (String tableName : destinationDataSource.getTypeNames()) {
                    if (tableName.equalsIgnoreCase(typeName)) {
                        schema = true;
                    }
                }
            }
            if (!schema)
                destinationDataSource.createSchema(originalSchema);

            final Transaction transaction = new DefaultTransaction("create");
            FeatureWriter<SimpleFeatureType, SimpleFeature> fw = null;
            FeatureReader<SimpleFeatureType, SimpleFeature> fr = null;
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(destinationDataSource
                    .getSchema(typeName));
            try {
                fw = destinationDataSource.getFeatureWriter(typeName, transaction);
                fr = sourceDataStore.getFeatureReader(query, transaction);
                SimpleFeatureType sourceSchema = sourceDataStore.getSchema(typeName);
                FeatureStore postgisStore = (FeatureStore) destinationDataSource
                        .getFeatureSource(typeName);
                while (fr.hasNext()) {
                    final SimpleFeature oldfeature = fr.next();

                    for (AttributeDescriptor ad : sourceSchema.getAttributeDescriptors()) {
                        String attribute = ad.getLocalName();
                        builder.set(attribute, oldfeature.getAttribute(attribute));
                    }
                    postgisStore.addFeatures(DataUtilities.collection(builder.buildFeature(null)));

                }

                // close transaction
                transaction.commit();

                return true;
            } catch (Throwable e) {
                try {
                    transaction.rollback();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                if (fr != null)
                    try {
                        fr.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                if (fw != null)
                    try {
                        fw.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
            } finally {
                try {
                    transaction.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Unable to transcode features", e);
        }
        return false;

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
