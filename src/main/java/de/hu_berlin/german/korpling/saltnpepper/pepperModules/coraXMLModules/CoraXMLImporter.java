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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.coraXMLModules;

import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModule;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleNotReadyException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * This importer imports data from the CoraXML format.
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="CoraXMLImporterComponent", factory="PepperImporterComponentFactory")
public class CoraXMLImporter extends PepperImporterImpl implements PepperImporter, CoraXMLDictionary
{

    //** customization properties */
    private String mod_tok_textlayer = ATT_ASCII;
    private String dipl_tok_textlayer = ATT_UTF;
    private boolean export_token_layer = true;
    private boolean tokenization_is_segmentation = false;

// =================================================== mandatory ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong>
	 * 
	 * A constructor for your module. Set the coordinates, with which your module shall be registered. 
	 * The coordinates (modules name, version and supported formats) are a kind of a fingerprint, 
	 * which should make your module unique.
	 */
	public CoraXMLImporter()
	{
		super();
		setProperties(new CoraXMLImporterProperties());
		this.setName("CoraXMLImporter");
		setSupplierContact(URI.createURI("saltnpepper@lists.hu-berlin.de"));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-CoraXMLModules"));
		setDesc("This importer transforms data in cora xml format to a Salt model. ");
		this.setVersion("1.0");
		this.addSupportedFormat("coraXML", "1.0", null);
		this.getSDocumentEndings().add(PepperImporter.ENDING_XML);
	}
	
	public PepperMapper createPepperMapper(SElementId sElementId)
	{
		CoraXML2SaltMapper mapper = new CoraXML2SaltMapper();
		mapper.setModTokTextlayer(mod_tok_textlayer);
		mapper.setDiplTokTextlayer(dipl_tok_textlayer);
		mapper.setExportTokenLayer(export_token_layer);
		mapper.setTokenizationIsSegmentation(tokenization_is_segmentation);
		return(mapper);
	}
	
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong>
	 * 
	 * This method is called by the pepper framework and returns if a corpus located at the given {@link URI} is importable
	 * by this importer. If yes, 1 must be returned, if no 0 must be returned. If it is not quite sure, if the given corpus
	 * is importable by this importer any value between 0 and 1 can be returned. If this method is not overridden, 
	 * null is returned.
	 * @return 1 if corpus is importable, 0 if corpus is not importable, 0 < X < 1, if no definitiv answer is possible,  null if method is not overridden 
	 */
	public Double isImportable(URI corpusPath)
	{
		//TODO some code to analyze the given corpus-structure
		return(null);
	}

// =================================================== optional ===================================================	
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong>
	 * 
	 * This method is called by the pepper framework after initializing this object and directly before start processing. 
	 * Initializing means setting properties {@link PepperModuleProperties}, setting temprorary files, resources etc. .
	 * returns false or throws an exception in case of {@link PepperModule} instance is not ready for any reason.
	 * @return false, {@link PepperModule} instance is not ready for any reason, true, else.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException
	{
	    if (this.getProperties() != null) {
		mod_tok_textlayer = ((CoraXMLImporterProperties) this.getProperties()).getModTokTextlayer();
		dipl_tok_textlayer = ((CoraXMLImporterProperties) this.getProperties()).getDiplTokTextlayer();
		export_token_layer = ((CoraXMLImporterProperties) this.getProperties()).getExportTokenLayer();
		tokenization_is_segmentation = ((CoraXMLImporterProperties) this.getProperties()).getTokenizationIsSegmentation();


	    }
		//TODO make some initializations if necessary
		return(super.isReadyToStart());
	}
}
