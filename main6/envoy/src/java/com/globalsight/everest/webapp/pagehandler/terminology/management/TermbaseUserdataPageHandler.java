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
import com.globalsight.terminology.IUserdataManager;
import com.globalsight.terminology.TermbaseException;

import com.globalsight.everest.company.CompanyThreadLocal;
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
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>This PageHandler is responsible for allowing access to a
 * termbases user data section.</p>
 */
public class TermbaseUserdataPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TermbaseUserdataPageHandler.class);

    //
    // Static Members
    //
    static private ITermbaseManager s_manager = null;

    //
    // Constructor
    //
    public TermbaseUserdataPageHandler()
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
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);

        String userId = getUser(session).getUserId();

        IUserdataManager userdata = (IUserdataManager)sessionMgr.getAttribute(
            TERMBASE_USERDATA);

        String action  = (String)p_request.getParameter(TERMBASE_ACTION);
        String tbid    = (String)p_request.getParameter(RADIO_BUTTON);
        String name    = null;

        try
        {
            if (tbid != null)
            {
                name = s_manager.getTermbaseName(Long.parseLong(tbid));
            }

            if (action == null)
            {
                // do nothing
            }
            else if (action.equals(TERMBASE_ACTION_INPUT_MODELS))
            {
            	if (tbid == null) 
				{
					p_response
							.sendRedirect("/globalsight/ControlServlet?activityName=termbases");
					return;
				}

                if (userdata == null ||
                    !userdata.getTermbaseName().equals(name))
                {
                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("initializing user data");
                    }

                    ITermbase tb = s_manager.connect(name, userId, "");
                    String definition = tb.getDefinition();

                    userdata = tb.getUserdataManager();

                    sessionMgr.setAttribute(TERMBASE, tb);
                    sessionMgr.setAttribute(TERMBASE_DEFINITION, definition);
                    sessionMgr.setAttribute(TERMBASE_USERDATA, userdata);
                    sessionMgr.setAttribute(TERMBASE_TB_NAME, name);
                    sessionMgr.setAttribute(TERMBASE_TB_ID, tbid);
                }

                List list = 
                    userdata.doGetInputModelList(IUserdataManager.TYPE_INPUTMODEL, "");

                sessionMgr.setAttribute(TERMBASE_OBJECT_NAMELIST, list);
            }
            else if (action.equals(TERMBASE_ACTION_CANCEL)  ||
                action.equals(TERMBASE_ACTION_DONE))
            {
                // we don't come here, do we??
                sessionMgr.removeElement(TERMBASE);
                sessionMgr.removeElement(TERMBASE_DEFINITION);
                sessionMgr.removeElement(TERMBASE_USERDATA);
                sessionMgr.removeElement(TERMBASE_OBJECT_NAMELIST);
                sessionMgr.removeElement(TERMBASE_OBJECT_TYPE);
                sessionMgr.removeElement(TERMBASE_OBJECT_USER);
                sessionMgr.removeElement(TERMBASE_OBJECT_NAME);
                sessionMgr.removeElement(TERMBASE_OBJECT_VALUE);
            }
        }
        catch (TermbaseException ex)
        {
            CATEGORY.error("Termbase error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.getMessage());
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Termbase error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TERMBASE_ERROR, ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}
