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
package com.globalsight.ling.tm2.segmenttm;

import static com.globalsight.ling.tm2.segmenttm.TmRemoveHelper.Query.REMOVE_TUV_L;
import static com.globalsight.ling.tm2.segmenttm.TmRemoveHelper.Query.REMOVE_TUV_L_BY_LANGUAGE;
import static com.globalsight.ling.tm2.segmenttm.TmRemoveHelper.Query.REMOVE_TUV_T;
import static com.globalsight.ling.tm2.segmenttm.TmRemoveHelper.Query.REMOVE_TUV_T_BY_LANGUAGE;
import static com.globalsight.ling.tm2.segmenttm.TmRemoveHelper.Query.REMOVE_TU_L;
import static com.globalsight.ling.tm2.segmenttm.TmRemoveHelper.Query.REMOVE_TU_T;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.projecthandler.ProjectTmTuT;
import com.globalsight.everest.projecthandler.ProjectTmTuvT;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.StatisticsInfo;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmInfo;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
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
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProgressReporter;

public class Tm2SegmentTmInfo implements SegmentTmInfo
{
    private boolean lock = false;
    
    @Override
    public LeverageMatchResults leverage(List<Tm> pTms,
            LeverageDataCenter pLeverageDataCenter, long p_jobId)
            throws Exception
    {
        Connection conn = DbUtil.getConnection();
        try
        {
            return new SegmentTmLeverager().leverage(conn, pTms,
                    pLeverageDataCenter, p_jobId);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    public LeverageMatches leverageSegment(BaseTmTuv pSourceTuv,
            LeverageOptions pLeverageOptions, List<Tm> pTms) throws Exception
    {
        Connection conn = DbUtil.getConnection();
        try
        {
            return new SegmentTmLeverager().leverageSegment(conn, pSourceTuv,
                    pLeverageOptions, pTms);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    public void deleteSegmentTmTus(Tm p_tm, Collection<SegmentTmTu> p_tus)
            throws Exception
    {
        Connection conn = DbUtil.getConnection();
        conn.setAutoCommit(false);
        try
        {
            new TmTuRemover().deleteTus(conn, p_tus);
            conn.commit();
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    public void deleteSegmentTmTuvs(Tm p_tm, Collection<SegmentTmTuv> p_tus)
            throws Exception
    {
        Connection conn = DbUtil.getConnection();
        conn.setAutoCommit(false);
        try
        {
            new TmTuRemover().deleteTuvs(conn, p_tus);
            conn.commit();
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    // This handles saving for both "save" and "populatePage" calls in the
    // TmCoreManager.
    @Override
    public TuvMappingHolder saveToSegmentTm(
            Collection<? extends BaseTmTu> p_segmentsToSave,
            GlobalSightLocale p_sourceLocale, Tm p_tm,
            Set<GlobalSightLocale> p_targetLocales, int p_mode,
            boolean p_fromTmImport) throws LingManagerException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            SegmentTmPopulator segTmPopulator = new SegmentTmPopulator(conn);
            segTmPopulator.setJob(getJob());
            return segTmPopulator.populateSegmentTm(p_segmentsToSave,
                    p_sourceLocale, p_tm, p_targetLocales, p_mode,
                    p_fromTmImport);
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
    public void updateSegmentTmTuvs(Tm p_tm, Collection<SegmentTmTuv> p_tuvs)
            throws LingManagerException
    {
        TmPopulator tmPopulator = new TmPopulator();
        tmPopulator.updateSegmentTmTuvs(p_tm, p_tuvs);
    }

    /**
     * Remove an entire TM.
     */
    @Override
    public boolean removeTmData(Tm pTm, ProgressReporter pReporter,
            InterruptMonitor pMonitor) throws LingManagerException
    {
        if (checkRemoveInterrupt(pReporter, pMonitor))
        {
            return false;
        }

        long tmId = pTm.getId();
        String tmName = pTm.getName();

        // pReporter.setMessageKey("lb_tm_remove_removing_tm_index",
        // "Removing Tm index ...");
        // pReporter.setPercentage(20);

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            TmRemoveHelper.removeIndex(tmId);
            SegmentTmPersistence persistence = new SegmentTmPersistence(conn);

            // get lock on Tm tables
            persistence.lockSegmentTmTables();

            if (checkRemoveInterrupt(pReporter, pMonitor))
            {
                return false;
            }
            // pReporter.setMessageKey("lb_tm_remove_removing_tuv_t",
            // "Removing translatable TUVs ...");
            // pReporter.setPercentage(35);
            TmRemoveHelper.removeData(conn, tmId, REMOVE_TUV_T);

            if (checkRemoveInterrupt(pReporter, pMonitor))
            {
                return false;
            }
            // pReporter.setMessageKey("lb_tm_remove_removing_tu_t",
            // "Removing translatable TUs ...");
            // pReporter.setPercentage(50);
            TmRemoveHelper.removeData(conn, tmId, REMOVE_TU_T);

            if (checkRemoveInterrupt(pReporter, pMonitor))
            {
                return false;
            }
            // pReporter.setMessageKey("lb_tm_remove_removing_tuv_l",
            // "Removing localizable TUVs ...");
            // pReporter.setPercentage(65);
            TmRemoveHelper.removeData(conn, tmId, REMOVE_TUV_L);

            if (checkRemoveInterrupt(pReporter, pMonitor))
            {
                return false;
            }
            // pReporter.setMessageKey("lb_tm_remove_removing_tu_l",
            // "Removing localizable TUs ...");
            // pReporter.setPercentage(80);
            TmRemoveHelper.removeData(conn, tmId, REMOVE_TU_L);

            pReporter.setMessageKey("", tmName
                    + " has been successfully removed.");
            conn.commit();
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            try
            {
                DbUtil.unlockTables(conn);
            }
            catch (Exception e)
            {
                throw new LingManagerException(e);
            }

            DbUtil.silentReturnConnection(conn);
        }

        if (checkRemoveInterrupt(pReporter, pMonitor))
        {
            return false;
        }

        return true;

    }

    public boolean removeTmData(Tm pTm, GlobalSightLocale pLocale,
            ProgressReporter pReporter, InterruptMonitor pMonitor)
            throws LingManagerException
    {
        if (checkRemoveInterrupt(pReporter, pMonitor))
        {
            return false;
        }

        String tmName = pTm.getName();

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            Set<GlobalSightLocale> tmLocales = LingServerProxy
                    .getTmCoreManager().getTmLocales(pTm);
            if (tmLocales.contains(pLocale))
            {
                SegmentTmPersistence persistence = new SegmentTmPersistence(
                        conn);

                // get lock on Tm tables
                persistence.lockSegmentTmTables();

                if (checkRemoveInterrupt(pReporter, pMonitor))
                {
                    return false;
                }

                long tmId = pTm.getId();
                long localeId = pLocale.getId();

                TmRemoveHelper.removeDataByLocale(conn, tmId, localeId,
                        REMOVE_TUV_T_BY_LANGUAGE);

                if (checkRemoveInterrupt(pReporter, pMonitor))
                {
                    return false;
                }

                TmRemoveHelper.removeDataByLocale(conn, tmId, localeId,
                        REMOVE_TUV_L_BY_LANGUAGE);
            }

            pReporter.setMessageKey("",
                    tmName + " - " + pLocale.getDisplayName()
                            + " has been successfully removed.");
            conn.commit();
            return true;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            try
            {
                DbUtil.unlockTables(conn);
            }
            catch (Exception e)
            {
                throw new LingManagerException(e);
            }

            DbUtil.silentReturnConnection(conn);
        }
    }

    private boolean checkRemoveInterrupt(ProgressReporter pReporter,
            InterruptMonitor pMonitor)
    {
        if (pMonitor.hasInterrupt())
        {
            pReporter.setMessageKey("lb_tm_remove_cancel_by_user",
                    "Tm removal has been cancelled by user request.");
            return true;
        }
        return false;
    }

    @Override
    public StatisticsInfo getStatistics(Tm pTm, Locale pUILocale,
            boolean p_includeProjects) throws LingManagerException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            return TmStatisticsHelper.getStatistics(conn, pTm, pUILocale,
                    p_includeProjects);
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
	public StatisticsInfo getTmExportInformation(Tm pTm, Locale pUILocale)
			throws LingManagerException
	{
		return getStatistics(pTm, pUILocale, true);
	}

	@Override
    // note this doesn't need the Tm, but it is needed in TM3 and we go along
    public List<SegmentTmTu> getSegmentsById(Tm tm, List<Long> tuIds)
            throws LingManagerException
    {
        Connection conn = null;
        TuReader r = null;
        try
        {
            conn = DbUtil.getConnection();
            r = new TuReader(conn);
            List<SegmentTmTu> tus = new ArrayList<SegmentTmTu>();
            r.batchReadTus(tuIds, 0, tuIds.size(), null);
            for (SegmentTmTu tu = r.getNextTu(); tu != null; tu = r.getNextTu())
            {
                tus.add(tu);
            }
            return tus;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            r.batchReadDone();
            DbUtil.silentReturnConnection(conn);
        }
    }

    static final int EXPORT_BATCH_SIZE = 200;

    @Override
    public SegmentResultSet getAllSegments(Tm tm, String createdBefore,
            String createdAfter, Connection conn) throws LingManagerException
    {
        try
        {
            return new Tm2SegmentResultSet(conn, TmExportHelper.getAllTuIds(
                    conn, tm, createdAfter, createdBefore), EXPORT_BATCH_SIZE);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

	@Override
	public SegmentResultSet getAllSegmentsByParamMap(Tm tm,
			Map<String, Object> paramMap, Connection conn)
			throws LingManagerException
	{
		try
		{
			return new Tm2SegmentResultSet(conn,
					TmExportHelper.getAllTuIdsByParamMap(conn, tm, paramMap),
					EXPORT_BATCH_SIZE);
		}
		catch (Exception e)
		{
			throw new LingManagerException(e);
		}
	}

    @Override
    public SegmentResultSet getAllSegments(Tm tm, long startTUId,
            Connection conn) throws LingManagerException
    {
        try
        {
            return new Tm2SegmentResultSet(conn, TmExportHelper.getAllTuIds(
                    conn, tm, startTUId), EXPORT_BATCH_SIZE);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public int getAllSegmentsCount(Tm tm, String createdBefore,
            String createdAfter) throws LingManagerException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            return TmExportHelper.getAllTuCount(conn, tm, createdAfter,
                    createdBefore);
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
	public int getAllSegmentsCountByParamMap(Tm tm, Map<String, Object> paramMap)
			throws LingManagerException
	{
		Connection conn = null;
		try
		{
			conn = DbUtil.getConnection();
			return TmExportHelper.getAllTuCountByParamMap(conn, tm, paramMap);
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
    public int getAllSegmentsCount(Tm tm, long startTUId)
            throws LingManagerException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            return TmExportHelper.getAllTuCount(conn, tm, startTUId);
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
    public List<TMidTUid> tmConcordanceQuery(List<Tm> tms, String query,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale,
            Connection conn) throws LingManagerException
    {
        TmConcordanceQuery q = new TmConcordanceQuery(conn);
        try
        {
            return q.query(tms, query, sourceLocale, targetLocale);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public Set<GlobalSightLocale> getLocalesForTm(Tm tm)
            throws LingManagerException
    {
        Set<GlobalSightLocale> result = new HashSet<GlobalSightLocale>();

        Statement stmt = null;
        ResultSet rset = null;
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery("SELECT distinct tuv.locale_id "
                    + "FROM project_tm_tuv_t tuv, project_tm_tu_t tu "
                    + "WHERE tu.tm_id = " + tm.getId()
                    + "  AND tu.id = tuv.tu_id");

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
            DbUtil.silentClose(rset);
            DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(conn);
        }
        return result;
    }

    @Override
    public int getSegmentCountForReindex(Tm tm)
    {
        Connection conn = null;
        int i = 0;
        try
        {
            conn = DbUtil.getConnection();
            i = TmExportHelper.getAllTuvCount(conn, tm, null, null);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
        return i;
    }

    @Override
    public boolean reindexTm(Tm tm, Reindexer reindexer, boolean indexTarget)
    {
        try
        {
            return new Tm2Reindexer().reindexTm(tm, reindexer, indexTarget);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return (o != null && o instanceof Tm2SegmentTmInfo);
    }

    @Override
    public String getCreatingUserByTuvId(Tm tm, long tuvId)
    {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        return (ptuv != null) ? ptuv.getCreationUser() : null;
    }

    @Override
    public Date getModifyDateByTuvId(Tm tm, long tuvId)
    {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        return (ptuv != null) ? ptuv.getModifyDate() : null;
    }

    @Override
    public String getSidByTuvId(Tm tm, long tuvId)
    {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        return (ptuv != null) ? ptuv.getSid() : null;
    }

    @Override
    public String getSourceTextByTuvId(Tm tm, long tuvId, long srcLocaleId)
    {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        if (ptuv != null)
        {
            ProjectTmTuT ptu = ptuv.getTu();
            for (ProjectTmTuvT tuv : ptu.getTuvs())
            {
                if (tuv.getLocale().getId() == srcLocaleId)
                {
                    return tuv.getSegmentString();
                }
            }
        }
        return null;
    }

    @Override
    public TuvBasicInfo getTuvBasicInfoByTuvId(Tm tm, long tuvId)
    {
        ProjectTmTuvT ptuv = HibernateUtil.get(ProjectTmTuvT.class, tuvId);
        return (ptuv == null) ? null : new TuvBasicInfo(
                ptuv.getSegmentString(), ptuv.getSegmentClob(),
                ptuv.getExactMatchKey(), ptuv.getLocale(),
                ptuv.getCreationDate(), ptuv.getCreationUser(),
                ptuv.getModifyDate(), ptuv.getModifyUser(),
                ptuv.getUpdatedByProject(), ptuv.getSid());
    }

    private Job m_job;

    @Override
    public void setJob(Job job)
    {
        m_job = job;
    }

    @Override
    public Job getJob()
    {
        return m_job;
    }

    @Override
    public void setLock(boolean lock)
    {
        this.lock = lock;
    }

    @Override
    public boolean getLock()
    {
        return this.lock;
    }
}
