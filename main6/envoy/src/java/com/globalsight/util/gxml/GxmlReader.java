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
import com.globalsight.util.gxml.GxmlSaxHelper;
import com.globalsight.util.gxml.PrsReader;

import com.globalsight.util.GeneralException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;

import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * <P>Parses a GXML document using SAX parsing model, saving the
 * result into a GxmlElement instance.</P>
 *
 * Algorithm Note:
 * 1. The parser does not parse sub-element type under &lt;segment&gt;
 *    level, means, all the content between a &lt;segment&gt; tag pair
 *    is taken as a single unit of text value String.
 * 2. Strings are expected to be in UCS-2.
 * 3. GSA tags are now honored.
 */
public class GxmlReader
    implements ContentHandler
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            GxmlReader.class.getName());

    //The Root Element instance
    protected GxmlElement m_root;
    protected GxmlRootElement m_gxmlRoot;

    // The current on-processing element
    protected GxmlElement m_currentElement;
    protected XMLReader m_parser;

    protected boolean m_ignoreText = true;

    private static final String CONTINUE_AFTER_FATAL_ERROR_FEATURE =
        "http://apache.org/xml/features/continue-after-fatal-error";


    /**
     * Gxml parser error handler class.
     */
    public static class ErrorHandler
        extends DefaultHandler
    {
        // catch parsing errors
        public void error(SAXParseException e)
            throws SAXException
        {
            warning(e);

            throw new SAXException("XML parse error at line " +
                e.getLineNumber() + " column " + e.getColumnNumber() +
                " URI:" + e.getSystemId() + " Message:" + e.getMessage());
        }

        // catch fatal errors
        public void fatalError(SAXParseException e)
            throws SAXException
        {
            error(e);
        }

        // catch warnings
        public void warning(SAXParseException e)
        {
            CATEGORY.error("Line:" + e.getLineNumber() +
                ", Column:" + e.getColumnNumber() +
                " URI:" + e.getSystemId() +
                " Message:" + e.getMessage(), e);
        }
    }

    protected static final ErrorHandler ERROR_HANDLER =
        new ErrorHandler();

    /**
     * <p>Package-private constructor: use GxmlReaderPool to get a new
     * instance.</p>
     */
    GxmlReader()
        throws GxmlException
    {
        // Acquire an XML parser from the factory.
        // Wed Aug 13 23:26:37 2003 CvdL: the standard JAXP registration
        // mechanism doesn't easily allow us to know which parser gets
        // used, or decide which one we want to use, so let's instantiate
        // the one we want directly.
        try
        {
            /*
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // request a bare bones parser
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            try
            {
                factory.setFeature(CONTINUE_AFTER_FATAL_ERROR_FEATURE, true);
            }
            catch (SAXException e)
            {
                CATEGORY.warn(CONTINUE_AFTER_FATAL_ERROR_FEATURE, e);
            }

            SAXParser sp = factory.newSAXParser();
            m_parser = new ParserAdapter (sp.getParser());

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("SAXParser is " + sp);
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("ParserAdapter is " + m_parser);
            }
            */

            // CvdL: enforce the AElfred reader, it is 50% faster than
            // Xerces 1.4.4.
            // Note: startElement(localname...qName) localname is null,
            // and qName contains the element name.
            m_parser = new org.dom4j.io.aelfred.SAXDriver();
            m_parser.setFeature("http://xml.org/sax/features/namespaces",
                false);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("AElfred parser is " + m_parser);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
            throw new GxmlException(GxmlException.MSG_FAILED_TO_GET_PARSER,
                null, e);
        }

        // set content and error handler
        m_parser.setContentHandler(this);
        m_parser.setErrorHandler(ERROR_HANDLER);
    }


    //
    // Begin: public class API
    //

    /**
     * <p>Parses the content of a GXML document and saves the parsing
     * result into a GxmlElement instance.</p>
     *
     * @param p_content - The content of a GXML file as a String
     * @return a GxmlRootElement instance holding the parsing result
     * @exception GxmlException, a Gxml component Exception
     */
    public GxmlRootElement parse(String p_content)
        throws GxmlException
    {
        m_root = null;
        m_gxmlRoot = null;
        m_currentElement = null;
        m_ignoreText = true;

        InputSource inputSource = new InputSource(
            new StringReader(p_content));

        try
        {
            m_parser.parse(inputSource);
        }
        catch (SAXException e)
        {
            CATEGORY.error("Parse error in `" + p_content + "'", e);
            throw new GxmlException(GxmlException.MSG_FAILED_TO_PARSE_GXML,
                null, e);
        }
        catch (IOException e)
        {
            CATEGORY.error("IO error in `" + p_content + "'", e);
            throw new GxmlException(GxmlException.MSG_FAILED_TO_GET_INPUTSOURCE,
                null, e);
        }

        return m_gxmlRoot;
    }

    public String toString()
    {
        return getClass().getName() + " m_root=" + m_root +
            " m_currentElement=" + m_currentElement;
    }

    //
    // Begin: SAX Event handlers, not public APIs
    //

    /**
     * The SAX characters event handler.  It appends the characters to
     * the current text String.
     */
    public void characters(char buf[], int offset, int len)
    {
        // If we are not in an element, such as after the XMLDecl and
        // before the root element, then ignore the characters.  They
        // should be white space.  If we are in an element that does
        // not contain text then ignore the characters.  They should
        // be white space.  We assume that we are parsing valid
        // well-formed Gxml documents.
        if (!m_ignoreText)
        {
            appendTextToCurrentElement(buf, offset, len);
        }
    }

    /**
     * The SAX startElement event handler.  For most of the tags, just
     * create the global instance of corresponding element type and
     * fill in the attributes.
     */
    public void startElement(String p_namespaceURI, String p_localName,
        String p_qName, Attributes p_attrs) throws SAXException
    {
        GxmlElement tempElement = null;

        String name = p_qName;

        try
        {
            if (GxmlNames.GXML_ROOT.equals(name))
            {
                m_ignoreText = true;

                // Don't overwrite the document root when parsing a
                // paginatedResultSetXml document with embedded diplomat
                // nodes.
                if (m_gxmlRoot == null)
                {
                    m_gxmlRoot = new GxmlRootElement();
                    fillCurrentElement(m_gxmlRoot, p_attrs);
                }
                else
                {
                    // everest.page.pageimport.TemplateGenerator expects
                    // an instance of GxmlRootElement. Sigh.
                    tempElement = new GxmlRootElement();
                    fillCurrentElement(tempElement, p_attrs);
                }

                return;
            }
            // order by most fequently occuring
            else if (GxmlNames.SKELETON.equals(name))
            {
                m_ignoreText = false;
                tempElement = new GxmlElement(GxmlElement.SKELETON, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.SEGMENT.equals(name))
            {
                m_ignoreText = false;
                tempElement = new GxmlElement(GxmlElement.SEGMENT, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.LOCALIZABLE.equals(name))
            {
                m_ignoreText = false;
                tempElement = new GxmlElement(GxmlElement.LOCALIZABLE, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.TRANSLATABLE.equals(name))
            {
                m_ignoreText = true;
                tempElement = new GxmlElement(GxmlElement.TRANSLATABLE, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }
            else if (GxmlNames.GS.equals(name))
            {
                tempElement = new GxmlElement(GxmlElement.GS, name);
                fillCurrentElement(tempElement, p_attrs);
                return;
            }

            // For all the child element of a Segment element don't parse
            // through each child element, just append the content to a
            // text unit of the parent segment element
            m_ignoreText = false;

            String text =
                GxmlSaxHelper.convertStartTagToString(name, p_attrs);

            appendTextToCurrentElement(text);
        }
        catch (Exception e)
        {
            CATEGORY.error("Unexpected error in element " + name, e);
            throw new SAXException(e);
        }
    }

    /**
     * The SAX endElement event handler.
     */
    public void endElement(String p_namespaceURI, String p_localName,
        String p_qName)
        throws SAXException
    {
        String name = p_qName;

        try
        {
            // order by most fequently occuring
            if (GxmlNames.SKELETON.equals(name) ||
                GxmlNames.SEGMENT.equals(name) ||
                GxmlNames.LOCALIZABLE.equals(name) ||
                GxmlNames.TRANSLATABLE.equals(name) ||
                GxmlNames.GXML_ROOT.equals(name) ||
                GxmlNames.GS.equals(name) )
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

            // m_ignoreText = false;

            // For all the child element of a Segment element: don't
            // parse through each child element but just append the
            // content of the child element to a text unit of the
            // parent segment element.
            appendTextToCurrentElement(
                GxmlSaxHelper.convertEndTagToString(name));
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

    ////////////////////////////////////////////////////////
    //  End:  SAX Event handlers, not public APIS
    ////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////
    //  Begin:  Local methods
    ////////////////////////////////////////////////////////

    protected void appendTextToCurrentElement(String p_text)
    {
        // The GxmlElements that can have text content are constructed
        // with a text buffer.  If we are not in an element, such as
        // after the XMLDecl and before the root element, then ignore
        // the characters.  They should be white space.  If we are in
        // an element that does not have a text buffer then ignore the
        // characters.  They should be white space.  We assume that we
        // are parsing valid well-formed Gxml documents.

        // As long as we haven't encountered the root element, there
        // is no currentElement.
        if (m_currentElement != null)
        {
            if (m_currentElement.getType() != GxmlElement.TEXT_NODE)
            {
                GxmlElement text = new TextNode();
                text.setParent(m_currentElement);
                m_currentElement.addChildElement(text);
                m_currentElement = text;
            }

            ((TextNode)m_currentElement).appendTextValue(p_text);
        }
    }

    protected void appendTextToCurrentElement(char buf[], int offset, int len)
    {
        // The GxmlElements that can have text content are constructed
        // with a text buffer.  If we are not in an element, such as
        // after the XMLDecl and before the root element, then ignore
        // the characters.  They should be white space.  If we are in
        // an element that does not have a text buffer then ignore the
        // characters.  They should be white space.  We assume that we
        // are parsing valid well-formed Gxml documents.

        // As long as we haven't encountered the root element, there
        // is no currentElement.
        if (m_currentElement != null)
        {
            if (m_currentElement.getType() != GxmlElement.TEXT_NODE)
            {
                GxmlElement text = new TextNode();
                text.setParent(m_currentElement);
                m_currentElement.addChildElement(text);
                m_currentElement = text;
            }

            ((TextNode)m_currentElement).appendTextValue(buf, offset, len);
        }
    }

    /**
     * <p>Fills the current element attributes, sets its parent, and
     * sets m_root if it has not been set yet.</p>
     *
     * @param p_gxmlElement found on start-tag
     * @param p_attributes attributes to fill
     */
    protected void fillCurrentElement(GxmlElement p_gxmlElement,
        Attributes p_attributes)
    {
        if (m_currentElement != null &&
            m_currentElement.getType() == GxmlElement.TEXT_NODE)
        {
            // end the TEXT_NODE
            m_currentElement = m_currentElement.getParent();
        }

        // add to the dom structure
        p_gxmlElement.setParent(m_currentElement);

        if (m_currentElement != null)
        {
            m_currentElement.addChildElement(p_gxmlElement);
        }

        if (m_root == null)
        {
            m_root = p_gxmlElement;
        }

        m_currentElement = p_gxmlElement;

        GxmlSaxHelper.fillAttributes(m_currentElement, p_attributes);
    }

    //
    // Test Block
    //
    /*
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("USAGE: java " + GxmlReader.class.getName() +
                " <fully qualified path name of Gxml file>");
            return;
        }

        String content = "";

        try
        {
            GxmlReader reader= new GxmlReader();
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

    public static String readFileToString(String p_fileName)
        throws FileNotFoundException, IOException
    {
        InputStream is = new FileInputStream(p_fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer text = new StringBuffer(100);
        String line;

        while ((line = br.readLine()) != null)
        {
            text.append(line + "\n");
        }

        return text.toString().trim();
    }
    */
}
