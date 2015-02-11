package com.globalsight.ling.tm2.segmenttm;

import static com.globalsight.ling.tm2.segmenttm.TmRemoveHelper.Query.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.projecthandler.ProjectTmTuT;
import com.globalsight.everest.projecthandler.ProjectTmTuvT;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.StatisticsInfo;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.segmenttm.TmConcordanceQuery;
import com.globalsight.ling.tm2.segmenttm.TmConcordanceQuery.TMidTUid;
import com.globalsight.ling.tm2.segmenttm.TuReader;
import com.globalsight.everest.util.comparator.LocaleComparator;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmInfo;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TuQueryResult;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.indexer.Reindexer;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatchResults;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.SegmentTmLeverager;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.persistence.SegmentTmPersistence;
import com.globalsight.ling.tm2.population.TmPopulator;
import com.globalsight.ling.tm2.segmenttm.TmStatisticsHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProgressReporter;

public class Tm2SegmentTmInfo implements SegmentTmInfo {
    
    
    @Override
    public LeverageMatchResults leverage(Session pSession, List<Tm> pTms,
            LeverageDataCenter pLeverageDataCenter) throws Exception {
        return new SegmentTmLeverager().leverage(pSession.connection(), pTms, pLeverageDataCenter);
    }

    @Override
    public LeverageMatches leverageSegment(Session pSession, 
            BaseTmTuv pSourceTuv, LeverageOptions pLeverageOptions,
            List<Tm> pTms) throws Exception {
        return new SegmentTmLeverager().leverageSegment(pSession.connection(), 
                                    pSourceTuv, pLeverageOptions, pTms);
    }

    @Override
    public void deleteSegmentTmTus(Session pSession, Tm p_tm,
            Collection<SegmentTmTu> p_tus) throws Exception {
        new TmTuRemover().deleteTus(pSession.connection(), p_tus);
    }
    
    @Override
    public void deleteSegmentTmTuvs(Session pSession, Tm p_tm,
            Collection<SegmentTmTuv> p_tus) throws Exception {
        new TmTuRemover().deleteTuvs(pSession.connection(), p_tus);
    }
    
    // This handles saving for both "save" and "populatePage" calls in the TmCoreManager.
    @Override
    public TuvMappingHolder saveToSegmentTm(Session p_session, 
            Collection<? extends BaseTmTu> p_segmentsToSave,
            GlobalSightLocale p_sourceLocale, Tm p_tm,
            Set<GlobalSightLocale> p_targetLocales, int p_mode, 
            boolean p_fromTmImport) throws LingManagerException {

        try {
            return new SegmentTmPopulator(p_session.connection()).populateSegmentTm(
                            p_segmentsToSave, p_sourceLocale, 
                            p_tm, p_targetLocales, p_mode, p_fromTmImport);
        }
        catch (Exception e) {
            throw new LingManagerException(e);
        }
    }

    @Override
    public void updateSegmentTmTuvs(Session p_session, Tm p_tm, 
            Collection<SegmentTmTuv> p_tuvs) throws LingManagerException {
        TmPopulator tmPopulator = new TmPopulator(p_session);
        tmPopulator.updateSegmentTmTuvs(p_tm, p_tuvs);
    }

    /**
     * Remove an entire TM.  
     */
    @Override
    public boolean removeTmData(Session pSession, Tm pTm, ProgressReporter pReporter,
            InterruptMonitor pMonitor) throws LingManagerException {
        if (checkRemoveInterrupt(pReporter, pMonitor)) {
            return false;
        }

        long tmId = pTm.getId();
        
        pReporter.setMessageKey("lb_tm_remove_removing_tm_index", "Removing Tm index ...");
        pReporter.setPercentage(20);

        Connection conn = pSession.connection();
        try
        {           
            TmRemoveHelper.removeIndex(tmId);
            SegmentTmPersistence persistence = new SegmentTmPersistence(conn);

            // get lock on Tm tables
            persistence.lockSegmentTmTables();

            if (checkRemoveInterrupt(pReporter, pMonitor)) {
                return false;
            }
            pReporter.setMessageKey("lb_tm_remove_removing_tuv_t", 
                    "Removing translatable TUVs ...");
            pReporter.setPercentage(35);
            TmRemoveHelper.removeData(conn, tmId, REMOVE_TUV_T);

            if (checkRemoveInterrupt(pReporter, pMonitor)) {
                return false;
            }
            pReporter.setMessageKey("lb_tm_remove_removing_tu_t", 
                    "Removing translatable TUs ...");
            pReporter.setPercentage(50);
            TmRemoveHelper.removeData(conn, tmId, REMOVE_TU_T);

            if (checkRemoveInterrupt(pReporter, pMonitor)) {
                return false;
            }
            pReporter.setMessageKey("lb_tm_remove_removing_tuv_l", 
                    "Removing localizable TUVs ...");
            pReporter.setPercentage(65);
            TmRemoveHelper.removeData(conn, tmId, REMOVE_TUV_L);

            if (checkRemoveInterrupt(pReporter, pMonitor)) {
                return false;
            }
            pReporter.setMessageKey("lb_tm_remove_removing_tu_l", 
                    "Removing localizable TUs ...");
            pReporter.setPercentage(80);
            TmRemoveHelper.removeData(conn, tmId, REMOVE_TU_L);

        } catch (Exception e) {
            throw new LingManagerException(e);
        }
        finally
        {
            try {
                DbUtil.unlockTables(conn);
            } catch (Exception e) {
                throw new LingManagerException(e);
            }
        }

        if (checkRemoveInterrupt(pReporter, pMonitor)) {
            return false;
        }
        
        return true;
 
    }

    public boolean removeTmData(Session pSession, Tm pTm, GlobalSightLocale pLocale,
            ProgressReporter pReporter, InterruptMonitor pMonitor)
            throws LingManagerException {
        if (checkRemoveInterrupt(pReporter, pMonitor)) {
            return false;
        }
        Connection conn = pSession.connection();
        try {
            SegmentTmPersistence persistence = new SegmentTmPersistence(conn);

            // get lock on Tm tables
            persistence.lockSegmentTmTables();

            if (checkRemoveInterrupt(pReporter, pMonitor)) {
                return false;
            }
            
            long tmId = pTm.getId();
            long localeId = pLocale.getId();

            pReporter.setMessageKey("lb_tm_remove_removing_tuv_t", 
                    "Removing translatable TUVs ...");
            pReporter.setPercentage(30);
            TmRemoveHelper.removeDataByLocale(conn, tmId, localeId, 
                    REMOVE_TUV_T_BY_LANGUAGE);
            
            if (checkRemoveInterrupt(pReporter, pMonitor)) {
                return false;
            }
            
            pReporter.setMessageKey("lb_tm_remove_removing_tuv_l", 
                    "Removing localizable TUVs ...");
            pReporter.setPercentage(60);
            TmRemoveHelper.removeDataByLocale(conn, tmId, localeId, 
                    REMOVE_TUV_L_BY_LANGUAGE);
        } catch (Exception e) {
            throw new LingManagerException(e);
        }
        finally
        {
            try {
                DbUtil.unlockTables(conn);
            } catch (Exception e) {
                throw new LingManagerException(e);
            }
        }
        
        pReporter.setMessageKey("lb_tm_remove_tmlang_success", 
                "Tm language has been successfully removed.");
        pReporter.setPercentage(100);
        return true;
    }
    
    private boolean checkRemoveInterrupt(ProgressReporter pReporter,
                        InterruptMonitor pMonitor) {
        if (pMonitor.hasInterrupt()) {
            pReporter.setMessageKey("lb_tm_remove_cancel_by_user", 
                        "Tm removal has been cancelled by user request.");
            return true;
        }
        return false;
    }

    @Override
    public StatisticsInfo getStatistics(Session session, Tm pTm, Locale pUILocale,
            boolean p_includeProjects) throws LingManagerException {
        Connection conn = session.connection();
        return TmStatisticsHelper.getStatistics(conn, pTm, pUILocale, p_includeProjects);
    }

    @Override
    // note this doesn't need the Tm, but it is needed in TM3 and we go along
    public List<SegmentTmTu> getSegmentsById(Session session, Tm tm,
            List<Long> tuIds) throws LingManagerException {
        TuReader r = new TuReader(session);
        List<SegmentTmTu> tus = new ArrayList<SegmentTmTu>();
        try {
            r.batchReadTus(tuIds, 0, tuIds.size(), null);
            for (SegmentTmTu tu = r.getNextTu(); tu != null; tu = r.getNextTu()) {
                tus.add(tu);
            }
            return tus;
        }
        catch (Exception e) {
            throw new LingManagerException(e);
        }
        finally {
            r.batchReadDone();
        }
    }

    static final int EXPORT_BATCH_SIZE = 200;

    @Override
    public SegmentResultSet getAllSegments(Session session, Tm tm, String createdBefore,
            String createdAfter) throws LingManagerException {
        try {
            return new Tm2SegmentResultSet(session, 
                TmExportHelper.getAllTuIds(session.connection(), tm, createdAfter, createdBefore), 
                EXPORT_BATCH_SIZE);
        }
        catch (Exception e) {
            throw new LingManagerException(e);
        }
    }   

    @Override
    public int getAllSegmentsCount(Session session, Tm tm,
            String createdBefore, String createdAfter)
            throws LingManagerException {
        try {
            return TmExportHelper.getAllTuCount(session.connection(), tm, createdAfter, createdBefore);
        }
        catch (Exception e) {
            throw new LingManagerException(e);
        }    
    }

    @Override
    public SegmentResultSet getSegmentsByLocale(Session session, Tm tm, String locale,
            String createdBefore, String createdAfter)
            throws LingManagerException {
        try {
            return new Tm2SegmentResultSet(session, TmExportHelper.getFilteredTuIds(tm, locale, 
                    createdBefore, createdAfter), EXPORT_BATCH_SIZE);
        }
        catch (Exception e) {
            throw new LingManagerException(e);
        }
    }

    @Override
    public int getSegmentsCountByLocale(Session session, Tm tm, String locale,
            String createdBefore, String createdAfter)
            throws LingManagerException {
        try {
            return TmExportHelper.getFilteredTuCount(tm, locale, createdAfter, createdBefore);
        }
        catch (Exception e) {
            throw new LingManagerException(e);
        }
    }
    
    @Override
    public SegmentResultSet getSegmentsByProjectName(Session session, Tm tm,
            String projectName, String createdBefore, String createdAfter)
            throws LingManagerException {
        try {
            return new Tm2SegmentResultSet(session, TmExportHelper.getProjectNameTuIds(tm, projectName, 
                    createdBefore, createdAfter), EXPORT_BATCH_SIZE);
        }
        catch (Exception e) {
            throw new LingManagerException(e);
        }
    }

    @Override
    public int getSegmentsCountByProjectName(Session session, Tm tm,
            String projectName, String createdBefore, String createdAfter)
            throws LingManagerException {
        try {
            return TmExportHelper.getProjectTuCount(tm, projectName, createdAfter, createdBefore);
        }
        catch (Exception e) {
            throw new LingManagerException(e);
        }
    }
    
    @Override
    public List<TMidTUid> tmConcordanceQuery(List<Tm> tms, String query, 
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale)
            throws LingManagerException {
        TmConcordanceQuery q = new TmConcordanceQuery(HibernateUtil.getSession().connection());
        try {
            return q.query(tms, query, sourceLocale, targetLocale);
        } 
        catch (Exception e) {
            throw new LingManagerException(e);
        }
    }
    
    // Originally from TmManagerLocal.doGetProjectTmLocales
    @Override
    public Set<GlobalSightLocale> getLocalesForTm(Session session, Tm tm) 
                    throws LingManagerException {
        Set<GlobalSightLocale> result = new HashSet<GlobalSightLocale>();

        Statement stmt = null;
        ResultSet rset = null;
        Connection conn = session.connection();
        try
        {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(
                "SELECT distinct tuv.locale_id " +
                "FROM project_tm_tuv_t tuv, project_tm_tu_t tu " +
                "WHERE tu.tm_id = " + tm.getId() +
                "  AND tu.id = tuv.tu_id");

            LocaleManager mgr = ServerProxy.getLocaleManager();
            while (rset.next())
            {
                long localeId = rset.getLong(1);

                GlobalSightLocale locale = mgr.getLocaleById(localeId);

                result.add(locale);
            }
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable ignore) {}

        }
        return result;
    }
    
    @Override
    public int getSegmentCountForReindex(Session session, Tm tm) {
        return getAllSegmentsCount(session, tm, null, null);
    }
    
    @Override
    public boolean reindexTm(Session session, Tm tm, Reindexer reindexer) {
        try {
            return new Tm2Reindexer(session).reindexTm(tm, reindexer);
        } catch (Exception e) {
            throw new LingManagerException(e);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        return (o != null && o instanceof Tm2SegmentTmInfo);
    }
    
    @Override
    public String getCreatingUserByTuvId(Session session, Tm tm, long tuvId) {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        return (ptuv != null) ? ptuv.getCreationUser() : null;
    }

    @Override
    public Date getModifyDateByTuvId(Session session, Tm tm, long tuvId) {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        return (ptuv != null) ? ptuv.getModifyDate() : null;
    }

    @Override
    public String getSidByTuvId(Session session, Tm tm, long tuvId) {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        return (ptuv != null) ? ptuv.getSid() : null;
    }

    @Override
    public String getSourceTextByTuvId(Session session, Tm tm, long tuvId, long srcLocaleId) {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        if (ptuv != null) {
            ProjectTmTuT ptu = ptuv.getTu();
            for (ProjectTmTuvT tuv : ptu.getTuvs()) {
                if (tuv.getLocale().getId() == srcLocaleId) {
                    return tuv.getSegmentString();
                }
            }
        }
        return null;
    }

    

}
