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



package it.geosolutions.geobatch.configuration.flow;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration;

/**
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public interface FlowConfiguration extends Configuration {
    /**
     * The Rule-Set Configuration
     */
    public EventGeneratorConfiguration getEventGeneratorConfiguration();

    /**
     * @param ruleSet
     *            the ruleSet to set
     */
    public void setEventGeneratorConfiguration(EventGeneratorConfiguration ruleSet);

    /**
     * The Rule-Set Configuration
     */
    public EventConsumerConfiguration getEventConsumerConfiguration();

    /**
     * The Rule-Set Configuration
     */
    public void setEventConsumerConfiguration(EventConsumerConfiguration eventConsumerConfiguration);

}
