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
package com.globalsight.everest.foundation;

import com.globalsight.util.ArrayConverter;

// import java.sql.Time;
import java.io.Serializable;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * PeriodOfTime represents the relative and absolute time. The relative time has
 * an interval, unit of time, and start time (i.e. every 5 days/hrs/min starting
 * at 12pm). The absolute time has an array of selected days, and a starting
 * time (i.e. every Tue, Thu starting at 12pm).
 * 
 * 
 * @author Tomy A. Doomany
 */

/*
 * MODIFIED MM/DD/YYYY TomyD 08/09/2000 Initial version.
 */

public class PeriodOfTime implements Serializable
{
    private static final long serialVersionUID = 2164985000620944250L;
    public final static int UNIT_DAY = 0;
    public final static int UNIT_HOUR = 1;
    public final static int UNIT_MINUTE = 2;

    private Timestamp m_startTime = null;
    private int m_interval = 0;
    private int m_unit = 0;
    private int[] m_days = new int[7];
    // private boolean m_isAbsolute = false;
    private char m_timerType = 'R';

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Construct a PeriodOfTime object.
     */
    public PeriodOfTime()
    {
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Helper Methods
    // ////////////////////////////////////////////////////////////////////////////////

    /**
     * Set the relative time info.
     * 
     * @param p_interval -
     *            The time interval.
     * @param p_unit -
     *            The time unit (day, hour, min).
     * @param p_startTime -
     *            The starting time
     */
    public void setTimeContext(int p_interval, int p_unit, Timestamp p_startTime)
    {
        m_interval = p_interval;
        m_unit = p_unit;
        m_startTime = p_startTime;
    }

    /**
     * Set the absolute time info.
     * 
     * @param p_days -
     *            An array of days.
     * @param p_startTime -
     *            The starting time
     */
    public void setTimeContext(int[] p_days, Timestamp p_startTime)
    {
        m_timerType = 'A';
        m_startTime = p_startTime;
        m_days = p_days;
        m_unit = UNIT_HOUR;
        Arrays.sort(m_days); // sort it before doing any binary search
    }

    /**
     * Determines whether this period of time is absolute.
     * 
     * @return True if it's an absolute period of time, otherwise return false.
     */
    public boolean isAbsolute()
    {
        if (m_timerType == 'A')
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Get the time interval.
     * 
     * @return The time interval.
     */
    public int getInterval()
    {
        return m_interval;
    }
    
    public void setInterval(int m_interval)
    {
        this.m_interval = m_interval;
    }

    /**
     * Get the time interval in milliseconds.
     * 
     * @return The interval in milliseconds.
     */
    public long getIntervalInMillisec()
    {
        long multiplier = getUnitInMillisec(m_unit);
        return (multiplier * m_interval);
    }

    /**
     * Get the unit (Hour, day, minute, etc.) of the time interval.
     * 
     * @return The time interval's unit.
     */
    public int getIntervalUnit()
    {
        return m_unit;
    }

    public int getUnit()
    {
        return m_unit;
    }
    
    public void setUnit(int unit)
    {
        m_unit = unit;
    }
    
    /**
     * Get the start time for the interval.
     * 
     * @return The starting time.
     */
    public Timestamp getStartTime()
    {
        return m_startTime;
    }

    /**
     * Get the string representation of the Timestamp.
     * 
     * @return The string representation of time.
     */
    public String getDisplayedStartTime()
    {
        return m_startTime == null ? "" : m_startTime.toString();
    }

    /**
     * Get the selected days. Note that the days are based Calendar's
     * DAY_OF_WEEK and are in the range of 0 to 6 for Sunday through Saturday
     * respectively.
     * 
     * @return - An array of days.
     */
    public int[] getDays()
    {
        return m_days;
    }

    /**
     * Get the number of days within the array.
     * 
     * @return The number of days.
     */
    public int getNumberOfDays()
    {
        return m_days.length;
    }

    /**
     * Finds a particular day within the array of selected days using the binary
     * search algorithm.
     * 
     * @param p_day -
     *            the day to be searched for.
     * @return The index of the search key if found within the array; otherwise
     *         (-(insertion point)-1). Insertion point is the index of the first
     *         element greater than the key, or the array size, if all elements
     *         in the array are less than the specified key.
     */
    public int findDay(int p_day)
    {
        return Arrays.binarySearch(m_days, p_day);
    }

    /**
     * Get the day at the specified index.
     * 
     * @param p_index -
     *            The index of the requested day.
     * @return The day at that particular index.
     */
    public int getDayAt(int p_index)
    {
        return m_days[p_index];
    }

    /**
     * Get the unit in milliseconds.
     * 
     * @param p_unit -
     *            The unit to be converted into milliseconds.
     * @return The unit (day, hour, and minute) in milliseconds.
     */
    static public long getUnitInMillisec(int p_unit)
    {
        long multiplier = 0;
        switch (p_unit)
        {
            case UNIT_MINUTE:
                multiplier = 60;
                break;
            case UNIT_HOUR:
                multiplier = 3600;
                break;
            case UNIT_DAY:
                multiplier = 86400;
                break;
            default:
                break;
        }

        return (multiplier * 1000);
    }

    public void setDaysAsIntegerArray(String p_str)
    {
        StringTokenizer st = new StringTokenizer(p_str, " ,\t\r\n");
        int i = 0;
        while (st.hasMoreTokens())
        {
            String token = st.nextToken();
            m_days[i++] = Integer.parseInt(token);
        }
    }

    public String getDaysAsString()
    {
        String newstr = "";
        for (int i = 0; i < m_days.length; i++)
        {
            newstr += String.valueOf(m_days[i]);
            if (i != m_days.length - 1)
                newstr += ",";
        }
        return newstr;
    }

    public void setDaysAsString(String p_str)
    {
        setDaysAsIntegerArray(p_str);
    }

    /**
     * Return a string representation of the object.
     */
    public String toString()
    {
        String unit = "UNIT_DAY";
        switch (m_unit)
        {
            case UNIT_MINUTE:
                unit = "UNIT_MINUTE";
                break;
            case UNIT_HOUR:
                unit = "UNIT_HOUR";
                break;
        }
        return super.toString()
                + " m_startTime="
                + (m_startTime != null ? m_startTime.toDebugString() : "null")
                + " m_interval="
                + Integer.toString(m_interval)
                + " m_unit="
                + unit
                + " m_days="
                + (m_days != null ? ArrayConverter.asList(m_days).toString()
                        : "null") + " m_timerType="
                + String.valueOf(m_timerType);
    }

    public char getTimerType()
    {
        return m_timerType;
    }

    public void setTimerType(char type)
    {
        m_timerType = type;
    }

    public void setStartTime(Timestamp time)
    {
        m_startTime = time;
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Helper Methods
    // ////////////////////////////////////////////////////////////////////////////////

}
