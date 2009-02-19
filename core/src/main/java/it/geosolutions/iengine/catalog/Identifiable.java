package it.geosolutions.iengine.catalog;

public interface Identifiable {

    /**
	 *
	 */
    public abstract String getDescription();

    /**
     * The Flow BaseEventConsumer Type identifier.
     */
    public abstract String getId();

    /**
     * The Flow BaseEventConsumer Type name.
     */
    public abstract String getName();

    /**
     * The Flow BaseEventConsumer Type identifier.
     */
    public abstract void setId(String id);

    /**
     * The Flow BaseEventConsumer Type name.
     */
    public abstract void setName(String name);

    /**
	 *
	 */
    public abstract void setDescription(String description);

}