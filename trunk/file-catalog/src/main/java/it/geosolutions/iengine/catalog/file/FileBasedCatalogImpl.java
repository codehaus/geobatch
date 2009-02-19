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
