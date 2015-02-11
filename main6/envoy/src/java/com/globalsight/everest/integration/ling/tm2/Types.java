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

import com.globalsight.ling.tm2.leverage.MatchState;

/**
 * Container of match types of a leveraged target page.
 */
public class Types
{
    private boolean m_isSubLevMatch = false;
    private int m_statisticsType;
    private int m_lingManagerType;
    private String sid;
    private MatchState matchState;

    // Constructor
    public Types(boolean p_isSubLevMatch, int p_statisticsType,
            int p_lingManagerType, MatchState p_matchState)
    {
        m_isSubLevMatch = p_isSubLevMatch;
        m_statisticsType = p_statisticsType;
        m_lingManagerType = p_lingManagerType;
        matchState = p_matchState;
    }

    public int getStatisticsMatchType()
    {
        return m_statisticsType;
    }

    public int getLingManagerMatchType()
    {
        return m_lingManagerType;
    }

    public boolean isSubLevMatch()
    {
        return m_isSubLevMatch;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    /**
     * There are two kinds of format to show exact match, judge use which one.
     * It is also used to count in-context match.
     * 
     * @return true if state is LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED or
     *         EXACT_MATCH_LOCALIZED
     */
    public boolean isExactMatchLocalized()
    {
        return (matchState.equals(MatchState.PAGE_TM_EXACT_MATCH) || matchState
                .equals(MatchState.SEGMENT_TM_EXACT_MATCH));
    }

    public MatchState getMatchState()
    {
        return matchState;
    }

    public void setMatchState(MatchState matchState)
    {
        this.matchState = matchState;
    }
}
