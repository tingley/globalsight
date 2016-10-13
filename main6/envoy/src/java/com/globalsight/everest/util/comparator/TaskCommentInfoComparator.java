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
import java.util.Date;
import java.util.Locale;
import com.globalsight.everest.comment.TaskCommentInfo;

/**
* This class can be used to compare TaskCommentInfo objects
*/
public class TaskCommentInfoComparator extends StringComparator
{
	//types of Comment comparison
	public static final int DATE = 0;
	public static final int LOCALE = 1;
	public static final int USER = 2;
	public static final int TASK = 3;


	/**
	* Creates a TaskCommentInfoComparator with the given locale.
	*/
	public TaskCommentInfoComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	 * Performs a comparison of two TaskCommentInfo objects by their
         * create date.  
	 */
	public int compare(java.lang.Object p_A, java.lang.Object p_B) 
        {
	    TaskCommentInfo a = (TaskCommentInfo) p_A;
	    TaskCommentInfo b = (TaskCommentInfo) p_B;

	    int rv;
            switch (m_type)
	    {
        default:
		case DATE:
		    Date aDate = a.getCreatedDateAsDate();
		    Date bDate = b.getCreatedDateAsDate();
                    if (aDate.after(bDate))
                    {
                        rv = 1;
                    }
                else if (aDate.equals(bDate))
                {
                    rv = 0;
                }
                else
                {
                    rv = -1;
                }
                break;
		case LOCALE:
            String aVal = a.getTargetLocale().getDisplayName();
            String bVal = b.getTargetLocale().getDisplayName();
            rv = aVal.compareTo(bVal);
            break;
		case USER:
            aVal = a.getCreatorId();
            bVal = b.getCreatorId();
            rv = aVal.compareTo(bVal);
            break;
		case TASK:
            aVal = a.getTaskName();
            bVal = b.getTaskName();
            rv = aVal.compareTo(bVal);
            break;
	    }
        return rv;
	}
}
