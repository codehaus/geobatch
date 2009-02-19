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



package it.geosolutions.iengine.catalog.event;

import java.util.List;

/**
 * @author Alessio
 * 
 */
public interface CatalogModifyEvent<T> extends CatalogEvent<T> {
    /**
     * The names of the properties that were modified.
     */
    List<String> getPropertyNames();

    /**
     * The old values of the properties that were modified.
     */
    List<T> getOldValues();

    /**
     * The new values of the properties that were modified.
     */
    List<T> getNewValues();
}
