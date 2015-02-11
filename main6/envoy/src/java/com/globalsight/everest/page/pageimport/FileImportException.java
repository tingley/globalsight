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
package com.globalsight.everest.page.pageimport;

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved.
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
import com.globalsight.util.GeneralExceptionConstants;

/**
 * An exception handling object for FileImporter component.
 * <p>
 * Supports OLD and new GeneralException
 * For this component the EXCEPTION ID ranges from 2800-2899.
 * For this component the ERROR message ID ranges from 4800-4899.
 * For this component the INFO message ID ranges from 6800-6899.
 * For this component the DEBUG message ID ranges from 8800-8899.
 */

public class FileImportException extends GeneralException
    implements GeneralExceptionConstants
{

    ///////////////////////////////////////////////////////////////////
    ///////////////  Component Specific Exception ID's  ///////////////
    ///////////////////////////////////////////////////////////////////
    public final static int EX_GET_TEMPLATE_FORMAT_ERROR = 2800;

    ///////////////////////////////////////////////////////////////////
    /////////////  Component Specific Error Message ID's  /////////////
    ///////////////////////////////////////////////////////////////////

    //message keys - for new GeneralException

    //takes in one argument - request id
    public final static String MSG_FAILED_TO_CREATE_TU_AND_TUV = "FailedToCreateTuAndTuv";
    //takes in two arguments - request id and external page id
    public final static String MSG_FAILED_TO_GET_PAGE_FOR_IMPORT = "FailedToGetPageForImport";
    //takes in two arguments - request id and external page id
    public final static String MSG_FAILED_TO_ADD_PAGE_TO_REQUEST = "FailedToAddPageToRequest";
    //takes in two arguments - request id and external page id
    public final static String MSG_FAILED_TO_IMPORT_PAGE = "FailedToImportPage";
    //takes in one argument - request id
    public final static String MSG_FAILED_TO_ITERATE_GXML_DOC = "FailedToIterateGxmlDoc";
    //takes in two arguments - source page id and request id
    public final static String MSG_FAIL_TO_CREATE_TARGET_PAGES = "FailedToCreateTargetPages";
    //takes in two arguments - source page id and request id
    public final static String MSG_FAILED_TO_RETRIEVE_LEVERAGE_GROUP_IDS = "FailedToRetrieveLeverageGroupIds";
    //takes in two arguments - source page id and request id
    public final static String MSG_FAILED_TO_LEVERAGE_SOURCE_PAGE = "FailedToLeverageSourcePage";
    //takes in one argument
    public final static String MSG_FAILED_TO_SET_EXCEPTION_IN_REQUEST = "FailedToSetExceptionInRequest";
    public final static String MSG_FAILED_TO_RETRIEVE_ALL_TMS = "FailedToRetrieveAllTmsFromTmManager";
    // argument 1: source page id
    public final static String MSG_FAILED_TO_INDEX = "FailedToIndexPage";

    // DB-related error
    public final static String MSG_FAILED_TO_GET_TEMPLATE_FORMAT = "FailedToGetTemplateFormat";
    // DB-content related error: takes 3 arguments
    public final static String MSG_TEMPLATE_FORMAT_NOT_FOUND = "FailedToFindTemplateFormat";

    public final static String MSG_FAILED_TO_FIND_LEVERAGER = "FailedToFindLeverager";
    public final static String MSG_FAILED_TO_FIND_LING_MANAGER = "FailedToFindLingManager";
    public final static String MSG_FAILED_TO_FIND_PAGE_EVENT_OBSERVER = "FailedToFindPageEventObserver";
    public final static String MSG_FAILED_TO_FIND_PAGE_MANAGER = "FailedToFindPageManager";
    public final static String MSG_FAILED_TO_FIND_REQUEST_HANDLER = "FailedToFindRequestHandler";
    public final static String MSG_FAILED_TO_FIND_TUV_MANAGER = "FailedToFindTuvManager";
    public final static String MSG_FAILED_TO_FIND_TM_MANAGER = "FailedToFindTmManager";
    public final static String MSG_FAILED_TO_FIND_INDEXER = "FailedToFindIndexer";
    public static final String MSG_FAILED_TO_FIND_NATIVE_FILE_MANAGER = "FailedToFindNativeFileManager";

    public static final String MSG_FAILED_TO_IMPORT_ACTIVE_TARGET_PAGE = "FailedToImportActiveTargetPage";
    public static final String MSG_FAILED_IMPORT_ALL_TARGETS_ACTIVE = "FailedImportAllTargetsActive";


    /*
     * Arg 1: The file name (including path) that failed the copy.
     */
    public static final String MSG_FAILED_TO_COPY_FILE_TO_STORAGE = 
        "FailedToCopyFileToStorage";
    /*
     * Arg 1: source page id
     */
    public static final String MSG_FAILED_TO_UPDATE_UNEXTRACTED_FILE_INFO = 
        "FailedToUpdateUnextractedFile";


    // message file name
    private static final String PROPERTY_FILE_NAME = "FileImportException";

    /*
     *
     */
    public FileImportException(String p_messageKey, String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, PROPERTY_FILE_NAME);
    }

    public FileImportException(Exception p_originalException)
    {
        super(p_originalException);
    }
}
