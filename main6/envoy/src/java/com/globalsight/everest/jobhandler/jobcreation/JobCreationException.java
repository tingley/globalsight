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
package com.globalsight.everest.jobhandler.jobcreation;

/* Copyright (c) 2001, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

import com.globalsight.util.GeneralException;

/**
 * An exception thrown during the process of creating jobs and adding requests
 * to a job.
 */
public class JobCreationException extends GeneralException
{
    // property file names
    public final static String PROPERTY_FILE_NAME = "JobCreationException";

    // message keys in the property file
    public final static String MSG_FAILED_TO_FIND_WORKFLOW_SERVER = "FailedToFindWorkflowServer";
    public final static String MSG_FAILED_TO_FIND_COSTING_ENGINE = "FailedToFindCostingEngine";
    public final static String MSG_FAILED_TO_FIND_JOB_DISPATCHER = "FailedToFindJobDispatcher";
    public final static String MSG_FAILED_TO_FIND_JOB_EVENT_OBSERVER = "FailedToFindJobEventObserver";
    public final static String MSG_FAILED_TO_FIND_TUV_MANAGER = "FailedToFindTuvManager";
    public final static String MSG_FAILED_TO_FIND_LEVERAGE_MATCH_LING_MANAGER = "FailedToFindLeverageMatchLingManager";
    public final static String MSG_FAILED_TO_FIND_PAGE_EVENT_OBSERVER = "FailedToFindPageEventObserver";

    // Args: 1 = batch id
    public final static String MSG_QUERY_FOR_BATCH_JOB_FAILED = "QueryForBatchJobFailed";
    // Args: 1 = request id
    public final static String MSG_FAILED_TO_CREATE_ERROR_PAGE = "FailedToCreateErrorPage";
    // Args: 1 = request id, 2 = source page id
    public final static String MSG_FAILED_TO_ADD_PAGE_TO_REQUEST = "FailedToAddPageToRequest";
    // Args: 1 = source page id
    public final static String MSG_FAILED_TO_NOTIFY_IMPORT_FAILURE = "FailedToNotifyImportFailure";
    // Args: 1 = request id
    public final static String MSG_IMPORT_PAGE_FAILED = "FailedToImportPage";
    //
    public final static String MSG_FAILED_TO_IMPORT_ALL_TARGETS_SUCCESSFULLY = "FailedToImportAllTargetsSuccessfully";
    public final static String MSG_FAILED_TO_INITIALIZE_NEW_JOB = "FailedToInitializeNewJob";
    // Args: 1 = request id - to add to newly created job
    public final static String MSG_FAILED_TO_CREATE_NEW_JOB = "FailedToCreateNewJob";
    // Args: 1 = request id - of request to add to found or new job
    public final static String MSG_FAILED_TO_QUERY_JOB_CREATION_RULE = "FailedToQueryJobCreationRule";
    // Args: 1 = request id, 2 = job id
    public final static String MSG_FAILED_TO_ADD_REQUEST_TO_JOB = "FailedToAddRequestToJob";
    // Args: 1 = request id (job id doesn't exist yet when creating these)
    public final static String MSG_FAILED_TO_CREATE_WORKFLOW_INSTANCES = "FailedToCreateWorkflowInstances";
    // Args: 1 = job id
    public final static String MSG_FAILED_TO_CREATE_DTP_WORKFLOW_INSTANCES = "FailedToCreateDtpWorkflowInstances";
    // Args: 1 = job id
    public final static String MSG_FAILED_TO_CREATE_A_JOB_DISPATCHER = "FailedToCreateAJobDispatcher";
    // Args: 1 = source page id
    public final static String MSG_STATISTICS_FAILED_FOR_PAGES = "FailedToGenerateStatisticsForSourcePageAndTargetPages";
    // Args: 1 = job id, 2 = state to change to
    public final static String MSG_FAILED_TO_UPDATE_JOB_STATE = "FailedToUpdateJobState";
    // Args: 1 = job id
    public final static String MSG_FAILED_TO_UPDATE_WORKFLOW_AND_PAGE_STATE = "FailedToUpdateWorkflowAndPageState";
    // Args: 1 = request id, 2 = exception
    public final static String MSG_FAILED_TO_SET_EXCEPTION_IN_REQUEST = "FailedToSetExceptionInRequest";

    public final static String MSG_FAILED_TO_GET_REQUEST_LIST = "FailedToGetRequestList";

    public final static String MSG_FAILED_TO_FIND_JOB_IN_DB = "FailedToFindJobInDB";

    /*
     * Create a JobCreationException with the specified message.
     * 
     * @p_messageKey The key to the message located in the property file.
     * 
     * @p_messageArguments An array of arguments needed for the message. This
     * can be null.
     * 
     * @p_originalException The original exception that this one is wrapping.
     * This can be null.
     */
    public JobCreationException(String p_messageKey,
            String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
                PROPERTY_FILE_NAME);
    }
}
