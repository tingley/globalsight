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
package com.globalsight.ling.inprogresstm;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.WorkflowRequest;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.inprogresstm.leverage.Leverager;
import com.globalsight.ling.inprogresstm.persistence.IndexPersistence;
import com.globalsight.ling.inprogresstm.persistence.TmPersistence;
import com.globalsight.ling.inprogresstm.population.TmPopulater;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Implementation of InProgressTmManager
 */
public class InProgressTmManagerLocal implements InProgressTmManager
{
    private static final Logger c_logger = Logger
            .getLogger(InProgressTmManagerLocal.class.getName());

    /**
     * Performs a dynamic leveraging for a translatable text. It first queries
     * in the in-progress TM and then in the Gold TM. It returns the leverage
     * results in DynamicLeverageResults object. All leverage options are taken
     * from the system objects retrieved based on the given source page id.
     * 
     * The source text parameter can be either a top level text or a subflow. If
     * it is a top level text, it may or may not include subflows. However, the
     * method leverages only the top level text and the leverage results don't
     * include subflows even if the source includes subflows. Subflows must be
     * leveraged separately.
     * 
     * When leveraging a subflow, segment type parameter must be a type of the
     * subflow (e.g. url-a if the subflow is a href value in <a> tag).
     * 
     * @param p_sourceText
     *            Source text in GXML format. The top <segment> tag may or may
     *            not present.
     * @param p_targetLocale
     *            target locale
     * @param p_segmentType
     *            segment type (text, string, etc)
     * @param p_sourcePageId
     *            source page id
     * @return DynamicLeverageResults object
     */
    public DynamicLeverageResults leverageTranslatable(String p_sourceText,
            GlobalSightLocale p_targetLocale, String p_segmentType,
            long p_sourcePageId) throws LingManagerException
    {
        SourcePage sourcePage = getSourcePage(p_sourcePageId);
        GlobalSightLocale sourceLocale = sourcePage.getGlobalSightLocale();
        BaseTmTuv srcTuv = TmUtil.createTmSegment(p_sourceText, 0,
                sourceLocale, p_segmentType, true);
        LeverageOptions leverageOptions = getLeverageOptions(sourcePage,
                p_targetLocale);

        return leverage(srcTuv, p_targetLocale, sourcePage, leverageOptions);
    }

    /**
     * Performs a dynamic leveraging for a localizable text. It first queries in
     * the in-progress TM and then in the Gold TM. It returns the leverage
     * results in DynamicLeverageResults object. All leverage options are taken
     * from the system objects retrieved based on the given source page id.
     * 
     * The source text parameter can be either a top level text or a subflow. If
     * it is a top level text, it may or may not include subflows. However, the
     * method leverages only the top level text and the leverage results don't
     * include subflows even if the source includes subflows. Subflows must be
     * leveraged separately.
     * 
     * When leveraging a subflow, segment type parameter must be a type of the
     * subflow (e.g. url-a if the subflow is a href value in <a> tag).
     * 
     * @param p_sourceText
     *            Source text in GXML format. The top <localizable> tag may or
     *            may not present.
     * @param p_targetLocale
     *            target locale
     * @param p_segmentType
     *            segment type (css-color, img-height, etc)
     * @param p_sourcePageId
     *            source page id
     * @return DynamicLeverageResults object
     */
    public DynamicLeverageResults leverageLocalizable(String p_sourceText,
            GlobalSightLocale p_targetLocale, String p_segmentType,
            long p_sourcePageId) throws LingManagerException
    {
        SourcePage sourcePage = getSourcePage(p_sourcePageId);
        GlobalSightLocale sourceLocale = sourcePage.getGlobalSightLocale();
        BaseTmTuv srcTuv = TmUtil.createTmSegment(p_sourceText, 0,
                sourceLocale, p_segmentType, false);
        LeverageOptions leverageOptions = getLeverageOptions(sourcePage,
                p_targetLocale);

        return leverage(srcTuv, p_targetLocale, sourcePage, leverageOptions);
    }

    /**
     * Performs a dynamic leveraging. It first queries in the in-progress TM and
     * then in the Gold TM. It returns the leverage results in
     * DynamicLeverageResults object. All leverage options are taken from the
     * system objects retrieved based on the given source page id.
     * 
     * The source TUV must be a complete segment which includes a top level text
     * as well as all subflows. This method leverages only specified part of the
     * segment, a top level text or a certain subflow. The sub id indicates
     * which part of the segment is leveraged. To leverage a top level text, the
     * sub id should be com.globalsight.ling.tm2.SegmentTmTu.ROOT.
     * 
     * When leveraging a merged segment, the source TUV must be a merged source
     * TUV and subflow ids must be adjusted so they are unique in the segment.
     * 
     * @param p_sourceTuv
     *            source TUV object
     * @param p_subId
     *            Subflow id
     * @param p_targetLocale
     *            target locale
     * @param p_sourcePageId
     *            source page id
     * @return DynamicLeverageResults object
     */
    public DynamicLeverageResults leverage(Tuv p_sourceTuv, String p_subId,
            GlobalSightLocale p_targetLocale, long p_sourcePageId)
            throws LingManagerException
    {
        SourcePage sourcePage = getSourcePage(p_sourcePageId);
        long jobId = sourcePage.getJobId();
        BaseTmTuv srcTuv = TmUtil.createTmSegment(p_sourceTuv, p_subId, jobId);
        LeverageOptions leverageOptions = getLeverageOptions(sourcePage,
                p_targetLocale);

        return leverage(srcTuv, p_targetLocale, sourcePage, leverageOptions);
    }

    private DynamicLeverageResults leverage(BaseTmTuv p_sourceTuv,
            GlobalSightLocale p_targetLocale, SourcePage p_sourcePage,
            LeverageOptions p_leverageOptions) throws LingManagerException
    {
        DynamicLeverageResults results = null;
        Connection conn = null;

        try
        {
            // leverage from in-progress TM
            if (p_leverageOptions.dynamicLeveragesFromInProgressTm())
            {
                conn = DbUtil.getConnection();
                Leverager leverager = new Leverager(conn);

                // Leverage from current job's in-progress TM data.
                long jobId = p_sourcePage.getJobId();
                Set<Long> jobIdsToLevFrom = new HashSet<Long>();
                jobIdsToLevFrom.add(jobId);
                // Leverage from jobs which are specified by TM profile options
                Set<Long> tmIds = new HashSet<Long>();
                tmIds = p_leverageOptions.getTmIdsForLevInProgressTmPurpose();

                if (p_sourceTuv.isTranslatable())
                {
                    results = leverager.leverageTranslatable(p_sourceTuv,
                            p_targetLocale, p_leverageOptions, jobId,
                            jobIdsToLevFrom, tmIds);
                }
                else
                {
                    GlobalSightLocale sourceLocale = p_sourcePage
                            .getGlobalSightLocale();
                    results = leverager.leverageLocalizable(p_sourceTuv,
                            sourceLocale, p_targetLocale, p_leverageOptions,
                            jobId, jobIdsToLevFrom, tmIds);
                }
            }

            // leverage from gold TM
            if (p_leverageOptions.dynamicLeveragesFromGoldTm())
            {
                long jobId = p_sourcePage.getJobId();
                TmCoreManager tmcm = LingServerProxy.getTmCoreManager();
                tmcm.setJobId(jobId);
                DynamicLeverageResults goldResult = tmcm.leverageSegment(
                        p_sourceTuv, p_leverageOptions);

                if (results == null)
                {
                    results = goldResult;
                }
                else
                {
                    results.merge(goldResult);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        if (results == null)
        {
            // create an empty DynamicLeverageResults. Cannot return null.
            results = new DynamicLeverageResults(p_sourceTuv.getSegment(),
                    p_sourcePage.getGlobalSightLocale(), p_targetLocale,
                    p_sourceTuv.isTranslatable());
        }

        results.serOrgSid(p_sourceTuv.getSid());

        boolean isTmProcedence = p_leverageOptions.isTmProcedence();
        long jobId = p_sourcePage.getJobId();
        if (isTmProcedence)
        {
            results.generalSortByTm(p_leverageOptions, jobId);
        }
        else
        {
            results.generalSort(p_leverageOptions, jobId);
        }
        return results;
    }

    /**
     * Saves a source and target segment pair to the in-progress TM.
     * 
     * The source and target TUVs must be a complete segment which includes a
     * top level text as well as all subflows. This method saves only specified
     * part of the segments, a top level text or a certain subflow. The sub id
     * indicates which part of the segment is saved. To save a top level text,
     * the sub id should be com.globalsight.ling.tm2.SegmentTmTu.ROOT.
     * 
     * When saving a merged segment, both source and target TUVs must be merged
     * TUVs and subflow ids must be adjusted so they are unique in the segment.
     * 
     * @param p_sourceTuv
     *            source TUV
     * @param p_targetTuv
     *            target TUV
     * @param p_subId
     *            subflow id
     * @param p_sourcePageId
     *            Source page id
     */
    public void save(Tuv p_sourceTuv, Tuv p_targetTuv, String p_subId,
            long p_sourcePageId) throws LingManagerException
    {
        try
        {
            SourcePage sourcePage = getSourcePage(p_sourcePageId);
            long jobId = sourcePage.getJobId();
            BaseTmTuv srcTuv = TmUtil.createTmSegment(p_sourceTuv, p_subId,
                    jobId);
            BaseTmTuv trgTuv = TmUtil.createTmSegment(p_targetTuv, p_subId,
                    jobId);

            TmPopulater tmPopulater = new TmPopulater();
            tmPopulater.saveSegment(srcTuv, trgTuv, sourcePage);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
    }

    /**
     * Saves a source and target translatable segment pair to the in-progress
     * TM.
     * 
     * The source and target text parameter can be either a top level text or a
     * subflow. If it is a top level text, it may or may not include subflows.
     * However, the method saves only the top level text and subflows are
     * discarded. Subflows must be saved separately.
     * 
     * When saving a subflow, segment type parameter must be a type of the
     * subflow (e.g. url-a if the subflow is a href value in <a> tag).
     * 
     * When saving a merged segment, both source and target text must be merged
     * text and the TU id must be of the first TU.
     * 
     * @param p_sourceText
     *            Source text in GXML format. The top <segment> tag may or may
     *            not present.
     * @param p_targetText
     *            Target text in GXML format. The top <segment> tag may or may
     *            not present.
     * @param p_tuId
     *            TU id of the segment
     * @param p_targetLocale
     *            target locale
     * @param p_segmentType
     *            segment type
     * @param p_sourcePageId
     *            Source page id
     */
    public void saveTranslatable(String p_sourceText, String p_targetText,
            long p_tuId, GlobalSightLocale p_targetLocale,
            String p_segmentType, long p_sourcePageId)
            throws LingManagerException
    {
        saveSegmentByText(p_sourceText, p_targetText, p_tuId, p_targetLocale,
                p_segmentType, p_sourcePageId, true);
    }

    /**
     * Saves a source and target localizable segment pair to the in-progress TM.
     * 
     * The source and target text parameter can be either a top level text or a
     * subflow. If it is a top level text, it may or may not include subflows.
     * However, the method saves only the top level text and subflows are
     * discarded. Subflows must be saved separately.
     * 
     * When saving a subflow, segment type parameter must be a type of the
     * subflow (e.g. url-a if the subflow is a href value in <a> tag).
     * 
     * When saving a merged segment, both source and target text must be merged
     * text and the TU id must be of the first TU.
     * 
     * @param p_sourceText
     *            Source text in GXML format. The top <localizable> tag may or
     *            may not present.
     * @param p_targetText
     *            Target text in GXML format. The top <localizable> tag may or
     *            may not present.
     * @param p_tuId
     *            TU id of the segment
     * @param p_targetLocale
     *            target locale
     * @param p_segmentType
     *            segment type
     * @param p_sourcePageId
     *            Source page id
     */
    public void saveLocalizable(String p_sourceText, String p_targetText,
            long p_tuId, GlobalSightLocale p_targetLocale,
            String p_segmentType, long p_sourcePageId)
            throws LingManagerException
    {
        saveSegmentByText(p_sourceText, p_targetText, p_tuId, p_targetLocale,
                p_segmentType, p_sourcePageId, false);
    }

    private void saveSegmentByText(String p_sourceText, String p_targetText,
            long p_tuId, GlobalSightLocale p_targetLocale,
            String p_segmentType, long p_sourcePageId, boolean p_isTranslatable)
            throws LingManagerException
    {
        try
        {
            SourcePage sourcePage = getSourcePage(p_sourcePageId);
            GlobalSightLocale sourceLocale = sourcePage.getGlobalSightLocale();

            BaseTmTuv srcTuv = TmUtil.createTmSegment(p_sourceText, p_tuId,
                    sourceLocale, p_segmentType, p_isTranslatable);
            BaseTmTuv trgTuv = TmUtil.createTmSegment(p_targetText, p_tuId,
                    p_targetLocale, p_segmentType, p_isTranslatable);

            TmPopulater tmPopulater = new TmPopulater();
            tmPopulater.saveSegment(srcTuv, trgTuv, sourcePage);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
    }

    /**
     * Delete segments and their indexes that belong to a job from the
     * in-progress TM.
     * 
     * @param p_jobId
     *            job id
     */
    public void deleteSegments(long p_jobId) throws LingManagerException
    {
        c_logger.debug("deleteSegments called with job id: " + p_jobId);

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            TmPersistence tmPersistence = new TmPersistence(conn);

            // get table lock
            tmPersistence.getLockForDeletion();

            // delete indexes
            IndexPersistence indexPersistence = new IndexPersistence(conn);
            indexPersistence.delete(p_jobId);

            // delete segments
            tmPersistence.deleteSegments(p_jobId);

            conn.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                conn.rollback();
            }
            catch (Exception se)
            {
                c_logger.error("rollback failed", se);
            }

            throw new LingManagerException(e);
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    DbUtil.unlockTables(conn);
                    conn.setAutoCommit(true);
                    DbUtil.returnConnection(conn);
                }
                catch (Exception e)
                {
                    throw new LingManagerException(e);
                }
            }
        }

    }

    /**
     * Get "LeverageOptions" for specified target locale.
     */
    public LeverageOptions getLeverageOptions(SourcePage p_sourcePage,
            GlobalSightLocale p_targetLocale)
    {
        Request req = p_sourcePage.getRequest();
        L10nProfile l10nProfile = req.getL10nProfile();
        LeveragingLocales allLeveragingLocales = l10nProfile
                .getLeveragingLocales();

        // create LeveragingLocales that contains only leveraging
        // locales for p_targetLocale
        LeveragingLocales leveragingLocalesForTarget = new LeveragingLocales();
        Set<GlobalSightLocale> locales = null;
        try
        {
            locales = allLeveragingLocales.getLeveragingLocales(p_targetLocale);
        }
        catch (Exception e)
        {
            // couldn't find the leverage locales in the l10nprofile list
            // so look through any added workflow
            Collection wrList = req.getJob().getWorkflowRequestList();
            if (wrList != null && wrList.size() > 0)
            {
                boolean found = false;
                for (Iterator i = wrList.iterator(); !found && i.hasNext();)
                {
                    WorkflowRequest wr = (WorkflowRequest) i.next();
                    Collection<WorkflowTemplateInfo> wfTemplate = wr
                            .getWorkflowTemplateList();
                    for (Iterator j = wfTemplate.iterator(); !found
                            && j.hasNext();)
                    {
                        WorkflowTemplateInfo wti = (WorkflowTemplateInfo) j
                                .next();
                        if (wti.getTargetLocale().equals(p_targetLocale))
                        {
                            locales = wti.getLeveragingLocales();
                            found = true;
                        }
                    }
                }

            }
        }

        leveragingLocalesForTarget.setLeveragingLocale(p_targetLocale,
                new HashSet<GlobalSightLocale>(locales));

        return new LeverageOptions(l10nProfile.getTranslationMemoryProfile(),
                leveragingLocalesForTarget);
    }

    // get SourcePage object from source page id
    private SourcePage getSourcePage(long p_sourcePageId)
            throws LingManagerException
    {
        SourcePage sourcePage = null;
        try
        {
            sourcePage = ServerProxy.getPageManager().getSourcePage(
                    p_sourcePageId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }

        return sourcePage;
    }

}
