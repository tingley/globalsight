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
package com.globalsight.everest.persistence.tuv;

import com.globalsight.everest.tuv.TuvImpl;

/**
 * Specifies the names of all the named queries for Tuv.
 */
public interface TuvQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //

    /**
     * A named query to return a collection of Tuv ids based on
     * the given SourcePage id and locale id for offline.
     * of ids.
     * <p>
     * Arguments: 
     *      1. A SourcePage id
     *      2. A locale id (of a source page)
     */
    public static final String SOURCE_TUV_IDS_FOR_OFFLINE = 
        "getSourceTuvIdsForOffline";

    /**
     * A named query to return a collection of Tuvs based on
     * the given SourcePage id for offline.
     * of ids.
     * <p>
     * Arguments: 1: A SourcePage id
     */
    public static final String SOURCE_TUVS_FOR_OFFLINE_BY_SOURCE_PAGE_ID
        = "getSourceTuvsForOfflineBySourcePageId";

    /**
     * A named query to return a collection of Tuvs based on
     * the given SourcePage id for statistics.
     * of ids.
     * <p>
     * Arguments: 1: A SourcePage id
     */
    public static final String SOURCE_TUVS_FOR_STATISTICS_BY_SOURCE_PAGE_ID
        = "getSourceTuvsForStatisticsBySourcePageId";

    /**
     * A named query to return a collection of Tuvs based on
     * the given TargetPage id for statistics.
     * of ids.
     * <p>
     * Arguments: 1: A TargetPage id
     */
    public static final String TARGET_TUVS_FOR_STATISTICS_BY_TARGET_PAGE_ID
        = "getTargetTuvsForStatisticsByTargetPageId";


    /**
     * A named query to return a collection of target locale Tuvs
     * based on
     * the given source page id, and target locale.
     * of ids.
     * <p>
     * Arguments: 1: A SourcePage id
     * Arguments: 2: A GlobalSightLocale id
     */
    public static final String EXPORT_TUVS_BY_SOURCE_PAGE_ID =
        "getExportTuvsBySourcePageId";


    public static final String TUV_BY_TU_LOCALE =
        "getTuvByTuLocale";
}
