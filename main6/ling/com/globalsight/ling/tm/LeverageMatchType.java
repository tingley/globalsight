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
package com.globalsight.ling.tm;

/**
 * Match types returned by the Leverager and default penalties.
 *
 * <P> WARNING: It's important that the numbering of these constants
 * is not changed!  When adding a new constant, place it in relative
 * order to the other types, then renumber.
*/
public interface LeverageMatchType
{
    /**
     */
    static public final int UNKNOWN = -1;
    static public final String UNKNOWN_NAME = "UNKNOWN";
    
    static public final boolean CONTAINTAGS = true;
    static public final boolean WITHOUTTAGS = false;

    /**
     * Guaranteed Exact Match.
     *
     **** Not used in System 4.0 ****
     */
    static public final int GUARANTEED_EXACT_MATCH = 0;
    static public final String GUARANTEED_EXACT_MATCH_NAME =
        "GUARANTEED_EXACT_MATCH";

    /**
     * Matched exactly only in the same leverage group (page or db record).
     */
    static public final int LEVERAGE_GROUP_EXACT_MATCH = 1;
    static public final String LEVERAGE_GROUP_EXACT_MATCH_NAME =
        "LEVERAGE_GROUP_EXACT_MATCH";

    /**
     * Exact match from the same TM. No text normailzations performed.
     */
    static public final int EXACT_MATCH_SAME_TM = 2;
    static public final String EXACT_MATCH_SAME_TM_NAME =
        "EXACT_MATCH_SAME_TM";

    /**
     * Exact match. No text normalizations performed.
     */
    static public final int EXACT_MATCH = 3;
    static public final String EXACT_MATCH_NAME =
        "EXACT_MATCH";

    /**
     * In-progress Exact match. No text normailzations performed.
     */
    static public final int UNVERIFIED_EXACT_MATCH = 4;
    static public final String UNVERIFIED_EXACT_MATCH_NAME =
        "UNVERIFIED_EXACT_MATCH";

    /**
     * If more than one exact matches have different translations they
     * are "demoted" by @link DEMOTED_EXACT_MATCH_PENATLY.
     */
    static public final int DEMOTED_EXACT_MATCH = 5;
    static public final String DEMOTED_EXACT_MATCH_NAME =
        "DEMOTED_EXACT_MATCH";

    /**
     * Assign a fuzzy score between 0 and 100 that denotes the
     * similarity of two strings. Apply the same text normalizations
     * as in the TEXT_ONLY Match.
     *
     * <p>Fuzzy match is calculated on text only, not tags. Any tag
     * differences will marked by assigning an appropriate penalty
     * (@see DEFAULT_TAG_DIFFERENCE_PENALTY).</p>
    */
    static public final int FUZZY_MATCH_SAME_TM = 6;
    static public final String FUZZY_MATCH_SAME_TM_NAME =
        "FUZZY_MATCH_SAME_TM";

    /**
     * Assign a fuzzy score between 0 and 100 that denotes the
     * similarity of two strings. Apply the same text normalizations
     * as in the TEXT_ONLY Match.
     *
     * <p>Fuzzy match is calculated on text only, not tags. Any tag
     * differences will marked by assigning an appropriate penalty
     * (@see DEFAULT_TAG_DIFFERENCE_PENALTY).</p>
    */
    static public final int FUZZY_MATCH = 7;
    static public final String FUZZY_MATCH_NAME = "FUZZY_MATCH";

    /**
     * Assign this penalty when several exact matches have different
     * translations.
     */
    static public final short DEMOTED_EXACT_MATCH_PENATLY = 1;

    /**
     * Assign this penalty when two strings differ in formatting only.
     * Formatting includes tags and whitesapce.
     */
    static public final short DEFAULT_TAG_DIFFERENCE_PENALTY = 1;
}
