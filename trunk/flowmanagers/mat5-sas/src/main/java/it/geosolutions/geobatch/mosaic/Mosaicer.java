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

package it.geosolutions.geobatch.mosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageWriteParam;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.utils.imageoverviews.OverviewsEmbedder;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;

/**
 * Comments here ...
 * 
 * @author Simone Giannechini, GeoSolutions
 * 
 * @version $GeoTIFFOverviewsEmbedder.java $ Revision: x.x $ 23/mar/07 11:42:25
 */
public class Mosaicer extends BaseAction<FileSystemMonitorEvent> implements
        Action<FileSystemMonitorEvent> {

    private MosaicerConfiguration configuration;

    private final static Logger LOGGER = Logger.getLogger(Mosaicer.class
            .toString());

    public Mosaicer(MosaicerConfiguration configuration) throws IOException {
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(
            Queue<FileSystemMonitorEvent> events) throws Exception {
        try {

        	//TODO: TEMP solution
       	    JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
                    512 * 1024 * 1024);
            JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
            JAI.getDefaultInstance().getTileScheduler().setParallelism(8);
            JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(8);
            JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
            JAI.getDefaultInstance().getTileScheduler().setPriority(5);
        	
            // looking for file
            // if (events.size() != 1)
            // throw new IllegalArgumentException(
            // "Wrong number of elements for this action: "
            // + events.size());
            //
            // // get the first event
            // final FileSystemMonitorEvent event = events.peek();
            // final File inputFile = event.getSource();
            //            
            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////

            GeneralEnvelope globEnvelope = null;

            final String directory = configuration.getWorkingDirectory();
            final double compressionRatio = configuration.getCompressionRatio();
            final String compressionType = configuration.getCompressionScheme();
            final int tileW = configuration.getTileW();
            final int tileH = configuration.getTileH();
            final int chunkW = configuration.getChunkWidth();
            final int chunkH = configuration.getChunkHeight();

            File fileDir = new File(directory);
            if (fileDir != null && fileDir.isDirectory()) {
                File[] files = fileDir.listFiles();

                final String outputFileName = new StringBuilder(directory)
                        .append("/raw/").toString();
                File dir = new File(outputFileName);
                configuration.setMosaicDirectory(outputFileName);
                if (!dir.exists())
                    dir.mkdir();

                if (files != null) {
                    final int numFiles = files.length;
                    for (int i = 0; i < numFiles; i++) {
                        final String path = files[i].getAbsolutePath()
                                .toLowerCase();
                        if (!path.endsWith("tif"))
                            continue;

                        // get a reader
                        final File file = files[i];
                        final GeoTiffReader reader = new GeoTiffReader(file,
                                null);

                        GeneralEnvelope envelope = (GeneralEnvelope) reader
                                .getOriginalEnvelope();
                        if (globEnvelope == null) {
                            globEnvelope = new GeneralEnvelope(envelope);
                            globEnvelope.setCoordinateReferenceSystem(envelope
                                    .getCoordinateReferenceSystem());
                        } else
                            globEnvelope.add(envelope);

                        reader.dispose();
                    }

                    // compute final g2w
                    final GeneralMatrix gm = new GeneralMatrix(3);

                    // change this Leverage on XML metadata
                    gm.setElement(0, 0, 0.025);
                    gm.setElement(1, 1, -0.015);
                    gm.setElement(0, 1, 0);
                    gm.setElement(1, 0, 0);
                    gm.setElement(0, 2, globEnvelope.getLowerCorner()
                            .getOrdinate(0));
                    gm.setElement(1, 2, globEnvelope.getUpperCorner()
                            .getOrdinate(1));
                    MathTransform mosaicTransform = ProjectiveTransform
                            .create(gm);
                    MathTransform world2GridTransform = mosaicTransform
                            .inverse();

                    GridCoverageFactory coverageFactory = CoverageFactoryFinder
                            .getGridCoverageFactory(null);

                    // final GridGeometry2D gg2d = new GridGeometry2D(
                    // PixelInCell.CELL_CORNER, mosaicTransform, globEnvelope,
                    // null);

                    // read them all
                    final List<GridCoverage2D> coverages = new ArrayList<GridCoverage2D>();
                    for (File file : files) {
                        final String path = file.getAbsolutePath()
                                .toLowerCase();
                        if (!path.endsWith("tif"))
                            continue;

                        final GeoTiffReader reader = new GeoTiffReader(file,
                                null);

                        coverages.add((GridCoverage2D) reader.read(null));
                        reader.dispose();
                    }

                    GridCoverage2D gc = createGridCoverageMosaic(coverages,
                            globEnvelope, world2GridTransform, coverageFactory);

                    // //
                    //
                    // Retiling Mosaic to smaller Coverages
                    //
                    // //
                    retileMosaic(gc, chunkW, chunkH, tileW, tileH,
                            compressionRatio, compressionType, outputFileName);

                }
            }

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }
    }

    private GridCoverage2D createGridCoverageMosaic(
            final List<GridCoverage2D> coverages,
            final GeneralEnvelope globEnvelope,
            final MathTransform world2GridTransform,
            final GridCoverageFactory coverageFactory) {
        final int nCov = coverages.size();

        final ParameterBlockJAI pbMosaic = new ParameterBlockJAI("Mosaic");
        pbMosaic.setParameter("mosaicType", MosaicDescriptor.MOSAIC_TYPE_BLEND);

        for (int i = 0; i < nCov; i++) {
            final GridCoverage2D coverage = coverages.get(i);

            final ParameterBlockJAI pbAffine = new ParameterBlockJAI("Affine");
            pbAffine.addSource(coverage.getRenderedImage());
            AffineTransform at = (AffineTransform) coverage.getGridGeometry()
                    .getGridToCRS2D();
            AffineTransform chained = (AffineTransform) at.clone();
            chained.preConcatenate((AffineTransform) world2GridTransform);
            pbAffine.setParameter("transform", chained);
            final RenderedOp affine = JAI.create("Affine", pbAffine);
            pbMosaic.addSource(affine);
        }

        RenderedOp mosaicImage = JAI.create("Mosaic", pbMosaic);
        return coverageFactory.create("my", mosaicImage, globEnvelope);

    }

    private void retileMosaic(GridCoverage2D gc, int chunkWidth,
            int chunkHeight, int internalTileWidth, int internalTileHeight,
            final double compressionRatio, final String compressionScheme,
            final String outputLocation) {

        // //
        //
        // getting source size and checking tile dimensions to be not
        // bigger than the original coverage size
        //
        // //
        final RenderedImage rImage = gc.getRenderedImage();
        final int w = rImage.getWidth();
        final int h = rImage.getHeight();
        chunkWidth = chunkWidth > w ? w : chunkWidth;
        chunkHeight = chunkHeight > h ? h : chunkHeight;

        // ///////////////////////////////////////////////////////////////////
        //
        // MAIN LOOP
        //
        //
        // ///////////////////////////////////////////////////////////////////
        final int numTileX = (int) (w / (chunkWidth * 1.0) + 1);
        final int numTileY = (int) (h / (chunkHeight * 1.0) + 1);
        for (int i = 0; i < numTileX; i++)
            for (int j = 0; j < numTileY; j++) {

                // //
                //
                // computing the bbox for this tile
                //
                // //
                final Rectangle sourceRegion = new Rectangle(i * chunkWidth, j
                        * chunkHeight, chunkWidth, chunkHeight);

                // //
                //
                // building gridgeometry for the read operation with the actual
                // envelope
                //
                // //
                final File fileOut = new File(outputLocation,
                        new StringBuilder("raw_mosaic").append("_").append(
                                Integer.toString(i * chunkWidth + j)).append(
                                ".").append("tiff").toString());
                // remove an old output file if it exists
                if (fileOut.exists())
                    fileOut.delete();

                // //
                //
                // Write this coverage out as a geotiff
                //
                // //
                final AbstractGridFormat outFormat = new GeoTiffFormat();
                try {

                    final GeoTiffWriteParams wp = new GeoTiffWriteParams();
                    wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
                    wp.setTiling(internalTileWidth, internalTileHeight);
                    wp.setSourceRegion(sourceRegion);
                    if (compressionScheme != null
                            && !Double.isNaN(compressionRatio)) {
                        wp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        wp.setCompressionType(compressionScheme);
                        wp.setCompressionQuality((float) compressionRatio);
                    }
                    final ParameterValueGroup params = outFormat
                            .getWriteParameters();
                    params.parameter(
                            AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName()
                                    .toString()).setValue(wp);

                    final GeoTiffWriter writerWI = new GeoTiffWriter(fileOut);
                    writerWI.write(gc, (GeneralParameterValue[]) params
                            .values().toArray(new GeneralParameterValue[1]));
                    writerWI.dispose();

                    // TODO: Leverage on GeoTiffOverviewsEmbedder when involving
                    // no more only FileSystemEvent
                    addOverviews(fileOut.getAbsolutePath());

                } catch (IOException e) {
                    return;
                }
            }
    }

    private void addOverviews(final String inputFileName) {
    	
    	LOGGER.log(Level.INFO, "Adding overviews");
        int downsampleStep = configuration.getDownsampleStep();
        if (downsampleStep <= 0)
            throw new IllegalArgumentException("Illegal downsampleStep: "
                    + downsampleStep);
        int numberOfSteps = configuration.getNumSteps();
        if (numberOfSteps <= 0)
            throw new IllegalArgumentException("Illegal numberOfSteps: "
                    + numberOfSteps);

        final OverviewsEmbedder oe = new OverviewsEmbedder();
        oe.setDownsampleStep(downsampleStep);
        oe.setNumSteps(configuration.getNumSteps());
        oe.setInterp(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
        oe.setScaleAlgorithm(configuration.getScaleAlgorithm());
        oe.setTileHeight(configuration.getTileH());
        oe.setTileWidth(configuration.getTileW());
        oe.setSourcePath(inputFileName);
        oe.run();
    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }
}
