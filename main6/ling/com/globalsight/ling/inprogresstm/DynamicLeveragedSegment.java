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
package com.globalsight.ling.inprogresstm;

import java.io.Serializable;
import java.util.Date;

import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.leverage.SidComparable;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

/**
 * DynamicLeveragedSegment represents a leveraged segment by the dynamic
 * leveraging.
 */
public class DynamicLeveragedSegment implements Serializable, SidComparable
{
    private static final long serialVersionUID = -2695320949652700166L;

    // match category definition
    public static final int FROM_GOLD_TM = 1;
    public static final int FROM_IN_PROGRESS_TM_SAME_JOB = 2;
    public static final int FROM_IN_PROGRESS_TM_OTHER_JOB = 3;

    // matched source and target text in GXML
    private String m_matchedSourceText;
    private String m_matchedTargetText;

    // matched source and target segment locale
    private GlobalSightLocale m_sourceLocale;
    private GlobalSightLocale m_targetLocale;

    // match type:
    private MatchState m_matchType;

    private Date modifyDate;
    // match score: 1 - 100
    private float m_score;

    private int m_tmIndex;
    // match category defined above
    private int m_matchCategory;

    // matched segment TUV id (for mostly debug purpose and for corpus
    // TM retrieval when the match is from Gold TM)
    private long m_matchedTuvId;
    private int m_orderNum;
    private String mtName = null;

    private long m_tmId;
    private String orgSid = null;
    private String sid = null;

    // These are used for saving the match TUV information
    private String matchedTuvJobName = null;
    private TuvBasicInfo matchedTuvBasicInfo = null;

    /**
     * Constructor
     * 
     * @param p_matchedSourceText
     *            matched source text
     * @param p_matchedTargetText
     *            matched target text
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocale
     *            target locale
     * @param p_matchType
     *            match type
     * @param p_score
     *            match score
     * @param p_matchCategory
     *            match category
     * @param p_tmId
     *            id of TM that contained the matched TUV
     * @param p_matchedTuvId
     *            matched TUV id
     */
    public DynamicLeveragedSegment(String p_matchedSourceText,
            String p_matchedTargetText, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, MatchState p_matchType,
            float p_score, int p_matchCategory, long p_tmId, long p_matchedTuvId)
    {
        m_matchedSourceText = p_matchedSourceText;
        m_matchedTargetText = p_matchedTargetText;
        m_sourceLocale = p_sourceLocale;
        m_targetLocale = p_targetLocale;
        m_matchType = p_matchType;
        m_score = p_score;
        m_matchCategory = p_matchCategory;
        m_tmId = p_tmId;
        m_matchedTuvId = p_matchedTuvId;
        // m_tmIndex = tmIndex;
    }

    public String getMtName()
    {
        return mtName;
    }

    public void setMtName(String mtName)
    {
        this.mtName = mtName;
    }

    public String getMatchedSourceText()
    {
        return m_matchedSourceText;
    }

    public String getMatchedTargetText()
    {
        return m_matchedTargetText;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    public MatchState getMatchType()
    {
        return m_matchType;
    }

    public float getScore()
    {
        return m_score;
    }

    public int getMatchCategory()
    {
        return m_matchCategory;
    }

    public long getTmId()
    {
        return m_tmId;
    }

    public long getMatchedTuvId()
    {
        return m_matchedTuvId;
    }

    public void setModifyDate(Date d)
    {
        modifyDate = d;
    }
    
    public Date getModifyDate()
    {
        return modifyDate;
    }

    public int getTmIndex()
    {
        return m_tmIndex;
    }

    public void setTmIndex(int tmIndex)
    {
        m_tmIndex = tmIndex;
    }

    public String toDebugString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("Score: " + m_score + "\n");
        sb.append("Matched source: " + m_matchedSourceText + "\n");
        sb.append("Matched target: " + m_matchedTargetText + "\n");
        sb.append("Source locale: " + m_sourceLocale.toString() + "\n");
        sb.append("Target locale: " + m_targetLocale.toString() + "\n");
        sb.append("Match type: " + m_matchType.getName() + "\n");
        sb.append("TM Index: " + m_tmIndex);
        String matchCategory = null;
        if (m_matchCategory == FROM_GOLD_TM)
        {
            matchCategory = "FROM_GOLD_TM";
        }
        else if (m_matchCategory == FROM_IN_PROGRESS_TM_SAME_JOB)
        {
            matchCategory = "FROM_IN_PROGRESS_TM_SAME_JOB";
        }
        else
        {
            matchCategory = "FROM_IN_PROGRESS_TM_OTHER_JOB";
        }

        sb.append("Match category: " + matchCategory + "\n");
        sb.append("Matched TUV id: " + m_matchedTuvId + "\n");

        return sb.toString();
    }

    public boolean equals(Object obj)
    {
        boolean ret = false;

        if (obj instanceof DynamicLeveragedSegment)
        {
            DynamicLeveragedSegment that = (DynamicLeveragedSegment) obj;
            String thisScore = StringUtil.formatPercent(this.m_score, 2);
            String thatScore = StringUtil.formatPercent(that.m_score, 2);
            ret = thisScore.equalsIgnoreCase(thatScore)
                    && this.m_matchedTargetText
                            .equals(that.m_matchedTargetText)
                    && (this.m_matchedTuvId == that.m_matchedTuvId);
        }

        return ret;
    }

    public int hashCode()
    {
        return (int) (m_score + m_matchedTargetText.hashCode() + m_tmIndex);
    }

    public String getSid()
    {
        return sid;
    }
    
    public void setSid(String s)
    {
        sid = s;
    }

    @Override
    public String getOrgSid(long p_jobId)
    {
        return orgSid;
    }

    public void setOrgSid(String orgSid)
    {
        this.orgSid = orgSid;
    }

    public int getOrderNum()
    {
        return this.m_orderNum;
    }

    public void setOrderNum(int p_orderNum)
    {
        this.m_orderNum = p_orderNum;
    }

    public String getMatchedTuvJobName()
    {
        return matchedTuvJobName;
    }

    public void setMatchedTuvJobName(String matchedTuvJobName)
    {
        this.matchedTuvJobName = matchedTuvJobName;
    }

    public TuvBasicInfo getMatchedTuvBasicInfo()
    {
        return matchedTuvBasicInfo;
    }

    public void setMatchedTuvBasicInfo(TuvBasicInfo matchedTuvBasicInfo)
    {
        this.matchedTuvBasicInfo = matchedTuvBasicInfo;
    }
}
