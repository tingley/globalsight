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

public class ServletExtension extends HttpServlet
{
    private static final long serialVersionUID = 1838251948143897146L;
    protected static final String DEFAULT_PAGE = "/jsp/Extension.jsp";
    protected static final String CLOSE_PAGE = "/jsp/saveWindow.jsp";

    protected static final String NEW = "New";
    protected static final String SAVE = "Save";
    protected static final String NEW_EXTENSION = "newExtension";
    protected static final String CURRENT_NAME = "currentName";
    protected static final String EXTENSION = "extension";
    protected static final String SELECTED_EXTENSIONS = "selectedExtensions";

    public ServletExtension() throws ServletException
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
        boolean manageExtension = true;

        Enumeration keys = p_request.getParameterNames();

        // check for a session
        HttpSession session = p_request.getSession(true);
        if (!session.isNew())
        {
            // determine the user's request
            while (keys.hasMoreElements() && manageExtension)
            {
                String element = (String) keys.nextElement();
                if (element.equals(NEW))
                {
                    newExtension(p_request);
                    manageExtension = false;
                }
                else if (element.equals(SAVE))
                {
                    saveExtension(p_request);
                    nextPage = CLOSE_PAGE;
                    manageExtension = false;
                }
            }
        }
        if (manageExtension)
        {
            nextPage = DEFAULT_PAGE;
        }

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
    public void newExtension(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(true);

        String emptyString = "";
        session.setAttribute(NEW_EXTENSION, emptyString);

        // save the current name
        session.setAttribute(CURRENT_NAME, emptyString);
    }

    /////////////////////////////////////////////////
    public void saveExtension(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        if (session != null)
        {
            // save the new extension
            String extenStr = (String) (p_request.getParameter(NEW_EXTENSION));
            DJ_Extension extension = new DJ_Extension(extenStr);
            extension.insert();
        }
    }

    private Logger theLogger = Logger.getLogger();
}
