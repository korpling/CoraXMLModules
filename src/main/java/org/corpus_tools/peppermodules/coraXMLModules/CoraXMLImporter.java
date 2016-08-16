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

import org.corpus_tools.pepper.common.PepperConfiguration;
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
	// ** customization properties */
	private String mod_tok_textlayer = ATT_ASCII;
	private String dipl_tok_textlayer = ATT_UTF;
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
		setVersion("1.0");
		addSupportedFormat("coraXML", "1.0", null);
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
		return (mapper);
	}

	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong>
	 * 
	 * This method is called by the pepper framework and returns if a corpus
	 * located at the given {@link URI} is importable by this importer. If yes,
	 * 1 must be returned, if no 0 must be returned. If it is not quite sure, if
	 * the given corpus is importable by this importer any value between 0 and 1
	 * can be returned. If this method is not overridden, null is returned.
	 * 
	 * @return 1 if corpus is importable, 0 if corpus is not importable, 0 < X <
	 *         1, if no definitiv answer is possible, null if method is not
	 *         overridden
	 */
	public Double isImportable(URI corpusPath) {
		// TODO some code to analyze the given corpus-structure
		return (null);
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
			export_subtoken_annotation = ((CoraXMLImporterProperties) this.getProperties()).getExportSubtokenannotation();
			tokenization_is_segmentation = ((CoraXMLImporterProperties) this.getProperties()).getTokenizationIsSegmentation();
			annotations_to_exclude = ((CoraXMLImporterProperties) this.getProperties()).getExcludeAnnotations();
			boundary_tags = ((CoraXMLImporterProperties) this.getProperties()).getBoundaryAnnotations();

		}
		// TODO make some initializations if necessary
		return (super.isReadyToStart());
	}
}
