package it.geosolutions.iengine.flow.event.consumer;

import it.geosolutions.iengine.configuration.event.consumer.EventConsumerConfiguration;

import java.util.EventObject;

public interface EventConsumer<T extends EventObject, C extends EventConsumerConfiguration> {
    /**
     * Clean up code for this {@link EventConsumer}.
     */
    public void dispose();

    /**
     * Retrieves the configuration for this {@link EventConsumer}.
     * 
     * @return the configuration for this {@link EventConsumer}.
     */
    public C getConfiguration();

    /**
     * Sets the configuration for this {@link EventConsumer}.
     * 
     * @param configuration
     *            to set for this {@link EventConsumer}.
     */
    public void setConfiguration(C configuration);

    /**
     * Retrieves the status for this {@link EventConsumer}.
     * 
     * @return the status for this {@link EventConsumer}.
     */
    public EventConsumerStatus getStatus();

    /**
     * Tries to consume the provided event. In case the provided event cannot be consumed it return
     * false.
     * 
     * @param event
     *            The event to consume
     * @return <code>true</code> if we can consume the provided event, <code>false</code> otherwise.
     */
    public boolean consume(T event);

    /**
     * Asks this {@link EventConsumer} to cancel its execution.
     * 
     */
    public void cancel();

    /**
     * Tells us whether or not this {@link EventConsumer} was asked to cancel its execution.
     * 
     * @return <code>true</code> in case this {@link EventConsumer} was asked to cancel its
     *         execution, <code>false</code> otherwise.
     */
    public boolean isCanceled();

}