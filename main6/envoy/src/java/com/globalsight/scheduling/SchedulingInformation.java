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


// GlobalSight
import com.globalsight.calendar.BaseFluxCalendar;
// JDK
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;


/**
 * SchedulingInformation contains a list of attributes required for 
 * scheduling an event.
 */

public class SchedulingInformation
    implements Serializable
{

    private BaseFluxCalendar m_calendar = null;
    private Class m_listener = null;
    private Date m_startDate = null;
    private HashMap m_eventInfo = null;
    private int m_actionType = -1;
    private int m_eventType = -1;
    private int m_objectType = -1;
    private int m_repeatCount = 0;
    private long m_duration = 0;
    private long m_objectId = -1;
    private String m_eventTypeName = null;
    private String m_recurranceExpression = null;



    // PUBLIC CONSTRUCTORS
    public SchedulingInformation()
    {
        super();
    }


    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Support Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the action type (i.e. accepting/finishing activity).
     * @return The action type.
     */
    public int getActionType()
    {
        return m_actionType;
    }

    /**
     * Get the holiday canlendar used for computing dates.
     * @return A holiday calendar, or null if there's none.
     */
    public BaseFluxCalendar getCalendar()
    {
        return m_calendar;
    }

    /**
     * Get the duration of this event.
     * @return The event's duration.
     */
    public long getDuration()
    {
        return m_duration;
    }

    /**
     * Get the event information as a HashMap.  Note that this object
     * is stored in database and the object type should not change, as
     * it's mapped to a table column.
     * @param The event info used for a scheduled event.
     */
    public HashMap getEventInfo()
    {
        return m_eventInfo;
    }

    /**
     * Get the event type (i.e. acceptance vs. completion)
     * @return The event type.
     */
    public int getEventType()
    {
        return m_eventType;
    }

    /**
     * Get the event type name.
     * @return The event's name.
     */
    public String getEventTypeName()
    {
        return m_eventTypeName;
    }

    /**
     * Get the scheduler listener class that will be notified by schedule engine(Quartz)
     * when a job is fired.
     * @return The event listener class.
     */
    public Class getListener()
    {
        return m_listener;
    }

    /**
     * Get the object id.
     * @return The id of the object.
     */
    public long getObjectId()
    {
        return m_objectId;
    }

    /**
     * Get the object type (i.e. workflow task).
     * @return The object type.
     */
    public int getObjectType()
    {
        return m_objectType;
    }

    /**
     * Get the recurrance expression for the event to be scheduled.
     * @return The recurrance expression.
     */
    public String getRecurranceExpression()
    {
        return m_recurranceExpression;
    }

    /**
     * Get the event's repeat count.
     * @return The event's repeat count.
     */
    public int getRepeatCount()
    {
        return m_repeatCount;
    }

    /**
     * Get the start date of this event.
     * @return The start date.
     */
    public Date getStartDate()
    {
        return m_startDate;
    }
    

    /**
     * Set the action type to be the specified value.
     * @param p_actionType - The action type to be set.
     */
    public void setActionType(int p_actionType)
    {
        m_actionType = p_actionType;
    }

    /**
     * Set the calendar to be the specified value.
     * @param p_calendar - The calendar to be used for scheduling purposes.
     */
    public void setCalendar(BaseFluxCalendar p_calendar)
    {
        m_calendar = p_calendar;
    }

    /**
     * Set the duration for the event to be scheduled.
     * @param p_duration - The event's duration.
     */
    public void setDuration(long p_duration)
    {
        m_duration = p_duration;
    }

    /**
     * Set the event info for this event to be the specified value.
     * @param p_eventInfo - The event info map to be set.
     */
    public void setEventInfo(HashMap p_eventInfo)
    {
        m_eventInfo = p_eventInfo;
    }

    /**
     * Set the event type to be the specified value.
     * @param p_eventType - The event type to be set.
     */
    public void setEventType(int p_eventType)
    {
        m_eventType = p_eventType;;
    }

    /**
     * Set the event type name to be the specified value.
     * @param p_eventTypeName - The event name to be set.
     */
    public void setEventTypeName(String p_eventTypeName)
    {
        m_eventTypeName = p_eventTypeName;;
    }

    /**
     * Set the scheduler listener class that will be notified by schedule engine (Quartz)
     * (when a job is fired) to be the specified class.
     * @param p_listener - The event listener class to be set.
     */
    public void setListener(Class p_listener)
    {
        m_listener = p_listener;
    }

    /**
     * Set the object id to be the specified id.  This id is used
     * to perform action on the object stored in our tables (i.e. a
     * workflow id or activity id).
     * @param p_objectId - The id of the object to be set.
     */
    public void setObjectId(long p_objectId)
    {
        m_objectId = p_objectId;
    }

    /**
     * Set the object type (i.e. workflow task) to be the specified value.
     * @param p_objectType - The object type to be set.
     */
    public void setObjectType(int p_objectType)
    {
        m_objectType = p_objectType;
    }

    /**
     * Set the recurrance expression for the event.
     * @param p_recurranceExpression The recurrance expression to be set.
     */
    public void setRecurranceExpression(String p_recurranceExpression)
    {
        m_recurranceExpression = p_recurranceExpression;
    }

    /**
     * Set the event's repeat count to be the specified value.
     * @param p_repeatCount - The event's repeat count to be set.
     */
    public void setRepeatCount(int p_repeatCount)
    {
        m_repeatCount = p_repeatCount;
    }

    /**
     * Set the start date of this event to be the specified date.
     * @param p_startDate - The start date to be set.
     */
    public void setStartDate(Date p_startDate)
    {
        m_startDate = p_startDate;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Public Support Methods
    //////////////////////////////////////////////////////////////////////
}
