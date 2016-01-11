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

package com.globalsight.webservices;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectInfo;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WfTemplateSearchParameters;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflow.ConditionNodeTargetInfo;
import com.globalsight.everest.workflow.WorkflowArrow;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowOwners;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.webservices.vo.JobFiles;

/**
 * WebService APIs of GlobalSight handles web services related to projects,
 * jobs, workflows, import, export,setup, etc. for GlobalSight
 * 
 * NOTE: The web service that Apache Axis generates will be named
 * Ambassador4Falcon
 */
public class Ambassador4Falcon extends JsonTypeWebService
{
    // Method names
    private static final Logger logger = Logger
            .getLogger(Ambassador4Falcon.class);

    public static final String GET_TRANSLATION_PERCENTAGE = "getTranslationPercentage";
    public static final String GET_JOB_IDS_WITH_STATUS_CHANGED = "getJobIDsWithStatusChanged";
    public static final String GET_DETAILED_WORD_COUNTS = "getDetailedWordcounts";
    public static final String GET_WORKFLOW_TEMPLATE_NAMES = "getWorkflowTemplateNames";
    public static final String GET_WORKFLOW_TEMPLATE_INFO = "getWorkflowTemplateInfo";
    public static final String MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES = "modifyWorkflowTemplateAssignees";
    public static final String GET_WORK_OFFLINE_FILES = "getWorkOfflineFiles";
    public static final String UPLOAD_WORK_OFFLINE_FILES = "uploadWorkOfflineFiles";
    public static final String IMPORT_WORK_OFFLINE_FILES = "importWorkOfflineFiles";
    public static final String ACCEPT_TASK = "acceptTask"; 
    public static final String COMPLETE_TASK = "completeTask";
    public static final String REJECT_TASK = "rejectTask";
    public static final String EXPORT_JOB = "exportJob";
    public static final String EXPORT_WORKFLOW = "exportWorkflow";
    public static final String GET_JOB_EXPORT_FILES = "getJobExportFiles";
    public static final String GET_JOB_EXPORT_WORKFLOW_FILES = "getJobExportWorkflowFiles";
    public static final String GET_IN_CONTEXT_REVIEW_LINK = "getInContextReviewLink";
    public static final String GET_ALL_PROJECT_PROFILES="getAllProjectProfiles";
    public static final String GET_ALL_PROJECTS_BY_USER = "getAllProjectsByUser";
    public static final String GET_ACTIVITY_LIST = "getActivityList";

    private static String NOT_IN_DB = "This job is not ready for query: ";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a GlobalSight WebService object.
     */
    public Ambassador4Falcon()
    {
        logger.info("Creating new GlobalSight Web Service for Falcon.");
    }

    /**
     * Return job IDs that "changes" have happened in specified past interval
     * minutes. This includes general events such as job creation, workflow
     * dispatch, workflow completion, task acceptance, task completion etc.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_intervalInMinute
     *            -- interval time in minutes.
     * @param p_companyName
     * 				--get job id of the company 
     * @return jobIDs in json string. A sample result is like:
     *         {"JOB_ID":"204,213,215,216,218,220,190,202,205,217,219"}
     * @throws WebServiceException
     * 
     */
    public String getJobIDsWithStatusChanged(String p_accessToken,
            int p_intervalInMinute,String p_companyName) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_JOB_IDS_WITH_STATUS_CHANGED);
		String jobsView = checkPermissionReturnStr(p_accessToken,
				Permission.JOBS_VIEW);
		if (StringUtil.isNotEmpty(jobsView))
			return jobsView;
	
        String json = "";
        WebServicesLog.Start activityStart = null;
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("intervalInMinute", p_intervalInMinute);
            activityStart = WebServicesLog.start(Ambassador4Falcon.class,
                            "getJobIDsWithStatusChanged(p_accessToken,p_intervalInMinute)",
                    activityArgs);
            if (StringUtil.isEmpty(p_accessToken) || p_intervalInMinute < 1)
            {
                return makeErrorJson(GET_JOB_IDS_WITH_STATUS_CHANGED,
                        "Invaild time range parameter.");
            }
			if (StringUtil.isEmpty(p_companyName))
			{
				return makeErrorJson(GET_JOB_IDS_WITH_STATUS_CHANGED,
						"Invaild comoany name parameter.");
			}
            // int hours = getHours(p_sinceTime);
            Calendar calendar = Calendar.getInstance();
            // calendar.add(Calendar.HOUR, 0 - hours);
            calendar.add(Calendar.MINUTE, 0 - p_intervalInMinute);
            String timeStamp = dateFormat.format(calendar.getTime());
            User user = getUser(getUsernameFromSession(p_accessToken));
            String curCompanyName = user.getCompanyName();
            String curCompanyId = CompanyWrapper.getCompanyIdByName(curCompanyName);
			if (!curCompanyId.equals("1")
					&& !curCompanyName.equalsIgnoreCase(p_companyName))
			{
				return makeErrorJson(GET_JOB_IDS_WITH_STATUS_CHANGED,
						"Invaild comoany name parameter.");
			}
            String sql = "SELECT DISTINCT workflow.JOB_ID FROM task_info, workflow, job "
                    + "WHERE workflow.COMPANY_ID = ? "
                    + "AND (task_info.ACCEPTED_DATE > ? "
                    + "    OR task_info.COMPLETED_DATE > ? "
                    + "    OR job.TIMESTAMP > ? "
                    + "    OR workflow.TIMESTAMP > ? "
                    + "    OR workflow.COMPLETED_DATE > ?"
                    + "    OR workflow.DISPATCH_DATE > ?) "
                    + "AND workflow.IFLOW_INSTANCE_ID = task_info.WORKFLOW_ID "
                    + "AND job.ID = workflow.JOB_ID";

            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql);
            query.setString(1, CompanyWrapper.getCompanyIdByName(p_companyName));
            query.setString(2, timeStamp);
            query.setString(3, timeStamp);
            query.setString(4, timeStamp);
            query.setString(5, timeStamp);
            query.setString(6, timeStamp);
            query.setString(7, timeStamp);
            results = query.executeQuery();
            json = resultSetToJson("JOB_ID", results);
        }
        catch (Exception e)
        {
            return makeErrorJson(GET_JOB_IDS_WITH_STATUS_CHANGED,
                    "Cannot get jobs correctly. " + e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
            releaseDBResource(results, query, connection);
        }

        return json;
    }
    
    /**
     * Get translation percentage of specified task and its target pages.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID in string, example: "8389"
     * @return String in JSON style, an example is:
     * {"jobId":452, "jobName":"3536_001_166227234", "taskId":8389, "taskName":"Translation1_1007", "targetLocale":"German (Germany) [de_DE]", "sourceLocale":"English (United States) [en_US]", "taskTranslationPrecentage":78,
     * "targetPages":"[{\"targetPageName\":\"en_US\\\\452\\\\Internet Explorer.docx\",\"pageTranslationPrecentage\":70},{\"targetPageName\":\"en_US\\\\452\\\\Internet Explorer2.docx\",\"pageTranslationPrecentage\":90}]"}
     */
	public String getTranslationPercentage(String p_accessToken, String p_taskId)
			throws WebServiceException
	{
		checkAccess(p_accessToken, GET_TRANSLATION_PERCENTAGE);
		WebServicesLog.Start activityStart = null;

		Map<Object, Object> activityArgs = new HashMap<Object, Object>();
		activityArgs.put("taskId", p_taskId);
		activityStart = WebServicesLog
				.start(Ambassador4Falcon.class,
						"getTranslationPercentage(accessToken, p_taskId)",
						activityArgs);
		User curUser = getUser(getUsernameFromSession(p_accessToken));
		Task task = TaskHelper.getTask(Long.parseLong(p_taskId));
		JSONObject jsonObj = new JSONObject();
		try
		{
			if (task != null)
			{
                long companyId = getCompanyByName(curUser.getCompanyName())
                        .getId();
                if (companyId != 1 && task.getCompanyId() != companyId)
			    {
                    return makeErrorJson(
                            GET_TRANSLATION_PERCENTAGE,
                            "Logged user is not super user or the task does not belong to the company of logger user.");
			    }

				jsonObj.put("jobId", task.getJobId());
				jsonObj.put("jobName", task.getJobName());
				jsonObj.put("sourceLocale", task.getSourceLocale()
						.getDisplayName());
				jsonObj.put("targetLocale", task.getTargetLocale()
						.getDisplayName());
				jsonObj.put("taskId", task.getId());
				jsonObj.put("taskName", task.getTaskName());
				int taskPrecentage = SegmentTuvUtil
						.getTranslatedPercentageForTask(task);
				jsonObj.put("taskTranslationPrecentage", taskPrecentage);

				JSONArray array = new JSONArray();
				List list = task.getTargetPages();
				for (int i = 0; i < list.size(); i++)
				{
					JSONObject json = new JSONObject();
					TargetPage targetPage = (TargetPage) list.get(i);
					json.put("targetPageName", targetPage.getExternalPageId());
					int pagePercentage = SegmentTuvUtil
							.getTranslatedPercentageForTargetPage(targetPage
									.getId());
					json.put("pageTranslationPrecentage", pagePercentage);
					array.put(json);
				}
				jsonObj.put("targetPages", array.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (activityStart != null)
		{
			activityStart.end();
		}
		return jsonObj.toString();
	}

    /**
     * <p>
     * Return job ID, jobName, description, creationDate, lang, Matches, 95-99%,
     * 85-94%, 75-84%, noMatch, repetitions, inContextMatches, MT, total,
     * MTConfidenceScore, filePath, fileName for specified jobs. This is similar
     * with "Detailed Word Counts By Job" report.
     * </p>
     * <p>
     * Falcon connector will use this API to read and price MT'd words, so it
     * can quote MT projects. Currently, the Falcon connector assumes for MT
     * projects all new words are MT'd words.
     * </p>
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_jobIds
     *            -- jobIds in array.
     * @param includeMTData
     *            -- flag to decide if include MT'd word counts.
     * @return String in JSON. A sample is like:
     * [{"total":236,"85-94%":0,"jobId":39,"filePath":"en_US\\39\\global","MTConfidenceScore":100,"inContextMatches":0,"75-84%":0,"95-99%":0,"jobName":"mumt_22759766","lang":"fr_FR","MT":0,"repetitions":0,
     * "noMatch":0,"projectDescription":"com1","creationDate ":"2013-11-01 11:48:20","fileName":"Welocalize_Company.html","100%Matches":236},
     * {"total":236,"85-94%":0,"jobId":39,"filePath":"en_US\\39\\global","MTConfidenceScore":100,"inContextMatches":0,"75-84%":0,"95-99%":0,"jobName":"mumt_22759766","lang":"de_DE","MT":0,"repetitions":0,
     * "noMatch":236,"projectDescription":"com1","creationDate ":"2013-11-01 11:48:20","fileName":"Welocalize_Company.html","100%Matches":0},
     * {"total":236,"85-94%":13,"jobId":38,"filePath":"en_US\\38\\global","MTConfidenceScore":100,"inContextMatches":53,"75-84%":13,"95-99%":30,"jobName":"mty_826004265","lang":"fr_FR","MT":0,"repetitions":0,
     * "noMatch":52,"projectDescription":"Template","creationDate ":"2013-11-01 11:41:14","fileName":"Welocalize_Company.html","100%Matches":75}]
     * 
     * @throws WebServiceException
     * 
     */
    public String getDetailedWordcounts(String p_accessToken,
            String[] p_jobIds, Boolean p_includeMTData)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_DETAILED_WORD_COUNTS);
		String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.REPORTS_DELL_FILE_LIST);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
        String json = "";
        WebServicesLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("jobIds", p_jobIds);
            activityArgs.put("includeMTData", p_includeMTData);
            activityStart = WebServicesLog
                    .start(Ambassador4Falcon.class,
                            "getDetailedWordcounts(accessToken, jobIds, includeMTData)",
                            activityArgs);
            if (StringUtil.isEmpty(p_accessToken) || p_jobIds.length < 1)
            {
                return makeErrorJson(GET_DETAILED_WORD_COUNTS,
                        "Invaild jobIds parameter.");
            }

            User curUser = getUser(getUsernameFromSession(p_accessToken));
            Company company = getCompanyByName(curUser.getCompanyName());

            JSONArray array = new JSONArray();
            for (String jobIdstr : p_jobIds)
            {
                if (!StringUtils.isNumeric(jobIdstr))
                    continue;
                long jobId = Long.valueOf(jobIdstr);
                Job job = ServerProxy.getJobHandler().getJobById(jobId);
                if (job != null)
                {
                    // If job is not from current user's company, ignore.
                    if (company.getId() != 1
                            && company.getId() != job.getCompanyId())
                    {
                        continue;
                    }

                    int threshold = job.getLeverageMatchThreshold();
                    String jobname = job.getJobName();
                    String pDesc = getProjectDesc(job);
                    String createDate = dateFormat
                            .format(job.getCreateDate());
                    for (Workflow p_workflow : job.getWorkflows())
                    {
                        int mtConfidenceScore = p_workflow
                                .getMtConfidenceScore();
                        String lang = p_workflow.getTargetLocale().toString();
                        for (TargetPage tg : p_workflow.getTargetPages())
                        {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("jobId", jobId);
                            jsonObj.put("jobName", jobname);
                            setFilePathName(tg, jsonObj);
                            jsonObj.put("projectDescription", pDesc);
                            jsonObj.put("creationDate", createDate);
                            jsonObj.put("lang", lang);
                            try
                            {
                                addWordCountForJson(tg, jsonObj, threshold,
                                        mtConfidenceScore, p_includeMTData);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            array.put(jsonObj);
                        }

                    }
                }
            }
            json = array.toString();
        }
        catch (Exception e)
        {
            return makeErrorJson(GET_DETAILED_WORD_COUNTS,
                    "Cannot get Wordcounts correctly. " + e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return json;
    }
    
    /**
     * Accept specified task.
     * 
     * @param p_accessToken
     *            The access token received from the login.
     * @param p_taskId
     *            Task Id to be accepted.
     * @return String in JSON. A sample is like:
	 * 			  {"acceptTask":"success"}
     * @throws WebServiceException
     */
    public String acceptTask(String p_accessToken, String p_taskId)
            throws WebServiceException
	{
		String rtnString = "success";
		checkAccess(p_accessToken, ACCEPT_TASK);
		String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.ACTIVITIES_ACCEPT);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		try
		{
			Assert.assertNotEmpty(p_accessToken, "Access token");
			Assert.assertIsInteger(p_taskId);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return makeErrorJson(ACCEPT_TASK, e.getMessage());
		}

		String acceptorName = getUsernameFromSession(p_accessToken);
		String acceptor = UserUtil.getUserIdByName(acceptorName);

		Task task = null;
		try
		{
			task = TaskHelper.getTask(Long.parseLong(p_taskId));
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			String message = "Failed to get task object by taskId : "
					+ p_taskId;
			return makeErrorJson(ACCEPT_TASK, message);
		}
		WebServicesLog.Start activityStart = null;
		try
		{
			if (task != null)
			{
				Map<Object, Object> activityArgs = new HashMap<Object, Object>();
				activityArgs.put("loggedUserName", acceptorName);
				activityArgs.put("taskId", p_taskId);
				activityStart = WebServicesLog.start(Ambassador4Falcon.class,
						"acceptTask(p_accessToken,p_taskId)", activityArgs);
				if (task.getState() == Task.STATE_ACCEPTED
						|| task.getState() == Task.STATE_COMPLETED)
				{
					return makeErrorJson(ACCEPT_TASK,
							"The current task has been accepted or completed state.");
				}
				WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
						.getWorkflowTaskInstance(acceptor, task.getId(),
								WorkflowConstants.TASK_ALL_STATES);
				task.setWorkflowTask(wfTask);
				List allAssignees = task.getAllAssignees();
				if (allAssignees != null && allAssignees.size() > 0)
				{
					if (!allAssignees.contains(acceptor))
					{
						String message = "'"
								+ acceptor
								+ "' is not an available assignee for current task "
								+ p_taskId;
						logger.warn(message);
						return makeErrorJson(ACCEPT_TASK, message);
					}
				}
				// GS will check if the acceptor is PM or available users
				TaskHelper.acceptTask(acceptor, task);
			}
			else
			{
				return makeErrorJson(ACCEPT_TASK, "Invaild task id.");
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			String message = "Failed to accept task for taskId : " + p_taskId
					+ ",maybe '" + acceptor
					+ "' do not have the authority to operate the task";
			return makeErrorJson(ACCEPT_TASK, message);
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}

		return rtnString;
	}
	
    /**
     * Complete task
     * 
     * @param p_accessToken
     *            The access token received from the login.
     * @param p_taskId
     *            Task Id to be completed.
     * @param p_destinationArrow
     *            This points to the next activity. Null if this task has no
     *            condition node.
     *  @return String in JSON. A sample is like:
	 * 			  {"completeTask":"success"}
     * @throws WebServiceException
     */
    public String completeTask(String p_accessToken, String p_taskId,
            String p_destinationArrow) throws WebServiceException
	{
		String rtnStr = "success";
		checkAccess(p_accessToken, "completeTask");
		String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.ACTIVITIES_ACCEPT);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
		try
		{
			Assert.assertNotEmpty(p_accessToken, "Access token");
			Assert.assertIsInteger(p_taskId);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return makeErrorJson(COMPLETE_TASK, e.getMessage());
		}

		String userName = this.getUsernameFromSession(p_accessToken);
		String userId = UserUtil.getUserIdByName(userName);

		// Task object
		TaskManager taskManager = ServerProxy.getTaskManager();
		Task task = null;
		try
		{
			task = taskManager.getTask(Long.parseLong(p_taskId));
			if (task == null)
				return makeErrorJson(COMPLETE_TASK, "Invaild task id.");
		}
		catch (RemoteException re)
		{
			String msg = "Fail to get task object by taskId : " + p_taskId;
			logger.error(msg, re);
			return makeErrorJson(COMPLETE_TASK, msg);
		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
		}

		// Compelte task
		WebServicesLog.Start activityStart = null;
		try
		{
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", userName);
			activityArgs.put("taskId", p_taskId);
			activityArgs.put("destinationArrow", p_destinationArrow);
			activityStart = WebServicesLog.start(Ambassador4Falcon.class,
					"completeTask(p_accessToken,p_taskId,p_destinationArrow)",
					activityArgs);
			// Find the user to complete task.
			WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
					.getWorkflowTaskInstance(userId, task.getId(),
							WorkflowConstants.TASK_ALL_STATES);
			task.setWorkflowTask(wfTask);
			List allAssignees = task.getAllAssignees();
			if (allAssignees != null && allAssignees.size() > 0)
			{
				if (!allAssignees.contains(userId))
				{
					String message = "'"
							+ userName
							+ "' is not an available assignee for current task "
							+ p_taskId;
					logger.warn(message);
					return makeErrorJson(COMPLETE_TASK, message);
				}
			}

			Vector conditionNodes = wfTask.getConditionNodeTargetInfos();
			if (conditionNodes != null && conditionNodes.size() > 0)
			{
				HashSet<String> arrowNames = new HashSet<String>();
				for (int i = 0; i < conditionNodes.size(); i++)
				{
					ConditionNodeTargetInfo info = (ConditionNodeTargetInfo) conditionNodes
							.get(i);
					arrowNames.add(info.getArrowName());
				}

				if (!arrowNames.contains(p_destinationArrow))
				{
					String message = "\"" + p_destinationArrow
							+ "\" is not a valid outgoing arrow name.";
					logger.warn(message);
					return makeErrorJson(COMPLETE_TASK, message);
				}
			}

			TaskImpl dbTask = HibernateUtil.get(TaskImpl.class, task.getId());
			ProjectImpl project = (ProjectImpl) dbTask.getWorkflow().getJob()
					.getProject();
			WorkflowImpl workflowImpl = (WorkflowImpl) dbTask.getWorkflow();
			boolean isCheckUnTranslatedSegments = project
					.isCheckUnTranslatedSegments();
			boolean isRequriedScore = workflowImpl.getScorecardShowType() == 1 ? true
					: false;
			boolean isReviewOnly = dbTask.isReviewOnly();
			if (isCheckUnTranslatedSegments && !isReviewOnly)
			{
				int percentage = SegmentTuvUtil
						.getTranslatedPercentageForTask(task);
				if (100 != percentage)
				{
					rtnStr = "The task is not 100% translated and can not be completed.";
					return makeErrorJson(COMPLETE_TASK, rtnStr);
				}
			}
			if (isRequriedScore && isReviewOnly)
			{
				if (StringUtil.isEmpty(workflowImpl.getScorecardComment()))
				{
					rtnStr = "The task is not scored and can not be completed.";
					return makeErrorJson(COMPLETE_TASK, rtnStr);
				}
			}

			if (task.getState() == Task.STATE_ACCEPTED)
			{
				ServerProxy.getTaskManager().completeTask(userId, task,
						p_destinationArrow, null);
			}
			else
			{
				rtnStr = "Cannot complete this task as it is not in 'ACCEPTED' state";
				 return makeErrorJson(COMPLETE_TASK, rtnStr);
			}
		}
		catch (Exception ex)
		{
			String msg = "Fail to complete task : " + p_taskId + " ; "
					+ ex.getMessage();
			logger.error(msg, ex);
			return makeErrorJson(COMPLETE_TASK, msg);
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}

		return rtnStr;
	}
	
    /**
     * Reject specified task.
     * 
     * @param p_accessToken
     *            The access token received from the login.
     * 
     * @param p_taskId
     *            Task Id to be accepted.
     * 
     * @param p_rejectComment
     *            Reject comment.
     * @return String in JSON. A sample is like:
	 * 				{"rejectTask":"success"}
     * @throws WebServiceException
     */
    public String rejectTask(String p_accessToken, String p_taskId,
            String p_rejectComment) throws WebServiceException
	{
		String rtnStr = "success";
		checkAccess(p_accessToken, REJECT_TASK);
		String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.ACTIVITIES_REJECT_AFTER_ACCEPTING);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
		try
		{
			Assert.assertNotEmpty(p_accessToken, "Access token");
			Assert.assertIsInteger(p_taskId);
			Assert.assertNotEmpty(p_rejectComment, "Reject comment");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return makeErrorJson(REJECT_TASK, e.getMessage());
		}
		// rejector
		String rejectUserName = getUsernameFromSession(p_accessToken);
		String rejectUserId = UserUtil.getUserIdByName(rejectUserName);
		Task task = null;
		WebServicesLog.Start activityStart = null;
		try
		{
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", rejectUserName);
			activityArgs.put("p_taskId", p_taskId);
			activityArgs.put("p_rejectComment", p_rejectComment);
			activityStart = WebServicesLog.start(Ambassador4Falcon.class,
					"rejectTask(p_accessToken,p_taskId,p_rejectComment)",
					activityArgs);
			WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
					.getWorkflowTaskInstance(rejectUserId,
							Long.parseLong(p_taskId),
							WorkflowConstants.TASK_ALL_STATES);
			task = (Task) HibernateUtil.get(TaskImpl.class,
					Long.parseLong(p_taskId));
			task.setWorkflowTask(wfTask);

			String rejectComment = EditUtil.utf8ToUnicode(p_rejectComment);
			if (task.getState() == Task.STATE_ACTIVE
					|| task.getState() == Task.STATE_ACCEPTED)
			{
				TaskHelper.rejectTask(rejectUserId, task, rejectComment);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			String message = "Failed to reject task by taskId : " + p_taskId;
			return makeErrorJson(REJECT_TASK, message);
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}

		return rtnStr;
	}
	
	/**
	 * Get all projects relevant information for specified company name.
	 * 
	 * @param p_accessToken
	 *            -- login user's token
	 * @param p_companyName
	 *            -- company name to get data from
	 * @return JSON string, sample is
	 *         "[{"projectID":1037,"projectManager":"gsalliepm","projectName":"Template","l10n_profiles":[{"sourceLocale":"English (United States) [en_US]" , "fileProfiles":[{"sourceFileFormat":"XML","fileProfileName": "file_profile_xml_02","fileProfileId":4,"fileExtensions":"xml"}],"tmProfileId":1,
	 *         "workflows":[{"wfTemplateLocalePair": "English (United States) [en_US] -> German (Germany) [de_DE]","mtProfileId":4,"wfTemplateName":"en_de_template","mtProfileEngine":"MS_Translator","mtProfileConfidenceScore":"90%","mtProfileName":"859MT_1_import_1","wfTemplateId":1313},
	 *         {"wfTemplateLocalePair":"English (United States) [en_US] -> French (France) [fr_FR]" ,"mtProfileId":3,"wfTemplateName":"en_fr_template","mtProfileEngine":"MS_Translator","mtProfileConfidenceScore":"90%","mtProfileName" :"859MT_1","wfTemplateId":1312}, 
	 *         {"wfTemplateLocalePair":"English (United States) [en_US] -> Spanish (Spain) [es_ES]" ,"mtProfileId":5,"wfTemplateName":"en_es_template","mtProfileEngine":"MS_Translator","mtProfileConfidenceScore":"90%","mtProfileName":"859MT_1_import_2","wfTemplateId":1314}],
	 *         "priority":3,"workflowDispatchType":"Automatic","l10nProfileId":12,"tmProfileName":"tm_profile_01","l10nProfileName":"localization_profile_en_01"}]}]".
	 * @throws WebServiceException
	 */
	public String getAllProjectProfiles(String p_accessToken,
			String p_companyName) throws WebServiceException
	{
		checkAccess(p_accessToken, GET_ALL_PROJECT_PROFILES);
		if (StringUtil.isEmpty(p_companyName))
		{
			return makeErrorJson(GET_ALL_PROJECT_PROFILES,
					"Invaild company name");
		}
		String companyId = null;
		try
		{
			companyId = CompanyWrapper.getCompanyIdByName(p_companyName);
		}
		catch (Exception e)
		{
			return makeErrorJson(GET_ALL_PROJECT_PROFILES,
					"No company named with '" + p_companyName + "'.");
		}
		String curUserName = getUsernameFromSession(p_accessToken);
		User curUser = UserUtil.getUserById(UserUtil
				.getUserIdByName(curUserName));
		String curCompanyName = curUser.getCompanyName();
		String curCompanyId = CompanyWrapper.getCompanyIdByName(curCompanyName);

		if (!curCompanyId.equals("1")
				&& !curCompanyName.equalsIgnoreCase(p_companyName))
		{
			return makeErrorJson(GET_ALL_PROJECT_PROFILES,
					"Current user is neither super user nor user from company '"
							+ p_companyName + "'.");
		}

		List<Project> projectList = null;
		WebServicesLog.Start activityStart = null;
		JSONArray projectArray = new JSONArray();
		try
		{
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", curUserName);
			activityArgs.put("p_companyName", p_companyName);
			activityStart = WebServicesLog.start(Ambassador4Falcon.class,
					"getAllProjectProfiles(p_accessToken,p_companyName)",
					activityArgs);
			projectList = ServerProxy.getProjectHandler()
					.getProjectsByCompanyId(Long.parseLong(companyId));
			for (Project project : projectList)
			{
				JSONObject projectJson = new JSONObject();
				projectJson.put("projectID", project.getId());
				projectJson.put("projectName", project.getName());
				projectJson.put("projectManager", project.getProjectManager());
				if (project.getAttributeSet() != null)
				{
					projectJson.put("attributeGroupId", project
							.getAttributeSet().getId());
					projectJson.put("attributeGroupName", project
							.getAttributeSet().getName());
				}
				JSONArray l10ProfileArray = new JSONArray();
				Collection allL10Profiles = ServerProxy.getProjectHandler()
						.getL10ProfilesByProjectId(project.getId());
				Iterator it = allL10Profiles.iterator();
				while (it.hasNext())
				{
					JSONObject l10Json = new JSONObject();
					BasicL10nProfile l10 = (BasicL10nProfile) it.next();
					l10Json.put("l10nProfileId", l10.getId());
					l10Json.put("l10nProfileName", l10.getName());
					l10Json.put("sourceLocale", l10.getSourceLocale()
							.getDisplayName());
					if (l10.isAutoDispatch())
					{
						l10Json.put("workflowDispatchType", "Automatic");
					}
					else
					{
						l10Json.put("workflowDispatchType", "Manual");
					}
					l10Json.put("priority", l10.getPriority());
					// tmProfile
					Iterator itTm = l10.getTmProfiles().iterator();
					while (itTm.hasNext())
					{
						TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) itTm
								.next();
						l10Json.put("tmProfileId", tmProfile.getId());
						l10Json.put("tmProfileName", tmProfile.getName());
					}
					// work flow
					JSONArray workflowArray = new JSONArray();
					GlobalSightLocale[] targets = l10.getTargetLocales();
					for (int i = 0; i < targets.length; i++)
					{
						JSONObject workflowJson = new JSONObject();
						WorkflowTemplateInfo workflowTemplateInfo = (WorkflowTemplateInfo) l10
								.getWorkflowTemplateInfo((GlobalSightLocale) targets[i]);
						workflowJson.put("wfTemplateId",
								workflowTemplateInfo.getId());
						workflowJson.put("wfTemplateName",
								workflowTemplateInfo.getName());
						LocalePair lp = ServerProxy.getLocaleManager()
								.getLocalePairBySourceTargetIds(
										workflowTemplateInfo.getSourceLocale()
												.getId(),
										workflowTemplateInfo.getTargetLocale()
												.getId());
						String localePair = lp.getSource().getDisplayName()
								+ " -> " + lp.getTarget().getDisplayName();
						workflowJson.put("wfTemplateLocalePair", localePair);
						MachineTranslationProfile mt = MTProfileHandlerHelper
								.getMTProfileByRelation(l10.getId(),
										workflowTemplateInfo.getId());
						if (mt != null)
						{
							workflowJson.put("mtProfileId", mt.getId());
							workflowJson.put("mtProfileName",
									mt.getMtProfileName());
							workflowJson.put("mtProfileEngine",
									mt.getMtEngine());
							workflowJson.put("mtProfileConfidenceScore",
									mt.getMtConfidenceScore()+"%");
						}
						workflowArray.put(workflowJson);
					}
					l10Json.put("workflows", workflowArray);
					// fileProfile
					JSONArray fileProfileArray = new JSONArray();
					Iterator itFileProfile = l10.getFileProfiles().iterator();
					while (itFileProfile.hasNext())
					{
						JSONObject fileProfileJson = new JSONObject();
						FileProfileImpl fileProfile = (FileProfileImpl) itFileProfile
								.next();
						if (!fileProfile.isActive())
							continue;
						fileProfileJson.put("fileProfileId",
								fileProfile.getId());
						fileProfileJson.put("fileProfileName",
								fileProfile.getName());
						fileProfileJson
								.put("sourceFileFormat",
										ServerProxy
												.getFileProfilePersistenceManager()
												.getKnownFormatTypeById(
														fileProfile
																.getKnownFormatTypeId(),
														false).getName());
						Vector<Long> extensionIds = fileProfile
								.getFileExtensionIds();
						if (extensionIds.size() == 0)
						{
							fileProfileJson.put("fileExtensions", "All");
						}
						else
						{
							String fileExtensions = "";
							for (int j = 0; j < extensionIds.size(); j++)
							{
								FileExtensionImpl extension = HibernateUtil
										.get(FileExtensionImpl.class,
												extensionIds.get(j));
								fileExtensions += extension.getName() + ",";
							}
							if (fileExtensions != ""
									&& fileExtensions.endsWith(","))
							{
								fileProfileJson.put("fileExtensions",
										fileExtensions
												.substring(0, fileExtensions
														.lastIndexOf(",")));
							}
						}
						fileProfileArray.put(fileProfileJson);
					}
					l10Json.put("fileProfiles", fileProfileArray);

					l10ProfileArray.put(l10Json);
				}
				projectJson.put("l10n_profiles", l10ProfileArray);
				projectArray.put(projectJson);
			}
		}
		catch (Exception e)
		{
			String message = "Unable to get all projects";
			logger.error(e.getMessage(), e);
			message = makeErrorJson(GET_ALL_PROJECT_PROFILES, message);
			throw new WebServiceException(message);
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}
		return projectArray.toString();
	}
	
    private int addWordCountForJson(TargetPage tg, JSONObject jsonObj,
            int threshold, int mtConfidenceScore, boolean includeMTData)
            throws Exception
    {
        boolean isInContextMatch = PageHandler.isInContextMatch(tg
                .getSourcePage().getRequest().getJob());
        PageWordCounts pageWC = tg.getWordCount();

        // 100% match
        int _100MatchWordCount = 0;
        // in context word match
        int inContextWordCount = 0;
        // Context match
        int contextMatchWC = pageWC.getContextMatchWordCount();
        if (isInContextMatch)
        {
            inContextWordCount = pageWC.getInContextWordCount();
            _100MatchWordCount = pageWC.getSegmentTmWordCount();
            contextMatchWC = 0;
        }
        else
        {
        	_100MatchWordCount = pageWC.getTotalExactMatchWordCount();
        	contextMatchWC = 0;
        }

        int hiFuzzyWordCount = pageWC.getThresholdHiFuzzyWordCount();
        int medHiFuzzyWordCount = pageWC.getThresholdMedHiFuzzyWordCount();
        int medFuzzyWordCount = pageWC.getThresholdMedFuzzyWordCount();
        int lowFuzzyWordCount = pageWC.getThresholdLowFuzzyWordCount();
        int noMatchWordCount = pageWC.getThresholdNoMatchWordCount();
        int repetitionsWordCount = pageWC.getRepetitionWordCount();
        int totalWords = pageWC.getTotalWordCount();
        // MT
        int mtTotalWordCount = pageWC.getMtTotalWordCount();
        int mtExactMatchWordCount = pageWC.getMtExactMatchWordCount();
        int mtFuzzyNoMatchWordCount = pageWC.getMtFuzzyNoMatchWordCount();
        int mtRepetitionsWordCount = pageWC.getMtRepetitionsWordCount();

        int noMatchWorcCountForDisplay = lowFuzzyWordCount + noMatchWordCount;
        // If include MT column, need adjust word count according to threshold
        // and MT confidence score.
        if (includeMTData)
        {
            if (mtConfidenceScore == 100)
            {
                _100MatchWordCount = _100MatchWordCount - mtExactMatchWordCount;
            }
            else if (mtConfidenceScore < 100 && mtConfidenceScore >= threshold)
            {
                if (mtConfidenceScore >= 95)
                {
                    hiFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 95 && mtConfidenceScore >= 85)
                {
                    medHiFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 85 && mtConfidenceScore >= 75)
                {
                    medFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 75)
                {
                    noMatchWorcCountForDisplay -= mtFuzzyNoMatchWordCount;
                }
                repetitionsWordCount -= mtRepetitionsWordCount;
            }
            else if (mtConfidenceScore < threshold)
            {
                noMatchWorcCountForDisplay -= mtFuzzyNoMatchWordCount;
                repetitionsWordCount -= mtRepetitionsWordCount;
            }
        }

        // write the information of word count
        jsonObj.put("100%Matches", _100MatchWordCount);
        jsonObj.put("95-99%", hiFuzzyWordCount);

        jsonObj.put("85-94%", medHiFuzzyWordCount);

        jsonObj.put("75-84%", medFuzzyWordCount);

        jsonObj.put("noMatch", noMatchWorcCountForDisplay);

        jsonObj.put("repetitions", repetitionsWordCount);

        if (isInContextMatch)
        {
            jsonObj.put("inContextMatches", inContextWordCount);
        }
        else
        {
            // We use the same key to avoid user's confusion.
            jsonObj.put("inContextMatches", contextMatchWC);
        }

        if (includeMTData)
        {
            jsonObj.put("MT", mtTotalWordCount);
        }

        jsonObj.put("total", totalWords);

        if (includeMTData)
        {
            jsonObj.put("MTConfidenceScore", mtConfidenceScore);
        }

        return totalWords;
    }

    private String getProjectDesc(Job p_job)
    {
        Project p = p_job.getL10nProfile().getProject();
        String d = StringUtils.chomp(p.getDescription());
        String desc = null;
        if (d == null || d.length() == 0)
            desc = p.getName();
        else
            desc = p.getName() + "-" + d;
        return desc;
    }

    private void setFilePathName(TargetPage tg, JSONObject jsonObj)
            throws Exception
    {
        String fileFullName = tg.getSourcePage().getExternalPageId();
        String filePath = fileFullName;
        String fileName = " ";
        if (fileFullName.indexOf("/") > -1)
        {
            fileName = fileFullName.substring(
                    fileFullName.lastIndexOf("/") + 1, fileFullName.length());
            filePath = fileFullName.substring(0, fileFullName.lastIndexOf("/"));
        }
        else if (fileFullName.indexOf("\\") > -1)
        {
            fileName = fileFullName.substring(
                    fileFullName.lastIndexOf("\\") + 1, fileFullName.length());
            filePath = fileFullName
                    .substring(0, fileFullName.lastIndexOf("\\"));
        }
        jsonObj.put("filePath", filePath);
        jsonObj.put("fileName", fileName);

    }

    /**
     * Exports the job specified by job name
     * 
     * @param p_accessToken
     * @param p_jobName
     *            String Job name
     * @return String in JSON. A sample is like:
     * 				{"status":"Export Request Sent","workflowLocale":"All Locales","jobName":"3801_656474062"}
     * @throws WebServiceException
     */
    public String exportJob(String p_accessToken, String p_jobName)
            throws WebServiceException
	{
		checkAccess(p_accessToken, EXPORT_JOB);
		String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.JOBS_EXPORT);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
		String jobName = p_jobName;
		WebServicesLog.Start activityStart = null;
		JSONObject jsonObj = new JSONObject();
		try
		{
			String userName = getUsernameFromSession(p_accessToken);
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", userName);
			activityArgs.put("jobName", p_jobName);
			activityStart = WebServicesLog.start(Ambassador4Falcon.class,
					"exportJob(p_accessToken, p_jobName)", activityArgs);
			Job job = queryJob(jobName, p_accessToken, jsonObj);
			if (job.getDisplayState().equalsIgnoreCase("ready"))
			{
				return makeErrorJson(EXPORT_JOB, p_jobName
						+ " is ready state , can not be export.");
			}
			Object[] workflows = job.getWorkflows().toArray();
			long projectId = job.getL10nProfile().getProject().getId();
			User projectMgr = ServerProxy.getProjectHandler()
					.getProjectById(projectId).getProjectManager();

			// export all workflow
			logger.info("Exporting all " + workflows.length
					+ " workflows for job " + jobName);
			for (int i = 0; i < workflows.length; i++)
			{
				Workflow w = (Workflow) workflows[i];
				if (!w.getState().equals(Workflow.IMPORT_FAILED)
						&& !w.getState().equals(Workflow.CANCELLED))
				{
					exportSingleWorkflow(job, w, projectMgr);
				}
			}
			jsonObj.put("jobName", EditUtil.encodeXmlEntities(jobName));
			jsonObj.put("workflowLocale", "All Locales");
			jsonObj.put("status", "Export Request Sent");
			return jsonObj.toString();

		}
		catch (Exception e)
		{
			logger.error("exportJob()", e);
			String message = "Could not export job " + jobName;
			return makeErrorJson(EXPORT_JOB, message);
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}
	}
    
    /**
     * Exports the job. If p_workflowLocale is null then all pages for all
     * workflows are exported, otherwise the specific workflow corresponding to
     * the locale is exported.
     * 
     * @param p_jobName
     *            -- name of job
     * @param p_workflowLocale
     *            -- locale of workflow to export
     * @return String in JSON. A sample is like:
     * 				{"status":"Export Request Sent","workflowLocale":"de_DE","jobName":"3801_656474062"}
     * @exception WebServiceException
     */
	public String exportWorkflow(String p_accessToken, String p_jobName,
			String p_workflowLocale) throws WebServiceException
	{
		checkAccess(p_accessToken, EXPORT_WORKFLOW);
		String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.JOB_WORKFLOWS_EXPORT);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
		String jobName = p_jobName;
		String workflowLocale = p_workflowLocale;
		String returnXml = "";
		WebServicesLog.Start activityStart = null;
		JSONObject jsonObj = new JSONObject();
		try
		{
			String userName = getUsernameFromSession(p_accessToken);
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", userName);
			activityArgs.put("jobName", p_jobName);
			activityArgs.put("workflowLocale", p_workflowLocale);
			activityStart = WebServicesLog
					.start(Ambassador.class,
							"exportWorkflow(p_accessToken, p_jobName,p_workflowLocale)",
							activityArgs);
			Job job = queryJob(jobName, p_accessToken, jsonObj);
			Object[] workflows = job.getWorkflows().toArray();
			long projectId = job.getL10nProfile().getProject().getId();
			User projectMgr = ServerProxy.getProjectHandler()
					.getProjectById(projectId).getProjectManager();
			boolean didExport = false;

			if (workflowLocale == null)
			{
				// export all workflow
				logger.info("Exporting all " + workflows.length
						+ " workflows for job " + jobName);
				for (int i = 0; i < workflows.length; i++)
				{
					Workflow w = (Workflow) workflows[i];
					if (!w.getState().equals(Workflow.IMPORT_FAILED)
							&& !w.getState().equals(Workflow.CANCELLED))
					{
						exportSingleWorkflow(job, w, projectMgr);
					}
				}
				didExport = true;
			}
			else
			{
				// export just one workflow
				Locale locale = GlobalSightLocale
						.makeLocaleFromString(workflowLocale);
				logger.info("Job " + jobName + " has " + workflows.length
						+ " workflow.");
				for (int i = 0; i < workflows.length; i++)
				{
					Workflow w = (Workflow) workflows[i];
					Locale wLocale = w.getTargetLocale().getLocale();
					if (locale.equals(wLocale))
					{
						exportSingleWorkflow(job, w, projectMgr);
						didExport = true;
						break;
					}
				}
			}

			if (didExport == false)
				throw new Exception("No workflow for locale " + workflowLocale);

			jsonObj.put("jobName", EditUtil.encodeXmlEntities(jobName));
			if (workflowLocale == null)
				jsonObj.put("workflowLocale", "All Locales");
			else jsonObj.put("workflowLocale", workflowLocale);

			jsonObj.put("status", "Export Request Sent");
			returnXml = jsonObj.toString();
		}
		catch (Exception e)
		{
			logger.error("exportWorkflow()", e);
			String message = "Could not export workflow for job " + jobName;
			return makeErrorJson(EXPORT_WORKFLOW, message);
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}
		return returnXml;
	}
    
	/**
     * Get exported job files (do not care if the workflow is "EXPORTED")
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     *            Job name
     * @return String in JSON. A sample is like:
     * 				{"paths":["de_DE/17/accuracy_test_results.htm","de_DE/17/multiple_channels.htm","de_DE/17/stability_test_results.htm"],"root":"http://10.10.215.38:8080/globalsight/cxedocs/allie"}
     * @throws WebServiceException
     */
    public String getJobExportFiles(String p_accessToken, String p_jobName)
            throws WebServiceException
	{
		checkAccess(p_accessToken, GET_JOB_EXPORT_FILES);
		String jobsView = checkPermissionReturnStr(p_accessToken,
				Permission.JOBS_VIEW);
		if (StringUtil.isNotEmpty(jobsView))
			return jobsView;
		String jobsExport = checkPermissionReturnStr(p_accessToken,
				Permission.JOBS_EXPORT);
		if (StringUtil.isNotEmpty(jobsExport))
			return jobsExport;
		
		
		String jobName = p_jobName;
		WebServicesLog.Start activityStart = null;
		JSONObject jsonObj = new JSONObject();
		Job job = queryJob(jobName, p_accessToken, jsonObj);
		String jobCompanyId = String.valueOf(job.getCompanyId());
		String curUserName = getUsernameFromSession(p_accessToken);
		User curUser = getUser(curUserName);
		Company curCompany = getCompanyByName(curUser.getCompanyName());
		if (curCompany.getId() != 1
				&& !isInSameCompany(curUserName, jobCompanyId))
			throw new WebServiceException(
					"Current user is not super user,cannot access the job which is not in the same company with current user");

		String status = job.getState();
		if (status == null)
		{
			return makeErrorJson(GET_JOB_EXPORT_FILES, "Job " + jobName
					+ " does not exist.");
		}

		JobFiles jobFiles = new JobFiles();

		StringBuilder prefix = new StringBuilder();
		prefix.append(getUrl()).append("/cxedocs/");
		String company = CompanyWrapper.getCompanyNameById(job.getCompanyId());
		prefix.append(URLEncoder.encode(company, "utf-8"));
		jobFiles.setRoot(prefix.toString());

		Set<String> passoloFiles = new HashSet<String>();
		long fileProfileId = -1l;
		FileProfile fp = null;
		FileProfilePersistenceManager fpManager = null;
		boolean isXLZFile = false;

		try
		{
			fpManager = ServerProxy.getFileProfilePersistenceManager();
		}
		catch (Exception e)
		{
			logger.error("Cannot get file profile manager.", e);
			return makeErrorJson(GET_JOB_EXPORT_FILES,
					"Cannot get file profile manager." + e.getMessage());
		}
		try
		{
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", curUserName);
			activityArgs.put("jobName", p_jobName);
			activityStart = WebServicesLog
					.start(Ambassador.class,
							"getJobExportFiles(p_accessToken, p_jobName)",
							activityArgs);
			for (Workflow w : job.getWorkflows())
			{
				if (Workflow.CANCELLED.equals(w.getState())
						|| Workflow.PENDING.equals(w.getState())
						|| Workflow.EXPORT_FAILED.equals(w.getState())
						|| Workflow.IMPORT_FAILED.equals(w.getState()))
					continue;

				ArrayList<String> fileList = new ArrayList<String>();
				for (TargetPage page : w.getTargetPages())
				{
					SourcePage sPage = page.getSourcePage();
					if (sPage != null && sPage.isPassoloPage())
					{
						String p = sPage.getPassoloFilePath();
						p = p.replace("\\", "/");
						p = p.substring(p.indexOf("/") + 1);
						passoloFiles.add(p);
						if (fileList.contains(p))
							continue;
						else
						{
							fileList.add(p);
						}

						continue;
					}
					fileProfileId = sPage.getRequest().getFileProfileId();
					fp = fpManager.getFileProfileById(fileProfileId, false);
					if (fpManager.isXlzReferenceXlfFileProfile(fp.getName()))
						isXLZFile = true;

					String path = page.getExternalPageId();
					path = path.replace("\\", "/");
                    if (StringUtil.isNotEmpty(fp.getScriptOnExport()))
                    {
                        path = handlePathForScripts(path, job);
                    }
					int index = path.indexOf("/");
					path = path.substring(index);
					path = getRealFilePathForXliff(path, isXLZFile);

					if (fileList.contains(path))
						continue;
					else
					{
						fileList.add(path);
					}
					StringBuffer allPath = new StringBuffer();
					allPath.append(page.getGlobalSightLocale());
					for (String s : path.split("/"))
					{
						if (s.length() > 0)
						{
							allPath.append("/").append(
									URLEncoder.encode(s, "utf-8"));
						}
					}
					jobFiles.addPath(allPath.toString());

					isXLZFile = false;
				}
			}

			for (String path : passoloFiles)
			{
				StringBuffer allPath = new StringBuffer();
				allPath.append("passolo");
				for (String s : path.split("/"))
				{
					if (s.length() > 0)
					{
						allPath.append("/").append(
								URLEncoder.encode(s, "utf-8"));
					}
				}
				jobFiles.addPath(allPath.toString());
			}
			JSONArray array = new JSONArray();
			List<String> listPaths = jobFiles.getPaths();
			for (String paths : listPaths)
			{
				array.put(paths);
			}
			jsonObj.put("paths", array);
			String root = jobFiles.getRoot();
			jsonObj.put("root", root);
			return jsonObj.toString();
		}
		catch (Exception e)
		{
			logger.error("Error found in getJobExportFiles.", e);
			return makeErrorJson(GET_JOB_EXPORT_FILES, e.getMessage());
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}
	}
    
    /**
     * Return exported files information for job's "EXPORTED" state workflows.
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     *            Job name
     * @param workflowLocale
     *            Locale of workflow, it can accept fr_FR, fr-FR, fr_fr formats
     *            If it is null, all "EXPORTED" workflows' exported files info
     *            will be returned.
     * @return String in JSON. A sample is like:
     * 				{"paths":["de_DE/17/accuracy_test_results.htm","de_DE/17/multiple_channels.htm","de_DE/17/stability_test_results.htm"],"root":"http://10.10.215.38:8080/globalsight/cxedocs/allie"}
     * @throws WebServiceException
     */
	public String getJobExportWorkflowFiles(String p_accessToken,
			String p_jobName, String workflowLocale) throws WebServiceException
	{
		checkAccess(p_accessToken, GET_JOB_EXPORT_WORKFLOW_FILES);
		String jobsView = checkPermissionReturnStr(p_accessToken,
				Permission.JOBS_VIEW);
		if (StringUtil.isNotEmpty(jobsView))
			return jobsView;
		String jobsExport = checkPermissionReturnStr(p_accessToken,
				Permission.JOBS_EXPORT);
		if (StringUtil.isNotEmpty(jobsExport))
			return jobsExport;
		
		WebServicesLog.Start activityStart = null;
		JSONObject jsonObj = new JSONObject();
		String jobName = p_jobName;
		Job job = queryJob(jobName, p_accessToken, jsonObj);
		String jobCompanyId = String.valueOf(job.getCompanyId());
		String curUserName = getUsernameFromSession(p_accessToken);
		User curUser = getUser(curUserName);
		Company curCompany = getCompanyByName(curUser.getCompanyName());
		if (curCompany.getId() != 1
				&& !isInSameCompany(curUserName, jobCompanyId))
			throw new WebServiceException(
					"Current user is not super user,cannot access the job which is not in the same company with current user");

		String status = job.getState();
		if (status == null)
		{
			return makeErrorJson(GET_JOB_EXPORT_WORKFLOW_FILES, "Job "
					+ jobName + " does not exist.");
		}

		JobFiles jobFiles = new JobFiles();
		long fileProfileId = -1l;
		FileProfile fp = null;
		FileProfilePersistenceManager fpManager = null;
		boolean isXLZFile = false;

		try
		{
			fpManager = ServerProxy.getFileProfilePersistenceManager();
		}
		catch (Exception e)
		{
			logger.error("Cannot get file profile manager.", e);
			return makeErrorJson(GET_JOB_EXPORT_WORKFLOW_FILES,
					"Cannot get file profile manager." + e.getMessage());
		}

		StringBuilder prefix = new StringBuilder();
		prefix.append(getUrl()).append("/cxedocs/");
		String company = CompanyWrapper.getCompanyNameById(job.getCompanyId());
		prefix.append(URLEncoder.encode(company, "utf-8"));
		jobFiles.setRoot(prefix.toString());

		Set<String> passoloFiles = new HashSet<String>();

		try
		{
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", curUserName);
			activityArgs.put("jobName", p_jobName);
			activityArgs.put("workflowLocale", workflowLocale);
			activityStart = WebServicesLog
					.start(Ambassador.class,
							"getJobExportWorkflowFiles(p_accessToken, p_jobName,workflowLocale)",
							activityArgs);
			for (Workflow w : job.getWorkflows())
			{
				if (StringUtil.isEmpty(workflowLocale))
				{
					// need to download all 'Exported' workflow files
					if (!Workflow.EXPORTED.equals(w.getState()))
						continue;
				}
				else
				{
					// download workflow files
					if (!isWorkflowOfLocaleExported(w, workflowLocale))
						continue;
				}
				ArrayList<String> fileList = new ArrayList<String>();
				for (TargetPage page : w.getTargetPages())
				{
					SourcePage sPage = page.getSourcePage();
					if (sPage != null && sPage.isPassoloPage())
					{
						String p = sPage.getPassoloFilePath();
						p = p.replace("\\", "/");
						p = p.substring(p.indexOf("/") + 1);
						passoloFiles.add(p);
						if (fileList.contains(p))
							continue;
						else
						{
							fileList.add(p);
						}

						continue;
					}

					fileProfileId = sPage.getRequest().getFileProfileId();
					fp = fpManager.getFileProfileById(fileProfileId, false);
					if (fpManager.isXlzReferenceXlfFileProfile(fp.getName()))
						isXLZFile = true;

					String path = page.getExternalPageId();
					path = path.replace("\\", "/");
                    if (StringUtil.isNotEmpty(fp.getScriptOnExport()))
                    {
                        path = handlePathForScripts(path, job);
                    }
					int index = path.indexOf("/");
					path = path.substring(index);
					path = getRealFilePathForXliff(path, isXLZFile);

					if (fileList.contains(path))
						continue;
					else
					{
						fileList.add(path);
					}

					StringBuffer allPath = new StringBuffer();
					allPath.append(page.getGlobalSightLocale());
					for (String s : path.split("/"))
					{
						if (s.length() > 0)
						{
							allPath.append("/").append(
									URLEncoder.encode(s, "utf-8"));
						}
					}
					jobFiles.addPath(allPath.toString());
				}
			}

			for (String path : passoloFiles)
			{
				StringBuffer allPath = new StringBuffer();
				allPath.append("passolo");
				for (String s : path.split("/"))
				{
					if (s.length() > 0)
					{
						allPath.append("/").append(
								URLEncoder.encode(s, "utf-8"));
					}
				}
				jobFiles.addPath(allPath.toString());
			}
			JSONArray array = new JSONArray();
			List<String> listPaths = jobFiles.getPaths();
			for (String paths : listPaths)
			{
				array.put(paths);
			}
			jsonObj.put("paths", array);
			String root = jobFiles.getRoot();
			jsonObj.put("root", root);
			return jsonObj.toString();
		}
		catch (Exception e)
		{
			logger.error("Error found in " + GET_JOB_EXPORT_WORKFLOW_FILES, e);
			return makeErrorJson(GET_JOB_EXPORT_WORKFLOW_FILES, e.getMessage());
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}
	}
    
    private String handlePathForScripts(String path, Job job)
    {
        path = path.replace("\\", "/");
        String finalPath = path;
        // for new scripts on import/export
        if (path.contains("/PreProcessed_" + job.getId() + "_"))
        {
            finalPath = path.replace(path.substring(
                    path.lastIndexOf("/PreProcessed_" + job.getId() + "_"),
                    path.lastIndexOf("/")), "");
        }
        // compatible codes for old import/export
        else
        {
            int index = path.lastIndexOf("/");
            if (index > -1)
            {
                String fileName = path.substring(index + 1);
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                String rest = path.substring(0, index);
                if (rest.endsWith("/" + fileName))
                {
                    finalPath = rest + "." + extension;
                }
            }
        }

        return finalPath;
    }

    private boolean isWorkflowOfLocaleExported(Workflow workflow, String locale)
    {
        if (workflow == null || StringUtil.isEmpty(locale))
            return false;

        String workflowState = workflow.getState();
        if (!workflowState.equals(Workflow.EXPORTED))
            return false;

        String lowerWorkflowLocale = workflow.getTargetLocale().toString()
                .toLowerCase();
        String lowerLocale = locale.replace('-', '_').toLowerCase();
        if (lowerWorkflowLocale.equals(lowerLocale))
            return true;
        else
            return false;
    }
    
    private String getUrl()
    {
        return AmbassadorUtil.getCapLoginOrPublicUrl();
    }
    
    private String getRealFilePathForXliff(String path, boolean isXLZFile)
    {
        if (StringUtil.isEmpty(path))
            return path;

        int index = -1;
        index = path.lastIndexOf(".sub/");
        if (index > 0)
        {
            // one big xliff file is split to some sub-files
            path = path.substring(0, index);
        }
        if (isXLZFile)
        {
            path = path.substring(0, path.lastIndexOf("/"));
            path += ".xlz";
        }

        return path;
    }
    
    /**
     * Check if the company info of job or project is the same with current user
     */
    private boolean isInSameCompany(String p_userName, String p_companyId)
    {
        if (p_userName == null || p_userName.trim().equals(""))
            return false;
        if (p_companyId == null || p_companyId.trim().equals(""))
            return false;
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(p_userName);
            String userCompanyId = ServerProxy.getJobHandler()
                    .getCompany(user.getCompanyName()).getIdAsLong().toString();
            return userCompanyId.equals(p_companyId) ? true : false;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    /**
     * Exports all target pages of the given workflow
     * 
     * @param p_job
     *            Entity of Job
     * @param p_workflow
     *            Entity of Workflow
     * @param p_user
     *            Specified user information
     * @throws Exception
     */
    private void exportSingleWorkflow(Job p_job, Workflow p_workflow,
            User p_user) throws Exception
    {
        List targetPages = p_workflow.getTargetPages();
        ArrayList pageIds = new ArrayList();
        for (int j = 0; j < targetPages.size(); j++)
        {
            TargetPage tp = (TargetPage) targetPages.get(j);
            pageIds.add(tp.getIdAsLong());
        }
        ExportParameters ep = new ExportParameters(p_workflow);
        boolean isTargetPage = true;
        ArrayList wfIds = new ArrayList();
        wfIds.add(p_workflow.getIdAsLong());
        Long taskId = null;

        logger.info("Exporting workflow  "
                + p_workflow.getTargetLocale().toString() + " for job "
                + p_job.getJobName());
        long exportBatchId = ServerProxy.getExportEventObserver()
                .notifyBeginExportTargetBatch(p_job, p_user, pageIds, wfIds,
                        taskId, ExportBatchEvent.INTERIM_PRIMARY);
        ServerProxy.getPageManager().exportPage(ep, pageIds, isTargetPage,
                exportBatchId);
    }
    
    /**
     * Gets out the Job object corresponding to the job name assuming that is
     * unique
     * 
     * @param p_jobName
     *            Job name
     * @param p_accessToken
     * @return Job Return job object if there exist.
     * @exception WebServiceException
     */
	private Job queryJob(String p_jobName, String p_accessToken,
			JSONObject jsonObj) throws WebServiceException
	{
		Connection connection = null;
		PreparedStatement query = null;
		ResultSet results = null;

		try
		{
			connection = ConnectionPool.getConnection();
			String condition = appendJobCondition(p_jobName);

			User user = getUser(getUsernameFromSession(p_accessToken));
			long companyId = CompanyWrapper.getCompanyByName(
					user.getCompanyName()).getId();

			String sql = null;
			if (companyId != 1)
			{
				sql = "SELECT ID FROM JOB WHERE COMPANY_ID=? AND " + condition;
				query = connection.prepareStatement(sql);
				query.setLong(1, companyId);
				query.setString(2, p_jobName);
			}
			else
			{
				sql = "SELECT ID FROM JOB WHERE " + condition;
				query = connection.prepareStatement(sql);
				query.setString(1, p_jobName);
			}

			results = query.executeQuery();
			if (results.next())
			{
				long id = results.getLong(1);
				Job job = ServerProxy.getJobHandler().getJobById(id);
				return job;
			}
			else
			{
				String message = NOT_IN_DB + p_jobName;
				message = jsonObj.put("queryJob", message).toString();
				/*
				 * Do not change this Exception message "This job is not ready
				 * for query", because getStatus() will deal with it in
				 * catch(Exception e) and Desktop Icon
				 * 
				 * com/globalsight/action/AddCommentAction.java :
				 * executeWithThread(String args[])
				 */
				throw new WebServiceException(message);
			}
		}
		catch (ConnectionPoolException cpe)
		{
			String message = "Unable to connect to database to get job status.";
			logger.error(message, cpe);
			try
			{
				message = jsonObj.put("queryJob", message).toString();
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			throw new WebServiceException(message);
		}
		catch (SQLException sqle)
		{
			String message = "Unable to query DB for job status.";
			logger.error(message, sqle);
			try
			{
				message = jsonObj.put("queryJob", message).toString();
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			throw new WebServiceException(message);
		}
		catch (WebServiceException le)
		{
			throw le;
		}
		catch (Exception e)
		{
			String message = "Unable to get job information from System4.";
			logger.error(message, e);
			try
			{
				message = jsonObj.put("queryJob", message).toString();
			}
			catch (JSONException e1)
			{
				e1.printStackTrace();
			}
			throw new WebServiceException(message);
		}
		finally
		{
			releaseDBResource(results, query, connection);
		}
	}
    
    private String appendJobCondition(String p_jobName)
    {
        String condition = "NAME=?";

        try
        {
            int index = p_jobName.lastIndexOf("_");
            if (index > -1)
            {
                String random = p_jobName.substring(index + 1);
                if (random != null && random.length() > 6
                        && StringUtils.isNumeric(random))
                {
                    condition = "(NAME=? OR NAME LIKE '%" + random + "')";
                }
            }
        }
        catch (Exception ignore)
        {

        }

        return condition;
    }
    
    /**
     * Create new user
     * 
     * @param p_accessToken
     *            String Access token. REQUIRED.
     * @param p_userId
     *            String User ID. REQUIRED.
     *            Example: 'qaadmin'
     * @param p_password
     *            String Password. It requires 8 characters at least. REQUIRED.
     * @param p_firstName
     *            String First name. It can have 100 characters at most. REQUIRED.
     * @param p_lastName
     *            String Last name. It can have 100 characters at most. REQUIRED.
     * @param p_email
     *            String Email address. REQUIRED.
     *            If the email address is not vaild then the user's status will be set up as inactive
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to.
     *            The element in the array is the name of permission group.
     *            Example: [{"Administrator"}, {"ProjectManager"}]
     * @param p_roles
     *            Roles String information of user. It uses a string with XML format to mark all roles information of user. REQUIRED.
     *            Example:
     *              <?xml version=\"1.0\"?>
     *                <roles>
     *                  <role>
     *                    <sourceLocale>en_US</sourceLocale>
     *                    <targetLocale>de_DE</targetLocale>
     *                    <activities>
     *                      <activity>
     *                        <name>Dtp1</name>
     *                      </activity>
     *                      <activity>
     *                        <name>Dtp2</name>
     *                      </activity>
     *                    </activities>
     *                  </role>
     *                </roles>
     *                 If super user create user,Roles Example:
     *                <?xml version=\"1.0\"?>
     *					<roles>
     *						<role>
     *							<companyName>allie</companyName>
     *							<sourceLocale>en_US</sourceLocale>
     *							<targetLocale>de_DE</targetLocale>
     *							<activities>
     *								<activity>
     *								<name>Dtp1</name>
     *								</activity>
     *								<activity>
     *									<name>Dtp2</name>
     *								</activity>
     *							</activities>
     *						</role>
     *					</roles>
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project. REQUIRED.
     * @param p_projectIds
     *            String[] ID of projects which user should be included in. If p_isInAllProject is true, this will not take effect.
     *            Example: [{"1"}, {"3"}]
     * @return int Return code 
     *        0 -- Success 
     *        1 -- Invalid access token 
     *        2 -- Invalid user id 
     *        3 -- Cannot create super user
     *        4 -- User exists
     *        5 -- User does NOT exist
     *        6 -- User is NOT in the same company with logged user
     *        7 -- Invalid user password 
     *        8 -- Invalid first name 
     *        9 -- Invalid last name 
     *       10 -- Invalid email address 
     *       11 -- Invalid permission groups 
     *       12 -- Invalid project information 
     *       13 -- Invalid role information 
     *       14-- Current login user does not have enough permission
     *       -1 -- Unknown exception
     * @throws WebServiceException
     */
	public int createUser(String p_accessToken, String p_userId,
			String p_password, String p_firstName, String p_lastName,
			String p_email, String[] p_permissionGrps, String p_roles,
			boolean p_isInAllProject, String[] p_projectIds)
			throws WebServiceException
	{
		AmbassadorHelper helper = new AmbassadorHelper();
		return helper.createUser(p_accessToken, p_userId, p_password,
				p_firstName, p_lastName, p_email, p_permissionGrps, null,
				p_roles, p_isInAllProject, p_projectIds);
	}

    /**
     * Modify user
     * 
     * @param p_accessToken
     *            String Access token. REQUIRED.
     * @param p_userId
     *            String User ID. REQUIRED.
     *            Example: 'qaadmin'
     * @param p_password
     *            String Password. It requires 8 characters at least.
     * @param p_firstName
     *            String First name. It can have 100 characters at most.
     * @param p_lastName
     *            String Last name. It can have 100 characters at most.
     * @param p_email
     *            String Email address.
     *            If the email address is invalid, the user status will be set up as inactive.
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to.
     *            The element in the array is the name of permission group.
     *            Example: [{"Administrator"}, {"ProjectManager"}]
     * @param p_roles
     *            Roles String information of user. It uses a string with XML format to mark all roles information of user.
     *            Example:
     *              <?xml version=\"1.0\"?>
     *                <roles>
     *                  <role>
     *                    <sourceLocale>en_US</sourceLocale>
     *                    <targetLocale>de_DE</targetLocale>
     *                    <activities>
     *                      <activity>
     *                        <name>Dtp1</name>
     *                      </activity>
     *                      <activity>
     *                        <name>Dtp2</name>
     *                      </activity>
     *                    </activities>
     *                  </role>
     *                </roles>
     *                 If super user modify user,Roles Example:
     *                <?xml version=\"1.0\"?>
     *					<roles>
     *						<role>
     *							<companyName>allie</companyName>
     *							<sourceLocale>en_US</sourceLocale>
     *							<targetLocale>de_DE</targetLocale>
     *							<activities>
     *								<activity>
     *								<name>Dtp1</name>
     *								</activity>
     *								<activity>
     *									<name>Dtp2</name>
     *								</activity>
     *							</activities>
     *						</role>
     *					</roles>
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project. REQUIRED.
     * @param p_projectIds
     *            String[] ID of projects which user should be included in. If p_isInAllProject is true, this will not take effect.
     *            Example: [{"1"}, {"3"}]
     * @return int Return code 
     *        0 -- Success 
     *        1 -- Invalid access token 
     *        2 -- Invalid user id 
     *        3 -- Cannot create super user
     *        4 -- User exists
     *        5 -- User does NOT exist
     *        6 -- User is NOT in the same company with logged user
     *        7 -- Invalid user password 
     *        8 -- Invalid first name 
     *        9 -- Invalid last name 
     *       10 -- Invalid email address 
     *       11 -- Invalid permission groups 
     *       12 -- Invalid project information 
     *       13 -- Invalid role information 
     *       14-- Current login user does not have enough permission
     *       -1 -- Unknown exception
     * @throws WebServiceException
     */
	public int modifyUser(String p_accessToken, String p_userId,
			String p_password, String p_firstName, String p_lastName,
			String p_email, String[] p_permissionGrps, String p_roles,
			boolean p_isInAllProject, String[] p_projectIds)
			throws WebServiceException
	{
		AmbassadorHelper helper = new AmbassadorHelper();
		return helper.modifyUser(p_accessToken, p_userId, p_password,
				p_firstName, p_lastName, p_email, p_permissionGrps, null,
				p_roles, p_isInAllProject, p_projectIds);
	}

    /**
     * Reassign task to other translators
     *  
     * @param p_accessToken
     *            String Access token
     * @param p_taskId
     *            String ID of task
     *            Example: "10"
     * @param p_users
     *            String[] Users' information who will be reassigned to. The element in the array is [{userid}].
     *            Example: ["qaadmin", "qauser"]
     * @return 
     *            Return null if the reassignment executes successfully.
     *            Otherwise, it will throw exception or return error message
     * @throws WebServiceException
     */
    public String taskReassign(String p_accessToken, String p_taskId,
            String[] p_users) throws WebServiceException
    {
        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.taskReassign(p_accessToken, p_taskId, p_users);
    }

    /**
     * Get all workflow template basic information available to this user.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @return -- String in JSON style, an example is:
     * [{"targetLocale":"German (Germany) [de_DE]","sourceLocale":"English (United States) [en_US]","projectManagerId":"yorkadmin","workflowTemplateName":"en_US_de_DE_T","companyName":"Welocalize","projectName":"Project_A"},
     *  {"targetLocale":"German (Germany) [de_DE]","sourceLocale":"English (United States) [en_US]","projectManagerId":"yorkadmin","workflowTemplateName":"en_US_de_DE_TR","companyName":"Welocalize","projectName":"Project_A"}]
     *  
     * @throws WebServiceException
     */
    @SuppressWarnings("unchecked")
    public String getWorkflowTemplateNames(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_WORKFLOW_TEMPLATE_NAMES);
    	String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.WORKFLOWS_VIEW);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
        String json = "";
        try
        {
            WfTemplateSearchParameters params = new WfTemplateSearchParameters();
            List<WorkflowTemplateInfo> templates =
                    (List<WorkflowTemplateInfo>) ServerProxy
                            .getProjectHandler().findWorkflowTemplates(params);

            if (templates == null || templates.size() < 1)
            {
                return makeErrorJson(GET_WORKFLOW_TEMPLATE_NAMES,
                        "Cannot get workflow template names correctly.");
            }

            Locale uiLocale = getUILocale(p_accessToken);
            JSONArray array = new JSONArray();
            for (WorkflowTemplateInfo wftInfo : templates)
            {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("workflowTemplateName", wftInfo.getName());
                jsonObj.put("projectName", wftInfo.getProject().getName());
                jsonObj.put("sourceLocale", wftInfo.getSourceLocale()
                        .getDisplayName(uiLocale));
                jsonObj.put("targetLocale", wftInfo.getTargetLocale()
                        .getDisplayName(uiLocale));
                jsonObj.put("projectManagerId", wftInfo.getProjectManagerId());
                String comName = ServerProxy.getJobHandler()
                        .getCompanyById(wftInfo.getCompanyId())
                        .getCompanyName();
                jsonObj.put("companyName", comName);
                array.put(jsonObj);
            }
            json = array.toString();
        }
        catch (Exception e)
        {
            return makeErrorJson(
                    GET_WORKFLOW_TEMPLATE_NAMES,
                    "Cannot get workflow template names correctly."
                            + e.getMessage());
        }

        return json;
    }

    /**
     * Get a listing of the activity names and their assignees for the given
     * workflow template name and company name.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_workflowTemplateName
     *            -- workflow template name
     * @param p_companyName
     *            -- company name
     * @return String in JSON style, an example is:
     * [{"Participants":"yorkanyone","AvailableAssignees":"yorkanyone,yorkadmin,superpm","sequence":1,"activityName":"Translation1_2","IncomingArrows":"Action9","OutgoingArrows":"Action15"},
     *  {"Participants":"yorkadmin,superpm","AvailableAssignees":"yorkanyone,yorkadmin,superpm","sequence":2,"activityName":"review_linguistc1_2","IncomingArrows":"Action16","OutgoingArrows":"Action11"}]
     *  
     * @throws WebServiceException
     * 
     */
    @SuppressWarnings("unchecked")
    public String getWorkflowTemplateInfo(String p_accessToken,
            String p_workflowTemplateName, String p_companyName)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_WORKFLOW_TEMPLATE_INFO);
    	String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.WORKFLOWS_VIEW);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
        if (StringUtil.isEmpty(p_workflowTemplateName))
        {
            return makeErrorJson(GET_WORKFLOW_TEMPLATE_INFO,
                    "empty workflowTemplateName.");
        }
        if (StringUtil.isEmpty(p_companyName)
                || getCompanyByName(p_companyName) == null)
        {
            return makeErrorJson(GET_WORKFLOW_TEMPLATE_INFO,
                    "empty companyName or no such company '" + p_companyName + "'.");
        }

		User curUser = getUser(getUsernameFromSession(p_accessToken));
		Company curcompany = getCompanyByName(curUser.getCompanyName());

		if (curcompany.getId() != 1
				&& !curcompany.getName().equalsIgnoreCase(p_companyName))
		{
			return makeErrorJson(GET_WORKFLOW_TEMPLATE_INFO,
					"Invaild comoany name parameter.");
		}
		
        String json = "";
        WebServicesLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("workflowTemplateName", p_workflowTemplateName);
            activityArgs.put("companyName", p_companyName);
            activityStart = WebServicesLog
                    .start(Ambassador4Falcon.class,
                            "getWorkflowTemplateInfo(accessToken, workflowTemplateName, companyName)",
                            activityArgs);

            WfTemplateSearchParameters params = new WfTemplateSearchParameters();
            params.setWorkflowName(p_workflowTemplateName);
            List<WorkflowTemplateInfo> templates =
                    (List<WorkflowTemplateInfo>) ServerProxy
                            .getProjectHandler().findWorkflowTemplates(params);

            if (templates == null || templates.size() < 1)
            {
                return makeErrorJson(GET_WORKFLOW_TEMPLATE_INFO,
                        "Cannot get WorkflowTemplateInfo with name '"
                                + p_workflowTemplateName + "'.");
            }

            // Filter to ensure the template belongs to specified company.
            Company company = getCompanyByName(p_companyName);
            for (Iterator<WorkflowTemplateInfo> wftInfoIter = templates
                    .iterator(); wftInfoIter.hasNext();)
            {
                WorkflowTemplateInfo wftInfo = wftInfoIter.next();
                // If template is not in specified company, ignore it.
                if (wftInfo.getCompanyId() != company.getId())
                {
                    wftInfoIter.remove();
                }
            }

            if (templates.size() < 1)
            {
                return makeErrorJson(GET_WORKFLOW_TEMPLATE_INFO,
                        "There is no workflow template that matches the name '"
                                + p_workflowTemplateName + "' and company '"
                                + p_companyName + "'.");
            }

            JSONArray array = new JSONArray();
            // generally the size of "templates" should be 1.
            WorkflowTemplateInfo wftInfo = templates.get(0);
            String srcLocale = wftInfo.getSourceLocale().toString();
            String trgLocale = wftInfo.getTargetLocale().toString();
            Set<String> projectUserIds =
                    wftInfo.getProject().getUserIds();

            WorkflowTemplate wfTemplate = WorkflowTemplateHandlerHelper
                    .getWorkflowTemplateById(wftInfo.getWorkflowTemplateId());
            Vector<WorkflowTask> wfTasks = wfTemplate.getWorkflowTasks();
            for (WorkflowTask wfTask : wfTasks)
            {
                if (wfTask.getType() == WorkflowConstants.ACTIVITY)
                {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("activityName", wfTask.getActivityName());
                    jsonObj.put("sequence", wfTask.getSequence());

                    String displayRoleName = wfTask.getDisplayRoleName();
                    if (WorkflowTask.DEFAULT_ROLE_NAME.equalsIgnoreCase(displayRoleName))
                    {
                        jsonObj.put("Participants", WorkflowTask.DEFAULT_ROLE_NAME);
                    }
                    else
                    {
                        String participantUserIds = convertParticipantUserNamesToIds(displayRoleName);
                        jsonObj.put("Participants", participantUserIds);
                    }

					String allAvailableAssignees = AmbassadorUtil
							.listToString(getAllAvailableAssigneeIds(
									wfTask.getActivityName(), srcLocale,
									trgLocale, projectUserIds));
                    jsonObj.put("AvailableAssignees", allAvailableAssignees);

                    StringBuffer inArrows = new StringBuffer();
                    Vector incomingArrows = wfTask.getIncomingArrows();
                    if (incomingArrows != null && incomingArrows.size() > 0)
                    {
                        for (int i = 0; i < incomingArrows.size(); i++)
                        {
                            WorkflowArrow arrow = (WorkflowArrow) incomingArrows.get(i);
                            inArrows.append(arrow.getName());
                            if (i + 1 < incomingArrows.size())
                            {
                                inArrows.append(",");
                            }
                        }
                    }
                    jsonObj.put("IncomingArrows", inArrows.toString());

                    StringBuffer outArrows = new StringBuffer();
                    getOutgoingArrows(outArrows, wfTask);
                    String outArrowNames = outArrows.toString();
                    if (outArrowNames.length() > 0 && outArrowNames.endsWith(","))
                    {
                        outArrowNames = outArrowNames.substring(0, outArrowNames.length() - 1);
                    }
                    jsonObj.put("OutgoingArrows", outArrowNames);
                    array.put(jsonObj);
                }
            }

            json = array.toString();
        }
        catch (Exception e)
        {
            return makeErrorJson(
                    GET_WORKFLOW_TEMPLATE_INFO,
                    "Cannot get workflow template info correctly. "
                            + e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return json;
    }

    /**
     * Get the outgoing arrow names comma separated. If the arrow points to a
     * condition node, then the condition node's outgoing arrows are wanted.
     * 
     * @param buf
     * @param wfTask
     */
    private void getOutgoingArrows(StringBuffer buf, WorkflowTask wfTask)
    {
        Vector outgoingArrows = wfTask.getOutgoingArrows();
        if (outgoingArrows != null && outgoingArrows.size() > 0)
        {
            for (int i = 0; i < outgoingArrows.size(); i++)
            {
                WorkflowArrow arrow = (WorkflowArrow) outgoingArrows.get(i);
                WorkflowTask targetWfTask = arrow.getTargetNode();
                if ("Condition Node".equalsIgnoreCase(targetWfTask.getName()))
                {
                    getOutgoingArrows(buf, targetWfTask);
                }
                else
                {
                    buf.append(arrow.getName()).append(",");
                }
            }
        }
    }

    /**
     * For a given workflow template name, activity name list, adjust the
     * assignees. Also allow "all qualified users" as a parameter. If no valid
     * assignees, also use "all qualified users" as default.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_workflowTemplateName
     *            -- workflow template name
     * @param p_companyName
     *            -- company name
     * @param p_activityAssigneesInJson
     *            -- activity assignees to be updated in JSON. A sample is:
     * [{"Participants":"All qualified users","activityName":"Translation1_2","sequence":"1"},{"Participants":"yorkadmin","activityName":"review_linguistc1_2","sequence":"2"}]
     * 
     * @return String in JSON style for error.If success, return nothing.
     * 
     * @throws WebServiceException
     */
    @SuppressWarnings("unchecked")
    public String modifyWorkflowTemplateAssignees(String p_accessToken,
            String p_workflowTemplateName, String p_companyName,
            String p_activityAssigneesInJson) throws WebServiceException
    {
        checkAccess(p_accessToken, MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES);
		String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.WORKFLOWS_VIEW);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
        // Parameters checking
        if (StringUtil.isEmpty(p_workflowTemplateName))
        {
            return makeErrorJson(MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                    "empty workflowTemplateName.");
        }

        if (StringUtil.isEmpty(p_companyName)
                || getCompanyByName(p_companyName) == null)
        {
            return makeErrorJson(MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                    "empty companyName or no such company.");
        }

        if (StringUtil.isEmpty(p_activityAssigneesInJson))
        {
            return makeErrorJson(MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                    "empty activityAssigneesInJson.");
        }

        Company company = getCompanyByName(p_companyName);
        User curUser = getUser(getUsernameFromSession(p_accessToken));
        Company companyOfLogUser = getCompanyByName(curUser.getCompanyName());
		if (companyOfLogUser.getId() != 1
				&& companyOfLogUser.getId() != company.getId())
		{
			return makeErrorJson(
					MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
					"Logged user can only access workflows of its own company, can not access another company's data.");
		}

        // Activity-Assignees to be updated
        HashMap<Integer, JSONObject> seq2JsonObjMap = new HashMap<Integer, JSONObject>();
        try
        {
            JSONArray array = new JSONArray(p_activityAssigneesInJson.trim());
            if (array.length() < 1)
            {
                return makeErrorJson(
                        MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                        "No activity assignees need to be changed according to 'activityAssigneesInJson' parameter: "
                                + p_activityAssigneesInJson);
            }
            for (int i = 0; i < array.length(); i++)
            {
                JSONObject obj = array.getJSONObject(i);
                seq2JsonObjMap.put(obj.getInt("sequence"), obj);
            }
        }
        catch (JSONException jsonEx)
        {
            return makeErrorJson(MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                    "Fail to get JSONArray from 'activityAssigneesInJson' parameter: "
                            + p_activityAssigneesInJson);
        }

        StringBuffer json = new StringBuffer();
        WebServicesLog.Start activityStart = null;
        try
        {
            // WebServicesLog
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("workflowTemplateName", p_workflowTemplateName);
            activityArgs.put("companyName", p_companyName);
            activityArgs.put("activityAssigneesInJson", p_activityAssigneesInJson);
            activityStart = WebServicesLog
                    .start(Ambassador4Falcon.class,
                            "modifyWorkflowTemplateAssignees(accessToken, workflowTemplateName, companyName, activityAssigneesInJson)",
                            activityArgs);

            // Possible "WorkflowTemplateInfo"
            WfTemplateSearchParameters params = new WfTemplateSearchParameters();
            params.setWorkflowName(p_workflowTemplateName.trim());
            params.setCompanyName(p_companyName);
            List<WorkflowTemplateInfo> templates =
                    (List<WorkflowTemplateInfo>) ServerProxy
                            .getProjectHandler().findWorkflowTemplates(params);
            if (templates == null || templates.size() < 1)
            {
                return makeErrorJson(MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                        "Cannot get WorkflowTemplateInfo with name '"
                                + p_workflowTemplateName + "'.");
            }

            // Filter to ensure the template belongs to specified company.
            for (Iterator<WorkflowTemplateInfo> wftInfoIter = templates
                    .iterator(); wftInfoIter.hasNext();)
            {
                WorkflowTemplateInfo wftInfo = wftInfoIter.next();
                // If template is not in specified company, ignore it.
                if (wftInfo.getCompanyId() != company.getId())
                {
                    wftInfoIter.remove();
                }
            }
            if (templates.size() < 1)
            {
                return makeErrorJson(MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                        "There is no workflow template that matches the name '"
                                + p_workflowTemplateName + "' and company '"
                                + p_companyName + "'.");
            }

            // Generally the size of "templates" should be 1.
            WorkflowTemplateInfo wftInfo = templates.get(0);
            String srcLocale = wftInfo.getSourceLocale().toString();
            String trgLocale = wftInfo.getTargetLocale().toString();

            WorkflowTemplate wfTemplate = WorkflowTemplateHandlerHelper
                    .getWorkflowTemplateById(wftInfo.getWorkflowTemplateId());
            Vector<WorkflowTask> wfTasks = wfTemplate.getWorkflowTasks();

            Iterator<Entry<Integer, JSONObject>> it = seq2JsonObjMap.entrySet().iterator();
            while (it.hasNext())
            {
                Entry<Integer, JSONObject> act = it.next();
                int sequence = act.getKey();
                JSONObject object = act.getValue();
                String activityName = object.getString("activityName");
                boolean activityFound = false;
                for (WorkflowTask wfTask : wfTasks)
                {
                    if (activityName != null
                            && activityName.equals(wfTask.getActivityName())
                            && sequence == wfTask.getSequence())
                    {
                        activityFound = true;
                        String participants = object.getString("Participants");
                        if (WorkflowTask.DEFAULT_ROLE_NAME.equalsIgnoreCase(participants))
                        {
                            updateWorkflowTaskToDefault(wfTask, srcLocale, trgLocale);
                        }
                        else
                        {
                            List<String> finalValidUserIds =
                                    getFinalValidUserIdsFromParticipants(
                                            wftInfo, wfTask, participants);

                            if (finalValidUserIds.size() > 0)
                            {
                                wfTask.setDisplayRoleName(getFinalValidUserNamesFromParticipants(finalValidUserIds));
                                // On activity properties applet UI, if select
                                // "A selected user...", this should be true.
                                wfTask.setRoleType(true);//
                                wfTask.setRolePreference(null);

                                String[] roles = new String[finalValidUserIds.size()];
                                for (int i = 0; i < finalValidUserIds.size(); i++)
                                {
                                    roles[i] = getRole(true, wfTask, srcLocale,
                                            trgLocale, finalValidUserIds.get(i));
                                }
                                wfTask.setRoles(roles);
                            }
                            else
                            {
                                json.append("No valid participants(")
                                        .append(participants)
                                        .append(") for activityName(")
                                        .append(activityName)
                                        .append(") and sequence (")
                                        .append(sequence).append(");");
                            }
                        }

                        break;
                    }
                }
                if (!activityFound)
                {
                    json.append(
                            "No such activity in workflow for activityName(")
                            .append(activityName).append(") and sequence (")
                            .append(sequence).append(");");
                }
            }

            // Save the changes.
            String[] wfManagerIds =
                    new String[wftInfo.getWorkflowManagerIds().size()];
            wfManagerIds = (String[]) wftInfo.getWorkflowManagerIds().toArray(wfManagerIds);
            WorkflowTemplate newWft = ServerProxy.getWorkflowServer()
                    .modifyWorkflowTemplate(wfTemplate,
                            new WorkflowOwners(wftInfo.getProjectManagerId(), wfManagerIds));
            wftInfo.setWorkflowTemplate(newWft);
            ServerProxy.getProjectHandler().modifyWorkflowTemplate(wftInfo);
        }
        catch (Exception e)
        {
            return makeErrorJson(
                    MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                    "Error when try to modifyWorkflowTemplateAssignees. "
                            + e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        if (json.length() == 0)
            json.append("successful");
        return makeErrorJson(MODIFY_WORKFLOW_TEMPLATE_ASSIGNEES,
                json.toString());
    }

    /**
     * The "displayRoleName" is composed of user names instead of user IDs, need
     * return userId here.
     * 
     * For "getWorkflowTemplateInfo(..)" API.
     * 
     * @param p_displayRoleName
     */
    private String convertParticipantUserNamesToIds(String p_displayRoleName)
    {
        if (StringUtil.isEmpty(p_displayRoleName))
            return "";

        String[] userNamesArray = stringToArray(p_displayRoleName);
        String[] userIdsArray = UserUtil
                .convertUserNamesToUserIds(userNamesArray);

        return arrayToString(userIdsArray);
    }

    /**
     * Get all available assignees for specified activity/source locale/target
     * locale.
     * 
     * @param activityName
     *            -- like "Translation1_2".
     * @param sourceLocale
     *            -- like "EN_US"(case-insensitive).
     * @param targetLocale
     *            -- like "ZH_CN"(case-insensitive).
     * @param projectUserIds
     *            -- user Ids current project has.
     * @return -- HashSet<String>
     */
    @SuppressWarnings("rawtypes")
    private HashSet<String> getAllAvailableAssigneeIds(String activityName,
            String sourceLocale, String targetLocale, Set<String> projectUserIds)
    {
        HashSet<String> results = new HashSet<String>();

        Collection usersInRole = WorkflowTemplateHandlerHelper.getUserRoles(
                activityName, sourceLocale, targetLocale);
        if (usersInRole != null && usersInRole.size() > 0)
        {
            Vector<UserRoleImpl> validUserRoles = new Vector<UserRoleImpl>();

            // filter out the users that aren't in the project
            for (Iterator i = usersInRole.iterator(); i.hasNext();)
            {
                UserRoleImpl userRole = (UserRoleImpl) i.next();
                if (projectUserIds.contains(userRole.getUser()))
                {
                    validUserRoles.add(userRole);
                }
            }

            for (int i = 0; i < validUserRoles.size(); i++)
            {
                UserRoleImpl userRole = (UserRoleImpl) validUserRoles.get(i);
                results.add(userRole.getUser());//userId
            }
        }

        return results;
    }

    private String getFinalValidUserNamesFromParticipants(
            List<String> userIdsFromParticipants)
    {
        Object[] userNames = UserUtil
                .convertUserIdsToUserNames(listToArray(userIdsFromParticipants));
        List<String> userNameList = new ArrayList<String>();
        for (Object userName : userNames)
        {
            userNameList.add((String) userName);
        }

        return AmbassadorUtil.listToString(userNameList);
    }

    /**
     * User may send wrong user ID/name, or user ID/name that belongs to another
     * company. Filter to get final valid userIds.
     */
    @SuppressWarnings("unchecked")
    private List<String> getFinalValidUserIdsFromParticipants(
            WorkflowTemplateInfo wftInfo, WorkflowTask wfTask,
            String participants)
    {
        String srcLocale = wftInfo.getSourceLocale().toString();
        String trgLocale = wftInfo.getTargetLocale().toString();
        Set<String> projectUserIds = wftInfo.getProject().getUserIds();

        HashSet<String> availableUserIds = getAllAvailableAssigneeIds(
                wfTask.getActivityName(), srcLocale, trgLocale, projectUserIds);

        Set<String> result = getValidUserIdsFromParticipants(participants);
        for (Iterator<String> it = result.iterator(); it.hasNext();)
        {
            // If is not available user, ignore it.
            if (!availableUserIds.contains(it.next()))
            {
                it.remove();
            }
        }

        return new ArrayList<String>(result);
    }

    /**
     * Client may send user ID or Name to API, check to return valid user Ids
     * here.
     */
    private Set<String> getValidUserIdsFromParticipants(String p_participants)
    {
        if (StringUtil.isEmpty(p_participants))
                return null;

        Set<String> userIds = new HashSet<String>();

        List<String> list = stringToList(p_participants);
        for (Iterator<String> it = list.iterator(); it.hasNext();)
        {
            try
            {
                String userIdOrName = it.next();
                User user = UserUtil.getUserById(userIdOrName);
                // userIdOrName is a valid userId.
                if (user != null)
                {
                    userIds.add(user.getUserId());
                }
                else
                {
                    String userId = UserUtil.getUserIdByName(userIdOrName);
                    // userIdOrName is a valid userName.
                    if (userId != null)
                    {
                        userIds.add(userId);
                    }
                }
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return userIds;
    }

    /**
     * <p>
     * If specify users, it can has multiple roles(comma saparated), a role
     * sample is like
     * "1 Translation1_2 en_US zh_CN yorkadmin,1 Translation1_2 en_US zh_CN yorkanyone"
     * </p>
     * <p>
     * If not specify users, it only has one role, a sample is:
     * "1 Translation1_2 en_US zh_CN".
     * </p>
     */
    private String getRole(boolean isUserRole, WorkflowTask wfTask,
            String srcLocale, String trgLocale, String userId)
    {
        StringBuilder role = new StringBuilder();
        role.append(wfTask.getActivity().getId());
        role.append(" ");
        role.append(wfTask.getActivityName());
        role.append(" ");
        role.append(srcLocale);
        role.append(" ");
        role.append(trgLocale);
        if (isUserRole)
        {
            role.append(" ").append(userId);
        }

        return role.toString();
    }

    private static Company getCompanyByName(String companyName)
    {
        try
        {
            return ServerProxy.getJobHandler().getCompany(companyName);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private void updateWorkflowTaskToDefault(WorkflowTask wfTask,
            String srcLocale, String trgLocale)
    {
        wfTask.setDisplayRoleName(WorkflowTask.DEFAULT_ROLE_NAME);
        wfTask.setRoleType(false);
        wfTask.setRolePreference(null);
        String[] roles = new String[1];
        roles[0] = getRole(false, wfTask, srcLocale, trgLocale, null);
        wfTask.setRoles(roles);
    }

    /**
     * Offline download to get reviewers comments report, translations edit
     * report or offline translation kit. For offline translation kit
     * downloading, it will follow logged user's "Download Options" as default.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to offline download file for.
     * @param p_workOfflineFileType
     *            -- 1 : Reviewer Comments Report or Translations Edit Report (this follows UI settings)
     *            -- 2 : Offline Translation Kit
     *            -- 3 : Translation Edit Report
     *            -- 4 : Reviewer Comments Report
     *            -- 5 : Reviewer Comments Report (Simplified)
     *            -- 6 : Post Review QA Report
     *            -- 7 : Translation Verification Report
     *            -- 8  : Biligual Trados RTF
	 *			   -- 9  : Trados 7 TTX
	 *			   -- 10  : OmegaT
	 *			   -- 11 : XLiff 1.2
	 *			   -- 12 : Xliff 2.0
	 *			   -- 13 : RTF List view
	 *			   -- 14 : Reviewer Comments Report with Compact Tags
	 *			   -- 15 : Reviewer Comments Report (Simplified) with Compact Tags
     *@param p_workofflineFileTypeOption
     *			   --1  : consolidate/split = split per file, include repeated segments = no (Default)
     *			   --2  : consolidate/split = consolidate (overrides preserve folder structure setting),include repeated segments = no
     *			   --3  : consolidate/split = split per wordcount of 2000, include repeated segments = no
     *			   --4  : consolidate/split = split per file, include repeated segments = yes
     *			   --5  : consolidate/split = consolidate (overrides preserve folder structure setting),include repeated segments = yes
     *			   --6  : consolidate/split = split per wordcount of 2000, include repeated segments = yes
     * @return -- JSON string. -- If fail, it is like
     *         '{"getWorkOfflineFiles":"Corresponding message is here."}'; -- If
     *         succeed, report returning is like
     *         '{"taskId":3715,"targetLocale":"zh_CN","acceptorUserId":"yorkadmin","path":"http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/TranslationsEditReport/20140219/ReviewersCommentsReport-(jobname_492637643)(337)-en_US_zh_CN-20140218
     *         162543.xlsx"}'. -- offline translation kit returning is like
     *         '{"taskId":3715,"targetLocale":"zh_CN","acceptorUserId":"yorkadmin","path":"http://10.10.215.21:8080/globalsight/DownloadOfflineKit/[CompanyName]/GlobalSight/CustomerDownload/[jobName_zh_CN.zip]"}'
     *         .
     * @throws WebServiceException
     */
	public String getWorkOfflineFiles(String p_accessToken, Long p_taskId,
			int p_workOfflineFileType, String p_workofflineFileTypeOption)
			throws WebServiceException
	{
		checkAccess(p_accessToken, GET_WORK_OFFLINE_FILES);

		AmbassadorHelper helper = new AmbassadorHelper();
		return helper.getWorkOfflineFiles(p_accessToken, p_taskId,
				p_workOfflineFileType, p_workofflineFileTypeOption, true);
	}

    /**
     * Upload offline files to server.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to upload file to.
     * @param p_workOfflineFileType
     *            -- 1 : For reports like "Reviewer Comments Report", "Simplified Reviewer Comments Report",
     *             "Translations Edit Report", "Post Review QA Report" or "Translation Verification Report".
     *            -- 2 : Offline Translation Kit
     * @param p_fileName
     *            -- the upload file name
     * @param bytes
     *            -- file contents in bytes
     * @return    -- If succeed, return a "identifyKey" which is used to differ
     *         this uploading, a sample is '{"identifyKey":"532689969","uploadWorkOfflineFiles":""}';
     *            -- If fail, no key, only return corresponding message, a sample is like '{"uploadWorkOfflineFiles":"Corresponding message is here."}';
     * 
     * @throws WebServiceException
     */
    public String uploadWorkOfflineFiles(String p_accessToken, Long p_taskId,
            int p_workOfflineFileType, String p_fileName, byte[] bytes)
            throws WebServiceException
    {
        checkAccess(p_accessToken, UPLOAD_WORK_OFFLINE_FILES);

        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.uploadWorkOfflineFiles(p_accessToken, p_taskId,
                p_workOfflineFileType, p_fileName, bytes, true);
    }

    /**
     * Process offline file to update system.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to import file into.
     * @param p_identifyKey
     *            -- identifyKey to help locate where the uploaded file is.
     * @param p_workOfflineFileType
     *            -- 1 : For reports like "Reviewer Comments Report", "Simplified Reviewer Comments Report",
     *             "Translations Edit Report", "Post Review QA Report" or "Translation Verification Report".
     *            -- 2 : Offline Translation Kit
     * @return -- Empty if succeed; if fail, return corresponding message.
     * 
     * @throws WebServiceException
     */
    public String importWorkOfflineFiles(String p_accessToken, Long p_taskId,
            String p_identifyKey, int p_workOfflineFileType)
            throws WebServiceException
    {
        checkAccess(p_accessToken, IMPORT_WORK_OFFLINE_FILES);

        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.importWorkOfflineFiles(p_accessToken, p_taskId,
                p_identifyKey, p_workOfflineFileType, true);
    }

    /**
     * Get a link for in context review for specified task ID. User need not
     * logging in GlobalSight.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID
     * @return A link like "http://10.10.215.20:8080/globalsight/ControlServlet?linkName=self&pageName=inctxrvED1&secret=E127B35E1A1C1B52C742353BBA176327D7F54956B373428134DE7252182EAA0D".
     * 
     * @throws WebServiceException
     */
    public String getInContextReviewLink(String p_accessToken, String p_taskId)
            throws WebServiceException
    {
        checkAccess(p_accessToken, GET_IN_CONTEXT_REVIEW_LINK);
        try {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertIsInteger(p_taskId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return makeErrorJson(GET_IN_CONTEXT_REVIEW_LINK, e.getMessage());
        }

        String loggingUserName = getUsernameFromSession(p_accessToken);
        String userId = UserUtil.getUserIdByName(loggingUserName);

        Task task = null;
        try
        {
            task = TaskHelper.getTask(Long.parseLong(p_taskId));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = "Failed to get task object by taskId : "
                    + p_taskId;
            return makeErrorJson(GET_IN_CONTEXT_REVIEW_LINK, message);
        }

        if (task == null)
        {
            return makeErrorJson(GET_IN_CONTEXT_REVIEW_LINK, "Can not get task by taskID.");
        }

        if (task.getState() == Task.STATE_COMPLETED)
        {
            return makeErrorJson(GET_IN_CONTEXT_REVIEW_LINK,
                    "The current task has been in completed state.");
        }

        WebServicesLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggingUserName);
            activityArgs.put("taskId", p_taskId);
            activityStart = WebServicesLog.start(Ambassador4Falcon.class,
                    "getInContextReviewLink(p_accessToken, p_taskId)", activityArgs);

            User pm = task.getWorkflow().getJob().getProject()
                    .getProjectManager();
            WorkflowTaskInstance wfTask = ServerProxy.getWorkflowServer()
                    .getWorkflowTaskInstance(userId, task.getId(),
                            WorkflowConstants.TASK_ALL_STATES);
            task.setWorkflowTask(wfTask);
            List allAssignees = task.getAllAssignees();
            if (allAssignees != null && allAssignees.size() > 0)
            {
                if (!allAssignees.contains(userId)
                        && !userId.equalsIgnoreCase(pm.getUserId()))
                {
                    String message = "'"
                            + userId
                            + "' is neither acceptor/available assignee of current task nor project manager.";
                    logger.warn(message);
                    return makeErrorJson(GET_IN_CONTEXT_REVIEW_LINK, message);
                }
            }

            StringBuffer link = new StringBuffer();
            link.append(AmbassadorUtil.getCapLoginOrPublicUrl());
            link.append("/ControlServlet?linkName=self&pageName=inctxrvED1&secret=");
            StringBuffer secret = new StringBuffer();
            secret.append("taskId=").append(p_taskId.trim())
                    .append("&nameField=").append(loggingUserName);
            link.append(AmbassadorUtil.encryptionString(secret.toString()));
            return link.toString();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String message = "Failed to get In Context Review Link for taskId : " + p_taskId;
            return makeErrorJson(GET_IN_CONTEXT_REVIEW_LINK, message);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }
    
    /**
     * Returns an json description containing projects information according by
     * current user
     * 
     * This method will return projects information which are in charge by
     * current user.
     * 
     * @param p_accessToken
     * 
     * @return java.lang.String 
     * @throws WebServiceException
     */
    public String getAllProjectsByUser(String p_accessToken)
            throws WebServiceException
	{
		checkAccess(p_accessToken, GET_ALL_PROJECTS_BY_USER);
		// checkPermission(p_accessToken, Permission.GET_ALL_PROJECTS);

		List projects = null;
		try
		{
			String username = getUsernameFromSession(p_accessToken);
			User user = ServerProxy.getUserManager().getUserByName(username);
			projects = ServerProxy.getProjectHandler()
					.getProjectInfosManagedByUser(user,
							Permission.GROUP_MODULE_GLOBALSIGHT);
		}
		catch (Exception e)
		{
			String message = "Unable to get all projects infos managed by user";
			logger.error(message, e);
			message = makeErrorJson("getAllProjectsByUser", message);
			throw new WebServiceException(message);
		}

		JSONArray array = new JSONArray();
		Iterator it = projects.iterator();
		try
		{
			while (it.hasNext())
			{
				JSONObject json = new JSONObject();
				ProjectInfo pi = (ProjectInfo) it.next();
				json.put("projectId", pi.getProjectId());
				json.put("projectName", pi.getName());
				json.put("companyId", pi.getCompanyId());
				json.put("companyName",
						CompanyWrapper.getCompanyNameById(pi.getCompanyId()));
				if (pi.getDescription() == null
						|| pi.getDescription().length() < 1)
				{
					json.put("description", "N/A");
				}
				else
				{
					json.put("description", pi.getDescription());
				}
				array.put(json);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return array.toString();
	}
    
	/**
	 * Get activities current logged user is in charge of.
	 * 
	 * @param p_accessToken
	 *            -- login user's token
	 * @param p_projectIds
	 *            -- project IDs, comma separated. A sample is "12,13". Can be
	 *            null.
	 * @param p_taskState
	 *            -- taskState, can be null. Available values are "8" or "3"("8"
	 *            means "in progress","3" means "available").
	 *
	 * @return string in JSON
	 * @throws WebServiceException
	 */
	public String getActivityList(String p_accessToken, String p_projectIds,
			String p_taskState) throws WebServiceException
	{
		checkAccess(p_accessToken, GET_ACTIVITY_LIST);
		try
		{
			if (StringUtils.isNotBlank(p_taskState))
			{
				Assert.assertIsInteger(p_taskState);
			}
			if (StringUtils.isNotBlank(p_projectIds))
			{
				if (p_projectIds.contains(","))
				{
					String[] projectIds = p_projectIds.split(",");
					for (String projectId : projectIds)
					{
						Assert.assertIsInteger(projectId);
					}
				}
				else
				{
					Assert.assertIsInteger(p_projectIds);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return makeErrorJson(GET_ACTIVITY_LIST, e.getMessage());
		}
		
		if (StringUtils.isNotBlank(p_taskState))
		{
			if (Integer.parseInt(p_taskState) != 3
					&& Integer.parseInt(p_taskState) != 8)
			{
				return makeErrorJson(GET_ACTIVITY_LIST,
						"Task state can only be 3 (available) and 8 (in progress). The input state is "
								+ p_taskState);
			}
		}
		
		try
		{
			List tasks = null;
			String username = getUsernameFromSession(p_accessToken);
			User user = ServerProxy.getUserManager().getUserByName(username);
			if (StringUtils.isBlank(p_taskState))
			{
				tasks = ServerProxy.getTaskManager().getTasks(user.getUserId(),
						-10);
			}
			else
			{
				tasks = ServerProxy.getTaskManager().getTasks(user.getUserId(),
						Integer.parseInt(p_taskState));
			}

			List<Long> projectIdList = new ArrayList<Long>();
			if (StringUtils.isNotBlank(p_projectIds))
			{
				String[] projectIds = p_projectIds.split(",");
				for (String projectId : projectIds)
				{
					projectIdList.add(Long.parseLong(projectId));
				}
			}

			JSONArray arrayOut = new JSONArray();
			Map<String, List<Task>> map = new HashMap<String, List<Task>>();
			List<Task> list = null;
			for (int i = 0; i < tasks.size(); i++)
			{
				Task task = (Task) tasks.get(i);
				if (projectIdList.size() > 0
						&& !projectIdList.contains(task.getWorkflow()
								.getJob().getProjectId()))
				{
					continue;
				}
				if (StringUtils.isBlank(p_taskState))
				{
					if (task.getState() != 3 && task.getState() != 8)
					{
						continue;
					}
				}
				if (map.containsKey(task.getJobId() + "," + task.getJobName()))
				{
					list = map.get(task.getJobId() + "," + task.getJobName());
					list.add(task);
				}
				else
				{
					list = new ArrayList<Task>();
					list.add(task);
				}
				map.put(task.getJobId() + "," + task.getJobName(), list);
			}

			for (Entry<String, List<Task>> entry : map.entrySet())
			{
				String key = entry.getKey();
				List<Task> taskList = entry.getValue();
				JSONObject jsonOut = new JSONObject();
				String[] keyArr = key.split(",");
				jsonOut.put("jobID", keyArr[0]);
				jsonOut.put("jobName", keyArr[1]);
				JSONArray arrayIn = new JSONArray();
				for (Task tk : taskList)
				{
					JSONObject jsonIn = new JSONObject();
					jsonIn.put("targetLanguage", tk.getTargetLocale());
					jsonIn.put("taskID", tk.getId());
					jsonIn.put("taskState", (tk.getState() == 3 ? "AVAILABLE" : "IN PROGRESS"));
					arrayIn.put(jsonIn);
				}
				jsonOut.put("workflow", arrayIn);
				arrayOut.put(jsonOut);
			}

			return arrayOut.toString();
		}
		catch (Exception e)
		{
			String message = "Unable to get all projects infos managed by user";
			logger.error(message, e);
			message = makeErrorJson(GET_ACTIVITY_LIST, message);
			throw new WebServiceException(message);
		}
	}
    
    ///////////////////////////////////////////////////////////////////////////
    //// COMMON PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////
    private static Locale getUILocale(String p_accessToken)
            throws UserManagerException, RemoteException, GeneralException
    {
        User user = ServerProxy.getUserManager().getUserByName(
                getUsernameFromSession(p_accessToken));
        return new Locale(user.getDefaultUILocale());
    }

    /**
     * Change array to string comma separated.
     * 
     * @return String like "string1,string2,string3".
     */
    private static String arrayToString(String[] array)
    {
        StringBuilder buffer = new StringBuilder();
        int counter = 0;
        for (String str : array)
        {
            if (counter > 0)
            {
                buffer.append(",");
            }
            counter++;
            buffer.append(str);
        }

        return buffer.toString();
    }

    private static List<String> stringToList(String string)
    {
        List<String> result = new ArrayList<String>();

        String[] tokens = Strings.split(string, ",");
        for (int i = 0; i < tokens.length; i++)
        {
            String token = tokens[i].trim();
            if (StringUtil.isNotEmpty(token))
            {
                result.add(token);
            }
        }
        return result;
    }

    private static String[] stringToArray(String string)
    {
        // get list first as stringToList has "NotEmpty" check.
        List<String> list = stringToList(string);

        return listToArray(list);
    }

    private static String[] listToArray(List<String> list)
    {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            array[i] = list.get(i);
        }

        return array;
    }
}
