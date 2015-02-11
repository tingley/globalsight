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
package com.globalsight.ling.aligner.io;

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.everest.tm.util.Tmx;

import java.io.Reader;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Iterator;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;


/**
 * TmxReader reads TMX file used for the alignment and returns a List
 * of BaseTmTuv objects. The TMX file used for the alignment project
 * is a monolingual, one TUV per TU file.
 */

public class TmxReader
{
    private String m_fileName;
    
    /**
     * Reads a TMX file from a Reader and returns a List of BaseTmTuv objects.
     *
     * @param p_reader Reader object from which the TMX file is read
     * @param p_locale Locale of the TMX file
     * @param p_fileName TMX file name (for error message)
     * @return List of BaseTmTuv objects
     */
    public List read(
        Reader p_reader, GlobalSightLocale p_locale, String p_fileName)
        throws Exception
    {
        // TMX file name
        m_fileName = p_fileName;
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        
        SAXParser parser = factory.newSAXParser();
        TmxHandler handler = new TmxHandler(p_locale);
        parser.parse(new InputSource(p_reader), handler);

        return handler.getTuvs();
    }
    

    private class TmxHandler
        extends DefaultHandler
    {
        private XmlEntities m_xmlEncoder;
        
        private GlobalSightLocale m_locale;
        private List m_tuvList;
        
        private BaseTmTu m_currentTu;
        private BaseTmTuv m_currentTuv;

        private StringBuffer m_currentContent;
        
        private boolean inProp;
        private boolean inSeg;
        private boolean inSub;
        
        

        private TmxHandler(GlobalSightLocale p_locale)
        {
            m_xmlEncoder = new XmlEntities();
            
            m_locale = p_locale;
            m_tuvList = new ArrayList();
            inProp = false;
            inSeg = false;
            inSub = false;
        }
           
        
        public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
        {
            try
            {
                if(qName.equals("tu"))
                {
                    processTuStart(attributes);
                }
                else if(qName.equals("seg"))
                {
                    processSegStart(attributes);
                }
                else if(qName.equals("prop"))
                {
                    processPropStart(attributes);
                }
                else if(qName.equals("bpt") || qName.equals("ept")
                    || qName.equals("ph") || qName.equals("it"))
                {
                    processContentMarkupStart(qName, attributes);
                }
                else if(qName.equals("sub"))
                {
                    processSubStart(attributes);
                }
            }
            catch(Exception e)
            {
                SAXException se;
                if(e instanceof SAXException)
                {
                    se = (SAXException)e;
                }
                else
                {
                    se = new SAXException(e);
                }
                
                throw se;
            }
        }
        


        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            try
            {
                if(qName.equals("seg"))
                {
                    processSegEnd();
                }
                else if(qName.equals("prop"))
                {
                    processPropEnd();
                }
                else if(qName.equals("bpt") || qName.equals("ept")
                    || qName.equals("ph") || qName.equals("it"))
                {
                    processContentMarkupEnd(qName);
                }
                else if(qName.equals("sub"))
                {
                    processSubEnd();
                }
            }
            catch(Exception e)
            {
                SAXException se;
                if(e instanceof SAXException)
                {
                    se = (SAXException)e;
                }
                else
                {
                    se = new SAXException(e);
                }
                
                throw se;
            }
        }
        


        public void characters(char[] ch, int start, int length)
            throws SAXException
        {
            if(inProp || (inSeg && (!inSub)))
            {
                String content = new String(ch, start, length);
                content = m_xmlEncoder.encodeStringBasic(content);
                
                m_currentContent.append(content);
            }
        }
        

        public InputSource resolveEntity(String publicId, String systemId)
        {
            if(systemId.indexOf("tmx-gs.dtd") > -1)
            {
                InputStream stream = TmxReader.class.getResourceAsStream(
                    "/resources/tmx-gs.dtd");
                return new InputSource(stream);
            }
            else
            {
              // use the default behaviour
                return null;
            }
        }


    // ErrorHandler interface methods

        public void error(SAXParseException e)
            throws SAXException
        {
            throw new SAXException(m_fileName + " parse error at\n  line "
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
            System.err.println(m_fileName + " parse warning at\n  line "
                + e.getLineNumber()
                + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
        }


        // handler private methods
        
        private void processTuStart(Attributes p_attributes)
            throws Exception
        {
            m_currentTu = new PageTmTu();

            String tuIdStr = p_attributes.getValue("tuid");
            Long tuId = new Long(tuIdStr);
            
            String dataType = p_attributes.getValue("datatype");

            m_currentTu.setId(tuId.longValue());
            m_currentTu.setFormat(dataType);
            m_currentTu.setTranslatable();
        }
        
            
        private void processPropStart(Attributes p_attributes)
        {
            String type = p_attributes.getValue("type");
            if(type.equals(Tmx.PROP_SEGMENTTYPE))
            {
                inProp = true;
                m_currentContent = new StringBuffer();
            }
        }


        private void processPropEnd()
        {
            if(inProp)
            {
                m_currentTu.setType(m_currentContent.toString());
                inProp = false;
            }
        }
        
        private void processSegStart(Attributes p_attributes)
        {
            m_currentTuv = new PageTmTuv();
            m_currentTuv.setId(m_currentTu.getId());
            m_currentTuv.setLocale(m_locale);
            m_currentTu.addTuv(m_currentTuv);
            
            inSeg = true;
            m_currentContent = new StringBuffer();
            m_currentContent.append("<segment>");
        }
        
            
        private void processSegEnd()
            throws Exception
        {
            m_currentTuv.setSegment(
                m_currentContent.toString() + "</segment>");

            inSeg = false;

            // Convert PageTmTuv to SegmentTmTuv so that it can be
            // saved in the Segment TM. Subflows are stripped so
            // createSegmentTmTus() returns only one SegmentTmTu.
            Iterator it
                = TmUtil.createSegmentTmTus(m_currentTu, m_locale).iterator();

            BaseTmTu tu = (BaseTmTu)it.next();
            BaseTmTuv tuv = tu.getFirstTuv(m_locale);
            
            m_tuvList.add(tuv);
        }
        

        private void processContentMarkupStart(
            String p_qName, Attributes p_attributes)
        {
            m_currentContent.append("<").append(p_qName).append(" ");
            String attStr = makeAttributesString(p_attributes);
            m_currentContent.append(attStr).append(">");
        }
        

        private void processContentMarkupEnd(String p_qName)
        {
            m_currentContent.append("</").append(p_qName).append(">");
        }
        

        // Subflows are simply ignored. They are not displayed in the
        // alignment QA client and not confirmed as properly aligned
        // by a QA person.
        private void processSubStart(Attributes p_attributes)
        {
            inSub = true;
        }
        
        
        private void processSubEnd()
        {
            inSub = false;
        }


        private String makeAttributesString(Attributes p_attributes)
        {
            StringBuffer buf = new StringBuffer();
            
            int len = p_attributes.getLength();
            for(int i = 0; i < len; i++)
            {
                buf.append(p_attributes.getQName(i)).append("=\"");
                String value = p_attributes.getValue(i);
                value = m_xmlEncoder.encodeStringBasic(value);
                buf.append(value).append("\" ");
            }
            
            return buf.toString();
        }
        

        private List getTuvs()
        {
            return m_tuvList;
        }
        
    }
    

}
