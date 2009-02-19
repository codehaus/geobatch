package it.geosolutions.iengine.catalog.dao.file.jibx;

import it.geosolutions.iengine.catalog.dao.CatalogConfigurationDAO;
import it.geosolutions.iengine.configuration.CatalogConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedCatalogConfiguration;

import java.io.File;
import java.io.FileInputStream;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

public class JIBXCatalogDAO extends JIBXDAO<CatalogConfiguration> implements
        CatalogConfigurationDAO {

    public JIBXCatalogDAO(String directory) {
        super(directory);
    }

    public CatalogConfiguration find(CatalogConfiguration exampleInstance, boolean lock) {
        return find(exampleInstance.getId(), lock);
    }

    public CatalogConfiguration find(String id, boolean lock) {
        try {
            final File entityfile = new File(getBaseDirectory(), id + ".xml");
            if (entityfile.exists() && entityfile.canRead()) {
                IBindingFactory bfact = BindingDirectory.getFactory(FileBasedCatalogConfiguration.class);
                IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
                FileBasedCatalogConfiguration obj = (FileBasedCatalogConfiguration) uctx.unmarshalDocument(new FileInputStream(entityfile), null);
                return obj;

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
