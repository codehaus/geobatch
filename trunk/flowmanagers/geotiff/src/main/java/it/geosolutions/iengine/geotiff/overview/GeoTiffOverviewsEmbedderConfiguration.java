package it.geosolutions.iengine.geotiff.overview;

import javax.media.jai.Interpolation;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;

import org.geotools.utils.CoverageToolsConstants;

public class GeoTiffOverviewsEmbedderConfiguration extends ActionConfiguration implements
        Configuration {

    private String workingDirectory;

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

    private String wildcardString = "*.*";

    /**
     * 
     * Interpolation method used througout all the program.
     * 
     * @TODO make the interpolation method customizable from the user perpsective.
     * 
     */
    private int interp = Interpolation.INTERP_NEAREST;

    private String serviceID;

    public GeoTiffOverviewsEmbedderConfiguration() {
        super();
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

    public String getWildcardString() {
        return wildcardString;
    }

    public void setWildcardString(String wildcardString) {
        this.wildcardString = wildcardString;
    }

    public int getInterp() {
        return interp;
    }

    public void setInterp(int interp) {
        this.interp = interp;
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "id:" + getId()
				+ ", name:" + getName()
				+ ", wxh:" + getTileW() + "x" + getTileH()
				+ ", stp:" + getNumSteps()
				+ "]";
	}
}
