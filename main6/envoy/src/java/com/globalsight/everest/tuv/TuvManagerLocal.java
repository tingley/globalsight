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

package com.globalsight.everest.tuv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * The TuvManagerLocal class is an implementation of TuvManager interface.
 */
public final class TuvManagerLocal implements TuvManager
{
    static private final Logger CATEGORY = Logger
            .getLogger(TuvManagerLocal.class);

    /**
     * Factory method for creating Tu instances. Does not persist the Tu.
     * 
     * @param p_tmId
     *            Tm identifier.
     * @param p_dataType
     *            DataType.
     * @param p_tuType
     *            TuType.
     * @param p_localizableType
     *            LocalizableType.
     * @param p_pid
     *            paragraph id (= GXML blockId)
     * @return Tu
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tu createTu(long p_tmId, String p_dataType, TuType p_tuType,
            char p_localizableType, long p_pid) throws TuvException,
            RemoteException

    {
        TuImpl tu = new TuImpl();

        tu.setTmId(p_tmId);
        tu.setDataType(p_dataType);
        tu.setTuType(p_tuType);
        tu.setLocalizableType(p_localizableType);
        tu.setPid(p_pid);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("createTu" + " p_tmId=" + p_tmId + " p_dataType="
                    + p_dataType + " p_tuType=" + p_tuType.toString()
                    + " p_localizableType=" + p_localizableType + " p_pid="
                    + p_pid + " returned TuImpl=" + tu.toString());
        }

        return tu;
    }

    /**
     * Factory method for creating Tuv instances. Does not persist the Tuv. Sets
     * the TUV state to NOT_LOCALIZED.
     * 
     * @param p_wordCount
     *            word count in the segment text.
     * @param p_globalSightLocale
     *            GlobalSightLocale of the Tuv.
     * @param p_sourcePage
     *            SourcePage associated with the Tuv.
     * @return Tuv
     * @throws TuvException
     *             when an error occurs.
     */
    public Tuv createTuv(int p_wordCount, GlobalSightLocale p_locale,
            SourcePage p_sourcePage) throws TuvException, RemoteException
    {
        TuvImpl tuv = new TuvImpl();

        tuv.setWordCount(p_wordCount);
        tuv.setGlobalSightLocale(p_locale);
        // tuv.setSourcePage(p_sourcePage);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("createTuv" + " p_wordCount=" + p_wordCount
                    + " p_globalSightLocale=" + p_locale.toString()
                    + " p_sourcePage= " + p_sourcePage.getId()
                    + " returned tuvImpl=" + tuv.toString());
        }

        return tuv;
    }
    
    /**
     * Update TuvImpl
     *
     * @param p_tuv
     *            TuvImpl
     * @throws Exception
     */
    public static void updateTuvs(Connection p_connection,
            List<TuvImplVo> p_tuvs, long p_jobId) throws Exception
    {
        if (p_tuvs == null || p_tuvs.size() == 0)
        {
            return;
        }

        PreparedStatement tuvUpdateStmt = null;
        try
        {
            StringBuilder sql = new StringBuilder();
            sql.append("update ")
                    .append(BigTableUtil.getTuvTableJobDataInByJobId(p_jobId))
                    .append(" set ");

            sql.append("segment_clob = ?, ");// 1
            sql.append("segment_string = ?,");// 2
            sql.append("exact_match_key = ?,");// 3
            sql.append("state = ?, "); // 4
            sql.append("merge_state = ?, ");// 5
            sql.append("timestamp = ?, ");// 6
            sql.append("last_modified = ?, ");// 7
            sql.append("modify_user = ? where id = ?");// 8,9

            tuvUpdateStmt = p_connection.prepareStatement(sql.toString());

            Timestamp t = new Timestamp(System.currentTimeMillis());
            
            // addBatch counters
            int batchUpdate = 0;

            for (TuvImplVo tuv : p_tuvs)
            {
                TuvImpl temp = new TuvImpl();
                temp.setGxml(tuv.getGxml());
                
                tuvUpdateStmt.setString(1, temp.getSegmentClob());
                tuvUpdateStmt.setString(2, temp.getSegmentString());
                tuvUpdateStmt.setLong(3, GlobalSightCrc
                        .calculate(((TuvLing) tuv).getExactMatchFormat()));
                tuvUpdateStmt.setString(4, tuv.getState() == null ? TuvState.LOCALIZED.getName()
                        : tuv.getState().getName());
                tuvUpdateStmt.setString(5, tuv.getMergeState());
                tuvUpdateStmt.setTimestamp(6, t);
                tuvUpdateStmt.setTimestamp(7, t);
                tuvUpdateStmt.setString(8, tuv.getLastModifiedUser());
                tuvUpdateStmt.setLong(9, tuv.getId());
                tuvUpdateStmt.addBatch();

                batchUpdate++;

                if (batchUpdate > DbUtil.BATCH_INSERT_UNIT)
                {
                    tuvUpdateStmt.executeBatch();
                    batchUpdate = 0;
                }
            }

            // execute the rest of the added batch
            if (batchUpdate > 0)
            {
                tuvUpdateStmt.executeBatch();
            }

            p_connection.commit();
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            DbUtil.silentClose(tuvUpdateStmt);
        }
    }
  
    /**
     * Saves the target segments from an offline edit.
     * 
     * @param p_tuvs
     *            TuvImplVo List of Tuvs to be saved
     * @throws TuvException
     *             when an error occurs.
     */
    public void saveTuvsFromOfflineNoJms(List<TuvImplVo> p_tuvs, long p_jobId)
            throws TuvException
    {
        Connection conn = null;
        boolean isAutoCommit = true;
        try
        {
            conn = DbUtil.getConnection();
            isAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            updateTuvs(conn, p_tuvs, p_jobId);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Cannot save offline TUV", ex);
            throw new TuvException("Cannot save offline TUVs", ex);
        }
        finally
        {
            try
            {
                conn.setAutoCommit(isAutoCommit);
            }
            catch (SQLException e)
            {
            }
            DbUtil.silentReturnConnection(conn);
        }
     }
    
    /**
     * Saves the target segments from an offline edit.
     * 
     * @param p_tuvs
     *            TuvImplVo List of Tuvs to be saved
     * @throws TuvException
     *             when an error occurs.
     */
    public void saveTuvsFromOffline(List<TuvImplVo> p_tuvs, long p_jobId)
            throws TuvException
    {
        TuvImpl tuvInDb = null;
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            List<TuvImpl> tuvsToBeUpdated = new ArrayList<TuvImpl>();
            for (TuvImplVo tuv : p_tuvs)
            {
                tuvInDb = SegmentTuvUtil.getTuvById(conn, tuv.getId(), p_jobId);
                tuvInDb.setExactMatchKey(GlobalSightCrc
                        .calculate(((TuvLing) tuv).getExactMatchFormat()));
                tuvInDb.setGxml(tuv.getGxml());
                tuvInDb.setState(tuv.getState() == null ? TuvState.LOCALIZED
                        : tuv.getState());
                tuvInDb.setMergeState(tuv.getMergeState());
                tuvInDb.setLastModifiedUser(tuv.getLastModifiedUser());
                tuvInDb.setLastModified(new Date());
                tuvInDb.setTimestamp(new Timestamp(System.currentTimeMillis()));

                tuvsToBeUpdated.add(tuvInDb);
            }

            SegmentTuvUtil.updateTuvs(tuvsToBeUpdated, p_jobId);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Cannot save offline TUV", ex);
            CATEGORY.error("id: " + tuvInDb.getId());
            CATEGORY.error("segment: " + tuvInDb.getSegmentString());
            // Ensure xliffAlt is loaded if it has
            Set<XliffAlt> alts = tuvInDb.getXliffAlt(true);
            CATEGORY.error("xliffAlt size: " + alts.size());
            int i = 0;
            for (XliffAlt alt : alts)
            {
                i++;
                CATEGORY.error("  XliffAlt " + i);
                CATEGORY.error("    id :" + alt.getId());
                CATEGORY.error("    segment :" + alt.getSegment());
                CATEGORY.error("    language :" + alt.getLanguage());
                CATEGORY.error("    quality :" + alt.getQuality());
                CATEGORY.error("    tuv id :" + alt.getTuvId());
            }

            throw new TuvException("Cannot save offline TUVs", ex);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * Clones the source page's tuv for a target page based on the specified
     * locale.
     * 
     * @param p_sourceLocaleTuv
     *            - The tuv to be cloned.
     * @param p_targetLocale
     *            - The locale of the tuv.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv cloneToTarget(Tuv p_sourceTuv, GlobalSightLocale p_targetLocale)
            throws TuvException, RemoteException
    {
        TuvImpl tuv = new TuvImpl((TuvImpl) p_sourceTuv);
        tuv.setGlobalSightLocale(p_targetLocale);
        tuv.setExactMatchKey(0);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("cloneToTarget" + " old tuv id "
                    + p_sourceTuv.getId() + " target locale="
                    + p_targetLocale.toString() + " new tuv id " + tuv.getId());
        }

        return tuv;
    }

    /**
     * Clones the source page's tuv for a target page based on the specified
     * locale.
     * 
     * @param p_sourceLocaleTuv
     *            - The tuv to be cloned.
     * @param p_targetLocale
     *            - The locale of the tuv.
     * @param p_matchType
     *            match type that the Tuv p_gxml came from. Must be
     *            LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH,
     *            LeverageMatchType.EXACT_MATCH or
     *            LeverageMatchType.UNVERIFIED_EXACT_MATCH.
     * @returns cloned Tuv associated it with a target locale.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv cloneToTarget(Tuv p_sourceLocaleTuv,
            GlobalSightLocale p_targetLocale, String p_gxml, int p_matchType)
            throws TuvException, RemoteException
    {
        TuvImpl tuv = (TuvImpl) cloneToTarget(p_sourceLocaleTuv, p_targetLocale);

        switch (p_matchType)
        {
            case LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH:
                tuv.setState(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED);
                break;
            case LeverageMatchType.EXACT_MATCH:
                tuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
                break;
            case LeverageMatchType.UNVERIFIED_EXACT_MATCH:
                tuv.setState(TuvState.UNVERIFIED_EXACT_MATCH);
                break;
            default:
                TuvException ex = new TuvException("cloneToTarget: "
                        + "p_matchType " + Integer.toString(p_matchType)
                        + " not valid for this method");
                CATEGORY.error(ex.getMessage(), ex);
                throw ex;
        }

        tuv.setGxmlWithSubIds(p_gxml);

        return tuv;
    }

    /**
     * Gets Tuvs in the specified locale for the export process.
     * 
     * @param p_sourcePage
     *            source locale page.
     * @param p_globalSightLocale
     *            GlobalSightLocale to return Tuvs in.
     * @return Collection Tuvs
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection getExportTuvs(SourcePage p_sourcePage,
            GlobalSightLocale p_locale) throws TuvException, RemoteException
    {
        List result = null;
        try
        {
            result = SegmentTuvUtil.getExportTuvs(p_locale.getId(),
                    p_sourcePage.getId());
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }

        return result;
    }

    /**
     * Gets PageSegments.
     * 
     * @param p_sourcePage
     *            source page.
     * @param p_trgLocales
     *            a collection of target locales
     * @return PageSegments.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public PageSegments getPageSegments(SourcePage p_sourcePage,
            Collection<GlobalSightLocale> p_trgLocales) throws TuvException,
            RemoteException
    {
        List<TuImplVo> tus = null;
        Connection conn = null;

        try
        {
            conn = DbUtil.getConnection();
            TuvJdbcQuery tuvQuery = new TuvJdbcQuery(conn);
            tus = tuvQuery.getTusBySourcePageIdAndLocales(p_sourcePage,
                    p_trgLocales);
            // Improve performance (required)
            loadTuTuvsPrior(p_sourcePage, p_trgLocales);
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new TuvException(ex);
        }
        finally
        {
            if (conn != null)
            {
                DbUtil.silentReturnConnection(conn);
            }
        }

        return new PageSegments(tus, p_sourcePage, p_trgLocales);
    }

    /**
     * Load data prior to improve performance.
     */
    private void loadTuTuvsPrior(SourcePage p_sourcePage,
            Collection p_trgLocales)
    {
        try
        {
            // Load all TUs for page
            SegmentTuUtil.getTusBySourcePageId(p_sourcePage.getId());
            // Load all source TUVs for source page
            SegmentTuvUtil.getSourceTuvs(p_sourcePage);
            // Load all target TUVs for target locales.
            for (TargetPage tp : p_sourcePage.getTargetPages())
            {
                if (p_trgLocales.contains(tp.getGlobalSightLocale()))
                {
                    SegmentTuvUtil.getTargetTuvs(tp);
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Error when load data prior for source page and target pages",
                    e);
        }
    }

    /**
     * Resets the status of the target tuv to NOT_LOCALIZED.
     * 
     * @param p_sourcePage
     *            source locale page.
     * @param p_sourceGlobalSightLocale
     *            the source locale.
     * @param p_sourceGlobalSightLocale
     *            the target locale.
     * @return Map of target locale Tuvs keyed by source locale TuvId.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public void resetTargetTuvStatus(SourcePage p_sourcePage,
            GlobalSightLocale p_targetLocale) throws TuvException,
            RemoteException
    {
        try
        {
            TargetPage targetPage = null;
            for (TargetPage tp : p_sourcePage.getTargetPages())
            {
                if (tp.getLocaleId() == p_targetLocale.getId())
                {
                    targetPage = tp;
                    break;
                }
            }

            List<TuvImpl> targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);
            for (TuvImpl targetTuv : targetTuvs)
            {
                targetTuv.setState(TuvState.NOT_LOCALIZED);
                targetTuv.setLastModified(new Date());
            }

            long jobId = p_sourcePage.getJobId();
            SegmentTuvUtil.updateTuvs(targetTuvs, jobId);
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }
    }

    /**
     * Get the Tuvs for the on-line editor for each specified Tu, in the
     * specified locale.
     * 
     * @param p_tuIds
     *            array of Tu Ids.
     * @param p_localeId
     *            locale Id associated with returned Tuv.
     * @param p_jobId
     * @return Collection of Tuv objects.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    @SuppressWarnings("rawtypes")
    public Collection getTuvsForOnlineEditor(long[] p_tuIds, long p_localeId,
            long p_jobId) throws TuvException, RemoteException
    {
        List tuvs = new ArrayList();
        try
        {
            tuvs = SegmentTuvUtil.getTuvsByTuIdsLocaleId(p_tuIds, p_localeId,
                    p_jobId);
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }

        return tuvs;
    }

    /**
     * Gets the Tuv for the segment editor for the specified Tu, in the
     * specified locale.
     * 
     * @param p_tuId
     *            unique identifier of a Tu object.
     * @param p_localeId
     *            identifier of a GlobalSightLocale associated with returned
     *            Tuv.
     * @return Tuv.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv getTuvForSegmentEditor(long p_tuId, long p_localeId,
            long p_jobId) throws TuvException, RemoteException
    {
        Tuv tuv = null;
        try
        {
            tuv = SegmentTuvUtil.getTuvByTuIdLocaleId(p_tuId, p_localeId,
                    p_jobId);
        }
        catch (Exception ex)
        {
            throw new TuvException(ex);
        }

        return tuv;
    }

    /**
     * Gets the Tuv for the segment editor for the specified Tuv identifier.
     * 
     * @param p_tuvId
     *            unique identifier of a Tuv object.
     * @return Tuv
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Tuv getTuvForSegmentEditor(long p_tuvId, long jobId)
            throws TuvException, RemoteException
    {
        try
        {
            return SegmentTuvUtil.getTuvById(p_tuvId, jobId);
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }
    }

    public Tu getTuForSegmentEditor(long p_tuId, long jobId)
            throws TuvException, RemoteException
    {
        try
        {
            return SegmentTuUtil.getTuById(p_tuId, jobId);
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }
    }

    /**
     * Gets the TaskTuvs from the end of each of the previous stages of the
     * workflow in reverse chronological order. The parameter p_maxResultNum is
     * ignored.
     * 
     * @param p_tuvId
     *            unique identifier of a Tuv object.
     * @param p_maxResultNum
     *            maximum number of TaskTuvs to return.
     * @return reverse chronological ordered List of TaskTuvs.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public List getPreviousTaskTuvs(long p_tuvId, int p_maxResultNum)
            throws TuvException, RemoteException
    {
        String hql = " from TaskTuv t where t.currentTuvId= :tuvId order by t.version desc";
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery(hql);
        query.setMaxResults(p_maxResultNum);
        query.setLong("tuvId", p_tuvId);
        List taskTuvs = query.list();
        // session.close();

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("getPreviousTaskTuvs" + " p_tuvId=" + p_tuvId
                    + " returned " + taskTuvs.size() + " TaskTuvs="
                    + taskTuvs.toString());
        }

        return taskTuvs;
    }

    /**
     * Updates a changed Tuv in the database.
     * 
     * @param p_tuv
     *            changed Tuv.
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public void updateTuvToLocalizedState(Tuv p_tuv, long p_jobId)
            throws TuvException, RemoteException
    {
        try
        {
            TuvImpl tuv = (TuvImpl) p_tuv;

            tuv.setExactMatchKey(GlobalSightCrc.calculate(tuv
                    .getExactMatchFormat()));
            tuv.setState(TuvState.LOCALIZED);
            tuv.setLastModified(new Date());

            SegmentTuvUtil.updateTuv(tuv, p_jobId);
        }
        catch (Exception ex)
        {
            throw new TuvException("updateTuv " + p_tuv.toString(), ex);
        }
    }

    /**
     * Gets source locale Tuvs for Statistics based on the SourcePage.
     * 
     * @param p_sourcePage
     *            source locale page.
     * @return Collection Tuvs
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection getSourceTuvsForStatistics(SourcePage p_sourcePage)
            throws TuvException, RemoteException
    {
        List result = new ArrayList();
        try
        {
            result = SegmentTuvUtil.getSourceTuvs(p_sourcePage);
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }

        return result;
    }

    /**
     * Gets target locale tuvs for statistics based on the TargetPage.
     * 
     * @param p_targetPage
     *            target locale page.
     * @return Collection Tuvs
     * @throws TuvException
     *             when an error occurs.
     * @throws RemoteException
     *             when a communication-related error occurs.
     */
    public Collection<Tuv> getTargetTuvsForStatistics(TargetPage p_targetPage)
            throws TuvException, RemoteException
    {
        List<Tuv> result = new ArrayList<Tuv>();
        try
        {
            result.addAll(SegmentTuvUtil.getTargetTuvs(p_targetPage));
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }

        return result;
    }

    /**
     * Retrieves the list of TUs belonging to a source page.
     * 
     * @param p_sourcePageId
     *            the id of the source page
     * @return Collection a sorted list of tus from the source page
     */
    public Collection<TuImpl> getTus(Long p_sourcePageId) throws TuvException,
            RemoteException
    {
        try
        {
            return SegmentTuUtil.getTusBySourcePageId(p_sourcePageId);
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }
    }

    /**
     * Judge if current page is WorldServer XLF file.
     * 
     * @param p_sourcePageId
     * @return boolean
     * @throws TuvException
     * @throws RemoteException
     */
    public boolean isWorldServerXliffSourceFile(Long p_sourcePageId)
            throws TuvException, RemoteException
    {
        boolean result = false;
        try
        {
            result = SegmentTuUtil.isWorldServerXlfSourceFile(p_sourcePageId);
        }
        catch (Exception e)
        {
            throw new TuvException(e);
        }

        return result;
    }

    /**
     * Computes and updates the exact match keys for a set of tuvs. This
     * function does neither read objects from nor write objects to the
     * database.
     */
    public void setExactMatchKeys(List p_tuvs)
    {
        for (int i = 0, max = p_tuvs.size(); i < max; i++)
        {
            TuvImpl tuv = (TuvImpl) p_tuvs.get(i);
            String exactMatchFormat = tuv.getExactMatchFormat();

            // generate exact match key
            long crc = GlobalSightCrc.calculate(exactMatchFormat);
            tuv.setExactMatchKey(crc);
        }
    }
}
