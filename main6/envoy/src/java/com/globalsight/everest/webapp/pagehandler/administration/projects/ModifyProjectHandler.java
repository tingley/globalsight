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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.projecthandler.ProjectInfo;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.comparator.UserInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.util.SortUtil;

public class ModifyProjectHandler extends PageHandler
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
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        Locale locale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);

        String action = request.getParameter("action");
        try
        {
            // Get list of term-base names
            List<String> termbases = getTermbaseNames(session);
            request.setAttribute("termbases", termbases);

            // save data from first page
            Project project = (Project) (sessionMgr.getAttribute("project") == null ? new ProjectImpl()
                    : sessionMgr.getAttribute("project"));
            List<ProjectInfo> projects = (List<ProjectInfo>) (sessionMgr
                    .getAttribute("projects") == null ? ProjectHandlerHelper
                    .getAllProjectsForGUI() : sessionMgr
                    .getAttribute("projects"));
            String linkName = (String) request.getParameter("linkName");
            // navigate default users page by page.
            if ("self".equals(linkName))
            {
                setData((ProjectImpl) project, request, sessionMgr);
                sessionMgr.setAttribute("project", project);
            }
            else if (action.equals("edit"))
            {
                String id = (String) request.getParameter(RADIO_BUTTON);
                if (id == null
                        || request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    response.sendRedirect("/globalsight/ControlServlet?activityName=projects");
                    return;
                }
                project = (Project) (sessionMgr.getAttribute("project") == null ? ProjectHandlerHelper
                        .getProjectById(Long.parseLong(id)) : sessionMgr
                        .getAttribute("project"));
                for (ProjectInfo pro : projects)
                {
                    if (pro.getName().equals(project.getName()))
                    {
                        projects.remove(pro);
                        break;
                    }
                }

                // If "Terminology Approval" option is "Yes", this file
                // profile's project must specify one termbase.
                setFileProfileTermData(request, project);
            }
            sessionMgr.setAttribute("project", project);
            sessionMgr.setAttribute("projects", projects);

            prepareUsersData(request, action, project, locale);

            request.setAttribute("allAttributeGroups",
                    AttributeManager.getAllAttributeSets());

            // If admin, need to get list of project managers.
            // If not, set project manager name in session.
            PermissionSet perms = (PermissionSet) session
                    .getAttribute(WebAppConstants.PERMISSIONS);
            if (perms.getPermissionFor(Permission.PROJECTS_EDIT_PM))
            {
                storeProjectManagers(request, locale);
                storeQuotePersons(request, locale);
            }
            else
            {
                User user = (User) sessionMgr
                        .getAttribute(WebAppConstants.USER);
                request.setAttribute("pm", user.getUserName());
                request.setAttribute("pmId", user.getUserId());
            }
        }
        catch (Exception e)// Config exception (already has message key...)
        {
            throw new EnvoyServletException(e);
        }

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /*
     * Stores the list of all people that can be quoted in projects.
     */
    private void storeQuotePersons(HttpServletRequest request, Locale p_locale)
            throws Exception
    {
        UserComparator userComparator = new UserComparator(
                UserComparator.DISPLAYNAME, p_locale);
        // Get quote email persons
        Vector<User> qePersons = ServerProxy.getUserManager().getUsers();
        Iterator<User> it = qePersons.iterator();
        // ignore super admin for quote person list.
        while (it.hasNext())
        {
            User u = (User) it.next();
            if (UserUtil.isSuperAdmin(u.getUserId()))
            {
                it.remove();
            }
        }
        SortUtil.sort(qePersons, userComparator);
        request.setAttribute("qePersons", qePersons);
    }

    /*
     * Stores the list of all people that can manage projects.
     */
    private void storeProjectManagers(HttpServletRequest request,
            Locale p_locale) throws Exception
    {
        long companyId = Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue());
        String companyName = ServerProxy.getJobHandler()
                .getCompanyById(companyId).getCompanyName();
        UserComparator userComparator = new UserComparator(
                UserComparator.DISPLAYNAME, p_locale);

        Collection<String> usernames = Permission.getPermissionManager()
                .getAllUsersWithPermission(Permission.PROJECTS_MANAGE);

        List<User> pms = new ArrayList<User>();
        for (String username : usernames)
        {
            try
            {
                User u = ServerProxy.getUserManager().getUser(username);
                if (companyName.equalsIgnoreCase(u.getCompanyName()))
                {
                    pms.add(u);
                }
                else if (UserUtil.isSuperPM(u.getUserId()))
                {
                    pms.add(u);
                }
            }
            catch (Exception ignore)
            {
            }
        }
        SortUtil.sort(pms, userComparator);
        request.setAttribute("pms", pms);
    }

    private void setData(ProjectImpl p_project, HttpServletRequest p_request,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {

        ProjectHandlerHelper.setData(p_project, p_request, false);

        p_project.setCompanyId(Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue()));
        String pmName = (String) p_sessionMgr.getAttribute("pmId");
        if (pmName == null)
        {
            pmName = (String) p_request.getParameter("pmField");
        }
        if (!"-1".equals(pmName) && StringUtils.isNotEmpty(pmName))
        {
            p_project.setProjectManager(ProjectHandlerHelper.getUser(pmName));
        }
    }

    private List<String> getTermbaseNames(HttpSession session)
    {
        String userId = getUser(session).getUserId();
        boolean isAdmin = UserUtil.isInPermissionGroup(userId,
                "Administrator");
        boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
        List<String> termbases = new ArrayList<String>();
        String currentCompanyId = CompanyThreadLocal.getInstance()
                .getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTBAccessControl = currentCompany
                .getEnableTBAccessControl();
        if (enableTBAccessControl)
        {
            if (isAdmin || isSuperAdmin)
            {
                termbases = TermbaseList.getNames();
            }
            else
            {
                ProjectTMTBUsers ptbUsers = new ProjectTMTBUsers();
                List termbaseIds = ptbUsers.getTList(userId, "TB");
                Iterator it = termbaseIds.iterator();
                while (it.hasNext())
                {
                    Termbase tb = TermbaseList.get(((BigInteger) it.next())
                            .longValue());
                    if (tb != null)
                    {
                        termbases.add(tb.getName());
                    }
                }
            }
        }
        else
        {
            termbases = TermbaseList.getNames();
        }

        return termbases;
    }

    /**
     * If "Terminology Approval" option is "Yes", this file profile's project
     * must specify one termbase.
     * 
     * @param request
     * @param project
     */
    private void setFileProfileTermData(HttpServletRequest request,
            Project project)
    {
        try
        {
            List<FileProfile> fps = ServerProxy.getProjectHandler()
                    .fileProfileListTerminology(project);
            request.setAttribute("fileProfileTermList", fps);
        }
        catch (Exception e)
        {
        }
    }

    private void prepareUsersData(HttpServletRequest request, String action,
            Project project, Locale locale)
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        List<UserInfo> defUsers = ProjectHandlerHelper
                .getPossibleUsersForProject(null);

        // loop through and make three lists. The first list is all
        // the Users that are available for all projects and cannot
        // be removed from a project. They are added to a project by
        // default. The second list is users that can be added
        // and removed from projects. The third list is the list of
        // added users that weren't added by default.
        ArrayList<UserInfo> possibleUsers = new ArrayList<UserInfo>();
        @SuppressWarnings("unchecked")
        Set<String> addedUsers = new TreeSet<String>(project.getUserIds());

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
            addedUsers = new TreeSet<String>();
            for (int i = 0; i < userids.length; i++)
            {
                addedUsers.add(userids[i]);
            }
        }
        else
        {
            toField = new String();
            Iterator<String> iter = addedUsers.iterator();
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
        Iterator<String> it = addedUsers.iterator();
        while (it.hasNext())
        {
            addedUsersIds.add((String) it.next());
        }

        SortUtil.sort(defUsers, new UserInfoComparator(locale));
        SortUtil.sort(possibleUsers, new UserInfoComparator(locale));
        SortUtil.sort(addedUsersIds, new StringComparator(locale));

        request.setAttribute("toField", toField);
        request.setAttribute("addedUsersIds", addedUsersIds);
        request.setAttribute("possibleUsers", possibleUsers);
        sessionMgr.setAttribute("defUsers", defUsers);

        setTableNavigation(request, session, defUsers,
                new UserInfoComparator(locale), 10, "numPerPage",
                "numPages", "defUsers", "sorting", "reverseSort",
                "pageNum", "lastPageNum", "listSize");
    }
}
