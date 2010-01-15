/*
 * $Header: it.geosolutions.geobatch.shp2pg.configuration.Shp2PgConfiguratorAction,v. 0.1 15/gen/2010 09.18.30 created by frank $
 * $Revision: 0.1 $
 * $Date: 15/gen/2010 09.18.30 $
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
package it.geosolutions.geobatch.shp2pg.configuration;

import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author frank
 * 
 */
public abstract class Shp2PgConfiguratorAction<T extends EventObject> extends BaseAction<T> {

    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(Shp2PgConfiguratorAction.class
            .toString());

    protected final Shp2PgActionConfiguration configuration;

    /**
     * Constructs a producer. The operation name will be the same than the parameter descriptor
     * name.
     * 
     */
    public Shp2PgConfiguratorAction(Shp2PgActionConfiguration configuration) {
        this.configuration = configuration;

        // //////////////////////////
        // get required parameters
        // //////////////////////////

        if ((configuration.getDbServerIp() == null) || (configuration.getDbPort() == null)) {
            LOGGER.log(Level.SEVERE, "Data Base url is null.");
            throw new IllegalStateException("Data Base url is null.");
        }

    }

    public Shp2PgActionConfiguration getConfiguration() {
        return configuration;
    }
}
