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
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.calendar.UserFluxCalendar;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The page handler for creating and updating a user calendar.
 */

public class BasicUserCalendarHandler extends PageHandler
    implements CalendarConstants
{
    
    public BasicUserCalendarHandler()
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
            reset(request, session);
        }
        else if (EDIT_ACTION.equals(action))
        {
            setUpEdit(request, session);
        }
        else if (APPLY_ACTION.equals(action))
        {
            saveCal(request, session);
        }
        else if (SAVE_ACTION.equals(action))
        {
            // This is a save of reserved times.  Save to calendar,
            // but don't persist.
            update(request, session);
        }
        else if (CHANGE_DATE_ACTION.equals(action))
        {
            setDate(request, session);
            saveCal(request, session);
        }
        else if (CHANGE_BASE_ACTION.equals(action))
        {
            updateFromBase(request, session);
        }
        request.setAttribute("tzs", CalendarHelper.getTimeZones(session));
        ArrayList list = (ArrayList)CalendarHelper.getAllCalendars();
        request.setAttribute("allCals", list);


        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    public static void setSessionData(HttpServletRequest request,
                                      HttpSession session,
                                      UserFluxCalendar cal,
                                      boolean recompute)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        request.setAttribute("selectedCal",
                 String.valueOf(cal.getParentCalendarId()));
        int days[] = CalendarHelper.getUserCalendarState(sessionMgr, cal, recompute);
        request.setAttribute(CalendarConstants.DAY_STATE, days);
    }

    /**
     * Set the calendar id in the session.
     */
    private void setUpEdit(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        setSessionData(request, session, cal, false);
        request.setAttribute("fromCalList", "true");
    }
    
    /**
     * Save the calendar.
     */
    private void saveCal(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        cal = CalendarHelper.modifyUserCalendar(request, session, cal);
        sessionMgr.setAttribute(CalendarConstants.CALENDAR, cal);
        setSessionData(request, session, cal, true);
        request.setAttribute("fromCalList", "true");
    }

    /**
     * Set the date the user is interesting in viewing.
     */
    private void setDate(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute("viewMonth",
             request.getParameter(CalendarConstants.MONTH_FIELD));
        sessionMgr.setAttribute("viewYear",
             request.getParameter(CalendarConstants.YEAR_FIELD));
    }

    /**
     * Update the calendar with data from the base calendar
     */
    private void updateFromBase(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        String parentId =
                request.getParameter(CalendarConstants.BASE_CAL_FIELD);
        CalendarHelper.updateUserCalFieldsFromBase(parentId, cal);
        setSessionData(request, session, cal, true);
        request.setAttribute("fromCalList", "true");
    }

    /**
     * Make sure session is updated.
     */
    private void update(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        CalendarHelper.updateUserCalReservedTimes(cal, sessionMgr);
        setSessionData(request, session, cal, true);
        request.setAttribute("fromCalList", "true");
    }

    /**
     * Reset the session - refetch the calendar
     */
    private void reset(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        request.setAttribute("fromCalList", "true");
        CalendarHelper.clearReservedTimeState(sessionMgr);
        UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        setSessionData(request, session, cal, true);
    }

}
