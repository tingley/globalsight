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
package com.globalsight.everest.persistence.page.pageexport;

/**
 * Specifies the names of all the named queries for this package (Page Export related).
 */
public interface PageExportQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES 
    //
    /**
     * A named query to return all available exporting pages by type.
     * <p>
     * Arguments: 1. Exporting Page Type (i.e. Source, Primary Target, ...)
     */
    public static String EXPORTING_PAGE_BY_TYPE = "exportingPageByType";
    
    /**
     * A named query to return an exporting page based on the page id.
     * <p>
     * Arguments: 1: Page Id.
     */
    public static String EXPORTING_PAGE_BY_PAGE_ID = "exportingPageByPageId";
    
    /**
     * A named query to return an exporting page based on the exportBatch id and page id.
     * <p>
     * Arguments: 1: ExportBatch Id  2: Page Id.
     */
    public static String EXPORTING_PAGE_BY_EXPORT_BATCH_ID_PAGE_ID = "exportingPageByExportBatchIdAndPageId";

    /**
     * A named query to return a particular export batch event based on its id. 
     * <p>
     * Arguments: 1 - export batch event id
     */
    public static String EXPORT_BATCH_EVENT_BY_ID = "exportBatchEventById";

    /**
     * A named query to return all export batch events in the system.
     * <p>
     * Arguments: NONE
     */
    public static String ALL_EXPORT_BATCH_EVENTS = "allExportBatchEvents";

    /**
     * A named query to return all export batch events based on a given state. 
     * <p>
     * Arguments: 1 - exporting page's state
     */
    public static String EXPORT_BATCH_EVENTS_BY_STATE = "exportBatchEventsByState";

    /**
     * A named query to return all export batch events that are less than or
     * equal to the given time. 
     * <p>
     * Arguments: 1 - start time
     */
    public static String EXPORT_BATCH_EVENTS_BY_START_TIME = "exportBatchEventsByStartTime";

    /**
     * A named query to return all export batch events for the given job id.
     * <p>
     * Arguments: 1 - job id
     */
    public static String EXPORT_BATCH_EVENTS_BY_JOB_ID = "exportBatchEventsByJobId";
}
