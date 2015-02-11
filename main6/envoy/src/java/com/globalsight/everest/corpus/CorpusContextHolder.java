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
package com.globalsight.everest.corpus;


/**
 * CorpusContextHolder
 * Holds the source and target partial context for a pair of
 * locale-specific documents in the corpus that correspond
 * to a particular segment in the TM.
 */
public class CorpusContextHolder
{
    //////////////////////////////////////
    // Static Members                  //
    /////////////////////////////////////

    //constants used for TOPLink queries - should match the values below
    public static final String PARTIAL_CONTEXT = "m_partialContext";
    public static final String LINK_DATE = "m_linkDate";
    public static final String TUV_ID = "m_tuvId";
    public static final String CUV_ID = "m_cuvId";

    //////////////////////////////////////
    // Private Members                  //
    //////////////////////////////////////
    private CorpusContext m_sourceContext = null;
    private CorpusContext m_targetContext = null;

    //////////////////////////////////////
    // Constructor                      //
    //////////////////////////////////////
    
    /**
     * Creates a CorpusContext object
     */
    public CorpusContextHolder()
    { 
    }

    /**
     * Creates a CorpusContextHolder with the given
     * source and target contexts.
     * 
     * @param p_srcContext
     * @param p_tgtContext
     */
    public CorpusContextHolder(CorpusContext p_srcContext, CorpusContext p_tgtContext)
    { 
        m_sourceContext = p_srcContext;
        m_targetContext = p_tgtContext;
    }


    //////////////////////////////////////
    // Public Methods                   //
    //////////////////////////////////////

    /**
     * Returns the source context
     * @return CorpusContext
     */
    public CorpusContext getSourceContext()
    {
        return m_sourceContext;
    }

    /**
     * Sets the source context
     * @param p_srcContext
     */
    public void setSourceContext(CorpusContext p_srcContext)
    {
        m_sourceContext = p_srcContext;
    }

    /**
     * Returns the target context
     * @return CorpusContext
     */
    public CorpusContext getTargetContext()
    {
        return m_targetContext;
    }

    /**
     * Sets the target context
     * @param p_tgtContext
     */
    public void setTargetContext(CorpusContext p_tgtContext)
    {
        m_targetContext = p_tgtContext;
    }
}

