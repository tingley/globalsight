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
import com.globalsight.everest.util.system.RemoteServer;
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
 * This class represents the remote implementation of TmCoreManager
 */
public class TmCoreManagerWLRMIImpl extends RemoteServer implements
        TmCoreManagerWLRemote
{
    private TmCoreManager m_localInstance = null;

    public TmCoreManagerWLRMIImpl() throws RemoteException,
            LingManagerException
    {
        super(SERVICE_NAME);
        m_localInstance = new TmCoreManagerLocal();
    }

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
    public TuvMappingHolder populatePageForAllLocales(SourcePage p_page,
            LeverageOptions p_options, long p_jobId) throws RemoteException,
            LingManagerException
    {
        return m_localInstance.populatePageForAllLocales(p_page, p_options,
                p_jobId);
    }

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
    public TuvMappingHolder populatePageByLocale(SourcePage p_page,
            LeverageOptions p_options, GlobalSightLocale p_locale, long p_jobId)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.populatePageByLocale(p_page, p_options,
                p_locale, p_jobId);
    }

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
    public LeverageDataCenter createLeverageDataCenterForPage(
            SourcePage p_sourcePage, LeverageOptions p_leverageOptions,
            long p_jobId) throws RemoteException, LingManagerException
    {
        return m_localInstance.createLeverageDataCenterForPage(p_sourcePage,
                p_leverageOptions, p_jobId);
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
            LeverageDataCenter p_leverageDataCenter) throws RemoteException,
            LingManagerException
    {
        m_localInstance.leveragePage(p_sourcePage, p_leverageDataCenter);
    }

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
    public void leveragePage(SourcePage p_sourcePage,
            LeverageDataCenter p_leverageDataCenter, boolean p_leverageRemoteTm)
            throws RemoteException, LingManagerException
    {
        m_localInstance.leveragePage(p_sourcePage, p_leverageDataCenter,
                p_leverageRemoteTm);
    }

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
            int p_mode) throws RemoteException, LingManagerException
    {
        return m_localInstance.saveToSegmentTm(p_tm, p_segments, p_mode);
    }

    public TuvMappingHolder saveToSegmentTm(Tm p_tm, Collection p_segments,
            int p_mode, String p_sourceTmName) throws RemoteException,
            LingManagerException, BatchException
    {
        return m_localInstance.saveToSegmentTm(p_tm, p_segments, p_mode,
                p_sourceTmName);
    }

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
            throws RemoteException, LingManagerException
    {
        m_localInstance.updateSegmentTmTuvs(p_tm, p_tuvs);
    }

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
            throws RemoteException, LingManagerException
    {
        m_localInstance.updateSegmentTmTuv(p_tm, p_tuv);
    }

    /**
     * Deletes Tuvs in the Gold Tm.
     * 
     * @param p_tuvs
     *            Tuvs (SegmentTmTuv) to be deleted.
     */
    public void deleteSegmentTmTuvs(Tm p_tm, Collection<SegmentTmTuv> p_tuvs)
            throws RemoteException, LingManagerException
    {
        m_localInstance.deleteSegmentTmTuvs(p_tm, p_tuvs);
    }

    /**
     * Deletes Tus and their Tuvs in the Gold Tm.
     * 
     * @param p_tus
     *            Tus (SegmentTmTu) to be deleted.
     */
    public void deleteSegmentTmTus(Tm p_tm, Collection<SegmentTmTu> p_tus)
            throws RemoteException, LingManagerException
    {
        m_localInstance.deleteSegmentTmTus(p_tm, p_tus);
    }
    
    public void deleteSegmentTmTus(Tm p_tm, Collection<SegmentTmTu> p_tus, boolean lock)
            throws RemoteException, LingManagerException
    {
        m_localInstance.deleteSegmentTmTus(p_tm, p_tus, lock);
    }

    @Override
    public boolean removeTmData(Tm pTm, ProgressReporter pReporter,
            InterruptMonitor pMonitor) throws RemoteException,
            LingManagerException
    {
        return m_localInstance.removeTmData(pTm, pReporter, pMonitor);
    }

    @Override
    public boolean removeTmData(Tm pTm, GlobalSightLocale pLocale,
            ProgressReporter pReporter, InterruptMonitor pMonitor)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.removeTmData(pTm, pLocale, pReporter, pMonitor);
    }

    @Override
    public StatisticsInfo getTmStatistics(Tm pTm, Locale pUiLocale,
            boolean p_includeProjects) throws LingManagerException,
            RemoteException
    {
        return m_localInstance.getTmStatistics(pTm, pUiLocale,
                p_includeProjects);
    }
    
	@Override
	public StatisticsInfo getTmExportInformation(Tm p_tm, Locale p_uiLocale)
			throws RemoteException, LingManagerException
	{
		return m_localInstance.getTmExportInformation(p_tm, p_uiLocale);
	}

    @Override
    public DynamicLeverageResults leverageSegment(BaseTmTuv p_tuv,
            LeverageOptions p_options) throws LingManagerException,
            RemoteException
    {
        m_localInstance.setJobId(getJobId());
        return m_localInstance.leverageSegment(p_tuv, p_options);
    }

    @Override
    public LeverageDataCenter leverageSegments(
            List<? extends BaseTmTuv> p_tuvs, GlobalSightLocale p_srcLocale,
            List<GlobalSightLocale> p_tgtLocales, LeverageOptions p_options)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.leverageSegments(p_tuvs, p_srcLocale,
                p_tgtLocales, p_options);
    }
    
    @Override
    public LeverageDataCenter leverageSegments(
            List<? extends BaseTmTuv> p_tuvs, GlobalSightLocale p_srcLocale,
            List<GlobalSightLocale> p_tgtLocales, LeverageOptions p_options,
            Job job) throws RemoteException, LingManagerException
    {
        return m_localInstance.leverageSegments(p_tuvs, p_srcLocale,
                p_tgtLocales, p_options, job);
    }

    @Override
    public List<SegmentTmTu> getSegmentsById(List<TMidTUid> tuIds)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.getSegmentsById(tuIds);
    }

    @Override
    public SegmentResultSet getAllSegments(Tm tm, String createdBefore,
            String createdAfter, Connection conn) throws RemoteException,
            LingManagerException
    {
        return m_localInstance.getAllSegments(tm, createdBefore, createdAfter,
                conn);
    }

    public SegmentResultSet getAllSegmentsByParamMap(Tm tm,
			Map<String, Object> paramMap, Connection conn)
			throws RemoteException, LingManagerException
	{
		return m_localInstance.getAllSegmentsByParamMap(tm, paramMap, conn);
	}

    @Override
    public int getAllSegmentsCount(Tm tm, String createdBefore,
            String createdAfter) throws RemoteException, LingManagerException
    {
        return m_localInstance.getAllSegmentsCount(tm, createdBefore,
                createdAfter);
    }

	public int getAllSegmentsCountByParamMap(Tm tm, Map<String, Object> paramMap)
			throws RemoteException, LingManagerException
	{
		return m_localInstance.getAllSegmentsCountByParamMap(tm, paramMap);
	}

    @Override
    public List<TMidTUid> tmConcordanceQuery(List<Tm> tms, String query,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale,
            Map<Tm, Integer> tmPriority) throws RemoteException,
            LingManagerException
    {
        return m_localInstance.tmConcordanceQuery(tms, query, sourceLocale,
                targetLocale, tmPriority);
    }

    @Override
    public Set<GlobalSightLocale> getTmLocales(Tm tm) throws RemoteException,
            LingManagerException
    {
        return m_localInstance.getTmLocales(tm);
    }

    @Override
    public Reindexer getReindexer(Collection<ProjectTM> tms)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.getReindexer(tms);
    }

    @Override
    public String getCreatingUserByTuvId(long tmId, long tuvId)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.getCreatingUserByTuvId(tmId, tuvId);
    }

    @Override
    public Date getModifyDateByTuvId(long tmId, long tuvId)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.getModifyDateByTuvId(tmId, tuvId);
    }

    @Override
    public String getSidByTuvId(long tmId, long tuvId) throws RemoteException,
            LingManagerException
    {
        return m_localInstance.getSidByTuvId(tmId, tuvId);
    }

    @Override
    public String getSourceTextByTuvId(long tmId, long tuvId, long srcLocaleId)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.getSourceTextByTuvId(tmId, tuvId, srcLocaleId);
    }

    @Override
    public TuvBasicInfo getTuvBasicInfoByTuvId(long tmId, long tuvId,
            long srcLocaleId) throws RemoteException, LingManagerException
    {
        // TODO Auto-generated method stub
        return m_localInstance.getTuvBasicInfoByTuvId(tmId, tuvId, srcLocaleId);
    }

    long jobid = -1;

    @Override
    public void setJobId(long jobId)
    {
        jobid = jobId;
    }

    @Override
    public long getJobId()
    {
        return jobid;
    }
}
