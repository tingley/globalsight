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
package com.globalsight.reports.xmlqueries;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.diplomat.util.Logger;

/**
 * XmlQueryServlet
 * <p>
 * This servlet is usd for doing XML queries against System4
 */
public class XmlQueryServlet extends HttpServlet
{
    /**
     * Reports Logger
     */
    private static org.apache.log4j.Logger s_logger = org.apache.log4j.Logger
            .getLogger(XmlQueryServlet.class);

    /**
     * Performs some query and then outputs XML
     * 
     * @param p_request
     * @param p_response
     * @exception IOException
     * @exception ServletException
     */
    public void doPost(HttpServletRequest p_request,
            HttpServletResponse p_response) throws IOException,
            ServletException
    {
        doGet(p_request, p_response);
    }

    /**
     * Performs some querying and writes out XML. Handled queries are: jobInfo
     * 
     * @param p_request
     *            -- parameter must include "queryName"
     * @param p_response
     * @exception IOException
     * @exception ServletException
     */
    public void doGet(HttpServletRequest p_request,
            HttpServletResponse p_response) throws IOException,
            ServletException
    {
        p_response.setContentType("text/xml;");
        p_response.setHeader("Pragma", "No-cache");
        p_response.setHeader("Cache-Control", "no-cache");
        p_response.setDateHeader("Expires", System.currentTimeMillis());
        String queryName = p_request.getParameter("queryName");
        String xml = "";
        try
        {
            if ("jobInfo".equals(queryName))
            {
                s_logger.info("Running xml query 'jobInfo' at "
                        + (new Date()).toString());
                JobInfoQuery query = new JobInfoQuery(p_request);
                xml = query.runQuery();
            }
        }
        catch (Exception e)
        {
            s_logger.error("Problem running xml query " + queryName, e);
        }
        Logger.writeDebugFile("jobInfo.xml", xml);
        p_response.getWriter().println(xml);
    }
}
