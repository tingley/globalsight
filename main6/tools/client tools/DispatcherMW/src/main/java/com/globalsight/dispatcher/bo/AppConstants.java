/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.dispatcher.bo;

public interface AppConstants
{
    public static final String PROJECT_NAME = "DispatcherMW";
    public static final String MSMT_CONTENT_TYPE = "text/plain";
    
    // WebService JSON Parameter Name
    public final String JSONPN_SOURCE_LANGUAGE = "srcLang";
    public final String JSONPN_TARGET_LANGUAGE = "trgLang";
    public final String JSONPN_SOURCE_TEXT = "src";
    public final String JSONPN_TARGET_TEXT = "trg";
    public final String JSONPN_STATUS = "status";
    public final String JSONPN_JOBID = "jobID";
    public final String JSONPN_ACCOUNT_ID = "accountID";
    public final String JSONPN_ACCOUNT_NAME = "accountName";
    public final String JSONPN_ERROR_MESSAGE = "errorMsg";
    public final String JSONPN_SECURITY_CODE = "securityCode";
    
    public final String XLF_SOURCE_LANGUAGE = "source-language";
    public final String XLF_TARGET_LANGUAGE = "target-language";
    public final String XLF_SOURCE_FOLDER = "source";
    public final String XLF_TARGET_FOLDER = "target";
    
    public final String STATUS_QUEUED = "queued";
    public final String STATUS_RUNNING = "running";
    public final String STATUS_COMPLETED = "completed";
    public final String STATUS_FAILED = "failed";    
    
    // WebService Status & Error Message 
    public final String STATUS_SUCCESS = "success";
    public final String STATUS_FAIl = "fail";
    public final String ERROR_NO_MTPROFILE = "No matched MT Profile is found.";
    public final String ERROR_NO_RESULT = "Can't get translated text.";
    public final String ERROR_NO_LOCALE = "Please check the input language:";
    
    // Language Error Message
    public final String MTPLanguage_ERROR_NAMEEXIST = "The Name already exists.";
    public final String MTPLanguage_ERROR_LPEXIST = "The Locale Pair already exists.";
    public final String MTPLanguage_ERROR_UNSUPPORT = "The Machine Translation Profile doesn't support the Locale Pair.";
    
    public final String FOLDER_DATA = "data";
    public final String FOLDER_File_Storage = "fileStorage";
    public final String FOLDER_TEMP = "temp";
}
