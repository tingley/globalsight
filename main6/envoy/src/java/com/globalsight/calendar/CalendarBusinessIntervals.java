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

// GlobalSight
import java.io.Serializable;

import org.quartz.Calendar;

import com.globalsight.scheduling.MultiCalendar;



/**
 * CalendarBusinessIntervals is an abstract class used as the base class for Calendar
 * and UserCalendar classes..
 */

public class CalendarBusinessIntervals
    implements Serializable
{
    private Calendar holidayCalendar = null;
    private Calendar workingDayCalendar = null;
    private Calendar reservedCalendar = null;
    private Calendar personalReservedCalendar = null;
    private MultiCalendar multiCalendar = null;
    // Allowable business interval range based on the holiday biz interval.
    private SortedAllowableIntervalRanges m_holidayBizIntervalRanges = null;
    // Allowable business interval range based on the working day biz interval.
    private SortedAllowableIntervalRanges m_workingDayBizIntervalRanges = null;
    // Allowable business interval range based on the reserved time biz interval.
    // Note that this value is set only for a user calendar
    private SortedAllowableIntervalRanges m_reservedTimeBizIntervalRanges = null;
    // Allowable business interval range based on the personal type reserved time
    // biz interval.  Note that this value is set only for a user calendar
    private SortedAllowableIntervalRanges m_personalEventBizIntervalRanges = null;
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Create an initialized CalendarBusinessIntervals based on a set of required 
     * attributes.
     * These attributes are only set when a calendar is retrieved.  The
     * SortedAllowableIntervalRanges attributes are used for displaying 
     * holidays and other non-working days (weekend and reserved time) in 
     * the calendar UI. Each object of the SortedAllowableIntervalRanges 
     * is an AllowableIntervalRange object.  The allowable business 
     * interval range is computed based on the current month and calendar's  
     * time zone. The start date is the beginning of the current month 
     * and the end date it the end of the current month.
     *
     * @param p_holidayBizInterval - The business interval created based 
     * on the calendar's holidays.
     * @param p_workingDayBizInterval - The business interval created 
     * based on the calendar's working days.
     * @param p_reservedTimeBizInterval - The business interval created
     * based on  the calendar's reserved time.  ONLY set for a user calendar.
     * @param p_calendarBizInterval - The business interval created based on
     * the intersection of all of the calendar's business intervals.
     * @param p_bizIntervalRangeForHolidays - a SortedAllowableIntervalRanges of 
     * AllowableIntervalRange based on the holiday business interval.  Each object
     * can return a beginning and an end date for that range.
     * @param p_bizIntervalRangeForWorkingDays - a SortedAllowableIntervalRanges of
     * AllowableIntervalRange based on the working day business interval.  
     * Each object can return a beginning and an end date for that range.
     * @param p_bizIntervalRangeForReservedTime - a SortedAllowableIntervalRanges
     * of AllowableIntervalRange based on the reserved time business interval.  
     * Each object can return a beginning and an end date for that range.
     */
    public CalendarBusinessIntervals(
    		Calendar p_holidayBizInterval, 
    		Calendar p_workingDayBizInterval,
    		Calendar p_reservedTimeBizInterval,
    		Calendar p_personalReservedTimeBizInterval,
    		MultiCalendar p_calendarBizInterval,
            SortedAllowableIntervalRanges p_bizIntervalRangeForHolidays,
            SortedAllowableIntervalRanges p_bizIntervalRangeForWorkingDays,
            SortedAllowableIntervalRanges p_bizIntervalRangeForReservedTime,
            SortedAllowableIntervalRanges p_bizIntervalRangeForPersonalEvent) {
    	
    	holidayCalendar = p_holidayBizInterval;
        workingDayCalendar = p_workingDayBizInterval;
        reservedCalendar = p_reservedTimeBizInterval;
        personalReservedCalendar = p_personalReservedTimeBizInterval;
        multiCalendar = p_calendarBizInterval;
        m_holidayBizIntervalRanges = p_bizIntervalRangeForHolidays;
        m_workingDayBizIntervalRanges = p_bizIntervalRangeForWorkingDays;
        m_reservedTimeBizIntervalRanges = p_bizIntervalRangeForReservedTime;
        m_personalEventBizIntervalRanges = p_bizIntervalRangeForPersonalEvent;
    	
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Package-scope Methods
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Get a set of allowable business interval range objects based on
     * the calendar's holidays.  The set contains all the allowable
     * business times for the current month (based on the calendar's
     * time zone).
     *
     * @return A SortedAllowableIntervalRanges of AllowableIntervalRange
     * based on the holiday business interval.  Each object
     *  can return a beginning and an end date for that range.
     */
    SortedAllowableIntervalRanges getBizIntervalRangeForHolidays()
    {
        return m_holidayBizIntervalRanges;
    }
    
    public MultiCalendar getMultiCalendar() {
    	return multiCalendar;
    }

    /**
     * Get a set of allowable business interval range objects based on
     * the calendar's working days.  The set contains all the allowable
     * business times for the current month (based on the calendar's
     * time zone).
     *
     * @return A SortedAllowableIntervalRanges of AllowableBusinessIntervalRange
     * based on the working day business interval.  Each 
     * object can return a beginning and an end date for that range.
     */
    SortedAllowableIntervalRanges getBizIntervalRangeForWorkingDays()
    {
        return m_workingDayBizIntervalRanges;
    }

    /**
     * Get a set of allowable business interval range objects based on
     * the calendar's 'personal' reserved times (ONLY for a user calendar).  
     * The set contains all the allowable business times for the 
     * current month (based on the calendar's time zone).
     *
     * @return A SortedAllowableIntervalRanges of AllowableIntervalRange
     * based on the reserved time business interval.  Each 
     * object can return a beginning and an end date for that range.
     */
    SortedAllowableIntervalRanges getBizIntervalRangeForPersonalEvents()
    {
        return m_personalEventBizIntervalRanges;
    }

    /**
     * Get a set of allowable business interval range objects based on
     * the calendar's reserved times (ONLY for a user calendar).  
     * The set contains all the allowable business times for the 
     * current month (based on the calendar's time zone).
     *
     * @return A SortedAllowableIntervalRanges of AllowableIntervalRange
     * based on the reserved time business interval.  Each 
     * object can return a beginning and an end date for that range.
     */
    SortedAllowableIntervalRanges getBizIntervalRangeForReservedTimes()
    {
        return m_reservedTimeBizIntervalRanges;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Package-scope Methods
    //////////////////////////////////////////////////////////////////////
}
