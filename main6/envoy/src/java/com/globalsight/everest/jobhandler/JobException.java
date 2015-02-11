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
package com.globalsight.everest.jobhandler;

import com.globalsight.util.GeneralException;

/**
 * An exception thrown during the process of creating and processing jobs.
 */
public class JobException
    extends GeneralException
{
    public final static String PROPERTY_FILE_NAME = "JobException";

    public final static String MSG_PERSISTENCE_SERVICE_FAILURE = "persistenceFailure";
    public final static String MSG_WORKFLOWMANAGER_FAILURE = "workflowManagerFailure";
    public final static String MSG_JOBEVENTOBSERVER_FAILURE = "jobEventObserverFailure";
    public final static String MSG_PROJECTHANDLER_FAILURE = "projectHandlerFailure";
    public final static String MSG_REMOTE_METHOD_FAILED = "remoteFailure";
    public final static String MSG_COSTING_ENGINE_FAILURE = "costingEngineFailure";

    /////////////////////////////////////////////////////
    // Activity exceptions
    /////////////////////////////////////////////////////

    // Args: 1 - activity name
    public final static String MSG_FAILED_TO_CREATE_ACTIVITY = "activityCreation";
    // Args: 1 - activity name
    public final static String MSG_FAILED_TO_CREATE_ROLE_FOR_ACTIVITY  = "createRoleForActivity";
    // Args: 1 = activity name
    public final static String MSG_FAILED_TO_GET_ACTIVITY = "getActivity";
    // Args: 1 = activity name
    public final static String MSG_FAILED_TO_MODIFY_ACTIVITY = "modifyActivity";
    // Args: 1 = activity name
    public final static String MSG_FAILED_TO_REMOVE_ACTIVITY = "removeActivity";
    // Args: 1 - activity name
    public final static String MSG_FAILED_TO_REMOVE_ROLE_FOR_ACTIVITY = "removeRoleForActivity";
    // Args: 1 = activity name
    public final static String MSG_ACTIVITY_ALREADY_EXISTS = "activityAlreadyExists";

    //////////////////////////////////////////////////////
    // Company exceptions
    //////////////////////////////////////////////////////
    // Args: 1 - activity name
    public final static String MSG_FAILED_TO_CREATE_COMPANY = "companyCreation";
    public final static String MSG_FAILED_TO_CREATE_COMPANY_CATEGORY = "CompanyCategoryCreation";
    // Args: 1 = activity name
    public final static String MSG_FAILED_TO_GET_COMPANY_BY_ID = "getCompanyById";
    public final static String MSG_FAILED_TO_GET_COMPANY_BY_NAME = "getCompanyByName";
    // Args: 1 = activity name
    public final static String MSG_FAILED_TO_MODIFY_COMPANY = "modifyCompany";
    // Args: 1 = activity name
    public final static String MSG_FAILED_TO_REMOVE_COMPANY = "removeCompany";
    // Args: 1 = activity name
    public final static String MSG_COMPANY_ALREADY_EXISTS = "companyAlreadyExists";
    public final static String MSG_FAILED_TO_GET_ALL_COMPANIES = "getCompanies";
    //////////////////////////////////////////////////////
    // Job exceptions
    ///////////////////////////////////////////////////////

    public final static String MSG_FAILED_TO_ARCHIVE_JOB = "archiveJob";
    public final static String MSG_FAILED_TO_CANCEL_WORKFLOW    = "cancelWorkflow";
    public final static String MSG_FAILED_TO_CHECK_FOR_JOB_CREATION = "jobCreationCheck";
    public final static String MSG_FAILED_TO_CREATE_NEW_JOB ="newJobCreation";
    public final static String MSG_FAILED_TO_CREATE_WORKFLOW_INSTANCES  = "workflowInstances";
    public final static String MSG_FAILED_TO_DISPATCH = "dispatchJob";
    public final static String MSG_FAILED_TO_GET_ALL_ACTIVITIES = "getAllActivities";
    public final static String MSG_FAILED_TO_GET_ALL_DTP_ACTIVITIES = "getAllDtpActivities";
    public final static String MSG_FAILED_TO_GET_ALL_TRANS_ACTIVITIES = "getAllTransActivities";
    public final static String MSG_FAILED_TO_GET_JOB_BY_ID = "jobById";
    public final static String MSG_FAILED_TO_LOOKUP_DISPATCHENGINE = "dispatchEngineLookup";
    public final static String MSG_FAILED_TO_QUERY_JOB_CREATION_RULE = "jobCreationRule";
    public final static String MSG_FAILED_TO_SET_TASK_FINISHED  = "taskFinished";
    public final static String MSG_FAILED_TO_SEND_EMAIL = "sendEmail";
    public final static String MSG_NO_JOBS_IN_PENDING_STATE = "noPendingJobs";
    public final static String MSG_FAILED_TO_CANCEL_JOB = "failCancelJob";
    public final static String
        MSG_FAILED_TO_GET_JOB_BY_STATE_AND_PROJECT_MANAGER  = "noJobsForPMAndState";
    public final static String MSG_FAILED_TO_UPDATE_WORDCOUNT_REACHED = "noWordCountReached";
    // Arguments:  1 = Job id
    public final static String MSG_FAILED_TO_REMOVE_ERROR_REQUEST = "removeErrorRequestFromJobFailure";
    // Arguments: 1 = Job id
    public final static String MSG_FAILED_TO_CANCEL_NON_EXISTENT_ERROR_REQUEST =
        "cancelNonExistentErrorRequest";
    public final static String MSG_FAILED_TO_MAKE_JOB_READY = "makeJobReady";
    // Arguments: 1 = Job id
    public final static String
        MSG_FAILED_TO_MOVE_FAILED_JOB_INTO_PENDING_STATE_AND_DISPATCH = "failedJobToPendingAndDispatch";
    //message used for workflow ui
    public final static String MSG_FAILED_TO_GET_JOB_BY_STATE   = "jobsByState";
    public final static String MSG_FAILED_TO_GET_JOB_BY_RATE   = "jobsByRate";
    public final static String MSG_FAILED_TO_GET_WORKFLOW_BY_JOBID= "jobById";
    // Arguments: 1 = source page id
    public final static String
        MSG_FAILED_TO_GET_JOB_BY_SOURCE_PAGE_ID = "jobBySourcePageId";
    public final static String MSG_FAILED_TO_DISPATCH_WORKFLOW ="manualDispatch";
    // Arguments: 1 = job id
    //            2 = word count
    public final static String MSG_FAILED_TO_OVERRIDE_WORD_COUNT = "failedWordCountOverride";
    // Arguments: 1 = job id
    public final static String MSG_FAILED_TO_CLEAR_WC_OVERRIDE = "failedWordCountOverrideClear";


    ///////////////////////////////////////////////////////////////////
    //// Component Specific Exception Id's from DISPATCH EXCEPTION ////
    ///////////////////////////////////////////////////////////////////
    public final static String MSG_FAILED_TO_FIND_DISPATCH_ENGINE = "failToFindDispatchEngine";
    public final static String MSG_FAILED_TO_CREATE_DISPATCH_TIMER = "dispatchTime";
    public final static String MSG_FAILED_TO_LOAD_DISPATCH_CRITERIA = "loadDispatch";
    public final static String MSG_FAILED_TO_READY_TO_BE_DISPATCHED = "readyToBeDispatch";
    public final static String MSG_FAILED_TO_START_DISPATCHING  = "startDispatching";
    public final static String MSG_FAILED_TO_STOP_DISPATCH_TIMER = "dispatchTimer";
    public final static String MSG_FAILED_TO_STORE_DISPATCH_CRITERIA = "storeDispatch";
    public final static String MSG_FAILED_TO_UPDATE_QUEUE = "updateQueue";
    public final static String MSG_TIME_STRINGERVAL_WASNT_SPECIFIED = "stringerval";
    public final static String MSG_UNIT_OF_TIME_SPECIFIED_IS_INVALID = "invalidUnitOfTime";
    public final static String MSG_UNIT_OF_TIME_WASNT_SPECIFIED = "noUnitOfTime";
    public final static String MSG_WORKFLOW_NEEDS_TO_BE_MANUALLY_DISPATCHED = "manualDispatch";

    /**
     * Constructs an instance of JobException using the exception
     * identification.
     * @param p_exceptionId The id for the type of exception.
     */
    public JobException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * Constructs an instance of JobException using the exception
     * identification, and original exception.
     * @param p_exceptionId The id for the type of exception.
     * @param p_originalException The originating exception.
     */
    public JobException(String p_messageKey, String[] p_messageArguments,
        Exception p_originalException, String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments,
            p_originalException, p_propertyFileName);
    }

    /**
     * Constructs an instance of JobException using the exception
     * identification, and original exception.
     * @param p_exceptionId The id for the type of exception.
     * @param p_originalException The originating exception.
     */
    public JobException(String p_messageKey, String[] p_messageArguments,
        Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments,
            p_originalException, PROPERTY_FILE_NAME);
    }
}
