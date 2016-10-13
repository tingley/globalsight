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

package com.globalsight.everest.webapp.pagehandler.guides;

// Envoy packages
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;


// Java/SUN packages
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class GuideHandler extends PageHandler
{
    public GuideHandler()
    {
    }

  /**
   * Invokes this PageHandler
   *
   * @param jspURL the URL of the JSP to be invoked
   * @param the original request sent from the browser
   * @param the original response object
   * @param context the Servlet context
   */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
                HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
                ServletContext p_context) 
    throws ServletException, IOException
    {
        dispatchJSP(p_thePageDescriptor, p_theRequest,p_theResponse, p_context);
    }

  /**
   * Invoke the correct JSP for this page
   */
    private void dispatchJSP(WebPageDescriptor p_thePageDescriptor,
                HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
                ServletContext p_context) 
    throws ServletException, IOException
    {
        //invoke JSP
        RequestDispatcher theDispatcher = p_context.getRequestDispatcher(p_thePageDescriptor.getJspURL());
        theDispatcher.forward( p_theRequest, p_theResponse);
    }

}



