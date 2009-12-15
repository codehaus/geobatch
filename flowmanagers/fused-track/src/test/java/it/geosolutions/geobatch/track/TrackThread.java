package it.geosolutions.geobatch.track;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Logger;

public class TrackThread extends Thread {
	private final static Logger log = Logger.getLogger(TrackThread.class.getName());

	private boolean allDone = false;

	public TrackThread() {

	}

	public void stopThread() {
		this.allDone = true;
	}

	public void run() {
		try {
			File data_dir = new File(
					"C:/Users/Francesco/Desktop/Fused10/" + 0);

			if (data_dir.exists()) {

				for (int y = 6; y < 1000; y++) {
					try {
						FileInputStream fis = new FileInputStream(
								"C:/Users/Francesco/Desktop/Fused10/0/" + y
										+ ".txt");
						FileOutputStream fos = new FileOutputStream(
								"C:/Users/Francesco/Desktop/GB_DATA_DIR/FusedTracksContacts/in/"
										+ y + ".txt");

						byte[] dati = new byte[fis.available()];
						fis.read(dati);
						fos.write(dati);

						fis.close();
						fos.close();
					} catch (Exception e) {

					}

					Thread.sleep(5000);
				}
			}

		} catch (InterruptedException e) {
			log.fine("Main thread interrupted!");
		}

		log.info("Exit by main thread");
	}
}
