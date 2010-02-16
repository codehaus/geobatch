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
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.geobatch.metocs.MetocActionConfiguration;
import it.geosolutions.geobatch.metocs.MetocConfigurationAction;
import it.geosolutions.geobatch.metocs.jaxb.model.MetocElementType;
import it.geosolutions.geobatch.metocs.jaxb.model.Metocs;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.utils.io.Utilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.media.jai.JAI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

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
public class JGSFLoDeSSSWANFileConfigurator extends MetocConfigurationAction <FileSystemMonitorEvent> {

	public static final long startTime;

	static {
		GregorianCalendar calendar = new GregorianCalendar(1980, 00, 01, 00, 00, 00);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		startTime = calendar.getTimeInMillis();
	}
	
	protected JGSFLoDeSSSWANFileConfigurator(
			MetocActionConfiguration configuration) throws IOException {
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
			final File outDir = Utilities.createTodayDirectory(workingDir, FilenameUtils.getBaseName(inputFileName));
            
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
            final File outputFile = new File(outDir, "JGSFLoDeSS_SWAN-Forecast-T" + new Date().getTime() + FilenameUtils.getBaseName(inputFileName).replaceAll("-", "") + ".nc");
            ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            //NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());
            
            Array time1Data = NetCDFConverterUtilities.getArray(nTimes, timeDataType);
            int TAU;
            for (int t=0; t<nTimes; t++) {
            	long timeValue = timeOriginalData.getLong(timeOriginalData.getIndex().set(t));
            	if (t == 0 && nTimes > 1) {
        			TAU = (int) (timeOriginalData.getLong(timeOriginalData.getIndex().set(t+1)) - timeValue) * 3600 * 1000;
        		}
            	// adding time offset
            	  timeValue = startTime + (timeValue * 3600000);
            	// converting back to seconds and storing to data
				  time1Data.setLong(time1Data.getIndex().set(t), timeValue / 1000 );
            }
            
            // time Variable data
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmm_HHH");
            final SimpleDateFormat fromSdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	fromSdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        	
            final List<Dimension> outDimensions = JGSFLoDeSSIOUtils.createNetCDFCFGeodeticDimensions(
            		ncFileOut,
            		true, timeDim.getLength(),
            		hasZeta, nZeta, JGSFLoDeSSIOUtils.UP, 
            		true, latDim.getLength(),
            		true, lonDim.getLength()
            );

            //Grabbing the Variables Dictionary
			JAXBContext context = JAXBContext.newInstance(Metocs.class);
			Unmarshaller um = context.createUnmarshaller();

			File metocDictionaryFile = IOUtils.findLocation(configuration.getMetocDictionaryPath(), new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory())); 
			Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(metocDictionaryFile));
            Map<String, Variable> foundVariables        = new HashMap<String, Variable>();
            Map<String, String> foundVariableLongNames  = new HashMap<String, String>();
            Map<String, String> foundVariableBriefNames = new HashMap<String, String>();
            Map<String, String> foundVariableUoM 		= new HashMap<String, String>();
			
            for (Object obj : ncFileIn.getVariables()) {
				final Variable var = (Variable) obj;
				if (var != null) {
					String varName = var.getName();
					if (varName.equalsIgnoreCase(NetCDFUtilities.LATITUDE)
							|| varName.equalsIgnoreCase(NetCDFUtilities.LONGITUDE)
							|| varName.equalsIgnoreCase(NetCDFUtilities.TIME)
							|| varName.equalsIgnoreCase(NetCDFUtilities.ZETA))
						continue;
					
					if (foundVariables.get(varName) == null){
						String longName = null;
            			String briefName = null;
            			String uom = null;
            			
            			for(MetocElementType m : metocDictionary.getMetoc()) {
            				if(
            					(varName.equalsIgnoreCase("Significant Wave Height") && m.getName().equals("sigwavheight")) ||
            					(varName.equalsIgnoreCase("Peak Wave Period") && m.getName().equals("peakwavperiod")) ||
            					(varName.equalsIgnoreCase("Mean Wave Direction") && m.getName().equals("meanwavdir"))
            				)
        					{
        						longName = m.getName();
        						briefName = m.getBrief();
        						uom = m.getDefaultUom();
        						uom = uom.indexOf(":") > 0 ? URLDecoder.decode(uom.substring(uom.lastIndexOf(":")+1), "UTF-8") : uom;
        						break;
        					}
        				}
            			
            			if (longName != null && briefName != null) {	
            				foundVariables.put(varName, var);
            				foundVariableLongNames.put(varName, longName);
            				foundVariableBriefNames.put(varName, briefName);
            				foundVariableUoM.put(varName, uom);
            			}
            		}
				}
            }
					
            double noData = Double.NaN;
			// defining output variable
            for (String varName : foundVariables.keySet()) {
            	// SIMONE: replaced foundVariables.get(varName).getDataType() with DataType.DOUBLE
            	ncFileOut.addVariable(foundVariableBriefNames.get(varName), DataType.DOUBLE, outDimensions);
            	//NetCDFConverterUtilities.setVariableAttributes(foundVariables.get(varName), ncFileOut, foundVariableBriefNames.get(varName), new String[] { "positions" });
                ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "long_name", foundVariableLongNames.get(varName));
                ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "units", foundVariableUoM.get(varName));
                
                if (Double.isNaN(noData)) {
                	Attribute missingValue = foundVariables.get(varName).findAttribute("missing_value");
                	if (missingValue != null) {
                		noData = missingValue.getNumericValue().doubleValue();
                		ncFileOut.addVariableAttribute(foundVariableBriefNames.get(varName), "missing_value", noData);
                	}
                }
            }
        	// Setting up global Attributes ...
        	ncFileOut.addGlobalAttribute("base_time", fromSdf.format(timeOriginDate));
        	ncFileOut.addGlobalAttribute("tau", TAU);
        	ncFileOut.addGlobalAttribute("nodata", noData);
            // writing bin data ...
            ncFileOut.create();

            // time Variable data
			
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
			for (Object object : ncFileIn.getVariables()) {
				Variable var = (Variable) object;
 				if (var != null) {
 					double offset = 0.0;
					double scale = 1.0;
					final Attribute offsetAtt = var.findAttribute("add_offset");
					final Attribute scaleAtt = var.findAttribute("scale_factor");
					
					offset = (offsetAtt != null ? offsetAtt.getNumericValue().doubleValue() : offset);
					scale  = (scaleAtt != null ? scaleAtt.getNumericValue().doubleValue() : scale);
 					
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