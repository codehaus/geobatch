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



package it.geosolutions.geobatch.catalog.dao.file.xstream;

import it.geosolutions.geobatch.catalog.dao.FlowManagerConfigurationDAO;
import it.geosolutions.geobatch.configuration.flow.FlowConfiguration;
import it.geosolutions.geobatch.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.geobatch.xstream.Alias;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

public class XStreamFlowConfigurationDAO
        extends XStreamDAO<FlowConfiguration>
        implements FlowManagerConfigurationDAO {

	private final static Logger LOGGER = Logger.getLogger(XStreamFlowConfigurationDAO.class.toString());
	
    public XStreamFlowConfigurationDAO(String directory, Alias alias) {
        super(directory, alias);
    }

    public FileBasedFlowConfiguration find(FlowConfiguration exampleInstance, boolean lock) {
        return find(exampleInstance.getId(), lock);
    }

    public FileBasedFlowConfiguration find(String id, boolean lock) {
        try {
            final File entityfile = new File(getBaseDirectory(), id + ".xml");
            if (entityfile.canRead() && !entityfile.isDirectory()) {
                XStream xstream = new XStream();
                alias.setAliases(xstream);

                FileBasedFlowConfiguration obj = (FileBasedFlowConfiguration) xstream
                        .fromXML(new BufferedInputStream(new FileInputStream(entityfile)));

				if(obj.getEventConsumerConfiguration() == null)
					LOGGER.severe("FileBasedFlowConfiguration " + obj + " does not have a ConsumerCfg");

				if(obj.getEventGeneratorConfiguration() == null)
					LOGGER.severe("FileBasedFlowConfiguration " + obj + " does not have a GeneratorCfg");

                return obj;
            }
        } catch (Throwable e) {
            if(LOGGER.isLoggable(Level.SEVERE))
           	   LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        }
        return null;
    }

}
