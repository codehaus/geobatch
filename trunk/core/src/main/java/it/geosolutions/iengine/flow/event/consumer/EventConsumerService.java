package it.geosolutions.iengine.flow.event.consumer;

import it.geosolutions.iengine.configuration.event.consumer.EventConsumerConfiguration;

import java.util.EventObject;

public interface EventConsumerService<T extends EventObject, E extends EventConsumerConfiguration> {
    public EventConsumer<T, E> createEventConsumer(E configuration);

    public boolean canCreateEventConsumer(E configuration);
}
