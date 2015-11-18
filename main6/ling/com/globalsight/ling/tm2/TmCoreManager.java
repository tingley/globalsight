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
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.tm.StatisticsInfo;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.inprogresstm.DynamicLeverageResults;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.indexer.Reindexer;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.persistence.error.BatchException;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProgressReporter;

/**
 * TmCoreManager is responsible for communicating with the other part of the
 * system. It does leverage, TM population, etc.
 */
public interface TmCoreManager
{
    /** When adding a TU to the TM, overwrite the existing TU. */
    public static final int SYNC_OVERWRITE = 1;
    /** When adding a TU to the TM, merge with the existing TU. */
    public static final int SYNC_MERGE = 2;
    /** When adding a TU to the TM, discard the new TU if a TU exists. */
    public static final int SYNC_DISCARD = 3;

    /** The "ORDER_NUM" for local TM matches starts from 1. */
    public static final int LM_ORDER_NUM_START_LOCAL_TM = 1;
    /** The "ORDER_NUM" for remote TM matches starts from 101. */
    public static final int LM_ORDER_NUM_START_REMOTE_TM = 101;
    /** The "ORDER_NUM" for TDA matches starts from 201. */
    public static final int LM_ORDER_NUM_START_TDA = 201;
    /**
     * The "ORDER_NUM" for MT matches starts from 301. Commonly they are 301 and
     * 302 as MT returns at most 2 matches for one segment.
     */
    public static final int LM_ORDER_NUM_START_MT = 301;
    /** The "ORDER_NUM" for XLF/PO matches is -1. */
    public static final int LM_ORDER_NUM_START_XLF_PO_TARGET = -1;

    /**
     * Save source and target segments that belong to the page. Segments are
     * saved in Page Tm and Segment Tm.
     * 
     * @param p_page
     *            source page that has been exported.
     * @param p_options
     *            Tm options. It has information which Project TM segments
     *            should be saved and etc.
     * @return mappings of translation_unit_variant id and project_tm_tuv_t id
     *         of this page
     */
    TuvMappingHolder populatePageForAllLocales(SourcePage p_page,
            LeverageOptions p_options, long p_jobId) throws RemoteException,
            LingManagerException;

    /**
     * Save source and a specified target segments that belong to the page.
     * Segments are saved in Page Tm and Segment Tm.
     * 
     * @param p_page
     *            source page that has been exported.
     * @param p_options
     *            Tm options. It has information which Project TM segments
     *            should be saved and etc.
     * @param p_locale
     *            target locale
     * @return mappings of translation_unit_variant id and project_tm_tuv_t id
     *         of this page
     */
    TuvMappingHolder populatePageByLocale(SourcePage p_page,
            LeverageOptions p_options, GlobalSightLocale p_locale, long p_jobId)
            throws RemoteException, LingManagerException;

    /**
     * create LeverageDataCenter with original source segments from a source
     * page
     * 
     * @param p_sourcePage
     *            SourcePage
     * @param p_leverageOptions
     *            LeverageOptions
     * @return LeverageDataCenter object
     */
    LeverageDataCenter createLeverageDataCenterForPage(SourcePage p_sourcePage,
            LeverageOptions p_leverageOptions, long p_jobId)
            throws RemoteException, LingManagerException;

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
    void leveragePage(SourcePage p_sourcePage,
            LeverageDataCenter p_leverageDataCenter) throws RemoteException,
            LingManagerException;

    /**
     * Leverage a given page. It does: - leverage Page Tm - leverage Segment Tm
     * - apply leverage options for both leverage matches - save matches to the
     * database - returns a list of exact matched segments
     * 
     * @param p_sourcePage
     * @param p_leverageDataCenter
     * @param p_leverageRemoteTm
     *            True:default If there is remote tm in tm profile,will leverage
     *            from it. False: Will not leverage from remote tm however there
     *            is remote tm in tm profile.
     * 
     * @throws RemoteException
     * @throws LingManagerException
     */
    void leveragePage(SourcePage p_sourcePage,
            LeverageDataCenter p_leverageDataCenter, boolean p_leverageRemoteTm)
            throws RemoteException, LingManagerException;

    /**
     * Saves segments to a Segment Tm. Use <code>p_mode</code> to overwrite
     * existing TUs, merge existing TUs with new TUs, or to discard new TUs if
     * they already exist in the TM.
     * 
     * @param p_tmId
     *            Tm id in which segments are saved
     * @param p_segments
     *            Collection of SegmentTmTu objects.
     * @param p_mode
     *            one of the "SYNC" constants, to overwrite existing TUs, merge
     *            existing TUs with new TUs, or to discard new TUs if they
     *            already exist in the TM.
     * @return TuvMappingHolder. m_tuvId and m_tuId values are arbitrary.
     * 
     * @throws LingManagerException
     */
    public TuvMappingHolder saveToSegmentTm(Tm p_tm, Collection p_segments,
            int p_mode) throws RemoteException, LingManagerException;

    public TuvMappingHolder saveToSegmentTm(Tm p_tm, Collection p_segments,
            int p_mode, String p_sourceTmName) throws RemoteException,
            LingManagerException, BatchException;

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
    public void updateSegmentTmTuvs(Tm p_tm, Collection p_tuvs)
            throws RemoteException, LingManagerException;

    /**
     * Updates a single existing TUV in a Segment Tm.
     * 
     * @param p_tmId
     *            Tm id in which segments are updated
     * @param p_tuv
     *            the SegmentTmTuv object to update.
     * 
     * @throws LingManagerException
     */
    public void updateSegmentTmTuv(Tm p_tm, BaseTmTuv p_tuv)
            throws RemoteException, LingManagerException;

    /**
     * Deletes Tuvs in the Gold Tm.
     * 
     * @param p_tuvs
     *            Tuvs (SegmentTmTuv) to be deleted.
     */
    public void deleteSegmentTmTuvs(Tm p_tm, Collection<SegmentTmTuv> p_tuvs)
            throws RemoteException, LingManagerException;

    /**
     * Deletes Tus and their Tuvs in the Gold Tm.
     * 
     * @param p_tus
     *            Tus (SegmentTmTu) to be deleted.
     */
    public void deleteSegmentTmTus(Tm p_tm, Collection<SegmentTmTu> p_tus)
            throws RemoteException, LingManagerException;
    
    public void deleteSegmentTmTus(Tm p_tm, Collection<SegmentTmTu> p_tus, boolean lock)
            throws RemoteException, LingManagerException;

    /**
     * Remove all data for the specified TM, including corpus data and the TM
     * itself.
     * 
     * Interface extracted from com.globalsight.everest.tm.TmRemover. This
     * operation should be completed in the current thread. The InterruptMonitor
     * may be used to tell if the job should be cancelled.
     * 
     * The implementation should update the provided ProgressReporter with
     * appropriate status as the operation is performed.
     * 
     * @param p_tm
     *            TM to remove data from
     * @param p_reporter
     *            process monitor
     * @param p_monitor
     *            monitor to check for interrupts
     * 
     * @return true if all the data was removed, false if there was an interrupt
     */
    public boolean removeTmData(Tm p_tm, ProgressReporter p_reporter,
            InterruptMonitor p_monitor) throws RemoteException,
            LingManagerException;

    /**
     * Remove all data in a given locale from the specified TM, including corpus
     * data. Interface extracted from com.globalsight.everest.tm.TmRemover. This
     * operation should be completed in the current thread. The InterruptMonitor
     * may be used to tell if the job should be cancelled.
     * 
     * @param p_tm
     *            TM to remove data from
     * @param p_locale
     *            locale to remove data for
     * @param p_interruptMonitor
     *            monitor to check for interrupts
     * 
     * @return true if all the data was removed, false if there was an interrupt
     */
    public boolean removeTmData(Tm p_tm, GlobalSightLocale p_locale,
            ProgressReporter p_reporter, InterruptMonitor p_monitor)
            throws RemoteException, LingManagerException;

    /**
     * Fetch statistics about a given TM. This is moved from
     * com.globalsight.everest.tm.TmManager.
     * 
     * @param p_tm
     *            TM to get statistics for
     * @param p_uiLocale
     *            Locale of the UI. Used when rendering language names.
     * @param p_includeProjects
     *            true if project data should be included
     * @todo It would be nice to move the Locale stuff out of here
     */
    public StatisticsInfo getTmStatistics(Tm p_tm, Locale p_uiLocale,
            boolean p_includeProjects) throws RemoteException,
            LingManagerException;
    
	public StatisticsInfo getTmExportInformation(Tm p_tm, Locale p_uiLocale)
			throws RemoteException, LingManagerException;

    /**
     * Single-segment leveraging. This is extracted from
     * com.global.everest.tm.inprogresstm.InProgressTmManagerLocal It is
     * designed to present 'fallback' leveraging against a gold tm for the
     * in-progress TM.
     * 
     * @param p_tuv
     *            tuv to leverage
     * @param p_options
     *            leverage options
     * @return leverage results
     */
    public DynamicLeverageResults leverageSegment(BaseTmTuv p_tuv,
            LeverageOptions p_options) throws RemoteException,
            LingManagerException;

    /**
     * Simplified signature to leverage one or more segments against a set of
     * TMs.
     * 
     * @param p_tuvs
     *            list of TUVs to leverage
     * @param p_srcLocale
     * @param p_tgtLocales
     * @param p_options
     * @return LeverageDataCenter containing leverage results
     * @throws RemoteException
     * @throws LingManagerException
     */
    public LeverageDataCenter leverageSegments(
            List<? extends BaseTmTuv> p_tuvs, GlobalSightLocale p_srcLocale,
            List<GlobalSightLocale> p_tgtLocales, LeverageOptions p_options)
            throws RemoteException, LingManagerException;
    
    public LeverageDataCenter leverageSegments(
            List<? extends BaseTmTuv> p_tuvs, GlobalSightLocale p_srcLocale,
            List<GlobalSightLocale> p_tgtLocales, LeverageOptions p_options,
            Job job) throws RemoteException, LingManagerException;

    /**
     * Return the given segments.
     */
    public List<SegmentTmTu> getSegmentsById(List<TMidTUid> tuIds)
            throws RemoteException, LingManagerException;

    /**
     * Return an Iterator that will return all segments in a TM, constrained by
     * date ranges.
     * 
     * NOTE: The date parameters are passed as Strings in DD/MM/YYYY form. This
     * is due to legacy code in com.globalsight.tm.export.ExportOptions It would
     * be nice to fix it.
     * 
     * WARNING: The caller MUST clean up the SegmentResultSet object by calling
     * its finish() method to avoid leaking a Hibernate session (and its
     * underlying DB connection).
	 *
     * @param tm
     *            TM to export from
     * @param createdBefore
     *            date lower bound (inclusive), in DD/MM/YYYY form
     * @param createdAfter
     *            date upper bounad (inclusive), in DD/MM/YYYY form
     * @return Iterator of segments
     */
	public SegmentResultSet getAllSegments(Tm tm, String createdBefore,
			String createdAfter, Connection conn) throws RemoteException,
			LingManagerException;

	public SegmentResultSet getAllSegmentsByParamMap(Tm tm,
			Map<String, Object> paramMap, Connection conn)
			throws RemoteException, LingManagerException;

    /**
     * Return the count of the number of segments that would be returned to a
     * corresponding call to getAllSegments.
     * 
     * NOTE: The date parameters are passed as Strings in DD/MM/YYYY form. This
     * is due to legacy code in com.globalsight.tm.export.ExportOptions It would
     * be nice to fix it.
     * 
     * @param tm
     *            TM to export from
     * @param createdBefore
     *            date lower bound (inclusive), in DD/MM/YYYY form
     * @param createdAfter
     *            date upper bounad (inclusive), in DD/MM/YYYY form
     * @return count of segments
     */
    public int getAllSegmentsCount(Tm tm, String createdBefore,
            String createdAfter) throws RemoteException, LingManagerException;

	public int getAllSegmentsCountByParamMap(Tm tm, Map<String, Object> paramMap)
			throws RemoteException, LingManagerException;

    /**
     * Perform a concordance query for the given string across the specified
     * TMs.
     * <p>
     * 
     * @param tms
     * @param query
     * @param sourceLocale
     * @param targetLocale
     * @param tmPriority
     *            Order of TMs for purposes of sorting results
     * @return List<TMidTUid>
     * @throws RemoteException
     * @throws LingManagerException
     */
    public List<TMidTUid> tmConcordanceQuery(List<Tm> tms, String query,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale,
            Map<Tm, Integer> tmPriority) throws RemoteException,
            LingManagerException;

    /**
     * Get a list of all GlobalSightLocales for which there are TUVs in this TM.
     * 
     * @param tm
     *            tm
     * @return set of locales
     */
    public Set<GlobalSightLocale> getTmLocales(Tm tm) throws RemoteException,
            LingManagerException;

    /**
     * Get a reindex for a set of TMs.
     * 
     * @param tms
     * @return
     * @throws RemoteException
     * @throws LingManagerException
     */
    public Reindexer getReindexer(Collection<ProjectTM> tms)
            throws RemoteException, LingManagerException;

    /**
     * Now for the biggest hack of them all: fetch a Segment TU by ID from a
     * specified TM (also by ID). This code exists primarily for SID support,
     * but the SID implementation is a mess. Rather than supporting SIDs all the
     * way through the app, the original author had various high level pieces of
     * code query the database directly to fetch SIDs at the last minute. This
     * worked "ok" until TM3 came along, at which point it was a problem. The
     * code that previously used those hacks now uses -this- hack instead, which
     * will return the TUV regardless of whether TM2 or TM3 is in use. Note that
     * the OfflinePageData uses similar hacks to load other pieces of non-SID
     * data such as creating user.
     * 
     * @deprecated Hopefully we will be able to clean this up someday.
     */
    public String getSidByTuvId(long tmId, long tuvId) throws RemoteException,
            LingManagerException;

    /**
     * @deprecated
     */
    public String getCreatingUserByTuvId(long tmId, long tuvId)
            throws RemoteException, LingManagerException;

    /**
     * @deprecated
     */
    public Date getModifyDateByTuvId(long tmId, long tuvId)
            throws RemoteException, LingManagerException;

    /**
     * @deprecated
     */
    public String getSourceTextByTuvId(long tmId, long tuvId, long srcLocaleId)
            throws RemoteException, LingManagerException;

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
    public TuvBasicInfo getTuvBasicInfoByTuvId(long tmId, long tuvId,
            long srcLocaleId) throws RemoteException, LingManagerException;

    public void setJobId(long jobId);

    public long getJobId();
}
