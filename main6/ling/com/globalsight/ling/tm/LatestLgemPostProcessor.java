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


import java.util.Map;
import java.util.HashMap;
import java.util.Collection;


class LatestLgemPostProcessor
    extends BasePostProcessor
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            LatestLgemPostProcessor.class);

    // Reuse the LgemHitsResult data structure.
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
    public LatestLgemPostProcessor(Map p_originalTuvs, long p_sourcePageId,
        Collection p_tuvIds, Collection p_locales)
    {
        super(LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH,
            p_originalTuvs, p_sourcePageId);

        m_hitResult = new LgemHitsResult(p_tuvIds, p_locales);
    }


    protected Collection postProcessInt(long p_tuvId)
        throws LingManagerException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("original matches = " + m_lmPerTuv.toString());
        }

        m_lmPerTuv.removeFalseHits(getOriginalTuv(p_tuvId));
        m_lmPerTuv.removeOlderDuplicates();
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

        m_lmPerTuv.assignOrderNum();

        // get Latest Leverage Group matches for the TUV
        Collection latestMatchesList =
            m_lmPerTuv.removeLgemMatches(m_hitResult);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("removeLatestExact = " + m_lmPerTuv.toString());
        }

        if (m_lmPerTuv.hasMatches())
        {
            m_hitResult.setDuplicatedLgemMatch(new Long(p_tuvId), m_lmPerTuv);
        }

        storeHits(latestMatchesList);

        // get CandidateMatch list of the above Lgem matches
        return getCandidateMatchList(latestMatchesList);
    }


    public LgemHitsResult getLgemHitsResult()
    {
        return m_hitResult;
    }
}
