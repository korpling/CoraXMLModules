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
	 * Name of CoraXML mod/dipl elements
	 */
	public static final String PROP_TOK_MOD = TAG_MOD;
	public static final String PROP_TOK_DIPL = TAG_DIPL;

	/**
	 * Name of properties which defines the prefix for mod and dipl layers.
	 */
	public static final String PROP_TOK_PREFIX = "tok.prefix";

	/**
	 * Name of properties which define whether a layer is exported.
	 */
	public static final String PROP_EXPORT_TOKEN = PREFIX_EXPORT + "token";

	/**
	 * Property which defines whether a reference annotation containing page and column information instead of lines.
	 */
	public static final String PROP_CREATE_REFERENCE_SPAN = PREFIX_EXPORT + "create_reference";

	/**
	 * Property which defines to which layer top-level comments are added
	 */
	public static final String PROP_COMMENT_LAYER_NAME = PREFIX_EXPORT + "layer_for_comments";

	/**
	 * Property which defines whether subtoken annotations are created from transcription markup.
	 */
	public static final String PROP_EXPORT_SUBTOKEN = PREFIX_EXPORT + "subtoken_markup";

	/**
	 * Property that marks tags as boundary annotations which mapped to spans
	 */
	public static final String PROP_BOUNDARY_ANNOTATIONS = PREFIX_EXPORT + "boundary_tags";

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
		this.addProperty(new PepperModuleProperty<>(PROP_CREATE_REFERENCE_SPAN, Boolean.class, "This property defines whether a reference span is created for lines. By default, it is not created (unless lines have a \"loc\"-attribute) (value:false)", Boolean.FALSE, false));
		this.addProperty(new PepperModuleProperty<>(PROP_COMMENT_LAYER_NAME, String.class, "This property defines the layer to which top-level comments are exported. By default, they are not exported (value:\"\")", "", false));
		this.addProperty(new PepperModuleProperty<>(PROP_EXPORT_SUBTOKEN, String.class, "This property defines whether markup in the transcription is turned into subtoken-annotations. The content of the string gives the name of the markup rules used. By default, no subtoken-annotations are created (value:\"\")", "", false));
		this.addProperty(new PepperModuleProperty<>(PROP_TOK_IS_SEG, Boolean.class, "This property defines whether mod and dipl each are strict segmentations of token, i.e. the combined values of trans for each are equal with the trans-value of token. By default, this is not assumed (value:false)", Boolean.FALSE, false));
		this.addProperty(new PepperModuleProperty<>(PROP_EXCLUDE_ANNOTATIONS, String.class, "This property defines a semicolon separated list of annotations that are ignored. By default all annotations are imported (value:\"\").", "", false));
		this.addProperty(new PepperModuleProperty<>(PROP_BOUNDARY_ANNOTATIONS, String.class, "This property defines a semicolon separated list of annotations that are treated as boundary annotatotions. By default this is the tag boundary (value:boundary)", TAG_BOUNDARY, false));
		this.addProperty(new PepperModuleProperty<>(PROP_TOK_MOD , String.class, "Name of the element in CoraXML containing the 'mod-token'", TAG_MOD, false));
		this.addProperty(new PepperModuleProperty<>(PROP_TOK_DIPL, String.class, "Name of the element in CoraXML containing the 'dipl-token'", TAG_DIPL, false));
		this.addProperty(new PepperModuleProperty<>(PROP_TOK_PREFIX, String.class, "This property defines a prefix that is attached to 'dipl-token' and 'mod-token' output layers.", "", false));
	}

	public String getExcludeAnnotations() {
		return ((String) this.getProperty(PROP_EXCLUDE_ANNOTATIONS).getValue());
	}

        public String getTokName(String type) {
            if (TAG_MOD.equals(type))
                return ((String) this.getProperty(PROP_TOK_MOD).getValue());
            else if (TAG_DIPL.equals(type))
                return ((String) this.getProperty(PROP_TOK_DIPL).getValue());
            else
                throw new Error("argument must be either " + TAG_DIPL + " or " + TAG_MOD);
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
	 * Returns the attribute to be used as prefix for mod- and dipl-Tokens
	 *
	 * @return
	 */
	public String getTokLayerPrefix() {
		return ((String) this.getProperty(PROP_TOK_PREFIX).getValue());
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
	 * Returns whether a reference span should be created
	 *
	 * @return
	 */
	public boolean getCreateReferenceSpan() {
		return ((Boolean) this.getProperty(PROP_CREATE_REFERENCE_SPAN).getValue());
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

	/**
	 * Returns the tags that are treated as boundaries
	 *
	 * @return
	 */
	public String getBoundaryAnnotations() {
		return ((String) this.getProperty(PROP_BOUNDARY_ANNOTATIONS).getValue());
	}

}
