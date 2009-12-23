package it.geosolutions.geobatch.track;


public class IngestTrack {
    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            TrackThread t = new TrackThread();
            t.start();
        } catch (Exception e) {
            System.out.println("EXCEPTION -> " + e.getLocalizedMessage());
        }
    }
}
