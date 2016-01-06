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
package com.globalsight.ling.tm2.leverage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandlerLocal;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.inprogresstm.DynamicLeverageResults;
import com.globalsight.ling.inprogresstm.DynamicLeveragedSegment;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.persistence.PageTmPersistence;
import com.globalsight.ling.tm2.segmenttm.Tm2SegmentTmInfo;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.util.GlobalSightLocale;

/**
 * Leverager is responsible for leveraging segments
 */

public class Leverager
{
    private static final Logger c_logger = Logger.getLogger(Leverager.class
            .getName());

    private Session m_session;

    /**
     * Storage TM's project TM index is -1. The default TM index for local TM is
     * the TM ID.
     */
    public static final int HIGHEST_PRIORTIY = -1;
    /** MT's project TM index is -2 */
    public static final int MT_PRIORITY = -2;
    /** Xliff project TM index is -3 */
    public static final int XLIFF_PRIORITY = -3;
    /** Remote TM project TM index is -4 */
    public static final int REMOTE_TM_PRIORITY = -4;
    /** Remote TM project TM index is -5 */
    public static final int TDA_TM_PRIORITY = -5;
    /** PO TM project TM index is -6 */
    public static final int PO_TM_PRIORITY = -6;
    /** In Progress TM matches is -7 */
    public static final int IN_PROGRESS_TM_PRIORITY = -7;

    public Leverager(Session p_session)
    {
        m_session = p_session;
    }

    /**
     * Leverage a given page. It does: - leverage Page Tm - leverage Segment Tm
     * - apply leverage options for both leverage matches - save matches to the
     * database - returns a list of exact matched segments
     * 
     * @param p_sourcePage
     *            source page
     * @param p_leverageDataCenter
     *            LeverageDataCenter object
     */
    public void leveragePage(SourcePage p_sourcePage,
            LeverageDataCenter p_leverageDataCenter, List<Tm> tm2Tms,
            List<Tm> tm3Tms) throws LingManagerException
    {
        Connection conn = null;
        try
        {
//            conn = DbUtil.getConnection();
            LeverageOptions leverageOptions = p_leverageDataCenter
                    .getLeverageOptions();

            LeverageMatchResults levMatchResult;

            /**
            // Leverage from Page TM if the user didn't choose latest
            // leveraging for re-import
            if (!leverageOptions.isLatestLeveragingForReimport())
            {
                PageTmLeverager ptLeverager = new PageTmLeverager();
                levMatchResult = ptLeverager.leverage(conn, p_sourcePage,
                        p_leverageDataCenter);
                if (levMatchResult != null)
                {
                    p_leverageDataCenter
                            .addLeverageResultsOfWholeSegment(levMatchResult);
                    // apply Page Tm leverage options
                    p_leverageDataCenter.applyPageTmOptions();
                }
            }
            else
            {
                PageTmPersistence ptPersistence = new PageTmPersistence(conn);
				GlobalSightLocale sourceLocale = p_sourcePage
						.getGlobalSightLocale();
                long tmId = ptPersistence.getPageTmId(
                        p_sourcePage.getExternalPageId(), sourceLocale);

                // latest leveraging for re-import:
                // if a page tm for the page exists, latest re-import is true
                leverageOptions.setLatestReimport(tmId != 0);
            }
			**/

            long jobId = p_sourcePage.getJobId();
            Job job = ServerProxy.getJobHandler().getJobById(jobId);

            if (!leverageOptions.dynamicLeveragesStopSearch())
            {
                levMatchResult = new LeverageMatchResults();
                // Leverage from Segment Tms.
                if (tm2Tms.size() > 0)
                {
                    levMatchResult = new Tm2SegmentTmInfo().leverage(tm2Tms,
                            p_leverageDataCenter, jobId);
                }
                if (tm3Tms.size() > 0)
                {
                    levMatchResult.merge(new Tm3SegmentTmInfo().leverage(tm3Tms,
                            p_leverageDataCenter, jobId));
                }
    
                p_leverageDataCenter.setJob(job);
                p_leverageDataCenter
                        .addLeverageResultsOfSegmentTmMatching(levMatchResult);
    
                // apply Segment Tm leverage options
                p_leverageDataCenter.applySegmentTmOptions();
            }
            else
            {
                leverageDataCenterWithStopSearch(job, p_leverageDataCenter,
                        tm2Tms, tm3Tms);
            }
        }
        catch (LingManagerException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * GBS-3282 hierarchical TMs stop search after hitting 100% match
     * 
     */
    public void leverageDataCenterWithStopSearch(Job job,
            LeverageDataCenter p_leverageDataCenter, List<Tm> tm2Tms,
            List<Tm> tm3Tms) throws Exception
    {
        p_leverageDataCenter.setDynLevStopSearch(true);
        LeverageMatchResults levMatchResultHere = new LeverageMatchResults();
        List<Tm> temp = new ArrayList<Tm>(1);
        ArrayList<String> dynLevExactMatchs = new ArrayList<String>();
        long jobId = job == null ? -1 : job.getId();
        for (Tm tm2 : tm2Tms)
        {
            temp.clear();
            temp.add(tm2);
            LeverageMatchResults tmLevMatchResults = new LeverageMatchResults();
            tmLevMatchResults = new Tm2SegmentTmInfo().leverage(temp,
                    p_leverageDataCenter, jobId);

            tmLevMatchResults = checkMatchResult(p_leverageDataCenter, job,
                    tmLevMatchResults, dynLevExactMatchs);

            levMatchResultHere.merge(tmLevMatchResults);
        }

        for (Tm tm3 : tm3Tms)
        {
            temp.clear();
            temp.add(tm3);
            LeverageMatchResults tmLevMatchResults = new LeverageMatchResults();
            tmLevMatchResults.merge(new Tm3SegmentTmInfo().leverage(temp,
                    p_leverageDataCenter, jobId));

            tmLevMatchResults = checkMatchResult(p_leverageDataCenter, job,
                    tmLevMatchResults, dynLevExactMatchs);
            levMatchResultHere.merge(tmLevMatchResults);
        }

        p_leverageDataCenter.setJob(job);
        p_leverageDataCenter
                .addLeverageResultsOfSegmentTmMatching(levMatchResultHere);
        p_leverageDataCenter.setDynLevStopSearch(false);
        dynLevExactMatchs.clear();
        dynLevExactMatchs = null;
    }

    private LeverageMatchResults checkMatchResult(
            LeverageDataCenter p_leverageDataCenter, Job p_job,
            LeverageMatchResults tmLevMatchResults,
            ArrayList<String> dynLevExactMatchs) throws Exception
    {
        long jobId = p_job == null ? -1 : p_job.getId();
        Collection ccc = p_leverageDataCenter.getTargetLocales();
        GlobalSightLocale srcLocale = p_leverageDataCenter.getSourceLocale();
        Iterator<LeverageMatches> itLeverageMatches = tmLevMatchResults
                .iterator();
        while (itLeverageMatches.hasNext())
        {
            LeverageMatches levMatches = itLeverageMatches.next();

            // value of m_uniqueSeparatedSegments can be null
            if (levMatches != null)
            {
                levMatches.setJob(p_job);
                levMatches.applySegmentTmOptions();
            }

            BaseTmTuv tuv = levMatches.getOriginalTuv();
            Set locales = levMatches.getExactMatchLocales(jobId);
            boolean isAllMatched = true;
            for (Object object : ccc)
            {
                GlobalSightLocale gsl = (GlobalSightLocale) object;

                // hit exact match
                if (locales.contains(gsl))
                {
                    String key = tuv.getId() + "|" + srcLocale.toString() + "|"
                            + gsl.toString();
                    // already hit exact match in previous tm, ignore
                    if (dynLevExactMatchs.contains(key))
                    {
                        levMatches.removeTargetLocale(gsl);
                    }
                    // hit new exact match, save this match
                    else
                    {
                        dynLevExactMatchs.add(key);
                    }
                }
                // no new exact match
                else
                {
                    isAllMatched = false;
                }
            }

            if (isAllMatched)
            {
                String key = tuv.getId() + "" + srcLocale.toString();

                p_leverageDataCenter.addDynLevExactMatch(key);
            }
            
        }
        
        return tmLevMatchResults;
    }

    /**
     * Leverage just one segment and returns the result in
     * DynamicLeverageResults object.
     * 
     * @param p_sourceTuv
     *            source segment
     * @param p_leverageOptions
     *            leverage options. It should contain only one target locale in
     *            LeveragingOptions.
     * @return DynamicLeverageResults
     */
    public DynamicLeverageResults leverageSegment(BaseTmTuv p_sourceTuv,
            LeverageOptions p_leverageOptions, List<Tm> tm2Tms, List<Tm> tm3Tms)
            throws Exception
    {
        Job j = null;
        try
        {
            if (getJobId() != -1)
            {
                j = (new JobHandlerLocal()).getJobById(getJobId());
            }
        }
        catch (Exception e)
        {
            // ignore
            j = null;
        }
        long jobId = j != null ? j.getId() : -1;
        
        if (!p_leverageOptions.dynamicLeveragesStopSearch())
        {
            LeverageMatches levMatches = new Tm2SegmentTmInfo()
                    .leverageSegment(p_sourceTuv, p_leverageOptions, tm2Tms);
            LeverageMatches tm3Matches = new Tm3SegmentTmInfo()
                    .leverageSegment(p_sourceTuv, p_leverageOptions, tm3Tms);
            if (tm3Matches != null)
            {
                levMatches.merge(tm3Matches);
            }
            
            levMatches.setJob(j);

            // apply leverage option.
            levMatches.applySegmentTmOptions();
            // remove STATISTICS_MATCH and NOT_A_MATCH
            levMatches.removeNoMatches();

            // create DynamicLeverageResults from LeverageMatches
            GlobalSightLocale targetLocale = getFirstTargetLocale(p_leverageOptions);
            return createDynamicLeverageResults(levMatches, p_sourceTuv,
                    targetLocale, jobId);
        }
        else
        {
            List<Tm> temp = new ArrayList<Tm>(1);
            boolean isExactMatch = false;
            LeverageMatches alllevMatches = new LeverageMatches(p_sourceTuv, p_leverageOptions);
            
            for (Tm tm2 : tm2Tms)
            {
                temp.clear();
                temp.add(tm2);

                LeverageMatches levMatches = new Tm2SegmentTmInfo()
                        .leverageSegment(p_sourceTuv, p_leverageOptions, temp);

                if (levMatches != null)
                {
                    levMatches.setJob(j);
                    // apply leverage option.
                    levMatches.applySegmentTmOptions();
                    // remove STATISTICS_MATCH and NOT_A_MATCH
                    levMatches.removeNoMatches();
                    
                    alllevMatches.merge(levMatches);
                    
                    ExactLeverageMatch elm = levMatches.getExactLeverageMatch(jobId);
                    // find exact match, then stop search other tms
                    if (elm != null && elm.targetLocaleIterator().hasNext())
                    {
                        isExactMatch = true;
                        break;
                    }
                }
            }
            
            if (!isExactMatch)
            {
                for (Tm tm3 : tm3Tms)
                {
                    temp.clear();
                    temp.add(tm3);

                    LeverageMatches tm3Matches = new Tm3SegmentTmInfo()
                            .leverageSegment(p_sourceTuv, p_leverageOptions, temp);

                    if (tm3Matches != null)
                    {
                        tm3Matches.setJob(j);
                        // apply leverage option.
                        tm3Matches.applySegmentTmOptions();
                        // remove STATISTICS_MATCH and NOT_A_MATCH
                        tm3Matches.removeNoMatches();
                        
                        alllevMatches.merge(tm3Matches);

                        ExactLeverageMatch elm = tm3Matches.getExactLeverageMatch(jobId);
                        // find exact match, then stop search other tms
                        if (elm != null && elm.targetLocaleIterator().hasNext())
                        {
                            isExactMatch = true;
                            break;
                        }
                    }
                }
            }

            // create DynamicLeverageResults from LeverageMatches
            GlobalSightLocale targetLocale = getFirstTargetLocale(p_leverageOptions);
            return createDynamicLeverageResults(alllevMatches, p_sourceTuv,
                    targetLocale, jobId);
        }
    }

    private DynamicLeverageResults createDynamicLeverageResults(
            LeverageMatches p_leverageMatches, BaseTmTuv p_sourceSegment,
            GlobalSightLocale p_targetLocale, long p_jobId)
    {
        GlobalSightLocale sourceLocale = p_sourceSegment.getLocale();
        LeverageOptions leverageOptions = p_leverageMatches
                .getLeverageOptions();
        DynamicLeverageResults dynamicLeverageResults = new DynamicLeverageResults(
                p_sourceSegment.getSegment(), sourceLocale, p_targetLocale,
                p_sourceSegment.isTranslatable());

        // populate DynamicLeverageResults
        for (Iterator it = p_leverageMatches.matchIterator(p_targetLocale,
                p_jobId); it.hasNext();)
        {
            LeveragedTuv trgTuv = (LeveragedTuv) it.next();
            long tmId = trgTuv.getTu().getTmId();
            int projectTmIndex = getProjectTmIndex(leverageOptions, tmId);
            BaseTmTuv srcTuv = trgTuv.getTu().getFirstTuv(sourceLocale);
            int matchCategory = DynamicLeveragedSegment.FROM_GOLD_TM;

            TuvBasicInfo tuvBasicInfo = null;
            try
            {
                tuvBasicInfo = LingServerProxy.getTmCoreManager()
                        .getTuvBasicInfoByTuvId(tmId, trgTuv.getId(),
                                trgTuv.getLocale().getId());
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            DynamicLeveragedSegment leveragedSegment = new DynamicLeveragedSegment(
                    srcTuv.getSegment(), trgTuv.getSegment(), sourceLocale,
                    trgTuv.getLocale(), trgTuv.getMatchState(),
                    trgTuv.getScore(), matchCategory, tmId, trgTuv.getId());
            leveragedSegment.setTmIndex(projectTmIndex);
            leveragedSegment.setMatchedTuvBasicInfo(tuvBasicInfo);
            leveragedSegment.setModifyDate(trgTuv.getModifyDate());
            leveragedSegment.setSid(trgTuv.getSid());
            dynamicLeverageResults.add(leveragedSegment);

        }

        return dynamicLeverageResults;
    }

    public static int getProjectTmIndex(LeverageOptions leverageOptions,
            long tmId)
    {
        Map<Long, Integer> tmIndexs = leverageOptions
                .getTmIndexsToLeverageFrom();
        // ToDo: The save TM have the highest priority.
        return (tmIndexs.get(tmId) == null) ? HIGHEST_PRIORTIY : tmIndexs
                .get(tmId);
    }

    private GlobalSightLocale getFirstTargetLocale(
            LeverageOptions p_leverageOptions)
    {
        GlobalSightLocale targetLocale = null;
        LeveragingLocales levLocales = p_leverageOptions.getLeveragingLocales();

        Iterator it = levLocales.getAllTargetLocales().iterator();
        if (it.hasNext())
        {
            targetLocale = (GlobalSightLocale) it.next();
        }

        return targetLocale;
    }

    private long jobId = -1;

    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }

    public long getJobId()
    {
        return this.jobId;
    }
}
