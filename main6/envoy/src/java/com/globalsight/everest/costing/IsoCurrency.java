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

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

// globalsight
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

public class IsoCurrency extends PersistentObject
{
	private static final long serialVersionUID = 4744212144316249260L;

	private static final String PROPERTY_KEY_PRE = "lb_iso_currency_";
	// used for TOPLink querying
	public static final String ISO_CODE = "m_code";

	// the ISO currency code - 3 character
	private String m_code = null;

	// Default constructor
	public IsoCurrency()
	{
	}

	public int compareTo(Object o)
	{
		int result = 0;

		if (o instanceof IsoCurrency)
		{
			result = getName().compareTo(((IsoCurrency) o).getName());
		}
		else
		{
			result = 1;
		}
		return result;
	}

	public boolean equals(Object o)
	{
		boolean result = false;

		if (o instanceof IsoCurrency)
		{
			result = getName().equals(((IsoCurrency) o).getName());
		}
		return result;
	}

	public String getCode()
	{
		return m_code;
	}

	public void setCode(String p_code)
	{
		m_code = p_code;
	}

	public String getDisplayName()
	{
		return getName() + " (" + m_code + ")";
	}
	
	public String getDisplayName(Locale loc)
    {
	    if (m_code == null)
	    {
	        return getDisplayName();
	    }
	    
        SystemResourceBundle srb = SystemResourceBundle.getInstance();
        ResourceBundle rb = srb.getResourceBundle(ResourceBundleConstants.LOCALE_RESOURCE_NAME, loc);
        String key = PROPERTY_KEY_PRE + m_code;
        String result = null;
        try
        {
            result = rb.getString(key);
        }
        catch (MissingResourceException e) 
        {
            result = getDisplayName();
        }
        
        return result;
    }
}
