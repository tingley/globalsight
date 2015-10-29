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

import java.math.BigInteger;
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

import org.apache.log4j.Logger;

import com.globalsight.everest.costing.BigDecimalHelper;
import com.globalsight.everest.edit.online.SegmentFilter;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TuTuvAttributeImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.SegmentTmExactMatchFormatHandler;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.docproc.extractor.xliff.XliffAltUtil;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.Fingerprint;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

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

    private static final String APPROVE_TUV_SQL = "UPDATE "
            + TUV_TABLE_PLACEHOLDER + " tuv "
            + "SET tuv.STATE = 'APPROVED' "
            + "WHERE tuv.ID IN (untranslated_target_tuv_ids) ";
    
    private static final String COUNT_TRANSLATE_TUV_ALL = "SELECT COUNT(DISTINCT TUV.ID) FROM "
            + TUV_TABLE_PLACEHOLDER
            + " tuv, "
            + TU_TABLE_PLACEHOLDER
            + " tu, target_page_leverage_group tplg "
            + " WHERE tuv.tu_id = tu.id"
            + " AND tu.leverage_group_id = tplg.lg_id"
            + " AND tuv.state != 'OUT_OF_DATE'"
            + " AND tuv.STATE != 'DO_NOT_TRANSLATE'"
            + " AND tuv.locale_id = ?"
            + " AND tplg.tp_id = ?";
    
    private static final String TRANSLATE_TUV_TRANSLATED = "SELECT DISTINCT TUV.ID FROM "
            + TUV_TABLE_PLACEHOLDER
            + " tuv, "
            + TU_TABLE_PLACEHOLDER
            + " tu, target_page_leverage_group tplg "
            + " WHERE tuv.tu_id = tu.id"
            + " AND tu.leverage_group_id = tplg.lg_id"
            + " AND (tuv.state = 'LOCALIZED' OR tuv.state = 'APPROVED' OR tuv.state = 'EXACT_MATCH_LOCALIZED')"
            + " AND tuv.locale_id = :lid"
            + " AND tplg.tp_id = :tid";  
 
    private static final String GET_SOURCE_PAGE_ID = "SELECT SOURCE_PAGE_ID FROM TARGET_PAGE WHERE ID = ?";
    private static final String GET_LOCALE_ID = "SELECT TARGET_LOCALE_ID FROM WORKFLOW w, TARGET_PAGE tp "
            + "WHERE tp.WORKFLOW_IFLOW_INSTANCE_ID = w.IFLOW_INSTANCE_ID AND tp.ID = ?";
    
    private static final String LEVERAGE_MATCH_TRANSLATED_TUV = "SELECT DISTINCT lm.ORIGINAL_SOURCE_TUV_ID FROM "
            + LM_TABLE_PLACEHOLDER 
            + " lm WHERE"
            + " lm.SCORE_NUM = 100"
            + " AND lm.MATCH_TYPE != 'UNVERIFIED_EXACT_MATCH'"
            + " AND lm.SOURCE_PAGE_ID = :sid"
            + " AND lm.SUB_ID = 0"
            + " AND lm.TARGET_LOCALE_ID = :lid";
    
    private static final String TARGET_TUV_SOURCE_TUV = "SELECT tuv2.id FROM "
            + TUV_TABLE_PLACEHOLDER
            + " tuv1, "
            + TUV_TABLE_PLACEHOLDER
            + " tuv2 WHERE"
            + " tuv1.TU_ID = tuv2.TU_ID"
            + " AND tuv1.ID in (:sids)"
            + " AND tuv2.LOCALE_ID = :lid"
            + " AND tuv2.STATE != 'OUT_OF_DATE'"
            + " AND tuv2.STATE != 'DO_NOT_TRANSLATE'";

    /**
     * Save TUVs into DB.
     *
     * @param p_connection
     * @param p_tuvs
     *            -- TuvImpl objects
     * @param companyId
     * @throws Exception
     */
	public static void saveTuvs(Connection p_connection,
			Collection<Tuv> p_tuvs, long p_jobId) throws Exception
    {
        PreparedStatement ps = null;

        try
        {
            // Update the TUV sequence first despite below succeeding or failure.
            SegmentTuTuvIndexUtil.updateTuvSequence(p_connection);

            String sql = SAVE_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    BigTableUtil.getTuvTableJobDataInByJobId(p_jobId));
            ps = p_connection.prepareStatement(sql);

            Set<Long> tuIds = new HashSet<Long>();
            int batchUpdate = 0;
			List<TuTuvAttributeImpl> sidAttibutes = new ArrayList<TuTuvAttributeImpl>();
            for (Iterator<Tuv> it = p_tuvs.iterator(); it.hasNext();)
            {
                TuvImpl tuv = (TuvImpl) it.next();
                tuIds.add(tuv.getTuId());

                ps.setLong(1, tuv.getId());
                ps.setLong(2, tuv.getOrder());
                ps.setLong(3, tuv.getLocaleId());
                ps.setLong(4, tuv.getTu(p_jobId).getId());
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
				// Since 8.6.1, always save SID into "value_text" column
				// of "translation_tu_tuv_attr_x" table, regardless its length.
                ps.setString(18, null);
                if (StringUtil.isNotEmpty(tuv.getSid()))
                {
                	// Also save it in original table if not too long.
                	if (tuv.getSid().length() < 254) {
                    	ps.setString(18, tuv.getSid());
                    }

                	TuTuvAttributeImpl sidAttr = new TuTuvAttributeImpl(
							tuv.getId(), TuTuvAttributeImpl.OBJECT_TYPE_TUV,
							TuTuvAttributeImpl.SID);
                    sidAttr.setTextValue(tuv.getSid());
                    sidAttibutes.add(sidAttr);
                }
                ps.setString(19, tuv.getSrcComment());
                ps.setLong(20, tuv.getRepetitionOfId());
                ps.setString(21, tuv.isRepeated() ? "Y" : "N");

                ps.addBatch();
                batchUpdate++;
                if (batchUpdate > DbUtil.BATCH_INSERT_UNIT)
                {
                    ps.executeBatch();
                    batchUpdate = 0;
                }
            }

            // execute the rest of the added batch
            if (batchUpdate > 0)
            {
                ps.executeBatch();
            }

            // Cache the TUVs for large pages when create jobs.
            if (tuIds.size() > 800)
            {
                for (Iterator<Tuv> it = p_tuvs.iterator(); it.hasNext();)
                {
                    TuvImpl tuv = (TuvImpl) it.next();
                    setTuvIntoCache(tuv);
                }
            }
            p_connection.commit();

            // Save SID into "translation_tu_tuv_xx" table.
			if (sidAttibutes.size() > 0)
			{
				SegmentTuTuvAttributeUtil.saveTuTuvAttributes(p_connection,
						sidAttibutes, p_jobId);
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
     * @param p_jobId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvById(long p_tuvId, long p_jobId)
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
            tuv = getTuvById(conn, p_tuvId, p_jobId);
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
     * @param p_tuvId
     * @param p_jobId
     * @return TuvImpl
     * @throws SQLException
     */
    public static TuvImpl getTuvById(Connection connection, long p_tuvId,
            long p_jobId) throws Exception
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
                    BigTableUtil.getTuvTableJobDataInByJobId(p_jobId));

            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_tuvId);
            rs = ps.executeQuery();

            List<TuvImpl> tuvs = convertResultSetToTuv(rs, true, p_jobId);
            if (tuvs != null && tuvs.size() > 0)
            {
                tuv = tuvs.get(0);
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
     * @param p_jobId
     * @return TuvImpl list
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuIdsLocaleId(long[] p_tuIds,
            long p_localeId, long p_jobId) throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Connection conn = DbUtil.getConnection();
        try
        {
            for (int i = 0; i < p_tuIds.length; i++)
            {
                result.add(getTuvByTuIdLocaleId(conn, p_tuIds[i], p_localeId,
                        p_jobId));
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
     * @param p_jobId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvByTuIdLocaleId(long p_tuId, long p_localeId,
            long jobId) throws Exception
    {
        TuvImpl tuv = null;

        Connection conn = DbUtil.getConnection();
        try
        {
            tuv = getTuvByTuIdLocaleId(conn, p_tuId, p_localeId, jobId);
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
     * @param p_jobId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvByTuIdLocaleId(Connection p_connection,
            long p_tuId, long p_localeId, long p_jobId) throws Exception
    {
        TuvImpl tuv = null;

        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        try
        {
            String sql = GET_TUV_BY_TU_ID_LOCALE_ID_SQL.replace(
                    TUV_TABLE_PLACEHOLDER,
                    BigTableUtil.getTuvTableJobDataInByJobId(p_jobId));

            ps1 = p_connection.prepareStatement(sql);
            ps1.setLong(1, p_localeId);
            ps1.setLong(2, p_tuId);
            rs1 = ps1.executeQuery();

            List<TuvImpl> result = convertResultSetToTuv(rs1, true, p_jobId);
            if (result != null && result.size() > 0)
            {
                tuv = result.get(0);
            }
            // Maybe current job has been archived, so...
            else
            {
                sql = GET_TUV_BY_TU_ID_LOCALE_ID_SQL.replace(
                        TUV_TABLE_PLACEHOLDER,
                        BigTableUtil.getTuvArchiveTableByJobId(p_jobId));

                ps2 = p_connection.prepareStatement(sql);
                ps2.setLong(1, p_localeId);
                ps2.setLong(2, p_tuId);
                rs2 = ps2.executeQuery();

                result = convertResultSetToTuv(rs2, true, p_jobId);
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
            releaseRsPsConnection(rs1, ps1, null);
            releaseRsPsConnection(rs2, ps2, null);
        }

        return tuv;
    }

    /**
     * Query translation unit variant by TuId.
     *
     * @param p_tuId
     * @param p_jobId
     * @return TUV list for specified TU_ID.
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuId(long p_tuId, long p_jobId)
            throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Connection conn = DbUtil.getConnection();
        try
        {
            result = getTuvsByTuId(conn, p_tuId, p_jobId);
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
     * @param p_tuId
     * @param p_jobId
     * @return TUV list for specified TU_ID.
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuId(Connection connection,
            long p_tuId, long p_jobId) throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        try
        {
            String sql = GET_TUVS_BY_TU_ID_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    BigTableUtil.getTuvTableJobDataInByJobId(p_jobId));

            ps1 = connection.prepareStatement(sql);
            ps1.setLong(1, p_tuId);
            rs1 = ps1.executeQuery();

            result = convertResultSetToTuv(rs1, true, p_jobId);
            if (result == null || result.size() == 0)
            {
                sql = GET_TUVS_BY_TU_ID_SQL.replace(TUV_TABLE_PLACEHOLDER,
                        BigTableUtil.getTuvArchiveTableByJobId(p_jobId));
                ps2 = connection.prepareStatement(sql);
                ps2.setLong(1, p_tuId);
                rs2 = ps2.executeQuery();

                result = convertResultSetToTuv(rs2, true, p_jobId);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get TUVs by tuID " + p_tuId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs1, ps1, null);
            releaseRsPsConnection(rs2, ps2, null);
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
            long spId = p_sourcePage.getId();
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInBySourcePageId(spId);
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInBySourcePageId(spId);
            String sql = GET_SOURCE_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_sourcePage.getLocaleId());
            ps.setLong(2, p_sourcePage.getId());
            rs = ps.executeQuery();

			result.addAll(convertResultSetToTuv(rs, false,
					p_sourcePage.getJobId()));

            // Load xliff_alt data in page level to improve performance.
            if (p_needLoadXlfAlts
                    && XliffAltUtil.isGenerateXliffAlt(p_sourcePage))
            {
                loadXliffAlts(result, p_sourcePage.getLocaleId(),
                        p_sourcePage.getId(), tuTableName, tuvTableName);
            }

            setHashValues(result);
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
     * @param p_localeId
     *            -- targetLocaleId
     * @param p_sourcePageId
     * @return TuvImpl list
     * @throws Exception
     */
    public static List<Tuv> getExportTuvs(long p_localeId, long p_sourcePageId)
            throws Exception
    {
        List<Tuv> result = new ArrayList<Tuv>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            conn = DbUtil.getConnection();
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInBySourcePageId(p_sourcePageId);
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInBySourcePageId(p_sourcePageId);
            String sql = GET_EXPORT_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_localeId);
            ps.setLong(2, p_sourcePageId);
            rs = ps.executeQuery();

            SourcePage sp = ServerProxy.getPageManager().getSourcePage(
                    p_sourcePageId);
            result.addAll(convertResultSetToTuv(rs, false, sp.getJobId()));

            if (XliffAltUtil.isGenerateXliffAlt(sp))
            {
                // Load xliff_alt data in page level to improve performance.
                loadXliffAlts(result, p_localeId, p_sourcePageId, tuTableName,
                        tuvTableName);
            }

            setHashValues(result);
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
        List<Tuv> tuvs = new ArrayList<Tuv>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            long sourcePageId = p_targetPage.getSourcePage().getId();
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInBySourcePageId(sourcePageId);
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInBySourcePageId(sourcePageId);
            String sql = GET_TARGET_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_targetPage.getLocaleId());
            ps.setLong(2, p_targetPage.getId());
            rs = ps.executeQuery();
			tuvs.addAll(convertResultSetToTuv(rs, false, p_targetPage
					.getSourcePage().getJobId()));

            // Load xliff_alt data in page level to improve performance.
            if (XliffAltUtil.isGenerateXliffAlt(p_targetPage.getSourcePage()))
            {
                loadXliffAlts2(new ArrayList<Tuv>(tuvs),
                        p_targetPage.getLocaleId(), p_targetPage.getId(),
                        tuTableName, tuvTableName);                
            }

            setHashValues(tuvs);

            List<TuvImpl> result = new ArrayList<TuvImpl>();
            for (Tuv tuv : tuvs)
            {
            	result.add((TuvImpl) tuv);
            }
            return result;
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
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInBySourcePageId(sourcePageId);
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInBySourcePageId(sourcePageId);
            String sql = GET_ALL_TARGET_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_targetPage.getLocaleId());
            ps.setLong(2, p_targetPage.getId());
            rs = ps.executeQuery();

			result.addAll(convertResultSetToTuv(rs, false, p_targetPage
					.getSourcePage().getJobId()));

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
     * @param p_jobId
     * @return List<TuImpl>
     * @throws Exception
     */
    public static List<TuvImpl> getRepTuvsByTuIdsAndLocaleId(
            List<Long> p_tuIds, long p_targetLocaleId, long p_jobId)
            throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInByJobId(p_jobId);
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInByJobId(p_jobId);
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

            result = convertResultSetToTuv(rs, false, p_jobId);
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

    public static void updateTuv(TuvImpl p_tuv, long jobId)
            throws Exception
    {
        List<TuvImpl> tuvs = new ArrayList<TuvImpl>();
        tuvs.add(p_tuv);
        updateTuvs(tuvs, jobId);
    }

    public static void updateTuv(Connection p_connection, TuvImpl p_tuv,
            long jobId) throws Exception
    {
        List<TuvImpl> tuvs = new ArrayList<TuvImpl>();
        tuvs.add(p_tuv);
        updateTuvs(p_connection, tuvs, jobId);
    }

    public static void updateTuvs(List<TuvImpl> p_tuvs, long p_jobId)
            throws Exception
    {
        Connection conn = DbUtil.getConnection();
        boolean isAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try
        {
            updateTuvs(conn, p_tuvs, p_jobId);
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
            List<TuvImpl> p_tuvs, long p_jobId) throws Exception
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
			List<TuTuvAttributeImpl> sidAttibutes = new ArrayList<TuTuvAttributeImpl>();
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

				// Since 8.6.1, always save SID into "value_text" column
				// of "translation_tu_tuv_attr_x" table, regardless its length.
                tuvUpdateStmt.setString(17, null);
                if (StringUtil.isNotEmpty(tuv.getSid()))
                {
                	// Also save it in original table if not too long.
                	if (tuv.getSid().length() < 254) {
                		tuvUpdateStmt.setString(17, tuv.getSid());
                    }

                	TuTuvAttributeImpl sidAttr = new TuTuvAttributeImpl(
							tuv.getId(), TuTuvAttributeImpl.OBJECT_TYPE_TUV,
							TuTuvAttributeImpl.SID);
                    sidAttr.setTextValue(tuv.getSid());
                    sidAttibutes.add(sidAttr);
                }
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

            // Update SID into "translation_tu_tuv_xx" table.
			if (sidAttibutes.size() > 0)
			{
				SegmentTuTuvAttributeUtil.updateSidAttributes(
						p_connection, sidAttibutes, p_jobId);
			}
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
            boolean loadExtraInfo, long p_jobId) throws Exception
    {
        ArrayList<TuvImpl> result = new ArrayList<TuvImpl>();
        if (rs == null)
        {
            return result;
        }

        List<Long> tuvIds = new ArrayList<Long>();
        while (rs.next())
        {
            TuvImpl tuv = new TuvImpl();

            Long tuvId = new Long(rs.getLong(1));
            tuvIds.add(tuvId);
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
            }

            result.add(tuv);
        }

		List<TuTuvAttributeImpl> sidAttrs = SegmentTuTuvAttributeUtil
				.getSidAttributesByTuvIds(tuvIds, p_jobId);
		HashMap<Long, String> sidAttrMap = new HashMap<Long, String>();
		for (TuTuvAttributeImpl sidAttr : sidAttrs)
		{
			sidAttrMap.put(sidAttr.getObjectId(), sidAttr.getTextValue());
		}
		for (TuvImpl tuv : result)
		{
			if (StringUtil.isNotEmpty(sidAttrMap.get(tuv.getId())))
			{
				tuv.setSid(sidAttrMap.get(tuv.getId()));
			}

			// MUST DO THIS HERE!!!
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
     * Get the translated percentage for specified target page.
     * 
     * @param trgPageId
     *            target page Id
     * 
     * @return translated percentage in string.
     */
    public static int getTranslatedPercentageForTargetPage(long trgPageId)
    {
        int[] counts = getTotalAndTranslatedTuvCount(trgPageId);

        int totalCounts = counts[0];
        int translatedCounts = counts[1];
        return calculateTranslatedPercentage(totalCounts, translatedCounts);
    }

    /**
     * Get the translated percentage for task.
     * 
     * @param p_task -- Task object
     *
     * @return translated percentage in string.
     * 
     */
    public static int getTranslatedPercentageForTask(Task p_task)
    {
        int totalCounts = 0;
        int translatedCounts = 0;

        List<TargetPage> targetPages = p_task.getTargetPages();
        for (TargetPage tp : targetPages)
        {
            int[] counts = getTotalAndTranslatedTuvCount(tp.getId());
            totalCounts += counts[0];
            translatedCounts += counts[1];
        }

        return calculateTranslatedPercentage(totalCounts, translatedCounts);
    }

	public static int getTranslatedPercentageForTargetPages(
			List<TargetPage> targetPages)
	{
		int totalCounts = 0;
		int translatedCounts = 0;

		for (TargetPage tp : targetPages)
		{
			int[] counts = getTotalAndTranslatedTuvCount(tp.getId());
			totalCounts += counts[0];
			translatedCounts += counts[1];
		}

		return calculateTranslatedPercentage(totalCounts, translatedCounts);
	}
    
    private static int calculateTranslatedPercentage(int totalCounts,
            int translatedCounts)
    {
        int translatedPercentage = 100;
        if (totalCounts != 0)
        {
            translatedPercentage = Math.round(BigDecimalHelper.divide(
                    translatedCounts * 100, totalCounts));
            // 9999/10000 may return 100, so -1 in purpose.
            if (translatedPercentage == 100 && totalCounts > translatedCounts)
            {
                translatedPercentage = 99;
            }
        }

        return translatedPercentage;
    }

    /**
     * Get total and translated segment counter in array.
     * 
     * @param targetPageId
     * 
     * @return int[2], int[0] is total count, int[1] is translated count.
     */
    private static int[] getTotalAndTranslatedTuvCount(Long targetPageId)
    {
        int[] result = new int[2];

        int total = 0;
        try
        {
            Long sourcePageId = ((BigInteger) HibernateUtil.getFirstWithSql(GET_SOURCE_PAGE_ID, targetPageId)).longValue();
            Long localeId = ((BigInteger) HibernateUtil.getFirstWithSql(GET_LOCALE_ID, targetPageId)).longValue();
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInBySourcePageId(sourcePageId);
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInBySourcePageId(sourcePageId);
            String sql = COUNT_TRANSLATE_TUV_ALL.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);
            total = ((BigInteger) HibernateUtil.getFirstWithSql(sql, localeId, targetPageId)).intValue();
            
            sql = TRANSLATE_TUV_TRANSLATED.replace(TUV_TABLE_PLACEHOLDER,
                    tuvTableName).replace(TU_TABLE_PLACEHOLDER, tuTableName);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("lid", localeId);
            map.put("tid", targetPageId);
            List<Long> tuvId = (List<Long>) HibernateUtil.searchWithSql(sql, map);
            Set<Long> tids = new HashSet<Long>();
            tids.addAll(tuvId);
            
            String lmTableName = BigTableUtil.getLMTableJobDataInBySourcePageId(sourcePageId);
            sql = LEVERAGE_MATCH_TRANSLATED_TUV.replace(LM_TABLE_PLACEHOLDER, lmTableName);
            map = new HashMap<String, Object>();
            map.put("lid", localeId);
            map.put("sid", sourcePageId);
            List<Object> sourceTuvId = (List<Object>) HibernateUtil.searchWithSql(sql, map);
            
            if (sourceTuvId.size() > 0)
            {
                sql = TARGET_TUV_SOURCE_TUV.replace(TUV_TABLE_PLACEHOLDER, tuvTableName);
                map = new HashMap<String, Object>();
                map.put("lid", localeId);
                
                Map<String, List<Object>> sids = new HashMap<String, List<Object>>();
                sids.put("sids", sourceTuvId);
                
                List<Long> targetTuvId = (List<Long>) HibernateUtil.searchWithSqlWithIn(sql, map, sids);
                tids.addAll(targetTuvId);
            }
            
            result[0] = total;
            result[1] = tids.size();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return result;
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

        for (TargetPage tp : p_tps)
        {
            int[] counts = getTotalAndTranslatedTuvCount(tp.getIdAsLong());
            int total = counts[0];
            int translatedCounts = counts[1];
            if (total > translatedCounts)
            {
                result.add(tp);
            }
        }

        return result;
    }

    /**
     * Return a string like "1,2,3,4".
     * @param ids
     * @return
     */
    private static String formStringFromList(List<Long> ids)
    {
        if (ids == null || ids.size() == 0)
            return null;

        StringBuilder idsBuffer = new StringBuilder();
        for (Long id : ids)
        {
            idsBuffer.append(id).append(",");
        }

        return idsBuffer.toString().substring(0, idsBuffer.length() - 1);
    }

    public static void approveTuvByTargetPageId(Long targetPageId)
    {
        PageManager pageManager = ServerProxy.getPageManager();
        LeverageMatchLingManager lingManager =
                LingServerProxy.getLeverageMatchLingManager();

        SourcePage sourcePage = null;
        TargetPage targetPage = null;
        MatchTypeStatistics tuvMatchTypes = null;
        List<Long> untranslatedTrgTuvIds = new ArrayList<Long>();
        try
        {
            targetPage = pageManager.getTargetPage(targetPageId);
            sourcePage= targetPage.getSourcePage();
            List sourceTuvs = getSourceTuvs(sourcePage);
            List targetTuvs = getTargetTuvs(targetPage);
            Long targetLocaleId = targetPage.getGlobalSightLocale().getIdAsLong();
            lingManager.setIncludeMtMatches(true);
            tuvMatchTypes = lingManager.getMatchTypesForStatistics(
                    sourcePage.getIdAsLong(), targetLocaleId, 0);

            for (int i = 0; i < targetTuvs.size(); i++)
            {
                Tuv sourceTuv = (Tuv) sourceTuvs.get(i);
                Tuv targetTuv = (Tuv) targetTuvs.get(i);
                if (!SegmentFilter.isTreatAsTranslated(sourceTuv, targetTuv,
                        tuvMatchTypes))
                {
                    untranslatedTrgTuvIds.add(targetTuv.getIdAsLong());
                }
            }

            String trgTuvIds = formStringFromList(untranslatedTrgTuvIds);

            if(trgTuvIds != null)
            {           	
            	Connection conn = null;
            	PreparedStatement ps = null;
            	try
            	{
            		conn = DbUtil.getConnection();
            		conn.setAutoCommit(false);
                    String tuvTableName = BigTableUtil
                            .getTuvTableJobDataInBySourcePageId(sourcePage
                                    .getId());
                    String sql = APPROVE_TUV_SQL.replace(TUV_TABLE_PLACEHOLDER,
                            tuvTableName).replace(
                            "untranslated_target_tuv_ids", trgTuvIds);
            		
            		ps = conn.prepareStatement(sql);
            		ps.executeUpdate();
            		conn.commit();
            	}
            	catch (Exception e)
            	{
            		logger.error("Error when approveTuvByTargetPageId(..).", e);
            	}
            	finally
            	{
            		DbUtil.silentClose(ps);
            		DbUtil.silentReturnConnection(conn);
            	}
            }
            
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Set previous hash and next hash for the ordered TUVs.
     */
    public static void setHashValues(List<Tuv> tuvs)
    {
    	TuvImpl preTuv = null;
    	TuvImpl curTuv = null;
    	for (Tuv tuv : tuvs)
    	{
    		curTuv = (TuvImpl) tuv;
    		curTuv.setPreviousHash(BaseTmTuv.FIRST_HASH);
    		curTuv.setNextHash(BaseTmTuv.LAST_HASH);
    		if (preTuv != null)
    		{
    			curTuv.setPreviousHash(getHashValue(preTuv.getGxml()));
    			preTuv.setNextHash(getHashValue(curTuv.getGxml()));
    		}
    		preTuv = curTuv;
    	}
    }

    public static long getHashValue(String data)
    {
        try
        {
            SegmentTmExactMatchFormatHandler handler =
                new SegmentTmExactMatchFormatHandler();
            DiplomatBasicParser diplomatParser =
                new DiplomatBasicParser(handler);
            diplomatParser.parse(data);
            return Fingerprint.fromString(handler.toString());
        }
        catch (Exception ex)
        {
        	logger.error("Error to get hash value for data: " + data, ex);
        }
    	return -1;
    }
}
