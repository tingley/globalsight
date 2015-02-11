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
package com.globalsight.everest.webapp.pagehandler.serviceware;

import org.apache.log4j.Logger;
 
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * <p>ServiceWareMainHandler is responsible helping the serviceware import ui</p>
 */
public class ServiceWareMainHandler extends PageHandler
{
    private static final Logger c_logger =
    Logger.getLogger(ServiceWareMainHandler.class);

    //
    // Constructor
    //
    public ServiceWareMainHandler()
    {
        super();
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invoke this PageHandler, which performs the search/browse request and puts information
     * on the request for the JSP to use
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
        try
        {
            if (p_request.getParameter("startImport") != null)
            {
                //they've chosen a fileprofile and KnowledgeObj
                String koId = p_request.getParameter("KOID");
                String fpId = p_request.getParameter("FPID");
                String jobName = p_request.getParameter("jobname");
                if (jobName == null || jobName.length()==0)
                    jobName = "KnowledgeObj_" + koId;

                //now call CxeProxy to send a serviceware import message
                CxeProxy.importFromServiceWare(koId,fpId,jobName);
                p_request.setAttribute("submittedImport",Boolean.TRUE);
                p_request.setAttribute("submittedJobName", jobName);
            }
            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
        }
        catch (EnvoyServletException ese)
        {
            throw ese;
        }
        catch (ServletException se)
        {
            throw se;
        }
        catch (IOException ioe)
        {
            throw ioe;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}

