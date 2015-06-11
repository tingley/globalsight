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

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.GlobalSightLocale;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

public class LeverageMatch extends PersistentObject implements Comparable,
        LeverageMatchType
{
    private static final long serialVersionUID = 5955207637581404579L;

    // Log facility
    private static final Category CATEGORY = Logger
            .getLogger(LeverageMatch.class.getName());

    public static final String ORIGINAL_SOURCE_TUV = "m_originalSourceTuv";
    public static final String TARGET_TUV = "m_leveragedTargetTuv";
    public static final String MATCH_TYPE = "m_matchType";
    public static final String ORDER_NUM = "m_orderNum";

    private static final String ROOT_TAGS_REGEX = "<segment[^>]*>|</segment[:space:]*>|<localizable[^>]*>|</localizable[:space:]*>";
    private static final REProgram c_removeRootTags = precompileRegexp(ROOT_TAGS_REGEX);

    private long m_originalSourceTuvId;
    private long m_matchedSourceTuvId;
    private long m_leveragedTargetTuvId;
    private GlobalSightLocale m_globalSightLocale = null;

    private String m_matchType = null;
    private short m_orderNum = 0;
    private short m_scoreNum = 0;
    private SegmentTagsAligner m_tagAligner;

    private static REProgram precompileRegexp(String p_regex)
    {
        REProgram reProgram = null;

        try
        {
            RECompiler compiler = new RECompiler();
            reProgram = compiler.compile(p_regex);
        }
        catch (RESyntaxException e)
        {
            // SNH (Should Never Happen)
            CATEGORY.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }

        return reProgram;
    }

    public LeverageMatch()
    {
        super();

        m_tagAligner = new SegmentTagsAligner();
    }

    public short getOrderNum()
    {
        return m_orderNum;
    }

    public void setOrderNum(short p_orderNum)
    {
        m_orderNum = p_orderNum;
    }

    public int getMatchType()
    {
        return matchTypeString2matchTypeId(m_matchType);
    }

    public void setMatchType(int p_matchType)
    {
        m_matchType = matchTypeId2matchTypeString(p_matchType);
    }

    public long getLeveragedTargetTuvId()
    {
        return m_leveragedTargetTuvId;
    }

    public void setLeveragedTargetTuv(long p_leveragedTargetTuvId)
    {
        m_leveragedTargetTuvId = p_leveragedTargetTuvId;
    }

    public long getOriginalSourceTuvId()
    {
        return m_originalSourceTuvId;
    }

    public void setOriginalSourceTuv(long p_originalSourceTuvId)
    {
        m_originalSourceTuvId = p_originalSourceTuvId;
    }

    public long getMatchedSourceTuvId()
    {
        return m_matchedSourceTuvId;
    }

    public void setMatchedSourceTuvId(long p_matchedSourceTuvId)
    {
        m_matchedSourceTuvId = p_matchedSourceTuvId;
    }

    public boolean isExactMatch(boolean includeDemoted)
    {
        boolean isExact = false;
        int type = matchTypeString2matchTypeId(m_matchType);

        switch (type)
        {
            case GUARANTEED_EXACT_MATCH:
            case LEVERAGE_GROUP_EXACT_MATCH:
            case EXACT_MATCH_SAME_TM:
            case EXACT_MATCH:
                isExact = true;
                break;

            default:
                isExact = false;
                break;
        }

        if (includeDemoted && type == DEMOTED_EXACT_MATCH)
        {
            isExact = true;
        }

        return isExact;
    }

    public void demoteToFuzzy()
    {
        if (!isDemoted()) // only allowed to demote once
        {
            m_scoreNum -= DEMOTED_EXACT_MATCH_PENATLY;
            m_matchType = DEMOTED_EXACT_MATCH_NAME;
        }
    }

    public void setScoreNum(short p_scoreNum)
    {
        m_scoreNum = p_scoreNum;
    }

    public short getScoreNum()
    {
        return m_scoreNum;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return m_globalSightLocale;
    }

    public long getTargetLocaleId()
    {
        return getTargetLocale().getId();
    }

    public String getLeveragedTargetString(String p_originalSourceGxml,
            String p_leveragedTargetGxml) throws LingManagerException
    {
        RE matcher = new RE(c_removeRootTags, RE.MATCH_NORMAL);
        String match;

        if (isExactMatch(true))
        {
            match = p_leveragedTargetGxml;
            // remove <segment> and <localizable> tags.
            return matcher.subst(match, "");
        }

        // fuzzy match:
        // return only the tags that match with the source and
        // are not duplicates (duplicate tags are ambiguous
        // and cannot be trusted).
        match = m_tagAligner.removeUnmatchedTargetTags(p_originalSourceGxml,
                p_leveragedTargetGxml);

        // remove <segment> and <localizable> tags.
        return matcher.subst(match, "");
    }

    public static String getMatchTypeName(int p_matchType)
    {
        return matchTypeId2matchTypeString(p_matchType);
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
            // in the following order by matchType.

            // 1. LEVERAGE_GROUP_EXACT_MATCH:
            // 2. EXACT_MATCH_SAME_TM:
            // 3. EXACT_MATCH:
            // Don't care the other type.
            cmp = getCompareKey(m_matchType) - getCompareKey(other.m_matchType);
        }

        return cmp;
    }

    private int getCompareKey(String p_matchType)
    {
        int key;

        switch (matchTypeString2matchTypeId(p_matchType))
        {
            case LEVERAGE_GROUP_EXACT_MATCH:
                key = 1;
                break;
            case EXACT_MATCH_SAME_TM:
                key = 2;
                break;
            case EXACT_MATCH:
                key = 3;
                break;
            default:
                key = 10;
        }

        return key;
    }

    public static String matchTypeId2matchTypeString(int p_matchTypeId)
    {
        String name = null;

        switch (p_matchTypeId)
        {
            case GUARANTEED_EXACT_MATCH:
                // Not used in System 4.0
                name = GUARANTEED_EXACT_MATCH_NAME;
                break;
            case LEVERAGE_GROUP_EXACT_MATCH:
                name = LEVERAGE_GROUP_EXACT_MATCH_NAME;
                break;
            case EXACT_MATCH_SAME_TM:
                name = EXACT_MATCH_SAME_TM_NAME;
                break;
            case EXACT_MATCH:
                name = EXACT_MATCH_NAME;
                break;
            case UNVERIFIED_EXACT_MATCH:
                name = UNVERIFIED_EXACT_MATCH_NAME;
                break;
            case DEMOTED_EXACT_MATCH:
                name = DEMOTED_EXACT_MATCH_NAME;
                break;
            case FUZZY_MATCH_SAME_TM:
                name = FUZZY_MATCH_SAME_TM_NAME;
                break;
            case FUZZY_MATCH:
                name = FUZZY_MATCH_NAME;
                break;
            default:
                name = UNKNOWN_NAME;
        }

        return name;
    }

    public static int matchTypeString2matchTypeId(String p_matchTypeString)
    {
        int id = LeverageMatchType.UNKNOWN;

        if (p_matchTypeString.equals(GUARANTEED_EXACT_MATCH_NAME))
        {
            id = GUARANTEED_EXACT_MATCH;
        }
        else if (p_matchTypeString.equals(LEVERAGE_GROUP_EXACT_MATCH_NAME))
        {
            id = LEVERAGE_GROUP_EXACT_MATCH;
        }
        else if (p_matchTypeString.equals(EXACT_MATCH_SAME_TM_NAME))
        {
            id = EXACT_MATCH_SAME_TM;
        }
        else if (p_matchTypeString.equals(EXACT_MATCH_NAME))
        {
            id = EXACT_MATCH;
        }
        else if (p_matchTypeString.equals(UNVERIFIED_EXACT_MATCH_NAME))
        {
            id = UNVERIFIED_EXACT_MATCH;
        }
        else if (p_matchTypeString.equals(DEMOTED_EXACT_MATCH_NAME))
        {
            id = DEMOTED_EXACT_MATCH;
        }
        else if (p_matchTypeString.equals(FUZZY_MATCH_SAME_TM_NAME))
        {
            id = FUZZY_MATCH_SAME_TM;
        }
        else if (p_matchTypeString.equals(FUZZY_MATCH_NAME))
        {
            id = FUZZY_MATCH;
        }
        else
        {
            id = UNKNOWN;
        }
        return id;
    }

    /**
     * Returns true if match type is DEMOTED_EXACT_MATCH false otherwise.
     */
    public boolean isDemoted()
    {
        if (m_matchType.equals(DEMOTED_EXACT_MATCH_NAME))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns true if match type is LEVERAGE_GROUP_EXACT_MATCH false otherwise.
     */
    public boolean isLeverageGroupExactMatch()
    {
        if (m_matchType.equals(LEVERAGE_GROUP_EXACT_MATCH_NAME))
        {
            return true;
        }

        return false;
    }

    /**
     * No numeric primary key in LEVERAGE_MATCH table. If the method is called a
     * RuntimeException is thrown.
     */
    public long getId()
    {
        return getTemporarilyUnavailableId();
    }
}
