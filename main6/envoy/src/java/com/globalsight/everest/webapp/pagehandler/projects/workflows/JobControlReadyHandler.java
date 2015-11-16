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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tm.searchreplace.TuvInfo;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler;
import com.globalsight.everest.webapp.pagehandler.projects.jobvo.JobVoReadySearcher;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

public class JobControlReadyHandler extends JobManagementHandler
{
    private static final String BASE_BEAN = "ready";
    
    /**
     * Invokes this EntryPageHandler object
     * <p>
     * 
     * @param p_ageDescriptor
     *            the description of the page to be produced.
     * @param p_request
     *            original request sent from the browser.
     * @param p_response
     *            original response object.
     * @param p_context
     *            the Servlet context.
     */
    public void myInvokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
    	String m_exportUrl = null;
    	HttpSession session = p_request.getSession(false);
    	SessionManager sessionMgr = (SessionManager) session
    		.getAttribute(SESSION_MANAGER);
    	boolean stateMarch = false;
    	if(Job.READY_TO_BE_DISPATCHED.equals((String)sessionMgr.getMyjobsAttribute("lastState")))
			stateMarch = true;
    	String action = p_request.getParameter(ACTION_STRING);
		if (StringUtil.isNotEmpty(action)
				&& "removeJobFromGroup".equals(action))
		{
			removeJobFromGroup(p_request);
		}
    	setJobSearchFilters(sessionMgr, p_request, stateMarch);

        p_request.setAttribute("action", p_request.getParameter("action"));
        HashMap beanMap = invokeJobControlPage(p_thePageDescriptor, p_request,
                BASE_BEAN);
        p_request.setAttribute("searchType",
                p_request.getParameter("searchType"));

        if (m_exportUrl == null)
        {
            m_exportUrl = ((NavigationBean) beanMap.get(EXPORT_BEAN))
                    .getPageURL();
        }

        if(p_request.getParameter("checkIsUploadingForExport") != null)
        {
        	long jobId = Long.parseLong(p_request.getParameter("jobId"));
        	Job job = WorkflowHandlerHelper.getJobById(jobId);
        	String result = "";
        	for (Workflow workflow: job.getWorkflows())
        	{
        		if(result.length() > 0)
        			break;
        		Hashtable<Long, Task> tasks = workflow.getTasks();
        		for(Long taskKey:  tasks.keySet())
        		{
        			if(tasks.get(taskKey).getIsUploading() == 'Y')
        			{
        				result = "uploading";
        				break;
        			}
        		}
        	}
            PrintWriter out = p_response.getWriter();
            p_response.setContentType("text/html");
            out.write(result);
            out.close();
            return;
		}
        
        if ("validateBeforeDispatch".equals(p_request.getParameter("action")))
        {
            String[] ids = p_request.getParameterValues("ids");
            StringBuffer jobName = new StringBuffer();
            for (String id : ids)
            {
                JobImpl job = HibernateUtil.get(JobImpl.class,
                        Long.parseLong(id));
                if (!job.hasSetCostCenter())
                {
                    if (jobName.length() > 0)
                    {
                        jobName.append(", ");
                    }

                    jobName.append(job.getName());
                }
            }

            if (jobName.length() > 0)
            {
                ResourceBundle bundle = PageHandler.getBundle(p_request
                        .getSession());
                ServletOutputStream out = p_response.getOutputStream();
                out.write((bundle.getString("msg_cost_center_empty_jobs")
                        + "\r\n" + jobName).getBytes());
                out.flush();
                out.close();
            }

            return;
        }
        else if ("validateBeforeRename"
                .equals(p_request.getParameter("action")))
        {
            String id = p_request.getParameter("jobId");
            Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
            StringBuffer jobName = new StringBuffer();
            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(id));
            for (Workflow wf : job.getWorkflows())
            {
                String wfState = wf.getState();
                // return only workflows that can be exported
                if (wfState.equals(Workflow.DISPATCHED)
                        || wfState.equals(Workflow.ARCHIVED)
                        || wfState.equals(Workflow.EXPORT_FAILED)
                        || wfState.equals(Workflow.EXPORTED)
                        || wfState.equals(Workflow.LOCALIZED))
                {
                	jobName.append("\r\n"
                            + wf.getTargetLocale().getDisplayName(uiLocale));
                }
            }

            if (jobName.length() > 0)
            {
                ResourceBundle bundle = PageHandler.getBundle(p_request
                        .getSession());
                ServletOutputStream out = p_response.getOutputStream();
                out.write(bundle.getString("msg_unable_rename_job").getBytes());
                out.flush();
                out.close();
            }

            return;
        }
        else if ("renameJobSummary".equals(p_request.getParameter("action")))
        {

            String id = p_request.getParameter("jobId");
            String jobName = new String (p_request.getParameter("jobName").getBytes(), "UTF-8");
            jobName = URLDecoder.decode(jobName, "UTF-8");
            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(id));
            job.setJobName(EditUtil.removeCRLF(jobName));
            try
            {
                HibernateUtil.merge(job);
            }
            catch (HibernateException e)
            {
                ServletOutputStream out = p_response.getOutputStream();
                out.write((e.getMessage()).getBytes());
                out.flush();
                out.close();
            }
            return;
        }
        else
        {
            performAppropriateOperation(p_request);
        }

        //For "Re-Create" job
        String recreateJobIds = p_request.getParameter(RECREATE_JOB_PARAM);
        if (StringUtil.isNotEmpty(recreateJobIds))
        {
            if (!isRefresh(sessionMgr, recreateJobIds, RECREATE_JOB_PARAM))
            {
                String message = recreateJob(p_request);
                if (StringUtil.isNotEmpty(message))
                {
                    p_request.setAttribute("recreateMessage", message);
                }
            }
        }
        
        sessionMgr.setMyjobsAttribute("lastState", Job.READY_TO_BE_DISPATCHED);
        JobVoReadySearcher searcher = new JobVoReadySearcher();
        searcher.setJobVos(p_request, true);
        p_request.setAttribute(EXPORT_URL_PARAM, m_exportUrl);
        p_request.setAttribute(JOB_LIST_START_PARAM,
                p_request.getParameter(JOB_LIST_START_PARAM));
        p_request.setAttribute(
                PAGING_SCRIPTLET,
                getPagingText(p_request,
                        ((NavigationBean) beanMap.get(BASE_BEAN)).getPageURL(),
                        Job.READY_TO_BE_DISPATCHED));       
        
        // Set the EXPORT_INIT_PARAM in the sessionMgr so we can bring
        // the user back here after they Export
        sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM,BASE_BEAN);
        sessionMgr.setAttribute("destinationPage", "ready");       
        // clear the session for download job from joblist page
        sessionMgr.setAttribute(DownloadFileHandler.DOWNLOAD_JOB_LOCALES, null);
        sessionMgr.setAttribute(DownloadFileHandler.DESKTOP_FOLDER, null);
        setJobProjectsLocales(sessionMgr, session);

        

        // turn on cache. do both. "pragma" for the older browsers.
        p_response.setHeader("Pragma", "yes-cache"); // HTTP 1.0
        p_response.setHeader("Cache-Control", "yes-cache"); // HTTP 1.1
        p_response.addHeader("Cache-Control", "yes-store"); // tell proxy not to
        // cache
        // forward to the jsp page.
        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher(p_thePageDescriptor.getJspURL());
        dispatcher.forward(p_request, p_response);
    }

    private String recreateJob(HttpServletRequest p_request)
    {
        String message = null;

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        Set<Long> allJobIds = new HashSet<Long>();
        String ids = p_request.getParameter(RECREATE_JOB_PARAM);
        StringTokenizer tokenizer = new StringTokenizer(ids);
        while (tokenizer.hasMoreTokens())
        {
            allJobIds.add(Long.parseLong(tokenizer.nextToken()));
        }

        Set<Long> problemJobIds = checkJobs(allJobIds);
        allJobIds.removeAll(problemJobIds);
        if (allJobIds.size() > 0)
        {
            sessionMgr.setAttribute(RECREATE_JOB_PARAM, ids);
            for (long id : allJobIds)
            {
                WorkflowHandlerHelper.recreateJob(id);
            }
            sessionMgr.removeElement(RECREATE_JOB_PARAM);
        }

        if (problemJobIds.size() > 0)
        {
            message = getMessage(problemJobIds);
        }

        return message;
    }

    private String getMessage(Set<Long> jobIds)
    {
        String message = null;

        StringBuffer msg = new StringBuffer();
        if (jobIds.size() > 0)
        {
            msg.append("Job ");
            for (long id : jobIds) {
                msg.append(id).append(",");
            }
            message = msg.substring(0, msg.length()-1);
            message += " can not be re-created because its requests information are not fully generated.";
        }

        return message;
    }

    private Set<Long> checkJobs(Set<Long> jobIds)
    {
        Set<Long> problemIds = new HashSet<Long>();
        for (long id : jobIds)
        {
            try
            {
                Job job = ServerProxy.getJobHandler().getJobById(id);
                if (!job.getIsAllRequestGenerated())
                {
                    problemIds.add(id);
                }
            }
            catch (Exception e)
            {
                problemIds.add(id);
            }
        }

        return problemIds;
    }

    /**
     * Overide getControlFlowHelper so we can do processing and redirect the
     * user correctly.
     * 
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {

        return new JobSearchControlFlowHelper(p_request, p_response);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // ///////////////////////// JOB CONTROL OPERATION
    // ////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    protected void performAppropriateOperation(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String param = null;
        String action = p_request.getParameter("action");

        if (p_request.getParameter(DISPATCH_JOB_PARAM) != null)
        {
            param = p_request.getParameter(DISPATCH_JOB_PARAM);
            if (isRefresh(sessionMgr, param, DISPATCH_JOB_PARAM))
            {
                return;
            }
            sessionMgr.setAttribute(DISPATCH_JOB_PARAM, param);
            String jobId = null;
            StringTokenizer tokenizer = new StringTokenizer(param);
            while (tokenizer.hasMoreTokens())
            {
                jobId = tokenizer.nextToken();
                WorkflowHandlerHelper.dispatchJob(WorkflowHandlerHelper
                        .getJobById(Long.parseLong(jobId)));
            }
        }
        else if (p_request.getParameter(DISCARD_JOB_PARAM) != null)
        {
            param = p_request.getParameter(DISCARD_JOB_PARAM);
            if (isRefresh(sessionMgr, param, DISCARD_JOB_PARAM))
            {
                return;
            }
            sessionMgr.setAttribute(DISCARD_JOB_PARAM, param);
            String jobId = null;
            StringTokenizer tokenizer = new StringTokenizer(param);
            while (tokenizer.hasMoreTokens())
            {
                jobId = tokenizer.nextToken();
                String userId = ((User) sessionMgr
                        .getAttribute(WebAppConstants.USER)).getUserId();
                // pass in a NULL state - should discard the job and ALL
                // workflows regardless of state
                WorkflowHandlerHelper
                        .cancelJob(userId, WorkflowHandlerHelper
                                .getJobById(Long.parseLong(jobId)), null);
            }
        }
        else if ("save".equals(action))
        {
            // save the results from a search/replace
            SearchHandlerHelper.replace((List<TuvInfo>) sessionMgr
                    .getAttribute("tuvInfos"));
        }
        else if (action != null && action.equals(PLANNED_COMP_DATE))
        {
            WorkflowHandlerHelper.updatePlannedCompletionDates(p_request);
        }
        else
        {
            // Don't do anything if they are just viewing the table
            // and not performing an action on a job
            return;
        }
    }
}
