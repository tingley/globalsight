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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorSearchParameters;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * The page handler for displaying the list of vendors.
 */

public class VendorsHandler extends PageHandler implements VendorConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(VendorsHandler.class);

    private int vendorsPerPage = 10;

    public VendorsHandler()
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            vendorsPerPage = sc
                    .getIntParameter(SystemConfigParamNames.VENDORS_PER_PAGE);
        }
        catch (Exception e)
        {
            // will use initialized defaults
        }
    }

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
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        String action = (String) request.getParameter("action");
        List vendors = null;

        if (action != null
                && (action.startsWith("cancel") || action.equals("ok")))
        {
            VendorSearchParameters sp = (VendorSearchParameters) sessionMgr
                    .getAttribute("vendorSearch");
            clearSessionExceptTableInfo(session, VENDOR_KEY);
            sessionMgr.setAttribute("vendorSearch", sp);
            vendors = (ArrayList) VendorHelper.searchVendors(user, sessionMgr);
        }
        else if ("save".equals(action))
        {
            // saving the changes of an existing vendor

            // First save data from basic info page
            Vendor vendor = (Vendor) sessionMgr.getAttribute("vendor");
            VendorHelper.saveBasicInfo(vendor, request);

            // persist the vendor
            FieldSecurity fs = (FieldSecurity) sessionMgr
                    .getAttribute(VendorConstants.FIELD_SECURITY_NOCHECK);
            VendorHelper.saveVendor(vendor, user, fs);
            vendors = (ArrayList) VendorHelper.searchVendors(user, sessionMgr);

            // Check for changes in Permissiong Groups
            updatePermissionGroups(vendor, sessionMgr);

            clearSessionExceptTableInfo(session, VENDOR_KEY);
        }
        else if ("create".equals(action))
        {
            // creating a vendor
            Vendor vendor = (Vendor) sessionMgr.getAttribute("vendor");

            if (vendor.getUserId() != null)
            {
                // save data from permissions page
                VendorHelper.savePermissions(vendor, request, sessionMgr);
            }
            else
            {
                // save data from security page
                FieldSecurity fs = (FieldSecurity) sessionMgr
                        .getAttribute(VendorConstants.FIELD_SECURITY_NOCHECK);
                VendorHelper.saveSecurity(fs, request);
            }

            // persist the vendor
            FieldSecurity fs = (FieldSecurity) sessionMgr
                    .getAttribute(VendorConstants.FIELD_SECURITY_NOCHECK);
            VendorHelper.createVendor(vendor, user, fs);
            if (vendor.getUserId() != null || vendor.getUser() != null)
            {
                addPermissionGroups(vendor, sessionMgr);
            }
            vendors = (ArrayList) VendorHelper.searchVendors(user, sessionMgr);
            clearSessionExceptTableInfo(session, VENDOR_KEY);
        }
        else if ("search".equals(action))
        {
            // search for vendors
            vendors = VendorHelper.searchVendors(request, user, sessionMgr);
            sessionMgr.setAttribute("vendors", vendors);
        }
        else if ("doneCustom".equals(action))
        {
            // VA gets here from hitting done on custom form designer
            // Save the custom page designer info to the database
            CustomPageHelper.saveCustomForm(request);
            vendors = (ArrayList) sessionMgr.getAttribute("vendors");
            if (vendors == null)
                vendors = (ArrayList) VendorHelper.getVendors(user);
        }
        else
        {
            vendors = (ArrayList) sessionMgr.getAttribute("vendors");
            if (vendors == null)
                vendors = (ArrayList) VendorHelper.getVendors(user);
        }
        initVendorsTable(request, session, sessionMgr, vendors);
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Overide getControlFlowHelper so we can do processing and redirect the
     * user correctly.
     * 
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {

        return new VendorControlFlowHelper(p_request, p_response);
    }

    private void initVendorsTable(HttpServletRequest request,
            HttpSession session, SessionManager sessionMgr, List vendors)
            throws EnvoyServletException
    {

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(request, session, vendors, new VendorComparator(
                uiLocale), vendorsPerPage, VENDOR_LIST, VENDOR_KEY);

        // Get list of vendors that will be displayed. Loop through
        // them and check field level security.
        List subList = (List) request.getAttribute(VENDOR_LIST);
        if (subList == null)
            return;
        request.setAttribute(VENDOR_LIST, subList);

        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        List securities = VendorHelper.getSecurities(subList, user);
        sessionMgr.setAttribute("securities", securities);
    }

    /**
     * Add Permission Groups to new vendor.
     */
    private void addPermissionGroups(Vendor p_vendor,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {
        ArrayList userPerms = (ArrayList) p_sessionMgr
                .getAttribute("userPerms");
        if (userPerms == null || userPerms.size() == 0)
        {
            CATEGORY.warn("No permission groups are being mapped to vendor "
                    + UserUtil.getUserNameById(p_vendor.getUserId()));
            return;
        }
        ArrayList list = new ArrayList(1);
        list.add(p_vendor.getUserId());
        try
        {
            PermissionManager manager = Permission.getPermissionManager();
            for (int i = 0; i < userPerms.size(); i++)
            {
                PermissionGroup pg = (PermissionGroup) userPerms.get(i);
                manager.mapUsersToPermissionGroup(list, pg);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * If there have been changes to the Permission Groups for a user, do the
     * update.
     */
    private void updatePermissionGroups(Vendor p_vendor,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {
        ArrayList changed = (ArrayList) p_sessionMgr.getAttribute("userPerms");
        if (changed == null)
            return;
        ArrayList existing = (ArrayList) PermissionHelper
                .getAllPermissionGroupsForUser(p_vendor.getUserId());
        if (existing == null && changed.size() == 0)
            return;

        ArrayList list = new ArrayList(1);
        list.add(p_vendor.getUserId());
        try
        {
            PermissionManager manager = Permission.getPermissionManager();
            if (existing == null)
            {
                // just adding new perm groups
                for (int i = 0; i < changed.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup) changed.get(i);
                    manager.mapUsersToPermissionGroup(list, pg);
                }
            }
            else
            {
                // need to determine what to add and what to remove.
                // Loop thru old list and see if perm is in new list. If not,
                // remove it.
                for (int i = 0; i < existing.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup) existing.get(i);
                    boolean found = false;
                    for (int j = 0; j < changed.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup) changed.get(j);
                        if (pg.getId() == cpg.getId())
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.unMapUsersFromPermissionGroup(list, pg);
                }

                // Loop thru new list and see if perm is in old list. If not,
                // add it.
                for (int i = 0; i < changed.size(); i++)
                {
                    boolean found = false;
                    PermissionGroup pg = (PermissionGroup) changed.get(i);
                    for (int j = 0; j < existing.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup) existing.get(j);
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
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}
