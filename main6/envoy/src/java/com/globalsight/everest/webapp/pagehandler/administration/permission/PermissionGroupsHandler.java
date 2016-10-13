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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionGroupImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;

/**
 * PermissionGroupsHandler is the pagehandler for PermissionGroups
 */
public class PermissionGroupsHandler extends PageHandler
{
    public static String CANCEL = "cancel";
    public static String CREATE = "create";
    public static String EDIT = "edit";
    public static String SAVE = "save";
    public static String REMOVE = "remove";
    public static String DETAILS = "details";
    public static String PERM_GROUP_LIST = "permGroups";
    public static String PERM_GROUP_KEY = "permGroup";
    private static int NUM_PER_PAGE = 10;

    /**
     * This identifies a request variable that holds the PermissionGroup ids
     * that belong to Super Company.
     */
    public static String SUPER_PGROUP_IDS = "superPermGroupIds";

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
        String action = p_request.getParameter("action");
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        try
        {
            if (CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session, PERM_GROUP_KEY);
            }
            else if (CREATE.equals(action))
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request,
                        FormUtil.Forms.NEW_PERMISSION_GROUP))
                {
                    // creating new permission group from set page
                    createPermissionGroup(session, p_request);
                    clearSessionExceptTableInfo(session, PERM_GROUP_KEY);
                }
            }
            else if (SAVE.equals(action))
            {
                // updating existing permissions from basic page
                savePermissionGroup(session, p_request);
                clearSessionExceptTableInfo(session, PERM_GROUP_KEY);
            }
            else if (REMOVE.equals(action))
            {
                removePermissionGroup(session, p_request);
            }
            if (!"self".equals(p_request.getParameter("linkName")))
            {
                sessionMgr.clear();
            }
            else
            {
                handleFilters(p_request, sessionMgr, action);
            }
            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        String pNameFilter = (String) sessionMgr.getAttribute("pNameFilter");
        String pCompanyFilter = (String) sessionMgr
                .getAttribute("pCompanyFilter");
        String condition = "";
        if (StringUtils.isNotBlank(pNameFilter))
        {
        	pNameFilter = pNameFilter.replace("'", "''");
        	condition += " and " + "p.name LIKE '%" + pNameFilter.trim() + "%'";
        }
        if (StringUtils.isNotBlank(pCompanyFilter))
        {
            condition += " and " + "c.name LIKE '%" + pCompanyFilter.trim()
                    + "%'";
        }
        ArrayList pgroups = (ArrayList) Permission.getPermissionManager()
                .getPermissionGroupsBycondition(condition);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        String numOfPerPage = p_request.getParameter("numOfPageSize");
        if (StringUtils.isNotEmpty(numOfPerPage))
        {
            try
            {
                NUM_PER_PAGE = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                NUM_PER_PAGE = Integer.MAX_VALUE;
            }
        }
        p_request.setAttribute(SUPER_PGROUP_IDS,
                PermissionHelper.getSuperPermissionGroupIds());

        setTableNavigation(p_request, p_session, pgroups,
                new PermissionGroupComparator(uiLocale), NUM_PER_PAGE,
                PERM_GROUP_LIST, PERM_GROUP_KEY);
    }

    /**
     * Create a PermissionGroup.
     */
    private void createPermissionGroup(HttpSession p_session,
            HttpServletRequest p_request) throws RemoteException,
            NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        PermissionGroupImpl permGroup = (PermissionGroupImpl) sessionMgr
                .getAttribute("permGroup");

        // Update data in permission group object
        PermissionHelper.saveUsers(permGroup, p_request);

        // create the PermissionGroup.
        Permission.getPermissionManager().createPermissionGroup(permGroup);

        // Update users in the db
        PermissionHelper.updateUsers(permGroup, p_request);
    }

    /**
     * Updates a PermissionGroup.
     */
    private void savePermissionGroup(HttpSession p_session,
            HttpServletRequest p_request) throws RemoteException,
            NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        PermissionGroupImpl permGroup = (PermissionGroupImpl) sessionMgr
                .getAttribute("permGroup");

        // Update data in permission group object
        PermissionHelper.saveBasicInfo(permGroup, p_request);

        // Update in db
        Permission.getPermissionManager().updatePermissionGroup(permGroup);

        // Update users in permission group
        PermissionHelper.updateUsers(permGroup, p_request);

        String activityDashboardViewPermissionCheckedOrNot = (String) p_session
                .getAttribute(Permission.ACTIVITY_DASHBOARD_VIEW);
        if (activityDashboardViewPermissionCheckedOrNot != null)
        {
            final Collection<String> usersInPermGroup = Permission
                    .getPermissionManager().getAllUsersForPermissionGroup(
                            permGroup.getId());
            // In order to refresh the activity dashboard with possible
            // incorrect numbers
            if ("unchecked".equals(activityDashboardViewPermissionCheckedOrNot))
            {
                // clean up the user owned records in TASK_INTERIM table for all
                // users in this permission group when the permission is
                // unchecked
                for (String userId : usersInPermGroup)
                {
                    TaskInterimPersistenceAccessor.deleteInterimUser(userId);
                }
            }
            else if ("checked"
                    .equals(activityDashboardViewPermissionCheckedOrNot))
            {
                // re-create the data in TASK_INTERIM table when the permission
                // is checked again
                Runnable runnable = new Runnable()
                {
                    public void run()
                    {
                        for (String userId : usersInPermGroup)
                        {
                            if (!TaskInterimPersistenceAccessor
                                    .isTriggered(userId))
                            {
                                TaskInterimPersistenceAccessor
                                        .initializeUserTasks(userId);
                            }
                        }
                    }
                };
                Thread t = new MultiCompanySupportedThread(runnable);
                t.start();
            }

            p_session.removeAttribute(Permission.ACTIVITY_DASHBOARD_VIEW);
        }
    }

    /**
     * Remove Permission if there are no dependencies.
     */
    private void removePermissionGroup(HttpSession p_session,
            HttpServletRequest p_request) throws RemoteException,
            NamingException, GeneralException, ServletException
    {
        String ids = (String) p_request.getParameter("radioBtn");
        if (ids == null || p_request.getMethod().equalsIgnoreCase("get"))
        {
            return;
        }
        String[] idarr = ids.trim().split(" ");
        for (String id : idarr)
        {
            if ("on".equals(id))
                continue;
            PermissionGroup permGroup = Permission.getPermissionManager()
                    .readPermissionGroup(Long.parseLong(id));
            Permission.getPermissionManager().deletePermissionGroup(permGroup);

        }
    }

    private void handleFilters(HttpServletRequest p_request,
            SessionManager sessionMgr, String action)
    {
        String pNameFilter = (String) p_request.getParameter("pNameFilter");
        String pCompanyFilter = (String) p_request
                .getParameter("pCompanyFilter");
        if (p_request.getMethod().equalsIgnoreCase(
                WebAppConstants.REQUEST_METHOD_GET))
        {
            pNameFilter = (String) sessionMgr.getAttribute("pNameFilter");
            pCompanyFilter = (String) sessionMgr.getAttribute("pCompanyFilter");
        }
        sessionMgr.setAttribute("pNameFilter", pNameFilter == null ? ""
                : pNameFilter);
        sessionMgr.setAttribute("pCompanyFilter", pCompanyFilter == null ? ""
                : pCompanyFilter);
    }

}
