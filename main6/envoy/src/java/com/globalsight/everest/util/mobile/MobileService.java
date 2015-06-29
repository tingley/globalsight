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
package com.globalsight.everest.util.mobile;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.taskmanager.TaskSearchParameters;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskSearchComparator;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskSearchUtil;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskVo;
import com.globalsight.everest.webapp.pagehandler.tasks.WorkflowTaskDataComparator;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowServer;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.TaskJbpmUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManager;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class MobileService extends HttpServlet
{
    private static final Logger logger = Logger
            .getLogger(MobileService.class);

    private static final long serialVersionUID = 1L;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private PrintWriter writer;
    
    private static final int pageSize = 20;
//    private static final int pageSize = 2;
    private static final MobileSecurity mobileSecurity = new MobileSecurity();

    public static final String ACTION = "action";
    public static final String ACCESSTOKEN = "accessToken";
    
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    
    public static final String STATUS = "status";
    public static final String PAGE_NUMBER = "pageNumber";
    
    public static final String JOB_ID = "jobId";
    public static final String TASK_ID = "taskId";
    public static final String WORKFLOW_ID = "wfId";

    public static final String GS_ERROR_MSG = "GSErrorMsg";

    public static final String SUCCESS = "success";
    public static final String SUCCESS_YES = "yes";
    public static final String SUCCESS_NO = "no";

    private WorkflowManager wfManager = null;
    private JobHandler jobHandler = null;
    private WorkflowServer workflowServer = null;
    private TaskManager taskManager = null;
    private FileProfilePersistenceManager fpManager = null;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    // session valid time 1 hour default.
    private static long SESSION_TIME_OUT = 60 * 60 * 1000;

    public void service(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        String msg = null;

        this.request = p_request;
        this.response = p_response;
        response.setCharacterEncoding(request.getCharacterEncoding());
        response.addHeader("Access-Control-Allow-Origin", "*");

        // ???
        setCompanyId();

        String method = request.getParameter(ACTION);
        // "login" and "logout" method does not need check access.
        if (!"login".equals(method) && !"logout".equals(method))
        {
            String accessToken = request.getParameter(ACCESSTOKEN);
//            accessToken = fixAccessToken(accessToken);
            boolean isValidToken = mobileSecurity.checkAccess(accessToken);
            if (isValidToken)
            {
                // session is timed out?
                Date now = new Date();
                long lastAccessTime = mobileSecurity.getLastAccessTime(accessToken);
                long timeSinceLastAccess = now.getTime() - lastAccessTime;
                if (timeSinceLastAccess > SESSION_TIME_OUT && !"logout".equals(method))
                {
                    mobileSecurity.removeUsernameFromSession(accessToken);
                    msg = "Session is timed out, please re-login.";
                }
                else
                {
                    // update to the latest access time.
                    mobileSecurity.updateAccessTime(accessToken);
                }
            }
            else
            {
                // If token is invalid, return a message to tell this.
                msg = "accessToken is invalid.";
            }
        }

        if (msg != null)
        {
            writeGSErrorMsg(msg, "service");
            return;
        }

        try
        {
            writer = response.getWriter();

            MobileService.class.getMethod(method, null).invoke(MobileService.this);

            writer.close();
        }
        catch (Exception e)
        {
            logger.error("Error when invoke the method:" + method, e);
            writeGSErrorMsg(e.getMessage(), "service");
        }
    }

    public void test()
    {
        writer.write("hello this is mobile net, welcome!");
    }
    
    public void getJobState()
    {
    	String msg = null;
        String jobId = request.getParameter(JOB_ID);

        try
        {
        	 JSONObject json = new JSONObject();
             json.put(SUCCESS, SUCCESS_YES);
             
        	JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(jobId));
        	if (job == null)
        	{
        		 json.put("jobStatus", "removed");
        	}
        	else
        	{
        		String state = job.getState();
        		String returnState = "";
        		
				if (Job.READY_TO_BE_DISPATCHED.equalsIgnoreCase(state)
						|| "READY".equalsIgnoreCase(state)) {
					returnState = "ready";
				} else if (Job.DISPATCHED.equalsIgnoreCase(state)) {
					returnState = "dispatched";
				} else if (Job.EXPORTED.equalsIgnoreCase(state)
						|| Job.EXPORT_FAIL.equalsIgnoreCase(state)) {
					returnState = "exported";
				} else if (Job.PENDING.equalsIgnoreCase(state)
						|| Job.BATCHRESERVED.equalsIgnoreCase(state)
						|| Job.ADD_FILE.equalsIgnoreCase(state)
						|| Job.IMPORTFAILED.equalsIgnoreCase(state)) {
					returnState = "pending";
				}

        		json.put("jobStatus", returnState);
        	}
         	
            writer.write(json.toString());
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error(e);
        }

        writeGSErrorMsg(msg, "getJobState");
    }
    
    public void getTaskState()
    {
    	String msg = null;
    	String accessToken = request.getParameter(ACCESSTOKEN);
        String taskId = request.getParameter(TASK_ID);

        try
        {
        	 JSONObject json = new JSONObject();
             json.put(SUCCESS, SUCCESS_YES);
             
        	TaskImpl t = HibernateUtil.get(TaskImpl.class, Long.parseLong(taskId));
        	if (t == null)
        	{
        		 json.put("taskStatus", "removed");
        	}
        	else
        	{
        		String userId = getUserIdByAccessToken(accessToken);
        		TaskSearchUtil.setState(t, userId);
        		
        		String state = t.getStateAsString();
        		String returnState = "removed";
        		if (Task.STATE_ACTIVE_STR.equalsIgnoreCase(state))
                {
        			returnState = "available";
                }
                else if (Task.STATE_ACCEPTED_STR.equalsIgnoreCase(state))
                {
                	returnState = "accepted";
                }
                else if (Task.STATE_COMPLETED_STR.equalsIgnoreCase(state))
                {
                	returnState = "finished";
                }
        		
        		json.put("taskStatus", returnState);
        	}
           
            writer.write(json.toString());
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error(e);
        }

        writeGSErrorMsg(msg, "getJobState");
    }

    /**
     * Check if userName and password are matched, and decide the menus current
     * user can see.
     * 
     * @throws JSONException
     */
    public void login() throws JSONException
    {
        String accessToken = null;
        String myMenus = null;
        String msg = null;

        String userName = request.getParameter(USERNAME);
        String password = request.getParameter(PASSWORD);
        try
        {
            accessToken = mobileSecurity.doLogin(userName, password);
            logger.debug("User '" + userName + "' is logging in from mobile.");

            PermissionSet perms = new PermissionSet();
            String userId = UserUtil.getUserIdByName(userName);
            perms = Permission.getPermissionManager().getPermissionSetForUser(
                    userId);
            if (perms.getPermissionFor(Permission.JOBS_VIEW))
            {
                myMenus = "jobs";
            }

            if (perms.getPermissionFor(Permission.ACTIVITIES_VIEW))
            {
                if (myMenus == null)
                {
                    myMenus = "activities";
                }
                else
                {
                    myMenus += ",activities";
                }
            }
            
            JSONObject json = new JSONObject();
            json.put(SUCCESS, SUCCESS_YES);
            json.put("accessToken", accessToken == null ? "" : accessToken);
            json.put("myMenus", myMenus == null ? "" : myMenus);
            json.put("permissions", getUserPermissions(perms));
            boolean isSuperPm = false;
            try
            {
                isSuperPm = ServerProxy.getUserManager()
                        .containsPermissionGroup(userId,
                                WebAppConstants.SUPER_PM_NAME);
            }
            catch (Exception e)
            {
                throw new MobileServiceException("getUserManager",e);
            }

            if (isSuperPm)
            {
                String superCompanyName = null;
                String[] companyNameStrings = null;
                try
                {
                    superCompanyName = ServerProxy
                            .getJobHandler()
                            .getCompanyById(
                                    Long.parseLong(CompanyWrapper.SUPER_COMPANY_ID))
                            .getCompanyName();
                    companyNameStrings = CompanyWrapper.getAllCompanyNames();
                }
                catch (Exception e)
                {
                    throw new MobileServiceException("getSuperCompanyName", e);
                }
                ArrayList companyNames = new ArrayList(
                        companyNameStrings.length);
                for (int i = 0; i < companyNameStrings.length; i++)
                    {
                        if (superCompanyName.equals(companyNameStrings[i]))
                        {
                            continue;
                        }
                        companyNames.add(companyNameStrings[i]);
                    }
                json.put("companyNames", companyNames);
            }
            
            writer.write(json.toString());
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when login with userName " + userName
                    + " and password " + password, e);
        }

        writeGSErrorMsg(msg, "login");
    }

    /**
     * Logout.
     * 
     * @throws JSONException
     */
    public void logout() throws JSONException
    {
        String msg = null;
        String accessToken = request.getParameter(ACCESSTOKEN);
        try
        {
            mobileSecurity.removeUsernameFromSession(accessToken);
            writeSuccess();
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Fail to logout with token " + accessToken, e);
        }

        writeGSErrorMsg(msg, "logout");
    }

    private List<JobImpl> subJob(String jobId, String jobCondition, List<JobImpl> jobs)
    {
    	List<JobImpl> subJobs = jobs;
    	
    	if (jobId != null && jobId.trim().length() > 0)
    	{
            long id = Long.parseLong(jobId);
        	
        	if ("lt".equalsIgnoreCase(jobCondition))
        	{
        		boolean found = false;
        		for (int i = 0; i < jobs.size(); i++)
        		{
        			JobImpl job = jobs.get(i);
        			if (job.getId() < id)
        			{
        				subJobs = jobs.subList(i, jobs.size());
        				found = true;
        				break;
        			}
        		}
        		
        		if (!found)
        		{
        			return new ArrayList<JobImpl>();
        		}
        	}
        	else
        	{
        		for (int i = 0; i < jobs.size(); i++)
        		{
        			JobImpl job = jobs.get(i);
        			if (job.getId() <= id)
        			{
        				subJobs =  jobs.subList(0, i);
        				break;
        			}
        		};
        	}
    	}
    	
    	if ("lt".equalsIgnoreCase(jobCondition) || jobCondition.length() == 0)
    	{
    		if (subJobs.size() > pageSize)
			{
				subJobs = subJobs.subList(0, pageSize);
			}
    	}
    	else
    	{
    		int size = subJobs.size();
			if (size > 5)
			{
				subJobs = subJobs.subList(size - 5, size);
			}
    	}

    	return subJobs;
    }
    
    private List<TaskVo> subActivity(String taskId, String jobCondition,  List<TaskVo> ts)
    {
        List<TaskVo> subActivity = ts;
    	
    	if (taskId != null && taskId.trim().length() > 0)
    	{
            long id = Long.parseLong(taskId);
        	
        	if ("lt".equalsIgnoreCase(jobCondition))
        	{
        		boolean found = false;
        		for (int i = 0; i < ts.size(); i++)
        		{
        			TaskVo tv = ts.get(i);
        			if (tv.getTaskId() < id)
        			{
        				subActivity = ts.subList(i, ts.size());
        				found = true;
        				break;
        			}
        		}
        		
        		if (!found)
        		{
        			return new ArrayList<TaskVo>();
        		}
        	}
        	else
        	{
        		for (int i = 0; i < ts.size(); i++)
        		{
        			TaskVo tv = ts.get(i);
        			if (tv.getTaskId() <= id)
        			{
        				subActivity =  ts.subList(0, i);
        				break;
        			}
        		};
        	}
    	}
    	
    	if ("lt".equalsIgnoreCase(jobCondition) || jobCondition.length() == 0)
    	{
    		if (subActivity.size() > pageSize)
			{
    			subActivity = subActivity.subList(0, pageSize);
			}
    	}
    	else
    	{
    		int size = subActivity.size();
			if (size > 5)
			{
				subActivity = subActivity.subList(size - 5, size);
			}
    	}

    	return subActivity;
    }
    
    /**
     * Get job list for state "READY_TO_BE_DISPATCH", "DISPATCHED", "EXPORTED"
     * and "PENDING".
     * 
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    public void getJobs() throws JSONException
    {
        String msg = null;

        String accessToken = request.getParameter(ACCESSTOKEN);
        String pageNumber = request.getParameter(PAGE_NUMBER);
        String status = request.getParameter(STATUS);
        String jobId = request.getParameter("jobId");
        String jobCondition = request.getParameter("jobCondition");

        try
        {
            PermissionSet userPerms = getUserPerms(accessToken);
            Vector<String> jobStates = getJobStates(status);
            String userId = getUserIdByAccessToken(accessToken);

            Set<JobImpl> jobsSet = new HashSet<JobImpl>();
            if (userPerms.getPermissionFor(Permission.JOB_SCOPE_ALL))
            {
                jobsSet.addAll(WorkflowHandlerHelper
                        .getJobsByStateList(jobStates));
            }
            else
            {
                if (userPerms.getPermissionFor(Permission.PROJECTS_MANAGE))
                {
                    jobsSet.addAll(WorkflowHandlerHelper
                            .getJobsByManagerIdAndStateList(userId, jobStates));
                }

                if (userPerms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
                {
                    jobsSet.addAll(WorkflowHandlerHelper
                            .getJobsByWfManagerIdAndStateList(userId, jobStates));
                }

                if (userPerms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
                {
                    jobsSet.addAll(getJobHandler().getJobsByUserIdAndState(
                            userId, jobStates));
                }
            }

            // Default: Sort by Job ID, descending, so the latest job will be at
            // the top of the list
            JobComparator comparator = new JobComparator(JobComparator.JOB_ID,
                    false, null);
            List<JobImpl> jobs = new ArrayList<JobImpl>(jobsSet);
            SortUtil.sort(jobs, comparator);
            jobs = subJob(jobId, jobCondition, jobs);

            int numOfJobs = jobs == null ? 0 : jobs.size();
            if (numOfJobs == 0)
            {
            	 writer.write("[]");
            }
            else
            {
            	JSONArray jsonArr = new JSONArray();
                for (JobImpl job : jobs)
                {
                    JSONObject json = new JSONObject();
                    json.put("jobId", job.getJobId());
                    json.put("jobName", job.getJobName());
                    json.put("project", job.getProject().getName());
                    json.put("jobWC", job.getWordCount());
                    json.put("sourceLocale",
                            makeSimpleLocaleString(job.getSourceLocale()));
                    json.put("dateCreated", dateFormat.format(job.getCreateDate()));
                    json.put("jobStatus", job.getState());
                    json.put("jobCompanyName",
                            CompanyWrapper.getCompanyNameById(job.getCompanyId()));
                    json.put("pageCount", job.getPageCount());
                    json.put("jobCreator", job.getCreateUserId());
                    jsonArr.put(json);
                }
                writer.write(jsonArr.toString());
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when listJob for pageNumber " + pageNumber
                    + " and status " + status, e);
        }

        writeGSErrorMsg(msg, "listJob");
    }

    /**
     * Get job source files and workflows information.
     * 
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    public void detailJob() throws JSONException
    {
        String msg = null;
        String jobId = request.getParameter(JOB_ID);

        JSONObject json = new JSONObject();
        JSONArray jsonSourceFilesArr = new JSONArray();
        JSONArray jsonWorkflowsArr = new JSONArray();
        try
        {
            Job job = getJobHandler().getJobById(Long.parseLong(jobId));
            Collection<SourcePage> sourcePages = job.getSourcePages();
            for (SourcePage sp : sourcePages)
            {
                JSONObject spJson = new JSONObject();
                spJson.put("fileName", sp.getShortPageName());
                long fpId = sp.getRequest().getDataSourceId();
                FileProfile fp = getFileProfileManager().getFileProfileById(
                        fpId, false);
                String fpName = "";
                if (fp != null)
                {
                    fpName = fp.getName();
                }
                spJson.put("fileProfile", fpName);
                spJson.put("jobFileWC", sp.getWordCount());
//                spJson.put("externalPageId", sp.getExternalPageId());
//                spJson.put("displayPageName", sp.getDisplayPageName());
                jsonSourceFilesArr.put(spJson);
            }

            Collection<Workflow> workflows = job.getWorkflows();
            for (Workflow wf : workflows)
            {
                if (!Workflow.CANCELLED.equalsIgnoreCase(wf.getState()))
                {
                    JSONObject wfJson = new JSONObject();
                    wfJson.put("wfId", wf.getId());
                    wfJson.put("targetLocale",
                            makeSimpleLocaleString(wf.getTargetLocale()));
                    wfJson.put("state", getWorkflowStateString(wf));
                    wfJson.put("CurrentAc", getWorkflowCurrentActivityName(wf));
                    jsonWorkflowsArr.put(wfJson);
                }
            }

            json.put("SourceFiles", jsonSourceFilesArr);
            json.put("Workflows", jsonWorkflowsArr);

            writer.write(json.toString());
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when get job details for jobId : " + jobId, e);
        }

        writeGSErrorMsg(msg, "detailJob");
    }

    public void discardJob() throws JSONException
    {
        String msg = null;

        String accessToken = request.getParameter(ACCESSTOKEN);
//        accessToken = fixAccessToken(accessToken);
        String userId = getUserIdByAccessToken(accessToken);
        String[] jobIds = request.getParameterValues("jobId");
        if (jobIds == null)
        {
        	jobIds = request.getParameterValues("jobId[]");
        }

        try
        {
            if (jobIds != null && jobIds.length > 0)
            {
                logger.info("Trying to discard jobs from mobile for jobIds '"
                        + getStringFromArray(jobIds) + "'.");
                for (int i = 0; i < jobIds.length; i++)
                {
                    String jobId = jobIds[i];
                    Job job = getJobHandler().getJobById(Long.parseLong(jobId));

                    if (!Job.CANCELLED.equals(job.getState()))
                    {
                        // Discard all workflows in this job
                        String wfState = null;
                        getJobHandler().cancelJob(userId, job, wfState);
                    }
                }
                writeSuccess();
            }
            else
            {
                msg = "Invalid parameter : jobId(s) : " + jobIds;
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when discardJob for jobIds : " + jobIds, e);
        }

        writeGSErrorMsg(msg, "discardJob");
    }

    /**
     * Discard workflow(s).
     * 
     * @throws JSONException
     */
    public void discardWorkflow() throws JSONException
    {
        String msg = null;

        String accessToken = request.getParameter(ACCESSTOKEN);
        String userId = getUserIdByAccessToken(accessToken);
        String[] workflowIds = request.getParameterValues(WORKFLOW_ID);
        if (workflowIds == null)
        {
        	workflowIds = request.getParameterValues("wfId[]");
        }

        try
        {
            if (workflowIds != null && workflowIds.length > 0)
            {
                logger.info("Trying to discard workflows from mobile for workflowIds '"
                        + getStringFromArray(workflowIds) + "'.");
                for (int i = 0; i < workflowIds.length; i++)
                {
                    String wfId = workflowIds[i];
                    if (canUpdateWorkflow(Long.parseLong(wfId)))
                    {
                        Workflow wf = getWorkflowManager()
                                .getWorkflowByIdRefresh(Long.parseLong(wfId));
                        getWorkflowManager().cancel(userId, wf);
                    }
                }
                writeSuccess();
            }
            else
            {
                msg = "Invalid parameter : workflowId(s) : " + workflowIds;
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when discard workflow for workflow Ids : "
                    + workflowIds, e);
        }

        writeGSErrorMsg(msg, "discardWorkflow");
    }

    public void dispatchJob() throws JSONException
    {
        String msg = null;

        String[] jobIds = request.getParameterValues("jobId");
        if (jobIds == null)
        {
        	jobIds = request.getParameterValues("jobId[]");
        }
        
        try
        {
            String jobName = validateBeforeDispatch(jobIds);
            if (jobName.length() > 0)
            {
                msg = "The following job(s) can not be dispatched until cost center attribute and required attributes are set '"
                        + jobName + "'.";
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Trying to dispatch jobs from mobile for jobIds '"
                            + getStringFromArray(jobIds) + "'.");  
                }
                for (String jobId : jobIds)
                {
                    Job job = getJobHandler().getJobById(Long.parseLong(jobId));
                    if (Job.READY_TO_BE_DISPATCHED.equals(job.getState()))
                    {
                        getWorkflowManager().dispatch(job);
                    }
                }
                writeSuccess();
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when dispatch job for job Ids : "
                    + jobIds, e);
        }

        writeGSErrorMsg(msg, "dispatchJob");
    }

    public void dispatchWorkflow() throws JSONException
    {
        String msg = null;

        String[] workflowIds = request.getParameterValues(WORKFLOW_ID);
        if (workflowIds == null)
        {
        	workflowIds = request.getParameterValues("wfId[]");
        }
        
        try
        {
            if (workflowIds != null && workflowIds.length > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Dispatch workflows from mobile for workflowIds '"
                            + getStringFromArray(workflowIds) + "'.");       
                }
                JSONArray jsonArr = new JSONArray();
                
                for (int i = 0; i < workflowIds.length; i++)
                {
                    String wfId = workflowIds[i];
                    if (canUpdateWorkflow(Long.parseLong(wfId)))
                    {
                        Workflow wf = getWorkflowManager()
                                .getWorkflowByIdRefresh(Long.parseLong(wfId));
                        if (Workflow.READY_TO_BE_DISPATCHED.equals(wf.getState()))
                        {
                            getWorkflowManager().dispatch(wf);    
                            JSONObject json = new JSONObject();
                            json.put("wfId", wf.getId());
                            json.put("CurrentAc", getWorkflowCurrentActivityName(wf));
                            jsonArr.put(json);
                        }
                    }
                }
                
                writer.write(jsonArr.toString());
//                writeSuccess();
            }
            else
            {
                msg = "Invalid parameter : workflowId(s) : " + workflowIds;
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when discard workflow for workflow Ids : "
                    + workflowIds, e);
        }

        writeGSErrorMsg(msg, "dispatchWorkflow");
    }
    
    public void getActivities() throws JSONException
    {
        String msg = null;

        String status = request.getParameter(STATUS);
        String accessToken = request.getParameter(ACCESSTOKEN);
        String taskId = request.getParameter("taskId");
        String condition = request.getParameter("condition");

        try
        {
            String userId = getUserIdByAccessToken(accessToken);
            User user = UserUtil.getUserById(userId);
            PermissionSet perms = getUserPerms(accessToken);
            boolean isProjectManager = TaskSearchUtil.isProjectManager(user);
            boolean canManageProject = perms
                    .getPermissionFor(Permission.PROJECTS_MANAGE);

            TaskSearchParameters searchParams = new TaskSearchParameters();
            int intStatus = getTaskStatus(status);
            searchParams.setActivityState(intStatus);

            List<TaskVo> vos = TaskSearchUtil.search(user, searchParams);

            SortUtil.sort(vos, new Comparator<TaskVo>() {

				@Override
				public int compare(TaskVo o1, TaskVo o2) {
					return (int) (o2.getTaskId() - o1.getTaskId());
				}
			});
            
            vos = subActivity(taskId, condition, vos);

            int numOfTasks = vos == null ? 0 : vos.size();
            if (numOfTasks == 0)
            {
            	 writer.write("[]");
            }
            else
            {
                List<Task> tasks = new ArrayList<Task>();
                for (TaskVo vo : vos)
                {
                    TaskImpl t = HibernateUtil.get(TaskImpl.class, vo.getTaskId());

                    if (WorkflowConstants.TASK_ALL_STATES == intStatus)
                    {
                        TaskSearchUtil.setState(t, user.getUserId());
                    }
                    else
                    {
                        t.setState(intStatus);
                    }

                    if (isProjectManager || canManageProject)
                    {
                        TaskSearchUtil.setAllAssignees(t);
                    }

                    tasks.add(t);
                }

                JSONArray jsonArr = new JSONArray();
                for (Task task : tasks)
                {
                    Job job = getJobHandler().getJobById(task.getJobId());

                    JSONObject json = new JSONObject();
                    json.put("jobName", job.getJobName());
                    json.put("jobId", job.getJobId());
                    json.put("activity",
                            getWorkflowCurrentActivityName(task.getWorkflow()));
                    json.put("company", CompanyWrapper.getCompanyNameById(task
                            .getCompanyId()));
                    json.put("project", task.getProjectName());
                    json.put("projectManager", task.getProjectManagerName());
                    json.put("taskWC", job.getWordCount());
                    json.put("priority", task.getPriority());
                    String locales = makeSimpleLocaleString(task.getSourceLocale())
                            + "-" + makeSimpleLocaleString(task.getTargetLocale()); 
                    json.put("locales", locales);
//                    json.put("dueBy", "01/08/2013");
//                    json.put("overdue", "no");
                    json.put("status", task.getStateAsString());
                    json.put("taskId", task.getId());
                    String assignees = task.getAllAssigneesAsString();
                    if (assignees != null)
                    {
                        assignees = assignees.replaceAll("<BR>", ",");
                    }
                    json.put("assignees", assignees);// this is for available tasks
                    json.put("acceptor", task.getAcceptor());

                    jsonArr.put(json);
                }
                writer.write(jsonArr.toString());
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when get avtivity list for status " + status, e);
        }

        writeGSErrorMsg(msg, "listActivity");
    }

    @SuppressWarnings("unchecked")
    public void detailActivity() throws JSONException
    {
        String msg = null;
        String taskId = request.getParameter(TASK_ID);
        try
        {
            JSONObject json = new JSONObject();
            JSONArray jsonSourceFilesArr = new JSONArray();

            TaskImpl task = HibernateUtil.get(TaskImpl.class,
                    Long.parseLong(taskId));
            if (task != null)
            {
                Job job = getJobHandler().getJobById(task.getJobId());
                Collection<SourcePage> sourcePages = job.getSourcePages();
                for (SourcePage sp : sourcePages)
                {
                    JSONObject spJson = new JSONObject();
                    spJson.put("fileName", sp.getShortPageName());
                    spJson.put("taskFileWC", sp.getWordCount());
                    jsonSourceFilesArr.put(spJson);
                }
                json.put("Files", jsonSourceFilesArr);
                writer.write(json.toString());
            }
            else
            {
                msg = "can't get task by task Id " + taskId;
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when get activity details for taskId : " + taskId, e);
        }

        writeGSErrorMsg(msg, "detailActivity");
    }

    public void acceptTask() throws JSONException
    {
        String msg = null;

        String accessToken = request.getParameter(ACCESSTOKEN);
        String userId = getUserIdByAccessToken(accessToken);
        String[] taskIds = request.getParameterValues(TASK_ID);
        if (taskIds == null)
        {
        	taskIds = request.getParameterValues("taskId[]");
        }
        
        try
        {
            if (taskIds != null && taskIds.length > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Trying to accept tasks from mobile for taskIds '"
                            + getStringFromArray(taskIds) + "'.");   
                }
                for (String taskId : taskIds)
                {
                    TaskImpl task = HibernateUtil.get(TaskImpl.class,
                            Long.parseLong(taskId));
                    if (task != null)
                    {
                        WorkflowTaskInstance wfTask = getWorkflowServer()
                                .getWorkflowTaskInstance(userId, task.getId(),
                                        Task.STATE_ACTIVE);
                        task.setWorkflowTask(wfTask);

                        if (!task.getAllAssignees().contains(userId))
                            continue;

                        if (task.getState() != Task.STATE_ACTIVE)
                            continue;

                        TaskHelper.acceptTask(userId, task);
                    }
                }
                writeSuccess();
            }
            else
            {
                msg = "Invalid parameter : taskId(s) : " + taskIds;
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when accept task for taskIds : " + taskIds, e);
        }

        writeGSErrorMsg(msg, "acceptTask");
    }
    
    public void completeTask() throws JSONException
    {
        String msg = null;

        String accessToken = request.getParameter(ACCESSTOKEN);
        String userId = getUserIdByAccessToken(accessToken);
        String[] taskIds = request.getParameterValues(TASK_ID);
        if (taskIds == null)
        {
        	taskIds = request.getParameterValues("taskId[]");
        }
        
        try
        {
            if (taskIds != null && taskIds.length > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Trying to complete tasks from mobile for taskIds '"
                            + getStringFromArray(taskIds) + "'.");
                }
                for (String taskId : taskIds)
                {
                    TaskImpl task = HibernateUtil.get(TaskImpl.class,
                            Long.parseLong(taskId));

                    // Is not off-line uploading to current task
                    if (task != null && task.getIsUploading() != 'Y')
                    {
                        int taskState = task.getState();
                        // Available tasks are allowed to accept and complete at
                        // one step.
                        if (taskState == Task.STATE_ACTIVE)
                        {
                            WorkflowTaskInstance wfTask = getWorkflowServer()
                                    .getWorkflowTaskInstance(userId,
                                            task.getId(), Task.STATE_ACTIVE);
                            task.setWorkflowTask(wfTask);
                            if (!task.getAllAssignees().contains(userId))
                                continue;

                            if (task.getState() != Task.STATE_ACTIVE)
                                continue;

                            // accept current activity
                            getTaskManager().acceptTask(userId, task, false);
                        }

                        if (taskState == Task.STATE_ACCEPTED)
                        {
                            WorkflowTaskInstance wti = getWorkflowServer()
                                    .getWorkflowTaskInstance(
                                            task.getWorkflow().getId(),
                                            task.getId());
                            task.setWorkflowTask(wti);

                            // complete task on the default path
                            getTaskManager().completeTask(userId, task, null, null);

                            // Auto-accept next task.
                            TaskHelper.autoAcceptNextTask(task, null);                            
                        }
                    }
                }
                writeSuccess();
            }
            else
            {
                msg = "Invalid parameter : taskId(s) : " + taskIds;
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when complete tasks for taskIds " + taskIds, e);
        }

        writeGSErrorMsg(msg, "completeTask");
    }

    private void setCompanyId()
    {
        try
        {
            String companyName =request.getParameter("companyName");
            if (StringUtils.isEmpty(companyName))
            {
                companyName = UserUtil.getCurrentCompanyName(request);
            }
            if (companyName != null)
            {
                long companyId = ServerProxy.getJobHandler()
                        .getCompany(companyName).getIdAsLong();
                CompanyThreadLocal.getInstance().setIdValue("" + companyId);                
            }
        }
        catch (Exception e)
        {
            logger.error("Can not get the Company!");
        }
    }

    /**
     * Determine the job state(s).
     * 
     * Ready: READY_TO_BE_DISPATCHED
     * In Progress :DISPATCHED
     * Exported: EXPORTED, EXPORT_FAILED
     * Pending: PENDING, BATCH_RESERVED, ADDING_FILES
     * 
     * @param p_status
     * @return
     */
    private Vector<String> getJobStates(String p_status)
    {
        Vector<String> jobStates = new Vector<String>();

        if (Job.READY_TO_BE_DISPATCHED.equalsIgnoreCase(p_status)
                || "READY".equalsIgnoreCase(p_status))
        {
            jobStates.add(Job.READY_TO_BE_DISPATCHED);
        }
        else if (Job.DISPATCHED.equalsIgnoreCase(p_status))
        {
            jobStates.add(Job.DISPATCHED);
        }
        else if (Job.EXPORTED.equalsIgnoreCase(p_status))
        {
            jobStates.add(Job.EXPORTED);
            jobStates.add(Job.EXPORT_FAIL);
        }
        else if (Job.PENDING.equalsIgnoreCase(p_status))
        {
            jobStates.add(Job.PENDING);
            jobStates.add(Job.BATCHRESERVED);//
            jobStates.add(Job.ADD_FILE);//
            jobStates.add(Job.IMPORTFAILED);
        }

        return jobStates;
    }
    
    private int getTaskStatus(String p_status)
    {
        if ("available".equalsIgnoreCase(p_status))
        {
            return Task.STATE_ACTIVE; // 3
        }
        else if ("accepted".equalsIgnoreCase(p_status))
        {
            return Task.STATE_ACCEPTED; // 8
        }
        else if ("finished".equalsIgnoreCase(p_status))
        {
            return Task.STATE_COMPLETED; // -1
        }
        else
        {
            return Task.STATE_ALL; // -10
        }
    }

    /**
     * If current job is in "ADDING FILES" state, can't make any changes on its
     * workflows.
     * 
     * @param workflowId
     * @return boolean
     */
    private boolean canUpdateWorkflow(long workflowId)
    {
        try
        {
            Workflow wf = getWorkflowManager().getWorkflowById(workflowId);
            if (Job.ADD_FILE.equalsIgnoreCase(wf.getJob().getState()))
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    private String validateBeforeDispatch(String[] jobIds)
    {
        StringBuffer jobName = new StringBuffer();
        for (String id : jobIds)
        {
            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(id));
            if (!job.hasSetCostCenter())
            {
                if (jobName.length() > 0)
                {
                    jobName.append(", ");
                }

                jobName.append(job.getName());
            }
        }

        return jobName.toString();
    }

    /**
     * Get current activity name by workflow. 
     * @param workflow
     * @return
     */
    private String getWorkflowCurrentActivityName(Workflow workflow)
    {
        String result = "";
        if (workflow != null && !Workflow.SKIPPING.equals(workflow.getState()))
        {
            TaskInstance task = WorkflowManagerLocal.getCurrentTask(workflow
                    .getId());
            if (task != null)
            {
                result = TaskJbpmUtil.getTaskDisplayName(task.getName());
            }
        }

        return result;
    }

    private PermissionSet getUserPerms(String p_accessToken)
            throws MobileServiceException
    {
        PermissionSet perms = null;
        try
        {
            String userName = mobileSecurity
                    .getUsernameFromSession(p_accessToken);
            String userId = UserUtil.getUserIdByName(userName);
            perms = Permission.getPermissionManager().getPermissionSetForUser(
                    userId);
        }
        catch (Exception e)
        {
            throw new MobileServiceException(
                    "Fail to get permission set by accessToken "
                            + p_accessToken, e);
        }
        
        return perms;
    }

    private String getUserIdByAccessToken(String p_accessToken)
    {
        String userName = mobileSecurity.getUsernameFromSession(p_accessToken);
        return UserUtil.getUserIdByName(userName);
    }
    
    private WorkflowManager getWorkflowManager()
    {
        if (wfManager != null)
            return wfManager;

        return ServerProxy.getWorkflowManager();
    }

    private JobHandler getJobHandler() throws GeneralException,
            RemoteException, NamingException
    {
        if (jobHandler != null)
            return jobHandler;

        return ServerProxy.getJobHandler();
    }

    private WorkflowServer getWorkflowServer()
    {
        if (workflowServer != null)
            return workflowServer;

        return ServerProxy.getWorkflowServer();
    }

    private TaskManager getTaskManager()
    {
        if (taskManager != null)
            return taskManager;

        return ServerProxy.getTaskManager();
    }

    private FileProfilePersistenceManager getFileProfileManager()
            throws GeneralException, RemoteException, NamingException
    {
        if (fpManager != null)
            return fpManager;
        
        return ServerProxy.getFileProfilePersistenceManager();
    }
    
    private void writeGSErrorMsg(String p_msg, String p_method)
    {
        if (p_msg != null)
        {
            try
            {
                if (p_method != null && !p_method.endsWith("()"))
                {
                    p_method += "()";
                }
                JSONObject json = new JSONObject();
                json.put(SUCCESS, SUCCESS_NO);
                json.put(GS_ERROR_MSG, p_method + "::" + p_msg);
                writer.write(json.toString());
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void writeSuccess()
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put(SUCCESS, SUCCESS_YES);
            writer.write(json.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private String makeSimpleLocaleString(GlobalSightLocale gsLocale)
    {
        if (gsLocale == null)
            return null;
        
        return gsLocale.getLanguageCode() + "_" + gsLocale.getCountryCode();
    }

    private int checkPageNumber(String p_pageNumberFromRequest, int p_allSize,
            int p_pageSize)
    {
        int pageNumber = 1;
        try
        {
            pageNumber = Integer.parseInt(p_pageNumberFromRequest);
            int totalPageNumber = getTotalPageNumber(p_allSize, p_pageSize);

            if (pageNumber < 1)
            {
                pageNumber = 1;                
            }
            if (pageNumber > totalPageNumber)
            {
                pageNumber = totalPageNumber;                
            }
        }
        catch (Exception e)
        {
            pageNumber = 1;
        }

        return pageNumber;
    }

    private int getTotalPageNumber(int p_allSize, int p_pageSize)
    {
        int totalPageNumber = 0;
        if (p_allSize % p_pageSize > 0)
        {
            totalPageNumber = Math.round(p_allSize / p_pageSize) + 1;
        }
        else
        {
            totalPageNumber = Math.round(p_allSize / p_pageSize);
        }

        return totalPageNumber;
    }

    /**
     * When the access token is passed to server via URL, the "+" in token will
     * become to white space " ", need replace them back.
     * 
     * @param p_accessToken
     * @return
     */
    private String fixAccessToken(String p_accessToken)
    {
        if (p_accessToken == null)
            return null;

        return p_accessToken.replaceAll(" ", "+");
    }

    private String getWorkflowStateString(Workflow wf)
    {
        String result = "";
        String state = wf.getState();
        if (Workflow.READY_TO_BE_DISPATCHED.equalsIgnoreCase(state))
        {
            result = "Ready";
        }
        else if (Workflow.DISPATCHED.equalsIgnoreCase(state))
        {
            result = "Dispatched";
        }
        else if (Workflow.BATCHRESERVED.equalsIgnoreCase(state))
        {
            result = "Batch_Reserved";
        }
        else if (Workflow.IMPORT_FAILED.equalsIgnoreCase(state))
        {
            result = "Import_Failed";
        }
        else if (Workflow.LOCALIZED.equalsIgnoreCase(state))
        {
            result = "Localized";
        }
        else if (Workflow.EXPORTING.equalsIgnoreCase(state))
        {
            result = "Exporting";
        }
        else if (Workflow.EXPORTED.equalsIgnoreCase(state))
        {
            result = "Exported";
        }
        else if (Workflow.EXPORT_FAILED.equalsIgnoreCase(state))
        {
            result = "Export_Failed";
        }
        else if (Workflow.ARCHIVED.equalsIgnoreCase(state))
        {
            result = "Archived";            
        }
        else if (Workflow.PENDING.equalsIgnoreCase(state))
        {
            result = "Pending";
        }
        else if (Workflow.SKIPPING.equalsIgnoreCase(state))
        {
            result = "Skipping";
        }
        else if (Workflow.CANCELLED.equalsIgnoreCase(state))
        {
            result = "Cancelled";
        }
        else
        {
            result = state.toLowerCase();
        }

        return result;
    }

    private String getUserPermissions(PermissionSet perms)
    {
    	ArrayList<String> ps = new ArrayList<String>();
    	ps.add(Permission.JOBS_DISPATCH);
    	ps.add(Permission.JOBS_DISCARD);
    	ps.add(Permission.JOB_WORKFLOWS_DISPATCH);
    	ps.add(Permission.JOB_WORKFLOWS_DISCARD);
    	ps.add(Permission.ACTIVITIES_ACCEPT_ALL);
    	ps.add(Permission.ACTIVITIES_BATCH_COMPLETE_ACTIVITY);
    	ps.add(Permission.ACTIVITIES_ACCEPT);
    	ps.add(Permission.PROJECTS_MANAGE);

        StringBuilder result = new StringBuilder();

        for (String p : ps) 
        {
        	if (perms.getPermissionFor(p))
            {
                appendPermission(result, p);
            }
        }

        return result.toString().replace(".", "_");
    }

    private void appendPermission(StringBuilder permissions, String permission)
    {
        if (permissions.length() == 0)
        {
            permissions.append("perm.").append(permission);
        }
        else
        {
            permissions.append(",perm.").append(permission);
        }
    }

    private String getStringFromArray(String[] array)
    {
        StringBuilder result = new StringBuilder();
        for (String str : array)
        {
            if (result.length() == 0)
            {
                result.append(str);
            }
            else
            {
                result.append(",").append(str);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Get job list for state "READY_TO_BE_DISPATCH", "DISPATCHED", "EXPORTED"
     * and "PENDING".
     * 
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    public void listJob() throws JSONException
    {
        String msg = null;

        String accessToken = request.getParameter(ACCESSTOKEN);
        String pageNumber = request.getParameter(PAGE_NUMBER);
        String status = request.getParameter(STATUS);

        try
        {
            PermissionSet userPerms = getUserPerms(accessToken);
            Vector<String> jobStates = getJobStates(status);
            String userId = getUserIdByAccessToken(accessToken);

            Set<JobImpl> jobsSet = new HashSet<JobImpl>();
            if (userPerms.getPermissionFor(Permission.JOB_SCOPE_ALL))
            {
                jobsSet.addAll(WorkflowHandlerHelper
                        .getJobsByStateList(jobStates));
            }
            else
            {
                if (userPerms.getPermissionFor(Permission.PROJECTS_MANAGE))
                {
                    jobsSet.addAll(WorkflowHandlerHelper
                            .getJobsByManagerIdAndStateList(userId, jobStates));
                }

                if (userPerms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
                {
                    jobsSet.addAll(WorkflowHandlerHelper
                            .getJobsByWfManagerIdAndStateList(userId, jobStates));
                }

                if (userPerms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
                {
                    jobsSet.addAll(getJobHandler().getJobsByUserIdAndState(
                            userId, jobStates));
                }
            }

            // Default: Sort by Job ID, descending, so the latest job will be at
            // the top of the list
            JobComparator comparator = new JobComparator(JobComparator.JOB_ID,
                    false, null);
            List<JobImpl> jobs = new ArrayList<JobImpl>(jobsSet);
            SortUtil.sort(jobs, comparator);

            int numOfJobs = jobs == null ? 0 : jobs.size();
            if (numOfJobs == 0)
            {
                msg = "There are no jobs to return.";
            }
            else
            {
                int intPageNumber = checkPageNumber(pageNumber, numOfJobs, pageSize);
                int jobListStart = (intPageNumber - 1) * pageSize;
                int jobListEnd = (jobListStart + pageSize) > numOfJobs ? numOfJobs
                        : (jobListStart + pageSize);
                jobListEnd = jobListEnd - 1;

                int i = 0;
                JSONArray jsonArr = new JSONArray();
                for (i = jobListStart; i <= jobListEnd; i++)
                {
                    Job job = (Job) jobs.get(i);
                    JSONObject json = new JSONObject();
                    json.put("jobId", job.getJobId());
                    json.put("jobName", job.getJobName());
                    json.put("project", job.getProject().getName());
                    json.put("jobWC", job.getWordCount());
                    json.put("sourceLocale",
                            makeSimpleLocaleString(job.getSourceLocale()));
                    json.put("dateCreated", dateFormat.format(job.getCreateDate()));
                    json.put("jobStatus", job.getState());
                    json.put("jobCompanyName",
                            CompanyWrapper.getCompanyNameById(job.getCompanyId()));
                    json.put("pageCount", job.getPageCount());
                    json.put("jobCreator", job.getCreateUserId());
                    jsonArr.put(json);
                }
                writer.write(jsonArr.toString());
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when listJob for pageNumber " + pageNumber
                    + " and status " + status, e);
        }

        writeGSErrorMsg(msg, "listJob");
    }
    
    public void listActivity() throws JSONException
    {
        String msg = null;

        String status = request.getParameter(STATUS);
        String pageNumber = request.getParameter(PAGE_NUMBER);
        String accessToken = request.getParameter(ACCESSTOKEN);

        try
        {
            String userId = getUserIdByAccessToken(accessToken);
            User user = UserUtil.getUserById(userId);
            PermissionSet perms = getUserPerms(accessToken);
            boolean isProjectManager = TaskSearchUtil.isProjectManager(user);
            boolean canManageProject = perms
                    .getPermissionFor(Permission.PROJECTS_MANAGE);

            TaskSearchParameters searchParams = new TaskSearchParameters();
            int intStatus = getTaskStatus(status);
            searchParams.setActivityState(intStatus);

            List<TaskVo> vos = TaskSearchUtil.search(user, searchParams);

            if (vos.size() > 1)
            {
                SortUtil.sort(vos, new TaskSearchComparator(
                        WorkflowTaskDataComparator.JOB_ID, false));
            }

            int numOfTasks = vos == null ? 0 : vos.size();
            if (numOfTasks == 0)
            {
                msg = "There are no tasks to return.";
            }
            else
            {
                int intPageNumber = checkPageNumber(pageNumber, numOfTasks,
                        pageSize);
                int start = (intPageNumber - 1) * pageSize;
                int end = (start + pageSize) > numOfTasks ? numOfTasks
                        : (start + pageSize);
                end = end - 1;

                List<Task> tasks = new ArrayList<Task>();
                for (int i = start; i <= end; i++)
                {
                    TaskVo vo = vos.get(i);
                    TaskImpl t = HibernateUtil.get(TaskImpl.class, vo.getTaskId());

                    if (WorkflowConstants.TASK_ALL_STATES == intStatus)
                    {
                        TaskSearchUtil.setState(t, user.getUserId());
                    }
                    else
                    {
                        t.setState(intStatus);
                    }

                    if (isProjectManager || canManageProject)
                    {
                        TaskSearchUtil.setAllAssignees(t);
                    }

                    tasks.add(t);
                }

                JSONArray jsonArr = new JSONArray();
                for (Task task : tasks)
                {
                    Job job = getJobHandler().getJobById(task.getJobId());

                    JSONObject json = new JSONObject();
                    json.put("jobName", job.getJobName());
                    json.put("jobId", job.getJobId());
                    json.put("activity",
                            getWorkflowCurrentActivityName(task.getWorkflow()));
                    json.put("company", CompanyWrapper.getCompanyNameById(task
                            .getCompanyId()));
                    json.put("project", task.getProjectName());
                    json.put("projectManager", task.getProjectManagerName());
                    json.put("taskWC", job.getWordCount());
                    json.put("priority", task.getPriority());
                    String locales = makeSimpleLocaleString(task.getSourceLocale())
                            + "-" + makeSimpleLocaleString(task.getTargetLocale()); 
                    json.put("locales", locales);
//                    json.put("dueBy", "01/08/2013");
//                    json.put("overdue", "no");
                    json.put("status", task.getStateAsString());
                    json.put("taskId", task.getId());
                    String assignees = task.getAllAssigneesAsString();
                    if (assignees != null)
                    {
                        assignees = assignees.replaceAll("<BR>", ",");
                    }
                    json.put("assignees", assignees);// this is for available tasks
                    json.put("acceptor", task.getAcceptor());

                    jsonArr.put(json);
                }
                writer.write(jsonArr.toString());
            }
        }
        catch (Exception e)
        {
            msg = e.getMessage();
            logger.error("Error when get avtivity list for status " + status, e);
        }

        writeGSErrorMsg(msg, "listActivity");
    }
}
