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

import java.util.HashMap;

/**
 * MatchState represents the state of a match.
 */

public class MatchState
{
    static public final MatchState IN_PROGRESS_TM_EXACT_MATCH
        = new MatchState(1, "IN_PROGRESS_TM_EXACT_MATCH", 1);
    static public final MatchState UNVERIFIED_EXACT_MATCH
        = new MatchState(2, "UNVERIFIED_EXACT_MATCH", 2);
    static public final MatchState PAGE_TM_EXACT_MATCH
        = new MatchState(3, "PAGE_TM_EXACT_MATCH", 3);
    static public final MatchState SEGMENT_TM_EXACT_MATCH
        = new MatchState(4, "SEGMENT_TM_EXACT_MATCH", 4);
    static public final MatchState MULTIPLE_TRANSLATION
        = new MatchState(5, "MULTIPLE_TRANSLATION", 5);
    static public final MatchState TYPE_DIFFERENT
        = new MatchState(6, "TYPE_DIFFERENT", 6);
    static public final MatchState CASE_DIFFERENT
        = new MatchState(7, "CASE_DIFFERENT", 7);
    static public final MatchState WHITESPACE_DIFFERENT
        = new MatchState(8, "WHITESPACE_DIFFERENT", 7);
    static public final MatchState CODE_DIFFERENT
        = new MatchState(9, "CODE_DIFFERENT", 7);
    static public final MatchState FUZZY_MATCH
        = new MatchState(10, "FUZZY_MATCH", 8);
    static public final MatchState STATISTICS_MATCH
        = new MatchState(11, "STATISTICS_MATCH", 9);
    static public final MatchState NOT_A_MATCH
        = new MatchState(12, "NOT_A_MATCH", 10);
    static public final MatchState MT_EXACT_MATCH
        = new MatchState(13, "MT_EXACT_MATCH", 11);
    static public final MatchState XLIFF_EXACT_MATCH
        = new MatchState(14, "XLIFF_EXACT_MATCH", 12);
    static public final MatchState TDA_MATCH
        = new MatchState(15, "TDA_MATCH", 13);
    static public final MatchState PO_EXACT_MATCH
        = new MatchState(16, "PO_EXACT_MATCH", 14);
    

    // map for getting MatchState object by looking up its name
    // Key: state name
    // Value: MatchState object
    static private final HashMap<String, MatchState> c_nameLookupTable = createNameLookup();

    static private HashMap<String, MatchState> createNameLookup()
    {
        HashMap<String, MatchState> map = new HashMap<String, MatchState>();
        
        map.put(IN_PROGRESS_TM_EXACT_MATCH.getName(),
            IN_PROGRESS_TM_EXACT_MATCH);
        map.put(UNVERIFIED_EXACT_MATCH.getName(),
            UNVERIFIED_EXACT_MATCH);
        map.put(PAGE_TM_EXACT_MATCH.getName(),
            PAGE_TM_EXACT_MATCH);
        map.put(SEGMENT_TM_EXACT_MATCH.getName(),
            SEGMENT_TM_EXACT_MATCH);
        map.put(MULTIPLE_TRANSLATION.getName(),
            MULTIPLE_TRANSLATION);
        map.put(TYPE_DIFFERENT.getName(),
            TYPE_DIFFERENT);
        map.put(CASE_DIFFERENT.getName(),
            CASE_DIFFERENT);
        map.put(WHITESPACE_DIFFERENT.getName(),
            WHITESPACE_DIFFERENT);
        map.put(CODE_DIFFERENT.getName(),
            CODE_DIFFERENT);
        map.put(FUZZY_MATCH.getName(),
            FUZZY_MATCH);
        map.put(STATISTICS_MATCH.getName(),
            STATISTICS_MATCH);
        map.put(NOT_A_MATCH.getName(),
            NOT_A_MATCH);
        map.put(MT_EXACT_MATCH.getName(),
        		MT_EXACT_MATCH);
        map.put(XLIFF_EXACT_MATCH.getName(),
                XLIFF_EXACT_MATCH);
        map.put(TDA_MATCH.getName(),
                TDA_MATCH);
        map.put(PO_EXACT_MATCH.getName(),
                PO_EXACT_MATCH);

        return map;
    }

    
    private int m_id;
    private String m_name;
    private int m_compareKey;
    
    private MatchState(int p_id, String p_name, int p_compareKey)
    {
        m_id = p_id;
        m_name = p_name;
        m_compareKey = p_compareKey;
    }


    public String getName()
    {
        return m_name;
    }
    
    
    public int getCompareKey()
    {
        return m_compareKey;
    }


    public boolean equals(Object p_other)
    {
        boolean ret = false;
        
        if(p_other instanceof MatchState)
        {
            ret = (m_id == ((MatchState)p_other).m_id);
        }
        
        return ret;
    }
    

    public int hashCode()
    {
        return m_id;
    }
    

    public static MatchState getMatchState(String p_stateName)
    {
        return (MatchState) c_nameLookupTable.get(p_stateName);
    }
    
    public static int getCompareKey(String p_stateName)
    {
        return getMatchState(p_stateName).getCompareKey();
    }
}
