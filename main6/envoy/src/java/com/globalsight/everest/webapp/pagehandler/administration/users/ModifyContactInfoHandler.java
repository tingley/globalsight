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
package com.globalsight.everest.webapp.pagehandler.administration.users;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class ModifyContactInfoHandler extends PageHandler
{
    
    public ModifyContactInfoHandler()
    {
    }

    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        
        String action = (String) request.getParameter(USER_ACTION);
        if (USER_ACTION_MODIFY_USER_CONTACT.equals(action))
        {
            setUpEdit(request, session);
        }
        else if (CANCEL.equals(action))
        {
            // canceling reserved times
            cancel(request, session);
        }
        request.setAttribute("fromUserEdit", "1");

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }


    /**
     *  Set up edit of contact info
     */
    private void setUpEdit(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        ModifyUserWrapper wrapper = (ModifyUserWrapper)
            sessionMgr.getAttribute(MODIFY_USER_WRAPPER);

        // Save data from previous page
        UserUtil.extractUserData(request, wrapper, false);

        // Set edit flag
        sessionMgr.setAttribute("editUser", "true");
    }
    

    private void cancel(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        ModifyUserWrapper wrapper = (ModifyUserWrapper)
            sessionMgr.getAttribute(MODIFY_USER_WRAPPER);
    }

    /**
     * Clear the session manager
     *
     * @param session - The client's HttpSession where the
     * session manager is stored.
     */
    private void clearSessionManager(HttpSession session)
    {
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        sessionMgr.clear();
    }
}
