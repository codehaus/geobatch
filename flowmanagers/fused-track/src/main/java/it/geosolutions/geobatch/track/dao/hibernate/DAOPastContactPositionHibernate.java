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

package it.geosolutions.geobatch.track.dao.hibernate;

import it.geosolutions.geobatch.track.dao.DAOException;
import it.geosolutions.geobatch.track.dao.PastContactPositionDAO;
import it.geosolutions.geobatch.track.model.PastContactPosition;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */

public class DAOPastContactPositionHibernate extends DAOAbstractSpring<PastContactPosition,Long>
		implements PastContactPositionDAO {

	public DAOPastContactPositionHibernate() {
		super(PastContactPosition.class);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public PastContactPosition save(PastContactPosition contact) throws DAOException {
		return super.makePersistent(contact);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(final String type) throws DAOException {

	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	@SuppressWarnings("unchecked")
	public List<PastContactPosition> findByPeriod(final long timestamp, 
			final long step, final long contactId) throws DAOException {
		
		Timestamp time = new Timestamp((timestamp-step)*1000);

		return (List<PastContactPosition>)super.getHibernateTemplate().find(
				"select sp from PastContactPosition as sp where sp.contact.contactId="
				+ contactId + " and sp.time>='" + time + "' order by sp.pastContactId asc");
	}
}
