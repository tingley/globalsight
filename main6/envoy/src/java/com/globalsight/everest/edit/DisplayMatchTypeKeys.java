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
package com.globalsight.everest.edit;

/**
 *
 * These keys access the property strings used to display match types in the editors.
 */
public interface DisplayMatchTypeKeys
{ 
    // General
    public static final String MSG_FUZZY_AND_SCORE = "FuzzyMatchWithScore";
    public static final String MSG_OL_FUZZY_ANDSCORE_ANDNOTE = "OfflineFuzzyMatchWithScoreAndNote";
    public static final String MSG_NOMATCH = "NoMatch";
    public static final String MSG_MATCHTYPE_UNKNOWN = "MatchTypeUnknown";

    // Protected ==========================================================================
    // Segment which are provided only for context. Changes to the segment will be ignored.
    // ====================================================================================
    public static final String MSG_OL_EXACT_LOCKED = "OfflineExactMatchProtected";
    public static final String MSG_OL_EXACT_SUB_LOCKED = "OfflineExactMatchSubProtected";
    public static final String MSG_OL_UNVERIFIED_EXACT_LOCKED = "OfflineUnverifiedExactMatchProtected";
    public static final String MSG_OL_UNVERIFIED_EXACT_SUB_LOCKED = "OfflineUnverifiedExactMatchSubProtected";    
    public static final String MSG_OL_CUR_TRG_EXCLUDED = "OfflineTargetExcluded";
    public static final String MSG_OL_CUR_TRG_SUB_EXCLUDED = "OfflineTargetSubExcluded";

    // Unprotected ========================================================================
    // A segment which may be edited.
    // ====================================================================================
    public static final String MSG_OL_EXACT_UNLOCKED = "OfflineExactMatchUnprotected";
    public static final String MSG_OL_EXACT_SUB_UNLOCKED = "OfflineExactMatchSubUnprotected";
    public static final String MSG_OL_UNVERIFIED_EXACT_UNLOCKED = "OfflineUnverifiedExactMatchUnprotected";
    public static final String MSG_OL_UNVERIFIED_EXACT_SUB_UNLOCKED = "OfflineUnverifiedExactMatchSubUnprotected";

}

