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
package com.globalsight.ling.tm.fuzzy;

import java.util.HashMap;
import java.util.Vector;
import java.util.List;
import java.util.Collection;

import java.rmi.RemoteException;

import com.globalsight.ling.tm.LeverageProperties;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.util.GlobalSightLocale;

/**
FuzzyIndexManager is responsible for interacting with the "FUZZY_INDEX" table.
The FUZZY_INDEX table contains all the persistent resources needed to calculate a fuzzy
score for any indexed Tuv. The LingManager will be implemented using pure JDBC
(i.e., no TopLink layers). The FUZZY_INDEX table stores indexed tokens.
An indexed token is composed of (1) a token (2) Locale (3) Posting Vector
(4) Frequency and (5) TmId.
<p>
Definitions for Linguistic Entities:
<p>
1.    Token - The Indexer generates many tokens for each individual Tuv.  A token
    is a locale specific language feature that will help to identify it's parent
    Tuv during fuzzy match.
2.    Posting Vector - List of Tuv ids that contain a specific token. Also,
    statistics for the Tuv needed to calculate a fuzzy score.
3.    Frequency - The total frequency of the token across all Tuvs in which
    it was observed.
4.    Localization Type - Translatable or localizable.
*/
public interface FuzzyIndexManager
{
    /**
    For each token in p_tokens add a row to the FUZZY_INDEX table with the p_tuvId,
    token CRC, token count, locale and tm id.
    @param p_tokens - A collection of Token objects.
    */
    void updateFuzzyIndex(Vector p_tokens)
    throws FuzzyIndexManagerException, RemoteException;

    /**
    Query the FUZZY_INDEX table and get all matches for each token in p_tokens.
    Limit the search to the supplied TM id and locale.

    @param p_tokens - A collection of Token objects (a hash table of tokens and
        their frequencies).
    @param p_locale - The locale of the Tuv used to generate the token.
    @param p_tmId - The id of the Tm we are searching.
    @return List of FuzzyCandidates sorted by fuzzy score.
    */
    Collection getFuzzyCandidates(
        HashMap p_tokens,
        GlobalSightLocale p_locale,
        List p_tmIds,
        LeverageProperties p_leverageProperties)
    throws FuzzyIndexManagerException, RemoteException;

    // CvdL
    public Collection getFuzzyCandidates(TuvLing p_originalSourceTuv,
        HashMap p_tokens, GlobalSightLocale p_locale, List p_tmIds,
        long p_locType, Collection p_leverageExcludeTypes,
        LeverageProperties p_leverageProperties)
        throws FuzzyIndexManagerException,
               RemoteException;


//    public void callIndexingProcedure(Vector p_paramList)
//        throws FuzzyIndexManagerException,
//               RemoteException;
    public void persistFuzzyIndexes(Vector p_paramList)
        throws FuzzyIndexManagerException,
               RemoteException;
    
}
