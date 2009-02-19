package it.geosolutions.iengine.flow.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.iengine.flow.FlowManagerService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataAccessFactory.Param;

public class FileBasedFlowManagerService extends BaseService implements
        FlowManagerService<FileSystemMonitorEvent, FileBasedFlowConfiguration> {

    private FileBasedFlowManagerService() {
        super(true);
    }

    public final static Param WORKING_DIR = new Param("WorkingDir", String.class, "WorkingDir",
            true);

    private final static Logger LOGGER = Logger.getLogger(FileBasedFlowManagerService.class
            .toString());

    public boolean canCreateFlowManager(FileBasedFlowConfiguration configuration) {

        final String workingDir = configuration.getWorkingDirectory();
        if (workingDir != null) {
            final File dir = new File((String) workingDir);
            if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
                // TODO message
                return false;
        }

        return true;

    }

    public FileBasedFlowManager createFlowManager(FileBasedFlowConfiguration configuration) {

        final String workingDir = configuration.getWorkingDirectory();
        if (workingDir != null) {
            final File dir = new File((String) workingDir);
            if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
                // TODO message
                return null;

            try {
                final FileBasedFlowManager manager = new FileBasedFlowManager();
                manager.setConfiguration(configuration);
                return manager;
            } catch (IOException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);

            }

        }
        return null;
    }

}
