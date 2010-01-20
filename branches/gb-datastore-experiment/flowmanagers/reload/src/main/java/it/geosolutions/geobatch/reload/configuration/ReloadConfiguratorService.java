/*
 * $Header: it.geosolutions.reload.configuration.ReloadConfiguratorService,v. 0.1 19/gen/2010 12.22.05 created by frank $
 * $Revision: 0.1 $
 * $Date: 19/gen/2010 12.22.05 $
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
package it.geosolutions.geobatch.reload.configuration;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.util.EventObject;

/**
 * @author frank
 * 
 */
public abstract class ReloadConfiguratorService<T extends EventObject, C extends ActionConfiguration>
		extends BaseService implements ActionService<T, C> {

	public ReloadConfiguratorService() {
		super(true);
	}

	public boolean canCreateAction(C configuration) {
		// XXX ImPLEMENT ME
		return true;
	}

}
