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
package com.globalsight.everest.webapp.pagehandler.administration.calendars;


public interface CalendarConstants
{

    /**
     * Constants used for distinguishing between system and user calendars
     */
    public static final int SYSTEM = 0;
    public static final int USER = 1;

    /**
     * Constants used for determining type of day
     */
    public static final int WORKINGDAY = 0;
    public static final int NONWORKINGDAY = 1;
    public static final int RESERVEDTIME = 2;

    /**
     * Constant for saving calendar in session
     */
    public static final String CALENDAR = "cal";
    
    /**
     * Constants for saving current month and year viewing in session
     */
    public static final String VIEWMONTH = "viewMonth";
    public static final String VIEWYEAR = "viewYear";
    
    /**
     * Constant for saving state for every day in the month
     */
    public static final String DAY_STATE = "dayState";
    
    /**
     * Constants for creating a user cal
     */
    public static final String BASE_CAL_ID = "baseCalId";
    public static final String TIME_ZONE = "timezone";

    /**
     * Constant used as a key for a list of dependencies.
     */
    public static final String DEPENDENCIES = "dependencies";

    //////////////////////////////////////////////////////////////////////
    //  Begin: UI Fields
    //////////////////////////////////////////////////////////////////////
    // fields for the first page of template creation.
    public static final String NAME_FIELD = "nameField";
    public static final String TZ_FIELD = "tzField";
    public static final String BUFFER_FIELD = "bufferField";
    public static final String DEF_CAL_FIELD = "defCalField";
    public static final String BIZ_HOURS_FIELD = "bizHoursField";
    public static final String MONTH_FIELD = "monthField";
    public static final String YEAR_FIELD = "yearField";
    public static final String DAY_FIELD = "dayField";
    public static final String FROM1_FIELD = "from1Field";
    public static final String FROM2_FIELD = "from2Field";
    public static final String FROM3_FIELD = "from3Field";
    public static final String FROM4_FIELD = "from4Field";
    public static final String FROM5_FIELD = "from5Field";
    public static final String TO1_FIELD = "to1Field";
    public static final String TO2_FIELD = "to2Field";
    public static final String TO3_FIELD = "to3Field";
    public static final String TO4_FIELD = "to4Field";
    public static final String TO5_FIELD = "to5Field";
    public static final String FROMMIN1_FIELD = "frommin1Field";
    public static final String FROMMIN2_FIELD = "frommin2Field";
    public static final String FROMMIN3_FIELD = "frommin3Field";
    public static final String FROMMIN4_FIELD = "frommin4Field";
    public static final String FROMMIN5_FIELD = "frommin5Field";
    public static final String TOMIN1_FIELD = "tomin1Field";
    public static final String TOMIN2_FIELD = "tomin2Field";
    public static final String TOMIN3_FIELD = "tomin3Field";
    public static final String TOMIN4_FIELD = "tomin4Field";
    public static final String TOMIN5_FIELD = "tomin5Field";
    public static final String MON_TIME_FIELD = "monTimeField";
    public static final String TUE_TIME_FIELD = "tueTimeField";
    public static final String WED_TIME_FIELD = "wedTimeField";
    public static final String THU_TIME_FIELD = "thuTimeField";
    public static final String FRI_TIME_FIELD = "friTimeField";
    public static final String SAT_TIME_FIELD = "satTimeField";
    public static final String SUN_TIME_FIELD = "sunTimeField";
    
    public static final String BASE_CAL_FIELD = "baseCalField";

    public static final String SUBJECT_FIELD = "subjectField";
    public static final String START_HOUR_FIELD = "startHourField";
    public static final String START_MIN_FIELD = "startMinField";
    public static final String END_HOUR_FIELD = "endHourField";
    public static final String END_MIN_FIELD = "endMinField";
    public static final String END_MONTH_FIELD = "endMonthField";
    public static final String END_DAY_FIELD = "endDayField";
    public static final String END_YEAR_FIELD = "endYearField";

    //////////////////////////////////////////////////////////////////////
    //  End: UI Fields
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Action Parameters
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used for a action.
     */
    public static final String ACTION = "action";
    /**
     * Constant used for a apply action.
     */
    public static final String APPLY_ACTION = "apply";
    /**
     * Constant used for a new action.
     */
    public static final String NEW_ACTION = "new1";
    /**
     * Constant used for a next action.
     */
    public static final String NEXT_ACTION = "next";
    /**
     * Constant used for a previous action.
     */
    public static final String PREVIOUS_ACTION = "previous";
    /**
     * Constant used for an edit action.
     */
    public static final String EDIT_ACTION = "edit";
    /**
     * Constant used for a cancel action.
     */
    public static final String CANCEL_ACTION = "cancel";
    /**
     * Constant used for a cancel of adding a holiday to a calendar.
     */
    public static final String CANCEL_HOL_ACTION = "cancelHol";
    /**
     * Constant used for a viewing a different date (month/year) in a calendar
     */
    public static final String CHANGE_DATE_ACTION = "changeDate";
    /**
     * Constant used for a changing the base calendar
     */
    public static final String CHANGE_BASE_ACTION = "changeBase";
    /**
     * Constant used for a duplicate action.
     */
    public static final String DUPLICATE_ACTION = "duplicate";
    /**
     * Constant used for a remove action.
     */
    public static final String REMOVE_ACTION = "remove";
    /**
     * Constant used for a remove a holiday from the system.
     */
    public static final String REMOVE_HOL_ACTION = "removeHol";
    /**
     * Constant used for reserved times list
     */
    public static final String RESERVED_TIMES_ACTION = "reservedTimes";
    /**
     * Constant used for reserved time
     */
    public static final String RESERVED_TIME_ACTION = "reservedTime";
    /**
     * Constant used for going to activity page from reserved times list
     */
    public static final String ACTIVITY_ACTION = "getTask";
    /**
     * Constant used for a save action.
     */
    public static final String SAVE_ACTION = "save";
    /**
     * Constant used for a making a calendar the default cal action.
     */
    public static final String MAKE_DEFAULT_ACTION = "makeDefault";
    /**
     * Constant used for going to holidays page.
     */
    public static final String HOLIDAYS_ACTION = "holidays";
    /**
     * Constant used for going to system calendars page.
     */
    public static final String SYS_CALS_ACTION = "sysCals";
    /**
     * Constant used for going to user calendars page.
     */
    public static final String USER_CALS_ACTION = "userCals";
    /**
     * Constant used for going to a calendars holidays
     */
    public static final String CAL_HOLIDAYS_ACTION = "calHolidays";

    //////////////////////////////////////////////////////////////////////
    //  End: Action Parameters
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  The following are used in the pages that have lists
    //////////////////////////////////////////////////////////////////////
    public static final String SYS_CAL_LIST = "sysCals";
    public static final String SYS_CAL_KEY = "sysCal";
    public static final String USER_CAL_LIST = "userCals";
    public static final String USER_CAL_KEY = "userCal";
    public static final String HOLIDAY_LIST = "holidays";
    public static final String HOLIDAY_KEY = "holiday";
    public static final String RT_LIST = "reservedTimes";
    public static final String RT_KEY = "reservedTime";

}
