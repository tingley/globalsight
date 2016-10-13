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

import org.apache.log4j.Logger;


import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.vendormanagement.Rating;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorException;
import com.globalsight.everest.foundation.User;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Dispatches the user to the correct JSP.
 */
class RatingControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            RatingControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public RatingControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    /**
     * Does the processing then 
     * returns the name of the link to follow
     * 
     * @return 
     * @exception EnvoyServletException
     */
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        HttpSession p_session = m_request.getSession(false);
        String action = (String) m_request.getParameter("action");
        String destinationPage = action;

        // action should only be null for paging purposes
        if (action == null) 
        {
            destinationPage = "self";
        }
        else if (action.equals("new"))
        {  
            destinationPage = "new1";
        }
        else if (action.equals("edit"))
        {  
            destinationPage = "edit";
        }
        else if (action.equals("remove"))
        {  
            HttpSession session = m_request.getSession(false);
            SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
            User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
            Vendor vendor = (Vendor)sessionMgr.getAttribute("vendor");
            String id = m_request.getParameter("rateId");

            try
            {
                VendorHelper.removeRating(user, vendor, Long.parseLong(id));
            }
            catch(GeneralException ge)
            {
                throw new EnvoyServletException(ge);
            }
            catch(RemoteException ge)
            {
                throw new EnvoyServletException(ge);
            }
            destinationPage = "remove";
        }
        return destinationPage;
    }
}

