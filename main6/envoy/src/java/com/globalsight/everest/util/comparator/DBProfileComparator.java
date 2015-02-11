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
import com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl;
import java.util.Locale;

/**
* This class can be used to compare DBConnectionImpl objects
*/
public class DBProfileComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;
	public static final int DESC = 1;
	public static final int ID = 2;

	public DBProfileComparator(int p_type,Locale p_locale)
	{
	    super(p_type, p_locale);
	}

	public DBProfileComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two Tm objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		DatabaseProfileImpl a = (DatabaseProfileImpl) p_A;
		DatabaseProfileImpl b = (DatabaseProfileImpl) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		default:
		case NAME:
			aValue = a.getName();
			bValue = b.getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case DESC:
			aValue = a.getDescription();
			bValue = b.getDescription();
			rv = this.compareStrings(aValue,bValue);
			break;
		case ID:
			long along = a.getId();
			long blong = b.getId();
            if (along > blong)
               rv = 1;
            else if (along == blong)
               rv = 0;
            else
               rv = -1;
			break;
		}
		return rv;
	}
}
