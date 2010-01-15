package it.geosolutions.geobatch.shp2pg;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.shp2pg.configuration.Shp2PgActionConfiguration;
import it.geosolutions.geobatch.shp2pg.configuration.Shp2PgConfiguratorAction;
import it.geosolutions.geobatch.shp2pg.util.Shp2Pg;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

public class Shp2PgFileConfigurator extends Shp2PgConfiguratorAction<FileSystemMonitorEvent> {

    private File tempOutDir = null;

    public Shp2PgFileConfigurator(Shp2PgActionConfiguration configuration) throws IOException {
        super(configuration);
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {

        try {

            // ///////////////////////////////////
            // Initializing input variables
            // ///////////////////////////////////

            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "ActionConfig is null.");
                throw new IllegalStateException("ActionConfig is null.");
            }

            // ///////////////////////////////////
            // Initializing input variables
            // ///////////////////////////////////

            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            if (!workingDir.exists() || !workingDir.isDirectory()) {
                LOGGER.log(Level.SEVERE, "Working directory does not exist ("
                        + workingDir.getAbsolutePath() + ").");
                throw new IllegalStateException("Working directory does not exist ("
                        + workingDir.getAbsolutePath() + ").");
            }

            // Fetch the first event in the queue.
            // We may have one in these 2 cases:
            // 1) a single event for a .zip file
            // 2) a list of events for the .shp+.dbf+.shx+ some other optional files

            FileSystemMonitorEvent event = events.peek();

            File[] shpList;
            final boolean isZipped;
            File zippedFile = null;
            if (events.size() == 1
                    && FilenameUtils.getExtension(event.getSource().getAbsolutePath())
                            .equalsIgnoreCase("zip")) {
                zippedFile = event.getSource();
                shpList = handleZipFile(zippedFile, workingDir);
                isZipped = true;
            } else {
                shpList = handleShapefile(events);
                isZipped = false;
            }

            // Geotools SHAPE FILE DATASTORE READ

            if (shpList == null)
                throw new Exception("Error while processing the shape file set");

            // look for the main shp file in the set
            File shapeFile = null;
            for (File file : shpList) {
                if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("shp")) {
                    shapeFile = file;
                    break;
                }
            }

            if (shapeFile == null) {
                LOGGER.log(Level.SEVERE, "Shp file not found in fileset.");
                throw new IllegalStateException("Shp file not found in fileset.");
            }

            final String shpBaseName;
            if (!isZipped)
                shpBaseName = FilenameUtils.getBaseName(shapeFile.getName());
            else {
                shpBaseName = FilenameUtils.getBaseName(zippedFile.getName());
            }

           
            Shp2Pg shp2pg = new Shp2Pg();
            
            shp2pg.copy(shapeFile, configuration);

            return events;

        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        } finally {
            // TODO: close all
        }
    }

    /**
     * Pack the files received in the events into an array.
     * 
     * <P>
     * <B>TODO</B>: should we check if all the needed files are in place (such as in {@link
     * handleZipFile(File,File)} ?
     * 
     * @param events
     *            The received event queue
     * @return
     */
    private File[] handleShapefile(Queue<FileSystemMonitorEvent> events) {
        File ret[] = new File[events.size()];
        int idx = 0;
        for (FileSystemMonitorEvent event : events) {
            ret[idx++] = event.getSource();
        }
        return ret;
    }

    private File[] handleZipFile(File source, File workingdir) {

        tempOutDir = new File(workingdir, "unzip_" + System.currentTimeMillis());

        try {
            if (!tempOutDir.mkdir()) {
                throw new IOException("Can't create temp dir '" + tempOutDir.getAbsolutePath()
                        + "'");
            }
            List<File> fileList = IOUtils.unzipFlat(source, tempOutDir);
            if (fileList == null) {
                throw new Exception("Error unzipping file");
            }

            if (fileList.isEmpty()) {
                throw new IllegalStateException("Unzip returned no files");
            }

            int shp = 0, shx = 0, dbf = 0;
            int prj = 0;

            // check that all the files have the same basename
            File file0 = fileList.get(0);
            String basename = FilenameUtils.getBaseName(file0.getName());
            for (File file : fileList) {
                if (!basename.equals(FilenameUtils.getBaseName(file.getAbsolutePath()))) {
                    throw new Exception("Basename mismatch (expected:'" + basename
                            + "', file found:'" + file.getAbsolutePath() + "')");
                }
                String ext = FilenameUtils.getExtension(file.getAbsolutePath());
                // do we want such an hardcoded list?
                if ("shp".equalsIgnoreCase(ext))
                    shp++;
                else if ("shx".equalsIgnoreCase(ext))
                    shx++;
                else if ("dbf".equalsIgnoreCase(ext))
                    dbf++;
                else if ("prj".equalsIgnoreCase(ext))
                    prj++;
                else {
                    // Do we want to be more lenient if unexpected/useless files are found?
                    throw new IllegalStateException("Unexpected file extension in zipfile '" + ext
                            + "'");
                }
            }

            if (shp * shx * dbf != 1) {
                throw new Exception("Bad fileset in zip file.");
            }

            return fileList.toArray(new File[fileList.size()]);

        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Error examining zipfile", t);
            try {
                // org.apache.commons.io.IOUtils.
                FileUtils.forceDelete(tempOutDir);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Can't delete temp dir '" + tempOutDir + "'", ex);
            }
            return null;
        }
    }

}
