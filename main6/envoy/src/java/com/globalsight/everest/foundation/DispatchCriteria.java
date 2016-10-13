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
package com.globalsight.everest.foundation;

// Core java classes
import java.io.Serializable;

/**
 * This class holds the criteria for automatically dispatching jobs. The
 * criteria could be: 1. immediate, OR 2. PeriodOfTime info (absolute/relative)
 * AND/OR VolumeOfData info.
 * 
 * 
 * @author Tomy A. Doomany
 */

/*
 * MODIFIED MM/DD/YYYY TomyD 08/09/2000 Initial version. pgautam 04/20/2001
 * Changes to dispatch criteria.
 */

public class DispatchCriteria implements Serializable
{
    private static final long serialVersionUID = -5413826708142674512L;

    /*
     * Let's first define constants. 0 => word count limit; 1 => word count
     * limit and timer (absolute/relative); 2 => batch;
     */
    public final static int WORD_COUNT_CONDITION = 0;
    public final static int WORD_COUNT_OR_TIMER_CONDITION = 1;
    public final static int BATCH_CONDITION = 2;

    // The m_condition stores the above combination details.
    private int m_condition;
    // The PeriodOfTime object used for relative/absolute time info.
    private PeriodOfTime m_timer;
    // holds the info of the data volume.
    private VolumeOfData m_volume;

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    public DispatchCriteria()
    {
        super();
        // initialize variables
        m_condition = BATCH_CONDITION;
        m_timer = null;
        m_volume = null;
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines whether the conditions should be AND'd together.
     * 
     * @return True if the conditions should be AND'd together, otherwise return
     *         false (if they should be OR'd).
     */
    public boolean andConditions()
    {
        return false;
    }

    /**
     * Set the condition to be the specified value.
     * 
     * @param p_condition -
     *            integer value for setting the condition.
     */
    public void setCondition(int p_condition)
    {
        m_condition = p_condition;
    }

    /**
     * Get the condition's value.
     * 
     * @return m_condition - integer value of the condition.
     */
    public int getCondition()
    {
        return m_condition;
    }

    /**
     * Determines whether the immediate condition is checked.
     * 
     * @return True if the immediate condition is checked, otherwise return
     *         false.
     */
    public boolean isImmediate()
    {
        // not supported anymore
        // if(m_condition == NO_DELAY_CONDITION)
        // {
        // return true;
        // } else
        // {
        return false;
        // }
    }

    /**
     * Set the time condition for a relative time.
     * 
     * @param p_interval -
     *            The time interval.
     * @param p_unit -
     *            The unit for the interval.
     * @param p_startTime -
     *            The starting time.
     */
    public void setTimeCondition(int p_interval, int p_unit,
            Timestamp p_startTime)
    {
        getPeriodOfTime().setTimeContext(p_interval, p_unit, p_startTime);
    }

    /**
     * Set the time condition for an absolute time.
     * 
     * @param p_days -
     *            The days used for the absolute time.
     * @param p_startTime -
     *            The starting time.
     */
    public void setTimeCondition(int[] p_days, Timestamp p_startTime)
    {
        getPeriodOfTime().setTimeContext(p_days, p_startTime);
    }

    /**
     * Get the period of time condition.
     * 
     * @return The time condition set up. This could be null if none is set.
     */
    public PeriodOfTime getTimeCondition()
    {
        return m_timer;
    }

    /**
     * Set the data volume's condition.
     * 
     * @param p_volume -
     *            The volume of data to be set.
     */
    public void setVolumeOfDataCondition(int p_volume)
    {
        setVolumeOfData(p_volume);
    }

    /**
     * Get the volume of data condition.
     * 
     * @return The data volume.
     */
    public VolumeOfData getVolumeOfDataCondition()
    {
        return m_volume;
    }

    /**
     * Set both a relative time and volume of data condition.
     * 
     * @param p_interval -
     *            The time interval.
     * @param p_unit -
     *            The unit for the interval.
     * @param p_startTime -
     *            The starting time.
     * @param p_volume -
     *            The volume of data to be set.
     */
    public void setTimeAndVolumeCondition(int p_interval, int p_unit,
            Timestamp p_startTime, int p_volume)
    {
        setVolumeOfData(p_volume);
        getPeriodOfTime().setTimeContext(p_interval, p_unit, p_startTime);
    }

    /**
     * Set both an absolute time and volume of data condition.
     * 
     * @param p_days -
     *            The days used for the absolute time.
     * @param p_startTime -
     *            The starting time.
     * @param p_volume -
     *            The volume of data to be set.
     */
    public void setTimeAndVolumeCondition(int[] p_days, Timestamp p_startTime,
            int p_volume)
    {
        setVolumeOfData(p_volume);
        getPeriodOfTime().setTimeContext(p_days, p_startTime);
    }

    /**
     * Return a string representation of the object.
     */
    public String toString()
    {
        return super.toString() + " m_timer="
                + (m_timer != null ? m_timer.toString() : "null")
                + " m_volume="
                + (m_volume != null ? m_volume.toString() : "null")
                + " m_condition=" + Integer.toString(m_condition);
    }

    // set the volume info
    private void setVolumeOfData(int p_volume)
    {
        getVolumeOfData().setVolumeContext(p_volume);
    }

    // Lazily instantiate a PeriodOfTime object
    private PeriodOfTime getPeriodOfTime()
    {
        if (m_timer == null)
            m_timer = new PeriodOfTime();

        return m_timer;
    }

    // Lazily instantiate a VolumeOfData object.
    private VolumeOfData getVolumeOfData()
    {
        if (m_volume == null)
            m_volume = new VolumeOfData();

        return m_volume;
    }

    public PeriodOfTime getTimer()
    {
        return getPeriodOfTime();
    }

    public void setTimer(PeriodOfTime m_timer)
    {
        this.m_timer = m_timer;
    }

    public VolumeOfData getVolume()
    {
        return getVolumeOfData();
    }

    public void setVolume(VolumeOfData m_volume)
    {
        this.m_volume = m_volume;
    }
}
