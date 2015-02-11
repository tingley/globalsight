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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

// Envoy packages
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

// java
import java.io.IOException;

import java.rmi.RemoteException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.everest.webapp.pagehandler.administration.reports.CustomExternalReportInfoBean;

/**
* Handles listing the reports for showReports.jsp
*/
public class ReportsMainHandler extends PageHandler
{
    public static String ATTR_CUSTOM_EXTERNAL_REPORTS = "customExternalReportInfos";

    private static final GlobalSightCategory s_category =
    (GlobalSightCategory)GlobalSightCategory.getLogger(
                                                      PageHandler.class);

    private static ArrayList s_customExternalReportInfos = new ArrayList();

    /**
     * Invokes this PageHandler object for showReports.jsp
     *
     * @param pageDescriptor the description of the page to be produced
     * @param request the original request sent from the browser
     * @param response original response object
     * @param context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request, HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException,
    IOException,
    EnvoyServletException
    {
        addCustomExternalReportInfo(p_request);
        super.invokePageHandler(p_pageDescriptor,p_request,p_response,p_context);
    }


    /**
     * Adds custom external report information to the request as attributes.
     * This is used in showReport.jsp
     * 
     * @param p_request
     */
    private void addCustomExternalReportInfo(HttpServletRequest p_request)
    {
        try
        {
            synchronized(s_customExternalReportInfos)
            {
                if (s_customExternalReportInfos.size() == 0)
                {
                    //load the custom report infos only the first time
                    int reportNum = 0;
                    SystemConfiguration config = SystemConfiguration.getInstance();
                    int numCustomExternalReports =
                        config.getIntParameter("reports.numCustomExternalReports");
                    s_category.debug("There are " + numCustomExternalReports + " external custom reports");

                    while (reportNum < numCustomExternalReports)
                    {
                        reportNum++;
                        String reportName = "reports.custom" + reportNum;
                        String url = config.getStringParameter(reportName + ".url");
                        String name = config.getStringParameter(reportName + ".name");
                        String desc = config.getStringParameter(reportName + ".desc");
                        CustomExternalReportInfoBean info =
                        new CustomExternalReportInfoBean(reportNum, url, name, desc);
                        s_customExternalReportInfos.add(info);
                        s_category.info("Registering custom external report: " + info.toString());
                    }
                }
            }
        }
        catch (Exception e)
        {
            s_category.error("Failed to read custom external report information.",e);
        }
        finally
        {
            p_request.setAttribute(ATTR_CUSTOM_EXTERNAL_REPORTS,s_customExternalReportInfos);
        }
    }
}

