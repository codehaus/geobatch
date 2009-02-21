/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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



package it.geosolutions.iengine.catalog.dao.file.xstream;

import com.thoughtworks.xstream.XStream;
import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.iengine.configuration.event.action.ActionConfiguration;
import it.geosolutions.iengine.configuration.event.action.geoserver.GeoServerActionConfiguration;
import it.geosolutions.iengine.configuration.event.consumer.file.FileBasedEventConsumerConfiguration;
import it.geosolutions.iengine.configuration.event.generator.file.FileBasedEventGeneratorConfiguration;
import it.geosolutions.iengine.configuration.flow.file.FileBasedFlowConfiguration;
import it.geosolutions.iengine.flow.event.consumer.file.FileEventRule;
import it.geosolutions.iengine.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration;
import it.geosolutions.iengine.xstream.Alias;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * 
 * @author etj
 */
public class XStreamFlowConfigurationDAOTest extends TestCase {

    private ClassPathXmlApplicationContext context;

    public XStreamFlowConfigurationDAOTest() {
    }

    @Before
    public void setUp() throws Exception {
        this.context = new ClassPathXmlApplicationContext();
    }

    @Test
    public void testDAO() throws IOException {
        // printSample();

        Resource resource = context.getResource("data");
        File dir = resource.getFile();
        assertTrue(dir.exists());

        File file = new File(dir, "flow1.xml");
        assertTrue(file.exists());

        XStreamFlowConfigurationDAO dao = new XStreamFlowConfigurationDAO(dir.getAbsolutePath());
        FileBasedFlowConfiguration fbfc = dao.find("flow1", false);

        assertNotNull(fbfc);

        assertEquals(fbfc.getId(), "flow1id");
        assertEquals(fbfc.getName(), "flow1name");
        assertEquals(fbfc.getDescription(), "flow1desc");

        FileBasedEventGeneratorConfiguration fbegc = (FileBasedEventGeneratorConfiguration) fbfc
                .getEventGeneratorConfiguration();
        assertNotNull(fbegc);

        FileBasedEventConsumerConfiguration fbecc = (FileBasedEventConsumerConfiguration) fbfc
                .getEventConsumerConfiguration();
        assertNotNull(fbecc);

        List<? extends ActionConfiguration> lac = fbecc.getActions();
        assertNotNull(lac);
        for (ActionConfiguration actionConfiguration : lac) {
            System.out.println(actionConfiguration);
        }
        assertEquals(2, lac.size());

        List<FileEventRule> lfer = fbecc.getRules();
        assertNotNull(lfer);
        for (FileEventRule fileEventRule : lfer) {
            System.out.println(fileEventRule);
        }

        assertEquals(1, lfer.size());

        // System.out.println(dir.getAbsoluteFile() + " " +dir.exists());

        {
        }
    }

    /**
     * Print the XML of a sample FlowConfiguration
     */
    private void printSample() {
        FileBasedFlowConfiguration flowCfg = new FileBasedFlowConfiguration();
        flowCfg.setId("samplefcfg_id");
        flowCfg.setName("samplefgcg_name");
        flowCfg.setServiceID("samplefgcg_srv");

        FileBasedEventGeneratorConfiguration fbegc = new FileBasedEventGeneratorConfiguration(
                "egc_id", "egc_name", "egc_desc", false, OsType.OS_LINUX, null, "/tmp", "*");
        flowCfg.setEventGeneratorConfiguration(fbegc);

        FileBasedEventConsumerConfiguration fbecc = new FileBasedEventConsumerConfiguration();
        fbecc.setId("ecc_id");
        fbecc.setName("ecc_name");
        FileEventRule fer = new FileEventRule("fer_id", "fer_name", "fer_desc", false);
        List<FileEventRule> ferlist = new ArrayList<FileEventRule>();
        ferlist.add(fer);
        fbecc.setRules(ferlist);
        GeoTiffOverviewsEmbedderConfiguration gtoc = new GeoTiffOverviewsEmbedderConfiguration();
        gtoc.setId("gtoc_id");
        gtoc.setName("gtoc_name");
        gtoc.setWildcardString("*.tiff?");
        GeoServerActionConfiguration gsac = new GeoServerActionConfiguration();
        gsac.setId("gsac_id");
        gsac.setName("gsac_name");
        List<ActionConfiguration> acfglist = new ArrayList<ActionConfiguration>();
        acfglist.add(gtoc);
        acfglist.add(gsac);
        fbecc.setActions(acfglist);
        flowCfg.setEventConsumerConfiguration(fbecc);

        XStream xstream = new XStream();
        Alias.setAliases(xstream);

        String xml = xstream.toXML(flowCfg);
        System.out.println(xml);
    }

}