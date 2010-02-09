/*
 * $Header: it.geosolutions.geobatch.shp2pg.Shp2PgFileGeneratorService,v. 0.1 15/gen/2010 09.33.52 created by frank $
 * $Revision: 0.1 $
 * $Date: 15/gen/2010 09.33.52 $
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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.shp2pg.configuration.Shp2PgActionConfiguration;
import it.geosolutions.geobatch.shp2pg.configuration.Shp2PgConfiguratorService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author frank
 * 
 */
public class Shp2PgFileGeneratorService extends
        Shp2PgConfiguratorService<FileSystemMonitorEvent, Shp2PgActionConfiguration> {

    private final static Logger LOGGER = Logger.getLogger(Shp2PgFileGeneratorService.class
            .toString());

    public Shp2PgFileConfigurator createAction(Shp2PgActionConfiguration configuration) {

        try {
            return new Shp2PgFileConfigurator(configuration);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            return null;
        }

    }

    @Override
    public boolean canCreateAction(Shp2PgActionConfiguration configuration) {
        final boolean superRetVal = super.canCreateAction(configuration);
        return superRetVal;
    }

}
