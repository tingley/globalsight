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
package com.globalsight.everest.webapp.pagehandler.administration.customer;

import java.util.Comparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.util.GlobalSightLocale;
import java.util.Date;
import java.util.Locale;

/**
* This class can be used to compare SourceFile objects
*/
public class SourceFileComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;
	public static final int STATUS = 1;

	public SourceFileComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two SourceFile objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		SourceFile a = (SourceFile) p_A;
		SourceFile b = (SourceFile) p_B;

		String aValue = null;
		String bValue = null;
		int rv;

		switch (m_type)
		{
		default:
		case NAME:
			aValue = a.getName();
			bValue = b.getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case STATUS:
			aValue = a.getStatus();
			bValue = b.getStatus();
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
