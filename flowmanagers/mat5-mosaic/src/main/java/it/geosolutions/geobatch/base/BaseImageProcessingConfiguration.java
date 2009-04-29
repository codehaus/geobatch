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
package it.geosolutions.geobatch.base;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.io.File;

import javax.media.jai.Interpolation;

import org.geotools.utils.CoverageToolsConstants;
import org.geotools.utils.imageoverviews.OverviewsEmbedder;

public abstract class BaseImageProcessingConfiguration extends ActionConfiguration
		implements Configuration {

	private String workingDirectory;
	/** Downsampling step. */
	private int downsampleStep;
	private int numSteps;
	/** Scale algorithm. */
	private String scaleAlgorithm;
	private double compressionRatio = CoverageToolsConstants.DEFAULT_COMPRESSION_RATIO;
	private String compressionScheme = CoverageToolsConstants.DEFAULT_COMPRESSION_SCHEME;
	/** Tile height. */
	private int tileH = -1;
	/** Tile width. */
	private int tileW = -1;
	private String serviceID;

	private String time = "";
    
	public BaseImageProcessingConfiguration(String id, String name, String description,
			boolean dirty) {
		super(id, name, description, dirty);
	}

	public BaseImageProcessingConfiguration() {
		super();
	}

	public void setTime(final String time) {
        this.time = time;
    }
    
    public String getTime() {
        return time;
    }
	
	/**
	 * @return the workingDirectory
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	/**
	 * @param workingDirectory
	 *            the workingDirectory to set
	 */
	public void setWorkingDirectory(final String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public int getDownsampleStep() {
		return downsampleStep;
	}

	public String getScaleAlgorithm() {
		return scaleAlgorithm;
	}

	public void setDownsampleStep(final int downsampleWH) {
		this.downsampleStep = downsampleWH;
	}

	public void setScaleAlgorithm(final String scaleAlgorithm) {
		this.scaleAlgorithm = scaleAlgorithm;
	}

	public int getNumSteps() {
		return numSteps;
	}

	public void setNumSteps(final int numSteps) {
		this.numSteps = numSteps;
	}

	public final double getCompressionRatio() {
		return compressionRatio;
	}

	public final String getCompressionScheme() {
		return compressionScheme;
	}

	public void setCompressionRatio(double compressionRatio) {
		this.compressionRatio = compressionRatio;
	}

	public void setCompressionScheme(String compressionScheme) {
		this.compressionScheme = compressionScheme;
	}

	public int getTileH() {
	    return tileH;
	}

	public int getTileW() {
	    return tileW;
	}

	public void setTileH(int tileH) {
	    this.tileH = tileH;
	}

	public void setTileW(int tileW) {
	    this.tileW = tileW;
	}

	/**
	 * @return the serviceID
	 */
	public String getServiceID() {
	    return serviceID;
	}

	/**
	 * @param serviceID
	 *                the serviceID to set
	 */
	public void setServiceID(String serviceID) {
	    this.serviceID = serviceID;
	}

	public static String buildRunName(final String location, final String time, final String prefix){
    	String dirName = "";
    	final File dir = new File(location);
         final String channelName = dir.getName();
         final String leg = dir.getParent();
         final File legF = new File(leg);
         final String legName = legF.getName();
         final String mission = legF.getParent();
         final File missionF = new File(mission);
         final String missionName = missionF.getName();
         dirName = new StringBuilder(location).append(File.separatorChar).append(prefix)
         .append(time).append("_")
         .append(missionName).append("_L")
         .append(legName.substring(3,legName.length())).append("_")
         .append(channelName).append(File.separatorChar).toString();
         return dirName;
    }
	
	public static void addOverviews(final String inputFileName, final int downsampleStep,
			final int numberOfSteps, final String scaleAlgorithm, final String compressionScheme,
			final double compressionRatio, final int tileWidth, final int tileHeight) {
        
        if (downsampleStep <= 0)
            throw new IllegalArgumentException("Illegal downsampleStep: "
                    + downsampleStep);
        if (numberOfSteps <= 0)
            throw new IllegalArgumentException("Illegal numberOfSteps: "
                    + numberOfSteps);

        final OverviewsEmbedder oe = new OverviewsEmbedder();
        oe.setDownsampleStep(downsampleStep);
        oe.setNumSteps(numberOfSteps);
        oe.setInterp(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
        oe.setScaleAlgorithm(scaleAlgorithm);
        oe.setTileHeight(tileHeight);
        oe.setTileWidth(tileWidth);
        oe.setSourcePath(inputFileName);
        if (compressionScheme != null
                && !Double.isNaN(compressionRatio)) {
            oe.setCompressionRatio(compressionRatio);
            oe.setCompressionScheme(compressionScheme);
        }
       
        oe.run();
    }
}
