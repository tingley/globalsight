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

package com.globalsight.reports.servlet;

import org.apache.log4j.Logger;

import inetsoft.sree.RepletRegistry;
import inetsoft.sree.web.ServletRepository;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.reports.GlobalSightReplet;

/**
 * The ReportsServlet is the repository of replets. It knows
 * how to execute the reports, and knows the listing of reports.
 */
public class ReportsServlet extends ServletRepository
{
    private static final long serialVersionUID = 537412505896417969L;

    private static final Logger CATEGORY =
        Logger.getLogger(
            ReportsServlet.class);

    private static final String LOGIN_PAGE = "/globalsight/wl";
    private static final String ERRORMSG =
        "Invalid Access. Must be logged in to GlobalSight.";

    private boolean m_checkAccess = true;

    public void init(ServletConfig p_config) throws ServletException
    {
        super.init(p_config);

        try
        {
            m_checkAccess = SystemConfiguration.getInstance().getBooleanParameter(
                SystemConfigParamNames.REPORTS_CHECK_ACCESS);

            if (m_checkAccess)
            {
                CATEGORY.info("Checking access to all reports.");
            }
            else
            {
                CATEGORY.info("Not checking access to any reports.");
            }
        }
        catch (Exception e)
        {
            CATEGORY.warn("Problem reading allowUncheckedAccess property from envoy.properties. Checking access to reports.");
            m_checkAccess = true;
        }
    }

    /**
     * Validates access to the reports servlet. Invalid access is
     * forwarded to the login page.
     * @throws ServletException
     */
    public void service(HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws ServletException, IOException
    {
        if (validate(p_request) == false)
        {
            p_response.sendRedirect(LOGIN_PAGE);
        }
        else
        {
            super.service(p_request, p_response);
        }

        //Check if the user saved a new report. If so, refresh the registry.
        try
        {
            String operation = p_request.getParameter("op");

            if (operation != null && operation.startsWith("save"))
            {
                RepletRegistry reg = RepletRegistry.getRegistry();
                reg.clear();
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not refresh registry after report save.", e);
        }
    }

    /**
     * Validates access by looking at the session
     *
     * @param HttpServletRequest
     * @return true|false
     */
    private boolean validate(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        if (session == null || p_request.isRequestedSessionIdValid() == false ||
            session.getAttribute(WebAppConstants.USER_NAME) == null)
        {
            if (m_checkAccess == false)
            {
                return true;
            }
            else
            {
                CATEGORY.error(ERRORMSG);
                return false;
            }
        }
        else
        {
            if (session != null)
            {
                GlobalSightReplet.setUserSessionCache(session);
            }

            return true;
        }
    }
}

