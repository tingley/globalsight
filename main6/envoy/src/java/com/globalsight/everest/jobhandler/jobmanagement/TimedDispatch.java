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

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.PeriodOfTime;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.jobhandler.jobmanagement.JobDispatchEngine;
import java.util.Date;


/**
 * A TimedDispatch represents a dispatch which will
 * happen based on a timer.
 */
public class TimedDispatch
{
    private static Logger s_logger=
           Logger.getLogger(
               TimedDispatch.class.getName());

    private Job m_job = null;
    private Date m_date = null;
    private boolean m_firstTime = true;

    /**
     * Creates a TimedDispatch
     * 
     * @param p_job  job to dispatch
     * @param p_periodOfTime the interval until dispatch
     */
    public TimedDispatch(Job p_job , PeriodOfTime p_periodOfTime)
    {
        m_job = p_job;
        long goTime = 0;
        if (p_periodOfTime.isAbsolute())
        {
            goTime= System.currentTimeMillis() +
            sleepWithAbsoluteTime(p_periodOfTime);
        }
        else
        {
            goTime= System.currentTimeMillis() +
            sleepWithInterval(p_periodOfTime);
        }
        m_date = new Date(goTime);
    }

    // returns interval sleep time
    private long sleepWithInterval(PeriodOfTime p_periodOfTime)
    {
        long relativeSleepTime;
        // if it's the first time, get the initial sleep time        
        if (m_firstTime)
        {
            // get initial sleep time
            relativeSleepTime = TimerHelper.getRelativeSleepTime(p_periodOfTime);
            // reset the flag
            m_firstTime = false;            
        }
        else // sleep time is the same as the interval
            relativeSleepTime = p_periodOfTime.getIntervalInMillisec();

        return relativeSleepTime;
    }


    // returns sleep time for an absolute time.
    private long sleepWithAbsoluteTime(PeriodOfTime p_periodOfTime)
    {      
        return  TimerHelper.getAbsoluteSleepTime(p_periodOfTime);
    }

    /**
     * The point in time at which the timer happens
     * @return Date 
     */
    public Date getStartDate()
    {
        return m_date;
    }

    public Job getJob()
    {
        return m_job;
    }
}

