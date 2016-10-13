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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.calendar.ReservedTime;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.ReservedTimeComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 *  This class handles the reserved times in the user calendar.
 */
public class ReservedTimeHandler extends PageHandler
    implements CalendarConstants
{
    
    public ReservedTimeHandler()
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
        SessionManager sessionMgr =
                 (SessionManager)session.getAttribute(SESSION_MANAGER);

        String action = request.getParameter(ACTION);

        if (CANCEL_ACTION.equals(action))
        {
            // cancel editing/creating a reserved time
            clearReservedTime(session);
            setUpReservedTimes(request, session,
                 (Timestamp)sessionMgr.getAttribute("reservedTimeDate"));
        }
        else if (REMOVE_ACTION.equals(action))
        {
            removeReservedTime(request, session);
            setUpReservedTimes(request, session,
                 (Timestamp)sessionMgr.getAttribute("reservedTimeDate"));
        }
        else if (RESERVED_TIMES_ACTION.equals(action))
        {
            UserFluxCalendar cal = getCalendar(request, session);
            CalendarHelper.updateUserCalFields(request, cal);
            Timestamp date = setDate(request, session, cal);
            setUpReservedTimes(request, session, date);
        }
        else if (EDIT_ACTION.equals(action))
        {
            setUpReservedTime(request, session);
        }
        else if (SAVE_ACTION.equals(action))
        {
            if (request.getParameter("edit") != null)
            {
                saveReservedTime(request, session);
            }
            else
            {
                createReservedTime(request, session);
            }
            clearReservedTime(session);
            setUpReservedTimes(request, session,
                 (Timestamp)sessionMgr.getAttribute("reservedTimeDate"));
        }
        else
        {
            // action is null if sorting column
            setUpReservedTimes(request, session,
                 (Timestamp)sessionMgr.getAttribute("reservedTimeDate"));
        }
        
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    /**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {
        return new ReservedTimeControlFlowHelper(p_request, p_response);
    }

    /**
     * Clear the reserved time from the session manager.
     */
    public static void clearReservedTime(HttpSession session)
    {
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        sessionMgr.removeElement("reservedTime");
        sessionMgr.removeElement("reservedTimeIndex");
    }

    /**
     * Get the list of reserved times for a particular day.
     * There is a list of reserved times for a calendar and a list of
     * updated reserved times.  
     */
    private void setUpReservedTimes(HttpServletRequest request,
                                   HttpSession session, Timestamp date)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
           (SessionManager)session.getAttribute(SESSION_MANAGER);

        // Get list of updated reserved times.
        ArrayList updatedRTS = (ArrayList)
            sessionMgr.getAttribute("updatedReservedTimes");

        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        List existing = new ArrayList();
        List events = cal.getReservedTimes();
        existing.addAll(events);
        List personalExisting = cal.getPersonalReservedTimes();
        existing.addAll(personalExisting);
        List proposedExisting = cal.getProposedActivities();
        existing.addAll(proposedExisting);

        // Now create a list to show.  Loop through the existing reserved times
        // and make sure they aren't in the updated list.  If it is, check it's
        // state.  If removed, don't add to show list, otherwise use it.
        // Then go through the updated list and add all new ones to the show
        // list if they fall on the correct day.
        ArrayList showList = new ArrayList();
        
        for (int i = 0; i < existing.size(); i++)
        {
            ReservedTime rt = (ReservedTime)existing.get(i);
            if (rt.isReservedForGivenDate(date.getDate(), date.getTimeZone()))
            {
                ReservedTimeState rtState = inUpdateList(rt, updatedRTS);
                if ((rtState != null &&
                     rtState.getState() == ReservedTimeState.NEW) ||
                     (rtState == null &&
                     rt.getCalendarAssociationState() !=
                     com.globalsight.calendar.CalendarConstants.DELETED))
                {
                    rtState = new ReservedTimeState(rt);
                    rtState.setId(rt.getId());
                    showList.add(rtState);
                }
            }
        }

        if (updatedRTS != null)
        {
            for (int i = 0; i < updatedRTS.size(); i++)
            {
                ReservedTimeState rtState = (ReservedTimeState)updatedRTS.get(i);
                if (rtState.getState() != ReservedTimeState.REMOVED)
                {
                    ReservedTime rt = rtState.getReservedTime();
            
                    if (rt.isReservedForGivenDate(date.getDate(), 
                                                  date.getTimeZone()))
                    {
                        showList.add(rtState);
                    }
                }
            }
        }
        sessionMgr.setAttribute("showList", showList);
        Locale locale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        setTableNavigation(request, session, showList,
                           new ReservedTimeComparator(locale),
                           10,   // change this to be configurable!
                           RT_LIST, RT_KEY);

    }

    /**
     * Get the particular reserved time.
     */
    private void setUpReservedTime(HttpServletRequest request,
                                   HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
           (SessionManager)session.getAttribute(SESSION_MANAGER);
        ArrayList rts = (ArrayList)sessionMgr.getAttribute("showList");

        String index = request.getParameter("index");
        ReservedTimeState rtState =
             (ReservedTimeState)rts.get(Integer.parseInt(index));
        sessionMgr.setAttribute("reservedTime", rtState.getReservedTime());
        sessionMgr.setAttribute("reservedTimeIndex", index);
    }

    /**
     * Create the reserved time but don't add it to calendar.
     * Only update the reservedTimes list.
     */
    private void createReservedTime(HttpServletRequest request,
                                   HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
           (SessionManager)session.getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);

        CalendarHelper.updateReservedTimeState(request, sessionMgr, null,
                                                ReservedTimeState.NEW);

        setUpReservedTimes(request, session, 
                 (Timestamp)sessionMgr.getAttribute("reservedTimeDate"));
    }

    /**
     * Save the reserved time to the updated list.
     */
    private void saveReservedTime(HttpServletRequest request,
                                   HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
           (SessionManager)session.getAttribute(SESSION_MANAGER);
        String index = (String)sessionMgr.getAttribute("reservedTimeIndex");
        ArrayList showList = (ArrayList)sessionMgr.getAttribute("showList");
        ReservedTimeState rtState =
             (ReservedTimeState)showList.get(Integer.parseInt(index));
        CalendarHelper.updateReservedTimeState(request, sessionMgr,
                                                rtState,
                                                ReservedTimeState.EDITED);
    }

    /**
     * Remove a reserved time from a calendar.
     */
    private void removeReservedTime(HttpServletRequest request,
                                    HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
           (SessionManager)session.getAttribute(SESSION_MANAGER);
        String index = request.getParameter("index");
        ArrayList showList = (ArrayList)sessionMgr.getAttribute("showList");
        ReservedTimeState rtState =
             (ReservedTimeState)showList.get(Integer.parseInt(index));
        CalendarHelper.updateReservedTimeState(request, sessionMgr,
                                                rtState,
                                                ReservedTimeState.REMOVED);
    }

    /**
     * Get the user calendar. 
     */
    private UserFluxCalendar getCalendar(HttpServletRequest request,
                                         HttpSession session)
    throws EnvoyServletException
    
    {
        SessionManager sessionMgr =
               (SessionManager)session.getAttribute(SESSION_MANAGER);
            
        UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        if (cal == null)
        {
            // Got here from new user calendar
            cal = setUpCal(request, sessionMgr);
        }
        return cal;
    }

    /**
     * Create a user calendar.  Save data entered so far.
     * Set the "state" for the days array.
     */
    private UserFluxCalendar setUpCal(
                    HttpServletRequest request, SessionManager sessionMgr)
    throws EnvoyServletException
    {
        CreateUserWrapper wrapper = (CreateUserWrapper)
            sessionMgr.getAttribute(UserConstants.CREATE_USER_WRAPPER);
        UserUtil.extractCalendarData(request, wrapper.getUserId());
        String parentId = request.getParameter(CalendarConstants.BASE_CAL_FIELD);
        String activityBuffer = request.getParameter(CalendarConstants.BUFFER_FIELD);
        String tz = request.getParameter(CalendarConstants.TZ_FIELD);
        UserFluxCalendar cal = new UserFluxCalendar(Long.parseLong(parentId),
                wrapper.getUserId(), Integer.parseInt(activityBuffer), tz);
        sessionMgr.setAttribute(CalendarConstants.CALENDAR, cal);
        int month, year;
        String buf = (String)sessionMgr.getAttribute("viewMonth");
        if (buf == null)
        {
            month = Calendar.getInstance().get(Calendar.MONTH);
            year = Calendar.getInstance().get(Calendar.YEAR);
        }
        else
        {
            month = Integer.parseInt(buf);
            buf = (String)sessionMgr.getAttribute("viewYear");
            year = Integer.parseInt(buf);
        }
        int days[] = CalendarHelper.getUserCalendarState(sessionMgr, cal);
        request.setAttribute(CalendarConstants.DAY_STATE, days);
        return cal;
    }


    /**
     * Set the date in the session
     */
    private Timestamp setDate(HttpServletRequest request, 
                              HttpSession session, 
                              UserFluxCalendar p_calendar)
    {
        SessionManager sessionMgr =
           (SessionManager)session.getAttribute(SESSION_MANAGER);

        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String year = request.getParameter("year");

        // Set a timestamp
        Timestamp date = new Timestamp(Integer.parseInt(year),
                                       Integer.parseInt(month), 
                                       Integer.parseInt(day),
                                       Timestamp.DATE, 
                                       p_calendar.getTimeZone());
        
        sessionMgr.setAttribute("reservedTimeDate", date);
        return date;
    }

    /**
     * Return the reserved time if it's in the updated list.
     * Else return null;
     */
    private ReservedTimeState inUpdateList(ReservedTime rt,
                                           ArrayList updatedList)
    {
        if (updatedList == null)
        {
            return null;
        }
        for (int i = 0; i < updatedList.size(); i++)
        {
            ReservedTimeState state = (ReservedTimeState)updatedList.get(i);
            if (state.getId() == rt.getId())
            {
                return state;
            }
        }
        return null;
    }
}
