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
package com.globalsight.everest.webapp.pagehandler.administration.vendors;

import java.util.Comparator;
import com.globalsight.everest.vendormanagement.VendorInfo;
import com.globalsight.everest.util.comparator.StringComparator;
import java.util.Locale;

/**
* This class can be used to compare VendorInfo objects
*/
public class VendorComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;
	public static final int COMPANYNAME = 1;
	public static final int ALIAS = 2;

	/**
	* Creates a VendorComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public VendorComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two VendorRole objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		VendorInfo a = (VendorInfo) p_A;
		VendorInfo b = (VendorInfo) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
                default:
		case NAME:
			aValue = a.getFullName();
			bValue = b.getFullName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case COMPANYNAME:
			aValue = a.getCompanyName();
			bValue = b.getCompanyName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case ALIAS:
			aValue = a.getPseudonym();
			bValue = b.getPseudonym();
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
