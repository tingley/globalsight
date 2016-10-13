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

import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.quartz.Calendar;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;

import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;

public class Quartz
{

	private static final Logger s_logger = Logger
			.getLogger(Quartz.class);

	private static Quartz quartz = new Quartz();

	private static final String TRIGGER_SUFFIX = "TRIGGER";

	private static final String CALENDAR_SUFFIX = "CALENDAR";

	private static final String JOB_PREFIX = "JOB";

	private static int index = 0;

	private static final int MAX_INDEX = 10000;

	private Scheduler scheduler = null;

	private Quartz()
	{
	}

	/**
     * Get the date after the first given date and expression duration time.
     * 
     * @param date
     * @param expression
     * @param bizCalendar
     * @param timeZone
     * @return
     * @throws Exception
     */
	public static Date applyTimeExpression(Date date, int expression,
			Calendar bizCalendar, TimeZone timeZone) throws Exception
	{
		java.util.Calendar calendar = null;
		if (timeZone != null)
		{
			calendar = java.util.Calendar.getInstance(timeZone);
		}
		else
		{
			calendar = java.util.Calendar.getInstance();
		}

		calendar.setTime(date);
		calendar.add(java.util.Calendar.SECOND, expression);

		return calendar.getTime();
	}

	/**
     * Find out scheduled job with the specified job name.
     * 
     * @param eventId
     * @return
     * @throws Exception
     */
	public static ScheduledEvent findEvent(String eventId) throws Exception
	{
		return quartz._findEvent(eventId);
	}

	/**
     * Update scheduled job with the specified job name.
     * 
     * @param eventInfo
     * @param jobName
     * @throws Exception
     */
	public static void modifyJob(EventInfo eventInfo, String jobName)
			throws Exception
	{
		quartz._modifyJob(eventInfo, jobName);
	}

	/**
     * Schedule a job.
     * 
     * @param event
     * @return
     * @throws Exception
     */
	public static String schedule(UnscheduledEvent event) throws Exception
	{
		return quartz._schedule(event);
	}

	/**
     * Shut down Quartz server.
     * 
     * @throws SystemShutdownException
     */
	public static void shutdown() throws SystemShutdownException
	{
		quartz._shutdown();
	}

	/**
     * Startup Quartz server.
     * 
     * @throws SystemStartupException
     */
	public static void startup() throws SystemStartupException
	{
		quartz._startup();
	}

	/**
     * Un-schedule a job with the specified job name.
     * 
     * @param jobName
     * @return
     * @throws Exception
     */
	public static boolean unschedule(String jobName) throws Exception
	{
		return quartz._unschedule(jobName);
	}

	/**
     * Find out scheduled job with the specified job name.
     * 
     * @param eventId
     * @return
     * @throws Exception
     */
	private ScheduledEvent _findEvent(String eventId) throws Exception
	{
		ScheduledEvent event = new ScheduledEvent();
		JobDetail jobDetail = scheduler.getJobDetail(new JobKey(eventId, SchedulerConstants.DEFAULT_GROUP));
		
		Trigger trigger = scheduler.getTrigger(new TriggerKey(eventId + TRIGGER_SUFFIX,
				SchedulerConstants.DEFAULT_GROUP));
		
		event.setId(jobDetail.getKey().getName());
		
		event.setState(String.valueOf(scheduler.getTriggerState(trigger.getKey())));
		
		event.setEventInfo((EventInfo) jobDetail.getJobDataMap().get(
				SchedulerConstants.KEY_PARAM));

		return event;
	}

	/**
     * Modify existing job with the specified job name. In fact, only update the
     * event info parameter.
     * 
     * @param eventInfo
     * @param jobName
     * @throws Exception
     */
	private void _modifyJob(EventInfo eventInfo, String jobName)
			throws Exception
	{
		JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobName,
				SchedulerConstants.DEFAULT_GROUP));
		
		jobDetail.getJobDataMap().put(SchedulerConstants.KEY_PARAM, eventInfo);
		scheduler.addJob(jobDetail, true);
	}

	/**
     * Schedule an unscheduled job.
     * 
     * @param event
     * @return Return the job name.
     * @throws Exception
     */
	private String _schedule(UnscheduledEvent event) throws Exception
	{
		String jobName = JOB_PREFIX + System.currentTimeMillis() + getIndex();

		// Build job detail
		JobDetailImpl jobDetail = new JobDetailImpl();
		jobDetail.setName(jobName);
		jobDetail.setGroup(SchedulerConstants.DEFAULT_GROUP);
		jobDetail.setJobClass(event.getEventHandlerClass());
		
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		jobDataMap.put(SchedulerConstants.EVENT_KEY, EventHandler.INITIAL);
		jobDataMap.put(SchedulerConstants.KEY_PARAM, event.getEventInfo());

		// Build trigger for this job
		Trigger trigger = null;
		TimeExpressionConverter timeExpressionConverter = new TimeExpressionConverter(
				event.getRecurrenceRule());

		if (timeExpressionConverter.isInterval())
 {
			int repeatCount = (event.getRepeatCount() > -1) ? event
					.getRepeatCount() : -1;

			if (event.getCalendar() != null
					|| timeExpressionConverter.getCalendar() != null) {
				trigger = TriggerBuilder.newTrigger().withIdentity(
						jobName + TRIGGER_SUFFIX,
						SchedulerConstants.DEFAULT_GROUP).withSchedule(
						SimpleScheduleBuilder.simpleSchedule()
								.withIntervalInMilliseconds(
										timeExpressionConverter
												.getIntervalMilli())
								.withRepeatCount(repeatCount))
						.modifiedByCalendar(jobName + CALENDAR_SUFFIX).startAt(
								event.getStartTime() == null ? new Date()
										: event.getStartTime()).endAt(
								event.getEndTime() != null ? event.getEndTime()
										: null).build();
			} else {
				trigger = TriggerBuilder.newTrigger().withIdentity(
						jobName + TRIGGER_SUFFIX,
						SchedulerConstants.DEFAULT_GROUP).withSchedule(
						SimpleScheduleBuilder.simpleSchedule()
								.withIntervalInMilliseconds(
										timeExpressionConverter
												.getIntervalMilli())
								.withRepeatCount(repeatCount)).startAt(
						event.getStartTime() == null ? new Date() : event
								.getStartTime()).endAt(
						event.getEndTime() != null ? event.getEndTime() : null)
						.build();
			}
		}
		else
 {
			if (event.getCalendar() != null
					|| timeExpressionConverter.getCalendar() != null) {
				trigger = (CronTrigger) TriggerBuilder.newTrigger()
						.withIdentity(jobName + TRIGGER_SUFFIX,
								SchedulerConstants.DEFAULT_GROUP).withSchedule(
								CronScheduleBuilder
										.cronSchedule(timeExpressionConverter
												.getCronExpression()))
						.modifiedByCalendar(jobName + CALENDAR_SUFFIX).startAt(
								event.getStartTime() == null ? new Date()
										: event.getStartTime()).endAt(
								event.getEndTime() != null ? event.getEndTime()
										: null).build();
			} else {
				trigger = (CronTrigger) TriggerBuilder.newTrigger()
						.withIdentity(jobName + TRIGGER_SUFFIX,
								SchedulerConstants.DEFAULT_GROUP).withSchedule(
								CronScheduleBuilder
										.cronSchedule(timeExpressionConverter
												.getCronExpression())).startAt(
								event.getStartTime() == null ? new Date()
										: event.getStartTime()).endAt(
								event.getEndTime() != null ? event.getEndTime()
										: null).build();
			}
		}

		// Add calendar if necessary
		if (event.getCalendar() != null
				|| timeExpressionConverter.getCalendar() != null)
		{
			MultiCalendar multiCalendar = new MultiCalendar();
			multiCalendar.addCalendar(timeExpressionConverter.getCalendar());

			if (event.getCalendar() != null)
			{
				multiCalendar.addCalendar(event.getCalendar()
						.getCalendarInterval().getMultiCalendar());
			}

			scheduler.addCalendar(jobName + CALENDAR_SUFFIX, multiCalendar,
					true, true);
		}

		scheduler.scheduleJob(jobDetail, trigger);

		return jobName;
	}

	private void _shutdown() throws SystemShutdownException
	{
		if (scheduler != null)
		{
			stopQuartz();
		}
	}

	private void _startup() throws SystemStartupException
	{
		if (scheduler == null)
		{
			startQuartz();
		}
	}

	/**
     * Unschedule the job which the specific job name.
     * 
     * @param jobName
     * @return
     * @throws Exception
     */
	private boolean _unschedule(String jobName) throws Exception
	{
		return scheduler.deleteJob(new JobKey(jobName,
				SchedulerConstants.DEFAULT_GROUP))
				&& scheduler.deleteCalendar(jobName + CALENDAR_SUFFIX);
	}

	private void startQuartz() throws SystemStartupException
	{
		try
		{
			startServer();
			statusMessage("started");
			scheduler.start();
		}
		catch (Exception e)
		{
			throw new SystemStartupException(
					SystemShutdownException.EX_GENERAL, e);
		}
	}

	/**
     * Start Quartz server.
     * 
     * @throws Exception
     */
	private void startServer() throws Exception
	{
		SchedulerFactory schedulerFactory = new StdSchedulerFactory(
				"quartz.properties");
		scheduler = schedulerFactory.getScheduler();
	}

	/**
     * Print Quartz server status message.
     * 
     * @param message
     */
	private void statusMessage(String message)
	{
		s_logger.info("Quartz server " + message + " successfully.");
	}

	/**
     * Stop Quartz server.
     * 
     * @throws SystemShutdownException
     */
	private void stopQuartz() throws SystemShutdownException
	{
		try
		{
			scheduler.shutdown(true);
			scheduler = null;
			statusMessage("stopped");
		}
		catch (Exception e)
		{
			throw new SystemShutdownException(
					SystemShutdownException.EX_GENERAL, e);
		}
	}

	/**
     * Gets the index for the unique name.
     * 
     * @return The index value.
     */
	private int getIndex()
	{
		if (index > MAX_INDEX)
		{
			index = 0;
		}
		return index++;
	}
}
