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

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.EditPagesControlFlowHelper;
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
public class EditAddSurchargesHandler extends PageHandler
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
            HttpSession session = p_request.getSession(false);
            SessionManager sessionMgr =
                    (SessionManager)session.getAttribute(SESSION_MANAGER);
            String pageName = p_pageDescriptor.getPageName();
            NavigationBean detailsBean =
                new NavigationBean(JobManagementHandler.EDIT_ADD_SURCHARGES_BEAN, pageName);
            NavigationBean surchargesBean =
                new NavigationBean(JobManagementHandler.SURCHARGES_BEAN, pageName);
            String jobId = p_request.getParameter(JobManagementHandler.JOB_ID);
            p_request.setAttribute(JobManagementHandler.JOB_ID, jobId);
            p_request.setAttribute(JobManagementHandler.EDIT_ADD_SURCHARGES_BEAN, detailsBean);
            p_request.setAttribute(JobManagementHandler.SURCHARGES_BEAN, surchargesBean);

            String surchargeName = null;
            String surchargeType = "0";
            String surchargeValue = null;
            String surchargesFor = (String)sessionMgr.getAttribute(JobManagementHandler.SURCHARGES_FOR);

            if (p_request.getParameterValues("formAction")[0].equals("add")) 
            {
                p_request.setAttribute(JobManagementHandler.SURCHARGE_ACTION, "add");
            }
            else 
            {
                surchargeName = p_request.getParameterValues(JobManagementHandler.SURCHARGE_NAME)[0];
                surchargeType = p_request.getParameterValues(JobManagementHandler.SURCHARGE_TYPE)[0];
                surchargeValue = p_request.getParameterValues(JobManagementHandler.SURCHARGE_VALUE)[0];
                p_request.setAttribute(JobManagementHandler.SURCHARGE_ACTION, "edit");
            }

            p_request.setAttribute(JobManagementHandler.SURCHARGE_NAME, surchargeName);
            p_request.setAttribute(JobManagementHandler.SURCHARGE_TYPE, surchargeType);
            p_request.setAttribute(JobManagementHandler.SURCHARGE_VALUE, surchargeValue);
            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Override getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {
        return new EditAddSurchargesControlFlowHelper(p_request, p_response);
    }
}



