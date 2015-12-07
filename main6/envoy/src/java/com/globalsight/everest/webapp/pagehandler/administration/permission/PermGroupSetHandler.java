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
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

/**
 * Pagehandler for the new & edit Permission Set pages.
 */
public class PermGroupSetHandler extends PageHandler
{
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        String action = p_request.getParameter("action");

        try
        {
            PermissionGroup permGroup = getPermissionGroup(p_request, sessionMgr);
            String permissionXml = Permission.getPermissionXml();//getPermissionXml(sessionMgr);

            if ("prev".equals(action))
            {
                // Save data from users page
                PermissionHelper.saveUsers(permGroup, p_request);
            }
            else
            {
                // Save data from basic info page 
                PermissionHelper.saveBasicInfo(permGroup, p_request);
            }
            
            // Get data for page 2 
            PermissionHelper.setPermissionXmlInSession(sessionMgr, session,
                         permGroup.getPermissionSet(), permissionXml);
            sessionMgr.setAttribute("permGroupName", permGroup.getName());
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    private PermissionGroup getPermissionGroup(HttpServletRequest request,
                             SessionManager sessionMgr)
    {
        PermissionGroupImpl permGroup = (PermissionGroupImpl)
                    sessionMgr.getAttribute("permGroup");
        if (permGroup == null)
        {
            permGroup = new PermissionGroupImpl();
            sessionMgr.setAttribute("permGroup", permGroup);
        }
        return permGroup;
    }
}

