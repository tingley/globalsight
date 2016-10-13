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
package com.globalsight.everest.page;

import com.globalsight.util.GeneralException;


/**
 * An exception thrown during the process of creating and
 * processing Pages.
 */
public class PageException
    extends GeneralException
{
    /**
     * Page related messages are stored in the following property file
     */
    static public final String PROPERTY_FILE_NAME = "PageException";

    ///////////////////////////////////////////////////////////////////
    ////////////  Component Specific Error Message Id's  //////////////
    ///////////////////////////////////////////////////////////////////
    static public final String MSG_FAILED_TO_CREATE_PAGE = "createPage";

    static public final String MSG_FAILED_TO_UPDATE_PAGE = "updatePage";

    static public final String MSG_FAILED_TO_DELETE_PAGE = "deletePage";

    // argument 1: request id of the page being imported
    static public final String MSG_FAILED_TO_IMPORT_PAGE = "importPage";

    static public final String MSG_FAILED_TO_GET_PAGE_BY_ID = "pageById";

    static public final String MSG_FAILED_TO_LOCATE_INDEXER = "locateIndexer";

    static public final String MSG_FAILED_TO_GET_TARGET_PAGE_BY_SOURCE_AND_LOCALE
        = "targetPageBySourceAndLocale";

    // argument 1: page id (could be source or target page)
    //          2: internal base href
    //          3: external base href
    static public final String MSG_FAILED_TO_UPDATE_BASE_HREFS
        = "updateBaseHref";

    // arg 1: target or source page (text "target" or "source")
    // arg 2: page id
    static public final String MSG_FAILED_TO_UPDATE_UNEXTRACTED_FILE
        = "updateUnextractedFile";

    static public final String MSG_FAILED_TO_UPDATE_WORD_COUNT
        = "updateWordCount";

    static public final String MSG_FAILED_TO_GET_TARGET_PAGES_OF_SOURCE
        = "targetPagesOfSource";

    // arg 1: workflow id
    static public final String MSG_FAILED_TO_GET_IMPORT_SUCCESS_TARGET_PAGES
        = "importSuccessTargetPagesOfWorkflow";

    static public final String MSG_FAILED_TO_GET_SOURCE_PAGES_STILL_IMPORTING
        = "sourcePagesStillImporting";

    static public final String MSG_FAILED_TO_GET_PAGE_BY_NAME_AND_LOCALE
        = "pageByNameAndLocale";

    static public final String MSG_FAILED_TO_GET_SOURCE_LOCALE
        = "getSourceLocale";

    static public final String MSG_FAILED_TO_UPDATE_PAGE_STATE
        = "updatePageState";

    static public final String MSG_FAILED_TO_LOCATE_TUV_EVENT_OBSERVER
        = "locateTuvEventObserver";

    static public final String MSG_FAILED_TO_LOCATE_WF_EVENT_OBSERVER
        = "locateWorkflowEventObserver";

    static public final String MSG_FAILED_TO_LOCATE_PAGE_EVENT_OBSERVER
        = "locatePageEventObserver";

    static public final String MSG_FAILED_TO_LOCATE_TUV_MANAGER
        = "locateTuvManager";

    static public final String MSG_FAILED_TO_LOCATE_LING_MANAGER
        = "locateLingManager";

    static public final String MSG_FAILED_TO_CLONE_SOURCE_PAGE
        = "cloneSourcePage";

    static public final String MSG_PAGETEMPLATE_GETPAGEDATA_TUVS_NOT_FILLED
        = "pageTemplateGetpagedataNoTuvFilled";

    static public final String MSG_PAGETEMPLATE_GETPAGEDATA_INVALID_PARTS
        = "pageTemplateGetpagedataInvalidParts";

    static public final String MSG_PAGETEMPLATE_GETPAGEDATA_PARTS_TUVS_UNMATCH
        = "pageTemplateGetpagedataPartsTuvsUnmatch";

    static public final String MSG_FAILED_TO_GET_TEMPLATE_PARTS
        = "getTemplateParts";

    static public final String MSG_FAILED_TO_GET_TUVS = "getTuvs";

    static public final String MSG_FAILED_TO_CONNECT_TO_CXE = "connectToCXE";

    static public final String MSG_FAILED_TO_GET_EXPORT_REQUEST_INFO
        = "exportRequestInfo";

    static public final String MSG_FAILED_TO_SET_EXPORT_PARAMETER
        = "setExportParam";

    static public final String MSG_FAILED_TO_GET_PREVIEW_INFO
        = "getPreviewInfo";

    static public final String MSG_FAILED_TO_CREATE_JMS_POOL
        = "createJmsPool";

    static public final String MSG_PAGE_IN_UPDATING_STATE_ERROR = 
        "pageInUpdatingStateError";

    // Args: 1 - source page id
    //       2 - validation errors
    static public final String MSG_FAILED_PAGE_UPDATE_VALIDATION = 
        "pageUpdateValidationFailed";


    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public PageException(Exception p_originalException)
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
    public PageException(String p_messageKey,
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
    protected PageException(String p_messageKey,
        String[] p_messageArguments,
        Exception p_originalException,
        String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            p_propertyFileName);
    }
}
