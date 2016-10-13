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
package com.globalsight.ling.persistence.token;

/**
 * Specifies the names of all the named queries for Token.
 */
public interface TokenQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return tokens based on a fuzzy match.
     * <p>
     * Arguments: 1: Query token count.
     *            2: Token CRCs list.
     *            3: TM IDs list.
     *            4: Locale id.
     *            5: Fuzzy threshold.
     */
    public static final String TOKENS_BY_FUZZY_MATCH = "getTokensByFuzzyMatch";

    /**
     * A named query to return tokens based on a fuzzy match.
     * <p>
     * Arguments: 1: Query token count.
     *            2: Token CRCs list.
     *            3: TM IDs list.
     *            4: Locale id.
     *            5: Segment type
     *            6: Excluded item types
     *            7: Source tuv id
     *            8: Fuzzy threshold.
     *
     */
    public static final String FILTERED_TOKENS_BY_FUZZY_MATCH =
        "getFilteredTokensByFuzzyMatch";

    //
    // THE FOLLOWING CONSTANTS ARE THE OLD NAMES REPRESENTING THE SAME
    // CONSTANTS ABOVE -- PLEASE DON'T USE
    //
    /**
     * A named query to return tokens based on a fuzzy search.
     * <p>
     * Arguments: 1: Query token count.
     *            2: Token CRCs list.
     *            3: TM IDs list.
     *            4: Locale id.
     *            5: Fuzzy threshold.
     *
     * @deprecated use TOKENS_BY_FUZZY_MATCH instead.
     */
    public static final String FUZZY_SEARCH_QUERY = TOKENS_BY_FUZZY_MATCH;
}
