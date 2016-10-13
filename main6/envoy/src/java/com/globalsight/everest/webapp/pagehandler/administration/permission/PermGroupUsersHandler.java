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
package com.globalsight.everest.webapp.pagehandler.administration.permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionGroupImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;

/**
 * Pagehandler for the new & edit Permission Users pages.
 */
public class PermGroupUsersHandler extends PageHandler
{
    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page desciptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        Locale locale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        String action = p_request.getParameter("action");

        try
        {
            PermissionGroup permGroup = getPermissionGroup(p_request,
                    sessionMgr);

            if ("users".equals(action))
            {
                // Save data from basic page
                PermissionHelper.saveBasicInfo(permGroup, p_request);
            }
            else if ("next".equals(action))
            {
                // Save data from permission set page
                PermissionHelper.savePermissionSet(permGroup, p_request);
                FormUtil.addSubmitToken(p_request,
                        FormUtil.Forms.NEW_PERMISSION_GROUP);
            }
            // Get data for users page
            sessionMgr.setAttribute("allUsers", getAvailableUsers(locale));
            sessionMgr.setAttribute("usersForPermGroup",
                    getUsers(locale, sessionMgr, permGroup));
            sessionMgr.setAttribute("permGroupName", permGroup.getName());

        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private PermissionGroup getPermissionGroup(HttpServletRequest request,
            SessionManager sessionMgr)
    {
        PermissionGroupImpl permGroup = (PermissionGroupImpl) sessionMgr
                .getAttribute("permGroup");
        if (permGroup == null)
        {
            permGroup = new PermissionGroupImpl();
            sessionMgr.setAttribute("permGroup", permGroup);
        }
        return permGroup;
    }

    private ArrayList getUsers(Locale locale, SessionManager sessionMgr,
            PermissionGroup permGroup) throws EnvoyServletException
    {
        ArrayList users = (ArrayList) sessionMgr.getAttribute("usersForGroup");
        if (users != null)
            return users;

        users = (ArrayList) PermissionHelper
                .getAllUsersForPermissionGroup(permGroup.getId());
        UserComparator userComparator = new UserComparator(
                UserComparator.DISPLAYNAME, locale);
        SortUtil.sort(users, userComparator);
        return users;
    }

    private Vector getAvailableUsers(Locale locale)
            throws EnvoyServletException
    {
        Vector users = null;
        try
        {
            users = UserHandlerHelper.getUsersForCurrentCompany();
            UserComparator userComparator = new UserComparator(
                    UserComparator.DISPLAYNAME, locale);
            SortUtil.sort(users, userComparator);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        return users;
    }
}
