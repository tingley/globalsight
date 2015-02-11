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
package com.globalsight.ling.inprogresstm.leverage;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.inprogresstm.persistence.TmPersistence;
import com.globalsight.ling.inprogresstm.DynamicLeverageResults;
import com.globalsight.ling.inprogresstm.DynamicLeveragedSegment;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.sql.Connection;



/**
 * Leverager is responsible for leveraging segments
 */

public class Leverager
{
    private static final GlobalSightCategory c_logger =
        (GlobalSightCategory) GlobalSightCategory.getLogger(
            Leverager.class.getName());

    /**
     * Leverage a translatable segment from the in-progress TM.
     *
     * @param p_sourceSegment source segment to be leveraged.
     * @param p_targetLocale target locale
     * @param p_leverageOptions leverage options
     * @param p_sourcePage source page
     * @return DynamicLeverageResults that contains all matches.
     */
    public DynamicLeverageResults leverageTranslatable(
        BaseTmTuv p_sourceSegment, GlobalSightLocale p_targetLocale,
        LeverageOptions p_leverageOptions, SourcePage p_sourcePage)
        throws Exception
    {
        long jobId = getJobId(p_sourcePage);
        Set tmIds = getTmIdsToLeverageFrom(p_leverageOptions);

        // find matches
        FuzzyMatcher fuzzyMatcher = new FuzzyMatcher(p_sourceSegment);
        Collection matches = fuzzyMatcher.leverage(
            jobId, tmIds, p_targetLocale,
            p_leverageOptions.getMatchThreshold(),
            p_leverageOptions.getNumberOfMatchesReturned());
        
        // Create LeverageMatches object
        LeverageMatches leverageMatches
            = new LeverageMatches(p_sourceSegment, p_leverageOptions);
        for(Iterator it = matches.iterator(); it.hasNext();)
        {
            LeveragedInProgressTu tu = (LeveragedInProgressTu)it.next();
            leverageMatches.add(tu);
        }

        // apply leverage option.
        leverageMatches.applySegmentTmOptions();
            
        // create DynamicLeverageResults
        return createDynamicLeverageResults(leverageMatches, p_sourceSegment,
            p_targetLocale, jobId);
    }



    /**
     * Leverage a localizable segment from the in-progress TM.
     *
     * @param p_sourceSegment source segment to be leveraged. The
     * exact match key must be calculated beforehand.
     * @param p_targetLocale target locale
     * @param p_leverageOptions leverage options
     * @param p_sourcePage source page
     * @return DynamicLeverageResults that contains all matches.
     */
    public DynamicLeverageResults leverageLocalizable(
        BaseTmTuv p_sourceSegment, GlobalSightLocale p_targetLocale,
        LeverageOptions p_leverageOptions, SourcePage p_sourcePage)
        throws Exception
    {
        GlobalSightLocale sourceLocale
            = p_sourcePage.getGlobalSightLocale();
        long exactMatchKey = p_sourceSegment.getExactMatchKey();
        long jobId = getJobId(p_sourcePage);
        Set tmIds = getTmIdsToLeverageFrom(p_leverageOptions);

        // find matches
        Collection matches;
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            TmPersistence tmPersistence = new TmPersistence(conn);
            matches = tmPersistence.leverageLocalizable(
                sourceLocale, p_targetLocale, exactMatchKey, jobId, tmIds);
        }
        finally
        {
            if(conn != null)
            {
                DbUtil.returnConnection(conn);
            }
        }
        
        // set score and state. Apply leverage option.
        LeverageMatches leverageMatches
            = new LeverageMatches(p_sourceSegment, p_leverageOptions);
        for(Iterator it = matches.iterator(); it.hasNext();)
        {
            LeveragedInProgressTu tu = (LeveragedInProgressTu)it.next();

            // compare the text to weed out false matches (exact match
            // key and text are not 1 to 1 relations)
            BaseTmTuv srcTuv = tu.getFirstTuv(sourceLocale);
            if(srcTuv.getExactMatchFormat().equals(
                   p_sourceSegment.getExactMatchFormat()))
            {
                tu.setScore(100);
                tu.setMatchState(MatchState.IN_PROGRESS_TM_EXACT_MATCH);
                leverageMatches.add(tu);
            }
        }

        // apply leverage option. Essentially, only type difference
        // penalty is applied for localizable segments
        leverageMatches.applySegmentTmOptions();
            
        // create DynamicLeverageResults
        return createDynamicLeverageResults(leverageMatches, p_sourceSegment,
            p_targetLocale, jobId);
    }


    private long getJobId(SourcePage p_sorucePage)
    {
        RequestImpl req = (RequestImpl)p_sorucePage.getRequest();
        return req.getJob().getId();
    }
    

    private Set getTmIdsToLeverageFrom(LeverageOptions p_leverageOptions)
        throws Exception
    {
        HashSet results = new HashSet();

        // leverage from jobs that writes to population TM
        if(p_leverageOptions.dynamicLeveragesFromPopulationTm())
        {
            long populationTmId = p_leverageOptions.getSaveTmId();
            results.add(new Long(populationTmId));
        }
        
        // leverage from jobs that writes to reference TM
        if(p_leverageOptions.dynamicLeveragesFromReferenceTm())
        {
            Collection tmIds = p_leverageOptions.getTmsToLeverageFrom();
            results.addAll(tmIds);
        }
        
        return results;
    }
    

    // create DynamicLeverageResults from LeverageMatches object
    private DynamicLeverageResults createDynamicLeverageResults(
        LeverageMatches p_leverageMatches, BaseTmTuv p_sourceSegment,
        GlobalSightLocale p_targetLocale, long p_currentJobId)
    {
        GlobalSightLocale sourceLocale = p_sourceSegment.getLocale();
        LeverageOptions options = p_leverageMatches.getLeverageOptions();
        DynamicLeverageResults dynamicLeverageResults
            = new DynamicLeverageResults(p_sourceSegment.getSegment(),
                sourceLocale, p_targetLocale,
                p_sourceSegment.isTranslatable());
        // populate DynamicLeverageResults
        for(Iterator it = p_leverageMatches.matchIterator(p_targetLocale);
            it.hasNext();)
        {
            // These results came from the IP TM.  This means that their TMID 
            // is not a TMID but rather a JOB_ID.  Therefore we need a way 
            // to distinguish them so we don't try to pass their bogus 
            // 
            LeveragedInProgressTuv trgTuv = (LeveragedInProgressTuv)it.next();
            BaseTmTuv srcTuv = trgTuv.getTu().getFirstTuv(sourceLocale);
            long leveragedJobId = trgTuv.getTu().getTmId();
            int matchCategory = (p_currentJobId == leveragedJobId
                ? DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_SAME_JOB
                : DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_OTHER_JOB);
            long tmId = trgTuv.getTu().getTmId();
            int projectTmIndex = com.globalsight.ling.tm2.leverage.Leverager.getProjectTmIndex(options, tmId);

            DynamicLeveragedSegment leveragedSegment
                = new DynamicLeveragedSegment(
                    srcTuv.getSegment(), trgTuv.getSegment(), sourceLocale,
                    trgTuv.getLocale(), trgTuv.getMatchState(),
                    trgTuv.getScore(), matchCategory, tmId, trgTuv.getId());
            leveragedSegment.setTmIndex(projectTmIndex);
            dynamicLeverageResults.add(leveragedSegment);
        }

        return dynamicLeverageResults;
    }
    
}
