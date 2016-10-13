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

package com.globalsight.everest.webapp.pagehandler.administration.projects;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.exporter.IExportManager;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.progress.ProcessStatus;

/**
 * <p>
 * This PageHandler is responsible for exporting data from TMs.
 * </p>
 */

public class ProjectExportPageHandler extends PageHandler implements
        WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(ProjectExportPageHandler.class);

    //
    // Static Members
    //
    static private ProjectHandler s_manager = null;

    //
    // Constructor
    //
    public ProjectExportPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getProjectHandler();
            }
            catch (Exception ex)
            {
                CATEGORY.error("Initialization failed.", ex);
            }
        }
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler.
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
                .getAttribute(SESSION_MANAGER);

        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);

        String userId = getUser(session).getUserId();

        String action = (String) p_request.getParameter(TM_ACTION);
        String options = (String) p_request.getParameter(TM_EXPORT_OPTIONS);
        String tmid = (String) p_request.getParameter(RADIO_BUTTON);
        String name = null;
        Project project = null;
        Object[] projectMembers = null;

        IExportManager exporter = (IExportManager) sessionMgr
                .getAttribute(TM_EXPORTER);
        ProcessStatus status = (ProcessStatus) sessionMgr
                .getAttribute(TM_TM_STATUS);

        try
        {
            if (tmid != null)
            {
                project = s_manager.getProjectById(Long.parseLong(tmid));
                name = project.getName();
                projectMembers = project.getUserIds().toArray();
            }

            if (options != null)
            {
                // options are posted as UTF-8 string
                options = EditUtil.utf8ToUnicode(options);
            }

            if (action.equals(TM_ACTION_EXPORT))
            {
                if (tmid == null
                        || p_request.getMethod().equalsIgnoreCase(
                                REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=projects");
                    return;
                }
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing export");
                }

                exporter = s_manager.getProjectDataExportManager(
                        getUser(session), Long.parseLong(tmid));

                options = exporter.getExportOptions();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initial options = " + options);
                }

                sessionMgr.setAttribute(TM_TM_NAME, name);
                sessionMgr.setAttribute(TM_TM_ID, tmid);
                sessionMgr.setAttribute(TM_EXPORT_OPTIONS, options);
                sessionMgr.setAttribute(TM_EXPORTER, exporter);
                sessionMgr.setAttribute(USER_NAMES,
                        UserUtil.convertUserIdsToUserNames(projectMembers));
                sessionMgr.setAttribute(COMPANY_NAME, CompanyWrapper
                        .getCompanyNameById(project.getCompanyId()));
            }
            else if (action.equals(TM_ACTION_ANALYZE_TM))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=projects");
                    return;
                }
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("options from client= " + options);
                }

                // pass down new options from client (won't reanalyze files)
                exporter.setExportOptions(options);

                // then retrieve new options
                options = exporter.analyze();
                sessionMgr.setAttribute(TM_EXPORT_OPTIONS, options);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("analysis options = " + options);
                }
            }
            else if (action.equals(TM_ACTION_CANCEL_EXPORT))
            {
                status.interrupt();
            }
            else if (action.equals(TM_ACTION_SET_EXPORT_OPTIONS))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=projects");
                    return;
                }
                // pass down new options from client (won't reanalyze files)

                // testrun may come here without setting options
                if (options != null)
                {
                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("options from client = " + options);
                    }

                    exporter.setExportOptions(options);
                }
                else
                {
                    options = exporter.getExportOptions();
                }

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("options = " + options);
                }

                sessionMgr.setAttribute(TM_EXPORT_OPTIONS, options);
            }
            else if (action.equals(TM_ACTION_START_EXPORT))
            {
                if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET))
                {
                    p_response
                            .sendRedirect("/globalsight/ControlServlet?activityName=projects");
                    return;
                }
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("running export with options = " + options);
                }

                // pass down new options from client
                exporter.setExportOptions(options);
                sessionMgr.setAttribute(TM_EXPORT_OPTIONS, options);

                try
                {
                    status = new ProcessStatus();
                    sessionMgr.setAttribute(TM_TM_STATUS, status);

                    exporter.attachListener(status);
                    exporter.doExport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("Export error occured ", ex);
                }
            }
            else if (action.equals(TM_ACTION_CANCEL)
                    || action.equals(TM_ACTION_DONE))
            {
                // we don't come here, do we??
                sessionMgr.removeElement(TM_EXPORTER);
                sessionMgr.removeElement(TM_EXPORT_OPTIONS);
                sessionMgr.removeElement(TM_TM_STATUS);
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error("export error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TM_ERROR, ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
}
