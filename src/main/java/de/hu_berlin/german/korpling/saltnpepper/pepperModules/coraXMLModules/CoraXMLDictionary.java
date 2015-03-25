/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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
/**
 * Dictionary of CoraXML
 * 
 * @author Florian Zipser
 *
 */
public interface CoraXMLDictionary {
	/** constant to address the xml-element 'layoutinfo'. **/
	public static final String TAG_LAYOUTINFO= "layoutinfo";
	/** constant to address the xml-element 'fm'. **/
	public static final String TAG_FM= "fm";
	/** constant to address the xml-element 'text'. **/
	public static final String TAG_TEXT= "text";
	/** constant to address the xml-element 'token'. **/
	public static final String TAG_TOKEN= "token";
	/** constant to address the xml-element 'page'. **/
	public static final String TAG_PAGE= "page";
	/** constant to address the xml-element 'shifttags'. **/
	public static final String TAG_SHIFTTAGS= "shifttags";
	/** constant to address the xml-element 'mod'. **/
	public static final String TAG_MOD= "mod";
	/** constant to address the xml-element 'dipl'. **/
	public static final String TAG_DIPL= "dipl";
	/** constant to address the xml-element 'column'. **/
	public static final String TAG_COLUMN= "column";
	/** constant to address the xml-element 'line'. **/
	public static final String TAG_LINE= "line";
	/** constant to address the xml-element 'comment'. **/
	public static final String TAG_COMMENT= "comment";
	/** constant to address the xml-element 'header'. **/
	public static final String TAG_HEADER= "header";
	/** constant to address the xml-element 'pos'. **/
	public static final String TAG_POS= "pos";

        public static final String TAG_POS_LEMMA="pos_gen";
        public static final String TAG_INFL = "infl";
        public static final String TAG_INFLCLASS = "inflClass";
        public static final String TAG_INFLCLASS_LEMMA = "inflClass_gen";

        // interne pos tags in Bonn XML zur korrektur
        public static final String TAG_POS_INT = "intern_pos";
        public static final String TAG_POS_INT_LEMMA = "intern_pos_gen";
        public static final String TAG_INFL_INT = "intern_infl";

	/** constant to address the xml-element 'lemma'. **/
	public static final String TAG_LEMMA= "lemma";
        public static final String TAG_LEMMA_ID = "lemma_idmwb";

	/** constant to address the xml-element 'morph'. **/
	public static final String TAG_MORPH= "morph";
	/** constant to address the xml-element 'suggestions'. **/
	public static final String TAG_SUGGESTIONS= "suggestions";

        public static final String TAG_NORM = "norm";
        public static final String TAG_NORMBROAD = "norm_broad";
        public static final String TAG_NORMALIGN = "norm_align";
        public static final String TAG_NORMALIGN_VARIANT = "grapho";


        public static final String ATT_SIMPLE="simple";
	/** constant to address the xml-attribute 'ascii'. **/
	public static final String ATT_ASCII= "ascii";
	/** constant to address the xml-attribute 'id'. **/
	public static final String ATT_ID= "id";
	/** constant to address the xml-attribute 'trans'. **/
	public static final String ATT_TRANS= "trans";
	/** constant to address the xml-attribute 'range'. **/
	public static final String ATT_RANGE= "range";
	/** constant to address the xml-attribute 'side'. **/
	public static final String ATT_SIDE= "side";
	/** constant to address the xml-attribute 'no'. **/
	public static final String ATT_NO= "no";
	/** constant to address the xml-attribute 'name'. **/
	public static final String ATT_NAME= "name";
	/** constant to address the xml-attribute 'utf'. **/
	public static final String ATT_UTF= "utf";
	/** constant to address the xml-attribute 'type'. **/
	public static final String ATT_TYPE= "type";
	/** constant to address the xml-attribute 'tag'. **/
	public static final String ATT_TAG= "tag";
	/** constant to address the xml-attribute 'score'. **/
	public static final String ATT_SCORE= "score";
	
}
