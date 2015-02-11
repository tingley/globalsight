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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.StoredProcCaller;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.log.GlobalSightCategory;

abstract class BasePostProcessor
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            BasePostProcessor.class);

    private static final String LEV_MATCH_SAVE_SP = "fuzzy_idx.ins_lev_match";

    private static final String BATCH_INSERT_UNIT_PROP_NAME =
        "leverager.batchInsertUnit";

    private static int BATCH_INSERT_UNIT = getBatchInsertUnit();

    private static int getBatchInsertUnit()
    {
        int unit;

        try
        {
            SystemConfiguration prop = SystemConfiguration.getInstance();
            unit = prop.getIntParameter(BATCH_INSERT_UNIT_PROP_NAME);
        }
        catch (Throwable e)
        {
            // It ususally shouldn't happen. Return default value.
            unit = 200;
        }

        return unit;
    }

    protected LeverageMatchesPerTuv m_lmPerTuv = new LeverageMatchesPerTuv();

    private LmCache m_lmToBeSaved = new LmCache(BATCH_INSERT_UNIT);
    private int m_matchType;
    private long m_sourcePageId;
    private Map m_originalTuvs;


    /**
     * Only constructor.
     *
     * @param p_matchType match type of hits
     * @param p_originalTuvs Map of original source Tuvs (Key: tuvId,
     * Value: BaseTmTuv)
     * @param p_sourcePageId source page id
     */
    protected BasePostProcessor(int p_matchType,
        Map p_originalTuvs, long p_sourcePageId)
    {
        m_matchType = p_matchType;
        m_originalTuvs = p_originalTuvs;
        m_sourcePageId = p_sourcePageId;
    }


    /**
     * return match type
     */
    public int getMatchType()
    {
        return m_matchType;
    }


    /**
     * Add CandidateMatch
     */
    public void add(CandidateMatch p_candidateMatch)
    {
        m_lmPerTuv.add(p_candidateMatch);
    }


    /**
     * Post process candidate matches.
     */
    public Collection postProcess(long p_tuvId)
        throws LingManagerException
    {
        Collection candidateMatchList = null;
        if (p_tuvId != -1)
        {
            candidateMatchList = postProcessInt(p_tuvId);
            m_lmPerTuv.clear();
        }
        else
        {
            candidateMatchList = new ArrayList();
        }

        return candidateMatchList;
    }


    abstract protected Collection postProcessInt(long p_tuvId)
        throws LingManagerException;


    public BaseTmTuv getOriginalTuv(long p_tuvId)
    {
        return (BaseTmTuv)m_originalTuvs.get(new Long(p_tuvId));
    }

    /**
     * Save a collection of CandidateMatch to the database.
    */
    public void saveHits()
        throws LingManagerException
    {
        Collection leverageMatches = m_lmToBeSaved.getLeverageMatches();
        // save all the leverage matches for this leverage group
        try
        {
            StoredProcCaller.insertLeverageMatches(m_sourcePageId,
                                                         leverageMatches);
        }
        catch (PersistenceException ex)
        {
            CATEGORY.error("problem with stored procedure: " +
                LEV_MATCH_SAVE_SP, ex);
            throw new LingManagerException(ex);
        }

        // clear the cache
        m_lmToBeSaved.clear();


    }

    public void saveHitsIfNecessary()
        throws LingManagerException
    {
        if (m_lmToBeSaved.size() >= BATCH_INSERT_UNIT)
        {
            saveHits();
        }
    }


    protected void storeHits(Collection p_hits)
    {
        m_lmToBeSaved.addAll(p_hits);
    }


    /**
     * Returns a Collection of CandidateMatches. The parameter is a
     * Collection of LeverageMatches. It is intended to be used to get
     * a Collection of CandidateMatches from a return value of
     * LeverageMatchesPerTuv#removeLgemMatches.
     */
    protected Collection getCandidateMatchList(
        Collection p_leverageMatchesList)
    {
        Collection result = new ArrayList(p_leverageMatchesList.size());

        Iterator it = p_leverageMatchesList.iterator();

        while (it.hasNext())
        {
            LeverageMatches lm = (LeverageMatches)it.next();

            // It's guaranteed that the list of CandidateMatch has
            // only one element
            List cmList = lm.getLeverageMatches();

            CandidateMatch cm = (CandidateMatch)cmList.get(0);

            result.add(cm);
        }

        return result;
    }


    private class LmCache
    {
        private int m_candidateMatchNum;
        private Collection m_leverageMatches;

        protected LmCache(int p_initialSize)
        {
            m_candidateMatchNum = 0;
            m_leverageMatches = new ArrayList(p_initialSize);
        }

        protected void addAll(Collection p_toSave)
        {
            Iterator it = p_toSave.iterator();
            while (it.hasNext())
            {
                LeverageMatches lm = (LeverageMatches)it.next();
                m_candidateMatchNum += lm.getLeverageMatches().size();
            }

            m_leverageMatches.addAll(p_toSave);
        }

        protected int size()
        {
            return m_candidateMatchNum;
        }


        protected void clear()
        {
            m_candidateMatchNum = 0;
            m_leverageMatches.clear();
        }

        protected Collection getLeverageMatches()
        {
            return m_leverageMatches;
        }

        protected Iterator iterator()
        {
            return m_leverageMatches.iterator();
        }
    }
}
