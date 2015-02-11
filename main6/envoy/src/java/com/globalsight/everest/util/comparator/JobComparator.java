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
import com.globalsight.everest.jobhandler.Job;
import java.util.Locale;
import java.util.Date;

/**
* This class can be used to compare Job objects
*/
public class JobComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;

	/**
	* Creates a JobComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by name
	*/
	public JobComparator(int p_type,Locale p_locale)
	{
	    super(p_type, p_locale);
	}

	public JobComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two Job objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		Job a = (Job) p_A;
		Job b = (Job) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		case NAME:
			aValue = a.getJobName();
			bValue = b.getJobName();
			rv = this.compareStrings(aValue,bValue);
                        if (rv == 0)
                        {
                            //compare by dates since the names are the same
                            Date aDate = a.getCreateDate();
                            Date bDate = b.getCreateDate();
                            if (aDate.after(bDate))
                                rv = 1;
                            else if (aDate.equals(bDate))
                                rv = 0;
                            else
                                rv = -1;
                        }
    			break;
		default:
			aValue = a.getJobName();
			bValue = b.getJobName();
			rv = this.compareStrings(aValue,bValue);
			break;
		}

		return rv;
	}
}
