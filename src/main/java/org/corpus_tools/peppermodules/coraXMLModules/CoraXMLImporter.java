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

import static org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties.PROP_COMMENT_LAYER_NAME;
import static org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties.PROP_EXPORT_SUBTOKEN;
import static org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties.PROP_TOKTEXT_MOD;
import static org.corpus_tools.peppermodules.coraXMLModules.CoraXMLImporterProperties.PROP_TOK_IS_SEG;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.core.SelfTestDesc;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

/**
 * This importer imports data from the CoraXML format.
 *
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name = "CoraXMLImporterComponent", factory = "PepperImporterComponentFactory")
public class CoraXMLImporter extends PepperImporterImpl implements PepperImporter, CoraXMLDictionary {
	public static final String MODULE_NAME = "CoraXMLImporter";
	private static final Pattern CORAXML_PATTERN1 = Pattern.compile("<?xml version=(\"|')1[.]0(\"|')");
	private static final Pattern CORAXML_PATTERN2 = Pattern.compile("<cora-header");
	public static final String FORMAT_NAME = "coraXML";
	public static final String FORMAT_VERSION = "1.0";
	// ** customization properties */
	private String mod_tok_textlayer = ATT_ASCII;
	private String dipl_tok_textlayer = ATT_UTF;
	private String tok_anno = TAG_MOD;
	private String tok_dipl = TAG_DIPL;
	private boolean export_token_layer = true;
	private String comment_layer_name = "";
	private String export_subtoken_annotation = "";
	private boolean tokenization_is_segmentation = false;
	private String annotations_to_exclude = "";
	private String boundary_tags = TAG_BOUNDARY;

	// =================================================== mandatory
	// ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong>
	 *
	 * A constructor for your module. Set the coordinates, with which your
	 * module shall be registered. The coordinates (modules name, version and
	 * supported formats) are a kind of a fingerprint, which should make your
	 * module unique.
	 */
	public CoraXMLImporter() {
		super();
		setProperties(new CoraXMLImporterProperties());
		setName(MODULE_NAME);
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-CoraXMLModules"));
		setDesc("This importer transforms data in cora xml format to a Salt model. ");
		setVersion(FORMAT_VERSION);
		addSupportedFormat(FORMAT_NAME, FORMAT_VERSION, null);
		getDocumentEndings().add(PepperImporter.ENDING_XML);
	}

	public PepperMapper createPepperMapper(Identifier Identifier) {
		CoraXML2SaltMapper mapper = new CoraXML2SaltMapper();
		mapper.setModTokTextlayer(mod_tok_textlayer);
		mapper.setDiplTokTextlayer(dipl_tok_textlayer);
		mapper.setExportTokenLayer(export_token_layer);
		mapper.setExportCommentsToLayer(comment_layer_name);
		mapper.setExportSubtokenannotation(export_subtoken_annotation);
		mapper.setTokenizationIsSegmentation(tokenization_is_segmentation);
		mapper.setExcludeAnnotations(annotations_to_exclude);
		mapper.setBoundaryAnnotations(boundary_tags);
		mapper.setTokNames(tok_anno, tok_dipl);
		return (mapper);
	}

	@Override
	public Double isImportable(URI corpusPath) {
		Double retValue = 0.0;
		for (String content : sampleFileContent(corpusPath, PepperImporter.ENDING_XML)) {
			Matcher matcher = CORAXML_PATTERN1.matcher(content);
			Matcher matcher2 = CORAXML_PATTERN2.matcher(content);
			if (matcher.find() && matcher2.find()) {
				retValue = 1.0;
				break;
			}
		}
		return retValue;
	}

	@Override
	public SelfTestDesc getSelfTestDesc() {
		final PepperModuleProperties props = getProperties();
		props.setPropertyValue(PROP_EXPORT_SUBTOKEN, "REN");
		props.setPropertyValue(PROP_TOKTEXT_MOD, "utf");
		props.setPropertyValue(PROP_TOK_IS_SEG, true);
		props.setPropertyValue(PROP_COMMENT_LAYER_NAME, "token");
		final URI inputPath = getResources().appendSegment("selfTests").appendSegment("coraXmlImporter")
				.appendSegment("in").appendSegment("ren");
		final URI expectedPath = getResources().appendSegment("selfTests").appendSegment("coraXmlImporter")
				.appendSegment("expected");
		return new SelfTestDesc.Builder().withInputCorpusPath(inputPath).withExpectedCorpusPath(expectedPath).build();
	}

	// =================================================== optional
	// ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong>
	 *
	 * This method is called by the pepper framework after initializing this
	 * object and directly before start processing. Initializing means setting
	 * properties {@link PepperModuleProperties}, setting temprorary files,
	 * resources etc. . returns false or throws an exception in case of
	 * {@link PepperModule} instance is not ready for any reason.
	 *
	 * @return false, {@link PepperModule} instance is not ready for any reason,
	 *         true, else.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException {
		if (this.getProperties() != null) {
			mod_tok_textlayer = ((CoraXMLImporterProperties) this.getProperties()).getModTokTextlayer();
			dipl_tok_textlayer = ((CoraXMLImporterProperties) this.getProperties()).getDiplTokTextlayer();
			export_token_layer = ((CoraXMLImporterProperties) this.getProperties()).getExportTokenLayer();
			comment_layer_name = ((CoraXMLImporterProperties) this.getProperties()).getExportCommentsToLayer();
			export_subtoken_annotation = ((CoraXMLImporterProperties) this.getProperties())
					.getExportSubtokenannotation();
			tokenization_is_segmentation = ((CoraXMLImporterProperties) this.getProperties())
					.getTokenizationIsSegmentation();
			annotations_to_exclude = ((CoraXMLImporterProperties) this.getProperties()).getExcludeAnnotations();
			boundary_tags = ((CoraXMLImporterProperties) this.getProperties()).getBoundaryAnnotations();
			tok_anno = ((CoraXMLImporterProperties) this.getProperties()).getTokName(TAG_MOD);
			tok_dipl = ((CoraXMLImporterProperties) this.getProperties()).getTokName(TAG_DIPL);

		}
		// TODO make some initializations if necessary
		return (super.isReadyToStart());
	}
}
