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
package com.globalsight.everest.comment;

import com.globalsight.everest.comment.IssueHistory;

import java.util.Comparator;

/**
 * Comparator implementation to enable sorting of IssueHistory.
 * Sorts in descending order.
 */
public class IssueHistoryComparator
    implements Comparator
{
    /**
     * Sorts two IssueHistory objects by descending date order
     * (youngest date first).
     */
    public int compare(Object p_object1, Object p_object2)
    {
        IssueHistory ih1 = (IssueHistory)p_object1;
        IssueHistory ih2 = (IssueHistory)p_object2;

        if (ih1 != null && ih2 != null)
        {
            if (ih1.dateReportedAsDate().before(
                ih2.dateReportedAsDate()))
            {
                return 1;
            }
            else if (ih1.dateReportedAsDate().after(
                ih2.dateReportedAsDate()))
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
        else if (ih1 != null)
        {
            return 1;
        }
        else if (ih2 != null)
        {
            return -1;
        }

        return 0;
    }
}
