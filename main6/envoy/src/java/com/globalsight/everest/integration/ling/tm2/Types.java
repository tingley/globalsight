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

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm2.leverage.MatchState;

/**
 * Container of match types of a leveraged target page.
 */
public class Types
{
    static private final Logger logger = Logger.getLogger(Types.class);

    // Types values are from LeverageMatch object.
    private LeverageMatch leverageMatch = null;

    private int statisticsType;// Threshold not-related
    private int statisticsTypeByThreshold;// Adjusted by threshold
    private int lingManagerMatchType;
    private MatchState matchState;
    private boolean isMtTranslation = false;
    private String sid;

    // Constructor
    public Types(int p_statisticsType, int p_statisticsTypeByThreshold,
            int p_lingManagerMatchType, MatchState p_matchState,
            boolean p_isMtTranslation)
    {
        statisticsType = p_statisticsType;
        statisticsTypeByThreshold = p_statisticsTypeByThreshold;
        lingManagerMatchType = p_lingManagerMatchType;
        matchState = p_matchState;
        isMtTranslation = p_isMtTranslation;
    }

    public void setStatisticsMatchType(int p_statisticsType)
    {
        this.statisticsType = p_statisticsType;
    }

    public int getStatisticsMatchType()
    {
        return statisticsType;
    }

    public void setStatisticsMatchTypeByThreshold(
            int p_statisticsTypeByThreshold)
    {
        this.statisticsTypeByThreshold = p_statisticsTypeByThreshold;
    }

    public int getStatisticsMatchTypeByThreshold()
    {
        return statisticsTypeByThreshold;
    }

    public void setLingManagerMatchType(int p_lingManagerMatchType)
    {
        this.lingManagerMatchType = p_lingManagerMatchType;
    }

    public int getLingManagerMatchType()
    {
        return lingManagerMatchType;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    public MatchState getMatchState()
    {
        return matchState;
    }

    public void setMatchState(MatchState matchState)
    {
        this.matchState = matchState;
    }

    public void setIsMtTranslation(boolean p_isMtTranslation)
    {
        this.isMtTranslation = p_isMtTranslation;
    }

    public boolean isMtTranslation()
    {
        return this.isMtTranslation;
    }

    /**
     * There are four kinds of format to show exact match, judge use which one.
     * It is also used to count in-context match.
     * 
     * @param p_tuvId
     *            : for XLF format.
     * @return true if state is PAGE_TM_EXACT_MATCH, SEGMENT_TM_EXACT_MATCH,
     *         PO_EXACT_MATCH or XLIFF_EXACT_MATCH(for WS XLF "locked"
     *         segments).
     */
    public boolean isExactMatchLocalized(long p_tuvId, long p_jobId)
    {
        // Judge if it is "XLIFF_EXACT_MATCH" and "isXliffLocked" first.
        if (MatchState.XLIFF_EXACT_MATCH.equals(matchState))
        {
            TuvImpl tuv = null;
            try
            {
                tuv = SegmentTuvUtil.getTuvById(p_tuvId, p_jobId);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
            if (tuv != null)
            {
                TuImpl tu = (TuImpl) tuv.getTu(p_jobId);
                if (tu != null)
                {
                    // If it is WorldServer XLIFF file,and current TU has
                    // 'lock_status="locked' attribute, can count it as ICE.
                    return tu.isXliffLocked()
                            || "no".equalsIgnoreCase(tu.getTranslate());
                }
            }
        }

        return (matchState.equals(MatchState.PAGE_TM_EXACT_MATCH)
                || matchState.equals(MatchState.SEGMENT_TM_EXACT_MATCH) || matchState
                    .equals(MatchState.PO_EXACT_MATCH));
    }

	public LeverageMatch getLeverageMatch() {
		return leverageMatch;
	}

	public void setLeverageMatch(LeverageMatch leverageMatch) {
		this.leverageMatch = leverageMatch;
	}
}
