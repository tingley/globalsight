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

/**
 * This interface defines the states for a Page.  The states are
 * defined as package level constants since they are ONLY accessed
 * through PageManager.
 */
public interface PageState
{
    /**
     * Constant used for identifying a page in the Importing state.
     */
    public final static String IMPORTING = "IMPORTING";
    /**
     * Constant used for a page that failed to be imported.
     */
    public final static String IMPORT_FAIL = "IMPORT_FAIL";
    /**
     * Constant used for a page that was successfully imported.
     */
    public final static String IMPORT_SUCCESS = "IMPORT_SUCCESS";
    /**
     * Constant used for identifying a page as an out of date page.
     */
    public final static String OUT_OF_DATE = "OUT_OF_DATE";
    /**
     * Constant used for identifying a page as not localized.
     * Like when the job is cancelled.
     */
    public final static String NOT_LOCALIZED = "NOT_LOCALIZED";
    /**
     * Constant used for identifying a page that's part of an active job.
     */
    public final static String ACTIVE_JOB = "ACTIVE_JOB";
    /**
     * Constant used for identifying a page that has been localized.
     */
    public final static String LOCALIZED = "LOCALIZED";
    /**
     * Constant used for identifying a page that has been exported.
     */
    public final static String EXPORTED = "EXPORTED";
    /**
     * Constant used for identifying a page in the process of being exported.
     */
    public final static String EXPORT_IN_PROGRESS = "EXPORT_IN_PROGRESS";
    /**
     * Constant used for identifying a page that failed to be exported.
     */
    public final static String EXPORT_FAIL = "EXPORT_FAIL";
    /**
     * Constant used for identifying a page that it's export process
     * was cancelled.
     */
    public final static String EXPORT_CANCELLED = "EXPORT_CANCELLED";
    /**
     * Constant used for identifying a page when its GXML is being updated.
     */
    public final static String UPDATING = "UPDATING";
}
