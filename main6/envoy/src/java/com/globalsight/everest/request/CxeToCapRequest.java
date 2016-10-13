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

/**
 * Stores the possible values for the messages CXE sends to CAP for
 * localization.
 */
public interface CxeToCapRequest
{
    // the parameters that are part of the request
    // see Request.java for the actual request types
    final static String REQUEST_TYPE = "CapRequestType";
    // may be extracted or unextracted
    final static String CONTENT = "Content";
    final static String L10N_REQUEST_XML = "L10nRequestXml";
    final static String EXCEPTION = "Exception";
    final static String EVENT_FLOW_XML = "EventFlowXml";
    final static String DISPLAY_NAME = "DisplayName";

    // the parameters that reflect status of the request
    final static String RECEIVER_PROBLEM = "Receiver Problem: ";

    // values stored in the XML
    final static String TRUE = "true";
    final static String FALSE = "false";

    // possible values within the L10nRequestXml
    interface L10nRequestXml
    {
        final static String L10N_REQUEST_XML      = "l0nRequestXml";
        final static String DATA_SOURCE_TYPE      = "dataSourceType";
        final static String DATA_SOURCE_ID        = "dataSourceId";
        final static String IS_PAGE_PREVIEWABLE   = "pageIsCxePreviewable";
        final static String IMPORT_INITIATOR      = "importInitiatorId";
        final static String UNIQUE_PAGE_NAME      = "externalPageId";
        final static String SOURCE_CODE_SET       = "originalCharacterEncoding";
        final static String L10N_PROFILE_ID       = "l10nProfileId";
        final static String BASE_HREF             = "baseHref";
        final static String BATCH_INFO            = "batchInfo";
        final static String BATCH_ID              = "batchId";
        final static String PAGE_COUNT            = "pageCount";
        final static String PAGE_NUMBER           = "pageNumber";
        final static String DOC_PAGE_COUNT        = "docPageCount";
        final static String DOC_PAGE_NUMBER       = "docPageNumber";
        final static String JOB_PREFIX_NAME       = "jobPrefixName";
        final static String JOB_PRIORITY		  = "priority";
        final static String ORIGINAL_SOURCE_FILE_CONTENT = "originalSourceFileContent";
    };
};
