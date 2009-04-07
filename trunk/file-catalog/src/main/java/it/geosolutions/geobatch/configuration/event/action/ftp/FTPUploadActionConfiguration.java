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



package it.geosolutions.geobatch.configuration.event.action.ftp;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

/**
 *
 * @author Ivano Picco
 */
public class FTPUploadActionConfiguration extends ActionConfiguration implements Configuration {
	protected FTPUploadActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
	}

	public enum FTPConnectMode{
		ACTIVE,PASSIVE;
	}

    public static final String DEFAULT_PORT = "21";
    
    public static final int defaultTimeout=5000;//5 seconds default timeout

	private String ftpserverHost;

    private String ftpserverPWD;

    private String ftpserverUSR;

    private int ftpserverPort;

    private String workingDirectory;

    private String dataTransferMethod;
    
    private int timeout;
    
    private boolean zipInput;
    
    private String zipFileName;
    
//    private WriteMode writeMode;
    
	private FTPConnectMode connectMode;
    
    public FTPConnectMode getConnectMode() {
		return connectMode;
	}

	public void setConnectMode(FTPConnectMode connectMode) {
		this.connectMode = connectMode;
	}

//    
//    public WriteMode getWriteMode() {
//		return writeMode;
//	}
//
//	public void setWriteMode(WriteMode writeMode) {
//		this.writeMode = writeMode;
//	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public boolean isZipInput() {
		return zipInput;
	}

	public void setZipInput(boolean zipInput) {
		this.zipInput = zipInput;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public FTPUploadActionConfiguration() {
        super();
    }

    public String getFtpserverHost() {
        return ftpserverHost;
    }

    public void setFtpserverHost(String ftpserverHost) {
        this.ftpserverHost = ftpserverHost;
    }

    public String getFtpserverUSR() {
        return ftpserverUSR;
    }

    public void setFtpserverUSR(String ftpserverUSR) {
        this.ftpserverUSR = ftpserverUSR;
    }

    public String getFtpserverPWD() {
        return ftpserverPWD;
    }

    public void setFtpserverPWD(String ftpserverPWD) {
        this.ftpserverPWD = ftpserverPWD;
    }

    public int getFtpserverPort() {
        return ftpserverPort;
    }

    public void setFtpserverPort(int ftpserverPort) {
        this.ftpserverPort = ftpserverPort;

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

    public String getDataTransferMethod() {
        return dataTransferMethod;
    }

    public void setDataTransferMethod(String dataTransferMethod) {
        this.dataTransferMethod = dataTransferMethod;
    }

	@Override
	public Object clone() throws CloneNotSupportedException {
		final FTPUploadActionConfiguration configuration= 
			new FTPUploadActionConfiguration(getId(),getName(),getDescription(),isDirty());
		configuration.setConnectMode(connectMode);
		configuration.setDataTransferMethod(dataTransferMethod);
		configuration.setFtpserverHost(ftpserverHost);
		configuration.setFtpserverPort(ftpserverPort);
		configuration.setFtpserverPWD(ftpserverPWD);
		configuration.setFtpserverUSR(ftpserverUSR);
		configuration.setServiceID(getServiceID());
		configuration.setTimeout(timeout);
		configuration.setWorkingDirectory(workingDirectory);
		configuration.setZipFileName(zipFileName);
		configuration.setZipInput(zipInput);		
		return configuration;
	}

}
