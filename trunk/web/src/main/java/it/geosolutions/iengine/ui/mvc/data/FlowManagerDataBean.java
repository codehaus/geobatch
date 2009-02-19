/*
 * $Header: $fileName$ $
 * $Revision: 0.1 $
 * $Date: $date$ $time.long$ $
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
/**
 *
 */
package it.geosolutions.iengine.ui.mvc.data;

/**
 * @author Alessio Fabiani
 * 
 */
public class FlowManagerDataBean {
    private String descriptorId;

    private String id;

    private String name;

    private String inputDir;

    private String outputDir;

    // private List<FileBasedCatalogConfiguration> availableDescriptors;

    /**
     * @return the descriptorId
     */
    public synchronized String getDescriptorId() {
        return descriptorId;
    }

    /**
     * @param descriptorId
     *            the descriptorId to set
     */
    public synchronized void setDescriptorId(String descriptorId) {
        this.descriptorId = descriptorId;
    }

    /**
     * @return the id
     */
    public synchronized String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public synchronized void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public synchronized String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public synchronized void setName(String name) {
        this.name = name;
    }

    /**
     * @return the inputDir
     */
    public synchronized String getInputDir() {
        return inputDir;
    }

    /**
     * @param inputDir
     *            the inputDir to set
     */
    public synchronized void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    // /**
    // * @return the availableDescriptors
    // */
    // public synchronized List<FileBasedCatalogConfiguration> getAvailableDescriptors() {
    // return availableDescriptors;
    // }
    //
    // /**
    // * @param availableDescriptors the availableDescriptors to set
    // */
    // public synchronized void setAvailableDescriptors(
    // List<FileBasedCatalogConfiguration> availableDescriptors) {
    // this.availableDescriptors = availableDescriptors;
    // }

    /**
     * @return the workingDirectory
     */
    public synchronized String getOutputDir() {
        return outputDir;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public synchronized void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
