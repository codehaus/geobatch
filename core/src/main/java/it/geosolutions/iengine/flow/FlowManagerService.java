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
/**
 *
 */
package it.geosolutions.iengine.flow;

import it.geosolutions.iengine.catalog.Service;
import it.geosolutions.iengine.configuration.flow.FlowConfiguration;

import java.util.EventObject;

/**
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public interface FlowManagerService<T extends EventObject, C extends FlowConfiguration> extends
        Service {
    public FlowManager<T, C> createFlowManager(C type);

    public boolean canCreateFlowManager(C type);
}