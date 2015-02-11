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

package com.globalsight.everest.statistics;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.integration.ling.tm2.Types;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.TuvSourceContentComparator;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Provides statistic services for pages.
 */
public class StatisticsService
{
    private static Logger c_logger = Logger.getLogger(StatisticsService.class);

    // private static SimpleDateFormat format = new
    // SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Calculate word counts for all target pages in a workflow, and calculate
     * the repetitions in the scope of workflow.
     */
    public static void calculateTargetPagesWordCount(Workflow p_workflow,
            Vector<String> p_excludedTuTypes)
    {
        try
        {
            List<TargetPage> targetPages = getAllTargetPagesForWorkflow(p_workflow);
            Map<SegmentTmTuv, List<SegmentTmTuv>> m_uniqueSegments = new HashMap<SegmentTmTuv, List<SegmentTmTuv>>();
            Map uniqueSegments2 = new HashMap();
            int threshold = p_workflow.getJob().getLeverageMatchThreshold();

            // Touch every page's TUs to load them to improve performance.
            for (TargetPage targetPage : targetPages)
            {
                if (targetPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
                {
                    SegmentTuUtil.getTusBySourcePageId(targetPage
                            .getSourcePage().getId());
                }
            }

            for (TargetPage targetPage : targetPages)
            {
                if (targetPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
                {
                    SourcePage sourcePage = targetPage.getSourcePage();
                    // As here don't need XliffAlt data, don't load them to
                    // improve performance.
                    boolean needLoadExtraInfo = false;
                    ArrayList<Tuv> sTuvs = SegmentTuvUtil.getSourceTuvs(
                            sourcePage, needLoadExtraInfo);
                    ArrayList<BaseTmTuv> splittedTuvs = splitSourceTuvs(sTuvs,
                            sourcePage.getGlobalSightLocale(),
                            sourcePage.getCompanyId());
                    String sourcePageId = sourcePage.getExternalPageId();
                    boolean isDefaultContextMatch = false;
                    // Only when "Leverage Default Matches" option is selected
                    // in TM profile, it is necessary to judge if the page is
                    // "DefaultContextMatch"(GBS-2214 by York since 2011-12-13)
                    if (PageHandler.isDefaultContextMatch(p_workflow.getJob()))
                    {
                        isDefaultContextMatch = isDefaultContextMatch(
                                sourcePageId, sourcePage);
                    }
                    targetPage.setIsDefaultContextMatch(isDefaultContextMatch);
                    Long targetLocaleId = targetPage.getLocaleId();
                    boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                            .isWorldServerXliffSourceFile(
                                    sourcePage.getIdAsLong());

                    LeverageMatchLingManager lmLingManager = getLeverageMatchLingManager();

                    if (isWSXlfSourceFile)
                    {
                        lmLingManager.setIncludeMtMatches(false);
                    }

                    MatchTypeStatistics matches = lmLingManager
                            .getMatchTypesForStatistics(
                                    sourcePage.getIdAsLong(), targetLocaleId,
                                    threshold);

                    PageWordCounts targetPageWordCounts = null;
                    if (isWSXlfSourceFile)
                    {
                        targetPageWordCounts = calculateWorldServerTargetPageWordCounts(
                                splittedTuvs, matches, p_excludedTuTypes);
                        targetPage.setWordCount(targetPageWordCounts);
                    }
                    else
                    {
                        targetPageWordCounts = calculateTargetPageWordCounts(
                                splittedTuvs, matches, p_excludedTuTypes,
                                m_uniqueSegments);
                        targetPage.setWordCount(targetPageWordCounts);
                    }

                    // Update "Segment-TM,allExactMatch,ICE" word counts.
                    updateExtraColumnWordCountsForTargetPage(targetPage,
                            splittedTuvs, matches, p_excludedTuTypes);

                    HibernateUtil.update(targetPage);

                    // update TU table for repetition information.
                    Map<Long, TuImpl> cachedTus = getTusMapBySourcePage(sourcePage);
                    updateRepetitionInfoToTu(splittedTuvs, matches,
                            uniqueSegments2, cachedTus,
                            Long.parseLong(sourcePage.getCompanyId()));
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
    }

    /**
     * get target pages for Workflow. Not from session cache
     * 
     * @param p_workflow
     * @return
     */
    private static List<TargetPage> getAllTargetPagesForWorkflow(
            Workflow p_workflow)
    {
        List<TargetPage> tpages = new ArrayList<TargetPage>();
        long wfid = p_workflow.getId();

        try
        {
            String sql = "select tp.* from target_page tp where tp.WORKFLOW_IFLOW_INSTANCE_ID = "
                    + wfid;
            tpages = HibernateUtil.searchWithSql(TargetPage.class, sql);
            List<TargetPage> oriTPS = p_workflow.getAllTargetPages();
            int tpagesSize = tpages.size();

            // add missing target pages to Workflow (in cache)
            if (oriTPS.size() < tpagesSize)
            {
                for (TargetPage tp : tpages)
                {
                    boolean exists = false;
                    for (TargetPage tp2 : oriTPS)
                    {
                        if (tp2.getId() == tp.getId())
                        {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists)
                    {
                        p_workflow.addTargetPage(tp);
                        oriTPS = p_workflow.getAllTargetPages();

                        if (oriTPS.size() == tpagesSize)
                        {
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            c_logger.warn("Error when getAllTargetPagesForWorkflow, wfid : "
                    + wfid, ex);
            tpages = p_workflow.getAllTargetPages();
        }

        return tpages;
    }

    /**
     * Calculate the target page word counts for WorldServer XLF target page.
     */
    private static PageWordCounts calculateWorldServerTargetPageWordCounts(
            ArrayList<BaseTmTuv> sTuvs, MatchTypeStatistics p_matches,
            Vector<String> p_excludedTuTypes)
    {
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;
        int mtExactMatchWordCount = 0;
        int xliffMatchWordCount = 0;
        // below 50%
        int repetitionWordCount = 0;
        int noMatchWordCount = 0;
        // 50%--75%
        int subLevRepetitionWordCount = 0;
        int subLevMatchWordCount = 0;
        // 75%--84%
        int medFuzzyRepetitionWordCount = 0;
        int medFuzzyWordCount = 0;
        // 85%--94%
        int medHighFuzzyRepetionWordCount = 0;
        int medHighFuzzyWordCount = 0;
        // 95%--99%
        int highFuzzyRepetionWordCount = 0;
        int highFuzzyWordCount = 0;
        // go through all segments
        int totalWordCount = 0;

        int thresholdHiFuzzyWordCount = 0;
        int thresholdMedHiFuzzyWordCount = 0;
        int thresholdMedFuzzyWordCount = 0;
        int thresholdLowFuzzyWordCount = 0;
        int thresholdNoMatchWordCount = 0;

        Iterator<BaseTmTuv> si = sTuvs.iterator();
        while (si.hasNext())
        {
            SegmentTmTuv tuv = (SegmentTmTuv) si.next();
            int wordCount = tuv.getWordCount();

            // Don't count excluded items.
            if (p_excludedTuTypes != null
                    && p_excludedTuTypes.contains(tuv.getTu().getType()))
            {
                wordCount = 0;
            }
            totalWordCount += wordCount;
            Types types = p_matches.getTypes(tuv.getId(),
                    ((SegmentTmTu) tuv.getTu()).getSubId());

            int matchType = types == null ? MatchTypeStatistics.NO_MATCH
                    : types.getStatisticsMatchType();
            String sourceContent = tuv.getTu().getSourceContent();
            // increment the word count according to the match type
            switch (matchType)
            {
                case MatchTypeStatistics.CONTEXT_EXACT:
                    contextMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_TM_EXACT:
                    segmentTmWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_MT_EXACT:
                    mtExactMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                    xliffMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.LOW_FUZZY:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        subLevRepetitionWordCount += wordCount;
                    }
                    else
                    {
                        subLevMatchWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_FUZZY:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        medFuzzyRepetitionWordCount += wordCount;
                    }
                    else
                    {
                        medFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_HI_FUZZY:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        medHighFuzzyRepetionWordCount += wordCount;
                    }
                    else
                    {
                        medHighFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.HI_FUZZY:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        highFuzzyRepetionWordCount += wordCount;
                    }
                    else
                    {
                        highFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.NO_MATCH:
                default:
                    if (sourceContent != null
                            && sourceContent.equals("repetition"))
                    {
                        repetitionWordCount += wordCount;
                    }
                    else
                    {
                        noMatchWordCount += wordCount;
                    }
                    break;
            }

            /*
             * This part is used to calculate the word counts relative to
             * threshold, they
             * are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount
             * ,thresholdMedFuzzyWordCount,
             * thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
             */
            int statisticsMatchTypeByThreshold = MatchTypeStatistics.THRESHOLD_NO_MATCH;
            if (types != null)
            {
                statisticsMatchTypeByThreshold = types
                        .getStatisticsMatchTypeByThreshold();
            }
            switch (statisticsMatchTypeByThreshold)
            {
                case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdMedHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdMedFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdLowFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                    if (sourceContent == null
                            || !sourceContent.equals("repetition"))
                    {
                        thresholdNoMatchWordCount += wordCount;
                    }
                    break;
                default:
                    break;
            }
        }

        PageWordCounts pageWordCounts = new PageWordCounts();
        pageWordCounts.setContextMatchWordCount(contextMatchWordCount);
        pageWordCounts.setSegmentTmWordCount(segmentTmWordCount);
        pageWordCounts.setMTExtractMatchWordCount(mtExactMatchWordCount);
        pageWordCounts.setXliffExtractMatchWordCount(xliffMatchWordCount);
        pageWordCounts.setRepetitionWordCount(repetitionWordCount
                + highFuzzyRepetionWordCount + medHighFuzzyRepetionWordCount
                + medFuzzyRepetitionWordCount + subLevRepetitionWordCount);
        pageWordCounts.setUnmatchedWordCount(noMatchWordCount);
        pageWordCounts.setSubLevMatchWordCount(subLevMatchWordCount);
        pageWordCounts.setSubLevRepetitionWordCount(subLevRepetitionWordCount);
        pageWordCounts
                .setHiFuzzyRepetitionWordCount(highFuzzyRepetionWordCount);
        pageWordCounts
                .setMedHighFuzzyRepetitionWordCount(medHighFuzzyRepetionWordCount);
        pageWordCounts
                .setMedFuzzyRepetitionWordCount(medFuzzyRepetitionWordCount);
        pageWordCounts.setTotalWordCount(totalWordCount);
        pageWordCounts.setHiFuzzyWordCount(highFuzzyWordCount);
        pageWordCounts.setMedHiFuzzyWordCount(medHighFuzzyWordCount);
        pageWordCounts.setMedFuzzyWordCount(medFuzzyWordCount);
        pageWordCounts.setLowFuzzyWordCount(subLevMatchWordCount);

        pageWordCounts.setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        pageWordCounts
                .setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        pageWordCounts
                .setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        pageWordCounts
                .setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        pageWordCounts.setThresholdNoMatchWordCount(thresholdNoMatchWordCount);

        return pageWordCounts;
    }

    /**
     * Calculate the target page word counts. Repetition word counts are in job
     * scope.
     */
    private static PageWordCounts calculateTargetPageWordCounts(
            ArrayList<BaseTmTuv> sTuvs, MatchTypeStatistics p_matches,
            Vector<String> p_excludedTuTypes,
            Map<SegmentTmTuv, List<SegmentTmTuv>> m_uniqueSegments)
    {
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;
        int mtExactMatchWordCount = 0;
        int xliffMatchWordCount = 0;
        int poMatchWordCount = 0;
        // below 50%
        int repetitionWordCount = 0;
        int noMatchWordCount = 0;
        // 50%--75%
        int subLevRepetitionWordCount = 0;
        int subLevMatchWordCount = 0;
        // 75%--84%
        int medFuzzyRepetitionWordCount = 0;
        int medFuzzyWordCount = 0;
        // 85%--94%
        int medHighFuzzyRepetionWordCount = 0;
        int medHighFuzzyWordCount = 0;
        // 95%--99%
        int highFuzzyRepetionWordCount = 0;
        int highFuzzyWordCount = 0;
        // go through all segments
        int totalWordCount = 0;

        int thresholdHiFuzzyWordCount = 0;
        int thresholdMedHiFuzzyWordCount = 0;
        int thresholdMedFuzzyWordCount = 0;
        int thresholdLowFuzzyWordCount = 0;
        int thresholdNoMatchWordCount = 0;

        Map<SegmentTmTuv, List<SegmentTmTuv>> tmp = new HashMap<SegmentTmTuv, List<SegmentTmTuv>>();
        tmp.putAll(m_uniqueSegments);

        Iterator<BaseTmTuv> si = sTuvs.iterator();
        while (si.hasNext())
        {
            SegmentTmTuv tuv = (SegmentTmTuv) si.next();
            int wordCount = tuv.getWordCount();

            // Don't count excluded items.
            if (p_excludedTuTypes != null
                    && p_excludedTuTypes.contains(tuv.getTu().getType()))
            {
                wordCount = 0;
            }
            totalWordCount += wordCount;
            Types types = p_matches.getTypes(tuv.getId(),
                    ((SegmentTmTu) tuv.getTu()).getSubId());

            int matchType = types == null ? MatchTypeStatistics.NO_MATCH
                    : types.getStatisticsMatchType();
            ArrayList<SegmentTmTuv> identicalSegments = null;
            // increment the word count according to the match type
            switch (matchType)
            {
                case MatchTypeStatistics.CONTEXT_EXACT:
                    contextMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_TM_EXACT:
                    segmentTmWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_MT_EXACT:
                    mtExactMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                    xliffMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.SEGMENT_PO_EXACT:
                    poMatchWordCount += wordCount;
                    break;
                case MatchTypeStatistics.LOW_FUZZY:
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        subLevMatchWordCount += wordCount;
                    }
                    else
                    {
                        subLevRepetitionWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_FUZZY:
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        medFuzzyWordCount += wordCount;
                    }
                    else
                    {
                        medFuzzyRepetitionWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_HI_FUZZY:
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        medHighFuzzyWordCount += wordCount;
                    }
                    else
                    {
                        medHighFuzzyRepetionWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.HI_FUZZY:
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        highFuzzyWordCount += wordCount;
                    }
                    else
                    {
                        highFuzzyRepetionWordCount += wordCount;
                    }
                    break;
                case LeverageMatchLingManager.NO_MATCH:
                default:
                    // no-match is counted only once and the rest are
                    // repetitions
                    identicalSegments = (ArrayList<SegmentTmTuv>) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList<SegmentTmTuv>();
                        m_uniqueSegments.put(tuv, identicalSegments);
                        identicalSegments.add(tuv);
                        noMatchWordCount += wordCount;
                    }
                    else
                    {
                        repetitionWordCount += wordCount;
                    }
                    break;
            }

            /*
             * This part is used to calculate the word counts relative to
             * threshold, they
             * are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount
             * ,thresholdMedFuzzyWordCount,
             * thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
             */
            int statisticsMatchTypeByThreshold = MatchTypeStatistics.THRESHOLD_NO_MATCH;
            if (types != null)
            {
                statisticsMatchTypeByThreshold = types
                        .getStatisticsMatchTypeByThreshold();
            }
            ArrayList<SegmentTmTuv> identicalSegmentsOfThreshold = null;
            switch (statisticsMatchTypeByThreshold)
            {
                case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdMedHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdMedFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdLowFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                    identicalSegmentsOfThreshold = (ArrayList<SegmentTmTuv>) tmp
                            .get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList<SegmentTmTuv>();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdNoMatchWordCount += wordCount;
                    }
                    break;
                default:
                    break;
            }
        }

        PageWordCounts tpWordCounts = new PageWordCounts();
        tpWordCounts.setContextMatchWordCount(contextMatchWordCount);
        tpWordCounts.setSegmentTmWordCount(segmentTmWordCount);
        tpWordCounts.setMTExtractMatchWordCount(mtExactMatchWordCount);
        tpWordCounts.setXliffExtractMatchWordCount(xliffMatchWordCount);
        tpWordCounts.setPoExactMatchWordCount(poMatchWordCount);
        tpWordCounts.setRepetitionWordCount(repetitionWordCount
                + highFuzzyRepetionWordCount + medHighFuzzyRepetionWordCount
                + medFuzzyRepetitionWordCount + subLevRepetitionWordCount);
        tpWordCounts.setUnmatchedWordCount(noMatchWordCount);
        tpWordCounts.setSubLevMatchWordCount(subLevMatchWordCount);
        tpWordCounts.setSubLevRepetitionWordCount(subLevRepetitionWordCount);
        tpWordCounts.setHiFuzzyRepetitionWordCount(highFuzzyRepetionWordCount);
        tpWordCounts
                .setMedHighFuzzyRepetitionWordCount(medHighFuzzyRepetionWordCount);
        tpWordCounts
                .setMedFuzzyRepetitionWordCount(medFuzzyRepetitionWordCount);
        tpWordCounts.setTotalWordCount(totalWordCount);
        tpWordCounts.setHiFuzzyWordCount(highFuzzyWordCount);
        tpWordCounts.setMedHiFuzzyWordCount(medHighFuzzyWordCount);
        tpWordCounts.setMedFuzzyWordCount(medFuzzyWordCount);
        tpWordCounts.setLowFuzzyWordCount(subLevMatchWordCount);

        tpWordCounts.setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        tpWordCounts.setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        tpWordCounts.setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        tpWordCounts
                .setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        tpWordCounts.setThresholdNoMatchWordCount(thresholdNoMatchWordCount);

        return tpWordCounts;
    }

    /**
     * Update word-counts for
     * "allExactMatchWordCounts(originally 'NoUseExactMatchWordCount')",
     * "Segment-TM word-counts","ICE".
     * 
     * No changed: Context-Match word-counts.
     * 
     * So if use ICE in TM profile: 100% is from SegmentTM, ICE is from ICE;
     * allExactMatchWordCounts = SegmentTM + ICE If use default context matches
     * in TM profile: Context Matches is from ContextMatches, 100% =
     * allExactMatchWordCounts - ContextMatches.
     */
    private static void updateExtraColumnWordCountsForTargetPage(
            TargetPage p_targetPage, ArrayList<BaseTmTuv> p_splittedSourceTuvs,
            MatchTypeStatistics p_matches, Vector<String> p_excludedTuTypes)
    {
        PageWordCounts pageWordCount = p_targetPage.getWordCount();
        // All 100% match word-count
        int totalExactMatchWordCount = pageWordCount.getSegmentTmWordCount()
                + pageWordCount.getContextMatchWordCount()
                + pageWordCount.getMTExtractMatchWordCount()
                + pageWordCount.getXliffExtractMatchWordCount()
                + pageWordCount.getPoExactMatchWordCount();
        pageWordCount.setTotalExactMatchWordCount(totalExactMatchWordCount);
        pageWordCount.setNoUseInContextMatchWordCount(0);

        // Set ICE word-count
        int inContextMatchWordCount = onePageInContextMatchWordCounts(
                pageWordCount, p_splittedSourceTuvs, p_matches,
                p_excludedTuTypes, p_targetPage.getSourcePage().getCompanyId());
        pageWordCount.setInContextWordCount(inContextMatchWordCount);

        // Count "context-match word counts" into "segment-TM word counts"
        // In current implementation,context-match word counts always is 0.
        // Here "segment-TM" word counts means "100%" on UI(excluded ICE only).
        pageWordCount.setSegmentTmWordCount(totalExactMatchWordCount
                - inContextMatchWordCount);

        p_targetPage.setWordCount(pageWordCount);
    }

    /**
     * This method is used to update TU table, and set repeated flag and
     * repetition flag.
     * 
     * @param sTuvs
     * @param p_matches
     * @param p_uniqueSegments
     * @param p_cachedTus
     *            :<Long, TuImpl>:<tuId, TuImpl>.
     */
    @SuppressWarnings("unchecked")
    private static void updateRepetitionInfoToTu(ArrayList<BaseTmTuv> sTuvs,
            MatchTypeStatistics p_matches, Map p_uniqueSegments,
            Map<Long, TuImpl> p_cachedTus, long p_companyId)
    {
        Set<TuImpl> repetitionSet = new HashSet<TuImpl>();
        Set<TuImpl> unRepetitionSet = new HashSet<TuImpl>();
        for (int i = 0; i < sTuvs.size(); i++)
        {
            SegmentTmTuv tuv = (SegmentTmTuv) sTuvs.get(i);
            Types types = p_matches.getTypes(tuv.getId(),
                    ((SegmentTmTu) tuv.getTu()).getSubId());
            int matchType = types == null ? MatchTypeStatistics.NO_MATCH
                    : types.getStatisticsMatchType();
            ArrayList<SegmentTmTuv> identicalSegments = null;

            switch (matchType)
            {
                case MatchTypeStatistics.CONTEXT_EXACT:
                case MatchTypeStatistics.SEGMENT_TM_EXACT:
                case MatchTypeStatistics.SEGMENT_MT_EXACT:
                case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                case MatchTypeStatistics.SEGMENT_PO_EXACT:
                    break;
                case MatchTypeStatistics.LOW_FUZZY:
                case MatchTypeStatistics.MED_FUZZY:
                case MatchTypeStatistics.MED_HI_FUZZY:
                case MatchTypeStatistics.HI_FUZZY:
                case MatchTypeStatistics.NO_MATCH:
                default:
                    long tuId = tuv.getTu().getId();
                    TuImpl tu = null;
                    if (p_cachedTus != null && p_cachedTus.size() > 0)
                    {
                        tu = p_cachedTus.get(tuId);
                    }
                    if (tu == null)
                    {
                        try
                        {
                            tu = SegmentTuUtil.getTuById(tuId,
                                    String.valueOf(p_companyId));
                        }
                        catch (Exception e)
                        {
                            c_logger.error(e.getMessage(), e);
                        }
                    }

                    // WorldServer XLF files are special,
                    // get repeated and repetition information from TU.
                    // Because the TU list is sorted, all repeated TUs are in
                    // front of the list
                    if ("worldserver".equalsIgnoreCase(tu.getGenerateFrom())
                            && "xlf".equalsIgnoreCase(tu.getDataType()))
                    {
                        if ("repeated".equalsIgnoreCase(tu.getSourceContent()))
                        {
                            tu.setRepeated(true);
                            tu.setRepetitionOfId(0);
                            p_uniqueSegments.put(tuv.getExactMatchKey(), tuId);
                            repetitionSet.add(tu);
                        }
                        else if ("repetition".equalsIgnoreCase(tu
                                .getSourceContent()))
                        {
                            long repeatedId = p_uniqueSegments.get(tuv
                                    .getExactMatchKey()) == null ? 0
                                    : (Long) p_uniqueSegments.get(tuv
                                            .getExactMatchKey());
                            /*
                             * Sometimes, World Server XLF files don't have
                             * repeated segments for repetition segments. In
                             * this case, we should treat the first repetition
                             * as repeated.
                             */
                            if (repeatedId == 0)
                            {
                                tu.setRepeated(true);
                                tu.setRepetitionOfId(0);
                                p_uniqueSegments.put(tuv.getExactMatchKey(),
                                        tuId);
                                repetitionSet.add(tu);
                            }
                            else
                            {
                                tu.setRepeated(false);
                                tu.setRepetitionOfId(repeatedId);
                                repetitionSet.add(tu);
                            }
                        }
                        else
                        {
                            // do nothing
                        }
                    }
                    else
                    {
                        identicalSegments = (ArrayList) p_uniqueSegments
                                .get(tuv);
                        /*
                         * If identicalSegments is not null, that means current
                         * TU has a same segment before, then we should get the
                         * former segment, mark it as repeated, and mark the
                         * current segment as repetition.
                         * 
                         * If identicalSegments is null, that means it's the
                         * first time that current segment appears.
                         * 
                         * Considering files can be added and removed from jobs,
                         * all TUs must update.
                         */
                        SegmentTmTuv latestTuv = null;
                        if (identicalSegments != null)
                        {
                            latestTuv = (SegmentTmTuv) identicalSegments.get(0);
                        }
                        if (identicalSegments != null
                                && latestTuv.getExactMatchKey() == tuv
                                        .getExactMatchKey()
                                && isFullSegmentRepitition(tu, tuv, latestTuv,
                                        String.valueOf(p_companyId)))
                        {
                            long repeatedTuId = latestTuv.getTu().getId();
                            TuImpl repeatedTu = null;
                            try
                            {
                                repeatedTu = SegmentTuUtil.getTuById(
                                        repeatedTuId,
                                        String.valueOf(p_companyId));
                            }
                            catch (Exception e)
                            {
                                c_logger.error(e.getMessage(), e);
                            }
                            repeatedTu.setRepeated(true);
                            repeatedTu.setRepetitionOfId(0);
                            // remove repeated TU from unRepetitionSet and
                            // add it to repetitionSet
                            repetitionSet.add(repeatedTu);
                            unRepetitionSet.remove(repeatedTu);
                            tu.setRepetitionOfId(repeatedTuId);
                            tu.setRepeated(false);
                            repetitionSet.add(tu);
                        }
                        else
                        {
                            if (!repetitionSet.contains(tu))
                            {
                                identicalSegments = new ArrayList<SegmentTmTuv>();
                                p_uniqueSegments.put(tuv, identicalSegments);
                                identicalSegments.add(tuv);
                                // add the TU to unRepetitionSet
                                tu.setRepetitionOfId(0);
                                tu.setRepeated(false);
                                unRepetitionSet.add(tu);
                            }
                        }
                    }
                    break;
            }
        }

        try
        {
            if (unRepetitionSet.size() != 0)
            {
                SegmentTuUtil.updateTus(unRepetitionSet, p_companyId);
            }
            if (repetitionSet.size() != 0)
            {
                SegmentTuUtil.updateTus(repetitionSet, p_companyId);
            }
        }
        catch (Exception e)
        {
            c_logger.error(e);
        }
    }

    /**
     * Compare if current TUV has the same exact match key (white space ignored)
     * with that from latest TUV.
     * 
     * Only when the full segment (including sub segments) has the same
     * exactMatchKey, take it as repetition.
     */
    private static boolean isFullSegmentRepitition(TuImpl currentTu,
            SegmentTmTuv currentTuv, SegmentTmTuv latestTuv, String companyId)
    {
        // If current TUV has no sub segments, no need continue to check, return
        // true;
        Tuv tuv = currentTu.getTuv(currentTuv.getLocale().getId(), companyId);
        List subEle = tuv.getSubflowsAsGxmlElements();
        if (subEle == null || subEle.size() == 0)
        {
            return true;
        }

        long currentTuvExactMatchKey = tuv.getExactMatchKey();

        long repeatedTuId = latestTuv.getTu().getId();
        TuImpl repeatedTu = null;
        try
        {
            repeatedTu = SegmentTuUtil.getTuById(repeatedTuId, companyId);
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
        long latestTuvExactMatchkey = repeatedTu.getTuv(
                latestTuv.getLocale().getId(), companyId).getExactMatchKey();

        return (currentTuvExactMatchKey == latestTuvExactMatchkey);
    }

    /**
     * Calculates the statistics for the workflows passed in. Adds the word
     * count to the workflows and commits to the database.
     * 
     * @param p_workflows
     *            - a List of workflows of which the statistics are calculated.
     *            All workflows must belong to the same job.
     */
    static public void calculateWorkflowStatistics(List<Workflow> p_workflows,
            Vector<String> p_excludedTuTypes) throws StatisticsException
    {
        if (p_workflows == null || p_workflows.size() < 1)
        {
            return;
        }

        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // calculate statistics per work flow
            for (Workflow wf : p_workflows)
            {
                wf = (WorkflowImpl) session.get(WorkflowImpl.class,
                        wf.getIdAsLong());

                ArrayList<BaseTmTuv> splitSourceTuvs = getAllSplitSourceTuvs(wf);
                ArrayList<BaseTmTuv> wsSplitSourceTuvs = getAllWorldServerSplitSourceTuvs(wf);
                // get the job's leverage match threshold
                SegmentRepetition segmentRepetition = new SegmentRepetition(
                        splitSourceTuvs);
                SegmentRepetition wsSegmentRepetition = new SegmentRepetition(
                        wsSplitSourceTuvs);

                // get match types for all target segments in a workflow
                int levMatchThreshold = wf.getJob().getLeverageMatchThreshold();
                MatchTypeStatistics matches = getMatchTypeStatistics(wf,
                        levMatchThreshold);

                PageWordCounts commonWfWordCount = calculateWorkflowWordCounts(
                        segmentRepetition, matches, p_excludedTuTypes);
                PageWordCounts worldServerWordCount = calculateWorldServerWorkflowWordCounts(
                        wsSegmentRepetition, matches, p_excludedTuTypes);

                updateExtraColumnWordCountsForWorkflow(commonWfWordCount,
                        worldServerWordCount, wf.getAllTargetPages());
                PageWordCounts wordCount = mergeWordCounts(commonWfWordCount,
                        worldServerWordCount);

                wf.setInContextMatchWordCount(wordCount.getInContextWordCount());
                wf.setNoUseInContextMatchWordCount(wordCount
                        .getNoUseInContextMatchWordCount());
                wf.setTotalExactMatchWordCount(wordCount
                        .getTotalExactMatchWordCount());
                wf.setContextMatchWordCount(wordCount
                        .getContextMatchWordCount());
                wf.setSegmentTmWordCount(wordCount.getSegmentTmWordCount());
                wf.setSubLevMatchWordCount(wordCount.getSubLevMatchWordCount());
                wf.setSubLevRepetitionWordCount(wordCount
                        .getSubLevRepetitionWordCount());
                wf.setLowFuzzyMatchWordCount(wordCount.getLowFuzzyWordCount());
                wf.setMedFuzzyMatchWordCount(wordCount.getMedFuzzyWordCount());
                wf.setMedHiFuzzyMatchWordCount(wordCount
                        .getMedHiFuzzyWordCount());
                wf.setHiFuzzyMatchWordCount(wordCount.getHiFuzzyWordCount());
                wf.setRepetitionWordCount(wordCount.getRepetitionWordCount());
                wf.setNoMatchWordCount(wordCount.getUnmatchedWordCount());
                wf.setTotalWordCount(wordCount.getTotalWordCount());

                wf.setHiFuzzyRepetitionWordCount(wordCount
                        .getHiFuzzyRepetitionWordCount());
                wf.setMedHiFuzzyRepetitionWordCount(wordCount
                        .getMedHighFuzzyRepetitionWordCount());
                wf.setMedFuzzyRepetitionWordCount(wordCount
                        .getMedFuzzyRepetitionWordCount());

                wf.setThresholdHiFuzzyWordCount(wordCount
                        .getThresholdHiFuzzyWordCount());
                wf.setThresholdLowFuzzyWordCount(wordCount
                        .getThresholdLowFuzzyWordCount());
                wf.setThresholdMedFuzzyWordCount(wordCount
                        .getThresholdMedFuzzyWordCount());
                wf.setThresholdMedHiFuzzyWordCount(wordCount
                        .getThresholdMedHiFuzzyWordCount());
                wf.setThresholdNoMatchWordCount(wordCount
                        .getThresholdNoMatchWordCount());

                session.update(wf);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            String[] args = new String[1];
            Workflow wf = (Workflow) p_workflows.get(0);
            String jobName = wf.getJob().getJobName();
            args[0] = jobName;

            throw new StatisticsException(
                    StatisticsException.MSG_FAILED_TO_GENERATE_STATISTICS_WORKFLOW,
                    args, e);
        }
    }

    /**
     * Calculates the workflow word counts from the match types and source TUVs
     * passed in. The word count calculation for a workflow is done differently
     * from a target page since a workflow could have more than one target page
     * and passing the Tuvs to the SegmentRepetition could result in have
     * multiple repetitions from different pages. This will cause the HashMap of
     * unique segments to be different from when each page was calculated
     * separately and therefore the end result will be very different from
     * adding up the total of each word count category of a list of target pages
     * that were processed separately.
     * 
     * @param p_sourceTuvs
     *            Collection of subflow separated source Tuvs. Type of elements
     *            is SegmentTmTuv. @param p_matches MatchTypeStatistics object
     */
    static private PageWordCounts calculateWorkflowWordCounts(
            SegmentRepetition p_segmentRepetition,
            MatchTypeStatistics p_matches, Vector<String> p_excludedTuTypes)
    {
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;

        int subLevMatchWordCount = 0;
        int subLevMatchRepWordCount = 0;
        // subLevMatchWordCount+subLevMatchRepWordCount
        // int lowFuzzyWordCount = 0;

        int medFuzzyWordCount = 0;
        int medFuzzyRepetitionWordCount = 0;

        int medHiFuzzyWordCount = 0;
        int medHiFuzzyRepetitionWordCount = 0;

        int hiFuzzyWordCount = 0;
        int hiFuzzyRepetitionWordCount = 0;

        int unmatchedWordCount = 0;
        int repetitionWordCount = 0;
        int mtExactMatchWordCount = 0;
        int xliffMatchWordCount = 0;
        int poMatchWordCount = 0;

        int thresholdHiFuzzyWordCount = 0;
        int thresholdMedHiFuzzyWordCount = 0;
        int thresholdMedFuzzyWordCount = 0;
        int thresholdLowFuzzyWordCount = 0;
        int thresholdNoMatchWordCount = 0;

        // go through all unique segments in p_sourceTuvs
        for (Iterator si = p_segmentRepetition.iterator(); si.hasNext();)
        {
            SegmentTmTuv key = (SegmentTmTuv) si.next();
            List tuvs = p_segmentRepetition.getIdenticalSegments(key);
            int size = tuvs == null ? 0 : tuvs.size();

            int lowFuzzyCounter1 = 0;
            int lowFuzzyCounter2 = 0;
            int medFuzzyCounter1 = 0;
            int medFuzzyCounter2 = 0;
            int medHighFuzzyCounter1 = 0;
            int medHighFuzzyCounter2 = 0;
            int highFuzzyCounter1 = 0;
            int highFuzzyCounter2 = 0;
            int noMatchCounter1 = 0;
            int noMatchCounter2 = 0;
            for (int i = 0; i < size; i++)
            {
                SegmentTmTuv source = (SegmentTmTuv) tuvs.get(i);
                int wordCount = source.getWordCount();

                // Don't count excluded items.
                if (p_excludedTuTypes != null
                        && p_excludedTuTypes.contains(source.getTu().getType()))
                {
                    wordCount = 0;
                }

                Types types = p_matches.getTypes(source.getId(),
                        ((SegmentTmTu) source.getTu()).getSubId());

                int statisticsMatchType = types == null ? MatchTypeStatistics.NO_MATCH
                        : types.getStatisticsMatchType();
                // increment the word count according to the match type
                switch (statisticsMatchType)
                {
                    case MatchTypeStatistics.CONTEXT_EXACT:
                        contextMatchWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.SEGMENT_TM_EXACT:
                        segmentTmWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.SEGMENT_MT_EXACT:
                        mtExactMatchWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                        xliffMatchWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.SEGMENT_PO_EXACT:
                        poMatchWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.LOW_FUZZY:
                        if (lowFuzzyCounter1 < 1)
                        {
                            subLevMatchWordCount += wordCount;
                        }
                        else
                        {
                            subLevMatchRepWordCount += wordCount;
                        }
                        lowFuzzyCounter1++;
                        break;
                    case MatchTypeStatistics.MED_FUZZY:
                        if (medFuzzyCounter1 < 1)
                        {
                            medFuzzyWordCount += wordCount;
                        }
                        else
                        {
                            medFuzzyRepetitionWordCount += wordCount;
                        }
                        medFuzzyCounter1++;
                        break;
                    case MatchTypeStatistics.MED_HI_FUZZY:
                        if (medHighFuzzyCounter1 < 1)
                        {
                            medHiFuzzyWordCount += wordCount;
                        }
                        else
                        {
                            medHiFuzzyRepetitionWordCount += wordCount;
                        }
                        medHighFuzzyCounter1++;
                        break;
                    case MatchTypeStatistics.HI_FUZZY:
                        if (highFuzzyCounter1 < 1)
                        {
                            hiFuzzyWordCount += wordCount;
                        }
                        else
                        {
                            hiFuzzyRepetitionWordCount += wordCount;
                        }
                        highFuzzyCounter1++;
                        break;
                    case LeverageMatchLingManager.NO_MATCH:
                    default:
                        if (noMatchCounter1 < 1)
                        {
                            unmatchedWordCount += wordCount;
                        }
                        else
                        {
                            repetitionWordCount += wordCount;
                        }
                        noMatchCounter1++;
                        break;
                }

                /*
                 * This part is used to calculate the word counts relative to
                 * threshold, they
                 * are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount
                 * ,thresholdMedFuzzyWordCount,
                 * thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
                 */
                int statisticsMatchTypeByThreshold = MatchTypeStatistics.THRESHOLD_NO_MATCH;
                if (types != null)
                {
                    statisticsMatchTypeByThreshold = types
                            .getStatisticsMatchTypeByThreshold();
                }
                switch (statisticsMatchTypeByThreshold)
                {
                    case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                        if (highFuzzyCounter2 < 1)
                        {
                            thresholdHiFuzzyWordCount += wordCount;
                        }
                        highFuzzyCounter2++;
                        break;
                    case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                        if (medHighFuzzyCounter2 < 1)
                        {
                            thresholdMedHiFuzzyWordCount += wordCount;
                        }
                        medHighFuzzyCounter2++;
                        break;
                    case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                        if (medFuzzyCounter2 < 1)
                        {
                            thresholdMedFuzzyWordCount += wordCount;
                        }
                        medFuzzyCounter2++;
                        break;
                    case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                        if (lowFuzzyCounter2 < 1)
                        {
                            thresholdLowFuzzyWordCount += wordCount;
                        }
                        lowFuzzyCounter2++;
                        break;
                    case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                        if (noMatchCounter2 < 1)
                        {
                            thresholdNoMatchWordCount += wordCount;
                        }
                        noMatchCounter2++;
                        break;
                    default:
                        break;
                }
            }
        }

        PageWordCounts result = new PageWordCounts();
        result.setSubLevMatchWordCount(subLevMatchWordCount);
        result.setSubLevRepetitionWordCount(subLevMatchRepWordCount);
        result.setContextMatchWordCount(contextMatchWordCount);
        result.setSegmentTmWordCount(segmentTmWordCount);
        result.setUnmatchedWordCount(unmatchedWordCount);
        result.setLowFuzzyWordCount(subLevMatchWordCount
                + subLevMatchRepWordCount);
        result.setMedFuzzyWordCount(medFuzzyWordCount);
        result.setMedFuzzyRepetitionWordCount(medFuzzyRepetitionWordCount);
        result.setMedHiFuzzyWordCount(medHiFuzzyWordCount);
        result.setMedHighFuzzyRepetitionWordCount(medHiFuzzyRepetitionWordCount);
        result.setHiFuzzyWordCount(hiFuzzyWordCount);
        result.setHiFuzzyRepetitionWordCount(hiFuzzyRepetitionWordCount);
        result.setRepetitionWordCount(repetitionWordCount);
        result.setMTExtractMatchWordCount(mtExactMatchWordCount);
        result.setXliffExtractMatchWordCount(xliffMatchWordCount);
        result.setPoExactMatchWordCount(poMatchWordCount);

        result.setTotalWordCount(contextMatchWordCount + segmentTmWordCount
                + mtExactMatchWordCount + xliffMatchWordCount
                + poMatchWordCount + subLevMatchWordCount
                + subLevMatchRepWordCount + medFuzzyWordCount
                + medHiFuzzyWordCount + hiFuzzyWordCount + unmatchedWordCount
                + repetitionWordCount + hiFuzzyRepetitionWordCount
                + medHiFuzzyRepetitionWordCount + medFuzzyRepetitionWordCount);

        result.setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        result.setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        result.setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        result.setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        result.setThresholdNoMatchWordCount(thresholdNoMatchWordCount);

        return result;
    }

    /**
     * Calculates the workflow word counts from the match types and source TUVs
     * passed in. The word count calculation for a workflow is done differently
     * from a target page since a workflow could have more than one target page
     * and passing the Tuvs to the SegmentRepetition could result in have
     * multiple repetitions from different pages. This will cause the HashMap of
     * unique segments to be different from when each page was calculated
     * separately and therefore the end result will be very different from
     * adding up the total of each word count category of a list of target pages
     * that were processed separately.
     * 
     * @param p_sourceTuvs
     *            Collection of subflow separated source Tuvs. Type of elements
     *            is SegmentTmTuv. @param p_matches MatchTypeStatistics object
     */
    private static PageWordCounts calculateWorldServerWorkflowWordCounts(
            SegmentRepetition p_segmentRepetition,
            MatchTypeStatistics p_matches, Vector<String> p_excludedTuTypes)
    {
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;

        int subLevMatchWordCount = 0;
        int subLevMatchRepWordCount = 0;

        int medFuzzyWordCount = 0;
        int medFuzzyRepetitionWordCount = 0;

        int medHiFuzzyWordCount = 0;
        int medHiFuzzyRepetitionWordCount = 0;

        int hiFuzzyWordCount = 0;
        int hiFuzzyRepetitionWordCount = 0;

        int unmatchedWordCount = 0;
        int repetitionWordCount = 0;
        int mtExactMatchWordCount = 0;
        int xliffMatchWordCount = 0;
        int poMatchWordCount = 0;

        int thresholdHiFuzzyWordCount = 0;
        int thresholdMedHiFuzzyWordCount = 0;
        int thresholdMedFuzzyWordCount = 0;
        int thresholdLowFuzzyWordCount = 0;
        int thresholdNoMatchWordCount = 0;

        for (Iterator si = p_segmentRepetition.iterator(); si.hasNext();)
        {
            SegmentTmTuv key = (SegmentTmTuv) si.next();
            List tuvs = p_segmentRepetition.getIdenticalSegments(key);
            int size = tuvs == null ? 0 : tuvs.size();

            for (int i = 0; i < size; i++)
            {
                SegmentTmTuv tmTuv = (SegmentTmTuv) tuvs.get(i);
                int wordCount = tmTuv.getWordCount();

                // Don't count excluded items.
                if (p_excludedTuTypes != null
                        && p_excludedTuTypes.contains(tmTuv.getTu().getType()))
                {
                    wordCount = 0;
                }

                Types types = p_matches.getTypes(tmTuv.getId(),
                        ((SegmentTmTu) tmTuv.getTu()).getSubId());

                int matchType = types == null ? MatchTypeStatistics.NO_MATCH
                        : types.getStatisticsMatchType();
                String sourceContent = tmTuv.getTu().getSourceContent();

                switch (matchType)
                {
                    case MatchTypeStatistics.CONTEXT_EXACT:
                        contextMatchWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.SEGMENT_TM_EXACT:
                        segmentTmWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.SEGMENT_MT_EXACT:
                        mtExactMatchWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                        xliffMatchWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.SEGMENT_PO_EXACT:
                        poMatchWordCount += wordCount;
                        break;
                    case MatchTypeStatistics.LOW_FUZZY:
                        if (sourceContent != null
                                && sourceContent.equals("repetition"))
                        {
                            subLevMatchRepWordCount += wordCount;
                        }
                        else
                        {
                            subLevMatchWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.MED_FUZZY:
                        if (sourceContent != null
                                && sourceContent.equals("repetition"))
                        {
                            medFuzzyRepetitionWordCount += wordCount;
                        }
                        else
                        {
                            medFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.MED_HI_FUZZY:
                        if (sourceContent != null
                                && sourceContent.equals("repetition"))
                        {
                            medHiFuzzyRepetitionWordCount += wordCount;
                        }
                        else
                        {
                            medHiFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.HI_FUZZY:
                        if (sourceContent != null
                                && sourceContent.equals("repetition"))
                        {
                            hiFuzzyRepetitionWordCount += wordCount;
                        }
                        else
                        {
                            hiFuzzyWordCount += wordCount;
                        }
                        break;
                    case LeverageMatchLingManager.NO_MATCH:
                    default:
                        // no-match is counted only once and the rest are
                        // repetitions
                        if (sourceContent != null
                                && sourceContent.equals("repetition"))
                        {
                            repetitionWordCount += wordCount;
                        }
                        else
                        {
                            unmatchedWordCount += wordCount;
                        }
                        break;
                }

                /*
                 * This part is used to calculate the word counts relative to
                 * threshold, they
                 * are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount
                 * ,thresholdMedFuzzyWordCount,
                 * thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
                 */
                int statisticsMatchTypeByThreshold = MatchTypeStatistics.THRESHOLD_NO_MATCH;
                if (types != null)
                {
                    statisticsMatchTypeByThreshold = types
                            .getStatisticsMatchTypeByThreshold();
                }
                switch (statisticsMatchTypeByThreshold)
                {
                    case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                        if (sourceContent == null
                                || !sourceContent.equals("repetition"))
                        {
                            thresholdHiFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                        if (sourceContent == null
                                || !sourceContent.equals("repetition"))
                        {
                            thresholdMedHiFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                        if (sourceContent == null
                                || !sourceContent.equals("repetition"))
                        {
                            thresholdMedFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                        if (sourceContent == null
                                || !sourceContent.equals("repetition"))
                        {
                            thresholdLowFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                        if (sourceContent == null
                                || !sourceContent.equals("repetition"))
                        {
                            thresholdNoMatchWordCount += wordCount;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        PageWordCounts result = new PageWordCounts();
        result.setSubLevMatchWordCount(subLevMatchWordCount);
        result.setSubLevRepetitionWordCount(subLevMatchRepWordCount);
        result.setContextMatchWordCount(contextMatchWordCount);
        result.setSegmentTmWordCount(segmentTmWordCount);
        result.setUnmatchedWordCount(unmatchedWordCount);
        result.setLowFuzzyWordCount(subLevMatchWordCount
                + subLevMatchRepWordCount);
        result.setMedFuzzyWordCount(medFuzzyWordCount);
        result.setMedFuzzyRepetitionWordCount(medFuzzyRepetitionWordCount);
        result.setMedHiFuzzyWordCount(medHiFuzzyWordCount);
        result.setMedHighFuzzyRepetitionWordCount(medHiFuzzyRepetitionWordCount);
        result.setHiFuzzyWordCount(hiFuzzyWordCount);
        result.setHiFuzzyRepetitionWordCount(hiFuzzyRepetitionWordCount);
        result.setRepetitionWordCount(repetitionWordCount);
        result.setMTExtractMatchWordCount(mtExactMatchWordCount);
        result.setXliffExtractMatchWordCount(xliffMatchWordCount);
        result.setPoExactMatchWordCount(poMatchWordCount);

        result.setTotalWordCount(contextMatchWordCount + segmentTmWordCount
                + mtExactMatchWordCount + xliffMatchWordCount
                + poMatchWordCount + subLevMatchWordCount
                + subLevMatchRepWordCount + medFuzzyWordCount
                + medHiFuzzyWordCount + hiFuzzyWordCount + unmatchedWordCount
                + repetitionWordCount + hiFuzzyRepetitionWordCount
                + medHiFuzzyRepetitionWordCount + medFuzzyRepetitionWordCount);

        result.setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        result.setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        result.setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        result.setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        result.setThresholdNoMatchWordCount(thresholdNoMatchWordCount);

        return result;
    }

    /**
     * Update extra columns' word-counts for workflow. They are:
     * totalExactMatchWordCounts(originally "NoUseExactMatchWordCounts",
     * inContextWordCount segmentTmWordCount defaultContextMatchWordCount
     */
    private static void updateExtraColumnWordCountsForWorkflow(
            PageWordCounts wordCount, PageWordCounts worldServerWordCount,
            Vector<TargetPage> targetPages)
    {
        int inContextWordCount = 0;
        int defaultContextMatchWordCount = 0;
        int all100MatchWordCount = 0;

        int worldServerInContextWordCount = 0;
        int worldServerDefaultContextMatchWordCount = 0;
        int worldServerAll100WordCount = 0;

        try
        {
            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage tp = (TargetPage) targetPages.get(i);

                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(
                                tp.getSourcePage().getIdAsLong());
                if (isWSXlfSourceFile)
                {
                    worldServerInContextWordCount += tp.getWordCount()
                            .getInContextWordCount();
                    worldServerDefaultContextMatchWordCount += tp
                            .getWordCount().getContextMatchWordCount();
                    worldServerAll100WordCount += tp.getWordCount()
                            .getTotalExactMatchWordCount();
                }
                else
                {
                    inContextWordCount += tp.getWordCount()
                            .getInContextWordCount();
                    defaultContextMatchWordCount += tp.getWordCount()
                            .getContextMatchWordCount();
                    all100MatchWordCount += tp.getWordCount()
                            .getTotalExactMatchWordCount();
                }
            }

            wordCount.setInContextWordCount(inContextWordCount);
            wordCount.setContextMatchWordCount(defaultContextMatchWordCount);
            wordCount.setTotalExactMatchWordCount(all100MatchWordCount);
            wordCount.setSegmentTmWordCount(all100MatchWordCount
                    - wordCount.getInContextWordCount());

            worldServerWordCount
                    .setInContextWordCount(worldServerInContextWordCount);
            worldServerWordCount
                    .setContextMatchWordCount(worldServerDefaultContextMatchWordCount);
            worldServerWordCount
                    .setTotalExactMatchWordCount(worldServerAll100WordCount);
            worldServerWordCount
                    .setSegmentTmWordCount(worldServerAll100WordCount
                            - worldServerWordCount.getInContextWordCount());
        }
        catch (Exception e)
        {
            c_logger.error("Query worldserver files error", e);
        }
    }

    /**
     * Merge common wordcounts and worldserver xlf wordcounts together
     * 
     * @param commonWordcounts
     * @param worldserverWordcounts
     * @return
     */
    private static PageWordCounts mergeWordCounts(PageWordCounts wc,
            PageWordCounts wwc)
    {
        PageWordCounts result = new PageWordCounts();
        result.setContextMatchWordCount(wc.getContextMatchWordCount()
                + wwc.getContextMatchWordCount());
        result.setHiFuzzyRepetitionWordCount(wc.getHiFuzzyRepetitionWordCount()
                + wwc.getHiFuzzyRepetitionWordCount());
        result.setHiFuzzyWordCount(wc.getHiFuzzyWordCount()
                + wwc.getHiFuzzyWordCount());
        result.setInContextWordCount(wc.getInContextWordCount()
                + wwc.getInContextWordCount());
        result.setLowFuzzyWordCount(wc.getLowFuzzyWordCount()
                + wwc.getLowFuzzyWordCount());
        result.setMedFuzzyRepetitionWordCount(wc
                .getMedFuzzyRepetitionWordCount()
                + wwc.getMedFuzzyRepetitionWordCount());
        result.setMedFuzzyWordCount(wc.getMedFuzzyWordCount()
                + wwc.getMedFuzzyWordCount());
        result.setMedHiFuzzyWordCount(wc.getMedHiFuzzyWordCount()
                + wwc.getMedHiFuzzyWordCount());
        result.setMedHighFuzzyRepetitionWordCount(wc
                .getMedHighFuzzyRepetitionWordCount()
                + wwc.getMedHighFuzzyRepetitionWordCount());
        result.setMTExtractMatchWordCount(wc.getMTExtractMatchWordCount()
                + wwc.getMTExtractMatchWordCount());
        result.setTotalExactMatchWordCount(wc.getTotalExactMatchWordCount()
                + wwc.getTotalExactMatchWordCount());
        result.setNoUseInContextMatchWordCount(wc
                .getNoUseInContextMatchWordCount()
                + wwc.getNoUseInContextMatchWordCount());
        result.setPoExactMatchWordCount(wc.getPoExactMatchWordCount()
                + wwc.getPoExactMatchWordCount());
        result.setRepetitionWordCount(wc.getRepetitionWordCount()
                + wwc.getRepetitionWordCount());
        result.setSegmentTmWordCount(wc.getSegmentTmWordCount()
                + wwc.getSegmentTmWordCount());
        result.setSubLevMatchWordCount(wc.getSubLevMatchWordCount()
                + wwc.getSubLevMatchWordCount());
        result.setSubLevRepetitionWordCount(wc.getSubLevRepetitionWordCount()
                + wwc.getSubLevRepetitionWordCount());
        result.setThresholdHiFuzzyWordCount(wc.getThresholdHiFuzzyWordCount()
                + wwc.getThresholdHiFuzzyWordCount());
        result.setThresholdLowFuzzyWordCount(wc.getThresholdLowFuzzyWordCount()
                + wwc.getThresholdLowFuzzyWordCount());
        result.setThresholdMedFuzzyWordCount(wc.getThresholdMedFuzzyWordCount()
                + wwc.getThresholdMedFuzzyWordCount());
        result.setThresholdMedHiFuzzyWordCount(wc
                .getThresholdMedHiFuzzyWordCount()
                + wwc.getThresholdMedHiFuzzyWordCount());
        result.setThresholdNoMatchWordCount(wc.getThresholdNoMatchWordCount()
                + wwc.getThresholdNoMatchWordCount());
        result.setTotalWordCount(wc.getTotalWordCount()
                + wwc.getTotalWordCount());
        result.setUnmatchedWordCount(wc.getUnmatchedWordCount()
                + wwc.getUnmatchedWordCount());
        result.setXliffExtractMatchWordCount(wc.getXliffExtractMatchWordCount()
                + wwc.getXliffExtractMatchWordCount());
        return result;
    }

    /**
     * Compute the in context match in one page
     * 
     * @param wordCount
     *            The page word count
     * @param splitSourceTuvs
     *            The source tuvs in one workflow
     * @param matches
     *            The tm matches in the TM
     * @return The in context match
     */
    private static int onePageInContextMatchWordCounts(
            PageWordCounts wordCount, ArrayList splitSourceTuvs,
            MatchTypeStatistics matches, Vector<String> p_excludedTuTypes,
            String companyId)
    {
        int inContextMatchWordCount = 0;

        for (int i = 0, max = splitSourceTuvs.size(); i < max; i++)
        {
            if (LeverageUtil.isIncontextMatch(i, splitSourceTuvs, null,
                    matches, p_excludedTuTypes, companyId))
            {
                inContextMatchWordCount += ((SegmentTmTuv) splitSourceTuvs
                        .get(i)).getWordCount();
            }
        }

        return inContextMatchWordCount;
    }

    /**
     * Takes a collection of original source Tuvs (type Tuv) and splits
     * subflows. Returns a collection of SegmentTmTuv.
     */
    @SuppressWarnings("unchecked")
    static public ArrayList<BaseTmTuv> splitSourceTuvs(
            ArrayList<Tuv> p_sourceTuvs, GlobalSightLocale p_sourceLocale,
            String companyId) throws Exception
    {
        // sort the list first, put all tuvs whose source content equals
        // "repeated" in front of the list. it will affect worldserver xlf
        // files, other files will not be impacted.
        Collections.sort(p_sourceTuvs,
                new TuvSourceContentComparator(companyId));
        // convert Tu, Tuv to PageTmTu, PageTmTuv to split segments
        ArrayList<PageTmTu> pageTmTuList = new ArrayList<PageTmTu>(
                p_sourceTuvs.size());

        for (int i = 0; i < p_sourceTuvs.size(); i++)
        {
            Tuv originalTuv = (Tuv) p_sourceTuvs.get(i);
            Tu originalTu = originalTuv.getTu(companyId);

            PageTmTu pageTmTu = new PageTmTu(originalTu.getId(), 0,
                    originalTu.getDataType(), originalTu.getTuType(),
                    !originalTu.isLocalizable());
            pageTmTu.setSourceContent(originalTu.getSourceContent());
            PageTmTuv pageTmTuv = new PageTmTuv(originalTuv.getId(),
                    originalTuv.getGxml(), p_sourceLocale);
            pageTmTuv.setSid(originalTuv.getSid());

            pageTmTu.addTuv(pageTmTuv);

            pageTmTuList.add(pageTmTu);
        }
        // make a list of splitted segment Tus
        ArrayList<SegmentTmTu> splittedTus = new ArrayList<SegmentTmTu>(
                pageTmTuList.size());

        for (PageTmTu pageTmTu : pageTmTuList)
        {
            Collection<SegmentTmTu> segmentTmTus = TmUtil.createSegmentTmTus(
                    pageTmTu, p_sourceLocale);
            splittedTus.addAll(segmentTmTus);
        }

        // make a list of SegmentTmTuv from a list of SegmentTmTu
        ArrayList<BaseTmTuv> splittedTuvs = new ArrayList<BaseTmTuv>(
                splittedTus.size());
        for (SegmentTmTu tu : splittedTus)
        {
            splittedTuvs.add(tu.getFirstTuv(p_sourceLocale));
        }

        return splittedTuvs;
    }

    /**
     * Split segments from workflow
     * 
     * @param p_workflow
     * @return
     * @throws Exception
     */
    static private ArrayList<BaseTmTuv> getAllSplitSourceTuvs(
            Workflow p_workflow) throws Exception
    {
        ArrayList<BaseTmTuv> splitTuvs = new ArrayList<BaseTmTuv>();
        List<TargetPage> targetPages = p_workflow.getAllTargetPages();

        for (TargetPage targetPage : targetPages)
        {
            SourcePage sourcePage = targetPage.getSourcePage();
            if (sourcePage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
            {
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(sourcePage.getIdAsLong());
                if (!isWSXlfSourceFile)
                {
                    // As here don't need XliffAlt data, don't load them to
                    // improve performance.
                    boolean needLoadExtraInfo = false;
                    ArrayList<Tuv> sTuvs = SegmentTuvUtil.getSourceTuvs(
                            sourcePage, needLoadExtraInfo);
                    splitTuvs.addAll(splitSourceTuvs(sTuvs,
                            sourcePage.getGlobalSightLocale(),
                            p_workflow.getCompanyId()));
                }
            }
        }

        return splitTuvs;
    }

    /**
     * Split segments from workflow, used for worldserver xlf files
     * 
     * @param p_workflow
     * @return
     * @throws Exception
     */
    static private ArrayList<BaseTmTuv> getAllWorldServerSplitSourceTuvs(
            Workflow p_workflow) throws Exception
    {
        ArrayList<BaseTmTuv> wsSplitTuvs = new ArrayList<BaseTmTuv>();
        List<TargetPage> targetPages = p_workflow.getAllTargetPages();

        for (TargetPage targetPage : targetPages)
        {
            SourcePage sourcePage = targetPage.getSourcePage();

            if (sourcePage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
            {
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(sourcePage.getIdAsLong());
                if (isWSXlfSourceFile)
                {
                    // As here don't need XliffAlt data, don't load them to
                    // improve performance.
                    boolean needLoadExtraInfo = false;
                    ArrayList<Tuv> sTuvs = SegmentTuvUtil.getSourceTuvs(
                            sourcePage, needLoadExtraInfo);
                    wsSplitTuvs.addAll(splitSourceTuvs(sTuvs,
                            sourcePage.getGlobalSightLocale(),
                            p_workflow.getCompanyId()));
                }
            }
        }

        return wsSplitTuvs;
    }

    static private MatchTypeStatistics getMatchTypeStatistics(
            Workflow p_workflow, int p_levMatchThreshold) throws Exception
    {
        MatchTypeStatistics result = new MatchTypeStatistics(
                p_levMatchThreshold);

        Long targetLocaleId = p_workflow.getTargetLocale().getIdAsLong();

        // get match types for all target segments in a workflow
        List<TargetPage> targetPages = p_workflow.getAllTargetPages();
        for (TargetPage targetPage : targetPages)
        {
            if (targetPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
            {
                // If the source file is WorldServer xliff file,MT translations
                // should NOT impact the word-count statistics.
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(
                                targetPage.getSourcePage().getIdAsLong());
                LeverageMatchLingManager lmLingManager = getLeverageMatchLingManager();
                if (isWSXlfSourceFile)
                {
                    lmLingManager.setIncludeMtMatches(false);
                }
                MatchTypeStatistics pageMatches = lmLingManager
                        .getMatchTypesForStatistics(targetPage.getSourcePage()
                                .getIdAsLong(), targetLocaleId,
                                p_levMatchThreshold);
                result.merge(pageMatches);
            }
        }
        return result;
    }

    static private LeverageMatchLingManager getLeverageMatchLingManager()
            throws StatisticsException
    {
        LeverageMatchLingManager result = null;

        try
        {
            result = LingServerProxy.getLeverageMatchLingManager();
        }
        catch (Exception e)
        {
            c_logger.error("Couldn't find the LeverageMatchLingManager", e);

            throw new StatisticsException(
                    StatisticsException.MSG_FAILED_TO_FIND_LEVERAGE_MATCH_LING_MANAGER,
                    null, e);
        }

        return result;
    }

    private static boolean isDefaultContextMatch(String sourcePageId,
            SourcePage page)
    {
        String localeStr = sourcePageId.substring(0,
                sourcePageId.indexOf(File.separator));

        String temp1 = sourcePageId.substring(localeStr.length()
                + File.separator.length());

        String isWebservice = temp1.substring(0, temp1.indexOf(File.separator))
                + "";

        if (isWebservice.indexOf("webservice") > -1)
        {
            temp1 = sourcePageId.substring(localeStr.length()
                    + File.separator.length() + "webservice".length()
                    + File.separator.length());
        }

        String temp2 = temp1.substring(temp1.indexOf(File.separator));
        String queryStr = localeStr + "%" + temp2;

        if (!File.separator.equals("/"))
        {
            queryStr = queryStr.replace("\\", "\\\\\\\\");
        }
        queryStr = queryStr.replace("'", "\\'");
        String sql = "select id from source_page where external_page_id LIKE '"
                + queryStr + "' and state not in ('OUT_OF_DATE','IMPORT_FAIL')";
        List list = HibernateUtil.searchWithSql(sql, null);
        list = removeCurrent(list, page.getId());
        for (int i = 0; i < list.size(); i++)
        {
            SourcePage sp = null;
            // All TUs and TUVs of the other source page will be loaded by
            // isSameOfSourcePage, bloating the Hibernate (first-level) cache.
            // Load them in a new session so we can free them by closing the
            // session after isSameOfSourcePage finishes.
            Session newSession = HibernateUtil.getSessionFactory()
                    .openSession();
            try
            {
                long otherSourcePageId = ((BigInteger) list.get(i)).longValue();
                sp = (SourcePage) newSession.get(SourcePage.class,
                        otherSourcePageId);
                if (isSameOfSourcePage(page, sp))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                c_logger.info("Can not get source page to compare" + e);
            }
            finally
            {
                newSession.close();
            }
        }
        return false;
    }

    private static List removeCurrent(List list, long id)
    {
        if (list == null || list.size() == 0)
        {
            return new ArrayList();
        }
        for (int i = 0; i < list.size(); i++)
        {
            long idInList = ((BigInteger) list.get(i)).longValue();
            if (id == idInList)
            {
                list.remove(i);
            }
        }
        return list;
    }

    private static boolean isSameOfSourcePage(SourcePage source,
            SourcePage target)
    {
        boolean flag = false;
        if (source == null || target == null)
        {
            return flag;
        }
        String companyId = source.getCompanyId();

        ArrayList<BaseTmTuv> sourceTuvs = new ArrayList<BaseTmTuv>();
        ArrayList<BaseTmTuv> targetTuvs = new ArrayList<BaseTmTuv>();
        try
        {
            // As here don't need XliffAlt data, don't load them to improve
            // performance.
            boolean needLoadExtraInfo = false;
            ArrayList<Tuv> sourceTuvsTmp = SegmentTuvUtil.getSourceTuvs(source,
                    needLoadExtraInfo);
            sourceTuvs = splitSourceTuvs(sourceTuvsTmp,
                    source.getGlobalSightLocale(), companyId);
            ArrayList<Tuv> targetTuvsTmp = SegmentTuvUtil.getSourceTuvs(target,
                    needLoadExtraInfo);
            targetTuvs = splitSourceTuvs(targetTuvsTmp,
                    target.getGlobalSightLocale(), companyId);
        }
        catch (Exception e)
        {
            c_logger.info("Can not get Tuvs to compare" + e);
        }
        if (sourceTuvs.size() != targetTuvs.size())
        {
            return flag;
        }

        int i;
        for (i = 0; i < sourceTuvs.size(); i++)
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) sourceTuvs.get(i);
            SegmentTmTuv targetTuv = (SegmentTmTuv) targetTuvs.get(i);
            if (sourceTuv.getExactMatchKey() != targetTuv.getExactMatchKey())
            {
                flag = false;
                break;
            }
        }
        if (i == sourceTuvs.size())
        {
            flag = true;
        }
        return flag;
    }

    private static Map<Long, TuImpl> getTusMapBySourcePage(
            SourcePage p_sourcePage)
    {
        Map<Long, TuImpl> result = new HashMap<Long, TuImpl>();

        ExtractedFile ef = (ExtractedFile) p_sourcePage.getPrimaryFile();
        for (Iterator lgIt = ef.getLeverageGroups().iterator(); lgIt.hasNext();)
        {
            LeverageGroup lg = (LeverageGroup) lgIt.next();
            Collection tus = lg.getTus();
            for (Iterator it = tus.iterator(); it.hasNext();)
            {
                TuImpl tu = (TuImpl) it.next();
                result.put(tu.getIdAsLong(), tu);
            }
        }

        return result;
    }

    /**
     * Calculate word-counts for all target pages and workflows in current job.
     * 
     * @param job
     */
    @SuppressWarnings("unchecked")
    public static void calculateWordCountsForJob(Job job)
    {
        c_logger.info("Start calculating word counts for job " + job.getId());
        // calculateWorkflowStatistics() commits statistics to DB
        StatisticsService.calculateWorkflowStatistics(
                new ArrayList(job.getWorkflows()), job.getL10nProfile()
                        .getTranslationMemoryProfile().getJobExcludeTuTypes());

        for (Workflow workflow : job.getWorkflows())
        {
            StatisticsService.calculateTargetPagesWordCount(workflow, job
                    .getL10nProfile().getTranslationMemoryProfile()
                    .getJobExcludeTuTypes());
        }
        c_logger.info("Done calculating word counts for job " + job.getId());
    }

}
