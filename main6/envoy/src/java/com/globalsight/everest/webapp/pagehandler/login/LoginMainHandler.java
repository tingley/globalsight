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

package com.globalsight.everest.webapp.pagehandler.login;

import org.apache.log4j.Logger;

// Envoy packages
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.netegrity.Netegrity;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

// JDK/Servlet
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * This page handler produces the index.jsp page in login module.
 */
public class LoginMainHandler extends PageHandler
{    
    private static Logger s_logger = Logger.getLogger(LoginMainHandler.class.getName());
    
    // The supported UI locales will remain the same as long as
    // the server is up and running.
    private String[] m_supportedLocales = null;
    private String m_defaultLocale = null;
    public static String m_enableSSO = null;

    /**
     * Invokes this PageHandler
     *
     * @param jspURL the URL of the JSP to be invoked
     * @param the original request sent from the browser
     * @param the original response object
     * @param context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException
    {
        //In case the "logout" link is clicked, invalidate the session
        HttpSession session = p_request.getSession(true);
        session.invalidate();

        initialize();
        
        // Trigger to clear cached Tu/Tuv objects to reduce memory.
        SegmentTuTuvCacheManager.clearUnTouchedTuTuvs();

        // turn off cache.  do both.  "pragma" for the older browsers.
        p_response.setHeader("Pragma", "no-cache"); //HTTP 1.0
        p_response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
        p_response.addHeader("Cache-Control", "no-store"); // tell proxy not to cache
        p_response.addHeader("Cache-Control", "max-age=0"); // stale right away

        p_request.setAttribute(SystemConfiguration.UI_LOCALES, m_supportedLocales);
        p_request.setAttribute(SystemConfiguration.DEFAULT_UI_LOCALE, m_defaultLocale);
        p_request.setAttribute(SystemConfiguration.ENABLE_SSO, m_enableSSO);

        if (Netegrity.isNetegrityEnabled())
        {
            //see if this is a login or logout
            if (p_request.getParameter("activityName") != null)
            {
                s_logger.info("Redirecting to Netegrity Logout URL.");
                p_response.sendRedirect(Netegrity.getNetegrityLogoutUrl());
            }
            else
            {
                s_logger.info("Redirecting to Netegrity Login URL. All Logins must come through Netegrity.");
                p_response.sendRedirect(Netegrity.getNetegrityLoginUrl());
            }
        }
        else
        {
            dispatchJSP(p_pageDescriptor, p_request, p_response, p_context);
        }
    }

    /**
     * Invoke the correct JSP for this page
     */
    protected void dispatchJSP(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException
    {
        // create the java beans and pass them to the request. use
        // dummy link, real link will be determined after the user
        // navigates out of the page
    	NavigationBean bean = null;
        
        //invoke JSP
        RequestDispatcher dispatcher;
        String linkName = p_request.getParameter("linkName");
        if (null != linkName && linkName.equals("resetPass"))
        {
            dispatcher = p_context.getRequestDispatcher("/envoy/login/resetPass.jsp");
            bean = new NavigationBean(DUMMY_LINK, "/envoy/login/resetPass.jsp");
            p_request.setAttribute(LoginConstants.form_action, "/globalsight/ControlServlet?linkName=resetPass&pageName=retrieve");
        }
        else if (null != linkName && linkName.equals("retrieveUsername"))
        {
            dispatcher = p_context.getRequestDispatcher("/envoy/login/retrieveUsername.jsp");
            bean = new NavigationBean(DUMMY_LINK, "/envoy/login/retrieveUsername.jsp");
            p_request.setAttribute(LoginConstants.form_action, "/globalsight/ControlServlet?linkName=retrieveUsername&pageName=retrieve");
        }
        else
        {
        	dispatcher = p_context.getRequestDispatcher(p_pageDescriptor.getJspURL());
        	bean = new NavigationBean(DUMMY_LINK, p_pageDescriptor.getPageName());
        }

        p_request.setAttribute(DUMMY_NAVIGATION_BEAN_NAME,bean);
        dispatcher.forward(p_request, p_response);
        
        
    }

    /**
     * Returns an optional object that helps in refining flow of
     * control.  This object helps specify the correct link to follow
     * after the user has left the page entry page by validating the
     * user name and the user password.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {
        return new EntryPageControlFlowHelper(p_request, p_response);
    }


    // Only populate the values once.
    private void initialize()
    {

        if (m_defaultLocale == null || m_supportedLocales == null || m_enableSSO == null)
        {
            try
            {        
                SystemConfiguration sc = 
                    SystemConfiguration.getInstance();
                m_supportedLocales = sc.getStrings(sc.UI_LOCALES);
                m_defaultLocale = sc.getStringParameter(sc.DEFAULT_UI_LOCALE);
                m_enableSSO = sc.getStringParameter(sc.ENABLE_SSO);
            }
            catch(Exception e)
            {
                m_supportedLocales = new String[1];
                m_supportedLocales[0] = "en_US";
                m_defaultLocale = "en_US";
                m_enableSSO = "false";
            }
        }        
    }
}
