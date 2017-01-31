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
