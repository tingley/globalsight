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

public class MonthlySchedule extends CompleteScheduleItem
{

    public MonthlySchedule(ActivityCompleteSchedule schedule)
    {
        super(schedule);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addValues(HttpServletRequest request, String name, List l)
    {
        String[] ms = request.getParameterValues(name);
        if (ms != null)
        {
            for (String m : ms)
            {
                l.add(Integer.parseInt(m));
            }
        }
    }

    @Override
    public void updateScheduleValue(HttpServletRequest request)
    {
        // TODO Auto-generated method stub
        List<Integer> monthlyMonths = new ArrayList<>();
        int monthCondition = 0;
        List<Integer> monthlyDays = new ArrayList<>();
        List<Integer> monthlyOnPrefix = new ArrayList<>();
        List<Integer> monthlyOnWeek = new ArrayList<>();
        if (matchTheType())
        {
            addValues(request, "monthlyMonths", monthlyMonths);

            String m = request.getParameter("monthCondition");
            if (m != null)
                monthCondition = Integer.parseInt(m);

            if (monthCondition == 1)
            {
                addValues(request, "monthlyDays", monthlyDays);
            }
            else
            {
                addValues(request, "monthlyOnPrefix", monthlyOnPrefix);
                addValues(request, "monthlyOnWeek", monthlyOnWeek);
            }
        }

        ActivityCompleteSchedule schedule = getSchedule();
        schedule.setMonthlyDays(monthlyDays);
        schedule.setMonthlyMonths(monthlyMonths);
        schedule.setMonthlyOnPrefix(monthlyOnPrefix);
        schedule.setMonthlyOnWeek(monthlyOnWeek);
        schedule.setMonthCondition(monthCondition);
    }

    @Override
    public boolean matchTheType()
    {
        return getSchedule().getScheduleType() == Activity.COMPLETE_TYPE_MONTHLY;
    }

    @Override
    public boolean autoComplete()
    {
        if (!matchStartTime())
            return false;

        if (!inMonths())
            return false;

        if (!inDays())
            return false;
        
        if (!inOns())
            return false;

        return true;
    }

    private boolean inMonths()
    {
        Calendar now = getNow();
        int m = now.get(Calendar.MONTH) + 1;

        ActivityCompleteSchedule schedule = getSchedule();
        List<Integer> ms = schedule.getMonthlyMonths();

        return ms.contains(m);
    }

    private boolean inDays()
    {
        ActivityCompleteSchedule schedule = getSchedule();
        if (schedule.getMonthCondition() != 1)
        {
            return true;
        }

        Calendar now = getNow();
        int sw = now.get(Calendar.DAY_OF_MONTH);
        List<Integer> ws = schedule.getMonthlyDays();

        // the last day
        if (ws.contains(32))
        {
            int last = now.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (last == sw)
                return true;
        }

        return ws.contains(sw);
    }

    private boolean inOns()
    {
        ActivityCompleteSchedule schedule = getSchedule();
        if (schedule.getMonthCondition() != 2)
        {
            return true;
        }

        Calendar now = getNow();
        int sw = now.get(Calendar.DAY_OF_WEEK) - 1;
        
        List<Integer> ws = schedule.getMonthlyOnWeek();
        if (!ws.contains(sw))
            return false;
        
        List<Integer> wms = schedule.getMonthlyOnPrefix();
        int wm = now.get(Calendar.WEEK_OF_MONTH);
        if (wms.contains(5))
        {
            int last = now.getActualMaximum(Calendar.WEEK_OF_MONTH);
            if (last == wm)
                return true;
        }
        
        return wms.contains(wm);
    }

    @Override
    protected String validateData(HttpServletRequest request)
    {
        if (!matchTheType())
            return null;
        
        String[] ws = request.getParameterValues("monthlyMonths");
        if (ws == null)
            return "msg_activity_no_month";
        
        String m = request.getParameter("monthCondition");
        if (!isInteger(m, 1, 2))
            return "msg_activity_monthly_no_option";
        
        if (m.equals("1"))
        {
            String[] ds = request.getParameterValues("monthlyDays");
            if (ds == null)
                return "msg_activity_monthly_no_day";
        }
        else
        {
            String[] monthlyOnPrefix = request.getParameterValues("monthlyOnPrefix");
            String[] monthlyOnWeek = request.getParameterValues("monthlyOnWeek");
            if (monthlyOnPrefix == null || monthlyOnWeek == null)
                return "msg_activity_invalid_on";
        }
        
        return null;
    }
}
