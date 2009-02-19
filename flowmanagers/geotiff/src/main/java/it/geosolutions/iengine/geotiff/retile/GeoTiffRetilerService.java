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
package it.geosolutions.iengine.geotiff.retile;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.flow.event.action.ActionService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ ShapeFileDTOProducerSPI.java $ Revision: x.x $ 19/feb/07 16:16:13
 */
public class GeoTiffRetilerService extends BaseService implements
        ActionService<FileSystemMonitorEvent, GeoTiffRetilerConfiguration> {

    private GeoTiffRetilerService() {
        super(true);
    }

    private final static Logger LOGGER = Logger
            .getLogger(GeoTiffRetiler.class.toString());

    public boolean canCreateAction(GeoTiffRetilerConfiguration configuration) {
        // XXX
        return true;
    }

    public GeoTiffRetiler createAction(GeoTiffRetilerConfiguration configuration) {
        try {
            return new GeoTiffRetiler(configuration);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            return null;
        }
    }

}
