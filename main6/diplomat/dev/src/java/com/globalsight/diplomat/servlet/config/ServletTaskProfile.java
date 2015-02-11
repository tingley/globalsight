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

public class ServletTaskProfile extends HttpServlet
{
    protected final int DEFAULT_RECORDS_PER_PAGE = 10;
    protected final int DEFAULT_PAGES_PER_BATCH = 5;
    protected final long DEFAULT_MAX_ELAPSED_HOURS = 168L; /* one week in hours */
    protected final String DEFAULT_PAGE = "/jsp/TaskProfile.jsp";
    protected final String MANAGE_PAGE = "/jsp/TaskProfileManage.jsp";
    protected final String CLOSE_PAGE = "/jsp/saveWindow.jsp";

    protected final String TASK_ID = "taskID";
    protected final String STATUS = "status";
    protected final String MODIFY = "Modify";
    protected final String NEW = "New";
    protected final String SAVE = "Save";
    protected final String TASK_NAME = "taskName";
    protected final String TABLE_NAME = "tableName";
    protected final String RECORDS_PER_PAGE = "recordsPerPage";
    protected final String PAGES_PER_BATCH = "pagesPerBatch";
    protected final String MAX_ELAPSED_HOURS = "maxElapsedHours";
    protected final String CONNECTION_ID = "connectionID";

    protected final String CURRENT_NAME = "currentName";
    protected final String PROFILE_OBJECTS = "profileObjects";
    protected final String UPDATE_ME = "updateMe";

    public ServletTaskProfile() throws ServletException
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
        session.removeAttribute(TASK_ID);
        session.setAttribute(TASK_NAME, emptyString);
        session.setAttribute(TABLE_NAME, emptyString);
        session.setAttribute(RECORDS_PER_PAGE, new Integer(
                DEFAULT_RECORDS_PER_PAGE));
        session.setAttribute(PAGES_PER_BATCH, new Integer(
                DEFAULT_PAGES_PER_BATCH));
        session.setAttribute(MAX_ELAPSED_HOURS, new Long(
                DEFAULT_MAX_ELAPSED_HOURS));
        session.setAttribute(CONNECTION_ID, new Long(0));
        session.setAttribute(UPDATE_ME, new Boolean(false));
        session.setAttribute(STATUS, NEW);

        // save the current name
        session.setAttribute(CURRENT_NAME, emptyString);
    }

    /////////////////////////////////////////////////
    public void modifyProfile(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);

        Long id = new Long(p_request.getParameter(TASK_ID));
        DJ_TaskQueue profile = new DJ_TaskQueue(id.longValue());
        session.setAttribute(TASK_ID, id);
        session.setAttribute(TASK_NAME, profile.getName());
        session.setAttribute(TABLE_NAME, profile.getTableName());
        session.setAttribute(RECORDS_PER_PAGE, new Integer(profile
                .getRecordsPerPage()));
        session.setAttribute(PAGES_PER_BATCH, new Integer(profile
                .getPagesPerBatch()));
        long maxElapsedMillis = profile.getMaxElapsedMillis();
        long divisor = 1000L /* millis */* 60L /* sec */* 60L /* min */;
        long hours = maxElapsedMillis / divisor;
        Long maxElapsedHours = new Long(hours);
        session.setAttribute(MAX_ELAPSED_HOURS, maxElapsedHours);
        session
                .setAttribute(CONNECTION_ID,
                        new Long(profile.getConnectionID()));
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
            if (session.getAttribute(TASK_ID) != null)
                id = (Long) session.getAttribute(TASK_ID);

            long connectionID = Long.parseLong(p_request
                    .getParameter(CONNECTION_ID));
            int recordsPerPage = Integer.parseInt(p_request
                    .getParameter(RECORDS_PER_PAGE));
            int pagesPerBatch = Integer.parseInt(p_request
                    .getParameter(PAGES_PER_BATCH));
            long multiplicand = 1000L /* millis */* 60L /* seconds */* 60L /* minutes */;
            long maxElapsedMillis = Long.parseLong(p_request
                    .getParameter(MAX_ELAPSED_HOURS))
                    * multiplicand;

            String taskname = p_request.getParameter(TASK_NAME);
            String tablename = p_request.getParameter(TABLE_NAME);

            DJ_TaskQueue profile = new DJ_TaskQueue(id.longValue(), taskname,
                    tablename, connectionID, recordsPerPage, pagesPerBatch,
                    maxElapsedMillis);

            if (((Boolean) session.getAttribute(UPDATE_ME)).booleanValue())
                profile.update();
            else
                profile.insert();
        }
    }

    private Logger theLogger = Logger.getLogger();
}
