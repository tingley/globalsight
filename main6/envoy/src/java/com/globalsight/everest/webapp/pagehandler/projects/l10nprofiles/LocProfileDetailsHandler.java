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

// Envoy packages
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

//Sun
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LocProfileDetailsHandler
    extends PageHandler
{

    /**
     * Invokes this PageHandler
     * <p>
     * @param p_pageDescriptor the page descriptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {

        String id = (String)p_request.getParameter(RADIO_BUTTON);
        if (id == null
				|| p_request.getMethod().equalsIgnoreCase(
						REQUEST_METHOD_GET)) 
		{
			p_response
					.sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
			return;
		}
        BasicL10nProfile locprofile = (BasicL10nProfile)
                LocProfileHandlerHelper.getL10nProfile(Long.parseLong(id));
        p_request.setAttribute("locprofile", locprofile);
        
        super.invokePageHandler(p_pageDescriptor, p_request,
                                p_response,p_context);
    }
}
