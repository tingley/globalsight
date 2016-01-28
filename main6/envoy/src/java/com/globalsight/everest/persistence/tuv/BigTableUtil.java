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

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class BigTableUtil implements TuvQueryConstants
{
    static private final Logger logger = Logger
            .getLogger(BigTableUtil.class);

    /**
     * Decide the TU table when create job.
     * 
     * <p>
     * Note that this is ONLY for job creation. Once job is created, follow its
     * own storing table ("tu_table" column in "job" table).
     * </p>
     * 
     * @param p_companyId
     * @param p_jobId
     * @return String TU table name
     * @throws Exception
     */
    public static String decideTuWorkingTableForJobCreation(long p_companyId,
            long p_jobId) throws Exception
    {
		if (getCompanyBigDataStoreLevel(p_companyId) == CompanyConstants.BIG_DATA_STORE_LEVEL_JOB)
        {
            return TRANSLATION_UNIT_TABLE + "_" + p_companyId + "_" + p_jobId;
        }
		// default
		return TRANSLATION_UNIT_TABLE + "_" + p_companyId;
    }

    /**
     * Decide the TU archive table when create job.
     * 
     * <p>
     * Note that this is ONLY for job creation. Once job is created, follow its
     * own storing table ("tu_archive_table" column in "job" table).
     * 
     * And archive table does not support per table per job.
     * </p>
     * 
     * @param p_companyId
     * @return String
     * @throws Exception
     */
    public static String decideTuArchiveTableForJobCreation(long p_companyId)
    {
		return TRANSLATION_UNIT_TABLE + "_" + p_companyId + "_ARCHIVED";
    }

    /**
     * Decide the TUV table when create job.
     * 
     * <p>
     * Note that this is ONLY for job creation. Once job is created, follow its
     * own storing table ("tuv_table" column in "job" table).
     * </p>
     * 
     * @param p_companyId
     * @param p_jobId
     * @return String TUV table name
     * @throws Exception
     */
    public static String decideTuvWorkingTableForJobCreation(long p_companyId,
            long p_jobId) throws Exception
    {
		if (getCompanyBigDataStoreLevel(p_companyId) == CompanyConstants.BIG_DATA_STORE_LEVEL_JOB)
        {
			return TRANSLATION_UNIT_VARIANT_TABLE + "_" + p_companyId + "_"
					+ p_jobId;
        }
        // default
        return TRANSLATION_UNIT_VARIANT_TABLE + "_" + p_companyId;
    }

    /**
     * Decide the TUV archive table when create job.
     * 
     * <p>
     * Note that this is ONLY for job creation. Once job is created, follow its
     * own storing table ("tuv_archive_table" column in "job" table).
     * 
     * And archive table does not support per table per job.
     * </p>
     * 
     * @param p_companyId
     * @return String
     * @throws Exception
     */
    public static String decideTuvArchiveTableForJobCreation(long p_companyId)
            throws Exception
    {
		return TRANSLATION_UNIT_VARIANT_TABLE + "_" + p_companyId + "_ARCHIVED";
    }

    /**
     * Decide the leverage match table when create job.
     * 
     * <p>
     * Note that this is ONLY for job creation. Once job is created, follow its
     * own storing table ("lm_table" column in "job" table).
     * </p>
     * 
     * @param p_companyId
     * @param p_jobId
     * @return String leverage match table name
     * @throws Exception
     */
    public static String decideLMWorkingTableForJobCreation(long p_companyId,
            long p_jobId) throws Exception
    {
		if (getCompanyBigDataStoreLevel(p_companyId) == CompanyConstants.BIG_DATA_STORE_LEVEL_JOB)
		{
			return LEVERAGE_MATCH_TABLE + "_" + p_companyId + "_" + p_jobId;
		}

        // default
        return LEVERAGE_MATCH_TABLE + "_" + p_companyId;
    }

    /**
     * Decide the leverage match archive table when create job.
     * 
     * <p>
     * Note that this is ONLY for job creation. Once job is created, follow its
     * own storing table ("lm_archive_table" column in "job" table).
     * 
     * And archive table does not support per table per job.
     * </p>
     * 
     * @param p_companyId
     * @return String leverage match table name
     * @throws Exception
     */
    public static String decideLMArchiveTableForJobCreation(long p_companyId)
            throws Exception
    {
		return LEVERAGE_MATCH_TABLE + "_" + p_companyId + "_ARCHIVED";
    }

    /**
     * Decide the leverage match extension table when create job.
     * 
     * <p>
     * Note that this is ONLY for job creation. Once job is created, follow its
     * own storing table ("lm_ext_table" column in "job" table).
     * </p>
     * 
     * @param p_companyId
     * @param p_jobId
     * @return String leverage match table name
     * @throws Exception
     */
	public static String decideLMExtWorkingTableForJobCreation(
			long p_companyId, long p_jobId) throws Exception
    {
		if (getCompanyBigDataStoreLevel(p_companyId) == CompanyConstants.BIG_DATA_STORE_LEVEL_JOB)
        {
			return LEVERAGE_MATCH_EXT_TABLE + "_" + p_companyId + "_" + p_jobId;
        }
        // default
        return LEVERAGE_MATCH_EXT_TABLE + "_" + p_companyId;
    }

	/**
     * Decide the leverage match extension archive table when create job.
     * 
     * <p>
     * Note that this is ONLY for job creation. Once job is created, follow its
     * own storing table ("lm_ext_archive_table" column in "job" table).
     * 
     * And archive table does not support per table per job.
     * </p>
     * 
     * @param p_companyId
     * @return String leverage match table name
     * @throws Exception
     */
    public static String decideLMExtArchiveTableForJobCreation(long p_companyId)
            throws Exception
    {
		return LEVERAGE_MATCH_EXT_TABLE + "_" + p_companyId + "_ARCHIVED";
    }

    /**
     * Get the TU table name that job data is in.
     * 
     * @param p_jobId
     * @return TU table name in String
     * @throws Exception
     */
    public static String getTuTableJobDataInByJobId(long p_jobId)
            throws Exception
    {
        Job job = getJobById(p_jobId);
        return findTuTableDataIn(job);
    }

    /**
     * Get the TU table name that job data is in.
     * 
     * @param p_sourcePageId
     * @return TU table name in String
     * @throws Exception
     */
    public static String getTuTableJobDataInBySourcePageId(long p_sourcePageId)
            throws Exception
    {
        Job job = getJobBySourcePageId(p_sourcePageId);
        return findTuTableDataIn(job);
    }

    /**
     * Get the TUV table name that job data is in.
     * 
     * @param p_jobId
     * @return TUV table name in String
     * @throws Exception
     */
    public static String getTuvTableJobDataInByJobId(long p_jobId)
            throws Exception
    {
        Job job = getJobById(p_jobId);
        return findTuvTableDataIn(job);
    }

    /**
     * Get the TUV table name that job data is in.
     * 
     * @param p_sourcePageId
     * @return TUV table name in String
     * @throws Exception
     */
    public static String getTuvTableJobDataInBySourcePageId(long p_sourcePageId)
            throws Exception
    {
        Job job = getJobBySourcePageId(p_sourcePageId);
        return findTuvTableDataIn(job);
    }
    
    /**
     * Get the leverage match table name that job data is in.
     * 
     * @param p_jobId
     * @return String
     * @throws Exception
     */
    public static String getLMTableJobDataInByJobId(long p_jobId)
            throws Exception
    {
        Job job = getJobById(p_jobId);
        return findLmTableDataIn(job);
    }

    /**
     * Get the leverage match table name that job data is in.
     * 
     * @param p_sourcePageId
     * @return String
     * @throws Exception
     */
    public static String getLMTableJobDataInBySourcePageId(long p_sourcePageId)
            throws Exception
    {
        Job job = getJobBySourcePageId(p_sourcePageId);
        return findLmTableDataIn(job);
    }

	public static String getLMExtTableJobDataInByJobId(long p_jobId)
	{
        Job job = getJobById(p_jobId);
        return findLmExtTableDataIn(job);
	}

	public static String getLMExtTableJobDataInBySourcePageId(
			long p_sourcePageId)
	{
        Job job = getJobBySourcePageId(p_sourcePageId);
        return findLmExtTableDataIn(job);
	}

	/**
     * Get the TU archive table name for specified job ID.
     * 
     * @param p_jobId
     * @return String
     * @throws Exception
     */
    public static String getTuArchiveTableByJobId(long p_jobId)
            throws Exception
    {
        return getJobById(p_jobId).getTuArchiveTable();
    }

    /**
     * Get the TUV archive table name for specified job ID.
     * 
     * @param p_jobId
     * @return String
     * @throws Exception
     */
    public static String getTuvArchiveTableByJobId(long p_jobId)
            throws Exception
    {
        return getJobById(p_jobId).getTuvArchiveTable();
    }

    /**
     * Get the TU archive table name for specified job ID.
     * 
     * @param p_jobId
     * @return String
     * @throws Exception
     */
    public static String getLMArchiveTableByJobId(long p_jobId)
            throws Exception
    {
        return getJobById(p_jobId).getLmArchiveTable();
    }

    /**
     * Return a fixed "template_part_archived" table name.
     */
    public static String getTemplatePartArchiveTable()
    {
        return "TEMPLATE_PART_ARCHIVED";
    }

    
    public static String getTemplatePartTableJobDataIn(long p_sourcePageId)
            throws Exception
    {
        String templatePartTableName = TEMPLATE_PART_TABLE;

        if (isJobDataMigrated(p_sourcePageId))
        {
            templatePartTableName += "_ARCHIVED";
        }

        return templatePartTableName;
    }

	public static String getTuTuvAttributeTableByJobId(long p_jobId)
	{
		long companyId = getJobById(p_jobId).getCompanyId();
		return "TRANSLATION_TU_TUV_ATTR_" + companyId;
	}

	public static boolean isJobDataMigrated(long p_sourcePageId)
            throws Exception
    {
        boolean isMigrated = false;
        try
        {
            isMigrated = getJobBySourcePageId(p_sourcePageId).isMigrated();
        }
        catch (Exception e)
        {
            return false;
        }

        return isMigrated;
    }

    public static int getCompanyBigDataStoreLevel(long p_companyId)
    {
        try
        {
            return getCompanyById(p_companyId).getBigDataStoreLevel();
        }
        catch (Exception e)
        {
            logger.error(e);
        }

        return CompanyConstants.BIG_DATA_STORE_LEVEL_COMPNAY;
    }

    public static Company getCompanyById(long p_companyId) throws Exception
    {
        return ServerProxy.getJobHandler().getCompanyById(p_companyId);
    }

    public static Job getJobById(long p_jobId)
    {
        try
        {
            return ServerProxy.getJobHandler().getJobById(p_jobId);
        }
        catch (Exception e)
        {
            logger.error("Fail to get job by job Id " + p_jobId, e);
        }

        return null;
    }

    public static Job getJobBySourcePageId(long p_sourcePageId)
    {
        try
        {
            SourcePage sp = getSourcePageById(p_sourcePageId);
            long jobId = sp.getJobId();
            // In case job ID is invalid in source_page table.
            if (jobId <= 0)
            {
                jobId = sp.getRequest().getJob().getId();
            }
            return getJobById(jobId);
        }
        catch (Exception e)
        {
            logger.error("Fail to get job by source page Id " + p_sourcePageId,
                    e);
        }

        return null;
    }

    public static SourcePage getSourcePageById(long p_sourcePageId)
    {
        try
        {
            return ServerProxy.getPageManager().getSourcePage(p_sourcePageId);
        }
        catch (Exception e)
        {
            logger.error("Fail to get source page by source page Id "
                    + p_sourcePageId, e);
        }

        return null;
    }

    public static long getCompanyIdBySourcePageId(long p_sourcePageId)
            throws Exception
    {
        long companyId = -1;

        try
        {
            companyId = ServerProxy.getPageManager()
                    .getSourcePage(p_sourcePageId).getCompanyId();
        }
        catch (Exception e)
        {
            logger.error("Failed to get companyId by sourcePageId "
                    + p_sourcePageId);
            throw e;
        }

        return companyId;
    }

    /**
	 * Check if COMPANY level TU/TUV/LM/ATTR tables exist, if not, create them.
	 * 
	 * <p>
	 * The table name styles are: "translation_unit_[companyId]",
	 * "translation_unit_variant_[companyId]", "leverage_match_[companyId]",
	 * "leverage_match_ext_[companyId]", "translation_tu_tuv_attr_[companyId]".
	 * </p>
	 * 
	 * @param companyId
	 */
    public static void checkTuTuvLmWorkingTablesForCompany(long companyId)
    {
        String tuTableName = "translation_unit_" + companyId;
        createTuTable(tuTableName);

        String tuvTableName = "translation_unit_variant_" + companyId;
        createTuvTable(tuvTableName);

        String lmTableName = "leverage_match_" + companyId;
        createLMTable(lmTableName);

        String lmExtTableName = "leverage_match_ext_" + companyId;
        createLMExtTable(lmExtTableName);

        String tuTuvAttrTableName = "translation_tu_tuv_attr_" + companyId;
        createTuTuvAttrTable(tuTuvAttrTableName);
    }

    /**
	 * Check if TU/TUV/LM archive tables exist, if not, create them. Note that
	 * archive tables do not support job level, only company level and system
	 * level.
	 * 
	 * <p>
	 * The table name styles are: "translation_unit_[companyId]_archived",
	 * "translation_unit_variant_[companyId]_archived",
	 * "leverage_match_[companyId]_archived" and
	 * "leverage_match_ext_[companyId]_archived".
	 * </p>
	 * 
	 * @param companyId
	 */
    public static void checkTuTuvLmArchiveTablesForCompany(long companyId)
    {
        String suffix = companyId + "_archived";
        String tuTableName = "translation_unit_" + suffix;
        createTuTable(tuTableName);

        String tuvTableName = "translation_unit_variant_" + suffix;
        createTuvTable(tuvTableName);

        String lmTableName = "leverage_match_" + suffix;
        createLMTable(lmTableName);

        String lmExtTableName = "leverage_match_ext_" + suffix;
        createLMExtTable(lmExtTableName);
    }

    /**
     * Check if TU/TUV/LM archive tables exist for specified job, if not, create
     * them.
     * 
     * <p>
     * Note that archive tables do not support job level, only company level and
     * system level. So this method equals to
     * "checkTuTuvLmArchiveTablesForCompany(long companyId)" in current
     * implementation.
     * </p>
     * <p>
     * The table name styles are: "translation_unit_[companyId]_archived",
     * "translation_unit_variant_[companyId]_archived",
     * "leverage_match_[companyId]_archived".
     * </p>
     * 
     * @param companyId
     */
    public static void checkTuTuvLmArchiveTablesForJob(long jobId)
    {
        Job job = BigTableUtil.getJobById(jobId);
        createTuTable(job.getTuArchiveTable());

        createTuvTable(job.getTuvArchiveTable());

        createLMTable(job.getLmArchiveTable());

        createLMExtTable(job.getLmExtArchiveTable());
    }

    /**
     * Check if template part archive table exists, if not, create it.
     * 
     * In current implementation, this table is fixed: template_part_archived
     * 
     * @param templatePartTable
     */
    public static void checkTemplatePartArchiveTable(String templatePartTable)
    {
        if (DbUtil.isTableExisted(templatePartTable))
        {
            return;
        }

        String sql1 = "DROP TABLE IF EXISTS " + templatePartTable + " CASCADE;";

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(templatePartTable).append(" ");
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
                + templatePartTable + "(TEMPLATE_ID);";

        try
        {
            HibernateUtil.executeSql(sql1);
            HibernateUtil.executeSql(sql2);
            HibernateUtil.executeSql(sql3);
        }
        catch (Exception e)
        {
            logger.error("Failed to create template part archive table '"
                    + templatePartTable + "'.", e);
        }
    }

    /**
     * Create TU table with specified TU table name.
     * 
     * TU table has 3 styles: translation_unit, translation_unit_[companyId],
     * translation_unit_[companyId]_[jobId].
     * 
     * @param p_tuTableName
     */
    public static void createTuTable(String p_tuTableName)
    {
        if (DbUtil.isTableExisted(p_tuTableName))
        {
            return;
        }

        String tuTableName = p_tuTableName;
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
            logger.error("Failed to create table " + tuTableName, e);
        }
    }

    /**
     * Create TUV table with specified TUV table name.
     * 
     * TUV table has 3 styles: translation_unit_variant,
     * translation_unit_variant_[companyId],
     * translation_unit_variant_[companyId]_[jobId].
     * 
     * @param p_tuvTableName
     */
    public static void createTuvTable(String p_tuvTableName)
    {
        if (DbUtil.isTableExisted(p_tuvTableName))
        {
            return;
        }

        String tuvTableName = p_tuvTableName;
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
        sb.append(" KEY `REPETITION_OF_ID` (`REPETITION_OF_ID`)");
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
            logger.error("Failed to create table " + tuvTableName, e);
        }
    }

    /**
     * Create leverage match table with specified leverage match table name.
     * 
     * Leverage match table has 3 styles: leverage_match,
     * leverage_match_[companyId], leverage_match_[companyId]_[jobId].
     * 
     * @param p_lmTableName
     */
    public static void createLMTable(String p_lmTableName)
    {
        if (DbUtil.isTableExisted(p_lmTableName))
        {
            return;
        }

        String lmTableName = p_lmTableName;
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
            logger.error("Failed to create table " + lmTableName, e);
        }
    }

    /**
	 * Create leverage match extension table with specified table name.
	 * 
	 * Leverage match extension table has 2 styles:
	 * leverage_match_ext_[companyId], leverage_match_ext_[companyId]_[jobId].
	 * 
	 * @param p_lmTableName
	 */
    public static void createLMExtTable(String lmExtTable)
    {
        if (DbUtil.isTableExisted(lmExtTable))
        {
            return;
        }

        String sql1 = "DROP TABLE IF EXISTS " + lmExtTable + " CASCADE;";

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(lmExtTable).append(" ");
        sb.append("(");
        sb.append(" SOURCE_PAGE_ID INT(11) DEFAULT NULL,");
        sb.append(" ORIGINAL_SOURCE_TUV_ID BIGINT(20) DEFAULT NULL,");
   		sb.append(" SUB_ID VARCHAR(40) DEFAULT NULL,");
   		sb.append(" TARGET_LOCALE_ID BIGINT(20) DEFAULT NULL,");
   		sb.append(" ORDER_NUM SMALLINT(6) DEFAULT NULL,");
		sb.append(" LAST_USAGE_DATE DATETIME DEFAULT NULL,");
		sb.append(" JOB_ID BIGINT(20) DEFAULT -1,");
		sb.append(" JOB_NAME VARCHAR(320) DEFAULT NULL,");
		sb.append(" PREVIOUS_HASH BIGINT(20) DEFAULT -1,");
		sb.append(" NEXT_HASH BIGINT(20) DEFAULT -1,");
		sb.append(" SID TEXT DEFAULT NULL,");
		sb.append(" varchar1 VARCHAR(512),");
		sb.append(" varchar2 VARCHAR(512),");
		sb.append(" varchar3 VARCHAR(512),");
		sb.append(" varchar4 VARCHAR(512),");
		sb.append(" text1 TEXT,");
		sb.append(" text2 TEXT,");
		sb.append("	long1 BIGINT(20),");
		sb.append(" long2 BIGINT(20),");
		sb.append(" date1 DATETIME,");
		sb.append(" date2 DATETIME,");
		sb.append(" UNIQUE KEY IDX_4_UNIQUE_KEY (ORIGINAL_SOURCE_TUV_ID, SUB_ID, TARGET_LOCALE_ID, ORDER_NUM),");
		sb.append(" KEY IDX_SPID_TRGLOCID (SOURCE_PAGE_ID, TARGET_LOCALE_ID)");
		sb.append(" ) ENGINE=INNODB;");
        String sql2 = sb.toString();

        try
        {
            HibernateUtil.executeSql(sql1);
            HibernateUtil.executeSql(sql2);
        }
        catch (Exception e)
        {
            logger.error("Failed to create table " + lmExtTable, e);
        }
    }

    /**
	 * Create "translation_tu_tuv_[companyId]" table. This table has no archived
	 * table, and it is one table per company, not support job level.
	 * 
	 * @param p_tuTuvAttrTableName
	 */
    public static void createTuTuvAttrTable(String p_tuTuvAttrTableName)
    {
        if (DbUtil.isTableExisted(p_tuTuvAttrTableName))
        {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(p_tuTuvAttrTableName).append(" ");
        sb.append("(");
        sb.append(" ID BIGINT(20) NOT NULL AUTO_INCREMENT, ");
        sb.append(" OBJECT_ID BIGINT(20) DEFAULT NULL, ");
        sb.append(" OBJECT_TYPE VARCHAR(20) DEFAULT NULL, ");
        sb.append(" NAME VARCHAR(100) NOT NULL, ");
        sb.append(" VARCHAR_VALUE VARCHAR(512) DEFAULT NULL, ");
        sb.append(" TEXT_VALUE TEXT, ");
        sb.append(" LONG_VALUE BIGINT(20) DEFAULT NULL, ");
        sb.append(" DATE_VALUE DATETIME DEFAULT NULL, ");
        sb.append(" PRIMARY KEY (ID), ");
        sb.append(" KEY IDX_OBJECT_ID_TYPE_NAME (OBJECT_ID, OBJECT_TYPE, NAME) ");
        sb.append(" ) ENGINE=INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET=utf8");

        try
        {
            HibernateUtil.executeSql(sb.toString());
        }
        catch (Exception e)
        {
            logger.error("Failed to create table " + p_tuTuvAttrTableName, e);
        }
    }

    //
    // PRIVATE METHODS
    //

    private static String findTuTableDataIn(Job job)
    {
        if (job.isMigrated())
        {
            return job.getTuArchiveTable();
        }
        else
        {
            return job.getTuTable();
        }
    }

    private static String findTuvTableDataIn(Job job)
    {
        if (job.isMigrated())
        {
            return job.getTuvArchiveTable();
        }
        else
        {
            return job.getTuvTable();
        }
    }

    private static String findLmTableDataIn(Job job)
    {
        if (job.isMigrated())
        {
            return job.getLmArchiveTable();
        }
        else
        {
            return job.getLmTable();
        }
    }

    private static String findLmExtTableDataIn(Job job)
    {
    	if (job.isMigrated())
    	{
    		return job.getLmExtArchiveTable();
    	}
    	else
    	{
    		return job.getLmExtTable();
    	}
    }
}
