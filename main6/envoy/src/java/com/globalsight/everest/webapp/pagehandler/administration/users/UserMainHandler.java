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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SSOUserUtil;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionGroupsHandler;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.modules.Modules;

/*
 * Page handler for display list of Users.
 */
public class UserMainHandler
    extends PageHandler
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            UserMainHandler.class);

    public static final String CREATE_USER_WRAPPER = "createUserWrapper";
    public static final String MODIFY_USER_WRAPPER = "modifyUserWrapper";
    public static final String ADD_ANOTHER = "addAnother";
    public static final String SEARCH_PARAMS = "searchParams";


    /**
     * Invokes this PageHandler.
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
                (SessionManager) session.getAttribute(SESSION_MANAGER);
        String action = p_request.getParameter("action");
        UserSearchParams params = (UserSearchParams)sessionMgr.getAttribute("fromSearch");


        if (action != null)
        {
            if (action.equals(USER_ACTION_CREATE_USER))
            {
                createUser(p_request);
            }
            else if (action.equals(USER_ACTION_MODIFY_USER))
            {
                modifyUser(p_request, false);
            }
            else if (action.equals(USER_ACTION_MODIFY2_USER))
            {
                modifyUser(p_request, true);
            }
            else if (action.equals("remove"))
            {
                removeUser(p_request);
            }
            else if (action.equals("search"))
            {
                params = searchUsers(p_request);
            }
        }
        else
        {
            checkPreReqData(p_request, session );
        }

        try
        {
            if (p_request.getParameter("linkName") != null &&
                !p_request.getParameter("linkName").equals("self"))
                    sessionMgr.clear();

            //add the searcher's permissions and company name to the params
            //so that they're always used to filter results
            PermissionSet perms = (PermissionSet) session.getAttribute(
                WebAppConstants.PERMISSIONS);
            User thisUser = (User) sessionMgr.getAttribute(WebAppConstants.USER);
            if (params==null)
                params = new UserSearchParams();
            params.setPermissionSetOfSearcher(perms);
            params.setCompanyOfSearcher(thisUser.getCompanyName());

            dataForTable(p_request, p_request.getSession(), params);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }

        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }


    /**
     * Perform create user action
     */
    private void createUser(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        // Get the session manager.
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager) session.getAttribute(SESSION_MANAGER);

        // Get the user wrapper off the session manager.
        CreateUserWrapper wrapper =
            (CreateUserWrapper) sessionMgr.getAttribute(CREATE_USER_WRAPPER);

        if (wrapper != null)
        {
            UserFluxCalendar cal;

            // Get the data from the last page (permissions page)
            UserUtil.extractPermissionData(p_request);
            try
            {
                if (Modules.isCalendaringInstalled())
                {
                    // Create the user's calendar
                    cal = (UserFluxCalendar)
                        sessionMgr.getAttribute(CalendarConstants.CALENDAR);
                }
                else
                {
                    // Create a user calendar based on the system calendar.
//                    FluxCalendar baseCal = CalendarHelper.getDefaultCalendar();
                    String companyId = CompanyWrapper.getCompanyIdByName(wrapper.getCompanyName());
                    FluxCalendar baseCal = CalendarHelper.getDefaultCalendar(companyId);
                    cal = new UserFluxCalendar(baseCal.getId(),
                        wrapper.getUserId(), baseCal.getTimeZoneId());
                    CalendarHelper.updateUserCalFieldsFromBase(baseCal, cal);
                }
            }
            catch(EnvoyServletException e)
            {
                // Don't create the user if calendar can't be created.
                throw e;
            }

            // Now commit the wrapper
            wrapper.setCalendar(cal);
            wrapper.commitWrapper();

            // Add permissions groups is necessary
            addPermissionGroups(wrapper, sessionMgr);

            // save sso user mapping
            updateSSOUserMapping(wrapper);
        }
        clearSessionExceptTableInfo(session, UserConstants.USER_KEY);
    }

    /**
     * Perform modify user action.
     *
     * @param getUserData - true if the user hit save from the first
     * page.  Need to get the data from the request.
     */
    private void modifyUser(HttpServletRequest p_request,
        boolean getUserData)
        throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager) session.getAttribute(SESSION_MANAGER);

        // Get the user wrapper off the session manager.
        ModifyUserWrapper wrapper =
            (ModifyUserWrapper) sessionMgr.getAttribute(MODIFY_USER_WRAPPER);

        if (getUserData)
        {
            UserUtil.extractUserData(p_request, wrapper, false);
        }

        // Commit the wrapper
        wrapper.commitWrapper(session);

        // Check for changes in Permissiong Groups
        updatePermissionGroups(wrapper, sessionMgr);
        
        // save sso user mapping
        updateSSOUserMapping(wrapper);

        clearSessionExceptTableInfo(session, UserConstants.USER_KEY);
        
        // If modify the current user, also need reset the session.
        String currentUserID = ((User) sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();
        if (currentUserID != null && currentUserID.equalsIgnoreCase(wrapper.getUserId()))
        {
            try
            {
                User user = ServerProxy.getUserManager().getUser(currentUserID);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * For sso user mapping
     * @param wrapper
     */
    private void updateSSOUserMapping(CreateUserWrapper wrapper)
    {
        String ssoUserName = wrapper.getSsoUserName();

        if (ssoUserName != null)
        {
            String companyName = wrapper.getCompanyName();
            long companyId = CompanyWrapper.getCompanyByName(companyName).getId();
            String userName = wrapper.getUserId();
            
            SSOUserUtil.saveUserMapping(companyId, userName, ssoUserName);
        }
    }

    /**
     * Remove a user.
     */
    private void removeUser(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager) session.getAttribute(SESSION_MANAGER);
        User loggedInUser = (User)sessionMgr.getAttribute(WebAppConstants.USER);
        String userId = p_request.getParameter("radioBtn");
        if (userId == null || p_request.getMethod().equalsIgnoreCase("get"))
        {
            return;
        }
        
        String deps = UserHandlerHelper.checkForDependencies(userId, session);
        if (deps == null)
        {
            // removes the user
            UserHandlerHelper.removeUser(loggedInUser, userId);
            SetDefaultRoleUtil.removeDefaultRoles(userId);
            ProjectTMTBUsers ptu = new ProjectTMTBUsers();
            ptu.deleteAllTMTB(userId);
        }
        else
        {
            CATEGORY.warn("Cannot delete user " + userId +
                          " because of the following dependencies:\r\n" +
                          deps);
            p_request.setAttribute(UserConstants.DEPENDENCIES, deps);
        }
    }

    /**
     * Search for users with certain criteria.
     */
    private UserSearchParams searchUsers(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        String buf = p_request.getParameter("nameTypeOptions");
        UserSearchParams params = new UserSearchParams();
        params.setNameType(Integer.parseInt(buf));
        buf = p_request.getParameter("nameOptions");
        params.setNameFilter(Integer.parseInt(buf));
        params.setNameParam(p_request.getParameter("nameField"));
        params.setSourceLocaleParam(p_request.getParameter("srcLocale"));
        params.setTargetLocaleParam(p_request.getParameter("targLocale"));
        params.setPermissionGroupParam(p_request.getParameter("permissionGroup"));
        return params;
    }

    /**
     * Before being able to create a User, certain objects must exist.
     * Check that here.
     */
    private void checkPreReqData(HttpServletRequest p_request, HttpSession p_session)
        throws EnvoyServletException
    {
        String userId = (String) p_session.getAttribute(WebAppConstants.USER_NAME);
        boolean isSuperAdmin = false;
        try {
            isSuperAdmin = UserUtil.isSuperAdmin(userId);
        } catch (Exception e) {
            throw new EnvoyServletException(e);
        } 
        if (isSuperAdmin)
        {
            return;
        }
        
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);
        Vector allSourceLocales = UserHandlerHelper.getAllSourceLocales();
        Vector allActivities = UserHandlerHelper.getAllActivities(uiLocale);

        if (allActivities == null || allActivities.size() < 1
            || allSourceLocales == null || allSourceLocales.size() < 1)
        {
            ResourceBundle bundle = getBundle(p_session);
            StringBuffer message = new StringBuffer();
            boolean addcomma = false;
            message.append(bundle.getString("msg_prereq_warning_1"));
            message.append(":  ");
            if(allActivities == null || allActivities.size() < 1)
            {
                message.append(bundle.getString("lb_activity_types"));
                addcomma = true;
            }
            if(allSourceLocales == null || allSourceLocales.size() < 1)
            {
                if (addcomma) message.append(", ");
                message.append(bundle.getString("lb_locale_pairs"));
            }
            message.append(".  ");
            message.append(bundle.getString("msg_prereq_warning_2"));

            p_request.setAttribute("preReqData", message.toString());
        }
    }

    /**
     * Get list of all users, sorted appropriately
     */
    private void dataForTable(HttpServletRequest p_request, HttpSession p_session,
                              UserSearchParams params)
        throws RemoteException, NamingException, GeneralException
    {
        Vector users =  UserUtil.getUsersForSearchParams(params);
        
        String userId = (String) p_session.getAttribute(WebAppConstants.USER_NAME);
        boolean isSuperAdmin = false;
        boolean isSuperPM = false;
        try {
            isSuperAdmin = UserUtil.isSuperAdmin(userId);
            if (!isSuperAdmin)
            {
                isSuperPM = UserUtil.isSuperPM(userId);
            }
        } catch (Exception e) {
            throw new EnvoyServletException(e);
        } 
        if (!isSuperAdmin)
        {
            String companyName = null;
            if (isSuperPM)
            {
                long companyId = Long.parseLong(CompanyThreadLocal.getInstance().getValue());
                companyName = ServerProxy.getJobHandler().getCompanyById(companyId).getName();
            }
            else 
            {
                companyName = params.getCompanyOfSearcher();
            }
            
            for (Iterator iter = users.iterator(); iter.hasNext();)
            {
                User user = (User) iter.next();
                if (!companyName.equals(user.getCompanyName()))
                {
                    iter.remove();
                }
            }
        }

        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, users,
                       new UserComparator(uiLocale, getBundle(p_session)),
                       10,
                       UserConstants.USER_LIST, UserConstants.USER_KEY);
        SessionManager sessionMgr =
            (SessionManager) p_session.getAttribute(SESSION_MANAGER);
        User loggedInUser = (User)sessionMgr.getAttribute(WebAppConstants.USER);
        
        // for GBS-1155.
        if (!CompanyThreadLocal.getInstance().fromSuperCompany())
        {
            p_request.setAttribute("securities",
                    UserHandlerHelper.getSecurities(users, loggedInUser));
        }
        
        sessionMgr.setAttribute("fromSearch", params);
    }

    /**
     * If there have been changes to the Permission Groups for a user,
     * do the update.
     */
    private void updatePermissionGroups(ModifyUserWrapper p_wrapper, SessionManager p_sessionMgr)
        throws EnvoyServletException
    {
        ArrayList changed = (ArrayList) p_sessionMgr.getAttribute("userPerms");
        if (changed == null) return;
        ArrayList existing = (ArrayList)PermissionHelper.getAllPermissionGroupsForUser(
                                    p_wrapper.getUserId());
        if (existing == null && changed.size() ==0) return;
        
        User user = p_wrapper.getUser();
        ArrayList list = new ArrayList(1);
        list.add(user.getUserId());
        try {
            PermissionManager manager = Permission.getPermissionManager();
            if (existing == null) 
            {
                // just adding new perm groups
                for (int i = 0; i < changed.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup)changed.get(i);
                    manager.mapUsersToPermissionGroup(list, pg);
                }
            }
            else
            {
                // need to determine what to add and what to remove.
                // Loop thru old list and see if perm is in new list.  If not,
                // remove it.
                for (int i = 0; i < existing.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup)existing.get(i);
                    boolean found = false;
                    for (int j = 0; j < changed.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup)changed.get(j);
                        if (pg.getId() == cpg.getId()) 
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.unMapUsersFromPermissionGroup(list, pg);
                }

                // Loop thru new list and see if perm is in old list.  If not,
                // add it.
                for (int i = 0; i < changed.size(); i++)
                {
                    boolean found = false;
                    PermissionGroup pg = (PermissionGroup)changed.get(i);
                    for (int j = 0; j < existing.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup)existing.get(j);
                        if (pg.getId() == cpg.getId()) 
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.mapUsersToPermissionGroup(list, pg);
                }
            }
        } catch (Exception e) {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Add Permission Groups to new user.
     */
    private void addPermissionGroups(CreateUserWrapper p_wrapper,
                         SessionManager p_sessionMgr)
        throws EnvoyServletException
    {
        ArrayList userPerms = (ArrayList) p_sessionMgr.getAttribute("userPerms");
        if (userPerms == null && userPerms.size() ==0) return;
        User user = p_wrapper.getUser();
        ArrayList list = new ArrayList(1);
        list.add(user.getUserId());
        try {
            PermissionManager manager = Permission.getPermissionManager();
            for (int i = 0; i < userPerms.size(); i++)
            {
                PermissionGroup pg = (PermissionGroup)userPerms.get(i);
                manager.mapUsersToPermissionGroup(list, pg);
            }
        } catch (Exception e) {
            throw new EnvoyServletException(e);
        }
    }
}
