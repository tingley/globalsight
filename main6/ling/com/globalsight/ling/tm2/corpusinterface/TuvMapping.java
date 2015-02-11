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
package com.globalsight.ling.tm2.corpusinterface;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;

/**
 * The TuvMapping class represent a mapping of segments in
 * project_tm_tuv_t and translation_unit_variant tables. This mapping
 * is used to build the Corpus based TM.
 */

public class TuvMapping
    implements Serializable
{
    private static Logger c_logger =
        Logger.getLogger(
            TuvMapping.class.getName());

    private long m_tuvId;  // translation_unit_variant id
    private long m_tuId;   // translation_unit id
    private long m_tmId;   // project_tm (id)
    private long m_projectTmTuId; // project_tm_tu_t_id
    private long m_projectTmTuvId; // project_tm_tuv_t id
    

    /**
     * Note that p_tmTuId and p_tmTuvId may be EITHER tm2 ids (references to
     * project_tm_tu(v)_t tables), OR tm3 ids.  This is why the tmid is necessary
     * (and also because tm3 data may be storing different TM data in different tables.)
     */
    public TuvMapping(long p_tuvId, long p_tuId, long p_tmId, long p_tmTuId, long p_tmTuvId)
    {
        m_tuvId = p_tuvId;
        m_tuId = p_tuId;
        m_tmId = p_tmId;
        m_projectTmTuId = p_tmTuId;
        m_projectTmTuvId = p_tmTuvId;
    }
    

    public long getTuvId()
    {
        return m_tuvId;
    }
    

    public long getTuId()
    {
        return m_tuId;
    }
    
    public long getTmId()
    {
        return m_tmId;
    }
    
    public long getProjectTmTuId()
    {
        return m_projectTmTuId;
    }


    public long getProjectTmTuvId()
    {
        return m_projectTmTuvId;
    }
    

    public String toDebugString()
    {
        return "Tuv id: " + m_tuvId + " Tu id: " + m_tuId
            + " TM ID: " + m_tmId 
            + " Project Tm Tu id: " + m_projectTmTuId
            + " Project Tm Tuv id: " + m_projectTmTuvId + "\n";
    }
    
}
