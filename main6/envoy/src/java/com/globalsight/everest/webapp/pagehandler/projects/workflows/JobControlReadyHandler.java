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
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;

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
        p_request.setAttribute("action", p_request.getParameter("action"));
        HashMap beanMap = invokeJobControlPage(p_thePageDescriptor, p_request,
                BASE_BEAN);
        p_request.setAttribute("searchType",
                p_request.getParameter("searchType"));

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
        else
        {
            performAppropriateOperation(p_request);
        }

        p_request.setAttribute(
                JOB_SCRIPTLET,
                getJobText(p_request, ((NavigationBean) beanMap.get(BASE_BEAN))
                        .getPageURL(), ((NavigationBean) beanMap
                        .get(MODIFY_BEAN)).getPageURL(),
                        ((NavigationBean) beanMap.get(DETAILS_BEAN))
                                .getPageURL(), ((NavigationBean) beanMap
                                .get(PLANNED_COMPLETION_DATE_BEAN))
                                .getPageURL(), getExpJobListing(p_request),
                        Job.READY_TO_BE_DISPATCHED, true, true));
        p_request.setAttribute(JOB_LIST_START_PARAM,
                p_request.getParameter(JOB_LIST_START_PARAM));
        p_request.setAttribute(
                PAGING_SCRIPTLET,
                getPagingText(p_request,
                        ((NavigationBean) beanMap.get(BASE_BEAN)).getPageURL(),
                        Job.READY_TO_BE_DISPATCHED));

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute("destinationPage", "ready");

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
        else if (action != null && action.equals("save"))
        {
            // save the results from a search/replace
            SearchHandlerHelper.replace(
                    (List) sessionMgr.getAttribute("tuvInfos"),
                    (String) sessionMgr.getAttribute(COMPANY_ID));
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
