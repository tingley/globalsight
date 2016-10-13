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
package com.globalsight.ling.tm2.leverage;

import org.apache.log4j.Logger;


/**
 * TokenMatch holds a match score and segment ids as a result of index
 * matching.
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 */

public class TokenMatch
    implements Comparable
{
    private static Logger c_logger =
        Logger.getLogger(
            TokenMatch.class.getName());

    private long m_originalTuvId;
    private String m_originalSubId;
    private long m_matchedTuId;
    private float m_score;

    
    // constructor
    public TokenMatch(long p_originalTuvId,
        String p_originalSubId, long p_matchedTuId, float p_score)
    {
        m_originalTuvId = p_originalTuvId;
        m_originalSubId = p_originalSubId;
        m_matchedTuId = p_matchedTuId;
        m_score = p_score;
    }


    public long getOriginalTuvId()
    {
        return m_originalTuvId;
    }
    
    public String getOriginalSubId()
    {
        return m_originalSubId;
    }
    
    public long getMatchedTuId()
    {
        return m_matchedTuId;
    }
    
    public float getScore()
    {
        return m_score;
    }


    public int compareTo(Object o)
    {
        TokenMatch other = (TokenMatch)o;
        
        return (int)(m_score - other.m_score);
    }
    

    // not used for now
//     public boolean equals(Object p_other)
//     {
//         if(p_other instanceof TokenMatch)
//         {
//             TokenMatch other = (TokenMatch)p_other;
            
// 	    return ((m_originalTuvId == other.m_originalTuvId)
//                 && (m_originalSubId.equals(other.p_originalSubId))
//                 && (m_matchedTuId == other.m_matchedTuId));
// 	}
//         return false;
//     }
    

//     public int hashCode()
//     {
//         return (int)(m_originalTuvId
//             + m_originalSubId.hashCode() + m_matchedTuId);
//     }


    
}
