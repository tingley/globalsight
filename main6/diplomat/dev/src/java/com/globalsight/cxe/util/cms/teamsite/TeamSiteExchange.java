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
package com.globalsight.cxe.util.cms.teamsite;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.ling.common.URLEncoder;

/**
 * This class handles exchange of information btween TeamSite and CAP
 * 
 * @version 1.0
 */

public class TeamSiteExchange
{
    public static String TS_DEFAULT = null;
    public static final String CAP_LOGIN_URL = "cap.login.url";

    public static final String TS_HTTP = "http://";

    public static final String TS_FIND_EXCLUDE_UNIX = "/iw-bin/GlobalSightFindExclude.cgi";
    public static final String TS_FIND_EXCLUDE_NT = "/iw-bin/iw_cgi_wrapper.cgi/GlobalSightFindExclude.ipl";
    public static final String TS_IMPORT_UNIX = "/iw-bin/GlobalSightImport.cgi";
    public static final String TS_IMPORT_NT = "/iw-bin/iw_cgi_wrapper.cgi/GlobalSightImport.ipl";
    public static final String TS_GENERATE_UNIX = "/iw-bin/GlobalSightDcrGen.cgi";
    public static final String TS_GENERATE_NT = "/iw-bin/iw_cgi_wrapper.cgi/GlobalSightDcrGen.ipl";
    public static final String PREVIEW_PREFIX = "GlobalSight_preview";
    public static final String TS_LOGIN_PAGE = "/iw-cc/command/iw.ui";

    private static String m_ServerType = null;

    // Category for log4j logging.
    private static final Logger CATEGORY = Logger
            .getLogger(TeamSiteExchange.class.getName());

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Default TeamSiteExchange constructor used ONLY for TopLink.
     */
    public TeamSiteExchange()
    {
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Setup HTTP Header to Access IW Server.
     * 
     * @return The url.
     */
    public static String httpTeamSiteHeader(TeamSiteServer p_server)
            throws ServletException
    {
        String TSServerName = null;
        String TSServerPort = null;

        try
        {
            TSServerName = p_server.getName();
            TSServerPort = (new Integer(p_server.getExportPort())).toString();
        }
        catch (Exception e)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug(
                        "Problem getting the TeamSite Server Information:", e);
            }
            throw new ServletException(e.getMessage());
        }

        // construct url
        String url = TS_HTTP + TSServerName + ":" + TSServerPort;

        return url;
    }

    public static String getTeamsiteSession(TeamSiteServer p_server)
            throws ServletException
    {
        String sessionId = null;

        String iw_user = p_server.getUser();
        String iw_password = p_server.getUserPass();
        String args = "iw_user=" + iw_user + "&iw_password=" + iw_password;
        String url = httpTeamSiteHeader(p_server) + TS_LOGIN_PAGE;
        sessionId = getHttpSessionId("POST", url, args);

        return sessionId;

    }

    public static String getHttpSessionId(String request_type, String url,
            String post_args)
    {

        try
        {
            URL tsURL = new URL(url);

            {
                CATEGORY.debug("teamsite URL: \n" + tsURL.toString() + "\n");
                CATEGORY.debug("teamsite post_args: \n" + post_args.toString()
                        + "\n");
                CATEGORY.debug("URL String:\n");
                CATEGORY.debug(tsURL.toString() + "?" + post_args.toString());
            }

            HttpURLConnection tsConnection = (HttpURLConnection) tsURL
                    .openConnection();
            tsConnection.setRequestMethod(request_type);
            tsConnection.setDoOutput(true);

            PrintWriter out = new PrintWriter(tsConnection.getOutputStream());

            out.print(post_args);
            out.close();

            CATEGORY.debug(tsConnection.getResponseMessage());
            String cookieVal = tsConnection.getHeaderField("Set-Cookie");
            String sessionId = null;

            sessionId = cookieVal.substring(cookieVal.indexOf("=") + 1,
                    cookieVal.indexOf(";"));

            CATEGORY.debug(cookieVal + "\r\n sessionId \r\n" + sessionId);

            if (sessionId == null)
            {
                String response = "";
                String lastLine = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        tsConnection.getInputStream()));
                String inputLine = null;
                while ((inputLine = in.readLine()) != null)
                {
                    response += inputLine;
                    lastLine = inputLine;
                }
                in.close();

                CATEGORY.debug("sendHttpRequest: os " + m_ServerType
                        + " response " + response);
            }
            return sessionId;

        }
        catch (Exception exc)
        {
            CATEGORY.error("sendHttpRequest exception " + exc.getMessage());

            return "randSession";
            // throw new ServletException(exc.getMessage());
        }
    }

    /**
     * Send HTTP Request.
     * 
     * @param request_type -
     *            The type of request, e.g. POST, GET ...
     * @param url -
     *            The url for this http request.
     * @param post_args -
     *            The names and values of the http request.
     * @return The response to the request.
     */
    public static String sendHttpRequest(String request_type, String url,
            String post_args) throws ServletException
    {

        try
        {
            URL tsURL = new URL(url);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("teamsite URL: " + tsURL.toString() + "\n");
                CATEGORY.debug("teamsite post_args: \n" + post_args.toString()
                        + "\n");
                CATEGORY.debug("URL String:\n");
                CATEGORY.debug(tsURL.toString() + "?" + post_args.toString());
            }

            HttpURLConnection tsConnection = (HttpURLConnection) tsURL
                    .openConnection();
            tsConnection.setRequestMethod(request_type);
            tsConnection.setDoOutput(true);

            PrintWriter out = new PrintWriter(tsConnection.getOutputStream());

            out.print(post_args);
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    tsConnection.getInputStream()));
            String inputLine = null;
            String lastLine = null;
            String response = "";

            while ((inputLine = in.readLine()) != null)
            {
                response += inputLine;
                lastLine = inputLine;
            }
            in.close();

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("sendHttpRequest: os " + m_ServerType
                        + " response " + response);
            }
            return response;
        }
        catch (Exception exc)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("sendHttpRequest exception " + exc.getMessage());
            }
            return null;
            // throw new ServletException(exc.getMessage());
        }
    }

    /**
     * Retrieve a list of IW directories
     * 
     * @param startingPath -
     *            The directory to descend.
     * @param ref_tlign -
     *            The reference to a list of ignored top-level names.
     * @param ref_ign -
     *            The reference to a list of ignored directories (period).
     * @param ignore_dirs -
     *            if non-zero and defined, include terminal dirs.
     * @param separator -
     *            a pattern used to distinguish between the returned directory
     *            paths.
     * @return The directory structure.
     */
    public static String retrieveDirs(String startingPath, String ref_tlign,
            String ref_ign, String ignore_dirs, String separator,
            TeamSiteServer p_server) throws ServletException
    {
        String url = null;
        try
        {
            // determine type of server
            m_ServerType = p_server.getOS();
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("m_ServerType: " + m_ServerType);
            }

            // Create a request
            if (m_ServerType.equalsIgnoreCase("nt"))
            {
                url = httpTeamSiteHeader(p_server) + TS_FIND_EXCLUDE_NT;
            }
            else if (m_ServerType.equalsIgnoreCase("unix"))
            {
                url = httpTeamSiteHeader(p_server) + TS_FIND_EXCLUDE_UNIX;
            }
        }
        catch (Exception e)
        {
            throw new ServletException(e.getMessage());
        }

        // original perl code procedure
        // @dirents = find_excluded( $startingPath,
        // [".raw"], ["STAGING","EDITION","WORKAREA"], 1);

        // construct the content to TeamSite Server

        startingPath = URLEncoder.encode(startingPath);
        ref_tlign = URLEncoder.encode(ref_tlign);
        ref_ign = URLEncoder.encode(ref_ign);
        ignore_dirs = URLEncoder.encode(ignore_dirs);
        separator = URLEncoder.encode(separator);
        String session = getTeamsiteSession(p_server);
        if (session == null)
        {
            session = URLEncoder.encode("teamsite_session");
        }

        // combine into a cgi argument string
        String post_args = "StartingPath=" + startingPath + "&IgnoreTopLevel="
                + ref_tlign;
        post_args += "&IgnoreDirectories=" + ref_ign + "&TerminalDir="
                + ignore_dirs;
        post_args += "&Separator=" + separator;
        post_args += "&session=" + session;

        // Send request
        String res;
        try
        {
            res = sendHttpRequest("POST", url, post_args);
        }
        catch (ServletException se)
        {
            res = null;
        }

        // check the outcome of the response and print failure message if
        // necessary
        return res;
    }

    /**
     * Retrieve TeamSite "default" starting path
     * 
     * @return startingPath - The directory to descend.
     */
    public static String defaultStartingPath(TeamSiteServer p_server,
            String p_store) throws ServletException
    {
        // thor: String TSMountDir = "/iwmnt";
        // qa3: String TSMountDir = "/iw01/iwmnt";
        // mars: String TSMountDir = "Y:\";
        String TSMountDir = null;

        try
        {
            TSMountDir = p_server.getMount();
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("TSMountDir: " + TSMountDir);
            }
        }
        catch (Exception e)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY
                        .debug(
                                "Problem getting the TeamSite Mount Directory Information:",
                                e);
            }
            throw new ServletException(e.getMessage());
        }

        try
        {
            TSMountDir = p_store;
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("TS_DEFAULT: " + TS_DEFAULT);
            }
        }
        catch (Exception e)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug(
                        "Problem getting the TeamSite Server Information:", e);
            }
            throw new ServletException(e.getMessage());
        }

        // construct startingPath
        String startingPath = TSMountDir + "/" + TS_DEFAULT;

        return startingPath;
    }

}
