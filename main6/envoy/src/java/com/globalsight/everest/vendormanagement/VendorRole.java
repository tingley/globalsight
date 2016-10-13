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
package com.globalsight.everest.vendormanagement;

// globalsight
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.workflow.Activity;

/**
 * This class represents one role that a vendor may have. A role is a
 * source/target locale pair along with an activity that the vendor can perform
 * within the locale pair. (i.e., Translate from English to French)
 */
public class VendorRole extends PersistentObject
{

	private static final long serialVersionUID = 1L;

	// for TOPLink query purposes
	public final static String M_LOCALE_PAIR = "m_localePair";

	public final static String M_RATE = "m_rate";

	public final static String M_ACTIVITY = "m_activity";

	private Activity m_activity = null;

	private LocalePair m_localePair = null;

	private Rate m_rate = null;

	private Vendor m_vendor = null; // back pointer for TOPLink

	public VendorRole()
	{

	}

	public VendorRole(Activity p_act, LocalePair p_lp)
	{
		m_activity = p_act;
		m_localePair = p_lp;
	}

	public VendorRole(Activity p_act, LocalePair p_lp, Rate p_rate)
	{
		m_activity = p_act;
		m_localePair = p_lp;
		m_rate = p_rate;
	}

	/**
	 * Return the activity of the role.
	 */
	public Activity getActivity()
	{
		return m_activity;
	}

	/**
	 * Set the activity of the role.
	 */
	public void setActivity(Activity p_activity)
	{
		m_activity = p_activity;
	}

	/**
	 * Get the locale pair of the role.
	 */
	public LocalePair getLocalePair()
	{
		return m_localePair;
	}

	/**
	 * Set the locale pair of the role.
	 */
	public void setLocalePair(LocalePair p_lp)
	{
		m_localePair = p_lp;
	}

	/**
	 * Get the rate that is associated with this role. Returns NULL if there
	 * isn't a rate.
	 */
	public Rate getRate()
	{
		return m_rate;
	}

	/**
	 * Sets the rate assigned to this role. If NULL is specified then it is
	 * clearing out the rate.
	 */
	public void setRate(Rate p_rate)
	{
		m_rate = p_rate;
	}

	/**
	 * 
	 */
	public boolean equals(Object p_vendorRole)
	{
		// doesn't check the ID, instead checks the contents

		VendorRole vr = (VendorRole) p_vendorRole;
		if (vr.getRate().equals(this.getRate())
				&& vr.getActivity().equals(this.getActivity())
				&& vr.getLocalePair().equals(this.getLocalePair()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public String toString()
	{
		StringBuffer dString = new StringBuffer();
		dString.append(super.toString());
		dString.append("m_activity=");
		dString.append(m_activity != null ? m_activity.toString() : "null");
		dString.append(", m_localePair=");
		dString.append(m_localePair != null ? m_localePair.toString() : "null");
		dString.append(", m_rate=");
		dString.append(m_rate != null ? m_rate.toString() : "null");
		dString.append(", m_vendor=");
		dString.append(m_vendor != null ? Long.toString(m_vendor.getId())
				: "null");
		return dString.toString();
	}

	void setVendor(Vendor p_vendor)
	{
		m_vendor = p_vendor;
	}

	public Vendor getVendor()
	{
		return m_vendor;
	}
}
