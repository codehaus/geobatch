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
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;

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

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * 
 * Public class to insert NetCDF data file (gliders measurements) into DB
 * 
 */
public class JGSFLoDeSSSWANFileConfigurator extends
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
	
	protected JGSFLoDeSSSWANFileConfigurator(
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
		NetcdfFile ncFileIn = null;
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

			inputFileName = FilenameUtils.getName(inputFileName);
			ncFileIn = NetcdfFile.open(event.getSource().getAbsolutePath());
			final File outDir = JGSFLoDeSSIOUtils.createTodayDirectory(workingDir);

			boolean hasZeta = false;
			// input dimensions
			final Dimension timeDim0 = ncFileIn.findDimension("time");
			final int nTimes = timeDim0.getLength();

			final Dimension latDim0 = ncFileIn.findDimension(NetCDFUtilities.LATITUDE);
			final int nLat = latDim0.getLength();

			final Dimension lonDim0 = ncFileIn.findDimension(NetCDFUtilities.LONGITUDE);
			final int nLon = lonDim0.getLength();

			// input VARIABLES
			final Variable timeOriginalVar = ncFileIn.findVariable("time");
			final Array timeOriginalData = timeOriginalVar.read();
			final Index timeOriginalIndex = timeOriginalData.getIndex();
			final DataType timeDataType = timeOriginalVar.getDataType();

			final Variable lonOriginalVar = ncFileIn.findVariable(NetCDFUtilities.LONGITUDE);
			final DataType lonDataType = lonOriginalVar.getDataType();

			final Variable latOriginalVar = ncFileIn.findVariable(NetCDFUtilities.LATITUDE);
			final DataType latDataType = latOriginalVar.getDataType();

			final Array latOriginalData = latOriginalVar.read();
			final Array lonOriginalData = lonOriginalVar.read();

			// //
			//
			// Depth related vars
			//
			// //
			Array levelOriginalData = null;
			int nZeta = 0;
			Array zeta1Data = null;
			DataType zetaDataType = null;

			final Variable levelOriginalVar = ncFileIn.findVariable("z"); // Depth
			if (levelOriginalVar != null) {
				nZeta = levelOriginalVar.getDimension(0).getLength();
				levelOriginalData = levelOriginalVar.read();
				zetaDataType = levelOriginalVar.getDataType();
				hasZeta = true;
			}

			// lat Variable
			Array lat1Data = NetCDFConverterUtilities.getArray(nLat, latDataType);
			NetCDFConverterUtilities.setData1D(latOriginalData, lat1Data, latDataType, nLat, true);

			// lon Variable
			Array lon1Data = NetCDFConverterUtilities.getArray(nLon, lonDataType);
			NetCDFConverterUtilities.setData1D(lonOriginalData, lon1Data, lonDataType, nLon, false);

			if (hasZeta) {
				// depth level Variable
				zeta1Data = NetCDFConverterUtilities.getArray(nZeta, zetaDataType);
				NetCDFConverterUtilities.setData1D(levelOriginalData, zeta1Data, zetaDataType, nZeta, false);
			}

			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(WGS_84);
			envelope.setRange(0, lon1Data.getDouble(lon1Data.getIndex().set(0)), lon1Data.getDouble(lon1Data.getIndex().set(nLon-1)));
			envelope.setRange(1, lat1Data.getDouble(lat1Data.getIndex().set(nLat-1)), lat1Data.getDouble(lat1Data.getIndex().set(0)));

			// {} Variables
			final ArrayList<String> variables = new ArrayList<String>(5);
			int numVars = 0;

			List<Variable> findVariables = ncFileIn.getVariables();
			for (Variable var : findVariables) {
				if (var != null) {
					String varName = var.getName();
					if (varName.equalsIgnoreCase(NetCDFUtilities.LATITUDE)
							|| varName.equalsIgnoreCase(NetCDFUtilities.LONGITUDE)
							|| varName.equalsIgnoreCase(NetCDFUtilities.TIME)
							|| varName.equalsIgnoreCase(NetCDFUtilities.ZETA))
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

					for (int z = 0; z < nZeta; z++) {
						for (int t = 0; t < nTimes; t++) {
							WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

							JGSFLoDeSSIOUtils.write2DData(userRaster, varName, var, originalVarArray, false, false, new int[] {t, z, nLat, nLon}, true);
							
							// ////
							// producing the Coverage here...
							// ////
							final StringBuilder coverageName = new StringBuilder("waveModel_SWAN");
							              coverageName.append("_").append(varName.replaceAll("_", ""));
							              coverageName.append("_").append(zeta1Data.getLong(zeta1Data.getIndex().set(z)));
										  coverageName.append("_").append(sdf.format(startTime + timeOriginalData.getLong(timeOriginalIndex.set(t))*1000));

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
