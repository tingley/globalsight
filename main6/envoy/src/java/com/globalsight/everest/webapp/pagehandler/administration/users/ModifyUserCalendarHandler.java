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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.BasicUserCalendarHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * The page handler for creating and updating a user calendar.
 */

public class ModifyUserCalendarHandler extends PageHandler
    implements CalendarConstants
{
    
    public ModifyUserCalendarHandler()
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
        
        String action = (String) request.getParameter(ACTION);
        if (EDIT_ACTION.equals(action))
        {
            setUpEdit(request, session);
        }
        else if (SAVE_ACTION.equals(action))
        {
            // save calendar, returning from reserved times
            save(request, session);
        }
        else if (CANCEL_ACTION.equals(action))
        {
            // canceling reserved times
            cancel(request, session);
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
        request.setAttribute("tzs", CalendarHelper.getTimeZones(session));
        
        SessionManager sessionMgr = (SessionManager) session
            .getAttribute(SESSION_MANAGER);
        ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
            .getAttribute(MODIFY_USER_WRAPPER);
        String companyId = CompanyWrapper.getCompanyIdByName(wrapper.getCompanyName());
        ArrayList list = (ArrayList)CalendarHelper.getAllCalendarsByCompanyId(companyId);
        request.setAttribute("allCals", list);
        request.setAttribute("fromUserEdit", "1");

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }


    /**
     * Set the calendar id in the session.
     */
    private void setUpEdit(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        ModifyUserWrapper wrapper = (ModifyUserWrapper)
            sessionMgr.getAttribute(MODIFY_USER_WRAPPER);

        // Save data from previous page
        UserUtil.extractUserData(request, wrapper, false);
        
        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        if (cal == null)
        {
            cal = getCalendar(wrapper);
            sessionMgr.setAttribute(CalendarConstants.CALENDAR, cal);
        }
        BasicUserCalendarHandler.setSessionData(request, session, cal, true);
    }
    
    /**
     * Change the base calendar to the one the user selected.
     */
    private void changeBaseCal(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        String parentId = (String)
            request.getParameter(CalendarConstants.BASE_CAL_FIELD);
        CalendarHelper.updateUserCalFieldsFromBase(parentId, cal);
        BasicUserCalendarHandler.setSessionData(
                    request, session, cal, true);

    }

    /**
     * Save the calendar
     */
    private void save(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);

        CalendarHelper.updateUserCalReservedTimes(cal, sessionMgr);
        ModifyUserWrapper wrapper = (ModifyUserWrapper)
            sessionMgr.getAttribute(MODIFY_USER_WRAPPER);
        BasicUserCalendarHandler.setSessionData(
                    request, session, cal, true);
    }

    private void cancel(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        CalendarHelper.clearReservedTimeState(sessionMgr);
        ModifyUserWrapper wrapper = (ModifyUserWrapper)
            sessionMgr.getAttribute(MODIFY_USER_WRAPPER);
        BasicUserCalendarHandler.setSessionData(
               request, session, getCalendar(wrapper), true);
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
     * Save calendar values.
     */
    private void updateCalFields(HttpServletRequest request,
                                 HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        ModifyUserWrapper wrapper =
            (ModifyUserWrapper) sessionMgr.getAttribute(MODIFY_USER_WRAPPER);

        UserUtil.extractCalendarData(request, wrapper.getUserId());

        // Get state for each day
        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        sessionMgr.setAttribute(CalendarConstants.CALENDAR, cal);
        int days[] = CalendarHelper.getUserCalendarState(sessionMgr, cal, true);
        request.setAttribute(CalendarConstants.DAY_STATE, days);
    }

    private UserFluxCalendar getCalendar(ModifyUserWrapper wrapper)
    throws EnvoyServletException
    {
        UserFluxCalendar cal = null;
        try {
            cal =
                ServerProxy.getCalendarManager().findUserCalendarByOwner(
                                                        wrapper.getUserId());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return cal;
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
