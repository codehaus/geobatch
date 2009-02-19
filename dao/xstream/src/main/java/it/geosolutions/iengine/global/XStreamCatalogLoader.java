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



package it.geosolutions.iengine.global;

import it.geosolutions.iengine.catalog.Catalog;
import it.geosolutions.iengine.catalog.Service;
import it.geosolutions.iengine.catalog.dao.file.xstream.XStreamCatalogDAO;
import it.geosolutions.iengine.catalog.dao.file.xstream.XStreamFlowConfigurationDAO;
import it.geosolutions.iengine.catalog.file.FileBaseCatalog;
import it.geosolutions.iengine.configuration.flow.file.FileBasedCatalogConfiguration;
import it.geosolutions.iengine.flow.file.FileBasedFlowManager;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * The application configuration facade.
 * 
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public class XStreamCatalogLoader extends CatalogHolder implements ApplicationContextAware {

    // enforcing singleton
    private XStreamCatalogLoader(Catalog catalog) {
        CatalogHolder.setCatalog(catalog);
    }

    private static final Logger LOGGER = Logger.getLogger(XStreamCatalogLoader.class.toString());

    ApplicationContext context;

    /**
     * IngestionEngine data dir. This directory is used by the Ingestion Engine to store Flows
     * configuration files.
     */
    private File dataDir;

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;

    }

    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        // see if exists a system property...

        // //
        //
        // We might have introduced the data dir via env var
        //
        // //
        try {

            if (dataDir == null) {
                // its defined!!
                String prop = System.getProperty("INGESTIONENGINE_DATA_DIR");
                if (prop != null)
                    dataDir = new File(prop);
                else {
                    prop = System.getenv("INGESTIONENGINE_DATA_DIR");
                    if (prop != null)
                        dataDir = new File(prop);
                    else {
                        if (this.context instanceof WebApplicationContext) {
                            String rootDir = ((WebApplicationContext) context).getServletContext()
                            .getInitParameter("INGESTIONENGINE_DATA_DIR");
		                    if (rootDir != null)
		                        dataDir = new File(rootDir);     
		                    else {
	                            rootDir = ((WebApplicationContext) context).getServletContext()
	                                    .getRealPath("/WEB-INF/data");
	                            if (rootDir != null)
	                                dataDir = new File(rootDir);
		                    }
                        } else
                            dataDir = new File("./data");
                    }
                }

            }

        } catch (SecurityException e) {
            // gobble exception
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
        }

        if (dataDir == null) 
            throw new NullPointerException("Could not initialize Data Directory: The provided path is null.");
        
        if (!dataDir.exists()) 
            throw new IllegalStateException("Could not initialize Data Directory: The provided path does not exists ("+dataDir+").");
        
        if ( !dataDir.isDirectory() || !dataDir.canRead())
            throw new IllegalStateException("Could not initialize Data Directory: The provided path is not a readable directory ("+dataDir+")");


        ((FileBaseCatalog) CatalogHolder.getCatalog()).setBaseDirectory(dataDir.getAbsolutePath());
        System.out.println("----------------------------------");
        System.out.println("- INGESTIONENGINE_DATA_DIR: " + dataDir.getAbsolutePath());
        System.out.println("----------------------------------");

        // //
        //
        // Now get the catalog we have been injected
        //
        // //
        final Catalog catalog = getCatalog();
        System.out.println("CATALOG IS " + catalog.getClass().getName());
        final FileBasedCatalogConfiguration configuration = new FileBasedCatalogConfiguration();
        configuration.setId(catalog.getId());
        catalog.setConfiguration(configuration);
        catalog.setDAO(new XStreamCatalogDAO(dataDir.getAbsolutePath()));
        catalog.load();

        // //
        //
        // load all services
        //
        // //
        final Map<String, ? extends Service> services = context.getBeansOfType(Service.class);
        for (Entry<String, ? extends Service> servicePair : services.entrySet()) {
            final Service service = servicePair.getValue();
            if (!service.isAvailable()) {
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info(new StringBuilder("Skipping service ").append(servicePair.getKey())
                            .append(service.getClass().toString()).toString());
                continue;
            }
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info(new StringBuilder("Loading service ").append(servicePair.getKey())
                        .append(service.getClass().toString()).toString());
            catalog.add(servicePair.getValue());
        }

        // load all flows
        final Iterator<File> it = FileUtils.iterateFiles(dataDir, new String[] { "xml" }, false);
        while (it.hasNext()) {
            final File o = it.next();
            if (o.getName().equalsIgnoreCase(catalog.getId() + ".xml"))
                continue;

            try {
                // try to load the flow and add it to the catalog
                final FileBasedFlowManager flow = new FileBasedFlowManager();
                flow.setId(FilenameUtils.getBaseName(o.getName()));
                flow.setDAO(new XStreamFlowConfigurationDAO(dataDir.getAbsolutePath()));
                flow.setCatalog(catalog);
                flow.load();

                // loaded
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info(new StringBuilder("Loading flow from file ").append(
                            o.getAbsolutePath()).toString());

                // add to the catalog
                catalog.add(flow);

            } catch (Throwable t) {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, "Skipping flow", t);
            }

        }

    }

    public File getDataDir() {
        return dataDir;
    }

}
