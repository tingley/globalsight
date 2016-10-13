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
package com.globalsight.everest.util.comparator;    

import com.globalsight.everest.vendormanagement.VendorRole;
import com.globalsight.everest.costing.Rate;

import java.util.Comparator;
import java.util.Locale;

/**
* This class can be used to compare VendorRole objects
*/
public class VendorRoleComparator extends StringComparator
{
	//types of comparison
	public static final int ACTIVITY = 0;
	public static final int SRC = 1;
	public static final int TARG = 2;
	public static final int CURRENCY = 3;
	public static final int RATE_TYPE = 4;

	/**
	* Creates a VendorRoleComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public VendorRoleComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two VendorRole objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		VendorRole a = (VendorRole) p_A;
		VendorRole b = (VendorRole) p_B;

		String aValue = "";
		String bValue = "";
		int rv;

		switch (m_type)
		{
		case ACTIVITY:
			aValue = a.getActivity().getName();
			bValue = b.getActivity().getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case SRC:
			aValue = a.getLocalePair().getSource().getDisplayName(getLocale());
			bValue = b.getLocalePair().getSource().getDisplayName(getLocale());
			rv = this.compareStrings(aValue,bValue);
			break;
		case TARG:
			aValue = a.getLocalePair().getTarget().getDisplayName(getLocale());
			bValue = b.getLocalePair().getTarget().getDisplayName(getLocale());
			rv = this.compareStrings(aValue,bValue);
			break;
		case CURRENCY:
            Rate ar = a.getRate();
            if (ar != null) 
                aValue = ar.getCurrency().getDisplayName();
            Rate br = b.getRate();
            if (br != null) 
                bValue = ar.getCurrency().getDisplayName();
			rv = this.compareStrings(aValue,bValue);
	         break;
		case RATE_TYPE:
            ar = a.getRate();
            aValue = getRateType(ar);
            br = b.getRate();
            bValue = getRateType(br);
			rv = this.compareStrings(aValue,bValue);
			break;
		default:
			aValue = a.getActivity().getName();
			bValue = b.getActivity().getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}

    private String getRateType(Rate rate)
    {
        String typeStr = "";
        if (rate != null) 
        {
            Integer type = rate.getRateType();
            switch (type.intValue()) 
            {
                case 1: 
                    typeStr = "Fixed";
                    break;
                case 2: 
                    typeStr = "Hourly";
                    break;
                case 3: 
                    typeStr = "Page";
                    break;
                case 4: 
                    typeStr = "Word Count";
                    break;
            }
        }
        return typeStr;
    }
}
