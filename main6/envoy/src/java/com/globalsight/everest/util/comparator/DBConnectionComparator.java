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
import com.globalsight.cxe.entity.dbconnection.DBConnectionImpl;
import java.util.Locale;

/**
* This class can be used to compare DBConnectionImpl objects
*/
public class DBConnectionComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;
	public static final int DRIVER = 1;
	public static final int CONNECTION = 2;
	public static final int USERNAME = 3;

	public DBConnectionComparator(int p_type,Locale p_locale)
	{
	    super(p_type, p_locale);
	}

	public DBConnectionComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two Tm objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		DBConnectionImpl a = (DBConnectionImpl) p_A;
		DBConnectionImpl b = (DBConnectionImpl) p_B;

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
		case DRIVER:
			aValue = a.getDriver();
			bValue = b.getDriver();
			rv = this.compareStrings(aValue,bValue);
			break;
		case CONNECTION:
			aValue = a.getConnection();
			bValue = b.getConnection();
			rv = this.compareStrings(aValue,bValue);
			break;
		case USERNAME:
			aValue = a.getUserName();
			bValue = b.getUserName();
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
