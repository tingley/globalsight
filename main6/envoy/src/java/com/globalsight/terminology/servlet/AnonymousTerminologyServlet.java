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
package com.globalsight.terminology.servlet;

import org.apache.log4j.Logger;

import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.TermbaseException;

import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GeneralException;

import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;

import java.util.Locale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This servlet serves the list of Termbase names to the Anonymous
 * Termbase Browser.
 */
public class AnonymousTerminologyServlet
    extends HttpServlet
    implements WebAppConstants
{
    private static final long serialVersionUID = -3946674370480046283L;

    private static final Logger CATEGORY =
        Logger.getLogger(
            AnonymousTerminologyServlet.class);

    //these URLs should not include the context-path since they're
    //being used with a request dispatcher
    private static final String STARTPAGE =
        "/envoy/terminology/viewer/AnonymousViewer.jsp";
    private static final String STARTPAGENOACCESS =
        "/envoy/terminology/viewer/AnonymousViewerDisabled.jsp";

    private static ITermbaseManager s_manager = null;
    private static ServletContext s_servletContext = null;

    public AnonymousTerminologyServlet ()
        throws ServletException
    {
        super();

        try
        {
            s_manager = ServerProxy.getTermbaseManager();
        }
        catch (GeneralException ex)
        {
            CATEGORY.error("could not initialize remote TermbaseManager", ex);
        }
    }

    public void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);

        // cache the context
        synchronized(this)
        {
            s_servletContext = config.getServletContext();
        }
    }

    public void doGet (HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws IOException, ServletException
    {
        doPost(p_request, p_response);
    }

    /**
     * Returns the list of Termbase names.
     */
    public void doPost (HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws IOException, ServletException
    {
        // For multi company.
        String companyName = UserUtil.getCurrentCompanyName(p_request);
        if (companyName != null)
        {
            CompanyThreadLocal.getInstance().setValue(companyName);
        }
        
        String page = STARTPAGENOACCESS;

        boolean anonymousAccessAllowed = false;

        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            anonymousAccessAllowed = config.getBooleanParameter(
                SystemConfigParamNames.ANONYMOUS_TERMBASES);
        }
        catch (GeneralException ex)
        {
            CATEGORY.error("could not read system configuration, " +
                "disabling anonymous termbases", ex);
        }

        if (anonymousAccessAllowed)
        {
            page = STARTPAGE;

            try
            {
                String p_userId =null;
                String xmlTermbases = s_manager.getTermbases(Locale.US, p_userId);

                p_request.setAttribute(WebAppConstants.TERMBASE_TB_NAMELIST,
                    xmlTermbases);
            }
            catch (Throwable ex)
            {
                CATEGORY.error(ex.getMessage(), ex);
                p_request.setAttribute(WebAppConstants.TERMBASE_TB_NAMELIST,
                    "<notermbases/>");
            }
        }

        s_servletContext.getRequestDispatcher(page).forward(
            p_request, p_response);
    }
}
