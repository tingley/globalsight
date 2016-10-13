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
package com.globalsight.everest.request;

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
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
 * internal working of the Request server component.
 */
public class RequestHandlerException extends GeneralException 
{
    // Reuqest related messages are stored in the following property file
    final static String PROPERTY_FILE_NAME = "RequestHandlerException";
    
    // error in looking up other components	
    static final String MSG_FAILED_TO_FIND_JOBCREATOR       = "FailedToFindJobCreator";
    static final String MSG_FAILED_TO_FIND_PROJECT_HANDLER  = "FailedToFindProjectHandler";
    static final String MSG_FAILED_TO_FIND_PAGE_MANAGER = "FailedToFindPageManager";
    static final String MSG_FAILED_TO_FIND_REQUEST_HANDLER = "FailedToFindRequestHandler";
    static final String MSG_FAILED_TO_FIND_FILE_PROFILE_MANAGER = "FailedToFindFileProfilePersistenceManager";
    static final String MSG_FAILED_TO_FIND_DB_PROFILE_MANAGER = "FailedToFindDatabaseProfilePersistenceManager";
    static final String MSG_FAILED_TO_FIND_USER_MANAGER = "FailedToFindUserManager";

    //takes in three arguments - request id (may be 0), external page id, attribute that can't be updated (as string)
    static final String MSG_PAGE_ATTRIBUTE_CAN_NOT_BE_UPDATED   = "PageAttributeCanNotBeUpdated";          
    //takes in one argument - request id
    static final String MSG_FAILED_TO_ADD_REQUEST_TO_JOBCREATOR = "FailedToAddRequestToJobCreator"; 
    //takes in one argment - request id
    static final String MSG_FAILED_TO_FIND_REQUEST          = "FailedToFindRequest";
    // takes in one argument - page id
    static final String MSG_FAILED_TO_FIND_REQUEST_BY_PAGE_ID = "FailedToFindRequestByPageId";
    //takes in one argument - profile id
    static final String MSG_FAILED_TO_GET_L10N_PROFILE      = "FailedToGetL10nProfile"; 
    //takes in three arguments - external page id, data source type, data source id
    static final String MSG_FAILED_TO_PERSIST_REQUEST       = "FailedToPersistRequest";
    //takes in two arguments - request id and page id
    static final String MSG_FAILED_TO_ADD_PAGE_TO_REQUEST   = "FailedToAddPageToRequest";
    //takes in two arguments - data profile id and request id
    static final String MSG_FAILED_TO_GET_DATA_SOURCE_NAME  = "FailedToGetDataSourceName";

    //takes in two arguments - external page id, l10nProfile name
    static final String MSG_PAGE_CAN_NOT_BE_IMPORTED        =  "PageCanNotBeImported";
    //takes in one argument - external page id (name)
    static final String MSG_PAGE_ALREADY_PART_OF_ACTIVE_JOB =  "PagePartOfActiveJob";
    // takes in one argument - external page id (name)
    static final String MSG_PAGE_WITH_SNIPPETS_IN_OF_ACTIVE_JOB = "PageWithSnippetsInActiveJob";
    //takes in two arguments - request id, external page id
    static final String MSG_FAILED_TO_REIMPORT = "PageFailedToBeReimported";
    static final String MSG_FAILED_TO_START_DELAYED_IMPORTS = "FailedToStartDelayedImportsOnStartup";
    // takes in one argument
    static final String MSG_FAILED_TO_COMPLETE_IMPORT_DURING_SHUTDOWN = 
                                                "PageFailedToCompleteImportDuringShutdown";
    public final static String MSG_FAILED_TO_GET_REQUESTS_STILL_IMPORTING =
                                            "FailedToGetRequestsStillImporting";
    public final static String MSG_FAILED_TO_CLEANUP_INCOMPLETE_IMPORTS = 
                                            "FailedToCleanupIncompleteImports";

    //takes in two argument - external page id, request type
    static final String MSG_INVALID_REQUEST_TYPE            = "InvalidRequestType";

    //parser and xml errors
    static final String MSG_FAILED_TO_PARSE_L10N_REQUEST_XML= "FailedToParseL10nRequestXml";            

    //takes in two arguments - request id and serialized exception
    static final String MSG_FAILED_TO_UPDATE_EXCEPTION_IN_REQUEST = "FailedToUpdateExceptionInRequest";

    /*
     * 
     */
    public RequestHandlerException(String p_messageKey, String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, PROPERTY_FILE_NAME);
    }

    /**
     * Constructor - Pass the original exception.
     */
    public RequestHandlerException(Exception p_originalException)
    {
        super(p_originalException);
    }
}
