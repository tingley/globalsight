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
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionGroupComparator;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.SortUtil;

/**
 * Pagehandler for adding/removing permission groups to/from a user.
 */
public class UserPermissionHandler extends PageHandler
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
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String action = (String) request.getParameter("action");
        CreateUserWrapper wrapper = null;

        if ("perms".equals(action))
        {
            // We're here from MOD1, and we need to save the basic data from
            // the user.
            sessionMgr.setAttribute("edit", "true");
            wrapper = (ModifyUserWrapper) sessionMgr
                    .getAttribute(UserConstants.MODIFY_USER_WRAPPER);
            UserUtil.extractUserData(request, wrapper, false);

        }
        else if ("next".equals(action))
        {
            // Get the data from the last page (security)
            wrapper = (CreateUserWrapper) sessionMgr
                    .getAttribute(UserConstants.CREATE_USER_WRAPPER);
            // Get the data from the last page (security page)
            FieldSecurity fs = (FieldSecurity) sessionMgr
                    .getAttribute("fieldSecurity");
            UserUtil.extractSecurity(fs, request);
            wrapper.setFieldSecurity(fs);
        }

        Boolean isCurrentUserSuperUser = Boolean.FALSE;
        if (CompanyWrapper.getSuperCompanyName().equalsIgnoreCase(
                wrapper.getCompanyName()))
        {
            isCurrentUserSuperUser = Boolean.TRUE;
        }
        else
        {
            isCurrentUserSuperUser = Boolean.FALSE;
        }
        request.setAttribute("isCurrentUserSuperUser", isCurrentUserSuperUser);

        String companyId = null;
        try
        {
            companyId = CompanyWrapper.getCompanyIdByName(wrapper
                    .getCompanyName());
        }
        catch (PersistenceException e)
        {
            throw new EnvoyServletException(e);
        }

        // Get data for page
        ArrayList allPerms = (ArrayList) PermissionHelper
                .getAllPermissionGroupsUserCanAssign(companyId, perms);
        SortUtil.sort(allPerms,
                new PermissionGroupComparator(Locale.getDefault()));
        request.setAttribute("allPerms", allPerms);
        ArrayList userPerms = (ArrayList) sessionMgr.getAttribute("userPerms");
        if (userPerms == null)
        {
            userPerms = (ArrayList) PermissionHelper
                    .getAllPermissionGroupsForUser(wrapper.getUserId());
            SortUtil.sort(userPerms,
                    new PermissionGroupComparator(Locale.getDefault()));
            sessionMgr.setAttribute("userPerms", userPerms);
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

}
