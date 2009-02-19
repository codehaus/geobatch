/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



package it.geosolutions.iengine.flow.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.catalog.file.FileBaseCatalog;
import it.geosolutions.iengine.catalog.impl.BasePersistentResource;
import it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.iengine.configuration.event.generator.EventGeneratorConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.iengine.flow.FlowManager;
import it.geosolutions.iengine.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.iengine.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.iengine.flow.event.consumer.file.FileBasedEventConsumer;
import it.geosolutions.iengine.flow.event.generator.EventGenerator;
import it.geosolutions.iengine.flow.event.generator.EventGeneratorService;
import it.geosolutions.iengine.flow.event.generator.FlowEventListener;
import it.geosolutions.iengine.flow.event.generator.file.FileBasedEventGenerator;
import it.geosolutions.iengine.global.CatalogHolder;
import it.geosolutions.iengine.io.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alessio Fabiani, GeoSolutions
 * 
 */
public class FileBasedFlowManager
		extends BasePersistentResource<FileBasedFlowConfiguration>
        implements FlowManager<FileSystemMonitorEvent, FileBasedFlowConfiguration>,
					FlowEventListener<FileSystemMonitorEvent>,
					Runnable {

    /** Default Logger **/
    private final static Logger LOGGER = Logger.getLogger(FlowManager.class.toString());

    /**
     * Base class for dispatchers.
     * 
     * @author AlFa
     * @version $ EventDispatcher.java $ Revision: 0.1 $ 22/gen/07 19:36:25
     */
    private final class EventDispatcher extends Thread {

        // ----------------------------------------------- PUBLIC METHODS
        /**
         * Default Constructor
         */
        public EventDispatcher() {
            super(new StringBuilder("EventDispatcherThread-").append(
                    FileBasedFlowManager.this.getId()).toString());
            setDaemon(true);// shut me down when parent shutdown
            // reset interrupted flag
            interrupted();
        }

        /**
         * Shutdown the dispatcher.
         */
        public void shutdown() {
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Shutting down the dispatcher ... NOW!");
            interrupt();

        }

        // ----------------------------------------------- UTILITY METHODS

        /**
    	 *
    	 */
        public void run() {
            try {
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("FileMonitorEventDispatcher is ready to dispatch Events.");

                while (!isInterrupted()) {

                    // //
                    // waiting for a new event
                    // //
                	final FileSystemMonitorEvent event;
                	try{
                		event = FileBasedFlowManager.this.eventMailBox.take();
                	}catch (InterruptedException e) {
                		this.interrupt();
                		return;
					}
                    // //
                    // is there some Event BaseEventConsumer waiting for a particular event?
                    // //
                    boolean eventServed = false;
                    final Iterator<BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration>> it = FileBasedFlowManager.this.collectingEventConsumers
                            .iterator();
                    while (it.hasNext()) {
                        final BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration> em = it
                                .next();
                        if (em.consume(event)) {
                            // //
                            // we have found an Event BaseEventConsumer waiting for this event, if
                            // we have changed state we remove it from the list
                            // //
                            if (em.getStatus().equals(EventConsumerStatus.EXECUTING))
                                it.remove();
                            
                            //event served
                            eventServed = true;
                            break;
                        }
                    }

                    if (!eventServed) {
                        // //
                        // if no EventConsumer is found, we need to create a new one
                        // //
                        final FileBasedEventConsumer fileBasedEventConsumer = new FileBasedEventConsumer(
                                getCatalog(),
                                (FileBasedEventConsumerConfiguration) FileBasedFlowManager.this
                                        .getConfiguration().getEventConsumerConfiguration());

                        if (fileBasedEventConsumer.consume(event)) {
                            // //
                            // we have found an Event BaseEventConsumer waiting for this event, if
                            // it has changed state we do not add it to the list of waiting consumers
                            // //
                            if (!fileBasedEventConsumer.getStatus().equals(EventConsumerStatus.EXECUTING))
                               FileBasedFlowManager.this.collectingEventConsumers.add(fileBasedEventConsumer);
                            eventServed = true;
                            if (LOGGER.isLoggable(Level.FINE))
                            	LOGGER.fine(new StringBuffer("Event not consumed ----> ").append(
                                    event.toString()).toString());
                        }

                    }
                }
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, new StringBuffer("Caught an Interrupted Exception: ")
                        .append(e.getLocalizedMessage()).toString(), e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, new StringBuffer("Caught an IOException Exception: ")
                        .append(e.getLocalizedMessage()).toString(), e);
            }

        }
    }

    private File workingDirectory;

    /**
     * initialized flag
     */
    private boolean initialized;

    /**
     * started flag
     */
    private boolean started = false;

    /**
     * paused flag
     */
    private boolean paused;

    /**
     * termination flag
     */
    private boolean termination;

    /**
     * The MailBox
     */
    private final BlockingQueue<FileSystemMonitorEvent> eventMailBox = new LinkedBlockingQueue<FileSystemMonitorEvent>();

    /**
     * The FileMonitorEventDispatcher
     */
    private EventDispatcher dispatcher;

    /**
     * File System Monitor
     */
//    private FileBasedEventGenerator eventGenerator;
    private EventGenerator eventGenerator;

    private final List<BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration>> collectingEventConsumers = new ArrayList<BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration>>();

    /**
     * @param configuration
     * @throws IOException
     */
    public FileBasedFlowManager() throws IOException {
    }

    /**
     * @param configuration
     * @throws IOException
     */
    private void initialize(FileBasedFlowConfiguration configuration) throws IOException {
        this.initialized = false;
        this.paused = false;
        this.termination = false;

		String baseDir = ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory();
		if(baseDir == null)
            throw new IllegalArgumentException("Working dir is null");

        this.workingDirectory = IOUtils.findLocation(configuration.getWorkingDirectory(), 
													 new File(baseDir));

        if (workingDirectory == null || !workingDirectory.exists() || !workingDirectory.canWrite()
                || !workingDirectory.isDirectory())
            throw new IllegalArgumentException(new StringBuilder("Working dir is invalid: ")
                    .append(">").append(baseDir).append("< ")
                    .append(">").append(configuration.getWorkingDirectory()).append("< ").toString()
					);

    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.FlowManager#dispose()
     */
    public synchronized void dispose() {
        LOGGER.info("Disposing: " + this.getId());
        this.termination = true;
        this.notify();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.FlowManager#isRunning()
     */
    public boolean isRunning() {
        return !paused && started;
    }

    public synchronized void run() {
        do {
            if (termination) {
                if (initialized) {
                    dispatcher.shutdown();
                    eventGenerator.dispose();
                    initialized = false;
                }

                paused = true;

                break;
            }

            while (paused) {
                try {
                    if (initialized && ((eventGenerator != null) && eventGenerator.isRunning())) {
                        eventGenerator.stop();
                        eventGenerator.dispose();
                        eventGenerator = null;
                    }

                    this.wait();

                    if (termination) {
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.severe("Error on dispatcher initialization: " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
            }

            if (!initialized) {
                // //
                // Initialize objects
                // //

                this.dispatcher = new EventDispatcher();
                dispatcher.start();
                initialized = true;
            }

            while (!paused) {
                try {
                    if (initialized && ((eventGenerator == null) || !eventGenerator.isRunning())) {
                        // //
                        // Creating the FileBasedEventGenerator, which waits for new events
                        // //
                        try {
                            LOGGER.info("EventGeneratorCreationStart");
                            final EventGeneratorConfiguration generatorConfig = getConfiguration().getEventGeneratorConfiguration();
                            final String serviceID = generatorConfig.getServiceID();
                            LOGGER.info("EventGeneratorCreationServiceID: "+ serviceID);
                            final EventGeneratorService<EventObject, EventGeneratorConfiguration> generatorService = getCatalog().getResource(serviceID, EventGeneratorService.class);
                            if (generatorService != null) {
                                LOGGER.info("EventGeneratorCreationFound!");
                                eventGenerator = generatorService.createEventGenerator(generatorConfig);
                                LOGGER.info("EventGeneratorCreationCreated!");
                                eventGenerator.addListener(this);
                                LOGGER.info("EventGeneratorCreationAdded!");
                                eventGenerator.start();
                                LOGGER.info("EventGeneratorCreationStarted!");
                            }
                            LOGGER.info("EventGeneratorCreationEnd");
                        } catch (Throwable t) {
                            LOGGER.log(Level.SEVERE, "Error on FS-Monitor initialization: " + t.getLocalizedMessage(), t);
                            throw new RuntimeException(t);
                        }
                    }

                    this.wait();

                    if (termination) {
                        break;
                    }
                } catch (InterruptedException e) {
                    LOGGER.severe("FlowManager cycle exception: " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
            }
        } while (true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.FlowManager#start()
     */
    public synchronized void resume() {
        LOGGER.info("Resuming: " + this.getId());

        if (!started) {
            getCatalog().getExecutor().execute(this);
            this.started = true;
            this.paused = false;
        } else if (!isRunning()) {
            this.paused = false;
            this.notify();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.FlowManager#stop()
     */
    public synchronized void pause() {
        LOGGER.info("Pausing: " + this.getId());

        if (isRunning()) {
            this.paused = true;
            this.notify();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.catalog.FlowManager#reset()
     */
    public void reset() {
        LOGGER.info("Resetting: " + this.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getId();
    }

    /**
     * @return the initialized
     */
    public boolean isInited() {
        return initialized;
    }

    /**
     * @return the paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * @return the termination
     */
    public boolean isTermination() {
        return termination;
    }

    /**
     * @return the workingDirectory
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory(File outputDir) {
        this.workingDirectory = outputDir;
    }

    public void flowEventCollected(EventObject fe) {
        if (!(fe instanceof FileSystemMonitorEvent)) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning(new StringBuilder("Rejecting event: ").append(fe.toString())
                        .toString());
            return;
        }
        try {
            this.eventMailBox.put((FileSystemMonitorEvent) fe);
        } catch (InterruptedException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

    }

    public EventGenerator<FileSystemMonitorEvent> getEventGenerator() {
        return this.eventGenerator;
    }

    public void setEventGenerator(EventGenerator<FileSystemMonitorEvent> eventGenerator) {
        this.eventGenerator = (FileBasedEventGenerator) eventGenerator;

    }

    public void eventGenerated(FileSystemMonitorEvent event) {
        try {
            this.eventMailBox.put(event);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void setConfiguration(FileBasedFlowConfiguration configuration) {
        super.setConfiguration(configuration);
        try {
            initialize(configuration);
        } catch (IOException e) {
            // XXX
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void load() {
        super.load();
    }

    @Override
    public synchronized boolean remove() {
        return super.remove();
    }

    @Override
    public synchronized void persist() {
        super.persist();
    }

}
