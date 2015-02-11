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
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm.LgemHitsResult;

import com.globalsight.util.GlobalSightLocale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

class LocalizablePostProcessor
    extends BasePostProcessor
{
    private LeveragingLocales m_leveragingLocales;
    private LgemHitsResult m_hitResult;

    /**
     * Only constructor.
     *
     * @param p_hitResult LgemHitsResult
     * @param p_originalTuvs Map of original source Tuvs
     *                       (Key: tuvId, Value: TuvLing)
     * @param p_sourcePageId source page id
     * @param p_leveragingLocales LeveragingLocales
     */
    public LocalizablePostProcessor(LgemHitsResult p_hitResult,
        Map p_originalTuvs, long p_sourcePageId,
        LeveragingLocales p_leveragingLocales)
    {
        super(LeverageMatchType.EXACT_MATCH,
            p_originalTuvs, p_sourcePageId);
        m_hitResult = p_hitResult;
        m_leveragingLocales = p_leveragingLocales;
    }


    protected Collection postProcessInt(long p_tuvId)
        throws LingManagerException
    {
        Long tuvIdAsLong = new Long(p_tuvId);

        m_lmPerTuv.regroupLeveragingLocales(m_leveragingLocales);

        // remove unnecessary matches (there is already LGEM)
        if (m_hitResult != null)
        {
            Collection tobeRemoved
                = new ArrayList(m_lmPerTuv.getLocales().size());

            Iterator localeIt = m_lmPerTuv.getLocales().iterator();
            while (localeIt.hasNext())
            {
                GlobalSightLocale locale = (GlobalSightLocale)localeIt.next();
                if (m_hitResult.hasMatch(tuvIdAsLong, locale))
                {
                    tobeRemoved.add(locale);
                }
            }
            // remove all unnecessary matches
            // : Do not remove the matches while iterating localeIt or
            // : you'll get ConcurrentModificationException
            m_lmPerTuv.remove(tobeRemoved);
        }

        m_lmPerTuv.removeFalseHits(getOriginalTuv(p_tuvId));

        // combine matches with duplicated lgem matches
        if (m_hitResult != null)
        {
            LeverageMatchesPerTuv dupLgems =
                m_hitResult.getDuplicatedLgemMatch(tuvIdAsLong);

            if (dupLgems != null)
            {
                m_lmPerTuv.merge(dupLgems);
            }
        }

        m_lmPerTuv.removeDuplicates();
        m_lmPerTuv.demoteExactMatches();
        m_lmPerTuv.assignLeverageMatchTypes();
        // Optional step - should be guided by global Leverage Options.
        m_lmPerTuv.clearFuzziesIfExact();
        m_lmPerTuv.assignOrderNum();

        // store all LeverageMatches in m_lmPerTuv
        storeHits(m_lmPerTuv.getLeverageMatches());

        return m_lmPerTuv.getExactMatches();
    }

}
