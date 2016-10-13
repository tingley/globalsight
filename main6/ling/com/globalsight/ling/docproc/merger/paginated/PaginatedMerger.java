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
package com.globalsight.ling.docproc.merger.paginated;

// Java
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

// GlobalSight
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.DiplomatMerger;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.L10nContent;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.merger.paginated.PaginatedMergerConstants;
import com.globalsight.ling.common.CodesetMapper;

// SAX & DOM
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
//import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.AttributeList;

// Xerces & Xalan
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;

/**
 * PaginatedResultSetXml Extractor.
 */

public class PaginatedMerger implements EntityResolver
{
    // storage of PaginatedResultSetXml
    private StringBuffer m_output = null;

    // character encoding
    private String m_IANAEncoding = null;
    private String m_JavaEncoding = null;

    /**
     * Extractor constructor comment.
     */
    public PaginatedMerger()
    {
    }

    /**
     * 
     * Parse the input file and get the original format from <diplomat> portion
     * 
     */
    public byte[] merge(String input, String encoding)
            throws ExtractorException
    {
        m_output = new StringBuffer();
        m_IANAEncoding = encoding;
        m_JavaEncoding = CodesetMapper.getJavaEncoding(encoding);
        if (m_JavaEncoding == null)
        {
            m_JavaEncoding = encoding;
        }

        GsSAXParser parser = new GsSAXParser();

        // don't read DTD
        parser.setEntityResolver(this);

        // // validating parser
        // try
        // {
        // parser.setFeature("http://xml.org/sax/features/validation",
        // true);
        // }
        // catch(Exception e)
        // {
        // // Shouldn't get here
        // }

        // parse PaginatedResultSetXml
        byte[] merged = null;
        try
        {
            parser.parse(new InputSource(new StringReader(input)));
            merged = m_output.toString().getBytes(m_JavaEncoding);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_ENCODING, e.toString());
        }
        catch (Exception ee)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.PAGINATED_PARSE_ERROR,
                    ee.toString());
        }

        return merged;

    }

    /**
     * Override EntityResolver#resolveEntity.
     * 
     * The purpose of this method is to avoid the parser trying to read the
     * external DTD
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
    {
        return new InputSource(new StringReader(""));
    }

    /**
     * Inner class to handle SAX events
     * 
     */
    private class GsSAXParser extends SAXParser implements
            PaginatedMergerConstants
    {
        // for extractor switching
        private StringBuffer m_mergingContent = null;

        // XML encoder
        private XmlEntities m_xmlEncoder = new XmlEntities();

        // administrative flags
        private boolean m_merges = false;

        /**
         * Constructor
         */
        GsSAXParser()
        {
            super();
        }

        public void reset() throws XNIException
        {
            super.reset();

            m_mergingContent = null;
            m_merges = false;
        }

        /** startDocument */
        public void startDocument() throws XNIException
        {
            m_output.append("<?xml version=\"1.0\" encoding=\""
                    + m_IANAEncoding + "\"?>\n");

            super.startDocument(null, null, null, null);
        }

        /** Start element */
        public void startElement(QName element, XMLAttributes attributes,
                Augmentations augs) throws XNIException
        {
            String elementName = element.rawname;

            // set the merge flag
            if (elementName.equals(NODE_DIPLOMAT))
            {
                m_merges = true;
                m_mergingContent = new StringBuffer();
            }

            StringBuffer startTag = new StringBuffer();
            // output tag
            startTag.append("<" + elementName);
            int len = attributes.getLength();
            for (int i = 0; i < len; i++)
            {
                String name = attributes.getLocalName(i);
                String value = attributes.getValue(i);

                startTag.append(" " + name + "=\""
                        + m_xmlEncoder.encodeStringBasic(value) + "\"");
            }
            startTag.append(">");

            if (m_merges)
            {
                m_mergingContent.append(startTag.toString());
            }
            else
            {
                m_output.append(startTag.toString());
            }

            super.startElement(element, attributes, augs);
        }

        /** Characters. */
        public void characters(XMLString text, Augmentations augs)
                throws XNIException
        {
            String stuff = new String(text.ch, text.offset, text.length);
            if (m_merges)
            {
                m_mergingContent.append(m_xmlEncoder.encodeStringBasic(stuff));
            }
            else
            {
                m_output.append(m_xmlEncoder.encodeStringBasic(stuff));
            }

            super.characters(text, augs);
        }

        /** Ignorable whitespace. */
        public void ignorableWhitespace(XMLString text, Augmentations augs)
                throws XNIException
        {
            characters(text, augs);
            // super.ignorableWhitespace(ch, start, length);

        }

        /** End element. */
        public void endElement(QName element, Augmentations augs)
                throws XNIException
        {
            String elementName = element.rawname;
            StringBuffer endTag = new StringBuffer();
            endTag.append("</" + elementName + ">");

            if (m_merges)
            {
                m_mergingContent.append(endTag.toString());

                if (elementName.equals(NODE_DIPLOMAT))
                {
                    // Do merge
                    String merged = null;
                    try
                    {
                        merged = mergeDiplomat(m_mergingContent.toString());
                    }
                    catch (DiplomatMergerException e)
                    {
                        throw new XNIException(e);
                    }
                    m_output.append(m_xmlEncoder.encodeStringBasic(merged));

                    m_mergingContent = null;
                    m_merges = false;
                }
            }
            else
            {
                m_output.append(endTag.toString());
            }

            super.endElement(element, augs);

        }

        public void comment(XMLString text, Augmentations augs)
                throws XNIException
        {
            m_output.append("<!--" + text.toString() + "-->\n");
            super.comment(text, augs);
        }

        public void startDTD(XMLLocator locator, Augmentations augs)
                throws XNIException
        {
            // strings
            // String name = fStringPool.toString(rootElement.rawname);
            // String pubid = fStringPool.toString(publicId);
            // String sysid = fStringPool.toString(systemId);
            //
            // m_output.append("<!DOCTYPE " + name + " ");
            //
            // String externalId = null;
            // if (sysid != null && pubid != null)
            // {
            // externalId = "PUBLIC \"" + pubid + "\" \"" + sysid + "\"";
            // }
            // else if (sysid != null)
            // {
            // externalId = "SYSTEM \"" + sysid + "\"";
            // }
            // if (externalId != null)
            // {
            // m_output.append(externalId);
            // }

            super.startDTD(locator, augs);
        }

        public void endDTD(Augmentations augs) throws XNIException
        {
            // m_output.append(">\n");

            super.endDTD(augs);
        }

        private String mergeDiplomat(String diplomat)
                throws DiplomatMergerException
        {
            DiplomatMerger diplomatMerger = new DiplomatMerger();
            L10nContent l10ncontent = new L10nContent();

            diplomatMerger.init(diplomat, l10ncontent);
            diplomatMerger.setTargetEncoding(m_JavaEncoding);
            diplomatMerger.merge();

            return l10ncontent.getL10nContent();
        }

    }
}
