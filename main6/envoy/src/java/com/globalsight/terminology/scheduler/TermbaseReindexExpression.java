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

package com.globalsight.terminology.scheduler;

import com.globalsight.everest.persistence.PersistentObject;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Persistent data object to record if a "reindex termbase" cron job
 * is defined for a given termbase, and for when it was scheduled.
 */
public class TermbaseReindexExpression
    extends PersistentObject
	implements Serializable, CronExpression
{
	/** Type signifying the "Reindex Termbase" task. */
	static public final String TYPE = "reindex";

    /**
     * <P>A map containing the alias representation of day of the week
     * used in scheduler as a time expression.</P>
     *
     * <P>Note that the key is the value used in Java for representing
     * day of week (1-7 for sun-sat).  The values are the strings SU,
     * MO, TU, WE, TH, FR, and SA.</P>
     */
    static private HashMap s_dayOfWeek = new HashMap(7);

    static
    {
        s_dayOfWeek.put(new Integer(1), "SU");
        s_dayOfWeek.put(new Integer(2), "MO");
        s_dayOfWeek.put(new Integer(3), "TU");
        s_dayOfWeek.put(new Integer(4), "WE");
        s_dayOfWeek.put(new Integer(5), "TH");
        s_dayOfWeek.put(new Integer(6), "FR");
        s_dayOfWeek.put(new Integer(7), "SA");
    }

    private Long m_termbaseId;

    private String m_minutes = "*";         // 0 to 59 comma separated
    private String m_hours = "*";           // 0 to 23 comma separated
    private String m_daysOfMonth = "*";     // 1 to 31
    private String m_months = "*";          // 0-11 or jan-dec
    private String m_daysOfWeek = "*";      // 1-7 for sun-sat
    private String m_dayOfYear = "*";       // 1-366
    private String m_weekOfMonth = "*";     // 1-6 or $ for last week
    private String m_weekOfYear = "*";      // 1-53 
    private String m_year = "*";            // 1970-3000

    private String m_cronExpression = null;

	//
	// Constructor
	//

    public TermbaseReindexExpression(Long p_termbaseId)
    {
        m_termbaseId = p_termbaseId;
	}

    public TermbaseReindexExpression(Long p_termbaseId, String p_minutes, 
		String p_hours, String p_daysOfMonth, String p_months, 
		String p_daysOfWeek, String p_dayOfYear, String p_weekOfMonth,
		String p_weekOfYear, String p_year)
    {
        m_termbaseId = p_termbaseId;

        m_minutes = p_minutes;
        m_hours = p_hours;
        m_daysOfMonth = p_daysOfMonth;
		m_months = p_months;
        m_daysOfWeek = p_daysOfWeek;
        m_dayOfYear = p_dayOfYear;
		m_weekOfMonth = p_weekOfMonth;
		m_weekOfYear = p_weekOfYear;
        m_year = p_year;
	}

    public Long getObjectId()
    {
        return m_termbaseId;
    }

	public String getType()
	{
		return TYPE;
	}

    //
    // Public Methods
    //

    public String getMinutes()
    {
        return m_minutes;
    }

    public void setMinutes(String p_arg)
    {
        m_minutes = p_arg;
    }

    public String getHours()
    {
        return m_hours;
    }

    public void setHours(String p_arg)
    {
        m_hours = p_arg;
    }

    public String getDaysOfMonth()
    {
        return m_daysOfMonth;
    }

    public void setDaysOfMonth(String p_arg)
    {
        m_daysOfMonth = p_arg;
    }

    public String getMonths()
    {
        return m_months;
    }

    public void setMonths(String p_arg)
    {
        m_months = p_arg;
    }

    public String getDaysOfWeek()
    {
        return m_daysOfWeek;
    }

    public void setDaysOfWeek(String p_arg)
    {
        m_daysOfWeek = p_arg;
    }

    public String getDayOfYear()
    {
        return m_dayOfYear;
    }

    public void setDayOfYear(String p_arg)
    {
        m_dayOfYear = p_arg;
    }

    public String getWeekOfMonth()
    {
        return m_weekOfMonth;
    }

    public void setWeekOfMonth(String p_arg)
    {
        m_weekOfMonth = p_arg;
    }

    public String getWeekOfYear()
    {
        return m_weekOfYear;
    }

    public void setWeekOfYear(String p_arg)
    {
        m_weekOfYear = p_arg;
    }

    public String getYear()
    {
        return m_year;
    }

    public void setYear(String p_arg)
    {
		m_year = p_arg;
    }

    // The following method should be called from the class that is going
    // to persist the CronExpression object.  Note that it should be called
    // right before persistence so the time expression is calculated properly.

    //Based on Cron Expression:
    //milliseconds seconds minutes hours days-of-month months days-of-week day-of-year week-of-month week-of-year year

    // Milliseconds 0-999
    // Seconds 0-59
    // Minutes 0-59
    // Hours 0-23
    // Days-of-month 1-31
    // Months 0-11 or jan-dec
    // Days-of-week 1-7 or sun-sat
    // Day-of-year 1-366
    // Week-of-month 1-6 (we also use it for the $ sign representing something like "last Monday")
    // Week-of-year 1-53
    // Year 1970-3000

    // Flux uses time expression for date related calculations.  A time
    // expression is created based on the following examples that are
    // supported thru our UI:
    // 1. "0 0 0 0 21 mar"               -->  March 21st
    // 2. "0 0 0 0 20 apr * * * * 2002"  -->  April 20th 2002
    // 3. "0 0 0 0 1MO jul"              --> First Monday of July
    // 4. "0 0 0 0 $WE aug * * * * 2002" --> Last Wednesday of August 2002
    // 4. "0 0 0 10 * * 1"               --> Every week on Sunday at 10 AM
    // 4. "0 0 0 8,20 * * * * * * *"     --> Every day at 8AM and 8PM.

    public void buildCronExpression()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("0 0 ");

		sb.append(getMinutes()).append(" ");
		sb.append(getHours()).append(" ");
		sb.append(getDaysOfMonth()).append(" ");
		sb.append(getMonths()).append(" ");
		sb.append(getDaysOfWeek()).append(" ");
		sb.append(getDayOfYear()).append(" ");
		sb.append(getWeekOfMonth()).append(" ");
		sb.append(getWeekOfYear()).append(" ");
		sb.append(getYear());

        setCronExpression(sb.toString());
    }

    public void setCronExpression(String p_arg)
    {
        m_cronExpression = p_arg;
    }

    public String getCronExpression()
    {
        if (m_cronExpression == null)
        {
            buildCronExpression();
        }

        return m_cronExpression;
    }

	public String toString()
	{
		StringBuffer res = new StringBuffer(this.getClass().getName());

        res.append(" id=").append(m_termbaseId);

        res.append(" minutes=").append(m_minutes);
        res.append(" hours=").append(m_hours);
        res.append(" daysOfMonth=").append(m_daysOfMonth);
		res.append(" months=").append(m_months);
        res.append(" daysOfWeek=").append(m_daysOfWeek);
        res.append(" dayOfYear=").append(m_dayOfYear);
		res.append(" weekOfMonth=").append(m_weekOfMonth);
		res.append(" weekOfYear=").append(m_weekOfYear);
        res.append(" year=").append(m_year);

		return res.toString();
	}
}
