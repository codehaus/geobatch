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


package it.geosolutions.geobatch.xstream;

import com.thoughtworks.xstream.XStream;

/**
 * TODO: We XStreamFlowConfigurationDAOneed to have one (or more) XML file and to bind aliases dynamically.
 * 
 * @author etj
 */
public class Alias {
    public static void setAliases(XStream xstream) {
        xstream.alias("CatalogConfiguration",
						it.geosolutions.geobatch.configuration.flow.file.FileBasedCatalogConfiguration.class);

        xstream.alias("FlowConfiguration",
						it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class);
        xstream.alias("FileEventRule",
						it.geosolutions.geobatch.flow.event.consumer.file.FileEventRule.class);
        xstream.alias("GeoTiffOverviewConfiguration",
                        it.geosolutions.geobatch.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration.class);
        xstream.alias("GeoTiffRetilerConfiguration",
						it.geosolutions.geobatch.geotiff.retile.GeoTiffRetilerConfiguration.class);
        xstream.alias("GeoServerActionConfiguration",
                        it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration.class);
        xstream.alias("RegistryActionConfiguration",
                it.geosolutions.geobatch.configuration.event.action.geoserver.RegistryActionConfiguration.class);
        
        xstream.alias("ImageMosaicActionConfiguration",
                it.geosolutions.geobatch.configuration.event.action.geoserver.plugin.ImageMosaicActionConfiguration.class);

        xstream.alias("MetocActionConfiguration",
                		it.geosolutions.geobatch.configuration.event.action.metoc.MetocActionConfiguration.class);

        xstream.alias("EventConsumerConfiguration",
                        it.geosolutions.geobatch.configuration.event.consumer.EventConsumerConfiguration.class,
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class);

        xstream.aliasField("EventConsumerConfiguration",
						it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class,
						"eventConsumerConfiguration");
        
        xstream.alias("ComposerConfiguration",
							it.geosolutions.geobatch.compose.ComposerConfiguration.class);
        xstream.alias("MosaicerConfiguration",
						it.geosolutions.geobatch.mosaic.MosaicerConfiguration.class);
        xstream.alias("FormatConverterConfiguration",
						it.geosolutions.geobatch.convert.FormatConverterConfiguration.class);
        
        xstream.alias("FsEventGeneratorConfiguration",
//                        it.geosolutions.geobatch.configuration.event.generator.EventGeneratorConfiguration.class,
                        it.geosolutions.geobatch.configuration.event.generator.file.FileBasedEventGeneratorConfiguration.class);
//        xstream.aliasField("FsEventGeneratorConfiguration",
//                it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration.class,
//                "eventGeneratorConfiguration");


//        xstream.alias("GlidersActionConfiguration",
//		        it.geosolutions.geobatch.gliders.configuration.GlidersActionConfiguration.class);
        
//        xstream.alias("GeoWebCacheActionConfiguration",
//        		it.geosolutions.geobatch.gwc.GeoWebCacheActionConfiguration.class);
        //xstream.alias("FusedTrackActionConfiguration",
        //		it.geosolutions.geobatch.track.configuration.FusedTrackActionConfiguration.class);

        xstream.addImplicitCollection(
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class,
                        "rules",
                        it.geosolutions.geobatch.flow.event.consumer.file.FileEventRule.class);

        xstream.addImplicitCollection(
                        it.geosolutions.geobatch.configuration.event.consumer.file.FileBasedEventConsumerConfiguration.class,
                        "actions",
                        it.geosolutions.geobatch.configuration.event.action.ActionConfiguration.class);

    }

}
