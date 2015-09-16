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
package com.globalsight.everest.webapp.pagehandler.tasks;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.inprogresstm.InProgressTmManager;
import com.globalsight.ling.inprogresstm.leverage.Leverager;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.SegmentUtil2;

/**
 * UpdateLeverageHelper includes some methods for "update leverage" feature.
 * 
 * @author YorkJin
 * @since 8.2
 */
public class UpdateLeverageHelper
{
    private static final Logger logger = Logger
            .getLogger(UpdateLeverageHelper.class);

    // Managers
    private static TuvManager tuvManager = ServerProxy.getTuvManager();
    private static LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
            .getLeverageMatchLingManager();

    /**
     * Get untranslated source segments. The untranslated segments must 1)be in
     * "NOT_LOCALIZED" state,2)source and target segment contents are the same,
     * 3)and best leverage match's score should be less than 100.
     */
    public static Collection<Tuv> getUntranslatedTuvs(TargetPage p_tp,
            long p_sourceLocaleId)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Begin getUntranslatedTuvs() for target page "
                    + p_tp.getId() + ", source locale Id " + p_sourceLocaleId);            
        }
        if (p_tp == null || p_sourceLocaleId < 1)
        {
            return new ArrayList<Tuv>();
        }

        Collection<Tuv> untranslatedSrcTuvs = new ArrayList<Tuv>();

        try
        {
            GlobalSightLocale targetLocale = p_tp.getGlobalSightLocale();
            Map<Long, Set<LeverageMatch>> myLMsMap = leverageMatchLingManager
                    .getExactMatchesForDownLoadTmx(p_tp.getSourcePage()
                            .getIdAsLong(), targetLocale.getIdAsLong());

            // Touch to load all TUs and source TUVs for performance.
            tuvManager.getTus(p_tp.getSourcePage().getIdAsLong());
            tuvManager.getSourceTuvsForStatistics(p_tp.getSourcePage());
            Collection<Tuv> targetTuvs = tuvManager
                    .getTargetTuvsForStatistics(p_tp);

            long jobId = p_tp.getSourcePage().getJobId();
            for (Tuv trgTuv : targetTuvs)
            {
                Tuv srcTuv = trgTuv.getTu(jobId).getTuv(p_sourceLocaleId, jobId);

                boolean hasExactMatch = false;
                Set<LeverageMatch> myLMs = myLMsMap.get(srcTuv.getIdAsLong());
                if (myLMs != null && myLMs.size() > 0)
                {
                    for (LeverageMatch myLM : myLMs)
                    {
                        if ("0".equals(myLM.getSubId())
                                && myLM.getScoreNum() == 100.0)
                        {
                            hasExactMatch = true;
                            break;
                        }
                    }
                }

                if (trgTuv.getState().getValue() == TuvState.NOT_LOCALIZED
                        .getValue()
                        && srcTuv.getGxml().equals(trgTuv.getGxml())
                        && !hasExactMatch)
                {
                    untranslatedSrcTuvs.add(srcTuv);
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("End getUntranslatedTuvs() for target page "
                        + p_tp.getId() + ", source locale Id "
                        + p_sourceLocaleId);                
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get untranslated Tuvs for target page : "
                    + p_tp.getId(), e);
            throw new EnvoyServletException(e);
        }

        return untranslatedSrcTuvs;
    }

    /**
     * Update from specified jobs' in-progress translations.
     */
    public static Map<Long, LeverageMatches> getInProgressTranslationFromJobs(
            Workflow p_workflow, List<BaseTmTuv> p_sourceTuvs,
            String[] p_selectedJobIds)
    {
        logger.debug("Begin getInProgressTranslationFromJobs()...");
        GlobalSightLocale targetLocale = p_workflow.getTargetLocale();

        // Parameter 3:"p_leverageOptions"
        TranslationMemoryProfile tmProfile = getTMProfile(p_workflow);
        Set<GlobalSightLocale> trgLocales = new HashSet<GlobalSightLocale>();
        trgLocales.add(targetLocale);
        LeveragingLocales levLocales = new LeveragingLocales();
        levLocales.setLeveragingLocale(targetLocale, trgLocales);
        LeverageOptions levOptions = new LeverageOptions(tmProfile, levLocales);

        // Parameter 4:"p_jobIdsToUpdateFrom"
        Set<Long> jobIdsToUpdateFrom = new HashSet<Long>();
        // Current job's in-progress TM data should always be leveraged.
        // jobIdsToUpdateFrom.add(jobId);
        // Selected jobs
        List<String> selectedJobIdsInStr = Arrays.asList(p_selectedJobIds);
        for (String id : selectedJobIdsInStr)
        {
            jobIdsToUpdateFrom.add(Long.parseLong(id));
        }

        // Parameter 5:"p_tmIds"(Keep this parameter empty)
        Set<Long> tmIds = new HashSet<Long>();

        // Leverage from specified jobs' in-progress translation data and store
        // them into map.
        Map<Long, LeverageMatches> ipMatches = new HashMap<Long, LeverageMatches>();

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            Leverager leverager = new Leverager(conn);
            for (BaseTmTuv srcTuv : p_sourceTuvs)
            {
                try
                {
                    LeverageMatches levMatches = null;
                    if (srcTuv.isTranslatable())
                    {
                        levMatches = leverager.leverageTranslatableSegment(
                                srcTuv, targetLocale, levOptions,
                                jobIdsToUpdateFrom, tmIds);
                    }
                    else
                    {
                        levMatches = leverager.leverageLocalizableSegment(
                                srcTuv, srcTuv.getLocale(), targetLocale,
                                levOptions, jobIdsToUpdateFrom, tmIds);
                    }

                    if (levMatches != null
                            && levMatches.getLeveragedTus().size() > 0)
                    {
                        ipMatches.put(srcTuv.getId(), levMatches);
                    }
                }
                catch (Exception e)
                {
                    logger.error(
                            "Error to leverage from in-progress TM for TuvID : "
                                    + srcTuv.getId(), e);
                    // throw new EnvoyServletException(e);
                }
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
        logger.debug("End getInProgressTranslationFromJobs()...");

        return ipMatches;
    }

    /**
     * Apply in progress TM penalty
     */
    public static LeverageMatches applyInProgressTmPenalty(
            LeverageMatches p_levMatches, TranslationMemoryProfile p_tmProfile,
            GlobalSightLocale p_targetLocale, int p_intIpTmPenalty)
    {
        if (p_levMatches == null || p_tmProfile == null
                || p_targetLocale == null || p_intIpTmPenalty == 0)
        {
            return p_levMatches;
        }

        List<LeveragedTu> levreagedTus = p_levMatches.getLeveragedTus();
        List<LeveragedTu> newLevreagedTus = new ArrayList<LeveragedTu>();
        long threshold = p_tmProfile.getFuzzyMatchThreshold();
        for (LeveragedTu tu : levreagedTus)
        {
            Collection<BaseTmTuv> tuvList = tu.getTuvList(p_targetLocale);
            for (Iterator tuvIter = tuvList.iterator(); tuvIter.hasNext();)
            {
                LeveragedTuv leveragedTuv = (LeveragedTuv) tuvIter.next();
                float score = leveragedTuv.getScore() - p_intIpTmPenalty;
                if (score < 50)
                {
                    tu.removeTuv(leveragedTuv);
                }
                else if (score < threshold && score >= 50)
                {
                    leveragedTuv.setScore(score);
                    leveragedTuv.setMatchState(MatchState.STATISTICS_MATCH);
                    tu.addTuv(leveragedTuv);
                }
                else if (score >= threshold && score < 100)
                {
                    leveragedTuv.setScore(score);
                    leveragedTuv.setMatchState(MatchState.FUZZY_MATCH);
                    tu.addTuv(leveragedTuv);
                }
            }

            if (tu.getTuvList(p_targetLocale) != null
                    && tu.getTuvList(p_targetLocale).size() > 0)
            {
                newLevreagedTus.add(tu);
            }
        }

        p_levMatches.setLeveragedTus(newLevreagedTus);

        return p_levMatches;
    }

    /**
     * Re-leverage reference TMs
     */
    public static LeverageDataCenter reApplyReferenceTMs(Workflow p_workflow,
            List<BaseTmTuv> p_sourceTuvs)
    {
        // Parameter 2,3
        GlobalSightLocale sourceLocale = p_workflow.getJob().getSourceLocale();
        GlobalSightLocale targetLocale = p_workflow.getTargetLocale();
        List<GlobalSightLocale> trgLocales = new ArrayList<GlobalSightLocale>();
        trgLocales.add(targetLocale);
        // Parameter 4
        L10nProfile lp = p_workflow.getJob().getL10nProfile();
        TranslationMemoryProfile tmProfile = lp.getTranslationMemoryProfile();
        LeveragingLocales leveragingLocales = lp.getLeveragingLocales();

        LeveragingLocales newLeveragingLocales = new LeveragingLocales();
        Set<GlobalSightLocale> levLocales = leveragingLocales
                .getLeveragingLocales(targetLocale);
        Set<GlobalSightLocale> newLevLocales = new HashSet<GlobalSightLocale>();
        newLevLocales.addAll(levLocales);
        newLeveragingLocales.setLeveragingLocale(targetLocale, newLevLocales);

        LeverageOptions leverageOptions = new LeverageOptions(tmProfile,
                newLeveragingLocales);

        // Leverage reference TMs
        LeverageDataCenter leverageDataCenter = null;
        try
        {
            leverageDataCenter = LingServerProxy.getTmCoreManager()
                    .leverageSegments(p_sourceTuvs, sourceLocale, trgLocales,
                            leverageOptions, p_workflow.getJob());
        }
        catch (Exception e)
        {
            logger.error("Failed to leverage segments for workflow: "
                    + p_workflow.getId(), e);
            throw new EnvoyServletException(e);
        }

        return leverageDataCenter;
    }

    /**
     * Check if there are duplicated matches in DB, if have, always pick the
     * latest.
     * 
     * @deprecated not in use since 8.6.5
     */
    public static Map<Long, LeverageMatches> removeMatchesExistedInDB(
            Map<Long, LeverageMatches> p_leverageMatchesMap,
            GlobalSightLocale p_targetLocale, long p_jobId)
    {
        if (p_leverageMatchesMap == null || p_leverageMatchesMap.size() == 0
                || p_targetLocale == null)
        {
            return new HashMap<Long, LeverageMatches>();
        }

        Map<Long, LeverageMatches> results = new HashMap<Long, LeverageMatches>();

        Connection conn = null;
        logger.debug("Begin filter duplicated matches.");
        Iterator iter = p_leverageMatchesMap.entrySet().iterator();
        try
        {
            conn = DbUtil.getConnection();
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next();
                Long originalSourceTuvId = (Long) entry.getKey();
                LeverageMatches levMatches = (LeverageMatches) entry.getValue();

                SegmentTmTuv originalSourceTuv = (SegmentTmTuv) levMatches
                        .getOriginalTuv();
                SegmentTmTu originalTu = (SegmentTmTu) originalSourceTuv
                        .getTu();

                List<LeveragedTu> levreagedTus = levMatches.getLeveragedTus();
                List<LeveragedTu> newLeveragedTus = new ArrayList<LeveragedTu>();
                for (LeveragedTu leveragedTu : levreagedTus)
                {
                    Collection<BaseTmTuv> tuvList = leveragedTu
                            .getTuvList(p_targetLocale);
                    if (tuvList != null && tuvList.size() > 0)
                    {
                        Iterator tuvIter = tuvList.iterator();
                        boolean tuvIterRemoved = false;
                        while (tuvIter.hasNext())
                        {
                            LeveragedTuv matchedTuv = (LeveragedTuv) tuvIter
                                    .next();
                            long tmId = leveragedTu.getTmId();
                            long matchedTuvId = matchedTuv.getId();
                            float score = matchedTuv.getScore();
                            String targetSegmentText = matchedTuv.getSegment();

                            SortedSet<LeverageMatch> tuvMatches;
                            tuvMatches = leverageMatchLingManager
                                    .getTuvMatches(originalSourceTuv.getId(),
                                            p_targetLocale.getId(),
                                            originalTu.getSubId(), false,
                                            p_jobId);
                            for (LeverageMatch lmInDB : tuvMatches)
                            {
                                int projectTmIndex = lmInDB.getProjectTmIndex();
                                // from project TM duplicated
                                boolean isProjectTmMatchDuplicated = false;
                                boolean isInProgressTmMatchDuplicated = false;
                                if (projectTmIndex >= -1
                                        && lmInDB.getTmId() == tmId
                                        && lmInDB.getMatchedTuvId() == matchedTuvId)
                                {
                                    isProjectTmMatchDuplicated = true;
                                }
                                // in progress TM matches duplicated
                                else if (projectTmIndex == com.globalsight.ling.tm2.leverage.Leverager.IN_PROGRESS_TM_PRIORITY
                                        && lmInDB.getTmId() == tmId // job id
                                        && targetSegmentText != null
                                        && targetSegmentText.equals(lmInDB
                                                .getMatchedText()))
                                {
                                    isInProgressTmMatchDuplicated = true;
                                }

                                // If duplicated, update the record in DB(always
                                // pick latest) instead of saving new.
                                if (isProjectTmMatchDuplicated
                                        || isInProgressTmMatchDuplicated)
                                {
                                    // Update "scoreNum" and "matchType" (delete
                                    // then re-save).
                                    Collection<LeverageMatch> lmList = new ArrayList<LeverageMatch>();
                                    lmInDB.setScoreNum(score);
                                    lmInDB.setMatchType(matchedTuv
                                            .getMatchState().getName());
                                    lmList.add(lmInDB);
                                    leverageMatchLingManager
                                            .deleteLeverageMatches(
                                                    lmInDB.getOriginalSourceTuvId(),
                                                    lmInDB.getSubId(),
                                                    lmInDB.getTargetLocaleId(),
                                                    new Long(lmInDB.getOrderNum()),
                                                    p_jobId);
                                    leverageMatchLingManager
                                            .saveLeveragedMatches(lmList, conn,
                                                    p_jobId);
                                    // Remove this TUV to avoid to be saved into
                                    // DB again.
                                    if (!tuvIterRemoved)
                                    {
                                        tuvIter.remove();
                                        tuvIterRemoved = true;
                                    }
                                }
                            }
                        }
                    }

                    // Do not save "STATISTICS_MATCH" into DB when update leverage.
                    if (!MatchState.STATISTICS_MATCH.equals(leveragedTu
                            .getMatchState()))
                    {
                        newLeveragedTus.add(leveragedTu);
                    }
                }

                levMatches.setLeveragedTus(newLeveragedTus);
                results.put(originalSourceTuvId, levMatches);
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to detect duplicated matches while updating leverage.",
                    e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
        logger.debug("End filter duplicated matches.");

        return results;
    }

    /**
     * Populate exact matches into target TUVs.
     */
    public static boolean populateExactMatchesToTargetTuvs(
            Map<Long, ArrayList<LeverageSegment>> exactMap,
            Collection<Tuv> untranslatedSrcTuvs,
            GlobalSightLocale p_targetLocale, String p_userId,
            long sourcePageId, long p_jobId) throws TuvException,
            RemoteException, GeneralException
    {
        if (exactMap != null && exactMap.size() > 0)
        {
            logger.debug("Begin populateExactMatchesToTargetTuvs()...");
            try
            {
                List<TuvImpl> tuvsToBeUpdated = new ArrayList<TuvImpl>();
                InProgressTmManager ipTmMgr = LingServerProxy
                        .getInProgressTmManager();
                for (Tuv srcTuv : untranslatedSrcTuvs)
                {
                    Tu tu = srcTuv.getTu(p_jobId);
                    long trgTuvId = tu.getTuv(p_targetLocale.getId(), p_jobId)
                            .getId();
                    Tuv trgTuv = tuvManager.getTuvForSegmentEditor(trgTuvId,
                            p_jobId);
                    ArrayList<LeverageSegment> lss = exactMap.get(srcTuv
                            .getIdAsLong());
                    if (lss != null && lss.size() > 0)
                    {
                        for (LeverageSegment segment : lss)
                        {
                            boolean canBeModified = SegmentUtil2.canBeModified(
                                    trgTuv, segment.getSegment(), p_jobId);
                            if (canBeModified)
                            {
                                trgTuv = modifyTUV(trgTuv, segment);
                                trgTuv.setLastModifiedUser(p_userId);
                                trgTuv.setState(TuvState.LOCALIZED);
                                tu.addTuv(trgTuv);
                                trgTuv.setTu(tu);

                                tuvsToBeUpdated.add((TuvImpl) trgTuv);
                                // save into In-Progress TM too.
                                ipTmMgr.save(srcTuv, trgTuv, "0", sourcePageId);
                                break;
                            }
                        }
                    }
                }

                SegmentTuvUtil.updateTuvs(tuvsToBeUpdated, p_jobId);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            logger.debug("End populateExactMatchesToTargetTuvs()...");
        }

        return true;
    }

    private static Tuv modifyTUV(Tuv tuv, LeverageSegment leverageSegment)
    {
        if (leverageSegment != null)
        {
            try
            {
                tuv.setGxml(leverageSegment.getSegment());
            }
            catch (Exception e)
            {
                logger.error("Exception when getting the gxml for a leveraged segment.");
                logger.error("+++The leverage match string is "
                        + leverageSegment.getSegment() + "+++");
                return null;
            }
            tuv.setMatchType(leverageSegment.getMatchType());

            if (leverageSegment.getMatchType() == null)
            {
                tuv.setMatchType(LeverageMatchType.UNKNOWN_NAME);
            }
        }

        return tuv;
    }

    public static TranslationMemoryProfile getTMProfile(Task p_task)
    {
        TranslationMemoryProfile tmProfile = p_task.getWorkflow().getJob()
                .getL10nProfile().getTranslationMemoryProfile();
        return tmProfile;
    }

    public static TranslationMemoryProfile getTMProfile(Workflow p_workflow)
    {
        TranslationMemoryProfile tmProfile = p_workflow.getJob()
                .getL10nProfile().getTranslationMemoryProfile();
        return tmProfile;
    }

    public static int getMode(TranslationMemoryProfile p_tmProfile)
    {
        int mode = LeverageOptions.PICK_LATEST;
        if (!p_tmProfile.isLatestMatchForReimport())
        {
            mode = p_tmProfile.getMultipleExactMatcheMode();
        }

        return mode;
    }

    /**
     * Filter to return workflow IDs whose state is matched.
     * 
     * @param p_wfIds
     * @return
     * @throws Exception
     */
    public static List<Long> filterWorkflowsByState(List<Long> p_wfIds,
            String p_wfState) throws Exception
    {
        if (p_wfIds == null || p_wfIds.size() == 0)
        {
            return Collections.emptyList();
        }

        List<Long> filteredWfIds = new ArrayList<Long>();
        for (Iterator it = p_wfIds.iterator(); it.hasNext();)
        {
            Long wfId = (Long) it.next();
            Workflow wf = ServerProxy.getWorkflowManager()
                    .getWorkflowById(wfId);
            if (p_wfState != null && p_wfState.equals(wf.getState()))
            {
                filteredWfIds.add(wfId);
            }
        }

        return filteredWfIds;
    }

    /**
     * Split the workflow IDs whitespace separated to return a list. i.e. String
     * "1 2 3 4" to a list to include these.
     * 
     * @param p_workflowIds
     * @return
     */
    public static List<Long> getWfIds(String p_workflowIds)
    {
        List<Long> wfIds = new ArrayList<Long>();

        if (p_workflowIds != null && !"".equals(p_workflowIds.trim()))
        {
            StringTokenizer tokenizer = new StringTokenizer(p_workflowIds);
            while (tokenizer.hasMoreTokens())
            {
                String wfId = tokenizer.nextToken();
                wfIds.add(Long.parseLong(wfId));
            }
        }

        return wfIds;
    }

}
