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
package com.globalsight.everest.webapp.pagehandler.tm.corpus;
import java.io.Serializable;
import com.globalsight.everest.corpus.CorpusContext;
import com.globalsight.everest.corpus.CorpusDoc;

/**
 * A CorpusViewBean is a java bean that holds some values for use by JSPs
 * related to source / target corpus comparisons.
 */
public class CorpusViewBean implements Serializable
{
    //////////////////////////////////////
    // Private Members                  //
    //////////////////////////////////////
    private CorpusContext m_sourceContext;
    private CorpusContext m_targetContext;
    private CorpusDoc m_sourceDoc;
    private CorpusDoc m_targetDoc;

    //////////////////////////////////////
    // Constructor                      //
    //////////////////////////////////////
    /**
     * Creates a CorpusViewBean object
     */
    public CorpusViewBean(CorpusContext p_sourceContext,
                          CorpusContext p_targetContext,
                          CorpusDoc p_sourceDoc,
                          CorpusDoc p_targetDoc)
    {
        m_sourceContext = p_sourceContext;
        m_targetContext = p_targetContext;
        m_sourceDoc = p_sourceDoc;
        m_targetDoc = p_targetDoc;
    }

    //////////////////////////////////////
    // Public Methods                   //
    //////////////////////////////////////

    /**
     * Returns the source corpus doc
     */
    public CorpusDoc getSourceCorpusDoc()
    {
        return m_sourceDoc;
    }
    
    /**
     * Returns the target corpus doc
     */
    public CorpusDoc getTargetCorpusDoc()
    {
        return m_targetDoc;
    }

    /**
     * Returns the source context
     */
    public CorpusContext getSourceContext()
    {
        return m_sourceContext;
    }

    /**
     * Returns the target context
     */
    public CorpusContext getTargetContext()
    {
        return m_targetContext;
    }
}

