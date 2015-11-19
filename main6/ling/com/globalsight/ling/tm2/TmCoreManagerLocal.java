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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvAttributeUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.StatisticsInfo;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tuv.TuTuvAttributeImpl;
import com.globalsight.everest.util.comparator.TMidTUidComparator;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.SegmentTmExactMatchFormatHandler;
import com.globalsight.ling.inprogresstm.DynamicLeverageResults;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.indexer.Reindexer;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatchResults;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tm2.leverage.RemoteLeverager;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.persistence.PageJobDataRetriever;
import com.globalsight.ling.tm2.persistence.SegmentQueryResult;
import com.globalsight.ling.tm2.persistence.error.BatchException;
import com.globalsight.ling.tm2.population.TmPopulator;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.ling.tm2.segmenttm.Tm2SegmentTmInfo;
import com.globalsight.ling.tm2.segmenttm.TmRemoveHelper;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProgressReporter;

/**
 * Implementation of TmCoreManager
 */

public class TmCoreManagerLocal implements TmCoreManager
{
    // object to lock for synchronizing tm population process (due to deadlocks)
    private Boolean m_tmPopulationLock = new Boolean(true);

    private static final Logger LOGGER = Logger
            .getLogger(TmCoreManagerLocal.class);

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
            LeverageOptions p_options, long p_jobId) throws LingManagerException
    {
        TuvMappingHolder mappingHolder = null;
        try
        {
            TmPopulator tmPopulator = new TmPopulator();
            mappingHolder = tmPopulator.populatePageForAllLocales(p_page,
                    p_options, p_jobId);
        }
        catch (LingManagerException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
        return mappingHolder;
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
            throws LingManagerException
    {
        TuvMappingHolder mappingHolder = null;

        try
        {
            synchronized (m_tmPopulationLock)
            {
                TmPopulator tmPopulator = new TmPopulator();
                mappingHolder = tmPopulator.populatePageByLocale(p_page,
                        p_options, p_locale, p_jobId);
            }
        }
        catch (LingManagerException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }

        return mappingHolder;
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
            long p_jobId) throws LingManagerException
    {
        Connection conn = null;
        LeverageDataCenter leverageDataCenter = null;
        PageJobDataRetriever pageJobDataRetriever = null;

        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            GlobalSightLocale sourceLocale = p_sourcePage
                    .getGlobalSightLocale();

            // prepare a repository of original segments (source
            // segments in translation_unit_variant)
            leverageDataCenter = new LeverageDataCenter(sourceLocale,
                    p_leverageOptions.getLeveragingLocales()
                            .getAllTargetLocales(), p_leverageOptions);

            // Get page data from translation_unit_variant and
            // translation_unit table
            pageJobDataRetriever = new PageJobDataRetriever(conn,
                    p_sourcePage.getId(), sourceLocale, p_jobId);
            SegmentQueryResult result = pageJobDataRetriever.queryForLeverage();
            List<BaseTmTu> tus = new ArrayList<BaseTmTu>();
            BaseTmTu tu = null;
            while ((tu = result.getNextTu()) != null)
            {
            	tus.add(tu);
            }

            setPreNextHashValues(tus, sourceLocale);

            List<Long> tuvIds = new ArrayList<Long>();
            List<BaseTmTuv> tuvs = new ArrayList<BaseTmTuv>();
            for (BaseTmTu tu2 : tus)
            {
                // Don't add a tuv that has an excluded type
                if (!p_leverageOptions.isExcluded(tu2.getType()))
                {
                    BaseTmTuv tuv = tu2.getFirstTuv(sourceLocale);

                    SegmentTmExactMatchFormatHandler handler = new SegmentTmExactMatchFormatHandler();
                    DiplomatBasicParser diplomatParser = new DiplomatBasicParser(
                            handler);

                    diplomatParser.parse(tuv.getSegment());
                    tuv.setExactMatchKey(GlobalSightCrc.calculate(handler
                            .toString()));
                    tuvIds.add(tuv.getId());
                    tuvs.add(tuv);
                }
            }

            // load SID from "translation_tu_tuv_attr_xx" table
			List<TuTuvAttributeImpl> sidAttrs = SegmentTuTuvAttributeUtil
					.getSidAttributesByTuvIds(tuvIds, p_jobId);
    		HashMap<Long, String> sidAttrMap = new HashMap<Long, String>();
    		for (TuTuvAttributeImpl sidAttr : sidAttrs)
    		{
    			sidAttrMap.put(sidAttr.getObjectId(), sidAttr.getTextValue());
    		}
    		for (BaseTmTuv tuv : tuvs)
    		{
    			if (StringUtil.isNotEmpty(sidAttrMap.get(tuv.getId())))
    			{
    				tuv.setSid(sidAttrMap.get(tuv.getId()));
    			}
    			leverageDataCenter.addOriginalSourceTuv(tuv);
    		}
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.setAutoCommit(true);
                    DbUtil.returnConnection(conn);
                }
                catch (Exception e)
                {
                    throw new LingManagerException(e);
                }
            }

            if (pageJobDataRetriever != null)
            {
                try
                {
                    pageJobDataRetriever.close();
                }
                catch (Exception e)
                {
                    throw new LingManagerException(e);
                }
            }
        }

        return leverageDataCenter;
    }

	private void setPreNextHashValues(List<BaseTmTu> tus,
			GlobalSightLocale sourceLocale) throws Exception
    {
        List<BaseTmTu> m_tentativeTus = new ArrayList<BaseTmTu>();
        BaseTmTu previousTu = null;
        for (BaseTmTu curTu : tus)
        {
            if (m_tentativeTus.size() > 0)
            {
            	previousTu = m_tentativeTus.get(m_tentativeTus.size() - 1);
            }
        	m_tentativeTus.add(curTu);

        	BaseTmTuv curSrcTuv = curTu.getFirstTuv(sourceLocale);
        	for (BaseTmTuv curTuv : curTu.getTuvs())
            {
    			// TUV uses its previous source tuv's hash value as previous
    			// hash, and use next source tuv's hash value as next hash. Never
    			// store target tuv's hash value.
            	// Will this be an issue in the further?
            	curTuv.setPreviousHash(BaseTmTuv.FIRST_HASH);
        		curTuv.setNextHash(BaseTmTuv.LAST_HASH);
            	if (previousTu != null)
            	{
            		BaseTmTuv preSrcTuv = previousTu.getFirstTuv(sourceLocale);
    				BaseTmTuv preTuv = previousTu.getFirstTuv(curTuv.getLocale());
    				preTuv.setNextHash(SegmentTuvUtil.getHashValue(curSrcTuv.getSegment()));
    				curTuv.setPreviousHash(SegmentTuvUtil.getHashValue(preSrcTuv.getSegment()));
            	}
            }
        }
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
            LeverageDataCenter p_leverageDataCenter)
            throws LingManagerException
    {
        // default remote leveraging page from remote tm
        // if it is configured in tm profile
        this.leveragePage(p_sourcePage, p_leverageDataCenter, true);
    }

    /**
     * Leverage all the segments in a source page against a set of TMs
     * identified as part of the LDC/LeverageOptions. Because these TMs may not
     * all have the same storage implementation (legacy tm2 vs tm3), we need to
     * split up the TMs into groups, call each engine separately, then merge the
     * results. Ugly! Luckily, this should only matter in the transient/upgrade
     * case. At some point, one would hope that everyone is on tm3 and we can
     * dump the tm2 code entirely.
     */
    public void leveragePage(SourcePage p_sourcePage,
            LeverageDataCenter p_leverageDataCenter, boolean p_leverageRemoteTm)
            throws LingManagerException
    {
        LeverageOptions leverageOptions = p_leverageDataCenter
                .getLeverageOptions();

        // if number of matches returned option is less than 1, no
        // action will be taken.
        if (leverageOptions.getNumberOfMatchesReturned() < 1)
        {
            return;
        }

        // Split the tms up by implementation
        SortedTms sortedTms = sortTmsByImplementation(leverageOptions
                .getLeverageTms());

        Session session = HibernateUtil.getSession();

        try
        {
            Leverager leverager = new Leverager(session);
            // Leverage the tm2 and tm3 tms
            leverager.leveragePage(p_sourcePage, p_leverageDataCenter,
                    sortedTms.tm2Tms, sortedTms.tm3Tms);

            // Leverage the remote TMs
            if (p_leverageRemoteTm && sortedTms.remoteTms.size() > 0)
            {
                new RemoteLeverager().remoteLeveragePage(p_sourcePage,
                        sortedTms.remoteTms, p_leverageDataCenter);
            }
        }
        catch (LingManagerException le)
        {
            le.printStackTrace();
            throw le;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
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
    @Override
    public TuvMappingHolder saveToSegmentTm(Tm tm, Collection p_segments,
            int p_mode) throws LingManagerException
    {
        if (p_segments.size() == 0)
        {
            return new TuvMappingHolder();
        }

        TuvMappingHolder holder = null;
        try
        {
            TmPopulator tmPopulator = new TmPopulator();
            holder = tmPopulator.saveSegmentToSegmentTm(p_segments, tm, p_mode);
        }
        catch (LingManagerException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }

        return holder;
    }

    /**
     * Saves a collection of segments to a segment TM. A BatchException does not
     * mean that the save failed, only that some segments could not be saved
     * (and the BatchException has the details).
     */
    @Override
    public TuvMappingHolder saveToSegmentTm(Tm tm, Collection p_segments,
            int p_mode, String p_sourceTmName) throws LingManagerException,
            BatchException
    {
        if (p_segments.size() == 0)
        {
            return new TuvMappingHolder();
        }
        Collection<SegmentTmTu> goodSegments = new ArrayList<SegmentTmTu>();
        Collection<BatchException.TuvError> errs = new ArrayList<BatchException.TuvError>();
        for (Object _tu : p_segments)
        {
            SegmentTmTu tu = (SegmentTmTu) _tu;
            boolean badTu = false;
            for (BaseTmTuv _tuv : tu.getTuvs())
            {
                SegmentTmTuv tuv = (SegmentTmTuv) _tuv;
                char[] chars = tuv.getSegment().toCharArray();
                for (int i = 0; i < chars.length; i++)
                {
                    if (Character.isHighSurrogate(chars[i]))
                    {
                        errs.add(new BatchException.TuvError(tuv,
                                "lb_import_tm_tuv_error_high_unicode",
                                "Contains high unicode characters"));
                        badTu = true;
                    }
                }
            }
            if (!badTu)
            {
                goodSegments.add(tu);
            }
        }

        TuvMappingHolder holder = null;

        try
        {
            TmPopulator tmPopulator = new TmPopulator();
            holder = tmPopulator.saveSegmentToSegmentTm(goodSegments, tm,
                    p_mode, p_sourceTmName);
        }
        catch (LingManagerException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }

        if (errs.size() > 0)
        {
            throw new BatchException(errs);
        }
        return holder;
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
    @Override
    public void updateSegmentTmTuvs(Tm tm, Collection p_tuvs)
            throws LingManagerException
    {
        if (p_tuvs.size() == 0)
        {
            return;
        }

        try
        {
            tm.getSegmentTmInfo().updateSegmentTmTuvs(tm, p_tuvs);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
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
    @Override
    public void updateSegmentTmTuv(Tm tm, BaseTmTuv p_tuv)
            throws LingManagerException
    {
    }

    /**
     * Deletes Tuvs in the Gold Tm.
     * 
     * @param p_tuvs
     *            Tuvs (SegmentTmTuv) to be deleted.
     */
    @Override
    public void deleteSegmentTmTuvs(Tm p_tm, Collection<SegmentTmTuv> p_tuvs)
            throws LingManagerException
    {
        if (p_tuvs.size() == 0)
        {
            return;
        }

        try
        {
            p_tm.getSegmentTmInfo().deleteSegmentTmTuvs(p_tm, p_tuvs);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    /**
     * Deletes Tus and their Tuvs in the Gold Tm.
     * 
     * @param p_tus
     *            Tus (SegmentTmTu) to be deleted.
     */
    public void deleteSegmentTmTus(Tm p_tm, Collection<SegmentTmTu> p_tus)
            throws LingManagerException
    {
        if (p_tus.size() == 0)
        {
            return;
        }

        try
        {
            p_tm.getSegmentTmInfo().deleteSegmentTmTus(p_tm, p_tus);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }
    
    public void deleteSegmentTmTus(Tm p_tm, Collection<SegmentTmTu> p_tus, boolean lock)
            throws LingManagerException
    {
        if (p_tus.size() == 0)
        {
            return;
        }

        try
        {
            SegmentTmInfo segTmInfo = p_tm.getSegmentTmInfo();
            segTmInfo
            .deleteSegmentTmTus(p_tm, p_tus);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    //
    // Stubs created during the refactoring process
    //

    /**
     * Remove TM data. This will first perform common corpus cleaning code
     * followed by calling segment tm-specific cleaning code.
     */
    @Override
    public boolean removeTmData(Tm pTm, ProgressReporter pReporter,
            InterruptMonitor pMonitor) throws RemoteException,
            LingManagerException
    {

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            // WARNING: About transactions and this bit.
            // Everything here is kept in a single transaction except for this
            // statement.
            // The reason is that removeCorpus() does two things:
            // 1) Explicit JDBC query on the passed connection to fetch some
            // corpus ids
            // 2) Pass the corpus IDs to the CorpusManagerLocal, which deletes
            // them by
            // OPENING A SEPARATE SESSION AND TRANSACTION FOR EACH.
            // Since the Hibernate session mgmt is global, this would trample
            // our transaction.
            // For now, we just do this stuff in its own (bad) individual
            // transactions.
            TmRemoveHelper.removeCorpus(conn, pTm.getId(), null);

            boolean success = pTm.getSegmentTmInfo().removeTmData(pTm,
                    pReporter, pMonitor);
            if (success)
            {
                TmRemoveHelper.removeTm(pTm);
            }
            return success;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * Remove TM data. This will first perform common corpus cleaning code
     * followed by calling segment tm-specific cleaning code.
     */
    @Override
    public boolean removeTmData(Tm pTm, GlobalSightLocale pLocale,
            ProgressReporter pReporter, InterruptMonitor pMonitor)
            throws RemoteException, LingManagerException
    {

        // pReporter.setMessageKey("lb_tm_remove_removing_corpus_tm",
        // "Removing Corpus Tm...");
        // pReporter.setPercentage(10);

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            // NOTE: this statement is outside the transaction. See the comment
            // above.
            TmRemoveHelper.removeCorpus(conn, pTm.getId(), pLocale.getId());
            boolean b = pTm.getSegmentTmInfo().removeTmData(pTm, pLocale,
                    pReporter, pMonitor);
            return b;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    public StatisticsInfo getTmStatistics(Tm pTm, Locale pUiLocale,
            boolean p_includeProjects)
    {
        return pTm.getSegmentTmInfo().getStatistics(pTm, pUiLocale,
                p_includeProjects);
    }

	@Override
	public StatisticsInfo getTmExportInformation(Tm pTm, Locale pUiLocale)
	{
		return pTm.getSegmentTmInfo().getTmExportInformation(pTm, pUiLocale);
	}
	
    @Override
    public DynamicLeverageResults leverageSegment(BaseTmTuv pTuv,
            LeverageOptions pOptions) throws RemoteException,
            LingManagerException
    {
        SortedTms sortedTms = sortTmsByImplementation(pOptions.getLeverageTms());
        Session session = HibernateUtil.getSession();
        try
        {
            Leverager lv = new Leverager(session);
            lv.setJobId(getJobId());

            DynamicLeverageResults results = lv.leverageSegment(pTuv, pOptions,
                    sortedTms.tm2Tms, sortedTms.tm3Tms);
            return results;
        }
        catch (Exception e)
        {
            e.printStackTrace();

            throw new LingManagerException(e);
        }
    }

    /**
     * Code refactored from BrowseCorpusMainHandler and Ambassador webservice
     * implementation, then split up to handle mixed tm2/tm3 TMs.
     */
    @Override
    public LeverageDataCenter leverageSegments(
            List<? extends BaseTmTuv> p_tuvs, GlobalSightLocale p_srcLocale,
            List<GlobalSightLocale> p_tgtLocales, LeverageOptions p_options)
            throws RemoteException, LingManagerException
    {
        return leverageSegments(p_tuvs, p_srcLocale, p_tgtLocales, p_options, null);
    }
    
    public LeverageDataCenter leverageSegments(
            List<? extends BaseTmTuv> p_tuvs, GlobalSightLocale p_srcLocale,
            List<GlobalSightLocale> p_tgtLocales, LeverageOptions p_options,
            Job p_job) throws RemoteException,
            LingManagerException
    {
        LeverageDataCenter leverageDataCenter = new LeverageDataCenter(
                p_srcLocale, p_tgtLocales, p_options);
        for (BaseTmTuv tuv : p_tuvs)
        {
            leverageDataCenter.addOriginalSourceTuv(tuv);
        }

        // Split the tms up by implementation
        SortedTms sortedTms = sortTmsByImplementation(p_options
                .getLeverageTms());

        try
        {
            if (!p_options.dynamicLeveragesStopSearch())
            {
                long jobId = p_job == null ? -1 : p_job.getId();// -1 is fine here.
                LeverageMatchResults levMatchResult = new LeverageMatchResults();
                if (sortedTms.tm2Tms.size() > 0)
                {
                    levMatchResult = new Tm2SegmentTmInfo().leverage(
                            sortedTms.tm2Tms, leverageDataCenter, jobId);
                }
                if (sortedTms.tm3Tms.size() > 0)
                {
                    levMatchResult.merge(new Tm3SegmentTmInfo().leverage(
                            sortedTms.tm3Tms, leverageDataCenter, jobId));
                }
                leverageDataCenter
                        .addLeverageResultsOfSegmentTmMatching(levMatchResult);
                leverageDataCenter.applySegmentTmOptions();
            }
            else
            {
                Session session = HibernateUtil.getSession();
                Leverager lv = new Leverager(session);
                lv.setJobId(getJobId());

                Job job = p_job == null ? null : p_job;
                if (job == null)
                {
                    try
                    {
                        job = ServerProxy.getJobHandler()
                                .getJobById(getJobId());
                    }
                    catch (Exception e)
                    {
                        // ignore
                    }
                }

                lv.leverageDataCenterWithStopSearch(job, leverageDataCenter,
                        sortedTms.tm2Tms, sortedTms.tm3Tms);
            }
        }
        catch (LingManagerException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        return leverageDataCenter;
    }

    @Override
    public List<SegmentTmTu> getSegmentsById(List<TMidTUid> tuIds)
            throws LingManagerException
    {
        Session session = HibernateUtil.getSession();
        try
        {
            Map<Long, List<Long>> tuIdsByTmId = new HashMap<Long, List<Long>>();

            for (TMidTUid id : tuIds)
            {
                if (!tuIdsByTmId.containsKey(id.getTmId()))
                {
                    tuIdsByTmId.put(id.getTmId(), new ArrayList<Long>());
                }
                tuIdsByTmId.get(id.getTmId()).add(id.getTuId());
            }

            ProjectHandler ph = ServerProxy.getProjectHandler();
            Map<TMidTUid, SegmentTmTu> result = new HashMap<TMidTUid, SegmentTmTu>();
            for (Map.Entry<Long, List<Long>> e : tuIdsByTmId.entrySet())
            {
                Tm tm = ph.getProjectTMById(e.getKey(), false);
                List<SegmentTmTu> tus = tm.getSegmentTmInfo().getSegmentsById(
                        tm, e.getValue());
                for (SegmentTmTu tu : tus)
                {
                    result.put(new TMidTUid(tm.getId(), tu.getId()), tu);
                }
            }

            ArrayList<SegmentTmTu> orderedResult = new ArrayList<SegmentTmTu>(
                    tuIds.size());
            for (TMidTUid tuId : tuIds)
            {
                orderedResult.add(result.get(tuId));
            }
            return orderedResult;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    /**
     * WARNING: This routine will leak a session object to the SegmentResultSet
     * that it creates. The caller must call SegmentResultSet.finish() in order
     * to clean it up.
     */
    @Override
    public SegmentResultSet getAllSegments(Tm tm, String createdBefore,
            String createdAfter, Connection conn) throws RemoteException,
            LingManagerException
    {
        return getInfo(tm)
                .getAllSegments(tm, createdBefore, createdAfter, conn);
    }

	public SegmentResultSet getAllSegmentsByParamMap(Tm tm,
			Map<String, Object> paramMap, Connection conn)
			throws RemoteException
	{
		return getInfo(tm).getAllSegmentsByParamMap(tm, paramMap, conn);
	}

    @Override
    public int getAllSegmentsCount(Tm tm, String createdBefore,
            String createdAfter) throws RemoteException, LingManagerException
    {
        return getInfo(tm).getAllSegmentsCount(tm, createdBefore, createdAfter);
    }

	public int getAllSegmentsCountByParamMap(Tm tm, Map<String, Object> paramMap)
			throws RemoteException, LingManagerException
	{
		return getInfo(tm).getAllSegmentsCountByParamMap(tm, paramMap);
	}

    // TODO: this needs to have session handling code added
    // tmPriority param is stupid--just pass the tms in order!
    @Override
    public List<TMidTUid> tmConcordanceQuery(List<Tm> tms, String query,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale,
            final Map<Tm, Integer> tmPriority) throws RemoteException,
            LingManagerException
    {
        List<Tm> sortedTms = new ArrayList<Tm>(tms);
        SortUtil.sort(sortedTms, new Comparator<Tm>()
        {
            private int getPriority(Tm tm)
            {
                if (tmPriority == null)
                {
                    return 0;
                }
                Integer priority = tmPriority.get(tm);
                return priority == null ? 0 : priority;
            }

            public int compare(Tm tm1, Tm tm2)
            {
                return getPriority(tm1) - getPriority(tm2);
            }

        });

        // Could batch up by TM implementation, but it's more complex and
        // probably no faster currently
        List<TMidTUid> result = new ArrayList<TMidTUid>();
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            for (Tm tm : sortedTms)
            {
                result.addAll(tm.getSegmentTmInfo().tmConcordanceQuery(
                        Collections.singletonList(tm), query, sourceLocale,
                        targetLocale, conn));
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
        // Fix for GBS-2381, order by score
        SortUtil.sort(result, new TMidTUidComparator(Locale.getDefault()));
        return result;
    }

    @Override
    public Set<GlobalSightLocale> getTmLocales(Tm tm)
            throws LingManagerException
    {
        try
        {
            return getInfo(tm).getLocalesForTm(tm);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
    }

    private SegmentTmInfo getInfo(Tm tm)
    {
        if (tm == null)
        {
            // DEBUG
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            // Walk upwards to find the first non-SQLUtil caller (skip 0, which
            // is Thread)
            for (int i = 1; i < stack.length; i++)
            {
                System.out.println(stack[i].toString());
            }
            // !DEBUG
            throw new IllegalArgumentException("Null tm");
        }
        return tm.getSegmentTmInfo();
    }

    /**
     * WARNING: This routine will leak a session object to the Reindexer that it
     * creates. The caller must clean it up by calling
     * TmUtil.closeStableSession().
     */
    @Override
    public Reindexer getReindexer(Collection<ProjectTM> tms)
            throws LingManagerException
    {
        // Leak this session to Reindexer, which runs in its own thread
        Session session = HibernateUtil.getSession();
        try
        {
            // Weed out any remote tms, since they don't get reindexed
            SortedTms sorted = sortTmsByImplementation(tms);
            sorted.tm2Tms.addAll(sorted.tm3Tms);
            return new Reindexer(session, sorted.tm2Tms);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public String getCreatingUserByTuvId(long tmId, long tuvId)
            throws RemoteException, LingManagerException
    {
        try
        {
            Tm tm = ServerProxy.getProjectHandler().getProjectTMById(tmId,
                    false);
            if (tm == null)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Can not find TM by tmId " + tmId
                            + ", perhaps it has been deleted.");
                }
                return null;
            }
            try
            {
                return getInfo(tm).getCreatingUserByTuvId(tm, tuvId);
            }
            catch (Exception e)
            {
                throw new LingManagerException(e);
            }
        }
        catch (NamingException e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public Date getModifyDateByTuvId(long tmId, long tuvId)
            throws RemoteException, LingManagerException
    {
        try
        {
            Tm tm = ServerProxy.getProjectHandler().getProjectTMById(tmId,
                    false);
            if (tm == null)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Can not find TM by tmId " + tmId
                            + ", perhaps it has been deleted.");
                }
                return null;
            }

            try
            {
                return getInfo(tm).getModifyDateByTuvId(tm, tuvId);
            }
            catch (Exception e)
            {
                throw new LingManagerException(e);
            }
        }
        catch (NamingException e)
        {
            throw new LingManagerException(e);
        }

    }

    @Override
    public String getSidByTuvId(long tmId, long tuvId) throws RemoteException,
            LingManagerException
    {
        try
        {
            Tm tm = ServerProxy.getProjectHandler().getProjectTMById(tmId,
                    false);
            if (tm == null)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Can not find TM by tmId " + tmId
                            + ", perhaps it has been deleted.");
                }
                return null;
            }
            try
            {
                return getInfo(tm).getSidByTuvId(tm, tuvId);
            }
            catch (Exception e)
            {
                throw new LingManagerException(e);
            }
        }
        catch (NamingException e)
        {
            throw new LingManagerException(e);
        }

    }

    @Override
    public String getSourceTextByTuvId(long tmId, long tuvId, long srcLocaleId)
            throws RemoteException, LingManagerException
    {
        try
        {
            Tm tm = ServerProxy.getProjectHandler().getProjectTMById(tmId,
                    false);
            if (tm == null)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Can not find TM by tmId " + tmId
                            + ", perhaps it has been deleted.");
                }
                return null;
            }
            try
            {
                return getInfo(tm).getSourceTextByTuvId(tm, tuvId, srcLocaleId);
            }
            catch (Exception e)
            {
                throw new LingManagerException(e);
            }
        }
        catch (NamingException e)
        {
            throw new LingManagerException(e);
        }
    }

    private static class SortedTms
    {
        List<Tm> tm2Tms = new ArrayList<Tm>();
        List<Tm> tm3Tms = new ArrayList<Tm>();
        List<Tm> remoteTms = new ArrayList<Tm>();
    }

    private SortedTms sortTmsByImplementation(Collection<? extends Tm> tms)
    {
        SortedTms sorted = new SortedTms();
        for (Tm tm : tms)
        {
            if (tm.getIsRemoteTm())
            {
                sorted.remoteTms.add(tm);
            }
            else if (tm.getTm3Id() != null)
            {
                sorted.tm3Tms.add(tm);
            }
            else
            {
                sorted.tm2Tms.add(tm);
            }
        }
        return sorted;
    }

    @Override
    public TuvBasicInfo getTuvBasicInfoByTuvId(long tmId, long tuvId,
            long srcLocaleId) throws RemoteException, LingManagerException
    {
        try
        {
            Tm tm = ServerProxy.getProjectHandler().getProjectTMById(tmId,
                    false);
            if (tm == null)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Can not find TM by tmId " + tmId
                            + ", perhaps it has been deleted.");
                }
                return null;
            }

            try
            {
                return getInfo(tm).getTuvBasicInfoByTuvId(tm, tuvId);
            }
            catch (Exception e)
            {
                throw new LingManagerException(e);
            }
        }
        catch (NamingException e)
        {
            throw new LingManagerException(e);
        }

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
