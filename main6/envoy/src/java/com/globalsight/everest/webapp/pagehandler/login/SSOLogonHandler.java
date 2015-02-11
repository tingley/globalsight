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
import com.globalsight.everest.foundation.sso.SSOSPHelper;
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
 * This page handler produces the SSO Logon page in login module.
 */
public class SSOLogonHandler extends PageHandler
{
    private static Logger s_logger = Logger
            .getLogger(LoginMainHandler.class.getName());

    /**
     * Invokes this PageHandler
     * 
     * @param jspURL
     *            the URL of the JSP to be invoked
     * @param the
     *            original request sent from the browser
     * @param the
     *            original response object
     * @param context
     *            the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context) throws ServletException,
            IOException
    {
        String ssoBackTo = (String) p_request.getAttribute("sso_back_to");
        String ssoUserId = (String) p_request.getAttribute("sso_username");
        String ssoPwd = (String) p_request.getAttribute("sso_password");
        String ssoIdpUrl = (String) p_request.getAttribute("sso_idp_url");
        String companyName = (String) p_request.getAttribute("sso_company_name");
        boolean useSaml = true;
        
        ssoIdpUrl = ssoIdpUrl + "?useSaml=" + useSaml;

        String requestData = null;
        try
        {
            SSOSPHelper helper = SSOSPHelper.createInstance(useSaml);
            requestData = helper.createSSOAssertion(ssoUserId, ssoPwd, companyName, ssoBackTo);
        }
        catch (Exception e)
        {
            throw new ServletException("Error occur when create samlRequest", e);
        }

        p_request.setAttribute("ssoIdpUrl", ssoIdpUrl);
        p_request.setAttribute("sentToIdp", "true");
        p_request.setAttribute("ssoAssertion", requestData);
        
        dispatchJSP(p_pageDescriptor, p_request, p_response, p_context);
    }

    /**
     * Invoke the correct JSP for this page
     */
    protected void dispatchJSP(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context) throws ServletException,
            IOException
    {
        // create the java beans and pass them to the request. use
        // dummy link, real link will be determined after the user
        // navigates out of the page
        NavigationBean bean = null;

        // invoke JSP
        RequestDispatcher dispatcher;
        String linkName = p_request.getParameter("linkName");
        if (null != linkName && linkName.equals("resetPass"))
        {
            dispatcher = p_context.getRequestDispatcher("/envoy/login/resetPass.jsp");
            bean = new NavigationBean(DUMMY_LINK, "/envoy/login/resetPass.jsp");
            p_request.setAttribute(LoginConstants.form_action,
                    "/globalsight/ControlServlet?linkName=resetPass&pageName=retrieve");
        }
        else if (null != linkName && linkName.equals("retrieveUsername"))
        {
            dispatcher = p_context.getRequestDispatcher("/envoy/login/retrieveUsername.jsp");
            bean = new NavigationBean(DUMMY_LINK, "/envoy/login/retrieveUsername.jsp");
            p_request.setAttribute(LoginConstants.form_action,
                    "/globalsight/ControlServlet?linkName=retrieveUsername&pageName=retrieve");
        }
        else
        {
            dispatcher = p_context.getRequestDispatcher(p_pageDescriptor.getJspURL());
            bean = new NavigationBean(DUMMY_LINK, p_pageDescriptor.getPageName());
        }

        p_request.setAttribute(DUMMY_NAVIGATION_BEAN_NAME, bean);
        dispatcher.forward(p_request, p_response);

    }

    /**
     * Returns an optional object that helps in refining flow of control. This
     * object helps specify the correct link to follow after the user has left
     * the page entry page by validating the user name and the user password.
     * 
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        return new EntryPageControlFlowHelper(p_request, p_response);
    }
}
