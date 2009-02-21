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



package it.geosolutions.iengine.catalog.file;

import it.geosolutions.iengine.catalog.Catalog;

/**
 * <p>
 * A Catalog based on an xml marshalled file.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public interface FileBaseCatalog extends Catalog {

    /**
     * Getter for the base directory.
     * 
     * @return baseDirectory
     */
    public String getBaseDirectory();

    /**
     * Setter for the base directory.
     * 
     * @param baseDirectory
     */
    public void setBaseDirectory(String baseDirectory);

}