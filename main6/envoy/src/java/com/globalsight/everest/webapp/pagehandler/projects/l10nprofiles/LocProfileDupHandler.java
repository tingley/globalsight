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
package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * DupWorkflowTemplateHandler is the page handler responsible for
 * displaying a list locales
 */

public class LocProfileDupHandler extends PageHandler
    implements LocProfileStateConstants
{
    
    public LocProfileDupHandler()
    {
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Override Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);

        if (CANCEL_ACTION.equals(p_request.getParameter(ACTION))) 
        {
            // clean session manager
            clearSessionManager(session);
        }

        String id = (String)p_request.getParameter("radioBtn");
        if (id == null
				|| p_request.getMethod().equalsIgnoreCase(
						REQUEST_METHOD_GET)) 
		{
			p_response
					.sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
			return;
		}
        p_request.setAttribute("DupLocProfile", id);//TODO
        getLocales(p_request);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    //////////////////////////////////////////////////////////////////////
    //  End: Override Methods
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Local Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Clear the session manager
     * 
     * @param p_session - The client's HttpSession where the 
     * session manager is stored.
     */
    private void clearSessionManager(HttpSession p_session)
    {
        SessionManager sessionMgr = 
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);

        sessionMgr.clear();
    }

    /**
     * Put the source, target, and locale pairs in the request
     * for the dup workflow page
     */
    private void getLocales(HttpServletRequest p_request)
        throws ServletException, IOException, EnvoyServletException

    {
        List vPairs = null;
        try {
            HttpSession session = p_request.getSession(false);
            Locale uiLocale =
                (Locale)session.getAttribute(WebAppConstants.UILOCALE);
            vPairs = LocProfileHandlerHelper.getAllLocalePairs(uiLocale);
            p_request.setAttribute(ALL_LOCALES, vPairs);
            vPairs = LocProfileHandlerHelper.getAllSourceLocales(uiLocale);
            p_request.setAttribute(SOURCE_LOCALE, vPairs);
            vPairs = LocProfileHandlerHelper.getAllTargetLocales(uiLocale);
            p_request.setAttribute(TARGET_LOCALE, vPairs);
        } catch (Exception e)
        {
            System.out.println("ERROR in getting locales");
        }
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Local Methods
    //////////////////////////////////////////////////////////////////////

}
