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

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import org.geotools.utils.CoverageToolsConstants;
/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class FormatConverterConfiguration extends ActionConfiguration implements
        Configuration {


	private String outputFormat;
    
    private String inputFormats;
    
    private String workingDirectory;

    private String outputDirectory;
    
    private String serviceID;

    private double compressionRatio = CoverageToolsConstants.DEFAULT_COMPRESSION_RATIO;

    private String compressionScheme = CoverageToolsConstants.DEFAULT_COMPRESSION_SCHEME;

    /** Downsampling step. */
    private int downsampleStep;

    private int numSteps;

    /** Scale algorithm. */
    private String scaleAlgorithm;

    /** Tile height. */
    private int tileH = -1;

    /** Tile width. */
    private int tileW = -1;
    
    
    public FormatConverterConfiguration() {
        super();
    }

    protected FormatConverterConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
	}
    
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
     *            the workingDirectory to set
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
     *            the serviceID to set
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

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

	@Override
	public Object clone() throws CloneNotSupportedException {
		final FormatConverterConfiguration configuration= 
			new FormatConverterConfiguration(getId(),getName(),getDescription(),isDirty());
		configuration.setCompressionRatio(compressionRatio);
		configuration.setCompressionScheme(compressionScheme);
		configuration.setDownsampleStep(downsampleStep);
		configuration.setInputFormats(inputFormats);
		configuration.setNumSteps(numSteps);
		configuration.setOutputDirectory(outputDirectory);
		configuration.setOutputFormat(outputFormat);
		configuration.setServiceID(serviceID);
		configuration.setTileH(tileH);
		configuration.setTileW(tileW);
		configuration.setWorkingDirectory(workingDirectory);
		return configuration;
	}
}
