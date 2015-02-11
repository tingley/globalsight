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
package com.globalsight.everest.webapp.pagehandler.administration.calendars;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.calendar.CalendarManagerException;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.util.GeneralException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Dispatches the user to the correct JSP.
 */
class ReservedTimeControlFlowHelper
    implements ControlFlowHelper, WebAppConstants, CalendarConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            CalendarControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;

    public ReservedTimeControlFlowHelper(HttpServletRequest p_request,
        HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    /**
     * Does the processing then returns the name of the link to follow.
     */
    public String determineLinkToFollow()
        throws EnvoyServletException
    {
        HttpSession p_session = m_request.getSession(false);

        String action = m_request.getParameter(ACTION);

        if (action == null || action.equals(REMOVE_ACTION))
        {
            return "self";
        }
        else if (action.equals(ACTIVITY_ACTION))
        {
            // Get taskId parameter
            String taskIdParam = m_request.getParameter(TASK_ID);
            long taskId = TaskHelper.getLong(taskIdParam);

            // get task state
            String taskStateParam = m_request.getParameter(TASK_STATE);
            // -10 is default task state
            int taskState = TaskHelper.getInt(taskStateParam, -10);
            // Get user id of the person who has logged in.
            User user = TaskHelper.getUser(p_session);

            try
            {
                //Get task
                Task task = TaskHelper.getTask(
                    user.getUserId(), taskId, taskState);

                if (task.getAcceptor() != null &&
                    task.getAcceptor().equals(user.getUserId()))
                {
                    return "detail";
                }
                else if (task.getAcceptor() == null &&
                    task.getProjectManagerId().equals(user.getUserId()))
                {
                    return "detail";
                }
                else if (task.getAcceptor() == null)
                {
                    List assignees = task.getAllAssignees();
                    for (int i = 0, max = assignees.size(); i < max; i++)
                    {
                        String assignee = (String)assignees.get(i);

                        if (assignee.equals(user.getUserId()))
                        {
                            return "detail";
                        }
                    }

                    return "noaccess";
                }

                return "noaccess";
            }
            catch (Exception e)
            {
                return "noaccess";
            }
        }

        return action;
    }
}
