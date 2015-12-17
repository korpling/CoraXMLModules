/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.coraXMLModules.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.testFramework.PepperTestUtil;
import org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporter;
import org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class CoraXMLImporterRENTest {

	private CoraXMLImporter fixture = null;
	
	String filePath = new File("").getAbsolutePath();
	
	public CoraXMLImporter getFixture() {
		return fixture;
	}

	public void setFixture(CoraXMLImporter fixture) {
		this.fixture = fixture;

	}
	
	@Before
	public void setUp(){
		setFixture(new CoraXMLImporter());

		getFixture().setSaltProject(SaltFactory.createSaltProject());
		getFixture().getSaltProject().addCorpusGraph(SaltFactory.createSCorpusGraph());

		getFixture().setProperties(new CoraXMLImporterProperties());
		
		filePath = filePath.concat("/src/test/resources/sample_ren/");
		
		File pepperParams = new File(filePath.concat("pepper.params"));
		getFixture().getProperties().addProperties(URI.createFileURI(pepperParams.getAbsolutePath()));
		
	}
	

	/**
	 * Test import against Gold import
	 */
	@Test
	public void testExampleCorpus() {
		
		File coraDir = new File(filePath.concat("cora/ren/"));
		File saltDir = new File(filePath.concat("salt/"));
		
		this.getFixture().setCorpusDesc(new CorpusDesc());
		getFixture().getCorpusDesc().setCorpusPath(URI.createFileURI(coraDir.getAbsolutePath()));
		getFixture().getCorpusDesc().setFormatDesc(new FormatDesc());
		getFixture().getCorpusDesc().getFormatDesc().setFormatName("coraXML").setFormatVersion("1.0");
						
		// import CorpusGraph
		SCorpusGraph importedCorpusGraph = SaltFactory.createSCorpusGraph(); 
		
		getFixture().getSaltProject().addCorpusGraph(importedCorpusGraph);
		getFixture().importCorpusStructure(importedCorpusGraph);
		
		this.start();
		
		importedCorpusGraph = getFixture().getCorpusGraph();
		
		// load gold CorpusGraph
		SaltProject goldProject = SaltFactory.createSaltProject();
		goldProject.loadSaltProject(URI.createFileURI(saltDir.getAbsolutePath()));
		SCorpusGraph goldCorpusGraph = goldProject.getCorpusGraphs().get(0);
		
		// compare number of Documents
		assertEquals(goldCorpusGraph.getDocuments().size(), importedCorpusGraph.getDocuments().size());
				
		// compare all Documents
		for (int i=0; i < importedCorpusGraph.getDocuments().size(); i++) {
			SDocument goldDoc = goldCorpusGraph.getDocuments().get(i);
			SDocument impDoc = importedCorpusGraph.getDocument(goldDoc.getIdentifier());
			assertNotNull(impDoc);
			assertTrue(goldDoc.getDocumentGraph().isIsomorph(impDoc.getDocumentGraph()));			
		}
		
	}


	public void start() {
		Collection<PepperModule> modules = new ArrayList<PepperModule>();
		modules.add(getFixture());
		PepperTestUtil.start(modules);
	}
	
}
