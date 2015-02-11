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

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.VendorFieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorRole;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.UserRoleImpl;
import com.globalsight.everest.foundation.User;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;

public class VendorBasicHandler extends PageHandler
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

        initStaticData(sessionMgr);
        if ("edit".equals(action))
        {
            String id = (String) request.getParameter("id");
            Vendor vendor = VendorHelper.getVendor(Long.parseLong(id));
            sessionMgr.setAttribute(VENDOR, vendor);
            User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
            FieldSecurity securitiesHash = VendorHelper.getSecurity(vendor, user, true);
            sessionMgr.setAttribute(FIELD_SECURITY_CHECK_PROJS, securitiesHash);
            determineShowUserInfo(sessionMgr, vendor, securitiesHash);
            sessionMgr.setAttribute("edit", "true");
            setCustomButton(sessionMgr);

            // If this vendor is not a GlobalSight user, get usernames
            // for the drop down and all possible usernames for detecting
            // duplicate names.
            if (vendor.getUser() == null)
            {
                Locale uiLocale =
                         (Locale)session.getAttribute(WebAppConstants.UILOCALE);
                sessionMgr.setAttribute("allUsernames", 
                        VendorHelper.getPossibleUsers(uiLocale));
            }
            else
            {
                Vector users = new Vector();
                users.add(vendor.getUser());
                sessionMgr.setAttribute("allUsernames",
                                        users);
            }
            sessionMgr.setAttribute("allAliases", VendorHelper.getAllAliases());
            sessionMgr.setAttribute("allVendorIds", VendorHelper.getAllCustomVendorIds());

            // This is needed for canceling the editing of roles
            sessionMgr.setAttribute("origVendor",
                 VendorHelper.getVendor(Long.parseLong(id)));
        } 
        else if ("previous".equals(action))
        {
            // save contact info
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            VendorHelper.saveContactInfo(vendor, request);
        }
        else if ("new".equals(action))
        {
            Locale uiLocale =
                 (Locale)session.getAttribute(WebAppConstants.UILOCALE);
            sessionMgr.setAttribute(FIELD_SECURITY_CHECK_PROJS, new VendorFieldSecurity());
            sessionMgr.setAttribute("allUsernames", 
                VendorHelper.getPossibleUsers(uiLocale));
            sessionMgr.setAttribute("allAliases", VendorHelper.getAllAliases());
            sessionMgr.setAttribute("allVendorIds", VendorHelper.getAllCustomVendorIds());
        }
        else if ("doneContact".equals(action))
        {
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            VendorHelper.saveContactInfo(vendor, request);
        } 
        else if ("doneCV".equals(action))
        {
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            VendorHelper.saveCV(vendor, request);
        } 
        else if ("doneCustom".equals(action))
        {
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            VendorHelper.saveCustom(vendor, request, sessionMgr);
        } 
        else if ("doneSecurity".equals(action))
        {
            FieldSecurity fs = (FieldSecurity)sessionMgr.getAttribute(FIELD_SECURITY_NOCHECK);
            VendorHelper.saveSecurity(fs, request);
        } 
        else if ("donePerms".equals(action))
        {
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            VendorHelper.savePermissions(vendor, request, sessionMgr);
        } 
        else if ("setUser".equals(action))
        {
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            if (vendor == null)
            {
                vendor = new Vendor();
                sessionMgr.setAttribute(VENDOR, vendor);
            }
            copyUserInfoToVendor(request, vendor, sessionMgr);
        }
        else if ("removeUser".equals(action))
        {
            // Selected a user, then deselected.
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            vendor.setUserId(null);
            vendor.setPassword(null);
        }
        else if ("doneProjects".equals(action))
        {
            // return from editing projects on existing vendor that is
            // not an GlobalSight user
            sessionMgr.setAttribute("ProjectPageVisited", "true");
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            VendorHelper.saveProjects(vendor, request);
        }
        else if ("cancelRoles".equals(action))
        {
            Vendor vendor = (Vendor)sessionMgr.getAttribute(VENDOR);
            Vendor origVendor = (Vendor)sessionMgr.getAttribute("origVendor");
            vendor.setRoles(origVendor.getRoles());
        } 

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private void initStaticData(SessionManager sessionMgr)
    throws EnvoyServletException
    {
        // Get status values for drop down
        String[] statuses = VendorHelper.getStatusValues();
        sessionMgr.setAttribute("statuses", statuses);

        // Get companies for drop down
        String[] companies = VendorHelper.getCompanyNames();
        sessionMgr.setAttribute("companyNames", companies);
    }

    /*
     * The user selected a username to be associated with this vendor.
     * Copy all the user info to the vendor.  They are to be kept in
     * sync.
     */
    private void copyUserInfoToVendor(HttpServletRequest request,
                                      Vendor vendor, SessionManager sessionMgr)
    throws EnvoyServletException
    {
        String userId = (String)request.getParameter("usernameSelect");
        vendor.setUserId(userId);
        User user = UserHandlerHelper.getUser(userId);
        vendor.setFirstName(user.getFirstName());
        vendor.setLastName(user.getLastName());
        vendor.setTitle(user.getTitle());
        vendor.setPassword(user.getPassword());
        vendor.setCompanyName(user.getCompanyName());
        vendor.setAddress(user.getAddress());
        vendor.setPhoneNumber(User.PhoneType.HOME,
                        user.getHomePhoneNumber());
        vendor.setPhoneNumber(User.PhoneType.OFFICE,
                        user.getOfficePhoneNumber());
        vendor.setPhoneNumber(User.PhoneType.CELL,
                        user.getCellPhoneNumber());
        vendor.setPhoneNumber(User.PhoneType.FAX,
                        user.getFaxPhoneNumber());
        vendor.setEmail(user.getEmail());
        vendor.setDefaultUILocale(user.getDefaultUILocale());
        copyRoles(vendor, user);
        copyProjects(vendor, user);
        copyPermissions(sessionMgr, user);

        vendor.setStatus("APPROVED");
    }

    private void copyPermissions(SessionManager sessionMgr, User user)
    throws EnvoyServletException
    {
        sessionMgr.setAttribute("userPerms",
             PermissionHelper.getAllPermissionGroupsForUser(user.getUserId()));
    }

    private void copyRoles(Vendor vendor, User user)
    throws EnvoyServletException
    {
        try
        {
            List roles = (List)ServerProxy.getUserManager().getUserRoles(user);
            if (roles == null)
            {
                vendor.setRoles(null);
                return;
            }
            Set vRoles = new HashSet(roles.size());
            for (int i =0; i < roles.size(); i++)
            {
                UserRoleImpl role = (UserRoleImpl)roles.get(i);
                String src = role.getSourceLocale();
                LocaleManager lm = ServerProxy.getLocaleManager();
                String targ = role.getTargetLocale();
                LocalePair lp = 
                    lm.getLocalePairBySourceTargetStrings(src, targ);
                String rateString = role.getRate();  
                if (rateString != null && rateString.length() > 0 )
                {
                    long rateId = Long.parseLong(rateString);
                    Rate rate = 
                        ServerProxy.getCostingEngine().getRate(rateId);
                    vRoles.add(new VendorRole(role.getActivity(), lp, rate));
                }
                else
                {
                    vRoles.add(new VendorRole(role.getActivity(), lp, null));
                }
            }
            vendor.setRoles(vRoles);
        } catch (Exception e) {
            throw new EnvoyServletException(e);
        }
    }

    private void copyProjects(Vendor vendor, User user)
    throws EnvoyServletException
    {
        try
        {
            List projects = (List)ServerProxy.getProjectHandler().getProjectsByUser(user.getUserId());
            vendor.setProjects(projects);
        } catch (Exception e) {
            throw new EnvoyServletException(e);
        }
    }

    private void setCustomButton(SessionManager sessionMgr)
    throws EnvoyServletException
    {
        sessionMgr.setAttribute("customPageTitle", CustomPageHelper.getPageTitle());
    }

    private void determineShowUserInfo(SessionManager sessionMgr,
                                       Vendor vendor, FieldSecurity securitiesHash)
    {
        String access = (String)securitiesHash.get("userName");
        if (access != null && !access.equals("shared"))
        {
            sessionMgr.setAttribute("neverShowUserInfo", "true");
            return;
        }

        boolean useInAmbassador = vendor.useInAmbassador();
        access = (String)securitiesHash.get("useInAmbassador");
        if (access != null && !access.equals("shared") && !useInAmbassador)
        {
            sessionMgr.setAttribute("neverShowUserInfo", "true");
            return;
        }

        String status = vendor.getStatus();
        access = (String)securitiesHash.get("status");
        if (access != null && !access.equals("shared") && !"APPROVED".equals(status))
        {
            sessionMgr.setAttribute("neverShowUserInfo", "true");
            return;
        }
    }
}
