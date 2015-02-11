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

import java.util.Calendar;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Holiday is an object representing a holiday that's associated with a
 * calendar. Note that a holiday object has a many-to-many relationship with
 * calendar object. One of the attributes of this object, m_timeExpression, is
 * only used by schedule engine (Quartz) and is defined when a holiday object is
 * created. Time expression is a description of a rule that is used to compute a
 * particular date or range of dates within a specific year. For example, the
 * rule might say "the 1st Monday in June", or "the last Thursday in November",
 * or possibly something like "the 10th day of the month".
 */
public class Holiday extends PersistentObject
{
    private static final long serialVersionUID = -5212932331537392275L;

    //
    // PRIVATE MEMBER VARIABLES
    //
    private int m_dayOfMonth = 0; // 1 to 31
    private Integer m_dayOfWeek = new Integer(0); // 1-7 for sun-sat
    private String m_description = null;
    private Integer m_endingYear = new Integer(0);
    private boolean m_isAbsolute = false;
    private int m_month = 0; // 0-11 for jan-dec
    private String m_timeExpression = null;
    private String m_weekOfMonth = null; // 1-4 or $ for last week

    private FluxCalendar m_fluxCalendar = null; // used for TOPLink's back
                                                // pointer
    private int m_calendarAssociationState = CalendarConstants.EXISTING;
    private long m_companyId;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Create an initialized Holiday (used by TOPLink).
     */
    public Holiday()
    {
        super();
    }

    /**
     * Constructor used during creation of a holiday.
     */
    public Holiday(String p_name, String p_description, int p_dayOfMonth,
            int p_dayOfWeek, int p_endingYear, boolean p_isAbsolute,
            int p_month, String p_weekOfMonth, String p_companyId)
    {
        super();
        setName(p_name);
        m_dayOfMonth = p_dayOfMonth;
        m_dayOfWeek = new Integer(p_dayOfWeek);
        m_description = p_description;
        m_endingYear = new Integer(p_endingYear);
        m_isAbsolute = p_isAbsolute;
        m_month = p_month;
        m_weekOfMonth = p_weekOfMonth;
        m_companyId = Long.parseLong(p_companyId);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    /**
     * Get the calendar association state of this holiday. This value is used
     * during creation/modification of a calendar in order to determine whether
     * this holiday is an existing one, a new one, or the one that should be
     * removed from the list of holidays for a particular calendar.
     * 
     * @return The state for calendar association.
     */
    public int getCalendarAssociationState()
    {
        return m_calendarAssociationState;
    }

    /**
     * Get the day of the month for this holiday. The day of the month is an
     * integer ranging from 1 to 31 (depending on the selected month).
     * 
     * @return An integer representing the day of the month.
     */
    public int getDayOfMonth()
    {
        return m_dayOfMonth;
    }

    /**
     * Get the day of the week for this holiday. The day of the week is an
     * integer ranging from 1 to 7 (for Sunday to Saturday).
     * 
     * @return An integer representing the day of the week.
     */
    public Integer getDayOfWeek()
    {
        return m_dayOfWeek;
    }

    public void setDayOfWeek(Integer p_dayOfWeek)
    {
        if (p_dayOfWeek == null)
        {
            return;
        }

        if (p_dayOfWeek.intValue() < 1 || p_dayOfWeek.intValue() > 7)
        {
            return;
        }

        m_dayOfWeek = p_dayOfWeek;
    }

    /**
     * Get the description of this holiday.
     * 
     * @return This holiday's description.
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Set the description of this holiday to the specified value.
     * 
     * @param p_description
     *            - The description to be set.
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Get the ending year (if any) for this holiday.
     * 
     * @return The ending year of the holiday or zero if there's no ending year.
     */
    public Integer getEndingYear()
    {
        return m_endingYear == null ? new Integer(0) : m_endingYear;
    }

    /**
     * Set the ending year for this holiday to the specified value.
     * 
     * @param p_endingYear
     *            - The ending year to be set.
     */
    public void setEndingYear(Integer p_endingYear)
    {
        m_endingYear = p_endingYear;
    }

    /**
     * Get the month for this holiday. The month is an integer ranging from 0 to
     * 11 (for January to December).
     * 
     * @return The holiday's month.
     */
    public int getMonth()
    {
        return m_month;
    }

    /**
     * Set the month for this holiday to the specified value (0-11 for jan-dec).
     * 
     * @param p_month
     *            - The month to be set.
     */
    public void setMonth(int p_month)
    {
        if (p_month < 0 || p_month > 11)
        {
            return;
        }
        m_month = p_month;
    }

    /**
     * Return the value of the time expression for this holiday.
     * 
     * @return the time expression.
     */
    public String getTimeExpression()
    {
        return m_timeExpression;
    }

    /**
     * Set the value of the expression on this object. This value is set right
     * before saving the holiday object.
     * 
     * @param p_expression
     *            the new value.
     */
    public void setTimeExpression(String p_timeExpression)
    {
        m_timeExpression = p_timeExpression;
    }

    /**
     * Get the week of the month for this holiday. The week of the month is in
     * the range of 1 to 4 and $ for the last week of the month. This is used
     * when a holiday is defined as "The First Monday of March" where '1' is
     * used as the representation of "The First". Also '$' is used to represent
     * the week of the month for "The Last Monday of March".
     * 
     * @return The week of the month.
     */
    public String getWeekOfMonth()
    {
        return m_weekOfMonth;
    }

    /**
     * Set the week of the month to be the specified value. Valid values are 1-4
     * and $ (for last week).
     * 
     * @param p_weekOfMonth
     *            - The week of the month to be set.
     */
    public void setWeekOfMonth(String p_weekOfMonth)
    {
        m_weekOfMonth = p_weekOfMonth;
    }

    /**
     * Determines whether the date for this holiday is defined as an absolute
     * date (i.e. March 21st or March 2nd 2003).
     * 
     * @return True is the date is absolute. Otherwise return false.
     */
    public boolean getIsAbsolute()
    {
        return m_isAbsolute;
    }

    /**
     * Set the flag to the specified value in order to determine whether the
     * date for this holiday is absolute.
     * 
     * @param p_isAbsolute
     *            - The value to be set.
     */
    public void setIsAbsolute(boolean p_isAbsolute)
    {
        m_isAbsolute = p_isAbsolute;
    }

    /**
     * Set the day of the month to be the specified value (1 to 31)
     * 
     * @param p_dayOfMonth
     *            - The day of the month to be set.
     */
    public void setDayOfMonth(int p_dayOfMonth)
    {
        if (p_dayOfMonth < 1 || p_dayOfMonth > 31)
        {
            return;
        }

        m_dayOfMonth = p_dayOfMonth;
    }

    /**
     * OVERRIDE: Return a string representation of the receiver.
     * 
     * @return a description of the time expression.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[Name=\"");
        sb.append(getName());
        sb.append("\", Expression=\"");
        sb.append(getTimeExpression());
        sb.append("\"]");
        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Override Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Indicates whether some other Holiday is "equal to" this one.
     * 
     * @param p_holiday
     *            The reference object with which to compare.
     * @return true if this holiday object is the same as the p_holiday
     *         argument; false otherwise.
     */
    public boolean equals(Object p_holiday)
    {
        if (p_holiday instanceof Holiday)
        {
            return (getId() == ((Holiday) p_holiday).getId());
        }
        return false;
    }

    /**
     * The hashCode method is overridden to support the 'equals' method. If two
     * Holiday objects are equal according to the equals(Object) method, then
     * calling the hashCode method on each of the two objects will produce the
     * same integer result.
     * 
     * @return the working hour id.
     */
    public int hashCode()
    {
        return getIdAsLong().hashCode();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public Override Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package Level Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Set the calendar association state to be the specified value.
     * 
     * @param p_state
     *            - The calendar association state to be set.
     */
    void setCalendarAssociationState(int p_state)
    {
        m_calendarAssociationState = p_state;
    }

    /*
     * Set the calendar where this holiday belongs to.
     * 
     * @param p_fluxCalendar The calendar where this holiday belongs to.
     */
    void setFluxCalendar(FluxCalendar p_fluxCalendar)
    {
        m_fluxCalendar = p_fluxCalendar;
        // since this back pointer is set when a holiday is added to a
        // calendar, we'll set the associate state here.
        m_calendarAssociationState = CalendarConstants.NEWLY_ADDED;
    }

    public boolean isAbsolute()
    {
        return m_isAbsolute;
    }

    /**
     * Check if the day is this holiday
     * 
     * @param calendar
     * @return
     */
    public boolean isHoliday(Calendar calendar)
    {
        if (getIsAbsolute())
        {
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int endingYear = getEndingYear().intValue();
            if (endingYear > 0)
            {
                int year = calendar.get(Calendar.YEAR);

                if (year == endingYear && month == m_month
                        && dayOfMonth == m_dayOfMonth)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                if (month == m_month && dayOfMonth == m_dayOfMonth)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        else
        {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
            String weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH) + "";
            int month = calendar.get(Calendar.MONTH);
            String lastMonth = calendar.getMaximum(Calendar.WEEK_OF_MONTH) + "";

            if (dayOfWeek == m_dayOfWeek
                    && month == m_month
                    && (m_weekOfMonth.equals(weekOfMonth) || (m_weekOfMonth
                            .equals("$") && lastMonth.equals(weekOfMonth))))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Package Level Methods
    // ////////////////////////////////////////////////////////////////////
}
