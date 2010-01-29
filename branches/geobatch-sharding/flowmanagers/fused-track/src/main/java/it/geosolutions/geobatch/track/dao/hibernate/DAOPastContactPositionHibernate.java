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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */
@Repository
@Transactional
public class DAOPastContactPositionHibernate implements PastContactPositionDAO {

	private EntityManager em;
	
	@PersistenceContext(unitName = "fusedtrack") 
	public void setEntityManager(EntityManager em){
		this.em = em;
	}

//	@Transactional(propagation = Propagation.REQUIRED)
	public void save(PastContactPosition contact) throws DAOException {
		this.em.persist(contact);
	}

//	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(final PastContactPosition pastContact) throws DAOException {
		this.em.remove(pastContact);
	}
	
//	@Transactional(propagation = Propagation.REQUIRED, readOnly=true)
	@SuppressWarnings("unchecked")
	public List<PastContactPosition> findByPeriod(final long timestamp, 
			final long step, final long contactId) throws DAOException {
		
//		super.getHibernateTemplate().setCacheQueries(true);
//		super.getHibernateTemplate().setQueryCacheRegion("query.PastContactPosition");
		
		Timestamp time = new Timestamp(timestamp - (step*1000));

		Query query = this.em.createNamedQuery("findPastContactPositionByPeriod");
		query.setParameter("contactId", contactId);
		query.setParameter("timeStamp", time);
		
//		query.setCacheable(true);	
//		query.setCacheRegion("query.PastContactPosition");
		
		return (List<PastContactPosition>)query.getResultList();
	}
}
