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

import java.text.NumberFormat;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * Represents money. It stores the amount and the denomination or currency the
 * amount is specified in.
 */
public class Money extends PersistentObject
{
    private static final long serialVersionUID = -4279854354179052171L;

    private float m_amount;
    private Currency m_currency;

    /**
     * Default constructor
     */
    public Money()
    {
    }

    /**
     * Constructor - Sets the amount to be the one specified. Sets the currency
     * to be the system-wide pivot currency
     */
    public Money(float p_amount)
    {
        super();
        m_amount = p_amount;
        String companyId = CompanyThreadLocal.getInstance().getValue();
        m_currency = (Currency) Currency.getPivotCurrencies().get(
                Long.parseLong(companyId));
    }

    /**
     * Constructor - to set the amount and currency.
     */
    public Money(float p_amount, Currency p_currency)
    {
        super();
        m_amount = p_amount;
        // set to pivot/system currency if not passed in
        if (p_currency == null)
        {
            String companyId = CompanyThreadLocal.getInstance().getValue();
            m_currency = (Currency) Currency.getPivotCurrencies().get(
                    Long.parseLong(companyId));
        }
        else
        {
            m_currency = p_currency;
        }
    }

    /**
     * Return the amount as a formatted amount.
     */
    public String getFormattedAmount()
    {
        NumberFormat cf = CurrencyFormat.getCurrencyFormat(m_currency);
        return cf.format(m_amount);
    }

    /**
     * Return the straight amount with all decimals, no formatting.
     * 
     */
    public float getAmount()
    {
        return m_amount;
    }

    /**
     * Set the amount.
     */
    public void setAmount(float p_amount)
    {
        m_amount = p_amount;
    }

    /**
     * Return the currency the money is in.
     */
    public Currency getCurrency()
    {
        return m_currency;
    }

    /**
     * Set the currency the money is in.
     */
    public void setCurrency(Currency p_currency)
    {
        m_currency = p_currency;
    }

    /**
     * Returns a new amount of money accoding to how many times this money
     * should be multipled by.
     */
    public Money multiply(float p_number)
    {
        return new Money(p_number * m_amount, getCurrency());
    }

    /**
     * Returns a new amount of money (adding the two together).
     */
    public Money add(Money p_m)
    {
        return new Money(p_m.getAmount() + getAmount(), getCurrency());
    }

    /**
	 * 
	 */
    public String toString()
    {
        return new String(getFormattedAmount() + " " + m_currency.toString());
    }

    /**
     * Formatting method to be used till NumberFormat used with currency and
     * locale. Rounds to 2 digits after decimal
     */
    public static float roundOff(float p_value)
    {
        long val = Math.round(p_value * 100); // cents
        return val / 100.0f;
    }

}
