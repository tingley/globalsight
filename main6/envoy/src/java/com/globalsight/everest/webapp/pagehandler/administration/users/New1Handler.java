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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.SortUtil;

public class New1Handler extends PageHandler
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
        String action = (String) request.getParameter("action");
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        if ("new".equals(action))
        {
            User user = (User) sessionMgr.getAttribute(USER);
            CreateUserWrapper wrapper = UserUtil.createCreateUserWrapper(user);
            sessionMgr.setAttribute(UserConstants.CREATE_USER_WRAPPER, wrapper);
            sessionMgr.setAttribute("securitiesHash", UserHandlerHelper
                    .getSecurity(wrapper.getUser(), user, true));
            String[] companies = UserHandlerHelper.getCompanyNames();
            // Fix for GBS-1693
            List<String> companyList = Arrays.asList(companies);
            SortUtil.sort(companyList,
                    new StringComparator(Locale.getDefault()));
            sessionMgr.setAttribute("companyNames", companyList.toArray());
        }
        else if ("previous".equals(action))
        {
            // Save the data from the base user page
            CreateUserWrapper wrapper = (CreateUserWrapper) sessionMgr
                    .getAttribute(UserConstants.CREATE_USER_WRAPPER);
            User user = (User) sessionMgr.getAttribute(USER);
            if (wrapper == null)
            {
                wrapper = UserUtil.createCreateUserWrapper(user);
            }
            UserUtil.extractContactInfoData(request, wrapper);

            // If user is super admin, then clear addedProjectIds in wrapper
            if (UserUtil.isSuperAdmin(user.getUserId()))
            {
                List addedProjectIds = (List) wrapper.getProjects();
                if (addedProjectIds != null && addedProjectIds.size() > 0)
                {
                    addedProjectIds.clear();
                }

            }
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

}
