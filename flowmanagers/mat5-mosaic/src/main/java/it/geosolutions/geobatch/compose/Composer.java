/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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

package it.geosolutions.geobatch.compose;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.convert.FormatConverter;
import it.geosolutions.geobatch.convert.FormatConverterConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.matfile5.sas.SasMosaicGeoServerGenerator;
import it.geosolutions.geobatch.mosaic.Mosaicer;
import it.geosolutions.geobatch.mosaic.MosaicerConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.TileScheduler;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class Composer extends BaseAction<FileSystemMonitorEvent> implements
        Action<FileSystemMonitorEvent> {

    //TODO: TEMP SOLUTION. LEVERAGES ON REAL XML PARSING
    private String LEG_DATA_LOCATION = "<dataLocation>";
    
    private ComposerConfiguration configuration;

    private final static Logger LOGGER = Logger
            .getLogger(Composer.class.toString());
    
    /** The DATE/TIME associated to this run (Actually is YYMMDD) */
    private String initTime = null;

    protected Composer(ComposerConfiguration configuration)
            throws IOException {
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {
            
            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "
                        + events.size());
            
           
            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            
            setJAIHints(configuration);
            
            // get the first event
            final FileSystemMonitorEvent event = events.remove();
            final File inputFile = event.getSource();
            
            // //
            // Get the directory containing the data from the specified
            // XML file
            // //
            final String directory = getDataDirectory(inputFile);
            if (directory==null || directory.trim().length()==0){
            	LOGGER.warning("Unable to find LegData location from the specified file: "+inputFile.getAbsolutePath());
            	return events;
            }
            	
            // Preparing parameters
            final double compressionRatio = configuration.getCompressionRatio();
            final String compressionScheme = configuration.getCompressionScheme();
            final String inputFormats = configuration.getInputFormats();
            final String outputFormat = configuration.getOutputFormat();
            final int downsampleStep = configuration.getDownsampleStep();
            final int numSteps = configuration.getNumSteps();
            final int tileH = configuration.getTileH();
            final int tileW = configuration.getTileW();
            final int chunkW = configuration.getChunkW();
            final int chunkH = configuration.getChunkH();
            final String baseDir = configuration.getOutputBaseFolder();
            
            //TODO: Refactor this search to leverage on a PATH_DEPTH parameter.
            //Actually is looking for specifiedDir/dirdepth1/dirdepth2/
            
            // //
            //
            // Checking LEGS for the current MISSION
            //
            // //
            ArrayList<File> directories = null;
            final File fileDir = new File(directory); //Mission dir
            if (fileDir != null && fileDir.isDirectory()) {
                final File[] foundFiles = fileDir.listFiles();
                if (foundFiles!=null){
                    directories = new ArrayList<File>();
                    for (File file : foundFiles){
                        if (file.exists() && file.isDirectory()){
                            directories.add(file);
                        }
                    }
                    
                }
            }
            
            // //
            //
            // Mission Scan: Looking for LEGS
            //
            // //
            if (directories != null && !directories.isEmpty()){
                Collections.sort(directories);
                final String leavesFolders = configuration.getLeavesFolders();
                final String leaves[] = leavesFolders.split(";");
                if (leaves != null){
                    final List<String> leavesArray = Arrays.asList(leaves);
                    final Set<String> leavesSet = new HashSet<String>(leavesArray);
                    
                    // //
                    //
                    // Leg Scan
                    //
                    // //
                    for (File legDir : directories){
                        if (legDir.isDirectory()){
                            final File subFolders[] = legDir.listFiles();
                            if (subFolders != null){
                            	
                            	// //
                            	//
                            	// Channel scan (leaves)
                            	//
                            	// //
                                for (int i=0; i<subFolders.length; i++){
                                    final File leaf = subFolders[i];
                                    final String leafName = leaf.getName();
                                    if (leavesSet.contains(leafName)){
                                      
                                      final String leafPath = leaf.getAbsolutePath();
                                      
                                      // Initialize time
                                      if (initTime == null){
                                          setInitTime(leafPath);
                                      }
                                      
                                      //Build the output directory path
                                      final StringBuffer outputDir = new StringBuffer(baseDir)
                                      .append(File.separatorChar).append(initTime).append(File.separatorChar)
                                      .append(fileDir.getName()).append(File.separatorChar)
                                      .append(legDir.getName()).append(File.separatorChar)
                                      .append(leafName);
                                      
                                      //Compose the mosaic.
                                      final String mosaicTobeIngested = composeMosaic(leafPath,outputDir.toString(), compressionRatio, compressionScheme,
                                              inputFormats, outputFormat, tileW, tileH, numSteps, downsampleStep, chunkW, chunkH,initTime);
                                      
                                      //Ingest the mosaics (balanced and raw) with the assumption that their path differ only in the prefix.
                                      //AS an instance: 
                                      // c:\data\010101\mission1\leg1\stbd\rawm_010101_mission1_leg1_stbd
                                      // c:\data\010101\mission1\leg1\stbd\balm_010101_mission1_leg1_stbd
                                      
                                      ingestMosaic(mosaicTobeIngested, Mosaicer.RAW_PREFIX);
                                      ingestMosaic(mosaicTobeIngested.replace(Mosaicer.RAW_PREFIX, Mosaicer.BALANCED_PREFIX), Mosaicer.BALANCED_PREFIX);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }

    }
    
    /**
     * Set the time of this Mission
     * 
     * @param leafPath
     */
    private void setInitTime(String leafPath) {
        //TODO: implement ME:
        //actually, get this time from the file name
        //next step is acquiring it from the matlab file
        
        final File fileDir = new File(leafPath);
        if (fileDir != null && fileDir.isDirectory()) {
            final File files[] = fileDir.listFiles();
            List<File> filesArray = Arrays.asList(files);
            Collections.sort(filesArray);
            final File file = filesArray.get(0);
            boolean found = false;
            if (file!=null){
                final String fileName = file.getName();
                String date = fileName;
                int index=0;
                
                //Files are so named like this:
                //muscle_col2_090316_1_2_p_5790_5962_40_150.tif
                
                for (int i=0;i<7&&index!=-1;i++){
                    index = date.lastIndexOf("_");
                    date = date.substring(0,index);
                }
                if (index!=-1){
                    final int indexOf = date.lastIndexOf("_");
                    if (indexOf!=-1){
                        initTime = date.substring(indexOf+1,index);
                        found = true;
                    }
                }
            }
            if(!found)
                initTime = new SimpleDateFormat("yyyyMMdd").format(new Date());
            //Current time in case it's unable to find it from the file
            
        }
    }

    /**
     * Setup a Geoserver Ingestion action to send the mosaic to Geoserver via REST 
     * @param mosaicToBeIngested the location of the mosaic to be ingested 
     * @param prefix one of {@link Mosaicer#BALANCED_PREFIX} or {@link Mosaicer#RAW_PREFIX}
     * @throws Exception
     */
    private void ingestMosaic(final String mosaicToBeIngested, final String prefix) throws Exception{
      final String location = mosaicToBeIngested;
      final int index = location.lastIndexOf(prefix);
      
      //Setting up the wmspath.
      //Actually it is set by simply changing mosaic's name underscores to slashes.
      //TODO: can be improved
      String wmsPath = location.substring(index + prefix.length(), location.length());
      wmsPath = wmsPath.replace("_","/");
      
      // //
      //
      // Setting up the GeoserverActionConfiguration properties
      //
      // //
      final GeoServerActionConfiguration geoserverConfig = new GeoServerActionConfiguration();
      geoserverConfig.setGeoserverURL(configuration.getGeoserverURL());
      geoserverConfig.setGeoserverUID(configuration.getGeoserverUID());
      geoserverConfig.setGeoserverPWD(configuration.getGeoserverPWD());
      geoserverConfig.setDataTransferMethod(configuration.getGeoserverUploadMethod());
      geoserverConfig.setWorkingDirectory(mosaicToBeIngested);
      geoserverConfig.setWmsPath(wmsPath);
      
      //Setting styles
      final String style;
      if (prefix.equalsIgnoreCase(Mosaicer.BALANCED_PREFIX)){
          style = SasMosaicGeoServerGenerator.SAS_STYLE;
      }
      else{
          style = SasMosaicGeoServerGenerator.DEFAULT_STYLE;
      }
      geoserverConfig.setDefaultStyle(style);
      final List<String> styles = new ArrayList<String>();
      styles.add(style);
      geoserverConfig.setStyles(styles);
      
      final SasMosaicGeoServerGenerator geoserverIngestion  = new SasMosaicGeoServerGenerator(geoserverConfig);
      geoserverIngestion.execute(null);
    }

    /**
     * Compose a mosaic using the set of specified parameters.
     * @param directory the directory containing raw tiles
     * 
     * @param outputDir the directory where to store the produced results.
     * @param compressionRatio the compression ratio to be used to compress output files.
     * @param compressionScheme the compression type
     * @param inputFormats the input formats to be converted and then mosaicked.
     * @param outputFormat the requested output format of conversion (As an instance, GeoTIFF)
     * @param tileW the inner image tiling width
     * @param tileH the inner image tiling height
     * @param numSteps the number of steps of overviews generation
     * @param downsampleStep the downsampling step between overviews
     * @param chunkW the width of each separated file composing the big final mosaic
     * @param chunkH the height of each separated file composing the big final mosaic
     * @param time the time of the tiles composing that mission. (Used to setup the output folder)
     * @return the location where the mosaic have been created
     * @throws Exception
     */
    private String composeMosaic(final String directory, final String outputDir,
            final double compressionRatio, final String compressionScheme, 
            final String inputFormats, String outputFormat, final int tileW, final int tileH, 
            final int numSteps, final int downsampleStep, final int chunkW, final int chunkH,
            final String time) throws Exception {
        
        
        // //
        //
        // First step: Data conversion
        //
        // //
        final FormatConverterConfiguration converterConfig = new FormatConverterConfiguration();
        converterConfig.setWorkingDirectory(directory);
        converterConfig.setOutputDirectory(outputDir);
        converterConfig.setId("conv");
        converterConfig.setDescription("Mat5 to tiff converter");
        converterConfig.setCompressionRatio(compressionRatio);
        converterConfig.setCompressionScheme(compressionScheme);
        converterConfig.setInputFormats(inputFormats);
        converterConfig.setOutputFormat(outputFormat);
        converterConfig.setTileH(tileH);
        converterConfig.setTileW(tileW);
        LOGGER.log(Level.INFO, "Ingesting MatFiles in the mosaic composer");
        
        final FormatConverter converter = new FormatConverter(converterConfig);
        converter.execute(null);
        
        // //
        //
        // Second step: Mosaic
        //
        // //
        final MosaicerConfiguration mosaicerConfig = new MosaicerConfiguration();
        mosaicerConfig.setCompressionRatio(compressionRatio);
        mosaicerConfig.setCompressionScheme(compressionScheme);
        mosaicerConfig.setId("mosaic");
        mosaicerConfig.setDescription("Mosaic composer");
        mosaicerConfig.setNumSteps(numSteps);
        mosaicerConfig.setDownsampleStep(downsampleStep);
        mosaicerConfig.setWorkingDirectory(outputDir);
        mosaicerConfig.setTileH(tileH);
        mosaicerConfig.setTileW(tileW);
        mosaicerConfig.setChunkHeight(chunkH);
        mosaicerConfig.setChunkWidth(chunkW);
        mosaicerConfig.setTime(time);

        LOGGER.log(Level.INFO, "Mosaic Composition");
        final Mosaicer mosaicer = new Mosaicer(mosaicerConfig);
        mosaicer.execute(null);
        return mosaicerConfig.getMosaicDirectory();
    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }
    
    //TODO: Improve me, leveraging on real XML
    private String getDataDirectory(final File xmlFile){
        String dataDir = null;
        if (xmlFile!=null){
            try {
                final FileImageInputStream fis = new FileImageInputStream(xmlFile);
                String location=null;
                while ((location = fis.readLine())!=null){
                    if (location.startsWith(LEG_DATA_LOCATION)){
                        dataDir=location.substring(location.indexOf(LEG_DATA_LOCATION)+LEG_DATA_LOCATION.length(), location.length()-(LEG_DATA_LOCATION.length()+1));
                        break;
                    }
                }
                
            } catch (FileNotFoundException e) {
                LOGGER.warning(new StringBuilder("Unable to find the specified file: ")
                .append(xmlFile).toString());
            } catch (IOException e) {
                LOGGER.warning(new StringBuilder("Problems occurred while reading: ")
                .append(xmlFile).append("due to ").append(e.getLocalizedMessage()).toString());
            }
        }
        return dataDir;
    }
    
    /**
     * Set JAI Hints from the current configuration
     * @param configuration
     */
    private void setJAIHints(final ComposerConfiguration configuration) {
        if (configuration!=null){
            final JAI jaiDef = JAI.getDefaultInstance();

            final TileCache cache = jaiDef.getTileCache();
            final long cacheSize = configuration.getJAICacheCapacity();
            cache.setMemoryCapacity(cacheSize*1024*1024);
//            cache.setMemoryThreshold(configuration.getJAICacheThreshold());

            final TileScheduler scheduler = jaiDef.getTileScheduler();
            scheduler.setParallelism(configuration.getJAIParallelism());
            scheduler.setPrefetchParallelism(configuration.getJAIParallelism());
            
            ImageIO.setUseCache(false);
        }
    }
}
