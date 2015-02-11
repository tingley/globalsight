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
package com.globalsight.ling.tm;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.globalsight.util.GlobalSightLocale;

/**
 * This class is used as return value from
 * com.globalsight.ling.tm.Leverager#leverage and leverageForReimport.
 * It holds all exact matches found while leveraging. This can be used
 * to create target pages.
 */
public class ExactMatchedSegments
    implements Serializable
{
    // key: GlobalSightLocale
    // value: Map of -- key: tuv id
    //                  value: LeverageSegment
    private Map m_localeMap = new HashMap();


    // Returns true if there are matches for the specified locale.
    public boolean hasLocale(GlobalSightLocale p_locale)
    {
        return m_localeMap.containsKey(p_locale);
    }

    // Returns LeverageSegment for the specified locale and TUV id. If
    // the match couldn't be found, null will be returned.
    public LeverageSegment getLeveragedSegment(
        GlobalSightLocale p_locale, Long p_tuvId)
    {
        LeverageSegment lev = null;
        
        Map tuvIdMap = (Map)m_localeMap.get(p_locale);
        if(tuvIdMap != null)
        {
            lev = (LeverageSegment)tuvIdMap.get(p_tuvId);
        }
        return lev;
    }
    

    public void putLeveragedSegment(GlobalSightLocale p_locale,
        long p_tuvId, String p_segment, String p_matchType, Date modifyDate, int tmIndex, String sid, long matchedTuvId)
    {
        Map tuvIdMap = (Map)m_localeMap.get(p_locale);
        if(tuvIdMap == null)
        {
            tuvIdMap = new HashMap();
            m_localeMap.put(p_locale, tuvIdMap);
        }
        
        tuvIdMap.put(new Long(p_tuvId),
            new LeverageSegment(p_segment, p_matchType, modifyDate, tmIndex, sid, matchedTuvId));
    }


    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        Iterator localeIt = m_localeMap.keySet().iterator();
        while(localeIt.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale)localeIt.next();
            sb.append("[" + locale + "]\n");

            Map tuvIdMap = (Map)m_localeMap.get(locale);
            Iterator tuvIdIt = tuvIdMap.keySet().iterator();
            while(tuvIdIt.hasNext())
            {
                Long tuvId = (Long)tuvIdIt.next();
                LeverageSegment ls = (LeverageSegment)tuvIdMap.get(tuvId);
                sb.append("  [" + tuvId + "]");
//                  sb.append(", [" + ls.getSegment() + "]");
//                  sb.append(", [" + ls.getMatchType() + "]");
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }

}
