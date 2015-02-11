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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WfTemplateSearchParameters;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflow.WorkflowArrow;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowOwners;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.log.ActivityLog;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

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
     * @return jobIDs in json string. A sample result is like:
     *         {"JOB_ID":"204,213,215,216,218,220,190,202,205,217,219"}
     * @throws WebServiceException
     * 
     */
    public String getJobIDsWithStatusChanged(String p_accessToken,
            int p_intervalInMinute) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_JOB_IDS_WITH_STATUS_CHANGED);
        checkPermission(p_accessToken, Permission.JOBS_VIEW);

        String json = "";
        ActivityLog.Start activityStart = null;
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("intervalInMinute", p_intervalInMinute);
            activityStart = ActivityLog.start(Ambassador4Falcon.class,
                            "getJobIDsWithStatusChanged(p_accessToken,p_intervalInMinute)",
                    activityArgs);
            if (StringUtil.isEmpty(p_accessToken) || p_intervalInMinute < 1)
            {
                return makeErrorJson(GET_JOB_IDS_WITH_STATUS_CHANGED,
                        "Invaild time range parameter.");
            }
            // int hours = getHours(p_sinceTime);
            Calendar calendar = Calendar.getInstance();
            // calendar.add(Calendar.HOUR, 0 - hours);
            calendar.add(Calendar.MINUTE, 0 - p_intervalInMinute);
            String timeStamp = dateFormat.format(calendar.getTime());
            User user = getUser(getUsernameFromSession(p_accessToken));
            String companyName = user.getCompanyName();

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
            query.setString(1, CompanyWrapper.getCompanyIdByName(companyName));
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
		ActivityLog.Start activityStart = null;

		Map<Object, Object> activityArgs = new HashMap<Object, Object>();
		activityArgs.put("taskId", p_taskId);
		activityStart = ActivityLog
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
        checkPermission(p_accessToken, Permission.REPORTS_DELL_FILE_LIST);

        String json = "";
        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("jobIds", p_jobIds);
            activityArgs.put("includeMTData", p_includeMTData);
            activityStart = ActivityLog
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

    private int addWordCountForJson(TargetPage tg, JSONObject jsonObj,
            int threshold, int mtConfidenceScore, boolean includeMTData)
            throws Exception
    {
        boolean isInContextMatch = PageHandler.isInContextMatch(tg
                .getSourcePage().getRequest().getJob());
        boolean isUseDefaultContextMatch = PageHandler
                .isDefaultContextMatch(tg);
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
            if (isUseDefaultContextMatch)
            {
                _100MatchWordCount = pageWC.getTotalExactMatchWordCount()
                        - contextMatchWC;
            }
            else
            {
                _100MatchWordCount = pageWC.getTotalExactMatchWordCount();
                contextMatchWC = 0;
            }
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
     * Create new user
     * 
     * @param p_accessToken
     *            String Access token. This field cannot be null
     * @param p_userId
     *            String User ID. This field cannot be null. 
     *            Example: 'qaadmin'
     * @param p_password
     *            String Password. This field cannot be null
     * @param p_firstName
     *            String First name. This field cannot be null
     * @param p_lastName
     *            String Last name. This field cannot be null
     * @param p_email
     *            String Email address. This field cannot be null. 
     *            If the email address is not vaild then the user's status will be set up as inactive
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to.
     *            The element in the array is the name of permission group.
     *            Example: [{"Administrator"}, {"ProjectManager"}]
     * @param p_status
     *            String Status of user. This parameter is not using now, it should be null.
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
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project.
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
     *       -1 -- Unknown exception
     * @throws WebServiceException
     */
    public int createUser(String p_accessToken, String p_userId,
            String p_password, String p_firstName, String p_lastName,
            String p_email, String[] p_permissionGrps, String p_status,
            String p_roles, boolean p_isInAllProject, String[] p_projectIds)
            throws WebServiceException
    {
        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.createUser(p_accessToken, p_userId, p_password,
                p_firstName, p_lastName, p_email, p_permissionGrps, p_status,
                p_roles, p_isInAllProject, p_projectIds);
    }

    /**
     * Modify user
     * 
     * @param p_accessToken
     *            String Access token. This field cannot be null
     * @param p_userId
     *            String User ID. This field cannot be null. 
     *            Example: 'qaadmin'
     * @param p_password
     *            String Password. This field cannot be null
     * @param p_firstName
     *            String First name. This field cannot be null
     * @param p_lastName
     *            String Last name. This field cannot be null
     * @param p_email
     *            String Email address. This field cannot be null. 
     *            If the email address is not vaild then the user's status will be set up as inactive
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to.
     *            The element in the array is the name of permission group.
     *            Example: [{"Administrator"}, {"ProjectManager"}]
     * @param p_status
     *            String Status of user. This parameter is not using now, it should be null.
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
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project.
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
     *       -1 -- Unknown exception
     * @throws WebServiceException
     */
    public int modifyUser(String p_accessToken, String p_userId,
            String p_password, String p_firstName, String p_lastName,
            String p_email, String[] p_permissionGrps, String p_status,
            String p_roles, boolean p_isInAllProject, String[] p_projectIds)
            throws WebServiceException
    {
        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.modifyUser(p_accessToken, p_userId, p_password,
                p_firstName, p_lastName, p_email, p_permissionGrps, p_status,
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
        checkPermission(p_accessToken, Permission.WORKFLOWS_VIEW);

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
        checkPermission(p_accessToken, Permission.WORKFLOWS_VIEW);

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

        String json = "";
        ActivityLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("workflowTemplateName", p_workflowTemplateName);
            activityArgs.put("companyName", p_companyName);
            activityStart = ActivityLog
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

                    String allAvailableAssignees = listToString(getAllAvailableAssigneeIds(
                            wfTask.getActivityName(), srcLocale, trgLocale,
                            projectUserIds));
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
        checkPermission(p_accessToken, Permission.WORKFLOWS_VIEW);

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
        if (companyOfLogUser.getId() != company.getId())
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
        ActivityLog.Start activityStart = null;
        try
        {
            // ActivityLog
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("workflowTemplateName", p_workflowTemplateName);
            activityArgs.put("companyName", p_companyName);
            activityArgs.put("activityAssigneesInJson", p_activityAssigneesInJson);
            activityStart = ActivityLog
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

        return listToString(userNameList);
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
     *            -- 1 : Reviewer Comments Report or Translations Edit Report --
     *            2 : Offline Translation Kit
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
            int p_workOfflineFileType) throws WebServiceException
    {
        checkAccess(p_accessToken, GET_WORK_OFFLINE_FILES);

        AmbassadorHelper helper = new AmbassadorHelper();
        return helper.getWorkOfflineFiles(p_accessToken, p_taskId,
                p_workOfflineFileType, true);
    }

    /**
     * Upload offline files to server.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to upload file to.
     * @param p_workOfflineFileType
     *            -- 1 : Reviewer Comments Report or Translations Edit Report
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
     *            -- 1 : Reviewer Comments Report or Translations Edit Report
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
     * Change list to string comma separated.
     * 
     * @return String like "string1,string2,string3".
     */
    private static String listToString(Collection<String> objects)
    {
        StringBuilder buffer = new StringBuilder();
        int counter = 0;
        for (String str : objects)
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
