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
import com.globalsight.everest.aligner.AlignmentStatus;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>This page handler is responsible for showing aligner packages,
 * package creation errors, and downloading packages.</p>
 */

public class AlignerPackageDownloadPageHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            AlignerPackageDownloadPageHandler.class);

    //
    // Static Members
    //
    static private AlignerManager s_manager = null;

    //
    // Constructor
    //
    public AlignerPackageDownloadPageHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getAlignerManager();
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
            WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale)session.getAttribute(
            WebAppConstants.UILOCALE);

        //String userId = getUser(session).getUserId();

        String action = p_request.getParameter(GAP_ACTION);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("action = " + action);
        }

        try
        {
            List gapPackages = (List)sessionMgr.getAttribute(GAP_PACKAGES);

            // Execute actions first.
            if (action == null)
            {
                // do nothing, just show list.
            }
            else if (action.equals(GAP_ACTION_REMOVEPACKAGE))
            {
                String packageName = p_request.getParameter(GAP_PACKAGE);
                packageName = EditUtil.utf8ToUnicode(packageName);

                s_manager.deletePackage(packageName);
            }
            else if (action.equals(GAP_ACTION_SHOWERRORS))
            {
                String packageName = p_request.getParameter(GAP_PACKAGE);
                packageName = EditUtil.utf8ToUnicode(packageName);

                AlignmentStatus status = null;

                for (int i = 0, max = gapPackages.size(); i < max; i++)
                {
                    AlignmentStatus s = (AlignmentStatus)gapPackages.get(i);

                    if (s.getPackageName().equals(packageName))
                    {
                        status = s;
                        break;
                    }
                }

                sessionMgr.setAttribute(GAP_PACKAGEINFO, status);
            }
            // TODO: remove an alignment package (changing the logic
            // above where errors are requested for packages that may
            // have been deleted).

            // At the end refresh current package list from server.
            gapPackages = s_manager.getAllPackages();

            sessionMgr.setAttribute(GAP_PACKAGES, gapPackages);
        }
        catch (Exception ex)
        {
            CATEGORY.warn("unexpected error", ex);

            // JSP needs to clear this.
            sessionMgr.setAttribute(GAP_ERROR, ex.toString());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}

