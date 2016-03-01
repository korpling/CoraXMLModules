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

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

/**
 * Defines the properties to be used for the {@link CoraXMLImporter}.
 *
 * @author Fabian Barteld
 *
 */
public class CoraXMLImporterProperties extends PepperModuleProperties implements CoraXMLDictionary {

	public static final String PREFIX_TOKTEXT = "toktext.";
	public static final String PREFIX_EXPORT = "export.";

	/**
	 * Name of properties which set the feature used for the token text.
	 */
	public static final String PROP_TOKTEXT_MOD = PREFIX_TOKTEXT + "mod";
	public static final String PROP_TOKTEXT_DIPL = PREFIX_TOKTEXT + "dipl";

	/**
	 * Name of properties which define whether a layer is exported.
	 */
	public static final String PROP_EXPORT_TOKEN = PREFIX_EXPORT + "token";
	
	/**
	 * Property which defines to which layer top-level comments are added
	 */
	public static final String PROP_COMMENT_LAYER_NAME = PREFIX_EXPORT + "layer_for_comments";
	
	/**
	 * Property which defines whether subtoken annotations are created from transcription markup.
	 */
	public static final String PROP_EXPORT_SUBTOKEN = PREFIX_EXPORT + "subtoken_markup";

	/**
	 * Name of the property which defines whether mod and dipl are segmentations
	 * of token
	 */
	public static final String PROP_TOK_IS_SEG = "mod/dipl_eq_token";


	/**
	 * Property that excludes annotations form being imported
	 */
	public static final String PROP_EXCLUDE_ANNOTATIONS = "exclude_from_import";

	public CoraXMLImporterProperties() {
		this.addProperty(new PepperModuleProperty<>(PROP_TOKTEXT_MOD, String.class, "This property defines which attribute of the mod-Tag is used for the token texts (trans, utf or ascii). By default, the simplified ascii version is used (value:ascii)", ATT_ASCII, false));
		this.addProperty(new PepperModuleProperty<>(PROP_TOKTEXT_DIPL, String.class, "This property defines which attribute of the dipl-Tag is used for the token texts (trans or utf). By default, the unicode version is used (value:utf)", ATT_UTF, false));
		this.addProperty(new PepperModuleProperty<>(PROP_EXPORT_TOKEN, Boolean.class, "This property defines whether the token layer is exported. By default, it is exported (value:true)", Boolean.TRUE, false));
		this.addProperty(new PepperModuleProperty<>(PROP_COMMENT_LAYER_NAME, String.class, "This property defines the layer to which top-level comments are exported. By default, they are not exported (value:\"\")", "", false));
		this.addProperty(new PepperModuleProperty<>(PROP_EXPORT_SUBTOKEN, String.class, "This property defines whether markup in the transcription is turned into subtoken-annotations. The content of the string gives the name of the markup rules used. By default, no subtoken-annotations are created (value:\"\")", "", false));
		this.addProperty(new PepperModuleProperty<>(PROP_TOK_IS_SEG, Boolean.class, "This property defines whether mod and dipl each are strict segmentations of token, i.e. the combined values of trans for each are equal with the trans-value of token. By default, this is not assumed (value:false)", Boolean.FALSE, false));
		this.addProperty(new PepperModuleProperty<>(PROP_EXCLUDE_ANNOTATIONS, String.class, "This property defines a semicolon separated list of annotations that are ignored. By default all annotations are imported (value:\"\").", "", false));

	}

	public String getExcludeAnnotations() {
		return ((String) this.getProperty(PROP_EXCLUDE_ANNOTATIONS).getValue());
	}

	/**
	 * Returns the attribute to be used for the text of the dipl-Token
	 *
	 * @return
	 */
	public String getDiplTokTextlayer() {
		return ((String) this.getProperty(PROP_TOKTEXT_DIPL).getValue());
	}

	/**
	 * Returns the attribute to be used for the text of the mod-Token
	 *
	 * @return
	 */
	public String getModTokTextlayer() {
		return ((String) this.getProperty(PROP_TOKTEXT_MOD).getValue());
	}

	/**
	 * Returns whether the token-layer should be exported
	 *
	 * @return
	 */
	public boolean getExportTokenLayer() {
		return ((Boolean) this.getProperty(PROP_EXPORT_TOKEN).getValue());
	}

	/**
	 * Returns the name of the layer to which top-level comments are added
	 *
	 * @return
	 */
	public String getExportCommentsToLayer() {
		return ((String) this.getProperty(PROP_COMMENT_LAYER_NAME).getValue());
	}


	/**
	 * Returns whether subtoken annotations should be exported
	 *
	 * @return
	 */
	public String getExportSubtokenannotation() {
		return ((String) this.getProperty(PROP_EXPORT_SUBTOKEN).getValue());
	}


	/**
	 * Returns whether mod and dipl are only segmenations of token
	 *
	 * @return
	 */
	public boolean getTokenizationIsSegmentation() {
		return ((Boolean) this.getProperty(PROP_TOK_IS_SEG).getValue());
	}

}
