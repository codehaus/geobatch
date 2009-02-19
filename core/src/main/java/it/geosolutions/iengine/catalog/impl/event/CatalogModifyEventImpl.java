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
/**
 *
 */
package it.geosolutions.iengine.catalog.impl.event;

import it.geosolutions.iengine.catalog.event.CatalogModifyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public class CatalogModifyEventImpl<T> extends CatalogEventImpl<T> implements CatalogModifyEvent<T> {
    public CatalogModifyEventImpl(final T source) {
        super(source);
    }

    private List<String> propertyNames = new ArrayList<String>();

    private List<T> oldValues = new ArrayList<T>();

    private List<T> newValues = new ArrayList<T>();

    public void setPropertyNames(final List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    public void setNewValues(final List<T> newValues) {
        this.newValues = newValues;
    }

    public void setOldValues(final List<T> oldValues) {
        this.oldValues = oldValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.event.CatalogModifyEvent#getNewValues()
     */
    public List<T> getNewValues() {
        return newValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.event.CatalogModifyEvent#getOldValues()
     */
    public List<T> getOldValues() {
        return oldValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.event.CatalogModifyEvent#getPropertyNames()
     */
    public List<String> getPropertyNames() {
        return propertyNames;
    }
}
