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
import com.globalsight.util.gxml.GxmlException;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.GxmlReader;

import com.globalsight.util.GeneralException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;


/**
 * <p>Parses a paginated result set using SAX parsing model, saving
 * the result into a GxmlElement instance.</P>
 *
 * <P>Algorithm Note:</P>
 * <OL>
 * <LI>Currently this parser ignores GSA tag</LI>
 * <LI>The parser does not parse sub-element type under
 * &lt;segment&gt; level, means, all the content between a
 * &lt;segment&gt; tag pair is taken as a single unit of text value
 * String. </LI>
 * </OL>
 */
public class PrsReader
    extends GxmlReader
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            PrsReader.class);

    /**
     * Use PrsReaderPool to get a new instance.
     */
    protected PrsReader()
        throws GxmlException
    {
        super();
    }

    //
    //  Begin:  public class API
    //

    /**
     * Parses the content of a PaginatedResultSet document and saves
     * the parsing result into a GxmlElement structure.
     *
     * @param p_content - The content of a GXML document fragment
     * represented by a String
     * @return a PrsRootElement instance holding the whole parsing
     * result
     * @throws GxmlException a Gxml component Exception
     */
    public PrsRootElement parsePRS(String p_content)
        throws GxmlException
    {
        return (PrsRootElement)super.parse(p_content);
    }

    //
    // Begin: SAX Event handlers, not public APIS
    //

    /**
     * The SAX startElement event handler For most of the tags, just
     * create the global instance of corresponding element type and
     * fill in the attributes.
     */
    public void startElement(String p_namespaceURI, String p_localName,
        String p_qName, Attributes p_attrs)
        throws SAXException
    {
        GxmlElement tempElement = null;

        String name = p_qName;

        try
        {
            // Prs root element - overwrites m_gxmlRoot, which should be null
            if (GxmlNames.PRS_ROOT.equals(name))
            {
                m_ignoreText = true;
                m_gxmlRoot = new PrsRootElement();
                fillCurrentElement(m_gxmlRoot, p_attrs);
                return;
            }
            // For a record Element starting tag
            else if (GxmlNames.RECORD.equals(name))
            {
                m_ignoreText = true;
                tempElement = new GxmlElement(GxmlElement.RECORD, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            // For acqSqlParm Element starting tag
            else if (GxmlNames.ACQSQLPARM.equals(name))
            {
                m_ignoreText = false;
                tempElement = new GxmlElement(GxmlElement.ACQSQLPARM,
                    name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            // For a column element starting tag
            else if (GxmlNames.COLUMN.equals(name))
            {
                m_ignoreText = true;
                tempElement = new GxmlElement(GxmlElement.COLUMN, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.COLUMN_HEADER.equals(name))
            {
                m_ignoreText = false;
                tempElement = new GxmlElement(GxmlElement.COLUMN_HEADER, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            // For a label element starting tag
            else if (GxmlNames.LABEL.equals(name))
            {
                m_ignoreText = false;
                tempElement = new GxmlElement(GxmlElement.LABEL, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.CONTEXT.equals(name))
            {
                m_ignoreText = true;
                tempElement = new GxmlElement(GxmlElement.CONTEXT, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.ROW.equals(name))
            {
                m_ignoreText = true;
                tempElement = new GxmlElement(GxmlElement.ROW, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            // For a content element starting tag
            else if (GxmlNames.CONTENT.equals(name))
            {
                m_ignoreText = false;
                tempElement = new GxmlElement(GxmlElement.CONTENT, name);
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
            // order by most fequently occuring
            if (GxmlNames.RECORD.equals(name) ||
                GxmlNames.ACQSQLPARM.equals(name) ||
                GxmlNames.COLUMN.equals(name) ||
                GxmlNames.COLUMN_HEADER.equals(name) ||
                GxmlNames.LABEL.equals(name) ||
                GxmlNames.CONTEXT.equals(name) ||
                GxmlNames.ROW.equals(name) ||
                GxmlNames.CONTENT.equals(name))
            {
                m_ignoreText = true;

                // First end the TEXT_NODE by making the current node
                // it's parent, then end the localName element.
                if (m_currentElement.getType() == GxmlElement.TEXT_NODE)
                {
                    m_currentElement = m_currentElement.getParent();
                }

                m_currentElement = m_currentElement.getParent();

                return;
            }
            else if (GxmlNames.PRS_ROOT.equals(name))
            {
                m_ignoreText = true;
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
    public void startDocument () throws SAXException { };
    public void endDocument() throws SAXException {};
    public void startPrefixMapping (String prefix, String uri)
        throws SAXException {};
    public void endPrefixMapping (String prefix) throws SAXException {};
    public void ignorableWhitespace (char ch[], int start, int length)
        throws SAXException {};
    public void processingInstruction (String target, String data)
        throws SAXException {};
    public void skippedEntity (String name) throws SAXException {};

    /////////////////////////////////////////////////////////////
    //  End:  SAX Event handlers, not public APIS
    /////////////////////////////////////////////////////////////

    /*
      /////////////////////////////////////////////////////////////
      //  Begin:  Test Block
      /////////////////////////////////////////////////////////////
      public static void main(String[] args)
      {
      if (args.length != 1)
      {
      System.out.println("USAGE:   java " +
      PrsReader.class.getName() +
      " <fully qualified path name of PaginatedResultSetXml file>");
      return;
      }

      String content = "";

      try
      {
      PrsReader reader = new PrsReader();
      content = readFileToString(args[0]);
      GxmlElement root = reader.parse(content);
      CATEGORY.debug(root.toString());
      CATEGORY.debug(content);
      }
      catch (Exception e)
      {
      CATEGORY.error(e.getMessage(), e);
      }
      }
    */
}
