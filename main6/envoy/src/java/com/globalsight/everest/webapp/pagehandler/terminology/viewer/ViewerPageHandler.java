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

package com.globalsight.everest.webapp.pagehandler.terminology.viewer;

import org.apache.log4j.Logger;

import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.TermbaseException;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

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
 * <p>ViewerPageHandler is responsible for executing functions for the
 * Termbase Viewer (aka Browser).</p>
 *
 * <p>Since this class operates on a window that remains open across
 * activities, DO NOT USE THE SESSIONMANAGER.</p>
 */
public class ViewerPageHandler
    extends PageHandler
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ViewerPageHandler.class);

    //
    // Constructor
    //
    public ViewerPageHandler()
    {
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invokes this PageHandler
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

        ITermbase termbase = (ITermbase)session.getAttribute(
            WebAppConstants.TERMBASE);

        String tbidStr = (String)p_request.getParameter(
            WebAppConstants.TERMBASE_TERMBASEID);
        String name = null;

        // If given a termbase name, connect to that termbase
        if (termbase == null || tbidStr != null)
        {
            User user = PageHandler.getUser(session);
            String userId;

            if (user == null)
            {
                userId = ITermbase.ANONYMOUS_USER;
            }
            else
            {
                userId = user.getUserId();
            }

            // TODO: user authentication, error handling
            try
            {
                long tbid = Long.parseLong(tbidStr);
                ITermbaseManager tbm = ServerProxy.getTermbaseManager();
                termbase = tbm.connect(tbid, userId, "");
                if (termbase != null)
                {
                    name = termbase.getName();
                }
            }
            catch (GeneralException ex)
            {
                throw new EnvoyServletException(ex);
            }

            // session holds on to the termbase interface pointer
            session.setAttribute(WebAppConstants.TERMBASE, termbase);
            session.setAttribute(WebAppConstants.TERMBASE_NAME, name);
            session.setAttribute(WebAppConstants.TERMBASE_TERMBASEID, tbidStr);
        }

        // Pass on additional parameters to the viewer like the
        // concept and term to show when loaded the first time.
        session.setAttribute(WebAppConstants.TERMBASE_CONCEPTID,
            p_request.getParameter(WebAppConstants.TERMBASE_CONCEPTID));
        session.setAttribute(WebAppConstants.TERMBASE_TERMID,
            p_request.getParameter(WebAppConstants.TERMBASE_TERMID));

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}

