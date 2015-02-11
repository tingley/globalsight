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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.inprogresstm.leverage.Leverager;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tw.PseudoCodec;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoErrorChecker;
import com.globalsight.ling.tw.Tmx2PseudoHandler;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.GxmlUtil;

/**
 * UpdateLeverageHelper includes some methods for "update leverage" feature.
 * @author YorkJin
 * @since 8.2
 */
public class UpdateLeverageHelper
{
    private static final Logger logger = 
        Logger.getLogger(UpdateLeverageHelper.class);
    
    // Managers
    private static TuvManager tuvManager = ServerProxy.getTuvManager();
    private static LeverageMatchLingManager leverageMatchLingManager = 
        LingServerProxy.getLeverageMatchLingManager();
//    private static TaskManager taskManager = ServerProxy.getTaskManager();
    
    private static String BEST_MATCH_SCORE = "select max(score_num) from leverage_match "
            + "where original_source_tuv_id = ? "
            + "and target_locale_id = ? "
            + "and sub_id = ? ";
    
    /**
     * Get untranslated source segments. The untranslated segments must 1)be in
     * "NOT_LOCALIZED" state,2)source and target segment contents are the same,
     * 3)and best leverage match's score should be less than 100.
     */
    public static Collection<Tuv> getUntranslatedTuvs(TargetPage p_tp,
            long p_sourceLocaleId)
    {
        if (p_tp == null || p_sourceLocaleId < 1)
        {
            return new ArrayList();
        }

        Collection<Tuv> untranslatedSrcTuvs = new ArrayList();
        Session session = null;
        PreparedStatement ps = null;
        try
        {
            GlobalSightLocale targetLocale = p_tp.getGlobalSightLocale();
            Collection<Tuv> targetTuvs = tuvManager.getTargetTuvsForStatistics(p_tp);
            session = TmUtil.getStableSession();
            ps = session.connection().prepareStatement(BEST_MATCH_SCORE);
            for (Tuv trgTuv : targetTuvs)
            {
                Tuv srcTuv = trgTuv.getTu().getTuv(p_sourceLocaleId);

                float bestMatchScore = 0;
                bestMatchScore = getBestMatchScore(ps, srcTuv.getId(),
                        targetLocale.getId(), "0");

                if (trgTuv.getState().getValue() == TuvState.NOT_LOCALIZED.getValue()
                        && srcTuv.getGxml().equals(trgTuv.getGxml())
                        && bestMatchScore < 100)
                {
                    untranslatedSrcTuvs.add(srcTuv);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get untranslated Tuvs for target page : "
                    + p_tp.getId(), e);
            throw new EnvoyServletException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ignore) {
                    ignore.printStackTrace();
                }
            }
            if (session != null) {
                TmUtil.closeStableSession(session);
            }
        }

        return untranslatedSrcTuvs;
    }
    
    /**
     * Get best match score for specified TUV.
     */
    private static float getBestMatchScore(PreparedStatement p_stmt,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId)
            throws Exception
    {
        // default;
        float bestMatchScore = 0;
        
        ResultSet rs = null;
        try
        {
            p_stmt.setLong(1, p_originalSourceTuvId);
            p_stmt.setLong(2, p_targetLocaleId);
            p_stmt.setString(3, p_subId);
            rs = p_stmt.executeQuery();
            while (rs.next())
            {
                bestMatchScore = rs.getInt(1);
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to get best match score for originalSourceTuvId "
                            + p_originalSourceTuvId + ", subId : " + p_subId
                            + ", targetLocaleId : " + p_targetLocaleId, e);
        }
        finally
        {
            DbUtil.silentClose(rs);
        }

        return bestMatchScore;
    }
    
    /**
     * Update from specified jobs' in-progress translations.
     */
    public static Map<Long, LeverageMatches> getInProgressTranslationFromJobs(
            Task p_task, List<BaseTmTuv> p_sourceTuvs, String[] p_selectedJobIds)
    {
        GlobalSightLocale targetLocale = p_task.getTargetLocale();

        // Parameter 3:"p_leverageOptions"
        TranslationMemoryProfile tmProfile = getTMProfile(p_task);
        Set trgLocales = new HashSet();
        trgLocales.add(targetLocale);
        LeveragingLocales levLocales = new LeveragingLocales();
        levLocales.setLeveragingLocale(targetLocale, trgLocales);
        LeverageOptions levOptions = new LeverageOptions(tmProfile,levLocales);

        // Parameter 4:"p_jobIdsToUpdateFrom"
        Set<Long> jobIdsToUpdateFrom = new HashSet();
        // Current job's in-progress TM data should always be leveraged.
//        jobIdsToUpdateFrom.add(jobId);
        // Selected jobs
        List<String> selectedJobIdsInStr = Arrays.asList(p_selectedJobIds);
        for (String id : selectedJobIdsInStr) {
            jobIdsToUpdateFrom.add(Long.parseLong(id));
        }

        // Parameter 5:"p_tmIds"(Keep this parameter empty)
        Set<Long> tmIds = new HashSet();

        // Leverage from specified jobs' in-progress translation data and store
        // them into map.
        Map<Long, LeverageMatches> ipMatches = new HashMap();
        
        Session session = TmUtil.getStableSession();
        try 
        {
            Leverager leverager = new Leverager(session.connection());
            for (BaseTmTuv srcTuv : p_sourceTuvs) 
            {
                try 
                {
                    LeverageMatches levMatches = null;
                    if (srcTuv.isTranslatable()) 
                    {
                        levMatches = leverager
                                .leverageTranslatableSegment(srcTuv, targetLocale,
                                        levOptions, jobIdsToUpdateFrom, tmIds);
                    }
                    else 
                    {
                        levMatches = leverager.leverageLocalizableSegment(srcTuv,
                                srcTuv.getLocale(), targetLocale, levOptions,
                                jobIdsToUpdateFrom, tmIds);
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
                    e.printStackTrace();
//                    throw new EnvoyServletException(e);
                }
            }
        }
        catch (Exception e) {

        }
        finally 
        {
            if (session != null) {
                TmUtil.closeStableSession(session);
            }
        }
        
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
        List<LeveragedTu> newLevreagedTus = new ArrayList();
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
                    && tu.getTuvList(p_targetLocale).size() > 0) {
                newLevreagedTus.add(tu);
            }
        }
        
        p_levMatches.setLeveragedTus(newLevreagedTus);

        return p_levMatches;
    }
    
    /**
     * Re-leverage reference TMs 
     */
    public static LeverageDataCenter reApplyReferenceTMs(Task p_task,
            List<BaseTmTuv> p_sourceTuvs)
    {
        // Parameter 2,3
        GlobalSightLocale sourceLocale = p_task.getSourceLocale();
        GlobalSightLocale targetLocale = p_task.getTargetLocale();
        List<GlobalSightLocale> trgLocales = new ArrayList();
        trgLocales.add(targetLocale);
        // Parameter 4
        L10nProfile lp = p_task.getWorkflow().getJob().getL10nProfile();
        TranslationMemoryProfile tmProfile = lp.getTranslationMemoryProfile();
        LeveragingLocales leveragingLocales = lp.getLeveragingLocales();
        LeverageOptions leverageOptions = new LeverageOptions(tmProfile,
                leveragingLocales);
//      levOptions.setMatchThreshold(threshold);
//      levOptions.setTmsToLeverageFrom(tmIdsOverride);

        // Leverage reference TMs
        LeverageDataCenter leverageDataCenter = null;
        try
        {
            leverageDataCenter = LingServerProxy.getTmCoreManager()
                    .leverageSegments(p_sourceTuvs, sourceLocale, trgLocales,
                            leverageOptions);
        }
        catch (Exception e)
        {
            logger.error("Failed to leverage segments for task : "
                    + p_task.getId(), e);
            throw new EnvoyServletException(e);
        }
      
      return leverageDataCenter;
    }
    
    /**
     * Check if there are duplicated matches in DB, if have, always pick the latest.
     */
    public static Map<Long, LeverageMatches> removeMatchesExistedInDB(
            Map<Long, LeverageMatches> p_leverageMatchesMap,
            GlobalSightLocale p_targetLocale)
    {
        if (p_leverageMatchesMap == null
                || p_leverageMatchesMap.size() == 0
                || p_targetLocale == null) {
            return new HashMap();
        }

        Map<Long, LeverageMatches> results = new HashMap();
        
        Iterator iter = p_leverageMatchesMap.entrySet().iterator();
        while (iter.hasNext()) 
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Long originalSourceTuvId = (Long) entry.getKey();
            LeverageMatches levMatches = (LeverageMatches) entry.getValue();
            
            SegmentTmTuv originalSourceTuv = 
                (SegmentTmTuv) levMatches.getOriginalTuv();
            SegmentTmTu originalTu = (SegmentTmTu) originalSourceTuv.getTu();

            List<LeveragedTu> levreagedTus = levMatches.getLeveragedTus();
            List<LeveragedTu> newLeveragedTus = new ArrayList();
            for (LeveragedTu leveragedTu : levreagedTus)
            {
                Collection<BaseTmTuv> tuvList = leveragedTu
                        .getTuvList(p_targetLocale);
                if (tuvList != null && tuvList.size() > 0)
                {
                    Iterator tuvIter = tuvList.iterator();
                    while (tuvIter.hasNext())
                    {
                        LeveragedTuv matchedTuv = (LeveragedTuv) tuvIter.next();
                        long tmId = leveragedTu.getTmId();
                        long matchedTuvId = matchedTuv.getId();
                        float score = matchedTuv.getScore();
                        String targetSegmentText = matchedTuv.getSegment();
                        
                        SortedSet<LeverageMatch> tuvMatches;
                        try {
                            tuvMatches = leverageMatchLingManager.getTuvMatches(
                                    originalSourceTuv.getId(), p_targetLocale
                                            .getId(), originalTu.getSubId(), false);
                            for (LeverageMatch lmInDB : tuvMatches)
                            {
                                int projectTmIndex = lmInDB.getProjectTmIndex();
                                // from project TM duplicated
                                boolean isProjectTmMatchDuplicated = false;
                                boolean isInProgressTmMatchDuplicated = false;
                                if (projectTmIndex >= -1 && lmInDB.getTmId() == tmId
                                        && lmInDB.getMatchedTuvId() == matchedTuvId)
                                {
                                    isProjectTmMatchDuplicated = true;
                                }
                                // in progress TM matches duplicated
                                else if (projectTmIndex == com.globalsight.ling.tm2.leverage.Leverager.IN_PROGRESS_TM_PRIORITY
                                        && lmInDB.getTmId() == tmId // job id
                                        && targetSegmentText != null
                                        && targetSegmentText.equals(lmInDB.getMatchedText()))
                                {
                                    isInProgressTmMatchDuplicated = true;
                                }
                                
                                // If duplicated, update the record in DB(always
                                // pick latest) instead of saving new.
                                if (isProjectTmMatchDuplicated || isInProgressTmMatchDuplicated)
                                {
                                    // Update "LeverageMatch" data in DB,only "score" changed.
                                    Collection lmList = new ArrayList();
                                    lmInDB.setScoreNum(score);
                                    lmInDB.setMatchType(matchedTuv.getMatchState().getName());
                                    lmList.add(lmInDB);
                                    leverageMatchLingManager.saveLeveragedMatches(lmList);
                                    // Remove this TUV to avoid to be saved into DB again.
                                    leveragedTu.removeTuv(matchedTuv);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Failed to detect duplicated matches while updating leverage.", e);
                        }
                    }
                }

                Collection tuvList2 = leveragedTu.getTuvList(p_targetLocale);
                if (tuvList2 != null && tuvList2.size() > 0) {
                    newLeveragedTus.add(leveragedTu);
                }
            }
            
            levMatches.setLeveragedTus(newLeveragedTus);
            results.put(originalSourceTuvId, levMatches);
        }

        return results;
    }
    
    /**
     * Populate exact matches into target TUVs. 
     */
    public static boolean populateExactMatchesToTargetTuvs(
            Map<Long, ArrayList<LeverageSegment>> exactMap,
            Collection<Tuv> untranslatedSrcTuvs,
            GlobalSightLocale p_targetLocale, User p_user) throws TuvException,
            RemoteException, GeneralException
    {
        if (exactMap != null && exactMap.size() > 0)
        {
            for (Tuv srcTuv :untranslatedSrcTuvs)
            {
                Tu tu = srcTuv.getTu();
                long trgTuvId = tu.getTuv(p_targetLocale.getId()).getId();
                Tuv trgTuv = tuvManager.getTuvForSegmentEditor(trgTuvId);
                ArrayList<LeverageSegment> lss = exactMap.get(srcTuv.getIdAsLong());
                if (lss != null && lss.size() > 0)
                {
                    for (LeverageSegment segment : lss)
                    {
                        if (canBeModified(trgTuv, segment))
                        {
                            trgTuv = modifyTUV(trgTuv, segment);
                            trgTuv.setLastModifiedUser(p_user.getUserId());
                            tu.addTuv(trgTuv);
                            trgTuv.setTu(tu);
                            tuvManager.updateTuv(trgTuv);

                            break;
                        }
                    }
                }
            }
        }

        return true;
    }

    private static boolean canBeModified(Tuv tuv, LeverageSegment leverageSegment)
    {
        try 
        {
            PseudoData pseudoData = toPsedoData(tuv.getGxmlExcludeTopTags());
            pseudoData.setPTagTargetString(toPsedoData(
                    GxmlUtil.stripRootTag(leverageSegment.getSegment()))
                    .getPTagSourceString());
            pseudoData.setDataType(tuv.getDataType());
            PseudoErrorChecker checker = new PseudoErrorChecker();

            return checker.check(pseudoData, tuv.getGxmlExcludeTopTags(), 0,
                    "utf8", 0, "utf8") == null;            
        }
        catch (Exception e) 
        {
            return false;
        }
    }
    
    private static PseudoData toPsedoData(String s)
            throws DiplomatBasicParserException
    {
        PseudoCodec codec = new PseudoCodec();
        s = codec.encode(s);
        PseudoData pseudoData = new PseudoData();
        pseudoData.setMode(PseudoConstants.PSEUDO_COMPACT);
        Tmx2PseudoHandler eventHandler = new Tmx2PseudoHandler(pseudoData);
        s = eventHandler.preProcessInternalText(s);
        DiplomatBasicParser parser = new DiplomatBasicParser(eventHandler);
        parser.parse(s);
        pseudoData = eventHandler.getResult();
        return eventHandler.getResult();
    }
    
    private static Tuv modifyTUV(Tuv tuv, LeverageSegment leverageSegment)
    {
        if (leverageSegment != null) {
            try {
                tuv.setGxml(leverageSegment.getSegment());
            } catch (Exception e) {
                logger.error("Exception when getting the gxml for a leveraged segment.");
                logger.error("+++The leverage match string is "
                        + leverageSegment.getSegment() + "+++");
                return null;
            }
            tuv.setMatchType(leverageSegment.getMatchType());

            if (leverageSegment.getMatchType() == null) {
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
    
    public static int getMode(TranslationMemoryProfile p_tmProfile)
    {
        int mode = LeverageOptions.PICK_LATEST;
        if (!p_tmProfile.isLatestMatchForReimport())
        {
            mode = p_tmProfile.getMultipleExactMatcheMode();
        }
        
        return mode;
    }

}
