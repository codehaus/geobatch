package it.geosolutions.iengine.catalog.dao.file.jibx;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.dao.DAO;
import it.geosolutions.iengine.catalog.dao.file.BaseFileBaseDAO;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

public abstract class JIBXDAO<T extends Configuration> 
		extends BaseFileBaseDAO<T>
		implements DAO<T, String> {

    public JIBXDAO(String directory) {
        super(directory);
    }

    public T persist(T entity) {
        IBindingFactory bfact;
        try {
            bfact = BindingDirectory.getFactory(entity.getClass());
            final IMarshallingContext mctx = bfact.createMarshallingContext();
            mctx.marshalDocument(entity, "UTF-8", null, new BufferedOutputStream(
                    new FileOutputStream(new File(getBaseDirectory(), entity.getId() + ".xml"))));
        } catch (JiBXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
