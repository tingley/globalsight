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

import com.globalsight.util.GeneralException;


/**
 * Represents an exception thrown during the process of scheduling events.
 */
public class EventSchedulerException
    extends GeneralException
{
    
    // PUBLIC CONSTANTS
    public final static String PROPERTY_FILE_NAME = 
        "EventSchedulerException";
    
    // Exception Message Keys
    public final static String MSG__CREATE_JMS_POOL_FAILED = 
        "failedToCreateJMSPool";
    public final static String MSG_HOLIDAY_BIZ_INTERVAL_FAILED = 
        "failedToMakeHolidayBizInterval";
    public final static String MSG_WORKING_DAY_BIZ_INTERVAL_FAILED = 
        "failedToMakeWorkingDayBizInterval";
    public final static String MSG_RESERVED_TIME_BIZ_INTERVAL_FAILED = 
        "failedToMakeReservedTimeBizInterval";
    public final static String MSG_CALENDAR_BIZ_INTERVALS_FAILED = 
        "failedToMakeCalendarBizIntervals";
    public final static String MSG_SCHEDULING_FAILED = 
        "schedulingFailed";
    public final static String MSG_UNSCHEDULING_FAILED = 
        "unschedulingFailed";
    
    


    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public EventSchedulerException(Exception p_originalException)
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
    public EventSchedulerException(String p_messageKey,
                                   String[] p_messageArguments,
                                   Exception p_originalException)
    {
        this(p_messageKey, p_messageArguments, p_originalException, 
             PROPERTY_FILE_NAME);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the error. 
     *        It can be null.
     * @param p_propertyFileName Property file base name. If the property file 
     *        is LingMessage.properties, the parameter should be "LingMessage".
     */
    protected EventSchedulerException(String p_messageKey,
                                      String[] p_messageArguments,
                                      Exception p_originalException,
                                      String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments, p_originalException, 
              p_propertyFileName);        
    }
}
