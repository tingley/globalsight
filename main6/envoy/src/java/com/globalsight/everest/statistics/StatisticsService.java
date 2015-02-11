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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.pageimport.StatisticsPersistenceCommand;
import com.globalsight.util.GlobalSightLocale;

/**
 * Provides statistic services for pages.
 */
public class StatisticsService
{
    static private GlobalSightCategory c_logger = (GlobalSightCategory) GlobalSightCategory
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
            ArrayList sTuvs = getSourceTuvs(p_sourcePage);
            ArrayList splittedTuvs = splitSourceTuvs(sTuvs, p_sourcePage
                    .getGlobalSightLocale());
            SegmentRepetition segmentRepetition = new SegmentRepetition(
                    splittedTuvs);
            String sourcePageId = p_sourcePage.getExternalPageId();
            boolean isDefaultContextMatch = isDefaultContextMatch(sourcePageId, p_sourcePage);
            
            Collection targetLocales = p_targetPages.keySet();
            Iterator ti = targetLocales.iterator();
            List<TargetPage> targetPages = new ArrayList<TargetPage>();

            while (ti.hasNext())
            {
                Long targetLocaleId = (Long) ti.next();
                
                // If the source file is WorldServer xliff file,MT translations
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

                PageWordCounts wordCount = calculatePageWordCounts(
                        segmentRepetition, matches, p_excludedTuTypes);

                TargetPage targetPage = (TargetPage) (p_targetPages
                        .get(targetLocaleId));

                wordCount.setNoUseExactMatchWordCount(wordCount
                        .getSegmentTmWordCount()
                        + wordCount.getMTExtractMatchWordCount()
                        + wordCount.getXliffExtractMatchWordCount());
                wordCount.setNoUseInContextMatchWordCount(0);
                wordCount = calculateInContextMatchWordCounts(wordCount,
                        splittedTuvs, matches, p_excludedTuTypes);
                if(isDefaultContextMatch && wordCount.getNoUseExactMatchWordCount() > 0)
                {
                    //Only consider the exact match word counts.
                    wordCount.setContextMatchWordCount(wordCount.getNoUseExactMatchWordCount());
                }
                
                targetPage.setWordCount(wordCount);
                targetPage.setIsDefaultContextMatch(isDefaultContextMatch && wordCount.getNoUseExactMatchWordCount() > 0);
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
            if(! sourceTuv.getSegment().equals(targetTuv.getSegment()))
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
        String sql = "select id from source_page where external_page_id LIKE '" + queryStr + "' and state not in ('OUT_OF_DATE','IMPORT_FAIL')";
        List list = HibernateUtil.searchWithSql(sql, null);
        list = removeCurrent(list, page.getId());
        for(int i = 0; i < list.size(); i++)
        {
            SourcePage sp = null;
            try
            {
                sp = ServerProxy.getPageManager().getSourcePage(((BigInteger) list.get(i)).longValue());
            }
            catch (Exception e)
            {
                c_logger.info("Can not get source page to compare" + e);
            }
            if(isSameOfSourcePage(page, sp))
            {
                return true;
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

            // get all segments of all source pages
            Workflow wf = (Workflow) p_workflows.get(0);
            wf = (WorkflowImpl) session.get(WorkflowImpl.class, wf
                    .getIdAsLong());
            ArrayList splitSourceTuvs = getAllSplitSourceTuvs(wf);
            // get the job's leverage match threshold
            int levMatchThreshold = wf.getJob().getLeverageMatchThreshold();
            SegmentRepetition segmentRepetition = new SegmentRepetition(
                    splitSourceTuvs);

            // calculate statistics per work flow
            Iterator itWf = p_workflows.iterator();
            while (itWf.hasNext())
            {
                wf = (Workflow) itWf.next();
                // get match types for all target segments in a workflow
                MatchTypeStatistics matches = getMatchTypeStatistics(wf,
                        levMatchThreshold);

                PageWordCounts wordCount = calculateWorkflowWordCounts(
                        segmentRepetition, matches, p_excludedTuTypes);

                wordCount.setNoUseInContextMatchWordCount(0);
                wordCount.setNoUseExactMatchWordCount(wordCount.getSegmentTmWordCount() 
                        + wordCount.getMTExtractMatchWordCount());

                // Update the In context match word count;
                int inContextWordCount = getAllInContextMatchWordCountInAllTargetPages(wf
                        .getAllTargetPages());
                int contextWordCount = getAllDefaultContextMatchWordCountInAllTargetPages(wf.getAllTargetPages());
                // wordCount =
                // calculateInContextMatchWordCounts(wordCount,splitSourceTuvs,matches);
                wordCount.setInContextWordCount(inContextWordCount);
                wordCount.setContextMatchWordCount(contextWordCount);
                wordCount.setSegmentTmWordCount(
                        wordCount.getSegmentTmWordCount()
                        + wordCount.getMTExtractMatchWordCount()
                        + wordCount.getXliffExtractMatchWordCount()
                        - wordCount.getInContextWordCount());

                wf.setInContextMatchWordCount(wordCount.getInContextWordCount());
                wf.setNoUseInContextMatchWordCount(wordCount.getNoUseInContextMatchWordCount());
                wf.setNoUseExactMatchWordCount(wordCount.getNoUseExactMatchWordCount());
                wf.setContextMatchWordCount(wordCount.getContextMatchWordCount());
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

    private static int getAllDefaultContextMatchWordCountInAllTargetPages(
            Vector allTargetPages)
    {
        int defaultContextMatchWordCount = 0;
        for(int i = 0; i < allTargetPages.size(); i++)
        {
            TargetPage tp = (TargetPage) allTargetPages.get(i);
            defaultContextMatchWordCount += tp.getWordCount().getContextMatchWordCount();
        }
        return defaultContextMatchWordCount;
    }

    private static int getAllInContextMatchWordCountInAllTargetPages(
            Vector allTargetPages)
    {
        int inContextWordCount = 0;
        for (int i = 0; i < allTargetPages.size(); i++)
        {
            TargetPage tp = (TargetPage) allTargetPages.get(i);
            inContextWordCount += tp.getWordCount().getInContextWordCount();
        }
        return inContextWordCount;
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
                - inContextMatchWordCount);

        return wordCount;
    }

    /**
     * Calculates the various word counts from the match types and source TUVs
     * passed in.
     * 
     * @param p_sourceTuvs Collection of subflow separated source Tuvs. Type of
     * elements is SegmentTmTuv. @param p_matches MatchTypeStatistics object
     */
    static private PageWordCounts calculatePageWordCounts(
            SegmentRepetition p_segmentRepetition,
            MatchTypeStatistics p_matches, Vector p_excludedTuTypes)
    {
        return calculateWorkflowWordCounts(p_segmentRepetition, p_matches, p_excludedTuTypes);
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
        int subLevMatchWordCount = 0;
        int subLevMatchRepWordCount = 0;
        int contextMatchWordCount = 0;
        int segmentTmWordCount = 0;
        int lowFuzzyWordCount = 0;
        int medFuzzyWordCount = 0;
        int medHiFuzzyWordCount = 0;
        int hiFuzzyWordCount = 0;
        int unmatchedWordCount = 0;
        int repetitionWordCount = 0;
        int mtExactMatchWordCount = 0;
        int xliffMatchWordCount = 0;

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

                if (types != null && types.isSubLevMatch())
                {
                    if (i < 1)
                    {
                        subLevMatchWordCount += wordCount;
                    }
                    else
                    {
                        subLevMatchRepWordCount += wordCount;
                    }
                }

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
                    lowFuzzyWordCount += wordCount;
                    break;
                case MatchTypeStatistics.MED_FUZZY:
                    medFuzzyWordCount += wordCount;
                    break;
                case MatchTypeStatistics.MED_HI_FUZZY:
                    medHiFuzzyWordCount += wordCount;
                    break;
                case MatchTypeStatistics.HI_FUZZY:
                    hiFuzzyWordCount += wordCount;
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
            }
        }

        PageWordCounts result = new PageWordCounts();
        result.setSubLevMatchWordCount(subLevMatchWordCount);
        result.setSubLevRepetitionWordCount(subLevMatchRepWordCount);
        result.setContextMatchWordCount(contextMatchWordCount);
        result.setSegmentTmWordCount(segmentTmWordCount);
        result.setUnmatchedWordCount(unmatchedWordCount);
        result.setLowFuzzyWordCount(lowFuzzyWordCount);
        result.setMedFuzzyWordCount(medFuzzyWordCount);
        result.setMedHiFuzzyWordCount(medHiFuzzyWordCount);
        result.setHiFuzzyWordCount(hiFuzzyWordCount);
        result.setRepetitionWordCount(repetitionWordCount);
        result.setMTExtractMatchWordCount(mtExactMatchWordCount);
        result.setXliffExtractMatchWordCount(xliffMatchWordCount);
        result.setTotalWordCount(contextMatchWordCount + segmentTmWordCount 
                + mtExactMatchWordCount + xliffMatchWordCount
                + lowFuzzyWordCount + medFuzzyWordCount + medHiFuzzyWordCount
                + hiFuzzyWordCount + unmatchedWordCount + repetitionWordCount);

        return result;
    }

    static public ArrayList getSourceTuvs(SourcePage p_sourcePage)
    {
        ArrayList result = new ArrayList();
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
    static public ArrayList splitSourceTuvs(ArrayList p_sourceTuvs,
            GlobalSightLocale p_sourceLocale) throws Exception
    {
        // convert Tu, Tuv to PageTmTu, PageTmTuv to split segments
        ArrayList pageTmTuList = new ArrayList(p_sourceTuvs.size());

        Iterator itOriginal = p_sourceTuvs.iterator();
        while (itOriginal.hasNext())
        {
            Tuv originalTuv = (Tuv) itOriginal.next();
            Tu originalTu = originalTuv.getTu();

            PageTmTu pageTmTu = new PageTmTu(originalTu.getId(), 0, originalTu
                    .getDataType(), originalTu.getTuType(), !originalTu
                    .isLocalizable());

            PageTmTuv pageTmTuv = new PageTmTuv(originalTuv.getId(),
                    originalTuv.getGxml(), p_sourceLocale);
            pageTmTuv.setSid(originalTuv.getSid());

            pageTmTu.addTuv(pageTmTuv);

            pageTmTuList.add(pageTmTu);
        }
        // make a list of splitted segment Tus
        ArrayList splittedTus = new ArrayList(pageTmTuList.size());

        Iterator itPageTmTu = pageTmTuList.iterator();
        while (itPageTmTu.hasNext())
        {
            PageTmTu pageTmTu = (PageTmTu) itPageTmTu.next();
            Collection segmentTmTus = TmUtil.createSegmentTmTus(pageTmTu,
                    p_sourceLocale);
            splittedTus.addAll(segmentTmTus);
        }

        // make a list of SegmentTmTuv from a list of SegmentTmTu
        ArrayList splittedTuvs = new ArrayList(splittedTus.size());
        Iterator itSegmentTmTu = splittedTus.iterator();
        while (itSegmentTmTu.hasNext())
        {
            SegmentTmTu tu = (SegmentTmTu) itSegmentTmTu.next();
            splittedTuvs.add(tu.getFirstTuv(p_sourceLocale));
        }

        return splittedTuvs;
    }

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
                ArrayList sTuvs = getSourceTuvs(sourcePage);
                splitTuvs.addAll(splitSourceTuvs(sTuvs, sourcePage
                        .getGlobalSightLocale()));
            }
        }

        return splitTuvs;
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
}
