/*
 * $Header: it.geosolutions.reload.configuration.ReloadActionConfiguration,v. 0.1 19/gen/2010 12.20.37 created by frank $
 * $Revision: 0.1 $
 * $Date: 19/gen/2010 12.20.37 $
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
package it.geosolutions.geobatch.reload.configuration;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

/**
 * @author frank
 * 
 */
public class ReloadActionConfiguration extends ActionConfiguration implements
		Configuration {

	private List<Server> servers = new ArrayList<Server>();

	public ReloadActionConfiguration() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 * @param description
	 * @param dirty
	 */
	public ReloadActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
	}

	/**
	 * @return the servers
	 */
	public List<Server> getServers() {
		return servers;
	}

	/**
	 * @param servers
	 *            the servers to set
	 */
	public void setServers(List<Server> servers) {
		this.servers = servers;
	}

	@Override
	public ActionConfiguration clone() throws CloneNotSupportedException {
		try {
			return (ReloadActionConfiguration) BeanUtils.cloneBean(this);
		} catch (IllegalAccessException e) {
			final CloneNotSupportedException cns = new CloneNotSupportedException();
			cns.initCause(e);
			throw cns;
		} catch (InstantiationException e) {
			final CloneNotSupportedException cns = new CloneNotSupportedException();
			cns.initCause(e);
			throw cns;
		} catch (InvocationTargetException e) {
			final CloneNotSupportedException cns = new CloneNotSupportedException();
			cns.initCause(e);
			throw cns;
		} catch (NoSuchMethodException e) {
			final CloneNotSupportedException cns = new CloneNotSupportedException();
			cns.initCause(e);
			throw cns;
		}
	}

}
