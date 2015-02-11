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

package com.globalsight.everest.webapp.pagehandler.aligner;

import org.apache.log4j.Logger;

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

import com.globalsight.everest.aligner.AlignerManager;
import com.globalsight.everest.aligner.AlignerManagerException;
import com.globalsight.everest.aligner.AlignerPackageOptions;
import com.globalsight.everest.aligner.AlignerPackageUploadOptions;

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
 * This page handler is the aligner packages main handler but does
 * nothing.
 */

public class AlignerPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            AlignerPageHandler.class);

    //
    // Constructor
    //
    public AlignerPageHandler()
    {
        super();
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

        /*
        Locale uiLocale = (Locale)session.getAttribute(
            WebAppConstants.UILOCALE);

        String userId = getUser(session).getUserId();
        */

        // Clean up stuff from other page handlers.

        // Package Creation:
        sessionMgr.removeElement(GAP_OPTIONS);
        sessionMgr.removeElement(GAP_FILELIST);
        sessionMgr.removeElement(GAP_FORMATTYPES);
        sessionMgr.removeElement(GAP_RULES);
        sessionMgr.removeElement(GAP_LOCALES);
        sessionMgr.removeElement(GAP_ENCODINGS);
        sessionMgr.removeElement(GAP_EXTENSIONS);
        sessionMgr.removeElement(GAP_TMS);

        // Other:
        sessionMgr.removeElement(GAP_ERROR);

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}

