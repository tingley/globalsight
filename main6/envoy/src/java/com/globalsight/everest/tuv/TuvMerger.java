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

package com.globalsight.everest.tuv;

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


public class TuvMerger
{
    /**
     * Merge the text of TUVs. The values of "id" of <sub> tags are
     * adjusted. 100 * order in p_tuvs will be added to each
     * value. TUV objects in p_tuvs won't be changed by this method.
     *
     * @param p_tuvs List of TUV objects. The text are merged in this order.
     * @return merged GXML string
     */
    public static String getMergedText(List p_tuvs)
        throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        CombineHandler handler = null;
        StringBuffer buf = new StringBuffer();
            
        int len = p_tuvs.size();
        for(int i = 0; i < len; i++)
        {
            Tuv tuv = (Tuv)p_tuvs.get(i);
            
            handler = new CombineHandler(i);
            parser.parse(new InputSource(
                new StringReader(tuv.getGxml())), handler);

            if(i == 0)
            {
                buf.append(handler.getFirstTag());
            }
            
            buf.append(handler.getText());
        }
            
        buf.append(handler.getLastTag());

        return buf.toString();
    }

        
    /**
     * Merge the text of TUVs. The merged text will be stored in the
     * first TUV in p_tuvs. The other TUV objects will get an empty
     * GXML (only top tags). The values of "id" of <sub> tags are
     * adjusted. 100 * order in p_tuvs will be added to each value.
     *
     * @param p_tuvs List of TUV objects. The text are merged in this order.
     */
    public static void mergeTuvs(List p_tuvs)
        throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        CombineHandler handler = null;
        StringBuffer buf = new StringBuffer();
            
        int len = p_tuvs.size();
        for(int i = 0; i < len; i++)
        {
            Tuv tuv = (Tuv)p_tuvs.get(i);
            
            handler = new CombineHandler(i);
            parser.parse(new InputSource(
                new StringReader(tuv.getGxml())), handler);

            if(i == 0)
            {
                buf.append(handler.getFirstTag());
            }
            
            buf.append(handler.getText());

            tuv.setGxml(handler.getFirstTag() + handler.getLastTag());
        }
            
        buf.append(handler.getLastTag());

        Tuv tuv = (Tuv)p_tuvs.get(0);
        tuv.setGxmlWithSubIds(buf.toString());
    }

        

    /**
     * Merge the list of text. The values of "id" of <sub> tags are
     * adjusted. 100 * order in p_text will be added to each value.
     *
     * @param p_text List of GXML strings. The root element must be
     * <segment> or <localizable>.
     * @return merged GXML string
     */
    public static String mergeStrings(List p_text)
        throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        CombineHandler handler = null;
        StringBuffer buf = new StringBuffer();
            
        int len = p_text.size();
        for(int i = 0; i < len; i++)
        {
            String gxml = (String)p_text.get(i);
            
            handler = new CombineHandler(i);
            parser.parse(new InputSource(
                new StringReader(gxml)), handler);

            if(i == 0)
            {
                buf.append(handler.getFirstTag());
            }
            
            buf.append(handler.getText());
        }
            
        buf.append(handler.getLastTag());

        return buf.toString();
    }

        
    private static class CombineHandler
        extends DefaultHandler
    {
        private XmlEntities m_xmlEncoder;
        private int m_order;
        private StringBuffer m_buf;

        private String m_firstTag;
        private String m_lastTag;
        
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
            if(qName.equals("segment") || qName.equals("localizable"))
            {
                processFirstTag(qName, attributes);
            }
            else if(qName.equals("sub"))
            {
                processSubTag(qName, attributes);
            }
            else
            {
                processContentMarkup(qName, attributes);
            }
        }
        

        public void endElement(String uri, String localName, String qName)
        {
            if(qName.equals("segment") || qName.equals("localizable"))
            {
                processLastTag(qName);
            }
            else
            {
                processEndTag(qName);
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


        private void processFirstTag(
            String p_qName, Attributes p_attributes)
        {
            m_firstTag = "<" + p_qName + " "
                + makeAttributesString(p_attributes, false) + ">";
        }
        
            
        private void processLastTag(String p_qName)
        {
            m_lastTag = "</" + p_qName + ">";
        }
        

        private void processEndTag(String p_qName)
        {
            m_buf.append("</").append(p_qName).append(">");
        }
            

        private void processSubTag(
            String p_qName, Attributes p_attributes)
        {
            m_buf.append("<").append(p_qName).append(" ");
            String attStr = makeAttributesString(p_attributes, true);
            m_buf.append(attStr).append(">");
        }
        
        private void processContentMarkup(
            String p_qName, Attributes p_attributes)
        {
            m_buf.append("<").append(p_qName).append(" ");
            String attStr = makeAttributesString(p_attributes, false);
            m_buf.append(attStr).append(">");
        }


        private String makeAttributesString(
            Attributes p_attributes, boolean p_isSubTag)
        {
            StringBuffer buf = new StringBuffer();
            
            int len = p_attributes.getLength();
            for(int i = 0; i < len; i++)
            {
                String key = p_attributes.getQName(i);
                String value = p_attributes.getValue(i);
                    
//                 if(key.equals("i") || key.equals("x"))
//                 {
//                     int num = Integer.parseInt(value);
//                     num = m_order * 100 + num;
//                     value = Integer.toString(num);
//                 }
//                 else
                if(p_isSubTag && key.equals("id"))
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


        public String getFirstTag()
        {
            return m_firstTag;
        }
        
        public String getLastTag()
        {
            return m_lastTag;
        }

    }


}
