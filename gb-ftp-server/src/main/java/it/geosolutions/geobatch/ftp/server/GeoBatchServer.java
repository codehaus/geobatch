package it.geosolutions.geobatch.ftp.server;

import org.apache.ftpserver.FtpServer;
import org.springframework.beans.factory.InitializingBean;

public class GeoBatchServer implements InitializingBean {

	private FtpServer ftpServer;

	public void afterPropertiesSet() throws Exception {
		this.ftpServer.start();
	}

	/**
	 * @param ftpServer
	 *            the ftpServer to set
	 */
	public void setFtpServer(FtpServer ftpServer) {
		this.ftpServer = ftpServer;
	}
}
