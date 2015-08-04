/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class JobDataMigration
{
    private static final Logger logger = Logger.getLogger(JobDataMigration.class);

    private static final int BATCH_CAPACITY = 1000;

    private static final String SQL_DROP = "drop table if exists ";

    private static final String FROM_TU_TABLE = "\uE000" + "_FROM_TU_TABLE_" + "\uE000";
    private static final String TO_TU_TABLE = "\uE000" + "_TO_TU_TABLE_" + "\uE000";

    private static final String FROM_TUV_TABLE = "\uE000" + "_FROM_TUV_TABLE_" + "\uE000";
    private static final String TO_TUV_TABLE = "\uE000" + "_TO_TUV_TABLE_" + "\uE000";

    private static final String FROM_LM_TABLE = "\uE000" + "_FROM_LM_TABLE_" + "\uE000";
    private static final String TO_LM_TABLE = "\uE000" + "_TO_LM_TABLE_" + "\uE000";

    // Move leverage match data to leverage match archived table.
    private static final String LM_ARCHIVED_INSERT =
            "REPLACE INTO " + TO_LM_TABLE + " ("
            + "SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, MATCHED_TEXT_CLOB, "
            + "TARGET_LOCALE_ID, MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, "
            + "MATCHED_TABLE_TYPE, PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, "
            + "MATCHED_ORIGINAL_SOURCE, JOB_DATA_TU_ID, SID, CREATION_USER, CREATION_DATE, "
            + "MODIFY_USER, MODIFY_DATE) "
            + "SELECT lm.SOURCE_PAGE_ID, lm.ORIGINAL_SOURCE_TUV_ID, lm.SUB_ID, lm.MATCHED_TEXT_STRING, lm.MATCHED_TEXT_CLOB, "
            + "lm.TARGET_LOCALE_ID, lm.MATCH_TYPE, lm.ORDER_NUM, lm.SCORE_NUM, lm.MATCHED_TUV_ID, "
            + "lm.MATCHED_TABLE_TYPE, lm.PROJECT_TM_INDEX, lm.TM_ID, lm.TM_PROFILE_ID, lm.MT_NAME, "
            + "lm.MATCHED_ORIGINAL_SOURCE, lm.JOB_DATA_TU_ID, lm.SID, lm.CREATION_USER, lm.CREATION_DATE, "
            + "lm.MODIFY_USER, lm.MODIFY_DATE "
            + "FROM " + FROM_LM_TABLE + " lm "
            + "WHERE lm.source_page_id IN ";

    // Move TUV data to TUV archived table.
    private static final String TUV_ARCHIVED_INSERT =
            "REPLACE INTO " + TO_TUV_TABLE + " ("
            + "ID, ORDER_NUM, LOCALE_ID, TU_ID, IS_INDEXED, "
            + "SEGMENT_CLOB, SEGMENT_STRING, WORD_COUNT, EXACT_MATCH_KEY, STATE, "
            + "MERGE_STATE, TIMESTAMP, LAST_MODIFIED, MODIFY_USER, CREATION_DATE, "
            + "CREATION_USER, UPDATED_BY_PROJECT, SID, SRC_COMMENT, REPETITION_OF_ID, "
            + "IS_REPEATED) "
            + "SELECT tuv.ID, tuv.ORDER_NUM, tuv.LOCALE_ID, tuv.TU_ID, tuv.IS_INDEXED, "
            + "tuv.SEGMENT_CLOB, tuv.SEGMENT_STRING, tuv.WORD_COUNT, tuv.EXACT_MATCH_KEY, tuv.STATE, "
            + "tuv.MERGE_STATE, tuv.TIMESTAMP, tuv.LAST_MODIFIED, tuv.MODIFY_USER, tuv.CREATION_DATE, "
            + "tuv.CREATION_USER, tuv.UPDATED_BY_PROJECT, tuv.SID, tuv.SRC_COMMENT, tuv.REPETITION_OF_ID, "
            + "tuv.IS_REPEATED "
            + "FROM " + FROM_TUV_TABLE + " tuv "
            + "WHERE tuv.ID IN ";

    // Move TUV data to "translation_unit_archived" table.
    private static final String TU_ARCHIVED_INSERT =
            "REPLACE INTO " + TO_TU_TABLE + " ( "
            + "ID, ORDER_NUM, TM_ID, DATA_TYPE, TU_TYPE, "
            + "LOCALIZE_TYPE, LEVERAGE_GROUP_ID, PID, SOURCE_TM_NAME, XLIFF_TRANSLATION_TYPE, "
            + "XLIFF_LOCKED, IWS_SCORE, XLIFF_TARGET_SEGMENT, XLIFF_TARGET_LANGUAGE, GENERATE_FROM, "
            + "SOURCE_CONTENT, PASSOLO_STATE, TRANSLATE) "
            + "SELECT tu.ID, tu.ORDER_NUM, tu.TM_ID, tu.DATA_TYPE, tu.TU_TYPE, "
            + "tu.LOCALIZE_TYPE, tu.LEVERAGE_GROUP_ID, tu.PID, tu.SOURCE_TM_NAME, tu.XLIFF_TRANSLATION_TYPE, "
            + "tu.XLIFF_LOCKED, tu.IWS_SCORE, tu.XLIFF_TARGET_SEGMENT, tu.XLIFF_TARGET_LANGUAGE, tu.GENERATE_FROM, "
            + "tu.SOURCE_CONTENT, tu.PASSOLO_STATE, tu.TRANSLATE "
            + "FROM " + FROM_TU_TABLE + " tu "
            + "WHERE tu.id IN ";

    // Move TUV data to "template_part_archived" table.
    private static final String TEMPLATE_PART_ARCHIVED_INSERT =
            "REPLACE INTO TEMPLATE_PART_ARCHIVED ( "
            + "ID, TEMPLATE_ID, ORDER_NUM, SKELETON_CLOB, SKELETON_STRING, TU_ID) "
            + "SELECT part.ID, part.TEMPLATE_ID, part.ORDER_NUM, part.SKELETON_CLOB, part.SKELETON_STRING, part.TU_ID "
            + "FROM template_part part "
            + "WHERE part.ID IN ";

	private static HashSet<Long> archivingJobs = new HashSet<Long>();
	private static Object LOCK = new Object();

    /**
     * Move data of specified job in TU/TUV/LM/TEMPLATE_PART tables to their
     * cloned tables for performance purpose.
     * 
     * @param p_job
     * @throws SQLException 
     */
    public static void migrateJobData(Job p_job) throws SQLException
    {
        long jobId = p_job.getId();
        // Re-export "archived" job can turn back to "exported" state, but
        // migrated data will not be back.
    	synchronized(LOCK)
    	{
            if (p_job.isMigrated() || archivingJobs.contains(jobId))
            {
                return;
            }
            else
            {
                archivingJobs.add(jobId);
            }
    	}

        Connection connection = null;
        try
        {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(true);

            // check archive tables. Archive jobs do not support job level.
            BigTableUtil.checkTuTuvLmArchiveTablesForJob(jobId);
            // on old system, "template_part_archived" table may not exist. For
            // new system, when install, this table will be created.
            BigTableUtil.checkTemplatePartArchiveTable("TEMPLATE_PART_ARCHIVED");

            // migrate data
            migrateLeverageMatchExtData(connection, jobId);
            migrateLeverageMatchData(connection, jobId);
            migrateTuvData(connection, jobId);
            migrateTuData(connection, jobId);
            migrateTemplatePartData(connection, jobId);

            // update job migrated flag
            p_job.setIsMigrated(true);
            HibernateUtil.update(p_job);

            dropJobLevelTables(connection, p_job);
            logger.info("Job " + p_job.getJobId() + " is archived successfully.");
        }
        catch (Exception e)
        {
            logger.error("Fail to migrate job " + p_job.getId(), e);
            connection.rollback();
        }
        finally
        {
        	synchronized(LOCK)
        	{
                archivingJobs.remove(jobId);
        	}
            DbUtil.silentReturnConnection(connection);
        }
    }

    
    /**
	 * Move leverage match extension data to its archived table for specified
	 * job and remove them from original working table.
	 * 
	 * @param connection
	 * @param jobId
	 * @throws SQLException
	 */
    private static void migrateLeverageMatchExtData(Connection connection,
            long jobId) throws Exception
    {
        String columns = "SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM, LAST_USAGE_DATE, JOB_ID, JOB_NAME, PREVIOUS_HASH, NEXT_HASH, SID";
        try
        {
            Job job = BigTableUtil.getJobById(jobId);
			StringBuilder sql = new StringBuilder("REPLACE INTO ")
					.append(job.getLmExtArchiveTable())
					.append(" (").append(columns).append(") ")
					.append("SELECT ").append(columns)
					.append(" FROM ").append(job.getLmExtTable())
					.append(" WHERE SOURCE_PAGE_ID IN ");

            List<List<Object>> spIdList = queryBatchList(connection,
					"SELECT page_id FROM request req WHERE job_id = ? ", jobId,
					5);

            exec(connection, sql.toString(), spIdList);

			exec(connection, "DELETE FROM " + job.getLmExtTable()
					+ " WHERE source_page_id IN ", spIdList);
        }
        catch (SQLException sqlEx)
        {
			logger.error("Failed to migrate leverage match extension data for job "
					+ jobId);
            throw sqlEx;
        }
    }

    /**
     * Move leverage match data to its archived table for specified job and
     * remove them from original working table.
     * 
     * @param connection
     * @param jobId
     * @throws SQLException
     */
    private static void migrateLeverageMatchData(Connection connection,
            long jobId) throws Exception
    {
        try
        {
            Job job = BigTableUtil.getJobById(jobId);
            String fromLmTable = job.getLmTable();
            String toLmTable = job.getLmArchiveTable();

            List<List<Object>> spIdList = queryBatchList(connection,
					"SELECT page_id FROM request req WHERE job_id = ? ", jobId,
					5);

			String lmInsertSql = LM_ARCHIVED_INSERT.replace(FROM_LM_TABLE,
                    fromLmTable).replace(TO_LM_TABLE, toLmTable);
            exec(connection, lmInsertSql, spIdList);

			exec(connection, "DELETE FROM " + fromLmTable
					+ " WHERE source_page_id IN ", spIdList);
        }
        catch (SQLException sqlEx)
        {
            logger.error("Failed to migrate leverage match data for job "
                    + jobId);
            throw sqlEx;
        }
    }

    /**
     * Move TUV data to TUV archive table for specified job and remove them from
     * original working table.
     * 
     * @param connection
     * @param jobId
     * @throws SQLException
     */
    private static void migrateTuvData(Connection connection, long jobId)
            throws Exception
    {
        try
        {
            Job job = BigTableUtil.getJobById(jobId);
            String fromTuTable = job.getTuTable();
            String fromTuvTable = job.getTuvTable();
            String toTuvTable = job.getTuvArchiveTable();

            StringBuilder sql = new StringBuilder();
			sql.append("SELECT tuv.ID FROM ")
					.append(fromTuvTable)
					.append(" tuv, ")
					.append(fromTuTable)
					.append(" tu, source_page_leverage_group splg, request req ")
					.append(" WHERE tuv.TU_ID = tu.ID ")
					.append(" AND tu.leverage_group_id = splg.lg_id ")
					.append(" AND splg.sp_id = req.page_id ")
					.append(" AND req.job_id = ? ");
			List<List<Object>> tuvIdList = queryBatchList(connection,
					sql.toString(), jobId, BATCH_CAPACITY);

			String tuvInsertSql = TUV_ARCHIVED_INSERT.replace(TO_TUV_TABLE,
					toTuvTable).replace(FROM_TUV_TABLE, fromTuvTable);
            exec(connection, tuvInsertSql, tuvIdList);

			exec(connection, "DELETE FROM " + fromTuvTable + " WHERE ID IN ",
					tuvIdList);
        }
        catch (Exception ex)
        {
            logger.error("Failed to migrate TUV data for job " + jobId);
            throw ex;
        }
    }

    /**
     * Move TU data to "translation_unit_archived" table for specified job and
     * remove them from original working table.
     * 
     * @param connection
     * @param jobId
     * @throws SQLException
     */
    private static void migrateTuData(Connection connection, long jobId)
            throws Exception
    {
        try
        {
            Job job = BigTableUtil.getJobById(jobId);
            String from = job.getTuTable();
            String to = job.getTuArchiveTable();

            StringBuilder sql = new StringBuilder();
			sql.append("SELECT tu.ID FROM ")
					.append(from)
					.append(" tu, source_page_leverage_group splg, request req ")
					.append(" WHERE tu.leverage_group_id = splg.lg_id ")
					.append(" AND splg.sp_id = req.page_id ")
					.append(" AND req.job_id = ? ");
			List<List<Object>> tuIdList = queryBatchList(connection,
					sql.toString(), jobId, BATCH_CAPACITY);

			String tuInsertSql = TU_ARCHIVED_INSERT.replace(TO_TU_TABLE, to)
					.replace(FROM_TU_TABLE, from);
			exec(connection, tuInsertSql, tuIdList);

			exec(connection, "DELETE FROM " + from + " WHERE ID IN ", tuIdList);
        }
        catch (Exception sqlEx)
        {
            logger.error("Failed to migrate TU data for job " + jobId);
            throw sqlEx;
        }
    }

    /**
     * Move template part data to "template_part_archived" table for specified
     * job and remove them from original working table.
     * 
     * @param connection
     * @param companyId
     * @param jobId
     * @throws SQLException
     */
    private static void migrateTemplatePartData(Connection connection,
            long jobId) throws SQLException
    {
        try
        {
            StringBuilder sql = new StringBuilder();
			sql.append("SELECT part.ID ")
					.append(" FROM template_part part, template tem, request req ")
					.append(" WHERE part.TEMPLATE_ID = tem.ID ")
					.append(" AND tem.SOURCE_PAGE_ID = req.PAGE_ID ")
					.append(" AND req.JOB_ID = ? ");
			List<List<Object>> partIdList = queryBatchList(connection,
					sql.toString(), jobId, BATCH_CAPACITY);

			exec(connection, TEMPLATE_PART_ARCHIVED_INSERT, partIdList);

			exec(connection, "DELETE FROM template_part WHERE ID IN ",
					partIdList);
        }
        catch (SQLException sqlEx)
        {
            logger.error("Failed to migrate 'TEMPLATE_PART' data for job "
                    + jobId);
            throw sqlEx;
        }
    }

    /**
     * If current job is using separate tables per job, drop the 3 tables after
     * job is archived.
     * 
     * @param connection
     * @param p_job
     * @throws SQLException 
     */
    private static void dropJobLevelTables(Connection conn, Job p_job)
            throws SQLException
    {
        long companyId = p_job.getCompanyId();
        long jobId = p_job.getId();
        String suffix = "_" + companyId + "_" + jobId;
        String lmTable = "leverage_match" + suffix;
        String tuTable = "translation_unit" + suffix;
        String tuvTable = "translation_unit_variant" + suffix;

        if (lmTable.equalsIgnoreCase(p_job.getLmTable())
                && DbUtil.isTableExisted(lmTable))
        {
            execOnce(conn, SQL_DROP + lmTable);
        }
        if (tuvTable.equalsIgnoreCase(p_job.getTuvTable())
                && DbUtil.isTableExisted(tuvTable))
        {
            execOnce(conn, SQL_DROP + tuvTable);
        }
        if (tuTable.equalsIgnoreCase(p_job.getTuTable())
                && DbUtil.isTableExisted(tuTable))
        {
            execOnce(conn, SQL_DROP + tuTable);
        }
    }

	private static void exec(Connection conn, String sql,
			List<List<Object>> batchList) throws SQLException
    {
        int batchCount = batchList.size();
        if (batchCount > 1)
        {
			logger.info(batchCount + " batches of records found to be deleted.");
        }
        int deletedBatchCount = 0;
        for (List<Object> list : batchList)
        {
            execOnce(conn, sql + toInClause(list));
            if (batchCount > 1)
            {
                deletedBatchCount++;
                int leftBatchCount = batchCount - deletedBatchCount;
                String message = "";
                if (deletedBatchCount == 1)
                {
                    if (leftBatchCount == 1)
                    {
                        message = "1 batch deleted, left 1";
                    }
                    else
                    {
                        message = "1 batch deleted, left " + leftBatchCount;
                    }
                }
                else
                {
                    if (leftBatchCount == 1)
                    {
                        message = deletedBatchCount
                                + " batches deleted, left 1";
                    }
                    else if (leftBatchCount > 1)
                    {
                        message = deletedBatchCount + " batches deleted, left "
                                + leftBatchCount;
                    }
                }
                if (leftBatchCount > 0)
                {
                    logger.info(message);
                }
            }
        }
    }

    private static void execOnce(Connection conn, String sql, Object param)
            throws SQLException
    {
        try
        {
            execOnce(toPreparedStatement(conn, sql, param));
        }
        catch (Exception e)
        {
            logger.info("Current SQL :: " + sql);
            throw new SQLException(e.getMessage());
        }
    }

    private static void execOnce(Connection conn, String sql)
            throws SQLException
    {
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            stmt.execute(sql);
        }
        catch (Exception e)
        {
            logger.info("Current SQL :: " + sql);
            throw new SQLException(e.getMessage());
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
        }
    }

    private static void execOnce(PreparedStatement ps) throws SQLException
    {
        try
        {
            ps.execute();
        }
        catch (Exception e)
        {
            logger.error("Current SQL :: " + ps.toString());
            throw new SQLException(e.getMessage());
        }
        finally
        {
            ConnectionPool.silentClose(ps);
        }
    }

    private static List<List<Object>> queryBatchList(Connection conn,
			String sql, Object param, int batchSize) throws SQLException
    {
        Statement stmt = null;
        PreparedStatement ps = null;
        try
        {
            ResultSet rs = null;
            if (param instanceof List)
            {
                StringBuilder sb = new StringBuilder(sql);
                sb.append(toInClause((List<?>) param));

                stmt = toStatement(conn);

                rs = stmt.executeQuery(sb.toString());
            }
            else
            {
                ps = toPreparedStatement(conn, sql, param);
                rs = ps.executeQuery();
            }
            return toBatchList(rs, batchSize);
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentClose(ps);
        }
    }

    @SuppressWarnings("rawtypes")
    private static String toInClause(List<?> list)
    {
        StringBuilder in = new StringBuilder();
        if (list.size() == 0)
            return "(0)";
        
        in.append("(");
        for (Object o : list)
        {
            if (o instanceof List)
            {
                if (((List) o).size() == 0)
                    continue;
                
                for (Object id : (List<?>) o)
                {
                    if (id instanceof String)
                    {
                        in.append("'");
                        in.append(((String) id).replace("\'", "\\\'"));
                        in.append("'");
                    }
                    else
                    {
                        in.append(id);
                    }
                    in.append(",");
                }
            }
            else if (o instanceof String)
            {
                in.append("'");
                in.append(((String) o).replace("\'", "\\\'"));
                in.append("'");
                in.append(",");
            }
            else
            {
                in.append(o);
                in.append(",");
            }
        }
        in.deleteCharAt(in.length() - 1);
        in.append(")");

        return in.toString();
    }

    private static Statement toStatement(Connection conn) throws SQLException
    {
        Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        // will have oom error when querying great number of records without
        // this setting
        stmt.setFetchSize(Integer.MIN_VALUE);
        return stmt;
    }

    private static PreparedStatement toPreparedStatement(Connection conn,
            String sql, Object param) throws SQLException
    {
        PreparedStatement ps = conn.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        // will have oom error when querying great number of records without
        // this setting
        ps.setFetchSize(Integer.MIN_VALUE);
        ps.setObject(1, param);
        return ps;
    }

    private static List<List<Object>> toBatchList(ResultSet rs, int batchSize)
			throws SQLException
    {
        List<List<Object>> batchList = new ArrayList<List<Object>>();
        if (rs == null)
        {
            return batchList;
        }
        List<Object> subList = new ArrayList<Object>();
        int count = 0;
        try
        {
            while (rs.next())
            {
                subList.add(rs.getObject(1));
                count++;
                if (count == batchSize)
                {
                    batchList.add(subList);
                    subList = new ArrayList<Object>();
                    count = 0;
                }
            }
            if (subList.size() > 0)
            {
                batchList.add(subList);
            }
        }
        finally
        {
            ConnectionPool.silentClose(rs);
        }
        return batchList;
    }
}
