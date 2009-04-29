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
import it.geosolutions.geobatch.base.BaseImageProcessingConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
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
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.utils.imageoverviews.OverviewsEmbedder;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;

/**
 * Main Mosaicer class.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class AbstractMosaicer extends BaseAction<FileSystemMonitorEvent> implements
        Action<FileSystemMonitorEvent> {

    protected MosaicerConfiguration configuration;

    private final static Logger LOGGER = Logger.getLogger(AbstractMosaicer.class
            .toString());

    public AbstractMosaicer(MosaicerConfiguration configuration) throws IOException {
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(
            Queue<FileSystemMonitorEvent> events) throws Exception {
        try {
        	
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

            final File fileDir = new File(directory);
            if (fileDir != null && fileDir.isDirectory()) {
                final File[] files = fileDir.listFiles();

                
                // //
                //
                // Setting directories hierarchy
                //
                // //
                final String outputDirectory = buildOutputDirName(directory);
//                final String outputBalanced = outputDirectory.replace(MOSAIC_PREFIX, BALANCED_PREFIX);
                final File dir = new File(outputDirectory);
//                final File balDir = new File(outputBalanced);
                configuration.setMosaicDirectory(outputDirectory);

                if (!dir.exists())
                    dir.mkdir();
//                if(!balDir.exists())
//                    balDir.mkdir();

                if (files != null) {
                    final int numFiles = files.length;
//                    double rotation=0.0d;
                    for (int i = 0; i < numFiles; i++) {
                        final String path = files[i].getAbsolutePath()
                                .toLowerCase();
                        if (!path.endsWith("tif"))
                            continue;

                        // get a reader
                        final File file = files[i];
                        final GeoTiffReader reader = new GeoTiffReader(file,
                                null);

                        // //
                        //
                        // Updating the global mosaic's envelope
                        //
                        // //
                        GeneralEnvelope envelope = (GeneralEnvelope) reader
                                .getOriginalEnvelope();
                        if (globEnvelope == null) {
                            globEnvelope = new GeneralEnvelope(envelope);
                            globEnvelope.setCoordinateReferenceSystem(envelope
                                    .getCoordinateReferenceSystem());
//                            AffineTransform at = (AffineTransform)reader.getOriginalGridToWorld(PixelInCell.CELL_CENTER);
//                            rotation = XAffineTransform.getRotation(at);
                        } else
                            globEnvelope.add(envelope);
                        reader.dispose();
                    }

                    // //
                    // computing the final g2w
                    // //
                    final GeneralMatrix gm = new GeneralMatrix(3);

                    // TODO: change this Leverage on XML metadata to handle rotation
                    gm.setElement(0, 0, 0.025);
                    gm.setElement(1, 1, -0.015);
                    gm.setElement(0, 1, 0);
                    gm.setElement(1, 0, 0);
                    gm.setElement(0, 2, globEnvelope.getLowerCorner()
                            .getOrdinate(0));
                    gm.setElement(1, 2, globEnvelope.getUpperCorner()
                            .getOrdinate(1));
                    final MathTransform mosaicTransform = ProjectiveTransform
                            .create(gm);
                    final MathTransform tempTransform = PixelTranslation.translate(mosaicTransform, PixelInCell.CELL_CORNER, PixelInCell.CELL_CENTER);
                    
                    final MathTransform world2GridTransform = tempTransform
                            .inverse();

                    final GridCoverageFactory coverageFactory = CoverageFactoryFinder
                            .getGridCoverageFactory(null);

                    // read them all
                    final List<GridCoverage2D> coverages = new LinkedList<GridCoverage2D>();
                    
                    final Map<String,File> sortedFiles = sortFilesByPing(files);
                    
                    final Iterator<String> it = sortedFiles.keySet().iterator();
                    while (it.hasNext()){
                        final File file = sortedFiles.get(it.next());
                        final GeoTiffReader reader = new GeoTiffReader(file,
                                null);
                        final GridCoverage2D gc = (GridCoverage2D) reader.read(null);
                        coverages.add(gc);
                        updates(gc);
                        reader.dispose();
                    }
                    
                    final RenderedImage mosaicImage = createMosaic(coverages,world2GridTransform);
                    final RenderedImage balancedMosaic = balanceMosaic(mosaicImage);
                    
                    final GridCoverage2D balancedGc = coverageFactory.create("balanced", balancedMosaic, globEnvelope);
                    LOGGER.log(Level.INFO, "Retiling the balanced mosaic");
                    retileMosaic(balancedGc, chunkW, chunkH, tileW, tileH,
                            compressionRatio, compressionType, outputDirectory);

                    
//                    GridCoverage2D gc = coverageFactory.create("mosaiced", mosaicImage, globEnvelope);
//
//                    // //
//                    //
//                    // Retiling Mosaic to smaller Coverages
//                    //
//                    // //
//                    LOGGER.log(Level.INFO, "Retiling the raw mosaic");
//                    retileMosaic(gc, chunkW, chunkH, tileW, tileH,
//                            compressionRatio, compressionType, outputDirectory);

                }
            }

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }
    }

    private Map<String,File> sortFilesByPing(final File[] files) {
        final Map<String,File> treeMap = new TreeMap<String, File>(java.util.Collections.reverseOrder());
        final DecimalFormat nf = new DecimalFormat("0000000000");
        
        for (File file : files) {
            final String path = file.getAbsolutePath()
                    .toLowerCase();
            if (!path.endsWith("tif"))
                continue;
            
            final String name = file.getName();
            final String[] dashes = name.split("_");
            
            //TODO: Files are in the form: 
            // MUSCLE_COL2_090316_1_1_p_2_143_40_150
            // Improve this ordering logic, leveraging on metadata
            
            final String number = nf.format(Integer.parseInt(dashes[6]));
            treeMap.put(number,file);
        } 
        return treeMap;
    }

    /** Update some internal machinery to optimize balancing computations */
    protected abstract void updates(GridCoverage2D gc);

    protected abstract RenderedImage balanceMosaic (RenderedImage ri);
    
    protected abstract String buildOutputDirName(String directory) ;

    private RenderedImage createMosaic(
            final List<GridCoverage2D> coverages,
            final MathTransform world2GridTransform) {
        final int nCov = coverages.size();

        final ParameterBlockJAI pbMosaic = new ParameterBlockJAI("Mosaic");
        pbMosaic.setParameter("mosaicType", MosaicDescriptor.MOSAIC_TYPE_OVERLAY);

        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.log(Level.INFO, new StringBuffer("Found ").append(nCov).append(" tiles").toString());
        
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

        final RenderedOp mosaicImage = JAI.create("Mosaic", pbMosaic);
        return mosaicImage;
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
        // ///////////////////////////////////////////////////////////////////
        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.log(Level.INFO, "Retiling mosaic to separated files");
        final int numTileX = w!=chunkWidth? (int) (w / (chunkWidth * 1.0) + 1):1;
        final int numTileY = h!=chunkHeight? (int) (h / (chunkHeight * 1.0) + 1):1;
        final List<String> filesToAddOverviews = new ArrayList<String>(numTileX*numTileY);
        
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
                final String fileName = buildFileName(outputLocation,i,j,chunkWidth);
                final File fileOut = new File(fileName);
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

                    if (LOGGER.isLoggable(Level.INFO))
                    	LOGGER.log(Level.INFO, new StringBuilder("Writing tile: ").append(i+1)
                    			.append(" of ").append(numTileX).append(" [X] -- ")
                    			.append(j+1)
                    			.append(" of ").append(numTileY).append(" [Y]").toString());
                    
                    final GeoTiffWriter writerWI = new GeoTiffWriter(fileOut);
                    writerWI.write(gc, (GeneralParameterValue[]) params
                            .values().toArray(new GeneralParameterValue[1]));
                    writerWI.dispose();
                    filesToAddOverviews.add(fileName);
                } catch (IOException e) {
                    return;
                }
            }
        
        //Overviews are added as a last step to minimize TileCache updates
        int nOverviewsDone = 1;
        final int nFiles = filesToAddOverviews.size();
        for (String fileOverviews: filesToAddOverviews){
            // TODO: Leverage on GeoTiffOverviewsEmbedder when involving
            // no more FileSystemEvent only
            // Or merge retiling and overviews adding to a single step
            LOGGER.log(Level.INFO, new StringBuilder("Adding overviews: File ").append(nOverviewsDone).
                    append(" of ").append(nFiles).toString());
            nOverviewsDone++;
				BaseImageProcessingConfiguration.addOverviews(fileOverviews,
						configuration.getDownsampleStep(),configuration.getNumSteps(),
						configuration.getScaleAlgorithm(),configuration.getCompressionScheme(),
						configuration.getCompressionRatio(),configuration.getTileW(),
						configuration.getTileH());
        }
    }

    protected abstract String buildFileName(String outputLocation, int i, int j,
            int chunkWidth) ;

    public ActionConfiguration getConfiguration() {
        return configuration;
    }
    
}
