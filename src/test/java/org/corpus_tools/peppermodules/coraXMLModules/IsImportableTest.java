package org.corpus_tools.peppermodules.coraXMLModules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.corpus_tools.pepper.testFramework.PepperTestUtil;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class IsImportableTest {

	private static Double VALUE_0 = Double.valueOf(0.0);
	private static Double VALUE_1 = Double.valueOf(1.0);

	private CoraXMLImporter fixture;

	public CoraXMLImporter getFixture() {
		return fixture;
	}

	public void setFixture(CoraXMLImporter fixture) {
		this.fixture = fixture;
	}

	@Before
	public void beforeEach() {
		setFixture(new CoraXMLImporter());
	}

	public static String getTestResources() {
		return (PepperTestUtil.getTestResources() + "isImportable/");
	}

	@Test
	public void whenCorpusPathContainsNoCoraXMLFiles_thenReturn0() {
		URI corpusPath = URI.createFileURI(getTestResources() + "noCora/");
		assertThat(getFixture().isImportable(corpusPath)).isEqualTo(VALUE_0);
	}

	@Test
	public void whenCorpusPathContainsNoFilesWithCoraXMLEnding_thenReturn0() {
		URI corpusPath = URI.createFileURI(getTestResources() + "fakeCora/");
		assertThat(getFixture().isImportable(corpusPath)).isEqualTo(VALUE_0);
	}

	@Test
	public void whenCorpusPathContainsOnlyCoraXMLFiles_thenReturn1() {
		URI corpusPath = URI.createFileURI(getTestResources() + "onlyCora/");
		assertThat(getFixture().isImportable(corpusPath)).isEqualTo(VALUE_1);
	}

	@Test
	public void whenCorpusPathContainsCoraXMLAndNoneCoraXMLFiles_thenReturn1() {
		URI corpusPath = URI.createFileURI(getTestResources() + "mixedContent/");
		assertThat(getFixture().isImportable(corpusPath)).isEqualTo(VALUE_1);
	}
}
