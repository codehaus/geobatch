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

import it.geosolutions.geobatch.ftp.server.GeoBatchServer;
import it.geosolutions.geobatch.ftp.server.GeoBatchUserManager;
import it.geosolutions.geobatch.ui.mvc.data.FtpUserDataBean;

import org.apache.ftpserver.impl.DefaultFtpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author giuseppe
 * 
 */
public class FtpUserFormValidator implements Validator {

	private GeoBatchServer server;

	public FtpUserFormValidator(ApplicationContext context) {
		this.server = (GeoBatchServer) context.getBean("geoBatchServer");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class givenClass) {
		return givenClass.equals(FtpUserDataBean.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		boolean present = false;
		FtpUserDataBean givenData = (FtpUserDataBean) obj;

		if (givenData == null) {
			errors.reject("error.nullpointer", "Null data received");
		} else {
			/* VALIDATE ALL FIELDS */
			if ((givenData.getUserId() == null)
					|| (givenData.getUserId().trim().length() <= 0)) {
				errors.rejectValue("userId", "error.code",
						"Ftp User Id is mandatory.");
			} else {
				if (((GeoBatchUserManager) ((DefaultFtpServer) server
						.getFtpServer()).getUserManager()).checkUser(givenData
						.getUserId())) {
					present = true;
					errors.rejectValue("userId", "error.code", "Ftp User "
							+ givenData.getUserId() + " has already entered.");
				}
			}

			if (!present) {
				if ((givenData.getPassword() == null)
						|| (givenData.getPassword().trim().length() <= 0)) {
					errors.rejectValue("password", "error.code",
							"Ftp User Password is mandatory.");
				}

				if ((givenData.getRepeatPassword() == null)
						|| (givenData.getRepeatPassword().trim().length() <= 0)) {
					errors.rejectValue("repeatPassword", "error.code",
							"Ftp User Repeat Password is mandatory.");
				}

				if ((!givenData.getPassword().equals(""))
						&& (!givenData.getRepeatPassword().equals(""))) {
					if (!givenData.getPassword().equals(
							givenData.getRepeatPassword())) {
						errors.rejectValue("password", "error.code",
								"The password must be the same.");
					}

				}

				if (!givenData.getDownloadRate().equals("")) {
					try {
						int downloadRate = Integer.parseInt(givenData
								.getDownloadRate());
						if (downloadRate < 0) {
							errors
									.rejectValue("downloadRate", "error.code",
											"Ftp User Download Rate must be greater than 0.");
						}
					} catch (NumberFormatException e) {
						errors.rejectValue("downloadRate", "error.code",
								"Ftp User Download Rate must be an integer.");
					}
				}

				if (!givenData.getUploadRate().equals("")) {
					try {
						int uploadRate = Integer.parseInt(givenData
								.getUploadRate());
						if (uploadRate < 0) {
							errors
									.rejectValue("uploadRate", "error.code",
											"Ftp User Upload Rate must be greater than 0.");
						}
					} catch (NumberFormatException e) {
						errors.rejectValue("uploadRate", "error.code",
								"Ftp User Upload Rate must be an integer.");
					}
				}
			}
		}
	}
}
