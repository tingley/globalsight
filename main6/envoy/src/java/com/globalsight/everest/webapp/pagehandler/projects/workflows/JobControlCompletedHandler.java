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

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler;
import com.globalsight.everest.workflowmanager.Workflow;
import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;

public class JobControlCompletedHandler extends JobManagementHandler
{
    private static final String BASE_BEAN = "complete";
    private NavigationBean m_exportBean = null;

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * @param p_ageDescriptor the description of the page to be produced.
     * @param p_request original request sent from the browser.
     * @param p_response original response object.
     * @param p_context the Servlet context.
     */
    public void myInvokePageHandler(WebPageDescriptor p_thePageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException, RemoteException, EnvoyServletException
    {
        m_exportBean = new NavigationBean(EXPORT_BEAN, 
                                          p_thePageDescriptor.getPageName());
        HashMap beanMap = invokeJobControlPage(p_thePageDescriptor, 
                                               p_request,
                                               LOCALIZED_BEAN);
        p_request.setAttribute("searchType", p_request.getParameter("searchType"));
        p_request.setAttribute("action", p_request.getParameter("action"));
        performAppropriateOperation(p_request);
        p_request.setAttribute(JOB_SCRIPTLET,
                               getJobText(p_request,
                                          ((NavigationBean)beanMap.get(LOCALIZED_BEAN)).getPageURL(),
                                          null,
                                          ((NavigationBean)beanMap.get(DETAILS_BEAN)).getPageURL(),
                                          ((NavigationBean)beanMap.get(PLANNED_COMPLETION_DATE_BEAN)).getPageURL(),
                                          getExpJobListing(p_request),
                                          Job.LOCALIZED, false));
        p_request.setAttribute(EXPORT_URL_PARAM, 
                               m_exportBean.getPageURL());
        p_request.setAttribute(JOB_ID, 
                               JOB_ID);
        p_request.setAttribute(JOB_LIST_START_PARAM, 
                               p_request.getParameter(JOB_LIST_START_PARAM));
        p_request.setAttribute(PAGING_SCRIPTLET, 
                               getPagingText(p_request, 
                                             ((NavigationBean)beanMap.get(BASE_BEAN)).getPageURL(),
                                             Job.LOCALIZED));

        // Set the EXPORT_INIT_PARAM in the sessionMgr so we can bring
        // the user back here after they Export
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM, 
                BASE_BEAN);

        sessionMgr.setAttribute("destinationPage", "localized");
        //clear the session for download job from joblist page
        sessionMgr.setAttribute(DownloadFileHandler.DOWNLOAD_JOB_LOCALES, null);
        sessionMgr.setAttribute(DownloadFileHandler.DESKTOP_FOLDER, null);

        // turn on cache.  do both.  "pragma" for the older browsers.
        p_response.setHeader("Pragma", "yes-cache"); //HTTP 1.0
        p_response.setHeader("Cache-Control", "yes-cache"); //HTTP 1.1
        p_response.addHeader("Cache-Control", "yes-store"); // tell proxy not to cache
        // forward to the jsp page.
        RequestDispatcher dispatcher =
            p_context.getRequestDispatcher(p_thePageDescriptor.getJspURL());
        dispatcher.forward(p_request, p_response);
    }

    /**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {

        return new JobSearchControlFlowHelper(p_request, p_response);
    }

    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////// JOB CONTROL OPERATION ////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    protected void performAppropriateOperation(HttpServletRequest p_request)
    throws EnvoyServletException
    {
        String action = p_request.getParameter("action");
        if (action != null && action.equals(PLANNED_COMP_DATE))
        {
            WorkflowHandlerHelper.
                updatePlannedCompletionDates(p_request);
        }
        // DO NOTHING.... since this takes you to another screen.
    }
}
