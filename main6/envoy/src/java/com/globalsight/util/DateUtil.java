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
package com.globalsight.util;

public class DateUtil
{
    /**
     * Formats a duration into a string use the format pattern.<br>
     * Pattern example:<br>&nbsp;&nbsp;&nbsp;&nbsp;
     *     %h hour %m minute %s second %ms millisecond
     * @param duration duration in long format
     * @param pattern pattern to apply
     * @return the formatted string
     */
    public static String formatDuration(long duration, String pattern) {
        DurationFormater time = new DurationFormater(duration, pattern);
        return time.getString();
    }

    /**
     * Formats a duration into a string like 3 days 25 hours 20 minute
     * @param duration
     * @return the formatted string
     */
    public static String formatDuration(long duration) {
        DurationFormater time = new DurationFormater(duration);
        return time.getDefaultDurationString();
    }
}

class DurationFormater
{
    public static final long MILISECONDS_PER_SECOND = 1000;
    public static final long SECONDS_PER_MINUTE = 60;
    public static final long MINUTES_PER_HOUR = 60;
    public static final long HOURS_PER_DAY = 24;

    public static final int MILISECOND = 0;
    public static final int SECOND = 1;
    public static final int MINUTE = 2;
    public static final int HOUR = 3;
    public static final int DAY = 4;

    public static final String PATTERNS[] = { "%ms", "%s", "%m", "%h", "%d" };

    private static final long[] AMOUNTS = { MILISECONDS_PER_SECOND,
            SECONDS_PER_MINUTE, MINUTES_PER_HOUR, HOURS_PER_DAY };

    private String pattern = "%h hour %m minute %s second %ms milisecond";
    private boolean detail = false;

    private long[] times = new long[5];
    private long time;

    public DurationFormater()
    {
    }

    public DurationFormater(long time, String pattern)
    {
        this.time = time;
        this.pattern = pattern;
        update();
    }

    public DurationFormater(long time)
    {
        this.time = time;
        update();
    }

    private void update()
    {
        long remain = time;
        for (int i = 0; i < AMOUNTS.length; i++)
        {
            times[i] = remain % AMOUNTS[i];
            remain = remain / AMOUNTS[i];
        }
        times[DAY] = (int) remain;
    }

    /**
     * i.e.: &quot;@h hour @m minute @s second @ms milisecond&quot;
     */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long duration)
    {
        time = duration;
        update();
    }

    public long getMiliseconds()
    {
        return times[MILISECOND];
    }

    public long getSeconds()
    {
        return times[SECOND];
    }

    public long getMinutes()
    {
        return times[MINUTE];
    }

    public long getHours()
    {
        return times[HOUR];
    }

    public long getDays()
    {
        return times[DAY];
    }

    public void setDetail(boolean detail)
    {
        this.detail = detail;
    }

    public String getString()
    {
        StringBuilder s = new StringBuilder(pattern);
        for (int i = 0; i < PATTERNS.length; i++)
        {
            int start = -1;
            int end = -1;
            while ((start = s.toString().indexOf(PATTERNS[i])) > -1)
            {
                end = start + PATTERNS[i].length();
                s.replace(start, end, String.valueOf(times[i]));
            }
        }
        return s.toString();
    }
    
    public String getDefaultDurationString()
    {
        StringBuilder desc = new StringBuilder(256);
        if (times[DAY] > 0)
        {
            desc.append(checkPlural(times[DAY], "day"));
        }
        if (times[HOUR] > 0)
        {
            desc.append(checkPlural(times[HOUR], "hour"));
        }
        if ((times[MINUTE] > 0) || (times[DAY] == 0 && times[MINUTE] == 0))
        {
            desc.append(checkPlural(times[MINUTE], "minute"));
        }
        if (detail)
        {
            desc.append(checkPlural(times[SECOND], "second"));
            desc.append(checkPlural(times[MILISECOND], "milisecond"));
        }
        return desc.toString();
    }

    public String toString()
    {
        if (pattern != null)
        {
            return getString();
        }
        return getDefaultDurationString();
    }

    private static String checkPlural(long amount, String unit)
    {
        StringBuilder desc = new StringBuilder(20);
        desc.append(amount).append(" ").append(unit);
        if (amount > 1)
        {
            desc.append("s");
        }
        return desc.append(" ").toString();
    }
}