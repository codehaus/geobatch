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

package it.geosolutions.geobatch.track.contact;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.track.configuration.FusedTrackActionConfiguration;
import it.geosolutions.geobatch.track.configuration.FusedTrackConfiguratorService;
import it.geosolutions.geobatch.track.dao.ContactDAO;
import it.geosolutions.geobatch.track.dao.ContactTypeDAO;
import it.geosolutions.geobatch.track.dao.PastContactPositionDAO;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.SessionFactory;


/**
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */

public class FusedTrackGeneratorService
		extends FusedTrackConfiguratorService<FileSystemMonitorEvent, FusedTrackActionConfiguration> {

	private final static Logger LOGGER = Logger
			.getLogger(FusedTrackGeneratorService.class.toString());

	private ContactDAO contactDAO;
	private PastContactPositionDAO pastContactPositionDAO;
	private ContactTypeDAO contactTypeDAO;

	private SessionFactory sessionFactory;
	

	public void setContactDAO(ContactDAO contactDAO) {
		this.contactDAO = contactDAO;
	}

	public ContactDAO getContactDAO() {
		return contactDAO;
	}


	public void setPastContactPositionDAO(PastContactPositionDAO pastContactPositionDAO) {
		this.pastContactPositionDAO = pastContactPositionDAO;
	}

	public PastContactPositionDAO getPastContactPositionDAO() {
		return pastContactPositionDAO;
	}
	
	public void setContactTypeDAO(ContactTypeDAO contactTypeDAO) {
		this.contactTypeDAO = contactTypeDAO;
	}

	public ContactTypeDAO getContactTypeDAO() {
		return contactTypeDAO;
	}
	
	public void setSessionFactory(SessionFactory factory){
		this.sessionFactory = factory;
	}
	
	public SessionFactory getSessionFactory(){
		return this.sessionFactory;
	}
	
	public FusedTrackGenerator createAction(FusedTrackActionConfiguration configuration) {
		try {
			return new FusedTrackGenerator(configuration, this.contactDAO, this.pastContactPositionDAO, this.sessionFactory);
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			return null;
		}
	}

	@Override
	public boolean canCreateAction(FusedTrackActionConfiguration configuration) {
		final boolean superRetVal = super.canCreateAction(configuration);
		return superRetVal;
	}

}
