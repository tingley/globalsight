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

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.vendormanagement.Rating;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The page handler for displaying the list of vendor ratings.
 */

public class VendorRatingsHandler extends PageHandler
    implements VendorConstants
{
    private int vendorsPerPage = 10;
    
    public VendorRatingsHandler()
    {
        try 
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            vendorsPerPage = sc.getIntParameter(
                SystemConfigParamNames.VENDORS_PER_PAGE);
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
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        String action = (String)request.getParameter("action");
        Vendor vendor = getVendor(request, sessionMgr);

        if ("save".equals(action))
        {
            // Save a new rating or update an existing rating

            boolean newRate = false;
            Rating rating = (Rating) sessionMgr.getAttribute("rating");
            if (rating == null)
            {
                newRate = true;
            }
            User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
            rating = VendorHelper.saveRating(rating, request, user.getUserId(), null);
            if (newRate)
                VendorHelper.addRating(vendor, rating, user);
            else
                VendorHelper.updateRating(vendor, rating, user);

            sessionMgr.removeElement("edit");
            sessionMgr.removeElement("rating");
        }
        else if ("cancel".equals(action))
        {
            sessionMgr.removeElement("rating");
            sessionMgr.removeElement("edit");
        }
        else if ("remove".equals(action))
        {
            User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
            Rating rating = (Rating) sessionMgr.getAttribute("rating");
            long id = Long.parseLong(request.getParameter("rateId"));
            VendorHelper.removeRating(user, vendor, id);
        }
        if ("save".equals(action) || "remove".equals(action))
        {
            // refetch the updated vendor
            vendor = VendorHelper.getVendor(vendor.getId());
            sessionMgr.setAttribute("vendor", vendor);
        }

        initRatingsTable(request, session, sessionMgr, vendor);
        request.setAttribute("average_rating", String.valueOf(vendor.getAverageRating()));
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
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

        return new RatingControlFlowHelper(p_request, p_response);
    }

    private Vendor getVendor(HttpServletRequest request, SessionManager sessionMgr)
        throws EnvoyServletException
    {
        Vendor vendor;
        String id = (String) request.getParameter("id");
        if (id != null)
        {
            vendor = VendorHelper.getVendor(Long.parseLong(id));
            sessionMgr.setAttribute("vendor", vendor);
        }
        else
        {
            vendor = (Vendor) sessionMgr.getAttribute("vendor");
        }
        return vendor;
    }

    private void initRatingsTable(HttpServletRequest request,
                                  HttpSession session,
                                  SessionManager sessionMgr,
                                  Vendor vendor)
    throws EnvoyServletException
    {

        // Get list of ratings
        List ratings =  vendor.getRatings();
        Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(request, session, ratings, 
                           new RatingComparator(uiLocale),
                           vendorsPerPage, 
                           RATE_LIST, RATE_KEY);
    }

}
