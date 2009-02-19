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

package it.geosolutions.iengine.flow.event.generator;

import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.configuration.event.generator.EventGeneratorConfiguration;

import java.util.EventObject;

/**
 * Comments here ...
 *
 * @author Ivano Picco
 *
 * @version $ GeoServerConfiguratorService.java $ Revision: 0.1 $ 12/feb/07 12:07:32
 */
public abstract class BaseEventGeneratorService<T extends EventObject, C extends EventGeneratorConfiguration>
        extends BaseService implements EventGeneratorService<T, C> {

}