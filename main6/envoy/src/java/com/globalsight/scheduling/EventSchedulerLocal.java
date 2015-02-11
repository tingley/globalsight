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
package com.globalsight.scheduling;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import org.quartz.Calendar;
import org.quartz.impl.calendar.AnnualCalendar;
import org.quartz.impl.calendar.DailyCalendar;
import org.quartz.impl.calendar.WeeklyCalendar;

import com.globalsight.calendar.AllowableIntervalRange;
import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.calendar.CalendarBusinessIntervals;
import com.globalsight.calendar.CalendarConstants;
import com.globalsight.calendar.Holiday;
import com.globalsight.calendar.ReservedTime;
import com.globalsight.calendar.SortedAllowableIntervalRanges;
import com.globalsight.calendar.WorkingDay;
import com.globalsight.calendar.WorkingHour;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.util.date.DateHelper;

/**
 * EventSchedulerLocal provides the main implementation of the EventScheduler
 * interface.
 */
public class EventSchedulerLocal implements EventScheduler
{
    private static final Logger s_category = Logger
            .getLogger(EventSchedulerLocal.class.getName());

    /**
     * Create an instance of the event scheduler.
     */
    public EventSchedulerLocal() throws EventSchedulerException
    {
        super();
    }

    public Date applyTimeExpression(Date p_date, int p_expression,
			BaseFluxCalendar p_calendar, TimeZone p_timeZone)
			throws RemoteException, EventSchedulerException {
		Date date = null;
		try {
			date = Quartz.applyTimeExpression(p_date, p_expression,
					p_calendar == null ? null : p_calendar
							.getCalendarInterval().getMultiCalendar(),
					p_timeZone);
		} catch (Exception e) {
			// ignore, just return the given date
			date = p_date;
		}

		return date;
	}

    /**
     * @see EventScheduler.determineDate(Date, BaseFluxCalendar, long)
     */
    public Date determineDate(Date p_startDate, BaseFluxCalendar p_calendar,
            long p_duration) throws RemoteException, EventSchedulerException
    {
        try
        {
            // first convert the duration from ms computed based on 24hr/day
            // to the business hours per day that's defined in the calendar.
            long[] daysHrsMins = DateHelper.daysHoursMinutes(p_duration);
            long range = DateHelper
                    .milliseconds(daysHrsMins[0], daysHrsMins[1],
                            daysHrsMins[2], p_calendar.getHoursPerDay());

            MultiCalendar calendar = p_calendar.getCalendarInterval().getMultiCalendar();

            int searchDuration = ((int) daysHrsMins[0] + 2) * 24 * 3600;
            return determineDate(calendar, p_startDate,
                      p_calendar, searchDuration,
                      range);
        }
        catch (Exception e)
        {
            Date date = determineDateWithoutCalendar(p_startDate, p_duration);
            s_category.error("Date was not determined by calendar. " + date);
            e.printStackTrace();
            return date;
        }
    }

    /**
     * @see EventScheduler.determineDate(Date, long)
     */
    public Date determineDateByDefaultCalendar(Date p_startDate,
            long p_duration, String p_companyId) throws RemoteException,
            EventSchedulerException
    {
        try
        {
            BaseFluxCalendar calendar = ServerProxy.getCalendarManager()
                    .findDefaultCalendar(p_companyId);
            return determineDate(p_startDate, calendar, p_duration);
        }
        catch (Exception e)
        {
            Date date = determineDateWithoutCalendar(p_startDate, p_duration);
            s_category.error("Date was not determined by calendar. " + date);
            e.printStackTrace();
            return date;
        }
    }

    /**
     * Find and return the scheduled event with the given id. (Note: only
     * scheduled events are persisted in the database; hence the restriction on
     * return value.)
     * 
     * @param p_eventId
     *            the id of the event to find.
     * 
     * @return the scheduled event that matches the given id.
     * 
     * @throws EventSchedulerException
     *             if the event does not exist or if a database problem occurs.
     */
    public ScheduledEvent findEvent(String p_eventId) throws RemoteException,
            EventSchedulerException
    {
        try
        {
            return Quartz.findEvent(p_eventId);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * @see EventScheduler.makeCalendarBusinessIntervals(List, List, List,
     *      Timestamp, TimeZone)
     */
    public CalendarBusinessIntervals makeCalendarBusinessIntervals(
            List<Holiday> p_holidays, List<ReservedTime> p_reservedTimes,
            List<ReservedTime> p_personalReservedTimes, List<WorkingDay> p_workingDays,
            Timestamp p_startDate, TimeZone p_timeZone) throws RemoteException,
            EventSchedulerException
    {
        // First create the business intervals
        MultiCalendar holidayCalendar = makeHolidayCalendar(p_holidays);

        MultiCalendar workingDayCalendar = makeWorkingDayCalendar(p_workingDays);

        MultiCalendar personalEventCalendar = makeReservedTimeCalendar(p_personalReservedTimes);

        MultiCalendar reservedTimeCalendar = makeReservedTimeCalendar(p_reservedTimes);

        try
        {
            // make sure we're at the beginning of the month
            p_startDate.setDayOfMonth(1);
            p_startDate.resetTimeOfDay();
            Date dt = p_startDate.getDate();

            return buildCalendarBusinessIntervals(
                    holidayCalendar, workingDayCalendar, reservedTimeCalendar,
                    personalEventCalendar, dt, p_timeZone);
        }
        catch (Exception e)
        {
            throw new EventSchedulerException(
                    EventSchedulerException.MSG_CALENDAR_BIZ_INTERVALS_FAILED,
                    null, e);
        }
    }
    
    /**
     * Build holiday calendar for Quartz.
     * 
     * @param holidays
     * @return
     */
    private MultiCalendar makeHolidayCalendar(List<Holiday> holidays) {
    	MultiCalendar calendar = new MultiCalendar();
		for (Holiday holiday : holidays) {
			if (holiday.getCalendarAssociationState() != CalendarConstants.DELETED) {
				java.util.Calendar holidayCalendar = java.util.Calendar.getInstance();
				
				// Set day time of holiday.
				holidayCalendar.set(java.util.Calendar.MILLISECOND, 0);
				holidayCalendar.set(java.util.Calendar.SECOND, 0);
				holidayCalendar.set(java.util.Calendar.MINUTE, 0);
				holidayCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
				holidayCalendar.set(java.util.Calendar.DAY_OF_MONTH, holiday
						.getDayOfMonth() > 0 ? holiday.getDayOfMonth() : 1);
				holidayCalendar.set(java.util.Calendar.MONTH, holiday
						.getMonth());

				if (holiday.getEndingYear().intValue() > 0) {
					// Make a annual calendar which excluded the holiday
					AnnualCalendar annualCalendar = new AnnualCalendar();
					annualCalendar.setDayExcluded(holidayCalendar, true);

					// Set the end date of this holiday
					holidayCalendar.set(java.util.Calendar.YEAR, holiday
							.getEndingYear().intValue());
					RangeCalendar endDateCalendar = new RangeCalendar(
							holidayCalendar.getTimeInMillis());
					endDateCalendar.setBaseCalendar(annualCalendar);

					calendar.addCalendar(endDateCalendar);
				} else {
					AnnualCalendar annualCalendar = new AnnualCalendar();
					annualCalendar.setDayExcluded(holidayCalendar, true);
					calendar.addCalendar(annualCalendar);
				}

			}

		}
		return calendar;            
    }
    
    private MultiCalendar makeWorkingDayCalendar(List<WorkingDay> workingDays) {
    	MultiCalendar calendar = new MultiCalendar();
    	Map<Integer, Calendar> weekDays = new HashMap<Integer, Calendar>();
    	
    	for (WorkingDay workingDay : workingDays) {	
    		if (workingDay.getCalendarAssociationState() != CalendarConstants.DELETED) {
    			List<WorkingHour> workingHours = workingDay.getWorkingHours();
    			for (WorkingHour workingHour : workingHours) {
    				
    				Calendar calendar1 = weekDays.get(new Integer(workingHour.getWorkingDay().getDay()));
    				
    				if (calendar1 == null) {
    					java.util.Calendar endCalendar = workingHour.getEndCalendar();
    					endCalendar.add(java.util.Calendar.MILLISECOND, -1);
    					
						DailyCalendar dailyCalendar = new DailyCalendar(workingHour.getStartCalendar(), endCalendar);
						dailyCalendar.setInvertTimeRange(true);
						
						weekDays.put(new Integer(workingHour.getWorkingDay().getDay()), dailyCalendar);
					} else {
						MultiCalendar multiCalendar = new MultiCalendar();
						multiCalendar.setIsIntersect(false);
						
						java.util.Calendar endCalendar = workingHour.getEndCalendar();
    					endCalendar.add(java.util.Calendar.MILLISECOND, -1);
    					
						DailyCalendar dailyCalendar = new DailyCalendar(workingHour.getStartCalendar(), endCalendar);
						dailyCalendar.setInvertTimeRange(true);
						
						multiCalendar.addCalendar(calendar1);
						multiCalendar.addCalendar(dailyCalendar);
						
						weekDays.put(new Integer(workingHour.getWorkingDay().getDay()), multiCalendar);
					}
				}
    		}
    	}
    	
    	WeeklyCalendar weeklyCalendar = new WeeklyCalendar();
    	for (int  i = java.util.Calendar.SUNDAY; i <= java.util.Calendar.SATURDAY; i++) {
    		Calendar baseCalendar = weekDays.get(new Integer(i));
    		weeklyCalendar.setDayExcluded(i,  baseCalendar == null);
    		
    		if (baseCalendar != null) {
	    		WeeklyWorkingHourCalendar weeklyWorkingHourCalendar = new WeeklyWorkingHourCalendar(i);
				weeklyWorkingHourCalendar.setBaseCalendar(baseCalendar);
				
				calendar.addCalendar(weeklyWorkingHourCalendar);
    		}
    	}
    	
    	calendar.addCalendar(weeklyCalendar);
    		
        return calendar;
    }

    /**
     * This method performs both scheduling or/and unscheduling of an event
     * using the information within the given HashMap object. The method
     * delegates the call to the message listener within the JMS pool
     * (asynchronous process).
     * 
     * @param p_map -
     *            A HashMap containig all required information for scheduling
     *            or/and unscheduling of an event.
     * 
     * @throws EventSchedulerException
     *             if the given event cannot be scheduled for any reason.
     */
    public void performSchedulingProcess(HashMap p_map) throws RemoteException,
            EventSchedulerException
    {
    	
        try
        {
            CompanyWrapper.saveCurrentCompanyIdInMap(p_map, s_category);

            JmsHelper.sendMessageToQueue(p_map, JmsHelper.JMS_SCHEDULING_QUEUE);
        }
        catch (Exception je)
        {
            s_category.error("EventSchedulerLocal: " + je.getMessage(), je);
            throwException(EventSchedulerException.MSG_SCHEDULING_FAILED, null,
                    je);
        }
    }

    /**
     * @see EventScheduler.scheduleEvent(SchedulingInformation)
     */
    public void scheduleEvent(SchedulingInformation p_schedulingInformation)
            throws RemoteException, EventSchedulerException
    {
        try
        {
            // record objectId, objectType, and eventType in event info map
            HashMap eventInfo = p_schedulingInformation.getEventInfo();
            eventInfo.put(SchedulerConstants.SCHEDULE_DOMAIN_OBJ_ID, new Long(
                    p_schedulingInformation.getObjectId()));
            eventInfo.put(SchedulerConstants.DOMAIN_OBJ_TYPE, new Integer(
                    p_schedulingInformation.getObjectType()));
            eventInfo.put(SchedulerConstants.SCHEDULE_EVENT_TYPE, new Integer(
                    p_schedulingInformation.getEventType()));

            // create a Quartz job
            String jobId = scheduleEvent(
                    p_schedulingInformation.getStartDate(),
                    p_schedulingInformation.getRecurranceExpression(),
                    p_schedulingInformation.getRepeatCount(),
                    p_schedulingInformation.getListener(),
                    p_schedulingInformation.getCalendar(), eventInfo,
                    p_schedulingInformation.getEventTypeName());

            // persist info in our db now
            persistEvent(p_schedulingInformation.getObjectId(),
                    p_schedulingInformation.getObjectType(),
                    p_schedulingInformation.getEventType(), jobId);
        }
        catch (Exception e)
        {
            throw new EventSchedulerException(
                    EventSchedulerException.MSG_SCHEDULING_FAILED, null, e);
        }
    }

    /**
     * @see EventScheduler.unschedule(FluxEventMap)
     */
    public void unschedule(FluxEventMap p_fluxEventMap) throws RemoteException,
            EventSchedulerException
    {
        try
        {
            // unschedule event
            Quartz.unschedule(p_fluxEventMap.getEventId());
            // now remove it from our own db
            EventSchedulerHelper.removeFluxEventMap(p_fluxEventMap);
        }
        catch (Exception e)
        {
            String[] args = { String.valueOf(p_fluxEventMap.getEventId()) };
            throwException(EventSchedulerException.MSG_UNSCHEDULING_FAILED,
                    args, e);
        }
    }

    /**
     * Perform all steps necessary to get the Quartz subsystem up-and-running on
     * the same server that is running this class.
     * 
     * @throws SystemStartupException
     */
    void startup() throws SystemStartupException
    {
        Quartz.startup();
    }

    /**
     * Perform all steps necessary to shutdown the Quartz subsystem.
     * 
     * @throws SystemShutdownException
     */
    void shutdown() throws SystemShutdownException
    {
        Quartz.shutdown();
    }

    // Throw an EventSchedulerException with the given parameters.
    private void throwException(String p_msg, String[] p_args,
            Exception p_exception) throws EventSchedulerException
    {
        throw new EventSchedulerException(p_msg, p_args, p_exception);
    }
    
    private CalendarBusinessIntervals buildCalendarBusinessIntervals(
            MultiCalendar holidayCalendar, 
            MultiCalendar workingDaysCalendar, 
            MultiCalendar reservedTimeCalendar,
            MultiCalendar personalEvenCalendar,
            Date dt, TimeZone timeZone) {
    	MultiCalendar multiCalendar = new MultiCalendar();
    	
    	// See SchedulerConstants.NUM_OF_DAYS_EXPRESSION. It is the second number of 32 days.
    	int durationSeconds = 32*24*3600;
    	
    	SortedSet<AllowableIntervalRange> holidayRangeSet = makeRange(
				holidayCalendar, dt, durationSeconds, timeZone,
				SchedulerConstants.CALENDAR_TYPE_HOLIDAY);
		multiCalendar.addCalendar(holidayCalendar);
    	
    	SortedSet<AllowableIntervalRange> workingHourRangeSet = makeRange(
				workingDaysCalendar, dt, durationSeconds, timeZone,
				SchedulerConstants.CALENDAR_TYPE_WORKINGHOUR);
		multiCalendar.addCalendar(workingDaysCalendar);
    	
        SortedAllowableIntervalRanges set = null;
        if (reservedTimeCalendar != null)
        {
            set = new SortedAllowableIntervalRanges(
                makeRange(
					reservedTimeCalendar, dt, durationSeconds, timeZone,
					SchedulerConstants.CALENDAR_TYPE_RESERVEDTIME));
            
            multiCalendar.addCalendar(reservedTimeCalendar);
        }

        // only for user calendar we need to set this
        SortedAllowableIntervalRanges personalSet = null;
        if (personalEvenCalendar != null)
        {
        	set = new SortedAllowableIntervalRanges(
                    makeRange(
					personalEvenCalendar, dt, durationSeconds, timeZone,
					SchedulerConstants.CALENDAR_TYPE_PERSONALRESERVED));
                
            multiCalendar.addCalendar(personalEvenCalendar);
        }
        
        return new CalendarBusinessIntervals(holidayCalendar,
				workingDaysCalendar, reservedTimeCalendar,
				personalEvenCalendar, multiCalendar,
				new SortedAllowableIntervalRanges(holidayRangeSet),
				new SortedAllowableIntervalRanges(workingHourRangeSet), set,
				personalSet);
    }
    
    /**
     * Get the sorted time range depending on the time and calendar. 
     * 
     * @param calendar
     * @param startDate
     * @param durationSeconds
     * @param timeZone
     * @param type
     * @return
     */
    public static SortedSet<AllowableIntervalRange> makeRange(MultiCalendar calendar,
			Date startDate, int durationSeconds, TimeZone timeZone, String type) {
    	SortedSet<AllowableIntervalRange> sortedSet = new TreeSet<AllowableIntervalRange>();
    	
    	java.util.Calendar startCalendar;
    	java.util.Calendar endCalendar;
    	
    	if (timeZone != null) {
    	    startCalendar = java.util.Calendar.getInstance(timeZone);
    	    endCalendar = java.util.Calendar.getInstance(timeZone);
    	} else {
    	    startCalendar = java.util.Calendar.getInstance();	
    	    endCalendar = java.util.Calendar.getInstance();
    	}
    	
    	startCalendar.setTime(startDate);
    	endCalendar.setTime(startDate);
    	endCalendar.add(java.util.Calendar.SECOND, durationSeconds);
    	
    	SortedSet<ExcludedTimeRange> excludes = new TreeSet<ExcludedTimeRange>();
    	calendar.makeRange(startCalendar, endCalendar, excludes);
    	
    	Date begin = startDate;
		for (ExcludedTimeRange timeRange : excludes) {
			if (!endCalendar.getTime().after(timeRange.getStartDate())) {
				sortedSet.add(new AllowableIntervalRange(begin, timeRange.getStartDate()));
				break;
			}
			
			if (timeRange.getStartDate().equals(timeRange.getEndDate())) {
				continue;
			}
			
			if (timeRange.getStartDate().after(begin)) {
				sortedSet.add(new AllowableIntervalRange(begin, timeRange.getStartDate()));
				begin = timeRange.getEndDate();
			} else if (timeRange.getEndDate().after(begin)) {
				begin = timeRange.getEndDate();
			}
			
			if (!endCalendar.getTime().after(timeRange.getEndDate())) {
				break;
			}
		}
		
		if (begin.before(endCalendar.getTime()))
		{
		    sortedSet.add(new AllowableIntervalRange(begin, endCalendar.getTime()));
		}
    	
    	return sortedSet;
    }
    
    private Date determineDate(MultiCalendar p_abd,
			Date p_startDate, BaseFluxCalendar p_calendar,
			int p_searchDuration, long p_duration) throws Exception {
		boolean found = false;
		long totalbusinessms = 0;
		Date previousEndDate = null;
		TimeZone timeZone = p_calendar.getTimeZone();

		Timestamp ts = new Timestamp(timeZone);
		ts.setDate(p_startDate);
		ts.resetTimeOfDay();
		Date initialDate = ts.getDate();

		while (!found) {
			boolean first = true;

			SortedSet set = makeRange(p_abd, initialDate, p_searchDuration,
					timeZone, SchedulerConstants.CALENDAR_TYPE_OTHER);

			if (set.size() == 0) {
				initialDate = applyTimeExpression(initialDate,
						p_searchDuration, p_calendar, timeZone);
			}

			Iterator i = set.iterator();
			while (i.hasNext() && !found) {
				AllowableIntervalRange abi = (AllowableIntervalRange) i.next();
				Date begin = abi.getBegin();
				// update beging date if it's less than start date in the first
				// loop
				if (first && begin.before(p_startDate)) {
					begin = p_startDate;
				}

				Date end = abi.getEnd();
				if (first && end.before(p_startDate)) {
					end = p_startDate;
				}

				// Set the next init date to the end of the range. If the
				// start date <= end date, add one minute to make sure that
				// the next set of ranges would not include this one.
				initialDate = p_startDate.before(end) ? end : new Date(end
						.getTime() + 60000);

				// get the business ms of this interval.
				long diff = end.getTime() - begin.getTime();

				// add this interval to the total business ms.
				totalbusinessms += diff;
				// If the total exceeds the range we specified then go
				// back and add the difference.
				if (totalbusinessms > p_duration) {
					totalbusinessms = totalbusinessms - diff;
					long increment = p_duration - totalbusinessms;
					long newms = begin.getTime() + increment;
					initialDate = new Date(newms);
					found = true;
					if (initialDate.equals(begin)) {
						initialDate = previousEndDate == null ? end : previousEndDate;
					}
				}
				previousEndDate = end;
			} // inner while loop

			first = false;
		} // outer while loop

		return initialDate;
	}

    /*
     * Compute a date based on the given start date and duration. No calendar is
     * used for this date calculation.
     */
    private Date determineDateWithoutCalendar(Date p_startDate, long p_duration)
    {
        Timestamp ts = new Timestamp();
        ts.setDate(p_startDate);
        ts.add(Timestamp.SECOND, new Long(p_duration / 1000).intValue());

        return ts.getDate();
    }
    
    private MultiCalendar makeReservedTimeCalendar(List<ReservedTime> reservedTimes) {
    	if (reservedTimes == null || reservedTimes.isEmpty()) {
    		return null;
    	}
    	
    	MultiCalendar calendar = null;
    	for (ReservedTime rt : reservedTimes) {
    		if (rt.getCalendarAssociationState() != 
                CalendarConstants.DELETED)
            {
    			RangeCalendar rangeCalendar = new RangeCalendar(rt.getStartDate()
						.getTime(), rt.getEndDate().getTime());
    			rangeCalendar.setAllForbidden(true);
				
				if (calendar == null) {
					calendar = new MultiCalendar();
				}
				
				calendar.addCalendar(rangeCalendar);
            }
    	}
    	
    	return calendar;
    }

    /*
     * Save event info in database. This info will be used for stopping or
     * updating an existing event.
     */
    private void persistEvent(long p_objectId, int p_objectType,
            int p_eventType, String p_jobId) throws Exception
    {
        try
        {
            FluxEventMap fem = new FluxEventMap();
            fem.setDomainObjectId(p_objectId);
            fem.setDomainObjectType(p_objectType);
            fem.setEventId(p_jobId);
            fem.setEventType(p_eventType);
            EventSchedulerHelper.createFluxEventMap(fem);
        }
        catch (Exception e)
        {
            try
            {
                // try to stop the job. if it fails, log the error
                Quartz.unschedule(p_jobId);
            }
            catch (Exception ex)
            {
                s_category.error("Failed to " + "unschedule event with id: "
                        + p_jobId);
            }
            throw e;
        }
    }

    /*
     * Schedule the event in Quartz
     */
    private String scheduleEvent(Date startDate, String recurrance,
            int repeatCount, Class listener, BaseFluxCalendar calendar,
            HashMap eventInfo, String eventType) throws Exception
    {

        // create a new UnScheduledEvent
        UnscheduledEvent event = new UnscheduledEvent();
        event.setStartTime(startDate);
        event.setRecurrenceRule(recurrance);
        event.setRepeatCount(repeatCount);
        event.setEventHandlerClass(listener);
        event.setCalendar(calendar);
        event.setEventInfo(new EventInfo(eventInfo));

        return Quartz.schedule(event); 
    }
}
