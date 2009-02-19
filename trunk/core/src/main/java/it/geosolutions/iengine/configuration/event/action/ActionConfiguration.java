package it.geosolutions.iengine.configuration.event.action;

import it.geosolutions.iengine.catalog.Configuration;
import it.geosolutions.iengine.catalog.impl.BaseConfiguration;

public class ActionConfiguration extends BaseConfiguration implements Configuration {
    
    public ActionConfiguration() {
        super();
    }

    public ActionConfiguration(String id, String name, String description, boolean dirty) {
        super(id, name, description, dirty);
    }

}
