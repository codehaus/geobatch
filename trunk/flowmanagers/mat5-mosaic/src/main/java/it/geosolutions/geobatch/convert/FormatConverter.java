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

package it.geosolutions.geobatch.convert;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.base.BaseImageProcessingConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.matfile5.sas.SasMosaicGeoServerGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class FormatConverter extends BaseAction<FileSystemMonitorEvent>
        implements Action<FileSystemMonitorEvent> {

    private FormatConverterConfiguration configuration;
    
    private final static Logger LOGGER = Logger.getLogger(FormatConverter.class
            .toString());

    public FormatConverter(FormatConverterConfiguration configuration)
            throws IOException {
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(
            Queue<FileSystemMonitorEvent> events) throws Exception {
        try {

            // TODO: Refactor this allowing empty queues
            // looking for file
            // if (events.size() != 1)
            // throw new IllegalArgumentException(
            // "Wrong number of elements for this action: "
            // + events.size());

            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "DataFlowConfig is null.");
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            // get the first event
            // final FileSystemMonitorEvent event = events.peek();
            // final File inputFile = event.getSource();
            final String inputFormats = configuration.getInputFormats();
            final String[] inputExtensions;
            if (inputFormats != null) {
                inputExtensions = inputFormats.split(":");
            } else {
                inputExtensions = null;
            }

            final String directory = configuration.getWorkingDirectory();
            final String outputDirectory = configuration.getOutputDirectory();

            // //
            //
            // Setup the output directories structure
            //
            // //
            final File outputDir = new File(outputDirectory);
            if (!outputDir.exists()) {
                makeDirectories(outputDirectory);

            }

            final File fileDir = new File(directory);
            if (fileDir != null && fileDir.isDirectory()) {
                final File files[] = fileDir.listFiles();

                if (files != null) {
                    GridFormatFinder.scanForPlugins();
                    final int numFiles = files.length;

                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.log(Level.INFO, new StringBuilder("Found ")
                                .append(numFiles).append(" files").toString());
                    
                    // //
                    // Check files for conversion
                    // //
                    for (int i = 0; i < numFiles; i++) {
                        final File file = files[i];
                        final String path = file.getAbsolutePath()
                                .toLowerCase();
                        if (inputExtensions != null) {
                            boolean accepted = false;
                            for (String ext : inputExtensions) {
                                if (path.endsWith(ext)) {
                                    accepted = true;
                                    break;
                                }
                            }
                            if (!accepted)
                                continue;
                        }

                        //A valid found has been found. Start conversion
                        final String name = FilenameUtils.getBaseName(path);
                        final String fileOutputName = new StringBuilder(
                                outputDirectory).append(File.separatorChar)
                                .append(name).append(".tif").toString();

                        // //
                        // Acquire proper format and reader
                        // //
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.log(Level.INFO, new StringBuilder(
                                    "Converting file N. ").append(i + 1)
                                    .append(":").append(path).toString());
                        final File outFile = new File(fileOutputName);
                        convert(file, outFile);
                        BaseImageProcessingConfiguration.addOverviews(fileOutputName,
        						configuration.getDownsampleStep(),configuration.getNumSteps(),
        						configuration.getScaleAlgorithm(),configuration.getCompressionScheme(),
        						configuration.getCompressionRatio(),configuration.getTileW(),
        						configuration.getTileH());
                        
                        String runName = BaseImageProcessingConfiguration.buildRunName(outFile.getParent(), 
                        		configuration.getTime(), "");
                        
                        int index = runName.lastIndexOf(File.separatorChar);
                        if (index == runName.length()-1){
                        	runName = runName.substring(0,runName.length()-1);
                        	index = runName.lastIndexOf(File.separatorChar);
                        }
                        
                        //Setting up the wmspath.
                        //Actually it is set by simply changing mosaic's name underscores to slashes.
                        //TODO: can be improved
                        final String filePath = runName.substring(index+1, runName.length());
                        final String wmsPath = SasMosaicGeoServerGenerator.buildWmsPath(filePath);
                        SasMosaicGeoServerGenerator.ingest(fileOutputName, wmsPath, configuration.getGeoserverURL(), 
                        		configuration.getGeoserverUID(), configuration.getGeoserverPWD(), 
                        		configuration.getGeoserverUploadMethod(), SasMosaicGeoServerGenerator.SAS_RAW_STYLE, "geotiff");

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
     * Build the proper directories hierarchy.
     * 
     * @param outputDirectory
     *                the path of the output dir to be built
     */
    private synchronized void makeDirectories(final String outputDirectory) {
        final File makeDir = new File(outputDirectory);
        
        // Recursive check. back to the parents until a folder already exists.
        if (!makeDir.exists() || !makeDir.isDirectory()) {
            makeDirectories(makeDir.getParent());
            makeDir.mkdir();
        }
    }

    /**
     * Convert the specified file and write it to the specified output file name.
     * 
     * @param file
     * @param outputFile
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private void convert(final File file, final File outputFile)
            throws IllegalArgumentException, IOException {

        // //
        //
        // Getting a GridFormat
        // 
        // //
        final AbstractGridFormat gridFormat = (AbstractGridFormat) GridFormatFinder
                .findFormat(file);
        if (gridFormat != null && !(gridFormat instanceof UnknownFormat)) {

            final int tileW = configuration.getTileW();
            final int tileH = configuration.getTileH();
            // //
            //
            // Reading the coverage
            // 
            // //
            final GridCoverageReader reader = gridFormat.getReader(file, null);

            final GridCoverage2D gc = (GridCoverage2D) reader.read(null);

            final String outputFormatType = configuration.getOutputFormat();

            // //
            // Acquire required writer
            // //
            final AbstractGridFormat writerFormat = (AbstractGridFormat) acquireFormatByName(outputFormatType);

            if (!(writerFormat instanceof UnknownFormat)) {
                GridCoverageWriter writer = writerFormat.getWriter(
                        outputFile);

                GeoToolsWriteParams params = null;
                ParameterValueGroup wparams = null;
                try {
                    wparams = writerFormat.getWriteParameters();
                    params = writerFormat.getDefaultImageIOWriteParameters();
                } catch (UnsupportedOperationException uoe) {
                    params = null;
                    wparams = null;
                }
                if (params != null) {
                    params.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
                    params.setTiling(tileW, tileH);
                    wparams.parameter(
                            AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName()
                                    .toString()).setValue(params);
                }

                // //
                //
                // Write the converted coverage
                //
                // //
                writer.write(gc,
                        wparams != null ? (GeneralParameterValue[]) wparams
                                .values().toArray(new GeneralParameterValue[1])
                                : null);
                writer.dispose();
                gc.dispose(true);
                reader.dispose();
            }
        }
    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Get a proper {@link Format} for the requested format name.
     * 
     * @param formatName
     * @return the proper instance of {@link Format} or an {@link UnknownFormat} 
     * instance in case no format is found.
     */
    public static Format acquireFormatByName(final String formatName) {
        final Format[] formats = GridFormatFinder.getFormatArray();
        Format format = null;
        final int length = formats.length;

        for (int i = 0; i < length; i++) {
            format = formats[i];

            if (format.getName().equals(formatName)) {
                return format;
            }
        }

        return new UnknownFormat();
    }
}
