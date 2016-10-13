package com.globalsight.everest.workflow.schedule;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import com.globalsight.everest.workflow.Activity;

public class OneTimeSchedule extends CompleteScheduleItem
{
    public OneTimeSchedule(ActivityCompleteSchedule schedule)
    {
        super(schedule);
    }

    @Override
    public boolean matchTheType()
    {
        return getSchedule().getScheduleType() == Activity.COMPLETE_TYPE_ONE_TIME;
    }

    @Override
    public void updateScheduleValue(HttpServletRequest request)
    {
        // do nothing
    }

    @Override
    public boolean autoComplete()
    {
        if (!matchTheType())
            return false;
        
        ActivityCompleteSchedule schedule = getSchedule();
        Calendar start = schedule.getStartTime();
        
        return getNow().after(start);
    }

    @Override
    protected String validateData(HttpServletRequest request)
    {
        return null;
    }
}
