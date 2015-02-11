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

import com.globalsight.util.GeneralException;


/**
 * Represents an exception thrown within CalendarManager remote interface.
 */
public class CalendarManagerException
    extends GeneralException
{
    
    // PUBLIC CONSTANTS
    public final static String PROPERTY_FILE_NAME = 
        "CalendarManagerException";
    
    // Exception Message Keys
    public final static String MSG_CANNOT_REMOVE_DEFAULT_CAL = 
        "cannotRemoveDefaultCal";
    public final static String MSG_CANNOT_REMOVE_DUE_TO_DEPENDENCY =
        "cannotRemoveDueToDependency";
    public final static String MSG_CREATE_CALENDAR_FAILED = 
        "failedToCreateCalendar";
    public final static String MSG_CREATE_HOLIDAY_FAILED = 
        "failedToCreateHoliday";
    public final static String MSG_CREATE_RESERVED_TIME_FAILED = 
        "failedToCreateReservedTime";
    public final static String MSG_CREATE_USER_CALENDAR_FAILED = 
        "failedToCreateUserCalendar";
    public final static String MSG_FAILED_TO_CHECK_FOR_DEPENDENCY = 
        "failedToCheckForDependency";
    public final static String MSG_FAILED_TO_IMPORT_ENTRIES = 
        "failedToImportEntries";
    public final static String MSG_FIND_CALENDAR_FAILED = 
        "failedToFindCalendar";
    public final static String MSG_FIND_DEFAULT_CALENDAR_FAILED = 
        "failedToFindDefaultCalendar";
    public final static String MSG_FIND_HOLIDAY_FAILED = 
        "failedToFindHoliday";
    public final static String MSG_FIND_HOLIDAY_BY_CAL_ID_FAILED = 
        "failedToFindHolidayByCalId";
    public final static String MSG_FIND_RESERVED_TIME_FAILED = 
        "failedToFindReservedTime";
    public final static String MSG_FIND_RESERVED_TIME_FOR_TASK_FAILED = 
        "failedToFindReservedTimeForTask";
    public final static String MSG_FIND_RESERVED_TIMES_FOR_DATE_FAILED = 
        "failedToFindReservedTimesForDate";
    public final static String MSG_FIND_USER_CAL_BY_ID_FAILED = 
        "failedToFindUserCalById";
    public final static String MSG_FIND_USER_CAL_BY_OWNER_FAILED = 
        "failedToFindUserCalByOwner";
    public final static String MSG_FIND_USER_CAL_BY_RESERVED_TIME_FAILED = 
        "failedToFindUserCalByReservedTime";
    public final static String MSG_FIND_USER_TIME_ZONE = 
        "failedToFindUserTimeZone";
    public final static String MSG_GET_ALL_CALENDARS_FAILED = 
        "failedToGetAllCalendars";
    public final static String MSG_GET_CALENDARS_BY_HOLIDAY_ID_FAILED = 
        "failedToGetCalendarsByHolidayId";
    public final static String MSG_GET_ALL_HOLIDAYS_FAILED = 
        "failedToGetAllHolidays";
    public final static String MSG_GET_ALL_USER_CALENDARS_FAILED = 
        "failedToGetAllUserCalendars";
    public final static String MSG_MAKE_DEFAULT_CALENDAR_FAILED = 
        "failedToMakedDefaultCalendar";
    public final static String MSG_MODIFY_CALENDAR_FAILED = 
        "failedToModifyCalendar";
    public final static String MSG_MODIFY_HOLIDAY_FAILED = 
        "failedToModifyHoliday";
    public final static String MSG_MODIFY_RESERVED_TIME_FAILED = 
        "failedToModifyReservedTime";
    public final static String MSG_MODIFY_USER_CALENDAR_FAILED = 
        "failedToModifyUserCalendar";
    public final static String MSG_REMOVE_CALENDAR_FAILED = 
        "failedToRemoveCalendar";
    public final static String MSG_REMOVE_HOLIDAY_FAILED = 
        "failedToRemoveHoliday";
    public final static String MSG_REMOVE_RESERVED_TIME_FAILED = 
        "failedToRemoveReservedTime";
    public final static String MSG_REMOVE_USER_CALENDAR_FAILED = 
        "failedToRemoveUserCalendar";
    


    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public CalendarManagerException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the error. 
     *        It can be null.
     */
    public CalendarManagerException(String p_messageKey,
                                   String[] p_messageArguments,
                                   Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, 
             PROPERTY_FILE_NAME);
    }
}
