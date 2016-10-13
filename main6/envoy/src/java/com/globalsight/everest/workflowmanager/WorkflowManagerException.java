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
package com.globalsight.everest.workflowmanager;

import com.globalsight.util.GeneralException;

/**
 * WorkflowManagerException is a subclass of GeneralException.  All
 * workflowManager related exceptions will throw this exception.
 */
public class WorkflowManagerException
    extends GeneralException
{
    
	private static final long serialVersionUID = 7733048016258073L;

	public final static String PROPERTY_FILE_NAME = "WorkflowManagerException";

    //
    // Component Specific Error Message Id's
    //
    public final static String MSG_REMOTE_METHOD_FAILED = "remoteFailure";
    public final static String MSG_FAILED_TO_GET_WORKFLOW_BY_ID =
        "workflowById";
    public final static String MSG_FAILED_TO_GET_LIST_OF_WORKFLOWS =
        "listOfWorkflows";
    public final static String MSG_FAILED_TO_GET_TASKS = "listOfTasks";
    public final static String MSG_FAILED_TO_MODIFY_WORKFLOW = "failedToModifyWf";
    public final static String MSG_FAILED_TO_REROUTE = "reroute";
    public final static String MSG_FAILED_TO_GET_IFLOW_INSTANCE =
        "iflowInstance";
    public final static String MSG_FAILED_TO_GET_TASKINFO_IN_DEFAULT_PATH = 
        "failedToGetTaskInfoInDefaultPath";
    public final static String MSG_FAILED_TO_GET_TASKS_IN_DEFAULT_PATH = 
        "failedToGetTasksInDefaultPath";
    public final static String MSG_FAILED_TO_UPDATE_WORKFLOW =
        "failUpdateWorkflow";
    public final static String MSG_FAILED_TO_EXPORT_WORKFLOW =
        "failExportWorkflow";
    public final static String MSG_FAILED_TO_ARCHIVE_WORKFLOW =
        "failArchiveWorkflow";
    public final static String MSG_FAILED_TO_CANCEL_WORKFLOW =
        "failCancelWorkflow";

    // Arg: 1 = workflow id or job id
    //      2 = user id requesting the cancel
    public final static String MSG_FAILED_TO_CANCEL_USER_NOT_ALLOWED = 
        "failCancelWorkflowUserNotAllowed";

    public final static String MSG_FAILED_TO_SET_TASK_COMPLETION =
        "failTaskCompletion";
    // failed to find the rate to update a task
    // Arg: 1 - task id
    public final static String MSG_FAILED_TO_FIND_RATE_FOR_TASK = 
        "failRateRetrievalForTask";
    public final static String MSG_FAILED_TO_GET_PERSISTENCE_SERVICE =
        "failToGetPersistenceService";
    public final static String MSG_PERSISTENCE_FAILURE = "persistenceFailure";
    public final static String MSG_FAILED_TO_DISPATCH_WORKFLOW =
        "failDispatchWorkflow";

    //Arguments: 1 - workflow id
    public final static String MSG_FAILED_TO_UPDATE_WORKFLOW_DURATION =
        "failUpdateWorkflowDuration";

    // Arguments: 1 - workflow id (iflow instance id)
    public final static String MSG_FAILED_TO_CALCULATE_WORKFLOW_COST =
        "failToCalculateWorkflowCost";     

    // Arguments: 1 - Task id
    public final static String MSG_FAILED_TO_START_CSTF_PROCESS =
        "failedToStartStfCreationProcess";

    // Msg for failure to update the planned completion date of a workflow.
    // Arguments: 1 - workflow id 
    public final static String MSG_FAILED_TO_UPDATE_PCD =
        "failedToUpdatePcd";

    public final static String MSG_FAILED_TO_LEVERAGE_SOURCE_PAGE =
        "failedToLeverageSourcePage";
    
    public final static String MSG_FAIL_TO_CREATE_TARGET_PAGE =
        "failedToCreateTargetPage";

    public final static String MSG_FAILED_IMPORT_ALL_TARGETS_ACTIVE
        = "failedToImportAllTargetPage";
    public final static String MSG_FAILED_TO_SET_EXCEPTION_IN_REQUEST
        ="failedToSetExceptionInRequest";

    public final static String MSG_FAILED_TO_COPY_FILE_TO_STORAGE
        = "failedToCopyFileToStorage";
    public final static String MSG_FAILED_TO_IMPORT_PAGE =
        "failedToImportPage";
    public final static String MSG_FAILED_TO_ADD_WORKFLOW =
        "failedToAddWorkflow";
    public final static String MSG_FAILED_TO_UPDATE_WORKFLOWS=
        "failedToUpdateWorkflows";
    public final static String MSG_FAILED_TO_RETRIEVE_WORKFLOW_REQUEST=
        "failedToRetrieveWorkflowRequest";
    /**
     * Constructs an instance of WorkflowManagerException using the
     * exception identification.
     *
     * @param p_exceptionId The id for the type of exception.
     */
    public WorkflowManagerException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * Constructs an instance of WorkflowManagerException using the
     * exception identification, message identification, and the
     * original exception.
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null
     * @param p_originalException Original exception that caused the
     * error.  It can be null.
     */

    public WorkflowManagerException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            PROPERTY_FILE_NAME);
    }

    /**
     * Constructs an instance of WorkflowManagerException using the
     * exception identification, message identification, and the
     * original exception.
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null
     * @param p_originalException Original exception that caused the
     * error. It can be null.
     * @param p_propertyFileName Property file base name. If the
     * property file is LingMessage.properties, the parameter should
     * be "LingMessage".
     */
    public WorkflowManagerException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException,
        String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            p_propertyFileName);
    }
}
