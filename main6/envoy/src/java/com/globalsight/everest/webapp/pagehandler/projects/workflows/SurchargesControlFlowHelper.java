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

import org.apache.log4j.Logger;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.util.GeneralException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * EditPagesControlFlowHelper, A page flow helper that 
 * saves the number of pages in a job then redirects
 * the user to the next JSP page.
 */
class SurchargesControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            SurchargesControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public SurchargesControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    // returns the name of the link to follow
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        String destinationPage = JobManagementHandler.SURCHARGES_BEAN;
        if (m_request.getParameterValues("formAction")[0].equals("remove"))
        {  
            try 
            {
                HttpSession session = m_request.getSession(false);
                SessionManager sessionMgr =
                    (SessionManager)session.getAttribute(SESSION_MANAGER);
                Cost cost = null;
                int costType = Cost.EXPENSE;
                String surchargesFor = (String) sessionMgr.getAttribute(JobManagementHandler.SURCHARGES_FOR);
                if(surchargesFor.equals(EXPENSES))
                {
                    cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.COST_OBJECT);
                    costType = Cost.EXPENSE;
                }
                else
                {
                    cost = (Cost)sessionMgr.getAttribute(JobManagementHandler.REVENUE_OBJECT);
                    costType = Cost.REVENUE;
                }
                Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(
                        m_request.getParameter(JobManagementHandler.JOB_ID)));
                // Clicked on the Remove button
                String[] surcharges = m_request.getParameterValues("surcharge");
                for (int i = 0; i < surcharges.length; i++) 
                {
                    cost = ServerProxy.getCostingEngine().removeSurcharge(cost.getId(), 
                                                                          surcharges[i], costType);
                }

                // Put the new cost object in the session
                if(surchargesFor.equals(EXPENSES))
                {
                    sessionMgr.setAttribute(JobManagementHandler.COST_OBJECT, cost);
                }
                else
                {
                    sessionMgr.setAttribute(JobManagementHandler.REVENUE_OBJECT, cost);
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
        }
        else if (m_request.getParameterValues("formAction")[0].equals("edit") || 
                 m_request.getParameterValues("formAction")[0].equals("add"))
        {
            // Clicked on the Edit or add button
            //m_request.setAttribute(JobManagementHandler.SURCHARGE_ACTION, "edit");
            destinationPage = JobManagementHandler.EDIT_ADD_SURCHARGES_BEAN;
        }
        else if (m_request.getParameterValues("formAction")[0].equals("ok"))
        {
            // Clicked on the OK button
            destinationPage = JobManagementHandler.DETAILS_BEAN;
        }
        else
        {
            // The formAction param is null, which means they are here for 
            // the first time so give the the Surcharges screen again.
            destinationPage = JobManagementHandler.SURCHARGES_BEAN;
        }
        return destinationPage;
    }
}
