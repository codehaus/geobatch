/*
 * $Header: it.geosolutions.reload.configuration.Server,v. 0.1 19/gen/2010 12.56.12 created by frank $
 * $Revision: 0.1 $
 * $Date: 19/gen/2010 12.56.12 $
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

/**
 * @author frank
 * 
 */
public class Server {

	private String serverIP;
	private String serverPort;
	private String serverUserName;
	private String serverPassword;

	
	
	/**
	 * @param serverIP
	 * @param serverPort
	 * @param serverUserName
	 * @param serverPassword
	 */
	public Server(String serverIP, String serverPort, String serverUserName,
			String serverPassword) {
		super();
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.serverUserName = serverUserName;
		this.serverPassword = serverPassword;
	}

	/**
	 * @return the serverIP
	 */
	public String getServerIP() {
		return serverIP;
	}

	/**
	 * @param serverIP
	 *            the serverIP to set
	 */
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	/**
	 * @return the serverPort
	 */
	public String getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort
	 *            the serverPort to set
	 */
	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	/**
	 * @return the serverUserName
	 */
	public String getServerUserName() {
		return serverUserName;
	}

	/**
	 * @param serverUserName
	 *            the serverUserName to set
	 */
	public void setServerUserName(String serverUserName) {
		this.serverUserName = serverUserName;
	}

	/**
	 * @return the serverPassword
	 */
	public String getServerPassword() {
		return serverPassword;
	}

	/**
	 * @param serverPassword
	 *            the serverPassword to set
	 */
	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

}
