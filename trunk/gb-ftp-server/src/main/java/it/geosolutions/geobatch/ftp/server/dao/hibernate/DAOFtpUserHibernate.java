/*
 * $Header: it.geosolutions.geobatch.ftp.server.dao.hibernate.DAOFtpUserHibernate,v. 0.1 13/ott/2009 10.02.48 created by giuseppe $
 * $Revision: 0.1 $
 * $Date: 13/ott/2009 10.02.48 $
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
package it.geosolutions.geobatch.ftp.server.dao.hibernate;

import it.geosolutions.geobatch.ftp.server.dao.DAOException;
import it.geosolutions.geobatch.ftp.server.dao.FtpUserDAO;
import it.geosolutions.geobatch.ftp.server.model.FtpUser;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author giuseppe
 * 
 */
public class DAOFtpUserHibernate extends DAOAbstractSpring<FtpUser, Long>
		implements FtpUserDAO {

	public DAOFtpUserHibernate() {
		super(FtpUser.class);
		// TODO Auto-generated constructor stub
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public FtpUser findByUserName(String userName) throws DAOException {
		List<FtpUser> users = super.findByCriteria(Restrictions.eq("userId",
				userName));
		return users.get(0);
	}

}
