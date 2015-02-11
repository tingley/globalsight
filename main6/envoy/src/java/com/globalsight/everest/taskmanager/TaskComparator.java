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

package com.globalsight.everest.taskmanager;


import java.util.Comparator;
import java.util.Date;


/**
 * Comparator implementation to enable sorting of Task objects based
 * on the completion date.
 */
public class TaskComparator implements Comparator
{
    private static final int lessThan = -1;
    private static final int greaterThan = 1;
    private static final int equalTo = 0;


    /**
     * Compares its two arguments for order. Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, 
     * equal to, or greater than the second.
     * @param p_object1 - First object to be used for comparison.
     * @param p_object2 - The second object to be used for comparison 
     *                    againts the first one.
     *
     * @return A negative integer, zero, or a positive integer as the 
     * first argument is less than, equal to, or greater than the second.
     */
    public int compare(Object p_object1, Object p_object2)
    {
        
        Task task1 = (Task)p_object1;
        Task task2 = (Task)p_object2;

        if (task1 != null && task2 != null)
        {
            Date dt1 = task1.getCompletedDate();
            Date dt2 = task2.getCompletedDate();
            
            if (dt1 == null && dt2 == null)
            {
                // both have no completed date, go to top of the list
                return equalTo;
            }
            else if (dt1 == null && dt2 != null)
            {
                return greaterThan;
            }
            else if (dt1 != null && dt2 == null)
            {
                return lessThan;
            }
            else
            {
                return dt1.compareTo(dt2);
            }            
        }
        return equalTo;
    }

    /**
     * Indicates whether some other object is "equal to" this Comparator. 
     * @param p_object - The reference object with which to compare.
     * @return True only if the specified object is also a comparator and
     * it imposes the same ordering as this comparator.
     */
    public boolean equals(Object p_object)
    {
        if (p_object == this)
            return true;
        return false;
    }
}
