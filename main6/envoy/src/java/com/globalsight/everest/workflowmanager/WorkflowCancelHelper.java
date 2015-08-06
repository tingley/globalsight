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
package com.globalsight.everest.workflowmanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.tuv.TuTuvAttributeImpl;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyFileRemoval;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;

public class WorkflowCancelHelper
{
    private static final Logger logger = Logger
            .getLogger(WorkflowCancelHelper.class.getName());

    private static final int BATCH_CAPACITY = 1000;

    private static long runtime = -1l;
    private static String runningMessage = "";
    private static String doneMessage = "";

    private static final String SQL_DELETE_SCORE = "DELETE FROM SCORECARD_SCORE WHERE WORKFLOW_ID = ?";
    
    private static final String SQL_DELETE_TPLG = "DELETE tplg.* "
            + "FROM target_page_leverage_group tplg, target_page tp "
            + "WHERE tplg.TP_ID = tp.ID "
            + "AND tp.WORKFLOW_IFLOW_INSTANCE_ID = ?";

    private static final String SQL_DELETE_TARGET_PAGE = "DELETE FROM target_page WHERE WORKFLOW_IFLOW_INSTANCE_ID = ?";

    private static final String SQL_DELETE_RESERVED_TIME = "DELETE rt.* "
            + "FROM reserved_time rt, task_info tsk "
            + "WHERE rt.TASK_ID = tsk.TASK_ID " + "AND tsk.WORKFLOW_ID = ?";

    private static final String SQL_DELETE_TASK_TUV = "DELETE tt.* "
            + "FROM task_tuv tt, task_info tsk "
            + "WHERE tt.TASK_ID = tsk.TASK_ID " + "AND tsk.WORKFLOW_ID = ?";

    private static final String SQL_DELETE_TASK_INFO_COMMENT = "DELETE c "
            + "FROM comments c, task_info ti "
            + "WHERE c.comment_object_id = ti.task_id "
            + "AND comment_object_type = 'T' "
            + "AND ti.workflow_id = ? ";

    private static final String SQL_DELETE_TASK_INFO = "DELETE FROM task_info WHERE workflow_id = ?";

    private static final String SQL_DELETE_WORKFLOW_OWNER = "DELETE FROM workflow_owner WHERE workflow_id = ?";

    private static final String SQL_DELETE_SECONDARY_TARGET_FILE = "DELETE FROM secondary_target_file WHERE workflow_id = ?";

    private static final String SQL_DELETE_EXPORT_BATCH = "DELETE FROM exportbatch_workflow WHERE workflow_id = ?";

    private static final String SQL_DELETE_WORKFLOW = "DELETE FROM workflow WHERE IFLOW_INSTANCE_ID = ?";

    private static final String SQL_DELETE_JBPM_TASKACTORPOOL = "DELETE jt.* "
            + "FROM jbpm_taskactorpool jt, jbpm_taskinstance tsk "
            + "WHERE jt.taskinstance_ = tsk.id_ " + "AND tsk.token_ = ?";

    private static final String SQL_DELETE_JBPM_GS_VARIABLE = "DELETE gv.* "
            + "FROM jbpm_gs_variable gv, jbpm_taskinstance ti "
            + "WHERE gv.taskinstance_id = ti.id_ "
            + "AND ti.token_ = ? ";

    private static final String SQL_DELETE_JBPM_VARIABLEINSTANCE = "DELETE jv.* "
            + "FROM jbpm_variableinstance jv, jbpm_taskinstance tsk "
            + "WHERE jv.TASKINSTANCE_ = tsk.ID_ " + "AND tsk.TOKEN_ = ?";

    private static final String SQL_DELETE_JBPM_TASKINSTANCE = "DELETE FROM jbpm_taskinstance WHERE token_ = ?";

    private static final String SQL_DELETE_ISSUE_HISTORY = "DELETE history "
            + "FROM issue_history history, issue iss "
            + "WHERE history.issue_id = iss.id "
            + "AND iss.target_page_id IN ";

    private static final String SQL_DELETE_ISSUE = "DELETE FROM issue "
            + "WHERE target_page_id IN ";

    public static void cancelWorkflow(Workflow workflow) throws Exception
    {
        runningMessage = "Deleting workflow data in table ";
        doneMessage = "Done deleting workflow data in table ";

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(true);
            long wfId = workflow.getId();
            long companyId = workflow.getCompanyId();
            long jobId = workflow.getJob().getId();
            long trgLocaleId = workflow.getTargetLocale().getId();
            String targetLocale = workflow.getTargetLocale().toString();
            
            deleteScore(conn, wfId);

            deleteTargetPageleverageGroup(conn, wfId);
            deleteTargetPage(conn, wfId);

            deleteReservedTime(conn, wfId);
            deleteTaskTuv(conn, wfId);
            deleteTaskInfoComment(conn, wfId);
            deleteTaskInfo(conn, wfId);

            deleteWorkflowOwner(conn, wfId);
            deleteSecondaryTargetFile(conn, wfId);
            deleteExportBatch(conn, wfId);
            deleteWorkflow(conn, wfId);

            deleteJbpmTaskActorPool(conn, wfId);
            deleteJbpmGsVariable(conn, wfId);
            deleteJbpmVariableInstance(conn, wfId);
            deleteJbpmTaskInstance(conn, wfId);

            deleteSegmentComment(conn, workflow);

            List<List<Object>> spIdBatchList = getSourcePageIds(workflow);
            deleteLmExtInfo(conn, trgLocaleId, spIdBatchList, jobId);
            deleteLeverageMatch(conn, trgLocaleId, spIdBatchList, jobId);
            deleteTuTuvAttributeByTuvIds(conn, trgLocaleId, spIdBatchList, jobId);
            deleteTuv(conn, trgLocaleId, spIdBatchList, jobId);

            deleteSecondaryTargetFileOnHD(companyId, jobId, wfId);
            deleteExportedTargetFilesOnHD(companyId, jobId, targetLocale);
            deleteConverterFilesOnHD(workflow, targetLocale);
        }
        catch (Exception e)
        {
            logger.error("Error when cancel workflow " + workflow.getId(), e);
            throw e;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    private static void deleteScore(Connection conn, long wfId)
    		throws SQLException
	{
		logStart("SCORECARD_SCORE");
		execOnce(conn, SQL_DELETE_SCORE, wfId);
		logEnd("SCORECARD_SCORE");
	}
    
    private static void deleteTargetPageleverageGroup(Connection conn, long wfId)
            throws SQLException
    {
        logStart("TARGET_PAGE_LEVERAGE_GROUP");
        execOnce(conn, SQL_DELETE_TPLG, wfId);
        logEnd("TARGET_PAGE_LEVERAGE_GROUP");
    }

    private static void deleteTargetPage(Connection conn, long wfId)
            throws SQLException
    {
        logStart("TARGET_PAGE");
        execOnce(conn, SQL_DELETE_TARGET_PAGE, wfId);
        logEnd("TARGET_PAGE");
    }

    private static void deleteReservedTime(Connection conn, long wfId)
            throws SQLException
    {
        logStart("RESERVED_TIME");
        execOnce(conn, SQL_DELETE_RESERVED_TIME, wfId);
        logEnd("RESERVED_TIME");
    }

    private static void deleteTaskTuv(Connection conn, long wfId)
            throws SQLException
    {
        logStart("TASK_TUV");
        execOnce(conn, SQL_DELETE_TASK_TUV, wfId);
        logEnd("TASK_TUV");
    }

    private static void deleteTaskInfoComment(Connection conn, long wfId)
            throws SQLException
    {
        logStart("COMMENTS");
        execOnce(conn, SQL_DELETE_TASK_INFO_COMMENT, wfId);
        logEnd("COMMENTS");
    }

    private static void deleteTaskInfo(Connection conn, long wfId)
            throws SQLException
    {
        logStart("TASK_INFO");
        execOnce(conn, SQL_DELETE_TASK_INFO, wfId);
        logEnd("TASK_INFO");
    }

    private static void deleteWorkflowOwner(Connection conn, long wfId)
            throws SQLException
    {
        logStart("WORKFLOW_OWNER");
        execOnce(conn, SQL_DELETE_WORKFLOW_OWNER, wfId);
        logEnd("WORKFLOW_OWNER");
    }

    private static void deleteSecondaryTargetFile(Connection conn, long wfId)
            throws SQLException
    {
        logStart("SECONDARY_TARGET_FILE");
        execOnce(conn, SQL_DELETE_SECONDARY_TARGET_FILE, wfId);
        logEnd("SECONDARY_TARGET_FILE");
    }

    private static void deleteSecondaryTargetFileOnHD(long companyId,
            long jobId, long wfId)
    {
        String stfDir = AmbFileStoragePathUtils
                .getFileStorageDirPath(companyId)
                + File.separator
                + AmbFileStoragePathUtils.STF_SUB_DIRECTORY
                + File.separator
                + jobId + File.separator + wfId;
        File file = new File(stfDir.replace("/", File.separator));
        FileUtil.deleteFile(file);
        logger.info("Done deleting secondary target files on hard disk.");
    }

    // Here don't care old jobs that use "jobName" in file path on HD, since
    // 8.5.1 it begins to use "jobId" in file path.
    private static void deleteExportedTargetFilesOnHD(long companyId,
            long jobId, String locale)
    {
        String dir1 = AmbFileStoragePathUtils.getCxeDocDirPath(companyId)
                + File.separator + locale + File.separator + jobId;
        File file = new File(dir1.replace("/", File.separator));
        FileUtil.deleteFile(file);

        String dir2 = AmbFileStoragePathUtils.getCxeDocDirPath(companyId)
                + File.separator + locale + File.separator
                + AmbFileStoragePathUtils.WEBSERVICE_DIR + File.separator
                + jobId;
        file = new File(dir2.replace("/", File.separator));
        FileUtil.deleteFile(file);
        logger.info("Done deleting exported target files on hard disk.");
    }

    private static void deleteConverterFilesOnHD(Workflow workflow,
            String targetLocale) throws Exception
    {
        ArrayList<Request> requests = new ArrayList<Request>(workflow.getJob()
                .getRequestList());
        ArrayList<String> eventFlowXmls = new ArrayList<String>();
        for (Request ri : requests)
        {
            eventFlowXmls.add(ri.getEventFlowXml());
        }
        if (eventFlowXmls.size() > 0)
        {
            for (String eventFlowXml : eventFlowXmls)
            {
                CompanyFileRemoval fileRemoval = new CompanyFileRemoval(
                        eventFlowXml);
                fileRemoval.removeConverterFile(targetLocale);
            }
        }
    }

    private static void deleteExportBatch(Connection conn, long wfId) throws SQLException
    {
        logStart("EXPORTBATCH_WORKFLOW");
        execOnce(conn, SQL_DELETE_EXPORT_BATCH, wfId);
        logEnd("EXPORTBATCH_WORKFLOW");
    }

    private static void deleteWorkflow(Connection conn, long wfId) throws SQLException
    {
        logStart("WORKFLOW");
        execOnce(conn, SQL_DELETE_WORKFLOW, wfId);
        logEnd("WORKFLOW");
    }

    private static void deleteJbpmTaskActorPool(Connection conn, long wfId)
            throws SQLException
    {
        logStart("JBPM_TASKACTORPOOL");
        execOnce(conn, SQL_DELETE_JBPM_TASKACTORPOOL, wfId);
        logEnd("JBPM_TASKACTORPOOL");
    }

    private static void deleteJbpmGsVariable(Connection conn, long wfId)
            throws SQLException
    {
        logStart("JBPM_GS_VARIABLE");
        execOnce(conn, SQL_DELETE_JBPM_GS_VARIABLE, wfId);
        logEnd("JBPM_GS_VARIABLE");
    }

    private static void deleteJbpmVariableInstance(Connection conn, long wfId)
            throws SQLException
    {
        logStart("JBPM_VARIABLEINSTANCE");
        execOnce(conn, SQL_DELETE_JBPM_VARIABLEINSTANCE, wfId);
        logEnd("JBPM_VARIABLEINSTANCE");
    }

    private static void deleteJbpmTaskInstance(Connection conn, long wfId)
            throws SQLException
    {
        logStart("TASK_INSTANCE");
        execOnce(conn, SQL_DELETE_JBPM_TASKINSTANCE, wfId);
        logEnd("TASK_INSTANCE");
    }

    private static void deleteSegmentComment(Connection conn, Workflow workflow)
            throws SQLException
    {
        List<List<Object>> tpIdsInbatch = getTargetPageIds(workflow);

        logStart("ISSUE_HISTORY");
        exec(conn, SQL_DELETE_ISSUE_HISTORY, tpIdsInbatch);
        logEnd("ISSUE_HISTORY");

        logStart("ISSUE");
        exec(conn, SQL_DELETE_ISSUE, tpIdsInbatch);
        logEnd("ISSUE");
    }

    private static void deleteLeverageMatch(Connection conn, long trgLocaleId,
    		List<List<Object>> spIdBatchList, long p_jobId) throws Exception
    {
        String lmTableName = BigTableUtil.getLMTableJobDataInByJobId(p_jobId);
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(lmTableName)
				.append(" WHERE target_locale_id = ").append(trgLocaleId)
				.append(" AND source_page_id IN ");

        logStart(lmTableName.toUpperCase());
        exec(conn, sql.toString(), spIdBatchList);
        logEnd(lmTableName.toUpperCase());
    }

	private static void deleteLmExtInfo(Connection conn, long trgLocaleId,
			List<List<Object>> spIdBatchList, long p_jobId) throws Exception
	{
		String lmExtTable = BigTableUtil.getLMExtTableJobDataInByJobId(p_jobId);
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(lmExtTable)
				.append(" WHERE target_locale_id = ").append(trgLocaleId)
				.append(" AND source_page_id IN ");

		logStart(lmExtTable.toUpperCase());
		exec(conn, sql.toString(), spIdBatchList);
		logEnd(lmExtTable.toUpperCase());
	}

	private static void deleteTuTuvAttributeByTuvIds(Connection conn,
			long trgLocaleId, List<List<Object>> spIdBatchList, long p_jobId)
			throws Exception
    {
        String tuTableName = BigTableUtil.getTuTableJobDataInByJobId(p_jobId);
        String tuvTableName = BigTableUtil.getTuvTableJobDataInByJobId(p_jobId);
        String tuTuvAttrTableName = BigTableUtil.getTuTuvAttributeTableByJobId(p_jobId);

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT attr.ID FROM ").append(tuTuvAttrTableName)
				.append(" attr, ").append(tuvTableName).append(" tuv, ")
				.append(tuTableName)
				.append(" tu, source_page_leverage_group splg ")
				.append(" WHERE attr.OBJECT_TYPE = '")
				.append(TuTuvAttributeImpl.OBJECT_TYPE_TUV).append("'")
				.append(" AND attr.OBJECT_ID = tuv.ID ")
				.append(" AND tuv.TU_ID = tu.ID ")
				.append(" AND tu.LEVERAGE_GROUP_ID = splg.LG_ID ")
				.append(" AND tuv.LOCALE_ID = ").append(trgLocaleId)
				.append(" AND splg.SP_ID IN ");
        List<List<Object>> attIds = queryBatchList(conn, sql.toString(),
        		spIdBatchList);

        logStart(tuTuvAttrTableName.toUpperCase());
        exec(conn, "DELETE FROM " + tuTuvAttrTableName + " WHERE ID IN ", attIds);
        logEnd(tuTuvAttrTableName.toUpperCase());
    }

    private static void deleteTuv(Connection conn, long trgLocaleId,
    		List<List<Object>> spIdBatchList, long p_jobId) throws Exception
    {
        String tuTableName = BigTableUtil.getTuTableJobDataInByJobId(p_jobId);
        String tuvTableName = BigTableUtil.getTuvTableJobDataInByJobId(p_jobId);

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT tuv.ID FROM ").append(tuvTableName).append(" tuv, ")
				.append(tuTableName)
				.append(" tu, source_page_leverage_group splg ")
				.append(" WHERE tuv.TU_ID = tu.ID ")
				.append(" AND tu.LEVERAGE_GROUP_ID = splg.LG_ID ")
				.append(" AND tuv.LOCALE_ID = ").append(trgLocaleId)
				.append(" AND splg.SP_ID IN ");
		List<List<Object>> tuvIdList = queryBatchList(conn, sql.toString(),
				spIdBatchList);

        logStart(tuvTableName.toUpperCase());
        exec(conn, "DELETE FROM " + tuvTableName + " WHERE ID IN ", tuvIdList);
        logEnd(tuvTableName.toUpperCase());
    }

    @SuppressWarnings("rawtypes")
	private static List<List<Object>> getSourcePageIds(Workflow workflow)
    {
    	List<Object> spIdList = new ArrayList<Object>();
        Collection sps = workflow.getJob().getSourcePages();
        for (Iterator it = sps.iterator(); it.hasNext();)
        {
            SourcePage sp = (SourcePage) it.next();
            spIdList.add(sp.getIdAsLong());
        }
        
        return toBatchList(spIdList, 10);
    }

    private static List<List<Object>> getTargetPageIds(Workflow workflow)
    {
    	List<Object> tpIdList = new ArrayList<Object>();
        Vector<TargetPage> tps = workflow.getAllTargetPages();
        for (TargetPage tp : tps)
        {
        	tpIdList.add(tp.getIdAsLong());
        }

        return toBatchList(tpIdList, 10);
    }

    private static List<List<Object>> queryBatchList(Connection conn, String sql,
            Object param) throws SQLException
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
            return toBatchList(rs);
        }
        finally
        {
            ConnectionPool.silentClose(stmt);
            ConnectionPool.silentClose(ps);
        }
    }

	private static List<List<Object>> toBatchList(ResultSet rs)
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
                if (count == BATCH_CAPACITY)
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

    private static List<List<Object>> toBatchList(List<Object> list, int batchSize)
    {
        List<List<Object>> batchList = new ArrayList<List<Object>>();
        if (list == null)
        {
            return batchList;
        }

        List<Object> subList = new ArrayList<Object>();
        int count = 0;
        for (Object obj : list)
        {
        	subList.add(obj);
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

        return batchList;
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

    private static void exec(Connection conn, String sql, List<List<Object>> batchList)
            throws SQLException
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
            logger.error(e);
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

    private static void logStart(String table)
    {
        runtime = System.currentTimeMillis();
        
        logger.info(runningMessage + table);
    }

    private static void logEnd(String table)
    {
        long runningTime = System.currentTimeMillis() - runtime;
        
        logger.info(doneMessage + table + " [" + runningTime + " msc]");
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
}
