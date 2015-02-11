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
package com.globalsight.everest.webapp.pagehandler.tasks;

import org.apache.log4j.Logger;


import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Dispatches the user to the correct JSP.
 */
class TaskSearchControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TaskSearchControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public TaskSearchControlFlowHelper(HttpServletRequest p_request,
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
        HttpSession p_session = m_request.getSession(false);
        String destinationPage = null;
        String action = (String) m_request.getParameter("action");

        // action should only be null for paging purposes
        if (action == null || action.equals("search") )
        {
            destinationPage = (String)m_request.getParameter("linkName");
        }
        else if (action.equals(JobSearchConstants.TASK_SEARCH_COOKIE) ||
                 action.equals(JobSearchConstants.MINI_TASK_SEARCH_COOKIE))
        {  
            destinationPage = "list";
        }
        else if (action.equals("goToSearch"))
        {
            destinationPage = "taskSearch";
        }
        else if (action.equals("one"))
        {
            destinationPage = "wordcountlist";
        }
        else if (action.equals("wclist"))
        {
            destinationPage = "wordcountlist";
        }
        return destinationPage;
    }
}

