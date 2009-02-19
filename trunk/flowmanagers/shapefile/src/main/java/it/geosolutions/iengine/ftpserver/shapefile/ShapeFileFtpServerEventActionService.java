/*
 * $Header: $fileName$ $
 * $Revision: 0.1 $
 * $Date: $date$ $time.long$ $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.iengine.ftpserver.shapefile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.configuration.event.action.ftpserver.FtpServerEventActionConfiguration;
import it.geosolutions.iengine.flow.event.action.ftpserver.FtpServerEventActionService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author Ivano Picco
 * 
 * @version $ ShapeFileFtpServerEventActionService.java $ Revision: x.x $ 19/feb/07 16:16:13
 */
public class ShapeFileFtpServerEventActionService extends
        FtpServerEventActionService<FileSystemMonitorEvent, FtpServerEventActionConfiguration> {
    private final static Logger LOGGER = Logger.getLogger(ShapeFileFtpServerEventActionService.class
            .toString());

    public ShapeFileFtpServerEventAction createAction(FtpServerEventActionConfiguration configuration) {
        try {
            return new ShapeFileFtpServerEventAction(configuration);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public boolean canCreateAction(FtpServerEventActionConfiguration configuration) {
        final boolean superRetVal = super.canCreateAction(configuration);
        return superRetVal;
    }

}
