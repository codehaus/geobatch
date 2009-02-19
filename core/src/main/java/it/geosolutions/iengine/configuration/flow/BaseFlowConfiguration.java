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



package it.geosolutions.iengine.configuration.flow;

import it.geosolutions.iengine.catalog.impl.BaseConfiguration;
import it.geosolutions.iengine.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.iengine.configuration.event.generator.EventGeneratorConfiguration;

/**
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public class BaseFlowConfiguration extends BaseConfiguration implements FlowConfiguration {

    private EventGeneratorConfiguration eventGeneratorConfiguration;

    private EventConsumerConfiguration eventConsumerConfiguration;

    public BaseFlowConfiguration(String id, String name,
            EventGeneratorConfiguration eventGeneratorConfiguration, String description,
            EventConsumerConfiguration eventConsumerConfiguration) {
        super(id, name, description);
        this.eventGeneratorConfiguration = eventGeneratorConfiguration;
        this.eventConsumerConfiguration = eventConsumerConfiguration;
    }

    public BaseFlowConfiguration() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.FlowManagerDescriptor#getRuleSet()
     */
    public EventGeneratorConfiguration getEventGeneratorConfiguration() {
        return eventGeneratorConfiguration;
    }

    /**
     * @param eventGeneratorConfiguration
     *            the eventGeneratorConfiguration to set
     */
    public void setEventGeneratorConfiguration(
            EventGeneratorConfiguration eventGeneratorConfiguration) {
        this.eventGeneratorConfiguration = eventGeneratorConfiguration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getId();
    }

    public EventConsumerConfiguration getEventConsumerConfiguration() {
        return eventConsumerConfiguration;
    }

    public void setEventConsumerConfiguration(EventConsumerConfiguration eventConsumerConfiguration) {
        this.eventConsumerConfiguration = eventConsumerConfiguration;

    }
}
