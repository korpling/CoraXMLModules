package de.hu_berlin.german.korpling.saltnpepper.pepperModules.sampleModules;

import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.omg.IOP.TAG_INTERNET_IOP;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.MAPPING_RESULT;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;

public class CoraXML2SaltMapper extends PepperMapperImpl implements PepperMapper, CoraXMLDictionary 
{	
	/**
	 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
	 * 
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public MAPPING_RESULT mapSDocument() {
		if (this.getSDocument().getSDocumentGraph()== null)
			this.getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		try
		{
			CoraXMLReader reader= new CoraXMLReader();
			this.readXMLResource(reader, this.getResourceURI());
		}catch (Exception e)
		{
			//TODO remove this
			e.printStackTrace();
			return(MAPPING_RESULT.FAILED);
		}
		return(MAPPING_RESULT.FINISHED); 
	}
	
	class CoraXMLReader extends DefaultHandler2
	{
		/** stores start for line **/
		Hashtable<String, SSpan> lineStart= new Hashtable<String, SSpan>();
		/** stores start for line **/
		Hashtable<String, SSpan> lineEnd= new Hashtable<String, SSpan>();
		Hashtable<SSpan, SSpan> dipl2line= new Hashtable<SSpan, SSpan>();
		
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
		/** number of read lemma in suggestion //suggestions/lemma **/
		private int sugLemma=1;
		/** number of read morph in suggestion //suggestions/morph **/
		private int sugMorph=1;
		
		/** name of segmentation identified by {@link CoraXMLDictionary#TAG_MOD}**/
		public static final String SEGMENTATION_NAME_MOD="mod";
		/** name of segmentation identified by {@link CoraXMLDictionary#TAG_DIPL}**/
		public static final String SEGMENTATION_NAME_DIPL="dipl";
		/** name of segmentation identified by {@link CoraXMLDictionary#TAG_TOKEN}**/
		public static final String SEGMENTATION_NAME_TOKEN="token";
		
		/** stores current line**/
		private SSpan currentLine= null;
		
		@Override
		public void startElement(	String uri,
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
			else if (TAG_TOKEN.equals(qName)){
				openToken= SaltFactory.eINSTANCE.createSSpan();
				openToken.createSAnnotation(null, SEGMENTATION_NAME_TOKEN, attributes.getValue(ATT_TRANS));
				getSDocument().getSDocumentGraph().addSNode(openToken);
				getSNodeStack().add(openToken);
				
			}
			else if (TAG_PAGE.equals(qName)){
				
			}
			else if (TAG_SHIFTTAGS.equals(qName)){
			}
			else if (TAG_MOD.equals(qName)){
				SSpan span= SaltFactory.eINSTANCE.createSSpan();
				span.createSAnnotation(null, SEGMENTATION_NAME_MOD, attributes.getValue(ATT_UTF));
				getSDocument().getSDocumentGraph().addSNode(span);
				this.getOpenMods().add(span);
				getSNodeStack().add(span);
			}
			else if (TAG_DIPL.equals(qName)){
				SSpan span= SaltFactory.eINSTANCE.createSSpan();
				span.createSAnnotation(null, SEGMENTATION_NAME_DIPL, attributes.getValue(ATT_UTF));
				getSDocument().getSDocumentGraph().addSNode(span);
				this.getOpenDipls().add(span);
				getSNodeStack().add(span);
				
				String id= attributes.getValue(ATT_ID);
				if (lineStart.get(id)!= null)
					currentLine= lineStart.get(id);
				
				dipl2line.put(span, currentLine);
				if (lineEnd.get(id)!= null)
					currentLine= null;
			}
			else if (TAG_COLUMN.equals(qName)){
				SSpan sSpan= SaltFactory.eINSTANCE.createSSpan();
				sSpan.createSAnnotation(null, ATT_ID, attributes.getValue(TAG_COLUMN));
				getSDocument().getSDocumentGraph().addSNode(sSpan);
				String[] parts= attributes.getValue(ATT_RANGE).split("[.][.]");
				if (parts.length>= 1){
					lineStart.put(parts[0], sSpan);
					if (parts.length== 2)
						lineEnd.put(parts[1], sSpan);
					else lineEnd.put(parts[0], sSpan);
				}
			}
			else if (TAG_LINE.equals(qName)){
				SSpan sSpan= SaltFactory.eINSTANCE.createSSpan();
				sSpan.createSAnnotation(null, TAG_LINE, attributes.getValue(ATT_NAME));
				getSDocument().getSDocumentGraph().addSNode(sSpan);
				String[] parts= attributes.getValue(ATT_RANGE).split("[.][.]");
				if (parts.length>= 1){
					lineStart.put(parts[0], sSpan);
					if (parts.length== 2)
						lineEnd.put(parts[1], sSpan);
					else lineEnd.put(parts[0], sSpan);
				}
			}
			else if (TAG_COMMENT.equals(qName)){
			}
			else if (TAG_HEADER.equals(qName)){
			}
			else if (TAG_POS.equals(qName)){
				if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek()))
				{
					SAnnotation sAnno= getSNodeStack().peek().createSAnnotation(TAG_SUGGESTIONS, TAG_POS+"_"+sugPos, attributes.getValue(ATT_TAG));
					if (attributes.getValue(ATT_SCORE)!= null){
						SAnnotation scoreAnno= SaltFactory.eINSTANCE.createSAnnotation();
						scoreAnno.setSName(ATT_SCORE);
						scoreAnno.setSValue(attributes.getValue(ATT_SCORE));
						sAnno.addLabel(scoreAnno);
					}
					sugPos++;
				}
				else
					getSNodeStack().peek().createSAnnotation(null, TAG_POS, attributes.getValue(ATT_TAG));
			}
			else if (TAG_LEMMA.equals(qName)){
				if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek()))
				{
					SAnnotation sAnno= getSNodeStack().peek().createSAnnotation(TAG_SUGGESTIONS, TAG_LEMMA+"_"+sugLemma, attributes.getValue(ATT_TAG));
					if (attributes.getValue(ATT_SCORE)!= null){
						SAnnotation scoreAnno= SaltFactory.eINSTANCE.createSAnnotation();
						scoreAnno.setSName(ATT_SCORE);
						scoreAnno.setSValue(attributes.getValue(ATT_SCORE));
						sAnno.addLabel(scoreAnno);
					}
					sugLemma++;
				}
				else
					getSNodeStack().peek().createSAnnotation(null, TAG_LEMMA, attributes.getValue(ATT_TAG));	
			}
			else if (TAG_MORPH.equals(qName)){
				if (TAG_SUGGESTIONS.equals(getXMLELementStack().peek()))
				{
					SAnnotation sAnno= getSNodeStack().peek().createSAnnotation(TAG_SUGGESTIONS, TAG_MORPH+"_"+sugMorph, attributes.getValue(ATT_TAG));
					if (attributes.getValue(ATT_SCORE)!= null){
						SAnnotation scoreAnno= SaltFactory.eINSTANCE.createSAnnotation();
						scoreAnno.setSName(ATT_SCORE);
						scoreAnno.setSValue(attributes.getValue(ATT_SCORE));
						sAnno.addLabel(scoreAnno);
					}
					sugMorph++;
				}
				else
					getSNodeStack().peek().createSAnnotation(null, TAG_MORPH, attributes.getValue(ATT_TAG));
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
		public void characters(	char[] ch,
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
		
		@Override
		public void endElement(	String uri,
				String localName,
				String qName)throws SAXException
		{
			if (TAG_TOKEN.equals(qName))
			{// tag is TAG_TOKEN
				int numOfTokens= getOpenDipls().size();
				if (numOfTokens <  getOpenMods().size())
					numOfTokens= getOpenMods().size();
				for (int i= 0; i< numOfTokens; i++)
				{
					int leftPos= getSTextualDS().getSText().length();
					getSTextualDS().setSText(getSTextualDS().getSText()+" ");
					int rightPos= getSTextualDS().getSText().length();
					SToken sToken= getSDocument().getSDocumentGraph().createSToken(sTextualDS, leftPos, rightPos);
					
					if (openToken!= null)
					{
						SSpanningRelation sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
						sSpanRel.setSSpan(openToken);
						sSpanRel.setSToken(sToken);
						getSDocument().getSDocumentGraph().addSRelation(sSpanRel);
					}
					if (i < getOpenDipls().size())
					{
						SSpan diplSpan= getOpenDipls().get(i);
						SSpanningRelation sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
						sSpanRel.setSSpan(diplSpan);
						sSpanRel.setSToken(sToken);
						getSDocument().getSDocumentGraph().addSRelation(sSpanRel);
						
						SSpan lineSpan= dipl2line.get(diplSpan);
						dipl2line.remove(diplSpan);
						sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
						sSpanRel.setSSpan(lineSpan);
						sSpanRel.setSToken(sToken);
						getSDocument().getSDocumentGraph().addSRelation(sSpanRel);
						
					}
					
					if (i < getOpenMods().size())
					{
						SSpanningRelation sSpanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
						sSpanRel.setSSpan(getOpenMods().get(i));
						sSpanRel.setSToken(sToken);
						getSDocument().getSDocumentGraph().addSRelation(sSpanRel);
					}
				}
				resetOpenDipls();
				resetOpenMods();
				getSNodeStack().pop();
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
				sugMorph= 0;
			}
			getXMLELementStack().pop();
		}
	}
}
