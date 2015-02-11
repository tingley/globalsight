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
package com.globalsight.everest.webapp.pagehandler.administration.vendors;

import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.VendorSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserStateConstants;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.foundation.User;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class VendorProjectsHandler extends PageHandler
            implements VendorConstants
{
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
    throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        String action = (String)request.getParameter(ACTION);
        Vendor vendor = getVendor(request, sessionMgr);
        FieldSecurity securitiesHash = 
            (FieldSecurity)sessionMgr.getAttribute(FIELD_SECURITY_CHECK_PROJS);

        String access = (String)securitiesHash.get(VendorSecureFields.PROJECTS);
        if ("hidden".equals(access))
        {
            super.invokePageHandler(pageDescriptor, request, response, context);
            return;
        }
        else if ("projects".equals(action))
        {
            // Save the data from the basic page
            VendorHelper.saveBasicInfo(vendor, request);

            // Get data for projects page
            if (vendor.getUser() != null || "locked".equals(access))
            {
                getReadOnlyProjectData(vendor, session, request);
            }
            else
            {
                getExistingEditableProjectData(vendor, session, request, sessionMgr);
            }
            sessionMgr.setAttribute("editUser", "true");
        }
        else if ("next".equals(action) || "prev".equals(action))
        {
            if ("next".equals(action))
            {
                // Might need to save data from the roles page.  If they
                // selected locales and activities but didn't hit the add
                // button, do automatic add now.
                Locale uiLocale = (Locale)session.getAttribute(
                            WebAppConstants.UILOCALE);
                VendorHelper.newRole(vendor, request, sessionMgr, uiLocale);
            }
            else if ("prev".equals(action))
            {
                if (CustomPageHelper.getPageTitle() == null)
                {
                    // save security page
                    VendorHelper.saveSecurity(securitiesHash, request);
                }
                else
                {
                    // save custom page
                    VendorHelper.saveCustom(vendor, request, sessionMgr);
                }
            }

            // Get data for projects page
            if (vendor.getUser() != null)
            {
                getReadOnlyProjectData(vendor, session, request);
            }
            else
            {
                getEditableProjectData(vendor, session, request, sessionMgr);
            }
            sessionMgr.setAttribute("ProjectPageVisited", "true");
        }
        else
        {
            if (vendor.getUser() != null)
            {
                getReadOnlyProjectData(vendor, session, request);
            }
            else
            {
                getEditableProjectData(vendor, session, request, sessionMgr);
            }
            sessionMgr.setAttribute("ProjectPageVisited", "true");
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {

        return new CustomFormControlFlowHelper(p_request, p_response);
    }

    private void getEditableProjectData(Vendor vendor,
                                        HttpSession session, 
                                        HttpServletRequest request,
                                        SessionManager sessionMgr)
    throws ServletException, IOException, EnvoyServletException
    {
        PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
        // get data for the page
        String userName = (String)session.getAttribute(WebAppConstants.USER_NAME);
        User user = UserHandlerHelper.getUser(userName);
        List projects = VendorHelper.getProjectsManagedByUser(user);

        request.setAttribute("availableProjects", projects);
        ArrayList addedProjects = null;
        if (sessionMgr.getAttribute("ProjectPageVisited") != null)
        {
            request.setAttribute("addedProjects", vendor.getProjects());
            request.setAttribute("future", new Boolean(vendor.isInAllProjects()));
        }
        else
        {
            request.setAttribute("addedProjects", projects);

            if (perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
            {
                request.setAttribute("future", new Boolean(true));
            } else {
                request.setAttribute("future", new Boolean(false));
            }
        }
    }

    private void getExistingEditableProjectData(Vendor vendor,
                                        HttpSession session, 
                                        HttpServletRequest request,
                                        SessionManager sessionMgr)
    throws ServletException, IOException, EnvoyServletException
    {
        // get data for the page
        String userName = (String)session.getAttribute(WebAppConstants.USER_NAME
);
        User user = UserHandlerHelper.getUser(userName);
        ArrayList availableProjects = (ArrayList)
                VendorHelper.getProjectsManagedByUser(user);

        ArrayList addedProjects =  (ArrayList) vendor.getProjects();

        List defaultProjects = UserHandlerHelper.setProjectsForEdit(
                availableProjects, addedProjects, vendor.isInAllProjects(),
                request, session);

        setTableNavigation(request, session, defaultProjects,
                           null,
                           10,   // change this to be configurable!
                           PROJECT_LIST, PROJECT_KEY);

       request.setAttribute("future", new Boolean(vendor.isInAllProjects()));
    }

    private void getReadOnlyProjectData(Vendor vendor,
                                        HttpSession session, 
                                        HttpServletRequest request)
    throws ServletException, IOException, EnvoyServletException
    {
        setTableNavigation(request, session, vendor.getProjects(),
                           null,
                           10,   // change this to be configurable!
                           PROJECT_LIST, PROJECT_KEY);

        request.setAttribute("future", new Boolean(vendor.isInAllProjects()));
    }

    private Vendor getVendor(HttpServletRequest request,
                             SessionManager sessionMgr)
    {
        Vendor vendor = (Vendor)sessionMgr.getAttribute("vendor");
        if (vendor == null)
        {
            vendor = new Vendor();
            sessionMgr.setAttribute("vendor", vendor);
        }
        return vendor;
    }

}
