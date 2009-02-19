/*
 * $Header: $fileName$ $
 * $Revision: 0.1 $
 * $Date: $date$ $time.long$ $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.iengine.flow.event.consumer;

import it.geosolutions.iengine.catalog.Catalog;
import it.geosolutions.iengine.catalog.impl.BaseResource;
import it.geosolutions.iengine.configuration.event.consumer.EventConsumerConfiguration;
import it.geosolutions.iengine.flow.event.action.Action;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public abstract class BaseEventConsumer<T extends EventObject, C extends EventConsumerConfiguration>
        extends BaseResource implements Runnable, EventConsumer<T, C> {

    /**
     */
    private volatile EventConsumerStatus eventConsumerStatus;

    /**
     * The MailBox
     */
    protected final Queue<T> eventsQueue = new LinkedList<T>();

    protected final List<Action<T>> actions = new ArrayList<Action<T>>();

    // ----------------------------------------------- PRIVATE ATTRIBUTES
    /**
     * Private Logger
     */
    private static Logger LOGGER = Logger.getLogger(BaseEventConsumer.class.toString());

    public BaseEventConsumer() {
        super();
        this.setStatus(EventConsumerStatus.IDLE);
    }

    public BaseEventConsumer(String id, String name, String description, Catalog catalog) {
        super(id, name, description, catalog);

        this.setStatus(EventConsumerStatus.IDLE);
    }

    public BaseEventConsumer(String id, String name, String description) {
        super(id, name, description);

    }

    /**
     * Default Constructor
     * 
     * @param catalog
     */
    public BaseEventConsumer(Catalog catalog) {
        super(catalog);
        this.setStatus(EventConsumerStatus.IDLE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.flow.event.consumer.EventConsumer#getStatus()
     */
    public EventConsumerStatus getStatus() {
        return this.eventConsumerStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.flow.event.consumer.EventConsumer#setStatus(it
     * .geosolutions.iengine .flow.event.consumer.Status)
     */
    protected void setStatus(EventConsumerStatus eventConsumerStatus) {

        // //
        // now changing eventConsumerStatus
        // //
        this.eventConsumerStatus = eventConsumerStatus;

    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.flow.event.consumer.EventConsumer#put(it.geosolutions
     * .filesystemmonitor .monitor.FileSystemMonitorEvent)
     */
    public boolean consume(T event) {
        if (!eventsQueue.offer(event))
            return false;

        return true;
    }

    /**
     * Once the configuring state has been successfully passed, by collecting all the necessary
     * Events, the BaseEventConsumer invokes this method in order to produce the DTOs. DTOs
     * represent the Java beans used by the Catalog BaseEventConsumer to configure GeoServer.
     * 
     * @param event
     * @return
     * 
     * @throws InterruptedException
     * @throws InitializationException
     */
    protected boolean applyActions(Queue<T> events) {
        // apply all the actions
        for (Action<T> action : this.actions) {
            try {
                events = action.execute(events);
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
                events.clear();
            }
            if (events == null || events.isEmpty())
                return false;
        }
        return true;
    }

    protected void addActions(final List<Action<T>> actions) {
        this.actions.addAll(actions);
    }

    public void dispose() {
        eventsQueue.clear();
        actions.clear();
    }
}
