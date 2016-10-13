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

import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.workflow.Activity;

public class ActivityScheduleUtil
{
    public static boolean  autoCompleteActivity(Activity act)
    {
        Calendar now = Calendar.getInstance();
        ActivityCompleteSchedule schedule = XmlUtil.string2Object(ActivityCompleteSchedule.class,
                act.getCompleteSchedule());
        List<CompleteScheduleItem> items = getAllSchedule(schedule);
        for (CompleteScheduleItem item : items)
        {
            item.setNow(now);
            if (item.autoComplete())
            {
                return true;
            }
        }
        
        return false;
    }

    public static void updateSchedule(ActivityCompleteSchedule schedule, HttpServletRequest request)
    {
        int scheduleType = Integer.parseInt(request.getParameter("scheduleType"));
        schedule.setScheduleType(scheduleType);
        schedule.setStartDate(request.getParameter("startDate"));
        schedule.setStartH(Integer.parseInt(request.getParameter("startH")));
        schedule.setStartM(Integer.parseInt(request.getParameter("startM")));
        
        List<CompleteScheduleItem> items = getAllSchedule(schedule);
        for (CompleteScheduleItem item : items)
        {
            item.updateScheduleValue(request);
        }
    }
    
    protected static boolean isInteger(String s, int mix, int max)
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
    
    public static String validateStartTime(HttpServletRequest request)
    {
        if (!isInteger(request.getParameter("scheduleType"), Integer.MIN_VALUE, Integer.MAX_VALUE))
            return "msg_activity_schedule_not_check";
        
        String startDate = request.getParameter("startDate"); 
        String startH = request.getParameter("startH");
        String startM = request.getParameter("startM"); 
        
        if (startDate == null || startDate.length() == 0)
            return "msg_activity_invalid_start_day";
        
        if (!isInteger(startH, 0, Integer.MAX_VALUE))
            return "msg_activity_invalid_start_hour";

        if (!isInteger(startM, 0, Integer.MAX_VALUE))
            return "msg_activity_invalid_start_minute";

        return null;
    }
    
    public static String validateSchedule(HttpServletRequest request)
    {
        String msg = validateStartTime(request);
        if (msg != null)
            return msg;
        
        ActivityCompleteSchedule schedule = new ActivityCompleteSchedule();
        String type = request.getParameter("scheduleType");
        if (type == null)
            return "msg_activity_schedule_not_check";
        
        int scheduleType = Integer.parseInt(type);
        schedule.setScheduleType(scheduleType);
        
        List<CompleteScheduleItem> items = getAllSchedule(schedule);
        for (CompleteScheduleItem item : items)
        {
            msg = item.validateData(request);
            if (msg != null)
                return msg;
        }
        
        return null;
    }

    private static List<CompleteScheduleItem> getAllSchedule(ActivityCompleteSchedule schedule)
    {
        List<CompleteScheduleItem> items = new ArrayList<>();
        DailySchedule d = new DailySchedule(schedule);
        items.add(d);

        WeeklySchedule w = new WeeklySchedule(schedule);
        items.add(w);

        MonthlySchedule m = new MonthlySchedule(schedule);
        items.add(m);
        
        OneTimeSchedule o = new OneTimeSchedule(schedule);
        items.add(o);
        
        return items;
    }
}
