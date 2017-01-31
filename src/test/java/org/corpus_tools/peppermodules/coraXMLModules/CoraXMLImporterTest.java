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

import java.io.File;

import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporter;
import org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties;
import org.junit.Before;

public class CoraXMLImporterTest extends PepperImporterTest {

	String filePath = new File("").getAbsolutePath();

	@Before
	public void setUp() {

		setFixture(new CoraXMLImporter());
		getFixture().setProperties(new CoraXMLImporterProperties());

		filePath = filePath.concat("/src/test/resources/");

		FormatDesc formatDef = new FormatDesc();
		formatDef.setFormatName("coraXML");
		formatDef.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDef);

	}

	// TODO: test CustomizationProperties

}
