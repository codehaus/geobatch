package it.geosolutions.iengine.flow.event.generator;

import it.geosolutions.iengine.catalog.Service;
import it.geosolutions.iengine.configuration.event.generator.EventGeneratorConfiguration;

import java.util.EventObject;

public interface EventGeneratorService<T extends EventObject, C extends EventGeneratorConfiguration>
        extends Service {
    public EventGenerator<T> createEventGenerator(final C configuration);

    public boolean canCreateEventGenerator(final C configuration);
}
