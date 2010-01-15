/*
 * $Header: it.geosolutions.geobatch.shp2pg.Shp2PgAliasRegistrar,v. 0.1 15/gen/2010 09.24.55 created by frank $
 * $Revision: 0.1 $
 * $Date: 15/gen/2010 09.24.55 $
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
package it.geosolutions.geobatch.shp2pg;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 * @author frank
 * 
 */
public class Shp2PgAliasRegistrar extends AliasRegistrar {

    public Shp2PgAliasRegistrar(AliasRegistry registry) {
        LOGGER.info(getClass().getSimpleName() + ": registering alias.");
        registry.putAlias("Shp2PgActionConfiguration",
                it.geosolutions.geobatch.shp2pg.configuration.Shp2PgActionConfiguration.class);
    }
}
