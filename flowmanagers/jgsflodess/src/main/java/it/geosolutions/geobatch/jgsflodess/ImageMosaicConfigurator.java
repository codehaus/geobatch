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
package it.geosolutions.geobatch.jgsflodess;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;

/**
 * 
 * Public class to ingest a geotiff image-mosaic into GeoServer
 * 
 */
public class ImageMosaicConfigurator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

    /**
     * 
     */
	public final static String GEOSERVER_VERSION = "2.x";

	protected ImageMosaicConfigurator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	/**
	 * 
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Starting with processing...");

		try {
			// looking for file
			if (events.size() != 1)
				throw new IllegalArgumentException("Wrong number of elements for this action: " + events.size());
			FileSystemMonitorEvent event = events.remove();
			final String configId = configuration.getName();

			// //
			// data flow configuration and dataStore name must not be null.
			// //
			if (configuration == null) {
				LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
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
				LOGGER.log(Level.SEVERE,
						"GeoServerDataDirectory is null or does not exist.");
				throw new IllegalStateException(
						"GeoServerDataDirectory is null or does not exist.");
			}

			// ... BUSINESS LOGIC ... //
			File inputDir = new File(event.getSource().getAbsolutePath());

			if (inputDir == null || !inputDir.exists() || !inputDir.isDirectory()) {
				LOGGER.log(Level.SEVERE, "Unexpected file '" + inputDir.getAbsolutePath() + "'");
				throw new IllegalStateException("Unexpected file '" + inputDir.getAbsolutePath() + "'");
			}

			//
			// CREATE REGEX PROPERTIES FILES
			//
			final File indexer        = new File(inputDir, "indexer.properties");
			final File timeregex      = new File(inputDir, "timeregex.properties");
			final File elevationregex = new File(inputDir, "elevationregex.properties");

			FileWriter outFile = null;
			PrintWriter out = null;

			// INDEXER
			try {
				outFile = new FileWriter(indexer);
				out = new PrintWriter(outFile);
				
				// Write text to file
				out.println("TimeAttribute=ingestion");
				out.println("ElevationAttribute=elevation");
				out.println("Schema=*the_geom:Polygon,location:String,ingestion:java.util.Date,elevation:Double");
				out.println("PropertyCollectors=TimestampFileNameExtractorSPI[timeregex](ingestion),ElevationFileNameExtractorSPI[elevationregex](elevation)");
			} catch (IOException e){
				LOGGER.severe(e.getLocalizedMessage());
			} finally {
				if (out != null) {
					out.flush();
					out.close();
				}
				
				outFile = null;
				out = null;
			}

			// TIME REGEX
			try {
				outFile = new FileWriter(timeregex);
				out = new PrintWriter(outFile);
				
				// Write text to file
				out.println("regex=[0-9]{8}T[0-9]{7}Z(?!.*[0-9]{8}T[0-9]{7}Z.*)");
			} catch (IOException e){
				LOGGER.severe(e.getLocalizedMessage());
			} finally {
				if (out != null) {
					out.flush();
					out.close();
				}
				
				outFile = null;
				out = null;
			}

			// ELEVATION REGEX
			try {
				outFile = new FileWriter(elevationregex);
				out = new PrintWriter(outFile);
				
				// Write text to file
				out.println("regex=\\\\d{4}");
			} catch (IOException e){
				LOGGER.severe(e.getLocalizedMessage());
			} finally {
				if (out != null) {
					out.flush();
					out.close();
				}
				
				outFile = null;
				out = null;
			}

			final String[] fileNames = inputDir.list(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (FilenameUtils.getExtension(name).equalsIgnoreCase("tiff") || 
							FilenameUtils.getExtension(name).equalsIgnoreCase("tif"))
						return true;
					
					return false;
				}
				
			});
			
			if (fileNames != null && fileNames.length > 0) {
				String[] cvNameParts = fileNames[0].split("_");
				
				if (cvNameParts != null && cvNameParts.length > 3) {
					String coverageStoreId = cvNameParts[0] + "_" + cvNameParts[1] + "_" + cvNameParts[2] + "_" + cvNameParts[4]; 
					
					// ////////////////////////////////////////////////////////////////////
					//
					// SENDING data to GeoServer via REST protocol.
					//
					// ////////////////////////////////////////////////////////////////////
					Map<String, String> queryParams = new HashMap<String, String>();
					queryParams.put("namespace", getConfiguration().getDefaultNamespace());
					queryParams.put("wmspath", getConfiguration().getWmsPath());
					final String[] layer = GeoServerRESTHelper.send(
							inputDir, 
							inputDir, 
							getConfiguration().getGeoserverURL(), 
							getConfiguration().getGeoserverUID(), 
							getConfiguration().getGeoserverPWD(),
							coverageStoreId, 
							coverageStoreId,
							queryParams, 
							"", 
							"EXTERNAL",
							"imagemosaic",
							GEOSERVER_VERSION, 
							getConfiguration().getStyles(), 
							getConfiguration().getDefaultStyle());
				}
			}
            
			// ... setting up the appropriate event for the next action
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			JAI.getDefaultInstance().getTileCache().flush();
			return null;
		} finally {
			JAI.getDefaultInstance().getTileCache().flush();
		}
	}
}