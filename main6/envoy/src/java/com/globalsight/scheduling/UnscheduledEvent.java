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
package com.globalsight.scheduling;

import com.globalsight.calendar.BaseFluxCalendar;

import java.util.Date;
import java.util.HashMap;

/**
 * UnscheduledEvent extends Event by providing additional functionality and
 * features pertinent to an event that is about to be scheduled.
 */
public class UnscheduledEvent
extends Event
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private Date m_startTime;
    private Date m_endTime;
    private String m_recurrenceRule;
    private int m_repeatCount;
    private Class m_handlerClass;
    private BaseFluxCalendar m_calendar;

    //
    // PUBLIC CONSTRUCTOR
    //
    /**
     * Construct an UnscheduledEvent.
     */
    public UnscheduledEvent()
    {
        super();
        m_startTime = null;
        m_endTime = null;
        m_recurrenceRule = null;
        m_repeatCount = -1;
        m_handlerClass = null;
        m_calendar = null;
    }

    //
    // PUBLIC ACCESSOR METHODS
    //

    /**
     * Return the class of the event handler for this event.
     *
     * @return the event handler class 
     */
    public Class getEventHandlerClass()
    {
        return m_handlerClass;
    }

    /**
     * Return the end time (date & time) for this event.
     *
     * @return the end time.
     */
    public Date getEndTime()
    {
        return m_endTime;
    }

    /**
     * Return the calendar that is being used by this event.
     *
     * @return the calendar (either user calendar or system calendar)
     */
    public BaseFluxCalendar getCalendar()
    {
        return m_calendar;
    }

    /**
     * Return the time expression representing the rule that governs how this
     * event recurs over time.
     *
     * @return the recurrence rule.
     */
    public String getRecurrenceRule()
    {
        return m_recurrenceRule;
    }

    /**
     * Return the repeat count for this event.  The repeat count represents
     * the number of times the event will re-fire <b>after</b> the initial
     * firing.
     *
     * @return the repeat count.
     */
    public int getRepeatCount()
    {
        return m_repeatCount;
    }

    /**
     * Return the start time (date & time) for this event.
     *
     * @return the start time.
     */
    public Date getStartTime()
    {
        return m_startTime;
    }

    /**
     * Set the class of the event handler for this event.  A value of null
     * indicates that all event handlers registered with the scheduler should
     * be called when this event fires.
     *
     * @param p_class the class of the event handler
     */
    public void setEventHandlerClass(Class p_class)
    {
        m_handlerClass = p_class;
    }

    /**
     * Set the end time (date & time) for this event.  This value may be null
     * to represent an indefinitely recurring event.
     *
     * @param p_endTime the new end time.
     */
    public void setEndTime(Date p_endTime)
    {
        m_endTime = p_endTime;
        m_repeatCount = -1;
    }
     
    /**
     * Set the calendar that is to be used by this event. 
     *
     * @param p_calendar the calendar to use.
     */
    public void setCalendar(BaseFluxCalendar p_calendar)
    {
        m_calendar = p_calendar;
    }
     
    /**
     * Set the time expression representing the rule that governs how this
     * event recurs over time.  This value may be null to indicate that the
     * event does NOT repeat.
     *
     * @return the recurrence rule.
     */
    public void setRecurrenceRule(String p_rule)
    {
        m_recurrenceRule = p_rule;
    }

    /**
     * Set the repeat count for this event.  The repeat count represents
     * the number of times the event will re-fire <b>after</b> the initial
     * firing.  Setting this parameter causes the end time to be reset.
     *
     * @return the repeat count.
     */
    public void setRepeatCount(int p_repeatCount)
    {
        if (p_repeatCount > -1)
        {
            m_repeatCount = p_repeatCount;
            m_endTime = null;
        }
    }

    /**
     * Set the start time (date & time) for this event.  This value may be null
     * to represent the current server time.
     *
     * @param p_date the new start time.
     */
    public void setStartTime(Date p_date)
    {
        m_startTime = p_date;
    }
}
