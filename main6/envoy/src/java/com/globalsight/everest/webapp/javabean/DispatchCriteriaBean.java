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
package com.globalsight.everest.webapp.javabean;

/**
 * DispatchCriteriaBean, A data bean used to pass DispatchCriteria info to JSPs.
 * <p>
 * @author  Bethany Wang
 * @version 1.0
 */

/*
 * MODIFIED     MM/DD/YYYY
 * BWang        12/20/2000   Initial version.
 */
public class DispatchCriteriaBean
{
    private boolean immediate;
    private boolean andConditions;
    private boolean volumeChecked;
    private boolean timeChecked;
    private boolean beforeTM;
    private boolean byDay;

    private String volume;
    private int startHour1;
    private int startHour2;
    private String interval;
    private String intervalUnit;

    private boolean everyDays;
    private boolean mondayChecked;
    private boolean tuesdayChecked;
    private boolean wednesdayChecked;
    private boolean thursdayChecked;
    private boolean fridayChecked;
    private boolean saturdayChecked;
    private boolean sundayChecked;

    public DispatchCriteriaBean()
    {
    }

    public boolean isImmediate()
    {
        return immediate;
    }

    public void setImmediate(boolean p_immediate)
    {
        immediate=p_immediate;
    }

    public boolean isAndConditions()
    {
        return andConditions;
    }

    public void setAndConditions(boolean p_andConditions)
    {
        andConditions=p_andConditions;
    }

    public boolean isVolumeChecked()
    {
        return volumeChecked;
    }

    public void setVolumeChecked(boolean p_volumeChecked)
    {
        volumeChecked=p_volumeChecked;
    }

    public boolean isTimeChecked()
    {
        return timeChecked;
    }

    public void setTimeChecked(boolean p_timeChecked)
    {
        timeChecked=p_timeChecked;
    }

    public boolean isBeforeTM()
    {
        return beforeTM;
    }

    public void setBeforeTM(boolean p_beforeTM)
    {
        beforeTM=p_beforeTM;
    }

    public boolean isByDay()
    {
        return byDay;
    }

    public void setByDay(boolean p_byDay)
    {
        byDay=p_byDay;
    }

    public String getVolume()
    {
        return volume;
    }

    public void setVolume(String p_volume)
    {
        volume=p_volume;
    }

    public int getStartHour1()
    {
        return startHour1;
    }

    public void setStartHour1(int p_startHour)
    {
        startHour1=p_startHour;
    }

    public int getStartHour2()
    {
        return startHour2;
    }

    public void setStartHour2(int p_startHour)
    {
        startHour2=p_startHour;
    }

    public String getInterval()
    {
        return interval;
    }

    public void setInterval(String p_interval)
    {
        interval=p_interval;
    }

    public String getIntervalUnit()
    {
        return intervalUnit;
    }

    public void setIntervalUnit(String p_intervalUnit)
    {
        intervalUnit=p_intervalUnit;
    }

    public boolean isEveryDays()
    {
        return everyDays;
    }

    public void setEveryDays(boolean p_everyDays)
    {
        everyDays=p_everyDays;
    }

    public boolean isMondayChecked()
    {
        return mondayChecked;
    }

    public void setMondayChecked(boolean p_mondayChecked)
    {
        mondayChecked=p_mondayChecked;
    }

    public boolean isTuesdayChecked ()
    {
        return tuesdayChecked;
    }

    public void setTuesdayChecked(boolean p_tuesdayChecked)
    {
        tuesdayChecked=p_tuesdayChecked;
    }

    public boolean isWednesdayChecked()
    {
        return wednesdayChecked;
    }

    public void setWednesdayChecked(boolean p_wednesdayChecked)
    {
        wednesdayChecked=p_wednesdayChecked;
    }

    public boolean isThursdayChecked()
    {
        return thursdayChecked;
    }

    public void setThursdayChecked(boolean p_thursdayChecked)
    {
        thursdayChecked=p_thursdayChecked;
    }

    public boolean isFridayChecked()
    {
        return fridayChecked;
    }

    public void setFridayChecked(boolean p_fridayChecked)
    {
        fridayChecked=p_fridayChecked;
    }

    public boolean isSaturdayChecked()
    {
        return saturdayChecked;
    }

    public void setSaturdayChecked(boolean p_saturdayChecked)
    {
        saturdayChecked=p_saturdayChecked;
    }

    public boolean isSundayChecked()
    {
        return sundayChecked;
    }

    public void setSundayChecked(boolean p_sundayChecked)
    {
        sundayChecked=p_sundayChecked;
    }
}