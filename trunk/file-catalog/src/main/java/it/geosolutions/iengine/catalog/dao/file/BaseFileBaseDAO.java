package it.geosolutions.iengine.catalog.dao.file;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.dao.DAO;

public abstract class BaseFileBaseDAO<T extends Configuration> implements DAO<T, String> {

    public BaseFileBaseDAO(String directory) {
        this.directory = directory;
    }

    private String directory;

    public String getBaseDirectory() {
        return directory;
    }

    public void setBaseDirectory(String dir) {
        this.directory = dir;

    }

}
