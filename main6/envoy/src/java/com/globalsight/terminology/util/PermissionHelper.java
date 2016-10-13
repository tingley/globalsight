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

package com.globalsight.terminology.util;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;

import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;

import java.util.Collection;

/**
 * Mostly copied from com.globalsight.everest.webapp.PermissionHelper.
 * @deprecated code should be converted to use permissions directly.
 */
public class PermissionHelper
{
    static private final Logger s_logger =
        Logger.getLogger(
            PermissionHelper.class);

    static public boolean hasPermission(String p_user, String p_permission)
    {
        boolean result = false;

        try
        {
            PermissionSet userPerms =
                (PermissionSet)Permission.getPermissionManager().
                getPermissionSetForUser(p_user);

            result = userPerms.getPermissionFor(p_permission);
        }
        catch (Exception ex)
        {
            s_logger.error("Failed to read permission set for user " +
                p_user);
        }

        return result;
    }

    /**
     * Checks if the user is an administractor.
     */
    static public boolean isUserAdministrator(String p_user)
    {
        return isUserInThisGroup(Permission.GROUP_ADMINISTRATOR, p_user);
    }

    /**
     * Checks if the user is a project manager.
     */
    static public boolean isUserProjectManager(String p_user)
    {
        return isUserInThisGroup(Permission.GROUP_PROJECT_MANAGER, p_user);
    }

    /**
     * Checks if the user is a workflow manager.
     */
    static public boolean isUserWorkflowManager(String p_user)
    {
        return isUserInThisGroup(Permission.GROUP_WORKFLOW_MANAGER, p_user);
    }

    /**
     * Checks if the user is a locale manager.
     */
    static public boolean isUserLocaleManager(String p_user)
    {
        return isUserInThisGroup(Permission.GROUP_LOCALE_MANAGER, p_user);
    }

    /**
     * Checks if the user is a localization participant.
     */
    static public boolean isUserLocalizationParticipant(String p_user)
    {
        return isUserInThisGroup(Permission.GROUP_LOCALIZATION_PARTICIPANT,
            p_user);
    }

    /**
     * To check if the login user is a customer uploader.
     */
    static public boolean isLoginUserCustomerUploader(String p_user)
    {
        return isUserInThisGroup(Permission.GROUP_CUSTOMER, p_user);
    }

    /**
     * Checks if the user is in the specified group.
     */
    static public boolean isUserInThisGroup(String p_groupName,
        String p_user)
    {
        boolean result = false;

        try
        {
            Collection permGroups = Permission.getPermissionManager().
                getAllPermissionGroupNamesForUser(p_user);

            if (permGroups != null)
            {
                result = permGroups.contains(p_groupName);
            }
        }
        catch (Exception ex)
        {
            s_logger.error("Failed to read permission groups for user " +
                p_user);
        }

        return result;
    }
}

