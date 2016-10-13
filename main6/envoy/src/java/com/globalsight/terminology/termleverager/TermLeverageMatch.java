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

package com.globalsight.terminology.termleverager;

import java.io.Serializable;
import java.util.Locale;

/**
 * A data class that holds data of a single term match: tuv ids, term
 * ids, terms, locales, scores, and so on.
 */
public class TermLeverageMatch
    implements Serializable
{
    private long m_termBaseId;
    private long m_sourceTuvId;
    //private Long m_sourceTuvIdLong;

    private long m_conceptId;
    private long m_matchedSourceTermId;
    private String m_matchedSourceTerm;

    private long m_matchedTargetTermId;
    private String m_matchedTargetTerm;

    private Locale m_targetPageLocale;
    private Locale m_sourcePageLocale;
    private Locale m_realSourceLocale;
    private Locale m_realTargetLocale;

    private int m_score;
    private int m_priority;

    /**
     * Value of the term status attribute ("preferred", "deprecated",
     * etc). Currently, the stored procedure find_fuzzy_match returns
     * either term matches with a single target whose status is
     * "preferred" or multiple targets if no status is specified for
     * the multiple targets.
     */
    private String m_targetStatus;

    //
    // Constructor
    //

    public TermLeverageMatch()
    {
    }

    //
    // Accessors
    //

    public long getTermBaseId()
    {
        return m_termBaseId;
    }

    public void setTermBaseId(long p_termBaseId)
    {
        m_termBaseId = p_termBaseId;
    }

    public long getSourceTuvId()
    {
        return m_sourceTuvId;
    }

    /*
    public Long getSourceTuvIdAsLong()
    {
        if (m_sourceTuvIdLong == null)
        {
            m_sourceTuvIdLong = new Long(m_sourceTuvId);
        }

        return m_sourceTuvIdLong;
    }
    */

    public long getMatchedSourceTermId()
    {
        return m_matchedSourceTermId;
    }

    public long getMatchedTargetTermId()
    {
        return m_matchedTargetTermId;
    }

    public Locale getTargetPageLocale()
    {
        return m_targetPageLocale;
    }

    public int getScore()
    {
        return m_score;
    }

    public int getPriority()
    {
        return m_priority;
    }

    public void setSourceTuvId(long p_sourceTuvId)
    {
        m_sourceTuvId = p_sourceTuvId;
    }

    public void setMatchedSourceTermId(long p_matchedSourceTermId)
    {
        m_matchedSourceTermId = p_matchedSourceTermId;
    }

    public void setMatchedTargetTermId(long p_matchedTargetTermId)
    {
        m_matchedTargetTermId = p_matchedTargetTermId;
    }

    public void setTargetPageLocale(Locale p_targetPageLocale)
    {
        m_targetPageLocale = p_targetPageLocale;
    }

    public void setScore(int p_score)
    {
        m_score = p_score;
    }

    public void setPriority(int p_priority)
    {
        m_priority = p_priority;
    }

    public String getMatchedSourceTerm()
    {
        return m_matchedSourceTerm;
    }

    public void setMatchedSourceTerm(String p_matchedSourceTerm)
    {
        m_matchedSourceTerm = p_matchedSourceTerm;
    }

    public String getMatchedTargetTerm()
    {
        return m_matchedTargetTerm;
    }

    public void setMatchedTargetTerm(String p_matchedTargetTerm)
    {
        m_matchedTargetTerm = p_matchedTargetTerm;
    }

    public String getTargetStatus()
    {
        return m_targetStatus;
    }

    public void setTargetStatus(String p_targetStatus)
    {
        m_targetStatus = p_targetStatus;
    }

    public Locale getSourcePageLocale()
    {
        return m_sourcePageLocale;
    }

    public void setSourcePageLocale(Locale p_sourcePageLocale)
    {
        m_sourcePageLocale = p_sourcePageLocale;
    }

    public long getConceptId()
    {
        return m_conceptId;
    }

    public void setConceptId(long p_conceptId)
    {
        m_conceptId = p_conceptId;
    }

    public Locale getRealSourceLocale()
    {
        return m_realSourceLocale;
    }

    public void setRealSourceLocale(Locale p_realSourceLocale)
    {
        m_realSourceLocale = p_realSourceLocale;
    }

    public Locale getRealTargetLocale()
    {
        return m_realTargetLocale;
    }

    public void setRealTargetLocale(Locale p_realTargetLocale)
    {
        m_realTargetLocale = p_realTargetLocale;
    }

    /**
     * Two leverage match objects are equal if their source term ids
     * are equal.
     */
    public boolean equals(Object p_termLeverageMatch)
    {
        if (p_termLeverageMatch instanceof TermLeverageMatch)
        {
            TermLeverageMatch other = (TermLeverageMatch)p_termLeverageMatch;

            return (other.getMatchedSourceTermId() == m_matchedSourceTermId);
        }
        else
        {
            return false;
        }
    }
}
