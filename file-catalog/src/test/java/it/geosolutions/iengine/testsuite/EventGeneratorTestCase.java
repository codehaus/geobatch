/**
 * 
 */
package it.geosolutions.iengine.testsuite;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.iengine.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.iengine.flow.event.generator.FlowEventListener;
import it.geosolutions.iengine.flow.event.generator.file.FileBasedEventGenerator;
import it.geosolutions.iengine.flow.event.generator.file.FileBasedEventGeneratorService;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public class EventGeneratorTestCase implements FlowEventListener<FileSystemMonitorEvent> {

    private final static Logger LOGGER = Logger.getLogger(EventGeneratorTestCase.class.toString());

    private ClassPathXmlApplicationContext context;

    private boolean caughtEvent;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFileBasedEventGeneratorService() throws IOException, InterruptedException {
        // //
        //
        // get the FileBasedEventGeneratorService bean service from the context
        //
        // //
        Object o = context.getBean("fsEventGeneratorService", FileBasedEventGeneratorService.class);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof FileBasedEventGeneratorService);
        final FileBasedEventGeneratorService service = (FileBasedEventGeneratorService) o;

        // //
        //
        // Create a fictitious configuration
        //
        // //
        final FileBasedEventGeneratorConfiguration configuration = new FileBasedEventGeneratorConfiguration();
        configuration.setId("id");
        configuration.setName("name");
        configuration.setDescription("description");
        configuration.setOsType(OsType.OS_UNDEFINED);
        configuration.setWorkingDirectory(TestData.file(this, ".").getAbsolutePath());

        // //
        //
        // Check if we can create the needed EventGenerator and if so create it
        //
        // //
        final boolean result = service.canCreateEventGenerator(configuration);
        Assert.assertTrue(result);

        // create the event generator
        final FileBasedEventGenerator eg = service.createEventGenerator(configuration);
        // start to listen on it and then create it
        eg.addListener(this);
        eg.start();

        Thread.sleep(5000);

        //
        final File file = TestData.temp(this, "test");
        if (file.exists()) {
            synchronized (this) {
                this.wait(5000);
            }
            Assert.assertTrue("unable to create test", this.caughtEvent);
        } else
            Assert.assertTrue("unable to create test", false);

    }

    public void eventGenerated(FileSystemMonitorEvent event) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        LOGGER.info(event.toString());
        try {
            Assert.assertTrue(event.getSource().getAbsolutePath().equalsIgnoreCase(
                    TestData.file(this, "test").getAbsolutePath()));
        } catch (IOException e) {
            Assert.assertTrue("unable to create test", false);
            return;
        }
        synchronized (this) {
            this.notify();
        }
        this.caughtEvent = true;

    };

}
