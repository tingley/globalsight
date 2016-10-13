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

package com.globalsight.everest.webapp.pagehandler.edit.online;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;

import com.globalsight.everest.edit.online.PageInfo;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;


import com.globalsight.util.GlobalSightLocale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * <p>EditorPageinfoHandler is responsible for retrieving the page
 * information for the page info dialog.</p>
 *
 * <p>The page info logic has been moved to this class to keep the
 * main page handler logic in EditorPageHandler.java clean.</p>
 */

public class EditorPageInfoHandler
    extends PageHandler
    implements EditorConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
          EditorPageHandler.class);

    //
    // Constructor
    //
    public EditorPageInfoHandler()
    {
        super();
    }

    //
    // Interface Methods: PageHandler
    //

    /**
    * Invokes this PageHandler
    *
    * @param p_thePageDescriptor the page desciptor
    * @param p_theRequest the original request sent from the browser
    * @param p_theResponse the original response object
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
        EditorState state = (EditorState)sessionMgr.getAttribute(
            WebAppConstants.EDITORSTATE);

        // Wed Mar 05 20:23:31 2003 CvdL: page info record now stored
        // in EditorState.
        PageInfo info = state.getPageInfo();

        sessionMgr.setAttribute(WebAppConstants.PAGEINFO, info);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
            p_context);
    }
}
