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

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionGroupImpl;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Pagehandler for the details page
 */
public class PermGroupDetailsHandler extends PageHandler
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

        String id = (String)p_request.getParameter("radioBtn");
        if (id == null || p_request.getMethod().equalsIgnoreCase("get"))
        {
            p_response.sendRedirect("/globalsight/ControlServlet?activityName=permissions");
            return;
        }
        
        try {
            PermissionGroup permGroup =
                    Permission.getPermissionManager().readPermissionGroup(
                    Long.parseLong(id));
            sessionMgr.setAttribute("permGroup", permGroup);


            PermissionHelper.setPermissionXmlInSession(sessionMgr, session,
                     permGroup.getPermissionSet(), Permission.getPermissionXml());
            sessionMgr.setAttribute("permGroupName", permGroup.getName());
    
            sessionMgr.setAttribute("users",
                PermissionHelper.getAllUsersForPermissionGroup(permGroup.getId()));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

}

