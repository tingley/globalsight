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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // java
import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import java.rmi.RemoteException;

// com.globalsight
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.workflowmanager.Workflow;

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
        HashMap beanMap = invokeJobControlPage(p_thePageDescriptor, p_request,
                BASE_BEAN);
        // error bean.
        m_importErrorBean = new NavigationBean(ERROR_BEAN, p_thePageDescriptor
                .getPageName());

        p_request.setAttribute("searchType", p_request
                .getParameter("searchType"));
        performAppropriateOperation(p_request);
        Vector jobStates = new Vector();
        jobStates.add(Job.PENDING);
        jobStates.add(Job.BATCHRESERVED);
        jobStates.add(Job.IMPORTFAILED);
        jobStates.add(Job.ADD_FILE);
        
        p_request.setAttribute(JOB_SCRIPTLET, getJobText(p_request,
                ((NavigationBean) beanMap.get(BASE_BEAN)).getPageURL(), null,
                ((NavigationBean) beanMap.get(DETAILS_BEAN)).getPageURL(),
                ((NavigationBean) beanMap.get(PLANNED_COMPLETION_DATE_BEAN))
                        .getPageURL(), getExpJobListing(p_request), jobStates,
                false));
        p_request.setAttribute(ERROR_URL_PARAM, m_importErrorBean.getPageURL());
        p_request.setAttribute(JOB_ID, JOB_ID);
        p_request.setAttribute(DISCARD_JOB_PARAM, DISCARD_JOB_PARAM);
        p_request.setAttribute(MAKE_READY_JOB_PARAM, MAKE_READY_JOB_PARAM);
        p_request.setAttribute(JOB_LIST_START_PARAM, p_request
                .getParameter(JOB_LIST_START_PARAM));
        p_request.setAttribute(PAGING_SCRIPTLET, getPagingText(p_request,
                ((NavigationBean) beanMap.get(BASE_BEAN)).getPageURL(),
                jobStates));

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute("destinationPage", "pending");

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
        String sessionId = session.getId();
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
                WorkflowHandlerHelper
                        .makeReadyJob(sessionId, WorkflowHandlerHelper
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
                WorkflowHandlerHelper
                        .cancelJob(userId, sessionId, WorkflowHandlerHelper
                                .getJobById(Long.parseLong(jobId)));
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
            WorkflowHandlerHelper.cancelImportErrorPages(userId, sessionId,
                    WorkflowHandlerHelper.getJobById(Long.parseLong(p_request
                            .getParameter(CANCEL_IMPORT_ERROR_PAGES_PARAM))));
        }
        // FOR UPDATING PLANNED COMPLETION DATES
        if (PLANNED_COMP_DATE.equals(p_request.getParameter("action")))
        {
            WorkflowHandlerHelper.updatePlannedCompletionDates(sessionId,
                    p_request);
        }

    }
}
