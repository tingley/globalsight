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

import java.util.Date;
import java.util.Locale;

import com.globalsight.everest.comment.IssueHistory;

/**
* This class can be used to compare IssueHistory objects
*/
public class IssueHistoryComparator extends StringComparator
{
	//types of Comment comparison
	public static final int DATE = 0;
	
	/**
	* Creates a IssueHistoryComparator with the given locale.
	*/
	public IssueHistoryComparator(Locale p_locale)
	{
	    super(p_locale);
	}
	
	/**
	 * Performs a comparison of two IssueHistory objects 
	 */
	public int compare(java.lang.Object p_A, java.lang.Object p_B) 
        {
	    IssueHistory a = (IssueHistory) p_A;
	    IssueHistory b = (IssueHistory) p_B;

	    int rv;
        switch (m_type)
	    {
	        default:
            case DATE:
                Date aDate = a.dateReportedAsDate();
                Date bDate = b.dateReportedAsDate();
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
	    }
        return rv;
	}
}
