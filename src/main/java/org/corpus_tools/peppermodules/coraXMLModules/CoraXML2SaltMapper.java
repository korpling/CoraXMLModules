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
package org.corpus_tools.peppermodules.coraXMLModules;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class CoraXML2SaltMapper extends PepperMapperImpl implements PepperMapper, CoraXMLDictionary {
	private static final Logger logger = LoggerFactory.getLogger(CoraXMLImporter.MODULE_NAME);

	/**
	 * defines which textual representation is used for the dipl and mod tokens
	 **/
	private String mod_tok_textlayer = ATT_ASCII; // one of "trans", "utf" and
													// "ascii"
	private String dipl_tok_textlayer = ATT_UTF; // one of "trans" and "utf"

	/** defines whether the token layer should be exported **/
	private boolean exportTokenLayer = true;

	/** defines whether dipl and mod are only segmentations of token **/
	private boolean tokenization_is_segmentation = false;

	/** defines whether internal annotations are imported **/
	private boolean import_internals = false;

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
		Hashtable<String, SSpan> lineStart = new Hashtable<String, SSpan>();
		Hashtable<String, SSpan> lineEnd = new Hashtable<String, SSpan>();

		Hashtable<String, SSpan> columnStart = new Hashtable<String, SSpan>();
		Hashtable<String, SSpan> columnEnd = new Hashtable<String, SSpan>();

		Hashtable<String, SSpan> pageStart = new Hashtable<String, SSpan>();
		Hashtable<String, SSpan> pageEnd = new Hashtable<String, SSpan>();

		Hashtable<String, SSpan> sideStart = new Hashtable<String, SSpan>();
		Hashtable<String, SSpan> sideEnd = new Hashtable<String, SSpan>();
		Hashtable<String, SSpan> locStart = new Hashtable<String, SSpan>();
		Hashtable<String, SSpan> locEnd = new Hashtable<String, SSpan>();
		String last_added_page_no = null;
		String last_added_page_end = null;
		int page_count = 0; // we start at zero because it will be incremented
							// before it's used

		private List<SToken> openDipls = null;

		/**
		 * returns all open {@link CoraXMLDictionary#TAG_DIPL} which are related
		 * to a {@link SSpan} and are not connected with {@link SToken} objects
		 * so far
		 **/
		private List<SToken> getOpenDipls() {
			if (openDipls == null) {
				openDipls = new ArrayList<>();
			}
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
			if (openMods == null) {
				openMods = new ArrayList<>();
			}
			return (openMods);
		}

		private void resetOpenMods() {
			openMods = null;
		}

		/** stores offsets of dipl-Tokens in the current token **/
		private List<Integer> open_dipl_offsets = null;

		private List<Integer> getDiplOffsets() {
			if (open_dipl_offsets == null) {
				open_dipl_offsets = new ArrayList<>();
			}
			return open_dipl_offsets;
		}

		private void resetDiplOffsets() {
			open_dipl_offsets = null;
		}

		private Integer getLastDiplOffset() {
			if (getDiplOffsets().isEmpty()) {
				return 0;
			} else {
				return getDiplOffsets().get(getDiplOffsets().size() - 1);
			}
		}

		/** stores offsets of mod-Tokens in the current token **/
		private List<Integer> open_mod_offsets = null;

		private List<Integer> getModOffsets() {
			if (open_mod_offsets == null) {
				open_mod_offsets = new ArrayList<>();
			}
			return open_mod_offsets;
		}

		private void resetModOffsets() {
			open_mod_offsets = null;
		}

		private Integer getLastModOffset() {
			if (getModOffsets().isEmpty()) {
				return 0;
			} else {
				return getModOffsets().get(getModOffsets().size() - 1);
			}
		}

		private Stack<SNode> sNodeStack = null;

		/** returns stack containing node hierarchie **/
		private Stack<SNode> getSNodeStack() {
			if (sNodeStack == null) {
				sNodeStack = new Stack<SNode>();
			}
			return (sNodeStack);
		}

		private Stack<String> xmlElementStack = null;

		/** returns stack containing xml-element hierarchie **/
		private Stack<String> getXMLELementStack() {
			if (xmlElementStack == null) {
				xmlElementStack = new Stack<String>();
			}
			return (xmlElementStack);
		}

		/** number of read pos in suggestion //suggestions/pos **/
		private int sugPos = 1;
		private int sugPosLemma = 1;
		/** number of read lemma in suggestion //suggestions/lemma **/
		private int sugLemma = 1;
		/** number of read morph in suggestion //suggestions/morph **/
		private int sugMorph = 1;

		/**
		 * name of segmentation identified by {@link CoraXMLDictionary#TAG_MOD}
		 **/
		public static final String SEGMENTATION_NAME_MOD = "tok_mod";
		/**
		 * name of segmentation identified by {@link CoraXMLDictionary#TAG_DIPL}
		 **/
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
		private SSpan currentLoc = null;
		/** stores current column **/
		private SSpan currentColumn = null;
		/** stores current page **/
		private SSpan currentPage = null;
		private SSpan currentSide = null;
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

		/// add a dipl or mod tok and return the SToken just added
		private SToken add_tok(STextualDS textualds, String textlayer, SToken last_added, String seg_name, List<SToken> opens, List<Integer> offsets, Integer last_offset, Attributes attr) {
			// increment the length of the text object
			int left_pos = textualds.getText().length();
			textualds.setText(textualds.getText() + StringEscapeUtils.unescapeHtml4(attr.getValue(textlayer)));
			int right_pos = textualds.getText().length();
			// create a SToken
			SToken tok = getDocument().getDocumentGraph().createToken(textualds, left_pos, right_pos);
			addOrderRelation(last_added, tok, seg_name);

			if (opens != null && offsets != null) {
				opens.add(tok);
				offsets.add(last_offset + attr.getValue(ATT_TRANS).length());
			}
			getSNodeStack().add(tok);

			// add Space
			textualds.setText(textualds.getText() + " ");

			return tok;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (TAG_TOKEN.equals(qName)) {
				if (exportTokenLayer) {
					last_token = add_tok(getSTextualDS_trans(), ATT_TRANS, last_token, SEGMENTATION_NAME_TOKEN, null, null, null, attributes);
				}
			} else if (TAG_PAGE.equals(qName)) {
				// currently, side is an attribute of page. this might change in
				// the future, but for now
				// we may need to create two spans with different lengths here
				SSpan pageSpan = SaltFactory.createSSpan();
				String[] range = attributes.getValue(ATT_RANGE).split("[.][.]");
				String start_id = range[0], end_id = (range.length == 1 ? range[0] : range[1]);
				if (range.length < 1 || range.length > 2)
					logger.warn("Could not get a valid range from the attribute of page id='" + attributes.getValue("id") + "' - range text was: '" + attributes.getValue(ATT_RANGE) + "'");

				// unless page#no is the same as with the previous <page>, we
				// need to make a new page span
				// otherwise we just have to change the last added page's range
				if (last_added_page_no == null || !attributes.getValue("no").equals(last_added_page_no)) {
					// make new page
					++page_count;
					String my_pageno = attributes.getValue("no");
					// page#no can be empty, which is an error in the input but
					// needs to be dealt with
					if (my_pageno == null) {
						my_pageno = Integer.toString(page_count);
						logger.warn("No page#no attribute found for page " + attributes.getValue("id") + " - falling back to page count");
					}
					pageSpan.createAnnotation(null, "page", attributes.getValue("no"));
					getDocument().getDocumentGraph().addNode(pageSpan);
					pageStart.put(start_id, pageSpan);
					pageEnd.put(end_id, pageSpan);
				} else {
					// join with previous page
					SSpan previous_span = null;
					if (last_added_page_end != null) {
						previous_span = pageEnd.get(last_added_page_end);
						pageEnd.remove(last_added_page_end);
					} else {
						logger.warn("Trying to join two page spans because I think there's a second side, but there is no last added page! " + "id: " + attributes.getValue("id"));
						previous_span = pageSpan;
					}
					pageEnd.put(end_id, previous_span);
				}
				last_added_page_no = attributes.getValue("no");
				last_added_page_end = end_id;

				SSpan sideSpan = SaltFactory.createSSpan();
				if (attributes.getValue(ATT_SIDE) != null) {
					// if side exists, create side span and make connections.
					sideSpan.createAnnotation(null, "side", attributes.getValue(ATT_SIDE));
					getDocument().getDocumentGraph().addNode(sideSpan);
					sideStart.put(start_id, sideSpan);
					sideEnd.put(end_id, sideSpan);
				}
			} else if (TAG_MOD.equals(qName)) {
				last_mod = add_tok(getSTextualDS_mod(), mod_tok_textlayer, last_mod, SEGMENTATION_NAME_MOD, getOpenMods(), getModOffsets(), getLastModOffset(), attributes);
			} else if (TAG_DIPL.equals(qName)) {
				last_dipl = add_tok(getSTextualDS_dipl(), dipl_tok_textlayer, last_dipl, SEGMENTATION_NAME_DIPL, getOpenDipls(), getDiplOffsets(), getLastDiplOffset(), attributes);

				String id = attributes.getValue(ATT_ID);
				if (lineStart.get(id) != null) {
					currentLine = lineStart.get(id);
				}
				if (currentLine == null) {
					logger.warn("Cannot add token '" + id + "' to current line, because current line is empty. ");
				} else {
					span_on_tok(currentLine, last_dipl);
				}
				if (lineEnd.get(id) != null) {
					currentLine = null;
				}
				// loc is basically a copy of line with a different content
				if (locStart.get(id) != null) {
					currentLoc = locStart.get(id);
				}
				if (currentLoc != null) {
					// we can't really warn at the moment if loc is null,
					// because a lot of documents have no loc attribute
					span_on_tok(currentLoc, last_dipl);
				}

				// switch column links to line identifier
				if (columnStart.get(id) != null) {
					currentColumn = columnStart.get(id);
				}
				if (currentColumn == null) {
					logger.warn("Cannot add token '" + id + "' to current column, because current column is empty. ");
				} else {
					span_on_tok(currentColumn, last_dipl);
				}
				if (columnEnd.get(id) != null) {
					currentColumn = null;
				}
				// switch column links to line identifier
				if (sideStart.get(id) != null) {
					currentSide = sideStart.get(id);
				}
				if (currentSide != null) {
					// we can't really warn if side is null, because a lot of
					// documents have no sides
					span_on_tok(currentSide, last_dipl);
				}

				// switch page links to column identifier
				if (pageStart.get(id) != null) {
					currentPage = pageStart.get(id);
				}
				if (currentPage == null) {
					logger.warn("Cannot add token '" + id + "' to current page, because current page is empty. ");
				} else {
					span_on_tok(currentPage, last_dipl);
				}
				if (pageEnd.get(id) != null) {
					currentPage = null;
				}
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
				SSpan layout_span = null;

				// switch page links to column identifier
				// we need to reset the column counter below, but where exactly
				// depends on
				// if we have sides or not
				layout_span = pageStart.get(id);
				if (layout_span != null) {
					pageStart.remove(id);
					pageStart.put(start, layout_span);
					if (currentSide == null)
						current_column = 'a';
				}
				layout_span = pageEnd.get(id);
				if (layout_span != null) {
					pageEnd.remove(id);
					pageEnd.put(end, layout_span);
				}
				layout_span = sideStart.get(id);
				if (layout_span != null) {
					sideStart.remove(id);
					sideStart.put(start, layout_span);
					current_column = 'a';
				}
				layout_span = sideEnd.get(id);
				if (layout_span != null) {
					sideEnd.remove(id);
					sideEnd.put(end, layout_span);
				}

				SSpan colSpan = SaltFactory.createSSpan();
				// ATT_ID contains the id of the column element (e.g. "c1"), but
				// this has to be changed
				// to alphabetic numbering ("c1" -> "a")
				colSpan.createAnnotation(null, TAG_COLUMN, Character.toString(current_column++));

				getDocument().getDocumentGraph().addNode(colSpan);

				columnStart.put(start, colSpan);
				columnEnd.put(end, colSpan);
			} else if (TAG_LINE.equals(qName)) {

				String[] parts = attributes.getValue(ATT_RANGE).split("[.][.]");
				String start = null, end = null;
				if (parts.length >= 1) {
					start = parts[0];
					end = parts.length > 1 ? parts[1] : parts[0];
				} else {
					logger.warn("Could not parse range string for line '" + attributes.getValue("id") + "'");
				}

				// here we create two spans, one for the line itself, and one
				// that sums up
				// the layout info in a single span which can be used instead of
				// the hierarchically
				// ordered spans
				SSpan lineSpan = SaltFactory.createSSpan();
				lineSpan.createAnnotation(null, "line", attributes.getValue("name"));
				getDocument().getDocumentGraph().addNode(lineSpan);
				lineStart.put(start, lineSpan);
				lineEnd.put(end, lineSpan);

				// only create loc span, if attribute exists
				if (attributes.getValue("loc") != null) {
					SSpan locSpan = SaltFactory.createSSpan();
					locSpan.createAnnotation(null, "reference", attributes.getValue("loc"));
					getDocument().getDocumentGraph().addNode(locSpan);
					locStart.put(start, locSpan);
					locEnd.put(end, locSpan);
				}

				// at this point all higher layout elements should have been
				// mapped to line ids
				// they need to be changed to tok_dipl id
				String id = attributes.getValue(ATT_ID);
				SSpan layout_span = columnStart.get(id);
				if (layout_span != null) {
					columnStart.remove(id);
					columnStart.put(start, layout_span);
				}
				layout_span = columnEnd.get(id);
				if (layout_span != null) {
					columnEnd.remove(id);
					columnEnd.put(end, layout_span);
				}

				layout_span = sideStart.get(id);
				if (layout_span != null) {
					sideStart.remove(id);
					sideStart.put(start, layout_span);
				}
				layout_span = sideEnd.get(id);
				if (layout_span != null) {
					sideEnd.remove(id);
					sideEnd.put(end, layout_span);
				}

				layout_span = pageStart.get(id);
				if (layout_span != null) {
					pageStart.remove(id);
					pageStart.put(start, layout_span);
				}
				layout_span = pageEnd.get(id);
				if (layout_span != null) {
					pageEnd.remove(id);
					pageEnd.put(end, layout_span);
				}
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
			} else if ("punc".equals(qName)) {
				if (attributes.getValue(ATT_TAG) != "")
					addSimpleRow("punc", attributes);
			} else if ("token_type".equals(qName)) {
				// value can be empty, in which case it should add "--"
				String value = attributes.getValue(ATT_TAG) != "" ? attributes.getValue(ATT_TAG) : "--";
				getSNodeStack().peek().createAnnotation(null, "tokenization", value);
			} else if ("intern_pos_gen".equals(qName) && import_internals) {
				addSimpleRow("posLemma_intern", attributes);
			} else if ("intern_pos".equals(qName) && import_internals) {
				addSimpleRow("pos_intern", attributes);
			} else if ("intern_infl".equals(qName) && import_internals) {
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
			} else if ("lemma_idmwb".equals(qName)) {
				String lemma_id = attributes.getValue(ATT_TAG);
				// lemma_id = "<a href='http://www.mhdwb-online.de/lemmaliste/"
				// + lemma_id + "'>" + lemma_id + "</a>";
				getSNodeStack().peek().createAnnotation(null, "lemmaId", lemma_id);
			} else if ("lemma_gen".equals(qName)) {
				addSimpleRow("lemmaLemma", attributes);
			} else if (TAG_LEMMA.equals(qName)) {
				if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek())) {
					SAnnotation sAnno = getSNodeStack().peek().createAnnotation(TAG_SUGGESTIONS, TAG_LEMMA + "_" + sugLemma, unescape(attributes));
					if (attributes.getValue(ATT_SCORE) != null) {
						sAnno.createAnnotation(null, ATT_SCORE, attributes.getValue(ATT_SCORE));
					}
					sugLemma++;
				} else {
					addSimpleRow(TAG_LEMMA, attributes);
				}
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
				for (int i = start; i < start + length; i++) {
					textBuf.append(ch[i]);
				}
				String text = textBuf.toString();

				String[] lines = text.split("\n");
				for (String line : lines) {
					line = line.trim();
					if (!line.isEmpty()) {
						String[] parts = line.split(":");
						if (parts.length >= 1) {
							String sName = parts[0].trim();
							String sValue = null;
							if (parts.length == 2) {
								sValue = parts[1].trim();
							}
							if (!getDocument().containsLabel(sName)) {
								getDocument().createMetaAnnotation(null, sName, sValue);
							}
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
			// initialization is actually unnecessary, but if i don't do it,
			// eclipse gives warnings
			SToken last_dipl = null;
			SToken last_mod = null;

			Integer dipl_start = getTimeline().getEnd() - 1;
			Integer mod_start = getTimeline().getEnd() - 1;
			Integer token_start = getTimeline().getEnd() - 1;

			while (dipl_pos < getOpenDipls().size() || mod_pos < getOpenMods().size()) {
				// create PointOfTime
				getTimeline().increasePointOfTime();
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

			Integer dipl_start = getTimeline().getEnd() - 1;
			Integer mod_start = getTimeline().getEnd() - 1;
			Integer token_start = getTimeline().getEnd() - 1;

			for (Integer tok_offset : tok_offsets) {
				// create PointOfTime
				getTimeline().increasePointOfTime();
				Integer end_point = getTimeline().getEnd() - 1;

				// get next dipl and mod
				Integer dipl_offset = getDiplOffsets().get(dipl_pos);
				Integer mod_offset = getModOffsets().get(mod_pos);

				if (dipl_offset == tok_offset) {
					last_dipl = getOpenDipls().get(dipl_pos++);
					addTimelineRelation(last_dipl, dipl_start, end_point);
					dipl_start = end_point;
				}
				if (mod_offset == tok_offset) {
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
			} // tag is TAG_TOKEN
			else if (TAG_DIPL.equals(qName)) {// tag is TAG_DIPL
				getSNodeStack().pop();
			} // tag is TAG_DIPL
			else if (TAG_MOD.equals(qName)) {// tag is TAG_DIPL
				getSNodeStack().pop();
			} // tag is TAG_DIPL
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
		if (ATT_ASCII.equals(textlayer) || ATT_UTF.equals(textlayer) || ATT_TRANS.equals(textlayer)) {
			this.mod_tok_textlayer = textlayer;
		}
	}

	public void setDiplTokTextlayer(String textlayer) {
		if (ATT_UTF.equals(textlayer) || ATT_TRANS.equals(textlayer)) {
			this.dipl_tok_textlayer = textlayer;
		}
	}

	public void setExportTokenLayer(boolean exportTokenLayer) {
		this.exportTokenLayer = exportTokenLayer;
	}

	public void setTokenizationIsSegmentation(boolean tokenization_is_segmentation) {
		this.tokenization_is_segmentation = tokenization_is_segmentation;
	}

	public void setImportInternals(boolean import_internals) {
		this.import_internals = import_internals;
	}

}
