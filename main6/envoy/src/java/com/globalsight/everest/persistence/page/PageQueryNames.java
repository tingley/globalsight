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
package com.globalsight.everest.persistence.page;

/**
 * Specifies the names of all the named queries for Page.
 */
public interface PageQueryNames
{
    /**
     * A named query to return a source page based on its id
     * <p>
     * Arguments: 1: Page Id.
     */
    public static String SOURCE_PAGE_BY_ID = "getSourcePageById";

    /**
     * A named query to return a target page based on its id
     * <p>
     * Arguments: 1: Page Id.
     */
    public static String TARGET_PAGE_BY_ID = "getTargetPageById";

    /**
     * A named query to return the most recently saved source page based
     * with the given external page id and source locale id.
     * <p>
     * Arguments: 1: External Page id
     *            2: Source locale id
     */
    public static String LATEST_SOURCE_PAGE = "getLatestSourcePage";

    /**
     * A named query to return the active target page based
     * with the given external page id, source locale id and target locale id.
     * Active is IMPORTING, IMPORT_SUCCESS, ACTIVE_JOB or LOCALIZED
     * <p>
     * Arguments: 1: External Page id
     *            2: Source locale id
     *            3: Target Locale id
     */
    public static String ACTIVE_TARGET_PAGE = "getActiveTargetPage";

    /**
     * A named query to return a target page with the specified
     * source page id and locale id
     * <p>
     * Arguments: 1: Source page id
     *            2: Locale id
     */
    public static String TARGET_PAGE_BY_SOURCE_ID_LOCALE_ID =
        "getTargetPageBySourceIdLocaleId";

    /**
     * A named query to return all target pages associated with source pages
     * with the specified source page id
     * <p>
     * Arguments: 1: Source page id
     */
    public static String TARGET_PAGES_BY_SOURCE_PAGE_ID =
        "getTargetPagesBySourceId";

    /**
    *  A named query to return the target page based on cuv id
    */
    public static String TARGET_PAGE_BY_CUV_ID = "getTargetPageByCuvId";


    /**
     * A named query to return all template parts of a page template 
     * associated with source page with the specified source page id
     * <p>
     * Arguments: 1: Source page id
     */
    public static String TEMPLATE_PARTS_BY_SOURCE_PAGE_ID =
        "getTemplatePartsBySourcePageId";
    /**
    *  A named query to return the source page based on job_id
    */
    public static String SOURCE_PAGE_BY_JOB_ID = "getSourcePageByJobId";

    /**
    *  A named query to return the source page based on cuv id
    */
    public static String SOURCE_PAGE_BY_CUV_ID = "getSourcePageByCuvId";

    /**
     * A named query to return all the source pages in a job that
     * are associated with an extracted file.
     */
    public static String EXTRACTED_SOURCE_PAGES_BY_JOB_ID = 
        "getExtractedSourcePagesByJobId";

    /**
     * A named query to return all the source pages in a job that
     * are associated with an un-extracted file.
     */
    public static String UNEXTRACTED_SOURCE_PAGES_BY_JOB_ID = 
        "getUnextractedSourcePagesByJobId";

    /**
     * A named query to return all source pages that are stuck importing.
     * This happens when the system is shutdown and pages are still importing.
     * This is query is used on start-up to identify and handle these pages.
     */
    public static String SOURCE_PAGES_STILL_IMPORTING = 
        "getSourcePagesStillImporting";

    /**
     * A named query to return all source page that are in a specified state.
     */
    public static String SOURCE_PAGES_BY_STATE = 
        "getSourcePagesInState";

    /**
     * A named query to return all target page that are in a specified state.
     */
    public static String TARGET_PAGES_BY_STATE = 
        "getTargetPagesInState";

}
