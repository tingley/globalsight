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
 * Specifies a surcharge that is a percentage of the cost.
 */
public class PercentageSurcharge extends Surcharge
{
	private static final long serialVersionUID = -2749959715624275334L;

	/*
	 * Percentage is stored as a decimal value. So 5% = .05, 100% = 1.00, 8.2% =
	 * .082
	 */
	private float m_percentage = 0;

	/**
	 * Constructor to be used by TOPLink only.
	 */
	public PercentageSurcharge()
	{
	}

	/**
	 * Constructor The percentage should be specified as a decimal value. So 5% =
	 * .05, 100% = 1.00, 8.2% = .082
	 */
	public PercentageSurcharge(float p_percentage)
	{
		m_percentage = p_percentage;
	}

	/**
	 * Returns the type of surcharge in string format. This is from the static
	 * strings specified in the superclass.
	 */
	public String getType()
	{
		return PERCENTAGE;
	}

	public String getTypeCode()
	{
		return TYPE_PERCENTAGE;
	}

	/**
	 * Return the amount of surcharge to add to the cost according to the cost
	 * passed in. This just returns the surcharge amount and NOT the p_cost plus
	 * the surcharge.
	 */
	public Money surchargeAmount(Money p_cost)
	{
		return p_cost.multiply(m_percentage);
	}

	/**
	 * Returns the percentage.
	 */
	public float getPercentage()
	{
		return m_percentage;
	}

	/**
	 * Reset the percentage.
	 */
	public void setPercentage(float p_percentage)
	{
		m_percentage = p_percentage;
	}
}
