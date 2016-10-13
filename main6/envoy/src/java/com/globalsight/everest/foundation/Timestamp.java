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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.NotActiveException;

import java.text.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Timestamp can have just a date, just a time or BOTH date & time.
 *
 * Different options of DateFormat's factory methods for controlling the
 * length of the result:
 * <p>
 * SHORT is completely numeric, such as 12.13.52 or 3:30pm
 * MEDIUM is longer, such as Jan 12, 1952
 * LONG is longer, such as January 12, 1952 or 3:30:32pm
 * FULL is pretty completely specified, such as Tuesday, April 12, 1952 AD
 * or 3:30:42pm PST.
 *
 */
public class Timestamp
    implements Serializable
{

    private static final long serialVersionUID = 8268206215844352882L;
    
    //
    // PUBLIC CONSTANTS
    //
    // indicating the type of timestamp, time-only, date-only, or date-time
    public final static int TIME = 0;
    public final static int DATE = 1;
    public final static int TIME_AND_DATE = 2;

    // Date Arithmetic fields
    public final static int YEAR = Calendar.YEAR;
    public final static int MONTH = Calendar.MONTH;
    public final static int DAY_OF_MONTH = Calendar.DAY_OF_MONTH;
    public final static int DAY_OF_WEEK_IN_MONTH = 
        Calendar.DAY_OF_WEEK_IN_MONTH;
    public final static int DAY = Calendar.DATE;
    public final static int HOUR = Calendar.HOUR_OF_DAY;
    public final static int MINUTE = Calendar.MINUTE;
    public final static int SECOND = Calendar.SECOND;

    // default date and time format
    public final static int DEFAULT_DATE_STYLE = DateFormat.SHORT;
    public final static int DEFAULT_TIME_STYLE = DateFormat.FULL;

    //
    // PRIVATE MEMBER VARIABLES
    //
    private Calendar m_calendar;
    private int m_type = TIME;
    private int m_dateStyle = DEFAULT_DATE_STYLE;
    private int m_timeStyle = DEFAULT_TIME_STYLE;
    private Locale m_locale = null;
    private TimeZone m_timeZone;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * General purpose full-fledged timestamp with server's default time zone.
     */
    public Timestamp()
    {
        this(TIME_AND_DATE);
    }

    /**
     * Constructor for using the given time zone.
     */
    public Timestamp(TimeZone p_timeZone)
    {
        this(DEFAULT_DATE_STYLE, DEFAULT_TIME_STYLE, 
             TIME_AND_DATE, p_timeZone);
    }

   /**
     * Construct a Timestamp by specifying whether it's a time type or date type.
     * @param p_type - The type of the timestamp.
     */
    public Timestamp(int p_type)
    {
        this(p_type, null);
    }

    /**
     * Construct a Timestamp by specifying whether it's a time type or date type.
     * @param p_type - The type of the timestamp.
     * @param p_timeZone The timestamp's default time zone.
     */
    public Timestamp(int p_type, TimeZone p_timeZone)
    {
        this(DEFAULT_DATE_STYLE, DEFAULT_TIME_STYLE, p_type, p_timeZone);
    }


    /**
     * Construct a Timestamp with a particular date, time, date style, and
     * time style.
     *
     * @param p_year the year of the Timestamp.
     * @param p_month the month of the Timestamp.
     * @param p_day the day of the month.
     * @param p_hour the hour of the Timestamp.
     * @param p_min the minute of the Timestamp.
     * @param p_sec the second of the Timestamp.
     * @param p_dateStyle the date style of the Timestamp.
     * @param p_timeStyle the time style of the Timestamp.
     */
    public Timestamp(int p_year,
                     int p_month,
                     int p_day,
                     int p_hour,
                     int p_min,
                     int p_sec,
                     int p_dateStyle,
                     int p_timeStyle)
    {
        this(p_dateStyle, p_timeStyle, TIME_AND_DATE, null);
        getCalendar().set(p_year, p_month, p_day, p_hour, p_min, p_sec);
    }

    /**
     * Construct a Timestamp that can either be a Time or a Date (based on
     * the parameter passed).
     *
     * @param p_yearOrHour the year for a Date or the hour for a Time
     * @param p_monthOrMinute the month for a Date or the minute for a Time
     * @param p_dayOrSecond the day for a Date or the second for a Time
     * @param p_type the Timestamp type DATE or TIME.
     */
    public Timestamp(int p_yearOrHour,
                     int p_monthOrMinute,
                     int p_dayOrSecond,
                     int p_type)
    {
        this(p_yearOrHour, p_monthOrMinute, p_dayOrSecond, p_type, null);
    }
    /**
     * Construct a Timestamp that can either be a Time or a Date (based on
     * the parameter passed).
     *
     * @param p_yearOrHour the year for a Date or the hour for a Time
     * @param p_monthOrMinute the month for a Date or the minute for a Time
     * @param p_dayOrSecond the day for a Date or the second for a Time
     * @param p_type the Timestamp type DATE or TIME.
     */
    public Timestamp(int p_yearOrHour,
                     int p_monthOrMinute,
                     int p_dayOrSecond,
                     int p_type,
                     TimeZone p_timeZone)

    {
        this(DEFAULT_DATE_STYLE, DEFAULT_TIME_STYLE, p_type, p_timeZone);
        if (p_type == TIME)
        {
            getCalendar().set(Calendar.HOUR_OF_DAY, p_yearOrHour);
            getCalendar().set(Calendar.MINUTE, p_monthOrMinute);
            getCalendar().set(Calendar.SECOND, p_dayOrSecond);
        }
        else if (p_type == DATE)
        {
            getCalendar().set(YEAR, p_yearOrHour);
            getCalendar().set(MONTH, p_monthOrMinute);
            getCalendar().set(DAY_OF_MONTH, p_dayOrSecond);
        }
    }

    /**
     * Construct a Timestamp object with the current Date.
     *
     * @param p_dateStyle the date style
     * @param p_timeStyle the time style
     * @param p_type the Timestamp type (DATE or TIME
     */
    public Timestamp(int p_dateStyle, int p_timeStyle, 
                     int p_type, TimeZone p_timeZone)
    {
        m_type = p_type;
        m_dateStyle = p_dateStyle;
        m_timeStyle = p_timeStyle;
        m_timeZone = p_timeZone;
        getCalendar().setTime(new Date());
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Get the current date in milliseconds.
     *
     * @return the current date in milliseconds.
     */
    public long getTimeInMillisec()
    {
        return getCalendar().getTime().getTime();
    }

    /**
     * Takes in a Date and Time.
     * Sets the DD/MM/YYYY portion from the date
     * Sets the HH:MI:SS portion from the time
     * 
     * @param p_date
     * @param p_time
     */
    public void setDateAndTime(Date p_date, Date p_time)
    {
        setDate(p_date);
        Calendar time = Calendar.getInstance();
        time.setTime(p_time);
        setHour(time.get(Calendar.HOUR_OF_DAY));
        setMinute(time.get(Calendar.MINUTE));
        setSecond(time.get(Calendar.SECOND));
        setMilliSecond(time.get(Calendar.MILLISECOND));
    }

    /**
     * Set the 'Current' time of this Timestamp to a particular time.  This
     * is used for reading a particular time from the property file and storing
     * it in the time stamp object.
     *
     * @param p_date the new date.
     */
    public void setTime(Date p_date)
    {
        setDate(p_date);

        // we only need the time (hour, min, and second) and date should be
        // current date
        int hour = getHour();
        int minute = getMinute();
        int second = getSecond();

        // now update the time based on the current date...
        getCalendar().setTime(new Date());
        setHour(hour);
        setMinute(minute);
        setSecond(second);
    }

    /**
     * Set the calendar's current time based on the given long value.
     *
     * @param p_timeInMillis The new time (in milliseconds) to be set.
     */
    public void setTimeInMillis(long p_timeInMillis)
    {
        if (p_timeInMillis <= 0)
        {
            p_timeInMillis = System.currentTimeMillis();
        }
        getCalendar().setTimeInMillis(p_timeInMillis);
    }

    /**
     * Set the date of this Timestamp.
     *
     * @param The new date to be set.
     */
    public void setDate(Date p_date)
    {
        if (p_date == null)
        {
            p_date = new Date();
        }
        getCalendar().setTime(p_date);
    }

    /**
     * Get this Calendar's date.
     *
     * @return The calendar's date.
     */
    public Date getDate()
    {
        return getCalendar().getTime();
    }

    /**
     * @see java.util.Calendar.getActualMaximum(int)
     */
    public int getActualMaximum(int p_field)
    {
        return getCalendar().getActualMaximum(p_field);
    }

    /**
     * @see java.util.Calendar.getActualMinimum(int)
     */
    public int getActualMinimum(int p_field)
    {
        return getCalendar().getActualMinimum(p_field);
    }

    /**
     * Set the year to be the specified value.
     *
     * @param p_year - The year to be set.
     */
    public void setYear(int p_year)
    {
        getCalendar().set(YEAR, p_year);
    }


    /**
     * Get the current year of the Timestamp.
     *
     * @return The year of the Timestamp.
     */
    public int getYear()
    {
        return getCalendar().get(YEAR);
    }


    /**
     * Set the month to the specified value.  Note that Java uses a 
     * "zero-based"  month numbering system (so August is month 7 and NOT 
     * month 8).
     *
     * @param p_month - The month to be set.
     */
    public void setMonth(int p_month)
    {
        getCalendar().set(MONTH, p_month);
    }

    /**
     * Get the month of this Timestamp.
     *
     * @return The month of the Timestamp.
     */
    public int getMonth()
    {
        return getCalendar().get(MONTH);
    }

    /**
     * Set the day of the month to the specified value.
     *
     * @param p_day - The day of the month to be set.
     */
    public void setDayOfMonth(int p_day)
    {
        getCalendar().set(DAY_OF_MONTH, p_day);
    }

    /**
     * Get the value of the "day of the month".
     *
     * @return The day of the month value.
     */
    public int getDayOfMonth()
    {
        return getCalendar().get(DAY_OF_MONTH);
    }

    /**
     * Set the day of the week for this Timestamp.  Day of the week is 
     * basically defined in the range of 1 to 7 for Sunday through Saturday
     * respectively.
     *
     * @param p_dayOfWeek - The day of the week to be set.
     */
    public void setDayOfWeek(int p_dayOfWeek)
    {
        getCalendar().set(Calendar.DAY_OF_WEEK, p_dayOfWeek);
    }

    /**
     * Get the day of the week's value.
     *
     * @return The day of the week.
     */
    public int getDayOfWeek()
    {
        return getCalendar().get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Set the day of week in the month for this Timestamp.  This is 
     * basically defined as the ordinal number of the day of the week 
     * within the current month.
     *
     * @param p_dayOfWeekInMonth - The day of week in month to be set.
     */
    public void setDayOfWeekInMonth(int p_dayOfWeekInMonth)
    {
        getCalendar().set(DAY_OF_WEEK_IN_MONTH, p_dayOfWeekInMonth);
    }

    /**
     * Get the day of week in month value.
     *
     * @return The day of week in the current month.
     */
    public int getDayOfWeekInMonth()
    {
        return getCalendar().get(DAY_OF_WEEK_IN_MONTH);
    }

    /**
     * Set the hour to the specified value.  Note that java uses a "zero-based"
     * hour numbering system (0 to 23 where 0 is basically midnight).
     *
     * @param p_hour - The hour to be set.
    */
    public void setHour(int p_hour)
    {
        getCalendar().set(Calendar.HOUR_OF_DAY, p_hour);
    }

    /**
     * Get the hour value of this Timestamp.
     *
     * @return The Timestamp's hour.
     */
    public int getHour()
    {
        return getCalendar().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Set the minute to the specified value.
     *
     * @param p_minute - The minute of the Timestamp to be set.
     */
    public void setMinute(int p_minute)
    {
        getCalendar().set(Calendar.MINUTE, p_minute);
    }

    /**
     * Get the minute of the Timestamp.
     *
     * @return The Timestamp's minute.
     */
    public int getMinute()
    {
        return getCalendar().get(Calendar.MINUTE);
    }

    /**
     * Set the Timestamp's second to the specified value.
     *
     * @param p_second - The second to be set.
     */
    public void setSecond(int p_second)
    {
        getCalendar().set(Calendar.SECOND, p_second);
    }

    /**
     * Set the Timestamp's milliseconds to the specified value.
     *
     * @param p_millisecond - The millisecond to be set.
     */
    public void setMilliSecond(int p_millisecond)
    {
        getCalendar().set(Calendar.MILLISECOND, p_millisecond);
    }


    /**
     * Get the Timestamp's second.
     *
     * @return The second.
     */
    public int getSecond()
    {
        return getCalendar().get(Calendar.SECOND);
    }

    /**
     * Get the AM or PM value from the getCalendar().
     *
     * @return The AM/PM value.
     */
    public int getAmPm()
    {
        return getCalendar().get(Calendar.AM_PM);
    }

    /**
     * Determines whether this Timestamp has the same day as the other
     * one.  This method does not compare the whole Timestamp object.
     * it only checks the day, month, and year (time info is not checked
     * here).
     *
     * @return true if the two Timestamp objects have the same day.
     */
    public boolean isSameDay(Timestamp p_timestamp)
    {
        return getYear() == p_timestamp.getYear() &&
            getMonth() == p_timestamp.getMonth() && 
            getDayOfMonth() == p_timestamp.getDayOfMonth();
    }

    /**
     * Is this Timestamp equal to another?
     *
     * @return true if it is.
     */
    public boolean isEqualTo(Timestamp p_timestamp)
    {
        return this.compareTo(p_timestamp) == 0;
    }

    /**
     * Is this Timestamp greater than another?
     *
     * @return true if it is.
     */
    public boolean isGreaterThan(Timestamp p_timestamp)
    {
        return this.compareTo(p_timestamp) > 0;
    }

    /**
     *  Is this Timestamp greater than or equal to another?
     *
     *  @return true if it is.
     */
    public boolean isGreaterThanOrEqualTo(Timestamp p_timestamp)
    {
        return this.compareTo(p_timestamp) >= 0;
    }

    /**
     * Is this Timestamp less than another?
     *
     * @return true if it is.
     */
    public boolean isLessThan(Timestamp p_timestamp)
    {
        return !isGreaterThanOrEqualTo(p_timestamp);
    }

    /**
     * Is this Timestamp less than or equal to another?
     *
     * @return true if it is.
     */
    public boolean isLessThanOrEqualTo(Timestamp p_timestamp)
    {
        return !isGreaterThan(p_timestamp);
    }

    /**
     * Adds the specified (signed) amount of time to the given time field,
     * based on the calendar's rules. For example, to subtract 5 days from
     * the current time of the calendar, you can achieve it by calling:
     * add(Timestamp.DAY, -5).
     *
     * @param p_field the Calendar's time field.
     * @param p_amount the amount of date or time to be added to the field.
     */
    public void add(int p_field, int p_amount)
    {
        getCalendar().add(p_field, p_amount);
    }

    /**
     * Set the locale of this timestamp.
     *
     * @param p_locale the new locale to use.
     */
    public void setLocale(Locale p_locale)
    {
        m_locale = p_locale;
    }

    /**
     * Get the locale of this timestamp.
     *
     * @return the currently set locale.
     */
    public Locale getLocale()
    {
        return m_locale == null ? Locale.getDefault() : m_locale;
    }

    /**
     * Set the time zone of this Timestamp to be the specified value.
     * @param p_timeZone - The time zone to be set.
     */
    public void setTimeZone(TimeZone p_timeZone)
    {
        m_timeZone = p_timeZone;

        // reset the calendar to have the correct time zone
        Date currentDate = getDate();
        m_calendar = getDateFormat().getCalendar();
        setDate(currentDate);
    }

    /**
     * Get the time zone of this Timestamp.
     * @return The time zone of this Timestamp.  If not set,
     * The server's default time zone would be retured.
     */
    public TimeZone getTimeZone()
    {
        return m_timeZone == null ? TimeZone.getDefault() : m_timeZone;
    }

    /**
     * Reset the time of this timestamp to be 0h0m0s0ms.
     * This method is used when you want to go to the beginning
     * of a day.
     */
    public void resetTimeOfDay()
    {
        getCalendar().set(Calendar.HOUR_OF_DAY, 0);
        getCalendar().set(Calendar.MINUTE, 0);
        getCalendar().set(Calendar.SECOND, 0);
        getCalendar().set(Calendar.MILLISECOND, 0);
    }
    
    /**
     * Return a string representation of this timestamp based on the 
     * calendar's locale and time zone.  Note that if no locale or time
     * time zone is set, the server's default values would be used.
     */
    public String toString()
    {
        return getDateFormat().format(getCalendar().getTime());
    }

    /**
     *  @return a string that dumps the contents of the object.
     */
    public String toDebugString()
    {
        String type = "TIME";
        switch (m_type)
        {
            case DATE:
                type = "DATE";
                break;
            case TIME_AND_DATE:
                type = "TIME_AND_DATE";
                break;
        }
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" ");
        sb.append(toString());
        sb.append(", m_type=");
        sb.append(type);
        sb.append(", m_locale=");
        sb.append(m_locale);
        return sb.toString();
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* Return the DateFormat object based on the style (time, date, or both) */
    /* and the locale. */
    private DateFormat getDateFormat()
    {
        DateFormat df = null;
        switch (m_type)
        {
            default:
                df = DateFormat.
                    getDateTimeInstance(m_dateStyle, m_timeStyle, getLocale());
                break;
            case DATE:
                df = DateFormat.
                    getDateInstance(m_dateStyle, getLocale());
                break;
            case TIME:
                df = DateFormat.
                    getTimeInstance(m_timeStyle, getLocale());
                break;
        }      
        df.setTimeZone(getTimeZone());
        return df;
    }

    /* Return the calendar used by this timestamp */
    private Calendar getCalendar()
    {
        if (m_calendar == null)
        {
            m_calendar = getDateFormat().getCalendar();
        }
        return m_calendar;
    }

    /* Compare this Timestamp to another.  Return the difference of the */
    /* Timestamps in milliseconds. */
    private long compareTo(Timestamp p_timestamp)
    {
        return
            (getCalendar().getTime().getTime()) - 
            (p_timestamp.getCalendar().getTime().getTime());
    }

    // the following 2 methods are needed to deal with a Java bug,  The bug is that
    // Calendar, which is Serializable, does not deserialize itself with the time
    // which was stored at Serialization.
    private void writeObject(ObjectOutputStream out)
    throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(m_type);
        out.writeInt(m_dateStyle);
        out.writeInt(m_timeStyle);
        long currentTime = getDateFormat().getCalendar().getTime().getTime();
        out.writeLong(currentTime);
    }

    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException, NotActiveException
    {
        in.defaultReadObject();
        m_type = in.readInt();
        m_dateStyle = in.readInt();
        m_timeStyle = in.readInt();
        // retrieve the time that was stored & use it to set the calendar
        long storedTime = in.readLong();
        getCalendar().setTime(new Date(storedTime));
    }
}
