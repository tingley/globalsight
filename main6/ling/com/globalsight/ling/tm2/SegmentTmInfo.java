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
package com.globalsight.ling.tm2;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.tm.StatisticsInfo;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.indexer.Reindexer;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatchResults;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.ling.tm2.segmenttm.Tm2SegmentTmInfo;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProgressReporter;

/**
 * Interface to abstract out the details of a segment TM implementation (tm2 vs
 * tm3).
 * 
 * @see     Tm2SegmentTmInfo
 * @see     Tm3SegmentTmInfo
 */
public interface SegmentTmInfo
{

    public TuvMappingHolder saveToSegmentTm(
            Collection<? extends BaseTmTu> p_segmentsToSave,
            GlobalSightLocale p_sourceLocale, Tm p_tm,
            Set<GlobalSightLocale> p_targetLocales, int p_mode,
            boolean p_fromTmImport) throws LingManagerException;

    /**
     * Updates existing TUVs in a Segment Tm.
     * 
     * @param p_tmId
     *            Tm id in which segments are updated
     * @param p_tuvs
     *            Collection of SegmentTmTuv objects.
     * 
     * @throws LingManagerException
     */
    public void updateSegmentTmTuvs(Tm p_tm, Collection<SegmentTmTuv> p_tuvs)
            throws LingManagerException;

    /**
     * leverage a page from Segment Tm
     * 
     * @param p_tms
     *            tms to leverage
     * @param p_leverageDataCenter
     *            LeverageDataCenter that contains segments that are leveraged.
     * @return LeverageMatchResults that contains leverage results
     */
    public LeverageMatchResults leverage(List<Tm> p_tms,
            LeverageDataCenter p_leverageDataCenter, long p_jobId)
            throws Exception;

    /**
     * Leverage a segment
     * 
     * @param p_sourceTuv
     *            source segment
     * @param p_leverageOptions
     *            leverage options
     * @param p_tms
     *            list of tms to leverage
     * @return LeverageMatches object
     */
    public LeverageMatches leverageSegment(BaseTmTuv p_sourceTuv,
            LeverageOptions p_leverageOptions, List<Tm> p_tms) throws Exception;

    // These are called for individual segment deletion (TmSegmentDeleteHandler)
    public void deleteSegmentTmTus(Tm p_tm, Collection<SegmentTmTu> p_tus)
            throws Exception;

    public void deleteSegmentTmTuvs(Tm p_tm, Collection<SegmentTmTuv> p_tuvs)
            throws Exception;

    //
    // Refactored stuff
    //

    // TM removal (TmRemover / RemoveTmHandler)
    public boolean removeTmData(Tm pTm, ProgressReporter pReporter,
            InterruptMonitor pMonitor) throws LingManagerException;

    // TM removal (TmRemover / RemoveTmHandler) - if "Remove by locale" is being
    // used
    public boolean removeTmData(Tm pTm, GlobalSightLocale pLocale,
            ProgressReporter pReporter, InterruptMonitor pMonitor)
            throws LingManagerException;

    public StatisticsInfo getStatistics(Tm pTm, Locale pUILocale,
            boolean p_includeProjects) throws LingManagerException;
    
	public StatisticsInfo getTmExportInformation(Tm pTm, Locale pUILocale)
			throws LingManagerException;

    public List<SegmentTmTu> getSegmentsById(Tm tm, List<Long> tuIds)
            throws LingManagerException;

    public SegmentResultSet getAllSegments(Tm tm, String createdBefore,
            String createdAfter, Connection conn) throws LingManagerException;

	public SegmentResultSet getAllSegmentsByParamMap(Tm tm,
			Map<String, Object> paramMap, Connection conn)
			throws LingManagerException;

    public SegmentResultSet getAllSegments(Tm tm, long startTUId,
            Connection conn) throws LingManagerException;

    public int getAllSegmentsCount(Tm tm, String createdBefore,
            String createdAfter) throws LingManagerException;

	public int getAllSegmentsCountByParamMap(Tm tm, Map<String, Object> paramMap)
			throws LingManagerException;

    public int getAllSegmentsCount(Tm tm, long startTUId)
            throws LingManagerException;

    public List<TMidTUid> tmConcordanceQuery(List<Tm> tms, String query,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale,
            Connection connection) throws LingManagerException;

    public Set<GlobalSightLocale> getLocalesForTm(Tm tm)
            throws LingManagerException;

    /**
     * Get the number of segments in this TM that need to be reindexed. In many
     * cases, this will be the same as the total number of segments.
     */
    public int getSegmentCountForReindex(Tm tm);

    /**
     * Reindex a single TM. Implementations should call
     * reindexer.incrementCounter() with updates to their progress, and
     * periodically check reindexer.getInterrupted() to see if the user has
     * cancelled the operation.
     * 
     * @param tm
     *            TM to reindex
     * @param reindexer
     *            Reindexer object for updating reindex count using calls to
     *            incrementCounter()
     * @param indexTarget
     *            whether create lucene index for target.
     * @return true if reindexing was successful
     */
    public boolean reindexTm(Tm tm, Reindexer reindexer, boolean indexTarget);

    /**
     * Gross hacks in support of prior legacy hacks. See
     * TmCoreManager.getSidByTuvId() et al
     */
    public String getSidByTuvId(Tm tm, long tuvId);

    public String getCreatingUserByTuvId(Tm tm, long tuvId);

    public Date getModifyDateByTuvId(Tm tm, long tuvId);

    /**
     * This one is particularly egregious. It's not clear to me if srcLocaleId
     * is ever the locale *other* than the one attached to the specified tuv. If
     * not, then we can do away with the parameter altogether.
     */
    public String getSourceTextByTuvId(Tm tm, long tuvId, long srcLocaleId);

    /**
     * This method is used for get TUV basic information
     * 
     * @param tmId
     * @param tuvId
     * @param srcLocaleId
     * @return
     * @throws RemoteException
     * @throws LingManagerException
     */
    public TuvBasicInfo getTuvBasicInfoByTuvId(Tm tm, long tuvId);

    public void setJob(Job job);

    public Job getJob();
    
    public void setLock(boolean lock);

    public boolean getLock();
}
