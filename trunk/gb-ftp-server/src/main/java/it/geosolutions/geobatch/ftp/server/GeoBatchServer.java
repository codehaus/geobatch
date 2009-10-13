package it.geosolutions.geobatch.ftp.server;

import org.apache.ftpserver.FtpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class GeoBatchServer {
	public static void main(String[] args) throws Throwable {
		ApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		FtpServer ftpServer = (FtpServer) classPathXmlApplicationContext
				.getBean("server");
		ftpServer.start();
	}

}
