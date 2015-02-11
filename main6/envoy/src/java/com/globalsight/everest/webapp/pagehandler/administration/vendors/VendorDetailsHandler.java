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
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.VendorSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.VendorRoleComparator;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;


public class VendorDetailsHandler extends PageHandler
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
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        Vendor vendor = setVendor(sessionMgr, request);
        setRoles(request, session, vendor);
        setProjects(request, session, vendor);
        User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
        FieldSecurity securitiesHash = VendorHelper.getSecurity(vendor, user, true);
        sessionMgr.setAttribute(FIELD_SECURITY_CHECK_PROJS, securitiesHash);

        request.setAttribute("pageContent",
                 CustomPageHelper.getPageContent(session, vendor.getCustomFields(),
                 (String)securitiesHash.get(VendorSecureFields.CUSTOM_FIELDS), true));


        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    private Vendor setVendor(SessionManager sessionMgr, HttpServletRequest request)
    throws EnvoyServletException 
    {
        String id = (String)request.getParameter("id");
        Vendor vendor = null;
        if (id != null)
        {
            vendor = VendorHelper.getVendor(Long.parseLong(id));
            sessionMgr.setAttribute("vendor", vendor);
            return vendor;
        }
        else
        {
            return (Vendor) sessionMgr.getAttribute("vendor");
        }
    }

    private void setRoles(HttpServletRequest request,
                                   HttpSession session,
                                   Vendor vendor)
    throws EnvoyServletException
    {
        Set roles = vendor.getRoles();
        Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);

        ArrayList rolesList;
        if (roles == null)
            rolesList = new ArrayList();
        else
            rolesList = new ArrayList(roles);
        setTableNavigation(request, session, rolesList,
                           new VendorRoleComparator(uiLocale),
                           10,
                           ROLE_LIST,
                           ROLE_KEY);
    }

    private void setProjects(HttpServletRequest request,
                                   HttpSession session,
                                   Vendor vendor)
    throws EnvoyServletException
    {
        List projects = vendor.getProjects();
        Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(request, session, projects,
                           new ProjectComparator(uiLocale),
                           10,
                           PROJECT_LIST,
                           PROJECT_KEY);
    }
}
