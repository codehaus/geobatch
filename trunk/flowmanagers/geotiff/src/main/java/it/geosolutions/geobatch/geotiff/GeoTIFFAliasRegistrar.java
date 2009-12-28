/*
 */

package it.geosolutions.geobatch.geotiff;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class GeoTIFFAliasRegistrar extends AliasRegistrar {

     public GeoTIFFAliasRegistrar(AliasRegistry registry) {
         LOGGER.info(getClass().getSimpleName() + ": registering alias.");
         registry.putAlias("GeoTiffOverviewConfiguration", it.geosolutions.geobatch.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration.class);
     }
 }
