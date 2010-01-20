/*
 * $Header: it.geosolutions.reload.configuration.ReloadConfiguratorAction,v. 0.1 19/gen/2010 12.21.50 created by frank $
 * $Revision: 0.1 $
 * $Date: 19/gen/2010 12.21.50 $
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

import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author frank
 * 
 */
public abstract class ReloadConfiguratorAction<T extends EventObject> extends
		BaseAction<T> {

	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(ReloadConfiguratorAction.class
            .toString());
    
    
    protected final ReloadActionConfiguration configuration;
	
    /**
     * Constructs a producer. The operation name will be the same than the parameter descriptor
     * name.
     * 
     */
    public ReloadConfiguratorAction(ReloadActionConfiguration configuration) {
        this.configuration = configuration;

        // //////////////////////////
        // get required parameters
        // //////////////////////////

		if ((configuration.getServers() == null)) {
			LOGGER.log(Level.SEVERE, "Servers Map is null.");
			throw new IllegalStateException("Servers Map is null.");
		}

    }

    public ReloadActionConfiguration getConfiguration() {
        return configuration;
    }
}