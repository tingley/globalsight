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
package com.globalsight.reports.servlet;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.reports.Constants;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.util.GeneralException;

public class ReportControlServlet extends HttpServlet
{
    private static final long serialVersionUID = -4329598522535816076L;

    private static final Logger CATEGORY = Logger
            .getLogger(ReportControlServlet.class);

    private static ServletContext m_servletContext = null;

    public BasicReportHandler targetReportHandler = null;

    /**
     * Reads the XML navigation file (ReportConfig.xml).
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        // cache the context
        synchronized (this)
        {
            m_servletContext = config.getServletContext();
        }

        if (!ReportHandlerFactory.isInitialized())
        {
            if (!ReportHandlerFactory
                    .createReportHandlerMap(Constants.REPORT_CONFIG_FILE))
            {
                CATEGORY.error("Error reading XML site description file."
                        + Constants.REPORT_CONFIG_FILE);
            }
        }
    }

    /**
     * Serve a GET request
     */
    public void doGet(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        try
        {
            _doGet(p_request, p_response);
        }
        catch (SocketException se)
        {
            CATEGORY.error("Socket Exception2: " + se.getMessage());
        }
    }

    /**
     * Serve a GET request
     */
    private void _doGet(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        dumpRequestValues(p_request);
        String targetPageName = p_request
                .getParameter(Constants.REPORT_PAGE_NAME);
        
        String companyName = UserUtil.getCurrentCompanyName(p_request);
        if (companyName != null)
        {
            CompanyThreadLocal.getInstance().setValue(companyName);
        }

        // if the page name, get the target report handler.
        if (targetPageName != null)
        {
            targetReportHandler = ReportHandlerFactory
                    .getReportHandlerInstance(targetPageName);
            try
            {
                targetReportHandler.invokeHandler(p_request, p_response,
                        m_servletContext);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //                p_response.sendRedirect("/globalsight/wl");
            }
        }
    }

    /**
     * Dumps out the request parameter and attribute names and values to the
     * log, if debugging is on for the logging category.
     * 
     * @param p_request
     *            http request
     */
    @SuppressWarnings("unchecked")
    private void dumpRequestValues(HttpServletRequest p_request)
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY
                    .debug("\r\nDumping Request Attributes and Parameter Values");
            System.out.println("-------------------------------");
            System.out.println("HTTP request=" + p_request.toString());
            System.out.println("HTTP request URI=" + p_request.getRequestURI());
            System.out.println("HTTP content type="
                    + p_request.getContentType());

            Enumeration<String> enumeration = p_request.getAttributeNames();
            while (enumeration.hasMoreElements())
            {
                String name = (String) enumeration.nextElement();
                Object value = (Object) p_request.getAttribute(name);
                System.out.println("attribute=" + name + ", value= " + value);
            }
            enumeration = p_request.getParameterNames();
            while (enumeration.hasMoreElements())
            {
                String name = (String) enumeration.nextElement();
                String value = (String) p_request.getParameter(name);
                System.out.println("parameter=" + name + ", value= " + value);
            }
        }
    }

    /**
     * Serve a POST request
     */
    public void doPost(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        doGet(p_request, p_response);
    }
}
