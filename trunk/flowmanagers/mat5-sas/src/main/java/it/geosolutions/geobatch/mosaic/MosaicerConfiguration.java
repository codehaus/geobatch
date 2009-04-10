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

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import org.geotools.utils.CoverageToolsConstants;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class MosaicerConfiguration extends ActionConfiguration implements
        Configuration {
	


	private String mosaicDirectory;

	public String getMosaicDirectory() {
		return mosaicDirectory;
	}

	public void setMosaicDirectory(String mosaicDirectory) {
		this.mosaicDirectory = mosaicDirectory;
	}

	private String workingDirectory;

    private double compressionRatio = Double.NaN;
    
    private String compressionScheme = CoverageToolsConstants.DEFAULT_COMPRESSION_SCHEME;
    
    private int chunkWidth = 5120;
    
    private int chunkHeight = 5120;
    
    private int chunkSize;

    /** Downsampling step. */
    private int downsampleStep = 2;

    private int numSteps;

    /** Scale algorithm. */
    private String scaleAlgorithm = "nn";
    
    /** Tile height. */
    private int tileH = 512;

    /** Tile width. */
    private int tileW = 512;

    private String serviceID;
    
    private int tileSizeLimit;

    public MosaicerConfiguration() {
        super();
    }
	protected MosaicerConfiguration(String id, String name, String description,
			boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
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
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public final double getCompressionRatio() {
        return compressionRatio;
    }

    public final String getCompressionScheme() {
        return compressionScheme;
    }

    public int getTileH() {
        return tileH;
    }

    public int getTileW() {
        return tileW;
    }

    public void setCompressionRatio(final double compressionRatio) {
        this.compressionRatio = compressionRatio;
    }

    public void setCompressionScheme(final String compressionScheme) {
        this.compressionScheme = compressionScheme;
    }

    public void setTileH(final int tileH) {
        this.tileH = tileH;
    }

    public void setTileW(final int tileW) {
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
     *            the serviceID to set
     */
    public void setServiceID(final String serviceID) {
        this.serviceID = serviceID;
    }

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "id:" + getId()
				+ ", name:" + getName()
				+ ", wxh:" + getTileW() + "x" + getTileH()
				+ "]";
	}

    public int getTileSizeLimit() {
        return tileSizeLimit;
    }

    public void setTileSizeLimit(final int tileSizeLimit) {
        this.tileSizeLimit = tileSizeLimit;
    }

    public int getDownsampleStep() {
        return downsampleStep;
    }

    public void setDownsampleStep(final int downsampleStep) {
        this.downsampleStep = downsampleStep;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(final int numSteps) {
        this.numSteps = numSteps;
    }

    public String getScaleAlgorithm() {
        return scaleAlgorithm;
    }

    public void setScaleAlgorithm(final String scaleAlgorithm) {
        this.scaleAlgorithm = scaleAlgorithm;
    }

	public int getChunkWidth() {
		return chunkWidth;
	}

	public void setChunkWidth(final int chunkWidth) {
		this.chunkWidth = chunkWidth;
	}

	public int getChunkHeight() {
		return chunkHeight;
	}

	public void setChunkHeight(final int chunkHeight) {
		this.chunkHeight = chunkHeight;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(final int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Override
	public MosaicerConfiguration clone() throws CloneNotSupportedException {
		final MosaicerConfiguration configuration = 
			new MosaicerConfiguration(getId(),getName(),getDescription(),isDirty());
		configuration.setChunkHeight(chunkHeight);
		configuration.setChunkWidth(chunkWidth);
		configuration.setChunkSize(chunkSize);
		configuration.setCompressionRatio(compressionRatio);
		configuration.setCompressionScheme(compressionScheme);
		configuration.setDownsampleStep(downsampleStep);
		configuration.setMosaicDirectory(mosaicDirectory);
		configuration.setNumSteps(numSteps);
		configuration.setScaleAlgorithm(scaleAlgorithm);
		configuration.setServiceID(serviceID);
		configuration.setTileH(tileH);
		configuration.setTileW(tileW);
		configuration.setTileSizeLimit(tileSizeLimit);
		configuration.setWorkingDirectory(workingDirectory);
		return configuration;
	}
}
