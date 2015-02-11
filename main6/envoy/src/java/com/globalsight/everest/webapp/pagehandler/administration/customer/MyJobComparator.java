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
* This class can be used to compare MyJob objects
*/
public class MyJobComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;
	public static final int SRC_LOCALE = 1;
	public static final int TARG_LOCALE = 2;
	public static final int WORD_CNT = 3;
	public static final int CREATE_DATE = 4;
	public static final int PLANNED_DATE = 5;

	public MyJobComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two Project objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		MyJob a = (MyJob) p_A;
		MyJob b = (MyJob) p_B;

		String aValue = null;
		String bValue = null;
		int rv;

		switch (m_type)
		{
		default:
		case NAME:
			aValue = a.getJobName();
			bValue = b.getJobName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case SRC_LOCALE:
			GlobalSightLocale aLocale = a.getSourceLocale();
			GlobalSightLocale bLocale = b.getSourceLocale();
			rv = this.compareStrings(aLocale.toString(),bLocale.toString());
			break;
		case TARG_LOCALE:
			aLocale = a.getTargetLocale();
			bLocale = b.getTargetLocale();
			rv = this.compareStrings(aLocale.toString(),bLocale.toString());
			break;
		case WORD_CNT:
			int aInt = a.getWordCount();
			int bInt = b.getWordCount();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
			break;
		case CREATE_DATE:
			Date aDate = a.getCreateDate();
			Date bDate = b.getCreateDate();
            if (aDate.after(bDate))
                rv = 1;
            else if (aDate.equals(bDate))
                rv = 0;
            else
                rv = -1;
			break;
		case PLANNED_DATE:
			aDate = a.getPlannedDate();
			bDate = b.getPlannedDate();
            if (aDate.after(bDate))
                rv = 1;
            else if (aDate.equals(bDate))
                rv = 0;
            else
                rv = -1;
			break;
		}
		return rv;
	}
}
