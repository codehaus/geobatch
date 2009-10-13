package it.geosolutions.geobatch.ftp.server;

import it.geosolutions.geobatch.ftp.server.dao.DAOException;
import it.geosolutions.geobatch.ftp.server.dao.hibernate.DAOFtpUserHibernate;
import it.geosolutions.geobatch.ftp.server.model.FtpUser;

import java.util.logging.Logger;

import org.apache.ftpserver.FtpServer;
import org.springframework.beans.factory.InitializingBean;

public class GeoBatchServer implements InitializingBean {

	private static Logger logger = Logger.getLogger(GeoBatchServer.class
			.getName());

	private DAOFtpUserHibernate ftpUserDAO;
	private FtpServer ftpServer;

	public void afterPropertiesSet() throws Exception {
		FtpUser ftpUser = new FtpUser();
		ftpUser.setUserId("giuseppe");
		ftpUser.setUserPassword("0x,peppino,0x");
		ftpUser.setHomeDirectory("/tmp/");
		ftpUser.setWritePermission(true);

		try {
			ftpUserDAO.makePersistent(ftpUser);
			logger
					.info("################################ Insert "
							+ ftpUser.toString()
							+ " in the db #######################");
			this.ftpServer.start();
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			logger.info("ERROR : " + e);
		}
	}

	/**
	 * @param ftpUserDAO
	 *            the ftpUserDAO to set
	 */
	public void setFtpUserDAO(DAOFtpUserHibernate ftpUserDAO) {
		this.ftpUserDAO = ftpUserDAO;
		logger.info("FTP SERVER SESSION FACTORY######################### " + ftpUserDAO.getSessionFactory().hashCode());
	}

	/**
	 * @param ftpServer
	 *            the ftpServer to set
	 */
	public void setFtpServer(FtpServer ftpServer) {
		this.ftpServer = ftpServer;
	}

	// public static void main(String[] args) throws Throwable {
	//
	// ApplicationContext classPathXmlApplicationContext = new
	// ClassPathXmlApplicationContext(
	// "applicationContext.xml");
	// DAOFtpUserHibernate ftpUserDAO = (DAOFtpUserHibernate)
	// classPathXmlApplicationContext
	// .getBean("ftpUserDAO");
	//
	// FtpUser ftpUser = new FtpUser();
	// ftpUser.setUserId("giuseppe");
	// ftpUser.setUserPassword("0x,peppino,0x");
	// ftpUser.setHomeDirectory("/tmp/");
	// ftpUser.setWritePermission(true);
	//
	// try {
	// ftpUserDAO.makePersistent(ftpUser);
	// logger.info("####################### Insert " + ftpUser.toString()
	// + " in the db ################################");
	// } catch (DAOException e) {
	// // TODO Auto-generated catch block
	// logger.finest("ERROR : " + e.getMessage());
	// }
	//
	// FtpServer ftpServer = (FtpServer) classPathXmlApplicationContext
	// .getBean("server");
	// ftpServer.start();
	// }

}
