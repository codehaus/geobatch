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
import it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.utils.io.Utilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
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
public class NURCWPSOutput2WMCFileConfigurator extends
	GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	/**
	 * GeoTIFF Writer Default Params
	 */
	public final static String GEOSERVER_VERSION = "2.x";
	
	private static final int DEFAULT_TILE_SIZE = 256;

	private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

	private static final String DEFAULT_COMPRESSION_TYPE = "LZW";

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHHmmss");

	public static final long matLabStartTime;
	
	static {
		GregorianCalendar calendar = new GregorianCalendar(0000, 00, 01, 00, 00, 00);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		matLabStartTime = calendar.getTimeInMillis();
	}
	
	protected NURCWPSOutput2WMCFileConfigurator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
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
			final File outDir = Utilities.createTodayDirectory(workingDir);

			// input DIMENSIONS
			final Dimension timeDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.TIME_DIM);
			final boolean timeDimExists = timeDim != null;
			
			final Dimension depthDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.DEPTH_DIM);
			final boolean depthDimExists = depthDim != null;

			final Dimension heightDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.HEIGHT_DIM);
			final boolean heightDimExists = heightDim != null;

			Dimension latDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LAT_DIM);
			if (latDim == null)
				latDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LAT_DIM_LONG);
			final boolean latDimExists = latDim != null;

			Dimension lonDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LON_DIM);
			if (lonDim == null)
				lonDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.LON_DIM_LONG);

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
			
			final Array timeOriginalData;
			final Index timeOriginalIndex; 
			final boolean hasTime; 
			if (timeOriginalVar != null){
				timeOriginalData = timeOriginalVar.read();
				timeOriginalIndex = timeOriginalData.getIndex();
				hasTime = true;
			}
			else{
				timeOriginalData = null;
				timeOriginalIndex = null;
				hasTime = false;
			}
			
			String baseTime = null;
			if (!hasTime){
				Date dateTime = new Date(System.currentTimeMillis());
				baseTime = sdf.format(dateTime);
			}
			else{
				baseTime = sdf.format(matLabStartTime + timeOriginalData.getLong(timeOriginalIndex.set(0))*86400000L);
			}

			Variable lonOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LON_DIM);
			if (lonOriginalVar == null)
				lonOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LON_DIM_LONG);

			Variable latOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LAT_DIM);
			if (latOriginalVar == null)
				latOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LAT_DIM_LONG);

			final Array latOriginalData = latOriginalVar.read();
			final Array lonOriginalData = lonOriginalVar.read();

			// //
			//
			// Depth related variables
			//
			// //
			Variable zetaOriginalVar = null;
			Array zetaOriginalData = null;
			
			if (hasZeta) {
				zetaOriginalVar = ncFileIn.findVariable(depthDimExists ? JGSFLoDeSSIOUtils.DEPTH_DIM : JGSFLoDeSSIOUtils.HEIGHT_DIM);
				if (zetaOriginalVar != null) {
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

			final List<WMCEntry> layerList = new ArrayList<WMCEntry>(10);
			
			for (Variable var : foundVariables) {
				if (var != null) {
					String varName = var.getName();
					if (var.getRank()==1 || varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LAT_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LON_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LAT_DIM_LONG)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.LON_DIM_LONG)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.TIME_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.HEIGHT_DIM)
							|| varName.equalsIgnoreCase(JGSFLoDeSSIOUtils.DEPTH_DIM))
						continue;
					variables.add(varName);
					
					// //
					// defining the SampleModel data type
					// //
					final SampleModel outSampleModel = Utilities.getSampleModel(var.getDataType(), 
							nLon, nLat,1); 

					Array originalVarArray = var.read();
					final boolean hasLocalZLevel = NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.DEPTH_DIM)
							|| NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.HEIGHT_DIM);
					final boolean hasLocalTime = NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.TIME_DIM) && hasTime;
					
					for (int z = 0; z < (hasLocalZLevel ? nZeta : 1); z++) {
						for (int t = 0; t < (hasLocalTime ? nTime : 1); t++) {
							WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

							int[] dimArray;
							if (hasLocalZLevel && hasLocalTime)
								dimArray = new int[] {t, z, nLat, nLon} ;
							else if (hasLocalZLevel)
								dimArray = new int[] {z, nLat, nLon} ;
							else if (hasLocalTime)
								dimArray = new int[] {t, nLat, nLon} ;
							else
								dimArray = new int[] {nLat, nLon} ;
							JGSFLoDeSSIOUtils.write2DData(userRaster, var, originalVarArray, false, false, dimArray, true);
							final String variableName = varName.replace("_", "").replace(" ", "");
							
							// ////
							// producing the Coverage here...
							// ////
							final StringBuilder coverageName = new StringBuilder(inputFileName)
							              .append("_").append(variableName)
							              .append("_").append(hasLocalZLevel ? zetaOriginalData.getLong(zetaOriginalData.getIndex().set(z)) : 0)
							              .append("_");
							if (!hasTime)
								coverageName.append(baseTime);
							else{
								coverageName.append(baseTime)
											.append("_");
								// Days since 01-01-0000 (Matlab time)	
								coverageName.append(timeDimExists ? sdf.format(matLabStartTime + timeOriginalData.getLong(timeOriginalIndex.set(t))*86400000L) : "00000000_0000000");
							}
							coverageName.append("-T").append(System.currentTimeMillis());

							final String coverageStoreId = coverageName.toString();

							File gtiffFile = Utilities.storeCoverageAsGeoTIFF(outDir, coverageName.toString(), variableName, userRaster, Double.NaN, envelope, DEFAULT_COMPRESSION_TYPE, DEFAULT_COMPRESSION_RATIO, DEFAULT_TILE_SIZE);

							// ////////////////////////////////////////////////////////////////////
							//
							// SENDING data to GeoServer via REST protocol.
							//
							// ////////////////////////////////////////////////////////////////////
							Map<String, String> queryParams = new HashMap<String, String>();
							queryParams.put("namespace", getConfiguration().getDefaultNamespace());
							queryParams.put("wmspath", getConfiguration().getWmsPath());
							final String returnedLayer[] = GeoServerRESTHelper.send(outDir, 
								gtiffFile, 
								getConfiguration().getGeoserverURL(), 
								getConfiguration().getGeoserverUID(), 
								getConfiguration().getGeoserverPWD(),
								coverageStoreId, 
								coverageName.toString(),
								queryParams, null,
								getConfiguration().getDataTransferMethod(),
								"geotiff",GEOSERVER_VERSION,
								getConfiguration().getStyles(), 
								getConfiguration().getDefaultStyle());
							
							if (returnedLayer != null) {
								final WMCEntry entry = new WMCEntry(returnedLayer[1],returnedLayer[2]);
								layerList.add(entry);
							}
						}
					}

					numVars++;
				}
			}
			
	        final WMCConfiguration wmcConfig = new WMCConfiguration();
	        wmcConfig.setBoundingBox("-180.0,-90.0,180.0,90.0");
	        wmcConfig.setCrs("EPSG:4326"); //TODO Check real CRS ID
	        wmcConfig.setGeoserverURL(getConfiguration().getGeoserverURL());
	        wmcConfig.setLayerList(layerList);
	        
	        if (LOGGER.isLoggable(Level.INFO))
	        	LOGGER.log(Level.INFO, "Ingesting MatFiles in the mosaic composer");
	        
	        final WMCFileConfigurator wmcCreator = new WMCFileConfigurator(wmcConfig);
	        Queue<FileSystemMonitorEvent> proceed = wmcCreator.execute(events);
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

	
	
	
}