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

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.inprogresstm.DynamicLeverageResults;
import com.globalsight.ling.inprogresstm.DynamicLeveragedSegment;
import com.globalsight.ling.inprogresstm.persistence.TmPersistence;
import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.util.GlobalSightLocale;

/**
 * Leverager is responsible for leveraging segments
 */
public class Leverager
{
    private static final Logger c_logger = Logger.getLogger(Leverager.class
            .getName());

    /**
     * Every "Leverager" object shares one DB connection.
     */
    private Connection connection = null;

    public Leverager(Connection p_connection)
    {
        connection = p_connection;
    }

    /**
     * Leverage a translatable segment from the in-progress TM.
     * 
     * @param p_sourceSegment
     *            -- source segment to be leveraged.
     * @param p_targetLocale
     *            -- target locale
     * @param p_leverageOptions
     *            -- leverage options
     * @param p_jobId
     *            -- current job ID
     * @param p_jobIdsToLevFrom
     *            -- job IDs current segment can leverage from these jobs TMs.
     * @param p_tmIds
     *            -- TM IDs current segment can leverage from these jobs TMs.
     * @return DynamicLeverageResults that contains all matches.
     */
    public DynamicLeverageResults leverageTranslatable(
            BaseTmTuv p_sourceSegment, GlobalSightLocale p_targetLocale,
            LeverageOptions p_leverageOptions, long p_jobId,
            Set<Long> p_jobIdsToLevFrom, Set<Long> p_tmIds) throws Exception
    {
        LeverageMatches leverageMatches = leverageTranslatableSegment(
                p_sourceSegment, p_targetLocale, p_leverageOptions,
                p_jobIdsToLevFrom, p_tmIds);

        // create DynamicLeverageResults
        return createDynamicLeverageResults(leverageMatches, p_sourceSegment,
                p_targetLocale, p_jobId);
    }

    /**
     * A common logic to leverage translatable segment from in-progress TM.
     * 
     * @return LeverageMatches
     */
    public LeverageMatches leverageTranslatableSegment(
            BaseTmTuv p_sourceSegment, GlobalSightLocale p_targetLocale,
            LeverageOptions p_leverageOptions, Set<Long> p_jobIds,
            Set<Long> tmIds) throws Exception
    {
        // find matches
        FuzzyMatcher fuzzyMatcher = new FuzzyMatcher(p_sourceSegment,
                connection);
        Collection matches = fuzzyMatcher.leverage(p_jobIds, tmIds,
                p_targetLocale, p_leverageOptions.getMatchThreshold(),
                p_leverageOptions.getNumberOfMatchesReturned());

        // Create LeverageMatches object
        LeverageMatches leverageMatches = new LeverageMatches(p_sourceSegment,
                p_leverageOptions);
        for (Iterator it = matches.iterator(); it.hasNext();)
        {
            LeveragedInProgressTu tu = (LeveragedInProgressTu) it.next();
            leverageMatches.add(tu);
        }

        // apply leverage option.
        leverageMatches.applySegmentTmOptions();

        return leverageMatches;
    }

    /**
     * Leverage a localizable segment from the in-progress TM.
     * 
     * @param p_sourceSegment
     *            -- source segment to be leveraged. The exact match key must be
     *            calculated beforehand.
     * @param p_sourceLocale
     *            -- source locale
     * @param p_targetLocale
     *            -- target locale
     * @param p_leverageOptions
     *            -- leverage options
     * @param p_jobId
     *            -- current job ID
     * @param p_jobIdsToLevFrom
     *            -- job IDs current segment can leverage from these jobs TMs.
     * @param p_tmIds
     *            -- TM IDs current segment can leverage from these jobs TMs.
     * @return DynamicLeverageResults that contains all matches.
     */
    public DynamicLeverageResults leverageLocalizable(
            BaseTmTuv p_sourceSegment, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale,
            LeverageOptions p_leverageOptions, long p_jobId,
            Set<Long> p_jobIdsToLevFrom, Set<Long> p_tmIds) throws Exception
    {
        LeverageMatches leverageMatches = leverageLocalizableSegment(
                p_sourceSegment, p_sourceLocale, p_targetLocale,
                p_leverageOptions, p_jobIdsToLevFrom, p_tmIds);

        // create DynamicLeverageResults
        return createDynamicLeverageResults(leverageMatches, p_sourceSegment,
                p_targetLocale, p_jobId);
    }

    /**
     * A common logic to leverage localizable segment from in-progress TM.
     * 
     * @return LeverageMatches
     */
    public LeverageMatches leverageLocalizableSegment(
            BaseTmTuv p_sourceSegment, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale,
            LeverageOptions p_leverageOptions, Set<Long> jobIds, Set<Long> tmIds)
            throws Exception
    {
        long exactMatchKey = p_sourceSegment.getExactMatchKey();
        // find matches
        Collection matches = null;

        TmPersistence tmPersistence = new TmPersistence(connection);
        matches = tmPersistence.leverageLocalizable(p_sourceLocale,
                p_targetLocale, exactMatchKey, jobIds, tmIds);

        // set score and state. Apply leverage option.
        LeverageMatches leverageMatches = new LeverageMatches(p_sourceSegment,
                p_leverageOptions);
        for (Iterator it = matches.iterator(); it.hasNext();)
        {
            LeveragedInProgressTu tu = (LeveragedInProgressTu) it.next();

            // compare the text to weed out false matches (exact match
            // key and text are not 1 to 1 relations)
            BaseTmTuv srcTuv = tu.getFirstTuv(p_sourceLocale);
            if (srcTuv.getExactMatchFormat().equals(
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

        return leverageMatches;
    }

    /**
     * Create DynamicLeverageResults from LeverageMatches object.
     */
    private DynamicLeverageResults createDynamicLeverageResults(
            LeverageMatches p_leverageMatches, BaseTmTuv p_sourceSegment,
            GlobalSightLocale p_targetLocale, long p_currentJobId)
    {
        GlobalSightLocale sourceLocale = p_sourceSegment.getLocale();
        // LeverageOptions options = p_leverageMatches.getLeverageOptions();
        DynamicLeverageResults dynamicLeverageResults = new DynamicLeverageResults(
                p_sourceSegment.getSegment(), sourceLocale, p_targetLocale,
                p_sourceSegment.isTranslatable());
        // populate DynamicLeverageResults
        for (Iterator it = p_leverageMatches.matchIterator(p_targetLocale,
                p_currentJobId); it.hasNext();)
        {
            // These results came from the IP TM. This means that their TMID
            // is not a TMID but rather a JOB_ID. Therefore we need a way
            // to distinguish them so we don't try to pass their bogus
            LeveragedInProgressTuv trgTuv = (LeveragedInProgressTuv) it.next();
            BaseTmTuv srcTuv = trgTuv.getTu().getFirstTuv(sourceLocale);
            // here the "tmId" should be the jobId
            long leveragedJobId = trgTuv.getTu().getTmId();
            int matchCategory = (p_currentJobId == leveragedJobId ? DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_SAME_JOB
                    : DynamicLeveragedSegment.FROM_IN_PROGRESS_TM_OTHER_JOB);

            long jobDataTuId = trgTuv.getJobDataTuId();
            GlobalSightLocale locale = trgTuv.getLocale();
            Tuv tuv = null;
            Job job = null;
            try
            {
                job = ServerProxy.getJobHandler().getJobById(leveragedJobId);
                tuv = ServerProxy.getTuvManager()
                        .getTuForSegmentEditor(jobDataTuId, leveragedJobId)
                        .getTuv(locale.getId(), leveragedJobId);
            }
            catch (Exception e)
            {
                c_logger.error(e.getMessage(), e);
            }

            TuvBasicInfo matchedTuvBasicInfo = new TuvBasicInfo(
                    trgTuv.getSegment(), null, String.valueOf(trgTuv
                            .getExactMatchKey()), locale, tuv.getCreatedDate(),
                    tuv.getCreatedUser(), tuv.getLastModified(),
                    tuv.getLastModifiedUser(), null, tuv.getSid());

            DynamicLeveragedSegment leveragedSegment = new DynamicLeveragedSegment(
                    srcTuv.getSegment(), trgTuv.getSegment(), sourceLocale,
                    trgTuv.getLocale(), trgTuv.getMatchState(),
                    trgTuv.getScore(), matchCategory, leveragedJobId,
                    trgTuv.getId());
            // For "in-progress" leverage,the projectTmIndex should be "-7".
            int projectTmIndex = com.globalsight.ling.tm2.leverage.Leverager.IN_PROGRESS_TM_PRIORITY;
            leveragedSegment.setTmIndex(projectTmIndex);
            leveragedSegment.setMatchedTuvBasicInfo(matchedTuvBasicInfo);
            leveragedSegment.setMatchedTuvJobName(job.getJobName());
            leveragedSegment.setSid(trgTuv.getSid());
            dynamicLeverageResults.add(leveragedSegment);
        }

        return dynamicLeverageResults;
    }
}
