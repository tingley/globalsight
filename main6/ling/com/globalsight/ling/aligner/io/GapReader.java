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

import com.globalsight.ling.aligner.AlignmentProject;
import com.globalsight.ling.aligner.AlignmentUnit;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.corpus.CorpusDoc;
import com.globalsight.everest.corpus.CorpusManager;

import java.io.Reader;
import java.io.StringReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;


/**
 * GapReader reads GlobalSight Alignment Package (GAP) file and set
 * the info in the file to AlignmentProject object as well as create
 * AlignmentUnit objects.
 */

public class GapReader
    implements GapConstants
{
    private AlignmentProject m_alignmentProject;
    

    public GapReader(AlignmentProject p_alignmentProject)
    {
        m_alignmentProject = p_alignmentProject;
    }
    

    public void read(Reader p_reader)
        throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        
        SAXParser parser = factory.newSAXParser();
        parser.parse(new InputSource(p_reader), new GapHandler());
    }
    
    
    private class GapHandler
        extends DefaultHandler
    {
        public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
        {
            try
            {
                if(qName.equals(ELEM_GAP))
                {
                    processGap(attributes);
                }
                else if(qName.equals(ELEM_FILES))
                {
                    processFiles(attributes);
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
        

        public InputSource resolveEntity(String publicId, String systemId)
        {
            if(systemId.indexOf("gap.dtd") > -1)
            {
                return new InputSource(new StringReader(GAP_DTD));
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
            throw new SAXException("GAP file parse error at\n  line "
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
            System.err.println("GAP file parse warning at\n  line "
                + e.getLineNumber()
                + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
        }


        private void processGap(Attributes p_attributes)
            throws Exception
        {
            LocaleManager localeMgr = ServerProxy.getLocaleManager();

            String sourceLocaleName = p_attributes.getValue(ATT_SOURCE_LOCALE);
            String targetLocaleName = p_attributes.getValue(ATT_TARGET_LOCALE);
            
            GlobalSightLocale sourceLocale
                = localeMgr.getLocaleByString(sourceLocaleName);
            GlobalSightLocale targetLocale
                = localeMgr.getLocaleByString(targetLocaleName);
            
            m_alignmentProject.setSourceLocale(sourceLocale);
            m_alignmentProject.setTargetLocale(targetLocale);
        }
        

        private void processFiles(Attributes p_attributes)
            throws Exception
        {
            String originalSourceFile
                = p_attributes.getValue(ATT_ORIGINAL_SOURCE_FILE);
            String originalTargetFile
                = p_attributes.getValue(ATT_ORIGINAL_TARGET_FILE);
            CorpusDoc soruceCorpusDoc
                = getCorpusDoc(p_attributes.getValue(ATT_SOURCE_CUV_ID));
            CorpusDoc targetCorpusDoc
                = getCorpusDoc(p_attributes.getValue(ATT_TARGET_CUV_ID));
            String soruceTmxFile = p_attributes.getValue(ATT_SOURCE_TMX);
            String targetTmxFile = p_attributes.getValue(ATT_TARGET_TMX);
            String gamFile = p_attributes.getValue(ATT_GAM);
            String state = p_attributes.getValue(ATT_STATE);

            AlignmentUnit alignmentUnit = new AlignmentUnit(
                originalSourceFile, originalTargetFile,
                m_alignmentProject.getProjectTmpDirectory());

            alignmentUnit.setSourceCorpusDoc(soruceCorpusDoc);
            alignmentUnit.setTargetCorpusDoc(targetCorpusDoc);
            alignmentUnit.setSourceTmxFileName(soruceTmxFile);
            alignmentUnit.setTargetTmxFileName(targetTmxFile);
            alignmentUnit.setGamFileName(gamFile);
            alignmentUnit.setState(state);

            m_alignmentProject.addAlignmentUnit(alignmentUnit);
        }

        private CorpusDoc getCorpusDoc(String p_corpusIdStr)
            throws Exception
        {
            Long id = new Long(p_corpusIdStr);
            
            CorpusManager corpusManager = ServerProxy.getCorpusManager();
            return corpusManager.getCorpusDoc(id);
        }
        
    }
    

}
