/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.diplomat.util.previewUrlXml;

import com.globalsight.diplomat.util.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import java.util.StringTokenizer;

//SAX,DOM
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * PreviewUrlXmlSubstituter
 * <p>
 * This class can substitute values in a PreviewUrlXml string with values
 * from a source PrsXml string and a target PrsXml string.
 */
public class PreviewUrlXmlSubstituter
{
	private String m_previewUrlXml; //the original preview URL XML
	private String m_sourcePrsXml; //the source Paginated Result Set XML
	private String m_targetPrsXml; //the target Paginated Result Set XML
	private NodeList m_sourceColumns; //the columns from the source PrsXml
	private NodeList m_targetColumns; //the columns from the target PrsXml

	/**
	* Creates a PreviewUrlXmlSubstituter class.
	* @param The string of Preview URL XML
	* @param The source (original) string of Paginated Result Set Xml
	* @param The targe string of Paginated Result Set Xml
	*/ 
	public PreviewUrlXmlSubstituter (String p_previewUrlXml, String p_sourcePrsXml, String p_targetPrsXml)
	{
		m_previewUrlXml = p_previewUrlXml;
		m_sourcePrsXml = p_sourcePrsXml;
		m_targetPrsXml = p_targetPrsXml;
	}

	/**
	* Performs the substitution. Any parameter or value with "substitution_source" set to "source"
	* or "target" will have its value swapped with the value from the source or target Paginated
	* Result Set Xml string.
	* @throws PreviewUrlException if a parsing error occurs during the substitution
	* @return the substituted PreviewUrlXml string
	*/
	public String doSubstitution()
	throws PreviewUrlXmlException
	{
		StringReader sr;
		InputSource is;

		//create DOM objects from the XML
		try {
		sr = new StringReader(m_previewUrlXml);
		is = new InputSource(sr);
		DOMParser parser = new DOMParser();
		parser.setFeature("http://xml.org/sax/features/validation", false); //don't validate
		parser.parse(is);
		Document previewUrlXmlDoc = parser.getDocument();
		Element previewUrlXmlElement = previewUrlXmlDoc.getDocumentElement();

		sr = new StringReader(m_sourcePrsXml);
		is = new InputSource(sr);
		DOMParser parser2 = new DOMParser();
		parser2.setFeature("http://xml.org/sax/features/validation", false); //don't validate
		parser2.parse(is);
		Element sourcePrsXmlElement = parser2.getDocument().getDocumentElement();

		sr = new StringReader(m_targetPrsXml);
		is = new InputSource(sr);
		DOMParser parser3 = new DOMParser();
		parser3.setFeature("http://xml.org/sax/features/validation", false); //don't validate
		parser3.parse(is);
		Element targetPrsXmlElement = parser3.getDocument().getDocumentElement();

		//get the columns from the source and target PrsXml files
		m_sourceColumns = sourcePrsXmlElement.getElementsByTagName("column");
		m_targetColumns = targetPrsXmlElement.getElementsByTagName("column");

		NodeList parameterTags = previewUrlXmlElement.getElementsByTagName("parameter");
		theLogger.println(Logger.DEBUG_C,"There are " + parameterTags.getLength() + " parameter tags");

		NodeList valueTags = previewUrlXmlElement.getElementsByTagName("value");
		theLogger.println(Logger.DEBUG_C,"There are " + valueTags.getLength() + " value tags");

		substituteTags(parameterTags);
		substituteTags(valueTags);

		//now write the xml doc back to a string
		OutputFormat format = new OutputFormat (previewUrlXmlDoc);
		StringWriter sw = new StringWriter();
		XMLSerializer serial = new XMLSerializer(sw,format);
		serial.asDOMSerializer();
		serial.serialize(previewUrlXmlDoc.getDocumentElement());
		String xmlString = sw.toString();

		m_targetColumns = null;
		m_sourceColumns = null;
		return xmlString;
		}
		catch (org.xml.sax.SAXException sae){
			throw new PreviewUrlXmlException("Unable to parse XML. " + sae.getMessage());
		}
		catch (IOException ioe) {
			throw new PreviewUrlXmlException("Unable to read XML. " + ioe.getMessage());
		}
		catch (Exception exc) {
			throw new PreviewUrlXmlException(exc.getMessage());
		}
	}

	/** 
	* Substitutes the "parameter" or "value" tags in the preview URL XML
	* @param the NodeList corresponding to the parameter or value tags in the preview Url Xml
	*/
	private void substituteTags (NodeList tags)
	throws Exception
	{
		String subSource;
		Element tagElement;
		String variable;
		String tableName;
		String columnName;
		String subbedvalue;
		StringTokenizer st;
		Element e;
		NodeList contentNodes;
		Element contentNode;
		Node parentNode;
		Node newTag;

		for (int i=0; i < tags.getLength(); i++)
		{
			tagElement = (Element) tags.item(i);
			subSource = tagElement.getAttribute("substitution_source");
			if (subSource == null || subSource.equals("") ||
          subSource.equalsIgnoreCase("none"))
         continue;
			theLogger.println(Logger.DEBUG_C,"subSource is " + subSource);
			variable = tagElement.getFirstChild().getNodeValue();
			theLogger.println(Logger.DEBUG_C,"\tvariable is " + variable);
			st = new StringTokenizer(variable,".");
			tableName = st.nextToken();
			columnName = st.nextToken();
			theLogger.println(Logger.DEBUG_C,"Looking for new variable from table "+ tableName + " column " + columnName);
      boolean foundSomething = false;
      
			String newvariable = "";
			if (subSource.equalsIgnoreCase("source"))
			{
				for (int j = 0; j < m_sourceColumns.getLength(); j++)
				{
					e = (Element) m_sourceColumns.item(j);
					if (e.getAttribute("name").equalsIgnoreCase(columnName) &&
						e.getAttribute("tableName").equalsIgnoreCase(tableName))
					{
						contentNodes = e.getElementsByTagName("content");
						contentNode = (Element) contentNodes.item(0);
						subbedvalue = contentNode.getFirstChild().getNodeValue();
						theLogger.println(Logger.DEBUG_C,"\tsubbedvalue is " + subbedvalue);

						parentNode = tagElement.getParentNode();
						newTag = tagElement.cloneNode(true);
						theLogger.println(Logger.DEBUG_C,"old value is " + newTag.getFirstChild().getNodeValue());
						newTag.getFirstChild().setNodeValue(subbedvalue);
						theLogger.println(Logger.DEBUG_C,"new value is " + newTag.getFirstChild().getNodeValue());
						parentNode.replaceChild(newTag, tagElement);
            foundSomething = true;
					}
				}
			}
			else if (subSource.equalsIgnoreCase("target"))
			{
				for (int j = 0; j < m_targetColumns.getLength(); j++)
				{
					e = (Element) m_targetColumns.item(j);
					if (e.getAttribute("name").equalsIgnoreCase(columnName) &&
						e.getAttribute("tableName").equalsIgnoreCase(tableName))
					{
						contentNodes = e.getElementsByTagName("content");
						contentNode = (Element) contentNodes.item(0);
						subbedvalue = contentNode.getFirstChild().getNodeValue();
						theLogger.println(Logger.DEBUG_C,"\tsubbedvalue is " + subbedvalue);
						parentNode = tagElement.getParentNode();
						newTag = tagElement.cloneNode(true);
						theLogger.println(Logger.DEBUG_C,"old value is " + newTag.getFirstChild().getNodeValue());
						newTag.getFirstChild().setNodeValue(subbedvalue);
						theLogger.println(Logger.DEBUG_C,"new value is " + newTag.getFirstChild().getNodeValue());
						parentNode.replaceChild(newTag, tagElement);
            foundSomething = true;
					}
				}
			}

      if (!foundSomething)
         theLogger.println(Logger.DEBUG_D, "Couldn't find value!");
		}
	}

	private Logger theLogger = Logger.getLogger();

	public static void main (String args[])
	throws Exception
	{
		if (args.length != 3)
		{
			System.out.println("USAGE: PreviewUrlXmlSubstituter <previewUrlXml> <s_prsxml> <t_prsxml>");
			System.exit(0);
		}

                try {
                   Logger l = Logger.getLogger();
                   l.setLogname("PreviewUrlXmlSubstituter");
                   l.println(Logger.INFO, "PreviewUrlXmlSubstituter starting up.");
                }
                catch (IOException e) {
                   System.out.println("Cannot create log file.");
                   System.exit(1);
                }

		StringBuffer previewUrlXml = new StringBuffer("");
		StringBuffer sourcePrsXml = new StringBuffer("");
		StringBuffer targetPrsXml = new StringBuffer("");

		BufferedReader br;
		String s;

		br = new BufferedReader(new FileReader(args[0]));
		while ((s=br.readLine()) != null)
		{
			previewUrlXml.append(s);
			previewUrlXml.append("\n");
		}

		br = new BufferedReader(new FileReader(args[1]));
		while ((s=br.readLine()) != null)
		{
			sourcePrsXml.append(s);
			sourcePrsXml.append("\n");

		}


		br = new BufferedReader(new FileReader(args[2]));
		while ((s=br.readLine()) != null)
		{
			targetPrsXml.append(s);
			targetPrsXml.append("\n");
		}


		//at this point assume that both PrsXml files contain only
		//one record
		System.out.println("Got all three files. Now doing substitution.");
		PreviewUrlXmlSubstituter p = new PreviewUrlXmlSubstituter(previewUrlXml.toString(),
																  sourcePrsXml.toString(),
																  targetPrsXml.toString());
		String subbedPreviewUrlXml = p.doSubstitution();
		System.out.println("-------------------");
		System.out.println(subbedPreviewUrlXml);
		System.exit(0);
	}
}

