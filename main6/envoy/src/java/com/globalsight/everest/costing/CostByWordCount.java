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
package com.globalsight.everest.costing;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Provides granular cost information by word count for costable objects (tasks,
 * workflows, jobs).
 */
public class CostByWordCount extends PersistentObject
{
    private static final long serialVersionUID = -5544386236807865510L;

    private Cost m_cost = null;
    private Float repetitionCost = new Float(0.00);
    private Float contextMatchCost = new Float(0.00);
    private Float segmentTmCost = new Float(0.00);
    private Float lowFuzzyMatchCost = new Float(0.00);
    private Float medFuzzyMatchCost = new Float(0.00);
    private Float medHiFuzzyMatchCost = new Float(0.00);
    private Float hiFuzzyMatchCost = new Float(0.00);
    private Float noMatchCost = new Float(0.00);
    private Float inContextMatchCost = new Float(0.00);
    private Float noUseInContextMatchCost = new Float(0.00);
    private Float noUseExactMatchCost = new Float(0.00);
    private Float defaultContextExactMatchCost = new Float(0.00);
    /**
     * Constructor for TOPLink use only.
     */
    public CostByWordCount()
    {
    }

    /**
     * Constructor which takes in the granular costs
     * 
     * @param p_cost
     * @param p_repetitionCost
     * @param p_exactMatchCost
     * @param p_fuzzyMatchCost
     * @param p_noMatchCost
     */
    public CostByWordCount(Cost p_cost, float p_repetitionCost,
            float p_contextMatchCost, float p_inContextMatchCost,
            float p_segmentTmCost, float p_lowFuzzyMatchCost,
            float p_medFuzzyMatchCost, float p_medHiFuzzyMatchCost,
            float p_hiFuzzyMatchCost, float p_noMatchCost,
            float p_noUseInContextMatchCost, float p_noUseExactMatchCost,
            float p_defaultContextSegmentTmMatchCost)
    {
        m_cost = p_cost;
        repetitionCost = new Float(p_repetitionCost);
        contextMatchCost = new Float(p_contextMatchCost);
        segmentTmCost = new Float(p_segmentTmCost);
        lowFuzzyMatchCost = new Float(p_lowFuzzyMatchCost);
        medFuzzyMatchCost = new Float(p_medFuzzyMatchCost);
        medHiFuzzyMatchCost = new Float(p_medHiFuzzyMatchCost);
        hiFuzzyMatchCost = new Float(p_hiFuzzyMatchCost);
        noMatchCost = new Float(p_noMatchCost);
        inContextMatchCost = new Float(p_inContextMatchCost);
        noUseInContextMatchCost = new Float(p_noUseInContextMatchCost);
        noUseExactMatchCost = new Float(p_noUseExactMatchCost);
        defaultContextExactMatchCost = p_defaultContextSegmentTmMatchCost;
    }

    /**
     * Constructor which takes in a cost. All granular costs are set to 0.0
     * 
     * @param p_cost
     */
    public CostByWordCount(Cost p_cost)
    {
        m_cost = p_cost;
    }

    /**
     * Returns the cost for which this CostByWordCount is a granular breakdown.
     * 
     * @return
     */
    public Cost getCost()
    {
        return m_cost;
    }

    /**
     * Sets the cost for which this CostByWordCount is a granular breakdown.
     * 
     */
    public void setCost(Cost p_cost)
    {
        m_cost = p_cost;
    }

    /**
     * Adds the granular costs by word count from the other CostByWordCount
     * object to this one's granular costs.
     * 
     * @param p_other
     */
    public void add(CostByWordCount p_other)
    {
        // The areas where calculations are done (multiply, add, subtract)
        // should be changed to use BigDecimal for the actual calculation.
        setContextMatchCost(BigDecimalHelper.add(getContextMatchCost(), p_other
                .getContextMatchCost()));
        setDefaultContextExactMatchCost(BigDecimalHelper.add(getDefaultContextExactMatchCost(),
                p_other.getDefaultContextExactMatchCost()));
        setSegmentTmMatchCost(BigDecimalHelper.add(getSegmentTmMatchCost(),
                p_other.getSegmentTmMatchCost()));
        setLowFuzzyMatchCost(BigDecimalHelper.add(getLowFuzzyMatchCost(),
                p_other.getLowFuzzyMatchCost()));
        setMedFuzzyMatchCost(BigDecimalHelper.add(getMedFuzzyMatchCost(),
                p_other.getMedFuzzyMatchCost()));
        setMedHiFuzzyMatchCost(BigDecimalHelper.add(getMedHiFuzzyMatchCost(),
                p_other.getMedHiFuzzyMatchCost()));
        setHiFuzzyMatchCost(BigDecimalHelper.add(getHiFuzzyMatchCost(), p_other
                .getHiFuzzyMatchCost()));
        setRepetitionCost(BigDecimalHelper.add(getRepetitionCost(), p_other
                .getRepetitionCost()));
        setNoMatchCost(BigDecimalHelper.add(getNoMatchCost(), p_other
                .getNoMatchCost()));
        setInContextMatchCost(BigDecimalHelper.add(getInContextMatchCost(),
                p_other.getInContextMatchCost()));
        setNoUseExactMatchCost(BigDecimalHelper.add(getNoUseExactMatchCost(),
                p_other.getNoUseExactMatchCost()));
        setNoUseInContextMatchCost(BigDecimalHelper.add(
                getNoUseInContextMatchCost(), p_other
                        .getNoUseInContextMatchCost()));
    }

    /**
     * Sets the granular cost values (exact match cost, fuzzy match cost, etc.)
     * to the values of the other CostByWordCount
     * 
     * @param p_other
     */
    public void set(CostByWordCount p_other)
    {
        setContextMatchCost(p_other.getContextMatchCost());
        setDefaultContextExactMatchCost(p_other.getDefaultContextExactMatchCost());
        setSegmentTmMatchCost(p_other.getSegmentTmMatchCost());
        setLowFuzzyMatchCost(p_other.getLowFuzzyMatchCost());
        setMedFuzzyMatchCost(p_other.getMedFuzzyMatchCost());
        setMedHiFuzzyMatchCost(p_other.getMedHiFuzzyMatchCost());
        setHiFuzzyMatchCost(p_other.getHiFuzzyMatchCost());
        setRepetitionCost(p_other.getRepetitionCost());
        setNoMatchCost(p_other.getNoMatchCost());
        setInContextMatchCost(p_other.getInContextMatchCost());
        setNoUseExactMatchCost(p_other.getNoUseExactMatchCost());
        setNoUseInContextMatchCost(p_other.getNoUseInContextMatchCost());
    }

    public float getInContextMatchCost()
    {
        return inContextMatchCost.floatValue();
    }

    public void setInContextMatchCost(float p_value)
    {
        inContextMatchCost = new Float(p_value);
    }

    /**
     * Get the Context Match cost.
     * 
     */
    public float getContextMatchCost()
    {
        return contextMatchCost == null ? 0.0f : contextMatchCost.floatValue();
    }

    /**
     * Set the Context Match cost to the value. Assumes it is in the Currency
     * set.
     */
    public void setContextMatchCost(float p_value)
    {
        contextMatchCost = new Float(p_value);
    }
    
    public void setDefaultContextExactMatchCost(float p_defaultContextExactMatchCost)
    {
        defaultContextExactMatchCost = new Float(p_defaultContextExactMatchCost);
    }

    public float getDefaultContextExactMatchCost()
    {
        return defaultContextExactMatchCost == null ? 0.0f : defaultContextExactMatchCost.floatValue();
    }

    public void setNoUseInContextMatchCost(float p_noUseInContextMatchCost)
    {
        noUseInContextMatchCost = new Float(p_noUseInContextMatchCost);
    }

    public float getNoUseInContextMatchCost()
    {
        return noUseInContextMatchCost == null ? 0f : noUseInContextMatchCost.floatValue();
    }

    public void setNoUseExactMatchCost(float p_noUseExactMatchCost)
    {
        noUseExactMatchCost = new Float(p_noUseExactMatchCost);
    }

    public float getNoUseExactMatchCost()
    {
        return noUseExactMatchCost == null ? 0f : noUseExactMatchCost.floatValue();
    }

    /**
     * Get the Segment TM Match cost.
     * 
     */
    public float getSegmentTmMatchCost()
    {
        return segmentTmCost == null ? 0.0f : segmentTmCost.floatValue();
    }

    /**
     * Set the Exact Segment TM Match cost to the value. Assumes it is in the
     * Currency set.
     */
    public void setSegmentTmMatchCost(float p_value)
    {
        segmentTmCost = new Float(p_value);
    }

    /**
     * Set the Low Fuzzy Match cost to the value. Assumes it is in the Currency
     * set.
     */
    public void setLowFuzzyMatchCost(float p_value)
    {
        lowFuzzyMatchCost = new Float(p_value);
    }

    /**
     * Get the Low Fuzzy Match cost.
     * 
     */
    public float getLowFuzzyMatchCost()
    {
        return lowFuzzyMatchCost == null ? 0.0f : lowFuzzyMatchCost
                .floatValue();
    }

    /**
     * Get the Med Fuzzy Match cost.
     * 
     */
    public float getMedFuzzyMatchCost()
    {
        return medFuzzyMatchCost == null ? 0.0f : medFuzzyMatchCost
                .floatValue();
    }

    /**
     * Set the Med Fuzzy Match cost to the value. Assumes it is in the Currency
     * set.
     */
    public void setMedFuzzyMatchCost(float p_value)
    {
        medFuzzyMatchCost = new Float(p_value);
    }

    /**
     * Get the Med-Hi Fuzzy Match cost.
     * 
     */
    public float getMedHiFuzzyMatchCost()
    {
        return medHiFuzzyMatchCost == null ? 0.0f : medHiFuzzyMatchCost
                .floatValue();
    }

    /**
     * Set the Med-Hi Fuzzy Match cost to the value. Assumes it is in the
     * Currency set.
     */
    public void setMedHiFuzzyMatchCost(float p_value)
    {
        medHiFuzzyMatchCost = new Float(p_value);
    }

    /**
     * Get the HI Fuzzy Match cost.
     * 
     */
    public float getHiFuzzyMatchCost()
    {
        return hiFuzzyMatchCost == null ? 0.0f : hiFuzzyMatchCost.floatValue();
    }

    /**
     * Set the HI Fuzzy Match cost to the value. Assumes it is in the Currency
     * set.
     */
    public void setHiFuzzyMatchCost(float p_value)
    {
        hiFuzzyMatchCost = new Float(p_value);
    }

    /**
     * Get the NoMatch cost.
     * 
     */
    public float getNoMatchCost()
    {
        return noMatchCost == null ? 0.0f : noMatchCost.floatValue();
    }

    /**
     * Set the NoMatch cost to the value. Assumes it is in the Currency set.
     */
    public void setNoMatchCost(float p_value)
    {
        noMatchCost = new Float(p_value);
    }

    /**
     * Get the RepetitionCost cost.
     * 
     */
    public float getRepetitionCost()
    {
        return repetitionCost == null ? 0.0f : repetitionCost.floatValue();
    }

    /**
     * Set the Repetition cost to the value. Assumes it is in the Currency set.
     */
    public void setRepetitionCost(float p_value)
    {
        repetitionCost = new Float(p_value);
    }

    /**
     * Returns a string representation of the CostByWordCount object. Useful for
     * debugging.
     * 
     * @return
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("CostByWordCount(id=");
        sb.append(getId());
        sb.append(",cost=");
        sb.append(m_cost);
        sb.append(",repetitionCost=");
        sb.append(repetitionCost);
        sb.append(", contextMatchCost=");
        sb.append(contextMatchCost);
        sb.append(", segmentTmCost=");
        sb.append(segmentTmCost);
        sb.append(", lowFuzzyMatchCost=");
        sb.append(lowFuzzyMatchCost);
        sb.append(", medFuzzyMatchCost=");
        sb.append(medFuzzyMatchCost);
        sb.append(", medHiFuzzyMatchCost=");
        sb.append(medHiFuzzyMatchCost);
        sb.append(", hiFuzzyMatchCost=");
        sb.append(hiFuzzyMatchCost);
        sb.append(", noMatchCost=");
        sb.append(noMatchCost);
        sb.append(")");
        return sb.toString();
    }
}
