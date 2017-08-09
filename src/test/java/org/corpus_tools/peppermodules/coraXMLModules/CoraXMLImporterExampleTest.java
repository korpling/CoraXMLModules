package org.corpus_tools.peppermodules.coraXMLModules;

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
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SaltProject;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public abstract class CoraXMLImporterExampleTest {
	
	public abstract String getSampleName();
	
	private String filePath = new File("").getAbsolutePath();
	
	private CoraXMLImporter fixture = null;
	
	private SCorpusGraph goldCorpusGraph;
	private SCorpusGraph importedCorpusGraph;
	
	public void setFixture(CoraXMLImporter fixture) {
		this.fixture = fixture;

	}
	
	public CoraXMLImporter getFixture() {
		return fixture;
	}

	@Before
	public void setUp() {
		setFixture(new CoraXMLImporter());

		getFixture().setSaltProject(SaltFactory.createSaltProject());
		getFixture().getSaltProject().addCorpusGraph(SaltFactory.createSCorpusGraph());

		getFixture().setProperties(new CoraXMLImporterProperties());

		filePath = filePath.concat("/src/test/resources/sample_" + this.getSampleName() + "/");

		File pepperParams = new File(filePath.concat("pepper.params"));
		getFixture().getProperties().addProperties(URI.createFileURI(pepperParams.getAbsolutePath()));

		File coraDir = new File(filePath.concat("cora/" + this.getSampleName() + "/"));
		File saltDir = new File(filePath.concat("salt/"));

		this.getFixture().setCorpusDesc(new CorpusDesc());
		getFixture().getCorpusDesc().setCorpusPath(URI.createFileURI(coraDir.getAbsolutePath()));
		getFixture().getCorpusDesc().setFormatDesc(new FormatDesc());
		getFixture().getCorpusDesc().getFormatDesc().setFormatName("coraXML").setFormatVersion("1.0");

		// import CorpusGraph
		this.importedCorpusGraph = SaltFactory.createSCorpusGraph();

		getFixture().getSaltProject().addCorpusGraph(importedCorpusGraph);
		getFixture().importCorpusStructure(importedCorpusGraph);

		this.start();

		this.importedCorpusGraph = getFixture().getCorpusGraph();

		// load gold CorpusGraph
		SaltProject goldProject = SaltFactory.createSaltProject();
		goldProject.loadSaltProject(URI.createFileURI(saltDir.getAbsolutePath()));
		this.goldCorpusGraph = goldProject.getCorpusGraphs().get(0);
	}

	@Test
	public void testExampleCorpus() {

		// compare number of Documents
		assertEquals(goldCorpusGraph.getDocuments().size(), importedCorpusGraph.getDocuments().size());

		// compare all Documents
		for (int i = 0; i < importedCorpusGraph.getDocuments().size(); i++) {
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
