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
package com.globalsight.everest.page;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * A data object designed to hold a variety of word counts for a page.
 */
public class PageWordCounts
    extends PersistentObject
{
    private static final long serialVersionUID = -5400707613158140160L;
    
    private Integer m_totalWordCount = new Integer(0);
    
    //The default context match word counts.
    private Integer m_contextMatchWordCount = new Integer(0);
    
    //The exact match word counts if use in-context match.
    private Integer m_segmentTmWordCount = new Integer(0);
    
    private Integer m_lowFuzzyWordCount = new Integer(0);
    private Integer m_medFuzzyWordCount = new Integer(0);
    private Integer m_medHiFuzzyWordCount = new Integer(0);
    private Integer m_hiFuzzyWordCount = new Integer(0);
    private Integer m_unmatchedWordCount = new Integer(0);
    private Integer m_repetitionWordCount = new Integer(0);
    private Integer m_subLevMatchWordCount = new Integer(0);
    private Integer m_subLevRepetitionWordCount = new Integer(0);
    
    //The in-context match word counts if use in-context match.
    private Integer m_inContextMatchWordCount = new Integer(0);
    
    //The exact match word counts if not use in-context match, the value is 0.
    private Integer m_noUseInContextMatchWordCount = new Integer(0);
    
    //The exact match word counts if not use in-context match.
    private Integer m_noUseExactMatchWordCount = new Integer(0);
    
    private Integer m_MTExactMatchWordCount = new Integer(0);
    
    private Integer m_xliffExactMatchWordCount = new Integer(0);

    //////////////////////////////////////////////////////////////////
    // Constructors
    //////////////////////////////////////////////////////////////////

    public PageWordCounts()
    {     
    }

    public PageWordCounts(int p_totalWordCount, int p_contextMatchWordCount,
            int p_segmentTmWordCount, int p_lowFuzzyWordCount,
            int p_medFuzzyWordCount, int p_medHiFuzzyWordCount,
            int p_hiFuzzyWordCount, int p_inContextMatchWordCount,
            int p_noUseInContextMatchWordCount, int p_noUseExactMatchWordCount,
            int p_unmatchedWordCount, int p_repetitionWordCount)
    {
        m_totalWordCount = new Integer(p_totalWordCount);
        m_contextMatchWordCount = new Integer(p_contextMatchWordCount);
        m_segmentTmWordCount = new Integer(p_segmentTmWordCount);
        m_lowFuzzyWordCount = new Integer(p_lowFuzzyWordCount);
        m_medFuzzyWordCount = new Integer(p_medFuzzyWordCount);
        m_medHiFuzzyWordCount = new Integer(p_medHiFuzzyWordCount);
        m_hiFuzzyWordCount = new Integer(p_hiFuzzyWordCount);
        m_inContextMatchWordCount = new Integer(p_inContextMatchWordCount);
        m_noUseInContextMatchWordCount = new Integer(p_noUseInContextMatchWordCount);
        m_noUseExactMatchWordCount = new Integer(p_noUseExactMatchWordCount);
        m_unmatchedWordCount = new Integer(p_unmatchedWordCount);
        m_repetitionWordCount = new Integer(p_repetitionWordCount);
    }

    //////////////////////////////////////////////////////////////////
    // public APIs
    //////////////////////////////////////////////////////////////////

    public void setInContextWordCount(int inContextWordCount)
    {
        m_inContextMatchWordCount = new Integer(inContextWordCount);
    }

    public int getInContextWordCount()
    {
        int inContextWordCount = 0;
        if (m_inContextMatchWordCount != null)
        {
            inContextWordCount = m_inContextMatchWordCount.intValue();
        }
        return inContextWordCount;
    }
    
    public void setNoUseInContextMatchWordCount(int noUseInContextMatchWordCount) {
        this.m_noUseInContextMatchWordCount = new Integer(noUseInContextMatchWordCount);
    }
    
    public void setNoUseExactMatchWordCount(int segmentTmWordCount) {
        this.m_noUseExactMatchWordCount = new Integer(segmentTmWordCount);
    }
    
    public int getNoUseInContextMatchWordCount() {
        int noUseInContextWordCount = 0;
        if(this.m_noUseInContextMatchWordCount != null){
            noUseInContextWordCount = this.m_noUseInContextMatchWordCount.intValue();
        }
        return noUseInContextWordCount;
    }
    
    public int getNoUseExactMatchWordCount() {
        int noUseExactWordCount = 0;
        if(this.m_noUseExactMatchWordCount != null){
            noUseExactWordCount = this.m_noUseExactMatchWordCount.intValue();
        }
        return noUseExactWordCount;
    }

    /**
     * To set the total word count.
     */
    public void setTotalWordCount(int p_totalWordCount)
    {
        m_totalWordCount = new Integer(p_totalWordCount);
    }

    /**
     * To get the total word count.
     * @return the total word count as int type.
     */
    public int getTotalWordCount()
    {
        int totalWordCount = 0;
        if (m_totalWordCount != null)
        {
            totalWordCount = m_totalWordCount.intValue();
        }
        return totalWordCount;
    }

    /**
     * To set the context match word count.
     */
    public void setContextMatchWordCount(int p_contextMatchWordCount)
    {
        m_contextMatchWordCount = new Integer(p_contextMatchWordCount);
    }

    /**
     * To get the context match word count.
     *
     * @return the context match word count as int type.
     */
    public int getContextMatchWordCount()
    {
        return m_contextMatchWordCount == null ? 
            0 : m_contextMatchWordCount.intValue();
    }

    /**
     * To set the context match word count.
     */
    public void setSegmentTmWordCount(int p_segmentTmWordCount)
    {
        m_segmentTmWordCount = new Integer(p_segmentTmWordCount);
    }

    /**
     * To get the segment TM match word count.
     *
     * @return the segment TM match word count as int type.
     */
    public int getSegmentTmWordCount()
    {
        return m_segmentTmWordCount == null ? 
            0 : m_segmentTmWordCount.intValue();
    }

    public void setLowFuzzyWordCount(int p_lowFuzzyWordCount)
    {
        m_lowFuzzyWordCount = new Integer(p_lowFuzzyWordCount);
    }

    /**
     * To get the low fuzzy matched word count (range of 50-74%).
     *
     * @return the low fuzzy matched word count as int type.
     */
    public int getLowFuzzyWordCount()
    {
        return m_lowFuzzyWordCount == null ? 0 : 
            m_lowFuzzyWordCount.intValue();
    }

    public void setMedFuzzyWordCount(int p_medFuzzyWordCount)
    {
        m_medFuzzyWordCount = new Integer(p_medFuzzyWordCount);
    }

    /**
     * To get the med fuzzy matched word count (range of 75-84%).
     *
     * @return the med fuzzy matched word count as int type.
     */
    public int getMedFuzzyWordCount()
    {
        return m_medFuzzyWordCount == null ? 0 : 
            m_medFuzzyWordCount.intValue();
    }

    public void setMedHiFuzzyWordCount(int p_medHiFuzzyWordCount)
    {
        m_medHiFuzzyWordCount = new Integer(p_medHiFuzzyWordCount);
    }

    /**
     * To get the med-hi fuzzy matched word count (range of 85-94%).
     *
     * @return the med-hi fuzzy matched word count as int type.
     */
    public int getMedHiFuzzyWordCount()
    {
        return m_medHiFuzzyWordCount == null ? 0 : 
            m_medHiFuzzyWordCount.intValue();
    }

    public void setHiFuzzyWordCount(int p_hiFuzzyWordCount)
    {
        m_hiFuzzyWordCount = new Integer(p_hiFuzzyWordCount);
    }

    /**
     * To get the hi fuzzy matched word count (range of 95-99%).
     *
     * @return the hi fuzzy matched word count as int type.
     */
    public int getHiFuzzyWordCount()
    {
        return m_hiFuzzyWordCount == null ? 0 : 
            m_hiFuzzyWordCount.intValue();
    }

    /**
     * To set the unmatched word count.
     */
    public void setUnmatchedWordCount(int p_unmatchedWordCount)
    {
        m_unmatchedWordCount = new Integer(p_unmatchedWordCount);
    }

    /**
     * To get the unmatched word count.
     *
     * @return the unmatched word count as int type.
     */
    public int getUnmatchedWordCount()
    {
        int unmatchedWordCount = 0;
        if (m_unmatchedWordCount != null)
        {
            unmatchedWordCount = m_unmatchedWordCount.intValue();
        }
        return unmatchedWordCount;
    }


    /**
     * To set the repetition word count.
     */
    public void setRepetitionWordCount(int p_repetitionWordCount)
    {
        m_repetitionWordCount = new Integer(p_repetitionWordCount);
    }

    /**
     * To get the repetition word count.
     *
     * @return the repetition word count as int type.
     */
    public int getRepetitionWordCount()
    {
        int repetitionWordCount = 0;
        if (m_repetitionWordCount != null)
        {
            repetitionWordCount = m_repetitionWordCount.intValue();
        }
        return repetitionWordCount;
    }

    /**
     * Get the word count for the sub-leverage-match category.
     * See setSubLevMatchWordCount method for more details.
     * 
     * @return The sub-leverage-match category word counts.
     */
    public int getSubLevMatchWordCount()
    {
        return m_subLevMatchWordCount == null ? 0 : 
            m_subLevMatchWordCount.intValue();
    }

    /**
     * Set the value of the Sub-Leverage-Match word count to be
     * the specified value.  This value is determined based on the 
     * leverage match threshold percentage of the job during import
     * with respect to the leverage match categories.  For example
     * if the leverage match threshold is at 70%, the value for
     * this word count would be the number of words in the 50-69%
     * category.
     *
     * @param p_subLevMatchWordCount The number of words in the sub
     * leverage match category.
     */
    public void setSubLevMatchWordCount(int p_subLevMatchWordCount)
    {
        m_subLevMatchWordCount = new Integer(p_subLevMatchWordCount);
    }

    /**
     * Get the repetition word count for the sub-leverage-match category.
     * See setSubLevRepetitionWordCount method for more details.
     * 
     * @return The sub-leverage-match-repetition word counts.
     */
    public int getSubLevRepetitionWordCount()
    {
        return m_subLevRepetitionWordCount == null ? 0 : 
            m_subLevRepetitionWordCount.intValue();
    }

    /**
     * Set the value of the Sub-Leverage-Match-Repetition word count to be
     * the specified value.  This value is determined based on the 
     * leverage match threshold percentage of the job during import
     * with respect to the leverage match categories.  For example
     * if the leverage match threshold is at 70%, the value for
     * this word count would be the number of repetitions in the 50-69%
     * category (since anything below 70% is considered no-match).
     *
     * @param p_subLevRepetitionWordCount The number of repetitions in the sub
     * leverage match category.
     */
    public void setSubLevRepetitionWordCount(int p_subLevRepetitionWordCount)
    {
        m_subLevRepetitionWordCount = new Integer(p_subLevRepetitionWordCount);
    }

    public void setMTExtractMatchWordCount(int p_mtExactMatchWordCount)
    {
    	this.m_MTExactMatchWordCount = new Integer(p_mtExactMatchWordCount);
    }
    
    public int getMTExtractMatchWordCount()
    {
    	return this.m_MTExactMatchWordCount;
    }
    
    public void setXliffExtractMatchWordCount(int p_xliffExactMatchWordCount)
    {
        this.m_xliffExactMatchWordCount = 
            new Integer(p_xliffExactMatchWordCount);
    }
    
    public int getXliffExtractMatchWordCount()
    {
        return this.m_xliffExactMatchWordCount;
    }

    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append(" [total=");
        sb.append((m_totalWordCount != null ?
            m_totalWordCount.toString() : "null"));
        sb.append(" context match=");
        sb.append((m_contextMatchWordCount != null ?
            m_contextMatchWordCount.toString() : "null"));
        sb.append(" segment TM=");
        sb.append((m_segmentTmWordCount != null ?
            m_segmentTmWordCount.toString() : "null"));
        sb.append(" low fuzzy (50%-74%)=");
        sb.append((m_lowFuzzyWordCount != null ?
            m_lowFuzzyWordCount.toString() : "null"));
        sb.append(" med fuzzy (75%-84%)=");
        sb.append((m_medFuzzyWordCount != null ?
            m_medFuzzyWordCount.toString() : "null"));
        sb.append(" med_hi fuzzy (85%-94%)=");
        sb.append((m_medHiFuzzyWordCount != null ?
            m_medHiFuzzyWordCount.toString() : "null"));
        sb.append(" HI fuzzy (95%-99%)=");
        sb.append((m_hiFuzzyWordCount != null ?
            m_hiFuzzyWordCount.toString() : "null"));
        sb.append(" unmatched=");
        sb.append((m_unmatchedWordCount != null ?
            m_unmatchedWordCount.toString() : "null"));
        sb.append(" repetition=");
        sb.append((m_repetitionWordCount != null ?
            m_repetitionWordCount.toString() : "null"));            
        sb.append("]");

        return sb.toString();
    }
}
