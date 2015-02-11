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


import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dispatches the user to the correct JSP.
 */
class JobSearchControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            JobSearchControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public JobSearchControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    /**
     * Does the processing then 
     * returns the name of the link to follow
     * 
     * @return 
     * @exception EnvoyServletException
     */
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        String destinationPage = null;
        String fromRequest = (String) m_request.getParameter("fromRequest");
        String searchType = (String) m_request.getParameter("searchType");

        // searchType should only be null for paging purposes
        if (searchType == null || "search".equals(searchType) ||
            fromRequest == null)
        {
            destinationPage = (String)m_request.getParameter("linkName");
        }
        else if (fromRequest != null)
        {  
            String status = (String) m_request.getParameter(JobSearchConstants.STATUS_OPTIONS);
            if (Job.PENDING.equals(status))
                 destinationPage = "pending";
            else if (Job.READY_TO_BE_DISPATCHED.equals(status))
                destinationPage = "ready";
            else if (Job.DISPATCHED.equals(status))
                destinationPage = "progress";
            else if (Job.LOCALIZED.equals(status))
                destinationPage = "complete";
            else if (Job.DTPINPROGRESS.equals(status))
                destinationPage = "dtpprogress";
            else if (Job.ARCHIVED.equals(status))
                destinationPage = "archived";
            else if (Job.EXPORTED.equals(status))
                destinationPage = "exported";
            else
            	destinationPage = "allStatus";
        }
        else if ("goToSearch".equals(searchType))
        {
            destinationPage = "jobSearch";
        }
        return destinationPage;
    }
}

