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
package com.globalsight.everest.jobhandler.jobmanagement;

//Core java classes
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.*;
import com.globalsight.util.DebugHelper;

/**
* TimerHelper is a helper class for calculating and providing the sleep time to the DispatchTimer
* thread.
*
* @author Tomy A. Doomany
*/


/*
 * MODIFIED     MM/DD/YYYY
 * TomyD        08/09/2000   Initial version.
 */

public class TimerHelper
{    
    private static final Logger c_category =
            Logger.getLogger(
            TimerHelper.class.getName());
        
    private static final int DAYS_OF_WEEK = 7;
    private static final int ZERO = 0;

    private static boolean DO_NOT_ADD_A_WEEK_OF_DAYS = false;
    private static boolean ADD_A_WEEK_OF_DAYS = true;
    
    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Constructor
    ////////////////////////////////////////////////////////////////////////////////// 
    /**
    * Construct an instance of TimerHelper.
    */
    private TimerHelper() 
    {        
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Constructor
    //////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Local Methods
    //////////////////////////////////////////////////////////////////////////////////
   
            ////////////////////
            //   Relative time
            ////////////////////
    /**
    * Get the sleep time for the relative period.
    * @param p_timerData - The period of time object that contains relative time info.
    * @return The sleep time for the relative period.
    */
    public static long getRelativeSleepTime(PeriodOfTime p_timerData)
    {
        /**
         * 1. need to know whether it's currently before 
         * or after the specified starting time 
         */
        Timestamp currentTime = new Timestamp(0);
        Timestamp startingTime = p_timerData.getStartTime();
        long interval = 0;
        // NOTE -- If the start time >= current time, 
        // then we'll start right away (sleep time is zero)
        if (startingTime.isLessThan(currentTime))
        {
            interval = p_timerData.getIntervalInMillisec();
        }
        long timeDifference = currentTime.getTimeInMillisec() 
                - startingTime.getTimeInMillisec();
        // if time difference is greater than 
        // interval, recalculate the time difference.
        if (interval != 0 && timeDifference > interval)
        {
            timeDifference = timeDifference % interval;
        }
        long initialSleepTime = interval - timeDifference;
        if (c_category.isDebugEnabled())
        {
            c_category.debug("getRelativeSleepTime:"
                    + " p_timerData=" 
                    + DebugHelper.getDebugString(p_timerData)
                    + " currentTime=" + currentTime.toString()
                    + " returns initialSleepTime=" + initialSleepTime);
        }    
        return initialSleepTime;
    }
        


            ////////////////////
            //   Absolute time
            ////////////////////
    /**
    * Get the sleep time for a particular absolute period.
    * @param p_timerData - The period of time object that contains absolute time info.
    * @return The sleep time for the absolute period.
    */
    public static long getAbsoluteSleepTime(PeriodOfTime p_timerData)
    {        
        // get current date
        Timestamp currentTime = new Timestamp(0);
        int currentDay = currentTime.getDayOfWeek();
        long initialSleepTime = getSleepTime(p_timerData, 
                currentTime, currentDay);  
        if (c_category.isDebugEnabled())
        {
            c_category.debug("getAbsoluteSleepTime:"
                    + " p_timerData=" 
                    + DebugHelper.getDebugString(p_timerData)
                    + " currentDay=" + currentDay
                    + " returns initialSleepTime=" + initialSleepTime);
        }     
        return initialSleepTime;
    }



    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Local Methods
    //////////////////////////////////////////////////////////////////////////////////

    // NOTE -- This is just a psudo code for coming up with a better algorithm...
    // Get the start day based on the PeriodOfTime object info and the current day.
    private static long getSleepTime(PeriodOfTime p_timerData, 
            Timestamp p_currentTime, int p_currentDay)
    {
        long initialSleepTime = 0;        
        int currentDay = p_currentTime.getDayOfWeek();  
        /**
         * If p_currentDay is in the array in PeriodOfTime
         * then it is returned as the startDay.
         * So startDay wil equal p_currentDay.
         * Otherwise it returns a day that might or
         * might not be equal to p_currentDay.
         */       
        int startDay = getStartDay(p_timerData, p_currentDay);
        // now that we have the start day, let's calculate the sleep time
        if (startDay > currentDay)  
        {   // i.e startDay = Wed   &   current day = Tue
            initialSleepTime = calculateSleepTime(p_timerData, 
                    p_currentTime, startDay, DO_NOT_ADD_A_WEEK_OF_DAYS);    
        }         
        if (startDay < currentDay)
        {
            initialSleepTime = calculateSleepTime(p_timerData, 
                    p_currentTime, startDay, ADD_A_WEEK_OF_DAYS);
        }
        if (startDay == currentDay)
        {
            if (p_currentTime.isGreaterThan(p_timerData.getStartTime()))
            {
                /** 
                 * If there is only one day in PeriodOfTime,
                 * then getStartDay will always return the same day, 
                 * and startDay will always == currentDay, no 
                 * matter how many times you call getSleepTime.
                 * Recursion error.
                 * <p>
                 * First see if there is a day in PeriodOfTime
                 * later in the week to start on. 
                 */
                for (int i = currentDay + 1; i < 8; i++)
                {
                    int potentialStartDay = getStartDay(
                            p_timerData, i);
                    if (potentialStartDay != currentDay)
                    { 
                        startDay = potentialStartDay;
                        break;
                    }
                }        
                if (startDay == currentDay)
                {
                    /**
                     * Try again, only from the beginning of
                     * the array in PeriodOfTime.
                     */
                    for (int i = 0; i < 8; i++)
                    {
                        int potentialStartDay = getStartDay(
                                p_timerData, i);
                        if (potentialStartDay != currentDay)
                        { 
                            startDay = potentialStartDay;
                            break;
                        }
                    }
                }  
                if (startDay == currentDay)
                {    
                    initialSleepTime = calculateSleepTime(p_timerData, 
                            p_currentTime, startDay, ADD_A_WEEK_OF_DAYS);     
                }
                if (startDay > currentDay)  
                {   // i.e startDay = Wed   &   current day = Tue
                    initialSleepTime = calculateSleepTime(p_timerData, 
                            p_currentTime, startDay, 
                            DO_NOT_ADD_A_WEEK_OF_DAYS);    
                }         
                if (startDay < currentDay)
                {
                    initialSleepTime = calculateSleepTime(p_timerData, 
                            p_currentTime, startDay, ADD_A_WEEK_OF_DAYS);
                }           
            }
            else 
            {
                initialSleepTime = p_timerData.getStartTime()
                        .getTimeInMillisec() - 
                        p_currentTime.getTimeInMillisec();
            }
        }
        if (c_category.isDebugEnabled())
        {
            c_category.debug("getSleepTime:"
                    + " p_timerData=" 
                    + DebugHelper.getDebugString(p_timerData)
                    + " p_currentTime=" 
                    + DebugHelper.getDebugString(p_currentTime)
                    + " p_currentDay=" + p_currentDay
                    + " returns initialSleepTime=" + initialSleepTime);
        } 
        return initialSleepTime;
    }


    // get the day difference in milliseconds.
    private static long getDayDiffInMillisec(int p_daysToAdd, 
            int p_startDay, int p_currentDay)
    {
        Integer dayDiff = new Integer((p_daysToAdd 
                + (p_startDay - p_currentDay)) * 24 * 3600000);
        return dayDiff.longValue();
    }        


    // calculate sleep time for absolute period.
    private static long calculateSleepTime(PeriodOfTime p_timerData, 
            Timestamp p_currentTime, 
            int p_startDay, boolean p_addAWeekOfDays)
    {
        int daysToAdd = 0;
        if (p_addAWeekOfDays)
        {
            daysToAdd = 7;
        }
        else
        {
            daysToAdd = 0;
        }
        long dayDiff = getDayDiffInMillisec(daysToAdd, 
                p_startDay, p_currentTime.getDayOfWeek());
        long initialSleepTime = dayDiff + 
                (p_timerData.getStartTime().getTimeInMillisec() - 
                p_currentTime.getTimeInMillisec());
        return initialSleepTime;
    }


    // get the starting day for an absolute period.
    private static int getStartDay(PeriodOfTime p_timerData, 
            int p_currentDay)
    {
        int startDay;
        // see if the current day is in the selected days array
        // See java.util.Arrays.binarySearch
        int index = p_timerData.findDay(p_currentDay);
        if(index >= 0)
        {   // current day is in the array!
            startDay = p_currentDay;
        }
        else
        {
            // find the index of one of the array days 
            // that's greater than the current day

            // we're basically calculating the "insertion point"
            // See java.util.Arrays.binarySearch
            int newIndex = -(index + 1);             
            // if the new index is not less than the 
            // array size, get the day at index 0
            startDay = p_timerData.getDayAt(
                    newIndex < p_timerData.getNumberOfDays() ?
                    newIndex :0);            
        }
        return startDay;
    }
}
