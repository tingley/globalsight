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
package com.globalsight.everest.webapp;

import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.foundation.User;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.Permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

/**
 * Do not use this class. Checking permissions by
 * group name is the old and wrong way to do it.
 * Check by using PermissionSet and individual
 * permissions.
 * This used to be named PermissionManager.java
 * 
 * @deprecated 
 */
public class PermissionHelper
{
    private static final GlobalSightCategory s_logger =
    (GlobalSightCategory) GlobalSightCategory.getLogger(
        PermissionHelper.class.getName());

    public PermissionHelper()
    {
        s_logger.warn("Creating " + PermissionHelper.class.getName() + ". Do not use this class!");
    }

    /**
     * To check if the login user is an administractor.
     * Note: The login user's info is stored in SessionManager.
     */
    public static boolean isLoginUserAdministrator(
        HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_ADMINISTRATOR);
    }

    /**
     * To check if the login user is a project manager.
     * Note: The login user's info is stored in SessionManager.
     */
    public static boolean isLoginUserProjectManager(
        HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_PROJECT_MANAGER);
    }

    /**
     * To check if the login user is a workflow manager.
     * Note: The login user's info is stored in SessionManager.
     */
    public static boolean isLoginUserWorkflowManager(HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_WORKFLOW_MANAGER);
    }

    /**
     * To check if the login user is a locale manager.
     * Note: The login user's info is stored in SessionManager.
     */
    public static boolean isLoginUserLocaleManager(HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_LOCALE_MANAGER);
    }

    /**
     * To check if the login user is a localization participant.
     * Note: The login user's info is stored in SessionManager.
     */
    public static boolean isLoginUserLocalizationParticipant(
        HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_LOCALIZATION_PARTICIPANT);
    }

    /**
     * To check if the login user is a customer uploader.
     * Note: The login user's info is stored in SessionManager.
     */
    public static boolean isLoginUserCustomerUploader(HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_CUSTOMER);
    }

    public static boolean isLoginUserVendorAdmin(
        HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_VENDOR_ADMIN);
    }

    public static boolean isLoginUserVendorManager(
        HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_VENDOR_MANAGER);
    }

    public static boolean isLoginUserVendorViewer(
        HttpServletRequest p_request)
    {
        return isUserInPermissionGroup(p_request,Permission.GROUP_VENDOR_VIEWER);
    }

    public static boolean isUserInPermissionGroup(
        HttpServletRequest p_request, String p_permGroupName)
    {
        ArrayList permGroups = getPermissionGroupNamesForUser(p_request);
        return permGroups.contains(p_permGroupName);
    }

    /**
     * Queries the permission group names for this user
     * and stores it in the session for subsequent access.
     * 
     * @param p_request
     * @return ArrayList of String
     */
    public static ArrayList getPermissionGroupNamesForUser(
        HttpServletRequest p_request)
    {
        warnAboutUsage();
        HttpSession session = p_request.getSession();
        String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        ArrayList permGroupNames = (ArrayList) session.getAttribute("permissionGroupNames");
        if (permGroupNames == null)
        {
            try {
                permGroupNames = new ArrayList(Permission.getPermissionManager().
                    getAllPermissionGroupNamesForUser(userId));
            }
            catch (Exception e)
            {
                s_logger.error("Failed to read permission groups for user " + userId,e);
                permGroupNames = new ArrayList();
            }
            session.setAttribute("permissionGroupNames",permGroupNames);
        }
        return permGroupNames;
    }

    //warns that the previous method was called from somewhere and it shouldn't be used
    private static void warnAboutUsage()
    {
        Exception e = new Exception("blah");
        StackTraceElement traces[] = e.getStackTrace();
        String method = traces[1].getMethodName();
        System.out.println("--WARNING, " + method + " called.");
        System.out.println("           " + traces[2].toString());
        System.out.println("           " + traces[3].toString());
        System.out.println("           " + traces[4].toString());
        System.out.println("           " + traces[5].toString());
    }
}
