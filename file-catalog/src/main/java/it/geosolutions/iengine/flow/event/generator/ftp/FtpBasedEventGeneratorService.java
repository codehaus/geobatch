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

package it.geosolutions.iengine.flow.event.generator.ftp;

import it.geosolutions.factory.NotSupportedException;
import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.iengine.catalog.file.FileBaseCatalog;
import it.geosolutions.iengine.configuration.event.generator.ftp.FtpBasedEventGeneratorConfiguration;
import it.geosolutions.iengine.flow.event.generator.BaseEventGeneratorService;
import it.geosolutions.iengine.global.CatalogHolder;
import it.geosolutions.iengine.io.utils.IOUtils;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ivano Picco
 * 
 */
public class FtpBasedEventGeneratorService
        extends BaseEventGeneratorService<FileSystemMonitorEvent,FtpBasedEventGeneratorConfiguration> {

    private final static Logger LOGGER = Logger.getLogger(FtpBasedEventGeneratorService.class
            .toString());


    /*
     * (non-Javadoc)
     * 
     * @see
     * it.geosolutions.iengine.flow.event.generator.EventGeneratorService#canCreateEventGenerator
     * (java.util.Map)
     */
    public boolean canCreateEventGenerator(FtpBasedEventGeneratorConfiguration configuration) {
        final OsType osType = configuration.getOsType();
        if (osType == null)
            return false;
        final File sensedDir;
        try {
            sensedDir = IOUtils.findLocation(configuration.getWorkingDirectory(), new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
            if (sensedDir != null) {
                if (sensedDir.exists() && sensedDir.isDirectory() && sensedDir.canRead()) // TODO message
                 return true;
            }
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.geosolutions.iengine.flow.event.generator.EventGeneratorService#createEventGenerator(java
     * .util.Map)
     */
    public FtpBasedEventGenerator createEventGenerator(
            FtpBasedEventGeneratorConfiguration configuration) {
        String errorMsg="";

        try {
            final OsType osType = configuration.getOsType();
            final FileSystemMonitorNotifications eventType = configuration.getEventType();
            final File sensedDir;
            sensedDir = IOUtils.findLocation(configuration.getWorkingDirectory(), new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
            if (sensedDir != null) {
                if (!sensedDir.exists() || !sensedDir.isDirectory() || !sensedDir.canRead()) {
                    errorMsg = "Error on working directory: ".concat(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory())+ "/".concat(configuration.getWorkingDirectory()) ;
                    throw new Exception(errorMsg);
                }
            } else {
                    errorMsg = "Error on working directory: ".concat(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory())+ "/".concat(configuration.getWorkingDirectory()) ;
                    throw new Exception(errorMsg);
            }
            if (configuration.getWildCard() == null)
                return new FtpBasedEventGenerator(osType, eventType, sensedDir);
            else
                return new FtpBasedEventGenerator(osType, eventType, sensedDir,
                        configuration.getWildCard(), configuration.getFtpserverUSR(),
                        configuration.getFtpserverPWD(),configuration.getFtpserverPort());
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (NotSupportedException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return null;
    }

}