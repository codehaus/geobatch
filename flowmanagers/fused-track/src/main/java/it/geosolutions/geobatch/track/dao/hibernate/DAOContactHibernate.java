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

import it.geosolutions.geobatch.track.dao.ContactDAO;
import it.geosolutions.geobatch.track.dao.DAOException;
import it.geosolutions.geobatch.track.model.Contact;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */

public class DAOContactHibernate extends DAOAbstractSpring<Contact,Long>
		implements ContactDAO {

	public DAOContactHibernate() {
		super(Contact.class);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public Contact save(Contact contact) throws DAOException {
		return super.makePersistent(contact);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(final Contact contact) throws DAOException {
		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void merge(final Contact contact) throws DAOException {
		super.getHibernateTemplate().merge(contact);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Contact isExist(final long id) throws DAOException {
		return (Contact)super.getHibernateTemplate().get(Contact.class, id);
	}
}
