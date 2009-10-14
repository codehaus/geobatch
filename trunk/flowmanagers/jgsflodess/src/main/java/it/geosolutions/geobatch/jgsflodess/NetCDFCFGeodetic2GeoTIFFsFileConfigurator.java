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
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.geobatch.utils.IOUtils;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * 
 * Public class to split NetCDF_CF Geodetic to GeoTIFFs and consequently
 * send them to GeoServer along with their basic metadata.
 * 
 * For the NetCDF_CF Geodetic file we assume that it contains georectified
 * geodetic grids and therefore has a maximum set of dimensions as follows:
 * 
 * lat {
 *  lat:long_name = "Latitude"
 *  lat:units = "degrees_north"
 * }
 * 
 * lon {
 *  lon:long_name = "Longitude"
 *  lon:units = "degrees_east"
 * }
 * 
 * time {
 *  time:long_name = "time"
 *  time:units = "seconds since 1980-1-1 0:0:0"
 * }
 * 
 * depth {
 *  depth:long_name = "depth";
 *  depth:units = "m";
 *  depth:positive = "down";
 * }
 * 
 * height {
 *  height:long_name = "height";
 *  height:units = "m";
 *  height:positive = "up";
 * }
 * 
 */
public class NetCDFCFGeodetic2GeoTIFFsFileConfigurator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	/**
	 * GeoTIFF Writer Default Params
	 */
	public final static String GEOSERVER_VERSION = "2.x";
	
	private final static GeoTiffFormat format = new GeoTiffFormat();

	private static final int DEFAULT_TILE_SIZE = 256;

	private static final int DEFAULT_TILE_CACHE_SIZE = 16;

	private static final int MINIMUM_TILE_SIZE = 50;

	private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

	private static final String DEFAULT_COMPRESSION_TYPE = "LZW";

	private static final int DEFAULT_OVERVIEWS_NUMBER = 0;

//	private static final int DEFAULT_OVERVIEWS_ALGORITHM = 0;
	
	/**
	 * Static DateFormat Converter
	 */
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_0HHmmss");
	
	protected NetCDFCFGeodetic2GeoTIFFsFileConfigurator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	/**
	 * EXECUTE METHOD 
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Starting with processing...");
		NetcdfFile ncFileIn = null;
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
				LOGGER.log(Level.SEVERE, "WorkingDirectory is null or does not exist.");
				throw new IllegalStateException("WorkingDirectory is null or does not exist.");
			}

			// ... BUSINESS LOGIC ... //
			String inputFileName = event.getSource().getAbsolutePath();
			final String filePrefix = FilenameUtils.getBaseName(inputFileName);
			final String fileSuffix = FilenameUtils.getExtension(inputFileName);
			final String fileNameFilter = getConfiguration().getStoreFilePrefix();

			String baseFileName = null;

			if (fileNameFilter != null) {
				if ((filePrefix.equals(fileNameFilter) || filePrefix.matches(fileNameFilter))
						&& ("nc".equalsIgnoreCase(fileSuffix) || "netcdf".equalsIgnoreCase(fileSuffix))) {
					// etj: are we missing something here?
					baseFileName = filePrefix;
				}
			} else if ("nc".equalsIgnoreCase(fileSuffix) || "netcdf".equalsIgnoreCase(fileSuffix)) {
				baseFileName = filePrefix;
			}

			if (baseFileName == null) {
				LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
				throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
			}

			inputFileName = FilenameUtils.getBaseName(inputFileName);
			ncFileIn = NetcdfFile.open(event.getSource().getAbsolutePath());
			final File outDir = JGSFLoDeSSIOUtils.createTodayDirectory(workingDir);

			// input DIMENSIONS
			final Dimension timeDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.TIME_DIM);
			final boolean timeDimExists = timeDim != null;

			final Dimension depthDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.DEPTH_DIM);
			final boolean depthDimExists = depthDim != null;

			final Dimension heightDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.HEIGHT_DIM);
			final boolean heightDimExists = heightDim != null;

			final Dimension latDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LAT_DIM);
			final boolean latDimExists = latDim != null;

			final Dimension lonDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LON_DIM);
			final boolean lonDimExists = lonDim != null;

			// dimensions' checks
			final boolean hasZeta = depthDimExists || heightDimExists;
			
			if (!latDimExists || !lonDimExists) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Invalid input NetCDF-CF Geodetic file: longitude and/or latitude dimensions could not be found!");
				throw new IllegalStateException("Invalid input NetCDF-CF Geodetic file: longitude and/or latitude dimensions could not be found!");
			}

			int nTime = timeDimExists ? timeDim.getLength() : 0;
			int nZeta = 0;
			int nLat  = latDim.getLength();
			int nLon  = lonDim.getLength();

			// input VARIABLES
			final Variable timeOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.TIME_DIM);
			final Array timeOriginalData = timeOriginalVar.read();
			final Index timeOriginalIndex = timeOriginalData.getIndex();
			final DataType timeDataType = timeOriginalVar.getDataType();

			final Variable lonOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LON_DIM);
			final DataType lonDataType = lonOriginalVar.getDataType();

			final Variable latOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LAT_DIM);
			final DataType latDataType = latOriginalVar.getDataType();

			final Array latOriginalData = latOriginalVar.read();
			final Array lonOriginalData = lonOriginalVar.read();

			// //
			//
			// Depth related variables
			//
			// //
			Variable zetaOriginalVar = null;
			DataType zetaDataType = null;
			Array zetaOriginalData = null;
			
			if (hasZeta) {
				zetaOriginalVar = ncFileIn.findVariable(depthDimExists ? JGSFLoDeSSIOUtils.DEPTH_DIM : JGSFLoDeSSIOUtils.HEIGHT_DIM);
				if (zetaOriginalVar != null) {
					zetaDataType = zetaOriginalVar.getDataType();
					nZeta = depthDimExists ? depthDim.getLength() : heightDim.getLength();
					zetaOriginalData = zetaOriginalVar.read();
				}
			}

			double[] bbox = JGSFLoDeSSIOUtils.computeExtrema(latOriginalData, lonOriginalData, latDim, lonDim);
			
			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);
			envelope.setRange(0, bbox[0], bbox[2]);
			envelope.setRange(1, bbox[1], bbox[3]);
			
			// Storing variables Variables as GeoTIFFs
			final List<Variable> foundVariables = ncFileIn.getVariables();
			final ArrayList<String> variables = new ArrayList<String>();
			int numVars = 0;

			for (Variable var : foundVariables) {
				if (var != null) {
					String varName = var.getName();
					if (varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LAT_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LON_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.TIME_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.HEIGHT_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.DEPTH_DIM))
						continue;
					variables.add(varName);
					
					// //
					// defining the SampleModel data type
					// //
					final int dataType;
					final DataType varDataType = var.getDataType();
					if (varDataType == DataType.FLOAT)
						dataType = DataBuffer.TYPE_FLOAT;
					else if (varDataType == DataType.DOUBLE)
						dataType = DataBuffer.TYPE_DOUBLE;
					else if (varDataType == DataType.BYTE)
						dataType = DataBuffer.TYPE_BYTE;
					else if (varDataType == DataType.SHORT)
						dataType = DataBuffer.TYPE_SHORT;
					else if (varDataType == DataType.INT)
						dataType = DataBuffer.TYPE_INT;
					else
						dataType = DataBuffer.TYPE_UNDEFINED;
					
					SampleModel outSampleModel = RasterFactory.createBandedSampleModel(
							dataType, //data type
							nLon, //width
							nLat, //height
							1); //num bands

					Array originalVarArray = var.read();

					for (int z = 0; z < (hasZeta ? nZeta : 1); z++) {
						for (int t = 0; t < (timeDimExists ? nTime : 1); t++) {
							WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

							JGSFLoDeSSIOUtils.write2DData(userRaster, varName, var, originalVarArray, false, false, new int[] {t, z, nLat, nLon}, true);
							
							// ////
							// producing the Coverage here...
							// ////
							final StringBuilder coverageName = new StringBuilder(inputFileName);
							              coverageName.append("_").append(varName.replaceAll("_", ""));
							              coverageName.append("_").append(hasZeta ? zetaOriginalData.getLong(zetaOriginalData.getIndex().set(z)) : 0);
										  coverageName.append("_").append(timeDimExists ? sdf.format(JGSFLoDeSSIOUtils.startTime + timeOriginalData.getLong(timeOriginalIndex.set(t))*1000) : "00000000_0000000");

							final String coverageStoreId = coverageName.toString();

							File gtiffFile = JGSFLoDeSSIOUtils.storeCoverageAsGeoTIFF(outDir, coverageName.toString(), varName, userRaster, envelope, DEFAULT_COMPRESSION_TYPE, DEFAULT_COMPRESSION_RATIO, DEFAULT_TILE_SIZE);

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

					numVars++;
				}
			}

			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			JAI.getDefaultInstance().getTileCache().flush();
			return null;
		} finally {
			try {
				if (ncFileIn != null)
					ncFileIn.close();
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			} finally {
				JAI.getDefaultInstance().getTileCache().flush();
			}
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
