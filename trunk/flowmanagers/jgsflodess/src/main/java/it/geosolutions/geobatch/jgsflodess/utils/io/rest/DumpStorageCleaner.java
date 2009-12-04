/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package it.geosolutions.geobatch.jgsflodess.utils.io.rest;

import it.geosolutions.geobatch.jgsflodess.config.global.JGSFLoDeSSGlobalConfig;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.util.logging.Logging;

/**
 * 
 * @author Fabiani
 *
 */
public class DumpStorageCleaner extends TimerTask {
    Logger LOGGER = Logging.getLogger(DumpStorageCleaner.class);

    long expirationDelay;

    @Override
    public void run() {
        try {
            File temp = new File(JGSFLoDeSSGlobalConfig.getJGSFLoDeSSDirectory());
            if (temp == null || !temp.exists())
                return;

            // ok, now scan for existing files there and clean up those 
            // that are too old
            long now = System.currentTimeMillis();
            for(File f : temp.listFiles()) {
                if(now - f.lastModified() > (expirationDelay * 1000))
                    IOUtils.deleteFile(f);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error occurred while trying to clean up old coverages from temp storage", e);
        }
    }

    /**
     * The file expiration delay in seconds, a file will be deleted when
     * it's been around more than expirationDelay
     * 
     * @return
     */
    public long getExpirationDelay() {
        return expirationDelay;
    }

    public void setExpirationDelay(long expirationDelay) {
        this.expirationDelay = expirationDelay;
    }

}
