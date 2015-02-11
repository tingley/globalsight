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

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.projects.jobvo.JobVoArchivedSearcher;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;

public class JobArchiveHandler extends JobManagementHandler
{
    private static final String BASE_BEAN = "archived";

    private static NavigationBean m_exportBean = null;

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
    public void myInvokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
    	HttpSession session = p_request.getSession(false);
    	SessionManager sessionMgr = (SessionManager) session
    		.getAttribute(SESSION_MANAGER);
    	boolean stateMarch = false;
		if(Job.ARCHIVED.equals((String)sessionMgr.getMyjobsAttribute("lastState")))
				stateMarch = true;
		setJobSearchFilters(sessionMgr, p_request, stateMarch);
    	
        p_request.setAttribute("action", p_request.getParameter("action"));
        HashMap beanMap = invokeJobControlPage(p_pageDescriptor, p_request,
                BASE_BEAN);
        p_request.setAttribute("searchType", p_request
                .getParameter("searchType"));

        m_exportBean = new NavigationBean(EXPORT_BEAN, p_pageDescriptor
                .getPageName());
        performAppropriateOperation(p_request);

        sessionMgr.setMyjobsAttribute("lastState", Job.ARCHIVED);
        JobVoArchivedSearcher searcher = new JobVoArchivedSearcher();
        searcher.setJobVos(p_request);
        
        p_request.setAttribute(BASE_BEAN, ((NavigationBean) beanMap
                .get(BASE_BEAN)));
        p_request.setAttribute(EXPORT_URL_PARAM, m_exportBean.getPageURL());
        p_request.setAttribute(JOB_ID, JOB_ID);
        p_request.setAttribute(JOB_LIST_START_PARAM, p_request
                .getParameter(JOB_LIST_START_PARAM));
        p_request.setAttribute(PAGING_SCRIPTLET, getPagingText(p_request,
                ((NavigationBean) beanMap.get(BASE_BEAN)).getPageURL(),
                Job.ARCHIVED));

        // Set the EXPORT_INIT_PARAM in the sessionMgr so we can bring
        // the user back here after they Export
        sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM,
                BASE_BEAN);

        sessionMgr.setAttribute("destinationPage", "archived");
        setJobProjectsLocales(sessionMgr, session);
        // turn on cache. do both. "pragma" for the older browsers.
        p_response.setHeader("Pragma", "yes-cache"); // HTTP 1.0
        p_response.setHeader("Cache-Control", "yes-cache"); // HTTP 1.1
        p_response.addHeader("Cache-Control", "yes-store"); // tell proxy not to
                                                            // cache
        // forward to the jsp page.
        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher(p_pageDescriptor.getJspURL());
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
        if (PLANNED_COMP_DATE.equals(p_request.getParameter("action")))
        {
            WorkflowHandlerHelper.updatePlannedCompletionDates(
                    p_request);
        }
        else if((param = p_request.getParameter(DISCARD_JOB_PARAM)) != null)
        {
            if (isRefresh(sessionMgr, param, DISCARD_JOB_PARAM))
            {
                return;
            }
            sessionMgr.setAttribute(DISCARD_JOB_PARAM, param);
            String jobId;
            StringTokenizer tokenizer = new StringTokenizer(param);
            while (tokenizer.hasMoreTokens())
            {
                jobId = tokenizer.nextToken();
                Job job = WorkflowHandlerHelper.getJobById(Long
                        .parseLong(jobId));
                String userId = ((User) sessionMgr
                        .getAttribute(WebAppConstants.USER)).getUserId();

                WorkflowHandlerHelper.cancelJob(userId, job, null);
            }
        }
    }
}
