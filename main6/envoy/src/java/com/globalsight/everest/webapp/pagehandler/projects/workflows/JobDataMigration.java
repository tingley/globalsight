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

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class JobDataMigration
{
    private static final Logger logger = Logger.getLogger(JobDataMigration.class);

    // Move leverage match data to leverage match archived table.
    private static final String LM_ARCHIVED_INSERT =
            "INSERT INTO " + TuvQueryConstants.LM_TABLE_PLACEHOLDER + "_ARCHIVED ("
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
            + "FROM " + TuvQueryConstants.LM_TABLE_PLACEHOLDER + " lm, request req "
            + "WHERE lm.source_page_id = req.PAGE_ID "
            + "AND req.JOB_ID = ?";

    // Delete data from original leverage match table
    private static final String LM_DELETE =
            "DELETE lm FROM " + TuvQueryConstants.LM_TABLE_PLACEHOLDER + " lm, request req "
            + "WHERE lm.source_page_id = req.page_id "
            + "AND req.job_id = ?";

    // Move TUV data to TUV archived table.
    private static final String TUV_ARCHIVED_INSERT =
            "INSERT INTO " + TuvQueryConstants.TUV_TABLE_PLACEHOLDER + "_ARCHIVED ("
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
            + "FROM " + TuvQueryConstants.TUV_TABLE_PLACEHOLDER + " tuv, "
            + TuvQueryConstants.TU_TABLE_PLACEHOLDER + " tu, source_page_leverage_group splg, request req "
            + "WHERE tuv.TU_ID = tu.ID "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = req.page_id "
            + "AND req.job_id = ? ";

    // Delete data from original TUV table
    private static final String TUV_DELETE =
            "DELETE tuv FROM " + TuvQueryConstants.TUV_TABLE_PLACEHOLDER + " tuv, "
                    + TuvQueryConstants.TU_TABLE_PLACEHOLDER + " tu, "
                    + "source_page_leverage_group splg, request req "
                    + "WHERE tuv.TU_ID = tu.ID "
                    + "AND tu.leverage_group_id = splg.lg_id "
                    + "AND splg.sp_id = req.page_id "
                    + "AND req.job_id = ? ";

    // Move TUV data to "translation_unit_archived" table.
    private static final String TU_ARCHIVED_INSERT =
            "INSERT INTO " + TuvQueryConstants.TU_TABLE_PLACEHOLDER + "_ARCHIVED ( "
            + "ID, ORDER_NUM, TM_ID, DATA_TYPE, TU_TYPE, "
            + "LOCALIZE_TYPE, LEVERAGE_GROUP_ID, PID, SOURCE_TM_NAME, XLIFF_TRANSLATION_TYPE, "
            + "XLIFF_LOCKED, IWS_SCORE, XLIFF_TARGET_SEGMENT, XLIFF_TARGET_LANGUAGE, GENERATE_FROM, "
            + "SOURCE_CONTENT, PASSOLO_STATE, TRANSLATE) "
            + "SELECT tu.ID, tu.ORDER_NUM, tu.TM_ID, tu.DATA_TYPE, tu.TU_TYPE, "
            + "tu.LOCALIZE_TYPE, tu.LEVERAGE_GROUP_ID, tu.PID, tu.SOURCE_TM_NAME, tu.XLIFF_TRANSLATION_TYPE, "
            + "tu.XLIFF_LOCKED, tu.IWS_SCORE, tu.XLIFF_TARGET_SEGMENT, tu.XLIFF_TARGET_LANGUAGE, tu.GENERATE_FROM, "
            + "tu.SOURCE_CONTENT, tu.PASSOLO_STATE, tu.TRANSLATE "
            + "FROM " + TuvQueryConstants.TU_TABLE_PLACEHOLDER + " tu, source_page_leverage_group splg, request req "
            + "WHERE tu.leverage_group_id = splg.lg_id "
            + "AND splg.sp_id = req.page_id "
            + "AND req.job_id = ? ";

    // Delete data from original TU table
    private static final String TU_DELETE =
            "DELETE tu FROM " + TuvQueryConstants.TU_TABLE_PLACEHOLDER + " tu, "
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

            long companyId = p_job.getCompanyId();
            long jobId = p_job.getId();

            // check tables
            checkArchiveTables(connection, companyId);

            // migrate data
            migrateLeverageMatchData(connection, companyId, jobId);
            migrateTuvData(connection, companyId, jobId);
            migrateTuData(connection, companyId, jobId);
            migrateTemplatePartData(connection, jobId);

            // commit all
            connection.commit();

            // update job migrated flag
            p_job.setIsMigrated(true);
            HibernateUtil.update(p_job);
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
     * Check if archive tables exist, if not, create them.
     * 
     * @param companyId
     * @throws SQLException 
     */
    public static void checkArchiveTables(long companyId) throws SQLException
    {
        Connection connection = null;
        boolean autoCommit = false;
        try
        {
            connection = DbUtil.getConnection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            checkArchiveTables(connection, companyId);

            connection.commit();
        }
        catch (Exception e)
        {
            
        }
        finally
        {
            connection.setAutoCommit(autoCommit);
            DbUtil.silentReturnConnection(connection);
        }
    }

    /**
     * Check if archive tables exist, if not, create them.
     * 
     * @param connection
     * @param companyId
     */
    public static void checkArchiveTables(Connection connection, long companyId)
    {
        // migrate leverage match data
        String LMTableName = SegmentTuTuvCacheManager
                .getLeverageMatchArchiveTableName(companyId);
        if (!DbUtil.isTableExisted(connection, LMTableName))
        {
            createLeverageMatchArchiveTable(LMTableName);
        }

        // migrate TUV data
        String tuvTableName = SegmentTuTuvCacheManager
                .getTuvArchiveTableName(companyId);
        if (!DbUtil.isTableExisted(connection, tuvTableName))
        {
            createTuvArchiveTable(tuvTableName);
        }

        // migrate TU data
        String tuTableName = SegmentTuTuvCacheManager
                .getTuArchiveTableName(companyId);
        if (!DbUtil.isTableExisted(connection, tuTableName))
        {
            createTuArchiveTable(tuTableName);
        }

        // migrate template part data
        String templatePartTableName = SegmentTuTuvCacheManager
                .getTemplatePartArchiveTableName();
        if (!DbUtil.isTableExisted(connection, templatePartTableName))
        {
            createTemplatePartArchiveTable(templatePartTableName);
        }
    }

    /**
     * Create leverage match table with specified name.
     */
    private static void createLeverageMatchArchiveTable(String lmTableName)
    {
        String sql1 = "DROP TABLE IF EXISTS " + lmTableName + " CASCADE;";

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(lmTableName).append(" ");
        sb.append("(");
        sb.append(" SOURCE_PAGE_ID INT, ");
        sb.append(" ORIGINAL_SOURCE_TUV_ID BIGINT, ");
        sb.append(" SUB_ID VARCHAR(40), ");
        sb.append(" MATCHED_TEXT_STRING TEXT, ");
        sb.append(" MATCHED_TEXT_CLOB MEDIUMTEXT, ");
        sb.append(" TARGET_LOCALE_ID BIGINT, ");
        sb.append(" MATCH_TYPE VARCHAR(80), ");
        sb.append(" ORDER_NUM SMALLINT, ");
        sb.append(" SCORE_NUM DECIMAL(8, 4) DEFAULT 0.00, ");
        sb.append(" MATCHED_TUV_ID INT, ");
        sb.append(" MATCHED_TABLE_TYPE SMALLINT DEFAULT '0', ");
        sb.append(" PROJECT_TM_INDEX int(4) DEFAULT '-1', ");
        sb.append(" TM_ID bigint(20) DEFAULT '0', ");
        sb.append(" TM_PROFILE_ID bigint(20) DEFAULT '0', ");
        sb.append(" MT_NAME VARCHAR(40), ");
        sb.append(" MATCHED_ORIGINAL_SOURCE MEDIUMTEXT, ");
        sb.append(" JOB_DATA_TU_ID BIGINT DEFAULT '-1', ");
        sb.append(" SID VARCHAR(255) DEFAULT NULL, ");
        sb.append(" CREATION_USER VARCHAR(80) DEFAULT NULL, ");
        sb.append(" CREATION_DATE DATETIME NULL, ");
        sb.append(" MODIFY_USER VARCHAR(80) DEFAULT NULL, ");
        sb.append(" MODIFY_DATE DATETIME NULL, ");
        sb.append(" PRIMARY KEY (ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM) ");
        sb.append(");");
        String sql2 = sb.toString();

        String sql3 = "CREATE INDEX INDEX_ORIG_LEV_ORD ON " + lmTableName
                + " (ORIGINAL_SOURCE_TUV_ID);";
        String sql4 = "CREATE INDEX IDX_LM_ORDER_ORIGSOURCETUV ON "
                + lmTableName + " (ORDER_NUM, ORIGINAL_SOURCE_TUV_ID);";
        String sql5 = "CREATE INDEX IDX_LM_SRCPGID_TGTLOCID_ORDNUM ON "
                + lmTableName
                + " (SOURCE_PAGE_ID, TARGET_LOCALE_ID,ORDER_NUM);";
        String sql6 = "CREATE INDEX IDX_LM_ORIGSRCTUV_TGTLOCID ON "
                + lmTableName + " (ORIGINAL_SOURCE_TUV_ID, TARGET_LOCALE_ID);";

        try
        {
            HibernateUtil.executeSql(sql1);
            HibernateUtil.executeSql(sql2);
            HibernateUtil.executeSql(sql3);
            HibernateUtil.executeSql(sql4);
            HibernateUtil.executeSql(sql5);
            HibernateUtil.executeSql(sql6);
        }
        catch (Exception e)
        {
            logger.error("Failed to create LM table '" + lmTableName + "'.", e);
        }
    }

    /**
     * Create TUV archive table with specified name.
     */
    private static void createTuvArchiveTable(String tuvTableName)
    {
        String sql1 = "DROP TABLE IF EXISTS " + tuvTableName + " CASCADE;";

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(tuvTableName).append(" ");
        sb.append("(");
        sb.append(" ID BIGINT PRIMARY KEY,");
        sb.append(" ORDER_NUM BIGINT NOT NULL,");
        sb.append(" LOCALE_ID BIGINT NOT NULL,");
        sb.append(" TU_ID BIGINT NOT NULL,");
        sb.append(" IS_INDEXED CHAR(1) NOT NULL CHECK (IS_INDEXED IN ('Y', 'N')),");
        sb.append(" SEGMENT_CLOB MEDIUMTEXT,");
        sb.append(" SEGMENT_STRING TEXT,");
        sb.append(" WORD_COUNT INT(10),");
        sb.append(" EXACT_MATCH_KEY BIGINT,");
        sb.append(" STATE VARCHAR(40) NOT NULL ");
        sb.append("   CHECK (STATE IN ('NOT_LOCALIZED','LOCALIZED','OUT_OF_DATE',");
        sb.append("     'COMPLETE','LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED',");
        sb.append("     'EXACT_MATCH_LOCALIZED', 'ALIGNMENT_LOCALIZED',");
        sb.append("     'UNVERIFIED_EXACT_MATCH')),");
        sb.append(" MERGE_STATE VARCHAR(20) NOT NULL ");
        sb.append("   CHECK (MERGE_STATE IN ('NOT_MERGED','MERGE_START','MERGE_MIDDLE','MERGE_END')),");
        sb.append(" TIMESTAMP DATETIME NOT NULL,");
        sb.append(" LAST_MODIFIED DATETIME NOT NULL,");
        sb.append(" MODIFY_USER  VARCHAR(80),");
        sb.append(" CREATION_DATE  DATETIME,");
        sb.append(" CREATION_USER  VARCHAR(80),");
        sb.append(" UPDATED_BY_PROJECT VARCHAR(40),");
        sb.append(" SID VARCHAR(255),");
        sb.append(" SRC_COMMENT TEXT,");
        sb.append(" REPETITION_OF_ID BIGINT DEFAULT NULL,");
        sb.append(" IS_REPEATED CHAR(1) DEFAULT 'N' CHECK (IS_REPEATED IN ('Y', 'N')),");
        sb.append(" KEY `REPETITION_OF_ID` (`REPETITION_OF_ID`) ");
        sb.append(");");
        String sql2 = sb.toString();

        String sql3 = "CREATE INDEX INDEX_ID_LOCALE_STATE ON " + tuvTableName
                + "(ID, LOCALE_ID, STATE);";
        String sql4 = "CREATE INDEX INDEX_TU_LOC_STATE ON " + tuvTableName
                + "(TU_ID, LOCALE_ID, STATE);";
        String sql5 = "CREATE INDEX IDX_TUV_EMKEY_LOC_TU ON " + tuvTableName
                + "(EXACT_MATCH_KEY, LOCALE_ID, TU_ID);";
        String sql6 = "CREATE INDEX INDEX_TUV_TUID_STATE ON " + tuvTableName
                + "(TU_ID, STATE);";
        String sql7 = "CREATE UNIQUE INDEX IDX_TUV_ID_TU ON " + tuvTableName
                + "(ID, TU_ID);";
        String sql8 = "CREATE INDEX IDX_TUV_LOC_TU_ORDER_ID ON " + tuvTableName
                + "(LOCALE_ID, TU_ID, ORDER_NUM, ID);";

        try
        {
            HibernateUtil.executeSql(sql1);
            HibernateUtil.executeSql(sql2);
            HibernateUtil.executeSql(sql3);
            HibernateUtil.executeSql(sql4);
            HibernateUtil.executeSql(sql5);
            HibernateUtil.executeSql(sql6);
            HibernateUtil.executeSql(sql7);
            HibernateUtil.executeSql(sql8);
        }
        catch (Exception e)
        {
            logger.error("Failed to create TUV table '" + tuvTableName + "'.", e);
        }
    }

    /**
     * Create TU archive table with specified name.
     * 
     * @param p_companyId
     */
    private static void createTuArchiveTable(String tuTableName)
    {
        String sql1 = "DROP TABLE IF EXISTS " + tuTableName + " CASCADE;";

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(tuTableName).append(" ");
        sb.append("(");
        sb.append(" ID BIGINT PRIMARY KEY,");
        sb.append(" ORDER_NUM INT NOT NULL,");
        sb.append(" TM_ID INT,");
        sb.append(" DATA_TYPE VARCHAR(20),");
        sb.append(" TU_TYPE VARCHAR(50),");
        sb.append(" LOCALIZE_TYPE CHAR(1) NOT NULL CHECK (LOCALIZE_TYPE IN ('L','T')),");
        sb.append(" LEVERAGE_GROUP_ID BIGINT NOT NULL,");
        sb.append(" PID INT NOT NULL,");
        sb.append(" SOURCE_TM_NAME VARCHAR(60),");
        sb.append(" XLIFF_TRANSLATION_TYPE VARCHAR(60),");
        sb.append(" XLIFF_LOCKED CHAR(1) NOT NULL DEFAULT 'N' CHECK (XLIFF_LOCKED IN ('Y', 'N')),");
        sb.append(" IWS_SCORE VARCHAR(50),");
        sb.append(" XLIFF_TARGET_SEGMENT MEDIUMTEXT,");
        sb.append(" XLIFF_TARGET_LANGUAGE varchar(30) DEFAULT NULL,");
        sb.append(" GENERATE_FROM varchar(50) DEFAULT NULL,");
        sb.append(" SOURCE_CONTENT varchar(30) DEFAULT NULL,");
        sb.append(" PASSOLO_STATE varchar(60) DEFAULT NULL,");
        sb.append(" TRANSLATE varchar(12) DEFAULT NULL");
        sb.append(");");
        String sql2 = sb.toString();

        String sql3 = "CREATE INDEX INDEX_ID_LG ON " + tuTableName
                + "(ID, LEVERAGE_GROUP_ID);";
        String sql4 = "CREATE INDEX IDX_TU_LG_ID_ORDER ON " + tuTableName
                + "(LEVERAGE_GROUP_ID, ID, ORDER_NUM);";
        String sql5 = "CREATE INDEX INDEX_IDLT_TU_TM ON " + tuTableName
                + "(ID, LOCALIZE_TYPE, TU_TYPE, TM_ID);";
        String sql6 = "CREATE INDEX IDX_TU_TYPE_ID ON " + tuTableName
                + "(TU_TYPE, ID);";

        try
        {
            HibernateUtil.executeSql(sql1);
            HibernateUtil.executeSql(sql2);
            HibernateUtil.executeSql(sql3);
            HibernateUtil.executeSql(sql4);
            HibernateUtil.executeSql(sql5);
            HibernateUtil.executeSql(sql6);
        }
        catch (Exception e)
        {
            logger.error("Failed to create TU table '" + tuTableName + "'.", e);
        }
    }

    private static void createTemplatePartArchiveTable(
            String templatePartTableName)
    {
        String sql1 = "DROP TABLE IF EXISTS " + templatePartTableName + " CASCADE;";

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(templatePartTableName).append(" ");
        sb.append("(");
        sb.append(" ID BIGINT PRIMARY KEY,");
        sb.append(" TEMPLATE_ID BIGINT NOT NULL,");
        sb.append(" ORDER_NUM INT NOT NULL,");
        sb.append(" SKELETON_CLOB LONGTEXT,");
        sb.append(" SKELETON_STRING VARCHAR(4000),");
        sb.append(" TU_ID BIGINT DEFAULT 0");
        sb.append(");");
        String sql2 = sb.toString();

        String sql3 = "CREATE INDEX IDX_TEMPLATE_PART_ARCHIVED_TEMPID ON "
                + templatePartTableName + "(TEMPLATE_ID);";
        
        try
        {
            HibernateUtil.executeSql(sql1);
            HibernateUtil.executeSql(sql2);
            HibernateUtil.executeSql(sql3);
        }
        catch (Exception e)
        {
            logger.error("Failed to create template part archive table '"
                    + templatePartTableName + "'.", e);
        }
    }
    
    /**
     * Move leverage match data to its archived table for specified job and
     * remove them from original working table.
     * 
     * @param connection
     * @param companyId
     * @param jobId
     * @throws SQLException
     */
    private static void migrateLeverageMatchData(Connection connection,
            long companyId, long jobId) throws SQLException
    {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try
        {
            String lmTableName = SegmentTuTuvCacheManager
                    .getLeverageMatchWorkingTableName(companyId);
            String lmInsertSql = LM_ARCHIVED_INSERT.replace(
                    TuvQueryConstants.LM_TABLE_PLACEHOLDER, lmTableName);
            String lmDeleteSql = LM_DELETE.replace(
                    TuvQueryConstants.LM_TABLE_PLACEHOLDER, lmTableName);

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
     * Move TUV data to "translation_unit_variant_archived" table for specified
     * job and remove them from original working table.
     * 
     * @param connection
     * @param companyId
     * @param jobId
     * @throws SQLException
     */
    private static void migrateTuvData(Connection connection, long companyId,
            long jobId) throws SQLException
    {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try
        {
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuWorkingTableName(companyId);
            String tuvTableName = SegmentTuTuvCacheManager
                    .getTuvWorkingTableName(companyId);

            String tuvInsertSql = TUV_ARCHIVED_INSERT.replace(
                    TuvQueryConstants.TUV_TABLE_PLACEHOLDER, tuvTableName)
                    .replace(TuvQueryConstants.TU_TABLE_PLACEHOLDER,
                            tuTableName);
            String tuvDeleteSql = TUV_DELETE.replace(
                    TuvQueryConstants.TUV_TABLE_PLACEHOLDER, tuvTableName)
                    .replace(TuvQueryConstants.TU_TABLE_PLACEHOLDER,
                            tuTableName);

            ps1 = connection.prepareStatement(tuvInsertSql);
            ps2 = connection.prepareStatement(tuvDeleteSql);

            ps1.setLong(1, jobId);
            ps2.setLong(1, jobId);

            ps1.execute();
            ps2.execute();
        }
        catch (SQLException sqlEx)
        {
            logger.error("Failed to migrate TUV data for job " + jobId);
            throw sqlEx;
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
     * @param companyId
     * @param jobId
     * @throws SQLException
     */
    private static void migrateTuData(Connection connection, long companyId,
            long jobId) throws SQLException
    {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try
        {
            String tuTableName = SegmentTuTuvCacheManager
                    .getTuWorkingTableName(companyId);

            String tuInsertSql = TU_ARCHIVED_INSERT.replace(
                    TuvQueryConstants.TU_TABLE_PLACEHOLDER, tuTableName);
            String tuDeleteSql = TU_DELETE.replace(
                    TuvQueryConstants.TU_TABLE_PLACEHOLDER, tuTableName);

            ps1 = connection.prepareStatement(tuInsertSql);
            ps2 = connection.prepareStatement(tuDeleteSql);

            ps1.setLong(1, jobId);
            ps2.setLong(1, jobId);

            ps1.execute();
            ps2.execute();
        }
        catch (SQLException sqlEx)
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
}
