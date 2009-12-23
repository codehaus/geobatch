package it.geosolutions.geobatch.track;

import it.geosolutions.geobatch.track.configuration.FusedTrackConfiguratorAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TrackThread extends Thread{
	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(TrackThread.class.toString());

	
	public TrackThread(){
		
	}

	public void run(){        
		try {	    				
			File data_dir = new File("C:/Users/Laura/Desktop/backup/fused-track-backup/dati/fused10/" + 0);
			
			if(data_dir.exists()){

				FileInputStream fis = null;
				FileOutputStream fos = null;
				
        		for(int y=0; y<1000; y++){
        			try{
            			fis = new FileInputStream("C:/Users/Laura/Desktop/backup/" +
            					"fused-track-backup/dati/fused10/0/" + y + ".txt");
            			fos = new FileOutputStream("C:/WORK/geobatch_demo/trunk/web/src/main/" +
            					"webapp/WEB-INF/data/FusedTracksContacts/in/" + y + ".txt");

            			byte [] dati = new byte[fis.available()];
            			fis.read(dati);
            			fos.write(dati);

            			fis.close();
            			fos.close();
        			}catch(Exception e){
        				try{
              				if(fis != null){
            					fis.close();
            					fis = null;
            				}
              				
              				if(fos != null){
              					fos.close();
              					fos = null;
            				}
              				
              				LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        				}catch(IOException exc){
        					LOGGER.log(Level.SEVERE, exc.getLocalizedMessage());
        				}        				
        			}
        			
        			Thread.sleep(5000);	
        		}
			}       			
		    	
	    }catch (InterruptedException e) {
	    	LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
	    }
	    
	    LOGGER.log(Level.INFO, "Exit by main thread");
	}
}
