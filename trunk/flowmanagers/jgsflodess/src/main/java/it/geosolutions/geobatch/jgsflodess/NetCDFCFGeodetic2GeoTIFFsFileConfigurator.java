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
import it.geosolutions.geobatch.configuration.event.action.geoserver.RegistryActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.flow.event.action.geoserver.RegistryConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.jgsflodess.config.global.JGSFLoDeSSGlobalConfig;
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.geobatch.metocs.jaxb.model.MetocElementType;
import it.geosolutions.geobatch.metocs.jaxb.model.Metocs;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.utils.io.Utilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFConverterUtilities;

import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;

import javax.media.jai.JAI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
public class NetCDFCFGeodetic2GeoTIFFsFileConfigurator extends
		RegistryConfiguratorAction<FileSystemMonitorEvent> {

	/**
	 * GeoTIFF Writer Default Params
	 */
	public final static String GEOSERVER_VERSION = "2.x";
	
	private static final int DEFAULT_TILE_SIZE = 256;

	private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

	private static final String DEFAULT_COMPRESSION_TYPE = "LZW";

	/**
	 * Static DateFormat Converter
	 */
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHHmmss");
	
	protected NetCDFCFGeodetic2GeoTIFFsFileConfigurator(
			RegistryActionConfiguration configuration) throws IOException {
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

			// input DIMENSIONS
			final Dimension timeDim = ncFileIn.findDimension(JGSFLoDeSSIOUtils.TIME_DIM);
			final boolean timeDimExists = timeDim != null;

			final String baseTime = ncFileIn.findGlobalAttribute("base_time").getStringValue();
			final String TAU = String.valueOf(ncFileIn.findGlobalAttribute("tau").getNumericValue().intValue());
			
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

			final Variable lonOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LON_DIM);

			final Variable latOriginalVar = ncFileIn.findVariable(JGSFLoDeSSIOUtils.LAT_DIM);

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
					final SampleModel outSampleModel = Utilities.getSampleModel(var.getDataType(), 
							nLon, nLat,1); 

					Array originalVarArray = var.read();
					final boolean hasLocalZLevel = NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.DEPTH_DIM)
							|| NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.HEIGHT_DIM);
					
					for (int z = 0; z < (hasLocalZLevel ? nZeta : 1); z++) {
						for (int t = 0; t < (timeDimExists ? nTime : 1); t++) {
							WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);

							JGSFLoDeSSIOUtils.write2DData(userRaster, var, originalVarArray, false, false, (hasLocalZLevel ? new int[] {t, z, nLat, nLon} : new int[] {t, nLat, nLon}), true);
							
							// ////
							// producing the Coverage here...
							// ////
							final StringBuilder coverageName = new StringBuilder(inputFileName)
							              .append("_").append(varName.replaceAll("_", ""))
							              .append("_").append(hasLocalZLevel ? zetaOriginalData.getLong(zetaOriginalData.getIndex().set(z)) : 0)
							              .append("_").append(baseTime)
										  .append("_").append(timeDimExists ? sdf.format(JGSFLoDeSSIOUtils.startTime + timeOriginalData.getLong(timeOriginalIndex.set(t))*1000) : "00000000_0000000")
										  .append("_").append(TAU);

							final String coverageStoreId = coverageName.toString();

							File gtiffFile = Utilities.storeCoverageAsGeoTIFF(outDir, coverageName.toString(), varName, userRaster, envelope, DEFAULT_COMPRESSION_TYPE, DEFAULT_COMPRESSION_RATIO, DEFAULT_TILE_SIZE);

							// ////////////////////////////////////////////////////////////////////
							//
							// SENDING data to GeoServer via REST protocol.
							//
							// ////////////////////////////////////////////////////////////////////
							Map<String, String> queryParams = new HashMap<String, String>();
							queryParams.put("namespace", getConfiguration().getDefaultNamespace());
							queryParams.put("wmspath", getConfiguration().getWmsPath());
							GeoServerRESTHelper.send(outDir, 
									gtiffFile, 
									getConfiguration().getGeoserverURL(), 
									getConfiguration().getGeoserverUID(), 
									getConfiguration().getGeoserverPWD(),
									coverageStoreId, 
									coverageName.toString(),
									queryParams, "", getConfiguration().getDataTransferMethod(),
									"geotiff",
									GEOSERVER_VERSION, getConfiguration().getStyles(), 
									getConfiguration().getDefaultStyle());

							// ////////////////////////////////////////////////////////////////////
							//
							// HARVESTING metadata to the Registry.
							//
							// ////////////////////////////////////////////////////////////////////
							
							final String xmlTemplate = getConfiguration().getMetocHarvesterXMLTemplatePath();
							if (xmlTemplate != null && xmlTemplate.trim().length()>0){
								final File metadataTemplate = new File(xmlTemplate);
								if (metadataTemplate != null && metadataTemplate.exists()){
									harvest(new File(JGSFLoDeSSGlobalConfig.getJGSFLoDeSSDirectory()), 
										gtiffFile,
										metadataTemplate,
										getConfiguration().getGeoserverURL(), 
										event.getTimestamp(), 
										getConfiguration().getDefaultNamespace(),
										coverageStoreId, 
										coverageName.toString(),
										NetCDFConverterUtilities.hasThisDimension(var, JGSFLoDeSSIOUtils.DEPTH_DIM) ? "DOWN" : "UP"
									);
								}
							}
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
	 * Harvest: Metadata Creator
	 * 
	 * @param outDir
	 * @param gtiffFile
	 * @param geoserverURL
	 * @param string
	 * @param defaultNamespace
	 * @param coverageStoreId
	 * @param string2
	 * @param string3
	 * @throws JAXBException 
	 * @throws IOException 
	 * @throws FactoryException 
	 * @throws ParseException 
	 */
	public void harvest(
			final File outDir, 
			final File gtiffFile, 
			final File metadataTemplate,
			final String geoserverURL,
			final long timestamp, 
			final String namespace, 
			final String coverageStoreId,
			final String coverageName, 
			final String zOrder
	) throws JAXBException, IOException, FactoryException, ParseException {
		// CoverageName Format:
		//  CRUISEEXP_MODELNAME-MODELTYPE_VARNAME(-u/v/mag/dir)_ZLEV_BASETIMEYYYYMMDD_BASETIMEHHHMMSS_FCSTTIMEYYYYMMDD_FCSTTIMEHHHMMSS_TAU
		
		//Grabbing the Variables Dictionary
		JAXBContext context = JAXBContext.newInstance(Metocs.class);
		Unmarshaller um = context.createUnmarshaller();
		Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(new File(getConfiguration().getMetocDictionaryPath())));
		
		// keep original name
		final File outFile = new File(outDir, coverageName + ".xml");
		
		// reading GeoTIFF file
		final GeoTiffReader reader = new GeoTiffReader(gtiffFile);
		final CoordinateReferenceSystem crs = reader.getCrs();
		final String srsId = CRS.lookupIdentifier(crs, false);
		final GeneralEnvelope envelope = reader.getOriginalEnvelope();
		final GridEnvelope2D range = (GridEnvelope2D) reader.getOriginalGridRange();

		final String[] metocFields = coverageName.split("_");
		
		// Read/Write Metadata
        
		// Create FileReader Object
        FileReader inputFileReader   = new FileReader(metadataTemplate);
        FileWriter outputFileWriter  = new FileWriter(outFile);

        try {

            // Create Buffered/PrintWriter Objects
            BufferedReader inputStream   = new BufferedReader(inputFileReader);
            PrintWriter    outputStream  = new PrintWriter(outputFileWriter);

            String inLine = null;
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            final SimpleDateFormat sdfMetoc = new SimpleDateFormat("yyyyMMdd HHHmmss");
            
            while ((inLine = inputStream.readLine()) != null) {
            	// Handle KeyWords

            	/** GENERAL **/
            	if (inLine.contains("#UUID#")) {
            		inLine = inLine.replaceAll("#UUID#", "urn:uuid:" + UUID.randomUUID() + ":ISO19139");
            	}

            	if (inLine.contains("#CREATION_DATE#")) {
            		inLine = inLine.replaceAll("#CREATION_DATE#", sdf.format(new Date(timestamp)));
            	}

            	if (inLine.contains("#CRUISE_OR_EXP#")) {
            		inLine = inLine.replaceAll("#CRUISE_OR_EXP#", metocFields[0]);
            	}
            	
            	if (inLine.contains("#SPATAL_REPR_TYPE#")) {
            		inLine = inLine.replaceAll("#SPATAL_REPR_TYPE#", "grid");
            	}

            	/** SRS **/            	
            	if (inLine.contains("#SRS_CODE#")) {
            		inLine = inLine.replaceAll("#SRS_CODE#", srsId.substring(srsId.indexOf(":")+1));
            	}

            	if (inLine.contains("#SRS_AUTORITY#")) {
            		inLine = inLine.replaceAll("#SRS_AUTORITY#", srsId.substring(0, srsId.indexOf(":")));
            	}

            	/** OGC SERVICES **/
            	if (inLine.contains("#LAYER_NAME#")) {
            		inLine = inLine.replaceAll("#LAYER_NAME#", namespace + ":" + coverageName);
            	}

            	if (inLine.contains("#WCS_URL#")) {
            		final StringBuilder wcsURL = new StringBuilder(getConfiguration().getGeoserverURL());
					wcsURL.append("/ows?");
            		inLine = inLine.replaceAll("#WCS_URL#", wcsURL.toString());
            	}

            	if (inLine.contains("#WMS_URL#")) {
            		final StringBuilder wmsURL = new StringBuilder(getConfiguration().getGeoserverURL());
					wmsURL.append("/ows?");
            		inLine = inLine.replaceAll("#WMS_URL#", wmsURL.toString());
            	}

            	if (inLine.contains("#WCS_GETCOVERAGE#")) {
            		final StringBuilder wcsGetCoverage = new StringBuilder(getConfiguration().getGeoserverURL());
										wcsGetCoverage.append("/ows?SERVICE=WCS&amp;VERSION=1.0.0&amp;REQUEST=GetCoverage");
										wcsGetCoverage.append("&amp;BBOX=")
												 .append(envelope.getLowerCorner().getOrdinate(0)).append(",")
												 .append(envelope.getLowerCorner().getOrdinate(1)).append(",")
												 .append(envelope.getUpperCorner().getOrdinate(0)).append(",")
												 .append(envelope.getUpperCorner().getOrdinate(1));
										wcsGetCoverage.append("&amp;FORMAT=geotiff");
										wcsGetCoverage.append("&amp;COVERAGE=").append(namespace + ":" + coverageName);
										wcsGetCoverage.append("&amp;WIDTH=").append(range.getWidth());
										wcsGetCoverage.append("&amp;HEIGHT=").append(range.getHeight());
										wcsGetCoverage.append("&amp;CRS=").append(srsId);
            		inLine = inLine.replaceAll("#WCS_GETCOVERAGE#", wcsGetCoverage.toString());
            	}

            	if (inLine.contains("#WMS_GETMAP#")) {
            		final StringBuilder wmsGetMap = new StringBuilder(getConfiguration().getGeoserverURL());
            							wmsGetMap.append("/ows?SERVICE=WMS&amp;VERSION=1.1.1&amp;REQUEST=GetMap");
            							wmsGetMap.append("&amp;BBOX=")
            									 .append(envelope.getLowerCorner().getOrdinate(0)).append(",")
            									 .append(envelope.getLowerCorner().getOrdinate(1)).append(",")
            									 .append(envelope.getUpperCorner().getOrdinate(0)).append(",")
            									 .append(envelope.getUpperCorner().getOrdinate(1));
            							wmsGetMap.append("&amp;STYLES=");
            							wmsGetMap.append("&amp;FORMAT=image/png");
            							wmsGetMap.append("&amp;LAYERS=").append(namespace + ":" + coverageName);
            							wmsGetMap.append("&amp;WIDTH=").append(range.getWidth());
            							wmsGetMap.append("&amp;HEIGHT=").append(range.getHeight());
            							wmsGetMap.append("&amp;SRS=").append(srsId);
            		inLine = inLine.replaceAll("#WMS_GETMAP#", wmsGetMap.toString());
            	}

            	/** VARIABLE **/            	
            	if (inLine.contains("#VAR_NAME#")) {
            		inLine = inLine.replaceAll("#VAR_NAME#", metocFields[2]);
            	}

            	if (inLine.contains("#VAR_UOM#")) {
            		for (MetocElementType m : metocDictionary.getMetoc()) {
            			if (m.getBrief().equals(metocFields[2]))
            				inLine = inLine.replaceAll("#VAR_UOM#", m.getDefaultUom().indexOf(":") > 0 ? URLDecoder.decode(m.getDefaultUom().substring(m.getDefaultUom().lastIndexOf(":")+1), "UTF-8") : m.getDefaultUom());
            		}
            	}
            	
            	if (inLine.contains("#VAR_DESCRIPTION#")) {
            		for (MetocElementType m : metocDictionary.getMetoc()) {
            			if (m.getBrief().equals(metocFields[2]))
            				inLine = inLine.replaceAll("#VAR_DESCRIPTION#", m.getName());
            		}
            	}

            	if (inLine.contains("#VECT_FLAG#")) {
            		if (metocFields[2].indexOf("-") > 0)
            			inLine = inLine.replaceAll("#VECT_FLAG#", "true");
            		else
            			inLine = inLine.replaceAll("#VECT_FLAG#", "false");
            	}
            	
            	if (inLine.contains("#VECT_DATA_TYPE#")) {
            		if (metocFields[2].indexOf("-") > 0 && 
            			(metocFields[2].substring(metocFields[2].lastIndexOf("-")+1).equals("u") || metocFields[2].substring(metocFields[2].lastIndexOf("-")+1).equals("v")))
            			inLine = inLine.replaceAll("#VECT_DATA_TYPE#", "cartesian");
            		else if (metocFields[2].indexOf("-") > 0 && 
                			(metocFields[2].substring(metocFields[2].lastIndexOf("-")+1).equals("mag") || metocFields[2].substring(metocFields[2].lastIndexOf("-")+1).equals("dir")))
                			inLine = inLine.replaceAll("#VECT_DATA_TYPE#", "polar");
            		else
            			inLine = inLine.replaceAll("#VECT_DATA_TYPE#", "");
            	}

            	if (inLine.contains("#VECT_RELATED_DATA#")) {
            		if (metocFields[2].indexOf("-") > 0 && 
            			metocFields[2].substring(metocFields[2].lastIndexOf("-")+1).equals("u"))
            			inLine = inLine.replaceAll("#VECT_RELATED_DATA#", coverageName.replace(metocFields[2], metocFields[2].substring(0, metocFields[2].indexOf("-")) + "-v"));
            		else if (metocFields[2].indexOf("-") > 0 && 
                			metocFields[2].substring(metocFields[2].lastIndexOf("-")+1).equals("v"))
                			inLine = inLine.replaceAll("#VECT_RELATED_DATA#", coverageName.replace(metocFields[2], metocFields[2].substring(0, metocFields[2].indexOf("-")) + "-u"));
            		else if (metocFields[2].indexOf("-") > 0 && 
                			metocFields[2].substring(metocFields[2].lastIndexOf("-")+1).equals("mag"))
                			inLine = inLine.replaceAll("#VECT_RELATED_DATA#", coverageName.replace(metocFields[2], metocFields[2].substring(0, metocFields[2].indexOf("-")) + "-dir"));
            		else if (metocFields[2].indexOf("-") > 0 && 
                			metocFields[2].substring(metocFields[2].lastIndexOf("-")+1).equals("dir"))
                			inLine = inLine.replaceAll("#VECT_RELATED_DATA#", coverageName.replace(metocFields[2], metocFields[2].substring(0, metocFields[2].indexOf("-")) + "-mag"));
            		else
            			inLine = inLine.replaceAll("#VECT_RELATED_DATA#", "");
            	}
            	
            	/** MODEL **/
            	if (inLine.contains("#MODEL_NAME#")) {
            		inLine = inLine.replaceAll("#MODEL_NAME#", metocFields[1].substring(0, metocFields[1].indexOf("-")));
            	}

            	if (inLine.contains("#MODEL_TYPE#")) {
            		inLine = inLine.replaceAll("#MODEL_TYPE#", metocFields[1].substring(metocFields[1].indexOf("-")+1));
            	}

            	if (inLine.contains("#MODEL_TAU#")) {
            		inLine = inLine.replaceAll("#MODEL_TAU#", metocFields[8]);
            	}
            	
            	if (inLine.contains("#TAU_UOM#")) {
            		inLine = inLine.replaceAll("#TAU_UOM#", "hour");
            	}

            	if (inLine.contains("#MODEL_RUNTIME#")) {
            		inLine = inLine.replaceAll("#MODEL_RUNTIME#", sdf.format(sdfMetoc.parse(metocFields[4] + " " + metocFields[5])));
            	}

            	if (inLine.contains("#FORECAST_TIME#")) {
            		inLine = inLine.replaceAll("#FORECAST_TIME#", sdf.format(sdfMetoc.parse(metocFields[6] + " " + metocFields[7])));
            	}

            	/** ENVELOPE/GRID-RANGE **/
            	if (inLine.contains("#LONLATBBOX_MINX#")) {
            		inLine = inLine.replaceAll("#LONLATBBOX_MINX#", String.valueOf(envelope.getLowerCorner().getOrdinate(0)));
            	}

            	if (inLine.contains("#LONLATBBOX_MINY#")) {
            		inLine = inLine.replaceAll("#LONLATBBOX_MINY#", String.valueOf(envelope.getLowerCorner().getOrdinate(1)));
            	}

            	if (inLine.contains("#LONLATBBOX_MAXX#")) {
            		inLine = inLine.replaceAll("#LONLATBBOX_MAXX#", String.valueOf(envelope.getUpperCorner().getOrdinate(0)));
            	}

            	if (inLine.contains("#LONLATBBOX_MAXY#")) {
            		inLine = inLine.replaceAll("#LONLATBBOX_MAXY#", String.valueOf(envelope.getUpperCorner().getOrdinate(1)));
            	}

            	if (inLine.contains("#POST_PROC_FLAG#")) {
            		inLine = inLine.replaceAll("#POST_PROC_FLAG#", "false");
            	}
            	
            	if (inLine.contains("#Z_UOM#")) {
            		inLine = inLine.replaceAll("#Z_UOM#", "m");
            	}

            	if (inLine.contains("#Z_ORDER#")) {
            		inLine = inLine.replaceAll("#Z_ORDER#", zOrder);
            	}

            	if (inLine.contains("#Z_LEVEL#")) {
            		inLine = inLine.replaceAll("#Z_LEVEL#", metocFields[3]);
            	}

            	if (inLine.contains("#PIXEL_UOM#")) {
            		inLine = inLine.replaceAll("#PIXEL_UOM#", "deg");
            	}
            	
            	if (inLine.contains("#RESX#")) {
            		double lon = envelope.getUpperCorner().getOrdinate(0) - envelope.getLowerCorner().getOrdinate(0);
            		inLine = inLine.replaceAll("#RESX#", String.valueOf(lon / range.getWidth()));
            	}
            	
            	if (inLine.contains("#RESY#")) {
            		double lat = envelope.getUpperCorner().getOrdinate(1) - envelope.getLowerCorner().getOrdinate(1);
            		inLine = inLine.replaceAll("#RESY#", String.valueOf(lat / range.getHeight()));
            	}
            	
            	if (inLine.contains("#WIDTH#")) {
            		inLine = inLine.replaceAll("#WIDTH#", String.valueOf(range.getWidth()));
            	}
            	
            	if (inLine.contains("#HEIGHT#")) {
            		inLine = inLine.replaceAll("#HEIGHT#", String.valueOf(range.getHeight()));
            	}
            	
            	if (inLine.contains("#GRID_ORIGIN#")) {
            		inLine = inLine.replaceAll("#GRID_ORIGIN#", envelope.getLowerCorner().getOrdinate(0) + " " + envelope.getLowerCorner().getOrdinate(1) + " " + (zOrder.equals("DOWN") ? "-" : "") + metocFields[3]);
            	}
            	
            	if (inLine.contains("#GRID_OFFSETS#")) {
            		double lon = envelope.getUpperCorner().getOrdinate(0) - envelope.getLowerCorner().getOrdinate(0);
            		double lat = envelope.getUpperCorner().getOrdinate(1) - envelope.getLowerCorner().getOrdinate(1);
            		double resX = lon / range.getWidth();
            		double resY = lat / range.getHeight();
            		inLine = inLine.replaceAll("#GRID_OFFSETS#", resX + " 0 0  0 " + resY + " 0  0 0 0");
            	}
            	
            	if (inLine.contains("#NODATA#")) {
            		// TODO: FIX THIS
            		inLine = inLine.replaceAll("#NODATA#", "-9999.0");
            	}
            	
                outputStream.println(inLine);
            }

        } catch (IOException e) {
        } finally {
        	inputFileReader.close();
        	outputFileWriter.close();
        }
		
		reader.dispose();
	}
	
}