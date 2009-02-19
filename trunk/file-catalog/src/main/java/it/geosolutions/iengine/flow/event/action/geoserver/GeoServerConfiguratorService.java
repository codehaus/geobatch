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
package it.geosolutions.iengine.flow.event.action.geoserver;

import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;
import it.geosolutions.iengine.flow.event.action.ActionService;

import java.util.EventObject;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ GeoServerConfiguratorService.java $ Revision: 0.1 $ 12/feb/07 12:07:32
 */
public abstract class GeoServerConfiguratorService<T extends EventObject, C extends ActionConfiguration>
        extends BaseService implements ActionService<T, C> {

    public GeoServerConfiguratorService() {
        super(true);
    }

    public boolean canCreateAction(C configuration) {
        // XXX ImPLEMENT ME
        return true;
    }

}
