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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.util.GlobalSightLocale;

/**
 * LeverageMatchLingManager is responsible for interacting with the
 * "LEVERAGE_MATCH" table. The LEVERAGE_MATCH table contains all the matches
 * gleaned from the TM by the Leverager.
 * 
 * The interface is implemented by
 * com.globalsight.everest.integration.ling.tm.LeverageMatchLingManagerLocal;
 */
public interface LeverageMatchLingManager
{
    public static final int NO_MATCH = 1;
    public static final int FUZZY = 2;
    public static final int EXACT = 3;
    public static final int UNVERIFIED = 4;
    public static final int STATISTICS = 5;

    /**
     * Constants that indicate the match category. (Moved from
     * com.globalsight.ling.tm2.persistence.LeverageMatchSaver).
     */
    public static final int SEGMENT_TM_T = 1;
    public static final int SEGMENT_TM_L = 2;
    public static final int PAGE_TM_T = 3;
    public static final int PAGE_TM_L = 4;
    public static final int IN_PROGRESS_TM_T = 5;
    public static final int IN_PROGRESS_TM_L = 6;

    /**
     * Delete leverage matches for specified TUV.
     * 
     * @param p_OriginalSourceTuvId
     *            -- Can not be null
     * @param p_subId
     *            -- Can be null
     * @param p_targetLocaleId
     *            -- Can be null
     * @param p_orderNum
     *            -- Can be null
     * @param p_companyId
     *            -- Can be null
     */
    public void deleteLeverageMatches(Long p_OriginalSourceTuvId,
            String p_subId, Long p_targetLocaleId, Long p_orderNum,
            Long p_companyId);

    /**
     * Delete leverage matches for specified source page.
     * 
     * @param p_sourcePageId
     */
    public void deleteLeverageMatches(Long p_sourcePageId);

    /**
     * For each source Tuv find any exact matches in the LEVERAGE_MATCH table
     * that should be leveraged into target tuvs.
     * 
     * @param p_sourcePageId
     *            source page ID
     * @param p_targetLocaleId
     *            target locale id
     * @return HashMap of target Strings. Key - source Tuv Id, Value -
     *         LeverageSegment.
     */
    HashMap getExactMatches(Long p_spLgId, Long p_targetLocaleId)
            throws RemoteException, LingManagerException;

    /**
     * This method returns all the fuzzy matches for the given sourceTuvIds. If
     * it finds DEMOTED_EXACT_MATCH or FUZZY_MATCH in LEVERAGE_MATCH table, it
     * returns all the matches it found. If it doesn't find fuzzy matches, it
     * means that the exact match for the segment has been already copied to the
     * target TUV or there is no match for the segment.
     * 
     * @param p_sourePageId
     *            soure page Id
     * @param p_targetLocaleId
     *            target locale id
     * @return HashMap of LeverageMatch. Key - source tuv id, Value - SortedSet
     *         of LeverageMatch in sorted order
     * 
     *         If the matches for certain Tuv id are not found, the Tuv id won't
     *         be included in the key. That is, the length of p_sourceTuvId and
     *         the length of the list of keys in the returned HashMap can be
     *         different.
     */
    HashMap getFuzzyMatches(Long p_sourePageId, Long p_targetLocaleId)
            throws RemoteException, LingManagerException;

    HashMap getExactMatchesWithSetInside(Long p_sourcePageId,
            Long p_targetLocaleId, int model, TranslationMemoryProfile tmProfile);

    /**
     * This method returns all the fuzzy matches for the given sourceTuvId. If
     * it finds DEMOTED_EXACT_MATCH or FUZZY_MATCH in LEVERAGE_MATCH table, it
     * returns all the matches it found. If it doesn't find fuzzy matches, it
     * means that the exact match for the segment has been already copied to the
     * target TUV or there is no match for the segment.
     * 
     * @param p_sourceTuvId
     *            source Tuv Id
     * @param p_targetLocaleId
     *            target locale id
     * @return SortedSet of LeverageMatch in sorted order. If no match is found,
     *         it returns null
     */
    SortedSet<LeverageMatch> getTuvMatches(Long p_sourceTuvId,
            Long p_targetLocaleId, String p_subId, boolean isTmProcedence,
            String companyId, long... tmIds) throws RemoteException,
            LingManagerException;

    /**
     * Returns match type of Tuvs. With the given source Tuv ids and a target
     * locale id, the method search for the record in LEVERAGE_MATCH table and
     * determine the match type for each Tuv id.
     * 
     * @param p_targetLocaleId
     *            target locale id
     * @param p_levMatchThreshold
     *            The leverage match threshold defined in TM profile which is a
     *            job level value.
     * @return MatchTypeStatistics object
     */

    MatchTypeStatistics getMatchTypesForStatistics(Long p_sourcePageId,
            Long p_targetLocaleId, int p_levMatchThreshold)
            throws RemoteException, LingManagerException;

    /**
     * Returns true if a specified state indicates that the match is copied into
     * a target segment.
     * 
     * @param p_lingManagerMatchType
     *            match state defined in this class
     */
    boolean isMatchCopied(int p_lingManagerMatchType) throws RemoteException,
            LingManagerException;

    public Map getExactMatchesForDownLoadTmx(Long pageId, Long idAsLong);

    public List getLeverageMatchesForOfflineDownLoad(Long p_sourcePageId,
            Long p_targetLocaleId);

    public void updateProjectTmIndex(long tmId, int projectTmIndex,
            long tmProfileId);

    public boolean isIncludeMtMatches();

    public void setIncludeMtMatches(boolean isIncludeMtMatches);

    /**
     * Save matched segments to the database
     * 
     * @param p_connection
     *            DB connection
     * @param p_sourcePage
     *            SourcePage object
     * @param p_leverageDataCenter
     *            Repository of matches of a page
     */
    public void saveLeverageResults(Connection p_connection,
            SourcePage p_sourcePage, LeverageDataCenter p_leverageDataCenter)
            throws LingManagerException;

    public void saveLeverageResults(Connection p_connection,
            long p_sourcePageId,
            Map<Long, LeverageMatches> p_leverageMatchesMap,
            GlobalSightLocale p_targetLocale, LeverageOptions p_leverageOptions)
            throws LingManagerException;

    /**
     * Update the LEVERAGED_MATCH tables with the collection of LeverageMatch.
     * The Leverager assigns priorities per locale so that there is a per locale
     * ordering. The collection of LeverageMatch itself has no implied order.
     * 
     * @param p_leverageMatchList
     *            - a Collection of LeverageMatch.
     */
    public void saveLeveragedMatches(Collection p_leverageMatchList)
            throws RemoteException, LingManagerException;

    public void saveLeveragedMatches(Collection p_leverageMatchList,
            Connection p_connection) throws LingManagerException;

    /**
     * Get best match score for specified TUV.
     */
    public float getBestMatchScore(Connection p_connection,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId,
            long p_companyId);

    public int getMaxOrderNum(Connection p_connection,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId,
            long p_companyId);

}
