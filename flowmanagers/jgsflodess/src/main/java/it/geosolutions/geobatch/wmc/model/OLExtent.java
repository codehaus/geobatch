/*
 * $Header: it.geosolutions.geobatch.wmc.model.OLMaxExtent,v. 0.1 03/dic/2009 01:23:16 created by Fabiani $
 * $Revision: 0.1 $
 * $Date: 03/dic/2009 01:23:16 $
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
public class OLExtent extends OLBaseClass {

	private String maxx = "180.0"; 
	private String maxy = "90.0"; 
	private String minx = "-180.0"; 
	private String miny = "-90.0";
	
	public OLExtent(String content) {
		super(content);
	}

	/**
	 * @return the maxx
	 */
	public String getMaxx() {
		return maxx;
	}

	/**
	 * @param maxx the maxx to set
	 */
	public void setMaxx(String maxx) {
		this.maxx = maxx;
	}

	/**
	 * @return the maxy
	 */
	public String getMaxy() {
		return maxy;
	}

	/**
	 * @param maxy the maxy to set
	 */
	public void setMaxy(String maxy) {
		this.maxy = maxy;
	}

	/**
	 * @return the minx
	 */
	public String getMinx() {
		return minx;
	}

	/**
	 * @param minx the minx to set
	 */
	public void setMinx(String minx) {
		this.minx = minx;
	}

	/**
	 * @return the miny
	 */
	public String getMiny() {
		return miny;
	}

	/**
	 * @param miny the miny to set
	 */
	public void setMiny(String miny) {
		this.miny = miny;
	}

}
