package it.geosolutions.iengine.flow.event.action;

import java.util.EventObject;
import java.util.Queue;

public interface Action<T extends EventObject> {
    public Queue<T> execute(Queue<T> events) throws Exception;

    public void create();

    public void destroy();

}
