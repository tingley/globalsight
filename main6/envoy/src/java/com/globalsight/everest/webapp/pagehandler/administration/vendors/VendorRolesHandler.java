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

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.comparator.VendorRoleComparator;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorRole;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class VendorRolesHandler extends PageHandler
            implements VendorConstants
{
    private int rolesPerPage = 10;

    public VendorRolesHandler()
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            rolesPerPage = sc.getIntParameter(
                SystemConfigParamNames.ROLES_PER_PAGE);
        }
        catch (Exception e)
        {
            // will use initialized defaults
        }
    }

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

        Vendor vendor = (Vendor)sessionMgr.getAttribute("vendor");
        if ("cancel".equals(action))
        {
            sessionMgr.removeElement("sourceLocale");
            sessionMgr.removeElement("targetLocale");
            initRolesTable(request, session, vendor);
        }
        else if ("next".equals(action))
        {
            // Save the data from the cv page
            VendorHelper.saveCV(vendor, request);

            // Get data for the roles page
            setRemainingLocales(vendor, sessionMgr);
            setActivities(request, session, sessionMgr);
        }
        else if ("previous".equals(action))
        {
            // Save the data from the projects page
            VendorHelper.saveProjects(vendor, request);

            // Get data for the roles page
            setRemainingLocales(vendor, sessionMgr);
            setActivities(request, session, sessionMgr);
        }
        else if ("rates".equals(action))
        {
            // The user selected a source and target locale.
            // Get the rates for the activities
            try {
                String src = (String)request.getParameter("srcLocales");
                String targ = (String)request.getParameter("targLocales");
                GlobalSightLocale sourceLocale =
                     ServerProxy.getLocaleManager().getLocaleById(Long.parseLong(src));
                GlobalSightLocale targetLocale =
                     ServerProxy.getLocaleManager().getLocaleById(Long.parseLong(targ));
                sessionMgr.setAttribute("sourceLocale", sourceLocale);
                sessionMgr.setAttribute("targetLocale", targetLocale);
                Locale uiLocale =
                     (Locale)session.getAttribute(WebAppConstants.UILOCALE);
                HashMap activities = VendorHelper.getActivities(sourceLocale,
                                         targetLocale, uiLocale);
                request.setAttribute("activities", activities);
                setRemainingLocales(vendor, sessionMgr);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }
        else if ("roles".equals(action))
        {
            // Save the data from the basic page
            VendorHelper.saveBasicInfo(vendor, request);

            initRolesTable(request, session, vendor);
        }
        else if ("self".equals(action) || "cancel".equals(action))
        {
            // sorting table
            initRolesTable(request, session, vendor);
        }
        else if ("new".equals(action))
        {
            // from editing roles page, user hit the new button
            setRemainingLocales(vendor, sessionMgr);
            setActivities(request, session, sessionMgr);
        }
        else if ("edit".equals(action))
        {
            setSelectedActivities(request, session, sessionMgr, vendor);
        }
        else if ("doneEditRole".equals(action))
        {
            // returned from editing a selected role

            // get the role from vendor with the given src & targ locale
            Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);
            VendorHelper.modifyRole(vendor, request, sessionMgr, uiLocale);
            initRolesTable(request, session, vendor);
        }
        else if ("add".equals(action))
        {
            // returned from adding a new role when creating a vendor
            // create the new role
            Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);
            VendorHelper.newRole(vendor, request, sessionMgr, uiLocale);
            sessionMgr.removeElement("sourceLocale");
            sessionMgr.removeElement("targetLocale");

            // Get data for the roles page
            setRemainingLocales(vendor, sessionMgr);
            setActivities(request, session, sessionMgr);
        }
        else if ("doneNewRole".equals(action) || "add".equals(action))
        {
            // returned from adding a new role when editing a vendor
            Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);
            VendorHelper.newRole(vendor, request, sessionMgr, uiLocale);
            sessionMgr.removeElement("sourceLocale");
            sessionMgr.removeElement("targetLocale");
            initRolesTable(request, session, vendor);
        }
        else
        {
            // sorting
            initRolesTable(request, session, vendor);
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private void initRolesTable(HttpServletRequest request,
                                   HttpSession session,
                                   Vendor vendor)
    throws EnvoyServletException
    {

        // Get list of vendors
        Set roles = (Set)vendor.getRoles();
        ArrayList rolesNoDups;
        // Roles contain a LocalePair and an Activity.  We only want to
        // display the LocalePair once for all it's activities, so create
        // a hashtable so that dup localepairs are removed, then convert
        // back to a list.
        /*
        if (roles != null)
        {
            HashMap hash = new HashMap();
            for (Iterator ri = roles.iterator() ; ri.hasNext() ;)
            {
                VendorRole role = (VendorRole) ri.next();
                if (!hash.containsKey(role.getLocalePair().toString()))
                {
                    hash.put(role.getLocalePair().toString(), role);
                }
            }
            Collection values = hash.values();
            rolesNoDups = new ArrayList(values);
        }
        else
        {
            rolesNoDups = new ArrayList(roles);
        }
        */
        

        Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(request, session, new ArrayList(roles),
                           new VendorRoleComparator(uiLocale),
                           rolesPerPage,
                           ROLE_LIST, ROLE_KEY);
    }
    
    /**
     * Create a hashtable of the localepairs that haven't been set for
     * this vendor yet.  The key the source locale and the target is all
     * passible target locales for that source.
     */
    private void setRemainingLocales(Vendor vendor, SessionManager sessionMgr)
    throws EnvoyServletException
    {
        Set roles = (Set)vendor.getRoles();
        ArrayList currentLocales = new ArrayList();
        if (roles != null)
        {
            for (Iterator ri=roles.iterator() ; ri.hasNext() ; )
            {
                VendorRole vr = (VendorRole)ri.next();
                currentLocales.add(vr.getLocalePair());
            }
            Hashtable localepairs = VendorHelper.getRemainingLocales(currentLocales);
            sessionMgr.setAttribute("remainingLocales", localepairs); 
        }
    }

    private void setSelectedActivities(HttpServletRequest request,
                               HttpSession session,
                               SessionManager sessionMgr,
                               Vendor vendor)
    throws EnvoyServletException
    {
        String src = (String)request.getParameter("sourceLocale");
        String targ = (String)request.getParameter("targetLocale");
        GlobalSightLocale sourceLocale =
             UserHandlerHelper.getLocaleByString(src);
        GlobalSightLocale targetLocale =
             UserHandlerHelper.getLocaleByString(targ);
        try {
            LocalePair lp = ServerProxy.getLocaleManager().getLocalePairBySourceTargetIds(sourceLocale.getId(), targetLocale.getId());
            sessionMgr.setAttribute("localePair", lp);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        Locale uiLocale =
             (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        HashMap activities = VendorHelper.getActivities(sourceLocale,
                 targetLocale, uiLocale);
        request.setAttribute("activities", activities);
        HashMap selectedActivities =
             VendorHelper.getSelectedActivities(vendor, sourceLocale,
                 targetLocale);
        request.setAttribute("selectedActivities", selectedActivities);
    }


    private void setActivities(HttpServletRequest request,
                               HttpSession session,
                               SessionManager sessionMgr)
    throws EnvoyServletException
    {
        Locale uiLocale =
             (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        Vector activities = UserHandlerHelper.getAllActivities(uiLocale);
        // Convert to HashMap (so can reuse activities.jsp)
        HashMap activitiesHash = new HashMap(activities.size());
        for (int i = 0; i < activities.size(); i++)
        {
            Activity activity = (Activity) activities.get(i);
            activitiesHash.put(activity, null);
        }
        request.setAttribute("activities", activitiesHash);
    }
}
