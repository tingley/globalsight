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
import java.util.Locale;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionGroupImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
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
    
    /**
     * This identifies a request variable that holds the PermissionGroup ids that belong to Super Company.
     */
    public static String SUPER_PGROUP_IDS = "superPermGroupIds";
    
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if (CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session, PERM_GROUP_KEY);
            }
            else if (CREATE.equals(action))
            {
                if (FormUtil.isNotDuplicateSubmisson(p_request, FormUtil.Forms.NEW_PERMISSION_GROUP)) {
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
            dataForTable(p_request, session);
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
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        ArrayList pgroups = (ArrayList) PermissionHelper.getAllPermissionGroups();
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);
        
        p_request.setAttribute(SUPER_PGROUP_IDS, PermissionHelper.getSuperPermissionGroupIds());

        setTableNavigation(p_request, p_session, pgroups,
                       new PermissionGroupComparator(uiLocale),
                       10,
                       PERM_GROUP_LIST, PERM_GROUP_KEY);
    }

    /**
     * Create a PermissionGroup.
     */
    private void createPermissionGroup(HttpSession p_session, HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager)
                p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        PermissionGroupImpl permGroup = (PermissionGroupImpl)
                    sessionMgr.getAttribute("permGroup");

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
    private void savePermissionGroup(HttpSession p_session, HttpServletRequest p_request)
         throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager)
                p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
        PermissionGroupImpl permGroup = (PermissionGroupImpl)
                    sessionMgr.getAttribute("permGroup");

        // Update data in permission group object
        PermissionHelper.saveBasicInfo(permGroup, p_request);

        // Update in db
        Permission.getPermissionManager().updatePermissionGroup(permGroup);

        // Update users in permission group
        PermissionHelper.updateUsers(permGroup, p_request);
    }

    /**
     * Remove Permission if there are no dependencies.
     */
    private void removePermissionGroup(HttpSession p_session, HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException, ServletException
    {
        String id = (String)p_request.getParameter("radioBtn");
        if (id == null || p_request.getMethod().equalsIgnoreCase("get"))
        {
            return;
        }
        
        PermissionGroup permGroup = Permission.getPermissionManager().readPermissionGroup(
                                            Long.parseLong(id));
        Permission.getPermissionManager().deletePermissionGroup(permGroup);
    }

}

