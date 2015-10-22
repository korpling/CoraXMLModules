/**
 * Copyright 2015 Humboldt-Universität zu Berlin
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
	 * Name of the property which defines whether mod and dipl are segmentations
	 * of token
	 */
	public static final String PROP_TOK_IS_SEG = "mod/dipl_eq_token";

	public CoraXMLImporterProperties() {
    this.addProperty(new PepperModuleProperty<>(PROP_TOKTEXT_MOD, String.class, "This property defines which attribute of the mod-Tag is used for the token texts (trans, utf or ascii). By default, the simplified ascii version is used (value:ascii)", ATT_ASCII, false));
    this.addProperty(new PepperModuleProperty<>(PROP_TOKTEXT_DIPL, String.class, "This property defines which attribute of the dipl-Tag is used for the token texts (trans or utf). By default, the unicode version is used (value:utf)", ATT_UTF, false));
    this.addProperty(new PepperModuleProperty<>(PROP_EXPORT_TOKEN, Boolean.class, "This property defines whether the token layer is exported. By default, it is exported (value:true)", Boolean.TRUE, false));
    this.addProperty(new PepperModuleProperty<>(PROP_TOK_IS_SEG, Boolean.class, "This property defines whether mod and dipl each are strict segmentations of token, i.e. the combined values of trans for each are equal with the trans-value of token. By default, this is not assumed (value:false)", Boolean.FALSE, false));


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
	 * Returns whether mod and dipl are only segmenations of token
	 *
	 * @return
	 */
	public boolean getTokenizationIsSegmentation() {
		return ((Boolean) this.getProperty(PROP_TOK_IS_SEG).getValue());
	}

}
