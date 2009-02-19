package it.geosolutions.iengine.catalog.dao.file.jibx;

import it.geosolutions.iengine.catalog.dao.FlowManagerConfigurationDAO;
import it.geosolutions.iengine.configuration.flow.FlowConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedCatalogConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

public class JIBXFlowConfigurationDAO
		extends JIBXDAO<FlowConfiguration>
		implements FlowManagerConfigurationDAO {

    public JIBXFlowConfigurationDAO(String directory) {
        super(directory);
    }

    public FileBasedFlowConfiguration find(FlowConfiguration exampleInstance, boolean lock) {
        return find(exampleInstance.getId(), lock);
    }

    public FileBasedFlowConfiguration find(String id, boolean lock) {
        try {
            final File entityfile = new File(getBaseDirectory(), id + ".xml");
            if (entityfile.exists() && entityfile.canRead()) {
                IBindingFactory bfact = BindingDirectory.getFactory(FileBasedCatalogConfiguration.class);
                IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
                FileBasedFlowConfiguration obj = (FileBasedFlowConfiguration) uctx.unmarshalDocument(new BufferedInputStream(new FileInputStream(entityfile)), null);
                return obj;

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
