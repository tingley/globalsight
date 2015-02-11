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
import com.globalsight.everest.vendormanagement.Rating;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.StringComparator;
import java.util.Date;
import java.util.Locale;

/**
* This class can be used to compare Rating objects
*/
public class RatingComparator extends StringComparator
{
	//types of comparison
	public static final int RATING = 0;
	public static final int ACTIVITY = 1;
	public static final int DATE = 2;
	public static final int COMMENT = 3;
	public static final int RATER = 4;
	public static final int JOBNAME = 5;
	public static final int JOBID = 6;
	public static final int SRCLOCALE = 7;
	public static final int TARGLOCALE = 8;

	/**
	* Creates a RatingComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public RatingComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two Rating objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		Rating a = (Rating) p_A;
		Rating b = (Rating) p_B;

		String aValue = null;
		String bValue = null;
		int rv;

		switch (m_type)
		{
		default:
		case RATING:
			int aInt = a.getValue();
			int bInt = b.getValue();
            if (aInt > bInt)
                rv = 1;
            else if (aInt == bInt)
                rv = 0;
            else
                rv = -1;
			break;
		case ACTIVITY:
			Task aTask = a.getTask();
            if (aTask != null)
                aValue = aTask.getTaskName();
			Task bTask = b.getTask();
            if (bTask != null)
                bValue = bTask.getTaskName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case DATE:
			Date aDate = a.getModifiedDate();
			Date bDate = b.getModifiedDate();
            if (aDate.after(bDate))
                rv = 1;
            else if (aDate.equals(bDate))
                rv = 0;
            else
                rv = -1;
			break;
		case COMMENT:
			aValue = a.getComment();
			bValue = b.getComment();
			rv = this.compareStrings(aValue,bValue);
			break;
		case RATER:
			aValue = a.getRaterUserId();
			bValue = b.getRaterUserId();
			rv = this.compareStrings(aValue,bValue);
			break;
		case JOBNAME:
			aTask = a.getTask();
            if (aTask != null)
                aValue = aTask.getJobName();
			bTask = b.getTask();
            if (bTask != null)
                bValue = bTask.getJobName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case JOBID:
			aTask = a.getTask();
            if (aTask != null)
                aValue = String.valueOf(aTask.getJobId());
			bTask = b.getTask();
            if (bTask != null)
                bValue = String.valueOf(bTask.getJobId());
			rv = this.compareStrings(aValue,bValue);
			break;
		case SRCLOCALE:
			aTask = a.getTask();
            if (aTask != null)
                aValue = aTask.getSourceLocale().getDisplayName(getLocale());
			bTask = b.getTask();
            if (bTask != null)
                bValue = bTask.getSourceLocale().getDisplayName(getLocale());
			rv = this.compareStrings(aValue,bValue);
			break;
		case TARGLOCALE:
			aTask = a.getTask();
            if (aTask != null)
                aValue = aTask.getTargetLocale().getDisplayName(getLocale());
			bTask = b.getTask();
            if (bTask != null)
                bValue = bTask.getTargetLocale().getDisplayName(getLocale());
			rv = this.compareStrings(aValue,bValue);
                        break;
		}
		return rv;
	}
}
