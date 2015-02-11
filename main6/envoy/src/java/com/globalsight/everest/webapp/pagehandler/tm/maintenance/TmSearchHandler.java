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

package com.globalsight.everest.webapp.pagehandler.tm.maintenance;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectHandler;

import com.globalsight.everest.tm.TmManagerLocal;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.everest.tm.searchreplace.SearchReplaceManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * TmSearchHandler is responsible for displaying the Tm search page.
 */
public class TmSearchHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TmSearchHandler.class);

    //
    // Static Members
    //

    // I think we cannot have static member variables.......
    static private ProjectHandler s_manager = null;

    //
    // Constructor
    //
    public TmSearchHandler()
    {
        super();

        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getProjectHandler();
            }
            catch (Exception ignore)
            {
            }
        }
    }

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
        throws ServletException, IOException, EnvoyServletException
    {
        setParameters(p_request);

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }

    //
    // Private Methods
    //
    private void setParameters(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        Locale uiLocale = (Locale)session.getAttribute(UILOCALE);

        //String userId = getUser(session).getUserId();

        String action = (String)p_request.getParameter(TM_ACTION);
        String tmid   = (String)p_request.getParameter(RADIO_TM_ID);

        String name = (String)sessionMgr.getAttribute(TM_TM_NAME);
        ProcessStatus status =
            (ProcessStatus)sessionMgr.getAttribute(TM_TM_STATUS);

        SearchReplaceManager manager = null;

        // Clean up status and results of previous search.
        if (status != null && name != null && name.length() > 0)
        {
            manager = (SearchReplaceManager)sessionMgr.getAttribute(
                TM_CONCORDANCE_MANAGER);
            if (manager != null) {
                manager.detachListener((IProcessStatusListener)status);            	
            }

            sessionMgr.removeElement(TM_TM_STATUS);
            sessionMgr.removeElement(TM_CONCORDANCE_MANAGER);
            sessionMgr.removeElement(TM_CONCORDANCE_SEARCH_RESULTS);
        }

        try
        {
            // Screen accessed for the first time
            if (tmid != null && tmid.length() > 0)
            {
                /*
                name = s_manager.getTmName(Long.parseLong(tmid));
                */
                ProjectTM tm =
                    s_manager.getProjectTMById(Long.parseLong(tmid), false);
                name = tm.getName();

                sessionMgr.setAttribute(TM_TM_ID, tmid);
                sessionMgr.setAttribute(TM_TM_NAME, name);
            }
            // Screen accessed by back button or "previous"
            else
            {
                tmid = (String)sessionMgr.getAttribute(TM_TM_ID);
                name = (String)sessionMgr.getAttribute(TM_TM_NAME);
            }

            // Search and Replace locales, read from the TM.
            List<GlobalSightLocale> localesInTm = TmManagerLocal.getProjectTmLocales(
                name, uiLocale);

            // Source search locales
            p_request.setAttribute(TM_SOURCE_SEARCH_LOCALES, localesInTm);

            // Target search locales
            p_request.setAttribute(TM_TARGET_SEARCH_LOCALES, localesInTm);
        }
        catch (Throwable ex)
        {
            CATEGORY.error(action, ex);

            // JSP needs to clear this.
            sessionMgr.setAttribute(TM_ERROR, ex.toString());
        }
    }
}

