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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.projecthandler.WfTemplateSearchParameters;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class WorkflowTemplateSearchHandler extends PageHandler
{
    
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
        SessionManager sessionMgr =
                (SessionManager) session.getAttribute(SESSION_MANAGER);
        Locale uiLocale = 
            (Locale)session.getAttribute(WebAppConstants.UILOCALE);

        // Get data needed for search page
        p_request.setAttribute(WorkflowTemplateConstants.SOURCE_LOCALES,
                 WorkflowTemplateHandlerHelper.getAllSourceLocales(uiLocale));
        p_request.setAttribute(WorkflowTemplateConstants.TARGET_LOCALES,
                 WorkflowTemplateHandlerHelper.getAllTargetLocales(uiLocale));
        // If not admin, get only the projects for that user (PM).
        List projectInfos;

        if (perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
        {
            projectInfos = 
                WorkflowTemplateHandlerHelper.getAllProjectInfos(uiLocale);
        }
        else
        {
            String userName = (String)session.getAttribute(
                                            WebAppConstants.USER_NAME);
            User user = UserHandlerHelper.getUser(userName);
            projectInfos = 
                WorkflowTemplateHandlerHelper.getAllProjectInfosForUser(user,
                                                                    uiLocale);
        }

        p_request.setAttribute(WorkflowTemplateConstants.PROJECTS, projectInfos);


        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

}
