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

package com.globalsight.terminology.termleverager;

//
// globalsight imports
//
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.page.SourcePage;

//
// Java imports
//
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import java.rmi.RemoteException;

/**
 * The TermLeverageManager interface is intended to provide management
 * of Term Leveraging.
 */
public interface TermLeverageManager
{
    // The name bound to the remote object.
    String SERVICE_NAME = "TermLeverageManager";

    /**
     * Leverages terms and persists them in TERM_LEVERAGE_MATCH table.
     *
     * @param p_tuvs a collection of source TUVs to be leveraged.
     * @param p_options leverage options
     * @param p_save flag whether the matches should be persisted in
     * the database
     *
     * @return a TermLeveragerResult object representing a collection
     * of TermLeverageMatch objects (grouped by source tuv id).
     */
    TermLeverageResult leverageTerms(Collection p_tuvs,
        TermLeverageOptions p_options)
        throws GeneralException, RemoteException;

    /**
     * Leverages terms and persists them in TERM_LEVERAGE_MATCH table.
     *
     * @param p_tuvs a collection of source TUVs to be leveraged.
     * @param p_options leverage options
     * @param p_companyId company id
     *
     * @return a TermLeveragerResult object representing a collection
     * of TermLeverageMatch objects (grouped by source tuv id).
     */
    TermLeverageResult leverageTerms(Collection p_tuvs,
        TermLeverageOptions p_options, String p_companyId)
        throws GeneralException, RemoteException;

    /**
     * Retrieves all the TermLeverageMatchResult for a given SourcePage.
     *
     * @param p_sourcePage SourcePage object
     * @param p_targetPageLocale the locale of the target page to which
     * terms are leveraged
     * @return TermLeverageMatchResultSet
     */
    TermLeverageMatchResultSet getTermMatchesForPage(SourcePage p_sourcePage,
        GlobalSightLocale p_targetPageLocale)
        throws GeneralException, RemoteException;


    /**
     * Retrieves the TermLeverageMatchResults for a single source TuvId.
     *
     * @param p_sourceTuvId the source TuvId
     * @param p_subId the id of a subflow within the source Tuv or 0
     * if the search is for the segment itself.
     * @param p_targetPageLocale the target locale for which to
     * retrieve matches.
     *
     * @return a list of TermLeverageMatchResult. The list is empty
     * when there is no match, but it is never null.
     */
    ArrayList<TermLeverageMatchResult> getTermMatchesForSegment(
            long p_srcTuvId, long p_subId, GlobalSightLocale p_targetPageLocale)
            throws GeneralException, RemoteException;
    
    /**
     * Retrieves Map<TuvId, Set<TermLeverageMatch>> for given SourcePages.
     * 
     * @param p_sourcePages
     *            source page set
     * @param p_targetPageLocale
     *            target page locale
     */
    public Map<Long, Set<TermLeverageMatch>> getTermMatchesForPages(
            Collection<SourcePage> p_sourcePages,
            GlobalSightLocale p_targetPageLocale) throws GeneralException,
            RemoteException;
}
