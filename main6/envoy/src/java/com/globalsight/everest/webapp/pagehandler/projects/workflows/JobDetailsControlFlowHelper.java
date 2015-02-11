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

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.GeneralException;

// java
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Dispatches the user to the correct JSP.
 */
class JobDetailsControlFlowHelper implements ControlFlowHelper, WebAppConstants
{
    // local variables
    private HttpServletRequest m_request = null;

    private HttpServletResponse m_response = null;

    public JobDetailsControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    /**
     * Does the processing then returns the name of the link to follow
     * 
     * @return
     * @exception EnvoyServletException
     */
    public String determineLinkToFollow() throws EnvoyServletException
    {

        HttpSession session = m_request.getSession(false);
        String destinationPage = null;

        // If no workflows, go back to appropriate jobs page
        if (m_request.getParameter("lastWF") != null)
        {
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);
            destinationPage = (String) sessionMgr
                    .getAttribute("destinationPage");
        }
        else
        {
            destinationPage = m_request.getParameter("linkName");
        }
        return destinationPage;
    }

}
