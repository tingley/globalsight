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
package com.globalsight.everest.webapp.pagehandler.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.webnavigation.WebSiteDescription;

public class WelcomeHandler
    extends PageHandler
{

    public WelcomeHandler()
    {
    }

    /**
     * Invokes this PageHandler
     *
     * @param jspURL the URL of the JSP to be invoked
     * @param the original request sent from the browser
     * @param the original response object
     * @param context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context) throws ServletException, IOException
    {
        HttpSession userSession = p_request.getSession();
        String userId = (String) userSession.getAttribute(WebAppConstants.USER_NAME);
        boolean isSuperPm = false;
        try {
            isSuperPm = ServerProxy.getUserManager()
                .containsPermissionGroup(userId, WebAppConstants.SUPER_PM_NAME);
        } catch (Exception e) {
            throw new ServletException(e);
        } 
        
        if (isSuperPm)
        {
            String companyName = (String) p_request.getParameter(WebAppConstants.COMPANY_NAME);
            if (UserUtil.isBlank(companyName))
            {
                String superCompanyName = null;
                String [] companyNameStrings = null;
                try {
                    superCompanyName = ServerProxy.getJobHandler().
                        getCompanyById(Long.parseLong(CompanyWrapper.SUPER_COMPANY_ID)).getCompanyName();
                    companyNameStrings = CompanyWrapper.getAllCompanyNames();
                } catch (Exception e) {
                    throw new ServletException(e);
                } 
                
                ArrayList companyNames = new ArrayList(companyNameStrings.length);
                for (int i = 0; i < companyNameStrings.length; i++)
                {
                    if (superCompanyName.equals(companyNameStrings[i]))
                    {
                        continue;
                    }
                    companyNames.add(companyNameStrings[i]);
                }
                Collections.sort(companyNames);
                
                p_request.setAttribute(WebAppConstants.COMPANY_NAMES, companyNames);
            }
            else 
            {
                userSession.setAttribute(WebAppConstants.SELECTED_COMPANY_NAME_FOR_SUPER_PM, companyName);
                CompanyThreadLocal.getInstance().setValue(companyName);
            }
        } 
        else {
            p_pageDescriptor = WebSiteDescription.instance()
                .getPageDescriptor(WebAppConstants.LOG4_PAGE_NAME);
        }
        
        // For "amb-118 logon tasks directly from notification"
        String loginFrom = p_request.getParameter(WebAppConstants.LOGIN_FROM);
        if(loginFrom != null && loginFrom.equals(WebAppConstants.LOGIN_FROM_EMAIL))
        {
        	String forwardUrl = p_request.getParameter("forwardUrl");
        	if(forwardUrl != null && !"".equals(forwardUrl))
        	{
                // GBS-2343: If there is no pageName in forwardUrl, the old
                // pageName LOG1 will be reused, and we will end up looping.
                if (forwardUrl.indexOf("pageName=") == -1)
                {
                    throw new ServletException(
                        "forwardUrl does not contain a pageName parameter (corrupt URL?)");
                }
        		RequestDispatcher dispatcher = p_request.getRequestDispatcher(forwardUrl);
        		dispatcher.forward(p_request, p_response);
        		return;
        	}
        }
        // for GBS-1302, view activity dashboard.
        PermissionSet userPerms = (PermissionSet) userSession
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (userPerms.getPermissionFor(Permission.ACTIVITY_DASHBOARD_VIEW))
        {
            Map<String, Long> map = TaskInterimPersistenceAccessor
                    .getTasksForDashboard(userId);
            p_request.setAttribute(WebAppConstants.DASHBOARD_ACTIVITY, map);
        }
        if (!TaskInterimPersistenceAccessor.isTriggered(userId))
        {
            // Migrate the user's existing tasks to TASK_INTERIM table.
            // Only invoked once when this user login GS first time after this
            // change. Use a new thread to make this run in background.
            final String uid = userId;
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    TaskInterimPersistenceAccessor.initializeUserTasks(uid);
                }
            };
            Thread t = new MultiCompanySupportedThread(runnable);
            t.start();
        }
        try {
            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
        } catch (EnvoyServletException e) {
            throw new ServletException(e);
        } 
    }

    /**
     * Invoke the correct JSP for this page
     */
    private void dispatchJSP(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context) throws ServletException, IOException
    {
        //invoke JSP
        RequestDispatcher dispatcher =
            p_context.getRequestDispatcher(p_pageDescriptor.getJspURL());

        dispatcher.forward(p_request, p_response);
    }
}



