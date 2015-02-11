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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.RoleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;


public class UserDetailsHandler extends PageHandler
            implements UserConstants
{
    
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
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        User user = setUser(sessionMgr, request);
        setRoles(request, session, user);
        setProjects(request, session, user);
        setPermissionGroups(request, session, user);
        User loggedInUser = (User)sessionMgr.getAttribute(WebAppConstants.USER);
        FieldSecurity securitiesHash =
            UserHandlerHelper.getSecurity(user, loggedInUser, true);
        sessionMgr.setAttribute("security", securitiesHash);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    private User setUser(SessionManager sessionMgr, HttpServletRequest request)
    throws EnvoyServletException 
    {
        String id = (String)request.getParameter("radioBtn");
        User user = null;
        if (id != null)
        {
            user = UserHandlerHelper.getUser(id);
            sessionMgr.setAttribute("user", user);
            return user;
        }
        else
        {
            return (User) sessionMgr.getAttribute("user");
        }
    }

    private void setRoles(HttpServletRequest request,
                                   HttpSession session,
                                   User user)
    throws EnvoyServletException
    {
        Collection roles = UserHandlerHelper.getUserRoles(user);
        ArrayList rolesList;
        if (roles == null)
            rolesList = new ArrayList();
        else
            rolesList = new ArrayList(roles);

        Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(request, session, rolesList,
                           new RoleComparator(uiLocale),
                           10,
                           UserConstants.ROLE_LIST,
                           UserConstants.ROLE_KEY);
    }

    private void setProjects(HttpServletRequest request,
                                   HttpSession session,
                                   User user)
    throws EnvoyServletException
    {
        ArrayList projects = (ArrayList)UserHandlerHelper.getProjectsByUser(user.getUserId());
        Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(request, session, projects,
                           new ProjectComparator(uiLocale),
                           10,
                           UserConstants.PROJECT_LIST,
                           UserConstants.PROJECT_KEY);
    }
    private void setPermissionGroups(HttpServletRequest request,
                                   HttpSession session,
                                   User user)
    throws EnvoyServletException
    {
        String[] pg = UserHandlerHelper.getAllPermissionGroupNamesForUser(user.getUserId());
        StringBuffer vendorPermissions = new StringBuffer();
        StringBuffer ambassadorPermissions = new StringBuffer();
        boolean vendorGroupFound = false;
        boolean ambGroupFound = false;
        for(int i=0; i<pg.length; i++)
        {
            String groupName = (String)pg[i];
            if (groupName.startsWith("Vendor"))
            {
                if(vendorGroupFound)
                {
                    vendorPermissions.append(" | ");
                }
                vendorPermissions.append(groupName);
                vendorGroupFound = true;
            }
            else
            {
                if(ambGroupFound)
                {
                    ambassadorPermissions.append(" | ");
                }
                ambassadorPermissions.append(groupName);
                ambGroupFound = true;
            }
        }
        request.setAttribute("vendorGroup", vendorPermissions.toString());
        request.setAttribute("ambGroup", ambassadorPermissions.toString());
    }
}

