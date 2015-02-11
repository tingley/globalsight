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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.calendar.CalendarWorkingDay;
import com.globalsight.calendar.CalendarWorkingHour;
import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.Holiday;
import com.globalsight.calendar.WorkingDay;
import com.globalsight.calendar.WorkingHour;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * The page handler for creating and updating a system calendar.
 */

public class BasicCalendarHandler extends PageHandler
    implements CalendarConstants
{
    
    public BasicCalendarHandler()
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
            clearSessionExceptTableInfo(session, SYS_CAL_KEY);
            super.invokePageHandler(pageDescriptor, request,
                                    response, context);
            return;
        }
        else if (NEW_ACTION.equals(action))
        {
            clearSessionExceptTableInfo(session, SYS_CAL_KEY);
        }
        else if (APPLY_ACTION.equals(action))
        {
            saveCal(request, session);
        }
        else if (EDIT_ACTION.equals(action))
        {
            setUpEdit(request, session);
        }
        else if (DUPLICATE_ACTION.equals(action))
        {
            duplicate(request, session);
        }
        else if (CANCEL_HOL_ACTION.equals(action))
        {
            // Canceling editing holidays and returning to editing cal.
            resetUpEdit(request, session);
        }
        else if (SAVE_ACTION.equals(action))
        {
            // This is a save of holidays and returning to edit cal page.
            saveHolidays(request, session);
        }
        else if (CHANGE_DATE_ACTION.equals(action))
        {
            setDate(request, session);
            saveCal(request, session);
        }
        request.setAttribute("tzs", CalendarHelper.getTimeZones(session));
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    /**
     * Save the calendar values.  The first apply will create the
     * calendar.  After that, it updates it.  There is also a case
     * where a calendar object exists, but it hasn't been persisted
     * yet.  That occurs when they are creating a new Calendar
     * and go to the "Add holidays" page. 
     */
    private void saveCal(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        FluxCalendar cal = (FluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        if (cal == null)
        {
            cal = new FluxCalendar();
            cal = CalendarHelper.createCal(request, session, cal);
        }
        else if (cal.getId() == -1) 
        {
            // hasn't been persisted yet
            CalendarHelper.updateCalFields(request, cal);
            cal = CalendarHelper.createCal(request, session, cal);
        }
        else
        {
            cal = CalendarHelper.modifyCalendar(request, session, cal);
        }
        sessionMgr.setAttribute("id", Long.toString(cal.getId()));
        setInSession(request, sessionMgr, cal);
    }

    /**
     * Save the holidays for this calendar.
     */
    private void saveHolidays(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
        FluxCalendar cal = (FluxCalendar)
                 sessionMgr.getAttribute(CalendarConstants.CALENDAR);

        // Update holidays
        String holidayStr =
            request.getParameter(HolidayConstants.ADD_HOLIDAYS);
        if (holidayStr != null && !holidayStr.equals(""))
        {
            // holidays to add
            String[] holidays = holidayStr.split(",");
            for (int i = 0; i < holidays.length; i++)
            {
                Holiday holiday =
                    CalendarHelper.getHoliday(Long.parseLong(holidays[i]));
                cal.addHoliday(holiday);
            }
        }
        holidayStr =
            request.getParameter(HolidayConstants.REMOVE_HOLIDAYS);
        if (holidayStr != null && !holidayStr.equals(""))
        {
            // holidays to remove
            String[] holidays = holidayStr.split(",");
            for (int i = 0; i < holidays.length; i++)
            {
                Holiday holiday =
                    CalendarHelper.getHoliday(Long.parseLong(holidays[i]));
                cal.removeHoliday(holiday);
            }
        }
        setInSession(request, sessionMgr, cal);
    }

    /**
     * Set the calendar id in the session.
     */
    private void duplicate(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        String id = request.getParameter("id");
        FluxCalendar cal = copyCal(CalendarHelper.getSysCalendar(Long.parseLong(id)));
        setInSession(request, sessionMgr, cal);
    }

    /**
     * Set the calendar id in the session.
     */
    private void setUpEdit(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        String id = request.getParameter("id");
        FluxCalendar cal= CalendarHelper.getSysCalendar(Long.parseLong(id));
        setInSession(request, sessionMgr, cal);
    }
    
    /**
     * Get the calendar from the session
     */
    private void resetUpEdit(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)session
            .getAttribute(SESSION_MANAGER);
        FluxCalendar cal = (FluxCalendar)
            sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        setInSession(request, sessionMgr, cal);
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
     * Set calendar and calendar "state" in session.  "state" is
     * specified for every day of the month, such as working day,
     * holiday, nonworking day.
     */
    private void setInSession(HttpServletRequest request,
                              SessionManager sessionMgr,
                              FluxCalendar cal)
    throws EnvoyServletException
    {
        int month, year;

        sessionMgr.setAttribute(CalendarConstants.CALENDAR, cal);
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
        int days[] = CalendarHelper.getCalendarState(cal, month, year);
        request.setAttribute(CalendarConstants.DAY_STATE, days);
    }

    /**
     * For duplicate, create a calendar and copy everything but the name.
     */
    private FluxCalendar copyCal(FluxCalendar cal)
    {
        FluxCalendar copy = new FluxCalendar("", cal.getTimeZoneId(),
                             false, cal.getHoursPerDay());
        
        //Copy company id
        copy.setCompanyId(cal.getCompanyId());

        // Copy working days
        List wds = cal.getWorkingDays();
        for (int i = 0; i < wds.size(); i++)
        {
            WorkingDay wd = (WorkingDay)wds.get(i);
            WorkingDay copyWD = new CalendarWorkingDay(wd.getDay());
            List whs = wd.getWorkingHours();
            for (int j = 0; j < whs.size(); j++)
            {
                WorkingHour wh = (WorkingHour)whs.get(j);
                CalendarWorkingHour copyWH = new CalendarWorkingHour(
                    wh.getOrder(),
                    wh.getStartHour(), wh.getStartMinute(),
                    wh.getEndHour(), wh.getEndMinute());
                copyWD.addWorkingHour(copyWH, copy.getTimeZone());
            }
            copy.addWorkingDay(copyWD);
        }
    
        // Copy holidays
        ArrayList hols = (ArrayList) cal.getHolidaysList();
        for (int i = 0; i < hols.size(); i++)
        {
            Holiday hol = (Holiday)hols.get(i);
//            Holiday copyHol = new Holiday(hol.getName(), hol.getDescription(),
//                                hol.getDayOfMonth(), hol.getDayOfWeek().intValue(),
//                                hol.getEndingYear().intValue(), hol.getIsAbsolute(),
//                                hol.getMonth(), hol.getWeekOfMonth(),
//                                hol.getCompanyId());
            copy.addHoliday(hol);
        }
        return copy;
    }
}
