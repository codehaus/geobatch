package it.geosolutions.iengine.flow.event.generator;

import java.util.EventListener;
import java.util.EventObject;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public interface FlowEventListener<T extends EventObject> extends EventListener {

    /**
     * Called when one of the monitored files are created, deleted or modified.
     * 
     * @param file
     *            File which has been changed.
     */
    void eventGenerated(T event);

}
