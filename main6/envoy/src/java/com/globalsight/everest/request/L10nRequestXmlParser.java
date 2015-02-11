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

package com.globalsight.everest.request;

// Core java classes
import java.io.IOException;
import java.io.BufferedReader;

import org.apache.log4j.Logger;

// Java extension classes
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


//globalsight

/**
 * This class provides parsing of the L10nRequestXml.  It retrieves
 * certain elements needed for starting a localization request.  Could
 * actually be used as a more generic parser - just wraps calls for
 * getting certain attributes and elements.
 */
public class L10nRequestXmlParser
{
    // used for logging purposes
    private static Logger c_logger =
        Logger.getLogger(
            L10nRequestXmlParser.class.getName());

    /**
     * the document being parsed
     */
    private Document m_doc = null;

    /**
     * Constructor - parses through the input and creates a DOM tree.
     *
     * @param p_br The buffered reader that contains the xml to be
     * parsed.
     * @exception RequestHandlerException A parsing error * occurred
     * when reading the xml.
     */
    public L10nRequestXmlParser(BufferedReader p_br)
        throws RequestHandlerException
    {

        // Parse the L10nRequestXml
        try
        {   
            InputSource is = new InputSource(p_br);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(is);
            m_doc = parser.getDocument();
        }
        catch (SAXException se)
        {
            c_logger.error("SAXException when parsing the L10nRequestXml", se);

            throw new RequestHandlerException(
                RequestHandlerException.MSG_FAILED_TO_PARSE_L10N_REQUEST_XML,
                null, se);
        }
        catch (IOException ioe)
        {
            c_logger.error("IOException when parsing the L10nRequestXml",ioe);

            throw new RequestHandlerException(
                RequestHandlerException.MSG_FAILED_TO_PARSE_L10N_REQUEST_XML,
                null, ioe);
        }
    }

    /**
     * Find the specified element in the DOM and return its value.
     *
     * @param p_element The element tag the attribute is associated
     * with.
     *
     * @return String The element value or null if the element
     * couldn't be found.
     */
    public String findElementInXml(String p_element)
    {
        Element elem = m_doc.getDocumentElement();
        String elemValue = null;

        //if the element being looked for is not the root element
        if (elem.getNodeName() != p_element)
        {
            NodeList  elements = m_doc.getDocumentElement().
                getElementsByTagName(p_element);

            if (elements.getLength() > 0)
            {
                elem = (Element)elements.item(0);
            }
        }
        if (elem != null)
        {
            Node n = elem.getFirstChild();
            boolean found = false;
            while (n != null && !found)
            {
                if (n.getNodeName() == "#text")
                {
                    elemValue = n.getNodeValue();
                    found = true;
                }
                n = n.getNextSibling();
            }
        }

        //will be the element value found or null
        return elemValue;
    }

    /**
     * Find the specified attribute in the DOM and return the value.
     *
     * @param p_element The element tag the attribute is associated
     * with.
     * @param p_attributeLookingFor The attribute tag that is being
     * looked for.
     *
     * @return String The attribute value or null if it couldn't be
     * found.
     */
    public String findAttributeInXml(String p_element,
        String p_attributeLookingFor)
    {
        String attrValue = null;
        Element elem = m_doc.getDocumentElement();

        //if didn't find it at the root
        if (elem.getNodeName() != p_element)
        {
            NodeList  elements = m_doc.getDocumentElement().
                getElementsByTagName(p_element);

            //should only be one
            if (elements.getLength() > 0)
            {
                elem = (Element)elements.item(0);
                NamedNodeMap attrs = elem.getAttributes();
            }
        }
        if (elem != null)
        {
            attrValue = elem.getAttribute(p_attributeLookingFor);
        }

        // returns the value of the attribute or null if it couldn't
        // be found
        return attrValue;
    }
}


