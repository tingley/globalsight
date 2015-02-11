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
package com.globalsight.util.date;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * DateHelper provides static method for date and time formatting.
 */
public class DateHelper
{
    //
    // PRIVATE CONSTANTS
    //
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long SECONDS_PER_MINUTE = 60;
    private static final long MINUTES_PER_HOUR = 60;
    private static final long HOURS_PER_DAY = 24;
    private static final long SECONDS_IN_HOUR = SECONDS_PER_MINUTE *
                                               MINUTES_PER_HOUR;
    private static final long SECONDS_IN_DAY = SECONDS_IN_HOUR *
                                               HOURS_PER_DAY;  // 86400 = 24 * 60 *60

    private static Timestamp m_dateTs = null;
    private static Timestamp m_dateTimeTs = null;


    //
    // PUBLIC STATIC METHODS
    //
    /**
     * This will convert milliseconds to days and also round up if
     * any remainder is left over.
     */
    public static int convertMillisecondsToDays(long p_timeInMilliseconds)
    {
        return millisecondsToDays(p_timeInMilliseconds, SECONDS_IN_DAY);
    }

    /**
     * This will convert milliseconds to days and also round up if
     * any remainder is left over.
     *
     * @param p_timeInMilliseconds The time in ms that should be converted
     * to days.
     * @param p_hoursPerBusinessDay - The conversion factor that indicates
     * the number of business hours within a day.  This value is an 
     * attribute of a calendar.
     *
     * @return The time in days.
     */
    public static int convertMillisecondsToDays(long p_timeInMilliseconds,
                                                long p_hoursPerBusinessDay)
    {
        long secondsInDay = SECONDS_IN_HOUR * p_hoursPerBusinessDay;
        return millisecondsToDays(p_timeInMilliseconds, secondsInDay);        
    }

    
    /**
     * Convert the given number of milliseconds into a string of the
     * form "02d 03h 45m", where the abbreviation characters are
     * obtained from the given resource bundle
     *
     * @param p_milliseconds a long value representing the number of
     * milliseconds to convert
     * @param p_dayAbbrev the day abbreviation to use
     * @param p_hourAbbrev the hour abbreviation to use
     * @param p_minuteAbbrev the minute abbreviation to use
     *
     * @return a string representation of the input value
     */
    public static String daysHoursMinutes(long p_milliseconds,
        String p_dayAbbrev, String p_hourAbbrev, String p_minuteAbbrev)
    {
        return daysHoursMinutes(p_milliseconds, p_dayAbbrev, 
                                p_hourAbbrev, p_minuteAbbrev, 
                                HOURS_PER_DAY);
    }

    /**
     * Convert the given number of milliseconds into a string of the
     * form "02d 03h 45m", where the abbreviation characters are
     * obtained from the given resource bundle
     *
     * @param p_milliseconds a long value representing the number of
     * milliseconds to convert
     * @param p_dayAbbrev the day abbreviation to use
     * @param p_hourAbbrev the hour abbreviation to use
     * @param p_minuteAbbrev the minute abbreviation to use
     * @param p_hoursPerBusinessDay - The conversion factor that indicates
     * the number of business hours within a day.  This value is an 
     * attribute of a calendar.
     *
     * @return a string representation of the input value
     */
    public static String daysHoursMinutes(
        long p_milliseconds, String p_dayAbbrev, 
        String p_hourAbbrev, String p_minuteAbbrev,
        long p_hoursPerBusinessDay)
    {
        long[] dhm = daysHoursMinutes(p_milliseconds, 
                                      p_hoursPerBusinessDay);
        StringBuffer sb = new StringBuffer();
        sb.append(pad(dhm[0]));
        sb.append(p_dayAbbrev);
        sb.append(" ");
        sb.append(pad(dhm[1]));
        sb.append(p_hourAbbrev);
        sb.append(" ");
        sb.append(pad(dhm[2]));
        sb.append(p_minuteAbbrev);

        return sb.toString();
    }

    /**
     * Convert the given number of milliseconds into an array of longs,
     * where each long represents the days, hours, minutes respectively.
     *
     * @param p_milliseconds a long value representing the number of milliseconds
     * to convert
     *
     * @return an array of longs, where long[0] = days, long[1] = hours, and
     * long[2] = minutes.
     */
    public static long[] daysHoursMinutes(long p_milliseconds)
    {
        return daysHoursMinutes(p_milliseconds, HOURS_PER_DAY);
    }

    /**
     * Convert the given number of milliseconds into an array of longs,
     * where each long represents the days, hours, minutes respectively.
     *
     * @param p_milliseconds a long value representing the number of milliseconds
     * to convert
     * @param p_hoursPerBusinessDay - The conversion factor that indicates
     * the number of business hours within a day.  This value is an 
     * attribute of a calendar.
     *
     * @return an array of longs, where long[0] = days, long[1] = hours, and
     * long[2] = minutes.
     */
    public static long[] daysHoursMinutes(long p_milliseconds,
                                          long p_hoursPerBusinessDay)
    {
        long[] dhm = new long[3];
        long seconds = p_milliseconds / MILLIS_PER_SECOND;
        long minutes = seconds / SECONDS_PER_MINUTE;
        long hours = minutes / MINUTES_PER_HOUR;
        minutes -= (hours * MINUTES_PER_HOUR);
        long days = hours / p_hoursPerBusinessDay;
        hours -= (days * p_hoursPerBusinessDay);
        dhm[0] = days;
        dhm[1] = hours;
        dhm[2] = minutes;
        return dhm;
    }

    /**
     * Get the display date for the given date object.  System's default
     * time zone and locale will be used.
     * @param p_date - The date to be displayed.
     */
    public static String getFormattedDate(Date p_date)
    {
        return getFormattedDate(p_date, null, null);
    }

    /**
     * Get the display date based on the given locale.  System's default
     * time zone will be used.
     * @param p_date - The date to be displayed.
     * @param p_displayLocale - The locale to be used for displaying date.
     */
    public static String getFormattedDate(Date p_date, Locale p_displayLocale)
    {
        return getFormattedDate(p_date, p_displayLocale, null);
    }

    /**
     * Get the display date based on the given locale, and time zone.
     * @param p_date - The date to be displayed.
     * @param p_displayLocale - The locale to be used for displaying date.
     * @param p_timeZone - Time zone to be used for displaying date.
     */
    public static String getFormattedDate(Date p_date, 
                                          Locale p_displayLocale,
                                          TimeZone p_timeZone)
    {
        String formattedDate = null;
        
        Timestamp ts = getDateTimestamp(p_timeZone);
        ts.setLocale(p_displayLocale);
        ts.setDate(p_date);
        formattedDate = ts.toString();
        
        return formattedDate;
    }

    /**
     * Get the display date and time for the given date object.
     * System's default time zone and locale will be used.
     * @param p_date - The date and time to be displayed.
     */
    public static String getFormattedDateAndTime(Date p_date)
    {
        return getFormattedDateAndTime(p_date, null);
    }

    /**
     * Get the display date and time based on the given locale.
     * System's default time zone will be used.
     * @param p_date - The date to be displayed.
     * @param p_displayLocale - The locale to be used for displaying date.
     */
    public static String getFormattedDateAndTime(Date p_date,
                                                 Locale p_displayLocale)
    {
        return getFormattedDateAndTime(p_date, p_displayLocale, null);
    }
    
    /**
     * Get the display date and time based on the given User.
     * System's default time zone will be used.
     * @param p_date - The date to be displayed.
     * @param p_user - Gets Locale from user.
     */
    public static String getFormattedDateAndTimeFromUser(Date p_date,
                                                 User p_user)
    {
        Locale locale = getUserLocal(p_user);
        return getFormattedDateAndTime(p_date, locale, null);
    }

    /**
     * Get the display date and time based on the given locale, and time zone.
     * @param p_date - The date to be displayed (as date and time).
     * @param p_displayLocale - The locale to be used for displaying date.
     * @param p_timeZone - Time zone to be used for displaying date.
     */
    public static String getFormattedDateAndTime(Date p_date, 
                                                 Locale p_displayLocale,
                                                 TimeZone p_timeZone)
    {
        String formattedDate = null;
        
        Timestamp ts = getDateAndTimeTimestamp(p_timeZone);
        ts.setLocale(p_displayLocale);
        ts.setDate(p_date);
        formattedDate = ts.toString();
        
        return formattedDate;
    }

    /**
     * Convert the given number of milliseconds into an array of longs,
     * where each long represents the hours, and minutes respectively.
     *
     * @param p_milliseconds a long value representing the number of milliseconds
     * to convert
     *
     * @return an array of longs, where long[0] = hours, and
     * long[1] = minutes.
     */
    public static long[] hoursMinutes(long p_milliseconds)
    {
        long[] hm = new long[2];
        long seconds = p_milliseconds / MILLIS_PER_SECOND;
        long minutes = seconds / SECONDS_PER_MINUTE;
        long hours = minutes / MINUTES_PER_HOUR;
        minutes -= (hours * MINUTES_PER_HOUR);
        
        hm[0] = hours;
        hm[1] = minutes;
        return hm;
    }
    
    public static long milliseconds(String p_days, String p_hours, String p_minutes)
    {
        return milliseconds(Long.valueOf(p_days), Long.valueOf(p_hours), Long.valueOf(p_minutes), HOURS_PER_DAY);
    }

    /**
     * Convert the given number of days, hours, and minutes into the
     * corresponding number of milliseconds.  Return the result.
     *
     * @param p_days a long value representing the number of days
     * @param p_hours a long value representing the number of hours
     * @param p_minutes a long value representing the number of minutes
     *
     * @return a long value representing the number of milliseconds
     */
    public static long milliseconds(long p_days, long p_hours, long p_minutes)
    {
        return milliseconds(p_days, p_hours, p_minutes, HOURS_PER_DAY);
    }

    /**
     * Convert the given number of days, hours, and minutes into the
     * corresponding number of milliseconds.  Return the result.
     *
     * @param p_days a long value representing the number of days
     * @param p_hours a long value representing the number of hours
     * @param p_minutes a long value representing the number of minutes
     * @param p_hoursPerBusinessDay - The conversion factor that indicates
     * the number of business hours within a day.  This value is an 
     * attribute of a calendar.
     *
     * @return a long value representing the number of milliseconds
     */
    public static long milliseconds(long p_days, long p_hours, 
                                    long p_minutes, 
                                    long p_hoursPerBusinessDay)
    {
        long seconds = p_days * p_hoursPerBusinessDay 
            * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;
        seconds += p_hours * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;
        seconds += p_minutes * SECONDS_PER_MINUTE;
        return seconds * MILLIS_PER_SECOND;
    }

    
    //
    // PRIVATE STATIC SUPPORT METHODS
    //

    /*
     * This will convert milliseconds to days and also round up if
     * any remainder is left over.
     */
    private static int millisecondsToDays(long p_timeInMilliseconds,
                                          long p_secondsInDay)
    {
        int seconds = (new Long(p_timeInMilliseconds/MILLIS_PER_SECOND)).intValue();
        int days = 0;

        // a remainder is found
        if ( Math.IEEEremainder(seconds, p_secondsInDay) != 0 ) 
        {
            days += 1;
        }

        if ( seconds >= p_secondsInDay )
        {
            days += seconds/p_secondsInDay;
        }
        return days;
    }

    /* Pad the given number so that it will have a leading zero if necessary */
    private static String pad(long p_number)
    {

        return (p_number < 10 ? "0" : "") + p_number;
    }

    /* lazy instantiation of date-only timestamp */
    private static Timestamp getDateTimestamp(TimeZone p_timeZone)
    {
        if (m_dateTs == null)
        {
            m_dateTs = new Timestamp(Timestamp.DATE, p_timeZone);
        }

        return m_dateTs;
    }

    /* lazy instantiation of date and time timestamp */
    private static Timestamp getDateAndTimeTimestamp(TimeZone p_timeZone)
    {
        if (m_dateTimeTs == null)
        {
            m_dateTimeTs = new Timestamp(p_timeZone);
        }

        return m_dateTimeTs;
    }
    
    /**
     * Get default UI locale information for specified user
     *
     * @param p_user User information
     * @return Locale Default UI locale for the specified user
     */
    private static Locale getUserLocal(User p_user)
    {
        String dl = p_user.getDefaultUILocale();
        if (dl == null)
            return new Locale("en", "US");
        else
        {
            try
            {
                String language = dl.substring(0, dl.indexOf("_"));
                String country = dl.substring(dl.indexOf("_") + 1);
                country = (country == null) ? "" : country;

                return new Locale(language, country);
            }
            catch (Exception e)
            {
                return new Locale("en", "US");
            }
        }
    }
}
