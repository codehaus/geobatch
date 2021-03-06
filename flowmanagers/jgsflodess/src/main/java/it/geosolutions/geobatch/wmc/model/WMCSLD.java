/*
 * $Header: it.geosolutions.geobatch.wmc.model.WMCSLD,v. 0.1 03/dic/2009 01:11:31 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 03/dic/2009 01:11:31 $
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
package it.geosolutions.geobatch.wmc.model;

/**
 * @author Fabiani
 *
 */
public class WMCSLD {
	private WMCOnlineResource onlineResource;

	/**
	 * @param onlineResource
	 */
	public WMCSLD(WMCOnlineResource onlineResource) {
		this.setOnlineResource(onlineResource);
	}

	/**
	 * @param onlineResource the onlineResource to set
	 */
	public void setOnlineResource(WMCOnlineResource onlineResource) {
		this.onlineResource = onlineResource;
	}

	/**
	 * @return the onlineResource
	 */
	public WMCOnlineResource getOnlineResource() {
		return onlineResource;
	}
	
}
