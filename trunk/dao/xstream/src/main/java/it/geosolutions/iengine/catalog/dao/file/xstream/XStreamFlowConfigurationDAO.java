package it.geosolutions.iengine.catalog.dao.file.xstream;

import it.geosolutions.iengine.catalog.dao.FlowManagerConfigurationDAO;
import it.geosolutions.iengine.configuration.flow.FlowConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.iengine.xstream.Alias;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

public class XStreamFlowConfigurationDAO extends XStreamDAO<FlowConfiguration> implements
        FlowManagerConfigurationDAO {

	private final static Logger LOGGER=Logger.getLogger(XStreamFlowConfigurationDAO.class.toString());
	
    public XStreamFlowConfigurationDAO(String directory) {
        super(directory);
    }

    public FileBasedFlowConfiguration find(FlowConfiguration exampleInstance, boolean lock) {
        return find(exampleInstance.getId(), lock);
    }

    public FileBasedFlowConfiguration find(String id, boolean lock) {
        try {
            final File entityfile = new File(getBaseDirectory(), id + ".xml");
            if (entityfile.canRead() && !entityfile.isDirectory()) {
                XStream xstream = new XStream();
                Alias.setAliases(xstream);

                FileBasedFlowConfiguration obj = (FileBasedFlowConfiguration) xstream
                        .fromXML(new BufferedInputStream(new FileInputStream(entityfile)));
                return obj;
            }
        } catch (Throwable e) {
            if(LOGGER.isLoggable(Level.SEVERE))
            	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        }
        return null;
    }

}
