/**
 * 
 */
package it.geosolutions.iengine.flow.event.generator.file;

import it.geosolutions.factory.NotSupportedException;
import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.iengine.catalog.impl.BaseService;
import it.geosolutions.iengine.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.iengine.flow.event.generator.EventGeneratorService;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public class FileBasedEventGeneratorService extends BaseService implements
        EventGeneratorService<FileSystemMonitorEvent, FileBasedEventGeneratorConfiguration> {

    private final static Logger LOGGER = Logger.getLogger(FileBasedEventGeneratorService.class
            .toString());

    /**
	 * 
	 */
    private FileBasedEventGeneratorService() {
        super(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.geosolutions.iengine.flow.event.generator.EventGeneratorService#canCreateEventGenerator
     * (java.util.Map)
     */
    public boolean canCreateEventGenerator(FileBasedEventGeneratorConfiguration configuration) {
        final OsType osType = configuration.getOsType();
        if (osType == null)
            return false;
        final String sensedDir = configuration.getWorkingDirectory();
        if (sensedDir != null) {
            final File dir = new File((String) sensedDir);
            if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
                // TODO message
                return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.geosolutions.iengine.flow.event.generator.EventGeneratorService#createEventGenerator(java
     * .util.Map)
     */
    public FileBasedEventGenerator createEventGenerator(
            FileBasedEventGeneratorConfiguration configuration) {

        try {
            final OsType osType = configuration.getOsType();
            final FileSystemMonitorNotifications eventType = configuration.getEventType();
            final String sensedDir = configuration.getWorkingDirectory();
            if (sensedDir != null) {
                final File dir = new File((String) sensedDir);
                if (!dir.exists() || !dir.isDirectory() || !dir.canRead())
                    // TODO message
                    return null;
            }
            if (configuration.getWildCard() == null)
                return new FileBasedEventGenerator(osType, eventType, new File(sensedDir));
            else
                return new FileBasedEventGenerator(osType, eventType, new File(sensedDir),
                        configuration.getWildCard());
        } catch (NotSupportedException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return null;
    }

}
