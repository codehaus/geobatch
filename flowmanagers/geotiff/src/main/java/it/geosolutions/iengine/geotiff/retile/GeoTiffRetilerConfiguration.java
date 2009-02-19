package it.geosolutions.iengine.geotiff.retile;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;

import org.geotools.utils.CoverageToolsConstants;

public class GeoTiffRetilerConfiguration extends ActionConfiguration implements
        Configuration {

	private long JAICapacity;
	
    public long getJAICapacity() {
		return JAICapacity;
	}

	public void setJAICapacity(long JAICapacity) {
			this.JAICapacity = JAICapacity;
	}

	private String workingDirectory;

    private double compressionRatio = Double.NaN;
    
    private String compressionScheme = CoverageToolsConstants.DEFAULT_COMPRESSION_SCHEME;

    /** Tile height. */
    private int tileH = 256;

    /** Tile width. */
    private int tileW = 256;

    private String serviceID;

    public GeoTiffRetilerConfiguration() {
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
				+ "]";
	}
}
