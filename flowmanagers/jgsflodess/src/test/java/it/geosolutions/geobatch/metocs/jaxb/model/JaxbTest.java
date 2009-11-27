/**
 * 
 */
package it.geosolutions.geobatch.metocs.jaxb.model;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * @author Alessio
 *
 */
public class JaxbTest {

	private static final String SOURCE_XML = "G:/work/IngestionEngine/trunk/flowmanagers/jgsflodess/src/test/java/it/geosolutions/geobatch/metocs/jaxb/model/metoc-dictionary.xml";
	
	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws JAXBException, FileNotFoundException {
		// create JAXB context and instantiate marshaller
		JAXBContext context = JAXBContext.newInstance(Metocs.class);
		
		System.out.println("Output from our XML File: ");
		Unmarshaller um = context.createUnmarshaller();
		Metocs metocDictionary = (Metocs) um.unmarshal(new FileReader(SOURCE_XML));

		for (int i = 0; i < metocDictionary.getMetoc().toArray().length; i++) {
			System.out.println("Metoc " + (i + 1) + ": '"
					+ metocDictionary.getMetoc().get(i).getName() + "' Brief: '"
					+ metocDictionary.getMetoc().get(i).getBrief() + "' UoM: '" 
					+ metocDictionary.getMetoc().get(i).getDefaultUom() + "' Type: '"
					+ metocDictionary.getMetoc().get(i).getType() + "' " 
			);
		}
	}

}
