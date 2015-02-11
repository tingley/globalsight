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
package com.globalsight.ling.docproc.extractor.xml;

// Xerces & Xalan
import org.apache.xerces.framework.XMLDocumentScanner;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.utils.ChunkyCharArray;
import org.apache.xerces.validators.common.GrammarResolverImpl;
import org.apache.xerces.validators.common.XMLValidator;


public class GsDOMParser extends DOMParser
{
    // XML declaration flags
    private boolean  m_haveXMLDecl = false;
    private String  m_version = null;
    private String m_standalone = null;
    private String encoding = null;
    private boolean emptyCdate = false;

    // Constructor
    public GsDOMParser()
        throws Exception
    {
        super();
        fEntityHandler = new GsXMLEntityHandler(fStringPool, fErrorReporter);

        // The following codes are copied from XMLParser(). It is
        // necessary because they are dependent to fEntityHandler and
        // each other.
        fScanner = new XMLDocumentScanner(fStringPool, fErrorReporter,
          fEntityHandler, new ChunkyCharArray(fStringPool));
        fValidator = new XMLValidator(fStringPool, fErrorReporter,
          fEntityHandler, fScanner);
        fGrammarResolver = new GrammarResolverImpl();
        fScanner.setGrammarResolver(fGrammarResolver);
        fValidator.setGrammarResolver(fGrammarResolver);

        // from DOMParser
        initHandlers(false, this, this);

        // make namespace aware
        setNamespaces(true);
    }

    /**
     * Overrides DOMPaeser#startDocument.
     *
     * This method stores XML version and standalone value.
     */
    public void xmlDecl(int versionIndex, int encodingIndex,
      int standAloneIndex)
        throws Exception
    {
        m_haveXMLDecl = true;
        m_version = null;
        m_standalone = null;
        encoding = null;

        if (versionIndex != -1)
        {
            m_version = fStringPool.toString(versionIndex);
        }

        if (standAloneIndex != -1)
        {
            m_standalone = fStringPool.toString(standAloneIndex);
        }
        
        if (encodingIndex != -1)
        {
        	encoding = fStringPool.toString(encodingIndex);
        }

        super.xmlDecl(versionIndex, encodingIndex, standAloneIndex);
    }


    public boolean getHaveXMLDecl()
    {
        return m_haveXMLDecl;
    }

    public String getXMLVersion()
    {
        return m_version;
    }

    public String getStandalone()
    {
        return m_standalone;
    }
    
    public String getEncoding()
    {
        return encoding;
    }

    @Override
    public void endCDATA() throws Exception
    {
        if (emptyCdate)
        {
            characters("".toCharArray(), 0, 0);
        }
        
        super.endCDATA();
    }

    @Override
    public void characters(int arg0) throws Exception
    {
        emptyCdate = false;
        super.characters(arg0);
    }

    @Override
    public void startCDATA() throws Exception
    {
        emptyCdate = true;
        super.startCDATA();
    }

}
