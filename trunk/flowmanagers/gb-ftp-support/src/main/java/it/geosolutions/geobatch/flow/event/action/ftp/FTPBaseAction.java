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



package it.geosolutions.geobatch.flow.event.action.ftp;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.configuration.event.action.ftp.FTPUploadActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.IOException;
import java.util.EventObject;
import java.util.logging.Logger;

/**
 * Comments here ...
 * 
 * @author Ivano Picco
 * 
 * @version $ GeoServerConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06
 */
public abstract class FTPBaseAction<T extends EventObject> extends BaseAction<T>
        implements Action<T> {
    /**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(FTPBaseAction.class.toString());

    protected final FTPUploadActionConfiguration configuration;

    protected final String ftpserverHost;

    protected final String ftpserverPWD;

    protected final String ftpserverUSR;

    protected final int ftpserverPort;

    protected final String dataTransferMethod;

    /**
     * Constructs a producer. The operation name will be the same than the parameter descriptor
     * name.
     * 
     * @param descriptor
     *            The parameters descriptor.
     * @throws IOException
     */
    public FTPBaseAction(FTPUploadActionConfiguration configuration)
            throws IOException {
        this.configuration = configuration;
        // //
        //
        // get required parameters
        //
        // //

        // ftpserver host
        ftpserverHost = configuration.getFtpserverHost();
        
        // ftpserver user
        ftpserverUSR = configuration.getFtpserverUSR();

        // ftpserver password
        ftpserverPWD = configuration.getFtpserverPWD();

        ftpserverPort = configuration.getFtpserverPort();

        dataTransferMethod = configuration.getDataTransferMethod();

    }

    /**
     * @param queryParams
     * @return
     */
//    public static String getQueryString(Map<String, String> queryParams) {
//        String queryString = "";
//
//        if (queryParams != null)
//            for (String key : queryParams.keySet()) {
//                queryString += (queryString.length() == 0 ? "" : "&") + key + "="
//                        + queryParams.get(key);
//            }
//
//        return queryString;
//    }

    public ActionConfiguration getConfiguration() {
        return configuration;
    }

}
