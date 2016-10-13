/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.webapp.pagehandler.administration.cvsjob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.cvsconfig.CVSServer;
import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.ProjectComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.SortUtil;

public class CVSUploadHandler extends PageHandler
{

    private static final Logger s_logger = Logger
            .getLogger(CVSUploadHandler.class);

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
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        try
        {
            sessionMgr.setAttribute("remainingLocales",
                    VendorHelper.getRemainingLocales(new ArrayList<Object>()));
            sessionMgr.setAttribute("jobType", "cvsJob");
            prepareListOfProjects(session, sessionMgr);

            setProjectOrDivisionLabel(sessionMgr, session);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Set the project label and JavaScript warning message based on the
     * property set in envoy.properties.
     */
    private void setProjectOrDivisionLabel(SessionManager p_sessionMgr,
            HttpSession p_session) throws Exception
    {
        // Set the label for project (Project vs. Division)
        SystemConfiguration sc = SystemConfiguration.getInstance();
        boolean isDivision = sc
                .getBooleanParameter(SystemConfigParamNames.IS_DELL);
        ResourceBundle bundle = PageHandler.getBundle(p_session);
        String projectLabel = isDivision ? "lb_division" : "lb_project";
        p_sessionMgr.setAttribute(WebAppConstants.PROJECT_LABEL,
                bundle.getString(projectLabel));

        String projectJsMsg = isDivision ? "jsmsg_select_division"
                : "jsmsg_select_project";
        p_sessionMgr.setAttribute(WebAppConstants.PROJECT_JS_MSG,
                bundle.getString(projectJsMsg));
    }

    private void prepareListOfProjects(HttpSession p_session,
            SessionManager p_sessionMgr) throws Exception
    {
        // now get the projects.
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        User user = (User) p_sessionMgr.getAttribute(WebAppConstants.USER);

        List projectInfos = ServerProxy.getProjectHandler()
                .getProjectInfosByUser(user.getUserId());

        if (projectInfos != null)
        {
            if (projectInfos.size() > 0)
            {
                ProjectComparator pc = new ProjectComparator(uiLocale);
                SortUtil.sort(projectInfos, pc);
            }
            p_sessionMgr.setAttribute("projectInfos", projectInfos);
        }

        CVSServerManagerLocal manager = new CVSServerManagerLocal();
        ArrayList<CVSServer> servers = (ArrayList<CVSServer>) manager
                .getAllServer();
        SortUtil.sort(servers, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((CVSServer) o1).getName().compareToIgnoreCase(
                        ((CVSServer) o2).getName());
            }
        });

        p_sessionMgr.setAttribute("cvsservers", servers);
    }
}
