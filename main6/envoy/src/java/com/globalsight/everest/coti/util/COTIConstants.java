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

package com.globalsight.everest.coti.util;

public interface COTIConstants
{
    public static final String ObjType_Project = "project";
    public static final String ObjType_Document = "document";

    public static final String reportType_costEstimation = "costEstimation";
    public static final String reportType_translationStatus = "translationStatus";

    public static final String Dir_Root_Name = "COTIApi";

    public static final String fileType_translation = "translation";
    public static final String fileType_reference = "reference";

    public static final String Dir_TranslationFiles_Name = "translation files";
    public static final String Dir_ReferenceFiles_Name = "reference files";

    // document created, started, revision, finished, cancelled, rejected,
    // unknown
    public static final String document_status_created = "created";
    public static final String document_status_started = "started";
    public static final String document_status_revision = "revision";
    public static final String document_status_finished = "finished";
    public static final String document_status_cancelled = "cancelled";
    public static final String document_status_rejected = "rejected";
    public static final String document_status_unknown = "unknown";

    // project created, started, finished, cancelled, rejected, unknown
    public static final String project_status_created = "created";
    public static final String project_status_started = "started";
    public static final String project_status_finished = "finished";
    public static final String project_status_cancelled = "cancelled";
    public static final String project_status_rejected = "rejected";
    public static final String project_status_unknown = "unknown";
    public static final String project_status_closed = "closed";
    public static final String project_status_deleted = "deleted";

}
