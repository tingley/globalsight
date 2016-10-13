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
 * EditFinalCostControlFlowHelper, A page flow helper that 
 * overrides the final cost with a user-entered value.
 */
class EditFinalCostControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            EditFinalCostControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public EditFinalCostControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    // returns the name of the link to follow
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        String destinationPage = JobManagementHandler.EDIT_FINAL_COST_BEAN;
        if (m_request.getParameterValues("formAction")[0].equals("save"))
        {  
            try
            {  
                HttpSession session = m_request.getSession(false);
                SessionManager sessionMgr =
                    (SessionManager)session.getAttribute(SESSION_MANAGER);
                // Clicked on Save on the Edit Final Cost screen, do the processing, then send them to JobDetails
                // to see the updated value
                Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(
                        m_request.getParameter(JobManagementHandler.JOB_ID))); 
                Cost cost = null;
                int costType = Cost.EXPENSE;
                String surchargesFor = (String)m_request.getParameter(JobManagementHandler.SURCHARGES_FOR);
                if(surchargesFor.equals(EXPENSES))
                {
                    costType = Cost.EXPENSE;
                }
                else
                {
                    costType = Cost.REVENUE;
                }

                if (m_request.getParameterValues(JobManagementHandler.REMOVE_OVERRIDE) != null &&
                    m_request.getParameterValues(JobManagementHandler.REMOVE_OVERRIDE)[0].equals("on"))
                {
                    cost = ServerProxy.getCostingEngine().clearOverrideCost(job, costType);
                }
                else
                {
                    cost = ServerProxy.getCostingEngine().overrideCost(job, 
                        Float.parseFloat(m_request.getParameterValues(JobManagementHandler.FINAL_COST)[0]), costType);
                }

                destinationPage = JobManagementHandler.DETAILS_BEAN;

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
        else if (m_request.getParameterValues("formAction")[0].equals("cancel"))
        {
            destinationPage = JobManagementHandler.DETAILS_BEAN;
        }
        else
        {
            // The formAction param is null, so give the the editPages screen
            // again.
            destinationPage = JobManagementHandler.EDIT_FINAL_COST_BEAN;
        }
        return destinationPage;
    }
}
