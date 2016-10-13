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

/**
 * Specifies a surcharge that is a flat-fee.
 */
public class FlatSurcharge extends Surcharge
{
	private static final long serialVersionUID = -8518547817583865993L;

	// stores the amount as a Money object (amount + currency)
	private Currency currency;
	private float amount;

	/**
	 * Default constructor.
	 */
	public FlatSurcharge()
	{
	}

	/**
	 * Constructor that sets the flat fee
	 * 
	 * @param p_currency -
	 *            The currency the amount is specified in.
	 * @param p_amount -
	 *            The amount of the flat-fee.
	 */
	public FlatSurcharge(Currency p_currency, float p_amount)
	{
		setAmount(p_amount, p_currency);
	}

	/**
	 * Constructor that sets the flat fee using the Money object.
	 * 
	 * @param p_amount -
	 *            The flat fee.
	 */
	public FlatSurcharge(Money p_amount)
	{
		setAmount(p_amount);
	}

	/**
	 * Returns the type of surcharge in string format. This is from the static
	 * strings specified in the superclass.
	 */
	public String getType()
	{
		return FLAT_FEE;
	}
    
    public String getTypeCode()
    {
    	return TYPE_FLAT;
    }

	/**
	 * Return the flat-fee surcharge amount.
	 * 
	 * @param p_cost -
	 *            This parameter will be ignored, so it can be NULL A flat-fee
	 *            surcharge is always the same no matter the cost.
	 * @return The amount of surcharge to add to the cost.
	 */
	public Money surchargeAmount(Money p_cost)
	{
		return getAmount();
	}

	/**
	 * Return the flat-fee amount.
	 */
	public Money getAmount()
	{
		return new Money(amount, currency);
	}

	/**
	 * Set/reset the flat-fee amount using the Money object.
	 */
	public void setAmount(Money p_amount)
	{
		amount = p_amount.getAmount();
		currency = p_amount.getCurrency();
	}

	/**
	 * Set/reset the flat-fee object.
	 */
	public void setAmount(float p_amount, Currency p_currency)
	{
		amount = p_amount;
		currency = p_currency;
	}
}
