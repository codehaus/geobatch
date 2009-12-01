/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.wmc;


import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.util.List;

/**
 * A Base Configuration class sharing common configuration's parameters
 *
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public class WMCConfiguration extends ActionConfiguration implements Configuration {

	private String workingDirectory;
	
    private String crs;

    private String boundingBox;

    private String geoserverURL;
    
    private List<WMCEntry> layerList;
    
	public List<WMCEntry> getLayerList() {
		return layerList;
	}

	public void setLayerList(List<WMCEntry> layerList) {
		this.layerList = layerList;
	}

	public WMCConfiguration(String id, String name, String description,
			boolean dirty) {
		super(id, name, description, dirty);
	}

	public WMCConfiguration() {
		super();
	}


	/**
	 * @return the workingDirectory
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	/**
	 * @param workingDirectory
	 *            the workingDirectory to set
	 */
	public void setWorkingDirectory(final String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public String getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(String boundingBox) {
		this.boundingBox = boundingBox;
	}


	public String getGeoserverURL() {
		return geoserverURL;
	}

	public void setGeoserverURL(String geoserverURL) {
		this.geoserverURL = geoserverURL;
	}

	@Override
	public ActionConfiguration clone() throws CloneNotSupportedException {
		 final WMCConfiguration configuration = new WMCConfiguration(getId(), getName(), getDescription(), isDirty());
	        configuration.setServiceID(getServiceID());
	        configuration.setBoundingBox(boundingBox);
	        configuration.setCrs(crs);
	        configuration.setGeoserverURL(geoserverURL);
	        configuration.setLayerList(layerList);
	        return configuration;
	    }

}
