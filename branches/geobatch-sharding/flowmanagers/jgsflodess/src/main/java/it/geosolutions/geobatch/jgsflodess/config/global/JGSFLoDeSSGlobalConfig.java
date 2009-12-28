/*
 * $Header: it.geosolutions.geobatch.jgsflodess.config.global.JGSFLoDeSSGlobalConfig,v. 0.1 04/dic/2009 17:50:01 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 04/dic/2009 17:50:01 $
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
package it.geosolutions.geobatch.jgsflodess.config.global;

/**
 * @author Fabiani
 *
 */
public final class JGSFLoDeSSGlobalConfig {

	private static String JGSFLoDeSSDirectory;
	
	/**
	 * 
	 */
	public JGSFLoDeSSGlobalConfig(String JGSFLoDeSSDirectory) {
		this.JGSFLoDeSSDirectory = JGSFLoDeSSDirectory;
	}

	/**
	 * @return the jGSFLoDeSSDirectory
	 */
	public static String getJGSFLoDeSSDirectory() {
		return JGSFLoDeSSDirectory;
	}

	
}