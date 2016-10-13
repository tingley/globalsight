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
package com.globalsight.calendar;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.importer.Entry;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.util.comparator.ReservedTimeComparator;
import com.globalsight.everest.util.comparator.UserInfoComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.scheduling.EventSchedulerHelper;
import com.globalsight.scheduling.FluxEventMap;
import com.globalsight.scheduling.ScheduledEvent;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.scheduling.SchedulingInformation;
import com.globalsight.util.SortUtil;

/**
 * CalendarManagerLocal provides the main implementation of the CalendarManager
 * interface.
 */
public class CalendarManagerLocal implements CalendarManager
{
    private static final Logger s_logger = Logger
            .getLogger(CalendarManagerLocal.class.getName());

    private static boolean s_isInstalled = false;

    private static HashMap<Integer, String> s_dayOfWeek = new HashMap<Integer, String>(
            7);
    static
    {
        validateInstallationKey();
    }

    // A map containing the alias representation of day of the week
    // used in Flux as a time expression. Note that the key is the
    // value used in Java for representing day of week (1-7 for sun-sat).
    static
    {
        s_dayOfWeek.put(new Integer(1), "SU");
        s_dayOfWeek.put(new Integer(2), "MO");
        s_dayOfWeek.put(new Integer(3), "TU");
        s_dayOfWeek.put(new Integer(4), "WE");
        s_dayOfWeek.put(new Integer(5), "TH");
        s_dayOfWeek.put(new Integer(6), "FR");
        s_dayOfWeek.put(new Integer(7), "SA");
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////

    /**
     * Create an instance of the event scheduler.
     */
    public CalendarManagerLocal() throws CalendarManagerException
    {
        super();
        // initialize with default values.
        int numberOfDays = 30;
        int startTime = 20;
        String recurrance = "+w";

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            startTime = sc
                    .getIntParameter(SystemConfigParamNames.CALENDAR_CLEANUP_START_TIME);
            recurrance = sc
                    .getStringParameter(SystemConfigParamNames.CALENDAR_CLEANUP_RECURRANCE);
            numberOfDays = sc
                    .getIntParameter(SystemConfigParamNames.CALENDAR_CLEANUP_PERIOD);
        }
        catch (Exception e)
        {
            logParameterSettingError(startTime, numberOfDays, recurrance, e);
        }
        // now start reserved time clean up scheduler...
        startCleanupScheduler(numberOfDays, startTime, recurrance);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Implementation of CalendarManager
    // ////////////////////////////////////////////////////////////////////
    /**
     * @see CalendarManager.computeCalendarIntervals( FluxCalendar, Timestamp)
     */
    public FluxCalendar computeCalendarIntervals(FluxCalendar p_calendar,
            Timestamp p_startDate) throws RemoteException,
            CalendarManagerException
    {
        try
        {
            buildCalendarBizIntervals(p_calendar, p_startDate);
            return p_calendar;
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(e);
        }
    }

    /**
     * @see CalendarManager.computeInterval(FluxCalendar, Date, Date)
     */
    public long computeInterval(FluxCalendar p_Calendar, Date oriDate,
            Date newDate) throws RemoteException, CalendarManagerException
    {
        long changedTime;
        boolean isAfter = newDate.after(oriDate);
        Calendar startCalendar = Calendar.getInstance(p_Calendar.getTimeZone());
        Calendar endCalendar = Calendar.getInstance(p_Calendar.getTimeZone());

        if (isAfter)
        {
            startCalendar.setTime(oriDate);
            endCalendar.setTime(newDate);
        }
        else
        {
            startCalendar.setTime(newDate);
            endCalendar.setTime(oriDate);
        }

        // set them as 0 to ignore seconds & millisecond
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);

        // get working days
        List wds = p_Calendar.getWorkingDays();
        HashMap<String, WorkingDay> workingDays = new HashMap<String, WorkingDay>();
        for (int i = 0; i < wds.size(); i++)
        {
            WorkingDay wd = (WorkingDay) wds.get(i);
            workingDays.put(wd.getDay() + "", wd);
        }

        // computer time day by day
        int startYear = startCalendar.get(Calendar.YEAR);
        int startDayOfYear = startCalendar.get(Calendar.DAY_OF_YEAR);
        int endDayOfYear = endCalendar.get(Calendar.DAY_OF_YEAR);
        int endYear = endCalendar.get(Calendar.YEAR);

        int daysCount = 0;
        List holiDays = p_Calendar.getHolidaysList();
        while (true)
        {
            // if both the year and dayOfYear are same, loop terminates
            if (startYear == endYear && startDayOfYear == endDayOfYear)
            {
                break;
            }

            if (isWorkingDay(startCalendar, workingDays, holiDays))
            {
                daysCount = daysCount + 1;
            }

            // add one day to Calendar
            startCalendar.add(Calendar.DAY_OF_YEAR, 1);
            startYear = startCalendar.get(Calendar.YEAR);
            startDayOfYear = startCalendar.get(Calendar.DAY_OF_YEAR);
        }

        // int hoursPerDay = companyCalendar.getHoursPerDay();
        changedTime = 24 * 60 * 60 * 1000 * daysCount;
        if (!isAfter)
        {
            changedTime = 0 - changedTime;
        }

        if (isWorkingDay(startCalendar, workingDays, holiDays))
        {
            // add time in one day
            long cc = endCalendar.getTimeInMillis()
                    - startCalendar.getTimeInMillis();
            if (isAfter)
            {
                changedTime = changedTime + cc;
            }
            else
            {
                changedTime = changedTime - cc;
            }
        }

        return changedTime;
    }

    /**
     * @see CalendarManager.computeUserCalendarIntervals( UserFluxCalendar,
     *      Timestamp)
     */
    public UserFluxCalendar computeUserCalendarIntervals(
            UserFluxCalendar p_userCalendar, Timestamp p_startDate)
            throws RemoteException, CalendarManagerException
    {
        // set the parent calendar
        p_userCalendar.setParentCalendar(getFluxCalendarById(new Long(
                p_userCalendar.getParentCalendarId())));
        try
        {
            buildUserCalendarBizIntervals(p_userCalendar, p_startDate);
            return p_userCalendar;
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(e);
        }
    }

    /**
     * @see CalendarManager.createCalendar(FluxCalendar)
     */
    public FluxCalendar createCalendar(FluxCalendar p_calendar,
            String p_creatorUserId) throws RemoteException,
            CalendarManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        try
        {
            List holidays = p_calendar.getHolidaysList();
            p_calendar.clearHolidayList();

            p_calendar.setLastUpdatedBy(p_creatorUserId);
            p_calendar.setLastUpdatedTime(new Date());

            session.save(p_calendar);

            for (int i = 0; i < holidays.size(); i++)
            {
                Holiday holiday = (Holiday) holidays.get(i);
                session.saveOrUpdate(holiday);
                p_calendar.addHoliday(holiday);
            }

            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_CREATE_CALENDAR_FAILED, null,
                    e);
        }

        return findCalendarById(p_calendar.getId());
    }

    /**
     * @see CalendarManager.createHoliday(Holiday)
     */
    public Holiday createHoliday(Holiday p_holiday) throws RemoteException,
            CalendarManagerException
    {
        try
        {
            buildHolidayTimeExpression(p_holiday);
            HibernateUtil.save(p_holiday);
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_CREATE_HOLIDAY_FAILED, null, e);
        }

        return findHolidayById(p_holiday.getId());
    }

    /**
     * @see CalendarManager.createReservedTime(ReservedTime)
     */
    public ReservedTime createReservedTime(long p_userCalendarId,
            ReservedTime p_reservedTime) throws RemoteException,
            CalendarManagerException
    {
        UserFluxCalendar cal = getUserFluxCalendarById(new Long(
                p_userCalendarId));
        try
        {
            HibernateUtil.save(p_reservedTime);

            cal.addReservedTime(p_reservedTime);
            HibernateUtil.saveOrUpdate(cal);
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_CREATE_RESERVED_TIME_FAILED,
                    null, e);
        }
        return p_reservedTime;
    }

    /**
     * @see CalendarManage.createCalendar(UserFluxCalendar, String)
     */
    public UserFluxCalendar createUserCalendar(UserFluxCalendar p_userCalendar,
            String p_creatorUserId) throws RemoteException,
            CalendarManagerException
    {
        try
        {
            p_userCalendar.setLastUpdatedBy(p_creatorUserId);
            p_userCalendar.setLastUpdatedTime(new Date());
            HibernateUtil.save(p_userCalendar);
        }
        catch (Exception e)
        {
            String[] args =
            { p_userCalendar.getOwnerUserId() };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_CREATE_USER_CALENDAR_FAILED,
                    args, e);
        }

        return computeUserCalendarIntervals(p_userCalendar, new Timestamp());
    }

    /**
     * @see CalendarManager.findCalendar(long)
     */
    public FluxCalendar findCalendarById(long p_calendarId)
            throws RemoteException, CalendarManagerException
    {
        try
        {
            FluxCalendar cal = HibernateUtil.get(FluxCalendar.class,
                    p_calendarId);
            return computeCalendarIntervals(cal, new Timestamp());
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(e);
        }
    }

    /**
     * @see CalendarManager.findDefaultCalendar(String p_companyId)
     */
    public FluxCalendar findDefaultCalendar(String p_companyId)
            throws RemoteException, CalendarManagerException
    {
        FluxCalendar cal = getDefaultCalendar(p_companyId);
        return computeCalendarIntervals(cal, new Timestamp());
    }

    /**
     * @see CalendarManager.findHoliday(long)
     */
    public Holiday findHolidayById(long p_holidayId) throws RemoteException,
            CalendarManagerException
    {
        return getHolidayById(new Long(p_holidayId));
    }

    /**
     * @see CalendarManager.findHolidaysByCalendarId(long)
     */
    public List findHolidaysByCalendarId(long p_calendarId)
            throws RemoteException, CalendarManagerException
    {
        try
        {
            String sql = "select h.* from HOLIDAY h, CALENDAR_HOLIDAY ch "
                    + " where ch.CALENDAR_ID = ? and h.id = ch.HOLIDAY_ID";
            return HibernateUtil.searchWithSql(Holiday.class, sql, new Long(
                    p_calendarId));
        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_calendarId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_HOLIDAY_BY_CAL_ID_FAILED,
                    args, e);
        }
    }

    /**
     * @see CalendarManager.findReservedTime(long)
     */
    public ReservedTime findReservedTimeById(long p_reservedTimeId)
            throws RemoteException, CalendarManagerException
    {
        return getReservedTimeById(new Long(p_reservedTimeId));
    }

    /**
     * @see CalendarManager.findReservedTimesByOwnerAndTaskId(String, long)
     */
    public Collection findReservedTimesByOwnerAndTaskId(String p_userId,
            long p_taskId) throws RemoteException, CalendarManagerException
    {
        try
        {
            String sql = "select rt.* from RESERVED_TIME rt, USER_CALENDAR uc "
                    + " where rt.TASK_ID = ? "
                    + " and rt.USER_CALENDAR_ID = uc.ID "
                    + " and uc.OWNER_USER_ID = ?";

            return HibernateUtil.searchWithSql(ReservedTime.class, sql,
                    new Long(p_taskId), p_userId);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_taskId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_RESERVED_TIME_FOR_TASK_FAILED,
                    args, e);
        }
    }

    /**
     * @see CalendarManager.findReservedTimesForGivenDate(long, Timestamp)
     */
    public List findReservedTimesForGivenDate(long p_userCalendarId,
            Timestamp p_date) throws RemoteException, CalendarManagerException
    {
        try
        {
            // need to have a new instance since we'll be modifying the
            // values of this timestamp
            Timestamp ts = new Timestamp();
            ts.setDate(p_date.getDate());
            ts.resetTimeOfDay();
            Date begin = ts.getDate();

            // end date = begin date at 23:59:59 (to make it one whole day)
            ts.add(Timestamp.DAY, 1);
            ts.add(Timestamp.SECOND, -1);
            Date end = ts.getDate();

            String sql = "select * from RESERVED_TIME "
                    + " where USER_CALENDAR_ID = ? and "
                    + "((START_TIME BETWEEN #BEGIN and #END) or "
                    + "(#BEGIN BETWEEN START_TIME and END_TIME))";

            return HibernateUtil.searchWithSql(ReservedTime.class, sql,
                    new Long(p_userCalendarId), begin, end, begin);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_userCalendarId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_RESERVED_TIMES_FOR_DATE_FAILED,
                    args, e);
        }
    }

    /**
     * @see CalendarManager.findUserCalendarById(long)
     */
    public UserFluxCalendar findUserCalendarById(long p_userCalendarId)
            throws RemoteException, CalendarManagerException
    {
        UserFluxCalendar cal = getUserFluxCalendarById(new Long(
                p_userCalendarId));

        return computeUserCalendarIntervals(cal,
                new Timestamp(cal.getTimeZone()));
    }

    /**
     * @see CalendarManager.findUserCalendarByOwner(String);
     */
    public UserFluxCalendar findUserCalendarByOwner(String p_ownerUserId)
            throws RemoteException, CalendarManagerException
    {
        UserFluxCalendar cal = findUserCalendarByOwner(p_ownerUserId, true);
        return computeUserCalendarIntervals(cal, new Timestamp());
    }

    /**
     * @see CalendarManager.findUserTimeZone(String)
     */
    public TimeZone findUserTimeZone(String p_userId) throws RemoteException,
            CalendarManagerException
    {
        try
        {
            String sql = "select TIME_ZONE from USER_CALENDAR "
                    + " where OWNER_USER_ID = ? ";

            String result = (String) HibernateUtil.getFirstWithSql(sql,
                    p_userId);

            // in case the user does not have a calendar yet
            if (result == null)
            {
                return createUserCal(p_userId).getTimeZone();
            }

            return TimeZone.getTimeZone(result);
        }
        catch (Exception e)
        {
            String args[] =
            { p_userId };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_USER_TIME_ZONE, args, e);
        }
    }

    /**
     * @see CalendarManager.getAllCalendars()
     */
    public List getAllCalendars() throws RemoteException,
            CalendarManagerException
    {
        try
        {
            String hql = " from FluxCalendar c ";
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                return HibernateUtil.search(hql);
            }
            else
            {
                hql += " where c.companyId = ? ";
                return HibernateUtil.search(hql,
                        Long.parseLong(currentCompanyId));
            }
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_GET_ALL_CALENDARS_FAILED,
                    null, e);
        }
    }

    /**
     * @see CalendarManager.getAllCalendars()
     */
    public List getAllCalendarsByCompanyId(String p_companyId)
            throws RemoteException, CalendarManagerException
    {
        try
        {
            String hql = " from FluxCalendar c where c.companyId = ? ";
            return HibernateUtil.search(hql, Long.parseLong(p_companyId));
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_GET_ALL_CALENDARS_FAILED,
                    null, e);
        }
    }

    /**
     * @see CalendarManager.getAllHolidays()
     */
    public List getAllHolidays() throws RemoteException,
            CalendarManagerException
    {
        try
        {
            String hql = " from Holiday h ";
            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                return HibernateUtil.search(hql);
            }
            else
            {
                hql += " where h.companyId = ? ";
                return HibernateUtil.search(hql,
                        Long.parseLong(currentCompanyId));
            }
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_GET_ALL_HOLIDAYS_FAILED, null,
                    e);
        }
    }

    /**
     * @see CalendarManager.getAllUserCalendars()
     */
    public List getAllUserCalendars() throws RemoteException,
            CalendarManagerException
    {
        try
        {
            String sql = "select uc.* from user_calendar uc, calendar c "
                    + " where uc.calendar_id = c.id ";

            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                return HibernateUtil.searchWithSql(UserFluxCalendar.class, sql);
            }
            else
            {
                sql += " and c.company_id = ? ";
                return HibernateUtil.searchWithSql(UserFluxCalendar.class, sql,
                        Long.parseLong(currentCompanyId));
            }
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_GET_ALL_USER_CALENDARS_FAILED,
                    null, e);
        }
    }

    /**
     * @see CalendarManager.importEntries(List)
     */
    public void importEntries(List p_entries) throws RemoteException,
            CalendarManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        try
        {
            int size = p_entries.size();
            HashMap map = new HashMap();
            for (int i = 0; i < size; i++)
            {
                Entry entry = (Entry) p_entries.get(i);
                if (entry.isEntryValid())
                {
                    String username = entry.getUsername();
                    UserFluxCalendar cal = (UserFluxCalendar) map.get(username);
                    if (cal == null)
                    {
                        cal = findUserCalendarByOwner(username, true);
                        session.saveOrUpdate(cal);
                        map.put(username, cal);
                    }

                    TimeZone tz = cal.getTimeZone();

                    // imported entry would be 'event' type.
                    ReservedTime rt = new ReservedTime(entry.getSubject(),
                            ReservedTime.TYPE_EVENT, createTimestamp(
                                    entry.getStartDate(),
                                    entry.getStartDateFormatType(), tz), 0, 0,
                            createTimestamp(entry.getEndDate(),
                                    entry.getEndDateFormatType(), tz), 0, 0,
                            null);

                    session.save(rt);
                    cal.addReservedTime(rt);
                    session.saveOrUpdate(cal);
                }
            }
            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FAILED_TO_IMPORT_ENTRIES,
                    null, e);
        }
    }

    /**
     * @see CalendarManager.makeDefaultCalendar(long, String)
     */
    public void makeDefaultCalendar(long p_calendarId, String p_modifierUserId)
            throws RemoteException, CalendarManagerException
    {
        FluxCalendar cal = getFluxCalendarById(new Long(p_calendarId));
        // no need to proceed if this already is a default calendar
        if (cal.getIsDefault())
        {
            return;
        }

        FluxCalendar defaultCal = getDefaultCalendar(String.valueOf(cal
                .getCompanyId()));
        try
        {
            // reset the previous default calendar
            defaultCal.setIsDefault(false);
            defaultCal.setLastUpdatedBy(p_modifierUserId);
            defaultCal.setLastUpdatedTime(new Date());
            HibernateUtil.saveOrUpdate(defaultCal);

            // now set the new default calendar
            cal.setIsDefault(true);
            cal.setLastUpdatedBy(p_modifierUserId);
            cal.setLastUpdatedTime(new Date());
            HibernateUtil.saveOrUpdate(cal);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_calendarId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_MAKE_DEFAULT_CALENDAR_FAILED,
                    args, e);
        }
    }

    /**
     * @see CalendarManager.modifyCalendar(FluxCalendar)
     */
    public FluxCalendar modifyCalendar(FluxCalendar p_calendar,
            String p_modifierUserId) throws RemoteException,
            CalendarManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        try
        {
            p_calendar.setLastUpdatedBy(p_modifierUserId);
            p_calendar.setLastUpdatedTime(new Date());

            // let's check the holidays and see if a new one
            // is added or an existing one is deleted.
            updateCalendarHolidays(p_calendar);

            // now update the working days and working hours
            updateCalendarWorkingDays(p_calendar);

            HibernateUtil.update(p_calendar);

            tx.commit();
            return computeCalendarIntervals(p_calendar, new Timestamp());
        }
        catch (Exception e)
        {
            tx.rollback();
            String[] args =
            { String.valueOf(p_calendar.getId()) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_MODIFY_CALENDAR_FAILED, args,
                    e);
        }
    }

    /**
     * @see CalendarManager.modifyHoliday(Holiday)
     */
    public Holiday modifyHoliday(Holiday p_holiday) throws RemoteException,
            CalendarManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        try
        {
            Holiday holiday = HibernateUtil.get(Holiday.class,
                    p_holiday.getIdAsLong());

            holiday.setName(p_holiday.getName());
            holiday.setDayOfMonth(p_holiday.getDayOfMonth());
            holiday.setDayOfWeek(p_holiday.getDayOfWeek());
            holiday.setDescription(p_holiday.getDescription());
            holiday.setEndingYear(p_holiday.getEndingYear());
            holiday.setIsAbsolute(p_holiday.getIsAbsolute());
            holiday.setMonth(p_holiday.getMonth());
            holiday.setWeekOfMonth(p_holiday.getWeekOfMonth());

            buildHolidayTimeExpression(holiday);

            session.saveOrUpdate(holiday);
            tx.commit();

            return holiday;
        }
        catch (Exception e)
        {
            tx.rollback();
            String[] args =
            { String.valueOf(p_holiday.getId()) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_MODIFY_HOLIDAY_FAILED, args, e);
        }
    }

    /**
     * @see CalendarManager.modifyUserCalendar(UserFluxCalendar)
     */
    public UserFluxCalendar modifyUserCalendar(UserFluxCalendar p_userCalendar,
            String p_modifierUserId) throws RemoteException,
            CalendarManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        try
        {
            updateCalendarWorkingDays(p_userCalendar);
            updateReservedTimesByType(p_userCalendar);
            p_userCalendar.setLastUpdatedBy(p_modifierUserId);
            p_userCalendar.setLastUpdatedTime(new Date());

            HibernateUtil.update(p_userCalendar);

            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            String[] args =
            { p_userCalendar.getOwnerUserId() };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_MODIFY_USER_CALENDAR_FAILED,
                    args, e);

        }

        return computeUserCalendarIntervals(p_userCalendar, new Timestamp());
    }

    /**
     * @see CalendarManager.removeScheduledActivity(long, String)
     */
    public void removeScheduledActivity(long p_taskId, String p_userId)
            throws RemoteException, CalendarManagerException
    {
        removeScheduledActivity(p_taskId,
                findUserCalendarByOwner(p_userId, true));
    }

    /**
     * @see CalendarManager.removeScheduledActivity(long, UserFluxCalendar)
     */
    public void removeScheduledActivity(long p_taskId,
            UserFluxCalendar p_calendar) throws RemoteException,
            CalendarManagerException
    {
        String ownerId = "";
        try
        {
            ownerId = p_calendar.getOwnerUserId();

            Object[] rts = findReservedTimesByOwnerAndTaskId(ownerId, p_taskId)
                    .toArray();

            for (int i = 0; i < rts.length; i++)
            {
                ReservedTime rt = (ReservedTime) rts[i];
                // Get the appropriate reserved time collection
                p_calendar.getCollectionByType(rt.getType()).remove(rt);

            }

            HibernateUtil.saveOrUpdate(p_calendar);
            HibernateUtil.delete(Arrays.asList(rts));
        }
        catch (Exception e)
        {
            String[] args =
            { ownerId, String.valueOf(p_taskId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_REMOVE_RESERVED_TIME_FAILED,
                    args, e);
        }
    }

    /**
     * @see CalendarManager.removeScheduledActivities(long);
     */
    public void removeScheduledActivities(long p_taskId)
            throws RemoteException, CalendarManagerException
    {
        String ownerId = "";

        Session session = HibernateUtil.getSession();

        try
        {
            String sql = "select r.* from RESERVED_TIME r, USER_CALENDAR u "
                    + " where r.TASK_ID = :TASK_ID and r.USER_CALENDAR_ID = u.ID ";

            SQLQuery query = session.createSQLQuery(sql).addEntity(
                    ReservedTime.class);
            query.setParameter("TASK_ID", new Long(p_taskId));

            Object[] rts = query.list().toArray();

            HashMap map = new HashMap(4);

            for (int i = 0; i < rts.length; i++)
            {
                ReservedTime rt = (ReservedTime) rts[i];

                Long calId = rt.getUserFluxCalendar().getIdAsLong();
                UserFluxCalendar cal = (UserFluxCalendar) map.get(calId);
                if (cal == null)
                {
                    cal = rt.getUserFluxCalendar();
                    map.put(calId, cal);
                }

                ownerId = cal.getOwnerUserId();

                cal.getCollectionByType(rt.getType()).remove(rt);

                HibernateUtil.saveOrUpdate(cal);
            }

            HibernateUtil.delete(Arrays.asList(rts));
        }
        catch (Exception e)
        {
            String[] args =
            { ownerId, String.valueOf(p_taskId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_REMOVE_RESERVED_TIME_FAILED,
                    args, e);
        }
    }

    /**
     * @see CalendarManager.removeCalendar(long)
     */
    public void removeCalendar(long p_calendarId) throws RemoteException,
            CalendarManagerException
    {
        FluxCalendar cal = getFluxCalendarById(new Long(p_calendarId));

        // cannot remove a default calendar.
        if (cal.getIsDefault())
        {
            String[] args =
            { String.valueOf(p_calendarId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_CANNOT_REMOVE_DEFAULT_CAL,
                    args, null);
        }

        // cannot remove if user calendars are derived from this one
        checkForDependencies(p_calendarId);

        try
        {
            HibernateUtil.delete(cal);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            String[] args =
            { String.valueOf(p_calendarId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_REMOVE_CALENDAR_FAILED, args,
                    e);
        }
    }

    /**
     * @see CalendarManager.removeHoliday(long)
     */
    public void removeHoliday(long p_holidayId) throws RemoteException,
            CalendarManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        Object[] calendars = getAllCalendarsByHolidayId(session, p_holidayId)
                .toArray();

        try
        {
            Holiday holiday = (Holiday) session.get(Holiday.class, new Long(
                    p_holidayId));

            for (int i = 0; i < calendars.length; i++)
            {
                FluxCalendar calendar = (FluxCalendar) calendars[i];
                calendar.getHolidaysList().remove(holiday);
            }

            session.delete(holiday);
            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            String[] args =
            { String.valueOf(p_holidayId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_REMOVE_HOLIDAY_FAILED, args, e);
        }
    }

    /**
     * @see CalendarManager.removeUserCalendar(long)
     */
    public void removeUserCalendar(String p_ownerUserId)
            throws RemoteException, CalendarManagerException
    {
        try
        {
            UserFluxCalendar cal = findUserCalendarByOwner(p_ownerUserId, false);
            if (cal != null)
                HibernateUtil.delete(cal);
        }
        catch (Exception e)
        {
            String[] args =
            { p_ownerUserId };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_REMOVE_USER_CALENDAR_FAILED,
                    args, e);
        }
    }

    /**
     * @see CalendarManager.userUnavailabilityReport(List, int, int)
     */
    public HashMap userUnavailabilityReport(List p_userInfos, int p_month,
            int p_year) throws RemoteException, CalendarManagerException
    {
        try
        {
            int sz = p_userInfos == null ? -1 : p_userInfos.size();
            // now sort the users based on the full name
            if (sz > 0)
            {
                SortUtil.sort(p_userInfos,
                        new UserInfoComparator(4, Locale.getDefault()));
            }

            return getUserUnavailability(sz, p_userInfos, p_month, p_year);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Implementation of CalendarManager
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Static Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Determine whether the calendaring component should be exposed to the end
     * user. If false, no UI will be shown.
     */
    public static boolean isInstalled()
    {
        return s_isInstalled;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Methods
    // ////////////////////////////////////////////////////////////////////

    private boolean isWorkingDay(Calendar calendar,
            HashMap<String, WorkingDay> workingDays, List holiDays)
    {
        boolean isWorkingDay = false;

        int startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // is working day, then check if it is holiday
        if (workingDays.containsKey("" + startDayOfWeek))
        {
            boolean isHoliday = false;
            for (int i = 0; i < holiDays.size(); i++)
            {
                Holiday holiday = (Holiday) holiDays.get(i);

                if (holiday.isHoliday(calendar))
                {
                    // is holiday, ignore
                    isHoliday = true;
                    break;
                }

            }

            // if is working day, and is not holiday, count it
            if (!isHoliday)
            {
                isWorkingDay = true;
            }
            else
            {
                isWorkingDay = false;
            }
        }
        else
        {
            // is weekend (non-working day), ignore
            isWorkingDay = false;
        }

        return isWorkingDay;
    }

    // Flux uses time expression for date related calculations. A time
    // expression is created based on the following examples that are
    // supported thru our UI:
    // 1. "0 0 0 0 21 mar" --> March 21st
    // 2. "0 0 0 0 20 apr * * * * 2002" --> April 20th 2002
    // 3. "0 0 0 0 1MO jul" --> First Monday of July
    // 4. "0 0 0 0 $WE aug * * * * 2002" --> Last Wednesday of August 2002
    private void buildHolidayTimeExpression(Holiday p_holiday)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("0 0 0 0 ");
        // append the day of month/week
        if (p_holiday.getIsAbsolute())
        {
            sb.append(p_holiday.getDayOfMonth());
        }
        else
        {
            sb.append(p_holiday.getWeekOfMonth());
            sb.append(s_dayOfWeek.get(p_holiday.getDayOfWeek()));
        }

        // append the month
        sb.append(" ");
        sb.append(p_holiday.getMonth());

        // now append the end year (if any)
        int endingYear = p_holiday.getEndingYear().intValue();
        if (endingYear > 0)
        {
            sb.append(" * * * * ");
            sb.append(endingYear);
        }

        p_holiday.setTimeExpression(sb.toString());
    }

    // build the user calendar's business interval used by Flux and UI.
    // start date is used to determine the beginning of the range of
    // allowable business interval ranges set. For example, when a user
    // is viewing the calendar for the month of September, the start date
    // would have the month of September set as it's month-of-the-year.
    private void buildUserCalendarBizIntervals(UserFluxCalendar p_calendar,
            Timestamp p_startDate) throws Exception
    {
        CalendarBusinessIntervals bizIntervals = ServerProxy
                .getEventScheduler().makeCalendarBusinessIntervals(
                        p_calendar.getHolidays(),
                        p_calendar.getReservedTimes(),
                        p_calendar.getPersonalReservedTimes(),
                        p_calendar.getWorkingDays(), p_startDate,
                        p_calendar.getTimeZone());

        p_calendar.setBusinessIntervalInfo(bizIntervals);
    }

    // build the calendar's business interval used by Flux and UI.
    // start date is used to determine the beginning of the range of
    // allowable business interval ranges set. For example, when a user
    // is viewing the calendar for the month of September, the start date
    // would have the month of September set as it's month-of-the-year.
    private void buildCalendarBizIntervals(FluxCalendar p_calendar,
            Timestamp p_startDate) throws Exception
    {
        CalendarBusinessIntervals bizIntervals = ServerProxy
                .getEventScheduler().makeCalendarBusinessIntervals(
                        p_calendar.getHolidaysList(), null, null,
                        p_calendar.getWorkingDays(), p_startDate,
                        p_calendar.getTimeZone());

        p_calendar.setBusinessIntervalInfo(bizIntervals);
    }

    // Check for dependency before removing a calendar. If one or
    // more user calendars are derived from this calendar, then it
    // cannot be removed.
    private void checkForDependencies(long p_calendarId)
            throws CalendarManagerException
    {
        Vector args = new Vector(1);
        args.add(new Long(p_calendarId));
        List list = null;
        try
        {
            String hql = "from UserFluxCalendar u where u.parentCalendarId = ?";
            list = HibernateUtil.search(hql, new Long(p_calendarId));
        }
        catch (Exception e)
        {
            String[] arg =
            { String.valueOf(p_calendarId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FAILED_TO_CHECK_FOR_DEPENDENCY,
                    arg, null);
        }

        if (list != null && list.size() > 0)
        {
            HashMap map = new HashMap();
            for (int i = 0; i < list.size(); i++)
            {
                UserFluxCalendar calendar = (UserFluxCalendar) list.get(0);
                map.put(calendar.getIdAsLong(), calendar.getName());
            }
            String[] arg =
            { String.valueOf(p_calendarId), map.values().toString() };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_CANNOT_REMOVE_DUE_TO_DEPENDENCY,
                    arg, null);
        }
    }

    /*
     * Create a Timestamp object by parsing the given date and the format
     * pattern.
     * 
     * @param p_date - A string representation of date (i.e. 01/30/04). @param
     * p_dateFormatPattern - The format pattern (i.e. MM/dd/yy). @param
     * p_timeZone - The time zone used for date creation. @return The Timestamp
     * object created based on the given string values.
     */
    private Timestamp createTimestamp(String p_date,
            String p_dateFormatPattern, TimeZone p_timeZone)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(p_dateFormatPattern);
            sdf.setTimeZone(p_timeZone);
            Timestamp ts = new Timestamp(p_timeZone);
            ts.setDate(sdf.parse(p_date));
            return ts;
        }
        catch (Exception e)
        {
            s_logger.error("failed to parse date (date, pattern): " + p_date
                    + ",  " + p_dateFormatPattern, e);
            return null;
        }
    }

    /*
     * Automatically create user calendar based on the system's default calendar
     * for a user who does not already have a calendar.
     */
    private UserFluxCalendar createUserCal(String ownerUserId) throws Exception
    {
        // FluxCalendar baseCal = findDefaultCalendar();
        User owner = ServerProxy.getUserManager().getUser(ownerUserId);
        String companyId = CompanyWrapper.getCompanyIdByName(owner
                .getCompanyName());
        FluxCalendar baseCal = findDefaultCalendar(companyId);
        UserFluxCalendar cal = new UserFluxCalendar(baseCal.getId(),
                ownerUserId, baseCal.getTimeZoneId());
        // cal.setParentCalendar(baseCal);
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

        try
        {
            cal = createUserCalendar(cal, "system");
        }
        catch (Exception e)
        {
            s_logger.error("Failed to automatically create calendar: " + e);
        }
        return cal;
    }

    /*
     * Return a user calendar for the specified user id.
     */
    private UserFluxCalendar findUserCalendarByOwner(String p_ownerUserId,
            boolean p_editable) throws CalendarManagerException
    {
        try
        {
            String hql = "from UserFluxCalendar u where u.ownerUserId = ?";

            List result = HibernateUtil.search(hql, p_ownerUserId);

            if (result == null || result.size() == 0)
            {
                return p_editable ? createUserCal(p_ownerUserId) : null;
            }

            return (UserFluxCalendar) result.get(0);
        }
        catch (Exception e)
        {
            String args[] =
            { p_ownerUserId };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_USER_CAL_BY_OWNER_FAILED,
                    args, e);
        }
    }

    // Get a list of calendars that have the associated holiday with
    // the given id. This method is used during the removal of a holiday.
    private Collection getAllCalendarsByHolidayId(Session session,
            long p_holidayId) throws CalendarManagerException
    {
        try
        {
            String sql = " select c.* from CALENDAR c, CALENDAR_HOLIDAY ch "
                    + " where ch.HOLIDAY_ID = ? and c.ID = ch.CALENDAR_ID ";

            String currentCompanyId = CompanyThreadLocal.getInstance()
                    .getValue();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentCompanyId))
            {
                SQLQuery query = session.createSQLQuery(sql).addEntity(
                        FluxCalendar.class);
                query.setParameter(0, new Long(p_holidayId));
                return query.list();
            }
            else
            {
                sql += " and c.COMPANY_ID = ? ";
                SQLQuery query = session.createSQLQuery(sql).addEntity(
                        FluxCalendar.class);
                query.setParameter(0, new Long(p_holidayId));
                query.setParameter(1, Long.parseLong(currentCompanyId));
                return query.list();
            }
        }
        catch (Exception e)
        {
            String[] arg =
            { String.valueOf(p_holidayId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_GET_CALENDARS_BY_HOLIDAY_ID_FAILED,
                    arg, null);
        }
    }

    // Get the company's default calendar for specific company
    private FluxCalendar getDefaultCalendar(String p_companyId)
            throws CalendarManagerException
    {
        try
        {
            String sql = " select * from CALENDAR "
                    + " where IS_DEFAULT = 'Y' and company_id = ? ";

            return HibernateUtil.getFirstWithSql(FluxCalendar.class, sql,
                    Long.parseLong(p_companyId));
        }
        catch (Exception e)
        {
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_DEFAULT_CALENDAR_FAILED,
                    null, e);
        }
    }

    // get a FluxCalendar object by id
    private FluxCalendar getFluxCalendarById(Long p_id)
            throws CalendarManagerException
    {
        try
        {
            return HibernateUtil.get(FluxCalendar.class, p_id);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_id) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_CALENDAR_FAILED, args, e);
        }
    }

    // get a Holiday object by id
    private Holiday getHolidayById(Long p_id) throws CalendarManagerException
    {
        try
        {
            return HibernateUtil.get(Holiday.class, p_id);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_id) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_HOLIDAY_FAILED, args, e);
        }
    }

    /**
     * Get a list of holidays for a specific month of the year for the given
     * user.
     */
    private List getHolidayByUserIdAndDate(String p_username, int p_month,
            int p_year) throws Exception
    {
        String sql = "select h.* from HOLIDAY h, USER_CALENDAR uc, "
                + " CALENDAR_HOLIDAY ch where ch.CALENDAR_ID = uc.CALENDAR_ID "
                + " and ch.HOLIDAY_ID = h.ID and uc.OWNER_USER_ID = ? "
                + " and h.MONTH = ? and (h.ENDING_YEAR is null "
                + " or h.ENDING_YEAR = 0 or h.ENDING_YEAR = ?)";

        return HibernateUtil.searchWithSql(Holiday.class, sql, p_username,
                new Integer(p_month), new Integer(p_year));
    }

    // get a ReservedTime object by id
    private ReservedTime getReservedTimeById(Long p_id)
            throws CalendarManagerException
    {
        try
        {
            return HibernateUtil.get(ReservedTime.class, p_id);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_id) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_RESERVED_TIME_FAILED,
                    args, e);
        }
    }

    /**
     * Get the reserved times for a user that fall in between the given start
     * and end date.
     */
    private List getReservedTimeByOwnerAndDate(String p_username,
            Date p_startDate, Date p_endDate) throws Exception
    {
        String sql = "select rt.* from RESERVED_TIME rt, USER_CALENDAR uc "
                + " where rt.USER_CALENDAR_ID = uc.ID and "
                + " uc.OWNER_USER_ID = ? and "
                + " ((rt.START_TIME BETWEEN ? and ?) or "
                + " (? BETWEEN rt.START_TIME and rt.END_TIME)) ";

        return HibernateUtil.searchWithSql(ReservedTime.class, sql, p_username,
                p_startDate, p_endDate, p_startDate);
    }

    // get a UserFluxCalendar object by id
    private UserFluxCalendar getUserFluxCalendarById(Long p_userCalId)
            throws CalendarManagerException
    {
        try
        {
            return HibernateUtil.get(UserFluxCalendar.class, p_userCalId);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_userCalId) };
            throw new CalendarManagerException(
                    CalendarManagerException.MSG_FIND_USER_CAL_BY_ID_FAILED,
                    args, e);
        }
    }

    /**
     * Get user's unavailability for a specific month of the year. This includes
     * both reserved times and holidays (as a reserved time of type 'Holiday').
     */
    private HashMap getUserUnavailability(int p_userInfoListSize,
            List p_userInfos, int p_month, int p_year) throws Exception
    {
        HashMap map = new HashMap(p_userInfoListSize);
        // loop thru the users and get the holidays and reserved times
        for (int i = 0; i < p_userInfoListSize; i++)
        {
            UserInfo userInfo = (UserInfo) p_userInfos.get(i);
            // now get reserved times
            Timestamp ts = new Timestamp();
            ts.setYear(p_year);
            ts.setMonth(p_month);
            ts.setDayOfMonth(1);
            ts.resetTimeOfDay();
            Date begin = ts.getDate();

            // end date = end of the month
            ts.add(Timestamp.MONTH, 1);
            ts.add(Timestamp.DAY, -1);
            Date end = ts.getDate();

            List rts = getReservedTimeByOwnerAndDate(userInfo.getUserId(),
                    begin, end);

            List holidays = getHolidayByUserIdAndDate(userInfo.getUserId(),
                    p_month, p_year);

            int size = holidays == null ? -1 : holidays.size();
            // timestamp for holidays
            for (int j = 0; j < size; j++)
            {
                Holiday h = (Holiday) holidays.get(j);

                ts = new Timestamp(Timestamp.DATE);
                ts.setMonth(h.getMonth());
                ts.setYear(p_year);
                // preparing the info
                if (h.getDayOfMonth() > 0)
                {
                    ts.setDayOfMonth(h.getDayOfMonth());
                }
                else
                // i.e. second monday of April
                {
                    ts.setDayOfWeek(h.getDayOfWeek().intValue());
                    try
                    {
                        // for week 1 to 4
                        ts.setDayOfWeekInMonth(Integer.parseInt(h
                                .getWeekOfMonth()));
                    }
                    catch (Exception e)
                    {
                        // for last week defined as "$"
                        ts.setDayOfWeekInMonth(ts
                                .getActualMaximum(Timestamp.DAY_OF_WEEK_IN_MONTH));
                    }
                }

                rts.add(new ReservedTime(h.getName(), "Holiday", ts, 0, 0, ts,
                        0, 0, null));
            }

            SortUtil.sort(rts,
                    new ReservedTimeComparator(2, Locale.getDefault()));

            // put the reserved times for the user
            map.put(userInfo, rts);
        }

        return map;
    }

    /*
     * Log the exception generated during parameter retreival.
     */
    private void logParameterSettingError(int p_startHour, int p_numberOfDays,
            String p_recurrance, Exception p_exception)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Failed to get the calendaring cleanup parameters ");
        sb.append("from the envoy.properties file.  The default ");
        sb.append("values were set: \r\n");
        sb.append("cleanup start time = ");
        sb.append(p_startHour);
        sb.append("\r\n cleanup period = ");
        sb.append(p_numberOfDays);
        sb.append("\r\n cleanup recurrance = ");
        sb.append(p_recurrance);

        s_logger.error(sb.toString(), p_exception);
    }

    /*
     * Schedule the event for calendering related clean-up
     */
    private void scheduleEvent(int p_startHour, int p_numberOfDays,
            String p_recurrance, Long p_objId, Integer p_eventType,
            Integer p_objType) throws Exception
    {
        Timestamp start = new Timestamp();
        if (start.getHour() > p_startHour)
        {
            start.setHour(p_startHour);
            start.add(Timestamp.DAY, 1);
        }

        Class listener = ReservedTimeRemovalEvent.class;
        HashMap eventInfo = new HashMap();
        eventInfo.put("num_of_days", new Integer(p_numberOfDays));
        eventInfo.put("startTime", new Integer(p_startHour));
        eventInfo.put("recurrance", p_recurrance);

        SchedulingInformation schedulingInformation = new SchedulingInformation();
        schedulingInformation.setStartDate(start.getDate());
        schedulingInformation.setRecurranceExpression(p_recurrance);
        schedulingInformation.setListener(listener);
        schedulingInformation.setEventInfo(eventInfo);
        schedulingInformation.setEventTypeName("removalOfReservedType");
        schedulingInformation.setEventType(p_eventType.intValue());
        schedulingInformation.setRepeatCount(-1);
        schedulingInformation.setObjectId(p_objId.longValue());
        schedulingInformation.setObjectType(p_objType.intValue());

        // ServerProxy.getEventScheduler().scheduleEvent(
        // schedulingInformation);
    }

    /*
     * Schedule the event that's responsible for calendar related clean up
     * process (i.e. removal of old reserved times)
     */
    private void startCleanupScheduler(int p_numberOfDays, int p_startHour,
            String p_recurrance)
    {

        Integer eventType = (Integer) SchedulerConstants.s_eventTypes
                .get("ReservedTime");
        Integer objType = (Integer) SchedulerConstants.s_objectTypes
                .get(ReservedTime.class);
        Long objId = new Long(1);

        try
        {
            FluxEventMap fem = EventSchedulerHelper.findFluxEventMap(eventType,
                    objType, objId);

            if (fem == null)
            {
                // create a new job
                scheduleEvent(p_startHour, p_numberOfDays, p_recurrance, objId,
                        eventType, objType);
            }
            else
            {
                ScheduledEvent event = ServerProxy.getEventScheduler()
                        .findEvent(fem.getEventId());

                Integer numOfDays = (Integer) event.getEventInfo()
                        .findEntryValue(SchedulerConstants.NUM_OF_DAYS);

                Integer st = (Integer) event.getEventInfo().findEntryValue(
                        SchedulerConstants.START_TIME);

                String r = (String) event.getEventInfo().findEntryValue(
                        SchedulerConstants.RECURRANCE);

                if (numOfDays.intValue() != p_numberOfDays
                        || st.intValue() != p_startHour
                        || !r.equals(p_recurrance))
                {
                    // delete the old job and create a new one
                    unscheduleEvent(fem);
                    scheduleEvent(p_startHour, p_numberOfDays, p_recurrance,
                            objId, eventType, objType);
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
        }
    }

    /*
     * Unschedule the given event and then remove it from the db.
     */
    private void unscheduleEvent(FluxEventMap p_fluxEventMap) throws Exception
    {
        ServerProxy.getEventScheduler().unschedule(p_fluxEventMap);
    }

    private void updateCalendarHolidays(FluxCalendar p_calendar)
            throws Exception
    {
        List holidays = p_calendar.getHolidaysList();
        for (int i = 0; i < holidays.size(); i++)
        {
            Holiday holiday = (Holiday) holidays.get(i);

            if (holiday.getCalendarAssociationState() == CalendarConstants.DELETED)
            {
                holidays.remove(holiday);
                i--;
            }
        }
    }

    private void updateReservedTimesByType(UserFluxCalendar p_calendar)
    {
        // update actual activities and general events
        updateReservedTimes(p_calendar, p_calendar.getReservedTimes());

        // update proposed activities
        updateReservedTimes(p_calendar, p_calendar.getProposedActivities());

        // update personal events
        updateReservedTimes(p_calendar, p_calendar.getPersonalReservedTimes());
    }

    private void updateCalendarWorkingDays(BaseFluxCalendar p_calendar)
            throws Exception
    {
        List wkDays = p_calendar.getWorkingDays();
        for (int i = 0; i < wkDays.size(); i++)
        {
            WorkingDay workingDay = (WorkingDay) wkDays.get(i);
            if (workingDay.getCalendarAssociationState() == CalendarConstants.DELETED)
            {
                p_calendar.removeWorkingDayFromList(workingDay);
                i--;
                HibernateUtil.delete(workingDay);
            }
            else if (workingDay.getId() > 0)
            {
                updateCalendarWorkingHours(p_calendar.getTimeZone(), workingDay);

                if (workingDay.getWorkingHours().size() == 0)
                {
                    p_calendar.removeWorkingDayFromList(workingDay);
                    i--;

                    HibernateUtil.delete(workingDay);
                }
            }
        }
    }

    private void updateCalendarWorkingHours(TimeZone p_timeZone,
            WorkingDay p_workingDay) throws Exception
    {
        // List wkHoursClone = p_wkdClone.getWorkingHours();
        List wkHours = p_workingDay.getWorkingHours();

        for (int i = 0; i < wkHours.size(); i++)
        {
            WorkingHour wkh = (WorkingHour) wkHours.get(i);
            if (wkh.getEndHour() < 0)
            {
                p_workingDay.removeWorkingHour(wkh);
                i--;

                HibernateUtil.delete(wkh);
            }
            else if (wkh.getId() > 0)
            {
                wkh.computeStartDateAndDuration(p_timeZone);
            }
        }
    }

    private void updateReservedTimes(UserFluxCalendar p_calendar,
            List p_changedReservedTimes)
    {
        // List p_changedReservedTimes = p_calendar.getReservedTimes();
        int size = p_changedReservedTimes.size();
        for (int i = 0; i < size; i++)
        {
            ReservedTime reservedTime = (ReservedTime) p_changedReservedTimes
                    .get(i);

            if (reservedTime.getCalendarAssociationState() == CalendarConstants.DELETED)
            {
                p_calendar.removeReservedTime(reservedTime);
            }
            // for existing reserved time, update its values
            else if (reservedTime.getId() > 0)
            {
                reservedTime.computeDurationAndDates(p_calendar.getTimeZone());
            }
        }
    }

    /*
     * Determine whether the valid installation key was entered. The key is
     * entered during the installation and persisted in db. If the key is
     * invalid, no calendaring UI would be exposed to the user.
     */
    private static void validateInstallationKey()
    {
        try
        {
            String realKey = "CAL-" + "GS".hashCode() + "-"
                    + "pmcal".hashCode();
            s_isInstalled = SystemConfiguration
                    .isKeyValid(SystemConfigParamNames.CALENDAR_INSTALL_KEY);
        }
        catch (Exception e)
        {
            String msg = "Failed to validate the Calendaring installation key. ";
            s_logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Private Methods
    // ////////////////////////////////////////////////////////////////////
}
