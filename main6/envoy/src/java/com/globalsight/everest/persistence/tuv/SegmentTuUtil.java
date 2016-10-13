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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * Helper for "translation_unit_[companyId]" storage.
 * 
 * @author york.jin
 * @since 2012-03-22
 * @version 8.2.3
 */
public class SegmentTuUtil extends SegmentTuTuvCacheManager implements
        TuvQueryConstants
{
    static private final Logger logger = Logger.getLogger(SegmentTuUtil.class);

    private static final String SELECT_COLUMNS = "SELECT "
            + "tu.ID, tu.ORDER_NUM, tu.TM_ID, tu.DATA_TYPE, tu.TU_TYPE, "
            + "tu.LOCALIZE_TYPE, tu.LEVERAGE_GROUP_ID, tu.PID, tu.SOURCE_TM_NAME, tu.XLIFF_TRANSLATION_TYPE, "
            + "tu.XLIFF_LOCKED, tu.IWS_SCORE, tu.XLIFF_TARGET_SEGMENT, tu.XLIFF_TARGET_LANGUAGE, tu.GENERATE_FROM, "
            + "tu.SOURCE_CONTENT, tu.PASSOLO_STATE, tu.TRANSLATE FROM ";

    private static final String GET_TU_BY_ID_SQL = SELECT_COLUMNS
            + TU_TABLE_PLACEHOLDER + " tu" + " " + "WHERE tu.ID = ? ";

    private static final String GET_TUS_BY_LEVERAGE_GROUP_ID_SQL = SELECT_COLUMNS
            + TU_TABLE_PLACEHOLDER + " tu " + "WHERE tu.LEVERAGE_GROUP_ID = ?";

    private static final String GET_TUS_BY_SPID_SQL = SELECT_COLUMNS
            + TU_TABLE_PLACEHOLDER + " tu, "
            + "source_page_leverage_group splg "
            + "WHERE tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = ? " + "ORDER BY tu.order_num asc ";

    private static final String IS_WORLD_SERVER_XLF_FILE = "SELECT COUNT(*) FROM "
            + TU_TABLE_PLACEHOLDER
            + " tu, "
            + "source_page_leverage_group splg "
            + "WHERE tu.leverage_group_id = splg.lg_id "
            + "AND tu.generate_from = '"
            + TuImpl.FROM_WORLDSERVER 
            + "' "
            + "AND splg.sp_id = ? ";

    public static void saveTus(Connection conn, Collection<Tu> p_tus,
            long p_jobId) throws Exception
    {
        PreparedStatement ps = null;

        try
        {
            // Update the TU sequence first despite below succeeding or failure.
            SegmentTuTuvIndexUtil.updateTuSequence(conn);

            conn.setAutoCommit(false);
            String tuTable = BigTableUtil.getTuTableJobDataInByJobId(p_jobId);
            StringBuilder strBuilder = new StringBuilder();
            strBuilder = strBuilder
                    .append("insert into ")
                    .append(tuTable)
                    .append(" (")
                    .append("id, order_num, tm_id, data_type, tu_type, ")
                    .append("localize_type, leverage_group_id, pid, source_tm_name, xliff_translation_type, ")
                    .append("xliff_locked, iws_score, xliff_target_segment, xliff_target_language, generate_from, ")
                    .append("source_content, passolo_state, translate) ")
                    .append("values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps = conn.prepareStatement(strBuilder.toString());

            // Cache the TUs for large pages when create job.
            boolean ifCacheTus = false;
            if (p_tus.size() > 800)
            {
                ifCacheTus = true;
            }
            int batchUpdate = 0;
            for (Iterator<Tu> it = p_tus.iterator(); it.hasNext();)
            {
                TuImpl tu = (TuImpl) it.next();
                if (ifCacheTus)
                {
                    // Cache this Tu before save it.
                    setTuIntoCache(tu, false, p_jobId);
                }

                ps.setLong(1, tu.getId());
                ps.setLong(2, tu.getOrder());
                ps.setLong(3, tu.getTmId());
                ps.setString(4, tu.getDataType());
                ps.setString(5, tu.getTuType());

                ps.setString(6, Character.toString(tu.getLocalizableType()));
                ps.setLong(7, tu.getLeverageGroupId());
                ps.setLong(8, tu.getPid());
                ps.setString(9, tu.getSourceTmName());
                ps.setString(10, tu.getXliffTranslationType());

                ps.setString(11, tu.isXliffLocked() ? "Y" : "N");
                ps.setString(12, tu.getIwsScore());
                ps.setString(13, tu.getXliffTarget());
                ps.setString(14, tu.getXliffTargetLanguage());
                ps.setString(15, tu.getGenerateFrom());

                ps.setString(16, tu.getSourceContent());
                ps.setString(17, tu.getPassoloState());
                ps.setString(18, tu.getTranslate());

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

            conn.commit();
        }
        catch (Exception e)
        {
            logger.error("Error when save TUs" + e.getMessage(), e);
            for (Iterator<Tu> it = p_tus.iterator(); it.hasNext();)
            {
                TuImpl tu = (TuImpl) it.next();
                removeTuFromCache(tu.getId());
            }
            throw e;
        }
        finally
        {
            releaseRsPsConnection(null, ps, null);
        }
    }

    public static TuImpl getTuById(long p_tuId, long p_jobId)
            throws Exception
    {
        TuImpl tu = getTuFromCache(p_tuId);
        if (tu != null)
        {
            return tu;
        }

        Connection conn = DbUtil.getConnection();
        try
        {
            tu = getTuById(conn, p_tuId, p_jobId);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        return tu;
    }

    public static TuImpl getTuById(Connection connection, long p_tuId,
            long p_jobId) throws Exception
    {
        TuImpl tu = getTuFromCache(p_tuId);
        if (tu != null)
        {
            return tu;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = GET_TU_BY_ID_SQL.replace(TU_TABLE_PLACEHOLDER,
                    BigTableUtil.getTuTableJobDataInByJobId(p_jobId));
            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_tuId);
            rs = ps.executeQuery();
            List<TuImpl> result = convertResultSetToTuImpl(rs, true, p_jobId);
            if (result != null && result.size() > 0)
            {
                tu = result.get(0);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get TU by tuID " + p_tuId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return tu;
    }

    public static List<TuImpl> getTusByLeverageGroupId(long p_leverageGroupId)
            throws Exception
    {
        List<TuImpl> result = new ArrayList<TuImpl>();

        Connection conn = DbUtil.getConnection();
        try
        {
            result = getTusByLeverageGroupId(conn, p_leverageGroupId);
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
     * Get all TUs which belong to the specified leverage group.
     * 
     * @param p_connection
     * @param p_leverageGroupId
     * @return
     * @throws SQLException
     */
    public static List<TuImpl> getTusByLeverageGroupId(Connection p_connection,
            long p_leverageGroupId) throws Exception
    {
        List<TuImpl> result = new ArrayList<TuImpl>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            SourcePage sp = ServerProxy.getPageManager()
                    .getSourcePageByLeverageGroupId(p_leverageGroupId);
            long jobId = sp.getJobId();
            String tuTableName = BigTableUtil.getTuTableJobDataInByJobId(jobId);
            String sql = GET_TUS_BY_LEVERAGE_GROUP_ID_SQL.replace(
                    TU_TABLE_PLACEHOLDER, tuTableName);

            ps = p_connection.prepareStatement(sql);
            ps.setLong(1, p_leverageGroupId);
            rs = ps.executeQuery();

            result = convertResultSetToTuImpl(rs, false, jobId);
            if (RemovedTagsUtil.isGenerateRemovedTags(sp.getId()))
            {
                // Load "removed tags" in page level to improve performance.
                RemovedTagsUtil.loadAllRemovedTagsForTus(result, sp.getId(),
                        tuTableName);
            }
        }
        catch (SQLException e)
        {
            logger.error(
                    "Error when getTusByLeverageGroupId for p_leverageGroupId "
                            + p_leverageGroupId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return result;
    }

    public static List<TuImpl> getTusBySourcePageId(long p_sourcePageId)
            throws Exception
    {
        List<TuImpl> result = new ArrayList<TuImpl>();

        Connection conn = DbUtil.getConnection();
        try
        {
            result = getTusBySourcePageId(conn, p_sourcePageId);
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

    public static List<TuImpl> getTusBySourcePageId(Connection p_connection,
            long p_sourcePageId) throws Exception
    {
        List<TuImpl> result = new ArrayList<TuImpl>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInBySourcePageId(p_sourcePageId);
            String sql = GET_TUS_BY_SPID_SQL.replace(TU_TABLE_PLACEHOLDER,
                    tuTableName);

            ps = p_connection.prepareStatement(sql);
            ps.setLong(1, p_sourcePageId);
            rs = ps.executeQuery();

            long jobId = BigTableUtil.getJobBySourcePageId(p_sourcePageId)
                    .getId();
            result = convertResultSetToTuImpl(rs, false, jobId);
            if (RemovedTagsUtil.isGenerateRemovedTags(p_sourcePageId))
            {
                // Load "removed tags" in page level to improve performance.
                RemovedTagsUtil.loadAllRemovedTagsForTus(result,
                        p_sourcePageId, tuTableName);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getTusBySourcePageId() for sourcePageId "
                    + p_sourcePageId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return result;
    }

    public static void updateTus(Collection<TuImpl> p_tus, long jobId)
            throws Exception
    {
        if (p_tus == null || p_tus.size() == 0)
            return;

        Connection conn = DbUtil.getConnection();
        conn.setAutoCommit(false);
        try
        {
            StringBuilder sql = new StringBuilder();
            sql.append("update ")
                    .append(BigTableUtil.getTuTableJobDataInByJobId(jobId))
                    .append(" set ");
            sql.append("order_num = ?, ");// 1
            sql.append("tm_id = ?, ");// 2
            sql.append("data_type = ?, ");// 3
            sql.append("tu_type = ?, ");// 4
            sql.append("localize_type = ?, ");// 5
            sql.append("leverage_group_id = ?, ");// 6
            sql.append("pid = ?, ");// 7
            sql.append("source_tm_name = ?, ");// 8
            sql.append("xliff_translation_type = ?, ");// 9
            sql.append("xliff_locked = ?, ");// 10
            sql.append("iws_score = ?, ");// 11
            sql.append("xliff_target_segment = ?, ");// 12
            sql.append("xliff_target_language = ?, ");// 13
            sql.append("generate_from = ?, ");// 14
            sql.append("source_content = ?, ");// 15
            sql.append("passolo_state = ?, ");// 16
            sql.append("translate = ? where id = ?");// 17 18

            PreparedStatement tuUpdateStmt = conn.prepareStatement(sql
                    .toString());

            // addBatch counters
            int batchUpdate = 0;

            for (TuImpl tu : p_tus)
            {
                tuUpdateStmt.setLong(1, tu.getOrder());
                tuUpdateStmt.setLong(2, tu.getTmId());
                tuUpdateStmt.setString(3, tu.getDataType());
                tuUpdateStmt.setString(4, tu.getTuType());
                tuUpdateStmt.setString(5,
                        String.valueOf(tu.getLocalizableType()));
                tuUpdateStmt.setLong(6, tu.getLeverageGroupId());
                tuUpdateStmt.setLong(7, tu.getPid());
                tuUpdateStmt.setString(8, tu.getSourceTmName());
                tuUpdateStmt.setString(9, tu.getXliffTranslationType());
                tuUpdateStmt.setString(10, tu.isXliffLocked() ? "Y" : "N");
                tuUpdateStmt.setString(11, tu.getIwsScore());
                tuUpdateStmt.setString(12, tu.getXliffTarget());
                tuUpdateStmt.setString(13, tu.getXliffTargetLanguage());
                tuUpdateStmt.setString(14, tu.getGenerateFrom());
                tuUpdateStmt.setString(15, tu.getSourceContent());
                tuUpdateStmt.setString(16, tu.getPassoloState());
                tuUpdateStmt.setString(17, tu.getTranslate());
                tuUpdateStmt.setLong(18, tu.getIdAsLong());

                tuUpdateStmt.addBatch();

                batchUpdate++;

                if (batchUpdate > DbUtil.BATCH_INSERT_UNIT)
                {
                    tuUpdateStmt.executeBatch();
                    batchUpdate = 0;
                }
            }

            // execute the rest of the added batch
            if (batchUpdate > 0)
            {
                tuUpdateStmt.executeBatch();
            }

            conn.commit();
            tuUpdateStmt.close();
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * Check if current source page is a world-server XLF file.
     * 
     * @param p_sourcePageId
     * @return boolean
     * @throws Exception
     */
    public static boolean isWorldServerXlfSourceFile(long p_sourcePageId)
            throws Exception
    {
        boolean result = false;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInBySourcePageId(p_sourcePageId);
            String sql = IS_WORLD_SERVER_XLF_FILE.replace(TU_TABLE_PLACEHOLDER,
                    tuTableName);
            connection = DbUtil.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_sourcePageId);
            rs = ps.executeQuery();
            if (rs.next())
            {
                long count = rs.getLong(1);
                if (count > 0)
                {
                    result = true;
                }
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Error when judge isWorldServerXlfSourceFile() for sourcePageId "
                            + p_sourcePageId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, connection);
        }

        return result;
    }

    /**
     * Convert ResultSet data into TuImpl objects.
     * 
     * @param rs
     *            -- ResultSet
     * @param p_loadExtraData
     *            -- boolean,generally if the ResultSet is small, this can be
     *            true;Otherwise false to improve performance.
     * @return -- List<TuImpl>
     * @throws SQLException
     */
    private static List<TuImpl> convertResultSetToTuImpl(ResultSet rs,
            boolean p_loadExtraData, long jobId) throws SQLException
    {
        List<TuImpl> result = new ArrayList<TuImpl>();
        if (rs == null)
        {
            return result;
        }

        while (rs.next())
        {
            TuImpl tu = null;

            Long tuId = new Long(rs.getLong(1));
            // Check if TU object has been in the cache.
            tu = getTuFromCache(tuId);
            if (tu != null)
            {
                result.add(tu);
                continue;
            }

            if (tu == null)
            {
                tu = new TuImpl();
            }
            tu.setId(rs.getLong(1));
            tu.setOrder(rs.getLong(2));
            tu.setTmId(rs.getLong(3));
            tu.setDataType(rs.getString(4));
            tu.setTuType(rs.getString(5));

            tu.setLocalizableType(rs.getString(6).charAt(0));
            tu.setLeverageGroupId(rs.getLong(7));
            tu.setPid(rs.getLong(8));
            tu.setSourceTmName(rs.getString(9));
            tu.setXliffTranslationType(rs.getString(10));

            tu.setXliffLocked("Y".equalsIgnoreCase(rs.getString(11)) ? true
                    : false);
            tu.setIwsScore(rs.getString(12));
            tu.setXliffTarget(rs.getString(13));
            tu.setXliffTargetLanguage(rs.getString(14));
            tu.setGenerateFrom(rs.getString(15));

            tu.setSourceContent(rs.getString(16));
            tu.setPassoloState(rs.getString(17));
            tu.setTranslate(rs.getString(18));

            if (p_loadExtraData)
            {
                // loadAllRemovedTags(tu);
            }
            result.add(tu);
            // Cache this Tu object
            setTuIntoCache(tu, false, jobId);
        }

        return result;
    }
}
