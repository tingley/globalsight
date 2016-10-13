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

import java.util.Comparator;
import java.util.Locale;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.foundation.Role;

/**
* This class can be used to compare Role objects
*/
public class RoleComparator extends StringComparator
{
	//types of Role comparison
	public static final int ACTIVITY = 0;
	public static final int SRC = 1;
	public static final int TARG = 2;


	/**
	* Creates a RoleComparator with the given locale.
	*/
	public RoleComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two User objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		Role a = (Role) p_A;
		Role b = (Role) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		default:
		case ACTIVITY:
			aValue = a.getActivity().getName();
			bValue = b.getActivity().getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case SRC:
            String language = a.getSourceLocale().substring(0,2);
            String country  = a.getSourceLocale().substring(3,5);
            GlobalSightLocale locale =
                               new GlobalSightLocale(language, country, false);
			aValue = locale.getDisplayName(m_locale);
            language = b.getSourceLocale().substring(0,2);
            country  = b.getSourceLocale().substring(3,5);
            locale = new GlobalSightLocale(language, country, false);
			bValue = locale.getDisplayName(m_locale);
			rv = this.compareStrings(aValue,bValue);
			break;
		case TARG:
            language = a.getTargetLocale().substring(0,2);
            country  = a.getTargetLocale().substring(3,5);
            locale = new GlobalSightLocale(language, country, false);
			aValue = locale.getDisplayName(m_locale);
            language = b.getTargetLocale().substring(0,2);
            country  = b.getTargetLocale().substring(3,5);
            locale = new GlobalSightLocale(language, country, false);
			bValue = locale.getDisplayName(m_locale);
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
