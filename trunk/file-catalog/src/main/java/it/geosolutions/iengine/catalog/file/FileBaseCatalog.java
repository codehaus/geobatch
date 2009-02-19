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