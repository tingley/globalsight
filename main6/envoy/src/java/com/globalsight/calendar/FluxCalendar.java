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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BaseFluxCalendar is an abstract class used as the base class for Calendar and
 * UserCalendar classes..
 */
public class FluxCalendar extends BaseFluxCalendar
{
    private static final long serialVersionUID = 6659471466510791146L;

    // PRIVATE MEMBER VARIABLES
    private boolean m_isDefault = false;
    private int m_hoursPerDay = 0;
    private List m_holidays = new ArrayList();

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constructor used by TOPLink.
     */
    public FluxCalendar()
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
     * @param p_isDefault
     *            - The value used to indicate that this is a default calendar.
     */
    public FluxCalendar(String p_name, String p_timeZone, boolean p_isDefault,
            int p_hoursPerDay)
    {
        super(p_name, p_timeZone);
        m_isDefault = p_isDefault;
        m_hoursPerDay = p_hoursPerDay;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Abstract Methods Implementation
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the hours per business day conversion factor which is used for
     * converting a duration into a valid unit of time.
     * 
     * @return The hours per business day conversion factor.
     */
    public int getHoursPerDay()
    {
        return m_hoursPerDay;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Abstract Methods Implementation
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Helper Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Add a holiday to the list of this calendar's holidays.
     * 
     * @param p_holiday
     *            - The holiday to be added.
     */
    public void addHoliday(Holiday p_holiday)
    {
        if (!m_holidays.contains(p_holiday))
        {
            p_holiday.setFluxCalendar(this);
            p_holiday.setCompanyId(this.getCompanyId());
            m_holidays.add(p_holiday);
        }
    }

    /**
     * Get the list of holiday objects for this calendar.
     * 
     * @return A list of calendar holidays.
     */
    public Set getHolidays()
    {
        return new HashSet(m_holidays);
    }

    /**
     * Get the list of holiday objects for this calendar.
     * 
     * @return A list of calendar holidays.
     */
    public void setHolidays(Set holidays)
    {
        m_holidays = new ArrayList(holidays);
    }

    /**
     * Get the list of holiday objects for this calendar.
     * 
     * @return A list of calendar holidays.
     */
    public List getHolidaysList()
    {
        return m_holidays;
    }

    /**
     * Determines whether this calendar is the system's default calendar.
     * 
     * @return True if it's the default calendar; otherwise returns false.
     */
    public boolean getIsDefault()
    {
        return m_isDefault;
    }

    /**
     * Determines whether this calendar is the system's default calendar.
     * 
     * @return True if it's the default calendar; otherwise returns false.
     */
    public boolean isDefault()
    {
        return m_isDefault;
    }

    /**
     * Remove the specified holiday from the list of this calendar's holidays.
     * The physical remove will only happen if this is a new calendar (not
     * created yet). For an existing calendar, the holiday's state would be set
     * to 'removed' until the calendar update takes place.
     * 
     * @param p_holiday
     *            - The holiday to be removed from this calendar.
     */
    public void removeHoliday(Holiday p_holiday)
    {
        Holiday holiday = (Holiday) m_holidays.get(m_holidays
                .indexOf(p_holiday));
        // if this is a new calendar, just remove holiday from the list
        if (getId() == -1 && holiday != null)
        {
            m_holidays.remove(holiday);
        }
        else if (holiday != null)
        {
            holiday.setCalendarAssociationState(CalendarConstants.DELETED);
        }
    }

    /**
     * Set the hours per business day conversion factor to be the specified
     * value.
     * 
     * @param p_hoursPerDay
     *            - The conversion factor to be set.
     */
    public void setHoursPerDay(int p_hoursPerDay)
    {
        m_hoursPerDay = p_hoursPerDay;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public Helper Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Clear the holiday list so the registered version of the holiday object
     * can be added to the calendar. This method is used during the creation of
     * the calendar object.
     */
    void clearHolidayList()
    {
        m_holidays.clear();
    }

    /**
     * Set the default flag to be the specified value. If set to true, this
     * calendar would be the system's default calendar.
     * 
     * @param p_isDefault
     *            - The value to be set which determine whether this calendar is
     *            the default calendar.
     */
    public void setIsDefault(boolean p_isDefault)
    {
        m_isDefault = p_isDefault;
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////
}
