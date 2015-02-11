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
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.EditPagesControlFlowHelper;
import com.globalsight.util.GeneralException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a page handler for handling the editing of 
 * the number of pages in a job. Invoked by clicking Pages -> Edit
 * on the Job Details screen.
 */
public class EditFinalCostHandler extends PageHandler
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
            NavigationBean editFinalCostBean =
                new NavigationBean(JobManagementHandler.EDIT_FINAL_COST_BEAN, pageName);

            p_request.setAttribute(JobManagementHandler.DETAILS_BEAN, detailsBean);
            p_request.setAttribute(JobManagementHandler.EDIT_FINAL_COST_BEAN, editFinalCostBean);

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
        return new EditFinalCostControlFlowHelper(p_request, p_response);
    }
}



