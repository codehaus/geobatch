package it.geosolutions.iengine.catalog.dao.file.xstream;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.dao.DAO;
import it.geosolutions.iengine.catalog.dao.DAOService;
import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.configuration.CatalogConfiguration;
import it.geosolutions.iengine.configuration.flow.FlowConfiguration;

public class XStreamDAOService<T extends Configuration> extends BaseService implements
        DAOService<T, String> {

    private String baseDirectory;

    public XStreamDAOService() {
        super(true);
    }

    public XStreamDAOService(String baseDirectory) {
        super(true);
        this.baseDirectory = baseDirectory;
    }

    public DAO createDAO(Class<T> clazz) {
        if (clazz.isAssignableFrom(FlowConfiguration.class))
            return new XStreamFlowConfigurationDAO(this.baseDirectory);
        if (clazz.isAssignableFrom(CatalogConfiguration.class))
            return new XStreamCatalogDAO(this.baseDirectory);
        return null;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

}
