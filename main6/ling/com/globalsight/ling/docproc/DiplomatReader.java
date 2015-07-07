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
package com.globalsight.ling.docproc;

import java.io.StringReader;

import java.util.Properties;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.XmlEntities;

/**
 * Converts a given DiplomatXML file or string to a <code>Output</code>
 * data structure.  <code>Output</code> can be thought of as a simple
 * DOM object model that we use for all internal Diplomat processing.
 *
 * This implementation uses the SAX 2 API to parse and convert the XML
 * input.  It does not validate.
 */
public class DiplomatReader
    implements org.xml.sax.ContentHandler, DiplomatNames
{
    private String m_strXml;
    private Output m_output;
    private XmlEntities m_xmlEntities;
    private StringBuffer m_currentSegment = new StringBuffer(128);
    private Stack m_stateStack;
    private Exception m_error;

    /**
     * XML parser error handler. Required by SAX.
     */
    private class ErrorHandler
        extends org.xml.sax.helpers.DefaultHandler
    {
        /** catch validation errors */
        public void error(SAXParseException e)
            throws SAXParseException
        {
            //throw e;  // not needed since we don't validate
        }

        /** catch warnings */
        public void warning(SAXParseException err)
            throws SAXParseException
        {
            throw err;
        }
    }

    //
    // Constructor
    //
    public DiplomatReader(String p_strXml)
    {
        super();
        init(p_strXml);
    }

    /**
     * Intialize a new DiplomatReader. Call this each time
     * we call getOutput().
     */
    public void init(String p_diplomat)
    {
        m_strXml = p_diplomat;

        m_error = null;
        m_currentSegment.setLength(0);
        m_output = new Output();
        m_xmlEntities = new XmlEntities();
        m_stateStack = new Stack();
    }


    /**
     * Parse the Diplomat input and return the corresponding Output
     * object.
     */
    public Output getOutput()
        throws DiplomatReaderException
    {
        try
        {
            XMLReader parser;

            // SY 10/28/03
            // Changed the parser from xerces to dom4j due to bad characters
            // (half surrogate) contained in some Word HTML. Xerces barks,
            // dom4j doesn't care. Although it is obvious which is better
            // parser, we don't want to spend too much time on this.

            /*
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // request a bare bones parser
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            SAXParser sp = factory.newSAXParser();
            parser = new ParserAdapter (sp.getParser());
            */

            parser = new org.dom4j.io.aelfred.SAXDriver();
            parser.setFeature("http://xml.org/sax/features/namespaces",
                false);

            // set our error handler and input
            parser.setContentHandler(this);
            parser.setErrorHandler(new ErrorHandler());
            InputSource is = new InputSource(new StringReader(m_strXml));

            parser.parse(is);
        }
        catch (SAXParseException ex)
        {
            throw new DiplomatReaderException(
                ExtractorExceptionConstants.INTERNAL_ERROR, ex);
        }
        catch (SAXException ex)
        {
            throw new DiplomatReaderException(
                ExtractorExceptionConstants.INTERNAL_ERROR, ex);
        }
        catch (Exception ex)
        {
            throw new DiplomatReaderException(
                ExtractorExceptionConstants.INTERNAL_ERROR, ex);
        }

        if (m_error != null)
        {
            throw new DiplomatReaderException(
                ExtractorExceptionConstants.INTERNAL_ERROR, m_error);
        }

        return m_output;
    }

    //
    // Implementation of Interface org.xml.sax.ContentHandler
    //

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


    /**
     * SAX handler. Intercept all character content.
     * Add content to appropriate node type.
     */
    public void characters(char buf[], int offset, int len)
    {
        String characters = new String(buf, offset, len);
        if (characters.length() > 0)
        {
            try
            {
                addCharContent(characters, getCurrentState());
            }
            catch (DiplomatReaderException e)
            {
                m_error = e;
            }
        }
    }

    /**
     * SAX handler. For each Diplomat start tag push on to our parser
     * stack the current state, create the appropriate Output node and
     * populate it's fields.
     */
    public void startElement(String namespaceURI, String tag,
        String qName, Attributes attrs)
        throws SAXException
    {
        if (qName.equalsIgnoreCase(DiplomatNames.Element.DIPLOMAT))
        {
            saveDiplomatAttrs(attrs);
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.TRANSLATABLE))
        {
            m_stateStack.push(new DiplomatParserState(
                DocumentElement.TRANSLATABLE, null, null));

            try
            {
                saveTranslatableAttrs(attrs);
            }
            catch (DiplomatReaderException e)
            {
                throw new SAXException (e);
            }
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.LOCALIZABLE))
        {
            m_stateStack.push(new DiplomatParserState(
                DocumentElement.LOCALIZABLE, null, null));

            try
            {
                saveLocalizableAttrs(attrs);
            }
            catch (DiplomatReaderException e)
            {
                throw new SAXException (e);
            }
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.SKELETON))
        {
            m_stateStack.push(new DiplomatParserState(
                DocumentElement.SKELETON, null, null));
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.GSA))
        {
            boolean delete = false;
            String extract = null;
            String description = null;
            String locale = null;
            String add = null;
            String added = null;
            String deleted = null;
            String snippetName = null;
            String snippetId = null;

            for (int i = 0, max = attrs.getLength(); i < max; i++)
            {
                String name = attrs.getQName(i);

                if (name.equalsIgnoreCase(DiplomatNames.Attribute.EXTRACT))
                {
                    extract = attrs.getValue(i);
                }
                else if (name.equalsIgnoreCase(DiplomatNames.Attribute.DESCRIPTION))
                {
                    description = attrs.getValue(i);
                }
                else if (name.equalsIgnoreCase(DiplomatNames.Attribute.LOCALE))
                {
                    locale = attrs.getValue(i);
                }
                else if (name.equalsIgnoreCase(DiplomatNames.Attribute.ADD))
                {
                    add = attrs.getValue(i);
                }
                else if (name.equalsIgnoreCase(DiplomatNames.Attribute.DELETE))
                {
                    if (attrs.getValue(i).equals("yes"))
                    {
                        delete = true;
                    }
                }
                else if (name.equalsIgnoreCase(DiplomatNames.Attribute.ADDED))
                {
                    added = attrs.getValue(i);
                }
                else if (name.equalsIgnoreCase(DiplomatNames.Attribute.NAME))
                {
                    snippetName = attrs.getValue(i);
                }
                else if (name.equalsIgnoreCase(DiplomatNames.Attribute.ID))
                {
                    snippetId = attrs.getValue(i);
                }
                else if (name.equalsIgnoreCase(DiplomatNames.Attribute.DELETED))
                {
                    deleted = attrs.getValue(i);
                }
            }

            // SAX (2) does not indicate empty elements, but we want
            // to output GSA as empty if they are.  The Output object
            // will set the last GSA to empty if there was no content.
            try
            {
                m_output.addGsaStart(extract, description, locale, add,
                    delete, added, deleted, snippetName, snippetId);
            }
            catch (DocumentElementException ex)
            {
                // Should not happen. The Extractor has already
                // validated the tag.
                m_error = ex;
            }
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.SEGMENT))
        {
            m_currentSegment.setLength(0);
            m_stateStack.push(new DiplomatParserState(
                DocumentElement.SEGMENT, null, null));
        }
        else // must be a TMX tag
        {
            saveDiplomatStartTag(qName, attrs, getCurrentState());
        }
    }

    /**
     * SAX handler.
     * For each Diplomat end tag take the appropriate action.
     */
    public void endElement(String namespaceURI, String tag,
        String qName)
    {
        if (qName.equalsIgnoreCase(DiplomatNames.Element.DIPLOMAT))
        {
            // do nothing
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.TRANSLATABLE))
        {
            m_stateStack.pop();
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.LOCALIZABLE))
        {
            m_stateStack.pop();
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.SKELETON))
        {
            m_stateStack.pop();
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.GSA))
        {
            // SAX (2) does not indicate empty elements, but we want
            // to output GSA as empty if they are.  The Output object
            // will set the last GSA to empty if there was no content.
            m_output.addGsaEnd();
        }
        else if (qName.equalsIgnoreCase(DiplomatNames.Element.SEGMENT))
        {
            try
            {
                SegmentNode sn = new SegmentNode();
                sn.setSegment(m_currentSegment.toString());
                m_output.addSegment(sn);
            }
            catch (DocumentElementException e)
            {
                m_error = e;
            }

            m_currentSegment.setLength(0);
            m_stateStack.pop();
        }
        else // TMX
        {
            saveDiplomatEndTag(qName, getCurrentState());
        }
    }

    //
    // More public and private methods
    //

    private void addCharContent(String p_content, int p_current)
        throws DiplomatReaderException
    {
        switch (p_current)
        {
        case DocumentElement.TRANSLATABLE:
            m_output.addTranslatable(p_content);
            break;

        case DocumentElement.LOCALIZABLE:
            m_output.addLocalizable(p_content);
            break;

        case DocumentElement.SKELETON:
            m_output.addSkeleton(p_content);
            break;

        case DocumentElement.SEGMENT:
            m_currentSegment.append(
                m_xmlEntities.encodeStringBasic(p_content));
            break;

        default:
            break;
        }
    }

    private int getCurrentState()
    {
        if (m_stateStack.empty())
        {
            return DocumentElement.NONE;
        }

        DiplomatParserState ps = (DiplomatParserState) m_stateStack.peek();
        return ps.getElementType();
    }

    private String makeEndElement(String name)
    {
        StringBuffer result = new StringBuffer(16);

        result.append("</");
        result.append(name);
        result.append(">");

        return result.toString();
    }

    private String makeStartElement(String elem, Properties attribs)
    {
        StringBuffer xml = new StringBuffer(16);

        xml.append("<");
        xml.append(elem);

        // write all attributes
        if (attribs != null && attribs.size() != 0)
        {
            for (Enumeration e = attribs.propertyNames(); e.hasMoreElements();)
            {
                String attribName = (String) e.nextElement();

                xml.append(" ");
                xml.append(attribName);
                xml.append("=\"");
                xml.append(attribs.getProperty(attribName));
                xml.append("\"");
            }
        }

        xml.append(">");

        return xml.toString();
    }

    private void saveDiplomatAttrs(Attributes p_attributes)
    {
        for (int i = 0, max = p_attributes.getLength(); i < max; i++)
        {
            String name = p_attributes.getQName(i);

            if (name.equalsIgnoreCase(DiplomatNames.Attribute.VERSION))
            {
                // Hard coded inside DiplomatAttribute class
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.LOCALE))
            {
                m_output.setLocale(p_attributes.getValue(i));
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.DATATYPE))
            {
                m_output.setDataFormat(p_attributes.getValue(i));
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.WORDCOUNT))
            {
                m_output.setTotalWordCount(
                    Integer.parseInt((p_attributes.getValue(i))));
            }
        }
    }

    private void saveDiplomatEndTag(String p_Name, int p_current)
    {
        String endtag = makeEndElement(p_Name);

        switch (p_current)
        {
        case DocumentElement.TRANSLATABLE:
            m_output.addTranslatableTmx(endtag);
            break;

        case DocumentElement.LOCALIZABLE:
            m_output.addLocalizableTmx(endtag);
            break;

        case DocumentElement.SEGMENT:
            m_currentSegment.append(endtag);
            break;

        default:
            break;
        }
    }

    private void saveDiplomatEndTag(String p_name, Attributes p_attributes,
        int p_current)
    {
        Properties attrs = new Properties();

        if (p_attributes != null)
        {
            for (int i = 0, max = p_attributes.getLength(); i < max; i++)
            {
                attrs.setProperty(p_attributes.getQName(i),
                    p_attributes.getValue(i));
            }
        }

        String starttag = makeStartElement(p_name, attrs);

        switch (p_current)
        {
        case DocumentElement.TRANSLATABLE:
            m_output.addTranslatableTmx(starttag);
            break;

        case DocumentElement.LOCALIZABLE:
            m_output.addLocalizableTmx(starttag);
            break;

        case DocumentElement.SEGMENT:
            m_currentSegment.append(starttag);
            break;

        default:
            break;
        }
    }

    private void saveDiplomatStartTag(String p_name,
        Attributes p_attributes, int p_current)
    {
        Properties attrs = new Properties();

        if (p_attributes != null)
        {
            for (int i = 0, max = p_attributes.getLength(); i < max; i++)
            {
                attrs.setProperty(p_attributes.getQName(i),
                    p_attributes.getValue(i));
            }
        }

        String starttag = makeStartElement(p_name, attrs);

        switch (p_current)
        {
        case DocumentElement.TRANSLATABLE:
            m_output.addTranslatableTmx(starttag);
            break;

        case DocumentElement.LOCALIZABLE:
            m_output.addLocalizableTmx(starttag);
            break;

        case DocumentElement.SEGMENT:
            m_currentSegment.append(starttag);
            break;

        default:
            break;
        }
    }

    private void saveLocalizableAttrs(Attributes p_attributes)
        throws DiplomatReaderException
    {
        String type = null;
        String datatype = null;
        int wordcount = 0;

        for (int i = 0, max = p_attributes.getLength(); i < max; i++)
        {
            String name = p_attributes.getQName(i);

            if (name.equalsIgnoreCase(DiplomatNames.Attribute.BLOCKID))
            {
                // skip this since it's generated by the DiplomatWriter
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.WORDCOUNT))
            {
                // skip
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.TYPE))
            {
                type = p_attributes.getValue(i);
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.DATATYPE))
            {
                datatype = p_attributes.getValue(i);
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.WORDCOUNT))
            {
                wordcount = Integer.parseInt(p_attributes.getValue(i));
            }
        }

        try
        {
            m_output.addLocalizable("");
            m_output.setLocalizableAttrs(datatype, type);

            if (wordcount > 0)
            {
                LocalizableElement elem =
                    (LocalizableElement)m_output.getCurrentElement();

                elem.setWordcount(wordcount);
            }
        }
        catch (DocumentElementException e)
        {
            throw new DiplomatReaderException (
                ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
    }

    private void saveTranslatableAttrs(Attributes p_attributes)
        throws DiplomatReaderException
    {
        String datatype = null;
        String type = null;
        int wordcount = 0;
        String isLocalized = null;
        String escapintChars = null;

        for (int i = 0, max = p_attributes.getLength(); i < max; i++)
        {
            String name = p_attributes.getQName(i);

            if (name.equalsIgnoreCase(DiplomatNames.Attribute.BLOCKID))
            {
                // skip this since it's generated by the DiplomatWriter
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.DATATYPE))
            {
                datatype = p_attributes.getValue(i);
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.TYPE))
            {
                type = p_attributes.getValue(i);
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.WORDCOUNT))
            {
                wordcount = Integer.parseInt(p_attributes.getValue(i));
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.ISLOCALIZED))
            {
                isLocalized = p_attributes.getValue(i);
            }
            else if (name.equalsIgnoreCase(DiplomatNames.Attribute.ESCAPINGCHARS))
            {
                escapintChars = p_attributes.getValue(i);
            }
        }

        try
        {
            m_output.addTranslatable("");
            m_output.setTranslatableAttrs(datatype, type, isLocalized);

            if (wordcount > 0)
            {
                TranslatableElement elem =
                    (TranslatableElement)m_output.getCurrentElement();

                elem.setWordcount(wordcount);
            }
            
            if (escapintChars != null)
            {
                TranslatableElement elem =
                        (TranslatableElement)m_output.getCurrentElement();
                elem.setEscapingChars(escapintChars);
            }
        }
        catch (DocumentElementException e)
        {
            throw new DiplomatReaderException (
                ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
    }
}
