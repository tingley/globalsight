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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * BaseFluxCalendar is an abstract class used as the base class for Calendar and
 * UserCalendar classes..
 */
public abstract class BaseFluxCalendar extends PersistentObject
{
    /**
     * Constant used for TopLink's query. The constant value has to be exactly
     * the same as the variable defined as an attribute of BaseFluxCalendar (for
     * mapping purposes).
     */
    public static final String TIME_ZONE_ID = "m_timeZoneId";

    // PRIVATE MEMBER VARIABLES
    private String m_lastUpdatedBy = "system"; // dummy name
    private Date m_lastUpdatedTime = new Date();
    private String m_timeZoneId = null;
    private List<WorkingDay> m_workingDays = new ArrayList<WorkingDay>();
    private long m_companyId;

    // The object containing all the business interval related
    // info about the calendar.
    private CalendarBusinessIntervals m_bizIntervals = null;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constructor used by TOPLink.
     */
    public BaseFluxCalendar()
    {
        super();
    }

    /**
     * Create an initialized BaseFluxCalendar based on a set of required
     * attributes.
     * 
     * @param p_name
     *            - The name of the calendar.
     * @param p_timeZoneId
     *            - The time zone id of this calendar.
     * @param p_workingDays
     *            - A list of
     */
    public BaseFluxCalendar(String p_name, String p_timeZoneId)
    {
        super();
        setName(p_name);
        m_timeZoneId = p_timeZoneId;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Abstract Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the hours per business day conversion factor which is used for
     * converting a duration into a valid unit of time.
     * 
     * @return The hours per business day conversion factor.
     */
    abstract public int getHoursPerDay();

    // ////////////////////////////////////////////////////////////////////
    // End: Abstract Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Helper Methods
    // ////////////////////////////////////////////////////////////////////

    public long getCompanyId()
    {
        return m_companyId;
    }

    public void setCompanyId(long p_companyId)
    {
        m_companyId = p_companyId;
    }

    /**
     * Add a working day to the list of this calendar's working days. The object
     * type would depend on the sub-class of BaseFluxCalendar.
     * 
     * @param p_workingDay
     *            - The working day to be added.
     */
    public void addWorkingDay(WorkingDay p_workingDay)
    {
        p_workingDay.setBaseFluxCalendar(this);
        m_workingDays.add(p_workingDay);
    }

    /**
     * Get a set of allowable business interval range objects based on the
     * calendar's holidays. The set contains all the allowable business times
     * for the current month (based on the calendar's time zone).
     * 
     * @return A SortedAllowableIntervalRanges of AllowableIntervalRange based
     *         on the holiday business interval. Each object can return a
     *         beginning and an end date for that range.
     */
    public SortedAllowableIntervalRanges getBizIntervalRangeForHolidays()
    {
        return m_bizIntervals.getBizIntervalRangeForHolidays();
    }

    /**
     * @deprecated Get a set of allowable business interval range objects based
     *             on the calendar's working days. The set contains all the
     *             allowable business times for the current month (based on the
     *             calendar's time zone).
     * 
     * @return A SortedAllowableIntervalRanges of AllowableBusinessIntervalRange
     *         based on the working day business interval. Each object can
     *         return a beginning and an end date for that range.
     */
    public SortedAllowableIntervalRanges getBizIntervalRangeForWorkingDays()
    {
        return m_bizIntervals.getBizIntervalRangeForWorkingDays();
    }

    /**
     * Get a set of allowable business interval range objects based on the
     * calendar's reserved times (ONLY for a user calendar). The set contains
     * all the allowable business times for the current month (based on the
     * calendar's time zone).
     * 
     * @return A SortedAllowableIntervalRanges of AllowableIntervalRange based
     *         on the reserved time business interval. Each object can return a
     *         beginning and an end date for that range.
     */
    public SortedAllowableIntervalRanges getBizIntervalRangeForReservedTimes()
    {
        return m_bizIntervals.getBizIntervalRangeForReservedTimes();
    }

    /**
     * Get a set of allowable business interval range objects based on the
     * calendar's 'personal' reserved times (ONLY for a user calendar). The set
     * contains all the allowable business times for the current month (based on
     * the calendar's time zone).
     * 
     * @return A SortedAllowableIntervalRanges of AllowableIntervalRange based
     *         on the 'personal' reserved time business interval. Each object
     *         can return a beginning and an end date for that range.
     */
    public SortedAllowableIntervalRanges getBizIntervalRangeForPersonalReservedTimes()
    {
        return m_bizIntervals.getBizIntervalRangeForPersonalEvents();
    }

    public CalendarBusinessIntervals getCalendarInterval()
    {
        return m_bizIntervals;
    }

    /**
     * Get the user id of the person who last updated the calendar.
     * 
     * @return The user id of the person who last updated the calendar.
     */
    public String getLastUpdatedBy()
    {
        return m_lastUpdatedBy;
    }

    /**
     * Get the update time of the calendar.
     * 
     * @return The time which the calendar was updated.
     */
    public Date getLastUpdatedTime()
    {
        return m_lastUpdatedTime;
    }

    /**
     * Get the time zone of the calendar.
     * 
     * @return The calendar's time zone.
     */
    public TimeZone getTimeZone()
    {
        return m_timeZoneId == null ? null : TimeZone.getTimeZone(m_timeZoneId);
    }

    /**
     * Get the time zone id of the calendar.
     * 
     * @return The calendar's time zone id.
     */
    public String getTimeZoneId()
    {
        return m_timeZoneId;
    }

    /**
     * Get the working days for this calendar.
     * 
     * @return A list of working days (as CalendarWorkingDay objects).
     */
    public List getWorkingDays()
    {
        return m_workingDays;
    }

    public void setWorkingDays(List workingDays)
    {
        m_workingDays = workingDays;
    }

    /**
     * Get the working day object for the given day.
     * 
     * @param A
     *            day to be checked among the working days of this calendar.
     * @return The calendar's working day for the given day, or null if it does
     *         not exist.
     */
    public WorkingDay getWorkingDay(int p_day)
    {
        int size = m_workingDays.size();
        WorkingDay workingDay = null;
        for (int i = 0; (workingDay == null && i < size); i++)
        {
            WorkingDay wd = (WorkingDay) m_workingDays.get(i);
            if (wd.getDay() == p_day)
            {
                workingDay = wd;
            }
        }

        return workingDay;
    }

    /**
     * Set the time zone id of the calendar to the specified value.
     * 
     * @param p_timeZoneId
     *            - The time zone id to be set.
     */
    public void setTimeZoneId(String p_timeZoneId)
    {
        m_timeZoneId = p_timeZoneId;
    }

    /**
     * Remove all of the calendar's working days.
     */
    public void removeAllWorkingDays()
    {
        // int size = m_workingDays.size();
        // for (int i = 0; i < size; i++)
        // {
        // removeWorkingDay((WorkingDay)m_workingDays.get(i));
        // }
        WorkingDay[] workingDays = new WorkingDay[m_workingDays.size()];
        workingDays = (WorkingDay[]) m_workingDays.toArray(workingDays);
        for (int i = 0; i < workingDays.length; i++)
        {
            removeWorkingDay(workingDays[i]);
        }
    }

    /**
     * Mark the specified working day as deleted or simply remove it from the
     * list if this is a new calendar (since nothing's persisted yet).
     * 
     * @param p_workingDay
     *            - The working day to be removed from this calendar.
     */
    public void removeWorkingDay(WorkingDay p_workingDay)
    {
        // if this is a new calendar, just remove holiday from the list
        if (getId() == -1)
        {
            m_workingDays.remove(p_workingDay);
        }
        else
        {
            // set the state to "deleted" so during an update,
            // it'll get removed.
            p_workingDay.setCalendarAssociationState(CalendarConstants.DELETED);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public Helper Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Remove the specified working day from this calendar's list.
     * 
     * @param p_workingDay
     *            - The working day to be removed from this calendar.
     */
    void removeWorkingDayFromList(WorkingDay p_workingDay)
    {
        m_workingDays.remove(p_workingDay);
    }

    /**
     * Set the business intervals of this calendar to be the specified value.
     * Note that the CalendarBusinessIntervals contains all the business
     * interval related info for a calendar.
     */
    void setBusinessIntervalInfo(CalendarBusinessIntervals p_bizIntervals)
    {
        m_bizIntervals = p_bizIntervals;
    }

    /**
     * Set the user id of the person updated the calendar to the specified
     * value.
     * 
     * @param p_lastUpdatedBy
     *            - The user id of the person who modified the calendar.
     */
    void setLastUpdatedBy(String p_lastUpdatedBy)
    {
        m_lastUpdatedBy = p_lastUpdatedBy;
    }

    /**
     * Set the last updated time to be the specified value.
     * 
     * @param p_lastUpdatedTime
     *            - The time to be set.
     */
    void setLastUpdatedTime(Date p_lastUpdatedTime)
    {
        m_lastUpdatedTime = p_lastUpdatedTime;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////
}
