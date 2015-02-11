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

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.servlet.util.ServerProxy;
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
//    private static final String FINISHED = "finished";

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
            + "MATCHED_ORIGINAL_SOURCE, JOB_DATA_TU_ID, SID, CREATION_USER, CREATION_DATE, "
            + "MODIFY_USER, MODIFY_DATE) "
            + "SELECT SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, MATCHED_TEXT_CLOB, "
            + "TARGET_LOCALE_ID, MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, "
            + "MATCHED_TABLE_TYPE, PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, "
            + "MATCHED_ORIGINAL_SOURCE, JOB_DATA_TU_ID, SID, CREATION_USER, CREATION_DATE, "
            + "MODIFY_USER, MODIFY_DATE "
            + "FROM _LEVERAGE_MATCH_DATA_IN_ " + "WHERE SOURCE_PAGE_ID = ?";

//    private static final String DELETE_LM_BY_SP_ID = "DELETE FROM LEVERAGE_MATCH WHERE SOURCE_PAGE_ID = ?";

    private static final String INSERT_TU_BY_LG_ID = "INSERT INTO "
            + TuvQueryConstants.TU_TABLE_PLACEHOLDER
            + " "
            + "(ID, ORDER_NUM, TM_ID, DATA_TYPE, TU_TYPE, "
            + "LOCALIZE_TYPE, LEVERAGE_GROUP_ID, PID, SOURCE_TM_NAME, XLIFF_TRANSLATION_TYPE, "
            + "XLIFF_LOCKED, IWS_SCORE, XLIFF_TARGET_SEGMENT, XLIFF_TARGET_LANGUAGE, GENERATE_FROM, "
            + "SOURCE_CONTENT, PASSOLO_STATE, TRANSLATE) "
            + "SELECT ID, ORDER_NUM, TM_ID, DATA_TYPE, TU_TYPE, "
            + "LOCALIZE_TYPE, LEVERAGE_GROUP_ID, PID, SOURCE_TM_NAME, XLIFF_TRANSLATION_TYPE, "
            + "XLIFF_LOCKED, IWS_SCORE, XLIFF_TARGET_SEGMENT, XLIFF_TARGET_LANGUAGE, GENERATE_FROM, "
            + "SOURCE_CONTENT, PASSOLO_STATE, TRANSLATE "
            + "FROM _TRANSLATION_UNIT_DATA_IN_ " + "WHERE leverage_group_id = ?";

    private static final String INSERT_TUV_BY_LG_ID = "INSERT INTO "
            + TuvQueryConstants.TUV_TABLE_PLACEHOLDER
            + " "
            + "(ID, ORDER_NUM, LOCALE_ID, TU_ID, IS_INDEXED, "
            + "SEGMENT_CLOB, SEGMENT_STRING, WORD_COUNT, EXACT_MATCH_KEY, STATE, "
            + "MERGE_STATE, TIMESTAMP, LAST_MODIFIED, MODIFY_USER, CREATION_DATE, "
            + "CREATION_USER, UPDATED_BY_PROJECT, SID, SRC_COMMENT, REPETITION_OF_ID, "
            + "IS_REPEATED) "
            + "SELECT tuv.ID, tuv.ORDER_NUM, tuv.LOCALE_ID, tuv.TU_ID, tuv.IS_INDEXED, "
            + "tuv.SEGMENT_CLOB, tuv.SEGMENT_STRING, tuv.WORD_COUNT, tuv.EXACT_MATCH_KEY, tuv.STATE, "
            + "tuv.MERGE_STATE, tuv.TIMESTAMP, tuv.LAST_MODIFIED, tuv.MODIFY_USER, tuv.CREATION_DATE, "
            + "tuv.CREATION_USER, tuv.UPDATED_BY_PROJECT, tuv.SID, tuv.SRC_COMMENT, tuv.REPETITION_OF_ID, "
            + "tuv.IS_REPEATED "
            + "FROM _TRANSLATION_UNIT_VARIANT_DATA_IN_ tuv, _TRANSLATION_UNIT_DATA_IN_ tu "
            + "WHERE tu.ID = tuv.TU_ID " + "AND tu.leverage_group_id = ?";

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
                String targetLMTable = "leverage_match_" + p_companyId;
                String targetTUTable = "translation_unit_" + p_companyId;
                String targetTUVTable = "translation_unit_variant_" + p_companyId;
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
                    Job curJob = null;

                    while (rs3.next())
                    {
                        long spId = rs3.getLong(1);
                        curJob = ServerProxy.getPageManager()
                                .getSourcePage(spId).getRequest().getJob();
                        long lgId = rs3.getLong(2);
                        if (!migratedLgIds.contains(lgId))
                        {
                            connection.setAutoCommit(false);

                            // Insert LM data to "leverage_match_[companyId]"
                            if (ps4 == null)
                            {
                                String fromLMTable = "leverage_match";
                                if (curJob.isMigrated())
                                {
                                    fromLMTable = "leverage_match_archived";
                                }
                                String sql4 = INSERT_LM_BY_SP_ID
                                        .replace(TuvQueryConstants.LM_TABLE_PLACEHOLDER, targetLMTable)
                                        .replace("_LEVERAGE_MATCH_DATA_IN_", fromLMTable);
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
                                String fromTUTable = "translation_unit";
                                if (curJob.isMigrated())
                                {
                                    fromTUTable = "translation_unit_archived";
                                }

                                String sql6 = INSERT_TU_BY_LG_ID
                                        .replace(TuvQueryConstants.TU_TABLE_PLACEHOLDER, targetTUTable)
                                        .replace("_TRANSLATION_UNIT_DATA_IN_", fromTUTable);
                                ps6 = connection.prepareStatement(sql6);
                            }
                            ps6.setLong(1, lgId);
                            ps6.execute();

                            // Insert TUV data into
                            // "translation_unit_variant_[companyId]".
                            if (ps7 == null)
                            {
                                String fromTUTable = "translation_unit";
                                String fromTUVTable = "translation_unit_variant";
                                if (curJob.isMigrated())
                                {
                                    fromTUTable = "translation_unit_archived";
                                    fromTUVTable = "translation_unit_variant_archived";
                                }
                                String sql7 = INSERT_TUV_BY_LG_ID
                                        .replace(TuvQueryConstants.TUV_TABLE_PLACEHOLDER, targetTUVTable)
                                        .replace("_TRANSLATION_UNIT_VARIANT_DATA_IN_", fromTUVTable)
                                        .replace("_TRANSLATION_UNIT_DATA_IN_", fromTUTable);
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
                        // Current job's data are moved to its OWN working
                        // tables, even it has been archived previously, so
                        // update flag to false.
                        curJob.setIsMigrated(false);
                        HibernateUtil.update(curJob);
                    }

                    logger.info("Company migration is finished completely.");
                    company.setMigrateProcessing(100);
                    company.setBigDataStoreLevel(CompanyConstants.BIG_DATA_STORE_LEVEL_COMPNAY);
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
        boolean isContained = migratingCompanyStatus.keySet().contains(
                p_companyId);
        if (company.getBigDataStoreLevel() == CompanyConstants.BIG_DATA_STORE_LEVEL_SYSTEM)
        {
            if (!isContained
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
            if (company.getBigDataStoreLevel() != 0)
            {
                return false;
            }

            boolean isContained = migratingCompanyStatus.keySet().contains(
                    p_companyId);
            if (!isContained
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
