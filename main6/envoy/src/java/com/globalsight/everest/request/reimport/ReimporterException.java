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
package com.globalsight.everest.request.reimport;

/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
 *
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

//globalsight
import com.globalsight.util.GeneralException;

/**
 * This exception is thrown for any exception that is related to the
 * internal working of the reimport component.
 */
public class ReimporterException extends GeneralException 
{
    // Reimport related messages are stored in the following property file
    final static String PROPERTY_FILE_NAME = "ReimporterException";
    
    // error in looking up other components	
    static final String MSG_FAILED_TO_FIND_JOB_HANDLER      = "FailedToFindJobHandler";
    static final String MSG_FAILED_TO_FIND_REQUEST_HANDLER  = "FailedToFindRequestHandler";
    static final String MSG_FAILED_TO_FIND_WORKFLOW_SERVER  = "FailedToFindWorkflowServer";
    static final String MSG_FAILED_TO_FIND_USER_MANAGER     = "FailedToFindUserManager";

    // arguments: 1: the request id
    static final String MSG_FAILED_TO_DELAY_REIMPORT          = "FailedToDelayReimport";
    static final String MSG_FAILED_TO_FIND_JOB_OF_PAGE        = "FailedToFindJobOfPage";
    // this is one is only used on start-up
    static final String MSG_FAILED_TO_LOAD_ALL_DELAYED_IMPORTS = "FailedToLoadAndStartImportingAllDelayedRequests";
    // arguments: 1 - job id, 2 - job name, 3 - the previous page id, 4 - the external page id, 
    static final String MSG_FAILED_TO_CANCEL_JOB_OF_PREVIOUS_PAGE = "FailedToCancelJobOfPreviousPage";


    /*
     * 
     */
    public ReimporterException(String p_messageKey, String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, PROPERTY_FILE_NAME);
    }

    /**
     * Constructor where only the original exception is passed.
     */
    public ReimporterException(Exception p_originalException)
    {
        super(p_originalException);
    }
}
