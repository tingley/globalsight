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
package com.globalsight.everest.taskmanager;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * This exception is thrown for any exception that is related to the
 * internal working of the TaskManager component.
 */
public class TaskException extends GeneralException
{
    // Exception messages are stored in the following property file
    public final static String PROPERTY_FILE_NAME = "TaskException";
                 
    ///////////////////////////////////////////////////////////////////
    ////////////  Error Message keys  //////////////
    ///////////////////////////////////////////////////////////////////

    public static final String MSG_FAILED_TO_INIT_SERVER =
        "failedToInitServer";
    public static final String MSG_FAILED_TO_ACCEPT_TASK =
        "failedToAcceptTask";
    public static final String MSG_FAILED_TO_COMPLETE_TASK =
        "failedToCompleteTask";
    public static final String MSG_FAILED_TO_REJECT_TASK =
        "failedToRejectTask";
    public static final String MSG_FAILED_TO_SAVE_COMMENT =
        "failedToSaveComment";
    public static final String MSG_FAILED_TO_GET_CURRENT_TASKS =
        "failedToGetCurrentTasks";
    public static final String MSG_FAILED_TO_GET_TASK =
        "failedToGetTask";
    public static final String MSG_FAILED_TO_GET_TASKS =
        "failedToGetTasks";
    // Args: 1 - workflow id
    public static final String MSG_FAILED_TO_GET_WF_ACCEPTED_TASKS = 
        "failedToGetWorkflowAcceptedTasks";
    public static final String MSG_FAILED_TO_UPDATE_TASK =
        "failedToUpdateTask";
    public static final String MSG_FAILED_TO_GET_TASK_IN_TRANSIT =
        "failedToGetActivityInTransit";
    public static final String MSG_FAILED_TO_GET_TASKS_BY_NAME_AND_JOB_ID =
        "failedToGetTasksByNameAndJobId";
    // Args: 1 - job id
    //       2 - rate type
    public static final String MSG_FAILED_TO_GET_TASKS_OF_JOB_BY_RATE_TYPE =
        "failedToGetTasksOfJobByRateType";
    public static final String MSG_FAILED_TO_GET_TASK_COMPLETION_TIME =
        "failedToGetTaskCompletionTime";

    // Args: 1 - workflow id
    public static final String MSG_FAILED_TO_GET_COMPLETED_TASKS =
        "failedToGetCompletedTasks";
    // Args: 1 - workflow id
    public static final String MSG_FAILED_TO_GET_TASKS_FOR_RATING = 
        "failedToGetTasksForRating";
    public static final String MSG_FAILED_TO_GET_TRANSLATE_TEXT =
            "failedToGetTranslatedText";

    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public TaskException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the error.
     * It can be null.
     */
    public TaskException(String p_messageKey, String[] p_messageArguments,
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
    public TaskException(String p_messageKey,
                         String[] p_messageArguments,
                         Exception p_originalException,
                         String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments, p_originalException, 
              p_propertyFileName);        
    }
}
