/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.wmc;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Public class to generate WMCs 
 * 
 */
public class WMCGeneratorService extends BaseService implements
	ActionService<FileSystemMonitorEvent, WMCActionConfiguration> {
	
    private final static Logger LOGGER = Logger.getLogger(WMCGeneratorService.class.toString());

    private WMCGeneratorService() {
        super(true);
    }
    
    /**
     *  Action creator
     * 
     *  @param configuration The data base action configuration 
     *  @return new WMCFileConfigurator()
     */
    public WMCFileConfigurator createAction(WMCActionConfiguration configuration) {
        try {
            return new WMCFileConfigurator(configuration);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            return null;
        }
    }

    public boolean canCreateAction(WMCActionConfiguration configuration) {
    	return true;
    }

}