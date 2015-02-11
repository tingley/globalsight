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
public class PageWordCounts extends PersistentObject
{
    private static final long serialVersionUID = -5400707613158140160L;
    
    private Integer m_totalWordCount = new Integer(0);
    private Integer m_noMatchWordCount = new Integer(0);
    private Integer m_lowFuzzyWordCount = new Integer(0);
    private Integer m_medFuzzyWordCount = new Integer(0);
    private Integer m_medHiFuzzyWordCount = new Integer(0);
    private Integer m_hiFuzzyWordCount = new Integer(0);
    private Integer m_repetitionWordCount = new Integer(0);

    // When use MT,
    // If MT confidence score is 100, mtTotalWordCount =
    // m_MTExactMatchWordCount, mtFuzzyNoMatchWordCount and
    // mtRepetitionsWordCount are 0 now.
    // If MT confidence score is < 100, mtTotalWordCount =
    // mtFuzzyNoMatchWordCount + mtRepetitionsWordCount, m_MTExactMatchWordCount
    // is 0 now.
    private Integer mtTotalWordCount = new Integer(0);
    private Integer mtFuzzyNoMatchWordCount = new Integer(0);
    private Integer mtRepetitionsWordCount = new Integer(0);
    // Word count MT engine "thinks".
    private Integer mtEngineWordCount = new Integer(0);

    /**
     * This includes ALL exact match word counts(segment-TM,context,MT,XLF and
     * PO exact matches etc).
     */
    private Integer totalExactMatchWordCount = new Integer(0);
    /**
     * The default context match word counts.
     */
    private Integer contextMatchWordCount = new Integer(0);
    /**
     * The exact match word counts(exclude ICE word counts).
     * (allExactMatchWordCount = segmentTmWordCount + inContextMatchWordCount)
     */
    private Integer segmentTmWordCount = new Integer(0);
    /**
     * The ICE word counts. 
     */
    private Integer inContextMatchWordCount = new Integer(0);

    private Integer m_noUseInContextMatchWordCount = new Integer(0);
    
    private Integer m_MTExactMatchWordCount = new Integer(0);
    private Integer m_xliffExactMatchWordCount = new Integer(0);
    private Integer m_poExactMatchWordCount = new Integer(0);
    
    private Integer m_thresholdHiFuzzyWordCount = new Integer(0);
    private Integer m_thresholdMedHiFuzzyWordCount = new Integer(0);
    private Integer m_thresholdMedFuzzyWordCount = new Integer(0);
    private Integer m_thresholdLowFuzzyWordCount = new Integer(0);
    private Integer m_thresholdNoMatchWordCount = new Integer(0);

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
            int p_noUseInContextMatchWordCount, int p_totalExactMatchWordCount,
            int p_noMatchWordCount, int p_repetitionWordCount)
    {
        m_totalWordCount = new Integer(p_totalWordCount);
        contextMatchWordCount = new Integer(p_contextMatchWordCount);
        segmentTmWordCount = new Integer(p_segmentTmWordCount);
        m_lowFuzzyWordCount = new Integer(p_lowFuzzyWordCount);
        m_medFuzzyWordCount = new Integer(p_medFuzzyWordCount);
        m_medHiFuzzyWordCount = new Integer(p_medHiFuzzyWordCount);
        m_hiFuzzyWordCount = new Integer(p_hiFuzzyWordCount);
        inContextMatchWordCount = new Integer(p_inContextMatchWordCount);
        m_noUseInContextMatchWordCount = new Integer(p_noUseInContextMatchWordCount);
        totalExactMatchWordCount = new Integer(p_totalExactMatchWordCount);
        m_noMatchWordCount = new Integer(p_noMatchWordCount);
        m_repetitionWordCount = new Integer(p_repetitionWordCount);
    }

    //////////////////////////////////////////////////////////////////
    // public APIs
    //////////////////////////////////////////////////////////////////

    public void setInContextWordCount(int inContextWordCount)
    {
        inContextMatchWordCount = new Integer(inContextWordCount);
    }

    public int getInContextWordCount()
    {
        int inContextWordCount = 0;
        if (inContextMatchWordCount != null)
        {
            inContextWordCount = inContextMatchWordCount.intValue();
        }
        return inContextWordCount;
    }
    
    public void setTotalExactMatchWordCount(int p_totalExactMatchWordCount) {
        this.totalExactMatchWordCount = new Integer(p_totalExactMatchWordCount);
    }
    
    public int getTotalExactMatchWordCount() {
        if(totalExactMatchWordCount != null) {
            return totalExactMatchWordCount.intValue();
        } else {
            return 0;
        }
    }
    
    /**
     * @deprecated -- This is useless in current codes(It is always 0).
     * @since 8.2.1
     */
    public void setNoUseInContextMatchWordCount(int noUseInContextMatchWordCount) {
        this.m_noUseInContextMatchWordCount = new Integer(noUseInContextMatchWordCount);
    }

    /**
     * @deprecated -- This is useless in current codes(It is always 0).
     * @since 8.2.1
     */
    public int getNoUseInContextMatchWordCount() {
        int noUseInContextWordCount = 0;
        if(this.m_noUseInContextMatchWordCount != null){
            noUseInContextWordCount = this.m_noUseInContextMatchWordCount.intValue();
        }
        return noUseInContextWordCount;
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
        contextMatchWordCount = new Integer(p_contextMatchWordCount);
    }

    /**
     * To get the context match word count.
     *
     * @return the context match word count as int type.
     */
    public int getContextMatchWordCount()
    {
        return contextMatchWordCount == null ? 
            0 : contextMatchWordCount.intValue();
    }

    
    public Integer getThresholdHiFuzzyWordCount()
    {
        return m_thresholdHiFuzzyWordCount;
    }

    public void setThresholdHiFuzzyWordCount(Integer thresholdHiFuzzyWordCount)
    {
        this.m_thresholdHiFuzzyWordCount = thresholdHiFuzzyWordCount;
    }

    public Integer getThresholdMedHiFuzzyWordCount()
    {
        return m_thresholdMedHiFuzzyWordCount;
    }

    public void setThresholdMedHiFuzzyWordCount(Integer thresholdMedHiFuzzyWordCount)
    {
        this.m_thresholdMedHiFuzzyWordCount = thresholdMedHiFuzzyWordCount;
    }

    public Integer getThresholdMedFuzzyWordCount()
    {
        return m_thresholdMedFuzzyWordCount;
    }

    public void setThresholdMedFuzzyWordCount(Integer thresholdMedFuzzyWordCount)
    {
        this.m_thresholdMedFuzzyWordCount = thresholdMedFuzzyWordCount;
    }

    public Integer getThresholdLowFuzzyWordCount()
    {
        return m_thresholdLowFuzzyWordCount;
    }

    public void setThresholdLowFuzzyWordCount(Integer thresholdLowFuzzyWordCount)
    {
        this.m_thresholdLowFuzzyWordCount = thresholdLowFuzzyWordCount;
    }

    
    public Integer getThresholdNoMatchWordCount()
    {
        return m_thresholdNoMatchWordCount;
    }

    public void setThresholdNoMatchWordCount(Integer thresholdNoMatchWordCount)
    {
        this.m_thresholdNoMatchWordCount = thresholdNoMatchWordCount;
    }

    /**
     * To set the context match word count.
     */
    public void setSegmentTmWordCount(int p_segmentTmWordCount)
    {
        segmentTmWordCount = new Integer(p_segmentTmWordCount);
    }

    /**
     * To get the segment TM match word count.
     *
     * @return the segment TM match word count as int type.
     */
    public int getSegmentTmWordCount()
    {
        return segmentTmWordCount == null ? 
            0 : segmentTmWordCount.intValue();
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
    public void setNoMatchWordCount(int p_noMatchWordCount)
    {
        m_noMatchWordCount = new Integer(p_noMatchWordCount);
    }

    /**
     * To get the unmatched word count.
     *
     * @return the unmatched word count as int type.
     */
    public int getNoMatchWordCount()
    {
        int noMatchWordCount = 0;
        if (m_noMatchWordCount != null)
        {
            noMatchWordCount = m_noMatchWordCount.intValue();
        }
        return noMatchWordCount;
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

    public void setMtExactMatchWordCount(int p_mtExactMatchWordCount)
    {
    	this.m_MTExactMatchWordCount = new Integer(p_mtExactMatchWordCount);
    }
    
    public int getMtExactMatchWordCount()
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
    
    public void setPoExactMatchWordCount(int p_poExactMatchWordCount)
    {
        this.m_poExactMatchWordCount = new Integer(p_poExactMatchWordCount);
    }
    
    public int getPoExactMatchWordCount()
    {
        return this.m_poExactMatchWordCount;
    }

    public void setMtTotalWordCount(int p_mtTotalWordCount)
    {
        this.mtTotalWordCount = p_mtTotalWordCount;
    }

    public int getMtTotalWordCount()
    {
        return this.mtTotalWordCount;
    }

    public void setMtFuzzyNoMatchWordCount(int p_mtFuzzyNoMatchWordCount)
    {
        this.mtFuzzyNoMatchWordCount = p_mtFuzzyNoMatchWordCount;
    }

    public int getMtFuzzyNoMatchWordCount()
    {
        return this.mtFuzzyNoMatchWordCount;
    }

    public void setMtRepetitionsWordCount(int p_mtRepetitionsWordCount)
    {
        this.mtRepetitionsWordCount = p_mtRepetitionsWordCount;
    }

    public int getMtRepetitionsWordCount()
    {
        return this.mtRepetitionsWordCount;
    }

    public void setMtEngineWordCount(int p_mtEngineWordCount)
    {
        this.mtEngineWordCount = p_mtEngineWordCount;
    }

    public int getMtEngineWordCount()
    {
        return this.mtEngineWordCount;
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
        sb.append((contextMatchWordCount != null ?
            contextMatchWordCount.toString() : "null"));
        sb.append(" segment TM=");
        sb.append((segmentTmWordCount != null ?
            segmentTmWordCount.toString() : "null"));
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
        sb.append((m_noMatchWordCount != null ?
            m_noMatchWordCount.toString() : "null"));
        sb.append(" repetition=");
        sb.append((m_repetitionWordCount != null ?
            m_repetitionWordCount.toString() : "null"));            
        sb.append("]");

        return sb.toString();
    }
}
