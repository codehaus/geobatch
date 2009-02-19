/**
 * 
 */
package it.geosolutions.iengine.testsuite;

import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.iengine.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.iengine.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedCatalogConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.iengine.flow.event.consumer.file.FileEventRule;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.junit.Test;

/**
 * @author Simone Giannecchini, GeoSolutions.
 * 
 */
public class ConfigurationTestCase {

//    @Test
    public void testFileBasedEventGeneratorConfiguration() throws JiBXException,
            FileNotFoundException, IOException {
        final FileBasedEventGeneratorConfiguration config = new FileBasedEventGeneratorConfiguration();
        config.setId("id");
        config.setName("test");
        config.setDescription("description");
        config.setOsType(OsType.OS_LINUX);
        config.setWildCard("*.*");
        config.setWorkingDirectory(TestData.file(this, ".").getAbsolutePath());

        // marshalling
        final IBindingFactory bfact = BindingDirectory
                .getFactory(FileBasedEventGeneratorConfiguration.class);
        final IMarshallingContext mctx = bfact.createMarshallingContext();
        mctx.marshalDocument(config, "UTF-8", null, new FileOutputStream(new File(TestData.file(
                this, "."), "FileBasedEventGeneratorConfiguration.xml")));

        // ummarshalling
        final IUnmarshallingContext umctx = bfact.createUnmarshallingContext();
        final FileBasedEventGeneratorConfiguration obj = (FileBasedEventGeneratorConfiguration) umctx
                .unmarshalDocument(new FileInputStream(new File(TestData.file(this, "."),
                        "FileBasedEventGeneratorConfiguration.xml")), null);

        // testing equality
        org.junit.Assert.assertTrue(config.getId().equalsIgnoreCase(obj.getId()));
        org.junit.Assert.assertTrue(config.getDescription().equalsIgnoreCase(obj.getDescription()));
        org.junit.Assert.assertTrue(config.getOsType().equals((obj.getOsType())));
        org.junit.Assert.assertTrue(config.getWildCard().equalsIgnoreCase(obj.getWildCard()));
        org.junit.Assert.assertTrue(config.getWorkingDirectory().equalsIgnoreCase(
                obj.getWorkingDirectory()));

    }

//    @Test
    public void testFileBasedEventConsumerConfiguration() throws JiBXException,
            FileNotFoundException, IOException {
        final FileBasedEventConsumerConfiguration config = new FileBasedEventConsumerConfiguration();
        config.setId("id");
        config.setName("test");
        config.setDescription("description");
        config.setWorkingDirectory(TestData.file(this, ".").getAbsolutePath());

        final FileEventRule rule = new FileEventRule();
        rule.setId("id");
        rule.setName("test");
        rule.setDescription("description");
        rule.setOriginalOccurrencies(2);
        rule.setOptional(false);
        rule.setRegex("*.txt");
        config.setRules(Arrays.asList(rule));

        final GeoServerActionConfiguration geoserverconfig = new GeoServerActionConfiguration();
        geoserverconfig.setId("id");
        geoserverconfig.setServiceID("serviceID");
        geoserverconfig.setName("test");
        geoserverconfig.setDescription("description");
        geoserverconfig.setEnvelope("");
        geoserverconfig.setGeoserverPWD("geoserver");
        geoserverconfig.setDataTransferMethod("dataTransferMethod");
        geoserverconfig.setGeoserverUID("admin");
        geoserverconfig.setGeoserverURL("http://localhost:8080");
        geoserverconfig.setStyles(new ArrayList<String>(Collections.singletonList("raster.sld")));
        geoserverconfig.setCrs("EPSG:4326");
        geoserverconfig.setStoreFilePrefix("ais");
        config.setActions(Arrays.asList(geoserverconfig));

        final IBindingFactory bfact = BindingDirectory
                .getFactory(FileBasedEventConsumerConfiguration.class);
        final IMarshallingContext mctx = bfact.createMarshallingContext();
        mctx.marshalDocument(config, "UTF-8", null, new FileOutputStream(new File(TestData.file(
                this, "."), "FileBasedEventConsumerConfiguration.xml")));

        final IUnmarshallingContext umctx = bfact.createUnmarshallingContext();
        final FileBasedEventConsumerConfiguration obj1 = (FileBasedEventConsumerConfiguration) umctx
                .unmarshalDocument(new FileInputStream(new File(TestData.file(this, "."),
                        "FileBasedEventConsumerConfiguration.xml")), null);
        System.out.println(obj1);
    }

//    @Test
    public void testFileBasedCatalogConfiguration() throws JiBXException, FileNotFoundException,
            IOException {
        final FileBasedCatalogConfiguration config = new FileBasedCatalogConfiguration();
        config.setId("catalog");
        config.setName("nameFileBasedCatalogConfiguration");
        config.setDescription("descriptionFileBasedCatalogConfiguration");
        config.setWorkingDirectory("c:/tmp/");

        final IBindingFactory bfact = BindingDirectory
                .getFactory(FileBasedCatalogConfiguration.class);
        final IMarshallingContext mctx = bfact.createMarshallingContext();
        mctx.marshalDocument(config, "UTF-8", null, new FileOutputStream(new File(TestData.file(
                this, "."), "catalog.xml")));

    }

    @Test
    public void testFlowConfigPersistence() throws IOException, JiBXException {

        // //
        //
        // Create a fictitious EventGeneratorConfiguration configuration
        //
        // //
        final FileBasedEventGeneratorConfiguration eventGeneratorConfiguration = new FileBasedEventGeneratorConfiguration();
        eventGeneratorConfiguration.setId("id");
        eventGeneratorConfiguration.setName("test");
        eventGeneratorConfiguration.setDescription("description");
        eventGeneratorConfiguration.setOsType(OsType.OS_LINUX);
        eventGeneratorConfiguration.setWildCard("*.*");
        eventGeneratorConfiguration.setWorkingDirectory("C:\\tmp\\flow1");

        // //
        //
        // Create a fictitious EventGeneratorConfiguration configuration
        //
        // //
        final FileBasedEventConsumerConfiguration config = new FileBasedEventConsumerConfiguration();
        config.setId("id");
        config.setName("test");
        config.setDescription("description");
        config.setWorkingDirectory("C:\\tmp\\flow1");

        final FileEventRule rule = new FileEventRule();
        rule.setId("id");
        rule.setName("test");
        rule.setDescription("description");
        rule.setOriginalOccurrencies(2);
        rule.setActualOccurrencies(2);
        rule.setOptional(false);
        rule.setRegex("*.*");
        config.setRules(Arrays.asList(rule));

        final GeoServerActionConfiguration geoserverconfig = new GeoServerActionConfiguration();
        geoserverconfig.setId("id");
        geoserverconfig.setServiceID("idd");
        geoserverconfig.setName("test");
        geoserverconfig.setDescription("description");
        geoserverconfig.setEnvelope("");
        geoserverconfig.setDataTransferMethod("dataTransferMethod");
        geoserverconfig.setGeoserverPWD("geoserver");
        geoserverconfig.setGeoserverUID("admin");
        geoserverconfig.setGeoserverURL("http://localhost:8080");
        geoserverconfig.setStyles(new ArrayList<String>(Collections.singletonList("raster.sld")));
        geoserverconfig.setCrs("EPSG:4326");
        geoserverconfig.setStoreFilePrefix("ais");
        config.setActions(Arrays.asList(geoserverconfig));

        // flow configuration
        final FileBasedFlowConfiguration flowConfiguration = new FileBasedFlowConfiguration();
        flowConfiguration.setId("flow1");
        flowConfiguration.setName("flow1");
        flowConfiguration.setDescription("flow1");
        flowConfiguration.setWorkingDirectory("C:\\tmp\\flow1");
        flowConfiguration.setEventConsumerConfiguration(config);
        flowConfiguration.setEventGeneratorConfiguration(eventGeneratorConfiguration);

        final IBindingFactory bfact = BindingDirectory.getFactory(FileBasedFlowConfiguration.class);
        final IMarshallingContext mctx = bfact.createMarshallingContext();
        mctx.marshalDocument(flowConfiguration, "UTF-8", null, new FileOutputStream(new File(
                TestData.file(this, "."), "flow1.xml")));
        
        
        // ummarshalling
        final IUnmarshallingContext umctx = bfact.createUnmarshallingContext();
        final FileBasedFlowConfiguration obj = (FileBasedFlowConfiguration) umctx
                .unmarshalDocument(new FileInputStream(new File(TestData.file(this, "."),
                        "flow1.xml")), null);
        
        System.out.println(obj);
    }

    // @Test
    // public void testJIBXDao() {
    // final JIBXDAOService<FileBasedFlowConfiguration> service=
    // (JIBXDAOService<FileBasedFlowConfiguration>)
    // context.getBean("JIBXFlowConfigurationDAOService",JIBXDAOService.class);
    // final DAO dao = service.createDAO(FileBasedFlowConfiguration.class);
    //		
    // }

}
