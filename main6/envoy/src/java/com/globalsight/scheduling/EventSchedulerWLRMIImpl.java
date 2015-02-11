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

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.calendar.CalendarBusinessIntervals;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;

/**
 * This class represents the remote implementation of an event scheduler.
 * Note that all of the methods of this class throw the following exceptions:
 * 1. EventSchedulerWLRMIImpl - For event scheduling related errors.
 * 2. RemoteException - For network related exception.
 */

public class EventSchedulerWLRMIImpl
    extends RemoteServer
    implements EventSchedulerWLRemote
{
    
    // PRIVATE MEMBER VARIABLES
    EventSchedulerLocal m_esl = null;

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Construct a remote event scheduler.
     */
    public EventSchedulerWLRMIImpl()
    throws RemoteException, EventSchedulerException
    {
        super(EventScheduler.SERVICE_NAME); 
        m_esl = new EventSchedulerLocal();        
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Implementation of EventScheduler
    //////////////////////////////////////////////////////////////////////
    /**
     * @see EventScheduler.determineDate(Date, BaseFluxCalendar, long)
     */
    public Date determineDate(Date p_startDate, 
                              BaseFluxCalendar p_calendar, 
                              long p_duration)
        throws RemoteException, EventSchedulerException
    {
        return m_esl.determineDate(p_startDate, p_calendar, p_duration);
    }

    /**
     * @see EventScheduler.determineDate(Date, long)
     */
    public Date determineDateByDefaultCalendar(Date p_startDate, 
                                               long p_duration,
                                               String p_companyId)
        throws RemoteException, EventSchedulerException
    {
        return m_esl.determineDateByDefaultCalendar(p_startDate, 
                                                    p_duration,
                                                    p_companyId);
    }

    /**
     * Find and return the scheduled event with the given id.  (Note: only
     * scheduled events are persisted in the database; hence the restriction
     * on return value.)
     *
     * @param p_eventId the id of the event to find.
     *
     * @return the scheduled event that matches the given id.
     *
     * @throws EventSchedulerException if the event does not exist or if a
     * database problem occurs. 
     */
    public ScheduledEvent findEvent(String p_eventId)
        throws RemoteException, EventSchedulerException
    {
        return m_esl.findEvent(p_eventId);
    }

    /**
     * @see EventScheduler.makeCalendarBusinessIntervals(List, List, List, 
     * Timestamp, TimeZone)
     */
    public CalendarBusinessIntervals makeCalendarBusinessIntervals(
        List p_holidays, List p_reservedTimes,
        List p_personalEvents, List p_workingDays, 
        Timestamp p_startDate, TimeZone p_timeZone)
        throws RemoteException, EventSchedulerException
    {
        return m_esl.makeCalendarBusinessIntervals(
            p_holidays, p_reservedTimes, p_personalEvents, 
            p_workingDays, p_startDate, p_timeZone);
    }
    /**
     * This method performs both scheduling or/and unscheduling of an 
     * event using the information within the given HashMap object.  The 
     * method delegates the call to the message listener within the JMS
     * pool (asynchronous process).
     *
     * @param p_map - A HashMap containig all required information
     * for scheduling or/and unscheduling of an event. 
     *
     * @throws EventSchedulerException if the given event cannot be scheduled
     * for any reason.
     */
    public void performSchedulingProcess(HashMap p_map)
        throws RemoteException, EventSchedulerException
    {
        m_esl.performSchedulingProcess(p_map);
    }

    /**
     * @see EventScheduler.scheduleEvent(SchedulingInformation)
     */
    public void scheduleEvent(SchedulingInformation p_schedulingInformation)
        throws RemoteException, EventSchedulerException
    {
        m_esl.scheduleEvent(p_schedulingInformation);
    }
    
    /**
     * @see EventScheduler.unschedule(FluxEventMap)
     */
    public void unschedule(FluxEventMap p_fluxEventMap)
        throws RemoteException, EventSchedulerException
    {
        m_esl.unschedule(p_fluxEventMap);
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Implementation of EventScheduler
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin:  RemoteServer's Overrides
    //////////////////////////////////////////////////////////////////////
    /* Override: make sure that the local implementation is properly */
    /* started up (Quartz startup). */
    public void init()
        throws SystemStartupException
    {
        m_esl.startup();
        super.init();
    }

    /* Override: make sure that the local implementation is properly */
    /* shut down (Quartz shutdown). */
    public void destroy()
        throws SystemShutdownException
    {
        m_esl.shutdown();
        super.destroy();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: RemoteServer's Overrides
    //////////////////////////////////////////////////////////////////////
}
