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
package com.globalsight.ling.docproc.extractor.paginated;

// Java
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatPostProcessor;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWordCounter;
import com.globalsight.ling.docproc.DiplomatWordCounterException;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * PaginatedResultSetXml Extractor.
 */

public class Extractor extends AbstractExtractor implements EntityResolver
{
    // storage of PaginatedResultSetXml
    private StringBuffer m_content = null;
    private Connection m_dbConnection = null;

    /**
     * Extractor constructor comment.
     */
    public Extractor(Connection p_connection)
    {
        super();
        m_dbConnection = p_connection;
    }

    /**
     * Set data type of the document
     */
    public void init(EFInputData p_Input, Output p_Output)
            throws ExtractorException
    {
        super.init(p_Input, p_Output);
        // getOutput().setDataFormat(ExtractorRegistry.FORMAT_XML);

        m_content = new StringBuffer();
    }

    /**
     * Parse the file and insert Diplomat XML inside PaginatedResultSetXml
     */
    public void extract() throws ExtractorException
    {
        GsSAXParser parser = new GsSAXParser();

        // get DTD from resource
        parser.setEntityResolver(this);

        // validating parser
        try
        {
            parser.setFeature("http://xml.org/sax/features/validation", true);
        }
        catch (Exception e)
        {
            // Shouldn't get here
        }

        // parse PaginatedResultSetXml
        try
        {
            parser.parse(new InputSource(readInput()));
        }
        catch (ExtractorException ee)
        {
            throw ee;
        }
        catch (Exception e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.PAGINATED_PARSE_ERROR,
                    e.toString());
        }
    }

    /**
     * This method is invoked by AbstractExractor framework. No rules for
     * PaginatedResultSetXml
     */
    public void loadRules() throws ExtractorException
    {
    }

    /**
     * Returns the result of Extraction.
     */
    public String getDiplomatizedXml()
    {
        return m_content.toString();
    }

    /**
     * Override EntityResolver#resolveEntity.
     * 
     * The purpose of this method is to read PaginatedResultSetXml.dtd from
     * resource and feed it to the validating parser.
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
    {
        return new InputSource(getClass().getResourceAsStream(
                ExtractorConstants.DTD_NAME));
    }

    /**
     * Inner class to handle SAX events.
     */
    private class GsSAXParser extends SAXParser implements ExtractorConstants
    {
        // for extractor switching
        private StringBuffer m_switchExtractionBuffer = null;
        private String m_format = null;
        private String m_ruleId = null;

        // XML encoder
        private XmlEntities m_xmlEncoder = new XmlEntities();

        // administrative flags
        private boolean m_extracts = false;
        private String m_doExtracts = null;

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

            m_switchExtractionBuffer = null;
            m_doExtracts = null;
        }

        /** startDocument */
        public void startDocument() throws XNIException
        {
            m_content.append("<?xml version=\"1.0\"?>\n");

            super.startDocument(null, null, null, null);
        }

        /** Start element */
        public void startElement(QName element, XMLAttributes attributes,
                Augmentations augs) throws XNIException
        {
            String elementName = element.rawname;

            // set the extracts flag (default = true)
            if (elementName.equals(NODE_COLUMN))
            {
                m_extracts = true;
                m_format = DEFAULT_DATA_FORMAT;
            }

            // output tag
            m_content.append("<" + elementName);
            int len = attributes.getLength();
            for (int i = 0; i < len; i++)
            {
                String name = attributes.getLocalName(i);
                String value = attributes.getValue(i);

                // set extracts flags
                if (name.equals(ATT_CONTENT_MODE)
                        && value.equals(ATT_VALUE_INVISIBLE))
                {
                    m_extracts = false;
                    m_format = null;
                }
                if (name.equals(ATT_DATA_TYPE) && m_extracts)
                {
                    m_format = value;
                }
                if (name.equals(ATT_RULE_ID) && m_extracts)
                {
                    m_ruleId = value;
                }

                m_content.append(" " + name + "=\""
                        + m_xmlEncoder.encodeStringBasic(value) + "\"");
            }
            m_content.append(">");

            // set flags
            if (elementName.equals(NODE_CONTENT) && m_extracts)
            {
                m_doExtracts = EXTRACT_CONTENT;
                m_switchExtractionBuffer = new StringBuffer();
            }

            super.startElement(element, attributes, augs);
        }

        /** Characters. */
        public void characters(XMLString text, Augmentations augs)
                throws XNIException
        {
            if (m_doExtracts != null)
            {
                m_switchExtractionBuffer.append(text.ch, text.offset,
                        text.length);
            }
            else
            {
                String stuff = new String(text.ch, text.offset, text.length);
                m_content.append(m_xmlEncoder.encodeStringBasic(stuff));
            }

            super.characters(text, augs);
        }

        /** Ignorable whitespace. */
        public void ignorableWhitespace(XMLString text, Augmentations augs)
                throws XNIException
        {
            m_content.append(text.ch, text.offset, text.length);
            super.ignorableWhitespace(text, augs);
        }

        /** End element. */
        public void endElement(QName element, Augmentations augs)
                throws XNIException
        {
            if (m_doExtracts != null)
            {
                // Do extraction
                String extracted = null;
                try
                {
                    String rules = getRulesString(m_ruleId);
                    extracted = switchExtractor(
                            m_switchExtractionBuffer.toString(), m_format,
                            rules);
                    m_ruleId = null;
                }
                catch (Exception e)
                {
                    throw new XNIException(e);
                }

                m_content.append(extracted);

                m_switchExtractionBuffer = null;
                m_doExtracts = null;
            }

            String name = element.rawname;
            m_content.append("</" + name + ">");

            if (name.equals(NODE_COLUMN))
            {
                m_extracts = false;
            }

            super.endElement(element, augs);
        }

        public void comment(XMLString text, Augmentations augs)
                throws XNIException
        {
            m_content.append("<!--" + text.toString() + "-->\n");
            super.comment(text, augs);
        }

        public void startDTD(XMLLocator locator, Augmentations augs)
                throws XNIException
        {
            // strings
            // String name = rootElement.name;
            // String pubid = locator.getPublicId();
            // String sysid = locator.getExpandedSystemId();
            //
            // m_content.append("<!DOCTYPE " + name + " ");
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
            //
            // if (externalId != null)
            // {
            // m_content.append(externalId);
            // }

            super.startDTD(locator, augs);
        }

        public void endDTD(Augmentations augs) throws XNIException
        {
            // m_content.append(">\n");

            super.endDTD(augs);
        }

        private String getRulesString(String ruleId) throws Exception
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String rules = null;
            if (ruleId == null || ruleId.equals("0"))
            {
                return null;
            }

            try
            {
                int ruleIdInt = Integer.parseInt(ruleId);
                String sql = "SELECT RULE_TEXT FROM XML_RULE WHERE ID = ?";
                stmt = m_dbConnection.prepareStatement(sql);
                stmt.setInt(1, ruleIdInt);
                rs = stmt.executeQuery();
                if (rs.next())
                {
                    rules = rs.getString(1);
                }
            }
            finally
            {
                DbUtil.silentClose(rs);
                DbUtil.silentClose(stmt);
            }

            return rules;
        }

        private String switchExtractor(String to_translate, String dataFormat,
                String rules) throws ExtractorException
        {
            Output out = Extractor.this.switchExtractor(to_translate,
                    dataFormat, rules);

            // Segment
            DiplomatSegmenter segmenter = new DiplomatSegmenter();
            try
            {
                segmenter.segment(out);
            }
            catch (Exception e)
            {
                throw new ExtractorException(e);
            }

            // Word count
            DiplomatWordCounter wordCounter = new DiplomatWordCounter();
            try
            {
                wordCounter.countDiplomatDocument(segmenter.getOutput());
            }
            catch (DiplomatWordCounterException ee)
            {
                throw new ExtractorException(ee.getExceptionId(), ee.toString());
            }

            // Postprocess to add x attributes
            DiplomatPostProcessor postproc = new DiplomatPostProcessor();
            try
            {
                postproc.postProcess(wordCounter.getOutput());
            }
            catch (ExtractorException ee)
            {
                throw ee;
            }

            out = postproc.getOutput();

            String result = DiplomatWriter.writeXML(out, false);

            if (result.length() > 0 && result.charAt(0) == '\n')
            {
                result = new String(result.toCharArray(), 1,
                        result.length() - 1);
            }

            return result;
        }
    }
}
