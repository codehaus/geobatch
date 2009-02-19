/*
 * $Header: $fileName$ $
 * $Revision: 0.1 $
 * $Date: $date$ $time.long$ $
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
package it.geosolutions.iengine.configuration.flow.file;

import it.geosolutions.iengine.catalog.impl.BaseConfiguration;
import it.geosolutions.iengine.configuration.CatalogConfiguration;

/**
 * <p>
 * A Configuration for the Catalog based on xml marshalled files.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 */
public class FileBasedCatalogConfiguration extends BaseConfiguration implements
        CatalogConfiguration {

    // private List<FlowConfiguration> flowConfigurations;

    /**
     * workingDirectory: this attribute represents the configuring directory for this flow. It can
     * be relative to the catalog.xml directory or absolute.
     * 
     * Attention: the configuring directory should be different from the one containing the
     * configuration files.
     */
    private String workingDirectory;

    /**
     * Default Constructor.
     */
    public FileBasedCatalogConfiguration() {
        super();
    }

    /**
     * Getter for the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Setter for the workingDirectory.
     * 
     * @param workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + "id:" + getId() + ", workingDirectory:"
                + getWorkingDirectory() + ", name:" + getName() + "]";
    }
}
