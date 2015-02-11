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

package com.globalsight.diplomat.servlet.config;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.diplomat.util.Logger;

public class ServletURLProfile extends HttpServlet
{
    protected final String DEFAULT_PAGE = "/jsp/URLProfile.jsp";
    protected final String MANAGE_PAGE = "/jsp/URLProfileManage.jsp";
    protected final String CLOSE_PAGE = "/jsp/saveWindow.jsp";

    protected final String STATUS = "status";
    protected final String MODIFY = "Modify";
    protected final String NEW = "New";
    protected final String SAVE = "Save";
    protected final String PROFILE_NAME = "profileName";
    protected final String CURRENT_NAME = "currentName";
    protected final String PROFILE_OBJECTS = "profileObjects";
    protected final String XML = "xml";
    protected final String URL_NAME = "urlName";
    protected final String UPDATE_ME = "updateMe";

    public ServletURLProfile() throws ServletException
    {
        try
        {
            theLogger.setLogname("DiplomatServlets");
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
    }

    /////////////////////////////////////////////////
    public void doGet(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        String nextPage = DEFAULT_PAGE;
        boolean manageProfile = true;

        Enumeration keys = p_request.getParameterNames();

        // check for a session
        HttpSession session = p_request.getSession(true);
        if (!session.isNew())
        {
            // determine the user's request
            while (keys.hasMoreElements() && manageProfile)
            {
                String element = (String) keys.nextElement();
                if (element.equals(NEW))
                {
                    newProfile(p_request);
                    manageProfile = false;
                }
                else if (element.equals(MODIFY))
                {
                    modifyProfile(p_request);
                    manageProfile = false;
                }
                else if (element.equals(SAVE))
                {
                    saveProfile(p_request);
                    nextPage = CLOSE_PAGE;
                    manageProfile = false;
                }
            }
        }
        if (manageProfile)
            nextPage = MANAGE_PAGE;

        RequestDispatcher requestDispatcher = getServletContext()
                .getRequestDispatcher(nextPage);
        if (requestDispatcher != null)
            requestDispatcher.forward(p_request, p_response);
    }

    /////////////////////////////////////////////////
    public void doPost(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        doGet(p_request, p_response);
    }

    /////////////////////////////////////////////////
    public void newProfile(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(true);

        String emptyString = "";
        session.removeAttribute(URL_NAME);
        session.setAttribute(PROFILE_NAME, emptyString);
        session.setAttribute(XML, emptyString);
        session.setAttribute(UPDATE_ME, new Boolean(false));
        session.setAttribute(STATUS, NEW);

        // save the current name
        session.setAttribute(CURRENT_NAME, emptyString);
    }

    /////////////////////////////////////////////////
    public void modifyProfile(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);

        Long id = new Long(p_request.getParameter(URL_NAME));
        DJ_URL_List profile = new DJ_URL_List(id.longValue());
        session.setAttribute(URL_NAME, id);
        session.setAttribute(PROFILE_NAME, profile.getName());
        session.setAttribute(XML, profile.getXML());
        session.setAttribute(UPDATE_ME, new Boolean(true));
        session.setAttribute(STATUS, MODIFY);

        // save the current name
        session.setAttribute(CURRENT_NAME, profile.getName());
    }

    /////////////////////////////////////////////////
    public void saveProfile(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        if (session != null)
        {
            Long id = new Long(0);
            if (session.getAttribute(URL_NAME) != null)
                id = (Long) session.getAttribute(URL_NAME);

            String profileName = p_request.getParameter(PROFILE_NAME);
            String xml = p_request.getParameter(XML);

            //to account for a crapscape 4.7 bug, add ">" if it does not exist
            //first trim the string
            xml = xml.trim();
            if (!xml.endsWith(">"))
                xml += ">\n";

            DJ_URL_List profile = new DJ_URL_List(id.longValue(), profileName,
                    xml);

            profile.save();
        }
    }

    private Logger theLogger = Logger.getLogger();
}
