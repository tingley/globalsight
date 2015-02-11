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

package com.globalsight.everest.webapp.pagehandler.terminology.maintenance;

import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.searchreplace.ISearchReplaceManager;
import com.globalsight.terminology.searchreplace.SearchReplaceParams;
import com.globalsight.terminology.searchreplace.SearchResults;
import com.globalsight.terminology.TermbaseException;

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

import com.globalsight.log.GlobalSightCategory;
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
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>PageHandler is responsible creating, deleting and modifying
 * termbases.</p>
 */

public class TermbaseMaintenancePageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            TermbaseMaintenancePageHandler.class);

    //
    // Static Members
    //
    static private ITermbaseManager s_manager = null;

    //
    // Constructor
    //
    public TermbaseMaintenancePageHandler()
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
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            SESSION_MANAGER);
        String userId = getUser(session).getUserId();
        String action = (String)p_request.getParameter(TERMBASE_ACTION);
        String tbid   = (String)p_request.getParameter(RADIO_BUTTON);
        String name = null;
        ISearchReplaceManager manager = null;
        ProcessStatus status = (ProcessStatus)sessionMgr.getAttribute(TERMBASE_STATUS);

        try
        {
            if (tbid != null)
            {
                name = s_manager.getTermbaseName(Long.parseLong(tbid));
                sessionMgr.setAttribute(TERMBASE_TB_ID, tbid);
                sessionMgr.setAttribute(TERMBASE_TB_NAME, name);
            }
            else
            {
                tbid = (String)sessionMgr.getAttribute(TERMBASE_TB_ID);
                name = (String)sessionMgr.getAttribute(TERMBASE_TB_NAME);
            }
            String replace =
                (String)p_request.getParameter(TERMBASE_REPLACE);
            String smartReplace =
                (String)p_request.getParameter(TERMBASE_SMARTREPLACE);
            if (action == null || action.equals(TERMBASE_ACTION_MAINTENANCE))
            {
            	if (tbid == null
						|| p_request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                // show main screen with search options
                String definition = s_manager.getDefinition(name, false);
                sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
            }
            else if (action.equals(TERMBASE_ACTION_SEARCH))
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                // Search the termbase for matching entries
                String search =
                    (String)p_request.getParameter(TERMBASE_SEARCH);
                String caseInsensitive =
                    (String)p_request.getParameter(TERMBASE_CASEINSENSITIVE);
                String level =
                    (String)p_request.getParameter(TERMBASE_LEVEL);
                String language =
                    (String)p_request.getParameter(TERMBASE_LANGUAGE);
                String field =
                    (String)p_request.getParameter(TERMBASE_FIELD);
                String fieldName =
                    (String)p_request.getParameter(TERMBASE_FIELDNAME);

                search = EditUtil.utf8ToUnicode(search);
                replace = EditUtil.utf8ToUnicode(replace);
                language = EditUtil.utf8ToUnicode(language);
                fieldName = EditUtil.utf8ToUnicode(fieldName);

                SearchReplaceParams params = new SearchReplaceParams(
                    search, replace, level, language, field, fieldName,
                    caseInsensitive, smartReplace);
                sessionMgr.setAttribute(TERMBASE_SEARCHCONDITION, params);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("initializing TB search & replace in TB " +
                        name + " -- " + params.toString());
                }

                ITermbase tb = s_manager.connect(name, userId, "");
                manager = tb.getSearchReplaceManager();
                sessionMgr.setAttribute(TERMBASE_SEARCHREPLACER, manager);

                status = new ProcessStatus();
                status.setResourceBundle(getBundle(session));
                sessionMgr.setAttribute(TERMBASE_STATUS, status);
                manager.attachListener((IProcessStatusListener)status);
                manager.search(params);
            }
            else if (action.equals(TERMBASE_ACTION_REFRESH))
            {
                // do nothing
            }
            else
            {
            	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}
                SearchReplaceParams params = (SearchReplaceParams)
                    sessionMgr.getAttribute(TERMBASE_SEARCHCONDITION);
                manager = (ISearchReplaceManager)
                    sessionMgr.getAttribute(TERMBASE_SEARCHREPLACER);
                String indexes =
                    (String)p_request.getParameter(TERMBASE_REPLACEINDEX);
                replace = EditUtil.utf8ToUnicode(replace);
                params.setReplaceText(replace);
                params.setSmartReplace(smartReplace);

                if (action.equals(TERMBASE_ACTION_SHOWPREVIOUS))
                {
                    SearchResults results = manager.getPreviousResults();
                    sessionMgr.setAttribute(TERMBASE_SEARCHRESULTS, results);
                }
                else if (action.equals(TERMBASE_ACTION_SHOWNEXT))
                {
                    SearchResults results = manager.getNextResults();
                    sessionMgr.setAttribute(TERMBASE_SEARCHRESULTS, results);
                }
                else if (action.equals(TERMBASE_ACTION_REPLACE))
                {
                    params.clearReplaceIndexes();
                    params.addAllReplaceIndexes(toArrayList(indexes));
                    manager.replace(params);
                }
                else if (action.equals(TERMBASE_ACTION_REPLACEALL))
                {
                    status = new ProcessStatus();
                    sessionMgr.setAttribute(TERMBASE_STATUS, status);
                    manager.attachListener((IProcessStatusListener)status);
                    manager.replaceAll(params);
                }
            }
        }
        catch (TermbaseException ex)
        {
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }

    private ArrayList toArrayList(String p_ids)
    {
        ArrayList result = new ArrayList();

        String[] ids = p_ids.split(",");

        for (int i = 0, max = ids.length; i < max; i++)
        {
            String id = ids[i];

            if (id != null && id.length() > 0)
            {
                result.add(Long.valueOf(id));
            }
        }

        return result;
    }
}

