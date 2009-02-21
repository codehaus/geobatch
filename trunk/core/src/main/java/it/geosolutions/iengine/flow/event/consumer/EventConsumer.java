/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



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