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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

@XmlRootElement
public class ActivityCompleteSchedule
{
    static private final Logger logger = Logger.getLogger(ActivityCompleteSchedule.class);
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    private int scheduleType;
    // the format is MM/dd/yyyy
    private String startDate;
    private int startH;
    private int startM;

    private int dailyRecur;
    private int weeklyRecur;
    private List<Integer> weeklyWeeks;

    private int monthCondition;
    private List<Integer> monthlyMonths;
    private List<Integer> monthlyDays;
    private List<Integer> monthlyOnPrefix;
    private List<Integer> monthlyOnWeek;

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public int getStartH()
    {
        return startH;
    }

    public void setStartH(int startH)
    {
        this.startH = startH;
    }

    public int getStartM()
    {
        return startM;
    }

    public void setStartM(int startM)
    {
        this.startM = startM;
    }

    public int getDailyRecur()
    {
        return dailyRecur;
    }

    public void setDailyRecur(int dailyRecur)
    {
        this.dailyRecur = dailyRecur;
    }

    public int getWeeklyRecur()
    {
        return weeklyRecur;
    }

    public void setWeeklyRecur(int weeklyRecur)
    {
        this.weeklyRecur = weeklyRecur;
    }

    public List<Integer> getWeeklyWeeks()
    {
        return weeklyWeeks;
    }

    public void setWeeklyWeeks(List<Integer> weeklyWeeks)
    {
        this.weeklyWeeks = weeklyWeeks;
    }

    public List<Integer> getMonthlyMonths()
    {
        return monthlyMonths;
    }

    public void setMonthlyMonths(List<Integer> monthlyMonths)
    {
        this.monthlyMonths = monthlyMonths;
    }

    public List<Integer> getMonthlyDays()
    {
        return monthlyDays;
    }

    public void setMonthlyDays(List<Integer> monthlyDays)
    {
        this.monthlyDays = monthlyDays;
    }

    public List<Integer> getMonthlyOnPrefix()
    {
        return monthlyOnPrefix;
    }

    public void setMonthlyOnPrefix(List<Integer> monthlyOnPrefix)
    {
        this.monthlyOnPrefix = monthlyOnPrefix;
    }

    public List<Integer> getMonthlyOnWeek()
    {
        return monthlyOnWeek;
    }

    public void setMonthlyOnWeek(List<Integer> monthlyOnWeek)
    {
        this.monthlyOnWeek = monthlyOnWeek;
    }

    public int getScheduleType()
    {
        return scheduleType;
    }

    public void setScheduleType(int scheduleType)
    {
        this.scheduleType = scheduleType;
    }

    public int getMonthCondition()
    {
        return monthCondition;
    }

    public void setMonthCondition(int monthCondition)
    {
        this.monthCondition = monthCondition;
    }

    public Calendar getStartTime()
    {
        try
        {
            Date date = DATE_FORMAT.parse(startDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            
            calendar.set(Calendar.HOUR_OF_DAY, startH);
            calendar.set(Calendar.MINUTE, startM);
            return calendar;
        }
        catch (ParseException e)
        {
            logger.error(e);
        }

        return null;
    }
}
