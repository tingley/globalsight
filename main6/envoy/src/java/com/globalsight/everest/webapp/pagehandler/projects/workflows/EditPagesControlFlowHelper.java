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
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.util.GeneralException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * EditPagesControlFlowHelper, A page flow helper that 
 * saves the number of pages in a job then redirects
 * the user to the next JSP page.
 */
class EditPagesControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            EditPagesControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public EditPagesControlFlowHelper(HttpServletRequest p_request,
                                      HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    // returns the name of the link to follow
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        String destinationPage = null;
        if (m_request.getParameterValues("formAction")[0].equals("save"))
        {  
            // Clicked on Save on the Edit Pages screen, do the processing, then send them to JobDetails
            // to see the updated value
            int p_unitOfWork = 3;
            long p_jobId = Long.parseLong((String)m_request.getParameterValues(JobManagementHandler.JOB_ID)[0]);
            float numPages = Float.parseFloat(m_request.getParameterValues("pages")[0]);
            try 
            {
                JobHandler jh = ServerProxy.getJobHandler();
                Job j = jh.getJobById(p_jobId);
                jh.updatePageCount(j,(int)numPages);
                ServerProxy.getCostingEngine().setEstimatedAmountOfWorkInJob(p_jobId, p_unitOfWork, numPages);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
            destinationPage = JobManagementHandler.DETAILS_BEAN;
        }
        else if (m_request.getParameterValues("formAction")[0].equals("cancel"))
        {
            destinationPage = JobManagementHandler.DETAILS_BEAN;
        }
        else
        {
            // The formAction param is null, so give the the editPages screen
            // again.
            destinationPage = JobManagementHandler.EDIT_PAGES_BEAN;
        }
        return destinationPage;

    }
}
