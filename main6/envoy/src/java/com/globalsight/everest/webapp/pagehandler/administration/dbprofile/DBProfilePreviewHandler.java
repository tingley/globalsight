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
package com.globalsight.everest.webapp.pagehandler.administration.dbprofile;

// Envoy packages
import java.io.IOException;

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
 * DBProfilePreviewHandler, A page handler to produce the entry page(index.jsp) for DataSources management.
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class DBProfilePreviewHandler extends PageHandler
{
    /**
     * Invokes this PageHandler
     * <p>
     * @param p_thePageDescriptor the page descriptor
     * @param p_theRequest the original request sent from the browser
     * @param p_theResponse the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
				  HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
				  ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        // put user input into session
        HttpSession httpSession = p_theRequest.getSession();
        SessionManager sessionMgr = 
            (SessionManager)httpSession.getAttribute(
                WebAppConstants.SESSION_MANAGER);
        if (p_theRequest.getParameter("acquisitionConn") != null)
            sessionMgr.setAttribute("acquisitionConn", p_theRequest.getParameter("acquisitionConn"));
        if (p_theRequest.getParameter("acquisitionSQL") != null)
            sessionMgr.setAttribute("acquisitionSQL", p_theRequest.getParameter("acquisitionSQL"));

        super.invokePageHandler(p_thePageDescriptor, p_theRequest, 
                                p_theResponse, p_context);
    }
}
