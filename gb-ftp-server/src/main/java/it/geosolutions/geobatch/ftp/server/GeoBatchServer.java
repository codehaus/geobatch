package it.geosolutions.geobatch.ftp.server;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;

public class GeoBatchServer {

	public GeoBatchServer() {
		
		FtpServerFactory serverFactory = new FtpServerFactory();
		FtpServer server = serverFactory.createServer();
		// start the server
		try {
			server.start();
		} catch (FtpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	

}
