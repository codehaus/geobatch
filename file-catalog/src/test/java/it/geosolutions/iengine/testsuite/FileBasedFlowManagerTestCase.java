/**
 * 
 */
package it.geosolutions.iengine.testsuite;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.iengine.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.iengine.flow.event.consumer.file.FileEventRule;
import it.geosolutions.iengine.flow.file.FileBasedFlowManager;
import it.geosolutions.iengine.flow.file.FileBasedFlowManagerService;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
public class FileBasedFlowManagerTestCase {

    private final static Logger LOGGER = Logger.getLogger(FileBasedFlowManagerTestCase.class
            .toString());

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
    public void testFSFlowManagerService() throws IOException, InterruptedException {
        // //
        //
        // get the FileBasedEventGeneratorService bean service from the context
        //
        // //
        Object o = context.getBean("fsFlowManagerService", FileBasedFlowManagerService.class);
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof FileBasedFlowManagerService);
        final FileBasedFlowManagerService service = (FileBasedFlowManagerService) o;

        // //
        //
        // Create a fictitious EventGeneratorConfiguration configuration
        //
        // //
        final FileBasedEventGeneratorConfiguration eventGeneratorConfiguration = new FileBasedEventGeneratorConfiguration();
        eventGeneratorConfiguration.setId("id");
        eventGeneratorConfiguration.setName("name");
        eventGeneratorConfiguration.setDescription("description");
        eventGeneratorConfiguration.setOsType(OsType.OS_UNDEFINED);
        eventGeneratorConfiguration.setWorkingDirectory(TestData.file(this, ".").getAbsolutePath());

        // //
        //
        // Create a fictitious EventGeneratorConfiguration configuration
        //
        // //
        final FileBasedEventConsumerConfiguration eventConsumerConfiguration = new FileBasedEventConsumerConfiguration();
        eventConsumerConfiguration.setId("fsEventConsumerConfiguration");
        eventConsumerConfiguration.setName("fsEventConsumerConfiguration");
        eventConsumerConfiguration.setDescription("fsEventConsumerConfiguration");
        eventConsumerConfiguration.setWorkingDirectory(TestData.file(this, ".").getAbsolutePath());

        final FileEventRule rule = new FileEventRule();
        rule.setAcceptableNotifications(Arrays.asList(FileSystemMonitorNotifications.FILE_ADDED));
        rule.setActualOccurrencies(1);
        rule.setId("tesRule");
        rule.setOptional(false);
        rule.setRegex(".*\\.txt");
        eventConsumerConfiguration.setRules(Arrays.asList(rule));
        // eventConsumerConfiguration.setActions(Arrays.asList(new
        // BaseAction<FileSystemMonitorEvent>() {
        //
        //
        // public FileSystemMonitorEvent process(
        // FileSystemMonitorEvent event) {
        // FileBasedFlowManagerTestCase.this.LOGGER.log(Level.INFO,event.toString());
        // return event;
        // }});

        // flow configuration
        final FileBasedFlowConfiguration flowConfiguration = new FileBasedFlowConfiguration();
        flowConfiguration.setId("id");
        flowConfiguration.setName("name");
        flowConfiguration.setDescription("description");
        flowConfiguration.setWorkingDirectory(TestData.file(this, ".").getAbsolutePath());
        flowConfiguration.setEventConsumerConfiguration(eventConsumerConfiguration);
        flowConfiguration.setEventGeneratorConfiguration(eventGeneratorConfiguration);

        // //
        //
        // Check if we can create the needed EventGenerator and if so create it
        //
        // //
        final boolean result = service.canCreateFlowManager(flowConfiguration);
        Assert.assertTrue(result);

        // create the event generator
        final FileBasedFlowManager eg = service.createFlowManager(flowConfiguration);
        Thread flowM = new Thread(eg);
        flowM.start();

        Thread.sleep(10000);

        //
        //
        final File file = TestData.temp(this, "test");
        // if(file.exists()) {
        synchronized (this) {
            this.wait(5000);
        }
        // Assert.assertTrue("unable to create test",this.caughtEvent);
        // }
        // else
        // Assert.assertTrue("unable to create test",false);

    };

}
