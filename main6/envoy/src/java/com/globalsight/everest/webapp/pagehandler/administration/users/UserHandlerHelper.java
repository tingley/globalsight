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

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.comparator.ActivityComparator;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.dependencychecking.UserDependencyChecker;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class UserHandlerHelper
{
    public static String PERMISSIONGROUP = "permissiongroup";
    public static String PROJECT = "project";

    public static Vector getAllSourceLocalesByCompanyId(String p_companyId)
            throws EnvoyServletException
    {
        Vector data = null;

        try
        {
            data = ServerProxy.getLocaleManager()
                    .getAllSourceLocalesByCompanyId(p_companyId);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }

        return data;
    }

    public static Vector getAllSourceLocales() throws EnvoyServletException
    {
        Vector data = null;

        try
        {
            data = ServerProxy.getLocaleManager().getAllSourceLocales();
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }

        return data;
    }

    public static Vector getAllTargetLocales() throws EnvoyServletException
    {
        Vector data = null;

        try
        {
            data = ServerProxy.getLocaleManager().getAllTargetLocales();
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }

        return data;
    }

    public static GlobalSightLocale getLocaleByString(String p_localeString)
            throws EnvoyServletException
    {
        GlobalSightLocale data = null;

        try
        {
            data = ServerProxy.getLocaleManager().getLocaleByString(
                    p_localeString);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }

        return data;
    }

    /**
     * Gets the PermissionGroups in the system. If the user doing the editing
     * 
     * 
     * NOTE: This used to filter out ones with "Vendor" in the name, but that
     * was removed in 6.7
     * 
     * @return
     * @exception EnvoyServletException
     */
    public static String[] getGroupNames() throws EnvoyServletException
    {
        String[] result = null;

        try
        {
            Collection permGroups = Permission.getPermissionManager()
                    .getAllPermissionGroups();
            Iterator iter = permGroups.iterator();
            ArrayList groupNames = new ArrayList();
            while (iter.hasNext())
            {
                PermissionGroup pg = (PermissionGroup) iter.next();
                groupNames.add(pg.getName());
            }
            result = new String[groupNames.size()];
            iter = groupNames.iterator();
            int i = 0;
            while (iter.hasNext())
                result[i++] = (String) iter.next();
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return result;
    }

    /**
     * Check if any objects have dependencies on this User. This should be
     * called BEFORE attempting to remove a User.
     * <p>
     * 
     * @param p_userId
     * @param session
     * @return
     * @exception EnvoyServletException
     *                Failed to look for dependencies for the profile. The cause
     *                is indicated by the exception message.
     */
    public static String checkForDependencies(String p_userId,
            HttpSession session) throws EnvoyServletException
    {
        try
        {
            ResourceBundle bundle = PageHandler.getBundle(session);
            User user = getUser(p_userId);

            UserDependencyChecker depChecker = new UserDependencyChecker();
            Hashtable catDeps = depChecker
                    .categorizeDependencies((UserImpl) user);

            // Now convert the hashtable into a Vector of Strings
            StringBuffer deps = new StringBuffer();
            if (catDeps.size() == 0)
            {
                return null;
            }

            deps.append("<span class=\"errorMsg\">");
            Object[] args =
            { bundle.getString("lb_user") };
            deps.append(MessageFormat.format(
                    bundle.getString("msg_dependency_users"), args));

            for (Enumeration e = catDeps.keys(); e.hasMoreElements();)
            {
                String key = (String) e.nextElement();
                deps.append("<p>*** " + bundle.getString(key) + " ***<br>");
                Vector values = (Vector) catDeps.get(key);
                for (int i = 0; i < values.size(); i++)
                {
                    deps.append((String) values.get(i));
                    deps.append("<br>");
                }
            }
            deps.append("</span>");
            return deps.toString();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Calls UserManager to remove the specified User from LDAP.
     * 
     * @param p_userRequestingRemove
     *            The user requesting the remove.
     * @param p_user
     *            The User object to remove from LDAP.
     */
    public static void removeUser(User p_userRequestingRemove, String p_userId)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getUserManager().removeUser(p_userRequestingRemove,
                    p_userId);
        }
        catch (UserManagerException ume)
        {
            throw new EnvoyServletException(ume);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
    }

    public static User getUser(String p_userId) throws EnvoyServletException
    {
        User data = null;
        try
        {
            data = ServerProxy.getUserManager().getUser(p_userId);
        }
        catch (UserManagerException ume)
        {
            throw new EnvoyServletException(ume);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        return data;
    }

    /**
     * Get all user names as an array of strings.
     * 
     * @return An array of user names.
     */
    public static String[] getAllUserNames() throws EnvoyServletException
    {
        String[] userNames = null;
        try
        {
            userNames = ServerProxy.getUserManager()
                    .getUserNamesFromAllCompanies();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return userNames;
    }

    public static ModifyUserWrapper createModifyUserWrapper(
            User p_userRequestingMod, User p_user) throws EnvoyServletException
    {
        try
        {
            return new ModifyUserWrapper(ServerProxy.getUserManager(),
                    p_userRequestingMod, p_user);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
    }

    public static String[] getUILocales() throws EnvoyServletException
    {
        String[] locales = null;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            locales = sc.getStrings(sc.UI_LOCALES);
        }
        catch (GeneralException e)
        {
            locales = new String[1];
            locales[0] = "en_US";
        }

        return locales;
    }

    public static Vector<Activity> getAllActivities(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList<Activity> al = new ArrayList<Activity>(ServerProxy
                    .getJobHandler().getAllActivities());
            ActivityComparator comp = new ActivityComparator(
                    ActivityComparator.NAME, p_locale);
            SortUtil.sort(al, comp);
            return new Vector<Activity>(al);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
        }
    }

    public static Vector getAllActivities(String p_companyId, Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getJobHandler()
                    .getAllActivitiesByCompanyId(p_companyId));
            ActivityComparator comp = new ActivityComparator(
                    ActivityComparator.NAME, p_locale);
            SortUtil.sort(al, comp);
            return new Vector(al);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
        }
    }

    public static Vector getTargetLocalesByCompanyId(
            GlobalSightLocale p_sourceLocale, String p_companyId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getTargetLocalesByCompanyId(
                    p_sourceLocale, p_companyId);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    public static Vector getTargetLocales(GlobalSightLocale p_sourceLocale)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getTargetLocales(
                    p_sourceLocale);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    /*
     * Get a reference to the object stored in the http session.
     */
    public static Object getObjectFromSession(HttpSession p_httpSession,
            String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_httpSession
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        return sessionMgr.getAttribute(p_key);
    }

    public static Vector getUsers() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getUsers();
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    public static Vector getUsersForCurrentCompany()
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getUsersForCurrentCompany();
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    public static Collection getUserRoles(User p_user)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getUserRoles(p_user);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    public static String[] getCompanyNames() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getCompanyNames();
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    public static List<Project> getProjectsManagedByUser(User user)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getProjectsManagedByUser(
                    user, Permission.GROUP_MODULE_GLOBALSIGHT);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    /**
     * Get the ProjectInfos for the user with userId. If this is the first time
     * the data is being fetched for the user, get it from the project handler.
     * Otherwise, get it from the user wrapper.
     */
    public static List getProjectsByUser(String userId)
            throws EnvoyServletException
    {
        try
        {
            return (List) ServerProxy.getProjectHandler().getProjectsByUser(
                    userId);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    /*
     * Return list of default projects. Update added projects.
     */
    public static List setProjectsForEdit(ArrayList availableProjects,
            ArrayList addedProjects, boolean isInAllProjects,
            HttpServletRequest request, HttpSession session)
    {

        // Default projects are the ones that are in added and not in
        // available. Create a default list, and remove the defaults from
        // the added list since they cannot be changed.
        //
        // There is one exception. If the logged in user is not the admin
        // and "future" is set to true, then put all added users in the
        // default list.
        // isAdmin now means user has the GET_ALL_PROJECTS permission??
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        boolean isAdmin = perms.getPermissionFor(Permission.GET_ALL_PROJECTS);
        ArrayList defaultProjects = null;
        if (addedProjects != null)
        {
            defaultProjects = new ArrayList();
            if (availableProjects == null || availableProjects.size() == 0
                    || (isInAllProjects && !isAdmin))
            {
                // all added projects are defaults
                defaultProjects = (ArrayList) addedProjects;
                addedProjects = null;
            }
            else
            {
                Hashtable hash = new Hashtable(availableProjects.size());
                Iterator iter = availableProjects.iterator();
                while (iter.hasNext())
                {
                    Project project = (Project) iter.next();
                    hash.put(new Long(project.getId()), project);
                }
                ArrayList removeList = new ArrayList();
                iter = addedProjects.iterator();
                while (iter.hasNext())
                {
                    Project project = (Project) iter.next();
                    if (hash.get(new Long(project.getId())) == null)
                    {
                        defaultProjects.add(project);
                        removeList.add(project);
                    }
                }
                iter = removeList.iterator();
                while (iter.hasNext())
                {
                    Project project = (Project) iter.next();
                    addedProjects.remove(project);
                }
            }
        }

        if (availableProjects != null && availableProjects.size() != 0)
        {
            SortUtil.sort(availableProjects,
                    new ProjectComparator(Locale.getDefault()));
        }
        else
        {
            availableProjects = new ArrayList();
        }
        if (addedProjects != null)
        {
            SortUtil.sort(addedProjects,
                    new ProjectComparator(Locale.getDefault()));
        }
        else
        {
            addedProjects = new ArrayList();
        }

        request.setAttribute("availableProjects", availableProjects);
        request.setAttribute("addedProjects", addedProjects);
        return defaultProjects;
    }

    /**
     * Get a list of field level securities for the specified users.
     */
    public static List getSecurities(List p_users, User p_user)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getSecurityManager().getFieldSecurities(p_user,
                    p_users, true);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the field level security hashtable
     */
    public static FieldSecurity getSecurity(User userToModify,
            User userRequestingModify, boolean checkProject)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getSecurityManager().getFieldSecurity(
                    userRequestingModify, userToModify, checkProject);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Gets the PermissionGroup names for the user
     * 
     * 
     * @return
     * @exception EnvoyServletException
     */
    public static String[] getAllPermissionGroupNamesForUser(String p_userId)
            throws EnvoyServletException
    {
        String[] result = null;

        try
        {
            Collection permGroups = Permission.getPermissionManager()
                    .getAllPermissionGroupsForUser(p_userId);
            Iterator iter = permGroups.iterator();
            ArrayList groupNames = new ArrayList();
            while (iter.hasNext())
            {
                PermissionGroup pg = (PermissionGroup) iter.next();
                groupNames.add(pg.getName());
            }
            result = new String[groupNames.size()];
            iter = groupNames.iterator();
            int i = 0;
            while (iter.hasNext())
                result[i++] = (String) iter.next();
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge.getExceptionId(), ge);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return result;
    }

    public static HashMap<String, String> getAllPerAndProNameForUser(
            String tableName) throws EnvoyServletException
    {
        HashMap<String, String> result = new HashMap<String, String>();

        try
        {
            Collection<Object[]> listo = Permission.getPermissionManager()
                    .getAlltableNameForUser(tableName);
            for (Iterator<Object[]> a = listo.iterator(); a.hasNext();)
            {
                Object[] test = a.next();
                String key = String.valueOf(test[0]);
                String value = result.get(key);
                if (value != null)
                {
                    result.put(key, value + "<br>" + String.valueOf(test[1]));

                }
                else
                {
                    result.put(key, String.valueOf(test[1]));

                }
            }
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return result;
    }

}
