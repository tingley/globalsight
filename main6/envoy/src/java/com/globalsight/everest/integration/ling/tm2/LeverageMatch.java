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

package com.globalsight.everest.integration.ling.tm2;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.leverage.SidComparable;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

@SuppressWarnings("rawtypes")
public class LeverageMatch extends PersistentObject implements Comparable,
        SidComparable
{
    private static final long serialVersionUID = 1620339898560263399L;

    private static final Logger CATEGORY = Logger
            .getLogger(LeverageMatch.class);

    private static final String ROOT_TAGS_REGEX = "<segment[^>]*>|</segment[:space:]*>|<localizable[^>]*>|</localizable[:space:]*>";
    private static final REProgram c_removeRootTags = precompileRegexp(ROOT_TAGS_REGEX);

    private static REProgram precompileRegexp(String p_regex)
    {
        REProgram reProgram = null;

        try
        {
            RECompiler compiler = new RECompiler();
            reProgram = compiler.compile(p_regex);
        }
        catch (RESyntaxException ex)
        {
            // SNH (Should Never Happen)
            CATEGORY.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        }

        return reProgram;
    }

    //
    // Private Members
    //

    private Long sourcePageId;
    private long m_originalSourceTuvId;
    private long m_matchedTuvId; // For DEBUG only
    private long matchedTableType;
    private String m_subid;
    private String matchedText;
    private String matchedClob;
    private GlobalSightLocale m_targetLocale;
    private String m_matchType;
    private short m_orderNum = 0;
    private float m_scoreNum = 0.0f;
    private int m_projectTmIndex = -1;
    private long m_tmId = 0;
    private long m_tmProfileId = 0;
    // SID of source TUV
    private String orgSid = null;
    // if the match data is from MT,then use this to save MT name
    private String mtName = null;
    private String matchedOriginalSource;
    private long jobDataTuId = 0;
    // SID of TM match
    private String sid;

    private String creationUser;
    private Date creationDate;
    private String modifyUser;
    private Date modifyDate;

    private Date lastUsageDate = null;
    private long jobId = -1;
    private String jobName = null;
    private long previousHash = -1;
    private long nextHash = -1;

    // Helper object - still necessary?
    // private SegmentTagsAligner m_tagAligner;

    //
    // Constructor
    //

    public LeverageMatch()
    {
        super();

        // m_tagAligner = new SegmentTagsAligner();
    }

    public LeverageMatch(LeverageMatch p_other)
    {
        sourcePageId = p_other.sourcePageId;
        m_originalSourceTuvId = p_other.m_originalSourceTuvId;
        m_matchedTuvId = p_other.m_matchedTuvId;
        matchedTableType = p_other.matchedTableType;
        m_subid = p_other.m_subid;
        matchedText = p_other.matchedText;
        matchedClob = p_other.matchedClob;
        m_targetLocale = p_other.m_targetLocale;
        m_matchType = p_other.m_matchType;
        m_orderNum = p_other.m_orderNum;
        m_scoreNum = p_other.m_scoreNum;
        m_projectTmIndex = p_other.m_projectTmIndex;
        m_tmId = p_other.m_tmId;
        m_tmProfileId = p_other.m_tmProfileId;
        sid = p_other.sid;
        orgSid = p_other.orgSid;
        mtName = p_other.mtName;
        matchedOriginalSource = p_other.matchedOriginalSource;
        jobDataTuId = p_other.jobDataTuId;
        creationUser = p_other.creationUser;
        creationDate = p_other.creationDate;
        modifyUser = p_other.modifyUser;
        modifyDate = p_other.modifyDate;
        lastUsageDate = p_other.lastUsageDate;
        jobId = p_other.jobId;
        jobName = p_other.jobName;
        previousHash = p_other.previousHash;
        nextHash = p_other.nextHash;
    }

    public long getTmProfileId()
    {
        return this.m_tmProfileId;
    }

    public void setTmProfileId(long tmProfileId)
    {
        m_tmProfileId = tmProfileId;
    }

    public long getTmId()
    {
        return this.m_tmId;
    }

    public void setTmId(long tmId)
    {
        m_tmId = tmId;
    }

    public int getProjectTmIndex()
    {
        return m_projectTmIndex;
    }

    public void setProjectTmIndex(int projectTmIndex)
    {
        this.m_projectTmIndex = projectTmIndex;
    }

    public Long getSourcePageId()
    {
        return sourcePageId;
    }

    public void setSourcePageId(Long sourcePageId)
    {
        this.sourcePageId = sourcePageId;
    }

    public long getOriginalSourceTuvId()
    {
        return m_originalSourceTuvId;
    }

    public void setOriginalSourceTuvId(long p_originalSourceTuvId)
    {
        m_originalSourceTuvId = p_originalSourceTuvId;
    }

    public long getMatchedTuvId()
    {
        return m_matchedTuvId;
    }

    public void setMatchedTuvId(long p_matchedTuvId)
    {
        m_matchedTuvId = p_matchedTuvId;
    }

    public long getMatchedTableType()
    {
        return matchedTableType;
    }

    public void setMatchedTableType(long p_matchedTableType)
    {
        matchedTableType = p_matchedTableType;
    }

    public String getMatchedText()
    {
        return matchedText == null ? matchedClob : matchedText;
    }

    public void setMatchedText(String matchedText)
    {
        if (matchedText != null)
        {
            if (EditUtil.getUTF8Len(matchedText) > CLOB_THRESHOLD)
            {
                this.matchedClob = matchedText;
            }
            else
            {
                this.matchedText = matchedText;
            }
        }
    }

    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    public void setTargetLocale(GlobalSightLocale targetLocale)
    {
        m_targetLocale = targetLocale;
    }

    public long getTargetLocaleId()
    {
        return m_targetLocale == null ? 0 : m_targetLocale.getId();
    }

    public void setTargetLocaleId(Long targetLocaleId)
    {
        try
        {
            m_targetLocale = ServerProxy.getLocaleManager().getLocaleById(
                    targetLocaleId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to set target locale for targetLocaleId "
                    + targetLocaleId);
        }
    }

    public String getMatchType()
    {
        return m_matchType;
    }

    public void setMatchType(String p_matchType)
    {
        m_matchType = p_matchType;
    }

    public short getOrderNum()
    {
        return m_orderNum;
    }

    public void setOrderNum(short p_orderNum)
    {
        m_orderNum = p_orderNum;
    }

    public float getScoreNum()
    {
        return m_scoreNum;
    }

    public void setScoreNum(float p_scoreNum)
    {
        m_scoreNum = p_scoreNum;
    }

    public String getSubId()
    {
        return m_subid;
    }

    public void setSubId(String subId)
    {
        m_subid = subId;
    }

    public String getCreationUser()
    {
        return creationUser;
    }

    public void setCreationUser(String creationUser)
    {
        this.creationUser = creationUser;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public Date getLastUsageDate() {
		return lastUsageDate;
	}

	public void setLastUsageDate(Date lastUsageDate) {
		this.lastUsageDate = lastUsageDate;
	}

	public long getJobId() {
		return jobId;
	}

	public void setJobId(long jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public long getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(long previousHash) {
		this.previousHash = previousHash;
	}

	public long getNextHash() {
		return nextHash;
	}

	public void setNextHash(long nextHash) {
		this.nextHash = nextHash;
	}

	public boolean isExactMatch()
    {
        return m_scoreNum == 100
                && !m_matchType.equals(MatchState.UNVERIFIED_EXACT_MATCH
                        .getName());
    }

    public boolean isUnverifiedExactMatch()
    {
        return m_matchType.equals(MatchState.UNVERIFIED_EXACT_MATCH.getName());
    }

    public boolean isFuzzyMatch()
    {
        return m_scoreNum < 100
                && !m_matchType.equals(MatchState.STATISTICS_MATCH.getName());
    }

    public boolean isStatisticsMatch()
    {
        return m_matchType.equals(MatchState.STATISTICS_MATCH.getName());
    }

    public MatchState getMatchState()
    {
        return MatchState.getMatchState(m_matchType);
    }

    public String getLeveragedString(String str)
    {
        RE matcher = new RE(c_removeRootTags, RE.MATCH_NORMAL);
        return matcher.subst(str, "");
    }

    public String getLeveragedTargetString() throws LingManagerException
    {
        RE matcher = new RE(c_removeRootTags, RE.MATCH_NORMAL);
        // String match;

        // bjb: quick fix, new Tm code now removes unmatched tags in fuzzy
        // matches,so all this method need to do is remove the outer root tags.
        // TODO: use levergeMatch.getMatchedText() with GxmlUtil.stripRootTag()
        // and
        // remove this method if the remainder of this method equates to
        // doing the same thing.

        // remove <segment> and <localizable> tags.
        return matcher.subst(getMatchedText(), "");

        // bjb Previous code:
        // if (isExactMatch())
        // {
        // match = p_leveragedTargetGxml;
        // // remove <segment> and <localizable> tags.
        // return matcher.subst(match, "");
        // }
        //
        // fuzzy match:
        // return only the tags that match with the source and
        // are not duplicates (duplicate tags are ambiguous
        // and cannot be trusted).
        // match = m_tagAligner.removeUnmatchedTargetTags(p_originalSourceGxml,
        // p_leveragedTargetGxml);
        //
        // remove <segment> and <localizable> tags.
        // return matcher.subst(match, "");
    }

    // Overridden method from Comparable
    public int compareTo(Object o)
    {
        LeverageMatch other = null;

        if (o instanceof LeverageMatch)
        {
            other = (LeverageMatch) o;
        }
        else
        {
            // If comparing to the other type, this class comes first.
            return -1;
        }

        int cmp = m_orderNum - other.m_orderNum;
        if (cmp == 0)
        {
            // If the order num happens to be the same, it is sorted
            // by predefined matchType order.

            cmp = MatchState.getCompareKey(m_matchType)
                    - MatchState.getCompareKey(other.m_matchType);
        }

        return cmp;
    }

    /**
     * No numeric primary key in LEVERAGE_MATCH table. If the method is called a
     * RuntimeException is thrown.
     */
    public long getId()
    {
        return getTemporarilyUnavailableId();
    }

    public String getMatchedClob()
    {
        return matchedClob == null ? matchedText : matchedClob;
    }

    public void setMatchedClob(String matchedClob)
    {
        if (matchedClob != null)
        {
            if (EditUtil.getUTF8Len(matchedClob) > CLOB_THRESHOLD)
            {
                this.matchedClob = matchedClob;
            }
            else
            {
                this.matchedText = matchedClob;
            }
        }
    }

    public String getMatchedSid()
    {
        return sid;
    }

    public String getOrgSid(long p_jobId)
    {
        if (orgSid == null && m_originalSourceTuvId > 0 && p_jobId > 0)
        {
            TuvImpl tuv = null;
            try
            {
                tuv = SegmentTuvUtil.getTuvById(m_originalSourceTuvId, p_jobId);
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
            if (tuv != null)
            {
                orgSid = tuv.getSid();
            }
        }

        return orgSid;
    }

    public Date getModifyDate()
    {
        return modifyDate;
    }

    public void setModifyDate(Date d)
    {
        modifyDate = d;
    }

    public String getModifyUser()
    {
        return this.modifyUser;
    }

    public void setModifyUser(String modifyUser)
    {
        this.modifyUser = modifyUser;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String s)
    {
        sid = s;
    }

    public void setMtName(String p_mtName)
    {
        this.mtName = p_mtName;
    }

    public String getMtName()
    {
        return this.mtName;
    }

    public void setMatchedOriginalSource(String p_sourceText)
    {
        this.matchedOriginalSource = p_sourceText;
    }

    public String getMatchedOriginalSource()
    {
        return this.matchedOriginalSource;
    }

    public void setJobDataTuId(long p_jobDataTuId)
    {
        this.jobDataTuId = p_jobDataTuId;
    }

    public long getJobDataTuId()
    {
        return this.jobDataTuId;
    }

    /*
     * Order all the leverage match result by the score_num.
     */
    public static void orderMatchResult(List list)
    {
        for (int i = 0; i < list.size() - 1; i++)
        {
            LeverageMatch lm1 = (LeverageMatch) list.get(i);

            for (int j = i + 1; j < list.size(); j++)
            {
                LeverageMatch lm2 = (LeverageMatch) list.get(j);

                if (lm1.getScoreNum() < lm2.getScoreNum())
                {
                    list.set(i, lm2);
                    list.set(j, lm1);
                    lm1 = lm2;
                }
            }
        }
    }

    public boolean equals(Object p_obj)
    {
        if (p_obj instanceof LeverageMatch)
        {
            LeverageMatch lm = (LeverageMatch) p_obj;
            if (this.m_originalSourceTuvId == lm.m_originalSourceTuvId
                    && this.m_subid.equals(lm.m_subid)
                    && this.m_targetLocale.equals(lm.m_targetLocale)
                    && this.m_orderNum == lm.m_orderNum)
            {
                return true;
            }
        }

        return false;
    }

    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(this.m_originalSourceTuvId);
        builder.append(this.m_subid);
        builder.append(this.m_targetLocale);
        builder.append(this.m_orderNum);
        return builder.toHashCode();
    }
}
