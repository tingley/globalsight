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

import java.lang.Comparable;
import java.util.Locale;
import java.util.Properties;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.ExactMatchFormatHandler;
import com.globalsight.ling.common.TuvSegmentBaseHandler;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.util.GlobalSightLocale;


public class CandidateMatch
    implements Comparable, LeverageMatchType, TuvLingConstants, Cloneable
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            CandidateMatch.class);

    private long m_originalSourceId;
    private long m_matchedTuId;
    private long m_matchedSourceId;
    private long m_matchedTargetId;
    private String m_matchType = null;
    private short m_orderNum = 0;
    private short m_scoreNum = 100;
    private String m_gxmlSource = null;
    private String m_gxmlTarget = null;
    private String m_exactMatchFormatSource = null;
    private String m_exactMatchFormatTarget = null;
    private GlobalSightLocale m_targetLocale;
    private String m_state = null;
    private String m_format = null;
    private String m_type = null;
    private boolean m_isTranslatable;
    // last modification date of the target tuv
    private Timestamp m_timestamp = null;

    public CandidateMatch()
    {
    }

    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch(CloneNotSupportedException e)
        {
            // doesn't happen
            return null;
        }
    }
    
        
    public String getSourceExactMatchFormat()
        throws LingManagerException
    {
        if (m_exactMatchFormatSource == null)
        {
            m_exactMatchFormatSource =
                getExactMatchFormat(m_gxmlSource);
        }

        return m_exactMatchFormatSource;
    }

    public String getTargetExactMatchFormat()
        throws LingManagerException
    {
        if (m_exactMatchFormatTarget == null)
        {
            m_exactMatchFormatTarget =
                getExactMatchFormat(m_gxmlTarget);
        }

        return m_exactMatchFormatTarget;
    }

    /**
     * Get the native formatted string - used to generate CRCs.
     */
    private String getExactMatchFormat(String p_gxmlSegment)
        throws LingManagerException
    {
        ExactMatchFormatHandler handler = new ExactMatchFormatHandler();
        DiplomatBasicParser diplomatParser =
            new DiplomatBasicParser(handler);

        try
        {
            diplomatParser.parse(p_gxmlSegment);
        }
        catch (DiplomatBasicParserException e)
        {
            long id = (p_gxmlSegment == m_gxmlSource ?
                m_matchedSourceId : m_matchedTargetId);

            CATEGORY.error("Corrupt TUV " + id + ": " + p_gxmlSegment);
            throw new LingManagerException(e);
        }

        return handler.toString();
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

    public String getMatchTypeString()
    {
        return m_matchType;
    }

    public boolean isCompleted()
    {
        if (m_state.equals(COMPLETE_NAME) ||
            m_state.equals(ALIGNMENT_LOCALIZED_NAME))
        {
            return true;
        }

        return false;
    }

    public void setMatchType(int p_matchType)
    {
        m_matchType = matchTypeId2matchTypeString(p_matchType);
    }

    public String getGxmlTarget()
    {
        return m_gxmlTarget;
    }

    public void setGxmlTarget(String p_gxml)
    {
        m_gxmlTarget = p_gxml;
    }

    public String getGxmlSource()
    {
        return m_gxmlSource;
    }

    public void setGxmlSource(String p_gxml)
    {
        m_gxmlSource = p_gxml;
    }

    public void setMatchedTuId(long p_matchedTuId)
    {
        m_matchedTuId = p_matchedTuId;
    }

    public long getMatchedTuId()
    {
        return m_matchedTuId;
    }

    public void setMatchedTargetId(long p_matchedTargetId)
    {
        m_matchedTargetId = p_matchedTargetId;
    }

    public long getMatchedTargetId()
    {
        return m_matchedTargetId;
    }

    public void setMatchedSourceId(long p_matchedSourceId)
    {
        m_matchedSourceId = p_matchedSourceId;
    }

    public long getMatchedSourceId()
    {
        return m_matchedSourceId;
    }

    public long getOriginalSourceId()
    {
        return m_originalSourceId;
    }

    public void setOriginalSourceId(long p_originalSourceId)
    {
        m_originalSourceId = p_originalSourceId;
    }

    /**
     * Generates a hash code for the receiver.  This method is
     * supported primarily for hash tables, such as those provided in
     * java.util.
     * @return an integer hash code for the receiver
     * @see java.util.Hashtable
     */
    public int hashCode()
    {
        int hashCode = 0;

        try
        {
            hashCode =
                (getSourceExactMatchFormat() +
                getTargetExactMatchFormat()).hashCode();
        }
        catch (LingManagerException e)
        {
            // This method can't throw any exception because it
            // overrides java.lang.Object#hashCode. So it just logs
            // the error.
            CATEGORY.error("Can't compute hash code: ", e);
        }

        return hashCode;
    }

    public boolean equals(Object p_other)
    {
        String oneSource = null;
        String oneTarget = null;
        String twoSource = null;
        String twoTarget = null;

        try
        {
            oneSource = getSourceExactMatchFormat();
            oneTarget = getTargetExactMatchFormat();

            twoSource =
                ((CandidateMatch)p_other).getSourceExactMatchFormat();
            twoTarget =
                ((CandidateMatch)p_other).getTargetExactMatchFormat();
        }
        catch (LingManagerException e)
        {
            // This method can't throw any exception because it
            // overrides java.lang.Object#equals. So it just logs the
            // error.
            CATEGORY.error("Can't compare objects: ", e);
        }

        return oneSource.equals(twoSource) && oneTarget.equals(twoTarget);
    }

    public boolean isExactMatch(boolean p_includeDemoted)
    {
        boolean isExact = false;
        int type  = matchTypeString2matchTypeId(m_matchType);

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

        if (p_includeDemoted && type == DEMOTED_EXACT_MATCH)
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

    public boolean sameMatchedSource(CandidateMatch p_other)
        throws LingManagerException
    {
        return getSourceExactMatchFormat().equals(
               p_other.getSourceExactMatchFormat());
    }

    public boolean sameMatchedSource(String p_other)
        throws LingManagerException
    {
        return getSourceExactMatchFormat().equals(p_other);
    }

    public void setScoreNum(short p_scoreNum)
    {
        m_scoreNum = p_scoreNum;
    }

    public short getScoreNum()
    {
        return m_scoreNum;
    }

    public long getTargetLocaleId()
    {
        return m_targetLocale.getId();
    }

    public Locale getTargetLocale()
    {
        return m_targetLocale.getLocale();
    }

    public GlobalSightLocale getTargetGlobalSightLocale()
    {
        return m_targetLocale;
    }

    public void setTargetLocale(GlobalSightLocale p_targetLocale)
    {
        m_targetLocale = p_targetLocale;
    }

    public static String getMatchTypeName(int p_matchType)
    {
        return matchTypeId2matchTypeString(p_matchType);
    }

    public String getState()
    {
        return m_state;
    }

    public void setState(String p_state)
    {
        m_state = p_state;
    }

    public String getFormat()
    {
        return m_format;
    }
    
    public void setFormat(String p_format)
    {
        m_format = p_format;
    }
    
    public String getType()
    {
        return m_type;
    }
    
    public void setType(String p_type)
    {
        m_type = p_type;
    }

    public boolean isTranslatable()
    {
        return m_isTranslatable;
    }

    public void setTranslatable(boolean p_isTranslatable)
    {
        m_isTranslatable = p_isTranslatable;
    }
    

    public Timestamp getTimestamp()
    {
        return m_timestamp;
    }

    public void setTimestamp(Timestamp p_timestamp)
    {
        m_timestamp = p_timestamp;
    }

    // Overridden method from Comparable
    public int compareTo(Object o)
    {
        CandidateMatch other = null;

        if (o instanceof CandidateMatch)
        {
            other = (CandidateMatch)o;
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
            // Don't care about the other type.
            cmp = getCompareKey(m_matchType) -
                  getCompareKey(other.m_matchType);
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
            name = LeverageMatchType.UNKNOWN_NAME;
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
            id = LeverageMatchType.UNKNOWN;
        }

        return id;
    }

    /**
     * Returns true if match type is DEMOTED_EXACT_MATCH false
     * otherwise.
     */
    public boolean isDemoted()
    {
        if (m_matchType.equals(DEMOTED_EXACT_MATCH_NAME))
        {
            return true;
        }

        return false;
    }


    public String toString()
    {
        return "{m_originalSourceId = " + m_originalSourceId
            + ", m_matchedSourceId = " + m_matchedSourceId
            + ", m_matchedTargetId = " + m_matchedTargetId
            + ", m_matchType = "
            + (m_matchType == null ? "null" : m_matchType)
            + ", m_orderNum = " + m_orderNum + ", m_scoreNum = "
            + m_scoreNum + ", m_targetLocale = " + m_targetLocale
            + ", m_state = " + (m_state == null ? "null" : m_state) + "}";
    }


    /**
     * Returns true if match type is LEVERAGE_GROUP_EXACT_MATCH false
     * otherwise.
     */
    public boolean isLeverageGroupExactMatch()
    {
        if (m_matchType.equals(LEVERAGE_GROUP_EXACT_MATCH_NAME))
        {
            return true;
        }

        return false;
    }

}
