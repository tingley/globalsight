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
package com.globalsight.util.gxml;

import org.apache.log4j.Logger;

import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.PrsReader;

import com.globalsight.util.GeneralException;

import java.util.Enumeration;
import java.util.Hashtable;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;

/**
 * Parses a GXML document fragment using SAX returning the result as a
 * GxmlElement structure.
 */
public class GxmlFragmentReader
    extends PrsReader
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            GxmlFragmentReader.class);

    /**
     * Use GxmlFragmentReaderPool to get a new instance.
     */
    GxmlFragmentReader() throws GxmlException
    {
        super();
    }

    //
    //  Begin:  public class API
    //

    /**
     * <p>Parses the content of a GXML document fragment and save the
     * parsing result into a GxmlElement structure.</p>
     *
     * @param p_content - The content of a GXML document fragment
     * represented by a String
     * @return a GxmlElement instance holding the whole parsing result
     * @throws GxmlException a Gxml component Exception
     */
    public GxmlElement parseFragment(String p_content)
        throws GxmlException
    {
        super.parse(p_content);
        // return whatever was parsed as the root element.
        return m_root;
    }

    //
    //  Begin:  SAX Event handlers, not public APIS
    //

    /**
     * The SAX characters event handler.
     * It appends the characters to the current text String.
     */
    public void characters(char buf[], int offset, int len)
    {
        // In a fragment, don't ignore text unless m_currentElement is null
        if (m_currentElement != null)
        {
            super.characters(buf, offset, len);
        }
    }

    /**
     * The SAX startElement event handler.
     * For most of the tags, just create the global instance of
     * corresponding element type and fill in the attributes.
     */
    public void startElement(String p_namespaceURI, String p_localName,
        String p_qName, Attributes p_attrs)
        throws SAXException
    {
        GxmlElement tempElement = null;

        String name = p_qName;

        try
        {
            m_ignoreText = false;

            // order by most fequent occurence
            if (GxmlNames.BPT.equals(name))
            {
                tempElement = new GxmlElement(GxmlElement.BPT, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.EPT.equals(name))
            {
                tempElement = new GxmlElement(GxmlElement.EPT, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.PH.equals(name))
            {
                tempElement = new GxmlElement(GxmlElement.PH, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.IT.equals(name))
            {
                tempElement = new GxmlElement(GxmlElement.IT, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.UT.equals(name))
            {
                tempElement = new GxmlElement(GxmlElement.UT, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.SUB.equals(name))
            {
                tempElement = new GxmlElement(GxmlElement.SUB, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }

            super.startElement(p_namespaceURI, p_localName, p_qName, p_attrs);
        }
        catch (SAXException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            CATEGORY.error("Unexpected error in element " + name, e);
            throw new SAXException(e);
        }
    }

    /**
     * The SAX endElement event handler
     */
    public void endElement(String p_namespaceURI, String p_localName,
        String p_qName)
        throws SAXException
    {
        String name = p_qName;

        try
        {
            if (GxmlNames.BPT.equals(name) ||
                GxmlNames.EPT.equals(name) ||
                GxmlNames.PH.equals(name) ||
                GxmlNames.IT.equals(name) ||
                GxmlNames.UT.equals(name) ||
                GxmlNames.SUB.equals(name))
            {
                m_ignoreText = false;

                // First end the TEXT_NODE by making the current node
                // its parent, then end the localName element.
                if (m_currentElement.getType() == GxmlElement.TEXT_NODE)
                {
                    m_currentElement = m_currentElement.getParent();
                }

                m_currentElement = m_currentElement.getParent();

                return;
            }

            super.endElement(p_namespaceURI, p_localName, p_qName);
        }
        catch (SAXException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            CATEGORY.error("Unexpected error in element " + name, e);
            throw new SAXException(e);
        }
    }

    public void setDocumentLocator (Locator locator) {};
    public void startDocument () throws SAXException {};
    public void endDocument() throws SAXException {};
    public void startPrefixMapping (String prefix, String uri)
        throws SAXException {};
    public void endPrefixMapping (String prefix) throws SAXException {};
    public void ignorableWhitespace (char ch[], int start, int length)
        throws SAXException {};
    public void processingInstruction (String target, String data)
        throws SAXException {};
    public void skippedEntity (String name) throws SAXException {};

    //
    //  End:  SAX Event handlers
    //

    //
    //  Begin:  Test Block
    //
    /*
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("USAGE:   java " +
                GxmlFragmentReader.class.getName() +
                " <fully qualified path name of Gxml fragment file>");
            return;
        }

        String content = "";

        try
        {
            GxmlFragmentReader reader = new GxmlFragmentReader();
            content = readFileToString(args[0]);
            CATEGORY.debug(content);
            GxmlElement root = reader.parse(content);
            CATEGORY.debug(root.toString());
            System.err.println(root.toString());
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
    }
    */
}
