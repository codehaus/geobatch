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

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageWriteParam;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class TileWriter implements Callable<String> {
	
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("it.geosolutions.geobatch.mosaic");
	
	private Rectangle sourceRegion;
	
	private int numTileX;
	
	private int numTileY;
	
	private int tileWidth;
	
	private int tileHeight;
	
	private GridCoverage2D gc;
	
	private String compressionScheme;
	
	private float compressionRatio;

	private int row;

	private int column;

	private String fileName;

	
	public TileWriter(final GridCoverage2D gc, final Rectangle sourceRegion, final int row, final int column,
			final int numTileX, final int numTileY, final int tileWidth, final int tileHeight, 
			final String fileName, final String compressionScheme, final float compressionRatio){
		this.gc = gc;
		this.sourceRegion = sourceRegion;
		this.numTileX = numTileX;
		this.numTileY = numTileY;
		this.tileHeight = tileHeight;
		this.tileWidth = tileWidth;
		this.fileName = fileName;
		this.row = row;
		this.column = column;
		this.compressionRatio = compressionRatio;
		this.compressionScheme = compressionScheme;
	}
	
	public String call() throws Exception {
	    // //
        //
        // building gridgeometry for the read operation with the actual
        // envelope
        //
        // //
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
            wp.setTiling(tileWidth, tileHeight);
            wp.setSourceRegion(sourceRegion);
            if (compressionScheme != null&& !Double.isNaN(compressionRatio)) {
                wp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                wp.setCompressionType(compressionScheme);
                wp.setCompressionQuality((float) compressionRatio);
            }
            final ParameterValueGroup params = outFormat.getWriteParameters();
            params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

            if (LOGGER.isLoggable(Level.INFO))
            	LOGGER.info(new StringBuilder("Writing tile: ").append(row+1).append(" of ")
            			.append(numTileX).append(" [X] -- ").append(column+1).append(" of ").
            			append(numTileY).append(" [Y]").toString());
            
            final GeoTiffWriter writerWI = new GeoTiffWriter(fileOut);
            writerWI.write(gc, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
            writerWI.dispose();
        } catch (IOException e) {
        	 if (LOGGER.isLoggable(Level.WARNING))
             	LOGGER.warning("Exception occurred whilst writing tiles:" + e.getLocalizedMessage());
        }
        
        return fileName;
	}
}
