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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import java.rmi.RemoteException;

// globalsight
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.servlet.util.SessionManager;

public class TaskSearchHandler extends PageHandler
{

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * @param pageDescriptor the description of the page to be produced.
     * @param request original request sent from the browser.
     * @param response original response object.
     * @param context the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
    throws ServletException, IOException, RemoteException, EnvoyServletException
    {
        String action = (String)request.getParameter("action");
        setup(request);
        
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {

        return new TaskSearchControlFlowHelper(p_request, p_response);
    }

    /**
     * Get data needed for the job search page.
     * Default values for fields are based on the last search done.
     */
    static public void setup(HttpServletRequest request)
    throws ServletException, IOException, RemoteException, EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager)
            session.getAttribute(WebAppConstants.SESSION_MANAGER);
        String userName = (String)session.getAttribute(WebAppConstants.USER_NAME);
        User user = UserHandlerHelper.getUser(userName);

        setSearchCriteria(request, sessionMgr, user.getUserId());

        // Get locales 
        Locale uiLocale =
            (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        List srcLocales = WorkflowTemplateHandlerHelper.getAllSourceLocales(uiLocale);
        request.setAttribute("srcLocales", srcLocales);
        List targLocales = WorkflowTemplateHandlerHelper.getAllTargetLocales(uiLocale);
        request.setAttribute("targLocales", targLocales);
    }

    /**
     * Get the previously set search criteria.  If it's not in the session,
     * get it from a cookie.  If not in a cookie, then it must be the first time
     * the user has issued a task search. (or they removed the cookie from the fs)
     */
    static private void setSearchCriteria(HttpServletRequest request,
                                   SessionManager sessionMgr,
                                   String userId)
    {
        String cookieName = JobSearchConstants.TASK_SEARCH_COOKIE + userId.hashCode();
        Cookie taskSearchCookie = (Cookie)sessionMgr.getAttribute(cookieName);
        if (taskSearchCookie == null)
        {
            Cookie[] cookies = (Cookie[])request.getCookies();
            if (cookies != null)
            {
                for (int i = 0; i < cookies.length; i ++)
                {   
                    Cookie cookie = (Cookie)cookies[i];
                    if (cookie.getName().equals(cookieName))
                    {
                        sessionMgr.setAttribute(cookieName, cookie);
                        break;
                    }
                }
            }
        }
        cookieName = JobSearchConstants.MINI_TASK_SEARCH_COOKIE + userId.hashCode();
        Cookie miniTaskSearchCookie = (Cookie)sessionMgr.getAttribute(cookieName);
        if (miniTaskSearchCookie == null)
        {
            Cookie[] cookies = (Cookie[])request.getCookies();
            if (cookies != null)
            {
                for (int i = 0; i < cookies.length; i ++)
                {   
                    Cookie cookie = (Cookie)cookies[i];
                    if (cookie.getName().equals(cookieName))
                    {
                        sessionMgr.setAttribute(cookieName, cookie);
                        break;
                    }
                }
            }
        }
    }
}
