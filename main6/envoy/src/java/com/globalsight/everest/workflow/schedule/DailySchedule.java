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

import com.globalsight.everest.workflow.Activity;

public class DailySchedule extends CompleteScheduleItem
{
    public DailySchedule(ActivityCompleteSchedule schedule)
    {
        super(schedule);
    }

    @Override
    public void updateScheduleValue(HttpServletRequest request)
    {
        int recur = 0;
        if (matchTheType())
        {
            recur = Integer.parseInt(request.getParameter("dailyRecur"));
        }
        getSchedule().setDailyRecur(recur);
    }

    @Override
    public boolean autoComplete()
    {
        if (!matchStartTime())
            return false;

        ActivityCompleteSchedule schedule = getSchedule();
        Calendar start = schedule.getStartTime();
        Calendar now = getNow();
        if (now.before(start))
            return false;

        int recur = schedule.getDailyRecur();
        
        long ds = start.getTimeInMillis() / (1000 * 60 * 60 * 24);
        long dn = now.getTimeInMillis() / (1000 * 60 * 60 * 24);
        
        long dif = dn - ds;
        if (recur > 0)
        {
            dif = dif % recur;
        }
        
        if (dif == 0)
            return true;

        return false;
    }

    @Override
    public boolean matchTheType()
    {
        return getSchedule().getScheduleType() == Activity.COMPLETE_TYPE_DAILY;
    }

    @Override
    protected String validateData(HttpServletRequest request)
    {
        if (!matchTheType())
            return null;
        
        if (!isInteger(request.getParameter("dailyRecur"), 0, Integer.MAX_VALUE))
            return "msg_activity_invalid_recur_day";
        
        return null;
    }
}
