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
package com.globalsight.cxe.servlet.cms.teamsite;

import com.globalsight.diplomat.util.previewUrlXml.UrlElement;
import com.globalsight.diplomat.util.previewUrlXml.ArgElement;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.Utility;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.diplomat.util.ConfigParameters;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.diplomat.util.ConfigParameters;
import com.globalsight.cxe.util.cms.teamsite.TeamSiteExchange;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

/** 
* TSPreviewServlet
* <p>
*  The UrlPresentationServlet accesses the Session object to
*  find out what the preview URLs are for the chosen locale.
*  It then displays the HTML for the URLs so the user can select them
*  and see the output in the appropriate frame.
* This servlet expects to be called by PreviewUrlFrame.jsp
*/
public class TSPreviewServlet extends HttpServlet
{
    public TSPreviewServlet()
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
     * Writes out the HTML for the URL lists corresponding to the following parameters:
     * <ol> <li> urlListId -- unique id of the url list in the session</li>
     * <li> targetFrame -- where to display the url list</li>
     * <li> locale -- the locale for the url list </li> </ol>
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
            HttpSession session = request.getSession();

            String locale = request.getParameter("locale");
            String templateAndExtension = request.getParameter("template");
            String filename = request.getParameter("filename");
            String type = request.getParameter("type");
            String relpath = request.getParameter("relpath");
            String absdir = request.getParameter("absdir");
            String reldir = request.getParameter("reldir");
            String myUrl = request.getParameter("tsurl");
            String tsHostName = request.getParameter("tshost");
            String dcr = "";
            String template = templateAndExtension.substring(0, templateAndExtension.indexOf(",") );
            String extension = templateAndExtension.substring(templateAndExtension.indexOf(",") + 1, templateAndExtension.lastIndexOf(","));
            String previewDir = templateAndExtension.substring(templateAndExtension.lastIndexOf(",") + 1);
            theLogger.println(Logger.DEBUG_A,"TSPreviewServlet::templatename :" + template);
            theLogger.println(Logger.DEBUG_A,"TSPreviewServlet::extension :" + extension);
            theLogger.println(Logger.DEBUG_A,"TSPreviewServlet::previewDir :" + previewDir);
            theLogger.println(Logger.DEBUG_A,"TSPreviewServlet::myUrl :" + myUrl);
            //just get both url lists to be safe and stick them in the session
            response.setContentType("text/html; charset=UTF-8");
            response.setHeader("Pragma", "no-cache"); //HTTP 1.0
            response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
            response.addHeader("Cache-Control", "no-store"); // tell proxy not to cache
            response.addHeader("Cache-Control", "max-age=0"); // stale right away

            Date theDate = new Date();
            long time = theDate.getTime();
            String cxeTimeStamp = Long.toString(time);


            // send translated file to Teamsite
            try
            {
               URL tsURL = new URL(myUrl);
               HttpURLConnection tsConnection = (HttpURLConnection)tsURL.openConnection();
               tsConnection.setRequestMethod("POST");
               tsConnection.setDoOutput(true);

               PrintWriter out = new PrintWriter(tsConnection.getOutputStream());
               //String fileContent = new String(content);

               out.print("dir=" + URLEncoder.encode(absdir) + "&");
               out.print("relpath=" + URLEncoder.encode(relpath) + "&");
               out.print("filename=" + URLEncoder.encode(filename) + "&");
               out.print("template=" + URLEncoder.encode(template) + "&");
               out.print("locale=" + URLEncoder.encode(locale) + "&");
               out.print("filetype=" + URLEncoder.encode(type) + "&");
               out.print("session=" + URLEncoder.encode("teamsiteSession") + "\n\n");
               out.close();

               BufferedReader in = new BufferedReader (new InputStreamReader(
                  tsConnection.getInputStream()));
               String inputLine = null;
               String lastLine = null;

               while ((inputLine = in.readLine()) != null)
               {
                  lastLine = inputLine;
               }
               in.close();

               //now check the LastLine to see what the returned status from the
               //Teamsite Integration CGI script was
               StringTokenizer st = new StringTokenizer(lastLine,"=");
               st.nextToken(); //skip status
               String status = st.nextToken();
               theLogger.println(Logger.DEBUG_A, "Status returned from Teamsite CGI: " + status);
            }
            catch (Exception exc)
            {
               theLogger.println(Logger.DEBUG_A,"caught exception "+exc);
               theLogger.println(Logger.DEBUG_A,"caught exception message "+exc.getMessage());
               //return false;
            }
            // End of call to TeamSite script

            //now write out the HTML for this servlet's response
            //PrintWriter out = response.getWriter();
            int tplIndex = template.indexOf(".tpl");
            String tplBasename = template.substring(0, tplIndex);
            TeamSiteServer tss =(TeamSiteServer)ServerProxy.getTeamSiteServerPersistenceManager()
                                     .getTeamSiteServerByName(tsHostName);

            SystemConfiguration config = SystemConfiguration.getInstance();
            String tsServerName = tsHostName; 
            String tsProxyPort = (new Integer(tss.getProxyPort())).toString();
            String tsImportPort = (new Integer(tss.getImportPort())).toString();
            String tsServerType = tss.getOS();
            theLogger.println(Logger.DEBUG_A,"TSPreviewServlet:: server type"+ tsServerType);
            String mount = tss.getMount();

            StringBuffer urlPrefix = new StringBuffer(TeamSiteExchange.TS_HTTP);
            urlPrefix.append(tsServerName).append(":").append(tsImportPort);

            StringBuffer url = new StringBuffer("/iw-mount/");
            url.append(reldir).append(previewDir).append("/");
            url.append(TeamSiteExchange.PREVIEW_PREFIX).append("_");
            url.append(locale).append("_");
            url.append(filename).append("_").append(tplBasename).append(".").append(extension);
            // String tsUrl = urlPrefix.toString() + Utility.URLEncodePath(url.toString());
            // Looks like IE does not like this url encoded.
            // So leaving it as it's
            String tsUrl = urlPrefix.toString() + url.toString();
            theLogger.println(Logger.DEBUG_A,"The Url TSPreviewServlet:: "+ tsUrl);
            response.sendRedirect(tsUrl);
        
        }
        catch (Exception e)
        {
            theLogger.printStackTrace(Logger.ERROR, "TSPreviewServlet problem:", e);
            throw new ServletException(e);
        }
    }

    private Logger theLogger = Logger.getLogger();
}
