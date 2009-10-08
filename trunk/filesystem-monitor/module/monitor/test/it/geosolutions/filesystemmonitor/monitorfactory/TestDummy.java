package it.geosolutions.filesystemmonitor.monitorfactory;

import it.geosolutions.factory.NotSupportedException;
import it.geosolutions.filesystemmonitor.FactoryFinder;
import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import junit.framework.TestCase;
import junit.textui.TestRunner;

public class TestDummy extends TestCase {
	
	public static void main(String[] args) {
		TestRunner.run(TestDummy.class);
	}
	
	public void testDummy() throws NotSupportedException{
		//get the registered services
		FileSystemMonitor abstractMonitor= FactoryFinder.getMonitor(null,OsType.OS_UNDEFINED);
		assertTrue(abstractMonitor!=null);
		assertTrue(abstractMonitor instanceof DummyMonitor);
	}

}
