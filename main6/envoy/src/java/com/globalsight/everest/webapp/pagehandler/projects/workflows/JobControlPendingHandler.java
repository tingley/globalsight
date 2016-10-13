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

// javax
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.projects.jobvo.JobVoPendingSearcher;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.StringUtil;

public class JobControlPendingHandler extends JobManagementHandler
{
    private static final String BASE_BEAN = "pending";

    private static NavigationBean m_importErrorBean = null;

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
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
    	boolean stateMarch = false;
    	if(Job.PENDING.equals((String)sessionMgr.getMyjobsAttribute("lastState")))
			stateMarch = true;
    	setJobSearchFilters(sessionMgr, p_request, stateMarch);
    	
        HashMap beanMap = invokeJobControlPage(p_thePageDescriptor, p_request, BASE_BEAN);
        // error bean.
        m_importErrorBean = new NavigationBean(ERROR_BEAN,
                p_thePageDescriptor.getPageName());

        p_request.setAttribute("searchType",
                p_request.getParameter("searchType"));
        Vector<String> jobStates = new Vector<String>();
        jobStates.addAll(Job.PENDING_STATUS_LIST);

        performAppropriateOperation(p_request);////////

        // For "Re-Create" job
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

        sessionMgr.setMyjobsAttribute("lastState", Job.PENDING);
        JobVoPendingSearcher searcher = new JobVoPendingSearcher();
        searcher.setJobVos(p_request);
        p_request.setAttribute(ERROR_URL_PARAM, m_importErrorBean.getPageURL());
        p_request.setAttribute(JOB_ID, JOB_ID);
        p_request.setAttribute(DISCARD_JOB_PARAM, DISCARD_JOB_PARAM);
        p_request.setAttribute(MAKE_READY_JOB_PARAM, MAKE_READY_JOB_PARAM);
        p_request.setAttribute(RECREATE_JOB_PARAM, RECREATE_JOB_PARAM);
        p_request.setAttribute(JOB_LIST_START_PARAM,
                p_request.getParameter(JOB_LIST_START_PARAM));
        p_request.setAttribute(
                PAGING_SCRIPTLET,
                getPagingText(p_request,
                        ((NavigationBean) beanMap.get(BASE_BEAN)).getPageURL(),
                        jobStates));

        sessionMgr.setAttribute("destinationPage", "pending");
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
    protected String getWFActionText(ResourceBundle p_bundle, String p_baseURL,
            Workflow p_workflow)
    {
        return "";
    }

    protected void performAppropriateOperation(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        String param = null;

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String userId = ((User) sessionMgr.getAttribute(WebAppConstants.USER))
                .getUserId();
        // THIS IS FOR JOB MAKE READY
        if ((param = p_request.getParameter(MAKE_READY_JOB_PARAM)) != null)
        {
            if (isRefresh(sessionMgr, param, MAKE_READY_JOB_PARAM))
            {
                return;
            }
            sessionMgr.setAttribute(MAKE_READY_JOB_PARAM, param);
            String jobId = null;
            StringTokenizer tokenizer = new StringTokenizer(param);
            while (tokenizer.hasMoreTokens())
            {
                jobId = tokenizer.nextToken();
                WorkflowHandlerHelper.makeReadyJob(WorkflowHandlerHelper
                        .getJobById(Long.parseLong(jobId)));
            }
        }
        // THIS IS FOR JOB CANCEL
        if ((param = p_request.getParameter(DISCARD_JOB_PARAM)) != null)
        {
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
                Job job = WorkflowHandlerHelper.getJobById(Long
                        .parseLong(jobId));

                WorkflowHandlerHelper.cancelJob(userId, job, null);
            }
        }
        // FOR CANCELLING THE IMPORT ERROR PAGES IN A IMPORT_FAILED JOB
        // MOVES THE JOB INTO THE PENDING OR DISPATCHED STATE
        if ((param = p_request.getParameter(CANCEL_IMPORT_ERROR_PAGES_PARAM)) != null)
        {
            if (isRefresh(sessionMgr, param, CANCEL_IMPORT_ERROR_PAGES_PARAM))
            {
                return;
            }
            sessionMgr.setAttribute(CANCEL_IMPORT_ERROR_PAGES_PARAM, param);
            WorkflowHandlerHelper.cancelImportErrorPages(userId,
                    WorkflowHandlerHelper.getJobById(Long.parseLong(p_request
                            .getParameter(CANCEL_IMPORT_ERROR_PAGES_PARAM))));
        }
        // FOR UPDATING PLANNED COMPLETION DATES
        if (PLANNED_COMP_DATE.equals(p_request.getParameter("action")))
        {
            WorkflowHandlerHelper.updatePlannedCompletionDates(p_request);
        }
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

    /**
     * To recreate job, it need all the request information in "request" table.
     * If there are source files that have no requests info, after recreate job,
     * the new job will lost such source files. So do not allow such jobs to
     * recreate.
     * 
     * @param jobIds
     * @return problem job ID list.
     */
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
     * Get a message to warn to user that these jobs can not be recreated.
     * @param jobIds
     * @return String -- A message to warn to user.
     */
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
}
