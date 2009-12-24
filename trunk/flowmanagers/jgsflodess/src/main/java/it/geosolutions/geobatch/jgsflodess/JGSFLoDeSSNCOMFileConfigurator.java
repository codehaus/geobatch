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

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class JGSFLoDeSSNCOMFileConfigurator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmm_HHH");
	
	protected JGSFLoDeSSNCOMFileConfigurator(
			GeoServerActionConfiguration configuration) throws IOException {
		super(configuration);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
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

			final File outDir = Utilities.createTodayDirectory(workingDir);
			
			inputFileName = FilenameUtils.getName(inputFileName);
			// decompress input file into a temp directory
			final File tempFile = File.createTempFile(inputFileName, ".tmp");
			final File ncomsDatasetDirectory = Utilities.decompress("NCOM", event.getSource(), tempFile);
			
			// ////
			// STEP 1: Looking for grid area definition NetCDF file
			//    - The file contains lat/lon grids describing irregular grid.
			// ////
			File[] NCOMGridAreaFiles = ncomsDatasetDirectory.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					if (!pathname.isDirectory())
						return true;
					
					return false;
				}

			});
			
			if (NCOMGridAreaFiles.length != 1) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Could not find NCOM Grid Area definition file.");
				throw new IOException("Could not find NCOM Grid Area definition file.");
			}
			
			ncGridAreaDefinitionFile = NetcdfFile.open(NCOMGridAreaFiles[0].getAbsolutePath());

			// input dimensions
			final Dimension X_Index = ncGridAreaDefinitionFile.findDimension("X_Index");

			final Dimension Y_Index = ncGridAreaDefinitionFile.findDimension("Y_Index");

			final Dimension Z_Index = ncGridAreaDefinitionFile.findDimension("Z-Index");

			// input VARIABLES
			final Variable lonOriginalVar = ncGridAreaDefinitionFile.findVariable("Longitude");
			final DataType lonDataType = lonOriginalVar.getDataType();

			final Variable latOriginalVar = ncGridAreaDefinitionFile.findVariable("Latitude");
			final DataType latDataType = latOriginalVar.getDataType();

			final Array latOriginalData = latOriginalVar.read();
			final Array lonOriginalData = lonOriginalVar.read();

			double[] bbox = JGSFLoDeSSIOUtils.computeExtrema(latOriginalData, lonOriginalData, Y_Index, X_Index);
			
			// building Envelope
			final GeneralEnvelope envelope = new GeneralEnvelope(JGSFLoDeSSIOUtils.WGS_84);
			envelope.setRange(0, bbox[0], bbox[2]);
			envelope.setRange(1, bbox[1], bbox[3]);


			// ////
			// STEP 2: Looking for directories containing variables
			//    - Each directory should contain separate NetCDF files for the variables.
			// ////
			File[] NCOMVarFolders = ncomsDatasetDirectory.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					if (pathname.isDirectory())
						return true;
					
					return false;
				}

			});
			
			if (NCOMVarFolders.length == 0) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.severe("Could not find NCOM Variables.");
				throw new IOException("Could not find NCOM Variables.");
			}

			// ////
			// ... create the output file data structure
			// ////
            final File outputFile = new File(outDir, "currentSpeedModel_NCOM.nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            
        	List<String> timesFound = new ArrayList<String>();
            
            // counting number of forecasts available
            for (File NCOMVarFolder : NCOMVarFolders) {
            	String[] filesList = NCOMVarFolder.list(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						if (FilenameUtils.getExtension(name).equalsIgnoreCase("nc") ||
								FilenameUtils.getExtension(name).equalsIgnoreCase("netcdf")) {
							return true;
						}
						
						return false;
					}
            		
            	});
            	
            	for (String fileName : filesList) {
            		String baseName = FilenameUtils.getBaseName(fileName);
            		String timeInstant = baseName.substring(baseName.length() - "yyyyMMdd00_hhh".length());
            		
            		if (!timesFound.contains(timeInstant))
            			timesFound.add(timeInstant);
            	}
            }
            
            // defining the file header and structure
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, timesFound.size(),
            		true, Z_Index.getLength(), JGSFLoDeSSIOUtils.DOWN, 
            		true, Y_Index.getLength(),
            		true, X_Index.getLength()
            );

            Map<String, Variable> foundVariables = new HashMap<String, Variable>();
            for (File NCOMVarFolder : NCOMVarFolders) {
				for (File NCOMVar : NCOMVarFolder.listFiles()) {
					if (FilenameUtils.getExtension(NCOMVar.getName()).equalsIgnoreCase("nc") ||
							FilenameUtils.getExtension(NCOMVar.getName()).equalsIgnoreCase("netcdf")) {
						NetcdfFile ncVarFile = null;
						try {
							ncVarFile = NetcdfFile.open(NCOMVar.getAbsolutePath());
							
							for (Object obj : ncVarFile.getVariables()) {
								final Variable var = (Variable) obj;
								final String varName = var.getName(); 
								if (!varName.equalsIgnoreCase("X_Index") &&
									!varName.equalsIgnoreCase("Y_Index") &&
									!varName.equalsIgnoreCase("Z-Index") &&
									!varName.equalsIgnoreCase("Depth")) {
									
									if (foundVariables.get(varName) == null)
										foundVariables.put(varName, var);
								}
							}
						}  finally {
							if (ncVarFile != null)
								ncVarFile.close();
						}
					}
				}
            }

			// defining output variable
            for (String varName : foundVariables.keySet()) {
				ncFileOut.addVariable(varName, foundVariables.get(varName).getDataType(), outDimensions);
                NetCDFConverterUtilities.setVariableAttributes(foundVariables.get(varName), ncFileOut, new String[] { "positions" });
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
            
            final double resY = (bbox[3] - bbox[1]) / Y_Index.getLength();
            final double resX = (bbox[2] - bbox[0]) / X_Index.getLength();
            
			// lat Variable data
			Array lat1Data = NetCDFConverterUtilities.getArray(Y_Index.getLength(), latDataType);
			for (int y = 0; y < Y_Index.getLength(); y++)
				lat1Data.setFloat(lat1Data.getIndex().set(y), (float) (bbox[1] + y*resY));
			ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, lat1Data);

			// lon Variable data
			Array lon1Data = NetCDFConverterUtilities.getArray(X_Index.getLength(), lonDataType);
			for (int x = 0; x < X_Index.getLength(); x++)
				lon1Data.setFloat(lon1Data.getIndex().set(x), (float) (bbox[0] + x*resX));
			ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lon1Data);
            
			List<Long> depthValuesFound = new ArrayList<Long>();
			for (File NCOMVarFolder : NCOMVarFolders) {
				for (File NCOMVar : NCOMVarFolder.listFiles()) {
					if (FilenameUtils.getExtension(NCOMVar.getName()).equalsIgnoreCase("nc") ||
							FilenameUtils.getExtension(NCOMVar.getName()).equalsIgnoreCase("netcdf")) {
						NetcdfFile ncVarFile = null;
						try {
							ncVarFile = NetcdfFile.open(NCOMVar.getAbsolutePath());
							
							// input dimensions
							final Dimension Depth = ncVarFile.findDimension("Depth");
							
							// input variables
							final Variable depthOriginalVar = ncVarFile.findVariable("Depth");
							final Array depthOriginalData = depthOriginalVar.read();
							
							for (Object obj : ncVarFile.getVariables()) {
								final Variable var = (Variable) obj;
								final String varName = var.getName(); 
								if (!varName.equalsIgnoreCase("X_Index") &&
									!varName.equalsIgnoreCase("Y_Index") &&
									!varName.equalsIgnoreCase("Z-Index") &&
									!varName.equalsIgnoreCase("Depth")) {
									
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
											X_Index.getLength(), //width
											Y_Index.getLength(), //height
											1); //num bands

									Array originalVarArray = var.read();
									
									for (int z = 0; z < Depth.getLength(); z++) {
										
										if (!depthValuesFound.contains(depthOriginalData.getLong(depthOriginalData.getIndex().set(z)))) {
											depthValuesFound.add(depthOriginalData.getLong(depthOriginalData.getIndex().set(z)));
										}
										
										WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

										JGSFLoDeSSIOUtils.write2DData(userRaster, var, originalVarArray, false, false, new int[] {z, Y_Index.getLength(), X_Index.getLength()}, false);

										// Resampling to a Regular Grid ...
										if (LOGGER.isLoggable(Level.INFO))
											LOGGER.info("Resampling to a Regular Grid ...");
										userRaster = JGSFLoDeSSIOUtils.warping(
												bbox, 
												lonOriginalData, 
												latOriginalData, 
												X_Index.getLength(), Y_Index.getLength(), 
												2, userRaster, 0,
												false);
										
										final Variable outVar = ncFileOut.findVariable(varName);
										final Array outVarData = outVar.read();

										int tIndex = 0;
										String baseName = FilenameUtils.getBaseName(NCOMVar.getName());
					            		String timeInstant = baseName.substring(baseName.length() - "yyyyMMdd00_hhh".length());
					            		
					            		for (String timeFound : timesFound) {
					            			if (timeFound.equals(timeInstant))
					            				break;
					            			tIndex++;
					            		}
					            		
										for (int y = 0; y < Y_Index.getLength(); y++)
											for (int x = 0; x < X_Index.getLength(); x++)
												outVarData.setFloat(outVarData.getIndex().set(tIndex, z, y, x), userRaster.getSampleFloat(x, y, 0));
										
										ncFileOut.write(varName, outVarData);
									}
								}
							}
						} finally {
							if (ncVarFile != null)
								ncVarFile.close();
						}
					}
				}
			}

			// z level Variable data
            Array zeta1Data = NetCDFConverterUtilities.getArray(Z_Index.getLength(), DataType.FLOAT);
            for (int z = 0; z < depthValuesFound.size(); z++)
            	zeta1Data.setLong(zeta1Data.getIndex().set(z), depthValuesFound.get(z));
            ncFileOut.write(JGSFLoDeSSIOUtils.DEPTH_DIM, zeta1Data);
            
			// ... setting up the appropriate event for the next action
			events.add(new FileSystemMonitorEvent(outputFile, FileSystemMonitorNotifications.FILE_ADDED));
			return events;
		} catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
			JAI.getDefaultInstance().getTileCache().flush();
			return null;
		} finally {
			try {
				if (ncGridAreaDefinitionFile != null)
					ncGridAreaDefinitionFile.close();

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
