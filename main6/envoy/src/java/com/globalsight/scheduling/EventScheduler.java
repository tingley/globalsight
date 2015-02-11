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
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.calendar.CalendarBusinessIntervals;
import com.globalsight.calendar.Holiday;
import com.globalsight.calendar.ReservedTime;
import com.globalsight.calendar.WorkingDay;
import com.globalsight.everest.foundation.Timestamp;


/**
 * The interface for scheduling events through Quartz.
 */

public interface EventScheduler
{
    /**
     * The public name bound to the remote object.
     */
    public static final String SERVICE_NAME = "EventScheduler";

    /**
     * Determine a valid working date based on the given calendar.
     * Note that the returned date is based on duration added to
     * the start date and validated thru the calendar.
     * @param p_startDate - The base date for determination of an end date.
     * @param p_calendar - The calendar used for the date calculation.
     * @param p_duration - The duration  used as a base for an end 
     *                     time (i.e. a task has started at the start date
     *                     and has to be completed by the given duration).
     *                     The duration is in milliseconds is is based on 
     *                     the 24 hours per day.  It will be re-calculated
     *                     based on the calendar's conversion factor.
     */
    Date determineDate(Date p_startDate, BaseFluxCalendar p_calendar,
                       long p_duration)
        throws RemoteException, EventSchedulerException;

    /**
     * Determine a valid working date based on the system's default calendar.
     * Note that the returned date is based on duration added to
     * the start date and validated thru the calendar.
     * @param p_startDate - The base date for determination of an end date.
     * @param p_duration - The duration  used as a base for an end 
     *                     time (i.e. a task has started at the start date
     *                     and has to be completed by the given duration).
     *                     The duration is in milliseconds is is based on 
     *                     the 24 hours per day.  It will be re-calculated
     *                     based on the calendar's conversion factor.
     */
    Date determineDateByDefaultCalendar(Date p_startDate, long p_duration, String p_companyId)
        throws RemoteException, EventSchedulerException;

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
    ScheduledEvent findEvent(String p_eventId)
        throws RemoteException, EventSchedulerException;

    /**
     * Make all the required business intervals along with their respective
     * allowable business interval ranges for a calendar.  Note that the 
     * range for the allowable business intervals would be based on the
     * given date.
     * biz interval only the holidays are excluded. 
     *
     * @param p_holidays - The holidays to be excluded from the business
     * interval.
     * @param p_reservedTimes - The reserved times to be excluded from
     * the business interval.
     * @param p_personalReservedTimes - The 'personal' reserved times to 
     * be excluded from the business interval.
     * @param p_workingDays - The working days to be included in 
     * the business interval.
     * @param p_startDate - The starting date for the range of allowable
     * business intervals (only the range for the whole month of the given
     * date would be set).
     * @param p_timeZone - The calendar's time zone.
     *
     * @return The calendar business interval object that contains all
     * the info about a calendar's business intervals.
     */
    public CalendarBusinessIntervals makeCalendarBusinessIntervals(
        List<Holiday> p_holidays, List<ReservedTime> p_reservedTimes,
        List<ReservedTime> p_personalReservedTimes, List<WorkingDay> p_workingDays, 
        Timestamp p_startDate, TimeZone p_timeZone)
        throws RemoteException, EventSchedulerException;

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
    void performSchedulingProcess(HashMap p_map)
        throws RemoteException, EventSchedulerException;

    /**
     * Schedule an event based on the given scheduling info.
     *
     * @param p_schedulingInformation - The scheduling information
     * to be used during the creation of a schedule job.
     *
     * @throws EventSchedulerException if the given event cannot be scheduled
     * for any reason.
     */
    void scheduleEvent(SchedulingInformation p_schedulingInformation)
        throws RemoteException, EventSchedulerException;

    /**
     * Unschedule the given event map from Quartz and remove the
     * flux_map object from the database.
     *
     * @param p_fluxEventMap - The event to be stopped and removed.
     *
     * @throws EventSchedulerException if the given event cannot be scheduled
     * for any reason.
     */
    void unschedule(FluxEventMap p_fluxEventMap)
        throws RemoteException, EventSchedulerException;
}
