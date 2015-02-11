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
class CustomFormControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            CustomFormControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public CustomFormControlFlowHelper(HttpServletRequest p_request,
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
        String destinationPage = null;
        String action = (String) m_request.getParameter("action");
        if (action == null)
        {
            return "self";
        }
        destinationPage = action;
        if (action.equals("next"))
        {  
            if (CustomPageHelper.getPageTitle() == null)
            {
                destinationPage = "nextNoCustom";
            }
            else
            {
                destinationPage = "next";
            }
        }
        else if (action.startsWith("prev"))
        {  
            if (CustomPageHelper.getPageTitle() == null)
            {
                destinationPage = "prevNoCustom";
            }
            else
            {
                destinationPage = "prev";
            }
        }
        else if (action.equals("modifyProjectsUser") || action.equals("doneSecurity")
                 || action.equals("doneProjects"))
        {  
            destinationPage = "done";
        }
        else
        {
            //unknown action must be handled
            CATEGORY.error("Uknown action " + action + ". Cannot determine next UI screen.");
            destinationPage = "self";
        }

        return destinationPage;
    }
}

