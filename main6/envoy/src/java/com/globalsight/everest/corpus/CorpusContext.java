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

import com.globalsight.everest.persistence.PersistentObject;
import java.io.Serializable;
import java.util.Date;
import com.globalsight.everest.corpus.CorpusDoc;

/**
 * A CorpusContext is a persistent object that corresponds to a row in the
 * corpus_map table. It holds the partial context for the mapping of one tuv to
 * one cuv.
 */
public class CorpusContext extends PersistentObject implements Serializable
{
    private static final long serialVersionUID = 6524579619404116764L;

    // ////////////////////////////////////
    // Static Members //
    // ///////////////////////////////////

    public static final String PARTIAL_CONTEXT = "m_partialContext";
    public static final String LINK_DATE = "m_linkDate";
    public static final String TUV_ID = "m_tuvId";
    public static final String CUV_ID = "m_cuvId";
    public static final String TU_ID = "m_tuId";

    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private String m_partialContext = null;
    private Date m_linkDate = null;
    private Long m_cuvId = null;
    private Long m_tuvId = null;
    private Long m_tuId = null;
    private Long m_tmId = null;
    
    private CorpusDoc corpusDoc;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////

    /**
     * Creates a CorpusContext object
     */
    public CorpusContext()
    {
        m_linkDate = new Date();
    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    /**
     * Returns the partial context
     * 
     * @return String
     */
    public String getPartialContext()
    {
        return m_partialContext;
    }

    /**
     * Sets the partial context
     * 
     * @param p_partialContext
     */
    public void setPartialContext(String p_partialContext)
    {
        m_partialContext = p_partialContext;
    }

    /**
     * Returns the date when the tuv was mapped to the cuv
     * 
     * @return Date
     */
    public Date getLinkDate()
    {
        return m_linkDate;
    }

    /**
     * Sets the date when the tuv was mapped to the cuv
     * 
     * @param p_linkDate
     */
    public void setLinkDate(Date p_linkDate)
    {
        m_linkDate = p_linkDate;
    }

    /**
     * Returns the cuv Id
     * 
     * @return String
     */
    public Long getCuvId()
    {
        return m_cuvId;
    }

    /**
     * Sets the cuv Id
     * 
     * @param p_cuvId
     */
    public void setCuvId(Long p_cuvId)
    {
        m_cuvId = p_cuvId;
    }

    /**
     * Returns the tuv Id
     * 
     * @return String
     */
    public Long getTuvId()
    {
        return m_tuvId;
    }

    /**
     * Sets the tuv Id
     * 
     * @param p_tuvId
     */
    public void setTuvId(Long p_tuvId)
    {
        m_tuvId = p_tuvId;
    }

    /**
     * Returns the tu Id
     * 
     * @return String
     */
    public Long getTuId()
    {
        return m_tuId;
    }

    /**
     * Sets the tu Id
     * 
     * @param p_tuId
     */
    public void setTuId(Long p_tuId)
    {
        m_tuId = p_tuId;
    }

    /**
     * Returns the TM ID.  This is a value in the PROJECT_TM 
     * table.  However, this value MAY BE NULL.  This occurs in
     * legacy situations where the tm2 segment storage is used.  
     * In that case, the tuId/tuvId values map into the 
     * project_tm_tu(v)_t tables; since those tables are flat, the 
     * legacy implementation did not bother to store a tmid.  
     * 
     * Non-null TM ID values may be newer tm2 data or for tm3 data.
     * The Project_TM table should be used and its SegmentTmInfo object
     * consulted as necessary.
     */
    public Long getTmId()
    {
        return m_tmId;
    }
    
    /**
     * Sets the TM id (key into the PROJECT_TM table).
     * @return 
     */
    public void setTmId(Long p_tmId)
    {
        m_tmId = p_tmId;
    }
    
    public CorpusDoc getCorpusDoc()
    {
        return corpusDoc;
    }

    public void setCorpusDoc(CorpusDoc cuv)
    {
        this.corpusDoc = cuv;       
        Long id = null;
        if (cuv != null)
        {
            id = cuv.getIdAsLong();
        }
        setCuvId(id);
    }
}
