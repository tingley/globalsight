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
package com.globalsight.ling.tm2.population;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.exporter.TmxChecker;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.SegmentTmInfo;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.indexer.TmSegmentIndexer;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.persistence.PageJobDataRetriever;
import com.globalsight.ling.tm2.persistence.PageTmPersistence;
import com.globalsight.ling.tm2.persistence.PageTmRetriever;
import com.globalsight.ling.tm2.persistence.SegmentQueryResult;
import com.globalsight.ling.tm2.persistence.SegmentTmPersistence;
import com.globalsight.ling.tm2.persistence.TmSegmentSaver;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.terminology.TermbaseManager;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

/**
 * Responsible to populate Tm
 */

public class TmPopulator
{
    private static final Logger LOGGER = Logger.getLogger(TmPopulator.class);

    public static final boolean TRANSLATABLE = true;
    public static final boolean LOCALIZABLE = false;

    /**
     * Save source and target segments that belong to the page. Segments are
     * saved in Page Tm and Segment Tm. This method saves all target locale
     * data.
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
            LeverageOptions p_options, long p_jobId)
            throws LingManagerException
    {
        Set<GlobalSightLocale> targetLocales = p_options.getLeveragingLocales()
                .getAllTargetLocales();
        return populatePage(p_page, p_options, targetLocales, p_jobId);
    }

    /**
     * Save source and target segments that belong to the page. Segments are
     * saved in Page Tm and Segment Tm. This method saves a specified target
     * locale data.
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
        Set<GlobalSightLocale> targetLocales = new HashSet<GlobalSightLocale>();
        targetLocales.add(p_locale);
        return populatePage(p_page, p_options, targetLocales, p_jobId);
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
     * @param p_targetLocales
     *            Set of target locales (GlobalSightLocale)
     * @return mappings of translation_unit_variant id and project_tm_tuv_t id
     *         of this page
     */
    private TuvMappingHolder populatePage(SourcePage p_page,
            LeverageOptions p_options, Set<GlobalSightLocale> p_targetLocales,
            long p_jobId) throws LingManagerException
    {
        TuvMappingHolder mappingHolder = null;
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            Job job = p_page.getRequest().getJob();
            GlobalSightLocale sourceLocale = p_page.getGlobalSightLocale();
            // prepare a repository of job data for the page
			PageJobData pageJobData = new PageJobData(sourceLocale, p_jobId,
					job.getJobName());

            // Get page data from TU and TUV table
            PageJobDataRetriever pageJobDataRetriever = new PageJobDataRetriever(
                    conn, p_page.getId(), sourceLocale, p_jobId);
            SegmentQueryResult result = pageJobDataRetriever
                    .queryForPopulation(p_targetLocales);
            BaseTmTu tu = null;
            while ((tu = result.getNextTu()) != null)
            {
                pageJobData.addTu((PageTmTu) tu, sourceLocale);
            }
            pageJobDataRetriever.close();

            // populate Page TM
            LOGGER.debug("Populating Page TM");
            populatePageTm(pageJobData.getTusToSaveToPageTm(p_options), p_page,
                    p_targetLocales);

            // populate into term-base if "Terminology Approval" is yes.
			populateIntoTermbase(pageJobData, job, p_page, p_options, p_targetLocales);

            // populate Segment TM
            LOGGER.debug("Populating Segment TM");
            Tm tm = ServerProxy.getProjectHandler().getProjectTMById(
                    p_options.getSaveTmId(), true);
            SegmentTmInfo segtmInfo = tm.getSegmentTmInfo();
            segtmInfo.setJob(job);
			mappingHolder = segtmInfo.saveToSegmentTm(
					pageJobData.getTusToSaveToSegmentTm(p_options,
							p_targetLocales, p_page), sourceLocale, tm,
					p_targetLocales, TmCoreManager.SYNC_MERGE, false);
        }
        catch (LingManagerException le)
        {
            throw le;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        return mappingHolder;
    }

	private void populateIntoTermbase(PageJobData pageJobData, Job job,
			SourcePage p_page, LeverageOptions p_options,
			Set<GlobalSightLocale> p_targetLocales)
	{
        try
        {
            if (job.getFileProfile().getTerminologyApproval() == 1)
            {
                String termbaseName = p_page.getRequest().getL10nProfile()
                        .getProject().getTermbaseName();
                Termbase tb = TermbaseList
                        .get(String.valueOf(p_page.getCompanyId()),
                                termbaseName);
                if (tb != null)
				{
					TermbaseManager tbm = new TermbaseManager();
					Collection p_segmentsToSave = pageJobData
							.getTusToSaveToSegmentTm(p_options,
									p_targetLocales, p_page);
					Iterator it = p_segmentsToSave.iterator();
					while (it.hasNext())
					{
						// separate subflows out of the main text and save
						// them as
						// independent segments
						BaseTmTu baseTu = (BaseTmTu) it.next();
						List baseTuvs = baseTu.getTuvs();
						tbm.batchAddTuvsAsNew(tb.getId(), baseTuvs, p_page
								.getRequest().getL10nProfile().getProject()
								.getProjectManager().getUserId());
					}
				}
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Terminology populating error: "
                    + e.getMessage());
        }
    }

    /**
     * Saves segments to a Segment Tm. Use <code>p_mode</code> to overwrite
     * existing TUs, merge existing TUs with new TUs, or to discard new TUs if
     * they already exist in the TM.
     * 
     * @param p_segmentsToSave
     *            Collection of SegmentTmTu objects.
     * @param p_saveTmId
     *            Tm id in which segments are saved
     * @param p_mode
     *            one of the "SYNC" constants, to overwrite existing TUs, merge
     *            existing TUs with new TUs, or to discard new TUs if they
     *            already exist in the TM.
     * @return TuvMappingHolder. m_tuvId and m_tuId values are arbitrary.
     */
    public TuvMappingHolder saveSegmentToSegmentTm(
            Collection<SegmentTmTu> p_segmentsToSave, Tm p_tm, int p_mode)
            throws LingManagerException
    {
        String sourceTmName = null;
        boolean isFromTmImport = false;
        return saveSegmentToSegmentTm(p_segmentsToSave, p_tm, p_mode,
                sourceTmName, isFromTmImport);
    }

    // if "p_sourceTmName" is valid, it should be from tm import. 
    public TuvMappingHolder saveSegmentToSegmentTm(
            Collection<SegmentTmTu> p_segmentsToSave, Tm p_tm, int p_mode,
            String p_sourceTmName) throws LingManagerException
    {
        boolean isFromTmImport = true;
        return saveSegmentToSegmentTm(p_segmentsToSave, p_tm, p_mode,
                p_sourceTmName, isFromTmImport);
    }

    private TuvMappingHolder saveSegmentToSegmentTm(
            Collection<SegmentTmTu> p_segmentsToSave, Tm p_tm, int p_mode,
            String p_sourceTmName, boolean isFromTmImport)
            throws LingManagerException
    {
        // sort Tus by source locale
        SegmentTuCollector segmentTuCollector = new SegmentTuCollector();

        TmxChecker tmxChecker = new TmxChecker();
        Iterator<SegmentTmTu> itTu = p_segmentsToSave.iterator();
        while (itTu.hasNext())
        {
            SegmentTmTu tu = (SegmentTmTu) itTu.next();
            if (StringUtil.isNotEmpty(p_sourceTmName))
            {
                tu.setSourceTmName(p_sourceTmName);
            }
            //
            List<BaseTmTuv> tuvs = tu.getTuvs();
            for (BaseTmTuv tuv : tuvs)
            {
            	tuv.setSegment(tmxChecker.revertInternalTag(tuv.getSegment()));
            }
            segmentTuCollector.addTu(tu);
        }

        TuvMappingHolder mappingHolder = null;
        try
        {
            Iterator<GlobalSightLocale> itSourceLocale = segmentTuCollector
                    .getAllSourceLocales().iterator();
            while (itSourceLocale.hasNext())
            {
                GlobalSightLocale sourceLocale = itSourceLocale.next();
                Collection<SegmentTmTu> tus = segmentTuCollector
                        .getTus(sourceLocale);
                Set<GlobalSightLocale> targetLocales = segmentTuCollector
                        .getTargetLocales(sourceLocale);

                // call populateSegmentTm per source locale
                mappingHolder = p_tm.getSegmentTmInfo().saveToSegmentTm(tus,
                        sourceLocale, p_tm, targetLocales, p_mode,
                        isFromTmImport);
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

        return mappingHolder;
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
    public void updateSegmentTmTuvs(Tm p_tm, Collection<SegmentTmTuv> p_tuvs)
            throws LingManagerException
    {
        // c_logger.debug("# of segment for update: " + p_tuvs.size());
        long tmId = p_tm.getId();
        try
        {
            Collection<SegmentTmTuv> sourceTranslatable = new ArrayList<SegmentTmTuv>();
            Collection<SegmentTmTuv> sourceLocalizable = new ArrayList<SegmentTmTuv>();
            Collection<SegmentTmTuv> targetTranslatable = new ArrayList<SegmentTmTuv>();
            Collection<SegmentTmTuv> targetLocalizable = new ArrayList<SegmentTmTuv>();

            Iterator<SegmentTmTuv> itTuv = p_tuvs.iterator();
            while (itTuv.hasNext())
            {
                SegmentTmTuv tuv = (SegmentTmTuv) itTuv.next();
                // set exact match key based on the current segment string
                tuv.setExactMatchKey();

                if (tuv.isSourceTuv())
                {
                    if (tuv.isTranslatable())
                    {
                        sourceTranslatable.add(tuv);
                    }
                    else
                    {
                        sourceLocalizable.add(tuv);
                    }
                }
                else
                {
                    if (tuv.isTranslatable())
                    {
                        targetTranslatable.add(tuv);
                    }
                    else
                    {
                        targetLocalizable.add(tuv);
                    }
                }
            }

            if (sourceTranslatable.size() > 0)
            {
                updateSegmentTmTuvsSource(tmId, sourceTranslatable,
                        TRANSLATABLE);
            }

            if (sourceLocalizable.size() > 0)
            {
                updateSegmentTmTuvsSource(tmId, sourceLocalizable,
                        LOCALIZABLE);
            }

            if (targetTranslatable.size() > 0)
            {
                updateSegmentTmTuvsTarget(tmId, targetTranslatable,
                        TRANSLATABLE);
            }

            if (targetLocalizable.size() > 0)
            {
                updateSegmentTmTuvsTarget(tmId, targetLocalizable,
                        LOCALIZABLE);
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
    }

    //
    // Private Methods
    //

    /*
     * Save segments to Page Tm
     * 
     * @param p_segmentsToSave Collection of PageTmTu (job data)
     * 
     * @param p_sourcePage SourcePage object
     * 
     * @param p_targetLocales Set of target locales of all segments
     */
    private void populatePageTm(Collection p_segmentsToSave,
            SourcePage p_sourcePage, Set p_targetLocales) throws Exception
    {
        // sanity check. No data to save, no further processing
        if (p_segmentsToSave.size() == 0)
        {
            return;
        }

        Connection conn = DbUtil.getConnection();
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        GlobalSightLocale sourceLocale = p_sourcePage.getGlobalSightLocale();

        // save segments to UniqueSegmentRepository to remove duplicates
        UniqueSegmentRepository jobDataToSave = new UniqueSegmentRepository(
                sourceLocale);
        Iterator it = p_segmentsToSave.iterator();
        while (it.hasNext())
        {
            jobDataToSave.addTu((BaseTmTu) it.next());
        }

        try
        {
            PageTmPersistence ptPersistence = new PageTmPersistence(conn);

            // get lock on tables
            ptPersistence.lockPageTmTables();
            LOGGER.debug("Got lock for page tm");

            // Get page data from Page TM
            PageTmSegmentRepository pageTmData = getPageDataFromPageTm(conn,
                    p_sourcePage, p_targetLocales);
            // Walk through Tus to be saved and see if an identical source
            // segment exists in the Page TM. According to the test
            // result, mark Tus for creation or Tuvs for addition to the
            // database in SegmentsForSave object.
            SegmentsForSave segmentsForSave = new SegmentsForSave(sourceLocale,
                    pageTmData.getTmId());
            it = jobDataToSave.sourceTuvIterator();
            while (it.hasNext())
            {
                BaseTmTuv jobSourceTuv = (BaseTmTuv) it.next();
                BaseTmTu jobTu = jobSourceTuv.getTu();
                BaseTmTu pageTmTu = pageTmData.getMatchedTu(jobSourceTuv);
                if (pageTmTu == null)
                {
                    segmentsForSave.addTuForCreate(jobTu);
                }
                else
                {
                    segmentsForSave.addAllTargetTuvsForAdd(pageTmTu.getId(),
                            jobTu);
                }
            }
            // Save segments to Page TM
            TmSegmentSaver tmSegmentSaver = new TmSegmentSaver(conn);
            tmSegmentSaver.saveToPageTm(segmentsForSave, pageTmData.getTmId());
            LOGGER.debug("Releasing lock for page tm");
            // commit the change and unlock tables
            conn.commit();
            conn.setAutoCommit(autoCommit);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LOGGER.debug("Releasing lock for page tm");
            // rollback the changes and unlock tables
            conn.rollback();
            throw e;
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            // rollback the changes and unlock tables
            conn.rollback();
            throw new Exception(t.getMessage());
        }
        finally
        {
            DbUtil.unlockTables(conn);
        	DbUtil.silentReturnConnection(conn);
        }
    }

    private PageTmSegmentRepository getPageDataFromPageTm(Connection conn,
            SourcePage p_sourcePage, Set p_targetLocales) throws Exception
    {
        GlobalSightLocale sourceLocale = p_sourcePage.getGlobalSightLocale();

        PageTmSegmentRepository pageTmData = new PageTmSegmentRepository(
                sourceLocale);
        // Prepare Page TM for population
        PageTmPersistence ptp = new PageTmPersistence(conn);
        // check if the Page TM for the page already exists.
        long tmId = ptp.getPageTmId(p_sourcePage.getExternalPageId(),
                sourceLocale);

        if (tmId == 0)
        {
            // The Page TM doesn't exist. Create one.
            tmId = ptp.createPageTm(p_sourcePage.getExternalPageId(),
                    sourceLocale);
        }
        else
        {
            // the Page TM already exists.
            // delete segments in the page in the specified locale
            ptp.deleteTargetSegments(tmId, p_targetLocales);
            // retrieve all source segments in the page
            PageTmRetriever pageTmRetriever = new PageTmRetriever(conn, tmId,
                    sourceLocale);
            SegmentQueryResult result = pageTmRetriever.query();
            BaseTmTu tu = null;
            while ((tu = result.getNextTu()) != null)
            {
                pageTmData.addTu(tu);
            }
            pageTmRetriever.close();
        }
        pageTmData.setTmId(tmId);
        return pageTmData;
    }

    private Set getAllTargetLocales(BaseTmTu p_tu,
            GlobalSightLocale p_sourceLocale)
    {
        HashSet targetLocales = null;
        if (p_tu != null)
        {
            targetLocales = new HashSet(p_tu.getAllTuvLocales());
            // remove source locale
            targetLocales.remove(p_sourceLocale);
        }
        else
        {
            targetLocales = new HashSet();
        }

        return targetLocales;
    }

    private void updateSegmentTmTuvsSource(long p_tmId, Collection p_tuvs,
            boolean p_translatable) throws Exception
    {
        // sort the collection of Tuvs by locale
        Map localeMap = sortTuvsByLocale(p_tuvs);

        // call segment update method by locale
        Iterator itLocale = localeMap.keySet().iterator();
        while (itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) itLocale.next();
            updateSegmentTmTuvsSourceByLocale(p_tmId,
                    (List) localeMap.get(locale), locale, p_translatable);
        }
    }

    // Updates given target segments in Segment Tm
    private void updateSegmentTmTuvsTarget(long p_tmId, Collection p_tuvs,
            boolean p_translatable) throws Exception
    {
        // sort the collection of Tuvs by locale
        Map localeMap = sortTuvsByLocale(p_tuvs);

        // call segment update method by locale
        Iterator itLocale = localeMap.keySet().iterator();
        while (itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) itLocale.next();
            updateSegmentTmTuvsTargetByLocale(p_tmId,
                    (List) localeMap.get(locale), locale, p_translatable);
        }
    }

    private void updateSegmentTmTuvsSourceByLocale(long p_tmId, List p_tuvs,
            GlobalSightLocale p_locale, boolean p_translatable)
            throws Exception
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            SegmentTmPersistence stPersistence = new SegmentTmPersistence(conn);

            // get lock on tables
            stPersistence.getLockForSegmentUpdate(p_translatable);

            // check for the existence of the modified Tuvs. If they
            // don't exist anymore in the database, remove them from
            // the list of Tuvs for update
            Collection nonExistingTuvs = stPersistence.checkForTuvExistence(
                    p_tuvs, p_translatable);

            List tuvsForUpdate = removeTuvsById(p_tuvs, nonExistingTuvs);

            // Check for the duplicate source segment in the same
            // Project Tm as the result of the source segment text
            // update. If the possible duplicates are detected, the
            // two Tus are merged.
            Map duplicateTus = stPersistence.checkForTuvDuplicatesForSource(
                    tuvsForUpdate, p_tmId, p_locale, p_translatable);

            if (duplicateTus.size() > 0)
            {
                TuMergeData tuMergeData = new TuMergeData(duplicateTus);

                List tuvsForRemoval = tuMergeData.getTuvsForRemoval();
                List tuvsForChangeTuId = tuMergeData.getTuvsForChangeTuId();
                List tuvsForTimeUpdate = tuMergeData.getTuvsForTimeChange();
                List tusForRemoval = tuMergeData.getTusForRemoval();
                List removedSource = tuMergeData.getRemovedSourceTuvs();

                tuvsForUpdate = removeTuvsById(tuvsForUpdate, removedSource);

                // remove Tuvs and their indexes
                stPersistence.removeTuvs(tuvsForRemoval, p_translatable,
                        p_locale, p_tmId);

                // change tu_id
                stPersistence.changeTuId(tuvsForChangeTuId, p_translatable,
                        p_locale, p_tmId, true);

                // update last modified time
                stPersistence.updateLastModifiedTimeTuvs(tuvsForTimeUpdate,
                        p_translatable);

                // remove Tus
                stPersistence.removeTus(tusForRemoval, p_translatable);
            }

            // update segment text and reindex the segments
            stPersistence.updateTuvs(tuvsForUpdate, p_translatable, p_locale,
                    p_tmId, TmSegmentIndexer.SOURCE);

            // commit the change and unlock tables
            conn.commit();
            conn.setAutoCommit(autoCommit);
        }
        catch (Exception e)
        {
            // rollback the changes and unlock tables
            conn.rollback();
            throw e;
        }
        catch (Throwable t)
        {
            // rollback the changes and unlock tables
            conn.rollback();
            throw new Exception(t.getMessage());
        }
        finally
        {
            DbUtil.unlockTables(conn);
            // Return connection at last.
            DbUtil.silentReturnConnection(conn);
        }
    }

    private void updateSegmentTmTuvsTargetByLocale(long p_tmId, List p_tuvs,
            GlobalSightLocale p_locale, boolean p_translatable)
            throws Exception
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            SegmentTmPersistence stPersistence = new SegmentTmPersistence(conn);

            // get lock on tables
            stPersistence.getLockForSegmentUpdate(p_translatable);

            // check for the existence of the modified Tuvs. If they
            // don't exist anymore in the database, remove them from
            // the list of Tuvs for update
            Collection nonExistingTuvs = stPersistence.checkForTuvExistence(
                    p_tuvs, p_translatable);

            List tuvsForUpdate = removeTuvsById(p_tuvs, nonExistingTuvs);

            // Check for the duplicate segment in the same Tu as the
            // result of the segment text update. If the possible
            // duplicates are detected, the Tuvs that are planned to be
            // updated will be removed from the database and the last
            // modified column of the Tuv that has the same text will
            // be updated.
            Map duplicateTuvs = stPersistence.checkForTuvDuplicatesForTarget(
                    tuvsForUpdate, p_tmId, p_locale, p_translatable);

            if (duplicateTuvs.size() > 0)
            {
                Collection tuvsToBeRemoved = duplicateTuvs.keySet();
                Collection tuvsForTimeUpdate = duplicateTuvs.values();
                tuvsForUpdate = removeTuvsById(tuvsForUpdate, tuvsToBeRemoved);

                // remove Tuvs and their indexes
                stPersistence.removeTuvs(tuvsToBeRemoved, p_translatable,
                        p_locale, p_tmId);

                // update last modified time
                stPersistence.updateLastModifiedTimeTuvs(tuvsForTimeUpdate,
                        p_translatable);
            }

            // update segment text and reindex the segments
            stPersistence.updateTuvs(tuvsForUpdate, p_translatable, p_locale,
                    p_tmId, TmSegmentIndexer.NOT_SOURCE);

            // commit the change and unlock tables
            conn.commit();
            conn.setAutoCommit(autoCommit);
        }
        catch (Exception e)
        {
            // rollback the changes and unlock tables
            conn.rollback();
            throw e;
        }
        catch (Throwable t)
        {
            // rollback the changes and unlock tables
            conn.rollback();
            throw new Exception(t.getMessage());
        }
        finally
        {
            DbUtil.unlockTables(conn);
            // Return connection at last.
            DbUtil.silentReturnConnection(conn);
        }
    }

    private Map sortTuvsByLocale(Collection p_tuvs)
    {
        Map localeMap = new HashMap();
        Iterator itTuv = p_tuvs.iterator();
        while (itTuv.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
            List tuvList = (List) localeMap.get(tuv.getLocale());
            if (tuvList == null)
            {
                tuvList = new ArrayList();
                localeMap.put(tuv.getLocale(), tuvList);
            }
            tuvList.add(tuv);
        }

        return localeMap;
    }

    /**
     * Remove all Tuvs in the second list from the first list of Tuvs by
     * comparing Tuv ids.
     * 
     * @param p_tuvs
     *            Collection of BaseTmTuv
     * @param p_removed
     *            Collection of BaseTmTuv to be removed.
     * @return p_tuvs, from which all element of p_removed are removed.
     */
    private List removeTuvsById(List p_tuvs, Collection p_removed)
    {
        Iterator itToBeRemoved = p_removed.iterator();
        while (itToBeRemoved.hasNext())
        {
            BaseTmTuv toBeRemoved = (BaseTmTuv) itToBeRemoved.next();

            Iterator itTuv = p_tuvs.iterator();
            while (itTuv.hasNext())
            {
                BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
                if (tuv.getId() == toBeRemoved.getId())
                {
                    itTuv.remove();
                    break;
                }
            }
        }

        return p_tuvs;
    }

    /**
     * This inner class is a data repository of merging Tus. It takes a list of
     * a pair of Tus to be merged and keep the list of:
     * 
     * Tus to be deleted Tuvs to be deleted Tuvs of which tu_id are changed
     * source Tuvs of which last modified time are changed source Tuvs to be
     * deleted
     */
    private class TuMergeData
    {
        private List m_tusForRemoval = new ArrayList();
        private List m_tuvsForRemoval = new ArrayList();
        private List m_tuvsForChangeTuId = new ArrayList();
        private List m_sourceTuvsForTimeUpdate = new ArrayList();
        private List m_sourceTuvsForRemoval = new ArrayList();

        /**
         * Constructor
         * 
         * @param p_tusToBeMerged
         *            Map of Tus that are merged. The key-value pair is the two
         *            Tus that are merged. The type of both object is
         *            SegmentTmTu.
         */
        private TuMergeData(Map p_tusToBeMerged)
        {
            Iterator itEntrySet = p_tusToBeMerged.entrySet().iterator();
            while (itEntrySet.hasNext())
            {
                Map.Entry entry = (Map.Entry) itEntrySet.next();
                SegmentTmTu merging = (SegmentTmTu) entry.getKey();
                SegmentTmTu merged = (SegmentTmTu) entry.getValue();

                m_tusForRemoval.add(merging);
                m_tuvsForRemoval.add(merging.getSourceTuv());
                m_sourceTuvsForRemoval.add(merging.getSourceTuv());
                m_sourceTuvsForTimeUpdate.add(merged.getSourceTuv());

                sortDataForMerge(merging, merged);
            }
        }

        private List getTuvsForRemoval()
        {
            return m_tuvsForRemoval;
        }

        private List getTuvsForChangeTuId()
        {
            return m_tuvsForChangeTuId;
        }

        private List getTuvsForTimeChange()
        {
            return m_sourceTuvsForTimeUpdate;
        }

        private List getTusForRemoval()
        {
            return m_tusForRemoval;
        }

        private List getRemovedSourceTuvs()
        {
            return m_sourceTuvsForRemoval;
        }

        private void sortDataForMerge(SegmentTmTu p_merging,
                SegmentTmTu p_merged)
        {
            Set localesForMerging = p_merging.getAllTuvLocales();
            GlobalSightLocale sourceLocale = p_merging.getSourceLocale();

            Iterator itLocaleForMerging = localesForMerging.iterator();
            while (itLocaleForMerging.hasNext())
            {
                GlobalSightLocale localeForMerging = (GlobalSightLocale) itLocaleForMerging
                        .next();

                // ignore source locale
                if (localeForMerging.equals(sourceLocale))
                {
                    continue;
                }

                Collection tuvsMerging = p_merging.getTuvList(localeForMerging);
                Collection tuvsMerged = p_merged.getTuvList(localeForMerging);
                if (tuvsMerged == null)
                {
                    // add all Tuvs in "merging" to "merged"
                    addTuvs(tuvsMerging, p_merged);
                }
                else
                {
                    // compare the list of Tuvs in a locale and decide
                    // which one are added or deleted
                    compareTuvList(tuvsMerging, tuvsMerged);
                }
            }
        }

        // change tu_id of the Tuvs and add them to change tu_id Tuv list
        private void addTuvs(Collection p_tuvsForAdd, BaseTmTu p_newTu)
        {
            Iterator itTuv = p_tuvsForAdd.iterator();
            while (itTuv.hasNext())
            {
                BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
                tuv.setTu(p_newTu);
                m_tuvsForChangeTuId.add(tuv);
            }
        }

        // compare the list of Tuvs in a locale and decide which one
        // are added or deleted
        private void compareTuvList(Collection p_tuvsMerging,
                Collection p_tuvsMerged)
        {
            Iterator itTuvMerging = p_tuvsMerging.iterator();
            while (itTuvMerging.hasNext())
            {
                BaseTmTuv tuvMerging = (BaseTmTuv) itTuvMerging.next();

                boolean found = false;
                BaseTmTuv tuvMerged = null;
                Iterator itTuvMerged = p_tuvsMerged.iterator();
                while (itTuvMerged.hasNext())
                {
                    tuvMerged = (BaseTmTuv) itTuvMerged.next();

                    if (tuvMerging.equals(tuvMerged))
                    {
                        found = true;

                        if (tuvMerged.getModifyDate().after(
                                tuvMerging.getModifyDate()))
                        {
                            m_tuvsForRemoval.add(tuvMerging);
                        }
                        else
                        {
                            m_tuvsForRemoval.add(tuvMerged);
                            tuvMerging.setTu(tuvMerged.getTu());
                            m_tuvsForChangeTuId.add(tuvMerging);
                        }

                        break;
                    }
                }

                if (!found)
                {
                    tuvMerging.setTu(tuvMerged.getTu());
                    m_tuvsForChangeTuId.add(tuvMerging);
                }
            }
        }

    }

    private class SegmentTuCollector
    {
        // Key: Source locale (GlobalSightLocale)
        // Value: SegmentTus (inner class of this class)
        private Map<GlobalSightLocale, SegmentTus> m_sourceLocaleTuMap = new HashMap<GlobalSightLocale, SegmentTus>();

        private void addTu(SegmentTmTu p_tu)
        {
            GlobalSightLocale sourceLocale = p_tu.getSourceLocale();
            SegmentTus segmentTus = m_sourceLocaleTuMap.get(sourceLocale);
            if (segmentTus == null)
            {
                segmentTus = new SegmentTus();
                m_sourceLocaleTuMap.put(sourceLocale, segmentTus);
            }
            segmentTus.addTu(p_tu);
        }

        private Set<GlobalSightLocale> getAllSourceLocales()
        {
            return m_sourceLocaleTuMap.keySet();
        }

        private Collection<SegmentTmTu> getTus(GlobalSightLocale p_sourceLocale)
        {
            Collection<SegmentTmTu> tus = null;

            SegmentTus segmentTus = m_sourceLocaleTuMap.get(p_sourceLocale);
            if (segmentTus != null)
            {
                tus = segmentTus.getTus();
            }

            return tus;
        }

        private Set<GlobalSightLocale> getTargetLocales(
                GlobalSightLocale p_sourceLocale)
        {
            Set<GlobalSightLocale> targetLocales = null;

            SegmentTus segmentTus = m_sourceLocaleTuMap.get(p_sourceLocale);
            if (segmentTus != null)
            {
                targetLocales = segmentTus.getTargetLocales();
                targetLocales.remove(p_sourceLocale);
            }

            return targetLocales;
        }

        private class SegmentTus
        {
            private Collection<SegmentTmTu> m_tus = new ArrayList<SegmentTmTu>();
            private Set<GlobalSightLocale> m_targetLocales = new HashSet();

            private void addTu(SegmentTmTu p_tu)
            {
                m_tus.add(p_tu);
                m_targetLocales.addAll(p_tu.getAllTuvLocales());
            }

            private Collection<SegmentTmTu> getTus()
            {
                return m_tus;
            }

            private Set<GlobalSightLocale> getTargetLocales()
            {
                return m_targetLocales;
            }
        }
    }

}
