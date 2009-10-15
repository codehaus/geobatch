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

package it.geosolutions.geobatch.ui.mvc.data;

/**
 * @author Francesco Izzi
 * 
 */
public class FtpUserDataBean {

	private String userId;

	private String userPassword;

	private String writePermission;

	private String uploadRate;

	private String downloadRate;

	/**
	 * @return the userId
	 */
	public synchronized String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public synchronized void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the userPassword
	 */
	public synchronized String getUserPassword() {
		return userPassword;
	}

	/**
	 * @param userPassword
	 *            the userPassword to set
	 */
	public synchronized void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	/**
	 * @return the writePermission
	 */
	public synchronized String getWritePermission() {
		return writePermission;
	}

	/**
	 * @param writePermission
	 *            the writePermission to set
	 */
	public synchronized void setWritePermission(String writePermission) {
		this.writePermission = writePermission;
	}

	/**
	 * @return the uploadRate
	 */
	public synchronized String getUploadRate() {
		return uploadRate;
	}

	/**
	 * @param uploadRate
	 *            the uploadRate to set
	 */
	public synchronized void setUploadRate(String uploadRate) {
		this.uploadRate = uploadRate;
	}

	/**
	 * @return the downloadRate
	 */
	public synchronized String getDownloadRate() {
		return downloadRate;
	}

	/**
	 * @param downloadRate
	 *            the downloadRate to set
	 */
	public synchronized void setDownloadRate(String downloadRate) {
		this.downloadRate = downloadRate;
	}

}
