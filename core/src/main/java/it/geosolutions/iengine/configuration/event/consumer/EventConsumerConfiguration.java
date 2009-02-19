package it.geosolutions.iengine.configuration.event.consumer;

import java.util.List;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;

public interface EventConsumerConfiguration extends Configuration {

    public List<? extends ActionConfiguration> getActions();

    public void setActions(List<? extends ActionConfiguration> actions);
}
