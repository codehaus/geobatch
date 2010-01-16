/*
 * $Header: it.geosolutions.geobatch.shp2pg.configuration.Shp2PgActionConfiguration,v. 0.1 15/gen/2010 09.10.07 created by frank $
 * $Revision: 0.1 $
 * $Date: 15/gen/2010 09.10.07 $
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
package it.geosolutions.geobatch.shp2pg.configuration;

import java.lang.reflect.InvocationTargetException;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import org.apache.commons.beanutils.BeanUtils;

/**
 * @author frank
 * 
 */
public class Shp2PgActionConfiguration extends ActionConfiguration implements Configuration {

    private String workingDirectory;

    private String dbPWD;

    private String dbUID;

    private String dbServerIp;

    private String dbPort;

    private String dbName;

    private String storeFilePrefix;

    private String configId;

    private String dbType;


    /**
	 * 
	 */
    public Shp2PgActionConfiguration() {
        super();
    }

    /**
     * @param id
     * @param name
     * @param description
     * @param dirty
     */
    public Shp2PgActionConfiguration(String id, String name, String description, boolean dirty) {
        super(id, name, description, dirty);
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * @return the dbPWD
     */
    public String getDbPWD() {
        return dbPWD;
    }

    /**
     * @param dbPWD
     *            the dbPWD to set
     */
    public void setDbPWD(String dbPWD) {
        this.dbPWD = dbPWD;
    }

    /**
     * @return the dbUID
     */
    public String getDbUID() {
        return dbUID;
    }

    /**
     * @param dbUID
     *            the dbUID to set
     */
    public void setDbUID(String dbUID) {
        this.dbUID = dbUID;
    }

    /**
     * @return the dbServerIp
     */
    public String getDbServerIp() {
        return dbServerIp;
    }

    /**
     * @param dbServerIp
     *            the dbServerIp to set
     */
    public void setDbServerIp(String dbServerIp) {
        this.dbServerIp = dbServerIp;
    }

    /**
     * @return the dbPort
     */
    public String getDbPort() {
        return dbPort;
    }

    /**
     * @param dbPort
     *            the dbPort to set
     */
    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    /**
     * @return the dbName
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param dbName
     *            the dbName to set
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * @return the storeFilePrefix
     */
    public String getStoreFilePrefix() {
        return storeFilePrefix;
    }

    /**
     * @param storeFilePrefix
     *            the storeFilePrefix to set
     */
    public void setStoreFilePrefix(String storeFilePrefix) {
        this.storeFilePrefix = storeFilePrefix;
    }

    /**
     * @return the configId
     */
    public String getConfigId() {
        return configId;
    }

    /**
     * @param configId
     *            the configId to set
     */
    public void setConfigId(String configId) {
        this.configId = configId;
    }

    /**
     * @return the dbType
     */
    public String getDbType() {
        return dbType;
    }

    /**
     * @param dbType
     *            the dbType to set
     */
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.configuration.event.action.ActionConfiguration #clone()
     */
    @Override
    public ActionConfiguration clone() throws CloneNotSupportedException {

        try {
            return (Shp2PgActionConfiguration) BeanUtils.cloneBean(this);
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
