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
package com.globalsight.ling.tm2.population;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.common.XmlEntities;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

/**
 * Repair TMX tags for TM population
 */

class TmxTagRepairer
{
    private static final Logger c_logger =
        Logger.getLogger(
            TmxTagRepairer.class.getName());


    /**
     * Add 'x' attribute in the target segments if they are
     * missing. The attribute values are taken from source
     * segments. If corresponding tags are not found from the source,
     * the new (and unique within the segments) attribute values are
     * given. All source segments' tags should have 'x' attribute.
     *
     * @param p_tu BaseTmTu object that is repaired
     * @param p_sourceLocale source locale
     */
    public static void fixMissingX(
        BaseTmTu p_tu, GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        // get reference tag lists from source segment
        BaseTmTuv sourceTuv = p_tu.getFirstTuv(p_sourceLocale);
        SourceTags sourceTags = getSourceTags(sourceTuv);

        Iterator itLocale = p_tu.getAllTuvLocales().iterator();
        while(itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale)itLocale.next();
            if(!locale.equals(p_sourceLocale))
            {
                // fix missing x attribute from target segment
                BaseTmTuv targetTuv = p_tu.getFirstTuv(locale);
                fixX(targetTuv, sourceTags);
            }
        }
    }


    private static SourceTags getSourceTags(BaseTmTuv p_sourceTuv)
        throws Exception
    {
        String sourceText = p_sourceTuv.getSegment();

        // create SAX parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        SourceTagCollector sourceTagCollector = new SourceTagCollector();
        
        parser.parse(new InputSource(
            new StringReader(sourceText)), sourceTagCollector);
        
        return sourceTagCollector.getSourceTags();
    }
    

    private static void fixX(BaseTmTuv p_targetTuv, SourceTags p_sourceTags)
        throws Exception
    {
        String targetText = p_targetTuv.getSegment();

        // create SAX parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        Xfixer xFixer = new Xfixer(p_sourceTags.deepClone());
        
        parser.parse(new InputSource(new StringReader(targetText)), xFixer);

        String text = xFixer.getSegmentText();
        p_targetTuv.setSegment(text);
    }
    

    private static class SourceTagCollector
        extends DefaultHandler
    {
        private SourceTags m_sourceTags = new SourceTags();
        
        public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
        {
            if(qName.equals("bpt") || qName.equals("it")
                || qName.equals("ph"))
            {
                processContentMarkup(qName, attributes);
            }
        }


        // ErrorHandler interface methods

        public void error(SAXParseException e)
            throws SAXException
        {
            throw new SAXException("Segment text parse error at\n  line "
                + e.getLineNumber()
                + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
        }

        public void fatalError(SAXParseException e)
            throws SAXException
        {
            error(e);
        }

        public void warning(SAXParseException e)
        {
            System.err.println("Segment text parse warning at\n  line "
                + e.getLineNumber()
                + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
        }


        public SourceTags getSourceTags()
        {
            return m_sourceTags;
        }
        
        
        private void processContentMarkup(
            String p_qName, Attributes p_attributes)
        {
            String type = p_attributes.getValue("type");
            if(type == null)
            {
                type = "end"; // for <it> with no type
            }
                
            String xValue = p_attributes.getValue("x");
            if (xValue != null) {
                Integer xValueInteger = new Integer(xValue);
                m_sourceTags.addTag(p_qName, type, xValueInteger);
            }
        }
    }


    private static class Xfixer
        extends DefaultHandler
    {
        private SourceTags m_sourceTags;
        private XmlEntities m_xmlEncoder;
        private StringBuffer m_buf;
        
        private Xfixer(SourceTags p_sourceTags)
        {
            m_sourceTags = p_sourceTags;
            m_xmlEncoder = new XmlEntities();
            m_buf = new StringBuffer();
        }
        
        
        public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
        {
            appendTag(qName, attributes);
        }


        public void endElement(String uri, String localName, String qName)
        {
            m_buf.append("</" + qName + ">");
        }


        public void characters(char[] ch, int start, int length)
            throws SAXException
        {
            String content = new String(ch, start, length);
            content = m_xmlEncoder.encodeStringBasic(content);
            m_buf.append(content);
        }


        private void appendTag(String p_qName, Attributes p_attributes)
        {
            m_buf.append("<").append(p_qName).append(" ");
            String attStr = makeAttributesString(p_qName, p_attributes);
            m_buf.append(attStr).append(">");
        }


        private String makeAttributesString(
            String p_qName, Attributes p_attributes)
        {
            StringBuffer buf = new StringBuffer();
            
            int len = p_attributes.getLength();
            for(int i = 0; i < len; i++)
            {
                String key = p_attributes.getQName(i);
                String value = p_attributes.getValue(i);

                value = m_xmlEncoder.encodeStringBasic(value);
                buf.append(key).append("=\"").append(value).append("\" ");
            }

            // add missing x attribute
            if((p_qName.equals("bpt") || p_qName.equals("it")
                   || p_qName.equals("ph"))
                && p_attributes.getValue("x") == null)
            {
                String type = p_attributes.getValue("type");
                if(type == null)
                {
                    type = "end"; // for <it> with no type
                }

                int xValue = m_sourceTags.getNextXvalue(p_qName, type);
                buf.append("x=\"").append(xValue).append("\" ");
            }
            
            return buf.toString();
        }


        private String getSegmentText()
        {
            return m_buf.toString();
        }


        // ErrorHandler interface methods

        public void error(SAXParseException e)
            throws SAXException
        {
            throw new SAXException("Segment text parse error at\n  line "
                + e.getLineNumber()
                + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
        }

        public void fatalError(SAXParseException e)
            throws SAXException
        {
            error(e);
        }

        public void warning(SAXParseException e)
        {
            System.err.println("Segment text parse warning at\n  line "
                + e.getLineNumber()
                + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
        }


        private void processContentMarkup(
            String p_qName, Attributes p_attributes)
        {
            String type = p_attributes.getValue("type");
            if(type == null)
            {
                type = "end"; // for <it> with no type
            }
                
            String xValue = p_attributes.getValue("x");
            Integer xValueInteger = new Integer(xValue);
                
            m_sourceTags.addTag(p_qName, type, xValueInteger);
        }
    }


    private static class SourceTags
    {
        // key: tag identifier. tag name + "-" + type e.g. "bpt-bold"
        // value: List of x attribute value
        Map m_tagMap = new HashMap();
        int m_maxXvalue = 0;
        
        public void addTag(String p_tagName, String p_type, Integer p_xValue)
        {
            String tagId = getTagIdentifier(p_tagName, p_type);
            List xList = (List)m_tagMap.get(tagId);
            if(xList == null)
            {
                xList = new ArrayList();
                m_tagMap.put(tagId, xList);
            }
            xList.add(p_xValue);
            
            // get max x value
            m_maxXvalue = Math.max(m_maxXvalue, p_xValue.intValue());
        }
        

        public int getNextXvalue(String p_tagName, String p_type)
        {
            int result = 0;
            
            String tagId = getTagIdentifier(p_tagName, p_type);
            List xList = (List)m_tagMap.get(tagId);
            if(xList != null && xList.size() > 0)
            {
                Integer i = (Integer)xList.remove(0);
                result = i.intValue();
            }
            else
            {
                result = ++m_maxXvalue;
            }
            
            return result;
        }

        
        public SourceTags deepClone()
        {
            SourceTags clone = new SourceTags();

            clone.m_maxXvalue = m_maxXvalue;

            Set entrySet = m_tagMap.entrySet();
            Iterator it = entrySet.iterator();
            while(it.hasNext())
            {
                Map.Entry entry = (Map.Entry)it.next();
                clone.m_tagMap.put(
                    entry.getKey(), new ArrayList((List)entry.getValue()));
            }

            return clone;
        }

        private String getTagIdentifier(String p_tagName, String p_type)
        {
            return p_tagName + "-" + p_type;
        }
        
    }
    
        
        
}
