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
package com.globalsight.cxe.util.scheduling;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.calendar.CalendarManager;
import com.globalsight.calendar.FluxCalendar;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.scheduling.EventInfo;
import com.globalsight.scheduling.EventSchedulerHelper;
import com.globalsight.scheduling.FluxEventMap;
import com.globalsight.scheduling.Quartz;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.scheduling.UnscheduledEvent;
import com.globalsight.util.date.DateHelper;

/**
 * Class {@code SchedulerUtil} is used for performing the event scheduling
 * without using JMS.
 * 
 * @since GBS-4400
 */
public class SchedulerUtil
{
    static private final Logger logger = Logger.getLogger(SchedulerUtil.class);

    /**
     * Performs the event scheduling asynchronously with thread instead of JMS.
     */
    static public void performSchedulingActionWithThread(Map<String, Object> data)
    {
        SchedulerRunnable runnable = new SchedulerRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Performs the event scheduling synchronously.
     */
    static public void performSchedulingAction(Map<String, Object> p_data)
    {
        Integer action = (Integer) p_data.get(SchedulerConstants.ACTION_TYPE);
        int actionType = action.intValue();
        try
        {
            switch (actionType)
            {
                default:
                    break;
                case SchedulerConstants.ACCEPT_ACTIVITY:
                    // stop the accept timer if still running (also remove from
                    // our
                    // map table)
                    unscheduleEvent(p_data);
                    // schedule completed by event (also persist in our map
                    // table)
                    scheduleEvent(p_data);
                    break;
                case SchedulerConstants.SKIP_ACTIVITY:
                    // stop all the remaining timers for the active task (remove
                    // from map table).
                    unscheduleEvent(p_data);
                    break;
                case SchedulerConstants.SKIP_FINISH_ACTIVITY:
                    // create timers for the next task (if any)
                    scheduleEvent(p_data);
                    break;
                case SchedulerConstants.CANCEL_WORKFLOW:
                    // stop all the remaining timers for the active task (remove
                    // from map table).
                    unscheduleEvent(p_data);
                    break;
                case SchedulerConstants.DISPATCH_WORKFLOW:
                    // creat accept timer and store info in our own db (job id)
                    scheduleEvent(p_data);
                    break;
                case SchedulerConstants.FINISH_ACTIVITY:
                    // stop all the remaining timers for the finished task
                    // (remove
                    // from db)
                    unscheduleEvent(p_data);
                    // create timers for the next task (if any)
                    scheduleEvent(p_data);
                    break;
                case SchedulerConstants.MODIFY_WORKFLOW:
                    // stop all the remaining timers for the modified task
                    unscheduleEvent(p_data);
                    // create new timers for that task.
                    scheduleEvent(p_data);
                    break;
                case SchedulerConstants.UNACCEPT_ACTIVITY:
                    // stop the "completed by" timer
                    unscheduleEvent(p_data);
                    // create an accept timer
                    scheduleEvent(p_data);
                    break;
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to schedule.", e);
            if (p_data != null)
            {
                try
                {
                    Map emailInfo = (Map) p_data.get(SchedulerConstants.EMAIL_INFO);
                    EventSchedulerHelper.notifyProjectManager(emailInfo,
                            SchedulerConstants.SCHEDULING_FAILED_SUBJECT,
                            SchedulerConstants.SCHEDULING_FAILED_BODY);
                }
                catch (Exception ex)
                {
                    logger.error("Failed to notify PM about scheduling failure.", e);
                }
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    private static void scheduleEvent(Map<String, Object> p_data) throws Exception
    {
        Integer eventType = (Integer) p_data.get(SchedulerConstants.SCHEDULE_EVENT_TYPE);
        if (eventType == null)
        {
            return;
        }
        BaseFluxCalendar calendar = (BaseFluxCalendar) p_data.get(SchedulerConstants.BIZ_CALENDAR);
        String companyId = (String) p_data.get(SchedulerConstants.CURRENT_COMPANY_ID);
        CalendarManager cm = ServerProxy.getCalendarManager();
        FluxCalendar defaultCalendar = cm.findDefaultCalendar(companyId);

        // threshold
        Float thresholdAsFloat = (Float) p_data.get(SchedulerConstants.WARNING_THRESHOLD);
        // creation date
        Long creationDate = (Long) p_data.get(SchedulerConstants.CREATED_DATE);
        // repeat count
        Integer repeatCount = (Integer) p_data.get(SchedulerConstants.REPEAT_COUNT);
        // listener class (i.e. EmailDispatcher, and etc.)
        Class listener = (Class) p_data.get(SchedulerConstants.LISTENER);
        // email info
        Map emailInfo = (Map) p_data.get(SchedulerConstants.EMAIL_INFO);
        // add "Accepted by" and "completed by" to the email info now
        Integer acceptType = (Integer) SchedulerConstants.s_eventTypes
                .get(SchedulerConstants.ACCEPT_TYPE);

        boolean isAcceptType = eventType.intValue() == acceptType.intValue();

        String eventTypePrefix = isAcceptType ? SchedulerConstants.ACCEPTANCE
                : SchedulerConstants.COMPLETION;
        // now using the estimated acceptance/completion date, determine the
        // warning date
        double threshold = thresholdAsFloat.doubleValue();
        long creationTime = creationDate.longValue();
        TaskInfo taskInfo = (TaskInfo) p_data.get(SchedulerConstants.TASK_INFO);
        Date deadline = isAcceptType ? taskInfo.getAcceptByDate() : taskInfo.getCompleteByDate();
        long deadlineInMs = deadline.getTime();
        long duration = deadlineInMs - creationTime; // in ms
        long warn = (long) Math.round(duration * (1 - threshold));
        Date warningDate = new Date(deadlineInMs - warn);

        // Here add two warning time, we use this two warning time deadlinePM
        // and deadlineUser
        Date expectedCompleteDate = taskInfo.getCompleteByDate();

        long deadlineNotifyPM = taskInfo.getOverdueToPM();

        if (deadlineNotifyPM <= 0)
        {
            deadlineNotifyPM = 3 * 24 * 60 * 60 * 1000;
        }

        Date deadlinePM = ServerProxy.getEventScheduler().determineDate(deadline, defaultCalendar,
                deadlineNotifyPM);

        long deadlineNotifyUser = taskInfo.getOverdueToUser();

        if (deadlineNotifyUser <= 0)
        {
            deadlineNotifyUser = 1 * 24 * 60 * 60 * 1000;
        }

        Date deadlineUser = ServerProxy.getEventScheduler().determineDate(deadline, defaultCalendar,
                deadlineNotifyUser);

        emailInfo.put(SchedulerConstants.EVENT_TYPE, eventTypePrefix);
        emailInfo.put(SchedulerConstants.DEADLINE_DATE, deadline);
        emailInfo.put(SchedulerConstants.DEADLINE_DATE_COMPLETION, expectedCompleteDate);

        // Here add two parameters to the emailInfo to show how many days be
        // overdue.
        long dhmPM[] = DateHelper.daysHoursMinutes(deadlineNotifyPM);
        long dhmUser[] = DateHelper.daysHoursMinutes(deadlineNotifyUser);

        emailInfo.put(SchedulerConstants.OVERDUE_PM, overdueString(dhmPM));
        emailInfo.put(SchedulerConstants.OVERDUE_USER, overdueString(dhmUser));

        String jobId = scheduleEvent(deadline, warningDate, repeatCount, listener, calendar,
                emailInfo, eventTypePrefix);
        emailInfo.put(SchedulerConstants.OVERDUE, Boolean.TRUE);
        String jobIdOverdue = scheduleEvent(deadlinePM, deadlineUser, repeatCount, listener,
                calendar, emailInfo, eventTypePrefix);

        // persist info in db
        persistEvent(p_data, eventType.intValue(), jobId);
        persistEvent(p_data, eventType.intValue(), jobIdOverdue);
    }

    private static String overdueString(long dhm[])
    {
        StringBuilder overdueString = new StringBuilder();
        if (dhm[0] > 0)
        {
            overdueString.append(String.valueOf(dhm[0]));
            overdueString.append(dhm[0] > 1 ? " days " : " day ");
        }
        if (dhm[1] > 0)
        {
            overdueString.append(String.valueOf(dhm[1]));
            overdueString.append(dhm[1] > 1 ? " hours " : " hour ");
        }
        if (dhm[2] > 0)
        {
            overdueString.append(String.valueOf(dhm[2]));
            overdueString.append(dhm[2] > 1 ? " minutes" : " minute");
        }
        return overdueString.toString();
    }

    private static void persistEvent(Map<String, Object> p_data, int p_eventType, String p_jobId)
    {
        try
        {
            Long objId = (Long) p_data.get(SchedulerConstants.SCHEDULE_DOMAIN_OBJ_ID);
            Integer objType = (Integer) p_data.get(SchedulerConstants.DOMAIN_OBJ_TYPE);
            FluxEventMap fem = new FluxEventMap();
            fem.setDomainObjectId(objId.longValue());
            fem.setDomainObjectType(objType.intValue());
            fem.setEventId(p_jobId);
            fem.setEventType(p_eventType);
            EventSchedulerHelper.createFluxEventMap(fem);
        }
        catch (Exception e)
        {
            logger.error("Failed to persist event with schedule job id:  " + p_jobId);
            try
            {
                Quartz.unschedule(p_jobId);
            }
            catch (Exception ex)
            {
                logger.error("Failed to unschedule event with schedule job id: " + p_jobId);
            }
        }
    }

    private static String scheduleEvent(Date deadline, Date warningDate, Integer repeatCount,
            Class listener, BaseFluxCalendar calendar, Map emailInfo, String eventTypePrefix)
            throws Exception
    {
        // now that we have both warning and deadline dates, we need to generate
        // a time expression to go from warning to the deadline date
        long diff = deadline.getTime() - warningDate.getTime();
        // Quartz need diff(repeat intrerval) greater than 0.
        if (diff <= 0)
        {
            logger.error("Modify the data, due Quartz need Repeat Interval greater than 0.");
            diff = 10000L;
        }

        // NOTE: "S" is millis and "s" is Sec
        StringBuffer sb = new StringBuffer();
        sb.append("+");
        if (diff > Integer.MAX_VALUE)
        {
            sb.append(diff / 1000l);
            sb.append("s");
        }
        else
        {
            sb.append(diff / 1000l);
            sb.append("S");
        }

        // create a new UnScheduledEvent
        UnscheduledEvent event = new UnscheduledEvent();
        event.setStartTime(warningDate);
        event.setRecurrenceRule(sb.toString());
        event.setRepeatCount(repeatCount.intValue());
        event.setEventHandlerClass(listener);
        event.setCalendar(calendar);
        event.setEventInfo(new EventInfo(emailInfo));

        return Quartz.schedule(event);
    }

    private static void unscheduleEvent(Map<String, Object> p_data) throws Exception
    {
        // based on domainObjId, domainObjType, and/or event type get JobId
        Long objId = (Long) p_data.get(SchedulerConstants.UNSCHEDULE_DOMAIN_OBJ_ID);
        Integer objType = (Integer) p_data.get(SchedulerConstants.DOMAIN_OBJ_TYPE);
        Integer eventType = (Integer) p_data.get(SchedulerConstants.UNSCHEDULE_EVENT_TYPE);

        // unschedule all the timers for this task.
        unscheduleEvent(objId, objType);
        if (eventType == null)
        {
            unscheduleEvent(objId, objType);
        }
        else
        {
            FluxEventMap fem = EventSchedulerHelper.findFluxEventMap(eventType, objType, objId);
            // unschedule job
            if (fem != null)
            {
                unschedule(fem);
            }
        }
    }

    private static void unscheduleEvent(Long p_objectId, Integer p_objectType) throws Exception
    {
        Iterator iterator = EventSchedulerHelper.findFluxEventMaps(p_objectType, p_objectId);
        while (iterator.hasNext())
        {
            FluxEventMap fem = (FluxEventMap) iterator.next();
            unschedule(fem);
        }
    }

    private static void unschedule(FluxEventMap p_fluxEventMap) throws Exception
    {
        // unschedule event
        Quartz.unschedule(p_fluxEventMap.getEventId());
        // now remove it from our own db
        EventSchedulerHelper.removeFluxEventMap(p_fluxEventMap);
    }
}
