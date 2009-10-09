package it.geosolutions.filesystemmonitor;

import it.geosolutions.factory.FactoryCreator;
import it.geosolutions.factory.FactoryRegistry;
import it.geosolutions.factory.Hints;
import it.geosolutions.factory.NotSupportedException;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public final class FactoryFinder {
	
	public static final OsType SUGGESTED_OS_TYPE;
	
	static {
		final String osName=System.getProperty("os.name").toLowerCase();	
		
		if(osName.contains("linux"))
			SUGGESTED_OS_TYPE= OsType.OS_LINUX;
		else
			if(osName.contains("windows"))
				SUGGESTED_OS_TYPE= OsType.OS_WINDOWS;
			else
				SUGGESTED_OS_TYPE=OsType.OS_UNDEFINED;		
	}
	/**
	 * The service registry for this manager. Will be initialized only when
	 * first needed.
	 */
	private static FactoryRegistry registry;

	/**
	 * Do not allows any instantiation of this class.
	 */
	private FactoryFinder() {
		// singleton
	}

	/**
	 * Returns the service registry. The registry will be created the first time
	 * this method is invoked.
	 */
	private static FactoryRegistry getServiceRegistry() {
		assert Thread.holdsLock(FactoryFinder.class);
		if (registry == null) {
			registry = new FactoryCreator(Arrays
					.asList(new Class[] { FileSystemMonitorSPI.class }));
		}
		registry.scanForPlugins();
		return registry;
	}

	public static synchronized FileSystemMonitor getMonitor(final Map<String,?>config,final OsType osType)
			throws NotSupportedException {
		return getMonitor(config,osType, null);
		
	}

	@SuppressWarnings("unchecked")
	public static synchronized FileSystemMonitor getMonitor(final Map<String,?>config,final OsType osType,final Hints hints) {
		FileSystemMonitorSPI monitorSPI = null;
		final Iterator<FileSystemMonitorSPI> fsmfi = getServiceRegistry().getServiceProviders(FileSystemMonitorSPI.class);
		while (fsmfi.hasNext()) {
			monitorSPI = fsmfi.next();
			if (monitorSPI!=null&&monitorSPI.isAvailable()&&monitorSPI.canWatch(osType)) {
				break;
			}
			monitorSPI = null;
		}
		if (monitorSPI != null) 
			return monitorSPI.createInstance(config,hints);
		
		return null;

		
	}
	/**
	 * Scans for factory plug-ins on the application class path. This method is
	 * needed because the application class path can theoretically change, or
	 * additional plug-ins may become available. Rather than re-scanning the
	 * classpath on every invocation of the API, the class path is scanned
	 * automatically only on the first invocation. Clients can call this method
	 * to prompt a re-scan. Thus this method need only be invoked by
	 * sophisticated applications which dynamically make new plug-ins available
	 * at runtime.
	 */
	public synchronized static void scanForPlugins() {
		if (registry != null) {
			registry.scanForPlugins();
		}
	}
	
	

}