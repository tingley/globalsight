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

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.calendar.CalendarManagerLocal;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

public class NewContactInfoHandler extends PageHandler
{

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String action = (String) request.getParameter("action");
        if (CalendarConstants.PREVIOUS_ACTION.equals(action))
        {
            saveCalData(session, request);
        }
        else if ("next".equals(action))
        {
            CreateUserWrapper wrapper = (CreateUserWrapper) sessionMgr
                    .getAttribute(UserConstants.CREATE_USER_WRAPPER);

            // Save data from previous page
            UserUtil.extractUserData(request, wrapper);
            if (request.getParameter(UserStateConstants.USER_GROUPS) != null)
                sessionMgr.setAttribute(UserStateConstants.USER_GROUPS, request
                        .getParameter(UserStateConstants.USER_GROUPS));
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);

    }

    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        return new New1ControlFlowHelper(p_request, p_response);
    }

    /**
     * Save the calendar data to the session
     */
    private void saveCalData(HttpSession session, HttpServletRequest request)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        CreateUserWrapper wrapper = (CreateUserWrapper) sessionMgr
                .getAttribute(UserConstants.CREATE_USER_WRAPPER);

        if (CalendarManagerLocal.isInstalled())
        {
            UserUtil.extractCalendarData(request, wrapper.getUserId());
        }
    }
}
