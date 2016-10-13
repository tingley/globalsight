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
package com.globalsight.everest.webapp.pagehandler.administration.tmprofile;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * TMProfileControlFlowHelper, A page flow helper that 
 * checks wfti dependencies and then dispatches the user
 * to the correct JSP.
 */
class TMProfileControlFlowHelper
    implements ControlFlowHelper, WebAppConstants, TMProfileConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TMProfileControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public TMProfileControlFlowHelper(HttpServletRequest p_request,
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
        if (m_request.getParameter(ACTION).equals(NEW_ACTION)) 
        {
            destinationPage = "new1";
        }
        else if (m_request.getParameter(ACTION).equals(EDIT_ACTION)) 
        {
            destinationPage = "modify";
        }
        else if (m_request.getParameter(ACTION).equals(CANCEL_ACTION)) 
        {
            // clean session manager
            clearSessionManager(p_session);
            destinationPage = "self";
        }
        else if (m_request.getParameter(ACTION) == null || m_request.getParameter(ACTION).length() == 0)
        {
            destinationPage = "self";
        }
        CATEGORY.info("The value of destinationPage is " + destinationPage);
        return destinationPage;
    }

    /**
     * Clear the session manager
     * 
     * @param p_session
     */
    private void clearSessionManager(HttpSession p_session)
    {
        SessionManager sessionMgr = 
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);
        sessionMgr.clear();        
    }
}
