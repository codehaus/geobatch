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
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.utils.io.Utilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;
import it.geosolutions.utils.coamps.data.FlatFileGrid;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.GeneralEnvelope;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to insert NetCDF data file (gliders measurements) into DB
 * 
 */
public class JGSFLoDeSSCOAMPSFileConfigurator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	/**
	 * Static DateFormat Converter
	 */
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHHmmss");
	
	protected JGSFLoDeSSCOAMPSFileConfigurator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * 
	 */
	public Queue<FileSystemMonitorEvent> execute(
			Queue<FileSystemMonitorEvent> events) throws Exception {

		if (LOGGER.isLoggable(Level.INFO))
			LOGGER.info("Starting with processing...");
		NetcdfFile ncGridAreaDefinitionFile = null;
		NetcdfFileWriteable ncFileOut = null;
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
				LOGGER.log(Level.SEVERE, "GeoServerDataDirectory is null or does not exist.");
				throw new IllegalStateException("GeoServerDataDirectory is null or does not exist.");
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

			final File outDir = Utilities.createTodayDirectory(workingDir);
			
			inputFileName = FilenameUtils.getName(inputFileName);
			// decompress input file into a temp directory
			final File tempFile = File.createTempFile(inputFileName, ".tmp");
			final File coampsDatasetDirectory = Utilities.decompress("COAMPS", event.getSource(), tempFile);

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
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);

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
			// ... create the output file data structure
			// ////
            final File outputFile = new File(outDir, "windModel_COAMPS.nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            final List<String> varsFound = new ArrayList<String>();
            final List<String> timesFound = new ArrayList<String>();
            final List<Long> levelsFound = new ArrayList<Long>();
            
            for (File COAMPSFile : COAMPSFiles) {
				final FlatFileGrid COAMPSFileGrid = new FlatFileGrid(COAMPSFile);
				
				if (COAMPSFileGrid != null && COAMPSFileGrid.getWidth() == width && COAMPSFileGrid.getHeight() == height) {
					final String timeInstant = COAMPSFileGrid.getTimeGroup().substring(0, COAMPSFileGrid.getTimeGroup().length() - 2) + "_" + COAMPSFileGrid.getForecastTime().substring(1);
					final Long level = new Long(COAMPSFileGrid.getLevel());

					if (!varsFound.contains(COAMPSFileGrid.getParamName().replaceAll("_", "")))
						varsFound.add(COAMPSFileGrid.getParamName().replaceAll("_", ""));
					
					if (!timesFound.contains(timeInstant))
						timesFound.add(timeInstant);
					
					if (!levelsFound.contains(level))
						levelsFound.add(level);
				}
            }
            
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, timesFound.size(),
            		true, levelsFound.size(), JGSFLoDeSSIOUtils.UP, 
            		true, height,
            		true, width
            );
            
            for (String varName : varsFound) {
            	// defining output variable
            	Variable var = ncFileOut.addVariable(varName, DataType.FLOAT, outDimensions);
            	NetCDFConverterUtilities.setVariableAttributes(var, ncFileOut, new String[] { "positions" });
			}
			
            // writing bin data ...
            ncFileOut.create();
            
            // time Variable data
            Array time1Data = NetCDFConverterUtilities.getArray(timesFound.size(), DataType.FLOAT);
            for (int t = 0; t < timesFound.size(); t++) {
            	Date timeInstant = sdf.parse(timesFound.get(t));
            	float timeValue = (timeInstant.getTime() - JGSFLoDeSSIOUtils.startTime) / 1000.0f;
				time1Data.setFloat(time1Data.getIndex().set(t), timeValue);
            }
            ncFileOut.write(JGSFLoDeSSIOUtils.TIME_DIM, time1Data);
            
            // z level Variable data
            Array zeta1Data = NetCDFConverterUtilities.getArray(levelsFound.size(), DataType.FLOAT);
            for (int z = 0; z < levelsFound.size(); z++)
            	zeta1Data.setLong(zeta1Data.getIndex().set(z), levelsFound.get(z));
            ncFileOut.write(JGSFLoDeSSIOUtils.HEIGHT_DIM, zeta1Data);
            
            final double resY = (envelope.getMaximum(1) - envelope.getMinimum(1)) / height;
            final double resX = (envelope.getMaximum(0) - envelope.getMinimum(0)) / width;

            // lat Variable data
			Array lat1Data = NetCDFConverterUtilities.getArray(height, DataType.FLOAT);
			for (int y = 0; y < height; y++)
				lat1Data.setFloat(lat1Data.getIndex().set(y), (float) (envelope.getMinimum(1) + y*resY));
			ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, lat1Data);

			// lon Variable data
			Array lon1Data = NetCDFConverterUtilities.getArray(width, DataType.FLOAT);
			for (int x = 0; x < width; x++)
				lon1Data.setFloat(lon1Data.getIndex().set(x), (float) (envelope.getMinimum(0) + x*resX));
			ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lon1Data);
			
			// ////
			// writing out rasters
			// ////
			SampleModel outSampleModel = RasterFactory.createBandedSampleModel(
					DataBuffer.TYPE_DOUBLE, //data type
					width, //width
					height, //height
					1); //num bands
			
			for (File COAMPSFile : COAMPSFiles) {
				final FlatFileGrid COAMPSFileGrid = new FlatFileGrid(COAMPSFile);
				
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
							false);
					
					final String varName = COAMPSFileGrid.getParamName().replaceAll("_", "");
					final Variable outVar = ncFileOut.findVariable(varName);
					final Array outVarData = outVar.read();

					int tIndex = 0;
					final String timeInstant = COAMPSFileGrid.getTimeGroup().substring(0, COAMPSFileGrid.getTimeGroup().length() - 2) + "_" + COAMPSFileGrid.getForecastTime().substring(1);
            		
            		for (String timeFound : timesFound) {
            			if (timeFound.equals(timeInstant))
            				break;
            			tIndex++;
            		}
            		
            		for (int z = 0; z < levelsFound.size(); z++)
            			for (int y = 0; y < height; y++)
            				for (int x = 0; x < width; x++)
            					outVarData.setFloat(outVarData.getIndex().set(tIndex, z, y, x), userRaster.getSampleFloat(x, y, 0));
					
					ncFileOut.write(varName, outVarData);
				}
			}
			
			// ... setting up the appropriate event for the next action
			events.add(new FileSystemMonitorEvent(outputFile, FileSystemMonitorNotifications.FILE_ADDED));
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			JAI.getDefaultInstance().getTileCache().flush();
			return null;
		} finally {
			try {
				if (ncFileOut != null)
					ncFileOut.close();
			} catch (IOException e) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			} finally {
				JAI.getDefaultInstance().getTileCache().flush();
			}
		}
	}
	
}
