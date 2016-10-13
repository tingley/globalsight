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
package com.globalsight.diplomat.servlet.ambassador;

import com.globalsight.diplomat.util.previewUrlXml.UrlElement;
import com.globalsight.diplomat.util.previewUrlXml.ArgElement;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

/** 
* ShowUrlFramesetServlet
* <p>
* This servlet sets up another frameset that is used to display the customer
* URLs. This allows modifying the content of a frame without violating
* Netscape's javascript security policy.
*/
public class ShowUrlFramesetServlet extends HttpServlet
{
    public ShowUrlFramesetServlet()
    throws ServletException
    {
        try
        {
            theLogger.setLogname("CxeServlets");
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
    }

    public void doPost (HttpServletRequest p_request, HttpServletResponse p_response)
    throws IOException, ServletException
    {
        doGet(p_request,p_response);
    }

    /**
     * Writes out the frameset that goes inside the target frames.
     * @param the request
     * @param the response
     * @throws IOException
     * @throws ServletException
     */
    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        try
        {
            response.setContentType("text/html;");
            //response.setHeader("Pragma", "No-cache");
            //response.setHeader("Cache-Control", "no-cache");
            //response.setDateHeader("Expires", theDate.toString());
            String urlIndex = (String)request.getParameter("urlIndex");
            String urlListId = (String)request.getParameter("urlListId");
            String type = (String) request.getParameter("type"); //Source or Target
            String topFrameName = type + "Top";
            String bottomFrameName = type + "Bottom";
            theLogger.println(Logger.DEBUG_D,
                              "ShowUrlFramesetServlet: session id=" +
                              request.getSession().getId() +
                              " type = " + type);

            Date theDate = new Date();
            long time= theDate.getTime();

            //the URL to show should be stored in the session with this ID
            SystemConfiguration config = SystemConfiguration.getInstance();
            String useSSL = config.getStringParameter("useSSL");
            String nonSSLPort = config.getStringParameter("nonSSLPort");
            String SSLPort = config.getStringParameter("SSLPort");
            String prefix;
            if (useSSL.equals("false"))
                prefix = "http://" + request.getServerName() + ":" + nonSSLPort;
            else
                prefix = "https://" + request.getServerName() + ":" + SSLPort;

            String topUrl = prefix + "/globalsight/ShowUrlServlet?urlListId=" +
                            urlListId + "&type=" + type + "&urlIndex=" + urlIndex +
                            "&cxeTimeStamp=" + time;
            String bottomUrl = prefix + "/globalsight/cxe/html/blank.html";

            PrintWriter out = response.getWriter();
            out.println("<html>");
            out.println("<head>");
            out.println("<META HTTP-EQUIV=\"Cache-Control\" CONTENT=\"no-cache\">");
            out.println("<META HTTP-EQUIV=\"Expires\" CONTENT=\"0\">");
            out.println("<frameset rows=\"0,100%\">");
            out.println("<frame name=\"" + topFrameName + "\" src=\"" +
                        topUrl + "\" scrolling=\"no\" frameborder=\"0\" marginheight=\"0\" marginwidth=\"0\">");
            out.println("<frame name=\"" + bottomFrameName + "\" src=\"" +
                        bottomUrl + "\" scrolling=\"auto\" frameborder=\"0\" marginheight=\"0\" marginwidth=\"0\">");
            out.println("</frameset>");
            out.println("</head>");
            out.println("</html>");
        }
        catch (Exception e)
        {
            theLogger.printStackTrace(Logger.ERROR, "ShowUrlFramesetServlet problem:", e);
            throw new ServletException(e);
        }
    }

    private Logger theLogger = Logger.getLogger();
}
