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

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.EditPagesControlFlowHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This is a page handler for handling the editing of 
 * the number of pages in a job. Invoked by clicking Pages -> Edit
 * on the Job Details screen.
 */
public class EditPagesHandler extends PageHandler
{


  /**
   * Invokes this PageHandler
   *
   * @param jspURL the URL of the JSP to be invoked
   * @param the original request sent from the browser
   * @param the original response object
   * @param context the Servlet context
   */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                HttpServletRequest p_request, HttpServletResponse p_response,
                ServletContext p_context) 
    throws ServletException, EnvoyServletException
    {
        try
        {
            String pageName = p_pageDescriptor.getPageName();
            NavigationBean detailsBean =
                new NavigationBean(JobManagementHandler.DETAILS_BEAN, pageName);
            NavigationBean editPagesBean =
                new NavigationBean(JobManagementHandler.EDIT_PAGES_BEAN, pageName);

            p_request.setAttribute(JobManagementHandler.DETAILS_BEAN, detailsBean);
            p_request.setAttribute(JobManagementHandler.EDIT_PAGES_BEAN, editPagesBean); 

            HttpSession session = p_request.getSession(false);
            SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
            long p_jobId = ((Long)sessionMgr.getAttribute(JobManagementHandler.JOB_ID)).longValue();
            Job j = WorkflowHandlerHelper.getJobById(p_jobId);
            // set the number of pages
            p_request.setAttribute(JobManagementHandler.PAGES_IN_JOB, 
                                   Integer.toString(j.getPageCount()));

            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
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
        return new EditPagesControlFlowHelper(p_request, p_response);
    }
}



