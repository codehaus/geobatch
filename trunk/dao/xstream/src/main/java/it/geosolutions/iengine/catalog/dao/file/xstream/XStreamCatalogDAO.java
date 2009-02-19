package it.geosolutions.iengine.catalog.dao.file.xstream;

import com.thoughtworks.xstream.XStream;
import it.geosolutions.iengine.catalog.dao.CatalogConfigurationDAO;
import it.geosolutions.iengine.configuration.CatalogConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedCatalogConfiguration;

import it.geosolutions.iengine.xstream.Alias;
import java.io.File;
import java.io.FileInputStream;

public class XStreamCatalogDAO extends XStreamDAO<CatalogConfiguration> implements
        CatalogConfigurationDAO {

    public XStreamCatalogDAO(String directory) {
        super(directory);
    }

    public CatalogConfiguration find(CatalogConfiguration exampleInstance, boolean lock) {
        return find(exampleInstance.getId(), lock);
    }

    public CatalogConfiguration find(String id, boolean lock) {
        try {
            final File entityfile = new File(getBaseDirectory(), id + ".xml");
            if (entityfile.canRead() && !entityfile.isDirectory()) {
                XStream xstream = new XStream();
                Alias.setAliases(xstream);
                FileBasedCatalogConfiguration obj = (FileBasedCatalogConfiguration) xstream.fromXML(new FileInputStream(entityfile));
                if (obj.getWorkingDirectory() == null)
                    obj.setWorkingDirectory(getBaseDirectory());
                System.out.println("XStreamCatalogDAO:: FOUND " + id + ">" + obj + "<");
                return obj;

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
