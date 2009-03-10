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

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;

/**
 * Comments here ...
 * 
 * @author Simone Giannechini, GeoSolutions
 * 
 * @version $GeoTIFFOverviewsEmbedder.java $ Revision: x.x $ 23/mar/07 11:42:25
 */
public class Mosaicer extends BaseAction<FileSystemMonitorEvent>
        implements Action<FileSystemMonitorEvent> {

    private MosaicerConfiguration configuration;

    private final static Logger LOGGER = Logger.getLogger(Mosaicer.class
            .toString());

    public Mosaicer(MosaicerConfiguration configuration)
            throws IOException {
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(
            Queue<FileSystemMonitorEvent> events) throws Exception {
        try {

            // looking for file
//            if (events.size() != 1)
//                throw new IllegalArgumentException(
//                        "Wrong number of elements for this action: "
//                                + events.size());
//
//            // get the first event
//            final FileSystemMonitorEvent event = events.peek();
//            final File inputFile = event.getSource();
//            
            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////

//            final String parent = inputFile.getParent();
//            File dataDir = new File(parent);
            
            GeneralEnvelope globEnvelope = null;

            final String directory = configuration.getWorkingDirectory();
            final double compressionRatio = configuration.getCompressionRatio();
            final String compressionType = configuration.getCompressionScheme();
            final int tileW = configuration.getTileW();
            final int tileH = configuration.getTileH();
            
            
            File fileDir = new File(directory);
            if (fileDir != null && fileDir.isDirectory()) {
            File[] files = fileDir.listFiles();
            final String fileOutputName = new StringBuilder(directory).append("/mosaic.tif").toString();
            final File outputFile = new File(fileOutputName);
            
            if (files != null) {
                final int numFiles = files.length;
                for (int i = 0; i < numFiles; i++) {
                    final String path = files[i].getAbsolutePath()
                            .toLowerCase();
                    if (!path.endsWith("tif"))
                        continue;

                    // get a reader
                    final File file = files[i];
                    final GeoTiffReader reader = new GeoTiffReader(file, null);

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
                MathTransform mosaicTransform = ProjectiveTransform.create(gm);
                MathTransform world2GridTransform = mosaicTransform.inverse();

                GridCoverageFactory coverageFactory = CoverageFactoryFinder
                        .getGridCoverageFactory(null);

                final GridGeometry2D gg2d = new GridGeometry2D(
                        PixelInCell.CELL_CORNER, mosaicTransform, globEnvelope,
                        null);

                // read them all
                final List<GridCoverage2D> coverages = new ArrayList<GridCoverage2D>();
                for (File file : files) {
                    final String path = file.getAbsolutePath().toLowerCase();
                    if (!path.endsWith("tif"))
                        continue;

                    final GeoTiffReader reader = new GeoTiffReader(file, null);

                    coverages.add((GridCoverage2D) reader.read(null));
                    reader.dispose();
                }

                final int nCov = coverages.size();

                final ParameterBlockJAI pbMosaic = new ParameterBlockJAI(
                        "Mosaic");
                pbMosaic.setParameter("mosaicType",
                        MosaicDescriptor.MOSAIC_TYPE_BLEND);

                for (int i = 0; i < nCov; i++) {
                    final GridCoverage2D coverage = coverages.get(i);

                    final ParameterBlockJAI pbAffine = new ParameterBlockJAI(
                            "Affine");
                    pbAffine.addSource(coverage.getRenderedImage());
                    AffineTransform at = (AffineTransform) coverage
                            .getGridGeometry().getGridToCRS2D();
                    AffineTransform chained = (AffineTransform) at.clone();
                    chained
                            .preConcatenate((AffineTransform) world2GridTransform);
                    pbAffine.setParameter("transform", chained);
                    final RenderedOp affine = JAI.create("Affine", pbAffine);
                    pbMosaic.addSource(affine);
                }

                RenderedOp mosaicImage = JAI.create("Mosaic", pbMosaic);
                GridCoverage2D gc = coverageFactory.create("my", mosaicImage,
                        globEnvelope);
                GeoTiffWriter writerTiff = new GeoTiffWriter(outputFile);
                
                // //
                // Setting write parameters
                // //
                final GeoTiffWriteParams wp = new GeoTiffWriteParams();
    			if (!Double.isNaN(compressionRatio)&&compressionType!=null) {
    				wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
    				wp.setCompressionType(compressionType);
    				wp.setCompressionQuality((float) compressionRatio);
    			}
    			wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
    			wp.setTiling(tileW,tileH);
    			final ParameterValueGroup wparams = new GeoTiffFormat().getWriteParameters();
    			wparams.parameter(
    					AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName()
    							.toString()).setValue(wp);
                
                writerTiff.write(gc, (GeneralParameterValue[]) wparams.values().toArray(new GeneralParameterValue[1]));
                writerTiff.dispose();

            }
        }

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
}
