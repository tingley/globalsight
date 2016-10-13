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
package com.globalsight.everest.edit.online;

import java.io.Serializable;

import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.TmUtil;

/**
 * <p>A data class that contains segment/subflow matching result: the
 * matched segment/subflow content and the matching percentage.</p>
 */
public class SegmentMatchResult
    implements Serializable
{
    private String m_matchContentSource;
    private String m_matchContent; 
    private String m_matchType = "";
    private float m_matchPercentage;
    //the project_tm_tuv_t id
    private long m_tuvId;
    private long m_tmId;
    private String tmName = "";
    private boolean mayHaveSid = false;
    
    private String sid;

    // These are used for saving the match TUV information
    private String matchedTuvJobName = null;
    private TuvBasicInfo matchedTuvBasicInfo;

    // matchContent must be a GXML segment without top tag
    public SegmentMatchResult(long p_tuvId, String p_matchContent,
            float p_matchPercentage, String p_matchType, long p_tmId, String tmName, 
        boolean mayHaveSid)
    {
        m_tuvId = p_tuvId;
        m_matchContent = p_matchContent;
        m_matchPercentage = p_matchPercentage;
        m_matchType = p_matchType;
        m_tmId = p_tmId;
        this.tmName = tmName;
        this.mayHaveSid = mayHaveSid;
    }
    
    public long getTmId() {
        return m_tmId;
    }
    public String getMatchContentSource()
    {
        return m_matchContentSource;
    }


    public void setMatchContentSource(String MatchContentSource)
    {
        m_matchContentSource = MatchContentSource;
    }
    public String getTmName()
    {
    	return this.tmName;
    }
    
    public long getTuvId()
    {
        return m_tuvId;
    }

    public String getMatchContent()
    {
        return m_matchContent;
    }

    public float getMatchPercentage()
    {
        return m_matchPercentage;
    }

    public String getMatchType()
    {
        return m_matchType;
    }
    
    public String getSid()
    {
        return sid;
    }
    
    public void setSid(String s)
    {
        sid = s;
    }

    public TuvBasicInfo getMatchedTuvBasicInfo()
    {
        return matchedTuvBasicInfo;
    }

    public void setMatchedTuvBasicInfo(TuvBasicInfo matchedTuvBasicInfo)
    {
        this.matchedTuvBasicInfo = matchedTuvBasicInfo;
    }

    public String getMatchedTuvJobName()
    {
        return matchedTuvJobName;
    }

    public void setMatchedTuvJobName(String matchedTuvJobName)
    {
        this.matchedTuvJobName = matchedTuvJobName;
    }

}
