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
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;

// java
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * EditPagesControlFlowHelper, A page flow helper that saves the number of pages
 * in a job then redirects the user to the next JSP page.
 */
class EditSourcePageWcControlFlowHelper implements ControlFlowHelper,
        WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(EditPagesControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public EditSourcePageWcControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    // returns the name of the link to follow
    public String determineLinkToFollow() throws EnvoyServletException
    {
        String destinationPage = null;
        if (m_request.getParameterValues("formAction")[0].equals("save"))
        {
            // Clicked on Save on the Edit Word Count screen, do the processing,
            // then send them to JobDetails
            // to see the updated value

            HttpSession session = m_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);

            // pass the source page and the word count information in a hashmap
            // for update
            HashMap pageWordCounts = new HashMap();

            Object jobIdObject = m_request
                    .getParameter(JobManagementHandler.JOB_ID);

            Long jobIdAsLong;
            if (jobIdObject instanceof Long)
            {
                jobIdAsLong = (Long) jobIdObject;
            }
            else
            {
                jobIdAsLong = Long.parseLong(jobIdObject.toString());
            }

            long jobId = jobIdAsLong.longValue();

            Job job = WorkflowHandlerHelper.getJobById(jobId);

            try
            {
                int numOfPages = Integer
                        .parseInt(m_request
                                .getParameter(JobManagementHandler.NUM_OF_PAGES_IN_JOB));
                Integer clearOverride = new Integer(-1);
                for (int i = 0; i < numOfPages; i++)
                {
                    if (m_request
                            .getParameter(JobManagementHandler.SOURCE_PAGE_ID
                                    + i) != null)
                    {
                        Long pageId = new Long(
                                m_request
                                        .getParameter(JobManagementHandler.SOURCE_PAGE_ID
                                                + i));
                        if (m_request
                                .getParameter(JobManagementHandler.WORDCOUNT
                                        + i) != null)
                        {
                            String countAsString = m_request
                                    .getParameter(JobManagementHandler.WORDCOUNT
                                            + i);
                            Integer wordCount = new Integer(countAsString
                                    .trim());
                            pageWordCounts.put(pageId, wordCount);
                        }
                        else
                        // is an override
                        {
                            if (m_request
                                    .getParameterValues(JobManagementHandler.REMOVE_OVERRIDE
                                            + i)[0].equals("yes"))
                            {
                                // clear the override
                                pageWordCounts.put(pageId, clearOverride);
                            }
                        }
                    }
                }

                if (m_request
                        .getParameterValues(JobManagementHandler.REMOVE_TOTAL_WC_OVERRIDEN) != null
                        && m_request
                                .getParameterValues(JobManagementHandler.REMOVE_TOTAL_WC_OVERRIDEN)[0]
                                .equals("yes"))
                {
                    job = ServerProxy.getJobHandler().clearOverridenWordCount(
                            job);
                }
                else
                {
                    String totalAsString = m_request
                            .getParameterValues(JobManagementHandler.TOTAL_SOURCE_PAGE_WC)[0];
                    int totalWordCount = Integer.parseInt(totalAsString.trim());
                    
                    // if the word count is different - save
                    if (totalWordCount != job.getWordCount())
                    {
                        job = ServerProxy.getJobHandler().overrideWordCount(
                                job, totalWordCount);
                    }
                }

                PageManager pm = ServerProxy.getPageManager();
                pm.updateWordCount(pageWordCounts);
            }
            catch (Exception e)
            {
                CATEGORY.error("Failed to save the word count(s) for page(s) "
                        + pageWordCounts.toString());
                throw new EnvoyServletException(
                        EnvoyServletException.MSG_FAILED_TO_UPDATE_WORD_COUNT,
                        null, e);
            }

            sessionMgr.removeElement(WebAppConstants.SOURCE_PAGE);
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
