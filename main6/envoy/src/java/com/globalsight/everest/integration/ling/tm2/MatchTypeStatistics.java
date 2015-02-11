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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.persistence.HandleSqlSearch;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Container of match type statistics of a leveraged target page.
 */
public class MatchTypeStatistics
{
    // Mapping of Tuv id + sub id and match type.
    // Key: String of Tuv id + sub id, e.g. "10026-0"
    // Value: Match type. Integer object represents the types
    private Map m_matchTypes = new HashMap();

    public static final int NO_MATCH = 1;
    public static final int LOW_FUZZY = 2;
    public static final int MED_FUZZY = 3;
    public static final int MED_HI_FUZZY = 4;
    public static final int HI_FUZZY = 5;
    public static final int CONTEXT_EXACT = 6;
    public static final int SEGMENT_TM_EXACT = 7;
    public static final int SEGMENT_MT_EXACT = 8;
    public static final int SEGMENT_XLIFF_EXACT = 9;

    // The following two attributes are defined as the low and high
    // range of the bucket used for the sub-leverage-match that's
    // considered the word count below the leverage match threshold
    // defined in TM profile.
    private int m_subLevMatchLow = 0;
    private int m_subLevMatchHi = 0;

    //
    // Constructor
    //
    public MatchTypeStatistics(int p_leverageMatchThreshold)
    {
        determineSubLevMatchRange(p_leverageMatchThreshold);
    }

    public void addMatchType(LeverageMatch match)
    {
        String key = makeKey(match.getOriginalSourceTuvId(), match.getSubId());
        MatchState p_matchState = match.getMatchState();
        float p_matchPoint = match.getScoreNum();

        int statisticsType = NO_MATCH;
        if (p_matchPoint == 100)
        {
            if (p_matchState != null && 
                    (p_matchState.equals(MatchState.UNVERIFIED_EXACT_MATCH) || p_matchState.equals(MatchState.PAGE_TM_EXACT_MATCH)))
            {
                statisticsType = CONTEXT_EXACT;
            } 
            else if (p_matchState != null && p_matchState.equals(MatchState.MT_EXACT_MATCH))
            {
                statisticsType = SEGMENT_MT_EXACT;
            }
            else if(p_matchState != null && p_matchState.equals(MatchState.XLIFF_EXACT_MATCH)) {
                statisticsType = SEGMENT_XLIFF_EXACT;
            }
            else
            {
                statisticsType = SEGMENT_TM_EXACT;
            }
        }
        else if (p_matchPoint >= 50 && p_matchPoint <75)
        {
            statisticsType = LOW_FUZZY;
        }
        else if (p_matchPoint >= 75 && p_matchPoint <85)
        {
            statisticsType = MED_FUZZY;
        }
        else if (p_matchPoint >= 85 && p_matchPoint <95)
        {
            statisticsType = MED_HI_FUZZY;
        }
        else if (p_matchPoint >= 95 && p_matchPoint <100)
        {
            statisticsType = HI_FUZZY;
        }

        int lingManagerType = LeverageMatchLingManager.NO_MATCH;
        if (p_matchPoint == 100)
        {
            if (p_matchState != null && p_matchState.equals(MatchState.UNVERIFIED_EXACT_MATCH))
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
            if (p_matchState != null && p_matchState.equals(MatchState.STATISTICS_MATCH))
            {
                lingManagerType = LeverageMatchLingManager.STATISTICS;
            }
            else if (!p_matchState.equals(MatchState.NOT_A_MATCH))
            {
                // All penalized matches are counted as fuzzy.
                lingManagerType = LeverageMatchLingManager.FUZZY;
            }
        }

        boolean isSubLevMatch = false;
        // check to see if it's also a sub-leverage-match (less than
        // leverage match threshold)
        if (m_subLevMatchLow > 0
                && (p_matchPoint >= m_subLevMatchLow && p_matchPoint <= m_subLevMatchHi))
        {
            isSubLevMatch = true;
        }

        Types types = new Types(isSubLevMatch, statisticsType, lingManagerType, p_matchState);

        types.setSid(getSid(match.getMatchedText(), match.getMatchedTuvId()));

        m_matchTypes.put(key, types);
    }

    private String getSid(String matchedString, long matchedTuvId)
    {
        String sid = null;
        List args = new ArrayList();
        args.add(matchedTuvId);
        args.add(matchedString);

        List<String> tables = new ArrayList<String>();
        tables.add("project_tm_tuv_t");
        tables.add("project_tm_tuv_l");
        tables.add("page_tm_tuv_t");
        tables.add("page_tm_tuv_l");

        for (String table : tables)
        {
            String sql = "select sid from " + table
                    + " where id = ? and SEGMENT_STRING = ?";
            try
            {
                List result = DbUtil.search(sql, args, getHandler());
                if (result.size() > 0)
                {
                    sid = (String) result.get(0);
                    break;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return sid;
    }

    private HandleSqlSearch getHandler()
    {
        return new HandleSqlSearch()
        {

            @Override
            public List<?> handleSearch(ResultSet set)
            {
                List result = new ArrayList();
                try
                {
                    if (set.next())
                    {
                        result.add(set.getString(1));
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }

                return result;
            }
        };
    }

    public String getSid(long p_tuvId, String p_subId)
    {
        String key = makeKey(p_tuvId, p_subId);
        Types types = (Types) m_matchTypes.get(key);

        String sid = null;
        if (types != null)
        {
            sid = types.getSid();
        }
        
        return sid;
    }
    
    public int getLingManagerMatchType(long p_tuvId, String p_subId)
    {
        String key = makeKey(p_tuvId, p_subId);
        Types types = (Types) m_matchTypes.get(key);

        int lingManagerMatchType = LeverageMatchLingManager.NO_MATCH;
        if (types != null)
        {
            lingManagerMatchType = types.getLingManagerMatchType();
        }

        return lingManagerMatchType;
    }

    public int getStatisticsMatchType(long p_tuvId, String p_subId)
    {
        String key = makeKey(p_tuvId, p_subId);
        Types types = (Types) m_matchTypes.get(key);

        int statisticsMatchType = NO_MATCH;
        if (types != null)
        {
            statisticsMatchType = types.getStatisticsMatchType();
        }

        return statisticsMatchType;
    }
    
    /**
     * There are two kinds of format to show exact match, judge use which one.
     * It is also used to count in-context match.
     * 
     * @return true if state is LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED or
     *         EXACT_MATCH_LOCALIZED
     */
    public boolean isExactMatchLocalized(long p_tuvId, String p_subId)
    {
        String key = makeKey(p_tuvId, p_subId);
        Types types = (Types) m_matchTypes.get(key);

        if (types != null)
        {
            if (MatchState.XLIFF_EXACT_MATCH.equals(types.getMatchState()))
            {
                TuvImpl tuv = HibernateUtil.get(TuvImpl.class, p_tuvId);
                if (tuv != null)
                {
                    TuImpl tu = (TuImpl) tuv.getTu();
                    if (tu != null)
                    {
                        return tu.isXliffLocked();
                    }
                }
            }
            
            return types.isExactMatchLocalized();
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
    public Types getTypes(long p_tuvId, String p_subId)
    {
        String key = makeKey(p_tuvId, p_subId);
        return (Types) m_matchTypes.get(key);
    }

    /**
     * Determine the range for the sub-leverage-match threshold bucket.
     */
    private void determineSubLevMatchRange(int p_leverageMatchThreshold)
    {
        if (p_leverageMatchThreshold <= 50)
        {
            return;
        }

        // the range would be 50 to levMatchThreshold - 1 (i.e. for
        // levMatchThreshold of 80%, it'll be 50-79%)
        m_subLevMatchLow = 50;
        m_subLevMatchHi = p_leverageMatchThreshold - 1;
    }

    public static String makeKey(long p_tuvId, String p_subId)
    {
        StringBuffer result = new StringBuffer();

        result.append(Long.toString(p_tuvId));
        result.append("-");
        result.append(p_subId);

        return result.toString();
    }
}
