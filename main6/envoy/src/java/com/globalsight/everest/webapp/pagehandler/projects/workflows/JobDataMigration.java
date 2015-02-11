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
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class JobDataMigration
{
    private static final Logger logger = Logger.getLogger(JobDataMigration.class);

    private static final String SQL_DROP = "drop table if exists ";

    private static final String FROM_TU_TABLE = "\uE000" + "_FROM_TU_TABLE_" + "\uE000";
    private static final String TO_TU_TABLE = "\uE000" + "_TO_TU_TABLE_" + "\uE000";

    private static final String FROM_TUV_TABLE = "\uE000" + "_FROM_TUV_TABLE_" + "\uE000";
    private static final String TO_TUV_TABLE = "\uE000" + "_TO_TUV_TABLE_" + "\uE000";

    private static final String FROM_LM_TABLE = "\uE000" + "_FROM_LM_TABLE_" + "\uE000";
    private static final String TO_LM_TABLE = "\uE000" + "_TO_LM_TABLE_" + "\uE000";

    // Move leverage match data to leverage match archived table.
    private static final String LM_ARCHIVED_INSERT =
            "INSERT INTO " + TO_LM_TABLE + " ("
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
            + "FROM " + FROM_LM_TABLE + " lm, request req "
            + "WHERE lm.source_page_id = req.PAGE_ID "
            + "AND req.JOB_ID = ?";

    // Delete data from original leverage match table
    private static final String LM_DELETE =
            "DELETE lm FROM " + FROM_LM_TABLE + " lm, request req "
            + "WHERE lm.source_page_id = req.page_id "
            + "AND req.job_id = ?";

    // Move TUV data to TUV archived table.
    private static final String TUV_ARCHIVED_INSERT =
            "INSERT INTO " + TO_TUV_TABLE + " ("
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
            + "FROM " + FROM_TUV_TABLE + " tuv, " + FROM_TU_TABLE + " tu, source_page_leverage_group splg, request req "
            + "WHERE tuv.TU_ID = tu.ID "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = req.page_id "
            + "AND req.job_id = ? ";

    // Delete data from original TUV table
    private static final String TUV_DELETE =
            "DELETE tuv FROM " + FROM_TUV_TABLE + " tuv, "
            + FROM_TU_TABLE + " tu, "
            + "source_page_leverage_group splg, request req "
            + "WHERE tuv.TU_ID = tu.ID "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = req.page_id "
            + "AND req.job_id = ? ";

    // Move TUV data to "translation_unit_archived" table.
    private static final String TU_ARCHIVED_INSERT =
            "INSERT INTO " + TO_TU_TABLE + " ( "
            + "ID, ORDER_NUM, TM_ID, DATA_TYPE, TU_TYPE, "
            + "LOCALIZE_TYPE, LEVERAGE_GROUP_ID, PID, SOURCE_TM_NAME, XLIFF_TRANSLATION_TYPE, "
            + "XLIFF_LOCKED, IWS_SCORE, XLIFF_TARGET_SEGMENT, XLIFF_TARGET_LANGUAGE, GENERATE_FROM, "
            + "SOURCE_CONTENT, PASSOLO_STATE, TRANSLATE) "
            + "SELECT tu.ID, tu.ORDER_NUM, tu.TM_ID, tu.DATA_TYPE, tu.TU_TYPE, "
            + "tu.LOCALIZE_TYPE, tu.LEVERAGE_GROUP_ID, tu.PID, tu.SOURCE_TM_NAME, tu.XLIFF_TRANSLATION_TYPE, "
            + "tu.XLIFF_LOCKED, tu.IWS_SCORE, tu.XLIFF_TARGET_SEGMENT, tu.XLIFF_TARGET_LANGUAGE, tu.GENERATE_FROM, "
            + "tu.SOURCE_CONTENT, tu.PASSOLO_STATE, tu.TRANSLATE "
            + "FROM " + FROM_TU_TABLE + " tu, source_page_leverage_group splg, request req "
            + "WHERE tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = req.page_id "
            + "AND req.job_id = ? ";

    // Delete data from original TU table
    private static final String TU_DELETE =
            "DELETE tu FROM " + FROM_TU_TABLE + " tu, "
            + "source_page_leverage_group splg, request req "
            + "WHERE tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = req.page_id "
            + "AND req.job_id = ? ";

    // Move TUV data to "template_part_archived" table.
    private static final String TEMPLATE_PART_ARCHIVED_INSERT =
            "INSERT INTO TEMPLATE_PART_ARCHIVED ( "
            + "ID, TEMPLATE_ID, ORDER_NUM, SKELETON_CLOB, SKELETON_STRING, TU_ID) "
            + "SELECT part.ID, part.TEMPLATE_ID, part.ORDER_NUM, part.SKELETON_CLOB, part.SKELETON_STRING, part.TU_ID "
            + "FROM template_part part, template tem, request req "
            + "WHERE part.TEMPLATE_ID = tem.ID "
            + "AND tem.SOURCE_PAGE_ID = req.PAGE_ID "
            + "AND req.JOB_ID = ? ";

    // Delete data from original table "template_part"
    private static final String TEMPLATE_PART_DELETE =
            "DELETE part FROM template_part part, template tem, request req "
            + "WHERE part.TEMPLATE_ID = tem.ID "
            + "AND tem.SOURCE_PAGE_ID = req.PAGE_ID "
            + "AND req.JOB_ID = ? ";
    /**
     * Move data of specified job in TU/TUV/LM/TEMPLATE_PART tables to their
     * cloned tables for performance purpose.
     * 
     * @param p_job
     * @throws SQLException 
     */
    public static void migrateJobData(Job p_job) throws SQLException
    {
        // Re-export "archived" job can turn back to "exported" state, but
        // migrated data will not be back.
        if (p_job.isMigrated())
        {
            return;
        }

        Connection connection = null;
        boolean autoCommit = false;

        try
        {
            connection = DbUtil.getConnection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            long jobId = p_job.getId();

            // check archive tables. Archive jobs do not support job level.
            BigTableUtil.checkTuTuvLmArchiveTablesForJob(jobId);
            // on old system, "template_part_archived" table may not exist. For
            // new system, when install, this table will be created.
            BigTableUtil.checkTemplatePartArchiveTable("TEMPLATE_PART_ARCHIVED");

            // migrate data
            migrateLeverageMatchData(connection, jobId);
            migrateTuvData(connection, jobId);
            migrateTuData(connection, jobId);
            migrateTemplatePartData(connection, jobId);

            // commit all
            connection.commit();

            // update job migrated flag
            p_job.setIsMigrated(true);
            HibernateUtil.update(p_job);

            dropJobLevelTables(connection, p_job);
            connection.commit();
        }
        catch (Exception e)
        {
            logger.error("Fail to migrate job " + p_job.getId(), e);
            connection.rollback();
        }
        finally
        {
            connection.setAutoCommit(autoCommit);
            DbUtil.silentReturnConnection(connection);
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
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try
        {
            Job job = BigTableUtil.getJobById(jobId);

            String fromLmTable = job.getLmTable();
            String toLmTable = job.getLmArchiveTable();

            String lmInsertSql = LM_ARCHIVED_INSERT.replace(FROM_LM_TABLE,
                    fromLmTable).replace(TO_LM_TABLE, toLmTable);
            String lmDeleteSql = LM_DELETE.replace(FROM_LM_TABLE, fromLmTable);

            ps1 = connection.prepareStatement(lmInsertSql);
            ps2 = connection.prepareStatement(lmDeleteSql);

            ps1.setLong(1, jobId);
            ps2.setLong(1, jobId);

            ps1.execute();
            ps2.execute();
        }
        catch (SQLException sqlEx)
        {
            logger.error("Failed to migrate leverage match data for job "
                    + jobId);
            throw sqlEx;
        }
        finally
        {
            DbUtil.silentClose(ps1);
            DbUtil.silentClose(ps2);
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
    private static void migrateTuvData(Connection connection, long p_jobId)
            throws Exception
    {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try
        {
            Job job = BigTableUtil.getJobById(p_jobId);
            String fromTuTable = job.getTuTable();
            String fromTuvTable = job.getTuvTable();
            String toTuvTable = job.getTuvArchiveTable();

            String tuvInsertSql = TUV_ARCHIVED_INSERT
                    .replace(TO_TUV_TABLE, toTuvTable)
                    .replace(FROM_TU_TABLE, fromTuTable)
                    .replace(FROM_TUV_TABLE, fromTuvTable);

            String tuvDeleteSql = TUV_DELETE.replace(FROM_TUV_TABLE,
                    fromTuvTable).replace(FROM_TU_TABLE, fromTuTable);

            ps1 = connection.prepareStatement(tuvInsertSql);
            ps2 = connection.prepareStatement(tuvDeleteSql);

            ps1.setLong(1, p_jobId);
            ps2.setLong(1, p_jobId);

            ps1.execute();
            ps2.execute();
        }
        catch (Exception ex)
        {
            logger.error("Failed to migrate TUV data for job " + p_jobId);
            throw ex;
        }
        finally
        {
            DbUtil.silentClose(ps1);
            DbUtil.silentClose(ps2);
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
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try
        {
            Job job = BigTableUtil.getJobById(jobId);
            String from = job.getTuTable();
            String to = job.getTuArchiveTable();

            String tuInsertSql = TU_ARCHIVED_INSERT.replace(TO_TU_TABLE, to)
                    .replace(FROM_TU_TABLE, from);
            String tuDeleteSql = TU_DELETE.replace(FROM_TU_TABLE, from);

            ps1 = connection.prepareStatement(tuInsertSql);
            ps2 = connection.prepareStatement(tuDeleteSql);

            ps1.setLong(1, jobId);
            ps2.setLong(1, jobId);

            ps1.execute();
            ps2.execute();
        }
        catch (Exception sqlEx)
        {
            logger.error("Failed to migrate TU data for job " + jobId);
            throw sqlEx;
        }
        finally
        {
            DbUtil.silentClose(ps1);
            DbUtil.silentClose(ps2);
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
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try
        {
            ps1 = connection.prepareStatement(TEMPLATE_PART_ARCHIVED_INSERT);
            ps2 = connection.prepareStatement(TEMPLATE_PART_DELETE);

            ps1.setLong(1, jobId);
            ps2.setLong(1, jobId);

            ps1.execute();
            ps2.execute();
        }
        catch (SQLException sqlEx)
        {
            logger.error("Failed to migrate 'TEMPLATE_PART' data for job "
                    + jobId);
            throw sqlEx;
        }
        finally
        {
            DbUtil.silentClose(ps1);
            DbUtil.silentClose(ps2);
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
}
