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
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.diplomat.util.Logger;

public class ServletFileProfile extends HttpServlet
{
    protected static final String DEFAULT_PAGE = "/jsp/FileProfile.jsp";
    protected static final String MANAGE_PAGE = "/jsp/FileProfileManage.jsp";
    protected static final String CLOSE_PAGE = "/jsp/saveWindow.jsp";

    protected static final String STATUS = "status";
    protected static final String MODIFY = "Modify";
    protected static final String NEW = "New";
    protected static final String SAVE = "Save";
    protected static final String PROFILE_NAME = "profileName";
    protected static final String CURRENT_NAME = "currentName";
    protected static final String PROFILE_OBJECTS = "profileObjects";
    protected static final String TYPE = "type";
    protected static final String RULE = "rule";
    protected static final String FILE_NAME = "fileName";
    protected static final String RULE_OBJECTS = "ruleObjects";
    protected static final String UPDATE_ME = "updateMe";
    protected static final String EXTENSION = "extension";
    protected static final String SELECTED_EXTENSIONS = "selectedExtensions";

    public ServletFileProfile() throws ServletException
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
        session.removeAttribute(FILE_NAME);
        session.setAttribute(PROFILE_NAME, emptyString);
        session.setAttribute(RULE, new Long(0));
        session.setAttribute(TYPE, new Long(0));
        session.setAttribute(RULE_OBJECTS, DJ_XML_Rule
                .retrieveXMLProfilesByName());
        session.setAttribute(UPDATE_ME, new Boolean(false));
        session.setAttribute(STATUS, NEW);

        // save the current name
        session.setAttribute(CURRENT_NAME, emptyString);

        // save an empty extension list
        session.setAttribute(SELECTED_EXTENSIONS, new Vector());
    }

    /////////////////////////////////////////////////
    public void modifyProfile(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);

        Long id = new Long(p_request.getParameter(FILE_NAME));
        DJ_FileProfile profile = new DJ_FileProfile(id.longValue());
        session.setAttribute(FILE_NAME, id);
        session.setAttribute(PROFILE_NAME, profile.getName());
        session.setAttribute(TYPE, new Long(profile.getType()));
        session.setAttribute(RULE, new Long(profile.getRule()));
        session.setAttribute(RULE_OBJECTS, DJ_XML_Rule
                .retrieveXMLProfilesByName());
        session.setAttribute(UPDATE_ME, new Boolean(true));
        session.setAttribute(STATUS, MODIFY);

        // save the current name
        session.setAttribute(CURRENT_NAME, profile.getName());

        // save the selected extensions
        DJ_FileProfileExtension extensions = new DJ_FileProfileExtension(id
                .longValue(), false);
        session.setAttribute(SELECTED_EXTENSIONS, extensions.getList());
    }

    /////////////////////////////////////////////////
    public void saveProfile(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        if (session != null)
        {
            // save the file profile
            Long id = new Long(0);
            if (session.getAttribute(FILE_NAME) != null)
                id = (Long) session.getAttribute(FILE_NAME);

            String profileName = p_request.getParameter(PROFILE_NAME);

            DJ_FileProfile profile = new DJ_FileProfile(id.longValue(),
                    profileName, Long.parseLong(p_request.getParameter(TYPE)),
                    Long.parseLong(p_request.getParameter(RULE)));
            if (((Boolean) session.getAttribute(UPDATE_ME)).booleanValue())
                profile.update();
            else
                profile.insert();

            // save the extensions
            Enumeration parameters = p_request.getParameterNames();
            Vector extensionList = new Vector();
            while (parameters.hasMoreElements())
            {
                String parameter = (String) parameters.nextElement();
                if (parameter.startsWith(EXTENSION))
                {
                    long extensionID = Long.parseLong(p_request
                            .getParameter(parameter));
                    WrapFileProfileExtensionTable extension = new WrapFileProfileExtensionTable(
                            profile.getID(), extensionID);
                    extensionList.add(extension);
                }
            }

            DJ_FileProfileExtension fpExtension = new DJ_FileProfileExtension(
                    extensionList);
            // delete all previous file profile id association
            fpExtension.delete();
            // save the records
            fpExtension.save();
        }
    }

    private Logger theLogger = Logger.getLogger();
}
