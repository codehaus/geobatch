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
/**
 *
 */
package it.geosolutions.geobatch.ui.mvc;

import java.util.List;

import it.geosolutions.geobatch.ftp.server.GeoBatchServer;
import it.geosolutions.geobatch.ftp.server.GeoBatchUserManager;
import it.geosolutions.geobatch.ftp.server.model.FtpUser;
import it.geosolutions.geobatch.ui.mvc.data.FtpUserDataBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ftpserver.impl.DefaultFtpServer;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * @author Alessio Fabiani
 * 
 */
public class FTPUserFormController extends SimpleFormController {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject
	 * (javax.servlet .http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		FtpUserDataBean backingObject = new FtpUserDataBean();

		/*
		 * The backing object should be set up here, with data for the initial
		 * values of the form�s fields. This could either be hard-coded, or
		 * retrieved from a database, perhaps by a parameter, eg.
		 * request.getParameter(�primaryKey�)
		 */
		// backingObject.setAvailableDescriptors(catalog.getFlowManagers(FileBasedCatalogConfiguration.class));
		logger.info("Returning backing object");

		return backingObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(java
	 * .lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		FtpUserDataBean givenData = (FtpUserDataBean) command;
		System.out.println(givenData.toString());
		GeoBatchServer server = (GeoBatchServer) getApplicationContext()
				.getBean("geoBatchServer");

		FtpUser user = new FtpUser();

		user.setUserId(givenData.getUserId());
		user.setUserPassword(givenData.getPassword());
		user.setWritePermission(givenData.getWritePermission());
		user.setUploadRate(Integer.parseInt(givenData.getUploadRate()));
		user.setDownloadRate(Integer.parseInt(givenData.getDownloadRate()));

		// add control here

		((GeoBatchUserManager) ((DefaultFtpServer) server.getFtpServer())
				.getUserManager()).save(user);

		List<FtpUser> ftpUsers = (List<FtpUser>)request.getSession().getAttribute("ftpUsers");
		
		ftpUsers.add(user);
		
		request.getSession().setAttribute("ftpUsers", ftpUsers);
		
		logger.info("Form data successfully submitted");

		return new ModelAndView(getSuccessView());
	}
	
	
}
