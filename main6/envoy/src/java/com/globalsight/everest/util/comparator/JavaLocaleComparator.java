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

/**
* This class can be used to compare Locale objects
*/
public class JavaLocaleComparator extends StringComparator
{
	public static final int DISPLAYNAME = 0;


	/**
	* Creates a JavaLocaleComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public JavaLocaleComparator(int p_type, Locale p_locale)
	{
	    super(p_type, p_locale);
	}

	public JavaLocaleComparator(Locale p_locale)
	{
	    super(p_locale);
    }

	/**
	* Performs a comparison of two GlobalSightLocale objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		Locale a = (Locale) p_A;
		Locale b = (Locale) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		default:
		case DISPLAYNAME:
			aValue = a.getDisplayName();
			bValue = b.getDisplayName();
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}


}
