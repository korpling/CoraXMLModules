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
import static org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties.PROP_COMMENT_LAYER_NAME;
import static org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties.PROP_EXPORT_SUBTOKEN;
import static org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties.PROP_TOKTEXT_MOD;
import static org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties.PROP_TOK_IS_SEG;

import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.common.ModuleFitness;
import org.corpus_tools.pepper.common.ModuleFitness.FitnessFeature;
import org.corpus_tools.pepper.core.ModuleFitnessChecker;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.pepper.testFramework.PepperTestUtil;
import org.junit.Before;
import org.junit.Test;

public class SelfTestTest extends PepperImporterTest {

	@Before
	public void beforeEach() {
		super.setFixture(new CoraXMLImporter());
		this.addSupportedFormat(new FormatDesc.FormatDescBuilder().withName(CoraXMLImporter.FORMAT_NAME)
				.withVersion(CoraXMLImporter.FORMAT_VERSION).build());
	}

	@Test
	public void whenSelfTestingModule_thenResultShouldBeTrue() {
		// GIVEN
		CoraXMLImporterProperties props = (CoraXMLImporterProperties) fixture.getProperties();
		props.setPropertyValue(PROP_EXPORT_SUBTOKEN, "REN");
		props.setPropertyValue(PROP_TOKTEXT_MOD, "utf");
		props.setPropertyValue(PROP_TOK_IS_SEG, true);
		props.setPropertyValue(PROP_COMMENT_LAYER_NAME, "token");

		// WHEN
		final ModuleFitness fitness = new ModuleFitnessChecker(PepperTestUtil.createDefaultPepper()).selfTest(fixture);

		// THEN
		assertThat(fitness.getFitness(FitnessFeature.HAS_SELFTEST)).isTrue();
		assertThat(fitness.getFitness(FitnessFeature.HAS_PASSED_SELFTEST)).isTrue();
		assertThat(fitness.getFitness(FitnessFeature.IS_IMPORTABLE_SEFTEST_DATA)).isTrue();
		assertThat(fitness.getFitness(FitnessFeature.IS_VALID_SELFTEST_DATA)).isTrue();
	}
}
