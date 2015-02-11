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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;

public class JobSearchHandlerHelper 
{
    /**
     * Get data needed for the job search page.
     * Default values for fields are based on the last search done.
     */
    static public void setupForSearch(HttpServletRequest request)
    throws ServletException, IOException, RemoteException, EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
        SessionManager sessionMgr = (SessionManager)
            session.getAttribute(WebAppConstants.SESSION_MANAGER);
        // remove any data in session relating to paging or sorting
        sessionMgr.removeElement("jobListStart");
        sessionMgr.removeElement("sort");

        String userName = (String)session.getAttribute(WebAppConstants.USER_NAME);
        User user = UserHandlerHelper.getUser(userName);
        JobSearchHandlerHelper.setSearchCriteria(request, sessionMgr, user.getUserId());

        // Get locales 
        Locale uiLocale =
            (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        List srcLocales = WorkflowTemplateHandlerHelper.getAllSourceLocales(uiLocale);
        request.setAttribute("srcLocales", srcLocales);
        List targLocales = WorkflowTemplateHandlerHelper.getAllTargetLocales(uiLocale);
        request.setAttribute("targLocales", targLocales);

        // Get only the projects for that user unless it's the admin
        List projectInfos = null;
        if (perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
        {
            projectInfos = WorkflowTemplateHandlerHelper
                    .getAllProjectInfos(uiLocale);
        }
        else if (perms.getPermissionFor(Permission.GET_PROJECTS_I_MANAGE))
        {
            projectInfos = WorkflowTemplateHandlerHelper
                    .getAllProjectInfosForUser(user, uiLocale);
        }
        else // for WFM and other VALID future access group (i.e. JobManager)
        {
            projectInfos = WorkflowTemplateHandlerHelper.getProjectInfosByUser(
                    userName, uiLocale);
        }

        request.setAttribute("projects", projectInfos);
    }

    static public Cookie getJobSearchCookie(HttpSession session,
                                 HttpServletRequest request)
    {
        String searchType = request.getParameter("searchType");
        if (searchType == null)
            searchType = (String) session.getAttribute("searchType");
        return getJobSearchCookie(session, request, searchType);
    }

    /**
     * Return the job search cookie for this user. First look in the request.
     * Then look in the session.
     * If not there, look in the filesystem.
     */
    static public Cookie getJobSearchCookie(HttpSession session,
                                 HttpServletRequest request, String searchType)
    {
        SessionManager sessionMgr = (SessionManager)
                session.getAttribute(WebAppConstants.SESSION_MANAGER);

        if (searchType != null && (searchType.equals("lastSearch") || 
            searchType.equals("stateOnly")))
        {
            searchType = (String)session.getAttribute(JobSearchConstants.LAST_JOB_SEARCH_TYPE);
        }
            
        String userId = (String)session.getAttribute(WebAppConstants.USER_NAME);
        User user = null;
        try
        {
            user = UserHandlerHelper.getUser(userId);
        }
        catch (EnvoyServletException e)
        {
            return null;
        }
        String cookieName = searchType + user.getUserId().hashCode();
        Cookie jobSearchCookie = (Cookie) sessionMgr.getAttribute(cookieName);
        if (jobSearchCookie != null)
            return  jobSearchCookie;

        Cookie[] cookies = (Cookie[])request.getCookies();
        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i ++)
            {
                Cookie cookie = (Cookie)cookies[i];
                if (cookie.getName().equals(cookieName))
                {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * Set the previously search criteria in the session if not already there.
     * If not already tehre, get it from a cookie.
     * If not in a cookie, then it must be the first time
     * the user has issued a job search. (or they removed the cookie from the fs)
     */
    static public void setSearchCriteria(HttpServletRequest request,
                                   SessionManager sessionMgr,
                                   String userId)
    {
        String cookieName = JobSearchConstants.JOB_SEARCH_COOKIE + userId.hashCode();
        Cookie jobSearchCookie = (Cookie) sessionMgr.getAttribute(cookieName);
        if (jobSearchCookie == null)
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

        cookieName = JobSearchConstants.MINI_JOB_SEARCH_COOKIE + userId.hashCode();
        Cookie miniJobSearchCookie = (Cookie)sessionMgr.getAttribute(cookieName);
        if (miniJobSearchCookie == null)
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
