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

import java.util.Collection;
import java.util.List;

import java.rmi.RemoteException;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;

import com.globalsight.everest.page.SourcePage;

/**
The Leverager is responsible for finding exact and/or fuzzy matches for Tuvs 
inside of a leverage group. A leverage group is currently defined as a page 
(static content) or a database record (dynamic content).
<p>
A leverage group provides a tighter context for the matching algorithms.
Any match found inside the same leverage group is considered of better quality 
than matches found in the TM at large. Internally the Leverager maintains a 
collection of TmHits. For each TmHit the Leverager creates rows in the 
LEVERAGE_MATCH table.
<p>
    Contents of the LEVERAGE_MATCH Table:
    ·    Pointer to the original source Tuv.
    ·    Pointer to the matching source Tuv.
    ·    Copy of the target Tuv. Possibly with stripped codes.
    ·    Pointer to original target Tuv.
    ·    Match Type.
    ·    Fuzzy Score.
    ·    Priority.
    ·    Locale ID.
<p>
Use Cases:
1. Leverage only against a leverage group (page or DB record). Also known as
   "leverage for re-import".
2. Leverage against the entire TM.
*/
public interface Leverager
{

    /**
    Leverage Against the Entire TM
    ·    Used during all imports. But really only useful when there are 
         preexisting TM's , or when some content in the current project TM has 
         been translated. The later implies that we have the ability to localize 
         the project piecemeal. This is always the case for dynamic content, 
         but seldom the case for static content.
    ·    Search for matches across multiple TMs. Prefer matches within the 
         same leverage group. TMs higher in the list will be preferred.
    ·    Use exclude types (LeverageExcludeTypes). The norm is to leverage 
         only translatable content, but the behavior is totally dependent on 
         the LeverageExcludeTypes list.
    ·    Only a match if the locType (translatable or localizable) is the same.
    ·    NOTE: Don't match on the input segment itself!

    @param p_leverageGroupIds - The group id (page) or group ids (db records) 
     that we need to leverage.
    @param p_tmIdsToSearch - List of TM ids to search.
    @param p_sourceLocale - Source locale to match against.
    @param p_targetLocales - List of target locales for which we return 
     leveraged matches.
    @param p_leverageProperties - (see below) Leverage specific parameters 
     (max hits, fuzzy threshold, match type cutoff etc..)
    @param p_leverageExcludeTypes - tuv_types to exclude from the search.
    @param p_services - used to get reference to the TuvLingManager.
    @return ExactMatchedSegments object
    */

    // OBSOLETE
//     ExactMatchedSegments leverage(
//         SourcePage p_sourcePage,
//         List p_tmIdsToSearch,
//         GlobalSightLocale p_sourceLocale,
//         LeveragingLocales p_leveragingLocales,
//         LeverageProperties p_leverageProperties,
//         Collection p_excludeTypes,
//         Collection p_leverageExcludeTypes)
//         throws RemoteException, LingManagerException;
    

    /**
    Leverage Against a Leverage Group (aka, "leverage for re-import")
    ·    Used only during re-import.
    ·    Search for matches only within the leverage group initially.
    ·    Use exclude types (LeverageGroupExcludeTypes) - but the norm is to 
         exclude nothing.
         That is, search for all translatable and localizable content.
    ·    Leverage against the entire TM, taking care not to re-leverage any 
         existing matches
         found in the previous match step.

    @param p_localeLgIdsMapper - map of group ids and target locales to leverage
    @param p_newPageleverageGroupIds - The group id (page) or group ids 
     (db records) of the re-imported content that we need to leverage.
    @param p_sourceLocale - Source locale to match against.
    @param p_targetLocales - List of target locales for which we return 
     leveraged matches.
    @param p_leverageProperties - (see below) Leverage specific parameters 
     (max hits, fuzzy threshold, match type cutoff etc..)
    @param p_leverageExcludeTypes - tuv_types to exclude from the search.
    @param p_services - used to get reference to the TuvLingManager.
    @return ExactMatchedSegments object
    */

    // OBSOLETE
//     ExactMatchedSegments leverageForReimport(
//         SourcePage p_sourcePage,
//         TargetLocaleLgIdsMapper p_localeLgIdsMapper,
//         List p_tmIdsToSearch,
//         GlobalSightLocale p_sourceLocale,
//         LeveragingLocales p_leveragingLocales,
//         LeverageProperties p_leverageProperties,
//         Collection p_excludeTypes,
//         Collection p_leverageExcludeTypes)
//         throws RemoteException, LingManagerException;
    
    /**
    Leverage Against a Leverage Group (aka, "leverage for re-import")
    ·    Used only during re-import.
    ·    Search for matches only within the leverage group initially.
    ·    Use exclude types (LeverageGroupExcludeTypes) - but the norm is to 
         exclude nothing.
         That is, search for all translatable and localizable content.
    ·    Leverage against the entire TM, taking care not to re-leverage any 
         existing matches
         found in the previous match step.

    @param p_localeLgIdsMapper - map of group ids and target locales to leverage
     (db records) of the re-imported content that we need to leverage.
    @param p_sourceLocale - Source locale to match against.
    @param p_leverageDataCenter LeverageDataCenter holds original source segments. This method will save the matches in this object.
    */
    void leverageForReimport(
        SourcePage p_sourcePage,
        TargetLocaleLgIdsMapper p_localeLgIdsMapper,
        GlobalSightLocale p_sourceLocale,
        LeverageDataCenter p_leverageDataCenter)
        throws RemoteException, LingManagerException;
}
