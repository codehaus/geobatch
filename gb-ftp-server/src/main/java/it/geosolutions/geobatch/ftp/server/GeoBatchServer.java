package it.geosolutions.geobatch.ftp.server;

import it.geosolutions.geobatch.ftp.server.dao.DAOException;
import it.geosolutions.geobatch.ftp.server.dao.hibernate.DAOFtpUserHibernate;
import it.geosolutions.geobatch.ftp.server.model.FtpUser;

import java.util.logging.Logger;

import org.apache.ftpserver.FtpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class GeoBatchServer {

	private static Logger logger = Logger.getLogger(GeoBatchServer.class
			.getName());

	public static void main(String[] args) throws Throwable {

		ApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		DAOFtpUserHibernate ftpUserDAO = (DAOFtpUserHibernate) classPathXmlApplicationContext
				.getBean("ftpUserDAO");

		FtpUser ftpUser = new FtpUser();
		ftpUser.setUserId("giuseppe");
		ftpUser.setUserPassword("0x,peppino,0x");
		ftpUser.setHomeDirectory("/tmp/");
		ftpUser.setWritePermission(true);

		try {
			ftpUserDAO.makePersistent(ftpUser);
			logger.info("####################### Insert " + ftpUser.toString()
					+ " in the db ################################");
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			logger.finest("ERROR : " + e.getMessage());
		}

		FtpServer ftpServer = (FtpServer) classPathXmlApplicationContext
				.getBean("server");
		ftpServer.start();
	}

}
