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

import java.io.File;

import javax.media.jai.Interpolation;

public class Utils {

	public static final String LEG_PREFIX = "_Leg";

	/**
	 * Build a proper run name.
	 * 
	 * @param location
	 * @param time
	 * @param prefix
	 * @return
	 */
	public static String buildRunName(final String location, final String time, final String prefix){
    	String dirName = "";
    	final File dir = new File(location);
         final String channelName = dir.getName();
         final String leg = dir.getParent();
         final File legF = new File(leg);
         final String legName = legF.getName();
         final String mission = legF.getParent();
         final File missionF = new File(mission);
//         final String missionName = missionF.getName();
         String missionName = missionF.getName();
         final int missionIndex = missionName.lastIndexOf("_");
         if (missionIndex!=-1){
             missionName = new StringBuilder("mission").append(missionName.substring(missionIndex+1)).toString();
         }
         
         dirName = new StringBuilder(location).append(File.separatorChar).append(prefix)
         .append(time).append("_")
         .append(missionName).append(LEG_PREFIX)
         .append(legName.substring(3,legName.length())).append("_")
         .append(channelName).toString();
         return dirName;
    }

	/**
	 * Add overviews to the specified input file, in compliance with the set of additional parameters
	 * @param inputFileName
	 * @param downsampleStep
	 * @param numberOfSteps
	 * @param scaleAlgorithm
	 * @param compressionScheme
	 * @param compressionRatio
	 * @param tileWidth
	 * @param tileHeight
	 */
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

	public static final char SEPARATOR = File.separatorChar;

}
