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
package it.geosolutions.iengine.catalog.event;

/**
 * @author Alessio
 * 
 */
public interface CatalogListener {
    /**
     * Handles the event of an addition to the catalog.
     */
    <T, C extends CatalogAddEvent<T>> void handleAddEvent(C event);

    /**
     * Handles the event of a removal from the catalog.
     * 
     */
    <T, C extends CatalogRemoveEvent<T>> void handleRemoveEvent(C event);

    /**
     * Handles the event of a modification to an object in the catalog.
     */
    <T, C extends CatalogModifyEvent<T>> void handleModifyEvent(C event);
}
