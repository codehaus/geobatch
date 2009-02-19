package it.geosolutions.iengine.flow.event.generator;

import java.util.EventObject;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * 
 */
public interface EventGenerator<T extends EventObject> {

    public void addListener(FlowEventListener<T> fileListener);

    public void removeListener(FlowEventListener<T> fileListener);

}
