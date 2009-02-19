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



package it.geosolutions.iengine.catalog.file;

import it.geosolutions.iengine.catalog.impl.BaseCatalog;

/**
 * <p>
 * A Catalog based on an xml marshalled file.
 * </p>
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
@SuppressWarnings("unchecked")
public class FileBasedCatalogImpl extends BaseCatalog implements FileBaseCatalog {

    /**
     * Default Constructor.
     */
    private FileBasedCatalogImpl() {
        super();
    }

    /**
     * 
     * @param baseDirectory
     */
    private FileBasedCatalogImpl(String baseDirectory) {
        super();
        this.baseDirectory = baseDirectory;
    }

    /**
     * baseDirectory: represents the base directory where the xml files are located. The
     * workingDirecotry will be relative to this base directory unless an absolute path has been
     * specified.
     */
    private String baseDirectory;

    /**
     * Getter for the baseDirectory.
     */
    public String getBaseDirectory() {
        return this.baseDirectory;
    }

    /**
     * Setter for the baseDirectory.
     */
    public void setBaseDirectory(final String baseDirectory) {
        this.baseDirectory = baseDirectory;

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + baseDirectory + "]";
    }

}
