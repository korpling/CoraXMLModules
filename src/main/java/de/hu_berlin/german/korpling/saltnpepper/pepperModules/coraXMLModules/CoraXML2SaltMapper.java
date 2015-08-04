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

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.apache.commons.lang3.StringEscapeUtils;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SOrderRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;

public class CoraXML2SaltMapper extends PepperMapperImpl implements PepperMapper, CoraXMLDictionary
{

   /** defines which textual representation is used for the dipl and mod tokens **/
   private String mod_tok_textlayer = ATT_ASCII; // one of "trans", "utf" and "ascii"
   private String dipl_tok_textlayer = ATT_UTF;  // one of "trans" and "utf"

   /** defines whether the token layer should be exported **/
   private boolean exportTokenLayer = true;

   /**
    * {@inheritDoc PepperMapper#setSDocument(SDocument)}
    *
    * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
    */
   @Override
   public DOCUMENT_STATUS mapSDocument() {
      if (this.getSDocument().getSDocumentGraph()== null){
         this.getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
      }
      CoraXMLReader reader= new CoraXMLReader();
      this.readXMLResource(reader, this.getResourceURI());

      return(DOCUMENT_STATUS.COMPLETED);
   }

   class CoraXMLReader extends DefaultHandler2
   {
      /** stores start for line **/
      Hashtable<String, SSpan> lineStart= new Hashtable<String, SSpan>();
      /** stores start for line **/
      Hashtable<String, SSpan> lineEnd= new Hashtable<String, SSpan>();
      /** stores all {@link SSpan} objects to be linked to given dipl {@link SSpan} (key= dipl, value, list of open {@link SSpan}s)**/
      Multimap<SSpan, SSpan> dipl2SSpan= HashMultimap.create();


      /** stores start for column **/
      Hashtable<String, SSpan> columnStart= new Hashtable<String, SSpan>();
      /** stores start for column **/
      Hashtable<String, SSpan> columnEnd= new Hashtable<String, SSpan>();

      /** stores start for page **/
      Hashtable<String, SSpan> pageStart= new Hashtable<String, SSpan>();
      /** stores start for page **/
      Hashtable<String, SSpan> pageEnd= new Hashtable<String, SSpan>();

      private List<SSpan> openDipls= null;
      /** returns all open {@link CoraXMLDictionary#TAG_DIPL} which are related to a {@link SSpan} and are not connected with {@link SToken} objects so far **/
      private List<SSpan> getOpenDipls(){
         if (openDipls== null)
            openDipls= new Vector<SSpan>();
         return(openDipls);
      }
      private void resetOpenDipls(){
         openDipls= null;
      }
      private List<SSpan> openMods= null;
      /** returns all open {@link CoraXMLDictionary#TAG_MOD} which are related to a {@link SSpan} and are not connected with {@link SToken} objects so far **/
      private List<SSpan> getOpenMods(){
         if (openMods== null)
            openMods= new Vector<SSpan>();
         return(openMods);
      }
      private void resetOpenMods(){
         openMods= null;
      }

      private SSpan openToken= null;

      private Stack<SNode> sNodeStack= null;
      /** returns stack containing node hierarchie**/
      private Stack<SNode> getSNodeStack(){
         if (sNodeStack== null)
            sNodeStack= new Stack<SNode>();
         return(sNodeStack);
      }
      private Stack<String> xmlElementStack= null;
      /** returns stack containing xml-element hierarchie**/
      private Stack<String> getXMLELementStack(){
         if (xmlElementStack== null)
            xmlElementStack= new Stack<String>();
         return(xmlElementStack);
      }

      /** number of read pos in suggestion //suggestions/pos **/
      private int sugPos=1;
      private int sugPosLemma=1;
      /** number of read lemma in suggestion //suggestions/lemma **/
      private int sugLemma=1;
      /** number of read morph in suggestion //suggestions/morph **/
      private int sugMorph=1;

      /** name of segmentation identified by {@link CoraXMLDictionary#TAG_MOD}**/
      public static final String SEGMENTATION_NAME_MOD="tok_mod";
      /** name of segmentation identified by {@link CoraXMLDictionary#TAG_DIPL}**/
      public static final String SEGMENTATION_NAME_DIPL="tok_dipl";
      /** name of segmentation identified by {@link CoraXMLDictionary#TAG_TOKEN}**/
      public static final String SEGMENTATION_NAME_TOKEN="token";

      /** stores last segmentation units**/
      private SSpan last_mod=null;
      private SSpan last_dipl=null;
      private SSpan last_token=null;

      /** stores current line**/
      private SSpan currentLine= null;
      /** stores current column**/
      private SSpan currentColumn= null;
      /** stores current page**/
      private SSpan currentPage= null;
      private char current_column = 'a';

      // this method name is a bit misleading, it only escapes the ATT_TAG attribute
      // and returns it as String
      private String unescape(Attributes attributes) {
          return StringEscapeUtils.unescapeHtml4(attributes.getValue(ATT_TAG));
      }

    private void addSimpleRow(String name, Attributes attributes) {
        addSimpleRow(name, attributes, null);
    }

    private void addSimpleRow(String name, Attributes attributes, String ns) {
        getSNodeStack().peek().createSAnnotation(ns, name, attributes.getValue(ATT_TAG));
    }

    private void addOrderRelation(SSpan source, SSpan target, String type) {
        if (source != null) {
            SOrderRelation orderRel = SaltFactory.eINSTANCE.createSOrderRelation();
            orderRel.setSSource(source);
            orderRel.setSTarget(target);
            orderRel.addSType(type);
            getSDocument().getSDocumentGraph().addSRelation(orderRel);
        }
    }

      @Override
      public void startElement(   String uri,
                              String localName,
                              String qName,
                              Attributes attributes)throws SAXException
       {
         if (TAG_LAYOUTINFO.equals(qName)){
         }
         else if (TAG_FM.equals(qName)){
         }
         else if (TAG_TEXT.equals(qName)){
         }
         else if (exportTokenLayer && TAG_TOKEN.equals(qName)){
            openToken= SaltFactory.eINSTANCE.createSSpan();
            openToken.createSAnnotation(null,
                                        SEGMENTATION_NAME_TOKEN,
                                        StringEscapeUtils.unescapeHtml4(attributes.getValue(ATT_TRANS)));
            getSDocument().getSDocumentGraph().addSNode(openToken);
            addOrderRelation(last_token, openToken, SEGMENTATION_NAME_TOKEN);
            last_token = openToken;
            getSNodeStack().add(openToken);

         }
         else if (TAG_PAGE.equals(qName)){
            SSpan colSpan= SaltFactory.eINSTANCE.createSSpan();
            // TODO there should be a span for the page no independent of the side
            colSpan.createSAnnotation(null, ATT_SIDE, attributes.getValue(ATT_SIDE));
            colSpan.createSAnnotation(null, "page", attributes.getValue(ATT_NO));

            getSDocument().getSDocumentGraph().addSNode(colSpan);
            String[] parts= attributes.getValue(ATT_RANGE).split("[.][.]");
            if (parts.length>= 1){
               pageStart.put(parts[0], colSpan);
               if (parts.length== 2)
                  pageEnd.put(parts[1], colSpan);
               else pageEnd.put(parts[0], colSpan);
            }
         }
         else if (TAG_SHIFTTAGS.equals(qName)){
         }
         else if (TAG_MOD.equals(qName)){
            SSpan span= SaltFactory.eINSTANCE.createSSpan();
            span.createSAnnotation(null, SEGMENTATION_NAME_MOD, StringEscapeUtils.unescapeHtml4(attributes.getValue(mod_tok_textlayer)));
            getSDocument().getSDocumentGraph().addSNode(span);
            addOrderRelation(last_mod, span, SEGMENTATION_NAME_MOD);
            last_mod = span;
            this.getOpenMods().add(span);
            getSNodeStack().add(span);
         }
         else if (TAG_DIPL.equals(qName)){
            SSpan span= SaltFactory.eINSTANCE.createSSpan();
            span.createSAnnotation(null, SEGMENTATION_NAME_DIPL, StringEscapeUtils.unescapeHtml4(attributes.getValue(dipl_tok_textlayer)));
            getSDocument().getSDocumentGraph().addSNode(span);
            addOrderRelation(last_dipl, span, SEGMENTATION_NAME_DIPL);
            last_dipl = span;
            this.getOpenDipls().add(span);
            getSNodeStack().add(span);

            String id= attributes.getValue(ATT_ID);
            if (lineStart.get(id)!= null)
               currentLine= lineStart.get(id);

            dipl2SSpan.put(span, currentLine);
            if (lineEnd.get(id)!= null)
               currentLine= null;

            //switch column links to line identifier
               if (columnStart.get(id)!= null)
                  currentColumn= columnStart.get(id);

               dipl2SSpan.put(span, currentColumn);
               if (columnEnd.get(id)!= null)
                  currentColumn= null;
            //switch column links to line identifier

            //switch page links to column identifier
               if (pageStart.get(id)!= null)
                  currentPage= pageStart.get(id);

               dipl2SSpan.put(span, currentPage);
               if (pageEnd.get(id)!= null)
                  currentPage= null;
            //switch page links to column identifier
         }
         else if (TAG_COLUMN.equals(qName)){
            String[] parts= attributes.getValue(ATT_RANGE).split("[.][.]");
            String start= null;
            String end= null;
            if (parts.length>= 1){
               start= parts[0];
               if (parts.length== 2)
                  end= parts[1];
               else end= parts[0];
            }

            String id= attributes.getValue(ATT_ID);
            //switch page links to column identifier
               SSpan pageSpan= pageStart.get(id);
               if (pageSpan!= null)
               {
                  pageStart.remove(id);
                  pageStart.put(start, pageSpan);
		  // reset column count
		  current_column = 'a';
               }
               pageSpan= pageEnd.get(id);
               if (pageSpan!= null)
               {
                  pageEnd.remove(id);
                  pageEnd.put(end, pageSpan);
               }
            //switch page links to column identifier

            SSpan colSpan= SaltFactory.eINSTANCE.createSSpan();
            //colSpan.createSAnnotation(null, TAG_COLUMN, attributes.getValue(ATT_ID));
            // ATT_ID contains the id of the column element (e.g. "c1"), but this has to be changed
            // to alphabetic numbering ("c1" -> "a")
            colSpan.createSAnnotation(null, TAG_COLUMN, Character.toString(current_column++));

            getSDocument().getSDocumentGraph().addSNode(colSpan);

            columnStart.put(start, colSpan);
            columnEnd.put(end, colSpan);
         }
         else if (TAG_LINE.equals(qName)){
            SSpan sSpan= SaltFactory.eINSTANCE.createSSpan();
            sSpan.createSAnnotation(null, TAG_LINE, attributes.getValue(ATT_NAME));
            getSDocument().getSDocumentGraph().addSNode(sSpan);
            String[] parts= attributes.getValue(ATT_RANGE).split("[.][.]");
            String start= null;
            String end= null;
            if (parts.length>= 1){
               start= parts[0];
               if (parts.length== 2)
                  end= parts[1];
               else end= parts[0];
            }
            lineStart.put(start, sSpan);
            lineEnd.put(end, sSpan);

            String id= attributes.getValue(ATT_ID);

            SSpan column= columnStart.get(id);
            if (column!= null)
            {
               columnStart.remove(id);
               columnStart.put(start, column);
            }
            column= columnEnd.get(id);
            if (column!= null)
            {
               columnEnd.remove(id);
               columnEnd.put(end, column);
            }

            SSpan pageSpan= pageStart.get(id);
            if (pageSpan!= null)
            {
               pageStart.remove(id);
               pageStart.put(start, pageSpan);
            }
            pageSpan= pageEnd.get(id);
            if (pageSpan!= null)
            {
               pageEnd.remove(id);
               pageEnd.put(end, pageSpan);
            }
         }
         else if (TAG_COMMENT.equals(qName)){
         }
         else if (TAG_HEADER.equals(qName)){
         }
         else if (TAG_POS.equals(qName)){
            if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek()))
            {
               SAnnotation sAnno= getSNodeStack().peek().createSAnnotation(TAG_SUGGESTIONS, TAG_POS+"_"+sugPos, unescape(attributes));
               if (attributes.getValue(ATT_SCORE)!= null){
                  SAnnotation scoreAnno= SaltFactory.eINSTANCE.createSAnnotation();
                  scoreAnno.setSName(ATT_SCORE);
                  scoreAnno.setSValue(attributes.getValue(ATT_SCORE));
                  sAnno.addLabel(scoreAnno);
               }
               sugPos++;
            }
            else
                                    addSimpleRow(TAG_POS, attributes);
         }
    else if ("intern_pos_gen".equals(qName)) {
        addSimpleRow("posLemma_intern", attributes);
    } else if ("intern_pos".equals(qName)) {
        addSimpleRow("pos_intern", attributes);
    } else if ("intern_infl".equals(qName)) {
        addSimpleRow("inflection_intern", attributes);
    }
    else if (TAG_POS_LEMMA.equals(qName)) {
        if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek())) {
            SAnnotation sAnno = getSNodeStack().peek().createSAnnotation(TAG_SUGGESTIONS, TAG_POS + "_" + sugPosLemma, unescape(attributes));
            if (attributes.getValue(ATT_SCORE) != null) {
                SAnnotation scoreAnno = SaltFactory.eINSTANCE.createSAnnotation();
                scoreAnno.setSName(ATT_SCORE);
                scoreAnno.setSValue(attributes.getValue(ATT_SCORE));
                sAnno.addLabel(scoreAnno);
            }
            sugPosLemma++;
        } else {
            addSimpleRow("posLemma", attributes);
        }
    }
    else if (TAG_NORM.equals(qName) || TAG_NORMBROAD.equals(qName)) {
        String ann_name = TAG_NORM.equals(qName) ? "norm" : "modern";
        addSimpleRow(ann_name, attributes);
    }
    else if (TAG_INFLCLASS.equals(qName)) {
        addSimpleRow("inflectionClass", attributes);
    }
    else if (TAG_INFLCLASS_LEMMA.equals(qName)) {
        addSimpleRow("inflectionClassLemma", attributes);
    }
    else if (TAG_INFL.equals(qName)) {
        addSimpleRow("inflection", attributes);
    }
    else if (TAG_NORMALIGN.equals(qName) || TAG_NORMALIGN_VARIANT.equals(qName)) {
        addSimpleRow("char_align", attributes);
    }
    else if (TAG_LEMMA_ID.equals(qName)) {
        /*
        StringBuilder sb = new StringBuilder();
        sb.append("http://www.mhdwb-online.de/lemmaliste/");
        sb.append(attributes.getValue(ATT_TAG));
        getSNodeStack().peek().createSAnnotation(null, "lemmaId", sb.toString());
        */
        addSimpleRow("lemmaId", attributes);
    }
    else if ("lemma_gen".equals(qName)) {
        addSimpleRow("lemmaLemma", attributes);
    }
         else if (TAG_LEMMA.equals(qName)){
            if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek()))
            {
               SAnnotation sAnno= getSNodeStack().peek().createSAnnotation(TAG_SUGGESTIONS,
                       TAG_LEMMA+"_"+sugLemma,
                       unescape(attributes));
               if (attributes.getValue(ATT_SCORE)!= null){
                  SAnnotation scoreAnno= SaltFactory.eINSTANCE.createSAnnotation();
                  scoreAnno.setSName(ATT_SCORE);
                  scoreAnno.setSValue(attributes.getValue(ATT_SCORE));
                  sAnno.addLabel(scoreAnno);
               }
               sugLemma++;
            }
            else
                                    addSimpleRow(TAG_LEMMA, attributes);
         }
         else if (TAG_MORPH.equals(qName)){
            if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek()))
            {
               SAnnotation sAnno= getSNodeStack().peek().createSAnnotation(TAG_SUGGESTIONS,
                       TAG_MORPH+"_"+sugMorph,
                       attributes.getValue(ATT_TAG));
               if (attributes.getValue(ATT_SCORE)!= null){
                  SAnnotation scoreAnno= SaltFactory.eINSTANCE.createSAnnotation();
                  scoreAnno.setSName(ATT_SCORE);
                  scoreAnno.setSValue(attributes.getValue(ATT_SCORE));
                  sAnno.addLabel(scoreAnno);
               }
               sugMorph++;
            }
            else
                                    addSimpleRow(TAG_MORPH, attributes);
         }
         //must be at the end, that one before last is always on top when processing
         getXMLELementStack().push(qName);
       }

      private STextualDS sTextualDS= null;
      private STextualDS getSTextualDS(){
         if (sTextualDS== null){
            sTextualDS= SaltFactory.eINSTANCE.createSTextualDS();
            sTextualDS.setSText("");
            getSDocument().getSDocumentGraph().addSNode(sTextualDS);
         }
         return(sTextualDS);
      }

      @Override
      public void characters(   char[] ch,
                           int start,
                           int length) throws SAXException
      {
         if (TAG_HEADER.equals(getXMLELementStack().peek())){
            StringBuffer textBuf= new StringBuffer();
            for (int i= start; i< start+length; i++)
               textBuf.append(ch[i]);

            String text= textBuf.toString();

            String[] lines= text.split("\n");
            for (String line: lines)
            {
               line= line.trim();
               if (!line.isEmpty())
               {
                  String[]  parts= line.split(":");
                  if (parts.length >= 1){
                     String sName= parts[0].trim();
                     String sValue=null;
                     if (parts.length == 2)
                        sValue= parts[1].trim();
                     if (!getSDocument().hasLabel(sName))
                        getSDocument().createSMetaAnnotation(null, sName, sValue);
                  }
               }
            }
         }
      }
      private void span_on_tok(SSpan span, SToken tok) {
          SSpanningRelation rel = SaltFactory.eINSTANCE.createSSpanningRelation();
          rel.setSSpan(span);
          rel.setSToken(tok);
          getSDocument().getSDocumentGraph().addSRelation(rel);
      }

      @Override
      public void endElement(String uri, String localName, String qName)
              throws SAXException {
         if (TAG_TOKEN.equals(qName)) {
             int dipl_pos = 0, mod_pos = 0;
             // initialization is actually unnecessary, but if i don't do it,
             // eclipse gives warnings
             SSpan last_dipl = null,
                   last_mod = null;
             Collection<SSpan> layout_spans = null;
             // dipl/mod/token creation works like this:
             // a dummy SToken is created with textual " " at the current text position
             // dipl/mod/token are all connected to this dummy SToken with an SRelation
             // all layout spans are retrieved from dipl2sspans and also connected to the SToken
             while (dipl_pos < getOpenDipls().size() || mod_pos < getOpenMods().size()) {
                 if (dipl_pos < getOpenDipls().size()) {
                     dipl2SSpan.removeAll(last_dipl);
                     last_dipl = getOpenDipls().get(dipl_pos++);
                     layout_spans = dipl2SSpan.get(last_dipl);
                 }
                 if (mod_pos < getOpenMods().size())
                     last_mod = getOpenMods().get(mod_pos++);
                 // increment the length of the (dummy) text object
                 int left_pos = getSTextualDS().getSText().length();
                 getSTextualDS().setSText(getSTextualDS().getSText() + " ");
                 int right_pos = getSTextualDS().getSText().length();
                 // create a tok to span dipl/mod/token on
                 SToken tok = getSDocument().getSDocumentGraph().createSToken(sTextualDS, left_pos, right_pos);
                 // span the token on the tok
                 if (exportTokenLayer) {
                     span_on_tok(openToken, tok);
                 }
                 // span the last retrieved dipl on the tok
                 span_on_tok(last_dipl, tok);
                 // connect the dipl to line/column/... spans
                 if (layout_spans != null)
                     for (SSpan layout_span : layout_spans)
                         span_on_tok(layout_span, tok);

                 // span the last retrieved mod on the tok
                 span_on_tok(last_mod, tok);
             }

            resetOpenDipls();
            resetOpenMods();
            if (exportTokenLayer) {
                getSNodeStack().pop();
            }
         }// tag is TAG_TOKEN
         else if (TAG_DIPL.equals(qName))
         {// tag is TAG_DIPL
            getSNodeStack().pop();
         }// tag is TAG_DIPL
         else if (TAG_MOD.equals(qName))
         {// tag is TAG_DIPL
            getSNodeStack().pop();
         }// tag is TAG_DIPL
         else if (TAG_SUGGESTIONS.equals(qName)){
            sugLemma= 0;
            sugPos= 0;
            sugPosLemma = 0;
            sugMorph= 0;
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

}
