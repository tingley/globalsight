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

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.TmRemover;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * <p>This PageHandler is responsible for reindexing TMs.</p>
 */

public class RemoveTmHandler
    extends PageHandler
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            RemoveTmHandler.class);

    //
    // Static Members
    //
    static private ProjectHandler /*TmManager*/ s_manager = null;

    //
    // Constructor
    //
    public RemoveTmHandler()
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
        String userId = getUser(session).getUserId();
        String action  = (String)p_request.getParameter(TM_ACTION);
        ResourceBundle bundle = PageHandler.getBundle(session);

        try
        {
            if (TM_ACTION_DELETE.equals(action))
            {
                
                String tmIdStr = (String)p_request.getParameter(TM_TM_ID);
                long tmId = Long.parseLong(tmIdStr);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Removing TM (tm id = " + tmIdStr + ")");
                }

                // Start reindex in a separate thread.
                TmRemover tmRemover = new TmRemover(tmId);
                tmRemover.SetDeleteLanguageFlag(false);
                tmRemover.setResourceBundle(bundle);
                tmRemover.initReplacingMessage();
                sessionMgr.setAttribute(TM_REMOVER, tmRemover);

                tmRemover.start();           
            }
            else if (TM_ACTION_CANCEL.equals(action))
            {
                TmRemover tmRemover
                    = (TmRemover)sessionMgr.getAttribute(TM_REMOVER);
                tmRemover.cancelProcess();
            }
            else if("deleteTMLanguage".equals(action)) {
                String tmIdStr = (String)p_request.getParameter(TM_TM_ID);
                long tmId = Long.parseLong(tmIdStr);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Removing TM (tm id = " + tmIdStr + ")");
                }
                
                // Start reindex in a separate thread.
                TmRemover tmRemover = new TmRemover(tmId);
                
                if(p_request.getParameter("LanguageList") != null) {
                    tmRemover.setLocaleId(
                        Long.parseLong(p_request.getParameter("LanguageList")));
                }
                else {
                    throw new ServletException();
                }
                
                tmRemover.SetDeleteLanguageFlag(true);
                tmRemover.setResourceBundle(bundle);
                tmRemover.initReplacingMessage();
                sessionMgr.setAttribute(TM_REMOVER, tmRemover);
                tmRemover.start();
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Tm removal error", ex);
            // JSP needs to clear this.
            sessionMgr.setAttribute(TM_ERROR, ex.getMessage());
        }

        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }
}
