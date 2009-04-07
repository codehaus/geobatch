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

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import org.geotools.utils.CoverageToolsConstants;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class ComposerConfiguration extends ActionConfiguration implements
        Configuration {

	private final static long DEFAULT_JAI_CACHE_CAPACITY = 128 * 1024 * 1024;

    private final static int DEFAULT_JAI_PARALLELISM = 2;

    private final static float DEFAULT_JAI_CACHE_THRESHOLD = 0.75f;

    private long JAICacheCapacity = DEFAULT_JAI_CACHE_CAPACITY;

    private int JAIParallelism = DEFAULT_JAI_PARALLELISM;

    private float JAICacheThreshold = DEFAULT_JAI_CACHE_THRESHOLD;

    private String outputFormat;

    private String workingDirectory;
    
    private String outputBaseFolder;
    
    private String leavesFolders;

    private String inputFormats;

    private String serviceID;
    
    public String getGeoserverURL() {
		return geoserverURL;
	}

	public void setGeoserverURL(String geoserverURL) {
		this.geoserverURL = geoserverURL;
	}

	public String getGeoserverUID() {
		return geoserverUID;
	}

	public void setGeoserverUID(String geoserverUID) {
		this.geoserverUID = geoserverUID;
	}

	public String getGeoserverPWD() {
		return geoserverPWD;
	}

	public void setGeoserverPWD(String geoserverPWD) {
		this.geoserverPWD = geoserverPWD;
	}

	public String getGeoserverUploadMethod() {
		return geoserverUploadMethod;
	}

	public void setGeoserverUploadMethod(String geoserverUploadMethod) {
		this.geoserverUploadMethod = geoserverUploadMethod;
	}

	private String geoserverURL;
    
    private String geoserverUID;
    
	private String geoserverPWD;
	
	private String geoserverUploadMethod;


    
    private double compressionRatio = CoverageToolsConstants.DEFAULT_COMPRESSION_RATIO;

    private String compressionScheme = CoverageToolsConstants.DEFAULT_COMPRESSION_SCHEME;
    
    public ComposerConfiguration() {
        super();
    }

    protected ComposerConfiguration(String id, String name, String description,
			boolean dirty) {
		super(id, name, description, dirty);
	}

    /** Downsampling step. */
    private int downsampleStep;

    private int numSteps;

    /** Scale algorithm. */
    private String scaleAlgorithm;

    /** Tile height. */
    private int tileH = -1;

    /** Tile width. */
    private int tileW = -1;

    private int chunkW = 5120;

    public int getChunkW() {
        return chunkW;
    }

    public void setChunkW(final int chunkW) {
        this.chunkW = chunkW;
    }

    public int getChunkH() {
        return chunkH;
    }

    public void setChunkH(final int chunkH) {
        this.chunkH = chunkH;
    }

    private int chunkH = 5120;

    public final double getCompressionRatio() {
        return compressionRatio;
    }

    public final String getCompressionScheme() {
        return compressionScheme;
    }

    public int getDownsampleStep() {
        return downsampleStep;
    }

    public String getScaleAlgorithm() {
        return scaleAlgorithm;
    }

    public int getTileH() {
        return tileH;
    }

    public int getTileW() {
        return tileW;
    }

    public void setCompressionRatio(double compressionRatio) {
        this.compressionRatio = compressionRatio;
    }

    public void setCompressionScheme(String compressionScheme) {
        this.compressionScheme = compressionScheme;
    }

    public void setDownsampleStep(int downsampleWH) {
        this.downsampleStep = downsampleWH;
    }

    public void setScaleAlgorithm(String scaleAlgorithm) {
        this.scaleAlgorithm = scaleAlgorithm;
    }

    public void setTileH(int tileH) {
        this.tileH = tileH;
    }

    public void setTileW(int tileW) {
        this.tileW = tileW;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(int numSteps) {
        this.numSteps = numSteps;
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *                the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
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

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getInputFormats() {
        return inputFormats;
    }

    public void setInputFormats(String inputFormats) {
        this.inputFormats = inputFormats;
    }

    public float getJAICacheThreshold() {
        return JAICacheThreshold;
    }

    public void setJAICacheThreshold(float cacheThreshold) {
        JAICacheThreshold = cacheThreshold;
    }

    public int getJAIParallelism() {
        return JAIParallelism;
    }

    public void setJAIParallelism(int parallelism) {
        JAIParallelism = parallelism;
    }

    public long getJAICacheCapacity() {
        return JAICacheCapacity;
    }

    public void setJAICacheCapacity(final long JAICacheCapacity) {
        this.JAICacheCapacity = JAICacheCapacity;
    }

    public String getOutputBaseFolder() {
        return outputBaseFolder;
    }

    public void setOutputBaseFolder(String outputBaseFolder) {
        this.outputBaseFolder = outputBaseFolder;
    }

    public String getLeavesFolders() {
        return leavesFolders;
    }

    public void setLeavesFolders(String leavesFolders) {
        this.leavesFolders = leavesFolders;
    }

	@Override
	public Object clone() throws CloneNotSupportedException {
		final ComposerConfiguration configuration=
			new ComposerConfiguration(getId(),getName(),getDescription(),isDirty());
		configuration.setServiceID(serviceID);
		configuration.setChunkH(chunkH);
		configuration.setChunkW(chunkW);
		configuration.setCompressionRatio(compressionRatio);
		configuration.setCompressionScheme(compressionScheme);
		configuration.setDownsampleStep(downsampleStep);
		configuration.setInputFormats(inputFormats);
		configuration.setJAICacheCapacity(JAICacheCapacity);
		configuration.setJAICacheThreshold(JAICacheThreshold);
		configuration.setJAIParallelism(JAIParallelism);
		configuration.setLeavesFolders(leavesFolders);
		configuration.setNumSteps(numSteps);
		configuration.setOutputBaseFolder(outputBaseFolder);
		configuration.setOutputFormat(outputFormat);
		configuration.setScaleAlgorithm(scaleAlgorithm);
		configuration.setTileH(tileH);
		configuration.setTileW(tileW);
		configuration.setWorkingDirectory(workingDirectory);
		configuration.setGeoserverPWD(geoserverPWD);
		configuration.setGeoserverUID(geoserverUID);
		configuration.setGeoserverUploadMethod(geoserverUploadMethod);
		configuration.setGeoserverURL(geoserverURL);
		return configuration;
	}
}
