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
package com.globalsight.ling.aligner;

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.common.XmlEntities;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.StringReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;


public class AlignedSegments
{
    // List of BaseTmTuv objects
    private List m_soruceTuvs;
    private List m_targetTuvs;

    public AlignedSegments(List p_sourceTuvs, List p_targetTuvs)
    {
        m_soruceTuvs = p_sourceTuvs;
        m_targetTuvs = p_targetTuvs;
    }
        

    public List getSourceSegments()
    {
        return m_soruceTuvs;
    }
        

    public List getTargetSegments()
    {
        return m_targetTuvs;
    }

        
    /**
     * This method combines a list of tuvs into one tuv and put
     * each source and target tuv into one tu so that it can be
     * passed to a TM population method. The method also checks
     * the code difference between the source and target and
     * delete unmatched codes.
     *
     * @return BaseTmTu that contains one source and one target
     * BaseTmTuv objects
     */
    public SegmentTmTu getAlignedSegment()
        throws Exception
    {
        // Create a new TU and copy the info from a source TU
        SegmentTmTu sourceTu
            = (SegmentTmTu)((SegmentTmTuv)m_soruceTuvs.get(0)).getTu();
        SegmentTmTu tu = new SegmentTmTu(sourceTu.getId(), 0,
            sourceTu.getFormat(), sourceTu.getType(),
            sourceTu.isTranslatable(), sourceTu.getSourceLocale());
        tu.setSubId(SegmentTmTu.ROOT);
            
        // create a source and a target Tuv
        SegmentTmTuv sourceTuv = combineTuvs(m_soruceTuvs);
        SegmentTmTuv targetTuv = combineTuvs(m_targetTuvs);

        // check the code difference and delete unmatched codes
        checkCode(sourceTuv, targetTuv);
            
        tu.addTuv(sourceTuv);
        tu.addTuv(targetTuv);
            
        return tu;
    }
        

    private SegmentTmTuv combineTuvs(List p_tuvs)
        throws Exception
    {
        BaseTmTuv orgTuv = (BaseTmTuv)p_tuvs.get(0);
        SegmentTmTuv tuv
            = new SegmentTmTuv(orgTuv.getId(), "", orgTuv.getLocale());

        StringBuffer buf = new StringBuffer();
        buf.append("<segment>");
            
        int len = p_tuvs.size();
        for(int i = 0; i < len; i++)
        {
            String segmentText
                = processSegmentText((BaseTmTuv)p_tuvs.get(i), i);
            buf.append(segmentText);
        }
            
        buf.append("</segment>");
        tuv.setSegment(buf.toString());
            
        return tuv;
    }
        

    private String processSegmentText(BaseTmTuv p_tuv, int p_order)
        throws Exception
    {
        String processedText = null;
            
        if(p_order == 0)
        {
            // strip <segment> and </segment> from the text
            processedText = p_tuv.getSegmentNoTopTag();
        }
        else
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
        
            SAXParser parser = factory.newSAXParser();
            CombineHandler handler = new CombineHandler(p_order);
            parser.parse(new InputSource(
                             new StringReader(p_tuv.getSegment())), handler);

            processedText = handler.getText();
        }

        return processedText;
    }
        
        
    private void checkCode(BaseTmTuv p_sourceTuv, BaseTmTuv p_targetTuv)
        throws Exception
    {
        String sourceText = p_sourceTuv.getSegment();
        String targetText = p_targetTuv.getSegment();

        // create SAX parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        // create source and target TagChecker objects
        TagCheckerHandler tagCheckerHandler = new TagCheckerHandler();

        parser.parse(new InputSource(
                         new StringReader(sourceText)), tagCheckerHandler);
        TagChecker sourceTagChecker = tagCheckerHandler.getTagChecker();
            
        parser.parse(new InputSource(
                         new StringReader(targetText)), tagCheckerHandler);
        TagChecker targetTagChecker = tagCheckerHandler.getTagChecker();

        sourceTagChecker.buildDeleteList(targetTagChecker);
        targetTagChecker.buildDeleteList(sourceTagChecker);
            
        // delete unmatched tags from source and target segment text
        TagDeleteHandler tagDeleteHandler
            = new TagDeleteHandler(sourceTagChecker);

        parser.parse(new InputSource(
                         new StringReader(sourceText)), tagDeleteHandler);
        sourceText = tagDeleteHandler.getText();
            
        tagDeleteHandler
            = new TagDeleteHandler(targetTagChecker);

        parser.parse(new InputSource(
                         new StringReader(targetText)), tagDeleteHandler);
        targetText = tagDeleteHandler.getText();

        // set the error-checked segment string to TUVs
        p_sourceTuv.setSegment(sourceText);
        p_targetTuv.setSegment(targetText);
    }
        

    private class CombineHandler
        extends DefaultHandler
    {
        private XmlEntities m_xmlEncoder;
        private int m_order;
        private StringBuffer m_buf;
            
        private CombineHandler(int p_order)
        {
            m_xmlEncoder = new XmlEntities();
            m_order = p_order;
            m_buf = new StringBuffer();
        }
           
        
        public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
        {
            if(!qName.equals("segment"))
            {
                processContentMarkup(qName, attributes);
            }
        }
        


        public void characters(char[] ch, int start, int length)
            throws SAXException
        {
            String content = new String(ch, start, length);
            content = m_xmlEncoder.encodeStringBasic(content);
            m_buf.append(content);
        }


        // ErrorHandler interface methods

        public void error(SAXParseException e)
            throws SAXException
        {
            throw new SAXException(" Segment text parse error at\n  line "
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
            m_buf.append("<").append(p_qName).append(" ");
            String attStr = makeAttributesString(p_attributes);
            m_buf.append(attStr).append("/>");
        }


        private String makeAttributesString(Attributes p_attributes)
        {
            StringBuffer buf = new StringBuffer();
            
            int len = p_attributes.getLength();
            for(int i = 0; i < len; i++)
            {
                String key = p_attributes.getQName(i);
                String value = p_attributes.getValue(i);
                    
                if(key.equals("i") || key.equals("x"))
                {
                    int num = Integer.parseInt(value);
                    num = m_order * 100 + num;
                    value = Integer.toString(num);
                }
                    
                value = m_xmlEncoder.encodeStringBasic(value);
                buf.append(key).append("=\"").append(value).append("\" ");
            }
            
            return buf.toString();
        }
        
            
        public String getText()
        {
            return m_buf.toString();
        }

    }


    private class TagCheckerHandler
        extends DefaultHandler
    {
        private TagChecker m_tagChecker;
            

        public void startDocument()
        {
            m_tagChecker = new TagChecker();
        }
           
        
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


        private void processContentMarkup(
            String p_qName, Attributes p_attributes)
        {
            String erasable = p_attributes.getValue("erasable");
            if(erasable != null && erasable.equals("yes"))
            {
                return;
            }
                
            String type = p_attributes.getValue("type");
            if(type == null)
            {
                type = "end"; // for <it> with no type
            }
                
            String ixValue;
                
            if(p_qName.equals("bpt"))
            {
                ixValue = p_attributes.getValue("i");
            }
            else
            {
                ixValue = p_attributes.getValue("x");
            }
                
            Integer ixValueInteger = null;
            if(ixValue != null)
            {
                ixValueInteger = new Integer(ixValue);
            }
                
            m_tagChecker.addTag(p_qName, type, ixValueInteger);
        }
        
            
        public TagChecker getTagChecker()
        {
            return m_tagChecker;
        }

    }


    private class TagDeleteHandler
        extends DefaultHandler
    {
        private XmlEntities m_xmlEncoder;
        private TagChecker m_tagChecker;
        private StringBuffer m_buf;
            
        private TagDeleteHandler(TagChecker p_tagChecker)
        {
            m_xmlEncoder = new XmlEntities();
            m_tagChecker = p_tagChecker;
            m_buf = new StringBuffer();
        }
           
        
        public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
        {
            if(qName.equals("segment"))
            {
                m_buf.append("<segment>");
            }
            else if(!isDelete(qName, attributes))
            {
                appendTag(qName, attributes);
            }
        }
        

        public void endElement(String uri, String localName, String qName)
        {
            if(qName.equals("segment"))
            {
                m_buf.append("</segment>");
            }
        }


        public void characters(char[] ch, int start, int length)
            throws SAXException
        {
            String content = new String(ch, start, length);
            content = m_xmlEncoder.encodeStringBasic(content);
            m_buf.append(content);
        }


        // ErrorHandler interface methods

        public void error(SAXParseException e)
            throws SAXException
        {
            throw new SAXException(" Segment text parse error at\n  line "
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


        private boolean isDelete(String p_qName, Attributes p_attributes)
        {
            String erasable = p_attributes.getValue("erasable");
            if(erasable != null && erasable.equals("yes"))
            {
                return false;
            }

            String type = p_attributes.getValue("type");
            if(type == null)
            {
                if(p_qName.equals("ept"))
                {
                    type = "";
                }
                else
                {
                    type = "end"; // for <it> with no type
                }
            }
                
            String ixValue;
                
            if(p_qName.equals("bpt") || p_qName.equals("ept"))
            {
                ixValue = p_attributes.getValue("i");
            }
            else
            {
                ixValue = p_attributes.getValue("x");
            }
                
            Integer ixValueInteger = null;
            if(ixValue != null)
            {
                ixValueInteger = new Integer(ixValue);
            }
                
            return m_tagChecker.isDelete(p_qName, type, ixValueInteger);
        }
                

        private void appendTag(String p_qName, Attributes p_attributes)
        {
            m_buf.append("<").append(p_qName).append(" ");
            String attStr = makeAttributesString(p_attributes);
            m_buf.append(attStr).append("/>");
        }


        private String makeAttributesString(Attributes p_attributes)
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
            
            return buf.toString();
        }
        
            
        public String getText()
        {
            return m_buf.toString();
        }

    }


    private class TagChecker
    {
        // bpt tags
        // key: type (String)
        // value: List of i attribute value (Integer)
        private Map m_bptTags = new HashMap();

        // ph tags
        // key: type (String)
        // value: List of x attribute value (Integer)
        private Map m_phTags = new HashMap();

        // ph tags without x attribute
        // key: type (String)
        // value: number of tags (Integer)
        private Map m_phNoXTags = new HashMap();

        // it tags
        // key: type (String) Type "end" is a ph tag with pos="end"
        // value: List of x attribute value (Integer)
        private Map m_itTags = new HashMap();


        // list of tags to be deleted. A String object represents
        // a tag. The format is tag name + type + i or x value
        // delimited by ">" character. Form example, <bpt
        // type="bold" i="1"> will be represented by a string
        // "bpt>bold>1" .
        private List m_deleteList = new ArrayList();
            

        // list of ph with no x attribute tags to be deleted.
        // key: type (String)
        // value: number of tags to be deleted (Integer). This
        // value will be changed as tags get deleted. 0 means
        // delete no more.
        private Map m_phNoXDeleteList = new HashMap();
            

        /**
         * Add tags to prepare for the tag checking. Tag name must
         * be one of "bpt", "it" and "ph" . "ept" tag doesn't need
         * to be added. We know that "ept" exists when "bpt" does
         * and their "i" attribute match.
         *
         * When "ph" doesn't have "x" attribute (it seems to
         * happen when its type is "x-nbsp"), pass null to
         * p_ixValue. When "it" tag's "pos" is "end", it doesn't
         * have "type". In that case, pass "end" to p_type.
         *
         * @param p_tagName tag name
         * @param p_type type from "type" attribute of the tag
         * @param p_ixValue value of "i" or "x" attribute of the
         * tag. For "bpt", it's "i" value. For the other tag, it's
         * "x" value.
         */
        public void addTag(
            String p_tagName, String p_type, Integer p_ixValue)
        {
            if(p_tagName.equals("bpt"))
            {
                addTag(m_bptTags, p_type, p_ixValue);
            }
            else if(p_tagName.equals("it"))
            {
                addTag(m_itTags, p_type, p_ixValue);
            }
            else if(p_tagName.equals("ph"))
            {
                if(p_ixValue == null)
                {
                    addTag(m_phNoXTags, p_type);
                }
                else
                {
                    addTag(m_phTags, p_type, p_ixValue);
                }
            }
        }
            

        /**
         * Builds list of tags to be deleted by comparing to the
         * other segment's TagChecker object. The list of tags
         * belong to the segment that this TagChecker
         * represents. The list doesn't contain the tags in the
         * other segment.
         *
         * @param p_other Other segment's TagChecker object
         */
        public void buildDeleteList(TagChecker p_other)
        {
            buildDeleteList("bpt", m_bptTags, p_other.m_bptTags);
            buildDeleteList("it", m_itTags, p_other.m_itTags);
            buildDeleteList("ph", m_phTags, p_other.m_phTags);
            buildPhNoXDeleteList(p_other.m_phNoXTags);
        }
            

        /**
         * Checks if the specified tag should be deleted.  Tag
         * name must be one of "bpt", "ept", "it" and "ph" .
         *
         * When the tag is "ept", pass an empty string ("", not
         * null) to p_type.  When "ph" doesn't have "x" attribute
         * (it seems to happen when its type is "x-nbsp"), pass
         * null to p_ixValue. When "it" tag's "pos" is "end", it
         * doesn't have "type". In that case, pass "end" to
         * p_type.
         *
         * @param p_tagName tag name
         * @param p_type type from "type" attribute of the tag
         * @param p_ixValue value of "i" or "x" attribute of the
         * tag. For "bpt", it's "i" value. For the other tag, it's
         * "x" value.
         * @return true if the tag should be deleted.
         */
        public boolean isDelete(
            String p_tagName, String p_type, Integer p_ixValue)
        {
            boolean result = false;
                
            if(p_ixValue != null)
            {
                String tagStr = composeDeleteTagStr(
                    p_tagName, p_type, p_ixValue.intValue());
                
                if(m_deleteList.contains(tagStr))
                {
                    result = true;
                }
            }
            else
            {
                Integer numInteger = (Integer)m_phNoXDeleteList.get(p_type);
                if(numInteger != null)
                {
                    int num = numInteger.intValue();
                    if(num > 0)
                    {
                        result = true;
                        m_phNoXDeleteList.put(p_type, new Integer(num - 1));
                    }
                }
            }
                
            return result;
        }


        private void addTag(
            Map p_tagMap, String p_type, Integer p_ixValue)
        {
            List l = (List)p_tagMap.get(p_type);
            if(l == null)
            {
                l = new ArrayList();
                p_tagMap.put(p_type, l);
            }
                
            l.add(p_ixValue);
        }
            

        private void addTag(Map p_tagMap, String p_type)
        {
            Integer numInteger = (Integer)p_tagMap.get(p_type);
            int num = 0;
            if(numInteger != null)
            {
                num = numInteger.intValue();
            }
            num++;
                
            numInteger = new Integer(num);
            p_tagMap.put(p_type, numInteger);
        }
            


        private void buildDeleteList(
            String p_tagName, Map p_sourceMap, Map p_targetMap)
        {
            boolean isBpt = p_tagName.equals("bpt");
                
            Set sourceKeys = p_sourceMap.keySet();
            Iterator it = sourceKeys.iterator();
            while(it.hasNext())
            {
                String type = (String)it.next();
                    
                List sourceIXvalues = (List)p_sourceMap.get(type);
                List targetIXvalues = (List)p_targetMap.get(type);
                if(targetIXvalues == null)
                {
                    // delete all source tags
                    addDeleteTags(p_tagName, type, sourceIXvalues);
                    if(isBpt)
                    {
                        addDeleteTags("ept", "", sourceIXvalues);
                    }
                }
                else
                {
                    int sourceSize = sourceIXvalues.size();
                    int targetSize = targetIXvalues.size();
                        
                    if(sourceSize > targetSize)
                    {
                        List deleteIXvalues = sourceIXvalues.subList(
                            targetSize, sourceSize);
                        addDeleteTags(p_tagName, type, deleteIXvalues);
                        if(isBpt)
                        {
                            addDeleteTags("ept", "", deleteIXvalues);
                        }
                    }
                }
            }
        }
            

        private void addDeleteTags(
            String p_tagName, String p_type, List p_ixValues)
        {
            Iterator it = p_ixValues.iterator();
            while(it.hasNext())
            {
                Integer ixValue = (Integer)it.next();
                String deleteTagStr = composeDeleteTagStr(
                    p_tagName, p_type, ixValue.intValue());
                    
                m_deleteList.add(deleteTagStr);
            }
        }


        private String composeDeleteTagStr(
            String p_tagName, String p_type, int p_ixValue)
        {
            return p_tagName + ">" + p_type + ">" + p_ixValue;
        }
            

        private void buildPhNoXDeleteList(Map p_targetPhTags)
        {
            Set sourceKeys = m_phNoXTags.keySet();
            Iterator it = sourceKeys.iterator();
            while(it.hasNext())
            {
                String type = (String)it.next();
                    
                Integer sourceNum = (Integer)m_phNoXTags.get(type);
                Integer targetNum = (Integer)p_targetPhTags.get(type);
                if(targetNum == null)
                {
                    m_phNoXDeleteList.put(type, sourceNum);
                }
                else
                {
                    int sourceSize = sourceNum.intValue();
                    int targetSize = targetNum.intValue();
                        
                    if(sourceSize > targetSize)
                    {
                        m_phNoXDeleteList.put(
                            type, new Integer(sourceSize - targetSize));
                    }
                }
            }
        }
    }
        
}
