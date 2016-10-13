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

// GlobalSight
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.globalsight.calendar.AllowableIntervalRange;
import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.calendar.CalendarManagerException;
import com.globalsight.calendar.CalendarWorkingDay;
import com.globalsight.calendar.CalendarWorkingHour;
import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.Holiday;
import com.globalsight.calendar.ReservedTime;
import com.globalsight.calendar.SortedAllowableIntervalRanges;
import com.globalsight.calendar.UserCalendarWorkingDay;
import com.globalsight.calendar.UserCalendarWorkingHour;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.calendar.WorkingDay;
import com.globalsight.calendar.WorkingHour;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.util.GeneralException;

/**
 * A bunch of helper methods for operating on a System Calendar, User Calendar,
 * Holiday, and Reserved Time.
 */

public class CalendarHelper
{

    /**
     * Get all the system calendars
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List getAllCalendars() throws EnvoyServletException
    {
        try
        {

            return ServerProxy.getCalendarManager().getAllCalendars();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all the system calendars
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List getAllCalendarsByCompanyId(String p_companyId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCalendarManager().getAllCalendarsByCompanyId(
                    p_companyId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all the user calendars
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List getAllUserCalendars() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCalendarManager().getAllUserCalendars();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all the holidays
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List getAllHolidays() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCalendarManager().getAllHolidays();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Create a holiday, getting the data from the request.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Holiday createHoliday(HttpServletRequest request,
            HttpSession session) throws EnvoyServletException
    {
        String name = request.getParameter(HolidayConstants.NAME);
        String desc = request.getParameter(HolidayConstants.DESC);
        String start = request.getParameter(HolidayConstants.START);
        int dayOfMonth = 0, dayOfWeek = 0, endYear = 0, month = 0;
        boolean isAbsolute;
        String weekOfMonth, buf;
        if (start.equals("dayVaries"))
        {
            isAbsolute = false;
            weekOfMonth = request.getParameter(HolidayConstants.WHEN);
            buf = request.getParameter(HolidayConstants.DAY_OF_WEEK);
            dayOfWeek = Integer.parseInt(buf);
            buf = request.getParameter(HolidayConstants.MONTH2);
            month = Integer.parseInt(buf);
        }
        else
        {
            isAbsolute = true;
            buf = request.getParameter(HolidayConstants.DAY);
            dayOfMonth = Integer.parseInt(buf);
            buf = request.getParameter(HolidayConstants.MONTH1);
            month = Integer.parseInt(buf);
            weekOfMonth = null;
        }
        String end = request.getParameter(HolidayConstants.END);
        if (end != null && end.equals("year"))
        {
            String y = request.getParameter(HolidayConstants.YEAR);
            endYear = Integer.parseInt(y);
        }
        String companyId = CompanyThreadLocal.getInstance().getValue();
        Holiday holiday = null;
        try
        {
            holiday = new Holiday(name, desc, dayOfMonth, dayOfWeek, endYear,
                    isAbsolute, month, weekOfMonth, companyId);

            holiday = ServerProxy.getCalendarManager().createHoliday(holiday);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return holiday;
    }

    /**
     * Remove a holiday
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static void removeHoliday(long holidayId)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getCalendarManager().removeHoliday(holidayId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Update a holiday, getting the data from the request
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Holiday modifyHoliday(HttpServletRequest request,
            HttpSession session, long id) throws EnvoyServletException
    {
        Holiday holiday = CalendarHelper.getHoliday(id);
        holiday.setName(request.getParameter(HolidayConstants.NAME));
        holiday.setDescription(request.getParameter(HolidayConstants.DESC));
        String buf;
        String start = request.getParameter(HolidayConstants.START);
        if (start.equals("dayVaries"))
        {
            holiday.setWeekOfMonth(request.getParameter(HolidayConstants.WHEN));
            buf = request.getParameter(HolidayConstants.DAY_OF_WEEK);
            holiday.setDayOfWeek(new Integer(buf));
            buf = request.getParameter(HolidayConstants.MONTH2);
            holiday.setMonth(Integer.parseInt(buf));
            holiday.setIsAbsolute(false);
        }
        else
        {
            buf = request.getParameter(HolidayConstants.DAY);
            holiday.setDayOfMonth(Integer.parseInt(buf));
            buf = request.getParameter(HolidayConstants.MONTH1);
            holiday.setMonth(Integer.parseInt(buf));
            holiday.setWeekOfMonth(null);
            holiday.setIsAbsolute(true);
        }
        String end = request.getParameter(HolidayConstants.END);
        if (end != null && end.equals("year"))
        {
            buf = request.getParameter(HolidayConstants.YEAR);
            holiday.setEndingYear(new Integer(buf));
        }
        else
        {
            holiday.setEndingYear(new Integer(0));
        }
        try
        {
            holiday = ServerProxy.getCalendarManager().modifyHoliday(holiday);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return holiday;
    }

    /**
     * Get a holiday with given id
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static Holiday getHoliday(long holidayId)
            throws EnvoyServletException
    {
        Holiday holiday = null;
        try
        {
            holiday = ServerProxy.getCalendarManager().findHolidayById(
                    holidayId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return holiday;
    }

    /**
     * Get a user calendar with given id
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static UserFluxCalendar getUserCalendar(long id)
            throws EnvoyServletException
    {
        UserFluxCalendar cal = null;
        try
        {
            cal = ServerProxy.getCalendarManager().findUserCalendarById(id);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return cal;
    }

    /**
     * Get a user calendar with given user id
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static UserFluxCalendar getUserCalendarByOwner(String ownerId)
            throws EnvoyServletException
    {
        UserFluxCalendar cal = null;
        try
        {
            cal = ServerProxy.getCalendarManager().findUserCalendarByOwner(
                    ownerId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return cal;
    }

    /**
     * Get the default calendar
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static FluxCalendar getDefaultCalendar(String p_companyId)
            throws EnvoyServletException
    {
        FluxCalendar cal = null;
        try
        {
            cal = ServerProxy.getCalendarManager().findDefaultCalendar(
                    p_companyId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return cal;
    }

    /**
     * Get a calendar with given id
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static FluxCalendar getSysCalendar(long id)
            throws EnvoyServletException
    {
        FluxCalendar cal = null;
        try
        {
            cal = ServerProxy.getCalendarManager().findCalendarById(id);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return cal;
    }

    /**
     * Get reserved times for a given day
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List getReservedTimesForDay(long calId, Timestamp date)
            throws EnvoyServletException
    {
        List list = null;
        try
        {
            list = ServerProxy.getCalendarManager()
                    .findReservedTimesForGivenDate(calId, date);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return list;
    }

    /**
     * Remove a System calendar
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static void removeSysCalendar(long id) throws EnvoyServletException,
            CalendarManagerException, GeneralException, RemoteException
    {
        ServerProxy.getCalendarManager().removeCalendar(id);
    }

    /**
     * Remove a User calendar
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static void removeUserCalendar(String ownerUserId)
            throws EnvoyServletException, CalendarManagerException,
            GeneralException, RemoteException
    {
        ServerProxy.getCalendarManager().removeUserCalendar(ownerUserId);
    }

    /**
     * Make this the default calendar
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static void makeDefaultCalendar(long id, HttpSession session)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getCalendarManager().makeDefaultCalendar(id,
                    (String) session.getAttribute(WebAppConstants.USER_NAME));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Persist the user calendar.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static UserFluxCalendar modifyUserCalendar(HttpSession session,
            UserFluxCalendar cal) throws EnvoyServletException
    {
        try
        {
            String userId = (String) session
                    .getAttribute(WebAppConstants.USER_NAME);

            if (userId.equals(cal.getOwnerUserId()))
            {
                session.setAttribute(WebAppConstants.USER_TIME_ZONE,
                        cal.getTimeZone());
            }

            return ServerProxy.getCalendarManager().modifyUserCalendar(cal,
                    userId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Update a user calendar with values from request and save.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static UserFluxCalendar modifyUserCalendar(
            HttpServletRequest request, HttpSession session,
            UserFluxCalendar cal) throws EnvoyServletException
    {
        updateUserCalFields(request, cal);

        return modifyUserCalendar(session, cal);

    }

    /**
     * Update a user calendar with values from request and save. This
     * modification is basically replacing the calendar info by its parent
     * calendar.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static UserFluxCalendar modifyUserCalendarFromBase(String parentId,
            HttpSession session, UserFluxCalendar cal)
            throws EnvoyServletException
    {
        updateUserCalFieldsFromBase(parentId, cal);

        return modifyUserCalendar(session, cal);
    }

    /**
     * Update a calendar
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static FluxCalendar modifyCalendar(HttpServletRequest request,
            HttpSession session, FluxCalendar cal) throws EnvoyServletException
    {
        updateCalFields(request, cal);

        try
        {
            return ServerProxy.getCalendarManager().modifyCalendar(cal,
                    (String) session.getAttribute(WebAppConstants.USER_NAME));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get list of valid timezones in the system
     */
    public static List getTimeZones(HttpSession session)
    {
        ResourceBundle bundle = PageHandler.getBundle(session);
        String tzs = bundle.getString("timezones");
        StringTokenizer tok = new StringTokenizer(tzs, ", ");
        ArrayList list = new ArrayList();
        while (tok.hasMoreTokens())
        {
            list.add(tok.nextToken());
        }
        return list;
    }

    /**
     * Create a user calendar.
     */
    public static UserFluxCalendar createUserCal(UserFluxCalendar cal,
            String userId, HttpSession session) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCalendarManager().createUserCalendar(cal,
                    (String) session.getAttribute(WebAppConstants.USER_NAME));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Create a system calendar.
     * 
     */
    public static FluxCalendar createCal(HttpServletRequest request,
            HttpSession session, FluxCalendar cal) throws EnvoyServletException
    {
        cal.setName(request.getParameter(CalendarConstants.NAME_FIELD));

        cal.setTimeZoneId(request.getParameter(CalendarConstants.TZ_FIELD));
        String hours = request.getParameter(CalendarConstants.BIZ_HOURS_FIELD);
        cal.setHoursPerDay(Integer.parseInt(hours));
        cal.setCompanyId(Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue()));

        // Calculate working hours and add working days if needed
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.SUN_TIME_FIELD, 1);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.MON_TIME_FIELD, 2);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.TUE_TIME_FIELD, 3);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.WED_TIME_FIELD, 4);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.THU_TIME_FIELD, 5);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.FRI_TIME_FIELD, 6);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.SAT_TIME_FIELD, 7);
        return createCal(session, cal);
    }

    /**
     * Persist the calendar
     */
    public static FluxCalendar createCal(HttpSession session, FluxCalendar cal)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCalendarManager().createCalendar(cal,
                    (String) session.getAttribute(WebAppConstants.USER_NAME));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Update the user calendar fields from the request parameters.
     */
    public static UserFluxCalendar updateUserCalFields(
            HttpServletRequest request, UserFluxCalendar cal)
            throws EnvoyServletException
    {
        String parentId = request
                .getParameter(CalendarConstants.BASE_CAL_FIELD);
        cal.setParentCalendar(getSysCalendar(Integer.parseInt(parentId)));
        cal.setTimeZoneId(request.getParameter(CalendarConstants.TZ_FIELD));
        String activityBuf = request
                .getParameter(CalendarConstants.BUFFER_FIELD);
        cal.setActivityBuffer(Integer.parseInt(activityBuf));

        updateWorkingDay(request, cal, CalendarConstants.USER,
                CalendarConstants.SUN_TIME_FIELD, 1);
        updateWorkingDay(request, cal, CalendarConstants.USER,
                CalendarConstants.MON_TIME_FIELD, 2);
        updateWorkingDay(request, cal, CalendarConstants.USER,
                CalendarConstants.TUE_TIME_FIELD, 3);
        updateWorkingDay(request, cal, CalendarConstants.USER,
                CalendarConstants.WED_TIME_FIELD, 4);
        updateWorkingDay(request, cal, CalendarConstants.USER,
                CalendarConstants.THU_TIME_FIELD, 5);
        updateWorkingDay(request, cal, CalendarConstants.USER,
                CalendarConstants.FRI_TIME_FIELD, 6);
        updateWorkingDay(request, cal, CalendarConstants.USER,
                CalendarConstants.SAT_TIME_FIELD, 7);

        return cal;
    }

    /**
     * Update the user calendar fields from the base calendar.
     */
    public static UserFluxCalendar updateUserCalFieldsFromBase(String parentId,
            UserFluxCalendar cal) throws EnvoyServletException
    {
        FluxCalendar baseCal = getSysCalendar(Integer.parseInt(parentId));
        return updateUserCalFieldsFromBase(baseCal, cal);
    }

    /**
     * Update the user calendar fields from the base calendar.
     */
    public static UserFluxCalendar updateUserCalFieldsFromBase(
            FluxCalendar baseCal, UserFluxCalendar cal)
            throws EnvoyServletException
    {
        cal.setParentCalendar(baseCal);
        cal.setTimeZoneId(baseCal.getTimeZoneId());

        // Remove all working days and working hours
        cal.removeAllWorkingDays();
        // Now add in all base calendar working days/hours
        List wds = baseCal.getWorkingDays();
        for (int i = 0; i < wds.size(); i++)
        {
            WorkingDay wd = (WorkingDay) wds.get(i);
            UserCalendarWorkingDay uwd = new UserCalendarWorkingDay(wd.getDay());
            List whs = wd.getWorkingHours();
            for (int j = 0; j < whs.size(); j++)
            {
                WorkingHour wh = (WorkingHour) whs.get(j);
                UserCalendarWorkingHour uwh = new UserCalendarWorkingHour(
                        wh.getOrder(), wh.getStartHour(), wh.getStartMinute(),
                        wh.getEndHour(), wh.getEndMinute());
                uwd.addWorkingHour(uwh, cal.getTimeZone());
            }
            cal.addWorkingDay(uwd);
        }
        return cal;
    }

    /**
     * Reserved times are not persisted until a calendar is saved. Loop through
     * the list and determine which need to be added to the calendar. They will
     * be persisted once the calendar is saved.
     */
    public static void updateUserCalReservedTimes(UserFluxCalendar cal,
            SessionManager sessionMgr) throws EnvoyServletException
    {
        ArrayList updatedRTS = (ArrayList) sessionMgr
                .getAttribute("updatedReservedTimes");
        if (updatedRTS != null)
        {
            for (int i = 0; i < updatedRTS.size(); i++)
            {
                ReservedTimeState rtState = (ReservedTimeState) updatedRTS
                        .get(i);
                ReservedTime rt = rtState.getReservedTime();
                switch (rtState.getState())
                {
                    case ReservedTimeState.REMOVED:
                        List list = null;
                        if (rt.getType() == ReservedTime.TYPE_EVENT)
                            list = cal.getReservedTimes();
                        else
                            list = cal.getPersonalReservedTimes();
                        ReservedTime found = null;
                        for (int j = 0; j < list.size(); j++)
                        {
                            found = (ReservedTime) list.get(j);
                            if (found == rt)
                            {
                                cal.removeReservedTime(rt);
                                break;
                            }
                        }
                        break;
                    case ReservedTimeState.EDITED:
                        // copy all fields
                        if (rt.getType() == ReservedTime.TYPE_EVENT)
                            list = cal.getReservedTimes();
                        else
                            list = cal.getPersonalReservedTimes();
                        found = null;
                        for (int j = 0; j < list.size(); j++)
                        {
                            found = (ReservedTime) list.get(j);
                            if (found.getId() == rtState.getId())
                            {
                                break;
                            }
                        }
                        copyReservedTimeFields(found, rtState.getReservedTime());
                        break;
                    case ReservedTimeState.NEW:
                        // add to the calendar
                        cal.addReservedTime(rt);
                        break;
                }
            }
            // Since it's been added to the calendar, we can remove it
            // from the session
            sessionMgr.removeElement("updatedReservedTimes");
        }
    }

    /**
     * Update the calendar fields fromt he request parameters.
     */
    public static void updateCalFields(HttpServletRequest request,
            FluxCalendar cal) throws EnvoyServletException
    {
        cal.setName(request.getParameter(CalendarConstants.NAME_FIELD));
        cal.setTimeZoneId(request.getParameter(CalendarConstants.TZ_FIELD));
        String hours = request.getParameter(CalendarConstants.BIZ_HOURS_FIELD);
        cal.setHoursPerDay(Integer.parseInt(hours));
        cal.setCompanyId(Long.parseLong(CompanyThreadLocal.getInstance()
                .getValue()));

        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.SUN_TIME_FIELD, 1);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.MON_TIME_FIELD, 2);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.TUE_TIME_FIELD, 3);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.WED_TIME_FIELD, 4);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.THU_TIME_FIELD, 5);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.FRI_TIME_FIELD, 6);
        updateWorkingDay(request, cal, CalendarConstants.SYSTEM,
                CalendarConstants.SAT_TIME_FIELD, 7);
    }

    public static void clearReservedTimeState(SessionManager sessionMgr)
    {
        sessionMgr.removeElement("showList");
        sessionMgr.removeElement("updatedReservedTimes");
    }

    /**
     * Update the "updatedReservedTime" list. This list is kept because reserved
     * times aren't updated in the Calendar object until save is pressed on the
     * reserved time list page.
     */
    public static void updateReservedTimeState(HttpServletRequest request,
            SessionManager sessionMgr, ReservedTimeState rtState, int newState)
    {
        ArrayList updatedRTS = (ArrayList) sessionMgr
                .getAttribute("updatedReservedTimes");
        if (updatedRTS == null)
        {
            updatedRTS = new ArrayList();
            sessionMgr.setAttribute("updatedReservedTimes", updatedRTS);
        }

        UserFluxCalendar cal = (UserFluxCalendar) sessionMgr
                .getAttribute(CalendarConstants.CALENDAR);
        switch (newState)
        {
            case ReservedTimeState.NEW:
                ReservedTime rt = updateReservedTimeFields(request, null, cal);
                ReservedTimeState rts = new ReservedTimeState(rt);
                rts.setState(ReservedTimeState.NEW);
                updatedRTS.add(rts);
                break;
            case ReservedTimeState.EDITED:
                if (rtState.getState() == ReservedTimeState.UNCHANGED)
                {
                    // first time edited. Just add to updated list.
                    rt = updateReservedTimeFields(request, null, cal);
                    rts = new ReservedTimeState(rt);
                    rts.setState(ReservedTimeState.EDITED);
                    rts.setId(rtState.getId());
                    updatedRTS.add(rts);
                }
                else
                {
                    // previously edited. Just update the object.
                    updateReservedTimeFields(request,
                            rtState.getReservedTime(), cal);
                }
                break;
            case ReservedTimeState.REMOVED:
                rt = rtState.getReservedTime();
                if (rtState.getState() == ReservedTimeState.NEW)
                {
                    rtState.setState(ReservedTimeState.REMOVED);
                }
                else
                {
                    rts = new ReservedTimeState(rt);
                    rts.setState(ReservedTimeState.REMOVED);
                    updatedRTS.add(rts);
                }
                break;
        }
        updatedRTS = (ArrayList) sessionMgr
                .getAttribute("updatedReservedTimes");

    }

    /**
     * Update all reserved time from the request parameters.
     */
    public static ReservedTime updateReservedTimeFields(
            HttpServletRequest request, ReservedTime rt,
            BaseFluxCalendar p_calendar)
    {
        String subject = request.getParameter(CalendarConstants.SUBJECT_FIELD);
        String type = ReservedTime.TYPE_EVENT;
        String month = request.getParameter(CalendarConstants.MONTH_FIELD);
        String day = request.getParameter(CalendarConstants.DAY_FIELD);
        String year = request.getParameter(CalendarConstants.YEAR_FIELD);
        Timestamp startDate = new Timestamp(p_calendar.getTimeZone());
        startDate.setMonth(Integer.parseInt(month));
        startDate.setDayOfMonth(Integer.parseInt(day));
        startDate.setYear(Integer.parseInt(year));
        String time = request.getParameter("timeRadio");
        int startHour, startMin, endHour, endMin;
        if (time.equals("allDay") || time.equals("allDayPersonal"))
        {
            startHour = 0;
            startMin = 0;
            endHour = 0;
            endMin = 0;
            if (time.equals("allDayPersonal"))
            {
                type = ReservedTime.TYPE_PERSONAL;
            }
        }
        else
        {
            String buf = request
                    .getParameter(CalendarConstants.START_HOUR_FIELD);
            startHour = Integer.parseInt(buf);
            buf = request.getParameter(CalendarConstants.START_MIN_FIELD);
            startMin = Integer.parseInt(buf);
            buf = request.getParameter(CalendarConstants.END_HOUR_FIELD);
            endHour = Integer.parseInt(buf);
            buf = request.getParameter(CalendarConstants.END_MIN_FIELD);
            endMin = Integer.parseInt(buf);
        }
        String end = request.getParameter("dateRadio");
        Timestamp endDate = null;
        if (end.equals("oneDayOnly"))
        {
            endDate = new Timestamp(p_calendar.getTimeZone());
            if (time.equals("allDay") || time.equals("allDayPersonal"))
            {
                endDate.setMonth(Integer.parseInt(month));
                endDate.setDayOfMonth(Integer.parseInt(day) + 1);
                endDate.setYear(Integer.parseInt(year));
            }
            else
            {
                endDate.setDate(startDate.getDate());
            }
        }
        else
        {
            endDate = new Timestamp(p_calendar.getTimeZone());
            String buf = request
                    .getParameter(CalendarConstants.END_MONTH_FIELD);
            int endMonth = Integer.parseInt(buf);
            buf = request.getParameter(CalendarConstants.END_DAY_FIELD);
            int endDay = Integer.parseInt(buf);
            buf = request.getParameter(CalendarConstants.END_YEAR_FIELD);
            int endYear = Integer.parseInt(buf);
            endDate.setMonth(endMonth);
            endDate.setDayOfMonth(endDay);
            endDate.setYear(endYear);
        }
        if (rt == null)
        {
            rt = new ReservedTime(subject, type, startDate, startHour,
                    startMin, endDate, endHour, endMin, null);
        }
        else
        {
            rt.setSubject(subject);
            rt.setType(type);
            rt.setStartTimestamp(startDate);
            rt.setStartHour(startHour);
            rt.setStartMinute(startMin);
            rt.setEndTimestamp(endDate);
            rt.setEndHour(endHour);
            rt.setEndMinute(endMin);
        }
        return rt;
    }

    /**
     * Return the reserved time with id "rtId".
     */
    public static ReservedTime getReservedTime(long rtId)
            throws EnvoyServletException
    {
        ReservedTime rt = null;
        try
        {
            rt = ServerProxy.getCalendarManager().findReservedTimeById(rtId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return rt;
    }

    /**
     * Add reserved time to the calendar with id "calId".
     */
    public static void addReservedTime(ReservedTime rt, long calId)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getCalendarManager().createReservedTime(calId, rt);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Return an array for every day of the month, that specifies the day type:
     * working or holiday
     */
    public static int[] getCalendarState(FluxCalendar cal, int month, int year)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getCalendarManager().computeCalendarIntervals(
                    cal,
                    new Timestamp(year, month, 1, Timestamp.DATE, cal
                            .getTimeZone()));
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        return computeDays(cal, month, year);
    }

    public static int[] getUserCalendarState(SessionManager sessionMgr,
            UserFluxCalendar cal) throws EnvoyServletException
    {
        return getUserCalendarState(sessionMgr, cal, true);
    }

    /**
     * Return an array for every day of the month, that specifies the day type:
     * working, holiday, reserved.
     */
    public static int[] getUserCalendarState(SessionManager sessionMgr,
            UserFluxCalendar cal, boolean recompute)
            throws EnvoyServletException
    {
        // First determine the month and year interested in
        int month, year;

        String buf = (String) sessionMgr
                .getAttribute(CalendarConstants.VIEWMONTH);
        if (buf == null)
        {
            month = Calendar.getInstance().get(Calendar.MONTH);
            year = Calendar.getInstance().get(Calendar.YEAR);
        }
        else
        {
            month = Integer.parseInt(buf);
            buf = (String) sessionMgr.getAttribute(CalendarConstants.VIEWYEAR);
            year = Integer.parseInt(buf);
        }
        if (recompute)
        {
            try
            {
                ServerProxy.getCalendarManager().computeUserCalendarIntervals(
                        cal,
                        new Timestamp(year, month, 1, Timestamp.DATE, cal
                                .getTimeZone()));
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }
        return computeDays(cal, month, year);
    }

    /**
     * Set the type in the day array. Days can be either a holiday, reserved,
     * working, or non-working.
     */
    private static int[] computeDays(BaseFluxCalendar cal, int month, int year)
    {
        // Default all days to non working days
        int[] days = new int[SchedulerConstants.NUM_OF_DAYS_INTERVAL];
        for (int i = 1; i < SchedulerConstants.NUM_OF_DAYS_INTERVAL; i++)
        {
            days[i] = CalendarConstants.NONWORKINGDAY;
        }

        // create a Timestamp for the given month and year
        TimeZone timeZone = cal.getTimeZone();
        Timestamp ts = new Timestamp(timeZone);
        ts.resetTimeOfDay();
        ts.setDayOfMonth(1);
        ts.setMonth(month);
        ts.setYear(year);

        // Get working days
        List wds = cal.getWorkingDays();

        // For each day of the month, determine if it falls on a working
        // day (i.e Monday, Tuesday...)
        Calendar tmpCal = Calendar.getInstance();
        tmpCal.set(Calendar.MONTH, month);
        tmpCal.set(Calendar.YEAR, year);
        for (int i = 1; i < SchedulerConstants.NUM_OF_DAYS_INTERVAL; i++)
        {
            tmpCal.set(Calendar.DATE, i);
            int dayOfWeek = tmpCal.get(Calendar.DAY_OF_WEEK);

            for (int j = 0; j < wds.size(); j++)
            {
                WorkingDay wd = (WorkingDay) wds.get(j);
                if (wd.getCalendarAssociationState() != com.globalsight.calendar.CalendarConstants.DELETED
                        && wd.getDay() == dayOfWeek)
                {
                    days[i] = CalendarConstants.WORKINGDAY;
                }
            }
        }

        // Get days with reserved times
        SortedAllowableIntervalRanges bi = cal
                .getBizIntervalRangeForReservedTimes();
        updateDaysState(bi, timeZone, ts, days, CalendarConstants.RESERVEDTIME);

        // Get Personal days
        bi = cal.getBizIntervalRangeForPersonalReservedTimes();
        updateDaysState(bi, timeZone, ts, days, CalendarConstants.NONWORKINGDAY);

        // Get holidays
        bi = cal.getBizIntervalRangeForHolidays();
        updateDaysState(bi, timeZone, ts, days, CalendarConstants.NONWORKINGDAY);

        return days;
    }

    /**
     * Loop through IntervalRanges to determine day type.
     * 
     * @param bi
     *            The interval ranges
     * @param p_startDate
     *            - The initial date of the calendar (1st day of the month)
     * @param days
     *            The array of days for the month
     * @param type
     *            The type of day interested in (holiday, nonworking, etc)
     */
    private static void updateDaysState(SortedAllowableIntervalRanges bi,
            TimeZone p_timeZone, Timestamp p_startDate, int[] days, int type)
    {
        int lastDayOfMonth = p_startDate
                .getActualMaximum(Timestamp.DAY_OF_MONTH);
        if (bi != null && bi.hasNext())
        {
            AllowableIntervalRange r;
            int beginDay, endDay = 0;

            Timestamp ts = new Timestamp(p_timeZone);
            boolean firstTime = true;
            boolean isSameMonth = true;
            int tempBeginDay = 0;
            /*
             * Loop through the intervals. Need to check end day because the
             * interval is 32 days and can go into the next month which we're
             * not interested in.
             */
            while (bi.hasNext() && isSameMonth)
            {
                r = bi.nextRange();
                ts.setDate(r.getBegin());
                beginDay = ts.getDayOfMonth();
                if (ts.getYear() != p_startDate.getYear() && !bi.hasNext())
                {
                    return;
                }

                // if the begin date is after the first day of the month
                // we need to know
                if (firstTime)
                {
                    firstTime = false;
                    tempBeginDay = determineBeginDayOfRange(p_startDate, ts,
                            beginDay);
                }

                if (endDay == beginDay)
                {
                    days[endDay] = type;
                }
                else
                {
                    // From the previous end date to this begin date are
                    // dates with reserved time
                    for (int i = endDay; i < beginDay; i++)
                    {
                        days[i] = type;
                    }
                }

                ts.setDate(r.getEnd());
                // special case for last day of the month (as non-working day)
                if (ts.getMonth() > p_startDate.getMonth())
                {
                    days[endDay] = type;
                }
                endDay = ts.getDayOfMonth();
                isSameMonth = p_startDate.getMonth() == ts.getMonth();
            }
            // make sure to mark the missing days prior to the 1st day of month
            for (int i = 0; i <= tempBeginDay; i++)
            {
                if (days[i] != CalendarConstants.NONWORKINGDAY)
                {
                    days[i] = type;
                }
            }
            // if the last day of the range was before the actual last
            // day of the month, mark the rest of the days
            if (ts.getYear() == p_startDate.getYear()
                    && ts.getMonth() == p_startDate.getMonth()
                    && endDay < lastDayOfMonth)
            {
                for (int i = endDay; i <= lastDayOfMonth; i++)
                {
                    if (days[i] != CalendarConstants.NONWORKINGDAY)
                    {
                        days[i] = type;
                    }
                }
            }
        }
        else if (bi != null) // Exceptional case
        {
            // When no result is sent back, it means that the
            // whole period of time is excluded (reserved or holiday)
            for (int i = 0; i <= lastDayOfMonth; i++)
            {
                if (days[i] != CalendarConstants.NONWORKINGDAY)
                {
                    days[i] = type;
                }
            }
        }
    }

    /**
     * Update the time fields in a working day.
     */
    private static void updateWorkingDay(HttpServletRequest request,
            BaseFluxCalendar cal, int type, String time, int day)
    {
        WorkingDay wd = cal.getWorkingDay(day);
        String times = request.getParameter(time);
        StringTokenizer tok = new StringTokenizer(times, ", ");
        int count = 1;
        while (tok.hasMoreTokens())
        {
            String startHour = tok.nextToken();
            String startMin = tok.nextToken();
            String endHour = tok.nextToken();
            String endMin = tok.nextToken();
            if (!startHour.equals("-1"))
            {
                WorkingHour wh1 = WorkingHourCreator.createWorkingHour(type,
                        count, Integer.parseInt(startHour),
                        Integer.parseInt(startMin), Integer.parseInt(endHour),
                        Integer.parseInt(endMin));
                if (wd == null)
                {
                    wd = WorkingDayCreator.createWorkingDay(type, day);
                    wd.addWorkingHour(wh1, cal.getTimeZone());
                    cal.addWorkingDay(wd);
                }
                else
                {
                    WorkingHour wh2 = wd.getWorkingHourByOrder(count);
                    if (wh2 == null)
                    {
                        wd.addWorkingHour(wh1, cal.getTimeZone());
                    }
                    else if (!wh1.equals(wh2))
                    {
                        wh2.setStartHour(Integer.parseInt(startHour));
                        wh2.setStartMinute(Integer.parseInt(startMin));
                        wh2.setEndHour(Integer.parseInt(endHour));
                        wh2.setEndMinute(Integer.parseInt(endMin));
                    }
                }
            }
            else
            {
                // check to see if need to remove working hour
                if (wd != null)
                {
                    WorkingHour wh2 = wd.getWorkingHourByOrder(count);
                    if (wh2 != null)
                    {
                        wh2.setStartHour(-1);
                        wh2.setStartMinute(-1);
                        wh2.setEndHour(-1);
                        wh2.setEndMinute(-1);
                    }
                }
            }
            count++;
        }
    }

    private static void copyReservedTimeFields(ReservedTime oldRT,
            ReservedTime newRT)
    {
        oldRT.setSubject(newRT.getSubject());
        oldRT.setType(newRT.getType());
        oldRT.setStartTimestamp(newRT.getStartTimestamp());
        oldRT.setStartHour(newRT.getStartHour());
        oldRT.setStartMinute(newRT.getStartMinute());
        oldRT.setEndTimestamp(newRT.getEndTimestamp());
        oldRT.setEndHour(newRT.getEndHour());
        oldRT.setEndMinute(newRT.getEndMinute());
    }

    /*
     * This will return an integer that's used as the limit of the loop
     * responsible for marking days prior to the begin date of the first range
     * (if the first begin starts after midnight, then we have to mark it).
     */
    private static int determineBeginDayOfRange(Timestamp p_startDate,
            Timestamp p_beginDayOfRange, int p_beginDay)
    {
        int tempBeginDay = 0;

        if (p_beginDayOfRange.isGreaterThan(p_startDate))
        {
            if (p_beginDayOfRange.getHour() > 0
                    || p_beginDayOfRange.getMinute() > 0
                    || p_beginDayOfRange.getSecond() > 0)
            {
                tempBeginDay = p_beginDay;
            }
            else
            {
                tempBeginDay = p_beginDay - 1;
            }
        }
        return tempBeginDay;
    }
}

class WorkingDayCreator
{

    public static WorkingDay createWorkingDay(int id, int day)
    {
        switch (id)
        {
            case CalendarConstants.SYSTEM:
                return new CalendarWorkingDay(day);
            case CalendarConstants.USER:
                return new UserCalendarWorkingDay(day);
        }
        return null;
    }
}

class WorkingHourCreator
{

    public static WorkingHour createWorkingHour(int id, int count,
            int startHour, int startMin, int endHour, int endMin)
    {
        switch (id)
        {
            case CalendarConstants.SYSTEM:
                return new CalendarWorkingHour(count, startHour, startMin,
                        endHour, endMin);
            case CalendarConstants.USER:
                return new UserCalendarWorkingHour(count, startHour, startMin,
                        endHour, endMin);
        }
        return null;
    }
}
