package it.geosolutions.iengine.catalog.impl;

import it.geosolutions.iengine.catalog.Identifiable;

public abstract class BaseIdentifiable implements Identifiable {

    private String id;

    private String name;

    private String description = "Flow BaseEventConsumer for the ingestion of AIS-Anomalies Files.";

    public BaseIdentifiable() {
    }

    public BaseIdentifiable(String id, String name, String description) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}