package it.geosolutions.iengine.flow.event.consumer.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.iengine.flow.event.consumer.EventConsumerService;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileBasedEventConsumerService extends BaseService implements
        EventConsumerService<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration> {

    private final static Logger LOGGER = Logger.getLogger(FileBasedEventConsumerService.class
            .toString());

    public boolean canCreateEventConsumer(FileBasedEventConsumerConfiguration configuration) {

        final String workingDir = configuration.getWorkingDirectory();
        if (workingDir != null) {
            final File dir = new File((String) workingDir);
            if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
                // TODO message
                return false;
        }
        return true;
    }

    public FileBasedEventConsumer createEventConsumer(
            FileBasedEventConsumerConfiguration configuration) {
        try {
            return new FileBasedEventConsumer(getCatalog(), configuration);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return null;
    }

}
