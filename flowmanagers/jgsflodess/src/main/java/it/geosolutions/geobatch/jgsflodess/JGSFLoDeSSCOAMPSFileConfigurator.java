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
import it.geosolutions.geobatch.io.utils.IOUtils;
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.utils.coamps.data.FlatFileGrid;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.nc2.NetcdfFile;

/**
 * 
 * Public class to insert NetCDF data file (gliders measurements) into DB
 * 
 */
public class JGSFLoDeSSCOAMPSFileConfigurator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	public final static String GEOSERVER_VERSION = "2.x";
	
	private final static GeoTiffFormat format = new GeoTiffFormat();

	private static final int DEFAULT_TILE_SIZE = 256;

	private static final int DEFAULT_TILE_CACHE_SIZE = 16;

	private static final int MINIMUM_TILE_SIZE = 50;

	private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

	private static final String DEFAULT_COMPRESSION_TYPE = "LZW";

	private static final int DEFAULT_OVERVIEWS_NUMBER = 0;

//	private static final int DEFAULT_OVERVIEWS_ALGORITHM = 0;

	private static final CoordinateReferenceSystem WGS_84;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_0HHmmss");
	
	private static final long startTime;

	static {
		GregorianCalendar calendar = new GregorianCalendar(1980, 00, 01, 00, 00, 00);
		calendar.setTimeZone(TimeZone.getTimeZone("Europe/Greenwich"));
		startTime = calendar.getTimeInMillis();
	}

	static {
		CoordinateReferenceSystem crs;
		try {
			crs = CRS.decode("EPSG:4326", true);

		} catch (NoSuchAuthorityCodeException e) {

			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			crs = DefaultGeographicCRS.WGS84;
		} catch (FactoryException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			crs = DefaultGeographicCRS.WGS84;
		}
		WGS_84 = crs;
	}
	
	protected JGSFLoDeSSCOAMPSFileConfigurator(
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
		NetcdfFile ncGridAreaDefinitionFile = null;
		try {
			// looking for file
			if (events.size() != 1)
				throw new IllegalArgumentException(
						"Wrong number of elements for this action: "
								+ events.size());
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
			String inputFileName = event.getSource().getAbsolutePath();
			final String filePrefix = FilenameUtils.getBaseName(inputFileName);
			final String fileSuffix = FilenameUtils.getExtension(inputFileName);
			final String fileNameFilter = getConfiguration().getStoreFilePrefix();

			String baseFileName = null;

			if (fileNameFilter != null) {
				if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
						&& ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix))) {
					// etj: are we missing something here?
					baseFileName = filePrefix;
				}
			} else if ("zip".equalsIgnoreCase(fileSuffix) || "tar".equalsIgnoreCase(fileSuffix)) {
				baseFileName = filePrefix;
			}

			if (baseFileName == null) {
				LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
				throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
			}

			final File outDir = JGSFLoDeSSIOUtils.createTodayDirectory(workingDir);
			
			inputFileName = FilenameUtils.getName(inputFileName);
			// decompress input file into a temp directory
			final File tempFile = File.createTempFile(inputFileName, ".tmp");
			final File coampsDatasetDirectory = JGSFLoDeSSIOUtils.decompress("COAMPS", event.getSource(), tempFile);

			// ////
			// CASE 1: A FlatFileDescriptor exists
			//    - A FlatFileDescriptor is a file endings with the key word "infofld".
			//      It contains all the COMAPS file Nests/Grid infos.
			// ////
			// TODO
			
			// ////
			// CASE 2: A FlatFileDescriptor does not exist
			//    - In such case we need to search for lon/lat grid files.
			// ////
			String[] gridInfoFileNames = coampsDatasetDirectory.list(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (name.startsWith("longit") || name.startsWith("latitu"))
						return true;
					
					return false;
				}
				
			});
			
			if (gridInfoFileNames.length < 2) {
				if(LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("COAMPS grid file information could not be found!");
				
				return null;
			}

			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(WGS_84);

			float xmin = Float.POSITIVE_INFINITY;
            float ymin = Float.POSITIVE_INFINITY;
            float xmax = Float.NEGATIVE_INFINITY;
            float ymax = Float.NEGATIVE_INFINITY;
            
			int width = -1;
			int height = -1;
			float[] lonData = null;
			float[] latData = null;
			
			for (String gridInfoFileName : gridInfoFileNames) {
				if (gridInfoFileName.toLowerCase().startsWith("longit")) {
					final FlatFileGrid lonFileGrid = new FlatFileGrid(new File(coampsDatasetDirectory, gridInfoFileName));
					lonData = lonFileGrid.getData();

					for (float lon : lonData) {
						xmin = lon < xmin ? lon : xmin;
						xmax = lon > xmax ? lon : xmax;
					}
					
					width  = width  == -1 ? lonFileGrid.getWidth()  : width;
					height = height == -1 ? lonFileGrid.getHeight() : height;

					envelope.setRange(0, xmin, xmax);
				}
				
				if (gridInfoFileName.toLowerCase().startsWith("latitu")) {
					final FlatFileGrid latFileGrid = new FlatFileGrid(new File(coampsDatasetDirectory, gridInfoFileName));
					latData = latFileGrid.getData();

					for (float lat : latData) {
						ymin = lat < ymin ? lat : ymin;
						ymax = lat > ymax ? lat : ymax;
					}
					
					width  = width  == -1 ? latFileGrid.getWidth()  : width;
					height = height == -1 ? latFileGrid.getHeight() : height;

					envelope.setRange(1, ymin, ymax);
				}

			}
			
			File[] COAMPSFiles = coampsDatasetDirectory.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (!name.startsWith("longit") && !name.startsWith("latitu"))
						return true;
					
					return false;
				}
				
			});
			
			// ////
			// writing out rasters
			// ////
			SampleModel outSampleModel = RasterFactory.createBandedSampleModel(
					DataBuffer.TYPE_DOUBLE, //data type
					width, //width
					height, //height
					1); //num bands
			
			for (File COMAPSFile : COAMPSFiles) {
				final FlatFileGrid COAMPSFileGrid = new FlatFileGrid(COMAPSFile);
				
				if (COAMPSFileGrid != null && COAMPSFileGrid.getWidth() == width && COAMPSFileGrid.getHeight() == height) {
					WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

					JGSFLoDeSSIOUtils.write2DData(userRaster, COAMPSFileGrid, false, false);
					
					// Resampling to a Regular Grid ...
					if (LOGGER.isLoggable(Level.INFO))
						LOGGER.info("Resampling to a Regular Grid ...");
					userRaster = JGSFLoDeSSIOUtils.warping(
							COAMPSFileGrid, 
							new double[] {xmin, ymin, xmax, ymax}, 
							lonData, 
							latData, 
							width, height, 
							2, userRaster, 0,
							true);
					
					// ////
					// producing the Coverage here...
					// ////
					final StringBuilder coverageName = new StringBuilder("windModel_COAMPS");
					              coverageName.append("_").append(COAMPSFileGrid.getParamName().replaceAll("_", ""));
					              coverageName.append("_").append(COAMPSFileGrid.getLevel());
					              coverageName.append("_").append(COAMPSFileGrid.getTimeGroup());
								  coverageName.append("_").append(COAMPSFileGrid.getForecastTime());
					
				    final String coverageStoreId = coverageName.toString();

					File gtiffFile = JGSFLoDeSSIOUtils.storeCoverageAsGeoTIFF(outDir, coverageName.toString(), COAMPSFileGrid.getParamName(), userRaster, envelope, DEFAULT_COMPRESSION_TYPE, DEFAULT_COMPRESSION_RATIO, DEFAULT_TILE_SIZE);
					
					// ////////////////////////////////////////////////////////////////////
					//
					// SENDING data to GeoServer via REST protocol.
					//
					// ////////////////////////////////////////////////////////////////////
					Map<String, String> queryParams = new HashMap<String, String>();
					queryParams.put("namespace", getConfiguration().getDefaultNamespace());
					queryParams.put("wmspath", getConfiguration().getWmsPath());
					send(outDir, 
						gtiffFile, 
						getConfiguration().getGeoserverURL(), 
						new Long(event.getTimestamp()).toString(), 
						coverageStoreId, 
						coverageName.toString(),
						getConfiguration().getStyles(), 
						configId,
						getConfiguration().getDefaultStyle(), 
						queryParams
					);
				}
			}
			
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			JAI.getDefaultInstance().getTileCache().flush();
			return null;
		}
	}
	
	/**
     */
	public void send(
			final File inputDataDir, final File data,
			final String geoserverBaseURL, final String timeStamp,
			final String coverageStoreId, final String storeFilePrefix,
			final List<String> dataStyles, final String configId,
			final String defaultStyle, final Map<String, String> queryParams)
			throws MalformedURLException, FileNotFoundException {
		URL geoserverREST_URL = null;
		boolean sent = false;

		String layerName = storeFilePrefix != null ? storeFilePrefix : coverageStoreId;

		if (GEOSERVER_VERSION.equalsIgnoreCase("1.7.x")) {
			if ("DIRECT".equals(getConfiguration().getDataTransferMethod())) {
				geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/"
						+ coverageStoreId + "/layers/" + layerName + "/file.geotiff"
						+ getQueryString(queryParams));
				sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL,
						new FileInputStream(data), getConfiguration()
								.getGeoserverUID(), getConfiguration()
								.getGeoserverPWD());
			} else if ("URL".equals(getConfiguration().getDataTransferMethod())) {
				geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/"
						+ coverageStoreId + "/layers/" + layerName + "/url.geotiff"
						+ getQueryString(queryParams));
				sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data
						.toURL().toExternalForm(), getConfiguration()
						.getGeoserverUID(), getConfiguration()
						.getGeoserverPWD());
			} else if ("EXTERNAL".equals(getConfiguration()
					.getDataTransferMethod())) {
				geoserverREST_URL = new URL(geoserverBaseURL + "/rest/folders/"
						+ coverageStoreId + "/layers/" + layerName
						+ "/external.geotiff" 
						+ getQueryString(queryParams));
				sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data
						.toURL().toExternalForm(), getConfiguration()
						.getGeoserverUID(), getConfiguration()
						.getGeoserverPWD());
			}
		} else {
			if ("DIRECT".equals(getConfiguration().getDataTransferMethod())) {
				geoserverREST_URL = new URL(geoserverBaseURL
						+ "/rest/workspaces/" + queryParams.get("namespace")
						+ "/coveragestores/" + coverageStoreId
						+ "/file.geotiff");
				sent = GeoServerRESTHelper.putBinaryFileTo(geoserverREST_URL,
						new FileInputStream(data), getConfiguration()
								.getGeoserverUID(), getConfiguration()
								.getGeoserverPWD());
			} else if ("URL".equals(getConfiguration().getDataTransferMethod())) {
				geoserverREST_URL = new URL(geoserverBaseURL
						+ "/rest/workspaces/" + queryParams.get("namespace")
						+ "/coveragestores/" + coverageStoreId + "/url.geotiff");
				sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data
						.toURL().toExternalForm(), getConfiguration()
						.getGeoserverUID(), getConfiguration()
						.getGeoserverPWD());
			} else if ("EXTERNAL".equals(getConfiguration()
					.getDataTransferMethod())) {
				geoserverREST_URL = new URL(geoserverBaseURL
						+ "/rest/workspaces/" + queryParams.get("namespace")
						+ "/coveragestores/" + coverageStoreId
						+ "/external.geotiff");
				sent = GeoServerRESTHelper.putContent(geoserverREST_URL, data
						.toURL().toExternalForm(), getConfiguration()
						.getGeoserverUID(), getConfiguration()
						.getGeoserverPWD());
			}

		}

		if (sent) {
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER
						.info("GeoTIFF GeoServerConfiguratorAction: coverage SUCCESSFULLY sent to GeoServer!");
			boolean sldSent = configureStyles(layerName);
		} else {
			if (LOGGER.isLoggable(Level.INFO))
				LOGGER
						.info("GeoTIFF GeoServerConfiguratorAction: coverage was NOT sent to GeoServer due to connection errors!");
		}
	}
}
