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

import org.apache.log4j.Logger;

import com.globalsight.ling.tm.BasePostProcessor;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.LgemHitsResult;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


class LatestExactPostProcessor
    extends BasePostProcessor
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            LatestExactPostProcessor.class);

    private LeveragingLocales m_leveragingLocales;
    private LgemHitsResult m_hitResult;

    /**
     * Only constructor.
     *
     * @param p_originalTuvs Map of original source Tuvs (Key: tuvId,
     * Value: TuvLing)
     * @param p_sourcePageId source page id
     * @param p_tuvIds a collection of all TUV ids (Long) to search
     * for matches.
     * @param p_locales a collection of all target locales
     * (GlobalSightLocale).
     */
    public LatestExactPostProcessor(Map p_originalTuvs, long p_sourcePageId,
        Collection p_tuvIds, LeveragingLocales p_leveragingLocales)
    {
        super(LeverageMatchType.FUZZY_MATCH, p_originalTuvs, p_sourcePageId);

        m_leveragingLocales = p_leveragingLocales;

        m_hitResult = new LgemHitsResult(p_tuvIds,
            p_leveragingLocales.getAllTargetLocales());
    }


    protected Collection postProcessInt(long p_tuvId)
        throws LingManagerException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("original matches = " + m_lmPerTuv.toString());
        }

        m_lmPerTuv.regroupLeveragingLocales(m_leveragingLocales);
        m_lmPerTuv.change100PercentFuzzyToExact(getOriginalTuv(p_tuvId));

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("change 100% fuzzy to exact matches = " +
                m_lmPerTuv.toString());
        }

        m_lmPerTuv.removeOlderDuplicates();

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("remove older duplicates = " +
                m_lmPerTuv.toString());
        }

        m_lmPerTuv.demoteAllButLatestExactMatches();

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("demote all but latest exact match = " +
                m_lmPerTuv.toString());
        }

        m_lmPerTuv.assignLeverageMatchTypes();

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("assignTypes = " + m_lmPerTuv.toString());
        }

        m_lmPerTuv.demoteAllButLatestExactMatches();

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("demoteAllButLatest = " + m_lmPerTuv.toString());
        }

        // Optional step - keep magic number of 5 fuzzies around
        m_lmPerTuv.clearFuzziesIfLatestExact(5);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("clearFuzzies(5) = " + m_lmPerTuv.toString());
        }

        // get Latest matches for the TUV
        Collection latestMatchesList =
            m_lmPerTuv.removeLatestExactMatches(m_hitResult);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("removeLatestExact = " + m_lmPerTuv.toString());
        }

        m_lmPerTuv.assignOrderNum();

        /* Does that do anything for latest exact leveraging?
        if (m_lmPerTuv.hasMatches())
        {
            m_hitResult.setDuplicatedLgemMatch(new Long(p_tuvId), m_lmPerTuv);
        }
        */

        storeHits(m_lmPerTuv.getLeverageMatches());

        // get CandidateMatch list of the above Lgem matches
        return getCandidateMatchList(latestMatchesList);
    }


    public LgemHitsResult getLgemHitsResult()
    {
        return m_hitResult;
    }
}
