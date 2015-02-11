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
package com.globalsight.calendar;

/**
 * CalendarWorkingDay is the class containing all of the required
 * calendaring constants.
 */
public class CalendarConstants    
{
    /* 
     * The following three constants are used during a calendar 
     * creation/modification.  When an object is added or removed
     * from a calendar, we'll update the "calendar association state".
     */
    // The object belongs to a calendar
    public static final int EXISTING = 0;

    // The object is added to a calendar
    public static final int NEWLY_ADDED = 1;

    // The object is deleted from a calendar
    public static final int DELETED = 2;
}
