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
package com.globalsight.ling.tm.fuzzy;

import java.io.Serializable;

/**
A potential fuzzy match.
*/
public class FuzzyCandidate
    implements Serializable
{
    private long m_tuvId = -1;
    private long m_tmId = -1;
    private short m_fuzzyScore = 0;

    /**
    FuzzyCandidate constructor comment.
    */
    public FuzzyCandidate()
    {
        super();
    }

    /**

    @param p_tuvId long
    @param p_fuzzyScore short
    */
    public FuzzyCandidate(long p_tuvId, short p_fuzzyScore)
    {
        m_tuvId = p_tuvId;
        m_fuzzyScore = p_fuzzyScore;
    }

    /**

    @return short
    */
    public short getFuzzyScore()
    {
        return m_fuzzyScore;
    }

    /**

    @return long
    */
    public long getTuvId()
    {
        return m_tuvId;
    }

    /**

    @param newM_fuzzyScore float
    */
    public void setFuzzyScore(short p_fuzzyScore)
    {
        m_fuzzyScore = p_fuzzyScore;
    }

    /**

    @param newM_tuvId long
    */
    public void setTuvId(long p_tuvId)
    {
        m_tuvId = p_tuvId;
    }

    /**
     *
     * @return long
     */
    public long getTmId()
    {
        return m_tmId;
    }

    /**
     *
     * @param newM_tmId long
     */
    public void setTmId(long p_tmId)
    {
        m_tmId = p_tmId;
    }

    // derived from Object class. If m_tuvId has the same value, they
    // are considered as the same.
    public boolean equals(Object p_obj)
    {
        boolean ret = false;
        
        if(p_obj instanceof FuzzyCandidate)
        {
            if(m_tuvId == ((FuzzyCandidate)p_obj).m_tuvId)
            {
                ret = true;
            }
        }
        return ret;
    }


    // derived from java.lang.Object class.
    public int hashCode()
    {
        return (int)m_tuvId;
    }
    
}
