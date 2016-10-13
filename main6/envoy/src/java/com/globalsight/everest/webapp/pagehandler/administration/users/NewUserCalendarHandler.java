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
package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.BasicUserCalendarHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * The page handler for creating and updating a user calendar.
 */

public class NewUserCalendarHandler extends PageHandler
    implements CalendarConstants
{
    
    public NewUserCalendarHandler()
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
        SessionManager sessionMgr = (SessionManager)session
        .getAttribute(SESSION_MANAGER);
        
        String action = (String) request.getParameter(ACTION);

        if (CANCEL_ACTION.equals(action))
        {
            CalendarHelper.clearReservedTimeState(sessionMgr);
            setDays(request, session);
        }
        else if (NEXT_ACTION.equals(action))
        {
            setUpNew(request, session);
        }
        else if (CHANGE_DATE_ACTION.equals(action))
        {
            setDate(request, session);
            updateCalFields(request, session);
        }
        else if (CHANGE_BASE_ACTION.equals(action))
        {
            changeBaseCal(request, session);
        }
        else if (SAVE_ACTION.equals(action))
        {
            UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
            CalendarHelper.updateUserCalReservedTimes(cal, sessionMgr);
            int days[] = CalendarHelper.getUserCalendarState(sessionMgr, cal);
            request.setAttribute(CalendarConstants.DAY_STATE, days);
        }
        else if (PREVIOUS_ACTION.equals(action) ||
                 action == null)
        {
            setDays(request, session);
        }
        request.setAttribute("tzs", CalendarHelper.getTimeZones(session));
        
        //Save the data from the base user page
        CreateUserWrapper wrapper =
            (CreateUserWrapper) sessionMgr.getAttribute(UserConstants.CREATE_USER_WRAPPER);
        String companyId = UserUtil.getSelectedCompanyId(request, wrapper);
        ArrayList list = (ArrayList)CalendarHelper.getAllCalendarsByCompanyId(companyId);
        request.setAttribute("allCals", list);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }


    /**
     * Get data needed for creating a user calendar.  The default
     * system calendar is used for prepopulating some of the fields.
     */
    private void setUpNew(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);

        //Save the data from the base user page
        CreateUserWrapper wrapper =
            (CreateUserWrapper) sessionMgr.getAttribute(UserConstants.CREATE_USER_WRAPPER);
        String companyId = UserUtil.getSelectedCompanyId(request, wrapper);
        ArrayList list = (ArrayList)CalendarHelper.getAllCalendarsByCompanyId(companyId);
        FluxCalendar sysCal = null;
        for (int i = 0; i < list.size(); i++)
        {
            sysCal = (FluxCalendar)list.get(i);
            if (sysCal.getIsDefault())
            {
                request.setAttribute("selectedCal",
                             String.valueOf(sysCal.getId()));
                sessionMgr.setAttribute("sysCal", sysCal);
                break;
            }
        }

        UserUtil.extractContactInfoData(request, wrapper);
        UserUtil.prepareRolesPage(session, request, wrapper, null, null, false);
        sessionMgr.setAttribute(UserConstants.CREATE_USER_WRAPPER, wrapper);

        // Get the each days state from the default system calendar
        int days[] = CalendarHelper.getCalendarState(sysCal,
             Calendar.getInstance().get(Calendar.MONTH),
             Calendar.getInstance().get(Calendar.YEAR));
        request.setAttribute(CalendarConstants.DAY_STATE, days);
        
    }

    /**
     * Save calendar values.
     */
    private void updateCalFields(HttpServletRequest request,
                                 HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        CreateUserWrapper wrapper =
            (CreateUserWrapper) sessionMgr.getAttribute(UserConstants.CREATE_USER_WRAPPER);

        UserUtil.extractCalendarData(request, wrapper.getUserId());

        // Get the each days state
        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        sessionMgr.setAttribute(CalendarConstants.CALENDAR, cal);
        int days[] = CalendarHelper.getUserCalendarState(sessionMgr, cal);
        request.setAttribute(CalendarConstants.DAY_STATE, days);
    }

    /**
     * Set the state for the days array.
     */
    private void setDays(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        int days[] = CalendarHelper.getUserCalendarState(sessionMgr, cal);
        request.setAttribute(CalendarConstants.DAY_STATE, days);
    }

    /**
     * Set the calendar id in the session.
     */
    private void saveUserData(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        CreateUserWrapper wrapper = (CreateUserWrapper)
            sessionMgr.getAttribute(UserConstants.CREATE_USER_WRAPPER);

        // Save data from previous page
        UserUtil.extractUserData(request, wrapper, false);
    }
    
    /**
     * Change the base calendar to the one the user selected.
     */
    private void changeBaseCal(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        CreateUserWrapper wrapper = (CreateUserWrapper)
            sessionMgr.getAttribute(UserConstants.CREATE_USER_WRAPPER);
        UserUtil.extractCalendarData(request, wrapper.getUserId(), true);
        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);

        BasicUserCalendarHandler.setSessionData(request, session, cal, true);
    }

    /**
     * Set the date the user is interesting in viewing.
     */
    private void setDate(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(CalendarConstants.VIEWMONTH,
             request.getParameter(CalendarConstants.MONTH_FIELD));
        sessionMgr.setAttribute(CalendarConstants.VIEWYEAR,
             request.getParameter(CalendarConstants.YEAR_FIELD));
    }

    /**
     * Clear the session manager
     *
     * @param session - The client's HttpSession where the
     * session manager is stored.
     */
    private void clearSessionManager(HttpSession session)
    {
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        sessionMgr.clear();
    }
}
