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
import it.geosolutions.geobatch.flow.event.action.Action;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper;
import org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ChannelSelectionImpl;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.ContrastEnhancementImpl;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.SelectedChannelTypeImpl;
import org.geotools.styling.StyleBuilder;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class Mosaicer extends AbstractMosaicer implements
        Action<FileSystemMonitorEvent> {

    private final static boolean IMAGE_IS_LINEAR;

    static{
        final String cl = System.getenv("SAS_COMPUTE_LOG");
        IMAGE_IS_LINEAR = !Boolean.parseBoolean(cl);
    }
    
    public static final String MOSAIC_PREFIX = "rawm_";
    public static final String BALANCED_PREFIX = "balm_";
    
    private double extrema[] = new double[]{Double.MAX_VALUE,Double.MIN_VALUE} ;

    public Mosaicer(MosaicerConfiguration configuration) throws IOException {
        super(configuration);
    }


    protected void updates(GridCoverage2D gc) {
        RenderedImage sourceImage = gc.getRenderedImage();
        if (IMAGE_IS_LINEAR){
            sourceImage = computeLog(sourceImage);
        }
        
        final ROI roi = new ROI(sourceImage, 0);
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(sourceImage); // The source image
        if (roi != null)
            pb.add(roi); // The region of the image to scan

        // Perform the extrema operation on the source image
        RenderedOp ex = JAI.create("extrema", pb);

        // Retrieve both the maximum and minimum pixel value
        final double[][] ext = (double[][]) ex.getProperty("extrema");
        
        if(extrema[0]>ext[0][0])
            extrema[0]=ext[0][0];
        if (extrema[1]<ext[1][0])
            extrema[1]=ext[1][0];
    }

    private RenderedImage computeLog(RenderedImage sourceImage) {
        final ParameterBlockJAI pbLog = new ParameterBlockJAI("Log");
        pbLog.addSource(sourceImage);
        RenderedOp logarithm = JAI.create("Log", pbLog);

        // //
        //
        // Applying a rescale to handle Decimal Logarithm.
        //
        // //
        final ParameterBlock pbRescale = new ParameterBlock();
        
        // Using logarithmic properties 
        final double scaleFactor = 20 / Math.log(10);

        final double[] scaleF = new double[] { scaleFactor };
        final double[] offsetF = new double[] { 0 };

        pbRescale.add(scaleF);
        pbRescale.add(offsetF);
        pbRescale.addSource(logarithm);

        return JAI.create("Rescale", pbRescale);
    }

    protected RenderedImage balanceMosaic(RenderedImage mosaicImage) {
        RenderedImage inputImage = mosaicImage;
        if (IMAGE_IS_LINEAR){
            inputImage = computeLog(inputImage);
        }
        
        final double[] scale = new double[] { (255) / (extrema[1] - extrema[0]) };
        final double[] offset = new double[] { ((255) * extrema[0])
                / (extrema[0] - extrema[1]) };

        // Preparing to rescaling values
        ParameterBlock pbRescale = new ParameterBlock();
        pbRescale.add(scale);
        pbRescale.add(offset);
        pbRescale.addSource(inputImage);
        RenderedOp rescaledImage = JAI.create("Rescale", pbRescale);

        ParameterBlock pbConvert = new ParameterBlock();
        pbConvert.addSource(rescaledImage);
        pbConvert.add(DataBuffer.TYPE_BYTE);
        RenderedOp destImage = JAI.create("format", pbConvert);
        
        return destImage;
//        return applyContrastEnhancement(destImage);
    }
    
    private RenderedImage applyContrastEnhancement(RenderedImage image){
        
        GridCoverage2D gc = CoverageFactoryFinder.getGridCoverageFactory(null)
        .create(
                        "name",
                        image, 
                        new GeneralEnvelope(new double[] { -90, -180 },
                                        new double[] { 90, 180 }));

        // the RasterSymbolizer Helper
        SubchainStyleVisitorCoverageProcessingAdapter rsh_StyleBuilder = new RasterSymbolizerHelper(gc, null);
        // build the RasterSymbolizer
        StyleBuilder sldBuilder = new StyleBuilder();
        // the RasterSymbolizer Helper
        rsh_StyleBuilder = new RasterSymbolizerHelper(gc, null);

        final RasterSymbolizer rsb_1 = sldBuilder.createRasterSymbolizer();
        final ChannelSelection chSel = new ChannelSelectionImpl();
        final SelectedChannelType chTypeGray = new SelectedChannelTypeImpl();
        final ContrastEnhancement cntEnh = new ContrastEnhancementImpl();

        cntEnh.setLogarithmic();
        
        chTypeGray.setChannelName("1");
        chTypeGray.setContrastEnhancement(cntEnh);
        chSel.setGrayChannel(chTypeGray);
        rsb_1.setChannelSelection(chSel);
        rsb_1.setOpacity(sldBuilder.literalExpression(1.0));
        rsb_1.setOverlap(sldBuilder.literalExpression("AVERAGE"));
        
        // visit the RasterSymbolizer
        rsh_StyleBuilder.visit(rsb_1);
        return ((GridCoverage2D)rsh_StyleBuilder.getOutput()).getRenderedImage();
    }


    /**
     * 
     * @param outputLocation
     * @return
     */
    protected String buildOutputDirName(final String outputLocation){
    	String dirName = "";
    	final File outputDir = new File(outputLocation);
         final String channelName = outputDir.getName();
         final String leg = outputDir.getParent();
         final File legF = new File(leg);
         final String legName = legF.getName();
         final String mission = legF.getParent();
         final File missionF = new File(mission);
         final String missionName = missionF.getName();
         final String time = configuration.getTime();
         dirName = new StringBuilder(outputLocation).append(File.separatorChar).append(MOSAIC_PREFIX)
         .append(time).append("_")
         .append(missionName).append("_L")
         .append(legName.substring(3,legName.length())).append("_")
         .append(channelName.substring(0,1)).append(File.separatorChar).toString();
         return dirName;
    }
    
    protected String buildFileName(final String outputLocation, final int i, final int j,
            final int chunkWidth) {
        final String name = new StringBuilder(outputLocation).append("m_")
        .append(Integer.toString(i * chunkWidth + j)).append(
                        ".").append("tif").toString();
        return name;
    }
}
