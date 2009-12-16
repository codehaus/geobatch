/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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



package it.geosolutions.geobatch.track.configuration;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;



public class FusedTrackActionConfiguration extends ActionConfiguration implements Configuration {

    protected FusedTrackActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
	}
    
    private String workingDirectory;
    
    private long stepTimeSecond;
    
    private double cogDegreeThreshold;

    private String logType;

    public FusedTrackActionConfiguration() {
        super();
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

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }
    
	public void setStepTimeSecond(long stepTimeSecond) {
		this.stepTimeSecond = stepTimeSecond;
	}

	public long getStepTimeSecond() {
		return stepTimeSecond;
	}

	public void setCogDegreeThreshold(double cogDegreeThreshold) {
		this.cogDegreeThreshold = cogDegreeThreshold;
	}

	public double getCogDegreeThreshold() {
		return cogDegreeThreshold;
	}

    @Override
    public ActionConfiguration clone() throws CloneNotSupportedException {
		final FusedTrackActionConfiguration configuration = 
			new FusedTrackActionConfiguration(super.getId(),super.getName(),super.getDescription(),super.isDirty());

		configuration.setWorkingDirectory(workingDirectory);
		configuration.setLogType(logType);
		configuration.setCogDegreeThreshold(cogDegreeThreshold);
		configuration.setStepTimeSecond(stepTimeSecond);
		configuration.setServiceID(getServiceID());

		return configuration;
    }
}
