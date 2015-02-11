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

package com.globalsight.everest.persistence.tuv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.comment.IssueEditionRelation;
import com.globalsight.everest.costing.BigDecimalHelper;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobDataMigration;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.docproc.extractor.xliff.XliffAltUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Helper for "translation_unit_variant_[companyId]" storage.
 *
 * @author york.jin
 * @since 2012-03-22
 * @version 8.2.3
 */
public class SegmentTuvUtil extends SegmentTuTuvCacheManager implements
        TuvQueryConstants
{
    static private final Logger logger = Logger.getLogger(SegmentTuvUtil.class);

    private static final String SELECT_COLUMNS = "SELECT "
            + "tuv.id, tuv.order_num, tuv.locale_id, tuv.tu_id, tuv.is_indexed, "
            + "tuv.segment_clob, tuv.segment_string, tuv.word_count, tuv.exact_match_key, tuv.state, "
            + "tuv.merge_state, tuv.timestamp, tuv.last_modified, tuv.modify_user, tuv.creation_date, "
            + "tuv.creation_user, tuv.updated_by_project, tuv.sid, tuv.src_comment, tuv.repetition_of_id, "
            + "tuv.is_repeated FROM ";

    private static final String GET_TUV_BY_TUV_ID_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv " 
            + "WHERE tuv.id = ? ";

    private static final String GET_TUVS_BY_TU_ID_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv "
            + "WHERE tuv.state != 'OUT_OF_DATE' " 
            + "AND tuv.tu_id = ? ";

    private static final String GET_TUV_BY_TU_ID_LOCALE_ID_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv "
            + "WHERE tuv.state != 'OUT_OF_DATE' " 
            + "AND tuv.locale_id = ? "
            + "AND tuv.tu_id = ? ";

    private static final String GET_SOURCE_TUVS_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv, " 
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "source_page_leverage_group splg " 
            + "WHERE tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND tuv.locale_id = ? " 
            + "AND splg.sp_id = ? "
            + "ORDER BY tuv.order_num asc";

    // This SQL gets the same result as GET_TARGET_TUVS_SQL
    private static final String GET_EXPORT_TUVS_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv, "
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "source_page_leverage_group splg "
            + "WHERE tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND tuv.state != 'OUT_OF_DATE' "
            + "AND tuv.locale_id = ? "
            + "AND splg.sp_id = ? "
            + "ORDER BY tuv.order_num asc";

    // NOTE: "Active" target TUVs only, no 'OUT_OF_DATE' TUVs.
    private static final String GET_TARGET_TUVS_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv, " 
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "target_page_leverage_group tplg " 
            + "WHERE tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = tplg.lg_id "
            + "AND tuv.state != 'OUT_OF_DATE' " 
            + "AND tuv.locale_id = ? "
            + "AND tplg.tp_id = ? " 
            + "ORDER BY tuv.order_num asc ";

    // NOTE: Include 'OUT_OF_DATE' segments.
    private static final String GET_ALL_TARGET_TUVS_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv, " 
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "target_page_leverage_group tplg " 
            + "WHERE tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = tplg.lg_id "
            + "AND tuv.locale_id = ? "
            + "AND tplg.tp_id = ? " 
            + "ORDER BY tuv.order_num asc ";
    
    private static final String LOAD_XLIFF_ALT_BY_SPID_LOCALE_SQL = "SELECT alt.* FROM xliff_alt alt, "
            + TUV_TABLE_PLACEHOLDER
            + " tuv, "
            + TU_TABLE_PLACEHOLDER
            + " tu, "
            + "source_page_leverage_group splg "
            + "WHERE alt.tuv_id = tuv.id "
            + "AND tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND tuv.locale_id = ? "
            + "AND splg.sp_id = ? "
            + "ORDER BY alt.id, alt.tuv_id ";

    private static final String LOAD_XLIFF_ALT_BY_TPID_LOCALE_SQL = "SELECT alt.* FROM xliff_alt alt, "
            + TUV_TABLE_PLACEHOLDER
            + " tuv, "
            + TU_TABLE_PLACEHOLDER
            + " tu, "
            + "target_page_leverage_group tplg "
            + "WHERE alt.tuv_id = tuv.id "
            + "AND tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = tplg.lg_id "
            + "AND tuv.locale_id = ? "
            + "AND tplg.tp_id = ? "
            + "ORDER BY alt.id, alt.tuv_id ";

    private static final String SAVE_TUVS_SQL = "INSERT INTO "
            + TUV_TABLE_PLACEHOLDER
            + " ("
            + "id, order_num, locale_id, tu_id, is_indexed, "
            + "segment_clob, segment_string, word_count, exact_match_key, state, "
            + "merge_state, timestamp, last_modified, modify_user, creation_date, "
            + "creation_user, updated_by_project, sid, src_comment, repetition_of_id, is_repeated) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String GET_REP_TUVS_BY_TU_ID_LOCALE_ID_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER
            + " tuv, "
            + TU_TABLE_PLACEHOLDER
            + " tu "
            + "WHERE tuv.TU_ID = tu.ID "
            + "AND (tuv.is_repeated = 'Y' OR tuv.repetition_of_id > 0) "
            + "AND tuv.locale_id = ? "
            + "AND tu.id in (?) ";

    private static final String GET_TOTAL_TARGET_TUVS_NUMBER_SQL =
            " SELECT tplg.TP_ID, COUNT(tuv.ID) AS counter FROM "
            + TUV_TABLE_PLACEHOLDER + " tuv, " 
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "target_page_leverage_group tplg "
            + "WHERE tuv.TU_ID = tu.ID "
            + "AND tu.LEVERAGE_GROUP_ID = tplg.LG_ID "
            + "AND tuv.LOCALE_ID = ? "
            + "AND tuv.STATE NOT IN ('OUT_OF_DATE', 'DO_NOT_TRANSLATE') "
            + "AND tplg.TP_ID in (target_page_ids) "
            + "GROUP BY tplg.TP_ID ";

    // Keep consistent with "TuvImpl.isNotLocalized()" method + "APPROVED".
    private static final String GET_UNTRANSLATED_TARGET_TUVS_NUMBER_SQL =
            " SELECT tplg.TP_ID, COUNT(tuv.ID) AS counter FROM "
            + TUV_TABLE_PLACEHOLDER + " tuv, " 
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "target_page_leverage_group tplg "
            + "WHERE tuv.TU_ID = tu.ID "
            + "AND tu.LEVERAGE_GROUP_ID = tplg.LG_ID "
            + "AND tuv.LOCALE_ID = ? "
            + "AND tplg.TP_ID in (target_page_ids) "
            + "AND tuv.STATE NOT IN ('LOCALIZED', 'EXACT_MATCH_LOCALIZED', "
            + "    'LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED', 'COMPLETE', "
            + "    'ALIGNMENT_LOCALIZED', 'UNVERIFIED_EXACT_MATCH', "
            + "    'OUT_OF_DATE', 'APPROVED', 'DO_NOT_TRANSLATE') "
            + "GROUP BY tplg.TP_ID ";

    private static final String APPROVE_TUV_SQL = "UPDATE "
            + TUV_TABLE_PLACEHOLDER + " tuv, "
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "target_page_leverage_group tplg "
            + "SET tuv.STATE = 'APPROVED' "
            + "WHERE tuv.TU_ID = tu.ID "
            + "AND tu.LEVERAGE_GROUP_ID = tplg.LG_ID "
            + "AND tuv.LOCALE_ID = ? "
            + "AND tplg.TP_ID IN (target_page_ids) "
            + "AND tuv.STATE NOT IN ('LOCALIZED', 'EXACT_MATCH_LOCALIZED', "
            + "    'LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED', 'COMPLETE', "
            + "    'ALIGNMENT_LOCALIZED', 'UNVERIFIED_EXACT_MATCH', "
            + "    'OUT_OF_DATE', 'APPROVED', 'DO_NOT_TRANSLATE') ";

    /**
     * Save TUVs into DB.
     *
     * @param conn
     * @param p_tuvs
     *            -- TuvImpl objects
     * @param companyId
     * @throws Exception
     */
    public static void saveTuvs(Connection conn, Collection<Tuv> p_tuvs,
            long companyId) throws Exception
    {
        PreparedStatement ps = null;

        try
        {
            // Update the TUV sequence first despite below succeeding or failure.
            SegmentTuTuvIndexUtil.updateTuvSequence(conn);

            String sql = SAVE_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvWorkingTableName(companyId));
            ps = conn.prepareStatement(sql);

            Set<Long> tuIds = new HashSet<Long>();
            for (Iterator<Tuv> it = p_tuvs.iterator(); it.hasNext();)
            {
                TuvImpl tuv = (TuvImpl) it.next();
                tuIds.add(tuv.getTuId());

                ps.setLong(1, tuv.getId());
                ps.setLong(2, tuv.getOrder());
                ps.setLong(3, tuv.getLocaleId());
                ps.setLong(4, tuv.getTu(companyId).getId());
                ps.setString(5, tuv.getIsIndexed() ? "Y" : "N");

                ps.setString(6, tuv.getSegmentClob());
                ps.setString(7, tuv.getSegmentString());
                ps.setInt(8, tuv.getWordCount());
                ps.setLong(9, tuv.getExactMatchKey());
                ps.setString(10, tuv.getState().getName());

                ps.setString(11, tuv.getMergeState());
                ps.setTimestamp(12, new java.sql.Timestamp(tuv.getTimestamp()
                        .getTime()));
                ps.setTimestamp(13, new java.sql.Timestamp(tuv
                        .getLastModified().getTime()));
                ps.setString(14, tuv.getLastModifiedUser());
                ps.setTimestamp(15, new java.sql.Timestamp(tuv.getCreatedDate()
                        .getTime()));

                ps.setString(16, tuv.getCreatedUser());
                ps.setString(17, tuv.getUpdatedProject());
                ps.setString(18, tuv.getSid());
                ps.setString(19, tuv.getSrcComment());
                ps.setLong(20, tuv.getRepetitionOfId());
                ps.setString(21, tuv.isRepeated() ? "Y" : "N");

                ps.addBatch();
            }
            ps.executeBatch();

            // Cache the TUVs for large pages when create jobs.
            if (tuIds.size() > 800)
            {
                for (Iterator<Tuv> it = p_tuvs.iterator(); it.hasNext();)
                {
                    TuvImpl tuv = (TuvImpl) it.next();
                    setTuvIntoCache(tuv);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when save TUVs " + e.getMessage(), e);
            for (Iterator<Tuv> it = p_tuvs.iterator(); it.hasNext();)
            {
                TuvImpl tuv = (TuvImpl) it.next();
                removeTuvFromCache(tuv.getId());
            }
            throw e;
        }
        finally
        {
            DbUtil.silentClose(ps);
        }
    }

    /**
     * Query TUV by TuvId.
     *
     * @param p_tuvId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvById(long p_tuvId, long companyId)
            throws Exception
    {
        TuvImpl tuv = getTuvFromCache(p_tuvId);
        if (tuv != null)
        {
            return tuv;
        }

        Connection conn = DbUtil.getConnection();
        try
        {
            tuv = getTuvById(conn, p_tuvId, companyId);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        return tuv;
    }

    /**
     * Query TUV by TuvId.
     *
     * @param connection
     * @param tuvId
     * @return TuvImpl
     * @throws SQLException
     */
    public static TuvImpl getTuvById(Connection connection, long p_tuvId,
            long companyId) throws Exception
    {
        TuvImpl tuv = getTuvFromCache(p_tuvId);
        if (tuv != null)
        {
            return tuv;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = GET_TUV_BY_TUV_ID_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvWorkingTableName(companyId));

            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_tuvId);
            rs = ps.executeQuery();

            List<TuvImpl> tuvs = convertResultSetToTuv(rs, true);
            if (tuvs != null && tuvs.size() > 0)
            {
                tuv = tuvs.get(0);
            }
            // Maybe current job has been archived, so...
            else
            {
                // Check if archive tables have been created, if not, create it.
                JobDataMigration.checkArchiveTables(connection, companyId);

                sql = GET_TUV_BY_TUV_ID_SQL.replace(TUV_TABLE_PLACEHOLDER,
                        getTuvArchiveTableName(companyId));
                ps = connection.prepareStatement(sql);
                ps.setLong(1, p_tuvId);
                rs = ps.executeQuery();

                tuvs = convertResultSetToTuv(rs, true);
                if (tuvs != null && tuvs.size() > 0)
                {
                    tuv = tuvs.get(0);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get TUV by tuvID " + p_tuvId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return tuv;
    }

    /**
     * Query TUVs by specified tuIds and localeId.
     *
     * @param p_tuIds
     * @param p_localeId
     * @return TuvImpl list
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuIdsLocaleId(long[] p_tuIds,
            long p_localeId, long companyId) throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Connection conn = DbUtil.getConnection();
        try
        {
            for (int i = 0; i < p_tuIds.length; i++)
            {
                result.add(getTuvByTuIdLocaleId(conn, p_tuIds[i], p_localeId,
                        companyId));
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Query TUV by tuId and localeId.
     *
     * @param p_tuId
     * @param p_localeId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvByTuIdLocaleId(long p_tuId, long p_localeId,
            long companyId) throws Exception
    {
        TuvImpl tuv = null;

        Connection conn = DbUtil.getConnection();
        try
        {
            tuv = getTuvByTuIdLocaleId(conn, p_tuId, p_localeId, companyId);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        return tuv;
    }

    /**
     * Query TUV by tuId and localeId.
     *
     * @param p_connection
     * @param p_tuId
     * @param p_localeId
     * @param companyId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvByTuIdLocaleId(Connection p_connection,
            long p_tuId, long p_localeId, long companyId) throws Exception
    {
        TuvImpl tuv = null;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = GET_TUV_BY_TU_ID_LOCALE_ID_SQL.replace(
                    TUV_TABLE_PLACEHOLDER, getTuvWorkingTableName(companyId));

            ps = p_connection.prepareStatement(sql);
            ps.setLong(1, p_localeId);
            ps.setLong(2, p_tuId);
            rs = ps.executeQuery();

            List<TuvImpl> result = convertResultSetToTuv(rs, true);
            if (result != null && result.size() > 0)
            {
                tuv = result.get(0);
            }
            // Maybe current job has been archived, so...
            else
            {
                sql = GET_TUV_BY_TU_ID_LOCALE_ID_SQL.replace(
                        TUV_TABLE_PLACEHOLDER,
                        getTuvArchiveTableName(companyId));

                ps = p_connection.prepareStatement(sql);
                ps.setLong(1, p_localeId);
                ps.setLong(2, p_tuId);
                rs = ps.executeQuery();

                result = convertResultSetToTuv(rs, true);
                if (result != null && result.size() > 0)
                {
                    tuv = result.get(0);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get TUV by TuId " + p_tuId
                    + " and localeId " + p_localeId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return tuv;
    }

    /**
     * Query translation unit variant by TuId.
     *
     * @param tuId
     * @return TUV list for specified TU_ID.
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuId(long p_tuId, long companyId)
            throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Connection conn = DbUtil.getConnection();
        try
        {
            result = getTuvsByTuId(conn, p_tuId, companyId);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Query translation unit variant by TuId.
     *
     * @param connection
     * @param tuId
     * @return TUV list for specified TU_ID.
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuId(Connection connection,
            long p_tuId, long companyId) throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = GET_TUVS_BY_TU_ID_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvWorkingTableName(companyId));

            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_tuId);
            rs = ps.executeQuery();

            result = convertResultSetToTuv(rs, true);
            if (result == null || result.size() == 0)
            {
                sql = GET_TUVS_BY_TU_ID_SQL.replace(TUV_TABLE_PLACEHOLDER,
                        getTuvArchiveTableName(companyId));
                ps = connection.prepareStatement(sql);
                ps.setLong(1, p_tuId);
                rs = ps.executeQuery();

                result = convertResultSetToTuv(rs, true);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get TUVs by tuID " + p_tuId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return result;
    }

    /**
     * Query source TUVs in current source page. Note:In this method, need load
     * extra data for TUVs such as xliff_alt etc.
     *
     * @param p_sourcePage
     *            -- SourcePage
     * @return ArrayList<Tuv>
     * @throws Exception
     */
    public static ArrayList<Tuv> getSourceTuvs(SourcePage p_sourcePage)
            throws Exception
    {
        boolean loadExtraInfo = true;
        return getSourceTuvs(p_sourcePage, loadExtraInfo);
    }

    /**
     * Query source TUVs in current source page.
     *
     * @param p_sourcePage
     *            -- SourcePage
     * @param p_loadXlfAlts
     *            -- if this is true,maybe reduce performance.
     * @return ArrayList<Tuv>
     * @throws Exception
     */
    public static ArrayList<Tuv> getSourceTuvs(SourcePage p_sourcePage,
            boolean p_needLoadXlfAlts) throws Exception
    {
        ArrayList<Tuv> result = new ArrayList<Tuv>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvTableNameJobDataIn(p_sourcePage.getId());
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuTableNameJobDataIn(p_sourcePage.getId());
            String sql = GET_SOURCE_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_sourcePage.getLocaleId());
            ps.setLong(2, p_sourcePage.getId());
            rs = ps.executeQuery();

            result.addAll(convertResultSetToTuv(rs, false));

            // Load xliff_alt data in page level to improve performance.
            if (p_needLoadXlfAlts
                    && XliffAltUtil.isGenerateXliffAlt(p_sourcePage))
            {
                loadXliffAlts(result, p_sourcePage.getLocaleId(),
                        p_sourcePage.getId(), tuTableName, tuvTableName);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getSourceTuvs() for source page "
                    + p_sourcePage.getId(), e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Query Tuvs for specified source page and loale. The locale can be source
     * locale or any target locale.
     *
     * @param p_companyId
     * @param p_localeId
     *            -- targetLocaleId
     * @param p_sourcePageId
     * @return TuvImpl list
     * @throws Exception
     */
    public static List<Tuv> getExportTuvs(long p_companyId, long p_localeId,
            long p_sourcePageId) throws Exception
    {
        List<Tuv> result = new ArrayList<Tuv>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            conn = DbUtil.getConnection();
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvTableNameJobDataIn(p_sourcePageId);
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuTableNameJobDataIn(p_sourcePageId);
            String sql = GET_EXPORT_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_localeId);
            ps.setLong(2, p_sourcePageId);
            rs = ps.executeQuery();

            result.addAll(convertResultSetToTuv(rs, false));

            SourcePage sp = ServerProxy.getPageManager().getSourcePage(
                    p_sourcePageId);
            if (XliffAltUtil.isGenerateXliffAlt(sp))
            {
                // Load xliff_alt data in page level to improve performance.
                loadXliffAlts(result, p_localeId, p_sourcePageId, tuTableName,
                        tuvTableName);                
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getExportTuvs for sourcePageId "
                    + p_sourcePageId + " and localeId " + p_localeId, e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Query target TUVs in current target page.
     *
     * @param p_targetPage
     * @return Tuv list
     * @throws Exception
     */
    public static List<TuvImpl> getTargetTuvs(TargetPage p_targetPage)
            throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            long sourcePageId = p_targetPage.getSourcePage().getId();
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvTableNameJobDataIn(sourcePageId);
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuTableNameJobDataIn(sourcePageId);
            String sql = GET_TARGET_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_targetPage.getLocaleId());
            ps.setLong(2, p_targetPage.getId());
            rs = ps.executeQuery();

            result = convertResultSetToTuv(rs, false);

            // Load xliff_alt data in page level to improve performance.
            if (XliffAltUtil.isGenerateXliffAlt(p_targetPage.getSourcePage()))
            {
                loadXliffAlts2(new ArrayList<Tuv>(result),
                        p_targetPage.getLocaleId(), p_targetPage.getId(),
                        tuTableName, tuvTableName);                
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getTargetTuvs for target page "
                    + p_targetPage.getId(), e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Query all target TUVs in current target page (including 'OUT_OF_DATE').
     *
     * @param p_targetPage
     * @return Tuv list
     * @throws Exception
     */
    public static List<Tuv> getAllTargetTuvs(TargetPage p_targetPage)
            throws Exception
    {
        List<Tuv> result = new ArrayList<Tuv>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            long sourcePageId = p_targetPage.getSourcePage().getId();
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvTableNameJobDataIn(sourcePageId);
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuTableNameJobDataIn(sourcePageId);
            String sql = GET_ALL_TARGET_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_targetPage.getLocaleId());
            ps.setLong(2, p_targetPage.getId());
            rs = ps.executeQuery();

            result.addAll(convertResultSetToTuv(rs, false));

            // Load xliff_alt data in page level to improve performance.
            if (XliffAltUtil.isGenerateXliffAlt(p_targetPage.getSourcePage()))
            {
                loadXliffAlts2(result, p_targetPage.getLocaleId(),
                        p_targetPage.getId(), tuTableName, tuvTableName);                
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getTargetTuvs for target page "
                    + p_targetPage.getId(), e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Get all repeated and repetitions target TUVs for specified target page.
     * 
     * @param p_targetPage
     * @return List<TuvImpl>
     * @throws Exception
     */
    public static List<TuvImpl> getRepTuvsByTargetPage(TargetPage p_targetPage)
            throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        List<TuvImpl> allTrgTuvs = getTargetTuvs(p_targetPage);
        for (TuvImpl tuv : allTrgTuvs)
        {
            if (tuv.isRepeated() || tuv.getRepetitionOfId() > 0)
            {
                result.add(tuv);
            }
        }

        return result;
    }

    /**
     * Get all repeated and repetitions target TUVs for specified TUs and locale.
     * 
     * @param p_tuIds in List.
     * @param p_targetLocaleId
     * @param p_companyId
     * @param p_isJobMigrated
     * @return List<TuImpl>
     * @throws Exception
     */
    public static List<TuvImpl> getRepTuvsByTuIdsAndLocaleId(
            List<Long> p_tuIds, long p_targetLocaleId, long p_companyId,
            boolean p_isJobMigrated) throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvTableNameJobDataIn(p_companyId, p_isJobMigrated);
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuTableNameJobDataIn(p_companyId, p_isJobMigrated);
            String sql = GET_REP_TUVS_BY_TU_ID_LOCALE_ID_SQL.replace(
                    TUV_TABLE_PLACEHOLDER, tuvTableName).replace(
                    TU_TABLE_PLACEHOLDER, tuTableName);

            StringBuilder tuIds = new StringBuilder();
            for (Long tuId : p_tuIds)
            {
                if (tuIds.length() == 0)
                    tuIds.append(tuId);
                else
                    tuIds.append(",").append(tuId);
            }
            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_targetLocaleId);
            ps.setString(2, tuIds.toString());
            rs = ps.executeQuery();

            result = convertResultSetToTuv(rs, false);
        }
        catch (Exception e)
        {
            logger.error("Error when getRepTuvsByTuIdsAndLocaleId().", e);
            throw e;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Load XliffAlt for TUVs of current source page.
     * 
     * @param p_tuvs
     * @param p_localeId
     * @param p_sourcePageId
     * @param p_tuTableName
     * @param p_tuvTableName
     */
    private static void loadXliffAlts(List<Tuv> p_tuvs, long p_localeId,
            long p_sourcePageId, String p_tuTableName, String p_tuvTableName)
    {
        try
        {
            String sql = LOAD_XLIFF_ALT_BY_SPID_LOCALE_SQL.replace(
                    TUV_TABLE_PLACEHOLDER, p_tuvTableName).replace(
                    TU_TABLE_PLACEHOLDER, p_tuTableName);

            List<XliffAlt> xlfAlts = HibernateUtil.searchWithSql(
                    XliffAlt.class, sql, p_localeId, p_sourcePageId);

            Map<Long, Set<XliffAlt>> xlfAltMap = getXliffAltMap(xlfAlts);

            if (xlfAltMap != null && xlfAltMap.size() > 0)
            {
                putXliffAltIntoTuv(xlfAltMap, p_tuvs);
            }

            for (Tuv tuv : p_tuvs)
            {
                recordWhichTuvExtraDataAlreadyLoaded(tuv.getIdAsLong(),
                        XLIFF_ALT);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when loadXliffAlts() for sourcePageId "
                    + p_sourcePageId + " localeId " + p_localeId, e);
        }
    }

    /**
     * Invoked by getTargetTuvs(...) method.
     *
     * @param companyId
     * @param p_tuvs
     * @param p_localeId
     * @param p_targetPageId
     */
    private static void loadXliffAlts2(List<Tuv> p_tuvs, long p_localeId,
            long p_targetPageId, String p_tuTableName, String p_tuvTableName)
    {
        try
        {
            String sql = LOAD_XLIFF_ALT_BY_TPID_LOCALE_SQL.replace(
                    TUV_TABLE_PLACEHOLDER, p_tuvTableName).replace(
                    TU_TABLE_PLACEHOLDER, p_tuTableName);

            List<XliffAlt> xlfAlts = HibernateUtil.searchWithSql(
                    XliffAlt.class, sql, p_localeId, p_targetPageId);

            Map<Long, Set<XliffAlt>> xlfAltMap = getXliffAltMap(xlfAlts);

            if (xlfAltMap != null && xlfAltMap.size() > 0)
            {
                putXliffAltIntoTuv(xlfAltMap, p_tuvs);
            }

            for (Tuv tuv : p_tuvs)
            {
                recordWhichTuvExtraDataAlreadyLoaded(tuv.getIdAsLong(),
                        XLIFF_ALT);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when loadXliffAlts2() for pageId "
                    + p_targetPageId + " localeId " + p_localeId, e);
        }
    }

    /**
     * Convert ResultSet into XliffAlt objects, and put them into map.
     *
     * @param alt
     *            -- List<XliffAlt>
     * @return -- Map<Long, Set<XliffAlt>> (tuvId : Set<XliffAlt>)
     */
    private static Map<Long, Set<XliffAlt>> getXliffAltMap(List<XliffAlt> alts)
    {
        // tuvId : Set<XliffAlt>
        Map<Long, Set<XliffAlt>> xlfAltMap = new HashMap<Long, Set<XliffAlt>>();

        for (XliffAlt alt : alts)
        {
            Set<XliffAlt> myAlts = xlfAltMap.get(alt.getTuvId());
            if (myAlts == null)
            {
                myAlts = new HashSet<XliffAlt>();
                myAlts.add(alt);
                xlfAltMap.put(alt.getTuvId(), myAlts);
            }
            else
            {
                myAlts.add(alt);
                xlfAltMap.put(alt.getTuvId(), myAlts);
            }
        }

        return xlfAltMap;
    }

    /**
     * Put XliffAlts into its own TUV.
     *
     * @param p_xlfAltMap
     * @param p_tuvs
     */
    private static void putXliffAltIntoTuv(
            Map<Long, Set<XliffAlt>> p_xlfAltMap, List<Tuv> p_tuvs)
    {
        if (p_xlfAltMap != null && p_xlfAltMap.size() > 0)
        {
            for (Tuv tuv : p_tuvs)
            {
                Set<XliffAlt> myAlts = p_xlfAltMap.get(tuv.getIdAsLong());
                if (myAlts != null && myAlts.size() > 0)
                {
                    tuv.setXliffAlt(myAlts);
                }
            }
        }
    }

    public static void updateTuv(TuvImpl p_tuv, long companyId)
            throws Exception
    {
        List<TuvImpl> tuvs = new ArrayList<TuvImpl>();
        tuvs.add(p_tuv);
        updateTuvs(tuvs, companyId);
    }

    public static void updateTuv(Connection p_connection, TuvImpl p_tuv,
            long companyId) throws Exception
    {
        List<TuvImpl> tuvs = new ArrayList<TuvImpl>();
        tuvs.add(p_tuv);
        updateTuvs(p_connection, tuvs, companyId);
    }

    public static void updateTuvs(List<TuvImpl> p_tuvs, long companyId)
            throws Exception
    {
        Connection conn = DbUtil.getConnection();
        boolean isAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try
        {
            updateTuvs(conn, p_tuvs, companyId);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            conn.setAutoCommit(isAutoCommit);
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * Update TuvImpl
     *
     * @param p_tuv
     *            TuvImpl
     * @throws Exception
     */
    public static void updateTuvs(Connection p_connection,
            List<TuvImpl> p_tuvs, long companyId) throws Exception
    {
        if (p_tuvs == null || p_tuvs.size() == 0)
        {
            return;
        }

        PreparedStatement tuvUpdateStmt = null;
        try
        {
            StringBuilder sql = new StringBuilder();

            sql.append("update ").append(getTuvWorkingTableName(companyId))
                    .append(" set ");
            sql.append("order_num = ?, ");// 1
            sql.append("locale_id = ?, ");// 2
            sql.append("tu_id = ?, ");// 3
            sql.append("is_indexed = ?, ");// 4
            sql.append("segment_clob = ?, ");// 5
            sql.append("segment_string = ?,");// 6
            sql.append("word_count = ?,");// 7
            sql.append("exact_match_key = ?,");// 8
            sql.append("state = ?, ");// 9
            sql.append("merge_state = ?, ");// 10
            sql.append("timestamp = ?, ");// 11
            sql.append("last_modified = ?, ");// 12
            sql.append("modify_user = ?, ");// 13
            sql.append("creation_date = ?, ");// 14
            sql.append("creation_user = ?, ");// 15
            sql.append("updated_by_project = ?, ");// 16
            sql.append("sid = ?, ");// 17
            sql.append("src_comment = ?, "); // 18
            sql.append("repetition_of_id = ?, "); // 19
            sql.append("is_repeated = ? where id = ?");// 20,21

            tuvUpdateStmt = p_connection.prepareStatement(sql.toString());

            // addBatch counters
            int batchUpdate = 0;

            for (TuvImpl tuv : p_tuvs)
            {
                tuv.setExactMatchKey(GlobalSightCrc.calculate(tuv
                        .getExactMatchFormat()));
                tuv.setLastModified(new Date());
                // We should not do this in a general API, this should be set
                // outside this API.
                // tuv.setState(TuvState.LOCALIZED);

                tuvUpdateStmt.setLong(1, tuv.getOrder());
                tuvUpdateStmt.setLong(2, tuv.getLocaleId());
                tuvUpdateStmt.setLong(3, tuv.getTuId());
                tuvUpdateStmt.setString(4, tuv.getIsIndexed() ? "Y" : "N");
                tuvUpdateStmt.setString(5, tuv.getSegmentClob());
                tuvUpdateStmt.setString(6, tuv.getSegmentString());
                tuvUpdateStmt.setInt(7, tuv.getWordCount());
                tuvUpdateStmt.setLong(8, tuv.getExactMatchKey());
                tuvUpdateStmt.setString(9, tuv.getState().getName());
                tuvUpdateStmt.setString(10, tuv.getMergeState());
                tuvUpdateStmt.setTimestamp(11, tuv.getTimestamp());
                tuvUpdateStmt.setTimestamp(12, new Timestamp(tuv
                        .getLastModified().getTime()));
                tuvUpdateStmt.setString(13, tuv.getLastModifiedUser());
                tuvUpdateStmt.setTimestamp(14, new Timestamp(tuv
                        .getCreatedDate().getTime()));
                tuvUpdateStmt.setString(15, tuv.getCreatedUser());
                tuvUpdateStmt.setString(16, tuv.getUpdatedProject());
                tuvUpdateStmt.setString(17, tuv.getSid());
                tuvUpdateStmt.setString(18, tuv.getSrcComment());
                tuvUpdateStmt.setLong(19, tuv.getRepetitionOfId());
                tuvUpdateStmt.setString(20, tuv.isRepeated() ? "Y" : "N");
                tuvUpdateStmt.setLong(21, tuv.getId());

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
     * Convert the ResultSet objects into TuvImpl objects.
     *
     * @param rs
     *            -- ResultSet which concludes TuvImpl data.
     * @param loadExtraInfo
     *            -- Note that if true, there may be performance problem.
     * @return
     * @throws Exception
     */
    private static ArrayList<TuvImpl> convertResultSetToTuv(ResultSet rs,
            boolean loadExtraInfo) throws Exception
    {
        ArrayList<TuvImpl> result = new ArrayList<TuvImpl>();
        if (rs == null)
        {
            return result;
        }

        while (rs.next())
        {
            TuvImpl tuv = new TuvImpl();

            Long tuvId = new Long(rs.getLong(1));
            if (getTuvFromCache(tuvId) != null)
            {
                result.add(getTuvFromCache(tuvId));
                continue;
            }

            tuv.setId(rs.getLong(1));
            tuv.setOrder(rs.getLong(2));
            tuv.setLocaleId(rs.getLong(3));
            tuv.setTuId(rs.getLong(4));
            tuv.setIsIndexed("Y".equalsIgnoreCase(rs.getString(5)) ? true
                    : false);

            tuv.setSegmentClob(rs.getString(6));
            tuv.setSegmentString(rs.getString(7));
            tuv.setWordCount(rs.getInt(8));
            tuv.setExactMatchKey(rs.getLong(9));
            tuv.setState(rs.getString(10));

            tuv.setMergeState(rs.getString(11));
            tuv.setTimestamp(rs.getTimestamp(12));
            tuv.setLastModified(new java.util.Date(rs.getTimestamp(13).getTime()));
            tuv.setLastModifiedUser(rs.getString(14));
            tuv.setCreatedDate(new java.util.Date(rs.getTimestamp(15).getTime()));

            tuv.setCreatedUser(rs.getString(16));
            tuv.setUpdatedProject(rs.getString(17));
            tuv.setSid(rs.getString(18));
            tuv.setSrcComment(rs.getString(19));
            tuv.setRepetitionOfId(rs.getLong(20));
            tuv.setRepeated("Y".equalsIgnoreCase(rs.getString(21)) ? true
                    : false);

            if (loadExtraInfo)
            {
                // Load XliffAlt data if current TUV has.
                // loadXliffAlt(tuv);
                // Load IssueEditionRelation for GS edition
                // loadIssueEditionRelation(tuv);
            }

            result.add(tuv);
            setTuvIntoCache(tuv);
        }

        return result;
    }

    /**
     * Load Xliff Alt-trans data for current TuvImpl if it is from XLF.
     *
     * @param tuv
     *            -- TuvImpl object
     */
    @SuppressWarnings("unchecked")
    public static void loadXliffAlt(TuvImpl tuv)
    {
        boolean isXliffAltLoaded = isTuvExtraDataLoaded(tuv.getIdAsLong(),
                XLIFF_ALT);
        if (!isXliffAltLoaded)
        {
            Set<XliffAlt> xliff_alt = new HashSet<XliffAlt>();
            String hql = "from XliffAlt xa where xa.tuvId = " + tuv.getId();
            List<XliffAlt> xliffAlts = (List<XliffAlt>) HibernateUtil
                    .search(hql);
            if (xliffAlts != null && xliffAlts.size() > 0)
            {
                for (XliffAlt alt : xliffAlts)
                {
                    alt.setTuv(tuv);
                }
                xliff_alt.addAll(xliffAlts);
                tuv.setXliffAlt(xliff_alt);
            }

            recordWhichTuvExtraDataAlreadyLoaded(tuv.getIdAsLong(), XLIFF_ALT);
        }
    }

    /**
     * Load IssueEditionRelation data for current TuvImpl if it is GS edition
     * TUV.
     *
     * @param tuv
     *            -- TuvImpl object
     */
    @SuppressWarnings("unchecked")
    public static void loadIssueEditionRelation(TuvImpl tuv)
    {
        boolean isIERLoaded = isTuvExtraDataLoaded(tuv.getIdAsLong(),
                ISSUE_EDITION_RELATION);
        if (!isIERLoaded)
        {
            Set<IssueEditionRelation> issueERs = new HashSet<IssueEditionRelation>();
            String hql = "from IssueEditionRelation ier where ier.tuvId = "
                    + tuv.getId();
            List<IssueEditionRelation> relations = (List<IssueEditionRelation>) HibernateUtil
                    .search(hql);
            if (relations != null && relations.size() > 0)
            {
                for (IssueEditionRelation relation : relations)
                {
                    relation.setTuv(tuv);
                }

                issueERs.addAll(relations);
                tuv.setIssueEditionRelation(issueERs);
            }

            recordWhichTuvExtraDataAlreadyLoaded(tuv.getIdAsLong(),
                    ISSUE_EDITION_RELATION);
        }
    }

    /**
     * Get the translated percentage for specified target page.
     * 
     * @param trgPageId
     *            target page Id
     * @return translated percentage
     * @throws Exception
     */
    public static String getTranslatedPercentage(long trgPageId)
            throws EnvoyServletException
    {
        List<Long> trgPageIds = new ArrayList<Long>();
        trgPageIds.add(trgPageId);
        return getTranslatedPercentage(trgPageIds);
    }

    /**
     * Get the translated percentage in task level.
     * 
     * @param targetPageIds
     *            -- List<Long> target page IDs from the same one task.
     * @return String
     * 
     */
    public static String getTranslatedPercentage(List<Long> targetPageIds)
    {
        if (targetPageIds == null || targetPageIds.size() == 0)
        {
            return null;
        }

        Map<Long, Integer> totalCountsMap = getTotalTargetTuvsNumber(targetPageIds);
        Map<Long, Integer> untranslatedCountsMap = getUntranslatedTargetTuvsNumber(targetPageIds);

        int totalCounts = 0;
        int untranslatedCounts = 0;
        try
        {
            for (Long trgPageId : targetPageIds)
            {
                if (totalCountsMap.get(trgPageId) != null)
                {
                    totalCounts += totalCountsMap.get(trgPageId);
                }

                if (untranslatedCountsMap.get(trgPageId) != null)
                {
                    untranslatedCounts += untranslatedCountsMap.get(trgPageId);
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        int translatedPercentage = 100;
        if (totalCounts != 0)
        {
            int translatedCounts = totalCounts - untranslatedCounts;
            translatedPercentage = Math.round(BigDecimalHelper.divide(
                    translatedCounts * 100, totalCounts));
            if(translatedPercentage == 100 && totalCounts > translatedCounts){
            	translatedPercentage = 99;
            }
        }

        return String.valueOf(translatedPercentage);
    }

    /**
     * Get the target pages which include un-translated segments.
     * 
     * @param p_tps
     *            Target page list
     */
    public static List<TargetPage> filterUnTranslatedTargetPages(
            List<TargetPage> p_tps)
    {
        List<TargetPage> result = new ArrayList<TargetPage>();

        List<Long> tpIds = new ArrayList<Long>();
        for (TargetPage tp : p_tps)
        {
            tpIds.add(tp.getIdAsLong());
        }

        Map<Long, Integer> untranslatedCountsMap = getUntranslatedTargetTuvsNumber(tpIds);

        for (TargetPage tp : p_tps)
        {
            Integer untranslatedCounter = untranslatedCountsMap.get(tp
                    .getIdAsLong());
            if (untranslatedCounter != null && untranslatedCounter > 0)
            {
                result.add(tp);
            }
        }

        return result;
    }

    /**
     * Get every target page's total TUV number in a map for specified target
     * page IDs.
     * 
     * @param targetPageIds
     *            -- these target pages should be from same task and have same
     *            target locale.
     * @return -- Map<Long, Long> : Map<targetPageId, SegmentNumber>
     */
    private static Map<Long, Integer> getTotalTargetTuvsNumber(
            List<Long> targetPageIds)
    {
        Map<Long, Integer> result = new HashMap<Long, Integer>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            conn = DbUtil.getConnection();
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(
                    targetPageIds.get(0));
            long spId = tp.getSourcePage().getId();
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvTableNameJobDataIn(spId);
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuTableNameJobDataIn(spId);
            String sql = GET_TOTAL_TARGET_TUVS_NUMBER_SQL
                    .replace(TUV_TABLE_PLACEHOLDER, tuvTableName)
                    .replace(TU_TABLE_PLACEHOLDER, tuTableName)
                    .replace("target_page_ids",
                            formStringFromList(targetPageIds));

            ps = conn.prepareStatement(sql);
            ps.setLong(1, tp.getLocaleId());
            rs = ps.executeQuery();

            while (rs != null && rs.next())
            {
                long tpId = rs.getLong(1);
                int counter = rs.getInt(2);
                result.put(tpId, counter);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getTotalTargetTuvsNumber(..).", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Get every target page's untranslated TUV number in a map for specified
     * target page IDs.
     * 
     * @param targetPageIds
     *            -- these target pages should be from same task and have same
     *            target locale.
     * @return -- Map<Long, Long> : Map<targetPageId, SegmentNumber>
     * 
     */
    private static Map<Long, Integer> getUntranslatedTargetTuvsNumber(
            List<Long> targetPageIds)
    {
        Map<Long, Integer> result = new HashMap<Long, Integer>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            conn = DbUtil.getConnection();
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(
                    targetPageIds.get(0));
            long spId = tp.getSourcePage().getId();
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvTableNameJobDataIn(spId);
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuTableNameJobDataIn(spId);
            String sql = GET_UNTRANSLATED_TARGET_TUVS_NUMBER_SQL
                    .replace(TUV_TABLE_PLACEHOLDER, tuvTableName)
                    .replace(TU_TABLE_PLACEHOLDER, tuTableName)
                    .replace("target_page_ids",
                            formStringFromList(targetPageIds));

            ps = conn.prepareStatement(sql);
            ps.setLong(1, tp.getLocaleId());
            rs = ps.executeQuery();

            while (rs != null && rs.next())
            {
                long tpId = rs.getLong(1);
                int counter = rs.getInt(2);
                result.put(tpId, counter);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getUntranslatedTargetTuvsNumber(..).", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return result;
    }

    /**
     * Return a string like "1,2,3,4".
     * @param pageIds
     * @return
     */
    private static String formStringFromList(List<Long> pageIds)
    {
        if (pageIds == null || pageIds.size() == 0)
            return null;

        StringBuilder tpIds = new StringBuilder();
        for (Long tpId : pageIds)
        {
            tpIds.append(tpId).append(",");
        }

        return tpIds.toString().substring(0, tpIds.length() - 1);
    }

    public static void approveTuvByTargetPageIds(List<Long> targetPageIds)
    {
        approveTuvByTargetPageIds(formStringFromList(targetPageIds));
    }

    public static void approveTuvByTargetPageIds(String targetPageIds)
    {
        if (StringUtils.isBlank(targetPageIds))
            return;

        Connection conn = null;
        PreparedStatement ps = null;

        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            String[] trgPageIds = targetPageIds.split(",");
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(
                    Long.parseLong(trgPageIds[0]));
            long spId = tp.getSourcePage().getId();
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvTableNameJobDataIn(spId);
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuTableNameJobDataIn(spId);
            String sql = APPROVE_TUV_SQL
                    .replace(TUV_TABLE_PLACEHOLDER, tuvTableName)
                    .replace(TU_TABLE_PLACEHOLDER, tuTableName)
                    .replace("target_page_ids", targetPageIds);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, tp.getLocaleId());
            ps.executeUpdate();
            conn.commit();
        }
        catch (Exception e)
        {
            logger.error("Error when approveTuvByTargetPageIds(..).", e);
        }
        finally
        {
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }
    }
}
