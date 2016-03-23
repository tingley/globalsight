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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;

/**
 * Container of match type statistics of a leveraged target page.
 */
public class MatchTypeStatistics
{
    // Mapping of Tuv id + sub id and match type.
    // Key: String of Tuv id + sub id, e.g. "10026-0"
    // Value: Match type. Integer object represents the types
    private Map<String, Types> m_matchTypes = new HashMap<String, Types>();

    public static final int NO_MATCH = 1;
    public static final int LOW_FUZZY = 2;
    public static final int MED_FUZZY = 3;
    public static final int MED_HI_FUZZY = 4;
    public static final int HI_FUZZY = 5;
    public static final int CONTEXT_EXACT = 6;
    public static final int SEGMENT_TM_EXACT = 7;
    public static final int SEGMENT_MT_EXACT = 8;
    public static final int SEGMENT_XLIFF_EXACT = 9;
    public static final int SEGMENT_PO_EXACT = 10;
    public static final int IN_PROGRESS_TM_EXACT = 11;

    // word count calculated by threshold
    public static final int THRESHOLD_NO_MATCH = 11;
    public static final int THRESHOLD_HI_FUZZY = 12;
    public static final int THRESHOLD_MED_HI_FUZZY = 13;
    public static final int THRESHOLD_MED_FUZZY = 14;
    public static final int THRESHOLD_LOW_FUZZY = 15;

    private int m_threshold = 0;

    public MatchTypeStatistics(int p_leverageMatchThreshold)
    {
        m_threshold = p_leverageMatchThreshold;
    }

    public void addMatchType(LeverageMatch match)
    {
        String key = makeKey(match.getOriginalSourceTuvId(), match.getSubId());
        MatchState p_matchState = match.getMatchState();
        float p_matchPoint = match.getScoreNum();

        // statisticsType (threshold non-related)
        int statisticsType = NO_MATCH;
        if (p_matchPoint == 100)
        {
            statisticsType = determineStatisticsTypeForExactMatch(p_matchState);
        }
        else if (p_matchPoint < 100 && p_matchPoint >= 50)
        {
            statisticsType = determineStatisticsTypeForFuzzyMatch(p_matchPoint);
        }

        // statisticsTypeByThreshold (threshold related)
        int statisticsTypeByThreshold = THRESHOLD_NO_MATCH;
        if (p_matchPoint == 100)
        {
            statisticsTypeByThreshold = statisticsType;
        }
        else if (p_matchPoint >= m_threshold)
        {
            statisticsTypeByThreshold = determineCostingStatisticsTypeForFuzzyMatch(p_matchPoint);
        }

        // lingManagerType
        int lingManagerMatchType = determineLingManagerType(p_matchPoint,
                p_matchState);

        boolean isMtTranslation = (match.getMtName() != null && match
                .getMtName().endsWith("_MT"));
        Types types = new Types(statisticsType, statisticsTypeByThreshold,
                lingManagerMatchType, p_matchState, isMtTranslation);

        types.setLeverageMatch(match);
        // Set SID
        String sid = match.getSid();
        types.setSid(sid);

        m_matchTypes.put(key, types);
    }

    private int determineStatisticsTypeForExactMatch(MatchState p_matchState)
    {
        int statisticsType = NO_MATCH;
        if (MatchState.UNVERIFIED_EXACT_MATCH.equals(p_matchState)
                || MatchState.PAGE_TM_EXACT_MATCH.equals(p_matchState))
        {
            statisticsType = CONTEXT_EXACT;
        }
        else if (MatchState.MT_EXACT_MATCH.equals(p_matchState))
        {
            statisticsType = SEGMENT_MT_EXACT;
        }
        else if (MatchState.XLIFF_EXACT_MATCH.equals(p_matchState))
        {
            statisticsType = SEGMENT_XLIFF_EXACT;
        }
        else if (MatchState.PO_EXACT_MATCH.equals(p_matchState))
        {
            statisticsType = SEGMENT_PO_EXACT;
        }
        else if (MatchState.IN_PROGRESS_TM_EXACT_MATCH.equals(p_matchState))
        {
            statisticsType = IN_PROGRESS_TM_EXACT;
        }
        else
        {
            statisticsType = SEGMENT_TM_EXACT;
        }

        return statisticsType;
    }

    private int determineStatisticsTypeForFuzzyMatch(float p_matchPoint)
    {
        int statisticsType = NO_MATCH;
        if (p_matchPoint >= 50 && p_matchPoint < 75)
        {
            statisticsType = LOW_FUZZY;
        }
        else if (p_matchPoint >= 75 && p_matchPoint < 85)
        {
            statisticsType = MED_FUZZY;
        }
        else if (p_matchPoint >= 85 && p_matchPoint < 95)
        {
            statisticsType = MED_HI_FUZZY;
        }
        else if (p_matchPoint >= 95 && p_matchPoint < 100)
        {
            statisticsType = HI_FUZZY;
        }

        return statisticsType;
    }

    private int determineCostingStatisticsTypeForFuzzyMatch(float p_matchPoint)
    {
        int statisticsType = THRESHOLD_NO_MATCH;
        if (m_threshold >= 95)
        {
            statisticsType = THRESHOLD_HI_FUZZY;
        }
        else if (m_threshold >= 85)
        {
            if (p_matchPoint >= 95)
            {
                statisticsType = THRESHOLD_HI_FUZZY;
            }
            else if (p_matchPoint < 95)
            {
                statisticsType = THRESHOLD_MED_HI_FUZZY;
            }
        }
        else if (m_threshold >= 75)
        {
            if (p_matchPoint >= 95)
            {
                statisticsType = THRESHOLD_HI_FUZZY;
            }
            else if (p_matchPoint >= 85 && p_matchPoint < 95)
            {
                statisticsType = THRESHOLD_MED_HI_FUZZY;
            }
            else if (p_matchPoint < 85)
            {
                statisticsType = THRESHOLD_MED_FUZZY;
            }
        }
        else
        {
            if (p_matchPoint >= 95)
            {
                statisticsType = THRESHOLD_HI_FUZZY;
            }
            else if (p_matchPoint >= 85 && p_matchPoint < 95)
            {
                statisticsType = THRESHOLD_MED_HI_FUZZY;
            }
            else if (p_matchPoint >= 75 && p_matchPoint < 85)
            {
                statisticsType = THRESHOLD_MED_FUZZY;
            }
            else if (p_matchPoint < 75)
            {
                statisticsType = THRESHOLD_LOW_FUZZY;
            }
        }

        return statisticsType;
    }

    /**
     * Determine the ling manager type.
     */
    private int determineLingManagerType(float p_matchPoint,
            MatchState p_matchState)
    {
        int lingManagerType = LeverageMatchLingManager.NO_MATCH;
        if (p_matchPoint == 100)
        {
            if (p_matchState != null
                    && p_matchState.equals(MatchState.UNVERIFIED_EXACT_MATCH))
            {
                lingManagerType = LeverageMatchLingManager.UNVERIFIED;
            }
            else
            {
                lingManagerType = LeverageMatchLingManager.EXACT;
            }
        }
        else
        {
            if (p_matchState != null
                    && p_matchState.equals(MatchState.STATISTICS_MATCH))
            {
                lingManagerType = LeverageMatchLingManager.STATISTICS;
            }
            else if (!p_matchState.equals(MatchState.NOT_A_MATCH))
            {
                // All penalized matches are counted as fuzzy.
                lingManagerType = LeverageMatchLingManager.FUZZY;
            }
        }

        return lingManagerType;
    }

    public String getSid(long p_srcTuvId, String p_subId)
    {
        String key = makeKey(p_srcTuvId, p_subId);
        Types types = (Types) m_matchTypes.get(key);

        String sid = null;
        if (types != null)
        {
            sid = types.getSid();
        }

        return sid;
    }

    public int getLingManagerMatchType(long p_srcTuvId, String p_subId)
    {
        String key = makeKey(p_srcTuvId, p_subId);
        Types types = (Types) m_matchTypes.get(key);

        int lingManagerMatchType = LeverageMatchLingManager.NO_MATCH;
        if (types != null)
        {
            lingManagerMatchType = types.getLingManagerMatchType();
        }

        return lingManagerMatchType;
    }

    public int getStatisticsMatchType(long p_srcTuvId, String p_subId)
    {
        String key = makeKey(p_srcTuvId, p_subId);
        Types types = (Types) m_matchTypes.get(key);

        int statisticsMatchType = NO_MATCH;
        if (types != null)
        {
            statisticsMatchType = types.getStatisticsMatchType();
        }

        return statisticsMatchType;
    }
    
    /**
     * Get Types Map, including all sub segments.
     * @return Map<"tuvId_subId", Types>
     */
    public Map<String, Types> getLingManagerMatchType(Tuv p_srcTuv)
    {
        Map<String, Types> result = new HashMap<String, Types>();
        long srcTuvId = p_srcTuv.getId();
        String subId = "0";        
        result.put(makeKey(srcTuvId, subId), getTypes(srcTuvId, subId));
        
        List<GxmlElement> subs = p_srcTuv.getSubflowsAsGxmlElements();
        for (int i = 0; subs != null && i < subs.size(); i++)
        {
            GxmlElement sub = subs.get(i);
            subId = sub.getAttribute(GxmlNames.SUB_ID);
            result.put(makeKey(srcTuvId, subId), getTypes(srcTuvId, subId));
        }

        return result;
    }

    /**
     * Judge whether the leverage match comes from Machine Translation.
     * Don't need check the subId.
     */
    public boolean isMachineTranslation(Tuv p_tuv)
    {
        Types type = getTypes(p_tuv.getId(), "0");
        if (type != null && type.isMtTranslation())
            return true;
        else
            return false;
    }
    
    /**
     * There are four kinds of format to show exact match, judge use which one.
     * It is also used to count in-context match.
     * 
     * @return true if state is PAGE_TM_EXACT_MATCH, SEGMENT_TM_EXACT_MATCH,
     *         PO_EXACT_MATCH or XLIFF_EXACT_MATCH(for WS XLF "locked"
     *         segments).
     */
    public boolean isExactMatchLocalized(long p_srcTuvId, String p_subId,
            long p_jobId)
    {
        String key = makeKey(p_srcTuvId, p_subId);
        Types types = (Types) m_matchTypes.get(key);

        if (types != null)
        {
            return types.isExactMatchLocalized(p_srcTuvId, p_jobId);
        }

        return false;
    }

    public void merge(MatchTypeStatistics p_other)
    {
        m_matchTypes.putAll(p_other.m_matchTypes);
    }

    /**
     * Get the types for the given tuv id and sub id.
     */
    public Types getTypes(long p_srcTuvId, String p_subId)
    {
        String key = makeKey(p_srcTuvId, p_subId);
        return (Types) m_matchTypes.get(key);
    }

    public static String makeKey(long p_srcTuvId, String p_subId)
    {
        StringBuffer result = new StringBuffer();

        result.append(Long.toString(p_srcTuvId));
        result.append("-");
        result.append(p_subId);

        return result.toString();
    }

    public void setMatchTypes(Map<String, Types> types)
    {
        m_matchTypes = types;
    }

}
