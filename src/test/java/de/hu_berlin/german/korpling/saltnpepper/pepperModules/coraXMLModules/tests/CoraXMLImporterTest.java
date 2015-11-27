package de.hu_berlin.german.korpling.saltnpepper.pepperModules.coraXMLModules.tests;

import java.io.File;

import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporter;
import org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties;
import org.junit.Before;

public class CoraXMLImporterTest extends PepperImporterTest {

	String filePath = new File("").getAbsolutePath();
	
	@Before
	public void setUp(){
		
		setFixture(new CoraXMLImporter());
		getFixture().setProperties(new CoraXMLImporterProperties());
		
		filePath = filePath.concat("/src/test/resources/");
		
		FormatDesc formatDef= new FormatDesc();
		formatDef.setFormatName("coraXML");
		formatDef.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDef);
		
	}
	
	// TODO: test CustomizationProperties
	
}
