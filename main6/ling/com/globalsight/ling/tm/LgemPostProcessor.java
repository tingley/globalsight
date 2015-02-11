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

import com.globalsight.ling.tm.BasePostProcessor;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.LgemHitsResult;
import com.globalsight.ling.tm2.BaseTmTuv;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


class LgemPostProcessor
    extends BasePostProcessor
{
    private LgemHitsResult m_hitResult;

    /**
     * Only constructor.
     *
     * @param p_originalTuvs Map of original source Tuvs (Key: tuvId,
     * Value: BaseTmTuv)
     * @param p_sourcePageId source page id
     * @param p_tuvIds a collection of all TUV ids (Long) to search
     * for matches.
     * @param p_locales a collection of all target locales
     * (GlobalSightLocale).
     */
    public LgemPostProcessor(Map p_originalTuvs, long p_sourcePageId,
        Collection p_tuvIds, Collection p_locales)
    {
        super(LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH,
            p_originalTuvs, p_sourcePageId);

        m_hitResult = new LgemHitsResult(p_tuvIds, p_locales);
    }


    protected Collection postProcessInt(long p_tuvId)
        throws LingManagerException
    {
        m_lmPerTuv.removeFalseHits(getOriginalTuv(p_tuvId));
        m_lmPerTuv.removeDuplicates();
        //m_lmPerTuv.demoteIncompleteMatches();

        // get Lgem matches for the TUV
        Collection leverageMatchesList
            = m_lmPerTuv.removeLgemMatches(m_hitResult);

        if (m_lmPerTuv.hasMatches())
        {
            m_hitResult.setDuplicatedLgemMatch(new Long(p_tuvId), m_lmPerTuv);
        }

        storeHits(leverageMatchesList);

        // get CandidateMatch list of the above Lgem matches
        return getCandidateMatchList(leverageMatchesList);
    }


    public LgemHitsResult getLgemHitsResult()
    {
        return m_hitResult;
    }

}
