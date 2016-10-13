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
package com.globalsight.ling.persistence.leveragematch;

/**
 * Specifies the names of all the named queries for TuvLing.
 */
public interface LeverageMatchQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all Leverage Matches that exactly meet the given
     * argument requirements.
     * <p>
     * Arguments: 1: source page leverage group id
     *            2: Target Locale Id
     */
    public static final String EXACT_MATCHES =
        "getLeverageMatchesByExactMatch";

    /**
     * A named query to return all Leverage Matches that meet the given
     * argument requirements regardless of Match Type
     * <p>
     * Arguments: 1: source page leverage group id
     *            2: Target Locale Id
     */
    public static final String FUZZY_MATCHES =
        "getLeverageMatchesByFuzzyMatch";

    /**
     * A named query to return all Leverage Matches that meet the given
     * argument requirements regardless of Match Type
     * <p>
     * Arguments: 1: Source Tuv Id
     *            2: Target Locale Id
     */
    public static final String FUZZY_MATCHES_FOR_TUV =
        "getLeverageMatchesByFuzzyMatchForTuv";

    /**
     * Retrieves all matches (exact + fuzzy) for a tuv.
     * Arguments: 1: Source Tuv Id
     *            2: Target Locale Id
     */
    public static final String ALL_MATCHES_FOR_TUV =
        "getAllLeverageMatchesForTuv";

    /**
     * A named query to return all Leverage Matches that meet the given
     * argument requirements regardless of Match Type
     * <p>
     * Arguments: 1: Original TUV Ids (list)
     *            2: Target Locale Id
     */
    public static final String BEST_MATCHES =
        "getLeverageMatchesByBestMatch";

}
