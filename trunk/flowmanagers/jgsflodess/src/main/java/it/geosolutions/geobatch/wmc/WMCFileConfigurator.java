/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WMCFileConfigurator extends BaseAction<FileSystemMonitorEvent> implements
Action<FileSystemMonitorEvent> {

	private final static Logger LOGGER = Logger.getLogger(WMCFileConfigurator.class.toString());
	
	private WMCConfiguration configuration;
	
    public final static String GEOSERVER_VERSION = "2.x";
	
	protected WMCFileConfigurator(
			WMCConfiguration configuration) throws IOException {
		this.configuration = configuration;
	}

	/**
	 * EXECUTE METHOD 
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		try {
			
// looking for file
//          if (events.size() != 1)
//              throw new IllegalArgumentException("Wrong number of elements for this action: "
//                      + events.size());
//          FileSystemMonitorEvent event = events.remove();
			
			// //
			// data flow configuration must not be null.
			// //
			if (configuration == null) {
				throw new IllegalStateException("DataFlowConfig is null.");
			}
			// ////////////////////////////////////////////////////////////////////
			//
			// Initializing input variables
			//
			// ////////////////////////////////////////////////////////////////////
			final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(), new File(
					((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

			// ////////////////////////////////////////////////////////////////////
			//
			// Checking input files.
			//
			// ////////////////////////////////////////////////////////////////////
			if ((workingDir == null) || !workingDir.exists()
					|| !workingDir.isDirectory()) {
				LOGGER.log(Level.SEVERE, "WorkingDirectory is null or does not exist.");
				throw new IllegalStateException("WorkingDirectory is null or does not exist.");
			}

			final String crs = configuration.getCrs();
			
			final String boundingBox = configuration.getBoundingBox();
			
			final String geoserverUrl = configuration.getGeoserverURL();

			final List<WMCEntry> layerList = configuration.getLayerList();
			
			// //
			//
			// Write header 
			//
			// // 
			
			// //
			//
			// Write layers pages
			//
			// //
			
			for (WMCEntry entry : layerList){
				final String nameSpace = entry.getNameSpace();
				final String layerName = entry.getLayerName();
			}
			
			// //
			//
			// Write footer
			//
			// //

			return events;
		} catch (Throwable t) {
			return null;
		} 
	}

	
	
	
}