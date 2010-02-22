/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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

package it.geosolutions.geobatch.ftp.client;

import java.util.logging.Level;

import it.geosolutions.geobatch.ftp.client.configuration.FTPDeleteActionConfiguration;
import it.geosolutions.geobatch.ftp.client.configuration.FTPDownloadActionConfiguration;
import it.geosolutions.geobatch.ftp.client.configuration.FTPUploadActionConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 * Register XStream aliases for the relevant services we ship in this class.
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 */
public class FTPAliasRegistrar extends AliasRegistrar {

     public FTPAliasRegistrar(AliasRegistry registry) {    	 
         if (LOGGER.isLoggable(Level.INFO))
        	 LOGGER.info(getClass().getSimpleName() + ": registering alias.");
         
         registry.putAlias("FTPUploadActionConfiguration", FTPUploadActionConfiguration.class);
         registry.putAlias("FTPDownloadActionConfiguration", FTPDownloadActionConfiguration.class);
         registry.putAlias("FTPDeleteActionConfiguration", FTPDeleteActionConfiguration.class);
     }
 }
