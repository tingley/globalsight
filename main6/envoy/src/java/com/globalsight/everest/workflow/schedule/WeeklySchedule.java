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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.globalsight.everest.workflow.Activity;

public class WeeklySchedule extends CompleteScheduleItem
{

    public WeeklySchedule(ActivityCompleteSchedule schedule)
    {
        super(schedule);
    }

    @Override
    public void updateScheduleValue(HttpServletRequest request)
    {
        int recur = 0;
        List<Integer> weeklyWeeks = new ArrayList<>();
        if (matchTheType())
        {
            recur = Integer.parseInt(request.getParameter("weeklyRecur"));
            String[] ws = request.getParameterValues("weeklyWeeks");
            if (ws != null)
            {
                for (String w : ws)
                {
                    weeklyWeeks.add(Integer.parseInt(w));
                }
            }
        }
        getSchedule().setWeeklyRecur(recur);
        getSchedule().setWeeklyWeeks(weeklyWeeks);
    }

    @Override
    public boolean matchTheType()
    {
        return getSchedule().getScheduleType() == Activity.COMPLETE_TYPE_WEEKLY;
    }

    @Override
    public boolean autoComplete()
    {
        if (!matchStartTime())
            return false;
        
        ActivityCompleteSchedule schedule = getSchedule();
        Calendar start = schedule.getStartTime();
        Calendar now = getNow();
        
        long ws = start.getTimeInMillis() / (1000 * 60 * 60 * 24 * 7);
        long wn = now.getTimeInMillis() / (1000 * 60 * 60 * 24 * 7);
        if ((wn - ws) % schedule.getWeeklyRecur() > 0)
        {
            return false;
        }
        
        return inWeeks();
    }
    
    private boolean inWeeks()
    {
        Calendar now = getNow();
        int sw = now.get(Calendar.DAY_OF_WEEK) - 1;
        
        ActivityCompleteSchedule schedule = getSchedule();
        List<Integer> ws = schedule.getWeeklyWeeks();
        
        return ws.contains(sw);
    }

    @Override
    protected String validateData(HttpServletRequest request)
    {
        if (!matchTheType())
            return null;
        
        if (!isInteger(request.getParameter("weeklyRecur"), 0, Integer.MAX_VALUE))
            return "msg_activity_invalid_recur_week";
        
        String[] ws = request.getParameterValues("weeklyWeeks");
        if (ws == null)
            return "msg_activity_no_day";
        
        return null;
    }

}
