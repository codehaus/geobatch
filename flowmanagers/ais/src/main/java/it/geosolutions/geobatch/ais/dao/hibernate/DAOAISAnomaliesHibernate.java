/*
 * $Header: it.geosolutions.geobatch.ais.dao.hibernate.DAOAISAnomaliesHibernate,v. 0.1 06/ott/2009 12.38.28 created by Francesco $
 * $Revision: 0.1 $
 * $Date: 06/ott/2009 12.38.28 $
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
package it.geosolutions.geobatch.ais.dao.hibernate;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.geosolutions.geobatch.ais.dao.DAOException;
import it.geosolutions.geobatch.ais.dao.IDAOAISAnomalies;
import it.geosolutions.geobatch.ais.model.AISAnomalies;

/**
 * @author Francesco
 *
 */
public class DAOAISAnomaliesHibernate extends DAOAbstractSpring<AISAnomalies> implements
		IDAOAISAnomalies {

	public DAOAISAnomaliesHibernate() {
		super(AISAnomalies.class);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public AISAnomalies makePersistent(AISAnomalies ais) throws DAOException{
		return super.makePersistent(ais);
	}

	

}
