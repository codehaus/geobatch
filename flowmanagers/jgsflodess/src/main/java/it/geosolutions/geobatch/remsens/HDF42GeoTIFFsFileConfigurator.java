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
package it.geosolutions.geobatch.remsens;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.geoserver.RegistryActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.geoserver.GeoServerRESTHelper;
import it.geosolutions.geobatch.flow.event.action.geoserver.RegistryConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.jgsflodess.utils.io.JGSFLoDeSSIOUtils;
import it.geosolutions.geobatch.metocs.jaxb.model.Metocs;
import it.geosolutions.geobatch.utils.IOUtils;
import it.geosolutions.geobatch.utils.io.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.geotools.coverage.Category;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.io.CoverageAccess;
import org.geotools.coverage.io.CoverageReadRequest;
import org.geotools.coverage.io.CoverageResponse;
import org.geotools.coverage.io.CoverageSource;
import org.geotools.coverage.io.CoverageAccess.AccessType;
import org.geotools.coverage.io.CoverageResponse.Status;
import org.geotools.coverage.io.domain.RasterDatasetDomainManager.HorizontalDomain;
import org.geotools.coverage.io.domain.RasterDatasetDomainManager.TemporalDomain;
import org.geotools.coverage.io.driver.BaseFileDriver;
import org.geotools.coverage.io.driver.Driver.DriverOperation;
import org.geotools.coverage.io.hdf4.HDF4Driver;
import org.geotools.coverage.io.impl.DefaultCoverageReadRequest;
import org.geotools.coverage.io.impl.range.DefaultRangeType;
import org.geotools.coverage.io.range.FieldType;
import org.geotools.coverage.io.range.RangeType;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.opengis.coverage.Coverage;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;

import ucar.nc2.NetcdfFile;

/**
 * 
 * Public class to split HDF4 files to GeoTIFFs and consequently
 * send them to GeoServer along with their basic metadata.
 */
public class HDF42GeoTIFFsFileConfigurator extends
		RegistryConfiguratorAction<FileSystemMonitorEvent> {

	/**
	 * GeoTIFF Writer Default Params
	 */
	public final static String GEOSERVER_VERSION = "2.x";
	
	private final static CoordinateReferenceSystem WGS84 = AbstractGridFormat.getDefaultCRS();

	private final static Operations OPERATIONS = new Operations(new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE));
	
	private static final int DEFAULT_TILE_SIZE = 256;

	private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

	private static final String DEFAULT_COMPRESSION_TYPE = "LZW";


	/**
	 * Static DateFormat Converter
	 */
//	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHHmmss");
	
//	private static final SimpleDateFormat sdfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
//	static {
//		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//		sdfISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
//	}
	
	protected HDF42GeoTIFFsFileConfigurator(final RegistryActionConfiguration configuration) throws IOException {
		super(configuration);
	}

	/**
	 * EXECUTE METHOD 
	 */
	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events) throws Exception {

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
						&& ("hdf4".equalsIgnoreCase(fileSuffix) || "hdf".equalsIgnoreCase(fileSuffix))) {
					// etj: are we missing something here?
					baseFileName = filePrefix;
				}
			} else if ("hdf4".equalsIgnoreCase(fileSuffix) || "hdf".equalsIgnoreCase(fileSuffix)) {
				baseFileName = filePrefix;
			}

			if (baseFileName == null) {
				LOGGER.log(Level.SEVERE, "Unexpected file '" + inputFileName + "'");
				throw new IllegalStateException("Unexpected file '" + inputFileName + "'");
			}

			String baseName = FilenameUtils.getBaseName(inputFileName);
			String baseTime = null;
			String endTime = null;
			
	        final BaseFileDriver driver = new HDF4Driver();
	        final File file= new File(inputFileName);
	        final URL source = file.toURI().toURL();
	        if (driver.canProcess(DriverOperation.CONNECT, source,null)) {

	            // getting access to the file
	            final CoverageAccess access = driver.process(DriverOperation.CONNECT,source, null, null,null);
	            if (access == null)
	                throw new IOException("Unable to connect");
	
	            // get the names
	            final List<Name> names = access.getNames(null);
	            for (Name name : names) {
	                // get a source
	                final CoverageSource gridSource = access.access(name, null,AccessType.READ_ONLY, null, null);
	                if (gridSource == null)
	                    throw new IOException("Unable to access");	                
	
	                // TEMPORAL DOMAIN
	                final TemporalDomain temporalDomain = gridSource.getDomainManager(null).getTemporalDomain();
	                if(temporalDomain == null)
	                	throw new IllegalStateException("Temporal domain is null");

	                final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmsss'Z'");
	                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	                
                	// get the temporal domain elements
                	for(TemporalGeometricPrimitive tg:temporalDomain.getTemporalElements(null)){
                         baseTime = sdf.format(((Period)tg).getBeginning().getPosition().getDate());
                         endTime = sdf.format(((Period)tg).getEnding().getPosition().getDate());
                	}
	            		
	                
	                // HORIZONTAL DOMAIN
	                final HorizontalDomain horizontalDomain= gridSource.getDomainManager(null).getHorizontalDomain();
	                if(horizontalDomain == null)
	                	throw new IllegalStateException("Horizontal domain is null");
	                
	                
	                // RANGE TYPE
	                final RangeType range = gridSource.getRangeType(null);
	                
	                final CoverageReadRequest readRequest = new DefaultCoverageReadRequest();
	                // //
	                //
	                // Setting up a limited range for the request.
	                //
	                // //
	                Iterator<FieldType> ftIterator = range.getFieldTypes().iterator();
	               
	                while (ftIterator.hasNext()) {
	                	HashSet<FieldType> fieldSet = new HashSet<FieldType>();
			            FieldType ft = null;
	                	
	                    ft = ftIterator.next();
	                    if (ft != null) {
	                        fieldSet.add(ft);
	                    }
	                    RangeType rangeSubset = new DefaultRangeType(range.getName(), range.getDescription(), fieldSet);
		                readRequest.setRangeSubset(rangeSubset);
		                CoverageResponse response = gridSource.read(readRequest, null);
		                if (response == null || response.getStatus() != Status.SUCCESS || !response.getExceptions().isEmpty())
		                    throw new IOException("Unable to read");
		
		                final Collection<? extends Coverage> results = response.getResults(null);
		                for (Coverage c : results) {
		                    GridCoverage2D coverage = (GridCoverage2D) c;
		                    final File outDir = Utilities.createTodayDirectory(workingDir);

							// Storing fields as GeoTIFFs
							
							final List<Category> categories = coverage.getSampleDimension(0).getCategories();
							double noData = Double.NaN;
							for (Category cat: categories){
								if (cat.getName().toString().equalsIgnoreCase("no data")){
									noData = cat.getRange().getMinimum();
									break;
								}
							}
							
							final String uom = getUom(ft);
							final String varLongName = ft.getDescription().toString();
							final String varBrief = getBrief(ft.getName().getLocalPart().toString());
							final String coverageName = buildCoverageName(baseName,ft,endTime);
							final String coverageStoreId = coverageName.toString();
							final GridCoverage2D resampledCoverage = (GridCoverage2D)  OPERATIONS.resample(coverage, WGS84);
							final File gtiffFile = Utilities.storeCoverageAsGeoTIFF(outDir, coverageName.toString(), resampledCoverage, DEFAULT_COMPRESSION_TYPE, DEFAULT_COMPRESSION_RATIO, DEFAULT_TILE_SIZE);
							
							final Envelope envelope = resampledCoverage.getEnvelope();
							final GridEnvelope2D gridRange = resampledCoverage.getGridGeometry().getGridRange2D();
							final CoordinateReferenceSystem crs = resampledCoverage.getCoordinateReferenceSystem();
							// ////////////////////////////////////////////////////////////////////
							//
							// SENDING data to GeoServer via REST protocol.
							//
							// ////////////////////////////////////////////////////////////////////
							Map<String, String> queryParams = new HashMap<String, String>();
							queryParams.put("namespace", getConfiguration().getDefaultNamespace());
							queryParams.put("wmspath", getConfiguration().getWmsPath());
							final String[] layer = GeoServerRESTHelper.send(outDir, 
									gtiffFile, 
									getConfiguration().getGeoserverURL(), 
									getConfiguration().getGeoserverUID(), 
									getConfiguration().getGeoserverPWD(),
									coverageStoreId, 
									coverageName.toString(),
									queryParams, "", getConfiguration().getDataTransferMethod(),
									"geotiff",
									GEOSERVER_VERSION, null,null /*getConfiguration().getStyles(), 
									getConfiguration().getDefaultStyle()*/);

							// ////////////////////////////////////////////////////////////////////
							//
							// HARVESTING metadata to the Registry.
							//
							// ////////////////////////////////////////////////////////////////////
							if (layer != null && layer.length > 0)
								harvest(outDir, 
										crs, envelope, gridRange,
										getConfiguration().getGeoserverURL(),
										getConfiguration().getRegistryURL(),
										getConfiguration().getProviderURL(),
										event.getTimestamp(), 
										getConfiguration().getDefaultNamespace(),
										coverageStoreId, 
										coverageName, varLongName, varBrief, uom, noData);
				                }
		                }
		            }
		        } else
		        	LOGGER.info("NOT ACCEPTED");

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

	private final static String getUom(final FieldType ft) {
		final String uom = ft.getUnitOfMeasure().toString();
		if (ft != null)
			if(ft.getName().getLocalPart().toString().contains("sst"))
				return "cel";
			else if(ft.getName().getLocalPart().toString().contains("lowcloud"))
				return "dimensionless";
		return uom;
	}
	
	private final static String getBrief(final String string) {
		if (string != null && string.trim().length()>0){
			if (string.equalsIgnoreCase("mcsst"))
				return "sst";
		}
		return string;
	}

	private String buildCoverageName(final String baseName, final FieldType ft, final String referenceTime) {
		final String varName = ft.getName().getLocalPart().toString();
		String description = ft.getDescription().toString();
		
		String source = "";
		String system = "";
		
		if (varName.equalsIgnoreCase("mcsst")||varName.equalsIgnoreCase("lowcloud")){
			if (varName.equalsIgnoreCase("mcsst"))
				description = getBrief("mcsst");
			source = "terascan";
			system = "NOAA-AVHRR";
		}
		// ////
		// producing the Coverage name ...
		// ////
		final StringBuilder coverageName = new StringBuilder(source).append("_").append(system)
			.append("_").append(description.replaceAll(" ", "")).append("_").append(referenceTime);
		
		return coverageName.toString();

	}

	
	/**
	 * Harvest: Metadata Creator
	 * 
	 * @param outDir
	 * @param gridRange 
	 * @param envelope 
	 * @param crs 
	 * @param gtiffFile
	 * @param geoserverURL
	 * @param string2 
	 * @param string 
	 * @param string
	 * @param defaultNamespace
	 * @param coverageStoreId
	 * @param noData 
	 * @param string2
	 * @param string3
	 * @return 
	 * @throws JAXBException 
	 * @throws IOException 
	 * @throws FactoryException 
	 * @throws ParseException 
	 */
	public boolean harvest(
			final File outDir, 
			final CoordinateReferenceSystem crs, 
			final Envelope envelope, 
			final GridEnvelope2D range,  
			final String geoserverURL,
			final String registryURL,
			final String providerURL, 
			final long timestamp, 
			final String namespace, 
			final String coverageStoreId,
			final String coverageName,
			final String varLongName,
			final String varBrief,
			final String uom,
			final double noData
	) throws JAXBException, IOException, FactoryException, ParseException {
		// CoverageName Format:
		//  CRUISEEXP_MODELNAME-MODELTYPE_VARNAME(-u/v/mag/dir)_ZLEV_BASETIMEYYYYMMDD_BASETIMEHHHMMSS_FCSTTIMEYYYYMMDD_FCSTTIMEHHHMMSS_TAU
		
		//Grabbing the Variables Dictionary
		JAXBContext context = JAXBContext.newInstance(Metocs.class);
		Unmarshaller um = context.createUnmarshaller();
		Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(new File(getConfiguration().getMetocDictionaryPath())));
		
		// get harvester XML template
		final File metadataTemplate = new File(getConfiguration().getMetocHarvesterXMLTemplatePath());
		
		// keep original name
		final File outFile = new File(outDir, coverageName + ".xml");
		
		final String srsId = CRS.lookupIdentifier(crs, false);
		final String[] metocFields = coverageName.split("_");
		
		// Create FileReader Object
        FileReader inputFileReader  = new FileReader(metadataTemplate);
        FileWriter outputFileWriter = new FileWriter(outFile);

        try {

            // Create Buffered/PrintWriter Objects
            BufferedReader inputStream   = new BufferedReader(inputFileReader);
            PrintWriter    outputStream  = new PrintWriter(outputFileWriter);

            String inLine = null;
            
            final String satelliteName = metocFields[1].substring(0, metocFields[1].indexOf("-"));
           	final String sensorName = metocFields[1].substring(metocFields[1].indexOf("-")+1);
           	final SimpleDateFormat sdfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        	sdfISO8601.setTimeZone(TimeZone.getTimeZone("UTC"));
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        	final String acquisitionTime = sdfISO8601.format(sdf.parse(metocFields[3] + "_" + metocFields[4]));
            final double ucx = envelope.getUpperCorner().getOrdinate(0);
            final double ucy = envelope.getUpperCorner().getOrdinate(1);
            final double lcy = envelope.getLowerCorner().getOrdinate(1);
            final double lcx = envelope.getLowerCorner().getOrdinate(0);
    		
            while ((inLine = inputStream.readLine()) != null) {
            	// Handle KeyWords

            	/** GENERAL **/
            	if (inLine.contains("#UUID#")) {
            		inLine = inLine.replaceAll("#UUID#", "urn:uuid:" + UUID.randomUUID() + ":ISO19139");
            	}

            	if (inLine.contains("#CREATION_DATE#")) {
            		inLine = inLine.replaceAll("#CREATION_DATE#", sdfISO8601.format(new Date(timestamp)));
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
            		final StringBuilder wcsGetCoverage = new StringBuilder(getConfiguration().getGeoserverURL())
            			.append("/ows?SERVICE=WCS&amp;VERSION=1.0.0&amp;REQUEST=GetCoverage")
						.append("&amp;BBOX=").append(lcx)
						.append(",").append(lcy).append(",")
						.append(ucx).append(",")
						.append(ucy)
						.append("&amp;FORMAT=geotiff")
						.append("&amp;COVERAGE=").append(namespace + ":" + coverageName)
						.append("&amp;WIDTH=").append((int)range.getWidth())
						.append("&amp;HEIGHT=").append((int)range.getHeight())
						.append("&amp;CRS=").append(srsId);
            		inLine = inLine.replaceAll("#WCS_GETCOVERAGE#", wcsGetCoverage.toString());
            	}

            	if (inLine.contains("#WMS_GETMAP#")) {
            		final StringBuilder wmsGetMap = new StringBuilder(getConfiguration().getGeoserverURL())
            			.append("/ows?SERVICE=WMS&amp;VERSION=1.1.1&amp;REQUEST=GetMap")
            			.append("&amp;BBOX=").append(lcx).append(",")
            			.append(lcy).append(",")
            			.append(ucx).append(",")
            			.append(ucy).append("&amp;STYLES=")
            			.append("&amp;FORMAT=image/png")
            			.append("&amp;LAYERS=").append(namespace + ":" + coverageName)
            			.append("&amp;WIDTH=").append((int)range.getWidth())
            			.append("&amp;HEIGHT=").append((int)range.getHeight())
            			.append("&amp;SRS=").append(srsId);
            		inLine = inLine.replaceAll("#WMS_GETMAP#", wmsGetMap.toString());
            	}

            	/** VARIABLE **/            	
            	if (inLine.contains("#VAR_NAME#")) {
            		inLine = inLine.replaceAll("#VAR_NAME#", varBrief);
            	}

            	if (inLine.contains("#VAR_UOM#")) {
            		inLine = inLine.replaceAll("#VAR_UOM#", uom);
//            		for (MetocElementType m : metocDictionary.getMetoc()) {
//            			if (m.getBrief().equals(metocFields[2]))
//            				inLine = inLine.replaceAll("#VAR_UOM#", m.getDefaultUom().indexOf(":") > 0 ? URLDecoder.decode(m.getDefaultUom().substring(m.getDefaultUom().lastIndexOf(":")+1), "UTF-8") : m.getDefaultUom());
//            		}
            	}
            	
            	if (inLine.contains("#VAR_DESCRIPTION#")) {
            		inLine = inLine.replaceAll("#VAR_DESCRIPTION#", varLongName);
//            		for (MetocElementType m : metocDictionary.getMetoc()) {
//            			if (m.getBrief().equals(metocFields[2]))
//            				inLine = inLine.replaceAll("#VAR_DESCRIPTION#", m.getName());
//            		}
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
            	if (inLine.contains("#SATELLITE_NAME#")) {
            		inLine = inLine.replaceAll("#SATELLITE_NAME#", satelliteName);
            	}

            	if (inLine.contains("#SENSOR_NAME#")) {
            		inLine = inLine.replaceAll("#SENSOR_NAME#", sensorName);
            	}

            	if (inLine.contains("#ACQUISITION_TIME#")) {
					inLine = inLine.replaceAll("#ACQUISITION_TIME#", acquisitionTime );
            	}

            	/** ENVELOPE/GRID-RANGE **/
            	if (inLine.contains("#LONLATBBOX_MINX#")) {
            		inLine = inLine.replaceAll("#LONLATBBOX_MINX#", String.valueOf(lcx));
            	}

            	if (inLine.contains("#LONLATBBOX_MINY#")) {
            		inLine = inLine.replaceAll("#LONLATBBOX_MINY#", String.valueOf(lcy));
            	}

            	if (inLine.contains("#LONLATBBOX_MAXX#")) {
            		inLine = inLine.replaceAll("#LONLATBBOX_MAXX#", String.valueOf(ucx));
            	}

            	if (inLine.contains("#LONLATBBOX_MAXY#")) {
            		inLine = inLine.replaceAll("#LONLATBBOX_MAXY#", String.valueOf(ucy));
            	}

            	if (inLine.contains("#POST_PROC_FLAG#")) {
            		inLine = inLine.replaceAll("#POST_PROC_FLAG#", "false");
            	}
            	
            	if (inLine.contains("#PIXEL_UOM#")) {
            		inLine = inLine.replaceAll("#PIXEL_UOM#", "deg");
            	}
            	
            	if (inLine.contains("#RESX#")) {
            		double lon = ucx - lcx;
            		inLine = inLine.replaceAll("#RESX#", String.valueOf(lon / range.getWidth()));
            	}
            	
            	if (inLine.contains("#RESY#")) {
            		double lat = ucy - lcy;
            		inLine = inLine.replaceAll("#RESY#", String.valueOf(lat / range.getHeight()));
            	}
            	
            	if (inLine.contains("#WIDTH#")) {
            		inLine = inLine.replaceAll("#WIDTH#", String.valueOf((int)range.getWidth()));
            	}
            	
            	if (inLine.contains("#HEIGHT#")) {
            		inLine = inLine.replaceAll("#HEIGHT#", String.valueOf((int)range.getHeight()));
            	}
            	
            	if (inLine.contains("#GRID_ORIGIN#")) {
            		inLine = inLine.replaceAll("#GRID_ORIGIN#", lcx + " " + lcy );
            	}
            	
            	if (inLine.contains("#GRID_OFFSETS#")) {
            		double lon = ucx - lcx;
            		double lat = ucy - lcy;
            		double resX = lon / range.getWidth();
            		double resY = lat / range.getHeight();
            		inLine = inLine.replaceAll("#GRID_OFFSETS#", resX + " 0  0 " + resY );
            	}
            	
            	if (inLine.contains("#NODATA#")) {
            		inLine = inLine.replaceAll("#NODATA#", Double.toString(noData));
            	}
            	
                outputStream.println(inLine);
            }

        } catch (IOException e) {
        } finally {
        	inputFileReader.close();
        	outputFileWriter.close();
        }
        
        return JGSFLoDeSSIOUtils.sendHarvestRequest(registryURL, providerURL, coverageName);
	}
	
}