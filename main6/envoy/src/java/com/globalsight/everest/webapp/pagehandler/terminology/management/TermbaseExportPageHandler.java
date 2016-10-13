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

package com.globalsight.everest.webapp.pagehandler.terminology.management;

import org.apache.log4j.Logger;

import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.audit.TermAuditLog;
import com.globalsight.terminology.audit.TermAuditEvent;

import com.globalsight.exporter.IExportManager;

import com.globalsight.everest.foundation.User;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>This PageHandler is responsible for exporting data from
 * termbases.</p>
 */

public class TermbaseExportPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TermbaseExportPageHandler.class);

    //
    // Static Members
    //
    static private ITermbaseManager s_manager = null;

    //
    // Constructor
    //
    public TermbaseExportPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getTermbaseManager();
            }
            catch (GeneralException ex)
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

        String userId = getUser(session).getUserId();

        String action  = (String)p_request.getParameter(TERMBASE_ACTION);
        String options = (String)p_request.getParameter(TERMBASE_EXPORT_OPTIONS);
        String tbid    = (String)p_request.getParameter(RADIO_BUTTON);
        String name    = null;

        IExportManager exporter =
            (IExportManager)sessionMgr.getAttribute(TERMBASE_EXPORTER);
        ProcessStatus status =
            (ProcessStatus)sessionMgr.getAttribute(TERMBASE_STATUS);

        try
        {
            if (tbid != null)
            {
                name = s_manager.getTermbaseName(Long.parseLong(tbid));
            }

            if (options != null)
            {
                // options are posted as UTF-8 string
                options = EditUtil.utf8ToUnicode(options);
            }

            if (action.equals(TERMBASE_ACTION_EXPORT))
            {
            	if (tbid == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing export");
                }

                ITermbase tb = s_manager.connect(name, userId, "");
                String definition = tb.getDefinition();

                exporter = tb.getExporter();
                options = exporter.getExportOptions();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initial options = " + options);
                }

                sessionMgr.setAttribute(TERMBASE_TB_NAME, name);
                sessionMgr.setAttribute(TERMBASE_TB_ID, tbid);
                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
                sessionMgr.setAttribute(TERMBASE_EXPORT_OPTIONS, options);
                sessionMgr.setAttribute(TERMBASE_EXPORTER, exporter);
            }
            else if (action.equals(TERMBASE_ACTION_ANALYZE_TERMBASE))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
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

                sessionMgr.setAttribute(TERMBASE_EXPORT_OPTIONS, options);
            }
            else if (action.equals(TERMBASE_ACTION_SET_EXPORT_OPTIONS))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
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

                sessionMgr.setAttribute(TERMBASE_EXPORT_OPTIONS, options);
            }
            else if (action.equals(TERMBASE_ACTION_START_EXPORT))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("running export with options = " + options);
                }

                // pass down new options from client
                exporter.setExportOptions(options);
                sessionMgr.setAttribute(TERMBASE_EXPORT_OPTIONS, options);

                // Start Export operation in a separate thread
                try
                {
                    TermAuditEvent event = new TermAuditEvent(
                        new Date(),
                        userId,
                        name,
                        name,
                        "ALL",
                        "export termbase", null);
                        event.details = "starting export of termbase";
                    TermAuditLog.log(event);
                    status = new TermExportProcessStatus(event);
                    status.setResourceBundle(getBundle(session));
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);

                    exporter.attachListener(status);
                    exporter.doExport();
                }
                catch (Throwable ex)
                {
                    // error here
                    CATEGORY.error("Export error occured ", ex);
                }
            }
            else if (action.equals(TERMBASE_ACTION_CANCEL_EXPORT))
            {
                status.interrupt();
            }
            else if (action.equals(TERMBASE_ACTION_DONE) ||
                action.equals(TERMBASE_ACTION_CANCEL))
            {
                // we don't come here, do we??
                sessionMgr.removeElement(TERMBASE_DEFINITION);
                sessionMgr.removeElement(TERMBASE_EXPORTER);
                sessionMgr.removeElement(TERMBASE_EXPORT_OPTIONS);
                sessionMgr.removeElement(WebAppConstants.TERMBASE_STATUS);
            }
        }
        catch (TermbaseException ex)
        {
            CATEGORY.error("export error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.getMessage());
        }
        catch (Throwable ex)
        {
            CATEGORY.error("export error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }

    private class TermExportProcessStatus extends ProcessStatus
    {
        private TermAuditEvent m_event;
        public TermExportProcessStatus()
        {
            super();
            m_event = new TermAuditEvent();
        }

        public TermExportProcessStatus(TermAuditEvent p_event)
        {
            super();
            m_event=p_event;
        }

    public void listen(int p_counter, int p_percentage, String p_message)
        throws IOException
    {
        super.listen(p_counter,p_percentage,p_message);
        if (this.getPercentage()==100)
        {
            m_event.date = new Date();
            m_event.details = "Finished term export";
            TermAuditLog.log(m_event);
        }
    }
    }
}
