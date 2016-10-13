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


public interface HolidayConstants
{
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constants used as attribute names
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  End: Constants used as attribute names
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: UI Fields
    //////////////////////////////////////////////////////////////////////

    // fields for holiday creation
    public static final String NAME = "nameField";
    public static final String DESC = "descField";
    public static final String MONTH1 = "month1Field";
    public static final String DAY = "dayField";
    public static final String WHEN = "whenField";
    public static final String DAY_OF_WEEK = "dayofweekField";
    public static final String MONTH2 = "month2Field";
    public static final String YEAR = "yearField";
    public static final String START = "startField";
    public static final String END = "endField";

    // fields for holiday page for a calendar
    public static final String ADD_HOLIDAYS = "addField";
    public static final String REMOVE_HOLIDAYS = "removeField";
    public static final String UPDATED_HOLIDAYS = "updatedField";

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
     * Constant used for a new action.
     */
    public static final String NEW_ACTION = "new1";
    /**
     * Constant used for an edit action.
     */
    public static final String EDIT_ACTION = "edit";
    /**
     * Constant used for a cancel action.
     */
    public static final String CANCEL_ACTION = "cancel";
    /**
     * Constant used for a remove action.
     */
    public static final String REMOVE_HOL_ACTION = "removeHol";
    /**
     * Constant used for a save action.
     */
    public static final String SAVE_ACTION = "save";


    /**
     * list names of all holidays
     */
    public static final String HOLIDAY_LIST = "holidays";
    public static final String HOLIDAY_KEY = "holiday";


    /**
     *  holidays for a particular calendar
     */
    public static final String CAL_HOLIDAY_LIST = "calHolidays";


    //////////////////////////////////////////////////////////////////////
    //  End: Action Parameters
    //////////////////////////////////////////////////////////////////////
}
