package it.geosolutions.iengine.flow.event.action;

import it.geosolutions.iengine.catalog.Service;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;

import java.util.EventObject;

public interface ActionService<T extends EventObject, C extends ActionConfiguration> extends
        Service {

    public Action<T> createAction(C configuration);

    public boolean canCreateAction(C configuration);
}
