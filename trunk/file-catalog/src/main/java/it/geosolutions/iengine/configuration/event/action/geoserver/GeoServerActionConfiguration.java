package it.geosolutions.iengine.configuration.event.action.geoserver;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;

import java.util.List;

public class GeoServerActionConfiguration extends ActionConfiguration implements Configuration {

    private String workingDirectory;

    private String crs;

    private String envelope;

    private String geoserverPWD;

    private String geoserverUID;

    private String geoserverURL;

    private String storeFilePrefix;

    private String configId;

    private String datatype;

    private String defaultNamespace;

    private String defaultNamespaceUri;

    private String defaultStyle;

    private String wmsPath;

    private List<String> styles;

    private String dataTransferMethod;

    public GeoServerActionConfiguration() {
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

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getEnvelope() {
        return envelope;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }

    public String getGeoserverPWD() {
        return geoserverPWD;
    }

    public void setGeoserverPWD(String geoserverPWD) {
        this.geoserverPWD = geoserverPWD;
    }

    public String getGeoserverUID() {
        return geoserverUID;
    }

    public void setGeoserverUID(String geoserverUID) {
        this.geoserverUID = geoserverUID;
    }

    public String getGeoserverURL() {
        return geoserverURL;
    }

    public void setGeoserverURL(String geoserverURL) {
        this.geoserverURL = geoserverURL;
    }

    public String getStoreFilePrefix() {
        return storeFilePrefix;
    }

    public void setStoreFilePrefix(String storeFilePrefix) {
        this.storeFilePrefix = storeFilePrefix;
    }

    public List<String> getStyles() {
        return styles;
    }

    public void setStyles(List<String> styles) {
        this.styles = styles;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public String getDefaultNamespaceUri() {
        return defaultNamespaceUri;
    }

    public void setDefaultNamespaceUri(String defaultNamespaceUri) {
        this.defaultNamespaceUri = defaultNamespaceUri;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public String getWmsPath() {
        return wmsPath;
    }

    public void setWmsPath(String wmsPath) {
        this.wmsPath = wmsPath;
    }

    public String getDataTransferMethod() {
        return dataTransferMethod;
    }

    public void setDataTransferMethod(String dataTransferMethod) {
        this.dataTransferMethod = dataTransferMethod;
    }

}
