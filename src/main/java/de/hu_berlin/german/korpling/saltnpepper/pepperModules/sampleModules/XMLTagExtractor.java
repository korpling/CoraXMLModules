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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.sampleModules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URI;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleXMLResourceException;

public class XMLTagExtractor extends DefaultHandler2 
{
	/** XML file to pasre **/
	private URI xmlResource= null;
	
	/** Sets xml file to be parsed. 
	 * @throws FileNotFoundException **/
	public void setXmlResource(URI resource) throws FileNotFoundException
	{
		if (resource== null)
			throw new NullPointerException("Cannot start extracting, xml resource is empty.");
		
		System.out.println("resource: "+ resource);
		
		File inFile= new File(resource.toString());
		if (!inFile.exists())
			throw new FileNotFoundException("Cannot start extracting, xml resource '"+inFile+"' does not exist.");
		this.xmlResource= resource;
	}
	/** returns xml file to be parsed. **/
	public URI getXmlResource()
	{
		return(this.xmlResource);
	}
	
	/** Java file to be outputted **/
	private URI javaResource= null;
	
	/** Sets java file to be parsed. 
	 * @throws FileNotFoundException **/
	public void setJavaResource(URI resource) throws FileNotFoundException
	{
		if (resource== null)
			throw new NullPointerException("Cannot start extracting, xml resource is empty.");
		File outFile= new File(resource.toString());
		if (!outFile.exists())
			throw new FileNotFoundException("Cannot start extracting, java resource '"+outFile+"' does not exist.");
		this.javaResource= resource;
	}
	/** returns java file to be parsed. **/
	public URI getJavaResource()
	{
		return(this.javaResource);
	}
	
	public static final String PREFIX_ELEMENT="TAG_";
	public static final String PREFIX_ATTRIBUTE="ATT_";
	
	public void extract()
	{
		File resourceFile= new File(getXmlResource().toString());
		if (!resourceFile.exists()) 
			throw new PepperModuleXMLResourceException("Cannot load a xml-resource, because the file does not exist: " + resourceFile);
		
		if (!resourceFile.canRead())
			throw new PepperModuleXMLResourceException("Cannot load a xml-resource, because the file can not be read: " + resourceFile);
		
		
		File outFile= new File(this.getJavaResource().toString()); 
		if (outFile.isDirectory())
		{
			String[] parts= resourceFile.getName().split("[.]");
			String outFileName= parts[0]+".java";
			outFile= new File(outFile.getAbsolutePath()+"/"+outFileName);
		}
		
		SAXParser parser;
        XMLReader xmlReader;
        
        SAXParserFactory factory= SAXParserFactory.newInstance();
        
        try
        {
       	parser= factory.newSAXParser();
	        xmlReader= parser.getXMLReader();
	        xmlReader.setContentHandler(this);
        } catch (ParserConfigurationException e) {
        	throw new PepperModuleXMLResourceException("Cannot load a xml-resource '"+resourceFile.getAbsolutePath()+"'.", e);
        }catch (Exception e) {
	    	throw new PepperModuleXMLResourceException("Cannot load a xml-resource '"+resourceFile.getAbsolutePath()+"'.", e);
		}
       
        
        try {
         	InputStream inputStream= new FileInputStream(resourceFile);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			xmlReader.parse(is);
        } catch (SAXException e) 
        {
        	
            try
            {
				parser= factory.newSAXParser();
		        xmlReader= parser.getXMLReader();
		        xmlReader.setContentHandler(this);
				xmlReader.parse(resourceFile.getAbsolutePath());
            }catch (Exception e1) {
            	throw new PepperModuleXMLResourceException("Cannot load a xml-resource '"+resourceFile.getAbsolutePath()+"'.", e1);
			}
		}
        catch (Exception e) {
			if (e instanceof PepperModuleException)
				throw (PepperModuleException)e;
			else throw new PepperModuleXMLResourceException("Cannot read xml-file'"+getXmlResource()+"', because of a nested exception. ",e);
		}
        
        PrintWriter writer = null;
        String dictionaryName= null;
        try
		{
        	writer= new PrintWriter(outFile, "UTF-8");
        	dictionaryName= outFile.getName().replace(".java", "");
        	writer.println("public interface "+dictionaryName+"{");
        	for (String tagName: getTagNames())
        	{
        		writer.println("\t\t/** constant to address the xml-element '"+tagName+"'. **/");
        		writer.println("\t\tpublic static final String "+PREFIX_ELEMENT+tagName.toUpperCase()+"= \""+tagName+"\";");
        	}
        	writer.println();
        	for (String attName: getAttributeNames())
        	{
        		writer.println("\t\t/** constant to address the xml-attribute '"+attName+"'. **/");
        		writer.println("\t\tpublic static final String "+PREFIX_ATTRIBUTE+attName.toUpperCase()+"= \""+attName+"\";");
        	}
        	writer.println("}");
            
		} catch (Exception e)
		{
			e.printStackTrace();
		}
        finally
        {
        	if (writer != null)
        		writer.close();
        }
        
        try
		{
        	outFile= new File(outFile.getAbsolutePath().replace(".java", "")+"Reader"+".java");
        	writer= new PrintWriter(outFile, "UTF-8");
        	writer.println("public class "+outFile.getName().replace(".java", "")+"implements "+dictionaryName+" {");
        	
        	writer.println("\t\t@Override");
        	writer.println("\t\tpublic void startElement(	String uri,");
        	writer.println("\t\t\t\tString localName,");
        	writer.println("\t\t\t\tString qName,");
        	writer.println("\t\t\t\tAttributes attributes)throws SAXException");
        	writer.println("\t\t{");
        	int i=0;
        	for (String tagName: getTagNames())
        	{
        		writer.print("\t\t\t");
        		if (i== 0)
        			writer.print("if");
        		else
        			writer.print("else if");
        		i++;
        		writer.println(" ("+PREFIX_ELEMENT+tagName.toUpperCase()+".equals(qName)){");
        		writer.println("\t\t\t}");
        	}
        	writer.println("\t\t}");
        	
        	writer.println("}");
            
		} catch (Exception e)
		{
			e.printStackTrace();
		}
        finally
        {
        	if (writer != null)
        		writer.close();
        }
	}
	
	/** containes all xml element names contained in xml file **/
	private HashSet<String> tagNames= null;
	/** returns all xml element names contained in xml file**/
	private HashSet<String> getTagNames()
	{
		if (tagNames== null)
			tagNames= new HashSet<String>();
		return(tagNames);
	}
	
	/** containes all xml element names contained in xml file **/
	private HashSet<String> attributeNames= null;
	/** returns all xml element names contained in xml file**/
	private HashSet<String> getAttributeNames()
	{
		if (attributeNames== null)
			attributeNames= new HashSet<String>();
		return(attributeNames);
	}
	
	@Override
	public void startElement(	String uri,
            					String localName,
            					String qName,
            					Attributes attributes)throws SAXException
    {
		getTagNames().add(qName);
		for (int i= 0; i< attributes.getLength(); i++)
		{
			getAttributeNames().add(attributes.getQName(i));
		}
    }
	
	public static final String ARG_INPUT="-i";
	public static final String ARG_OUTPUT="-o";
	
	public static void main(String[] args)
	{
		URI input= null;
		URI output= null;
		if (args!= null)
		{
			for (int i= 0; i< args.length; i++)
			{
				if (ARG_INPUT.equals(args[i]))
				{
					input= URI.create(args[i+1]);
					i++;
					
				}
				else if (ARG_OUTPUT.equals(args[i]))
				{
					output= URI.create(args[i+1]);
					i++;
				}
			}
		}
		
		XMLTagExtractor extractor= new XMLTagExtractor();
		try
		{
			extractor.setXmlResource(input);
			extractor.setJavaResource(output);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		extractor.extract();
	}
}
