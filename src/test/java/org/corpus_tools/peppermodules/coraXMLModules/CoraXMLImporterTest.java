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

import java.io.File;

import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.common.ModuleFitness;
import org.corpus_tools.pepper.common.ModuleFitness.FitnessFeature;
import org.corpus_tools.pepper.core.ModuleFitnessChecker;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.pepper.testFramework.PepperTestUtil;
import org.junit.Before;
import org.junit.Test;

public class CoraXMLImporterTest extends PepperImporterTest {

	String filePath = new File("").getAbsolutePath();

	@Before
	public void setUp() {
		super.setFixture(new CoraXMLImporter());
		this.addSupportedFormat(new FormatDesc.FormatDescBuilder().withName(CoraXMLImporter.FORMAT_NAME)
				.withVersion(CoraXMLImporter.FORMAT_VERSION).build());
	}

	@Test
	public void whenSimulatingFitnessCheck_thenModuleMustPassSelfTest() {
		// WHEN
		final ModuleFitness fitness = new ModuleFitnessChecker(PepperTestUtil.createDefaultPepper()).selfTest(fixture);

		// THEN
		assertThat(fitness.getFitness(FitnessFeature.HAS_SELFTEST)).isTrue();
		assertThat(fitness.getFitness(FitnessFeature.HAS_PASSED_SELFTEST)).isTrue();
		assertThat(fitness.getFitness(FitnessFeature.IS_IMPORTABLE_SEFTEST_DATA)).isTrue();
		assertThat(fitness.getFitness(FitnessFeature.IS_VALID_SELFTEST_DATA)).isTrue();
	}

}
