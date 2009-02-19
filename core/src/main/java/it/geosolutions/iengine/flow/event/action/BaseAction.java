package it.geosolutions.iengine.flow.event.action;

import it.geosolutions.iengine.catalog.impl.BaseIdentifiable;

import java.util.EventObject;

public abstract class BaseAction<T extends EventObject> extends BaseIdentifiable implements
        Action<T> {

    public void create() {
        // TODO Auto-generated method stub

    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

}
