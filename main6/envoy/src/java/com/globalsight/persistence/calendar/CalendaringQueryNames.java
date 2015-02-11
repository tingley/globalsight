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
package com.globalsight.persistence.calendar;

/**
 * Specifies the names of all the registered named queries.
 */
public interface CalendaringQueryNames
{
    ///////////////////
    //  Holiday
    ///////////////////
    /**
     * A named query to return all holidays.
     * <p>
     * Arguments: none.
     */
    public static String ALL_HOLIDAYS = "getAllHolidays";

    /**
     * A named query to return the holidays specified by the given calendar id.
     * <p>
     * Arguments: 1: Calendar Id.
     */
    public static String ALL_HOLIDAYS_BY_CAL_ID = "allHolidaysByCalId";

    /**
     * A named query to return the holiday specified by the given id.
     * <p>
     * Arguments: 1: Holiday Id.
     */
    public static String HOLIDAY_BY_ID = "holidayById";


    /**
     * A named query to return the holidays based on a user id and 
     * date (month and year).
     * <p>
     * Arguments: 1: Username.
     * Arguments: 2: Month of the holiday.
     * Arguments: 3: Year of the holiday.
     */
    public static String HOLIDAYS_BY_USERID_AND_DATE = "holidaysByUserIdAndDate";

    ///////////////////
    //  Calendar
    ///////////////////
    /**
     * A named query to return all calendars.
     * <p>
     * Arguments: none.
     */
    public static String ALL_CALENDARS = "getAllCalendars";

    /**
     * A named query to return all calendars by the given holiday id.
     * <p>
     * Arguments: 1: Holiday Id.
     */
    public static String ALL_CALENDARS_BY_HOLIDAY_ID = 
        "getAllCalendarsByHolidayId";

    /**
     * A named query to return the default calendar.
     * <p>
     * Arguments: none.
     */
    public static String DEFAULT_CALENDAR = "getDefaultCalendar";

    /**
     * A named query to return the calendar specified by the given id.
     * <p>
     * Arguments: 1: Calendar Id.
     */
    public static String CALENDAR_BY_ID = "calendarById";


    ///////////////////
    //  User Calendar
    ///////////////////
    /**
     * A named query to return all user calendars.
     * <p>
     * Arguments: none.
     */
    public static String ALL_USER_CALENDARS = "getAllUserCalendars";

    /**
     * A named query to return the user calendar specified by the given id.
     * <p>
     * Arguments: 1: User Calendar Id.
     */
    public static String USER_CALENDAR_BY_ID = "userCalendarById";    

    /**
     * A named query to return the user calendars specified by the given 
     * parent calendar's id.
     * <p>
     * Arguments: 1: Calendar Id (id of the parent calendar).
     */
    public static String USER_CAL_BY_PARENT_CAL_ID = "userCalByParentCalId";

    /**
     * A named query to return the user calendars specified by the given 
     * reserved time id.
     * <p>
     * Arguments: 1: Reserved Time Id 
     */
    public static String USER_CALENDAR_BY_RESERVED_TIME_ID = 
        "userCalByReservedTimeId";

    /**
     * A named query to return the user calendar specified by the given 
     * owner username.
     * <p>
     * Arguments: 1: Owner's user id.
     */
    public static String USER_CALENDAR_BY_OWNER = "userCalendarByOwner";

    /**
     * A named query to return the time zone of the user calendar for
     * the given owner username.
     * <p>
     * Arguments: 1: Owner's user id.
     */
    public static String USER_CALENDAR_TIME_ZONE_BY_OWNER = 
        "userCalendarTimeZoneByOwner";

    ///////////////////
    //  Reserved Time
    ///////////////////
    /**
     * A named query to return all reserved times for a given user calendar
     * id.
     * <p>
     * Arguments: 1: User Calendar Id.
     */
    public static String RESERVED_TIMES_BY_USER_CAL_ID = 
        "getReservedTimesByUserCalId";

    /**
     * A named query to return all reserved times for a given user calendar
     * id and a given start date.
     * <p>
     * Arguments: 1: User Calendar Id.
     *            2: Start Date.
     */
    public static String RESERVED_TIMES_BY_CAL_ID_AND_START_DATE = 
        "getReservedTimesByCalIdAndStartDate";

    /**
     * A named query to return all reserved times with an end date
     * less than the given date.
     * <p>
     * Arguments: 1: End Date.     
     */
    public static String RESERVED_TIMES_BEFORE_GIVEN_END_DATE = 
        "getReservedTimesBeforeGivenEndDate";

    /**
     * A named query to return the reserved time specified by the given id.
     * <p>
     * Arguments: 1: Reserved Time Id.
     */
    public static String RESERVED_TIME_BY_ID = "reservedTimeById";    

    /**
     * A named query to return all reserved times for a given user
     * in the specified date range (start and end date).
     * <p>
     * Arguments: 1: Calendar's owner username.
     *            2: Start Date.
     *            2: End Date.
     */
    public static String RESERVED_TIMES_BY_OWNER_AND_DATE_RANGE = 
        "getReservedTimesByOwnerAndDateRange";

    /**
     * A named query to return the reserved times specified by the given
     * owner of the user calendar and the task id.
     * <p>
     * Arguments: 1: Reserved Time's task Id.
     * Arguments: 2: User calendar owner
     */
    public static String RESERVED_TIMES_BY_OWNER_AND_TASK_ID = 
        "reservedTimeByOwnerAndTaskId";

    /**
     * A named query to return the reserved times specified by the given
     * user calendar owners.
     * <p>
     * Arguments: 1: User calendar's owner ids (comma separated).
     */
    public static String RESERVED_TIMES_BY_OWNERS = 
        "reservedTimeByOwners";

    /**
     * A named query to return the reserved times specified by the given
     * task id.
     * <p>
     * Arguments: 1: Reserved Time's task Id.
     */
    public static String RESERVED_TIMES_BY_TASK_ID = 
        "reservedTimeByTaskId";
}
