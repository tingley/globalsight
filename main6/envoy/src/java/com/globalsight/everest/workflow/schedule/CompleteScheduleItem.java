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
package com.globalsight.everest.workflow.schedule;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

public abstract class CompleteScheduleItem
{
    private ActivityCompleteSchedule schedule;
    private Calendar now;

    public CompleteScheduleItem(ActivityCompleteSchedule schedule)
    {
        super();
        this.schedule = schedule;
    }

    public ActivityCompleteSchedule getSchedule()
    {
        return schedule;
    }

    public void setSchedule(ActivityCompleteSchedule schedule)
    {
        this.schedule = schedule;
    }

    public boolean matchStartTime()
    {
        if (!matchTheType())
            return false;

        ActivityCompleteSchedule schedule = getSchedule();
        Calendar start = schedule.getStartTime();
        if (now.before(start))
            return false;
        
        long ms = start.getTimeInMillis() / (1000 * 60);
        long mn = now.getTimeInMillis() / (1000 * 60);
        
        long diff = (mn - ms) % 60;

        // the run time can last 30 min than the start time.
        return diff < 30;
    }

    public Calendar getNow()
    {
        return now;
    }

    public void setNow(Calendar now)
    {
        this.now = now;
    }

    public abstract boolean matchTheType();

    public abstract void updateScheduleValue(HttpServletRequest request);

    public abstract boolean autoComplete();
    
    protected boolean isInteger(String s, int mix, int max)
    {
        if (s == null || s.length() == 0)
            return false;
        try
        {
            int n = Integer.parseInt(s);
            return n >= mix && n <=max;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    protected abstract String validateData(HttpServletRequest request);
}
