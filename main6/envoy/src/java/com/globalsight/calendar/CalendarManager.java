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

//GlobaSight
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.globalsight.everest.foundation.Timestamp;

/**
 * The interface responsible for calendaring support.
 */

public interface CalendarManager
{
    /**
     * The public name bound to the remote object.
     */
    public static final String SERVICE_NAME = "CalendarManager";

    /**
     * Compute the business intervals for the given calendar based on the
     * provided start date.
     * 
     * @param p_calendar
     *            - The calendar to be used for business interval update.
     * @param p_startDate
     *            - The start date used as the beginning range for the allowable
     *            business interval ranges.
     * @return the calendar with updated business intervals.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    FluxCalendar computeCalendarIntervals(FluxCalendar p_calendar,
            Timestamp p_startDate) throws RemoteException,
            CalendarManagerException;

    /**
     * Computes the interval changed from oriDate to newDate based on the given
     * calendar.
     */
    long computeInterval(FluxCalendar p_Calendar, Date oriDate, Date newDate)
            throws RemoteException, CalendarManagerException;

    /**
     * Compute the business intervals for the given user calendar based on the
     * provided start date.
     * 
     * @param p_userCalendar
     *            - The user calendar to be used for business interval update.
     * @param p_startDate
     *            - The start date used as the beginning range for the allowable
     *            business interval ranges.
     * @return the user calendar with updated business intervals.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    UserFluxCalendar computeUserCalendarIntervals(
            UserFluxCalendar p_userCalendar, Timestamp p_startDate)
            throws RemoteException, CalendarManagerException;

    /**
     * Create a new calendar.
     * 
     * @param p_calendar
     *            - The calendar to be created.
     * @param p_creatorUserId
     *            - The user id of the creator.
     * @return the created calendar.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    FluxCalendar createCalendar(FluxCalendar p_calendar, String p_creatorUserId)
            throws RemoteException, CalendarManagerException;

    /**
     * Create a new holiday.
     * 
     * @param p_holiday
     *            The holiday to be created.
     * @return The newly created holiday.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    Holiday createHoliday(Holiday p_holiday) throws RemoteException,
            CalendarManagerException;

    /**
     * Create a new reserved time for a user calendar.
     * 
     * @param p_userCalendarId
     *            The user calendar id which this reserved time belongs to.
     * @param p_reservedTime
     *            The reserved time to be created.
     * @return The newly created reserved time.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    ReservedTime createReservedTime(long p_userCalendarId,
            ReservedTime p_reservedTime) throws RemoteException,
            CalendarManagerException;

    /**
     * Create a new user calendar.
     * 
     * @param p_userCalendar
     *            - The user calendar to be created.
     * @param p_creatorUserId
     *            - The user id of the creator.
     * @return the created user calendar object.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    UserFluxCalendar createUserCalendar(UserFluxCalendar p_userCalendar,
            String p_creatorUserId) throws RemoteException,
            CalendarManagerException;

    /**
     * Find a calendar based on the given calendar id.
     * 
     * @param p_calendarId
     *            - The id of calendar to be found.
     * @return The calendar object or null if not found.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    FluxCalendar findCalendarById(long p_calendarId) throws RemoteException,
            CalendarManagerException;

    /**
     * Find the company's default calendar for specific company.
     * 
     * @return The system's default calendar.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    FluxCalendar findDefaultCalendar(String p_companyId)
            throws RemoteException, CalendarManagerException;

    /**
     * Find a holiday based on the given id.
     * 
     * @return The holiday object found.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    Holiday findHolidayById(long p_holidayId) throws RemoteException,
            CalendarManagerException;

    /**
     * Find a list of holidays associated with the calendar based on the give
     * calendar id.
     * 
     * @return A list of holidays for a given calendar.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    List findHolidaysByCalendarId(long p_calendarId) throws RemoteException,
            CalendarManagerException;

    /**
     * Find a reserved time based on the given id. A reserved time only belongs
     * to one calendar.
     * 
     * @return A reserved time based on the given id.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    ReservedTime findReservedTimeById(long p_reservedTimeId)
            throws RemoteException, CalendarManagerException;

    /**
     * Find a list of reserved times based on the given task id and owner of the
     * user calendar.
     * 
     * @param p_userId
     *            - The user id of the calendar owner.
     * @param p_taskId
     *            - The task id which the reserved time was created for.
     * @return A colleciton of reserved times based on the given user calendar
     *         owner and task id.
     * 
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    Collection findReservedTimesByOwnerAndTaskId(String p_userId, long p_taskId)
            throws RemoteException, CalendarManagerException;

    /**
     * Find the reserved times of the given calendar for the specified date.
     * 
     * @param p_userCalendarId
     *            - The id of the user calendar.
     * @param p_date
     *            - The date for which a list of reserved times should be
     *            returned.
     * @return A list of reserved times for that particular date. The returned
     *         reserved times could also have a start date that's before the
     *         given date but the end date happens to be greater than the
     *         specified date.
     * 
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    List findReservedTimesForGivenDate(long p_userCalendarId, Timestamp p_date)
            throws RemoteException, CalendarManagerException;

    /**
     * Find the user calendar based on the given calendar id.
     * 
     * @return The user calendar by the given id.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    UserFluxCalendar findUserCalendarById(long p_userCalendarId)
            throws RemoteException, CalendarManagerException;

    /**
     * Find the user calendar based on the owner's user id.
     * 
     * @return The user calendar.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    UserFluxCalendar findUserCalendarByOwner(String p_OwnerUserId)
            throws RemoteException, CalendarManagerException;

    /**
     * Find the user's time zone from the user calendar.
     * 
     * @param p_userId
     *            - The user id of the calendar owner.
     * @return The time zone of the user's calendar.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception
     */
    TimeZone findUserTimeZone(String p_userId) throws RemoteException,
            CalendarManagerException;

    /**
     * Get all calendars in the system. The objects in the list are of type
     * FluxCalendar.
     * 
     * @return A list of calendars.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    List getAllCalendars() throws RemoteException, CalendarManagerException;

    public List getAllCalendarsByCompanyId(String p_companyId)
            throws RemoteException, CalendarManagerException;

    /**
     * Get a list of all holidays in the system.
     * 
     * @return A list of holidays.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    List getAllHolidays() throws RemoteException, CalendarManagerException;

    /**
     * Get a list of all user calendars in the system. The objects in the list
     * are of type UserFluxCalendar.
     * 
     * @return A list of user calendars found in the system.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    List getAllUserCalendars() throws RemoteException, CalendarManagerException;

    /**
     * Save the imported entries as reserved times associated with their
     * respective user calendar. Note that the type of reserved time that is
     * imported is set to ReservedTime.TYPE_EVENT.
     * 
     * @param p_entries
     *            - A list of Entry objects that would be converted to a
     *            reserved time.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    void importEntries(List p_entries) throws RemoteException,
            CalendarManagerException;

    /**
     * Make the calendar with the given id be the system's default calendar.
     * Note that only one default calendar could exist in the system. In this
     * case, the previous default calendar would be reset to a non-default.
     * 
     * @param p_calendarId
     *            - the id of the calendar which will become the default
     *            calendar.
     * @param p_modifierUserId
     *            - The id of the user performing the action.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    void makeDefaultCalendar(long p_calendarId, String p_modifierUserId)
            throws RemoteException, CalendarManagerException;

    /**
     * Save the modified calendar.
     * 
     * @param p_calendar
     *            - The modified calendar.
     * @param p_modifierUserId
     *            - The id of the user performing the modification.
     * @return The modified calendar.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    FluxCalendar modifyCalendar(FluxCalendar p_calendar, String p_modifierUserId)
            throws RemoteException, CalendarManagerException;

    /**
     * Save the given modified holiday.
     * 
     * @param p_holiday
     *            - The holiday to be save.
     * @return The modified holiday.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    Holiday modifyHoliday(Holiday p_holiday) throws RemoteException,
            CalendarManagerException;

    /**
     * Save the modified user calendar.
     * 
     * @param p_userCalendar
     *            - The modified user calendar to be saved.
     * @param p_modifierUserId
     *            - The id of the user performing the modification.
     * @return The modified user calendar.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    UserFluxCalendar modifyUserCalendar(UserFluxCalendar p_userCalendar,
            String p_modifierUserId) throws RemoteException,
            CalendarManagerException;

    /**
     * Remove the reserved time associated with the given task from the user's
     * calendar. This method is used when a user completes a task or rejects a
     * task that he/she had already accepted.
     * 
     * @param p_taskId
     *            - The id of the task that was either completed or unaccepted
     *            (rejected after acceptance).
     * @param p_userId
     *            - The user id of the person who completed the task (or
     *            unaccepted it).
     * 
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    void removeScheduledActivity(long p_taskId, String p_userId)
            throws RemoteException, CalendarManagerException;

    /**
     * Remove the reserved time associated with the given task from the given
     * user calendar. This method is used when a user completes a task or
     * rejects a task that he/she had already accepted.
     * 
     * @param p_taskId
     *            - The id of the task that was either completed or unaccepted
     *            (rejected after acceptance).
     * @param p_calendar
     *            - The calendar of the user.
     * 
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    void removeScheduledActivity(long p_taskId, UserFluxCalendar p_calendar)
            throws RemoteException, CalendarManagerException;

    /**
     * Remove the reserved time associated with the given task from the user
     * calendars. This method is used when the task that was not accepted yet
     * was reassigned (so the possible type activities would be removed).
     * 
     * @param p_taskId
     *            - The id of the task that was reassigned.
     * 
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    void removeScheduledActivities(long p_taskId) throws RemoteException,
            CalendarManagerException;

    /**
     * Remove the calendar with the give id from the system. Note that a default
     * calendar or a calendar that is associated with one or more user calendars
     * cannot be removed (an exception will be thrown in these cases).
     * 
     * @param p_calendarId
     *            - The id of the calendar to be removed.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    void removeCalendar(long p_calendarId) throws RemoteException,
            CalendarManagerException;

    /**
     * Remove the holiday with the given id from the system. The removal will
     * also remove the holiday for the calendar's that it was associated with.
     * 
     * @param p_holidayId
     *            - The id of the holiday to be removed.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    void removeHoliday(long p_holidayId) throws RemoteException,
            CalendarManagerException;

    /**
     * Remove the user calendar from the system.
     * 
     * @param p_ownerUserId
     *            - The owner's user id of the calendar to be removed.
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    void removeUserCalendar(String p_ownerUserId) throws RemoteException,
            CalendarManagerException;

    /**
     * Provide the unavailable times for each user of the specified user info
     * list. The information is grouped in a Map where the user info is the key
     * and a list of reserved times (which also includes holidays specified with
     * a "Holiday" type as a reserved time) as the value. Note that the
     * unavialability is determined for the whole given month of the year.
     * 
     * @param p_userInfos
     *            A list of UserInfo objects for whom the reserved times (and
     *            holidays as reserved times) are queried.
     * @param p_month
     *            The month for which the reserved times are queried.
     * @param p_year
     *            The year for which the reserved times are queried.
     * 
     * @return A Map with user info as key and list of reserved times as value.
     * 
     * @throws RemoteException
     *             Network related exception.
     * @throws CalendarManagerException
     *             Component related exception.
     */
    HashMap userUnavailabilityReport(List p_userInfos, int p_month, int p_year)
            throws RemoteException, CalendarManagerException;
}
