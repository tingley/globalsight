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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.util.SortUtil;

public class BasicProjectHandler extends PageHandler
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
        HttpSession session = request.getSession(false);
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        String action = request.getParameter("action");
        try
        {
            // get the bundle.
            ResourceBundle bundle = getBundle(session);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);

            SystemConfiguration sc = SystemConfiguration.getInstance();
            String pm = sc.getStringParameter(sc.PROJECT_MANAGER_GROUP);
            Locale locale = (Locale) session
                    .getAttribute(WebAppConstants.UILOCALE);
            User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);

            // Get list of termbases
            String userId = getUser(session).getUserId();
            boolean isAdmin = UserUtil.isInPermissionGroup(userId,
                    "Administrator");
            boolean isSuperAdmin = UserUtil.isSuperAdmin(userId);
            List termbases = new ArrayList();
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

            sessionMgr.setAttribute("termbases", termbases);
            // Get list of projects can detect duplicate names
            sessionMgr.setAttribute("projects",
                    ProjectHandlerHelper.getAllProjectsForGUI());

            sessionMgr.setAttribute("allAttributeGroups",
                    AttributeManager.getAllAttributeSets());

            if (action.equals("edit"))
            {
                // set project in session
                String id = (String) request.getParameter(RADIO_BUTTON);
                if (id == null
                        || request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    response.sendRedirect("/globalsight/ControlServlet?activityName=projects");
                    return;
                }
                Project project = ProjectHandlerHelper.getProjectById(Long
                        .parseLong(id));
                try
                {
                    sessionMgr.setAttribute("fileProfileTermList",
                            ServerProxy.getProjectHandler()
                                    .fileProfileListTerminology(project));

                }
                catch (Exception e)
                {
                }
                sessionMgr.setAttribute("project", project);
                sessionMgr.setAttribute("edit", "true");
                if (perms.getPermissionFor(Permission.PROJECTS_EDIT_PM))
                {
                    storeProjectManagers(sessionMgr, locale, pm);
                    storeQuotePersons(sessionMgr, locale);
                }
            }
            else if (action.equals("saveUsers"))
            {
                if (request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    response.sendRedirect("/globalsight/ControlServlet?activityName=projects");
                    return;
                }
                Project project = (Project) sessionMgr.getAttribute("project");
                ProjectHandlerHelper.extractUsers(project, request, sessionMgr);
            }
            else
            {
                if (request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    response.sendRedirect("/globalsight/ControlServlet?activityName=projects");
                    return;
                }
                // If admin, need to get list of project managers.
                // If not, set project manager name in session.
                if (perms.getPermissionFor(Permission.PROJECTS_EDIT_PM))
                {
                    storeProjectManagers(sessionMgr, locale, pm);
                    storeQuotePersons(sessionMgr, locale);
                }
                else
                {
                    sessionMgr.setAttribute("pm", user.getUserName());
                    sessionMgr.setAttribute("pmId", user.getUserId());
                }
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
    private void storeQuotePersons(SessionManager p_sessionMgr, Locale p_locale)
            throws Exception
    {
        UserComparator userComparator = new UserComparator(
                UserComparator.DISPLAYNAME, p_locale);
        // Get quote email persons
        Vector qePersons = ServerProxy.getUserManager().getUsers();
        Iterator it = qePersons.iterator();
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
        p_sessionMgr.setAttribute("qePersons", qePersons);
    }

    /*
     * Stores the list of all people that can manage projects.
     */
    private void storeProjectManagers(SessionManager p_sessionMgr,
            Locale p_locale, String p_pmGroup) throws Exception
    {
        long companyId = Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue());
        String companyName = ServerProxy.getJobHandler()
                .getCompanyById(companyId).getCompanyName();
        UserComparator userComparator = new UserComparator(
                UserComparator.DISPLAYNAME, p_locale);

        Collection usernames = Permission.getPermissionManager()
                .getAllUsersWithPermission(Permission.PROJECTS_MANAGE);

        List pms = new ArrayList();
        Iterator iter = usernames.iterator();
        while (iter.hasNext())
        {
            String username = (String) iter.next();
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
        p_sessionMgr.setAttribute("pms", pms);
    }

}
