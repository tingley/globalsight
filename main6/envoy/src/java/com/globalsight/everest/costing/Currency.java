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
import java.util.Locale;
import java.util.Map;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Represents a currency object. The conversion factor is the conversion from
 * the pivot currency which is a static data member. The pivot currency will be
 * a Currency object with a conversion factor of 1.
 */
public class Currency extends PersistentObject
{
    private static final long serialVersionUID = -7476943230355084901L;

    // used for TOPLink querying
    public static final String ISO_CURRENCY = "m_isoCurrency";

    // private data members

    // the default currency - the one all other currencies
    // base their conversion factor on
    static private Map<Object, Currency> m_pivotCurrencies = new HashMap<Object, Currency>();

    private IsoCurrency m_isoCurrency = null; // contains name and 3 character
                                              // iso code
    private float m_conversionFactor;
    private long m_companyId;

    /**
     * Get the default currency. This is at the package level - other components
     * can get it through the costing engine.
     */
    static Map getPivotCurrencies()
    {
        if (m_pivotCurrencies.size() == 0)
        {
            Currency c = null;
            try
            {
                Long companyId = Long.parseLong(CompanyThreadLocal
                        .getInstance().getValue());
                c = (Currency) HibernateUtil.getFirst(
                        "from Currency c where c.companyId = ?", companyId);
            }
            catch (java.lang.NumberFormatException e)
            {
                c = HibernateUtil.get(Currency.class, 1);
            }
            if (c != null)
            {
                m_pivotCurrencies.put(c.getCompanyId(), c);
            }
        }

        return m_pivotCurrencies;
    }

    /**
     * Set the default currency. Assumes it isn't NULL. The caller
     * (CostingEngine) needs to test this before setting it. This is only at the
     * package level - no other components can set it.
     */
    static void setPivotCurrencies(Map p_pivotCurrencies)
    {
        m_pivotCurrencies = p_pivotCurrencies;
    }

    /**
     * Default constructor
     */
    public Currency()
    {
        super();
        m_conversionFactor = 1;
    }

    public Currency(float m_conversionFactor)
    {
        this.m_conversionFactor = m_conversionFactor;
    }

    /**
     * Constructor used to set all the data members.
     */
    public Currency(IsoCurrency p_isoCur, float p_conversionFactor,
            long p_companyId)
    {
        m_isoCurrency = p_isoCur;
        setConversionFactor(p_conversionFactor);
        m_companyId = p_companyId;
    }

    public String getIsoCode()
    {
        return m_isoCurrency.getCode();
    }

    public long getCompanyId()
    {
        return m_companyId;
    }

    public void setCompanyId(long p_companyId)
    {
        m_companyId = p_companyId;
    }

    public float getConversionFactor()
    {
        return m_conversionFactor;
    }

    public void setConversionFactor(float p_conversion)
    {
        m_conversionFactor = roundOff(p_conversion);
    }

    public IsoCurrency getIsoCurrency()
    {
        return m_isoCurrency;
    }

    public void setIsoCurrency(IsoCurrency isoCurrency)
    {
        m_isoCurrency = isoCurrency;
    }

    /**
     * Override the getName method.
     */
    public String getName()
    {
        return m_isoCurrency.getName();
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("Currency: ");
        sb.append("m_id=");
        sb.append(getId());
        sb.append(", m_isoCurrency=");
        sb.append(m_isoCurrency.toString());
        sb.append(", m_conversionFactor=");
        sb.append(m_conversionFactor);
        sb.append(", m_companyId=");
        sb.append(m_companyId);
        return sb.toString();
    }

    public boolean equals(Object p_currency)
    {
        Currency cur2 = (Currency) p_currency;
        boolean isEqual = false;
        if (getName().equals(cur2.getName()))
        {
            if (getIsoCode().equals(cur2.getIsoCode())
                    && m_conversionFactor == cur2.getConversionFactor()
                    && m_companyId == cur2.getCompanyId())
            {
                isEqual = true;
            }
        }
        return isEqual;
    }

    public String getDisplayName()
    {
        return m_isoCurrency.getDisplayName();
    }

    public String getDisplayName(Locale loc)
    {
        return m_isoCurrency.getDisplayName(loc);
    }

    public static void addPivotCurdrency(Currency p_pivotCurrency)
    {
        m_pivotCurrencies.put(p_pivotCurrency.getCompanyId(), p_pivotCurrency);
    }

    /**
     * Rounds to 3 digits after decimal
     */
    private static float roundOff(float p_value)
    {
        long val = Math.round(p_value * 1000);
        return val / 1000.0f;
    }
}
