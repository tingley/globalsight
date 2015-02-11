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
import java.sql.Connection;
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
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.TargetPageComparator;
import com.globalsight.everest.util.comparator.TuvSourceContentComparator;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.pageimport.StatisticsPersistenceCommand;
import com.globalsight.util.GlobalSightLocale;

/**
 * Provides statistic services for pages.
 */
public class StatisticsService
{
    static private Logger c_logger = Logger
            .getLogger(StatisticsService.class);

    /**
     * Calculates the statistics for the target pages passed in. Adds the word
     * count to the target page and commits to the database.
     * 
     * @param p_sourcePage -
     *            The source page that the target pages are related to.
     * @param p_targetPages -
     *            A hash map of all the target pages to calculate stastics for.
     *            The key is the target locale as a GlobalSightLocale id (Long).
     * @param p_levMatchThreshold
     *            The leverage match threshold set at TM profile (which is a job
     *            level value).
     */
    static public void calculateTargetPageStatistics(SourcePage p_sourcePage,
            Map p_targetPages, Vector p_excludedTuTypes, int p_levMatchThreshold)
            throws StatisticsException
    {
        Connection connection = null;

        try
        {
            ArrayList<Tuv> sTuvs = getSourceTuvs(p_sourcePage);
            ArrayList splittedTuvs = splitSourceTuvs(sTuvs, p_sourcePage
                    .getGlobalSightLocale());
            SegmentRepetition segmentRepetition = new SegmentRepetition(splittedTuvs);
            String sourcePageId = p_sourcePage.getExternalPageId();
            boolean isDefaultContextMatch = 
                isDefaultContextMatch(sourcePageId, p_sourcePage);
            
            Collection targetLocales = p_targetPages.keySet();
            Iterator ti = targetLocales.iterator();
            List<TargetPage> targetPages = new ArrayList<TargetPage>();

            while (ti.hasNext())
            {
                Long targetLocaleId = (Long) ti.next();

                // If the source file is WorldServer XLF file,MT translations
                // should NOT impact the word-count statistics.
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(
                                p_sourcePage.getIdAsLong());
                LeverageMatchLingManager lmLingManager = getLeverageMatchLingManager();
                if (isWSXlfSourceFile)
                {
                    lmLingManager.setIncludeMtMatches(false);
                }
                MatchTypeStatistics matches = lmLingManager
                        .getMatchTypesForStatistics(p_sourcePage.getIdAsLong(),
                                targetLocaleId, p_levMatchThreshold);

                PageWordCounts wordCount = null;
                if (isWSXlfSourceFile)
                {
                    wordCount = calculateWorldServerWorkflowWordCounts(
                            segmentRepetition, matches, p_excludedTuTypes);
                }
                else 
                {
                    wordCount = calculateWorkflowWordCounts(
                            segmentRepetition, matches, p_excludedTuTypes);
                }
                TargetPage targetPage = (TargetPage) (p_targetPages
                        .get(targetLocaleId));

                wordCount.setNoUseExactMatchWordCount(wordCount
                        .getSegmentTmWordCount()
                        + wordCount.getMTExtractMatchWordCount()
                        + wordCount.getXliffExtractMatchWordCount()
                        + wordCount.getPoExactMatchWordCount());
                wordCount.setNoUseInContextMatchWordCount(0);
                wordCount = calculateInContextMatchWordCounts(wordCount,
                        splittedTuvs, matches, p_excludedTuTypes);
                if (isDefaultContextMatch
                        && wordCount.getNoUseExactMatchWordCount() > 0)
                {
                    //Only consider the exact match word counts.
                    wordCount.setContextMatchWordCount(wordCount
                            .getNoUseExactMatchWordCount());
                }
                
                targetPage.setWordCount(wordCount);
                targetPage.setIsDefaultContextMatch(isDefaultContextMatch
                        && wordCount.getNoUseExactMatchWordCount() > 0);
                targetPages.add(targetPage);
            }

            StatisticsPersistenceCommand spc = new StatisticsPersistenceCommand(
                    targetPages);

            connection = PersistenceService.getInstance().getConnection();
            connection.setAutoCommit(false);

            spc.persistObjects(connection);

            connection.commit();
        }
        catch (Exception e)
        {
            try
            {
                if (connection != null)
                {
                    connection.rollback();
                }
            }
            catch (Throwable t)
            {
                // ignore
                c_logger.error("can't rollback transaction", t);
            }

            String[] args = new String[1];
            args[0] = Long.toString(p_sourcePage.getId());

            throw new StatisticsException(
                    StatisticsException.MSG_FAILED_TO_GENERATE_STATISTICS,
                    args, e);
        }
        finally
        {
            try
            {
                if (connection != null)
                {
                    connection.setAutoCommit(true);
                    PersistenceService.getInstance().returnConnection(
                            connection);
                }
            }
            catch (Exception e)
            {
                c_logger.info("The connection could not be returned" + e);
            }
        }
    }

    private static boolean isSameOfSourcePage(SourcePage source, SourcePage target)
    {
        boolean flag = false;
        if(source == null || target == null)
        {
            return flag;
        }

        ArrayList sourceTuvs = new ArrayList();
        ArrayList targetTuvs = new ArrayList();
        try
        {
            sourceTuvs = splitSourceTuvs(getSourceTuvs(source), source.getGlobalSightLocale());
            targetTuvs = splitSourceTuvs(getSourceTuvs(target), target.getGlobalSightLocale());
        }
        catch (Exception e)
        {
            c_logger.info("Can not get Tuvs to compare" + e);
        }
        if(sourceTuvs.size() != targetTuvs.size())
        {
            return flag;
        }

        int i;
        for(i = 0; i < sourceTuvs.size(); i++)
        {
            SegmentTmTuv sourceTuv = (SegmentTmTuv) sourceTuvs.get(i);
            SegmentTmTuv targetTuv = (SegmentTmTuv) targetTuvs.get(i);
            if (sourceTuv.getExactMatchKey() != targetTuv.getExactMatchKey())
//            if(!sourceTuv.getSegment().equals(targetTuv.getSegment()))
            {
                flag = false;
                break;
            }
        }
        if(i == sourceTuvs.size())
        {
            flag = true;
        }
        return flag;
    }
    
    public static boolean isDefaultContextMatch(TargetPage target)
    {
        return isDefaultContextMatch(target.getSourcePage());
    }
    
    public static boolean isDefaultContextMatch(SourcePage source)
    {
        return isDefaultContextMatch(source.getExternalPageId(), source);
    }
    
    public static boolean isDefaultContextMatch(
            String sourcePageId, SourcePage page)
    {
        String localeStr = sourcePageId.substring(0, sourcePageId.indexOf(File.separator));

        String temp1 = sourcePageId.substring(localeStr.length() + File.separator.length());
        
        String isWebservice = temp1.substring(0, temp1.indexOf(File.separator)) + "";
        
        if(isWebservice.indexOf("webservice") > -1) {
            temp1 = sourcePageId.substring(localeStr.length() 
                                           + File.separator.length() 
                                           + "webservice".length() 
                                           + File.separator.length());
        }
        
        String temp2 = temp1.substring(temp1.indexOf(File.separator));
        String queryStr = localeStr + "%" + temp2;
        
        if(! File.separator.equals("/"))
        {
            queryStr = queryStr.replace("\\", "\\\\\\\\");
        }
        queryStr = queryStr.replace("'","\\'");
        String sql = "select id from source_page where external_page_id LIKE '"
                + queryStr + "' and state not in ('OUT_OF_DATE','IMPORT_FAIL')";
        List list = HibernateUtil.searchWithSql(sql, null);
        list = removeCurrent(list, page.getId());
        for(int i = 0; i < list.size(); i++)
        {
            SourcePage sp = null;
            // All TUs and TUVs of the other source page will be loaded by
            // isSameOfSourcePage, bloating the Hibernate (first-level) cache.
            // Load them in a new session so we can free them by closing the
            // session after isSameOfSourcePage finishes.
            Session newSession = HibernateUtil.getSessionFactory().openSession();
            try
            {
                long otherSourcePageId = ((BigInteger) list.get(i)).longValue();
                sp = (SourcePage) newSession.get(SourcePage.class, otherSourcePageId);
                if(isSameOfSourcePage(page, sp))
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
        if(list == null || list.size() == 0)
        {
            return new ArrayList();
        }
        for(int i = 0; i < list.size(); i++)
        {
            long idInList = ((BigInteger) list.get(i)).longValue();
            if(id == idInList)
            {
                list.remove(i);
            }
        }
        return list;
    }

    /**
     * Calculates the statistics for the workflows passed in. Adds the word
     * count to the workflows and commits to the database.
     * 
     * @param p_workflows -
     *            a List of workflows of which the statistics are calculated.
     *            All workflows must belong to the same job.
     */
    static public void calculateWorkflowStatistics(List p_workflows,
            Vector p_excludedTuTypes) throws StatisticsException
    {
        // sanity check
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
            Iterator itWf = p_workflows.iterator();
            while (itWf.hasNext())
            {
                // get all segments of all source pages
                Workflow wf = (Workflow) itWf.next();
                wf = (WorkflowImpl) session.get(WorkflowImpl.class, wf.getIdAsLong());
                
                ArrayList splitSourceTuvs = getAllSplitSourceTuvs(wf);
                ArrayList wsSplitSourceTuvs = getAllWorldServerSplitSourceTuvs(wf);
                // get the job's leverage match threshold
                SegmentRepetition segmentRepetition = new SegmentRepetition(
                        splitSourceTuvs);
                SegmentRepetition wsSegmentRepetition = new SegmentRepetition(
                        wsSplitSourceTuvs);
                
                // get match types for all target segments in a workflow
                int levMatchThreshold = wf.getJob().getLeverageMatchThreshold();
                MatchTypeStatistics matches = getMatchTypeStatistics(wf,
                        levMatchThreshold);

                PageWordCounts commonWordCount = calculateWorkflowWordCounts(
                        segmentRepetition, matches, p_excludedTuTypes);
                PageWordCounts worldServerWordCount = calculateWorldServerWorkflowWordCounts(
                        wsSegmentRepetition, matches, p_excludedTuTypes);

                setOtherWordCounts(commonWordCount, worldServerWordCount, wf.getAllTargetPages());
                PageWordCounts wordCount = mergeWordCounts(commonWordCount, worldServerWordCount);

                wf.setInContextMatchWordCount(wordCount.getInContextWordCount());
                wf.setNoUseInContextMatchWordCount(wordCount.getNoUseInContextMatchWordCount());
                wf.setNoUseExactMatchWordCount(wordCount.getNoUseExactMatchWordCount());
                wf.setContextMatchWordCount(wordCount.getContextMatchWordCount());
                wf.setSegmentTmWordCount(wordCount.getSegmentTmWordCount());
                wf.setSubLevMatchWordCount(wordCount.getSubLevMatchWordCount());
                wf.setSubLevRepetitionWordCount(wordCount.getSubLevRepetitionWordCount());
                wf.setLowFuzzyMatchWordCount(wordCount.getLowFuzzyWordCount());
                wf.setMedFuzzyMatchWordCount(wordCount.getMedFuzzyWordCount());
                wf.setMedHiFuzzyMatchWordCount(wordCount.getMedHiFuzzyWordCount());
                wf.setHiFuzzyMatchWordCount(wordCount.getHiFuzzyWordCount());
                wf.setRepetitionWordCount(wordCount.getRepetitionWordCount());
                wf.setNoMatchWordCount(wordCount.getUnmatchedWordCount());
                wf.setTotalWordCount(wordCount.getTotalWordCount());
                
                wf.setHiFuzzyRepetitionWordCount(wordCount.getHiFuzzyRepetitionWordCount());
                wf.setMedHiFuzzyRepetitionWordCount(wordCount.getMedHighFuzzyRepetitionWordCount());
                wf.setMedFuzzyRepetitionWordCount(wordCount.getMedFuzzyRepetitionWordCount());
                
                wf.setThresholdHiFuzzyWordCount(wordCount.getThresholdHiFuzzyWordCount());
                wf.setThresholdLowFuzzyWordCount(wordCount.getThresholdLowFuzzyWordCount());
                wf.setThresholdMedFuzzyWordCount(wordCount.getThresholdMedFuzzyWordCount());
                wf.setThresholdMedHiFuzzyWordCount(wordCount.getThresholdMedHiFuzzyWordCount());
                wf.setThresholdNoMatchWordCount(wordCount.getThresholdNoMatchWordCount());

                session.update(wf);
            }

            transaction.commit();

            // Used to fix the GBS-2020, make the repetition from file level to
            // workflow level
            Iterator itWfRepetition = p_workflows.iterator();
            while (itWfRepetition.hasNext())
            {
                Workflow wf = (Workflow) itWfRepetition.next();
                calculateTargetPageRepetitionStatistics(wf, p_excludedTuTypes);
            }
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

    private static void setOtherWordCounts(PageWordCounts wordCount,
            PageWordCounts worldServerWordCount, Vector targetPages)
    {
        int inContextWordCount = 0;
        int defaultContextMatchWordCount = 0;
        
        int worldServerInContextWordCount = 0;
        int worldServerDefaultContextMatchWordCount = 0;
        
        try
        {
            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage tp = (TargetPage) targetPages.get(i);
                
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(tp.getSourcePage().getIdAsLong());
                if (isWSXlfSourceFile)
                {
                    worldServerInContextWordCount += tp.getWordCount().getInContextWordCount();
                    worldServerDefaultContextMatchWordCount += tp.getWordCount().getContextMatchWordCount();
                }
                else 
                {
                    inContextWordCount += tp.getWordCount().getInContextWordCount();
                    defaultContextMatchWordCount += tp.getWordCount().getContextMatchWordCount();
                }
            }
            wordCount.setInContextWordCount(inContextWordCount);
            wordCount.setContextMatchWordCount(defaultContextMatchWordCount);
            wordCount.setSegmentTmWordCount(
                    wordCount.getSegmentTmWordCount()
                    + wordCount.getMTExtractMatchWordCount()
                    + wordCount.getXliffExtractMatchWordCount()
                    + wordCount.getPoExactMatchWordCount()
                    - wordCount.getInContextWordCount());
            
            worldServerWordCount.setInContextWordCount(worldServerInContextWordCount);
            worldServerWordCount.setContextMatchWordCount(worldServerDefaultContextMatchWordCount);
            worldServerWordCount.setSegmentTmWordCount(
                    worldServerWordCount.getSegmentTmWordCount()
                    + worldServerWordCount.getMTExtractMatchWordCount()
                    + worldServerWordCount.getXliffExtractMatchWordCount()
                    + worldServerWordCount.getPoExactMatchWordCount()
                    - worldServerWordCount.getInContextWordCount());
        }
        catch (Exception e)
        {
            c_logger.error("Query worldserver files error");
        }
    }
    
    /**
     * Merge common wordcounts and worldserver xlf wordcounts together
     * @param commonWordcounts
     * @param worldserverWordcounts
     * @return
     */
    private static PageWordCounts mergeWordCounts(PageWordCounts wc, PageWordCounts wwc)
    {
        PageWordCounts result = new PageWordCounts();
        result.setContextMatchWordCount(wc.getContextMatchWordCount() + wwc.getContextMatchWordCount());
        result.setHiFuzzyRepetitionWordCount(wc.getHiFuzzyRepetitionWordCount() + wwc.getHiFuzzyRepetitionWordCount());
        result.setHiFuzzyWordCount(wc.getHiFuzzyWordCount() + wwc.getHiFuzzyWordCount());
        result.setInContextWordCount(wc.getInContextWordCount() + wwc.getInContextWordCount());
        result.setLowFuzzyWordCount(wc.getLowFuzzyWordCount() + wwc.getLowFuzzyWordCount());
        result.setMedFuzzyRepetitionWordCount(wc.getMedFuzzyRepetitionWordCount() + wwc.getMedFuzzyRepetitionWordCount());
        result.setMedFuzzyWordCount(wc.getMedFuzzyWordCount() + wwc.getMedFuzzyWordCount());
        result.setMedHiFuzzyWordCount(wc.getMedHiFuzzyWordCount() + wwc.getMedHiFuzzyWordCount());
        result.setMedHighFuzzyRepetitionWordCount(wc.getMedHighFuzzyRepetitionWordCount() + wwc.getMedHighFuzzyRepetitionWordCount());
        result.setMTExtractMatchWordCount(wc.getMTExtractMatchWordCount() + wwc.getMTExtractMatchWordCount());
        result.setNoUseExactMatchWordCount(wc.getNoUseExactMatchWordCount() + wwc.getNoUseExactMatchWordCount());
        result.setNoUseInContextMatchWordCount(wc.getNoUseInContextMatchWordCount() + wwc.getNoUseInContextMatchWordCount());
        result.setPoExactMatchWordCount(wc.getPoExactMatchWordCount() + wwc.getPoExactMatchWordCount());
        result.setRepetitionWordCount(wc.getRepetitionWordCount() + wwc.getRepetitionWordCount());
        result.setSegmentTmWordCount(wc.getSegmentTmWordCount() + wwc.getSegmentTmWordCount());
        result.setSubLevMatchWordCount(wc.getSubLevMatchWordCount() + wwc.getSubLevMatchWordCount());
        result.setSubLevRepetitionWordCount(wc.getSubLevRepetitionWordCount() + wwc.getSubLevRepetitionWordCount());
        result.setThresholdHiFuzzyWordCount(wc.getThresholdHiFuzzyWordCount() + wwc.getThresholdHiFuzzyWordCount());
        result.setThresholdLowFuzzyWordCount(wc.getThresholdLowFuzzyWordCount() + wwc.getThresholdLowFuzzyWordCount());
        result.setThresholdMedFuzzyWordCount(wc.getThresholdMedFuzzyWordCount() + wwc.getThresholdMedFuzzyWordCount());
        result.setThresholdMedHiFuzzyWordCount(wc.getThresholdMedHiFuzzyWordCount() + wwc.getThresholdMedHiFuzzyWordCount());
        result.setThresholdNoMatchWordCount(wc.getThresholdNoMatchWordCount() + wwc.getThresholdNoMatchWordCount());
        result.setTotalWordCount(wc.getTotalWordCount() + wwc.getTotalWordCount());
        result.setUnmatchedWordCount(wc.getUnmatchedWordCount() + wwc.getUnmatchedWordCount());
        result.setXliffExtractMatchWordCount(wc.getXliffExtractMatchWordCount() + wwc.getXliffExtractMatchWordCount());
        return result;
    }
    
//    private static int getAllDefaultContextMatchWordCountInAllTargetPages(
//            Vector allTargetPages)
//    {
//        int defaultContextMatchWordCount = 0;
//        for(int i = 0; i < allTargetPages.size(); i++)
//        {
//            TargetPage tp = (TargetPage) allTargetPages.get(i);
//            defaultContextMatchWordCount += tp.getWordCount().getContextMatchWordCount();
//        }
//        return defaultContextMatchWordCount;
//    }
//
//    private static int getAllInContextMatchWordCountInAllTargetPages(
//            Vector allTargetPages)
//    {
//        int inContextWordCount = 0;
//        for (int i = 0; i < allTargetPages.size(); i++)
//        {
//            TargetPage tp = (TargetPage) allTargetPages.get(i);
//            inContextWordCount += tp.getWordCount().getInContextWordCount();
//        }
//        return inContextWordCount;
//    }

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
            MatchTypeStatistics matches, Vector p_excludedTuTypes)
    {
        int inContextMatchWordCount = 0;
        
        for (int i = 0, max = splitSourceTuvs.size(); i < max; i++)
        {
            if (LeverageUtil.isIncontextMatch(i, splitSourceTuvs,
                    null, matches, p_excludedTuTypes))
            {
                inContextMatchWordCount += ((SegmentTmTuv) splitSourceTuvs
                        .get(i)).getWordCount();
            }
        }
        
        return inContextMatchWordCount;
    }

    /**
     * Update the in context match and exact match
     * 
     * @return The page word count which have been updated the related in
     *         context match and exact match.
     */
    private static PageWordCounts calculateInContextMatchWordCounts(
            PageWordCounts wordCount, ArrayList splitSourceTuvs,
            MatchTypeStatistics matches, Vector p_excludedTuTypes)
    {
        int inContextMatchWordCount = onePageInContextMatchWordCounts(
                wordCount, splitSourceTuvs, matches, p_excludedTuTypes);
        wordCount.setInContextWordCount(inContextMatchWordCount);
        wordCount.setSegmentTmWordCount(wordCount.getSegmentTmWordCount() 
                + wordCount.getMTExtractMatchWordCount()
                + wordCount.getXliffExtractMatchWordCount()
                + wordCount.getPoExactMatchWordCount()
                - inContextMatchWordCount);

        return wordCount;
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
     * @param p_sourceTuvs Collection of subflow separated source Tuvs. Type of
     * elements is SegmentTmTuv. @param p_matches MatchTypeStatistics object
     */
    private static PageWordCounts calculateWorldServerWorkflowWordCounts(
            SegmentRepetition p_segmentRepetition,
            MatchTypeStatistics p_matches, Vector p_excludedTuTypes)
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
                    if (sourceContent != null && sourceContent.equals("repetition"))
                    {
                        subLevMatchRepWordCount += wordCount;
                    }
                    else 
                    {
                        subLevMatchWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_FUZZY:
                    if (sourceContent != null && sourceContent.equals("repetition"))
                    {
                        medFuzzyRepetitionWordCount += wordCount;
                    }
                    else 
                    {
                        medFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_HI_FUZZY:
                    if (sourceContent != null && sourceContent.equals("repetition"))
                    {
                        medHiFuzzyRepetitionWordCount += wordCount;
                    }
                    else 
                    {
                        medHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.HI_FUZZY:
                    if (sourceContent != null && sourceContent.equals("repetition"))
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
                    if (sourceContent != null && sourceContent.equals("repetition"))
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
                 * This part is used to calculate the word counts relative to threshold,
                 * they are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount,thresholdMedFuzzyWordCount,
                 *          thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
                 */
                Types typesOfThreshold = p_matches.getTypesByThreshold(tmTuv.getId(),
                        ((SegmentTmTu) tmTuv.getTu()).getSubId());
                int matchTypeOfThreshold = typesOfThreshold == null ? MatchTypeStatistics.THRESHOLD_NO_MATCH
                        : typesOfThreshold.getStatisticsMatchType();
                switch (matchTypeOfThreshold)
                {
                    case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                        if (sourceContent == null || !sourceContent.equals("repetition"))
                        {
                            thresholdHiFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                        if (sourceContent == null || !sourceContent.equals("repetition"))
                        {
                            thresholdMedHiFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                        if (sourceContent == null || !sourceContent.equals("repetition"))
                        {
                            thresholdMedFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                        if (sourceContent == null || !sourceContent.equals("repetition"))
                        {
                            thresholdLowFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                        if (sourceContent == null || !sourceContent.equals("repetition"))
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
        result.setLowFuzzyWordCount(subLevMatchWordCount + subLevMatchRepWordCount);
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
     * @param p_sourceTuvs Collection of subflow separated source Tuvs. Type of
     * elements is SegmentTmTuv. @param p_matches MatchTypeStatistics object
     */
    static private PageWordCounts calculateWorkflowWordCounts(
            SegmentRepetition p_segmentRepetition,
            MatchTypeStatistics p_matches, Vector p_excludedTuTypes)
    {
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;
        
        int subLevMatchWordCount = 0;
        int subLevMatchRepWordCount = 0;
//        int lowFuzzyWordCount = 0; // subLevMatchWordCount+subLevMatchRepWordCount
        
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

                int matchType = types == null ? MatchTypeStatistics.NO_MATCH
                        : types.getStatisticsMatchType();
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
                    if (i < 1)
                    {
                        subLevMatchWordCount += wordCount;
                    }
                    else 
                    {
                        subLevMatchRepWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_FUZZY:
                    if (i < 1)
                    {
                        medFuzzyWordCount += wordCount;
                    }
                    else 
                    {
                        medFuzzyRepetitionWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_HI_FUZZY:
                    if (i < 1)
                    {
                        medHiFuzzyWordCount += wordCount;
                    }
                    else 
                    {
                        medHiFuzzyRepetitionWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.HI_FUZZY:
                    if (i < 1)
                    {
                        hiFuzzyWordCount += wordCount;
                    }
                    else 
                    {
                        hiFuzzyRepetitionWordCount += wordCount;
                    }
                    break;
                case LeverageMatchLingManager.NO_MATCH:
                default:
                    // no-match is counted only once and the rest are
                    // repetitions
                    if (i < 1)
                    {
                        unmatchedWordCount += wordCount;
                    }
                    else
                    {
                        repetitionWordCount += wordCount;
                    }
                    break;
                }
                
                /*
                 * This part is used to calculate the word counts relative to threshold,
                 * they are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount,thresholdMedFuzzyWordCount,
                 *          thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
                 */
                Types typesOfThreshold = p_matches.getTypesByThreshold(source.getId(),
                        ((SegmentTmTu) source.getTu()).getSubId());
                int matchTypeOfThreshold = typesOfThreshold == null ? MatchTypeStatistics.THRESHOLD_NO_MATCH
                        : typesOfThreshold.getStatisticsMatchType();
                switch (matchTypeOfThreshold)
                {
                    case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                        if (i < 1)
                        {
                            thresholdHiFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                        if (i < 1)
                        {
                            thresholdMedHiFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                        if (i < 1)
                        {
                            thresholdMedFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                        if (i < 1)
                        {
                            thresholdLowFuzzyWordCount += wordCount;
                        }
                        break;
                    case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                        if (i < 1)
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
        result.setLowFuzzyWordCount(subLevMatchWordCount + subLevMatchRepWordCount);
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

    static public ArrayList<Tuv> getSourceTuvs(SourcePage p_sourcePage)
    {
        ArrayList<Tuv> result = new ArrayList<Tuv>();
        ExtractedFile ef = (ExtractedFile) p_sourcePage.getPrimaryFile();
        Iterator lgIterator = ef.getLeverageGroups().iterator();

        while (lgIterator.hasNext())
        {
            LeverageGroup lg = (LeverageGroup) lgIterator.next();
            Collection cTus = lg.getTus();
            Iterator iTus = cTus.iterator();

            while (iTus.hasNext())
            {
                Tu tu = (Tu) iTus.next();
                Collection cTuvs = tu.getTuvs();
                Iterator iTuvs = cTuvs.iterator();

                while (iTuvs.hasNext())
                {
                    Tuv tuv = (Tuv) iTuvs.next();

                    if (tuv.getLocaleId() == p_sourcePage.getLocaleId())
                    {
                        result.add(tuv);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Takes a collection of original source Tuvs (type Tuv) and splits
     * subflows. Returns a collection of SegmentTmTuv.
     */
    static public ArrayList splitSourceTuvs(ArrayList<Tuv> p_sourceTuvs,
            GlobalSightLocale p_sourceLocale) throws Exception
    {
        // sort the list first, put all tuvs whose source content equals "repeated"
        // in front of the list. it will affect worldserver xlf files, other files
        // will not be impacted.
        Collections.sort(p_sourceTuvs, new TuvSourceContentComparator());
        // convert Tu, Tuv to PageTmTu, PageTmTuv to split segments
        ArrayList<PageTmTu> pageTmTuList = new ArrayList<PageTmTu>(p_sourceTuvs.size());

        for (int i = 0; i < p_sourceTuvs.size(); i++)
        {
            Tuv originalTuv = (Tuv) p_sourceTuvs.get(i);
            Tu originalTu = originalTuv.getTu();

            PageTmTu pageTmTu = new PageTmTu(originalTu.getId(), 0, originalTu
                    .getDataType(), originalTu.getTuType(), !originalTu
                    .isLocalizable());
            pageTmTu.setSourceContent(originalTu.getSourceContent());
            PageTmTuv pageTmTuv = new PageTmTuv(originalTuv.getId(),
                    originalTuv.getGxml(), p_sourceLocale);
            pageTmTuv.setSid(originalTuv.getSid());

            pageTmTu.addTuv(pageTmTuv);

            pageTmTuList.add(pageTmTu);
        }
        // make a list of splitted segment Tus
        ArrayList<SegmentTmTu> splittedTus = new ArrayList<SegmentTmTu>(pageTmTuList.size());

        for (PageTmTu pageTmTu : pageTmTuList)
        {
            Collection<SegmentTmTu> segmentTmTus = TmUtil.createSegmentTmTus(pageTmTu,
                    p_sourceLocale);
            splittedTus.addAll(segmentTmTus);
        }

        // make a list of SegmentTmTuv from a list of SegmentTmTu
        ArrayList splittedTuvs = new ArrayList(splittedTus.size());
        for (SegmentTmTu tu : splittedTus)
        {
            splittedTuvs.add(tu.getFirstTuv(p_sourceLocale));
        }

        return splittedTuvs;
    }
    
    /**
     * Split segments from workflow
     * @param p_workflow
     * @return
     * @throws Exception
     */
    static private ArrayList getAllSplitSourceTuvs(Workflow p_workflow)
            throws Exception
    {
        ArrayList splitTuvs = new ArrayList();
        List targetPages = p_workflow.getTargetPages();

        Iterator it = targetPages.iterator();
        while (it.hasNext())
        {
            TargetPage targetPage = (TargetPage) it.next();
            SourcePage sourcePage = targetPage.getSourcePage();

            if (sourcePage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
            {
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(sourcePage.getIdAsLong());
                if (!isWSXlfSourceFile)
                {
                    ArrayList<Tuv> sTuvs = getSourceTuvs(sourcePage);
                    splitTuvs.addAll(splitSourceTuvs(sTuvs, sourcePage
                            .getGlobalSightLocale()));
                }
            }
        }

        return splitTuvs;
    }
    
    /**
     * Split segments from workflow, used for worldserver xlf files
     * @param p_workflow
     * @return
     * @throws Exception
     */
    static private ArrayList getAllWorldServerSplitSourceTuvs(Workflow p_workflow)
            throws Exception
    {
        ArrayList wsSplitTuvs = new ArrayList();

        List targetPages = p_workflow.getTargetPages();

        Iterator it = targetPages.iterator();
        while (it.hasNext())
        {
            TargetPage targetPage = (TargetPage) it.next();
            SourcePage sourcePage = targetPage.getSourcePage();

            if (sourcePage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
            {
                boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                        .isWorldServerXliffSourceFile(sourcePage.getIdAsLong());
                if (isWSXlfSourceFile)
                {
                    ArrayList<Tuv> sTuvs = getSourceTuvs(sourcePage);
                    wsSplitTuvs.addAll(splitSourceTuvs(sTuvs, sourcePage
                            .getGlobalSightLocale()));
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
        Iterator itTp = p_workflow.getTargetPages().iterator();
        while (itTp.hasNext())
        {
            TargetPage targetPage = (TargetPage) itTp.next();
            
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
                MatchTypeStatistics matches = lmLingManager
                        .getMatchTypesForStatistics(targetPage.getSourcePage()
                                .getIdAsLong(), targetLocaleId,
                                p_levMatchThreshold);
                result.merge(matches);
                result.mergeThres(matches);
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

    /**
     * Calculates the repetition/no match word counts for every target page in
     * workflow. The repetition displays in Detailed Word Counts of Activity
     * also can be see in file list report
     * 
     * @param p_workflow
     * @param p_excludedTuTypes
     * @throws StatisticsException
     */
    static private void calculateTargetPageRepetitionStatistics(
            Workflow p_workflow, Vector p_excludedTuTypes)
            throws StatisticsException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // This map is used for saving the unique segments for this workflow
            Map m_uniqueSegments = new HashMap();
            Map m_uniqueSegments2 = new HashMap();
            // get the job's leverage match threshold
            int levMatchThreshold = p_workflow.getJob()
                    .getLeverageMatchThreshold();

            // target page Order by target page id
            List<TargetPage> targetPages = p_workflow.getAllTargetPages();
            
            if (targetPages != null && targetPages.size() > 0)
            {
                TargetPageComparator comp = new TargetPageComparator(
                        TargetPageComparator.ID, targetPages.get(0)
                                .getSourcePage().getGlobalSightLocale()
                                .getLocale());
                Collections.sort(targetPages, comp);

                Iterator<TargetPage> itTP = targetPages.iterator();
                while (itTP.hasNext())
                {
                    TargetPage tp = itTP.next();
                    if (tp.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
                    {
                        SourcePage sp = tp.getSourcePage();

                        tp = (TargetPage) session.get(TargetPage.class, tp
                                .getId());
                        ArrayList sTuvs = getSourceTuvs(sp);
                        ArrayList splittedTuvs = splitSourceTuvs(sTuvs, sp
                                .getGlobalSightLocale());
                        // If the source file is WorldServer xliff file,MT
                        // translations should NOT impact the word-count statistics.
                        boolean isWSXlfSourceFile = ServerProxy.getTuvManager()
                                .isWorldServerXliffSourceFile(sp.getIdAsLong());
                        LeverageMatchLingManager lmLingManager = getLeverageMatchLingManager();
                        MatchTypeStatistics matches = lmLingManager
                                .getMatchTypesForStatistics(sp.getIdAsLong(),
                                        tp.getLocaleId(), levMatchThreshold);
                        if (isWSXlfSourceFile)
                        {
                            lmLingManager.setIncludeMtMatches(false);
                            calculateWorldServerRepetitionCounts(splittedTuvs, matches,
                                    p_excludedTuTypes, tp);
                        }
                        else 
                        {
                            // Get the repetition word count for this target page
                            calculateRepetitionCounts(splittedTuvs, matches,
                                    p_excludedTuTypes, m_uniqueSegments, tp);
                        }
                        saveRepetitionInfoToTu(splittedTuvs, matches,
                                m_uniqueSegments2);
                        session.update(tp);
                    }
                }
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
            String jobName = p_workflow.getJob().getJobName();
            args[0] = jobName;

            throw new StatisticsException(
                    StatisticsException.MSG_FAILED_TO_GENERATE_STATISTICS_WORKFLOW,
                    args, e);
        }
    }
    
    /**
     * Calculate the repetition word count for target page of Worldserver xlf files
     * 
     * @param sTuvs
     * @param p_matches
     * @param p_excludedTuTypes
     * @param m_uniqueSegments
     * @return
     */
    private static void calculateWorldServerRepetitionCounts(ArrayList sTuvs,
            MatchTypeStatistics p_matches, Vector p_excludedTuTypes,
            TargetPage targetPage)
    {
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
        
        Iterator si = sTuvs.iterator();
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
            Types types = p_matches.getTypes(tuv.getId(), ((SegmentTmTu) tuv
                    .getTu()).getSubId());

            int matchType = types == null ? MatchTypeStatistics.NO_MATCH : types
                    .getStatisticsMatchType();
            String sourceContent = tuv.getTu().getSourceContent();
            // increment the word count according to the match type
            switch (matchType)
            {
                case MatchTypeStatistics.CONTEXT_EXACT:
                case MatchTypeStatistics.SEGMENT_TM_EXACT:
                case MatchTypeStatistics.SEGMENT_MT_EXACT:
                case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                case MatchTypeStatistics.SEGMENT_PO_EXACT:
                    break;
                case MatchTypeStatistics.LOW_FUZZY:
                    if (sourceContent != null && sourceContent.equals("repetition"))
                    {
                        subLevRepetitionWordCount += wordCount;
                    }
                    else 
                    {
                        subLevMatchWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_FUZZY:
                    if (sourceContent != null && sourceContent.equals("repetition"))
                    {
                        medFuzzyRepetitionWordCount += wordCount;
                    }
                    else 
                    {
                        medFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.MED_HI_FUZZY:
                    if (sourceContent != null && sourceContent.equals("repetition"))
                    {
                        medHighFuzzyRepetionWordCount += wordCount;
                    }
                    else 
                    {
                        medHighFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.HI_FUZZY:
                    if (sourceContent != null && sourceContent.equals("repetition"))
                    {
                        highFuzzyRepetionWordCount += wordCount;
                    }
                    else 
                    {
                        highFuzzyWordCount += wordCount;
                    }
                    break;
                case LeverageMatchLingManager.NO_MATCH:
                default:
                    if (sourceContent != null && sourceContent.equals("repetition"))
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
             * This part is used to calculate the word counts relative to threshold,
             * they are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount,thresholdMedFuzzyWordCount,
             *          thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
             */
            Types typesOfThreshold = p_matches.getTypesByThreshold(tuv.getId(),
                    ((SegmentTmTu) tuv.getTu()).getSubId());
            int matchTypeOfThreshold = typesOfThreshold == null ? MatchTypeStatistics.THRESHOLD_NO_MATCH
                    : typesOfThreshold.getStatisticsMatchType();
            switch (matchTypeOfThreshold)
            {
                case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                    if (sourceContent == null || !sourceContent.equals("repetition"))
                    {
                        thresholdHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                    if (sourceContent == null || !sourceContent.equals("repetition"))
                    {
                        thresholdMedHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                    if (sourceContent == null || !sourceContent.equals("repetition"))
                    {
                        thresholdMedFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                    if (sourceContent == null || !sourceContent.equals("repetition"))
                    {
                        thresholdLowFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                    if (sourceContent == null || !sourceContent.equals("repetition"))
                    {
                        thresholdNoMatchWordCount += wordCount;
                    }
                    break;
                default:
                    break;
            }
        }
        targetPage.getWordCount().setRepetitionWordCount(
                repetitionWordCount + highFuzzyRepetionWordCount
                        + medHighFuzzyRepetionWordCount
                        + medFuzzyRepetitionWordCount
                        + subLevRepetitionWordCount);
        targetPage.getWordCount().setUnmatchedWordCount(noMatchWordCount);
        targetPage.getWordCount().setSubLevMatchWordCount(subLevMatchWordCount);
        targetPage.getWordCount().setSubLevRepetitionWordCount(subLevRepetitionWordCount);
        targetPage.getWordCount().setHiFuzzyRepetitionWordCount(highFuzzyRepetionWordCount);
        targetPage.getWordCount().setMedHighFuzzyRepetitionWordCount(medHighFuzzyRepetionWordCount);
        targetPage.getWordCount().setMedFuzzyRepetitionWordCount(medFuzzyRepetitionWordCount);
        targetPage.getWordCount().setTotalWordCount(totalWordCount);
        targetPage.getWordCount().setHiFuzzyWordCount(highFuzzyWordCount);
        targetPage.getWordCount().setMedHiFuzzyWordCount(medHighFuzzyWordCount);
        targetPage.getWordCount().setMedFuzzyWordCount(medFuzzyWordCount);
        targetPage.getWordCount().setLowFuzzyWordCount(subLevMatchWordCount);
        
        targetPage.getWordCount().setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        targetPage.getWordCount().setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        targetPage.getWordCount().setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        targetPage.getWordCount().setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        targetPage.getWordCount().setThresholdNoMatchWordCount(thresholdNoMatchWordCount);
    }

    /**
     * Calculate the repetition/no match word count for target page
     * 
     * @param sTuvs
     * @param p_matches
     * @param p_excludedTuTypes
     * @param m_uniqueSegments
     * @return
     */
    private static void calculateRepetitionCounts(ArrayList sTuvs,
            MatchTypeStatistics p_matches, Vector p_excludedTuTypes,
            Map m_uniqueSegments, TargetPage targetPage)
    {
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
        
        Map tmp = new HashMap();
        tmp.putAll(m_uniqueSegments);
        
        Iterator si = sTuvs.iterator();
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
            Types types = p_matches.getTypes(tuv.getId(), ((SegmentTmTu) tuv
                    .getTu()).getSubId());

            int matchType = types == null ? MatchTypeStatistics.NO_MATCH : types
                    .getStatisticsMatchType();
            ArrayList identicalSegments = null;
            // increment the word count according to the match type
            switch (matchType)
            {
                case MatchTypeStatistics.CONTEXT_EXACT:
                case MatchTypeStatistics.SEGMENT_TM_EXACT:
                case MatchTypeStatistics.SEGMENT_MT_EXACT:
                case MatchTypeStatistics.SEGMENT_XLIFF_EXACT:
                case MatchTypeStatistics.SEGMENT_PO_EXACT:
                    break;
                case MatchTypeStatistics.LOW_FUZZY:
                    identicalSegments = (ArrayList) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList();
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
                    identicalSegments = (ArrayList) m_uniqueSegments.get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList();
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
                    identicalSegments = (ArrayList) m_uniqueSegments.get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList();
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
                    identicalSegments = (ArrayList) m_uniqueSegments.get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList();
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
                    identicalSegments = (ArrayList) m_uniqueSegments
                            .get(tuv);
                    if (identicalSegments == null)
                    {
                        identicalSegments = new ArrayList();
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
             * This part is used to calculate the word counts relative to threshold,
             * they are:thresholdHiFuzzyWordCount,thresholdLowFuzzyWordCount,thresholdMedFuzzyWordCount,
             *          thresholdMedHiFuzzyWordCount,thresholdNoMatchWordCount
             */
            Types typesOfThreshold = p_matches.getTypesByThreshold(tuv.getId(),
                    ((SegmentTmTu) tuv.getTu()).getSubId());
            int matchTypeOfThreshold = typesOfThreshold == null ? MatchTypeStatistics.THRESHOLD_NO_MATCH
                    : typesOfThreshold.getStatisticsMatchType();
            ArrayList identicalSegmentsOfThreshold = null;
            switch (matchTypeOfThreshold)
            {
                case MatchTypeStatistics.THRESHOLD_HI_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList) tmp.get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList) tmp.get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdMedHiFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_MED_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList) tmp.get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdMedFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_LOW_FUZZY:
                    identicalSegmentsOfThreshold = (ArrayList) tmp.get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdLowFuzzyWordCount += wordCount;
                    }
                    break;
                case MatchTypeStatistics.THRESHOLD_NO_MATCH:
                    identicalSegmentsOfThreshold = (ArrayList) tmp.get(tuv);
                    if (identicalSegmentsOfThreshold == null)
                    {
                        identicalSegmentsOfThreshold = new ArrayList();
                        tmp.put(tuv, identicalSegmentsOfThreshold);
                        identicalSegmentsOfThreshold.add(tuv);
                        thresholdNoMatchWordCount += wordCount;
                    }
                    break;
                default:
                    break;
            }
        }
        targetPage.getWordCount().setRepetitionWordCount(
                repetitionWordCount + highFuzzyRepetionWordCount
                        + medHighFuzzyRepetionWordCount
                        + medFuzzyRepetitionWordCount
                        + subLevRepetitionWordCount);
        targetPage.getWordCount().setUnmatchedWordCount(noMatchWordCount);
        targetPage.getWordCount().setSubLevMatchWordCount(subLevMatchWordCount);
        targetPage.getWordCount().setSubLevRepetitionWordCount(subLevRepetitionWordCount);
        targetPage.getWordCount().setHiFuzzyRepetitionWordCount(highFuzzyRepetionWordCount);
        targetPage.getWordCount().setMedHighFuzzyRepetitionWordCount(medHighFuzzyRepetionWordCount);
        targetPage.getWordCount().setMedFuzzyRepetitionWordCount(medFuzzyRepetitionWordCount);
        targetPage.getWordCount().setTotalWordCount(totalWordCount);
        targetPage.getWordCount().setHiFuzzyWordCount(highFuzzyWordCount);
        targetPage.getWordCount().setMedHiFuzzyWordCount(medHighFuzzyWordCount);
        targetPage.getWordCount().setMedFuzzyWordCount(medFuzzyWordCount);
        targetPage.getWordCount().setLowFuzzyWordCount(subLevMatchWordCount);
        
        targetPage.getWordCount().setThresholdHiFuzzyWordCount(thresholdHiFuzzyWordCount);
        targetPage.getWordCount().setThresholdLowFuzzyWordCount(thresholdLowFuzzyWordCount);
        targetPage.getWordCount().setThresholdMedFuzzyWordCount(thresholdMedFuzzyWordCount);
        targetPage.getWordCount().setThresholdMedHiFuzzyWordCount(thresholdMedHiFuzzyWordCount);
        targetPage.getWordCount().setThresholdNoMatchWordCount(thresholdNoMatchWordCount);
    }
    
    /**
     * This method is used to update TU table, and set repeated flag and repetition flag.
     * @param sTuvs
     * @param p_matches
     * @param m_uniqueSegments
     */
    private static void saveRepetitionInfoToTu(ArrayList sTuvs,
            MatchTypeStatistics p_matches, Map m_uniqueSegments)
    {
        Set repetitionSet = new HashSet();
        Set unRepetitionSet = new HashSet();
        for (int i = 0; i < sTuvs.size(); i++)
        {
            SegmentTmTuv tuv = (SegmentTmTuv) sTuvs.get(i);
            Types types = p_matches.getTypes(tuv.getId(), ((SegmentTmTu) tuv
                    .getTu()).getSubId());
            int matchType = types == null ? MatchTypeStatistics.NO_MATCH : types
                    .getStatisticsMatchType();
            ArrayList identicalSegments = null;
            
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
                    TuImpl tu = HibernateUtil.get(TuImpl.class, tuId);
                
                    // worldserver xlf files are special, 
                    // get repeated and repetition infomation from TU.
                    // Because the tu list is sorted, all repeated TUs are in front
                    // of the list
                    if (tu.getGenerateFrom() != null && tu.getDataType() != null
                            && tu.getGenerateFrom().equalsIgnoreCase("worldserver")
                            && tu.getDataType().equalsIgnoreCase("xlf"))
                    {
                        if (tu.getSourceContent() != null
                                && tu.getSourceContent().equalsIgnoreCase("repeated"))
                        {
                            tu.setRepeated(true);
                            tu.setRepetitionOfId(0);
                            
                            m_uniqueSegments.put(tuv.getExactMatchKey(), tuId);
                            repetitionSet.add(tu);
                        }
                        else if (tu.getSourceContent() != null
                                && tu.getSourceContent().equalsIgnoreCase("repetition"))
                        {
                            long repeatedId = m_uniqueSegments.get(tuv
                                    .getExactMatchKey()) == null ? 0 : (Long) m_uniqueSegments
                                    .get(tuv.getExactMatchKey());
                            /*
                             * Sometimes, World Server XLF files don't have repeated segments
                             * for repetition segments. In this case, we should treat the first
                             * repetition as repeated.
                             */
                            if (repeatedId == 0)
                            {
                                tu.setRepeated(true);
                                tu.setRepetitionOfId(0);
                                m_uniqueSegments.put(tuv.getExactMatchKey(), tuId);
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
                        identicalSegments = (ArrayList) m_uniqueSegments.get(tuv);
                        /*
                         * If identicalSegments is not null, that means current TU has a same segment before,
                         * then we should get the former segment, mark it as repeated, and mark the current
                         * segment as repetition.
                         * 
                         * If identicalSegments is null, that means it's the first time that current segment
                         * appears.
                         * 
                         * Considering files can be added and removed from jobs, all TUs must update.
                         */
                        SegmentTmTuv latestTuv = identicalSegments == null ? null : (SegmentTmTuv) identicalSegments
                                .get(0);
                        
                        if (identicalSegments != null && 
                                latestTuv.getExactMatchKey() == tuv.getExactMatchKey())
                        {
                            long repeatedTuId = latestTuv.getTu().getId();
                            TuImpl repeatedTu = HibernateUtil.get(TuImpl.class, repeatedTuId);
                            repeatedTu.setRepeated(true);
                            // remove repeated TU from unRepetitionSet and add it to repetitionSet
                            repetitionSet.add(repeatedTu);
                            unRepetitionSet.remove(repeatedTu);
                            
                            tu.setRepetitionOfId(repeatedTuId);
                            repetitionSet.add(tu);
                        }
                        else
                        {
                            identicalSegments = new ArrayList();
                            m_uniqueSegments.put(tuv, identicalSegments);
                            identicalSegments.add(tuv);
                            // add the TU to unRepetitionSet
                            tu.setRepetitionOfId(0);
                            tu.setRepeated(false);
                            unRepetitionSet.add(tu);
                        }
                    }
                    break;
            }
        }
        if (repetitionSet.size() != 0)
        {
            HibernateUtil.update(repetitionSet);
        }
        if (unRepetitionSet.size() != 0)
        {
            HibernateUtil.update(unRepetitionSet);
        }
    }
    
}
