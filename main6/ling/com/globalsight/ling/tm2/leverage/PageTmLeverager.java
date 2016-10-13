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
package com.globalsight.ling.tm2.leverage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.persistence.PageTmPersistence;
import com.globalsight.util.GlobalSightLocale;

/**
 * PageTmLeverager is responsible for leveraging Page Tm segments
 */

class PageTmLeverager
{
    /**
     * leverage a page from Page Tm
     * 
     * @param p_sourcePage
     *            source page
     * @param p_leverageDataCenter
     *            LeverageDataCenter that contains segments that are leveraged.
     * @return LeverageMatchResults that contains leverage results
     */
    public LeverageMatchResults leverage(Connection p_connection,
            SourcePage p_sourcePage, LeverageDataCenter p_leverageDataCenter)
            throws Exception
    {
        LeverageMatchResults levMatchResults = new LeverageMatchResults();
        GlobalSightLocale sourceLocale = p_leverageDataCenter.getSourceLocale();

        PageTmPersistence ptPersistence = new PageTmPersistence(p_connection);

        // check if the page has been imported before
        long tmId = ptPersistence.getPageTmId(p_sourcePage.getExternalPageId(),
                sourceLocale);
        long jobId = p_sourcePage.getJobId();
        if (tmId != 0)
        {
            // classify original source segments to translatable
            // and localizable
            Collection trSegments = new ArrayList();
            Collection loSegments = new ArrayList();
            Iterator itOriginalSegment = p_leverageDataCenter
                    .getOriginalWholeSegments(jobId).iterator();
            while (itOriginalSegment.hasNext())
            {
                BaseTmTuv originalSegment = (BaseTmTuv) itOriginalSegment
                        .next();
                if (originalSegment.isTranslatable())
                {
                    trSegments.add(originalSegment);
                }
                else
                {
                    loSegments.add(originalSegment);
                }
            }

            LeverageOptions leverageOptions = p_leverageDataCenter
                    .getLeverageOptions();

            // leverage translatable
            levMatchResults = leverageTranslatable(ptPersistence, tmId,
                    sourceLocale, trSegments, leverageOptions);

            // leverage localizable if the user choose to do so
            if (leverageOptions.isLeveragingLocalizables())
            {
                LeverageMatchResults loLevMatchResults = leverageLocalizable(
                        ptPersistence, tmId, sourceLocale, loSegments,
                        leverageOptions);
                levMatchResults.merge(loLevMatchResults);
            }
        }

        // To avoid page tm impaction, return null always here!!!
        return null;
//        return levMatchResults;
    }

    /**
     * Leverage translatable segments from Page Tm
     * 
     * @param p_ptPersistence
     *            PageTmPersistence
     * @param p_tmId
     *            Tm id from which segments are leveraged
     * @param p_sourceLocale
     *            source locale
     * @param p_trSegments
     *            Collection of original segments (BaseTmTuv)
     * @return LeverageMatchResults object
     */
    private LeverageMatchResults leverageTranslatable(
            PageTmPersistence p_ptPersistence, long p_tmId,
            GlobalSightLocale p_sourceLocale, Collection p_trSegments,
            LeverageOptions p_leverageOptions) throws Exception
    {
        return leverageSegments(p_ptPersistence, p_tmId, p_sourceLocale,
                p_trSegments, true, p_leverageOptions);
    }

    /**
     * Leverage localizable segments from Page Tm
     * 
     * @param p_ptPersistence
     *            PageTmPersistence
     * @param p_tmId
     *            Tm id from which segments are leveraged
     * @param p_sourceLocale
     *            source locale
     * @param p_loSegments
     *            Collection of original segments (BaseTmTuv)
     * @return LeverageMatchResults object
     */
    private LeverageMatchResults leverageLocalizable(
            PageTmPersistence p_ptPersistence, long p_tmId,
            GlobalSightLocale p_sourceLocale, Collection p_loSegments,
            LeverageOptions p_leverageOptions) throws Exception
    {
        return leverageSegments(p_ptPersistence, p_tmId, p_sourceLocale,
                p_loSegments, false, p_leverageOptions);
    }

    /**
     * Leverage segments from Page Tm
     * 
     * @param p_ptPersistence
     *            PageTmPersistence
     * @param p_tmId
     *            Tm id from which segments are leveraged
     * @param p_sourceLocale
     *            source locale
     * @param p_segments
     *            Collection of original segments (BaseTmTuv)
     * @param p_isTranslatable
     *            indicates if leverage translatable segments
     * @return LeverageMatchResults object
     */
    private LeverageMatchResults leverageSegments(
            PageTmPersistence p_ptPersistence, long p_tmId,
            GlobalSightLocale p_sourceLocale, Collection p_segments,
            boolean p_isTranslatable, LeverageOptions p_leverageOptions)
            throws Exception
    {
        LeverageMatchResults levResults = new LeverageMatchResults();
        Iterator itLevMatches = p_ptPersistence
                .leverage(p_tmId, p_sourceLocale, p_segments, p_isTranslatable,
                        p_leverageOptions);
        while (itLevMatches.hasNext())
        {
            LeverageMatches levMatches = (LeverageMatches) itLevMatches.next();
            levResults.add(levMatches);
        }

        return levResults;
    }

}
