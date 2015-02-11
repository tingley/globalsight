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

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.calendar.Holiday;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The page handler for creating and updating a holiday in the system.
 */

public class BasicHolidayHandler extends PageHandler
    implements HolidayConstants
{
    
    public BasicHolidayHandler()
    {
    }

    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);

        String action = request.getParameter(ACTION);

        if (CANCEL_ACTION.equals(action))
        {
            // clean session manager
            clearSessionExceptTableInfo(session, HOLIDAY_KEY);
        }
        else if (EDIT_ACTION.equals(action))
        {
            setUpEdit(request, session);
        }
        else if (NEW_ACTION.equals(action))
        {
            // save any holidays the user may have already put on the list
            saveState(request, session);
        }
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    /**
     * Set the holiday id in the session.
     */
    private void setUpEdit(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        String id = (String)request.getParameter("id");
        sessionMgr.setAttribute("id", id);
        Holiday holiday = CalendarHelper.getHoliday(Long.parseLong(id));
        request.setAttribute("holiday", holiday);
    }


    /**
     * Set the holiday id in the session.
     */
    private void saveState(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        // Convert comma separated string to list
        ArrayList holidayList = new ArrayList();
        String holidayStr =
            (String) request.getParameter(HolidayConstants.UPDATED_HOLIDAYS);
        if (holidayStr != null && !holidayStr.equals(""))
        {
            String[] holidays = holidayStr.split(",");
            for (int i = 0; i < holidays.length; i++)
            {
                Holiday holiday =
                    CalendarHelper.getHoliday(Long.parseLong(holidays[i]));
                holidayList.add(holiday);
            }
        }
        sessionMgr.setAttribute(HolidayConstants.CAL_HOLIDAY_LIST,
            holidayList);

        sessionMgr.setAttribute(HolidayConstants.ADD_HOLIDAYS,
            request.getParameter(HolidayConstants.ADD_HOLIDAYS));
        sessionMgr.setAttribute(HolidayConstants.REMOVE_HOLIDAYS,
            request.getParameter(HolidayConstants.REMOVE_HOLIDAYS));
    }

}
