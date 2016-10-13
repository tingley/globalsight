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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.workflow.Activity;

/**
 * Provides the information needed to store rate information.
 */
public class Rate extends PersistentObject
{
    private static final long serialVersionUID = -6465470807772781953L;

    // query key used to build a TOPLink query
    public static final String M_NO_MATCH_RATE = "m_noMatchRate";
    public static final String M_UNIT_RATE = "m_unitRate";
    public static final String TYPE = "m_type";

    // Please do not change the order for UnitOfWork Items
    // The properties file assumes this order.
    // So if you need to add new items keep it in sync
    // with the properties file.
    public interface UnitOfWork
    {
        public final static Integer FIXED = new Integer(1);
        public final static Integer HOURLY = new Integer(2);
        public final static Integer PAGE_COUNT = new Integer(3);
        public final static Integer WORD_COUNT = new Integer(4);
        public final static Integer WORD_COUNT_BY = new Integer(5);
    };

    // holds all the rate types (unit of works)
    static private Integer[] m_allRateTypes;
    static private HashMap TYPE_MAY;
    static
    {
        TYPE_MAY = new HashMap();
        TYPE_MAY.put(new Integer(1), "F");
        TYPE_MAY.put(new Integer(2), "H");
        TYPE_MAY.put(new Integer(3), "P");
        TYPE_MAY.put(new Integer(4), "W");
        TYPE_MAY.put(new Integer(5), "B");
    }

    // ------------------------ private data members-----------------------

    private Integer m_type; // the type of rate is is
    // from one of the unit of works above.
    private Currency m_currency = null;
    private LocalePair m_localePair = null; // the locale pair the rate is
    // associated with
    private Activity m_activity = null; // the activity the rate is associated
    // with
    private float m_unitRate = 0; // Rate For Page/Hourly/Fixed type.
    private float m_contextMatchRate = 0; // Rate For Word-Count Exact Context
    // Match.
    private float m_segmentTmRate = 0; // Rate For Word-Count Exact Segment TM
    // Match.
    private float m_lowFuzzyMatchRate = 0; // Rate For Word-Count Low Fuzzy
    // Match (50-74%).
    private float m_medFuzzyMatchRate = 0; // Rate For Word-Count Med Fuzzy
    // Match (75-84%).
    private float m_medHiFuzzyMatchRate = 0; // Rate For Word-Count Med Hi
    // Fuzzy Match (85-94%).
    private float m_hiFuzzyMatchRate = 0; // Rate For Word-Count Hi Fuzzy
    // Match (95-99%).
    private float m_noMatchRate = 0; // Rate For Word-Count No Match.
    private float repetitionRate = 0; // Rate For Word-Count No Match
    private float inContextMatchRate = 0;
    // Repetitions.

    public boolean useActive = true;
    
    // Added by Vincent Yan 2009/07/13
    private float inContextMatchRatePer = 0;
    private float contextMatchRatePer = 0;
    private float segmentTmRatePer = 0;
    private float hiFuzzyMatchRatePer = 0;
    private float medHiFuzzyMatchRatePer = 0;
    private float medFuzzyMatchRatePer = 0;
    private float lowFuzzyMatchRatePer = 0;
    private float repetitionRatePer = 0;

	public float getInContextMatchRatePer() {
		return inContextMatchRatePer;
	}

	public void setInContextMatchRatePer(float inContextMatchRatePer) {
		this.inContextMatchRatePer = inContextMatchRatePer;
	}


    
    // initialize the collection of rate types
    static
    {
        m_allRateTypes = new Integer[5];
        m_allRateTypes[0] = UnitOfWork.FIXED;
        m_allRateTypes[1] = UnitOfWork.HOURLY;
        m_allRateTypes[2] = UnitOfWork.PAGE_COUNT;
        m_allRateTypes[3] = UnitOfWork.WORD_COUNT;
        m_allRateTypes[4] = UnitOfWork.WORD_COUNT_BY;
    }

    private void clearWordCountRates()
    {
        m_contextMatchRate = 0;
        m_segmentTmRate = 0;
        m_lowFuzzyMatchRate = 0;
        m_medFuzzyMatchRate = 0;
        m_medHiFuzzyMatchRate = 0;
        m_hiFuzzyMatchRate = 0;
        m_noMatchRate = 0;
        repetitionRate = 0;
    }

    private void clearUnitRate()
    {
        m_unitRate = 0;
    }

    public static Integer[] getRateTypes()
    {
        return m_allRateTypes;
    }

    /**
     * Default constructor - used by TOPLink to populate object from database
     * contents.
     */
    public Rate()
    {
        super();
    }

    /**
     * Constructor.
     */
    public Rate(String p_name, Currency p_currency, Integer p_type,
            LocalePair p_localePair, Activity p_activity)
    {
        super();
        setName(p_name);
        setCurrency(p_currency);
        setRateType(p_type);
        setLocalePair(p_localePair);
        setActivity(p_activity);
    }

    /**
     * Constructor used to set all the data members. Note that this constructor
     * should be used only for non-word count rate type
     */
    public Rate(String p_name, Currency p_currency, Integer p_type,
            LocalePair p_localePair, Activity p_activity, float p_rate)
    {
        super();
        setName(p_name);
        setCurrency(p_currency);
        setRateType(p_type);
        setLocalePair(p_localePair);
        setActivity(p_activity);
        if (p_type != UnitOfWork.WORD_COUNT)
        {
            setUnitRate(p_rate);
        }
    }

    /**
     * Constructor used to set all the data members. Note that this constructor
     * should be used only for word count rate type
     */
    public Rate(String p_name, Currency p_currency, LocalePair p_localePair,
            Activity p_activity, float p_contextMatchRate,
            float p_segmentTmRate, float p_lowFuzzyMatchRate,
            float p_medFuzzyMatchRate, float p_medHiFuzzyMatchRate,
            float p_hiFuzzyMatchRate, float p_noMatch, float p_repetition)
    {
        super();
        setName(p_name);
        setCurrency(p_currency);
        setRateType(UnitOfWork.WORD_COUNT);
        setLocalePair(p_localePair);
        setActivity(p_activity);
        m_contextMatchRate = p_contextMatchRate;
        m_segmentTmRate = p_segmentTmRate;
        m_lowFuzzyMatchRate = p_lowFuzzyMatchRate;
        m_medFuzzyMatchRate = p_medFuzzyMatchRate;
        m_medHiFuzzyMatchRate = p_medHiFuzzyMatchRate;
        m_hiFuzzyMatchRate = p_hiFuzzyMatchRate;
        setNoMatchRate(p_noMatch);
        setRepetitionRate(p_repetition);
    }

    public float getInContextMatchRate() {
        return inContextMatchRate;
    }

    public void setInContextMatchRate(float inContextMatchRate) {
        this.inContextMatchRate = inContextMatchRate;
    }

    public Integer getRateType()
    {
        return m_type;
    }

    public void setRateType(Integer p_type)
    {
        m_type = p_type;
        if (p_type.equals(UnitOfWork.WORD_COUNT))
        {
            clearUnitRate();
        }
        else
        {
            clearWordCountRates();
        }
    }

    public Currency getCurrency()
    {
        return m_currency;
    }

    public void setCurrency(Currency p_currency)
    {
        m_currency = p_currency;
    }

    public LocalePair getLocalePair()
    {
        return m_localePair;
    }

    public void setLocalePair(LocalePair p_localePair)
    {
        m_localePair = p_localePair;
    }

    public Activity getActivity()
    {
        return m_activity;
    }

    public void setActivity(Activity p_activity)
    {
        m_activity = p_activity;
    }

    public float getUnitRate()
    {
        return m_unitRate;
    }

    public void setUnitRate(float p_rate)
    {
        m_unitRate = p_rate;
    }

    /**
     * Get the unit rate, but in the pivot currency.
     */
    public float getUnitPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return m_unitRate / getCurrency().getConversionFactor();
    }

    // ////////////////////////////////////////////////////////////////////
    // Exact Match for WORD-COUNT type only
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the exact context match rate (for WORD-COUNT type only).
     */
    public float getContextMatchRate()
    {
        return m_contextMatchRate;
    }

    /**
     * Set the context match rate to be the specified value.
     * 
     * @param p_contextMatchRate
     *            The exact page tm rate to be set.
     */
    public void setContextMatchRate(float p_contextMatchRate)
    {
        m_contextMatchRate = p_contextMatchRate;
    }

    /**
     * Get the context match rate, but in the pivot currency.
     */
    public float getContextMatchPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return m_contextMatchRate / getCurrency().getConversionFactor();
    }

    /**
     * Get the exact segment tm rate (for WORD-COUNT type only).
     */
    public float getSegmentTmRate()
    {
        return m_segmentTmRate;
    }

    /**
     * Set the segment tm rate to be the specified value.
     * 
     * @param p_segmentTmRate
     *            The exact segment tm rate to be set.
     */
    public void setSegmentTmRate(float p_segmentTmRate)
    {
        m_segmentTmRate = p_segmentTmRate;
    }

    /**
     * Get the segment tm rate, but in the pivot currency.
     */
    public float getSegmentTmPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return m_segmentTmRate / getCurrency().getConversionFactor();
    }

    // ////////////////////////////////////////////////////////////////////
    // Fuzzy Match category for WORD-COUNT type only
    // ////////////////////////////////////////////////////////////////////
    public float getLowFuzzyMatchRate()
    {
        return m_lowFuzzyMatchRate;
    }

    public void setLowFuzzyMatchRate(float p_lowFuzzyMatchRate)
    {
        m_lowFuzzyMatchRate = p_lowFuzzyMatchRate;
    }

    /**
     * Get the low fuzzy match rate, but in the pivot currency.
     */
    public float getLowFuzzyMatchPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return m_lowFuzzyMatchRate / getCurrency().getConversionFactor();
    }

    public float getMedFuzzyMatchRate()
    {
        return m_medFuzzyMatchRate;
    }

    public void setMedFuzzyMatchRate(float p_medFuzzyMatchRate)
    {
        m_medFuzzyMatchRate = p_medFuzzyMatchRate;
    }

    /**
     * Get the MED fuzzy match rate, but in the pivot currency.
     */
    public float getMedFuzzyMatchPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return m_medFuzzyMatchRate / getCurrency().getConversionFactor();
    }

    public float getMedHiFuzzyMatchRate()
    {
        return m_medHiFuzzyMatchRate;
    }

    public void setMedHiFuzzyMatchRate(float p_medHiFuzzyMatchRate)
    {
        m_medHiFuzzyMatchRate = p_medHiFuzzyMatchRate;
    }

    /**
     * Get the MED-HI fuzzy match rate, but in the pivot currency.
     */
    public float getMedHiFuzzyMatchPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return m_medHiFuzzyMatchRate / getCurrency().getConversionFactor();
    }

    public float getHiFuzzyMatchRate()
    {
        return m_hiFuzzyMatchRate;
    }

    public void setHiFuzzyMatchRate(float p_hiFuzzyMatchRate)
    {
        m_hiFuzzyMatchRate = p_hiFuzzyMatchRate;
    }

    /**
     * Get the HI fuzzy match rate, but in the pivot currency.
     */
    public float getHiFuzzyMatchPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return m_hiFuzzyMatchRate / getCurrency().getConversionFactor();
    }

    public float getNoMatchRate()
    {
        return m_noMatchRate;
    }

    public void setNoMatchRate(float p_noMatchRate)
    {
        m_noMatchRate = p_noMatchRate;
    }

    /**
     * Get the no match rate, but in the pivot currency.
     */
    public float getNoMatchPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return m_noMatchRate / getCurrency().getConversionFactor();
    }

    public float getRepetitionRate()
    {
        return repetitionRate;
    }

    public void setRepetitionRate(float repetitionRate)
    {
        this.repetitionRate = repetitionRate;
    }

    public float getRepetitionRatePer()
    {
        return repetitionRatePer;
    }

    public void setRepetitionRatePer(float repetitionRatePer)
    {
        this.repetitionRatePer = repetitionRatePer;
    }

    /**
     * Get the repetition rate, but in the pivot currency.
     */
    public float getRepetitionPivotCurrencyRate()
    {
        // tbd
        // need to use with remainder - i assume float
        // divided by float will be fine
        return repetitionRate / getCurrency().getConversionFactor();
    }

    /**
     * Create an amount of work object that matches the type of this rate. If
     * this type shouldn't have an amount of work (i.e. FIXED) then it'll return
     * NULL.
     */
    public AmountOfWork createAmountOfWork()
    {
        AmountOfWork work = null;
        // if a valid unit of work to need an AmountOfWork objet
        if (getRateType().equals(Rate.UnitOfWork.HOURLY)
                || getRateType().equals(Rate.UnitOfWork.PAGE_COUNT))
        {
            work = new AmountOfWork(getRateType());
        }
        return work;
    }

    /**
     * Override the toString method
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Rate: ");
        sb.append("m_name=");
        sb.append(getName());
        sb.append(", m_currency=");
        sb.append(m_currency.getIsoCode());
        sb.append(", m_type=");
        sb.append(m_type);
        sb.append(", m_activity=");
        sb.append(m_activity.getName());
        sb.append(", m_localePair=");
        sb.append(m_localePair.toString());
        if (m_type.equals(UnitOfWork.WORD_COUNT))
        {
            sb.append(", m_contextMatchRate=");
            sb.append(m_contextMatchRate);
            sb.append(", m_segmentTmRate=");
            sb.append(m_segmentTmRate);
            sb.append(", m_lowFuzzyMatchRate=");
            sb.append(m_lowFuzzyMatchRate);
            sb.append(", m_medFuzzyMatchRate=");
            sb.append(m_medFuzzyMatchRate);
            sb.append(", m_medHiFuzzyMatchRate=");
            sb.append(m_medHiFuzzyMatchRate);
            sb.append(", m_hiFuzzyMatchRate=");
            sb.append(m_hiFuzzyMatchRate);
            sb.append(", m_noMatchRate=");
            sb.append(m_noMatchRate);
            sb.append(", repetitionRate=");
            sb.append(repetitionRate);

        }
        else
        {
            sb.append(", m_unitRate=");
            sb.append(m_unitRate);
        }
        return sb.toString();
    }

    public String getType()
    {
        String type = null;
        if (m_type != null)
        {
            type = (String) TYPE_MAY.get(m_type);
        }

        return type;
    }

    public void setType(String p_type)
    {
        Integer type = null;
        if (p_type != null)
        {
            Set keys = TYPE_MAY.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext())
            {
                Integer key = (Integer) iterator.next();
                String value = (String) TYPE_MAY.get(key);
                if (p_type.equalsIgnoreCase(value))
                {
                    type = key;
                    break;
                }
            }
        }

        this.m_type = type;
    }

	public float getContextMatchRatePer() {
		return contextMatchRatePer;
	}

	public void setContextMatchRatePer(float contextMatchRatePer) {
		this.contextMatchRatePer = contextMatchRatePer;
	}

	public float getSegmentTmRatePer() {
		return segmentTmRatePer;
	}

	public void setSegmentTmRatePer(float segmentTmRatePer) {
		this.segmentTmRatePer = segmentTmRatePer;
	}

	public float getHiFuzzyMatchRatePer() {
		return hiFuzzyMatchRatePer;
	}

	public void setHiFuzzyMatchRatePer(float hiFuzzyMatchRatePer) {
		this.hiFuzzyMatchRatePer = hiFuzzyMatchRatePer;
	}
 
	public float getMedHiFuzzyMatchRatePer() {
		return medHiFuzzyMatchRatePer;
	}

	public void setMedHiFuzzyMatchRatePer(float medHiFuzzyMatchRatePer) {
		this.medHiFuzzyMatchRatePer = medHiFuzzyMatchRatePer;
	}

	public float getMedFuzzyMatchRatePer() {
		return medFuzzyMatchRatePer;
	}

	public void setMedFuzzyMatchRatePer(float medFuzzyMatchRatePer) {
		this.medFuzzyMatchRatePer = medFuzzyMatchRatePer;
	}

	public float getLowFuzzyMatchRatePer() {
		return lowFuzzyMatchRatePer;
	}

	public void setLowFuzzyMatchRatePer(float lowFuzzyMatchRatePer) {
		this.lowFuzzyMatchRatePer = lowFuzzyMatchRatePer;
	}

    
    
}
