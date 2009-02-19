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
/**
 *
 */
package it.geosolutions.iengine.configuration.flow.file;

import it.geosolutions.iengine.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.iengine.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.iengine.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.iengine.configuration.flow.BaseFlowConfiguration;
import it.geosolutions.iengine.configuration.flow.FlowConfiguration;

/**
 * <p>
 * A Configuration for the Flow based on xml marshalled files.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 */
public class FileBasedFlowConfiguration extends BaseFlowConfiguration implements FlowConfiguration {

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
    public FileBasedFlowConfiguration() {
        super();
    }

    /**
     * 
     * @param id
     * @param name
     * @param eventGeneratorConfiguration
     * @param description
     * @param eventConsumerConfiguration
     */
    public FileBasedFlowConfiguration(String id, String name,
            FileBasedEventGeneratorConfiguration eventGeneratorConfiguration, String description,
            FileBasedEventConsumerConfiguration eventConsumerConfiguration) {
        super(id, name, eventGeneratorConfiguration, description, eventConsumerConfiguration);
    }

    /**
     * 
     * @param id
     * @param name
     * @param eventGeneratorConfiguration
     * @param description
     * @param eventConsumerConfiguration
     * @param workingDirectory
     */
    public FileBasedFlowConfiguration(String id, String name,
            EventGeneratorConfiguration eventGeneratorConfiguration, String description,
            EventConsumerConfiguration eventConsumerConfiguration, String workingDirectory) {
        super(id, name, eventGeneratorConfiguration, description, eventConsumerConfiguration);
        this.workingDirectory = workingDirectory;
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
        setDirty(true);
    }

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" +
				"id:" + getId()
				+ ", name:" + getName()
				+ ", sid:" + getServiceID()
				+ ", wdir:" + getWorkingDirectory()
				+ ", egcfg:" + getEventGeneratorConfiguration()
				+ "]";
	}


}
