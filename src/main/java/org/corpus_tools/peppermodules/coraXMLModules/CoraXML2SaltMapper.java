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

import java.util.Arrays;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class CoraXML2SaltMapper extends PepperMapperImpl implements PepperMapper, CoraXMLDictionary {
    private static final Logger logger= LoggerFactory.getLogger(CoraXMLImporter.MODULE_NAME);
    /** defines which textual representation is used for the dipl and mod tokens **/
    private String mod_tok_textlayer = ATT_ASCII; // one of "trans", "utf" and
    private String dipl_tok_textlayer = ATT_UTF; // one of "trans" and "utf"
    /** defines whether the token layer should be exported **/
    private boolean exportTokenLayer = true;
    /** defines to which layer top-level comments are added; none, if empty **/
    private String exportCommentsToLayer = "";
    /** defines whether transcription markup should be turned into annotations **/
    /** the content of the string gives the type of the markup convention **/
    private String exportSubtokenannotation = "";
    /** defines whether dipl and mod are only segmentations of token **/
    private boolean tokenization_is_segmentation = true;
    /** defines a list of annotation types that are not imported **/
    private Set<String> annotations_to_ignore = new TreeSet<String>();
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
    public void setExportCommentsToLayer(String layerName) {
        this.exportCommentsToLayer = layerName;
    }
    public void setExportSubtokenannotation(String subtokannot) {
        this.exportSubtokenannotation = subtokannot;
    }
    public void setTokenizationIsSegmentation(boolean tokenization_is_segmentation) {
        this.tokenization_is_segmentation = tokenization_is_segmentation;
    }
    public void setExcludeAnnotations(String annotations_to_exclude) {
        this.annotations_to_ignore.addAll(Arrays.asList(annotations_to_exclude.split(";")));
    }

    /**
     * {@inheritDoc PepperMapper#setSDocument(SDocument)}
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

        boolean in_mod = false;

        StringBuffer header_text = new StringBuffer();
        StringBuffer comment_text = new StringBuffer();
        String comment_type;
        private Stack<String> xmlElementStack = null;
        private Stack<String> getXMLELementStack() {
            if (xmlElementStack == null) {
                xmlElementStack = new Stack<String>();
            }
            return (xmlElementStack);
        }

        Layout layout = null;
        /// lazy initialization ensures we can pass parameters
        /// that were set after CoraXMLReader creation
        Layout layout() {
            if (layout == null) {
                layout = new Layout(getDocument().getDocumentGraph());
            }
            return layout;
        }
        Text text = null;
        Text text() {
            if (text == null) {
                text = new Text(getDocument().getDocumentGraph(),
                                dipl_tok_textlayer,
                                mod_tok_textlayer, exportTokenLayer, !exportSubtokenannotation.isEmpty());
            }
            return text;
        }
        @Override
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes)
        throws SAXException {

            // layout tags
            if (TAG_PAGE.equals(qName)) {
            layout().make_page(attributes)
                    .make_side(attributes);
            } else if (TAG_COLUMN.equals(qName)) {
                layout().make_column(attributes);
            } else if (TAG_LINE.equals(qName)) {
                layout().make_line(attributes);
            }

            // token tags
            else if (TAG_TOKEN.equals(qName)) {
                if (exportTokenLayer)
                    text().layer("token")
                          .add_token(attributes);
            } else if (TAG_MOD.equals(qName)) {
                text().layer("mod")
                      .add_token(attributes);
                this.in_mod = true;
            } else if (TAG_DIPL.equals(qName)) {
                text().layer(qName)
                      .add_token(attributes);
                layout().render(attributes.getValue(ATT_ID),
                                text().layer(qName).last_token());
                if (!exportSubtokenannotation.isEmpty()) {
                    text().layer("sub").add_token(attributes, exportSubtokenannotation);
                }
            }

            // annotations on mod
            else if (this.in_mod && !annotations_to_ignore.contains(qName)) {

                // annotations with special treatment
                if (TAG_BOUNDARY.equals(qName)) {
                    // boundary annotations are realized as spans between them
                    text().annotate_boundary(TAG_BOUNDARY, attributes);
                }
                else if ("punc".equals(qName)) {
                    if (attributes.getValue(ATT_TAG) != "" &&
                            attributes.getValue(ATT_TAG) != "--")
                        text().annotate("punc", attributes);
                } else if (TAG_TOKENIZATION.equals(qName)) {
                    // value can be empty, in which case it should add "--"
                    String value = attributes.getValue(ATT_TAG) != ""
                            ? attributes.getValue(ATT_TAG) : "--";
                            text().annotate("tokenization", value);
                } else if (TAG_POS_LEMMA.equals(qName)) {
                    text().annotate("posLemma", attributes);
                } else if (TAG_NORM.equals(qName) || TAG_NORMBROAD.equals(qName)) {
                    String ann_name = TAG_NORM.equals(qName)
                            ? "norm" : "modern";
                    text().annotate(ann_name, attributes);
                } else if (TAG_INFLCLASS.equals(qName)) {
                    text().annotate("inflectionClass", attributes);
                } else if (TAG_INFLCLASS_LEMMA.equals(qName)) {
                    text().annotate("inflectionClassLemma",
                            attributes);
                } else if (TAG_INFL.equals(qName)) {
                    text().annotate("inflection", attributes);
                } else if (TAG_NORMALIGN.equals(qName)
                        || TAG_NORMALIGN_VARIANT.equals(qName)) {
                    text().annotate("char_align", attributes);
                } else if ("lemma_idmwb".equals(qName)) {
                    text().annotate("lemmaId", attributes);
                } else if ("lemma_gen".equals(qName)) {
                    text().annotate("lemmaLemma", attributes);
                }

                // span annotations
                else if (attributes.getValue("span-id") != null) {
                    text().add_tok_to_span(qName, attributes);
                }

                // all other annotations
                else if (attributes.getValue("tag") != null) {
                    text().annotate(qName, attributes);
                }
            }

            // top level comments
            // (mod-level comments have been consumed by previous else if)
            else if ("comment".equals(qName)) {
                comment_type = attributes.getValue("type");
            }

            // must be at the end, that one before last is always on top when
            // processing
            getXMLELementStack().push(qName);
        }

        @Override
        public void characters(char[] c_str, int start, int length)
        throws SAXException {
            // we can't rely on getting the entire content here, SAX may quit
            // after an amount if it's buffers are full
            if (TAG_HEADER.equals(getXMLELementStack().peek())) {
                header_text.append(c_str, start, length);
            }
            else if ("comment".equals(getXMLELementStack().peek())) {
                comment_text.append(c_str, start, length);
            }
        }


        @Override
        public void endElement(String uri, String localName, String qName)
        throws SAXException {
            if (TAG_TOKEN.equals(qName)) {
                if (tokenization_is_segmentation) {
                    // TODO: check whether the dipls and mods really contain the
                    // same text
                    // otherwise warn and call create_stokens_simple
                        text().map_tokens_to_timeline_aligned();
                    } else
                        text().map_tokens_to_timeline_simple();
                }
                else if (TAG_MOD.equals(qName)) {// tag is TAG_MOD
                    this.in_mod = false;
                } else if (TAG_HEADER.equals(qName)) {
                    String[] lines = header_text.toString().split(System.getProperty("line.separator"));
                    for (String line : lines) {
                        line = line.trim();
                        if (line.isEmpty())
                            continue;
                        String[] parts = line.split(":", 2);
                        if (parts.length >= 1) {
                            String name = parts[0].trim();
                            String value = null;
                            if (parts.length == 2){
                                value = parts[1].trim();
                            }
                            if (!getDocument().containsLabel(name)){
                                getDocument().createMetaAnnotation(null, name, value);
                            } else {
                                logger.warn("Attempting to create a meta that is already present!\n"
                                        +   "Name: '" + name + "', value: '" + value + "'");
                            }
                        }
                    }
                }
                else if ("comment".equals(qName)) {
                    // if comment_text is not empty, this is a top level comment
                    if (comment_text.length() > 0) {
                        if(!exportCommentsToLayer.isEmpty()) {
                            text().layer(exportCommentsToLayer).add_comment(comment_text.toString(), comment_type);
                            if (tokenization_is_segmentation) {
                                text().map_tokens_to_timeline_aligned();
                            } else
                                text().map_tokens_to_timeline_simple();
                        }
                        comment_text = new StringBuffer();
                    }
                }
                getXMLELementStack().pop();
        }
    }

}

