package com.globalsight.ling.tm2;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import com.globalsight.everest.tm.StatisticsInfo;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.indexer.Reindexer;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatchResults;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.segmenttm.TmConcordanceQuery.TMidTUid;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProgressReporter;

/**
 * Interface to abstract out the details
 * of a segment TM implementation (tm2 vs tm3).
 */
public interface SegmentTmInfo {

    public TuvMappingHolder saveToSegmentTm(Session p_session, 
            Collection<? extends BaseTmTu> p_segmentsToSave,
            GlobalSightLocale p_sourceLocale, Tm p_tm,
            Set<GlobalSightLocale> p_targetLocales, int p_mode, 
            boolean p_fromTmImport) throws LingManagerException;
    
    /**
     * Updates existing TUVs in a Segment Tm.
     *
     * @param p_tmId Tm id in which segments are updated
     * @param p_tuvs Collection of SegmentTmTuv objects.
     *
     * @throws LingManagerException
     */
    public void updateSegmentTmTuvs(Session p_session, Tm p_tm, 
                Collection<SegmentTmTuv> p_tuvs) throws LingManagerException;


    /**
     * leverage a page from Segment Tm
     * 
     * @param p_session Hibernate session
     * @param p_tms tms to leverage
     * @param p_leverageDataCenter
     *            LeverageDataCenter that contains segments that are leveraged.
     * @return LeverageMatchResults that contains leverage results
     */
    public LeverageMatchResults leverage(Session p_session, List<Tm> p_tms,
            LeverageDataCenter p_leverageDataCenter) throws Exception;

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
    public LeverageMatches leverageSegment(Session pSession,
            BaseTmTuv p_sourceTuv, LeverageOptions p_leverageOptions,
            List<Tm> p_tms) 
            throws Exception;

    // These are called for individual segment deletion (TmSegmentDeleteHandler)
    public void deleteSegmentTmTus(Session pSession, Tm p_tm, 
            Collection<SegmentTmTu> p_tus) throws Exception;
    
    public void deleteSegmentTmTuvs(Session pSession, Tm p_tm, 
            Collection<SegmentTmTuv> p_tus) throws Exception;

    
    //
    // Refactored stuff
    //
    
    // TM removal (TmRemover / RemoveTmHandler)
    public boolean removeTmData(Session pSession, Tm pTm, ProgressReporter pReporter,
            InterruptMonitor pMonitor) throws LingManagerException;

    // TM removal (TmRemover / RemoveTmHandler) - if "Remove by locale" is being used
    public boolean removeTmData(Session pSession, Tm pTm, GlobalSightLocale pLocale,
            ProgressReporter pReporter, InterruptMonitor pMonitor)
            throws LingManagerException;
    
    public StatisticsInfo getStatistics(Session session, Tm pTm, Locale pUILocale, 
            boolean p_includeProjects) throws LingManagerException;

    public List<SegmentTmTu> getSegmentsById(Session session, Tm tm,
            List<Long> tuIds) throws LingManagerException;

    public SegmentResultSet getAllSegments(Session session, Tm tm, String createdBefore,
            String createdAfter) throws LingManagerException;

    public SegmentResultSet getSegmentsByLocale(Session session, Tm tm, String locale,
            String createdBefore, String createdAfter)
            throws LingManagerException;

    public SegmentResultSet getSegmentsByProjectName(Session session, Tm tm,
            String projectName, String createdBefore, String createdAfter)
            throws LingManagerException;

    public int getAllSegmentsCount(Session session, Tm tm, String createdBefore,
            String createdAfter) throws LingManagerException;

    public int getSegmentsCountByLocale(Session session, Tm tm, String locale,
            String createdBefore, String createdAfter)
            throws LingManagerException;

    public int getSegmentsCountByProjectName(Session session, Tm tm,
            String projectName, String createdBefore, String createdAfter)
            throws LingManagerException;
    
    public List<TMidTUid> tmConcordanceQuery(List<Tm> tms, String query, 
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale)
            throws LingManagerException;
    
    public Set<GlobalSightLocale> getLocalesForTm(Session session, Tm tm)
            throws LingManagerException;
    
    
    /**
     * Get the number of segments in this TM that need to be reindexed.
     * In many cases, this will be the same as the total number of segments.
     */
    public int getSegmentCountForReindex(Session session, Tm tm);
    
    /**
     * Reindex a single TM.  Implementations should call 
     * reindexer.incrementCounter() with updates to their progress, 
     * and periodically check reindexer.getInterrupted() to see if 
     * the user has cancelled the operation.
     * 
     * @param session Session to use for this operation
     * @param tm TM to reindex
     * @param reindexer Reindexer object for updating reindex count using
     *        calls to incrementCounter()
     * @return true if reindexing was successful
     */
    public boolean reindexTm(Session session, Tm tm, Reindexer reindexer);
    
    /**
     * Gross hacks in support of prior legacy hacks.  See 
     * TmCoreManager.getSidByTuvId() et al
     */
    public String getSidByTuvId(Session session, Tm tm, long tuvId);
    public String getCreatingUserByTuvId(Session session, Tm tm, long tuvId);
    public Date getModifyDateByTuvId(Session session, Tm tm, long tuvId);
    
    /**
     * This one is particularly egregious.  It's not clear to me if srcLocaleId is
     * ever the locale *other* than the one attached to the specified tuv.  If 
     * not, then we can do away with the parameter altogether.
     */
    public String getSourceTextByTuvId(Session session, Tm tm, long tuvId, long srcLocaleId);



}
