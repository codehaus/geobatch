/*
 * $Header: it.geosolutions.geobatch.reload.ReloadFileConfigurator,v. 0.1 19/gen/2010 13.12.46 created by frank $
 * $Revision: 0.1 $
 * $Date: 19/gen/2010 13.12.46 $
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
import it.geosolutions.geobatch.reload.configuration.ReloadActionConfiguration;
import it.geosolutions.geobatch.reload.configuration.ReloadConfiguratorAction;
import it.geosolutions.geobatch.reload.configuration.Server;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Queue;
import java.util.logging.Level;

/**
 * @author frank
 * 
 */
public class ReloadFileConfigurator extends
		ReloadConfiguratorAction<FileSystemMonitorEvent> {

	/**
	 * @param configuration
	 */
	public ReloadFileConfigurator(ReloadActionConfiguration configuration)
			throws IOException {
		super(configuration);
	}

	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		try {

			// ///////////////////////////////////
			// Initializing input variables
			// ///////////////////////////////////

			if (configuration == null) {
				LOGGER.log(Level.SEVERE, "ActionConfig is null.");
				throw new IllegalStateException("ActionConfig is null.");
			}

			if (configuration.getServers() == null) {
				LOGGER.log(Level.SEVERE, "Servers Map is null.");
				throw new IllegalStateException("Servers Map is null.");
			}

			for (final Server server : configuration.getServers()) {

				URL geoserver = new URL("http://" + server.getServerIP() + ":"
						+ server.getServerPort() + "/geoserver/rest/reload");

				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(server
								.getServerUserName(), server
								.getServerPassword().toCharArray());

					}
				});

				HttpURLConnection con = (HttpURLConnection) geoserver
						.openConnection();
				con.setRequestMethod("POST");

				if (con.getResponseCode() == 200) {
					LOGGER.log(Level.INFO, "SUCCESS 200: Reload configuration for: "+ server.getServerIP());
				} else if (con.getResponseCode() == 405){
					LOGGER.log(Level.SEVERE, "ERROR 405: The specified HTTP method is not allowed for the requested resource (): " +server.getServerIP());
					throw new IllegalStateException("The specified HTTP method is not allowed for the requested resource (): " +server.getServerIP());
				} else {
					LOGGER.log(Level.SEVERE, "ERROR: Unable to reload configuration for: " +server.getServerIP());
					throw new IllegalStateException("ERROR: Unable to reload configuration for: " +server.getServerIP());
				}

			}

		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		} finally {
			// TODO: close all
		}

		return events;
	}

}
