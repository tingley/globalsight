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
package com.globalsight.everest.webapp.pagehandler.administration.projects;

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.util.comparator.UserInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FormUtil;
import com.globalsight.util.SortUtil;

public class UsersProjectHandler extends PageHandler
{
    private static final int CREATE_NEW_PROJECT = 1;
    private static final int MODIFY_EXISTING_PROJECT = 2;
    private static final int REMOVE_EXISTING_PROJECT = 3;

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
        if (request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
        {
            response.sendRedirect("/globalsight/ControlServlet?activityName=projects");
            return;
        }
        HttpSession session = request.getSession(false);
        try
        {
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);

            // save data from first page
            Project project = (Project) sessionMgr.getAttribute("project");
            if (project == null)
            {
                project = ProjectHandlerHelper.createProject();
            }
            setData((ProjectImpl) project, request, sessionMgr);
            sessionMgr.setAttribute("project", project);

            // Get default users
            ArrayList defUsers = (ArrayList) ProjectHandlerHelper
                    .getPossibleUsersForProject(project.getProjectManager());

            // loop through and make three lists. The first list is all
            // the Users that are available for all projects and cannot
            // be removed from a project. They are added to a project by
            // default. The second list is users that can be added
            // and removed from projects. The third list is the list of
            // added users that weren't added by default.
            ArrayList possibleUsers = new ArrayList();
            Set addedUsers = new TreeSet(project.getUserIds());

            for (int i = 0; i < defUsers.size(); i++)
            {
                UserInfo userInfo = (UserInfo) defUsers.get(i);
                if (userInfo.isInAllProjects())
                {
                    // It's added by default, remove it from added list
                    addedUsers.remove(userInfo.getUserId());
                }
                else
                {
                    possibleUsers.add(userInfo);
                    defUsers.remove(i--);
                }
            }

            // If we got here via a sort, then addedUsers may not be correct.
            // Check request for hidden field "toField". If it's set, use
            // that list rather than the addedUsers just calculated.
            String toField = (String) request.getParameter("toField");
            if (toField != null)
            {
                String[] userids = toField.split(",");
                addedUsers = new TreeSet();
                for (int i = 0; i < userids.length; i++)
                {
                    addedUsers.add(userids[i]);
                }
            }
            else
            {
                toField = new String();
                Iterator iter = addedUsers.iterator();
                boolean first = true;
                while (iter.hasNext())
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        toField += ",";
                    }
                    toField += iter.next();
                }
            }

            ArrayList<String> addedUsersIds = new ArrayList<String>();
            Iterator it = addedUsers.iterator();
            while (it.hasNext())
            {
                addedUsersIds.add((String) it.next());
            }

            // fix for GBS-1693
            SortUtil.sort(defUsers, new UserInfoComparator(Locale.getDefault()));
            SortUtil.sort(possibleUsers,
                    new UserInfoComparator(Locale.getDefault()));
            SortUtil.sort(addedUsersIds,
                    new StringComparator(Locale.getDefault()));

            request.setAttribute("toField", toField);
            request.setAttribute("addedUsersIds", addedUsersIds);
            Locale locale = (Locale) session
                    .getAttribute(WebAppConstants.UILOCALE);
            setTableNavigation(request, session, defUsers,
                    new UserInfoComparator(locale), 10, "numPerPage",
                    "numPages", "defUsers", "sorting", "reverseSort",
                    "pageNum", "lastPageNum", "listSize");
            sessionMgr.setAttribute("defUsers", defUsers);

            // Set possible users
            sessionMgr.setAttribute("possibleUsers", possibleUsers);

            FormUtil.addSubmitToken(request, FormUtil.Forms.NEW_PROJECT);

            // Determine all added users.
            // Filter out ones that are added by default because they
            // are part of all projects.
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private void setData(ProjectImpl p_project, HttpServletRequest p_request,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {
        // If just doing a sort, don't set fields
        String linkName = (String) p_request.getParameter("linkName");
        if ("self".equals(linkName))
            return;

        ProjectHandlerHelper.setData(p_project, p_request, false);

        p_project.setCompanyId(Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue()));
        String pmName = (String) p_sessionMgr.getAttribute("pmId");
        if (pmName == null)
        {
            pmName = (String) p_request.getParameter("pmField");
        }
        if (pmName != null)
        {
            p_project.setProjectManager(ProjectHandlerHelper.getUser(pmName));
        }
    }
}