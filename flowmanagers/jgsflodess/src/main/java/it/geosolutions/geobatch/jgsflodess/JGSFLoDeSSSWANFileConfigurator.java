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
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import javax.media.jai.JAI;

import org.apache.commons.io.FilenameUtils;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * Public class to insert NetCDF data file (gliders measurements) into DB
 * 
 */
public class JGSFLoDeSSSWANFileConfigurator extends
		GeoServerConfiguratorAction<FileSystemMonitorEvent> {

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
			final File outDir = Utilities.createTodayDirectory(workingDir);
            
			boolean hasZeta = false;
			
			// input dimensions
			final Dimension timeDim = ncFileIn.findDimension("time");
			final int nTimes = timeDim.getLength();

			final Dimension latDim = ncFileIn.findDimension(NetCDFUtilities.LATITUDE);
			final int nLat = latDim.getLength();

			final Dimension lonDim = ncFileIn.findDimension(NetCDFUtilities.LONGITUDE);
			final int nLon = lonDim.getLength();

			// input VARIABLES
			final Variable timeOriginalVar = ncFileIn.findVariable("time");
			final Array timeOriginalData = timeOriginalVar.read();
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

			final Variable levelOriginalVar = ncFileIn.findVariable("z"); // Height
			if (levelOriginalVar != null) {
				nZeta = levelOriginalVar.getDimension(0).getLength();
				levelOriginalData = levelOriginalVar.read();
				zetaDataType = levelOriginalVar.getDataType();
				hasZeta = true;
			}

			// ////
			// ... create the output file data structure
			// ////
            final File outputFile = new File(outDir, "waveModel_SWAN.nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, timeDim.getLength(),
            		hasZeta, nZeta, JGSFLoDeSSIOUtils.UP, 
            		true, latDim.getLength(),
            		true, lonDim.getLength()
            );

            final List<Variable> foundVariables = ncFileIn.getVariables();
			for (Variable var : foundVariables) {
				if (var != null) {
					String varName = var.getName();
					if (varName.equalsIgnoreCase(NetCDFUtilities.LATITUDE)
							|| varName.equalsIgnoreCase(NetCDFUtilities.LONGITUDE)
							|| varName.equalsIgnoreCase(NetCDFUtilities.TIME)
							|| varName.equalsIgnoreCase(NetCDFUtilities.ZETA))
						continue;
					// defining output variable
					ncFileOut.addVariable(varName, var.getDataType(), outDimensions);
	                NetCDFConverterUtilities.setVariableAttributes(var, ncFileOut, new String[] { "positions" });
				}
			}
			
            // writing bin data ...
            ncFileOut.create();

            // time Variable data
			Array time1Data = NetCDFConverterUtilities.getArray(nTimes, timeDataType);
			NetCDFConverterUtilities.setData1D(timeOriginalData, time1Data, timeDataType, nTimes, false);
			ncFileOut.write(JGSFLoDeSSIOUtils.TIME_DIM, time1Data);
            
			// z level Variable data
			if (hasZeta) {
				zeta1Data = NetCDFConverterUtilities.getArray(nZeta, zetaDataType);
				NetCDFConverterUtilities.setData1D(levelOriginalData, zeta1Data, zetaDataType, nZeta, false);
				ncFileOut.write(JGSFLoDeSSIOUtils.HEIGHT_DIM, zeta1Data);
			}

			// lat Variable data
			Array lat1Data = NetCDFConverterUtilities.getArray(nLat, latDataType);
			NetCDFConverterUtilities.setData1D(latOriginalData, lat1Data, latDataType, nLat, false);
			ncFileOut.write(JGSFLoDeSSIOUtils.LAT_DIM, lat1Data);

			// lon Variable data
			Array lon1Data = NetCDFConverterUtilities.getArray(nLon, lonDataType);
			NetCDFConverterUtilities.setData1D(lonOriginalData, lon1Data, lonDataType, nLon, false);
			ncFileOut.write(JGSFLoDeSSIOUtils.LON_DIM, lon1Data);

			// {} Variables
			for (Variable var : foundVariables) {
				if (var != null) {
					String varName = var.getName();
					if (varName.equalsIgnoreCase(NetCDFUtilities.LATITUDE)
							|| varName.equalsIgnoreCase(NetCDFUtilities.LONGITUDE)
							|| varName.equalsIgnoreCase(NetCDFUtilities.TIME)
							|| varName.equalsIgnoreCase(NetCDFUtilities.ZETA))
						continue;
					// writing output variable
					final Array originalVarData = var.read();
	                final Index varIndex = originalVarData.getIndex();
	                Attribute fv = var.findAttribute("_FillValue");
	                float fillValue = Float.NaN;
	                if (fv != null) {
	                    fillValue = (fv.getNumericValue()).floatValue();
	                }
	                
	                ncFileOut.write(varName, originalVarData);
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
				if (ncFileIn != null)
					ncFileIn.close();
				
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