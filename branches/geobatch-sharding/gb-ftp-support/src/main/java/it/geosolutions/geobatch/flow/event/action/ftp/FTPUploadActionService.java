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

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.configuration.event.action.ftp.FTPUploadActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.io.IOException;

/**
 * Comments here ...
 * 
 * @author Ivano Picco
 * 
 * @version $ GeoServerConfiguratorService.java $ Revision: 0.1 $ 12/feb/07 12:07:32
 */
public class FTPUploadActionService
        extends BaseService implements ActionService<FileSystemMonitorEvent, FTPUploadActionConfiguration> {

    public FTPUploadActionService() {
        super(true);
    }

    public boolean canCreateAction(FTPUploadActionConfiguration configuration) {
        // XXX ImPLEMENT ME
        return true;
    }

	public FTPUploadAction createAction(FTPUploadActionConfiguration configuration) {
		// TODO Auto-generated method stub
		try {
			return new FTPUploadAction(configuration);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}