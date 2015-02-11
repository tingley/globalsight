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

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;

/**
 * EditPagesControlFlowHelper, A page flow helper that saves the number of pages
 * in a job then redirects the user to the next JSP page.
 */
class EstimatedCompletionDateControlFlowHelper implements ControlFlowHelper,
        WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(EditPagesControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public EstimatedCompletionDateControlFlowHelper(
            HttpServletRequest p_request, HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    // returns the name of the link to follow
    public String determineLinkToFollow() throws EnvoyServletException
    {
        String destinationPage = null;

        HttpSession session = m_request.getSession(false);

        TimeZone timezone = (TimeZone) session
                .getAttribute(WebAppConstants.USER_TIME_ZONE);

        String action = m_request.getParameter("action");
        String from = m_request.getParameter("from");
        if (action == null)
        {
            // The formAction param is null, so give the the editPages screen
            // again.
            destinationPage = JobManagementHandler.ESTIMATED_COMP_DATE_BEAN;
        }
        else if (action.equals(JobManagementHandler.ESTIMATED_COMP_DATE))
        {
            // Save the estimated workflow completion date
            WorkflowHandlerHelper.updateEstimatedCompletionDates(m_request,
                    timezone);
            destinationPage = from != null ? from
                    : JobManagementHandler.DETAILS_BEAN;
        }
        else if (action.equals("cancel"))
        {
            destinationPage = from != null ? from
                    : JobManagementHandler.DETAILS_BEAN;
        }
        else
        {
            destinationPage = from != null ? from
                    : JobManagementHandler.DETAILS_BEAN;
        }
        return destinationPage;

    }
}
