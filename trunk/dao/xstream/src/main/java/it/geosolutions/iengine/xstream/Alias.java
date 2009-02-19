/*
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
