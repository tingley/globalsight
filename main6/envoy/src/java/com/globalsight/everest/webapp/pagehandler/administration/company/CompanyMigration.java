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
package com.globalsight.everest.webapp.pagehandler.administration.company;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class CompanyMigration
{
    private static final Logger logger = Logger
            .getLogger(CompanyMigration.class.getName());

    // <Long, String> : <companyId, "migrating" || "stopped">.
    // Generally, only one company is allowed to migrate at the same time for
    // performance.
    // If the migration is finished, it should be removed.
    // If the migration is stopped for error or restart, its status should be
    // updated to "stopped".
    private static Map<Long, String> migratingCompanyStatus = new HashMap<Long, String>(
            1);
    public static final String MIGRATING = "migrating";
    public static final String STOPPED = "stopped";
    private static final String FINISHED = "finished";

    private static final String SQL_QUERY_TABLE = "show tables like ";

    private static final String GET_SP_COUNT_FOR_COMPANY = "SELECT COUNT(sp.id) "
            + "FROM source_page sp, source_page_leverage_group sp_lg "
            + "WHERE sp_lg.sp_id = sp.id " + "AND sp.company_id = ?";

    private static final String GET_SP_ID_LG_ID_FOR_COMPANY = "SELECT sp.id, sp_lg.lg_id "
            + "FROM source_page sp, source_page_leverage_group sp_lg "
            + "WHERE sp_lg.sp_id = sp.id " + "AND sp.company_id = ?";

    private static final String INSERT_LM_BY_SP_ID = "INSERT INTO "
            + TuvQueryConstants.LM_TABLE_PLACEHOLDER
            + " ("
            + "SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, MATCHED_TEXT_CLOB, "
            + "TARGET_LOCALE_ID, MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, "
            + "MATCHED_TABLE_TYPE, PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, "
            + "MATCHED_ORIGINAL_SOURCE, JOB_DATA_TU_ID) "
            + "SELECT SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, MATCHED_TEXT_CLOB, "
            + "TARGET_LOCALE_ID, MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, "
            + "MATCHED_TABLE_TYPE, PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, "
            + "MATCHED_ORIGINAL_SOURCE, JOB_DATA_TU_ID "
            + "FROM LEVERAGE_MATCH " + "WHERE SOURCE_PAGE_ID = ?";

    private static final String DELETE_LM_BY_SP_ID = "DELETE FROM LEVERAGE_MATCH WHERE SOURCE_PAGE_ID = ?";

    private static final String INSERT_TU_BY_LG_ID = "INSERT INTO "
            + TuvQueryConstants.TU_TABLE_PLACEHOLDER
            + " "
            + "(ID, ORDER_NUM, TM_ID, DATA_TYPE, TU_TYPE, "
            + "LOCALIZE_TYPE, LEVERAGE_GROUP_ID, PID, SOURCE_TM_NAME, XLIFF_TRANSLATION_TYPE, "
            + "XLIFF_LOCKED, IWS_SCORE, XLIFF_TARGET_SEGMENT, XLIFF_TARGET_LANGUAGE, GENERATE_FROM, "
            + "SOURCE_CONTENT, PASSOLO_STATE, TRANSLATE, REPETITION_OF_ID, IS_REPEATED ) "
            + "SELECT ID, ORDER_NUM, TM_ID, DATA_TYPE, TU_TYPE, "
            + "LOCALIZE_TYPE, LEVERAGE_GROUP_ID, PID, SOURCE_TM_NAME, XLIFF_TRANSLATION_TYPE, "
            + "XLIFF_LOCKED, IWS_SCORE, XLIFF_TARGET_SEGMENT, XLIFF_TARGET_LANGUAGE, GENERATE_FROM, "
            + "SOURCE_CONTENT, PASSOLO_STATE, TRANSLATE, REPETITION_OF_ID, IS_REPEATED "
            + "FROM translation_unit " + "WHERE leverage_group_id = ?";

    private static final String INSERT_TUV_BY_LG_ID = "INSERT INTO "
            + TuvQueryConstants.TUV_TABLE_PLACEHOLDER
            + " "
            + "(ID, ORDER_NUM, LOCALE_ID, TU_ID, IS_INDEXED, "
            + "SEGMENT_CLOB, SEGMENT_STRING, WORD_COUNT, EXACT_MATCH_KEY, STATE, "
            + "MERGE_STATE, TIMESTAMP, LAST_MODIFIED, MODIFY_USER, CREATION_DATE, "
            + "CREATION_USER, UPDATED_BY_PROJECT, SID, SRC_COMMENT ) "
            + "SELECT tuv.ID, tuv.ORDER_NUM, tuv.LOCALE_ID, tuv.TU_ID, tuv.IS_INDEXED, "
            + "tuv.SEGMENT_CLOB, tuv.SEGMENT_STRING, tuv.WORD_COUNT, tuv.EXACT_MATCH_KEY, tuv.STATE, "
            + "tuv.MERGE_STATE, tuv.TIMESTAMP, tuv.LAST_MODIFIED, tuv.MODIFY_USER, tuv.CREATION_DATE, "
            + "tuv.CREATION_USER, tuv.UPDATED_BY_PROJECT, tuv.SID, tuv.SRC_COMMENT "
            + "FROM translation_unit_variant tuv, translation_unit tu "
            + "WHERE tu.ID = tuv.TU_ID " + "AND tu.leverage_group_id = ?";

    /**
     * Check if the corresponding table is existed,if not, create it.
     * 
     * @param p_companyId
     */
    public static void checkLeverageMatchTable(long p_companyId)
    {
        String table = "leverage_match_" + p_companyId;
        Session session = TmUtil.getStableSession();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = SQL_QUERY_TABLE + "'" + table + "'";
            ps = session.connection().prepareStatement(sql);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                createLMTableForCompany(p_companyId);
            }
        }
        catch (Exception e)
        {
            // Probably this table does not exist at all, create now.
            createLMTableForCompany(p_companyId);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            if (session != null)
            {
                TmUtil.closeStableSession(session);
            }
        }
    }

    /**
     * Check if the corresponding table is existed,if not, create it.
     * 
     * @param p_companyId
     */
    public static void checkTuTable(long p_companyId)
    {
        String table = "translation_unit_" + p_companyId;
        Session session = TmUtil.getStableSession();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = SQL_QUERY_TABLE + "'" + table + "'";
            ps = session.connection().prepareStatement(sql);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                createTuTableForCompany(p_companyId);
            }
        }
        catch (Exception e)
        {
            // Probably this table does not exist at all, create now.
            createTuTableForCompany(p_companyId);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            if (session != null)
            {
                TmUtil.closeStableSession(session);
            }
        }
    }

    /**
     * Check if the corresponding table is existed,if not, create it.
     * 
     * @param p_companyId
     */
    public static void checkTuvTable(long p_companyId)
    {
        String table = "translation_unit_variant_" + p_companyId;
        Session session = TmUtil.getStableSession();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = SQL_QUERY_TABLE + "'" + table + "'";
            ps = session.connection().prepareStatement(sql);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                createTuvTableForCompany(p_companyId);
            }
        }
        catch (Exception e)
        {
            // Probably this table does not exist at all, create now.
            createTuvTableForCompany(p_companyId);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            if (session != null)
            {
                TmUtil.closeStableSession(session);
            }
        }
    }

    /**
     * Every company has its own leverage match table named
     * "leverage_match_[companyId]".
     * 
     * @param p_companyId
     */
    public static void createLMTableForCompany(long p_companyId)
    {
        String lmTableName = "leverage_match_" + p_companyId;

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
            logger.error("Failed to create table " + lmTableName
                    + " for current company " + p_companyId, e);
        }
    }

    /**
     * Every company has its own TU table like "translation_unit_[companyId]".
     * 
     * @param p_companyId
     */
    public static void createTuTableForCompany(long p_companyId)
    {
        String tuTableName = "translation_unit_" + p_companyId;

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
        sb.append(" TRANSLATE varchar(12) DEFAULT NULL,");
        sb.append(" REPETITION_OF_ID BIGINT DEFAULT NULL,");
        sb.append(" IS_REPEATED CHAR(1) DEFAULT 'N' CHECK (IS_REPEATED IN ('Y', 'N')),");
        sb.append(" KEY `REPETITION_OF_ID` (`REPETITION_OF_ID`)");
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
            logger.error("Failed to create table " + tuTableName
                    + " for current company " + p_companyId, e);
        }
    }

    /**
     * Every company has its own TUV table like
     * "translation_unit_variant_[companyId]".
     * 
     * @param p_companyId
     */
    public static void createTuvTableForCompany(long p_companyId)
    {
        String tuvTableName = "translation_unit_variant_" + p_companyId;

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
        sb.append(" SRC_COMMENT TEXT ");
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
            logger.error("Failed to create table " + tuvTableName
                    + " for current company " + p_companyId, e);
        }
    }

    public static void migrateToSeparatedTables(final long p_companyId)
            throws Exception
    {
        Company company = ServerProxy.getJobHandler().getCompanyById(
                p_companyId);

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Company company = null;

                Connection connection = null;
                PreparedStatement ps1 = null;
                ResultSet rs1 = null;
                PreparedStatement ps2 = null;
                ResultSet rs2 = null;
                PreparedStatement ps3 = null;
                ResultSet rs3 = null;
                PreparedStatement ps4 = null;
                PreparedStatement ps5 = null;
                PreparedStatement ps6 = null;
                PreparedStatement ps7 = null;

                int totalSpIdNumber = 0;
                int finishedSpIdCount = 0;
                int count = 0;
                try
                {
                    company = ServerProxy.getJobHandler().getCompanyById(
                            p_companyId);
                    connection = DbUtil.getConnection();
                    // Get all lgIds that have been handled
                    String sql1 = "select distinct(leverage_group_id) from translation_unit_"
                            + p_companyId;
                    ps1 = connection.prepareStatement(sql1);
                    rs1 = ps1.executeQuery();
                    Set<Long> migratedLgIds = new HashSet<Long>();
                    while (rs1.next())
                    {
                        Long lgId = rs1.getLong(1);
                        migratedLgIds.add(lgId);
                    }
                    finishedSpIdCount += migratedLgIds.size();

                    // Get the total source page number that need to be handled.
                    ps2 = connection.prepareStatement(GET_SP_COUNT_FOR_COMPANY);
                    ps2.setLong(1, p_companyId);
                    rs2 = ps2.executeQuery();
                    if (rs2.next())
                    {
                        totalSpIdNumber = rs2.getInt(1);
                        logger.info("The total number of source pages that need migrate is "
                                + totalSpIdNumber);
                    }
                    if (finishedSpIdCount > 0)
                    {
                        logger.info(finishedSpIdCount
                                + " source pages have finished migration in previous migration.");
                    }

                    // Loop all the spIds and lgIds in current company.
                    ps3 = connection
                            .prepareStatement(GET_SP_ID_LG_ID_FOR_COMPANY);
                    ps3.setLong(1, p_companyId);
                    rs3 = ps3.executeQuery();

                    while (rs3.next())
                    {
                        long spId = rs3.getLong(1);
                        long lgId = rs3.getLong(2);
                        if (!migratedLgIds.contains(lgId))
                        {
                            connection.setAutoCommit(false);

                            // Insert LM data to "leverage_match_[companyId]"
                            if (ps4 == null)
                            {
                                String sql4 = INSERT_LM_BY_SP_ID.replace(
                                        TuvQueryConstants.LM_TABLE_PLACEHOLDER,
                                        "leverage_match_" + p_companyId);
                                ps4 = connection.prepareStatement(sql4);
                            }
                            ps4.setLong(1, spId);
                            ps4.execute();

                            // Delete LM data from "leverage_match".
                            // if (ps5 == null){
                            // ps5 =
                            // connection.prepareStatement(DELETE_LM_BY_SP_ID);
                            // }
                            // ps5.setLong(1, spId);
                            // ps5.execute();

                            // Insert TU data into
                            // "translation_unit_[companyId]"
                            if (ps6 == null)
                            {
                                String sql6 = INSERT_TU_BY_LG_ID.replace(
                                        TuvQueryConstants.TU_TABLE_PLACEHOLDER,
                                        "translation_unit_" + p_companyId);
                                ps6 = connection.prepareStatement(sql6);
                            }
                            ps6.setLong(1, lgId);
                            ps6.execute();

                            // Insert TUV data into
                            // "translation_unit_variant_[companyId]".
                            if (ps7 == null)
                            {
                                String sql7 = INSERT_TUV_BY_LG_ID
                                        .replace(
                                                TuvQueryConstants.TUV_TABLE_PLACEHOLDER,
                                                "translation_unit_variant_"
                                                        + p_companyId);
                                ps7 = connection.prepareStatement(sql7);
                            }
                            ps7.setLong(1, lgId);
                            ps7.execute();

                            connection.commit();
                            count++;
                            finishedSpIdCount++;
                            logger.info("The number of source pages that have finished migration is "
                                    + finishedSpIdCount + ", Sp_Id is " + spId);
                            if (count == 3)
                            {
                                int processing = (finishedSpIdCount * 100)
                                        / totalSpIdNumber;
                                company.setMigrateProcessing(processing);
                                HibernateUtil.update(company);
                                count = 0;
                            }
                        }
                    }

                    logger.info("Company migration is finished completely.");
                    company.setMigrateProcessing(100);
                    company.setSeparateTmTuTuvTables(1);
                    HibernateUtil.update(company);

                    migratingCompanyStatus.remove(p_companyId);
                }
                catch (Exception ex)
                {
                    migratingCompanyStatus.put(p_companyId, STOPPED);
                    logger.error(ex);
                    try
                    {
                        connection.rollback();
                    }
                    catch (SQLException e)
                    {
                        logger.error(e);
                    }
                }
                finally
                {
                    DbUtil.silentClose(rs1);
                    DbUtil.silentClose(rs2);
                    DbUtil.silentClose(rs3);

                    DbUtil.silentClose(ps1);
                    DbUtil.silentClose(ps2);
                    DbUtil.silentClose(ps3);
                    DbUtil.silentClose(ps4);
                    DbUtil.silentClose(ps5);
                    DbUtil.silentClose(ps6);
                    DbUtil.silentClose(ps7);

                    if (connection != null)
                    {
                        DbUtil.silentReturnConnection(connection);
                    }
                }
            }
        };

        // If this company has not been migrated or the migration is stopped,
        // you can restart the thread.
        // If the migration is still in progress, do nothing.
        boolean isConstains = migratingCompanyStatus.keySet().contains(
                p_companyId);
        if (company.getSeparateTmTuTuvTables() == 0)
        {
            if (!isConstains
                    || STOPPED.equals(migratingCompanyStatus.get(p_companyId)))
            {
                Thread t = new MultiCompanySupportedThread(runnable);
                t.setName("companyMigration-" + p_companyId);
                t.start();
                migratingCompanyStatus.put(p_companyId, MIGRATING);
            }
        }
    }

    /**
     * Get the companyId whose migration is in progress ("migrating" or
     * "stopped").
     * 
     * @return
     */
    public static Long getMigrationCompanyId()
    {
        if (migratingCompanyStatus != null && migratingCompanyStatus.size() > 0)
        {
            return migratingCompanyStatus.keySet().iterator().next();
        }

        return null;
    }

    public static String getMigrationStatus(Long p_companyId)
    {
        if (migratingCompanyStatus != null)
        {
            return migratingCompanyStatus.get(p_companyId);
        }

        return null;
    }

    /**
     * Judge if current company can run "migrate". 1. If this company is using
     * separated tables, return false; 2. If this company has never run the
     * migration, return true; 3. If this company migration is stopped for
     * certain reason, return true to allow user to rerun the migration to
     * finish the rest. 4. If the company migration is still in progress, return
     * false.
     * 
     * @param p_companyId
     * @return boolean
     */
    public static boolean canMigrateCompany(Long p_companyId)
    {
        try
        {
            Company company = ServerProxy.getJobHandler().getCompanyById(
                    p_companyId);
            if (company.getSeparateTmTuTuvTables() == 1)
            {
                return false;
            }

            boolean isConstains = migratingCompanyStatus.keySet().contains(
                    p_companyId);
            if (!isConstains
                    || STOPPED.equals(migratingCompanyStatus.get(p_companyId)))
            {
                return true;
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }

        return false;
    }
}
