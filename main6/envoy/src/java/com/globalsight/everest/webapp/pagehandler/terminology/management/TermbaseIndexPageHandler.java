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
import com.globalsight.terminology.indexer.IIndexManager;

import com.globalsight.util.progress.IProcessStatusListener2;
import com.globalsight.util.progress.ProcessStatus2;

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

import java.io.IOException;
import java.util.ArrayList;
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
 * <p>This PageHandler is responsible for creating termbase indexes.</p>
 */
public class TermbaseIndexPageHandler
    extends PageHandler
    implements WebAppConstants
{
    static private final Logger CATEGORY =
        Logger.getLogger(
            TermbaseIndexPageHandler.class);

    //
    // Static Members
    //
    static private ITermbaseManager s_manager = null;

    //
    // Constructor
    //
    public TermbaseIndexPageHandler()
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
                // ignore.
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

        String userId = getUser(session).getUserId();

        String action = (String)p_request.getParameter(TERMBASE_ACTION);
        String tbid   = (String)p_request.getParameter(RADIO_BUTTON);
        String name = null;

        try
        {
            if (tbid != null)
            {
                name = s_manager.getTermbaseName(Long.parseLong(tbid));

                sessionMgr.setAttribute(TERMBASE_TB_ID, tbid);
                sessionMgr.setAttribute(TERMBASE_TB_NAME, name);
            }

            if (action == null)
            {
                name = (String)sessionMgr.getAttribute(TERMBASE_TB_NAME);

                // load existing definition and statistics for status
                String definition = s_manager.getDefinition(name, false);
                String statistics = s_manager.getStatistics(name);

                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
                sessionMgr.setAttribute(TERMBASE_STATISTICS, statistics);
            }
            else if (action.equals(TERMBASE_ACTION_INDEX))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                name = (String)sessionMgr.getAttribute(TERMBASE_TB_NAME);
                ITermbase tb = s_manager.connect(name, userId, "");

                try
                {
                    IIndexManager indexer = tb.getIndexer();

                    ProcessStatus2 status = new ProcessStatus2();
                    status.setResourceBundle(getBundle(session));
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);

                    indexer.attachListener(status);
                    indexer.doIndex();
                }
                catch (Throwable ex)
                {
                    CATEGORY.error("cannot start indexing ", ex);
                    sessionMgr.setAttribute(TERMBASE_ERROR, ex.toString());
                }
            }
            else if (action.equals(TERMBASE_ACTION_REFRESH))
            {
                // Do nothing. Let progress bar refresh.
            }
            else if (action.equals(TERMBASE_ACTION_CANCEL))
            {
                // Cancel indexing.
                ProcessStatus2 status =
                    (ProcessStatus2)sessionMgr.getAttribute(TERMBASE_STATUS);

                status.interrupt();

                sessionMgr.removeElement(TERMBASE_IMPORTER);
                sessionMgr.removeElement(TERMBASE_STATUS);
            }
        }
        catch (TermbaseException ex)
        {
            CATEGORY.error("indexing error ", ex);

            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}

