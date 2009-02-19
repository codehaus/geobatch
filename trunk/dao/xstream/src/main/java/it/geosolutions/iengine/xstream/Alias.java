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


package it.geosolutions.iengine.xstream;

import com.thoughtworks.xstream.XStream;

/**
 * TODO: We need to have one (or more) XML file and to bind aliases dynamically.
 * 
 * @author etj
 */
public class Alias {
    public static void setAliases(XStream xstream) {
        xstream
                .alias(
                        "CatalogConfiguration",
                        it.geosolutions.iengine.configuration.flow.file.FileBasedCatalogConfiguration.class);

        xstream.alias("FlowConfiguration",
                it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration.class);
        xstream.alias("FileEventRule",
                it.geosolutions.iengine.flow.event.consumer.file.FileEventRule.class);
        xstream
                .alias(
                        "GeoTiffOverviewConfiguration",
                        it.geosolutions.iengine.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration.class);
        xstream
        .alias(
                "GeoTiffRetilerConfiguration",
                it.geosolutions.iengine.geotiff.retile.GeoTiffRetilerConfiguration.class);        
        xstream
                .alias(
                        "GeoServerActionConfiguration",
                        it.geosolutions.iengine.configuration.event.action.geoserver.GeoServerActionConfiguration.class);

        xstream
                .alias(
                        "FtpServerActionConfiguration",
                        it.geosolutions.iengine.configuration.event.action.ftpserver.FtpServerEventActionConfiguration.class);
        xstream
                .alias(
                        "EventConsumerConfiguration",
                        it.geosolutions.iengine.configuration.event.consumer.EventConsumerConfiguration.class,
                        it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class);
        xstream.aliasField("EventConsumerConfiguration",
                it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration.class,
                "eventConsumerConfiguration");
        
        xstream
                .alias(
                        "EventGeneratorConfiguration",
                        it.geosolutions.iengine.configuration.event.generator.EventGeneratorConfiguration.class,
                        it.geosolutions.iengine.configuration.event.generator.file.FileBasedEventGeneratorConfiguration.class);
        xstream.aliasField("EventGeneratorConfiguration",
                it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration.class,
                "eventGeneratorConfiguration");

        xstream
                .addImplicitCollection(
                        it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class,
                        "rules",
                        it.geosolutions.iengine.flow.event.consumer.file.FileEventRule.class);

        xstream
                .addImplicitCollection(
                        it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class,
                        "actions",
                        it.geosolutions.iengine.configuration.event.action.ActionConfiguration.class);

    }

}
