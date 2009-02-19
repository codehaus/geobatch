/*
 * $Header: $fileName$ $
 * $Revision: 0.1 $
 * $Date: $date$ $time.long$ $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.iengine.flow.event.consumer.file;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.iengine.catalog.Catalog;
import it.geosolutions.iengine.catalog.file.FileBaseCatalog;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;
import it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.iengine.flow.event.action.Action;
import it.geosolutions.iengine.flow.event.action.ActionService;
import it.geosolutions.iengine.flow.event.consumer.BaseEventConsumer;
import it.geosolutions.iengine.flow.event.consumer.EventConsumer;
import it.geosolutions.iengine.flow.event.consumer.EventConsumerStatus;
import it.geosolutions.iengine.global.CatalogHolder;
import it.geosolutions.iengine.io.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ FileBasedEventConsumer.java $ Revision: 0.1 $ 30/gen/07 12:35:10
 */
public class FileBasedEventConsumer extends
        BaseEventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration> implements
        EventConsumer<FileSystemMonitorEvent, FileBasedEventConsumerConfiguration> {
    // ----------------------------------------------------------------------------
    /**
     * Common file prefix (unless the rule specify another one)
     */
    private String commonPrefixRegex;

    /**
     * Stream Transfer control
     */
    private long numInputFiles = 0;

    /**
     * Storing mandatory rules and the times they will occur.
     */
    private final List<FileEventRule> mandatoryRules = Collections
            .synchronizedList(new ArrayList<FileEventRule>());

    /**
     * Storing optional rules and the times they will occur.
     */
    private final List<FileEventRule> optionalRules = Collections
            .synchronizedList(new ArrayList<FileEventRule>());

    /**
     *
     */
    private File workingDir;

    private FileBasedEventConsumerConfiguration configuration;

    private volatile boolean canceled;

    // ----------------------------------------------- PUBLIC CONSTRUCTORS

    /**
     * Default logger
     */
    private final static Logger LOGGER = Logger.getLogger(FileBasedEventConsumer.class.toString());

    // ----------------------------------------------- PROTECTED METHODS

    public FileBasedEventConsumer(Catalog catalog, FileBasedEventConsumerConfiguration configuration)
            throws InterruptedException, IOException {
        super(catalog);
        final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(), new File(
                ((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        if (workingDir != null) {
            if (workingDir.exists() && workingDir.isDirectory() & workingDir.canRead()) {
                initialize(configuration, workingDir);
                return;
            }
        }
        throw new IllegalArgumentException("Invalid configuring directory");

    }

    // ----------------------------------------------- PUBLIC METHODS

    /**
     * This method allows the BaseDispatcher to check if an Event can be processed by the current
     * BaseEventConsumer.
     * 
     * @return boolean true if the event can be accepted, i.e. this BaseEventConsumer was waiting
     *         for it.
     * @throws InterruptedException
     */
    private boolean canConsume(FileSystemMonitorEvent event) {
        final String path = event.getSource().getAbsolutePath();
        final String fileName = FilenameUtils.getName(path);
        final String filePrefix = FilenameUtils.getBaseName(fileName);

        boolean res = this.checkMandatoryRuleConsistency(event.getNotification(), filePrefix,
                fileName);

        if (!res) {
            res = this.checkOptionalRuleConsistency(event.getNotification(), filePrefix, fileName);
        }
        return res;
    }

    // ----------------------------------------------------------------------------

    /**
     * Helper method to check for mandatory rules consistency.
     * 
     * @param fileName
     * 
     * @return boolean
     * 
     */
    private boolean checkMandatoryRuleConsistency(final FileSystemMonitorNotifications eventType,
            final String prefix, final String fileName) {

        int occurrencies;

        for (FileEventRule rule : this.mandatoryRules) {

            // check event type
            final List<FileSystemMonitorNotifications> eventTypes = rule
                    .getAcceptableNotifications();
            if (!checkEvent(eventType, eventTypes))
                return false;

            occurrencies = rule.getActualOccurrencies();
            final Pattern p = Pattern.compile(rule.getRegex());
            if (p.matcher(fileName).matches() && (occurrencies > 0)) {
                if (this.commonPrefixRegex == null) {
                    this.commonPrefixRegex = prefix;
                    rule.setActualOccurrencies(occurrencies - 1);

                    return true;
                } else if (prefix.startsWith(this.commonPrefixRegex)) {
                    rule.setActualOccurrencies(occurrencies - 1);

                    return true;
                }
            }
        }

        return false;
    }

    private static boolean checkEvent(FileSystemMonitorNotifications eventType,
            List<FileSystemMonitorNotifications> eventTypes) {
        if (eventTypes == null)
            return true;
        for (FileSystemMonitorNotifications notification : eventTypes) {
            if (notification.equals(eventType))
                return true;

        }
        return false;
    }

    /**
     * Helper method to check for optional rules consistency.
     * 
     * @param fileName
     * 
     * @return boolean
     */
    private boolean checkOptionalRuleConsistency(final FileSystemMonitorNotifications eventType,
            final String prefix, final String fileName) {
        int occurrencies;

        for (FileEventRule rule : this.optionalRules) {
            // check event type
            final List<FileSystemMonitorNotifications> eventTypes = rule
                    .getAcceptableNotifications();
            if (!checkEvent(eventType, eventTypes))
                return false;
            occurrencies = rule.getActualOccurrencies();

            final Pattern p = Pattern.compile(rule.getRegex());

            if (p.matcher(fileName).matches() && (occurrencies > 0)) {
                if (this.commonPrefixRegex == null) {
                    this.commonPrefixRegex = prefix;
                    rule.setActualOccurrencies(occurrencies - 1);

                    return true;
                } else if (prefix.startsWith(this.commonPrefixRegex)) {
                    rule.setActualOccurrencies(occurrencies - 1);

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * FileBasedEventConsumer initialization.
     * 
     * @throws InitializationException
     * @throws InterruptedException
     */
    private void initialize(FileBasedEventConsumerConfiguration configuration, File workingDir)
            throws InterruptedException {
        this.configuration = configuration;
        this.workingDir = workingDir;
        this.commonPrefixRegex = null;
        this.mandatoryRules.clear();
        this.optionalRules.clear();
        this.canceled = false;

        // //
        // preparing output directory
        // //
        final String dataStoreId = this.configuration.getName();

        File file = new File(this.workingDir, File.separator + dataStoreId);
        if (!file.exists())
            file.mkdir();

        if ((file == null) || !file.exists() || !file.isDirectory()) {
            LOGGER.log(Level.SEVERE, "Could not create GEOSERVER data directories");
            throw new IllegalStateException("Could not create GEOSERVER data directories!");
        }

        // ////////////////////////////////////////////////////////////////////
        // RULES
        // ////////////////////////////////////////////////////////////////////

        numInputFiles = 0;
        for (FileEventRule rule : configuration.getRules()) {
            FileEventRule cleanRule;
            try {
                cleanRule = (FileEventRule) rule.clone();
            } catch (CloneNotSupportedException e) {
                // XXX
                e.printStackTrace();
                continue;
            }
            if (!rule.isOptional()) {
                this.mandatoryRules.add(cleanRule);
                numInputFiles += rule.getOriginalOccurrencies();
            } else {
                this.optionalRules.add(cleanRule);
            }
        }

        // ////////////////////////////////////////////////////////////////////
        // ACTIONS
        // ////////////////////////////////////////////////////////////////////
        final List<Action<FileSystemMonitorEvent>> actions = new ArrayList<Action<FileSystemMonitorEvent>>();
        for (ActionConfiguration actionConfig : configuration.getActions()) {
            final String serviceID = actionConfig.getServiceID();
            final ActionService<FileSystemMonitorEvent, ActionConfiguration> actionService = getCatalog()
                    .getResource(serviceID, ActionService.class);
            if (actionService != null) {
                Action<FileSystemMonitorEvent> action = actionService.createAction(actionConfig);
                actions.add(action);
            }
        }
        super.addActions(actions);

    }

    /**
     * Main Thread cycle.
     */
    public void run() {
        this.canceled = false;

        try {

            // prepare date format instance for creating dir for this moment
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSSz");
            final String timeStamp = dateFormat.format(new Date());
            final File currentRunDirectory = new File(this.workingDir, timeStamp);
            currentRunDirectory.mkdir();

            if ((currentRunDirectory == null) || !currentRunDirectory.exists()
                    || !currentRunDirectory.isDirectory()) {
                LOGGER.log(Level.SEVERE, "Could not create GEOSERVER data directories");
                throw new IllegalStateException("Could not create GEOSERVER data directories!");
            }

            File backup = null;
            if (this.configuration.isPerformBackup()) {
                backup = new File(currentRunDirectory, "backup");
                if (!backup.exists()) {
                    backup.mkdirs();
                }
            }

            final Queue<FileSystemMonitorEvent> preprocessedEventsQueue = new LinkedList<FileSystemMonitorEvent>();
            for (FileSystemMonitorEvent ev : this.eventsQueue) {
                LOGGER.info(new StringBuffer("FileBasedEventConsumer [").append(
                        Thread.currentThread().getName()).append(
                        "]: new element retrieved from the MailBox.").toString());

                // get info for the input file event
                final String filePath = ev.getSource().toString();
                final File sourceDataFile = new File(filePath);
                final String fileName = FilenameUtils.getName(filePath);

                final File destDataFile = new File(currentRunDirectory, fileName);
                destDataFile.createNewFile();
                if (IOUtils.acquireLock(this, sourceDataFile)) {
                    IOUtils.copyFile(sourceDataFile, destDataFile);
                    LOGGER.info("Accepted file >> " + Thread.currentThread().getName() + " - " + fileName);
                }

                // XXX make possible to add timestamp
                preprocessedEventsQueue.offer(new FileSystemMonitorEvent(destDataFile, FileSystemMonitorNotifications.FILE_ADDED));

                // Backing up files and delete sources.
                if (this.configuration.isPerformBackup()) {
                    LOGGER.info("FileBasedEventConsumer " + Thread.currentThread().getName()
                            + " --- Performing BackUp of input files");
                    try {
                        if (IOUtils.acquireLock(this, sourceDataFile)) {

                            File destFile = new File(backup, fileName);
                            if (destFile.exists())
                                throw new IOException("Back up file already existent!");

                            IOUtils.moveFileTo(sourceDataFile, backup, true);

                        }
                    } catch (IOException e) {
                        if (LOGGER.isLoggable(Level.SEVERE))
                            LOGGER.log(Level.SEVERE, "FileBasedEventConsumer "
                                    + Thread.currentThread().getName() + " could not backup file "
                                    + fileName + " due to the following IO error: "
                                    + e.getLocalizedMessage(), e);
                    } catch (InterruptedException e) {
                        if (LOGGER.isLoggable(Level.SEVERE))
                            LOGGER.log(Level.SEVERE, "FileBasedEventConsumer "
                                    + Thread.currentThread().getName() + " could not backup file "
                                    + fileName + " due to the following IO error: "
                                    + e.getLocalizedMessage(), e);
                    }
                } else
                    // schedule for removal
                    IOUtils.deleteFile(sourceDataFile);

            }

            // //
            // TODO if no further processing is necessary or can be
            // done due to some error, set eventConsumerStatus to Finished or
            // Failure.
            // //
            LOGGER.info(new StringBuffer("FileBasedEventConsumer [").append(
                    Thread.currentThread().getName()).append("]: new element processed.")
                    .toString());
            // //
            // if the processing has been done successfully,
            // produce the DTOs.
            // //
            if (this.applyActions(preprocessedEventsQueue))
                this.setStatus(EventConsumerStatus.COMPLETED);
            else
                this.setStatus(EventConsumerStatus.FAILED);

        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "FileBasedEventConsumer "
                        + Thread.currentThread().getName() + " could not move file "
                        + " due to the following IO error: " + e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "FileBasedEventConsumer "
                        + Thread.currentThread().getName() + " could not move file "
                        + " due to the following IO error: " + e.getLocalizedMessage(), e);
        } finally {
            LOGGER.info(Thread.currentThread().getName() + " DONE!");
            this.dispose();
        }
    }

    public void setConfiguration(FileBasedEventConsumerConfiguration configuration) {
        this.configuration = configuration;

    }

    /**
     * @return the workingDirectory
     */
    public File getWorkingDir() {
        return workingDir;
    }

    public FileBasedEventConsumerConfiguration getConfiguration() {
        return configuration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.iengine.manager.Manager#dispose()
     */
    public void dispose() {
        LOGGER.info(Thread.currentThread().getName() + " DISPOSING!");

        super.dispose();
        this.numInputFiles = 0;
        this.configuration = null;
        this.commonPrefixRegex = null;
        this.mandatoryRules.clear();
        this.optionalRules.clear();

    }

    @Override
    public boolean consume(FileSystemMonitorEvent event) {
        if (getStatus() != EventConsumerStatus.IDLE && getStatus() != EventConsumerStatus.WAITING)
            return false;
        if (!canConsume(event))
            return false;
        super.consume(event);

        this.numInputFiles--;
        if (numInputFiles == 0)
            setStatus(EventConsumerStatus.EXECUTING);

        if (getStatus() == EventConsumerStatus.IDLE)
            setStatus(EventConsumerStatus.WAITING);
        else if (getStatus() == EventConsumerStatus.WAITING)
            setStatus(EventConsumerStatus.EXECUTING);
        // else throw error

        return true;
    }

    public void cancel() {
        this.canceled = true;

    }

    public boolean isCanceled() {
        return canceled;
    }

    @Override
    protected void setStatus(EventConsumerStatus eventConsumerStatus) {
        super.setStatus(eventConsumerStatus);
        // are we executing? If yes, let's trigger a thread!
        if (eventConsumerStatus == EventConsumerStatus.EXECUTING)
            getCatalog().getExecutor().execute(this);
    }

}
