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

package com.globalsight.everest.webapp.pagehandler.tm.management;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmManager;
import com.globalsight.everest.tm.TmManagerLocal;
import com.globalsight.everest.projecthandler.ProjectHandler;

import com.globalsight.exporter.IExportManager;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>This PageHandler is responsible for exporting data from TMs.</p>
 */

public class TmExportPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TmExportPageHandler.class);

    //
    // Static Members
    //
    static private ProjectHandler /*TmManager*/ s_manager = null;

    //
    // Constructor
    //
    public TmExportPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getProjectHandler()/*getTmManager()*/;
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
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        Locale uiLocale = (Locale)session.getAttribute(UILOCALE);
        ResourceBundle bundle = getBundle(session);

        String userId = getUser(session).getUserId();

        String action  = (String)p_request.getParameter(TM_ACTION);
        String options = (String)p_request.getParameter(TM_EXPORT_OPTIONS);
        String tmid    = (String)p_request.getParameter(RADIO_TM_ID);
        String name    = null;
        String definition = null;
        String tmtype = "TM2";
        Tm tm = null;

        IExportManager exporter =
            (IExportManager)sessionMgr.getAttribute(TM_EXPORTER);

        ProcessStatus status =
            (ProcessStatus)sessionMgr.getAttribute(TM_TM_STATUS);

        try
        {
            if (tmid != null)
            {
                // tm = s_manager.getTmById(Long.parseLong(tmid));
                tm = s_manager.getProjectTMById(Long.parseLong(tmid), false);
                name = tm.getName();

                // JSP needs the language names, so get the statistics
                // and pass that as long as a TM has no proper definition.
                // Note that the JSP uses both the languages and whether or 
                // not the TM is empty.
                definition = LingServerProxy.getTmCoreManager()
                        .getTmStatistics(tm, uiLocale, true).asXML(true);
                
                Long tm3id = tm.getTm3Id();
                tmtype = tm3id == null ? "TM2" : "TM3";
            }

            if (options != null)
            {
                // options are posted as UTF-8 string
                options = EditUtil.utf8ToUnicode(options);
                int index = options.indexOf("</exportOptions>");
                if (index > 0)
                {
                    options = options.substring(0, index + "</exportOptions>".length());
                }
            }

            if (action.equals(TM_ACTION_EXPORT))
            {
            	if (tmid == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=tm");
					return;
				}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing export");
                }

                //exporter = s_manager.getExporter(name);
                exporter = TmManagerLocal.getProjectTmExporter(name);

                options = exporter.getExportOptions();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initial options = " + options);
                }

                sessionMgr.setAttribute(TM_TM_NAME, name);
                sessionMgr.setAttribute(TM_TM_ID, tmid);
                sessionMgr.setAttribute(TM_DEFINITION, definition);
                sessionMgr.setAttribute(TM_PROJECT, definition);
                sessionMgr.setAttribute(TM_EXPORT_OPTIONS, options);
                sessionMgr.setAttribute(TM_EXPORTER, exporter);
                sessionMgr.setAttribute(TM_TYPE, tmtype);
            }
            else if (action.equals(TM_ACTION_ANALYZE_TM))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=tm");
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

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("analysis options = " + options);
                }

                sessionMgr.setAttribute(TM_EXPORT_OPTIONS, options);
            }
            else if (action.equals(TM_ACTION_SET_EXPORT_OPTIONS))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=tm");
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
							.sendRedirect("/globalsight/ControlServlet?activityName=tm");
					return;
				}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("running export with options = " + options);
                }

                // pass down new options from client
                exporter.setExportOptions(options);

                // Let the jsp page show progress of export
                sessionMgr.setAttribute(TM_EXPORT_OPTIONS, options);

                // Start export in a separate thread.
                try
                {
                    status = new ProcessStatus();
                    status.setResourceBundle(bundle);
                    sessionMgr.setAttribute(TM_TM_STATUS, status);

                    exporter.attachListener(status);
                    exporter.doExport();
                }
                catch (Throwable ex)
                {
                    CATEGORY.error("Export error occured ", ex);
                }
            }
            else if (action.equals(TM_ACTION_CANCEL_EXPORT))
            {
                status.interrupt();
            }
            else if (action.equals(TM_ACTION_CANCEL) ||
                action.equals(TM_ACTION_DONE))
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

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}
