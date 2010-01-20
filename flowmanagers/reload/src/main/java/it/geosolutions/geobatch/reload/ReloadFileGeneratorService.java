/*
 * $Header: it.geosolutions.geobatch.reload.ReloadFileGeneratorService,v. 0.1 19/gen/2010 13.10.29 created by frank $
 * $Revision: 0.1 $
 * $Date: 19/gen/2010 13.10.29 $
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
package it.geosolutions.geobatch.reload;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.reload.configuration.ReloadActionConfiguration;
import it.geosolutions.geobatch.reload.configuration.ReloadConfiguratorService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author frank
 * 
 */
public class ReloadFileGeneratorService
		extends
		ReloadConfiguratorService<FileSystemMonitorEvent, ReloadActionConfiguration> {

	private final static Logger LOGGER = Logger
			.getLogger(ReloadFileGeneratorService.class.toString());

	public Action<FileSystemMonitorEvent> createAction(
			ReloadActionConfiguration configuration) {
		try {
			return new ReloadFileConfigurator(configuration);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
			return null;
		}
	}

	@Override
	public boolean canCreateAction(ReloadActionConfiguration configuration) {
		final boolean superRetVal = super.canCreateAction(configuration);
		return superRetVal;
	}

}
