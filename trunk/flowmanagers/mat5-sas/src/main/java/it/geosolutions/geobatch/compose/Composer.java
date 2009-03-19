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
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.TileScheduler;

/**
 * Comments here ...
 * 
 * @author Simone Giannechini, GeoSolutions
 */
public class Composer extends BaseAction<FileSystemMonitorEvent> implements
        Action<FileSystemMonitorEvent> {

    //TODO: TEMP SOLUTION. LEVERAGES ON REAL XML PARSING
    private String LEG_DATA_LOCATION = "<dataLocation>";
    
    private ComposerConfiguration configuration;

    private final static Logger LOGGER = Logger
            .getLogger(Composer.class.toString());

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
            
            final String directory = getDataDirectory(inputFile);
            if (directory==null || directory.trim().length()==0){
            	LOGGER.warning("Unable to find LegData location from the specified file: "+inputFile.getAbsolutePath());
            	return events;
            }
            	
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
            
            
            final FormatConverterConfiguration converterConfig = new FormatConverterConfiguration();
            converterConfig.setWorkingDirectory(directory);
            converterConfig.setId("conv1");
            converterConfig.setDescription("Mat5 to tiff converter");
            converterConfig.setCompressionRatio(compressionRatio);
            converterConfig.setCompressionScheme(compressionScheme);
            converterConfig.setInputFormats(inputFormats);
            converterConfig.setOutputFormat(outputFormat);
            converterConfig.setTileH(tileH);
            converterConfig.setTileW(tileW);
            LOGGER.log(Level.INFO, "Ingesting MatFiles in the mosaic composer");
            
            FormatConverter converter = new FormatConverter(converterConfig);
            converter.execute(null);
            
            final MosaicerConfiguration mosaicerConfig = new MosaicerConfiguration();
            mosaicerConfig.setCompressionRatio(compressionRatio);
            mosaicerConfig.setCompressionScheme(compressionScheme);
            mosaicerConfig.setNumSteps(numSteps);
            mosaicerConfig.setDownsampleStep(downsampleStep);
            mosaicerConfig.setWorkingDirectory(directory);
            mosaicerConfig.setTileH(tileH);
            mosaicerConfig.setTileW(tileW);
            mosaicerConfig.setChunkHeight(chunkH);
            mosaicerConfig.setChunkWidth(chunkW);

            LOGGER.log(Level.INFO, "Composing the mosaic with raw tiles");
            Mosaicer mosaicer = new Mosaicer(mosaicerConfig);
            mosaicer.execute(null);
            
//            final GeoServerActionConfiguration geoserverConfig = new GeoServerActionConfiguration();
//            geoserverConfig.setGeoserverURL("http://localhost:8080/geoserver");
//            geoserverConfig.setGeoserverUID("admin");
//            geoserverConfig.setGeoserverPWD("geoserver");
//            geoserverConfig.setDataTransferMethod("DIRECT");
////            geoserverConfig.setWorkingDirectory(mosaicerConfig.getMosaicDirectory());
//            geoserverConfig.setWorkingDirectory(directory + "/raw");
//            
//            
//            final SasMosaicGeoServerGenerator geoserverIngestion  = new SasMosaicGeoServerGenerator(geoserverConfig);
//            geoserverIngestion.execute(null);
                        
            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }

    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }
    
    //TODO: Improve me, leveraging on real XML
    private String getDataDirectory(final File xmlFile){
        String dataDir = null;
        if (xmlFile!=null){
            try {
                FileImageInputStream fis = new FileImageInputStream(xmlFile);
                String location=null;
                while ((location = fis.readLine())!=null){
                    if (location.startsWith(LEG_DATA_LOCATION)){
                        dataDir=location.substring(location.indexOf(LEG_DATA_LOCATION)+LEG_DATA_LOCATION.length(), location.length()-(LEG_DATA_LOCATION.length()+1));
                        break;
                    }
                }
                
            } catch (FileNotFoundException e) {
                //TODO: LOG warning
            } catch (IOException e) {
              //TODO: LOG warning
            }
        }
        return dataDir;
    }
    
    private void setJAIHints(ComposerConfiguration configuration) {
        if (configuration!=null){
            final JAI jaiDef = JAI.getDefaultInstance();

            final TileCache cache = jaiDef.getTileCache();
            cache.setMemoryCapacity(configuration.getJAICacheCapacity());
            cache.setMemoryThreshold(configuration.getJAICacheThreshold());

            final TileScheduler scheduler = jaiDef.getTileScheduler();
            scheduler.setParallelism(configuration.getJAIParallelism());
            scheduler.setPrefetchParallelism(configuration.getJAIParallelism());
        }
    }
}
