package it.geosolutions.iengine.catalog.dao.file.jibx;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.dao.DAO;
import it.geosolutions.iengine.catalog.dao.DAOService;
import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.configuration.CatalogConfiguration;
import it.geosolutions.iengine.configuration.flow.FlowConfiguration;

public class JIBXDAOService<T extends Configuration> extends BaseService implements
        DAOService<T, String> {

    private String baseDirectory;

    public JIBXDAOService() {
        super(true);
    }

    public JIBXDAOService(String baseDirectory) {
        super(true);
        this.baseDirectory = baseDirectory;
    }

    public DAO createDAO(Class<T> clazz) {
        if (clazz.isAssignableFrom(FlowConfiguration.class))
            return new JIBXFlowConfigurationDAO(this.baseDirectory);
        if (clazz.isAssignableFrom(CatalogConfiguration.class))
            return new JIBXCatalogDAO(this.baseDirectory);
        return null;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

}
