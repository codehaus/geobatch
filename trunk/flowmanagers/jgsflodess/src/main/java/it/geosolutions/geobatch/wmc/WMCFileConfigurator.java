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
import it.geosolutions.geobatch.wmc.model.GeneralWMCConfiguration;
import it.geosolutions.geobatch.wmc.model.OLIsBaseLayer;
import it.geosolutions.geobatch.wmc.model.OLLayerID;
import it.geosolutions.geobatch.wmc.model.OLMaxExtent;
import it.geosolutions.geobatch.wmc.model.OLSingleTile;
import it.geosolutions.geobatch.wmc.model.OLTransparent;
import it.geosolutions.geobatch.wmc.model.ViewContext;
import it.geosolutions.geobatch.wmc.model.WMCBoundingBox;
import it.geosolutions.geobatch.wmc.model.WMCExtension;
import it.geosolutions.geobatch.wmc.model.WMCFormat;
import it.geosolutions.geobatch.wmc.model.WMCLayer;
import it.geosolutions.geobatch.wmc.model.WMCOnlineResource;
import it.geosolutions.geobatch.wmc.model.WMCServer;
import it.geosolutions.geobatch.wmc.model.WMCWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

public class WMCFileConfigurator extends BaseAction<FileSystemMonitorEvent>
		implements Action<FileSystemMonitorEvent> {

	private final static Logger LOGGER = Logger
			.getLogger(WMCFileConfigurator.class.toString());

	private WMCActionConfiguration configuration;

	public final static String GEOSERVER_VERSION = "2.x";

	protected WMCFileConfigurator(WMCActionConfiguration configuration)
			throws IOException {
		this.configuration = configuration;
	}

	/**
	 * EXECUTE METHOD
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		try {
			// looking for file
			if (events.size() == 0)
				throw new IllegalArgumentException(
						"Wrong number of elements for this action: " + events.size());

			// ////////////////////////////////////////////////////////////////////
			//
			// Initializing input variables
			//
			// ////////////////////////////////////////////////////////////////////
			final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(), 
					new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

			// ////////////////////////////////////////////////////////////////////
			//
			// Checking input files.
			//
			// ////////////////////////////////////////////////////////////////////
			if ((workingDir == null) || !workingDir.exists() || !workingDir.isDirectory()) {
				LOGGER.log(Level.SEVERE, "WorkingDirectory is null or does not exist.");
				throw new IllegalStateException("WorkingDirectory is null or does not exist.");
			}

			final List<WMCEntry> entryList = new ArrayList<WMCEntry>();
			
			LOGGER.info("WMCFileConfigurator ... fetching events...");
			while (events.size() > 0) {
				FileSystemMonitorEvent event = events.remove();

				// //
				// data flow configuration must not be null.
				// //
				if (configuration == null) {
					throw new IllegalStateException("DataFlowConfig is null.");
				}

				// ... BUSINESS LOGIC ... //
				final File inputFile = event.getSource();
				String inputFileName = inputFile.getAbsolutePath();
				final String filePrefix = FilenameUtils.getBaseName(inputFileName);
				final String fileSuffix = FilenameUtils.getExtension(inputFileName);

				String baseFileName = null;

				if ("layer".equalsIgnoreCase(fileSuffix)) {
					baseFileName = filePrefix;
				}

				if (baseFileName == null) {
					LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
					throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
				}

				Properties props = new Properties();

				//try retrieve data from file
				try {
					props.load(new FileInputStream(inputFile));
				}

				//catch exception in case properties file does not exist
				catch(IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}

				final String namespace = props.getProperty("namespace");
				final String storeid = props.getProperty("storeid");
				final String layerid = props.getProperty("layerid");
				final String driver = props.getProperty("driver");

				WMCEntry entry = new WMCEntry(namespace, layerid);
				entryList.add(entry);
			}

			//
			//
			// Write down the WMC file ...
			//
			//
			final List<WMCLayer> layerList = new ArrayList<WMCLayer>();

			final String crs = configuration.getCrs();

			final String boundingBox = configuration.getBoundingBox();
			
			final int width = Integer.parseInt(configuration.getWidth());

			final int height = Integer.parseInt(configuration.getHeight());
			
			final String geoserverUrl = configuration.getGeoserverURL();

			// //
			// GENERAL CONFIG ...
			// //
			ViewContext viewContext = new ViewContext("GeoBatchWMC", "1.0.0");
			WMCWindow window = new WMCWindow(height, width);
			GeneralWMCConfiguration generalConfig = new GeneralWMCConfiguration(window, "GeoBatchWMC", "GeoBatchWMC");
			String[] cfgbbox = boundingBox.split(",");
			WMCBoundingBox bbox = new WMCBoundingBox(crs, Double
					.valueOf(cfgbbox[0]), Double.valueOf(cfgbbox[1]), Double
					.valueOf(cfgbbox[2]), Double.valueOf(cfgbbox[3]));

			// //
			// BASE LAYER ...
			// //
			final String baseLayerName = configuration.getBaseLayerId();
			final String baseLayerTitle = configuration.getBaseLayerTitle();
			final String baseLayerURL = configuration.getBaseLayerURL();
			final String baseLayerFormat = configuration.getBaseLayerFormat();

			WMCLayer testLayer = new WMCLayer("0", "0", baseLayerName, baseLayerTitle, crs);
			WMCServer server = new WMCServer("wms", "1.1.1", "wms");
			List<WMCFormat> formatList = new ArrayList<WMCFormat>();
			// List<WMCStyle> styleList = new ArrayList<WMCStyle>();
			WMCExtension extension = new WMCExtension();
			extension.setId(new OLLayerID(baseLayerName));
			extension.setMaxExtent(new OLMaxExtent(null));
			extension.setIsBaseLayer(new OLIsBaseLayer("TRUE"));
			extension.setSingleTile(new OLSingleTile("TRUE"));
			extension.setTransparent(new OLTransparent("FALSE"));

			formatList.add(new WMCFormat("1", baseLayerFormat));

			server.setOnlineResource(new WMCOnlineResource("simple", baseLayerURL));
			testLayer.setServer(server);
			testLayer.setFormatList(formatList);
			testLayer.setExtension(extension);

			layerList.add(testLayer);
			
			// //
			//
			// Write layers pages
			//
			// //
			for (WMCEntry entry : entryList) {
				final String nameSpace = entry.getNameSpace();
				final String layerName = entry.getLayerName();

				testLayer = new WMCLayer("0", "0", nameSpace + ":" + layerName, layerName, crs);
				server = new WMCServer("wms", "1.1.1", "wms");
				formatList = new ArrayList<WMCFormat>();
				// List<WMCStyle> styleList = new ArrayList<WMCStyle>();
				extension = new WMCExtension();
				extension.setId(new OLLayerID(layerName));
				extension.setMaxExtent(new OLMaxExtent(null));
				extension.setIsBaseLayer(new OLIsBaseLayer("FALSE"));
				extension.setSingleTile(new OLSingleTile("FALSE"));
				extension.setTransparent(new OLTransparent("TRUE"));

				formatList.add(new WMCFormat("1", "image/png"));
				// styleList.add(new WMCStyle("1", new WMCSLD(new WMCOnlineResource("simple", "http://localhost:8081/NurcCruises/resources/xml/SLDDefault.xml"))));

				server.setOnlineResource(new WMCOnlineResource("simple", geoserverUrl));
				testLayer.setServer(server);
				testLayer.setFormatList(formatList);
				// testLayer.setStyleList(styleList);
				testLayer.setExtension(extension);

				layerList.add(testLayer);
			}

			// //
			//
			// Finalize
			//
			// //
			window.setBbox(bbox);
			viewContext.setGeneral(generalConfig);
			viewContext.setLayerList(layerList);

			final File outputDir = configuration.getOutputDirectory() != null ? new File(configuration.getOutputDirectory()) : workingDir;
			
			if (outputDir != null && outputDir.exists() && outputDir.isDirectory()) {
				FileWriter outFile = null;
				PrintWriter out = null;
				try {
					outFile = new FileWriter(new File(outputDir, "GeoBatchWMC-" + new Date().getTime() + ".xml"));
					out = new PrintWriter(outFile);
				
					new WMCStream().toXML(viewContext, out);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				} finally {
					if (out != null) {
						out.flush();
						out.close();
					}
					
					outFile = null;
					out = null;
				}
			}
			
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			return null;
		}
	}
}