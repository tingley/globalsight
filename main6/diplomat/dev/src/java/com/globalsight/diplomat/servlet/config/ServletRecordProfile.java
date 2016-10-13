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
import java.util.Comparator;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.ling.common.Transcoder;

public class ServletRecordProfile extends HttpServlet
{
    protected final String DEFAULT_PAGE = "/jsp/RecordProfile.jsp";
    protected final String MANAGE_PAGE = "/jsp/RecordProfileManage.jsp";
    protected final String CLOSE_PAGE = "/jsp/saveWindow.jsp";

    protected final String ACQUISITION_SQL = "acquisitionSql";
    protected final String ACQUISITION_CONTEXT_SQL = "acquisitionContextSql";
    protected final String ACQUISITION_CONNECT = "acquisitionConnect";
    protected final String PREVIEW_INSERT_SQL = "previewInsertSql";
    protected final String PREVIEW_UPDATE_SQL = "previewUpdateSql";
    protected final String PREVIEW_CONNECT = "previewConnect";
    protected final String FINAL_INSERT_SQL = "finalInsertSql";
    protected final String FINAL_UPDATE_SQL = "finalUpdateSql";
    protected final String FINAL_CONNECT = "finalConnect";
    protected final String PREVIEW_URL = "previewUrl";
    protected final String MANUAL_MODE = "manualMode";

    protected final String STATUS = "status";
    protected final String ADD = "Add";
    protected final String NEW = "New";
    protected final String DELETE = "Delete";
    protected final String SAVE = "Save";
    protected final String EDIT = "Edit";
    protected final String PROFILE = "Profile";
    protected final String COLUMN = "column";
    protected final String TABLE_NAME = "tableName";
    protected final String TYPE = "type";
    protected final String RULE = "rule";
    protected final String NAME = "name";
    protected final String EDIT_ID = "editID";
    protected final String PROFILE_OBJECTS = "profileObjects";
    protected final String MODIFY = "Modify";
    protected final String RECORD_NAME = "recordName";
    protected final String CURRENT_NAME = "currentName";
    protected final String COLUMN_OBJECTS = "columnObjects";
    protected final String CONTENT_MODE = "contentMode";
    protected final String LABEL = "label";

    public ServletRecordProfile() throws ServletException
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
        HttpSession session = p_request.getSession(true);

        if (!session.isNew())
        {
            Enumeration keys = p_request.getParameterNames();

            // determine the user's request
            while (keys.hasMoreElements())
            {
                String element = (String) keys.nextElement();
                if (element.equals(ADD))
                {
                    addColumn(p_request);
                }
                else if (element.equals(DELETE))
                {
                    deleteColumn(p_request);
                }
                else if (element.equals(SAVE))
                {
                    nextPage = CLOSE_PAGE;
                    saveProfile(p_request);
                }
                else if (element.equals(NEW) || element.equals(MODIFY))
                    instantiateProfile(p_request);
            }
        }
        else
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
    protected void instantiateProfile(HttpServletRequest p_request)
    {
        // retrieve the session
        HttpSession session = p_request.getSession(false);
        DJ_RecordProfile profile = null;

        // Are we modifying an existing record?
        if ((p_request.getParameter(MODIFY) != null)
                && (p_request.getParameter(RECORD_NAME) != null))
        {
            long id = (new Long(p_request.getParameter(RECORD_NAME)))
                    .longValue();
            if (id > 0)
                profile = new DJ_RecordProfile(id);
            session.setAttribute(STATUS, MODIFY);
        }
        else
        {
            profile = new DJ_RecordProfile();
            session.setAttribute(STATUS, NEW);
        }

        // save the current name
        session.setAttribute(CURRENT_NAME, profile.getName());

        // write to the session
        saveSession(session, profile);
    }

    /////////////////////////////////////////////////
    // save values the user enters in the form
    protected void saveFormValues(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        DJ_RecordProfile profile = (DJ_RecordProfile) session
                .getAttribute(PROFILE);
        if (profile != null)
        {
            String name = p_request.getParameter(NAME);
            String acqSql = p_request.getParameter(ACQUISITION_SQL);
            String conSql = p_request.getParameter(ACQUISITION_CONTEXT_SQL);
            String piSql = p_request.getParameter(PREVIEW_INSERT_SQL);
            String puSql = p_request.getParameter(PREVIEW_UPDATE_SQL);
            String fiSql = p_request.getParameter(FINAL_INSERT_SQL);
            String fuSql = p_request.getParameter(FINAL_UPDATE_SQL);

            profile.setName(name);
            profile.setAcquisitionSql(acqSql);
            profile.setAcquisitionContextSql(conSql);
            profile.setAcquisitionConnectID(Long.parseLong(p_request
                    .getParameter(ACQUISITION_CONNECT)));

            profile.setPreviewInsertSql(piSql);
            profile.setPreviewUpdateSql(puSql);
            profile.setPreviewConnectID(Long.parseLong(p_request
                    .getParameter(PREVIEW_CONNECT)));

            profile.setFinalInsertSql(fiSql);
            profile.setFinalUpdateSql(fuSql);
            profile.setFinalConnectID(Long.parseLong(p_request
                    .getParameter(FINAL_CONNECT)));

            profile.setPreviewUrlID(Long.parseLong(p_request
                    .getParameter(PREVIEW_URL)));
            boolean manualMode = false;
            if ((Integer.parseInt(p_request.getParameter(MANUAL_MODE))) == 1)
                manualMode = true;
            profile.setManualMode(manualMode);
        }
    }

    /////////////////////////////////////////////////
    protected void saveProfile(HttpServletRequest p_request)
    {
        // save entered data
        saveFormValues(p_request);

        HttpSession session = p_request.getSession(false);
        DJ_RecordProfile profile = (DJ_RecordProfile) session
                .getAttribute(PROFILE);
        if (profile != null)
        {
            profile.save();
        }
    }

    /////////////////////////////////////////////////
    protected void deleteColumn(HttpServletRequest p_request)
    {
        // save entered data
        saveFormValues(p_request);

        int column = (new Integer(p_request.getParameter(COLUMN))).intValue();

        // retrieve the session
        HttpSession session = p_request.getSession(false);

        // retrieve the existing profile
        DJ_RecordProfile profile = (DJ_RecordProfile) session
                .getAttribute(PROFILE);
        if (profile != null)
        {
            Vector columnList = profile.getColumnList();
            // find the first object with the column number and remove it
            for (int i = 0; i < columnList.size(); ++i)
            {
                DJ_ColumnProfile columnProfile = (DJ_ColumnProfile) columnList
                        .elementAt(i);
                if (columnProfile.getColumnNumber() == column)
                {
                    columnList.removeElementAt(i);
                    break;
                }
            }

            // write to the session
            saveSession(session, profile);
        }
    }

    /////////////////////////////////////////////////
    protected void addColumn(HttpServletRequest p_request)
    {
        // save entered data
        saveFormValues(p_request);

        // retrieve the column form data
        int column = (new Integer(p_request.getParameter(COLUMN))).intValue();
        long type = (new Long(p_request.getParameter(TYPE))).longValue();
        long rule = (new Long(p_request.getParameter(RULE))).longValue();
        int mode = Integer.parseInt(p_request.getParameter(CONTENT_MODE));

        String tableName = p_request.getParameter(TABLE_NAME);
        String label = p_request.getParameter(LABEL);

        // create a new column
        DJ_ColumnProfile columnProfile = new DJ_ColumnProfile(column,
                tableName, type, rule, mode, label);

        // retrieve the session
        HttpSession session = p_request.getSession(false);

        // retrieve the existing profile
        DJ_RecordProfile profile = (DJ_RecordProfile) session
                .getAttribute(PROFILE);

        Vector columnList = profile.getColumnList();

        // add the new column
        columnList.add(columnProfile);

        // write to the session
        saveSession(session, profile);
    }

    /////////////////////////////////////////////////
    protected TreeSet sortColumnList(Vector p_columnList)
    {
        // sort our columns
        TreeSet columns = new TreeSet(new columnComparator());
        columns.addAll(p_columnList);
        return (columns);
    }

    /////////////////////////////////////////////////
    protected void saveSession(HttpSession p_session, DJ_RecordProfile p_profile)
    {
        p_session.setAttribute(PROFILE, p_profile);
        p_session.setAttribute(COLUMN_OBJECTS, sortColumnList(p_profile
                .getColumnList()));
    }

    /////////////////////////////////////////////////
    protected class columnComparator implements Comparator
    {
        public int compare(Object p_a, Object p_b)
        {
            int x = ((DJ_ColumnProfile) p_a).getColumnNumber();
            int y = ((DJ_ColumnProfile) p_b).getColumnNumber();

            if (x < y)
                return (-1);
            if (x > y)
                return (1);
            return (0);
        }
    }

    private Logger theLogger = Logger.getLogger();
}
