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
package it.geosolutions.iengine.flow.event.action.geoserver;

import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;
import it.geosolutions.iengine.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.iengine.flow.event.action.Action;
import it.geosolutions.iengine.flow.event.action.BaseAction;

import java.io.IOException;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ GeoServerConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06
 */
public abstract class GeoServerConfiguratorAction<T extends EventObject> extends BaseAction<T>
        implements Action<T> {
    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(GeoServerConfiguratorAction.class.toString());

    protected final GeoServerActionConfiguration configuration;

    protected final String geoserverURL;

    protected final String geoserverUID;

    protected final String geoserverPWD;

    protected String storeFilePrefix;

    protected final List<String> styles;

    protected final String envelope;

    protected final String crs;

    protected final String datatype;

    protected final String defaultNamespace;

    protected final String defaultNamespaceUri;

    protected final String defaultStyle;

    protected final String wmsPath;

    protected final String dataTransferMethod;

    /**
     * Constructs a producer. The operation name will be the same than the parameter descriptor
     * name.
     * 
     * @param descriptor
     *            The parameters descriptor.
     * @throws IOException
     */
    public GeoServerConfiguratorAction(GeoServerActionConfiguration configuration)
            throws IOException {
        this.configuration = configuration;
        // //
        //
        // get required parameters
        //
        // //

        // geoserver URL
        geoserverURL = configuration.getGeoserverURL();

        // geoserver UID
        geoserverUID = configuration.getGeoserverUID();

        // geoserver PWD
        geoserverPWD = configuration.getGeoserverPWD();

        // DataFilePrefix
        storeFilePrefix = configuration.getStoreFilePrefix();

        // styles
        styles = configuration.getStyles();

        // //
        //
        // get optional parameters
        //
        // //

        // geoserver UID
        envelope = configuration.getEnvelope();

        crs = configuration.getCrs();

        datatype = configuration.getDatatype();

        defaultNamespace = configuration.getDefaultNamespace();

        defaultNamespaceUri = configuration.getDefaultNamespaceUri();

        defaultStyle = configuration.getDefaultStyle();

        wmsPath = configuration.getWmsPath();

        dataTransferMethod = configuration.getDataTransferMethod();

    }

    /**
     * @param queryParams
     * @return
     */
    public static String getQueryString(Map<String, String> queryParams) {
        String queryString = "";

        if (queryParams != null)
            for (String key : queryParams.keySet()) {
                queryString += (queryString.length() == 0 ? "" : "&") + key + "="
                        + queryParams.get(key);
            }

        return queryString;
    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }

}
