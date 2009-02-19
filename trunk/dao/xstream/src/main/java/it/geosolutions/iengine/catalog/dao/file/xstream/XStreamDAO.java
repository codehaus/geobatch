package it.geosolutions.iengine.catalog.dao.file.xstream;

import com.thoughtworks.xstream.XStream;
import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.dao.DAO;
import it.geosolutions.iengine.catalog.dao.file.BaseFileBaseDAO;

import it.geosolutions.iengine.xstream.Alias;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public abstract class XStreamDAO<T extends Configuration> extends BaseFileBaseDAO<T> implements DAO<T, String> {

    public XStreamDAO(String directory) {
        super(directory);
    }

    public T persist(T entity) {
        try {
            XStream xstream = new XStream();
            Alias.setAliases(xstream);

            xstream.toXML(entity, new BufferedOutputStream(new FileOutputStream(new File(
                    getBaseDirectory(), entity.getId() + ".xml"))));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return entity;
    }

    public T refresh(T entity) {
        return find(entity.getId(), false);
    }

    public boolean remove(T entity) {

        // XXX use file cleaner
        final File entityfile = new File(getBaseDirectory(), entity.getId() + ".xml");
        if (entityfile.exists() && entityfile.delete())
            return true;
        return false;

    }

}
