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
package com.globalsight.calendar;

// GlobalSight
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.util.system.RemoteServer;

/**
 * This class represents the remote implementation of a Calendar Manager. Note
 * that all of the methods of this class throw the following exceptions: 1.
 * CalendarManagerWLRMIImpl - For component related errors. 2. RemoteException -
 * For network related exception.
 */

public class CalendarManagerWLRMIImpl extends RemoteServer implements
        CalendarManagerWLRemote
{

    // PRIVATE MEMBER VARIABLES
    CalendarManagerLocal m_localInstance = null;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////

    /**
     * Construct a remote event scheduler.
     */
    public CalendarManagerWLRMIImpl() throws RemoteException,
            CalendarManagerException
    {
        super(CalendarManager.SERVICE_NAME);
        m_localInstance = new CalendarManagerLocal();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Implementation of CalendarManager
    // ////////////////////////////////////////////////////////////////////

    /**
     * @see CalendarManager.computeCalendarIntervals( UserFluxCalendar,
     *      Timestamp)
     */
    public FluxCalendar computeCalendarIntervals(FluxCalendar p_calendar,
            Timestamp p_startDate) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance
                .computeCalendarIntervals(p_calendar, p_startDate);
    }

    /**
     * @see CalendarManager.computeInterval(FluxCalendar, Date, Date)
     */
    public long computeInterval(FluxCalendar p_Calendar, Date oriDate,
            Date newDate) throws RemoteException, CalendarManagerException
    {
        return m_localInstance.computeInterval(p_Calendar, oriDate, newDate);
    }

    /**
     * @see CalendarManager.computeUserCalendarIntervals( UserFluxCalendar,
     *      Timestamp)
     */
    public UserFluxCalendar computeUserCalendarIntervals(
            UserFluxCalendar p_userCalendar, Timestamp p_startDate)
            throws RemoteException, CalendarManagerException
    {
        return m_localInstance.computeUserCalendarIntervals(p_userCalendar,
                p_startDate);
    }

    /**
     * @see CalendarManager.createCalendar(FluxCalendar, String)
     */
    public FluxCalendar createCalendar(FluxCalendar p_calendar,
            String p_creatorUserId) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.createCalendar(p_calendar, p_creatorUserId);
    }

    /**
     * @see CalendarManager.createHoliday(Holiday)
     */
    public Holiday createHoliday(Holiday p_holiday) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.createHoliday(p_holiday);
    }

    /**
     * @see CalendarManager.createReservedTime(ReservedTime)
     */
    public ReservedTime createReservedTime(long p_userCalendarId,
            ReservedTime p_reservedTime) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.createReservedTime(p_userCalendarId,
                p_reservedTime);
    }

    /**
     * @see CalendarManager.createUserCalendar(UserFluxCalendar, String)
     */
    public UserFluxCalendar createUserCalendar(UserFluxCalendar p_calendar,
            String p_creatorUserId) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.createUserCalendar(p_calendar, p_creatorUserId);
    }

    /**
     * @see CalendarManager.findCalendarById(long)
     */
    public FluxCalendar findCalendarById(long p_calendarId)
            throws RemoteException, CalendarManagerException
    {
        return m_localInstance.findCalendarById(p_calendarId);
    }

    /**
     * @see CalendarManager.findDefaultCalendar()
     */
    public FluxCalendar findDefaultCalendar(String p_companyId)
            throws RemoteException, CalendarManagerException
    {
        return m_localInstance.findDefaultCalendar(p_companyId);
    }

    /**
     * @see CalendarManager.findHolidayById(long)
     */
    public Holiday findHolidayById(long p_holidayId) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.findHolidayById(p_holidayId);
    }

    /**
     * @author CalendarManager.findHolidaysByCalendarId(long)
     */
    public List findHolidaysByCalendarId(long p_calendarId)
            throws RemoteException, CalendarManagerException
    {
        return m_localInstance.findHolidaysByCalendarId(p_calendarId);
    }

    /**
     * @see CalendarManager.findReservedTimeById(long)
     */
    public ReservedTime findReservedTimeById(long p_reservedTimeId)
            throws RemoteException, CalendarManagerException
    {
        return m_localInstance.findReservedTimeById(p_reservedTimeId);
    }

    /**
     * @see CalendarManager.findReservedTimesByOwnerAndTaskId(String, long)
     */
    public Collection findReservedTimesByOwnerAndTaskId(String p_userId,
            long p_taskId) throws RemoteException, CalendarManagerException
    {
        return m_localInstance.findReservedTimesByOwnerAndTaskId(p_userId,
                p_taskId);
    }

    /**
     * @see CalendarManager.findReservedTimesForGivenDate(long, Timestamp)
     */
    public List findReservedTimesForGivenDate(long p_userCalendarId,
            Timestamp p_date) throws RemoteException, CalendarManagerException
    {
        return m_localInstance.findReservedTimesForGivenDate(p_userCalendarId,
                p_date);
    }

    /**
     * @see CalendarManager.findUserCalendarById(long)
     */
    public UserFluxCalendar findUserCalendarById(long p_userCalendarId)
            throws RemoteException, CalendarManagerException
    {
        return m_localInstance.findUserCalendarById(p_userCalendarId);
    }

    /**
     * @see CalendarManager.findUserCalendarByOwner(String)
     */
    public UserFluxCalendar findUserCalendarByOwner(String p_ownerUserId)
            throws RemoteException, CalendarManagerException
    {
        return m_localInstance.findUserCalendarByOwner(p_ownerUserId);
    }

    /**
     * @see CalendarManager.findUserTimeZone(String)
     */
    public TimeZone findUserTimeZone(String p_userId) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.findUserTimeZone(p_userId);
    }

    /**
     * @see CalendarManager.getAllCalendars()
     */
    public List getAllCalendars() throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.getAllCalendars();
    }

    public List getAllCalendarsByCompanyId(String p_companyId)
            throws RemoteException, CalendarManagerException
    {
        return m_localInstance.getAllCalendarsByCompanyId(p_companyId);
    }

    /**
     * @see CalendarManager.getAllHolidays()
     */
    public List getAllHolidays() throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.getAllHolidays();
    }

    /**
     * @see CalendarManager.getAllUserCalendars()
     */
    public List getAllUserCalendars() throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.getAllUserCalendars();
    }

    /**
     * @see CalendarManager.importEntries(List)
     */
    public void importEntries(List p_entries) throws RemoteException,
            CalendarManagerException
    {
        m_localInstance.importEntries(p_entries);
    }

    /**
     * @see CalendarManager.makeDefaultCalendar(long)
     */
    public void makeDefaultCalendar(long p_calendarId, String p_modifierUserId)
            throws RemoteException, CalendarManagerException
    {
        m_localInstance.makeDefaultCalendar(p_calendarId, p_modifierUserId);
    }

    /**
     * @see CalendarManager.modifyCalendar(FluxCalendar, String)
     */
    public FluxCalendar modifyCalendar(FluxCalendar p_calendar,
            String p_modifierUserId) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.modifyCalendar(p_calendar, p_modifierUserId);
    }

    /**
     * @see CalendarManager.modifyHoliday(Holiday)
     */
    public Holiday modifyHoliday(Holiday p_holiday) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.modifyHoliday(p_holiday);
    }

    /**
     * @see CalendarManager.modifyUserCalendar(UserFluxCalendar, String)
     */
    public UserFluxCalendar modifyUserCalendar(UserFluxCalendar p_userCalendar,
            String p_modifierUserId) throws RemoteException,
            CalendarManagerException
    {
        return m_localInstance.modifyUserCalendar(p_userCalendar,
                p_modifierUserId);
    }

    /**
     * @see CalendarManager.removeScheduledActivity(long, String)
     */
    public void removeScheduledActivity(long p_taskId, String p_userId)
            throws RemoteException, CalendarManagerException
    {
        m_localInstance.removeScheduledActivity(p_taskId, p_userId);
    }

    /**
     * @see CalendarManager.removeScheduledActivity(long, UserFluxCalendar)
     */
    public void removeScheduledActivity(long p_taskId,
            UserFluxCalendar p_calendar) throws RemoteException,
            CalendarManagerException
    {
        m_localInstance.removeScheduledActivity(p_taskId, p_calendar);
    }

    /**
     * @see CalendarManager.removeScheduledActivities(long);
     */
    public void removeScheduledActivities(long p_taskId)
            throws RemoteException, CalendarManagerException
    {
        m_localInstance.removeScheduledActivities(p_taskId);
    }

    /**
     * @see CalendarManager.removeCalendar(long)
     */
    public void removeCalendar(long p_calendarId) throws RemoteException,
            CalendarManagerException
    {
        m_localInstance.removeCalendar(p_calendarId);
    }

    /**
     * @see CalendarManager.removeHoliday(long)
     */
    public void removeHoliday(long p_holidayId) throws RemoteException,
            CalendarManagerException
    {
        m_localInstance.removeHoliday(p_holidayId);
    }

    /**
     * @see CalendarManager.removeUserCalendar(long)
     */
    public void removeUserCalendar(String p_ownerUserId)
            throws RemoteException, CalendarManagerException
    {
        m_localInstance.removeUserCalendar(p_ownerUserId);
    }

    /**
     * @see CalendarManager.userUnavailabilityReport(List, int, int)
     */
    public HashMap userUnavailabilityReport(List p_userInfos, int p_month,
            int p_year) throws RemoteException, CalendarManagerException
    {
        return m_localInstance.userUnavailabilityReport(p_userInfos, p_month,
                p_year);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Implementation of CalendarManager
    // ////////////////////////////////////////////////////////////////////
}
