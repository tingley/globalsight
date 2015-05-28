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
package com.globalsight.everest.webapp.pagehandler.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.adapter.passolo.PassoloUtil;
import com.globalsight.everest.comment.CommentFilesDownLoad;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostCalculator;
import com.globalsight.everest.costing.CostingEngineLocal;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.edit.offline.download.JobPackageZipper;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PagePersistenceAccessor;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportHelper;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.comment.CommentMainHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.PageComparator;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.ScorecardScore;
import com.globalsight.everest.workflow.ScorecardScoreHelper;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.zip.ZipIt;

@SuppressWarnings("deprecation")
public class TaskDetailHandler extends PageHandler
{
    private static final Logger CATEGORY = Logger
            .getLogger(TaskDetailHandler.class);

    public static final String TASK_HOURS = "taskHours";
    public static final String TASK_HOURS_STATE = "taskHours";
    public static final String TASK_PAGES = "taskPages";
    public static final String TASK_PAGES_STATE = "taskPages";
    public static final String TASK_PAGE_IDS = "taskPageIds";
    private static final String BASE_BEAN = "taskDetails";

    protected static boolean s_isCostingEnabled = false;
    protected static boolean s_isRevenueEnabled = false;
    protected static boolean s_isParagraphEditorEnabled = false;

    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_isCostingEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
            s_isRevenueEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED);

            s_isParagraphEditorEnabled = EditHelper
                    .isParagraphEditorInstalled();
        }
        catch (Throwable e)
        {
            CATEGORY.error(
                    "TaskDetailHandler: Problem getting costing parameter from database ",
                    e);
        }
    }

    public TaskDetailHandler()
    {
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_thePageDescriptor
     *            the page desciptor
     * @param p_theRequest
     *            the original request sent from the browser
     * @param p_theResponse
     *            the original response object
     * @param p_context
     *            context the Servlet context
     * @throws NamingException
     */
    @SuppressWarnings("unchecked")
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession httpSession = p_request.getSession();
        // Get user id of the person who has logged in.
        User user = TaskHelper.getUser(httpSession);

        PermissionSet perms = new PermissionSet();
        try
        {
            perms = Permission.getPermissionManager().getPermissionSetForUser(
                    httpSession.getAttribute(WebAppConstants.USER_NAME)
                            .toString());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        httpSession.removeAttribute(WebAppConstants.PERMISSIONS);
        httpSession.setAttribute(WebAppConstants.PERMISSIONS, perms);

        SessionManager sessionMgr = (SessionManager) httpSession
                .getAttribute(SESSION_MANAGER);

        // Set the task complete delay time for this company
        sessionMgr.setAttribute(
                SystemConfigParamNames.TASK_COMPLETE_DELAY_TIME,
                SystemConfiguration.getInstance().getStringParameter(
                        SystemConfigParamNames.TASK_COMPLETE_DELAY_TIME));
        sessionMgr.setAttribute(
                SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME,
                SystemConfiguration.getInstance().getStringParameter(
                        SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME));
        
        String action = p_request.getParameter(TASK_ACTION);

        if (TASK_ACTION_SAVEDETAILS.equals(action))
        {
            saveTaskDetails(p_request, httpSession, user.getUserId());
        }
        else if (TASK_ACTION_ACCEPT.equals(action))
        {
            acceptTask(p_request, httpSession, user.getUserId());

            // set detail page id in session
            TaskHelper.storeObject(httpSession, TASK_DETAILPAGE_ID,
                    TaskHelper.DETAIL_PAGE_2);
        }
        else if (DTP_DOWNLOAD.equals(action))
        {
            dtpDownload(p_request, p_response);
            return;
        }
        else if (DTP_UPLOAD.equals(action))
        {
            dtpUpload(p_request);
        }
        else if (TASK_ACTION_CREATE_STF.equals(action))
        {
            startStfCreationForWorkflow(p_request,httpSession, user.getUserId());
        }
        else if (TASK_ACTION_RETRIEVE.equals(action))
        {
        	sessionMgr.removeElement("sourcePageIdList");
        	if(!getTask(p_request,httpSession,p_response,p_context,perms,user.getUserId()))
	        	return;
        }
        // default case action==null but must also handle pagesearch action
        else if (action == null)
        {
        	Task task = null;
    		String taskIdParam = p_request.getParameter(TASK_ID);
    		String taskStateParam = p_request.getParameter(TASK_STATE);
    		if(taskIdParam != null && taskStateParam != null)
    		{
    			long taskId = TaskHelper.getLong(taskIdParam); 
    			int taskState = TaskHelper.getInt(taskStateParam, -10);
    			//get task
    			task = TaskHelper.getTask(user.getUserId(), taskId,taskState);
    			TaskHelper.storeObject(httpSession, TASK, task);
    		}

            Locale uiLocale = (Locale) httpSession.getAttribute(UILOCALE);
            // Save the target pages to session - sorted
            List targetPages = task.getTargetPages();

            // store the search text that the pages are filtered by
            p_request
                    .setAttribute(
                            JobManagementHandler.PAGE_SEARCH_PARAM,
                            p_request
                                    .getParameter(JobManagementHandler.PAGE_SEARCH_PARAM));
            // sorts the pages in the correct order and store the column and
            // sort order
            // also filters them according to the search params
            setPages(p_request, httpSession, targetPages, uiLocale);
        }
        else if (TASK_ACTION_TRANSLATED_TEXT_RETRIEVE.equals(action))
        {
            // for counting translated text issue
            String pageIds = p_request.getParameter(TASK_PAGE_IDS);

            if (pageIds == null || pageIds.length() == 0 || "undefined".equalsIgnoreCase(pageIds))
            {
                return;
            }
            String[] pageIdsArray = pageIds.split(",");
            getPercent(p_response, pageIdsArray);

            return;
        }
        else if (TASK_ACTION_APPROVE_TUV.equals(action))
        {
            String pageIds = p_request.getParameter(TASK_PAGE_IDS);
            if (StringUtils.isBlank(pageIds))
                return;

            // for counting translated text issue
            PrintWriter out = p_response.getWriter();
            p_response.setContentType("text/html");
            // Approve TUVs
            String[] trgPageIds = pageIds.split(",");
            for (String trgPageId : trgPageIds)
            {
                SegmentTuvUtil.approveTuvByTargetPageId(Long
                        .parseLong(trgPageId));
            }
            out.write("1");
            out.close();
            return;
        }
        else if (TASK_ACTION_DOWNLOAD_SOURCEPAGES.equals(action))
        {
            // Get taskId parameter
            String taskIdParam = p_request.getParameter(TASK_ID);
            long taskId = TaskHelper.getLong(taskIdParam);
            Task task = null;
            // get task state (determines from which tab, the task details is
            // requested)
            String taskStateParam = p_request.getParameter(TASK_STATE);
            int taskState = TaskHelper.getInt(taskStateParam, -10);// -10 as
                                                                   // default
            try
            {
                // Get task
                task = TaskHelper.getTask(user.getUserId(), taskId, taskState);
            }
            catch (Exception e)
            {
            }
            downloadSourcePages(p_request, p_response, task);
            return;
        }
        else if(TASK_ACTION_SAVECOMMENT.equals(action))
        {
        	// Get taskId parameter
            String taskIdParam = p_request.getParameter(TASK_ID);
            long taskId = TaskHelper.getLong(taskIdParam);
            String taskStateParam = p_request.getParameter(TASK_STATE);
            int taskState = TaskHelper.getInt(taskStateParam, -10);
            
            Task task = TaskHelper.getTask(user.getUserId(), taskId, taskState);
            TaskHelper.storeObject(httpSession, WORK_OBJECT, task);
        }
        else if(TASK_ACTION_SCORECARD.equals(action))
        {
        	// Get taskId parameter
            String taskIdParam = p_request.getParameter(TASK_ID);
            long taskId = TaskHelper.getLong(taskIdParam);
            String taskStateParam = p_request.getParameter(TASK_STATE);
            int taskState = TaskHelper.getInt(taskStateParam, -10); 
            Task task = TaskHelper.getTask(user.getUserId(), taskId, taskState);
            
        	HashMap<String, Integer> scorecardMap = new HashMap<String, Integer>();
        	long companyId = task.getCompanyId();
        	ResourceBundle bundle = PageHandler.getBundle(httpSession);
        	boolean isScored = isScored(scorecardMap,
        			companyId, task.getWorkflow().getId(), bundle);
        	List<Select> scorecardCategories = ScorecardScoreHelper.initSelectList(
        			companyId, bundle);
        	String scorecardComment = ((WorkflowImpl)task.getWorkflow()).getScorecardComment();
        	sessionMgr.setAttribute("scorecardCategories", scorecardCategories);
        	sessionMgr.setAttribute("scorecard", scorecardMap);
        	sessionMgr.setAttribute("isScored", isScored);
        	if(StringUtil.isEmpty(scorecardComment))
        		scorecardComment = "";
        	sessionMgr.setAttribute("scorecardComment", scorecardComment);
        	TaskHelper.storeObject(httpSession, WORK_OBJECT, task);
        	Locale uiLocale = (Locale) httpSession.getAttribute(UILOCALE);
            List targetPages = task.getTargetPages();
        	setPages(p_request, httpSession, targetPages, uiLocale);
        	getTask(p_request,httpSession,p_response,p_context,perms,user.getUserId());
        }
        else if(TASK_ACTION_SAVE_SCORECARD.equals(action))
        {
        	// Get taskId parameter
            String taskIdParam = p_request.getParameter(TASK_ID);
            long taskId = TaskHelper.getLong(taskIdParam);
            String taskStateParam = p_request.getParameter(TASK_STATE);
            int taskState = TaskHelper.getInt(taskStateParam, -10); 
            Task task = TaskHelper.getTask(user.getUserId(), taskId, taskState);
    
        	HashMap<String, Integer> scorecardMap = new HashMap<String, Integer>();
        	long companyId = task.getCompanyId();
        	ResourceBundle bundle = PageHandler.getBundle(httpSession);
        	
        	//save
        	List<Select> scorecardCategories = ScorecardScoreHelper.initSelectList(
        			companyId, bundle);
        	long workflowId = task.getWorkflow().getId();
        	long jobId = task.getJobId();
        	String userId = (String) httpSession.getAttribute(WebAppConstants.USER_NAME);
        	String scorecardComment = p_request.getParameter("scoreComment");
        	
        	Session session = HibernateUtil.getSession();
            Transaction tx = session.beginTransaction();
            try 
            {
            	for(Select select: scorecardCategories)
            	{
            		ScorecardScore score= new ScorecardScore();
            		score.setScorecardCategory(select.getValue());
            		score.setScore(new Integer(p_request.getParameter(select.getValue())));
            		score.setWorkflowId(workflowId);
            		score.setJobId(jobId);
            		score.setCompanyId(companyId);
            		score.setModifyUserId(userId);
            		score.setIsActive(true);
            		HibernateUtil.save(score);
            	}
            	
            	WorkflowImpl workflowImpl = (WorkflowImpl) task.getWorkflow();
            	workflowImpl.setScorecardComment(scorecardComment);
            	HibernateUtil.save(workflowImpl);	
            	tx.commit();
			} catch (Exception e) {
				tx.rollback();
				e.printStackTrace();
			}
			
        	boolean isScored = isScored(scorecardMap,
        			companyId, task.getWorkflow().getId(), bundle);
        	sessionMgr.setAttribute("scorecardCategories", scorecardCategories);
        	sessionMgr.setAttribute("scorecard", scorecardMap);
        	sessionMgr.setAttribute("isScored", isScored);
        	sessionMgr.setAttribute("scorecardComment", scorecardComment);
        }
        
        //saveComment

        // Set the EXPORT_INIT_PARAM in the sessionMgr so we can bring
        // the user back here after they Export
        sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM,
                BASE_BEAN);
        Task task = (Task) TaskHelper.retrieveObject(httpSession, WORK_OBJECT);
        if (task != null)
        {
            sessionMgr.setAttribute(JobManagementHandler.JOB_ID,
                    (new Long(task.getJobId())).toString());
            sessionMgr.setAttribute(WebAppConstants.TASK_ID,
                    (new Long(task.getId())).toString());
        }

        p_request.setAttribute(WebAppConstants.PARAGRAPH_EDITOR,
                s_isParagraphEditorEnabled ? "true" : "false");

        // Keeps page cache for JavaScript Function.
        isCache = true;
        CommentMainHandler commentMainHandler = new CommentMainHandler();
        commentMainHandler.handleRequest(p_pageDescriptor, p_request,
                p_response, p_context);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void getPercent(HttpServletResponse p_response,
            String[] pageIdsArray) throws IOException
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pageIdsArray.length; i++)
        {
            int percent = SegmentTuvUtil
                    .getTranslatedPercentageForTargetPage(Long
                            .parseLong(pageIdsArray[i]));
            sb.append(percent).append(",");
        }

        if (sb.length() != 0)
        {
            PrintWriter out = p_response.getWriter();
            p_response.setContentType("text/html");
            out.write(sb.toString());
            out.close();
        }
    }

    /**
     * Returns the JSP Page to use as the Error Page if an error happens when
     * using this PageHandler
     * 
     * @return
     */
    public String getErrorPage()
    {
        return WebAppConstants.ACTIVITY_ERROR_PAGE;
    }

    private List setPages(HttpServletRequest p_request, HttpSession p_session,
            List p_targetPages, Locale p_uiLocale)
    {
        // sorts the pages in the correct order and store the column and sort
        // order
        // also filters them according to the search params
        p_targetPages = filterPagesByName(p_request, p_session, p_targetPages);
        sortPages(p_request, p_session, p_uiLocale, p_targetPages);
        TaskHelper.storeObject(p_session, WebAppConstants.TARGET_PAGES,
                p_targetPages);
        return p_targetPages;
    }
/**
 * Get more information on task
 * @param p_request
 * @param p_session
 * @param p_response
 * @param p_context
 * @param PermissionSet
 * @param p_userId
 * **/
	private boolean getTask(HttpServletRequest p_request, HttpSession p_session,
			HttpServletResponse p_response, ServletContext p_context,
			PermissionSet perms, String p_userId) throws ServletException,
			IOException, EnvoyServletException 
	{
		// Get taskId parameter
		String taskIdParam = p_request.getParameter(TASK_ID);
		long taskId = TaskHelper.getLong(taskIdParam);
		Task task = null;
		// get task state (determines from which tab, the task details is
		// requested)
		String taskStateParam = p_request.getParameter(TASK_STATE);
		int taskState = TaskHelper.getInt(taskStateParam, -10);// -10 as
																// default
		try {
			// Get task
			task = TaskHelper.getTask(p_userId, taskId, taskState);
		} catch (Exception e) {
			CATEGORY.info(e);
			ResourceBundle bundle = getBundle(p_session);
			String stateLabel = "";
			switch (taskState) {
			case Task.STATE_ACCEPTED:
				stateLabel = bundle.getString("lb_accepted");
				break;
			case Task.STATE_COMPLETED:
				stateLabel = bundle.getString("lb_finished");
				break;
			case Task.STATE_REJECTED:
				stateLabel = bundle.getString("lb_rejected");
				break;
			case Task.STATE_ACTIVE:
				stateLabel = bundle.getString("lb_available");
				break;
			}
			Object[] args = { p_request.getParameter("jobname"), stateLabel };
			p_request.setAttribute("badresults", MessageFormat.format(
					bundle.getString("msg_bad_task"), args));
			// remove the task from the most recently used list
			String menuName = p_request.getParameter("cookie");
			TaskHelper.removeMRUtask(p_request, p_session, menuName, p_response);

			// forward to the jsp page.
			SessionManager sessionMgr = (SessionManager) p_session
            		.getAttribute(PageHandler.SESSION_MANAGER);
			sessionMgr.setMyactivitiesAttribute("badresults", MessageFormat.format(
					bundle.getString("msg_bad_task"), args));
			TaskListParams listParams = (TaskListParams) 
        		sessionMgr.getMyactivitiesAttribute(TaskListConstants.TASK_LIST_PARAMS);
			int targetState = listParams.getTaskState();
	        if(targetState == 3)
	        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=myactivities&state=3&listType=stateOnly&init=0");
	        else if(targetState == 8)
	        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=myactivities&state=8&listType=stateOnly&init=0");
	        else if(targetState == -1)
	        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=myactivities&state=-1&listType=stateOnly&init=0");
	        else if(targetState == 6)
	        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=myactivities&state=6&listType=stateOnly&init=0");
	        else
        		p_response.sendRedirect("/globalsight/ControlServlet?activityName=myactivities&state=-10&listType=stateOnly&init=0");
			return false;
		}

		// store task state in the SessionManager
		TaskHelper.storeObject(p_session, TASK_STATE, taskStateParam);

		// Save the task to session
		TaskHelper.storeObject(p_session, WORK_OBJECT, task);
		
		Locale uiLocale = (Locale) p_session.getAttribute(UILOCALE);
		List targetPages = getTargetPages(p_session,task);
		targetPages = setPages(p_request, p_session, targetPages, uiLocale);

		// Set detail page id in session
		TaskHelper.storeObject(p_session, TASK_DETAILPAGE_ID,
				TaskHelper.DETAIL_PAGE_1);

		boolean isProjectMgr = perms
				.getPermissionFor(Permission.PROJECTS_MANAGE);
		if (isProjectMgr) {
			TaskHelper.storeObject(p_session, IS_ASSIGNEE, new Boolean(task
					.getAllAssignees().contains(p_userId)));
		}
		
		// GBS-3189: Create PDF
		boolean isPreviewPDF = PreviewPDFHelper.isEnablePreviewPDF(task);
		p_request.setAttribute("isPreviewPDF", isPreviewPDF);

		// Determine whether we should display the Hourly rate field
		boolean isHourlyRate = ((task.getState() == Task.STATE_ACCEPTED) && isHourlyRate(
				task, null)) ? true : false;
		TaskHelper.storeObject(p_session, TASK_HOURS_STATE, new Boolean(
				isHourlyRate));
		// Determine whether we should display the Page Count field
		boolean isPageBasedRate = ((task.getState() == Task.STATE_ACCEPTED) && isPageBasedRate(
				task, null)) ? true : false;
		TaskHelper.storeObject(p_session, TASK_PAGES_STATE, new Boolean(
				isPageBasedRate));

		TaskHelper.updateMRUtask(p_request, p_session, task, p_response,
				task.getState());
		
		// Get the number of open and closed issues.
		// get just the number of issues in OPEN state
		// query is also considered a subset of the OPEN state
		int openSegmentCount = 0, closedSegmentCount = 0;
		List<String> oStates = new ArrayList<String>();
		oStates.add(Issue.STATUS_OPEN);
		oStates.add(Issue.STATUS_QUERY);
		oStates.add(Issue.STATUS_REJECTED);
		openSegmentCount = getIssueCount(task, p_session, oStates);
		// Closed Comments
		List<String> cStates = new ArrayList<String>();
		cStates.add(Issue.STATUS_CLOSED);
		closedSegmentCount = getIssueCount(task, p_session, cStates);
		p_session.setAttribute(
				JobManagementHandler.OPEN_AND_QUERY_SEGMENT_COMMENTS,
				new Integer(openSegmentCount).toString());
		p_session.setAttribute(JobManagementHandler.CLOSED_SEGMENT_COMMENTS,
				new Integer(closedSegmentCount).toString());
		return true;

	}
	/**
	 * Get TargetPages
	 * @param p_session
	 * @param task
	 * */
	private List getTargetPages	(HttpSession p_session, Task task) {
		
		// Save the target pages to session - sorted
		List targetPages = null;
		if (task.getTaskType().equals(Task.TYPE_TRANSLATION)) {
			targetPages = task.getTargetPages();
		} else {
			try {
				Job job = ServerProxy.getJobHandler().getJobById(
						task.getJobId());
				List workflows = new ArrayList();
				workflows.addAll(job.getWorkflows());
				for (int i = 0; i < workflows.size(); i++) {
					Workflow workflow = (Workflow) workflows.get(i);
					if (workflow.getTargetLocale().equals(
							task.getTargetLocale())) {
						if (workflow.getWorkflowType().equals(
								WorkflowTypeConstants.TYPE_TRANSLATION)) {
							targetPages = new ArrayList();
							targetPages.addAll(workflow.getTargetPages());
							break;
						}
					}
				}
				if (targetPages == null) {
					targetPages = new ArrayList();
				}
			} catch (Exception e) {
				throw new EnvoyServletException(e);
			}
		}
		return targetPages;
	}
    /**
     * Accepts the task.
     * 
     * @param p_request
     * @param p_session
     * @param p_userId
     * @exception ServletException
     * @exception IOException
     * @exception EnvoyServletException
     * @throws NamingException
     */
    private void acceptTask(HttpServletRequest p_request,
            HttpSession p_session, String p_userId)
            throws EnvoyServletException
    {
        // Get task from session
        //Task task = (Task) TaskHelper.retrieveObject(p_session, WORK_OBJECT);
        String taskIdParam = p_request.getParameter(TASK_ID);
        long taskId = TaskHelper.getLong(taskIdParam);
        Task task = null;
        try
        {
            task = (Task) HibernateUtil.get(TaskImpl.class, taskId);
            if (task != null && task.getState() == Task.STATE_FINISHING)
            {
                TaskHelper.storeObject(p_session, WORK_OBJECT, task);
                return;
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
        try
        {
            // Accept the task
            TaskHelper.acceptTask(p_userId, task);

            if (task != null)
            {
                // update task in session (need to get task in accepted
                // state now).
                TaskHelper.updateTaskInSession(p_session, p_userId,
                        task.getId(), Task.STATE_ACCEPTED);

                // store hourly rate state in the SessionManager
                TaskHelper.storeObject(p_session, TASK_HOURS_STATE,
                        new Boolean(isHourlyRate(task, p_userId)));

                // store Page Count state in the SessionManager
                TaskHelper.storeObject(p_session, TASK_PAGES_STATE,
                        new Boolean(isPageBasedRate(task, p_userId)));
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
    }

    /**
     * For dtpDownload and dtpUpload.
     */
    private String getTargetFileName(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        String filePath = null;

        try
        {
            long targetPageId = new Long(
                    p_request.getParameter(WebAppConstants.TARGET_PAGE_ID))
                    .longValue();
            TargetPage targetPage = PagePersistenceAccessor
                    .getTargetPageById(targetPageId);

            // Get sub file path
            filePath = this.makeDirectoryNameOperatingSystemSafe(targetPage
                    .getSourcePage().getExternalPageId());
            String[] subStrings = null;
            if (System.getProperty("os.name").startsWith("Windows"))
            {
                subStrings = filePath.split(File.separator + File.separator);
            }
            else
            {
                subStrings = filePath.split(File.separator);
            }

            if (subStrings.length > 0)
            {
                int length = subStrings[0].length() + 1;
                filePath = filePath.substring(length);
            }
            else
            {
                filePath = "";
            }

            String targetLocale = targetPage.getGlobalSightLocale().toString();
            filePath = ExportHelper.transformExportedFilename(filePath,
                    targetLocale);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }

        return filePath;
    }

    /**
     * For dtpDownload and dtpUpload.
     */
    private String getTargetFilePath(HttpServletRequest p_request)
    {
        StringBuffer filePath = new StringBuffer();

        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            long targetPageId = new Long(
                    p_request.getParameter(WebAppConstants.TARGET_PAGE_ID))
                    .longValue();
            TargetPage targetPage = PagePersistenceAccessor
                    .getTargetPageById(targetPageId);

            filePath.append(
                    config.getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR))
                    .append(targetPage.getExportSubDir())
                    .append(File.separatorChar);
        }
        catch (GeneralException e)
        {
            throw new EnvoyServletException(e);
        }

        return filePath.toString();
    }

    private String getFullTargetFilePath(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        return this.getTargetFilePath(p_request)
                + this.getTargetFileName(p_request);
    }

    private String makeDirectoryNameOperatingSystemSafe(String p_dirName)
    {
        char UNIX_SEPARATOR = '/';
        char WIN_SEPARATOR = '\\';

        // first check if it's a UNC pathname and we're on Windows. If so leave
        // it alone.
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows") && p_dirName.startsWith("\\\\"))
        {
            return p_dirName;
        }

        String newName = p_dirName.replace(UNIX_SEPARATOR, File.separatorChar);
        newName = newName.replace(WIN_SEPARATOR, File.separatorChar);
        if (newName.endsWith(File.separator))
        {
            newName = newName.substring(0, newName.length() - 1);
        }

        return newName;
    }

    private void downloadSourcePages(HttpServletRequest p_request,
            HttpServletResponse p_response, Task p_task) throws IOException
    {
        HttpSession session = p_request.getSession(false);
        List sourcePages = (List) p_task.getSourcePages();

        Iterator it = sourcePages.iterator();
        String m_cxeDocsDir = SystemConfiguration.getInstance()
                .getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR,
                		String.valueOf(p_task.getCompanyId()));
        ArrayList<String> fileNames = new ArrayList<String>();
        ArrayList<String> filePaths = new ArrayList<String>();
        Map<String, String> mapOfNamePath = new HashMap<String, String>();
        while (it.hasNext())
        {
            SourcePage sourcePage = (SourcePage) it.next();

            if (sourcePage.hasRemoved())
            {
                continue;
            }

            StringBuffer sourceSb = new StringBuffer().append(m_cxeDocsDir)
                    .append("/");
            String externalPageId = sourcePage.getExternalPageId();
            externalPageId = externalPageId.replace("\\", "/");

            if (PassoloUtil.isPassoloFile(sourcePage))
            {
                externalPageId = externalPageId.substring(0,
                        externalPageId.lastIndexOf(".lpu/") + 4);
            }

            externalPageId = SourcePage.filtSpecialFile(externalPageId);

            if (filePaths.contains(externalPageId))
                continue;

            sourceSb = sourceSb.append(externalPageId);
            filePaths.add(externalPageId);
            fileNames.add(sourceSb.toString());
            mapOfNamePath.put(sourceSb.toString(), externalPageId);
        }
        Map<String, String> entryNamesMap = new HashMap<String, String>();
        String jobName = p_task.getJobName();
        String zipFileName = URLEncoder.encode(jobName + ".zip");
        File tmpFile = File.createTempFile("GSDownloadSource", ".zip");
        try
        {
            JobPackageZipper m_zipper = new JobPackageZipper();
            m_zipper.createZipFile(tmpFile);
            entryNamesMap = ZipIt.getEntryNamesMap(filePaths);
            for (int i = 0; i < fileNames.size(); i++)
            {
                filePaths.set(i,
                        entryNamesMap.get(mapOfNamePath.get(fileNames.get(i))));
            }
            addSourcePages(m_zipper, fileNames, filePaths, zipFileName);
            m_zipper.closeZipFile();
            CommentFilesDownLoad commentFilesDownload = new CommentFilesDownLoad();
            commentFilesDownload.sendFileToClient(p_request, p_response,
            		jobName + ".zip", tmpFile);
        }
        finally
        {
            FileUtil.deleteTempFile(tmpFile);
        }
    }

    private void addSourcePages(JobPackageZipper m_zipper,
            List<String> fileNames, List<String> filePaths, String zipFileName)
    {
        for (int i = 0; i < fileNames.size(); i++)
        {
            File file = new File(fileNames.get(i));

            if (!file.exists())
            {
                XmlEntities entity = new XmlEntities();
                File dir = file.getParentFile();
                if (dir.isDirectory())
                {
                    File[] files = dir.listFiles();
                    for (File f : files)
                    {
                        if (file.getName().equals(
                                entity.decodeStringBasic(f.getName())))
                        {
                            file = f;
                            break;
                        }
                    }
                }
            }

            if (file.exists())
            {
                FileInputStream input = null;
                try
                {
                    input = new FileInputStream(file);
                    m_zipper.writePath(filePaths.get(i));
                    m_zipper.writeFile(input);
                    input.close();
                }
                catch (IOException ex)
                {
                    CATEGORY.warn("cannot write file: " + file
                            + " to zip stream.", ex);
                }
                finally
                {
                    try
                    {
                        if (input != null)
                        {
                            input.close();
                        }
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
        }
    }

    private void dtpDownload(HttpServletRequest p_request,
            HttpServletResponse p_response) throws IOException,
            EnvoyServletException
    {

        byte[] buf = new byte[4096];

        // Craete the zip file
        File downloadFile = new File(this.getFullTargetFilePath(p_request));
        FileInputStream inDownloadFile = new FileInputStream(downloadFile);

        File zipFile = new File(this.getTargetFilePath(p_request)
                + "dtpDownloadFile.zip");
        ZipOutputStream outZipFile = new ZipOutputStream(new FileOutputStream(
                zipFile));

        outZipFile
                .putNextEntry(new ZipEntry(this.getTargetFileName(p_request)));
        for (int len = 0; (len = inDownloadFile.read(buf)) > 0;)
        {
            outZipFile.write(buf, 0, len);
        }
        outZipFile.closeEntry();
        inDownloadFile.close();
        outZipFile.close();

        p_response.setContentType("application/zip");
        p_response.setHeader("Content-Disposition", "attachment; filename="
                + zipFile.getName() + ";");
        if (p_request.isSecure())
        {
            setHeaderForHTTPSDownload(p_response);
        }
        p_response.setContentLength((int) zipFile.length());

        // Send the zip file to the client
        byte[] inBuff = new byte[4096];
        FileInputStream fis = new FileInputStream(zipFile);
        int bytesRead = 0;
        while ((bytesRead = fis.read(inBuff)) != -1)
        {
            p_response.getOutputStream().write(inBuff, 0, bytesRead);
        }

        if (bytesRead > 0)
        {
            p_response.getOutputStream().write(inBuff, 0, bytesRead);
        }
        fis.close();

        zipFile.delete();
    }

    private void dtpUpload(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        String uploadFileName = this.getFullTargetFilePath(p_request);

        try
        {
            DefaultFileItemFactory factory = new DefaultFileItemFactory();

            // Create a new file upload handler
            DiskFileUpload upload = new DiskFileUpload(factory);

            List items = upload.parseRequest(p_request);
            Iterator iter = items.iterator();
            if (iter.hasNext())
            {
                FileItem item = (FileItem) iter.next();
                File uploadDir = new File(this.getTargetFilePath(p_request));
                if (!uploadDir.isDirectory())
                {
                    uploadDir.mkdir();
                }
                File uploadFile = new File(uploadFileName);
                item.write(uploadFile);
            }
        }
        catch (FileUploadException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Save all the user editable task detail fields
     * 
     * @param p_request
     * @param p_session
     * @param p_userId
     * @exception EnvoyServletException
     */
    private void saveTaskDetails(HttpServletRequest p_request,
            HttpSession p_session, String p_userId)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        //Task task = (Task) TaskHelper.retrieveObject(p_session, WORK_OBJECT);
        String taskIdParam = p_request.getParameter(TASK_ID);
        long taskId = TaskHelper.getLong(taskIdParam);
        
        int state = 0;
        try
        {
            state = Integer.parseInt(p_request.getParameter(TASK_STATE));
        }
        catch (Exception e)
        {
            state = Task.STATE_ACCEPTED;
        }
        Task task = TaskHelper.getTask(p_userId, taskId,state);
        
        if (task != null)
        {
            // Save the hourly amount-of-work...
            // but first, check if the hourly rate field has been enabled
            boolean isHourlyJobCosting = ((Boolean) TaskHelper.retrieveObject(
                    p_session, TASK_HOURS_STATE)).booleanValue();
            if (isHourlyJobCosting)
            {
                TaskHelper.setActualAmountOfWork(task, Rate.UnitOfWork.HOURLY,
                        p_request.getParameter(TASK_HOURS));

                // update task in session
                TaskHelper.updateTaskInSession(p_session, p_userId,
                        task.getId(), state);
            }
            boolean isPageBasedJobCosting = ((Boolean) TaskHelper
                    .retrieveObject(p_session, TASK_PAGES_STATE))
                    .booleanValue();
            if (isPageBasedJobCosting)
            {
                TaskHelper.setActualAmountOfWork(task,
                        Rate.UnitOfWork.PAGE_COUNT,
                        p_request.getParameter(TASK_PAGES));

                // update task in session
                TaskHelper.updateTaskInSession(p_session, p_userId,
                        task.getId(), state);
            }
            if (s_isCostingEnabled)
            {
                try
                {
                    Job job = task.getWorkflow().getJob();
                    String curr = (String) sessionMgr
                            .getAttribute(JobManagementHandler.CURRENCY);
                    if (curr == null)
                    {
                        // Get the pivot currency;
                        Currency c = ServerProxy.getCostingEngine()
                                .getPivotCurrency();
                        curr = c.getIsoCode();
                    }
                    Currency oCurrency = ServerProxy.getCostingEngine()
                            .getCurrency(curr);
                    // Calculate Expenses
                    CostCalculator calculator = new CostCalculator(job.getId(),
                            oCurrency, true, Cost.EXPENSE);
                    calculator.sendToCalculateCost();
                    if (s_isRevenueEnabled)
                    {
                        // Calculate Revenue
                        calculator = new CostCalculator(job.getId(), oCurrency,
                                true, Cost.REVENUE);
                        calculator.sendToCalculateCost();
                    }
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(
                            GeneralExceptionConstants.EX_GENERAL, e);
                }
            }
        }
    }

    /*
     * Start the process for creation of secondary target file using this task
     * id. Note that the STFs are created for a given workflow only.
     */
    private void startStfCreationForWorkflow(HttpServletRequest p_request,
    		HttpSession p_session,String p_userId) throws EnvoyServletException
    {
    	String taskIdParam = p_request.getParameter(TASK_ID);
    	long taskId = TaskHelper.getLong(taskIdParam);
    	
    	String taskStateParam = p_request.getParameter(TASK_STATE);
    	int taskState = TaskHelper.getInt(taskStateParam, -10);
    	
    	Task task = TaskHelper.getTask(p_userId, taskId,taskState);
//        long taskId = -1;
        try
        {
//            Task task = (Task) TaskHelper.retrieveObject(p_session, WORK_OBJECT);
//            taskId = task.getId();
            ServerProxy.getTaskManager().updateStfCreationState(taskId,
                    Task.IN_PROGRESS);
            ServerProxy.getWorkflowManager().startStfCreationForWorkflow(
                    taskId, task.getWorkflow(), p_userId);

            // now update task in session
            TaskHelper.updateTaskInSession(p_session, p_userId, taskId);
        }
        catch (Exception e)
        {
            try
            {
                ServerProxy.getTaskManager().updateStfCreationState(taskId,
                        Task.FAILED);
            }
            catch (Exception ex)
            {
                CATEGORY.error(
                        "Failed to update stf creation state of task to FAILED ",
                        ex);
            }
            throw new EnvoyServletException(e);
        }
    }

    private boolean isHourlyRate(Task p_task, String p_user)
    {
        // If costing has been disabled,
        // we do not want to show the hour based rate field in the ui.
        boolean isHourly = false;
        if (s_isCostingEnabled)
        {
            Rate actualRate = getActualRateToBeUsed(p_task, p_user);
            if ((actualRate != null && actualRate.getRateType().equals(
                    Rate.UnitOfWork.HOURLY))
                    || ((p_task.getRevenueRate() != null) && p_task
                            .getRevenueRate().getRateType()
                            .equals(Rate.UnitOfWork.HOURLY)))
            {
                isHourly = true;
            }
        }
        return isHourly;
    }

    private boolean isPageBasedRate(Task p_task, String p_user)
    {
        // If costing has been disabled,
        // we do not want to show the Page based rate field in the ui.
        boolean isPage = false;
        if (s_isCostingEnabled)
        {
            Rate actualRate = getActualRateToBeUsed(p_task, p_user);
            if ((actualRate != null && actualRate.getRateType().equals(
                    Rate.UnitOfWork.PAGE_COUNT))
                    || ((p_task.getRevenueRate() != null) && p_task
                            .getRevenueRate().getRateType()
                            .equals(Rate.UnitOfWork.PAGE_COUNT)))
            {
                isPage = true;
            }
        }
        return isPage;
    }

    /**
     * This is copied from CostingEngineLocal The logic is not exactly same but
     * quite similar.
     */
    private Rate getActualRateToBeUsed(Task t, String p_acceptor)
    {
        Rate useRate = t.getExpenseRate();
        int selectionCriteria = t.getRateSelectionCriteria();
        User user = null;

        if (selectionCriteria == WorkflowConstants.USE_SELECTED_RATE_UNTIL_ACCEPTANCE)
        {
            // find out who accepted the task
            try
            {
                String acceptor = t.getAcceptor();
                if (acceptor != null)
                {
                    user = ServerProxy.getUserManager().getUser(acceptor);
                }
                else
                {
                    if (p_acceptor != null)
                    {
                        user = ServerProxy.getUserManager().getUser(p_acceptor);
                    }
                }
            }
            catch (Exception e)
            {
                CATEGORY.error(
                        "TaskDetailHandler::Problem getting user information ",
                        e);
            }
            try
            {
                // Now find out what is the default rate for this user.
                if (user != null)
                {
                    // find out user role
                    Vector uRoles = new Vector(ServerProxy.getUserManager()
                            .getUserRoles(user));
                    String activity = t.getTaskName();
                    GlobalSightLocale source = t.getSourceLocale();
                    GlobalSightLocale target = t.getTargetLocale();

                    for (int i = 0; i < uRoles.size(); i++)
                    {
                        Role curRole = (Role) uRoles.get(i);
                        // Get the source and target locale for each role.
                        String sourceLocale = curRole.getSourceLocale();
                        String targetLocale = curRole.getTargetLocale();
                        Activity act = curRole.getActivity();
                        UserRole cRole = (UserRole) uRoles.get(i);

                        if (act.getActivityName().equals(activity)
                                && sourceLocale.equals(source.toString())
                                && targetLocale.equals(target.toString()))
                        {
                            // Found the userRole we are talking about
                            if (cRole != null && cRole.getRate() != null)
                            {
                                Long rate = new Long(cRole.getRate());
                                useRate = getRate(rate.longValue());
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                CATEGORY.error(
                        "TaskDetailHandler::Problem getting user information ",
                        e);
            }
        }
        return useRate;
    }

    /*
     * Copied from CostingEngine
     */
    private Rate getRate(long p_id) throws RemoteException
    {
        CostingEngineLocal local = new CostingEngineLocal();
        return local.getRate(p_id);
    }

    private int getIssueCount(Task task, HttpSession session,
            List<String> states) throws EnvoyServletException
    {
        int count = 0;

        Workflow wf = task.getWorkflow();
        if (!(wf.getState().equals(Workflow.CANCELLED)))
        {
            List pages = wf.getTargetPages();
            List<Long> targetPageIds = new ArrayList<Long>();
            for (int j = 0; j < pages.size(); j++)
            {
                TargetPage tPage = (TargetPage) pages.get(j);
                String state = tPage.getPageState();
                if (!PageState.IMPORT_FAIL.equals(state))
                {
                    targetPageIds.add(tPage.getId());
                }
            }

            try
            {
                CommentManager manager = ServerProxy.getCommentManager();
                count = manager.getIssueCount(Issue.TYPE_SEGMENT,
                        targetPageIds, states);
            }
            catch (Exception ex)
            {
                throw new EnvoyServletException(ex);
            }
        }
        return count;
    }

    /**
     * Filter the pages by the specified search filter. Return only the pages
     * that match the filter.
     */
    protected List filterPagesByName(HttpServletRequest p_request,
            HttpSession p_session, List p_pages)
	{
    	SessionManager sessionMgr = (SessionManager) p_session
    			.getAttribute(WebAppConstants.SESSION_MANAGER);
		String thisFileSearch = (String) p_request
				.getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);
		if (thisFileSearch == null)
			thisFileSearch = (String) p_session
					.getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);

		String thisSearchText = (String) p_request
				.getParameter(JobManagementHandler.PAGE_SEARCH_TEXT);

		String thisSearchLocale = (String) p_request
				.getParameter(JobManagementHandler.PAGE_SEARCH_LOCALE);
		
		if (thisSearchText != null && !"".equals(thisSearchText))
		{
			try
			{
				thisSearchText = URLDecoder.decode(thisSearchText, "UTF-8");
			}
			catch (Exception e)
			{
				throw new EnvoyServletException(e);
			}
		}
		p_request.setAttribute(JobManagementHandler.PAGE_SEARCH_TEXT,
				thisSearchText);
		p_request.setAttribute(JobManagementHandler.PAGE_SEARCH_LOCALE,
				thisSearchLocale);
		if (thisSearchLocale != null && !"".equals(thisSearchText)
				&& thisSearchText != null)
		{
			Task task = (Task) TaskHelper.getStoredObject(p_session, TASK);
			List<Long> sourcePageIdList = new ArrayList<Long>();
			Job job = task.getWorkflow().getJob();
			String tuTableName = null;
			String tuvTableName = null;
			try
			{
				tuTableName = BigTableUtil.getTuTableJobDataInByJobId(job
						.getId());
				tuvTableName = BigTableUtil.getTuvTableJobDataInByJobId(job
						.getId());
			}
			catch (Exception e)
			{
				throw new EnvoyServletException(e);
			}
			List newPages = new ArrayList();
			
			long pageId = -1;
			long localeId = -1;
			long sourcePageId = -1;
			for (Iterator fi = p_pages.iterator(); fi.hasNext();)
			{
				TargetPage page = (TargetPage) fi.next();
				if ("targetLocale".equals(thisSearchLocale))
				{
					pageId = page.getId();
					localeId = page.getLocaleId();
					sourcePageId = page.getSourcePage().getId();
				}
				else if ("sourceLocale".equals(thisSearchLocale))
				{
					localeId = page.getSourcePage().getLocaleId();
					pageId = page.getSourcePage().getId();
					sourcePageId = pageId;
				}
				boolean check = TaskHelper.checkPageContainText(tuTableName,
						tuvTableName, thisSearchLocale, thisSearchText, pageId,
						localeId);

				if (check){
					newPages.add(page);
					sourcePageIdList.add(sourcePageId);
				}
			}
			sessionMgr.setAttribute("sourcePageIdList", sourcePageIdList);
			return newPages;
		}

		if (thisFileSearch != null)
		{
			ArrayList filteredFiles = new ArrayList();
			for (Iterator fi = p_pages.iterator(); fi.hasNext();)
			{
				Page p = (Page) fi.next();
				if (p.getExternalPageId().indexOf(thisFileSearch) >= 0)
				{
					filteredFiles.add(p);
				}
			}
			return filteredFiles;
		}
			// just return all - no filter
		return p_pages;
	}

    /**
     * Sorts the target pages for the task specified by the sort column and
     * direction.
     */
    @SuppressWarnings("unchecked")
    protected void sortPages(HttpServletRequest p_request,
            HttpSession p_session, Locale p_uiLocale, List p_pages)
    {
        // first get comparator from session
        PageComparator comparator = (PageComparator) p_session
                .getAttribute(JobManagementHandler.PAGE_COMPARATOR);
        if (comparator == null)
        {
            // Default: Sort by external page id (page name) ascending, so it'll
            // be alphabetized
            comparator = new PageComparator(PageComparator.EXTERNAL_PAGE_ID,
                    true, p_uiLocale);
            p_session.setAttribute(JobManagementHandler.PAGE_COMPARATOR,
                    comparator);
        }

        String criteria = p_request
                .getParameter(JobManagementHandler.PAGE_SORT_PARAM);
        if (criteria != null)
        {
            int sortCriteria = Integer.parseInt(criteria);
            if (comparator.getSortColumn() == sortCriteria)
            {
                // just reverse the sort order
                comparator.reverseSortingOrder();
            }
            else
            {
                // set the sort column
                comparator.setSortColumn(sortCriteria);
            }
        }

        SortUtil.sort(p_pages, comparator);
        p_session.setAttribute(JobManagementHandler.PAGE_SORT_COLUMN,
                new Integer(comparator.getSortColumn()));
        p_session.setAttribute(JobManagementHandler.PAGE_SORT_ASCENDING,
                new Boolean(comparator.getSortAscending()));
    }
    
    private boolean isScored(HashMap<String, Integer> scorecardMap, long companyId,
    		long workflowId, ResourceBundle bundle)
    {
    	List<Select> scorecardCategories = ScorecardScoreHelper.initSelectList(
    			companyId, bundle);
    	
    	List<ScorecardScore> scoreList = ScorecardScoreHelper.getScoreByWrkflowId(workflowId);
    	
    	boolean isScored = false;
    	for(Select category:scorecardCategories)
    	{
    		String categoryString = category.getValue();
    		boolean isCategoryScored = false;
    		for(ScorecardScore score:scoreList)
    		{
    			if(score.getScorecardCategory().equals(categoryString))
    			{
    				isCategoryScored = true;
    				isScored = true;
    				scorecardMap.put(categoryString, score.getScore());
    				break;
    			}
    		}
    		 
    		if(!isCategoryScored)
    		{
    			scorecardMap.put(categoryString, 0);
    		}
    	}
    	return isScored;
    }
    
    
    
}
