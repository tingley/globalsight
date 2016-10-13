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
package com.globalsight.ling.persistence.tuvling;

/**
 * Specifies the names of all the named queries for TuvLing.
 */
public interface TuvLingQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return the TUV with the given id.
     * <p>
     * Arguments: 1: Tuv id
     */
    public static final String TUV_BY_ID = "getTuvById";

    /**
     * A named query to return all TUVs that match the given arguments.
     * <p>
     * Arguments: 1: Exact match key
     *            2: Locale
     *            3: Leverage group id
     *            4: Localization type
     *            5: Exclude types
     *            6: TM id
     */
    public static final String TUVS_BY_EXACT_MATCH = "getTuvsByExactMatch";


    // CvdL
    /**
     * A named query to return all TUVs that match the given arguments.
     * <p>
     * Arguments: 1: Exact match key
     *            2: Locale
     *            3: Localization type
     *            4: !Original source Tuv
     *            5: Exclude types
     *            6: TM ids
     *            7: Target locales
     */
    public static final String ALL_TUVS_BY_EXACT_MATCH =
        "getAllTuvsByExactMatch";

    // CvdL
    /**
     * A named query to return all TUVs that match the given arguments.
     * <p>
     * Arguments: 1: Exact match key
     *            2: Locale
     *            3: Localization type
     *            4: !Original source Tuv
     *            5: Exclude types
     *            6: LG id
     *            7: Target locales
     */
    public static final String ALL_TUVS_BY_LG_EXACT_MATCH =
        "getAllTuvsByLGExactMatch";

    /**
     * A named query to return the TUVs that match the given arguments.
     * <p>
     * Arguments: 1: TU id
     *            2: List of target locales
     */
    public static final String TUVS_BY_TU_ID_TARGET_LOCALES =
        "getTuvsByTuIdTargetLocales";

    /**
     * A named query to return the TUVs that match the given arguments.
     * <p>
     * Arguments: 1: Matched TUV id
     *            3: List of target locales
     */
    public static final String TUVS_BY_TUV_ID_TARGET_LOCALES =
        "getTuvsByTuvIdTargetLocales";

    /**
     * A named query to return leveraged TUVs that match the given arguments.
     * <p>
     * Arguments: 1: Exact match key
     *            2: Locale
     *            3: Leverage group id
     *            4: Localization type
     *            5: Exclude types
     */
    public static final String TUVS_BY_LEVERAGED_EXACT_MATCH =
        "getTuvsByLeveragedExactMatch";

    /**
     * A named query to return all TUVs for the given locale and leverage
     * group id.
     * <p>
     * Arguments: 1: Locale
     *            2: Leverage group id
     */
    public static final String TUVS_BY_LOCALE_LEVERAGE_ID =
        "getTuvsByLocaleLeverageId";

     /**
     * A named query to return all TUVs for the given locale and leverage
     * group id and filter out excluded types.
     * <p>
     * Arguments: 1: Locale
     *            2: Excluded types list
     *            3: Leverage group id
     */
    public static final String TUVS_BY_LOCALE_LEVERAGE_ID_WITHOUT_EXCLUDES =
        "getTuvsByLocaleLeverageIdWithoutExcludes";

    /**
     * A named query to return TUVs by fuzzy match.
     * <p>
     * Arguments: 1: Tuv ids
     *            2: Localization type
     *            3: Exclude types
     */
    public static final String TUVS_BY_FUZZY_MATCH = "getTuvsByFuzzyMatch";
}
