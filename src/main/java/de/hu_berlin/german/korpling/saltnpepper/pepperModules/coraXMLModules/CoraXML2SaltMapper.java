/** Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
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

import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang3.StringEscapeUtils;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class CoraXML2SaltMapper extends PepperMapperImpl implements PepperMapper, CoraXMLDictionary {

	/** defines which textual representation is used for the dipl and mod tokens **/
	private String mod_tok_textlayer = ATT_ASCII; // one of "trans", "utf" and
													// "ascii"
	private String dipl_tok_textlayer = ATT_UTF; // one of "trans" and "utf"

	/** defines whether the token layer should be exported **/
	private boolean exportTokenLayer = true;

	/** defines whether dipl and mod are only segmentations of token **/
	private boolean tokenization_is_segmentation = false;

	/**
	 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
	 *
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (this.getDocument().getDocumentGraph() == null) {
			this.getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		}
		CoraXMLReader reader = new CoraXMLReader();
		this.readXMLResource(reader, this.getResourceURI());

		return (DOCUMENT_STATUS.COMPLETED);
	}

	class CoraXMLReader extends DefaultHandler2 {
		/** stores start for line **/
		Hashtable<String, SSpan> lineStart = new Hashtable<String, SSpan>();
		/** stores start for line **/
		Hashtable<String, SSpan> lineEnd = new Hashtable<String, SSpan>();

		/** stores start for column **/
		Hashtable<String, SSpan> columnStart = new Hashtable<String, SSpan>();
		/** stores start for column **/
		Hashtable<String, SSpan> columnEnd = new Hashtable<String, SSpan>();

		/** stores start for page **/
		Hashtable<String, SSpan> pageStart = new Hashtable<String, SSpan>();
		/** stores start for page **/
		Hashtable<String, SSpan> pageEnd = new Hashtable<String, SSpan>();

		private List<SToken> openDipls = null;

		/**
		 * returns all open {@link CoraXMLDictionary#TAG_DIPL} which are related
		 * to a {@link SSpan} and are not connected with {@link SToken} objects
		 * so far
		 **/
		private List<SToken> getOpenDipls() {
			if (openDipls == null)
				openDipls = new Vector<SToken>();
			return (openDipls);
		}

		private void resetOpenDipls() {
			openDipls = null;
		}

		private List<SToken> openMods = null;

		/**
		 * returns all open {@link CoraXMLDictionary#TAG_MOD} which are related
		 * to a {@link SSpan} and are not connected with {@link SToken} objects
		 * so far
		 **/
		private List<SToken> getOpenMods() {
			if (openMods == null)
				openMods = new Vector<SToken>();
			return (openMods);
		}

		private void resetOpenMods() {
			openMods = null;
		}

		/** stores offsets of dipl-Tokens in the current token **/
		private List<Integer> open_dipl_offsets = null;

		private List<Integer> getDiplOffsets() {
			if (open_dipl_offsets == null)
				open_dipl_offsets = new Vector<Integer>();
			return open_dipl_offsets;
		}

		private void resetDiplOffsets() {
			open_dipl_offsets = null;
		}

		private Integer getLastDiplOffest() {
			if (getDiplOffsets().isEmpty()) {
				return 0;
			} else {
				return getDiplOffsets().get(getDiplOffsets().size() - 1);
			}
		}

		/** stores offsets of mod-Tokens in the current token **/
		private List<Integer> open_mod_offsets = null;

		private List<Integer> getModOffsets() {
			if (open_mod_offsets == null)
				open_mod_offsets = new Vector<Integer>();
			return open_mod_offsets;
		}

		private void resetModOffsets() {
			open_mod_offsets = null;
		}

		private Integer getLastModOffest() {
			if (getModOffsets().isEmpty()) {
				return 0;
			} else {
				return getModOffsets().get(getModOffsets().size() - 1);
			}
		}

		private Stack<SNode> sNodeStack = null;

		/** returns stack containing node hierarchie **/
		private Stack<SNode> getSNodeStack() {
			if (sNodeStack == null)
				sNodeStack = new Stack<SNode>();
			return (sNodeStack);
		}

		private Stack<String> xmlElementStack = null;

		/** returns stack containing xml-element hierarchie **/
		private Stack<String> getXMLELementStack() {
			if (xmlElementStack == null)
				xmlElementStack = new Stack<String>();
			return (xmlElementStack);
		}

		/** number of read pos in suggestion //suggestions/pos **/
		private int sugPos = 1;
		private int sugPosLemma = 1;
		/** number of read lemma in suggestion //suggestions/lemma **/
		private int sugLemma = 1;
		/** number of read morph in suggestion //suggestions/morph **/
		private int sugMorph = 1;

		/** name of segmentation identified by {@link CoraXMLDictionary#TAG_MOD} **/
		public static final String SEGMENTATION_NAME_MOD = "tok_mod";
		/** name of segmentation identified by {@link CoraXMLDictionary#TAG_DIPL} **/
		public static final String SEGMENTATION_NAME_DIPL = "tok_dipl";
		/**
		 * name of segmentation identified by
		 * {@link CoraXMLDictionary#TAG_TOKEN}
		 **/
		public static final String SEGMENTATION_NAME_TOKEN = "token";

		/** stores last segmentation units **/
		private SToken last_mod = null;
		private SToken last_dipl = null;
		private SToken last_token = null;

		/** stores current line **/
		private SSpan currentLine = null;
		/** stores current column **/
		private SSpan currentColumn = null;
		/** stores current page **/
		private SSpan currentPage = null;
		private char current_column = 'a';

		// this method name is a bit misleading, it only escapes the ATT_TAG
		// attribute
		// and returns it as String
		private String unescape(Attributes attributes) {
			return StringEscapeUtils.unescapeHtml4(attributes.getValue(ATT_TAG));
		}

		private void addSimpleRow(String name, Attributes attributes) {
			addSimpleRow(name, attributes, null);
		}

		private void addSimpleRow(String name, Attributes attributes, String ns) {
			getSNodeStack().peek().createAnnotation(ns, name, attributes.getValue(ATT_TAG));
		}

		private void addOrderRelation(SToken source, SToken target, String type) {
			if (source != null) {
				SOrderRelation orderRel = SaltFactory.createSOrderRelation();
				orderRel.setSource(source);
				orderRel.setTarget(target);
				orderRel.setType(type);
				getDocument().getDocumentGraph().addRelation(orderRel);
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (TAG_LAYOUTINFO.equals(qName)) {
				//TODO anything to do here? otherwise remove this
			} else if (TAG_FM.equals(qName)) {
				//TODO anything to do here? otherwise remove this
			} else if (TAG_TEXT.equals(qName)) {
				//TODO anything to do here? otherwise remove this
			} else if (TAG_TOKEN.equals(qName)) {

				if (exportTokenLayer) {
					// increment the length of the text object
					int left_pos = getSTextualDS_trans().getText().length();
					getSTextualDS_trans().setText(getSTextualDS_trans().getText() + StringEscapeUtils.unescapeHtml4(attributes.getValue(ATT_TRANS)));
					int right_pos = getSTextualDS_trans().getText().length();
					// create a tok
					SToken tok = getDocument().getDocumentGraph().createToken(getSTextualDS_trans(), left_pos, right_pos);
					addOrderRelation(last_token, tok, SEGMENTATION_NAME_TOKEN);
					last_token = tok;
					getSNodeStack().add(tok);

					// add Space
					getSTextualDS_trans().setText(getSTextualDS_trans().getText() + " ");
				}
			} else if (TAG_PAGE.equals(qName)) {
				SSpan colSpan = SaltFactory.createSSpan();
				// TODO there should be a span for the page no independent of
				// the side
				colSpan.createAnnotation(null, ATT_SIDE, attributes.getValue(ATT_SIDE));
				colSpan.createAnnotation(null, "page", attributes.getValue(ATT_NO));

				getDocument().getDocumentGraph().addNode(colSpan);
				String[] parts = attributes.getValue(ATT_RANGE).split("[.][.]");
				if (parts.length >= 1) {
					pageStart.put(parts[0], colSpan);
					if (parts.length == 2)
						pageEnd.put(parts[1], colSpan);
					else
						pageEnd.put(parts[0], colSpan);
				}
			} else if (TAG_SHIFTTAGS.equals(qName)) {
			} else if (TAG_MOD.equals(qName)) {
				// increment the length of the text object
				int left_pos = getSTextualDS_mod().getText().length();
				getSTextualDS_mod().setText(getSTextualDS_mod().getText() + StringEscapeUtils.unescapeHtml4(attributes.getValue(mod_tok_textlayer)));
				int right_pos = getSTextualDS_mod().getText().length();
				// create a mod_tok
				SToken tok = getDocument().getDocumentGraph().createToken(getSTextualDS_mod(), left_pos, right_pos);
				addOrderRelation(last_mod, tok, SEGMENTATION_NAME_MOD);
				last_mod = tok;
				this.getOpenMods().add(tok);
				this.getModOffsets().add(this.getLastModOffest() + attributes.getValue(ATT_TRANS).length());
				getSNodeStack().add(tok);

				// add Space
				getSTextualDS_mod().setText(getSTextualDS_mod().getText() + " ");
			} else if (TAG_DIPL.equals(qName)) {
				// increment the length of the text object
				int left_pos = getSTextualDS_dipl().getText().length();
				getSTextualDS_dipl().setText(getSTextualDS_dipl().getText() + StringEscapeUtils.unescapeHtml4(attributes.getValue(dipl_tok_textlayer)));
				int right_pos = getSTextualDS_dipl().getText().length();
				// create a dipl_tok
				SToken tok = getDocument().getDocumentGraph().createToken(getSTextualDS_dipl(), left_pos, right_pos);
				addOrderRelation(last_dipl, tok, SEGMENTATION_NAME_DIPL);
				last_dipl = tok;
				this.getOpenDipls().add(tok);
				this.getDiplOffsets().add(this.getLastDiplOffest() + attributes.getValue(ATT_TRANS).length());
				getSNodeStack().add(tok);

				// add Space
				getSTextualDS_dipl().setText(getSTextualDS_dipl().getText() + " ");

				String id = attributes.getValue(ATT_ID);
				if (lineStart.get(id) != null)
					currentLine = lineStart.get(id);
				span_on_tok(currentLine, tok);
				if (lineEnd.get(id) != null)
					currentLine = null;

				// switch column links to line identifier
				if (columnStart.get(id) != null)
					currentColumn = columnStart.get(id);
				span_on_tok(currentColumn, tok);
				if (columnEnd.get(id) != null)
					currentColumn = null;
				// switch column links to line identifier

				// switch page links to column identifier
				if (pageStart.get(id) != null)
					currentPage = pageStart.get(id);
				span_on_tok(currentPage, tok);
				if (pageEnd.get(id) != null)
					currentPage = null;
				// switch page links to column identifier
			} else if (TAG_COLUMN.equals(qName)) {
				String[] parts = attributes.getValue(ATT_RANGE).split("[.][.]");
				String start = null;
				String end = null;
				if (parts.length >= 1) {
					start = parts[0];
					if (parts.length == 2)
						end = parts[1];
					else
						end = parts[0];
				}

				String id = attributes.getValue(ATT_ID);
				// switch page links to column identifier
				SSpan pageSpan = pageStart.get(id);
				if (pageSpan != null) {
					pageStart.remove(id);
					pageStart.put(start, pageSpan);
					// reset column count
					current_column = 'a';
				}
				pageSpan = pageEnd.get(id);
				if (pageSpan != null) {
					pageEnd.remove(id);
					pageEnd.put(end, pageSpan);
				}
				// switch page links to column identifier

				SSpan colSpan = SaltFactory.createSSpan();
				// colSpan.createAnnotation(null, TAG_COLUMN,
				// attributes.getValue(ATT_ID));
				// ATT_ID contains the id of the column element (e.g. "c1"), but
				// this has to be changed
				// to alphabetic numbering ("c1" -> "a")
				colSpan.createAnnotation(null, TAG_COLUMN, Character.toString(current_column++));

				getDocument().getDocumentGraph().addNode(colSpan);

				columnStart.put(start, colSpan);
				columnEnd.put(end, colSpan);
			} else if (TAG_LINE.equals(qName)) {
				SSpan sSpan = SaltFactory.createSSpan();
				sSpan.createAnnotation(null, TAG_LINE, attributes.getValue(ATT_NAME));
				getDocument().getDocumentGraph().addNode(sSpan);
				String[] parts = attributes.getValue(ATT_RANGE).split("[.][.]");
				String start = null;
				String end = null;
				if (parts.length >= 1) {
					start = parts[0];
					if (parts.length == 2)
						end = parts[1];
					else
						end = parts[0];
				}
				lineStart.put(start, sSpan);
				lineEnd.put(end, sSpan);

				String id = attributes.getValue(ATT_ID);

				SSpan column = columnStart.get(id);
				if (column != null) {
					columnStart.remove(id);
					columnStart.put(start, column);
				}
				column = columnEnd.get(id);
				if (column != null) {
					columnEnd.remove(id);
					columnEnd.put(end, column);
				}

				SSpan pageSpan = pageStart.get(id);
				if (pageSpan != null) {
					pageStart.remove(id);
					pageStart.put(start, pageSpan);
				}
				pageSpan = pageEnd.get(id);
				if (pageSpan != null) {
					pageEnd.remove(id);
					pageEnd.put(end, pageSpan);
				}
			} else if (TAG_COMMENT.equals(qName)) {
				//TODO anything to do here? otherwise remove this
			} else if (TAG_HEADER.equals(qName)) {
				//TODO anything to do here? otherwise remove this
			} else if (TAG_POS.equals(qName)) {
				if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek())) {
					SAnnotation sAnno = getSNodeStack().peek().createAnnotation(TAG_SUGGESTIONS, TAG_POS + "_" + sugPos, unescape(attributes));
					if (attributes.getValue(ATT_SCORE) != null) {
						SAnnotation scoreAnno = SaltFactory.createSAnnotation();
						scoreAnno.setName(ATT_SCORE);
						scoreAnno.setValue(attributes.getValue(ATT_SCORE));
						sAnno.addLabel(scoreAnno);
					}
					sugPos++;
				} else
					addSimpleRow(TAG_POS, attributes);
			} else if ("intern_pos_gen".equals(qName)) {
				addSimpleRow("posLemma_intern", attributes);
			} else if ("intern_pos".equals(qName)) {
				addSimpleRow("pos_intern", attributes);
			} else if ("intern_infl".equals(qName)) {
				addSimpleRow("inflection_intern", attributes);
			} else if (TAG_POS_LEMMA.equals(qName)) {
				if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek())) {
					SAnnotation sAnno = getSNodeStack().peek().createAnnotation(TAG_SUGGESTIONS, TAG_POS + "_" + sugPosLemma, unescape(attributes));
					if (attributes.getValue(ATT_SCORE) != null) {
						SAnnotation scoreAnno = SaltFactory.createSAnnotation();
						scoreAnno.setName(ATT_SCORE);
						scoreAnno.setValue(attributes.getValue(ATT_SCORE));
						sAnno.addLabel(scoreAnno);
					}
					sugPosLemma++;
				} else {
					addSimpleRow("posLemma", attributes);
				}
			} else if (TAG_NORM.equals(qName) || TAG_NORMBROAD.equals(qName)) {
				String ann_name = TAG_NORM.equals(qName) ? "norm" : "modern";
				addSimpleRow(ann_name, attributes);
			} else if (TAG_INFLCLASS.equals(qName)) {
				addSimpleRow("inflectionClass", attributes);
			} else if (TAG_INFLCLASS_LEMMA.equals(qName)) {
				addSimpleRow("inflectionClassLemma", attributes);
			} else if (TAG_INFL.equals(qName)) {
				addSimpleRow("inflection", attributes);
			} else if (TAG_NORMALIGN.equals(qName) || TAG_NORMALIGN_VARIANT.equals(qName)) {
				addSimpleRow("char_align", attributes);
			} else if (TAG_LEMMA_ID.equals(qName)) {
				/*
				 * StringBuilder sb = new StringBuilder();
				 * sb.append("http://www.mhdwb-online.de/lemmaliste/");
				 * sb.append(attributes.getValue(ATT_TAG));
				 * getSNodeStack().peek().createAnnotation(null, "lemmaId",
				 * sb.toString());
				 */
				addSimpleRow("lemmaId", attributes);
			} else if ("lemma_gen".equals(qName)) {
				addSimpleRow("lemmaLemma", attributes);
			} else if (TAG_LEMMA.equals(qName)) {
				if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek())) {
					SAnnotation sAnno = getSNodeStack().peek().createAnnotation(TAG_SUGGESTIONS, TAG_LEMMA + "_" + sugLemma, unescape(attributes));
					if (attributes.getValue(ATT_SCORE) != null) {
						SAnnotation scoreAnno = SaltFactory.createSAnnotation();
						scoreAnno.setName(ATT_SCORE);
						scoreAnno.setValue(attributes.getValue(ATT_SCORE));
						sAnno.addLabel(scoreAnno);
					}
					sugLemma++;
				} else
					addSimpleRow(TAG_LEMMA, attributes);
			} else if (TAG_MORPH.equals(qName)) {
				if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek())) {
					SAnnotation sAnno = getSNodeStack().peek().createAnnotation(TAG_SUGGESTIONS, TAG_MORPH + "_" + sugMorph, attributes.getValue(ATT_TAG));
					if (attributes.getValue(ATT_SCORE) != null) {
						SAnnotation scoreAnno = SaltFactory.createSAnnotation();
						scoreAnno.setName(ATT_SCORE);
						scoreAnno.setValue(attributes.getValue(ATT_SCORE));
						sAnno.addLabel(scoreAnno);
					}
					sugMorph++;
				} else
					addSimpleRow(TAG_MORPH, attributes);
			}
			// must be at the end, that one before last is always on top when
			// processing
			getXMLELementStack().push(qName);
		}

		private STextualDS sTextualDS_trans = null;

		private STextualDS getSTextualDS_trans() {
			if (sTextualDS_trans == null) {
				sTextualDS_trans = SaltFactory.createSTextualDS();
				sTextualDS_trans.setText("");
				getDocument().getDocumentGraph().addNode(sTextualDS_trans);
			}
			return (sTextualDS_trans);
		}

		private STextualDS sTextualDS_dipl = null;

		private STextualDS getSTextualDS_dipl() {
			if (sTextualDS_dipl == null) {
				sTextualDS_dipl = SaltFactory.createSTextualDS();
				sTextualDS_dipl.setText("");
				getDocument().getDocumentGraph().addNode(sTextualDS_dipl);
			}
			return (sTextualDS_dipl);
		}

		private STextualDS sTextualDS_mod = null;

		private STextualDS getSTextualDS_mod() {
			if (sTextualDS_mod == null) {
				sTextualDS_mod = SaltFactory.createSTextualDS();
				sTextualDS_mod.setText("");
				getDocument().getDocumentGraph().addNode(sTextualDS_mod);
			}
			return (sTextualDS_mod);
		}

		private STimeline getTimeline() {
			if (getDocument().getDocumentGraph().getTimeline() == null) {
				STimeline sTimeline = SaltFactory.createSTimeline();
				getDocument().getDocumentGraph().setTimeline(sTimeline);
				// add initial PointOfTime
				sTimeline.increasePointOfTime();
			}

			return getDocument().getDocumentGraph().getTimeline();
		}

		private void addTimelineRelation(SToken sToken, Integer startPos, Integer endPos) {
			STimelineRelation sTimeRel = SaltFactory.createSTimelineRelation();
			sTimeRel.setTarget(this.getTimeline());
			sTimeRel.setSource(sToken);
			sTimeRel.setStart(startPos);
			sTimeRel.setEnd(endPos);
			getDocument().getDocumentGraph().addRelation(sTimeRel);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (TAG_HEADER.equals(getXMLELementStack().peek())) {
				StringBuffer textBuf = new StringBuffer();
				for (int i = start; i < start + length; i++)
					textBuf.append(ch[i]);

				String text = textBuf.toString();

				String[] lines = text.split("\n");
				for (String line : lines) {
					line = line.trim();
					if (!line.isEmpty()) {
						String[] parts = line.split(":");
						if (parts.length >= 1) {
							String sName = parts[0].trim();
							String sValue = null;
							if (parts.length == 2)
								sValue = parts[1].trim();
							if (!getDocument().containsLabel(sName))
								getDocument().createMetaAnnotation(null, sName, sValue);
						}
					}
				}
			}
		}

		private void span_on_tok(SSpan span, SToken tok) {
			SSpanningRelation rel = SaltFactory.createSSpanningRelation();
			rel.setSource(span);
			rel.setTarget(tok);
			getDocument().getDocumentGraph().addRelation(rel);
		}

		// / mappings onto the timeline
		private void map_stokens_to_timeline_simple() {

			int dipl_pos = 0, mod_pos = 0;
			// // initialization is actually unnecessary, but if i don't do it,
			// // eclipse gives warnings
			SToken last_dipl = null;
			SToken last_mod = null;

			Integer dipl_start = getTimeline().getEnd() - 1;
			Integer mod_start = getTimeline().getEnd() - 1;
			Integer token_start = getTimeline().getEnd() - 1;

			while (dipl_pos < getOpenDipls().size() || mod_pos < getOpenMods().size()) {

				// create PointOfTime
				getTimeline().increasePointOfTime();;
				Integer end_point = getTimeline().getEnd() - 1;

				if (dipl_pos < getOpenDipls().size()) {
					if (last_dipl != null) {
						addTimelineRelation(last_dipl, dipl_start, end_point);
						dipl_start = end_point;
					}
					last_dipl = getOpenDipls().get(dipl_pos++);
				}
				if (mod_pos < getOpenMods().size()) {
					if (last_mod != null) {
						addTimelineRelation(last_mod, mod_start, end_point);
						mod_start = end_point;
					}
					last_mod = getOpenMods().get(mod_pos++);
				}
			}

			getTimeline().increasePointOfTime();
			Integer end_point = getTimeline().getEnd() - 1;

			if (last_dipl != null) {
				addTimelineRelation(last_dipl, dipl_start, end_point);
			}
			if (last_mod != null) {
				addTimelineRelation(last_mod, mod_start, end_point);
			}
			if (exportTokenLayer) {
				addTimelineRelation(last_token, token_start, end_point);
			}

		}

		private void map_stokens_to_timeline_aligned() {

			SortedSet<Integer> tok_offsets = new TreeSet<Integer>();
			tok_offsets.addAll(getDiplOffsets());
			tok_offsets.addAll(getModOffsets());

			int dipl_pos = 0, mod_pos = 0;
			SToken last_dipl = null;
			SToken last_mod = null;

			Integer dipl_start = getTimeline().getEnd()- 1;
			Integer mod_start = getTimeline().getEnd() - 1;
			Integer token_start = getTimeline().getEnd() - 1;

			for (Integer tok_offset : tok_offsets) {

				// create PointOfTime
				getTimeline().increasePointOfTime();
				Integer end_point = getTimeline().getEnd() - 1;

				// get next dipl and mod
				Integer dipl_offset = getDiplOffsets().get(dipl_pos);
				Integer mod_offset = getModOffsets().get(mod_pos);

				if (dipl_offset == tok_offset) { // } && (dipl_pos <
													// getOpenDipls().size())) {
					last_dipl = getOpenDipls().get(dipl_pos++);
					addTimelineRelation(last_dipl, dipl_start, end_point);
					dipl_start = end_point;
				}
				if (mod_offset == tok_offset) { // && (mod_pos <
												// getOpenMods().size())) {
					last_mod = getOpenMods().get(mod_pos++);
					addTimelineRelation(last_mod, mod_start, end_point);
					mod_start = end_point;
				}
			}
			if (exportTokenLayer) {
				addTimelineRelation(last_token, token_start, getTimeline().getEnd() - 1);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (TAG_TOKEN.equals(qName)) {

				if (tokenization_is_segmentation) {
					// TODO: check whether the dipls and mods really contain the
					// same text
					// otherwise warn and call create_stokens_simple
					map_stokens_to_timeline_aligned();
				} else
					map_stokens_to_timeline_simple();

				resetOpenDipls();
				resetDiplOffsets();
				resetOpenMods();
				resetModOffsets();
				if (exportTokenLayer) {
					getSNodeStack().pop();
				}
			}// tag is TAG_TOKEN
			else if (TAG_DIPL.equals(qName)) {// tag is TAG_DIPL
				getSNodeStack().pop();
			}// tag is TAG_DIPL
			else if (TAG_MOD.equals(qName)) {// tag is TAG_DIPL
				getSNodeStack().pop();
			}// tag is TAG_DIPL
			else if (TAG_SUGGESTIONS.equals(qName)) {
				sugLemma = 0;
				sugPos = 0;
				sugPosLemma = 0;
				sugMorph = 0;
			}
			getXMLELementStack().pop();
		}
	}

	// TODO: deal with invalid values for textlayer
	public void setModTokTextlayer(String textlayer) {
		if (textlayer.equals(ATT_ASCII) || textlayer.equals(ATT_UTF) || textlayer.equals(ATT_TRANS)) {
			this.mod_tok_textlayer = textlayer;
		}
	}

	public void setDiplTokTextlayer(String textlayer) {
		if (textlayer.equals(ATT_UTF) || textlayer.equals(ATT_TRANS)) {
			this.dipl_tok_textlayer = textlayer;
		}
	}

	public void setExportTokenLayer(boolean exportTokenLayer) {
		this.exportTokenLayer = exportTokenLayer;
	}

	public void setTokenizationIsSegmentation(boolean tokenization_is_segmentation) {
		this.tokenization_is_segmentation = tokenization_is_segmentation;
	}

}
