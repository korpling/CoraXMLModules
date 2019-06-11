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
package org.corpus_tools.peppermodules.coraXMLModules;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.pepper.testFramework.PepperTestUtil;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class CoraXMLImporterTest extends PepperImporterTest {

	@Before
	public void setUp() {
		super.setFixture(new CoraXMLImporter());
		getFixture().setProperties(new CoraXMLImporterProperties());

		FormatDesc formatDef = new FormatDesc();
		formatDef.setFormatName("coraXML");
		formatDef.setFormatVersion("1.0");
		this.addFormatWhichShouldBeSupported(formatDef);
	}

	public static String getTestResources() {
		return (PepperTestUtil.getTestResources() + "CoraXMLImporter/");
	}

	protected void loadCorpus(URI corpusPath) {
		getFixture().setCorpusDesc(new CorpusDesc());
		getFixture().getCorpusDesc().setCorpusPath(corpusPath).setFormatDesc(new FormatDesc());
		getFixture().getCorpusDesc().getFormatDesc().setFormatName("coraXML").setFormatVersion("1.0");
		this.start();
	}

	protected Map<String,String> getImportedMetaAnnotations() {
		Set<SMetaAnnotation> impHeader = getFixture().getCorpusGraph().getDocuments().get(0).getMetaAnnotations();
		Map<String,String> mannos = new HashMap<>();

		for (SMetaAnnotation meta: impHeader) {
			mannos.put(meta.getName(), meta.getValue_STEXT());
		}

		return mannos;
	}

	protected Map<String,String> getExpectedMetaAnnotations() {
		Map<String,String> mannos = new HashMap<>();
		mannos.put("Test1", "Value1");
		mannos.put("Test2", "Value2");
		return mannos;
	}

	protected void testHeader(String name) {
		URI corpusPath = URI.createFileURI(getTestResources() + name + "/");
		loadCorpus(corpusPath);
		assertEquals(this.getImportedMetaAnnotations(), this.getExpectedMetaAnnotations());
	}

	@Test
	public void headerContainsText() {
		this.testHeader("textHeader");
	}

	@Test
	public void headerContainsXML() {
		this.testHeader("xmlHeader");
	}


}
