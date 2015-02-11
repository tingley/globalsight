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
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.calendar.CalendarManager;
import com.globalsight.calendar.FluxCalendar;
import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.date.DateHelper;

/**
 * This MessageDrivenBean is responsible for performing the event scheduling as
 * an asynchronous process.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_SCHEDULING_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class SchedulerMDB extends GenericQueueMDB
{
    // If today is a holiday or a weekend day, then go forward to
    // the first weekday that is not also a holiday.
    private static final String s_expression = ">b";

    private static final Logger s_logger = Logger.getLogger(SchedulerMDB.class
            .getName());

    /**
     * SchedulerMDB constructor.
     */
    public SchedulerMDB()
    {
        super(s_logger);
    }

    /**
     * Start the scheduling related process as a separate thread. This method is
     * not a public API and is ONLY invoked by it's consumer for scheduling
     * purposes (i.e. scheduling/unscheduling an event). Upon an exception, a
     * notification will be sent to the Project Manager.
     * 
     * @param p_message
     *            - The message to be passed.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_message)
    {
        HashMap map = null;
        try
        {
            if (p_message.getJMSRedelivered())
            {
                s_logger.warn("Ignoring duplicate JMS message.");
                return;
            }
            // get the hashtable that contains all info need for scheduling
            map = (HashMap) ((ObjectMessage) p_message).getObject();

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) map.get(CompanyWrapper.CURRENT_COMPANY_ID));

            performSchedulingAction(map);
        }
        catch (Exception e)
        {
            s_logger.error("SchedulerMDB failed to schedule.", e);
            if (map != null)
            {
                try
                {
                    HashMap emailInfo = (HashMap) map
                            .get(SchedulerConstants.EMAIL_INFO);
                    EventSchedulerHelper.notifyProjectManager(emailInfo,
                            SchedulerConstants.SCHEDULING_FAILED_SUBJECT,
                            SchedulerConstants.SCHEDULING_FAILED_BODY);
                }
                catch (Exception ex)
                {
                    s_logger.error(
                            "Failed to notify PM about scheduling failure.", e);
                }
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    // TomyD -- each of these actions will have a different set of info stored
    // in the HashMap.
    // Part of this info is stored within WorkflowServerLocal class since it's
    // the one that calls
    // the EventSchedulerLocal for performing the action (i.e. email info,
    // accept/complete duration,
    // %threshold, and so on).
    private void performSchedulingAction(HashMap p_map) throws Exception
    {
        Integer action = (Integer) p_map.get(SchedulerConstants.ACTION_TYPE);
        int actionType = action.intValue();
        switch (actionType)
        {
            default:
                break;
            case SchedulerConstants.ACCEPT_ACTIVITY:
                // stop the accept timer if still running (also remove from our
                // map table)
                unscheduleEvent(p_map);
                // schedule completed by event (also persist in our map table)
                scheduleEvent(p_map);
                break;
            case SchedulerConstants.SKIP_ACTIVITY:
                // stop all the remaining timers for the active task (remove
                // from map table).
                unscheduleEvent(p_map);
                break;
            case SchedulerConstants.SKIP_FINISH_ACTIVITY:
                // create timers for the next task (if any)
                scheduleEvent(p_map);
                break;
            case SchedulerConstants.CANCEL_WORKFLOW:
                // stop all the remaining timers for the active task (remove
                // from map table).
                unscheduleEvent(p_map);
                break;
            case SchedulerConstants.DISPATCH_WORKFLOW:
                // creat accept timer and store info in our own db (job id)
                scheduleEvent(p_map);
                break;
            case SchedulerConstants.FINISH_ACTIVITY:
                // stop all the remaining timers for the finished task (remove
                // from db)
                unscheduleEvent(p_map);
                // create timers for the next task (if any)
                scheduleEvent(p_map);
                break;
            case SchedulerConstants.MODIFY_WORKFLOW: // activate task (modify
                                                     // wf)
                // stop all the remaining timers for the modified task
                unscheduleEvent(p_map);
                // create new timers for that task.
                scheduleEvent(p_map);
                break;
            case SchedulerConstants.UNACCEPT_ACTIVITY: // reject task
                // stop the "completed by" timer
                unscheduleEvent(p_map);
                // create an accept timer
                scheduleEvent(p_map);
                break;
        }
    }

    // unschedule event by first removing it from schedule queue and then from
    // our flux_gs_map table.
    private void unscheduleEvent(HashMap p_map) throws Exception
    {
        // based on domainObjId, domainObjType, and/or event type get JobId
        Long objId = (Long) p_map
                .get(SchedulerConstants.UNSCHEDULE_DOMAIN_OBJ_ID);
        Integer objType = (Integer) p_map
                .get(SchedulerConstants.DOMAIN_OBJ_TYPE);
        Integer eventType = (Integer) p_map
                .get(SchedulerConstants.UNSCHEDULE_EVENT_TYPE);

        // unschedule all the timers for this task.
        unscheduleEvent(objId, objType);
        if (eventType == null)
        {
            unscheduleEvent(objId, objType);
        }
        else
        {
            FluxEventMap fem = EventSchedulerHelper.findFluxEventMap(eventType,
                    objType, objId);
            // unschedule job
            if (fem != null)
            {
                unschedule(fem);
            }
        }
    }

    // unschedule events by first removing them from schedule queue and then
    // from
    // our flux_gs_map table. This method is invoked during workflow
    // cancellation
    // as the state of a work item is not known (it's more expensive to find a
    // work item
    // based on the node instance id).
    private void unscheduleEvent(Long p_objectId, Integer p_objectType)
            throws Exception
    {

        Iterator iterator = EventSchedulerHelper.findFluxEventMaps(
                p_objectType, p_objectId);
        while (iterator.hasNext())
        {
            FluxEventMap fem = (FluxEventMap) iterator.next();
            unschedule(fem);
        }
    }

    // info for scheduling a job
    private void scheduleEvent(HashMap p_map) throws Exception
    {
        // get schedule event type (whether it's for scheduling accept or
        // complete event)
        Integer eventType = (Integer) p_map
                .get(SchedulerConstants.SCHEDULE_EVENT_TYPE);

        // it's possible to get here when an activity has been finished. If
        // there's
        // no more activity after it, no scheduling is required.
        if (eventType == null)
        {
            return;
        }

        // biz calendar
        BaseFluxCalendar calendar = (BaseFluxCalendar) p_map
                .get(SchedulerConstants.BIZ_CALENDAR);

        // This base calendar is used for the activity due issue, this calendar
        // is the
        // default calendar of GlobalSight.
        String companyId = (String) p_map
                .get(SchedulerConstants.CURRENT_COMPANY_ID);
        CalendarManager cm = ServerProxy.getCalendarManager();
        FluxCalendar defaultCalendar = cm.findDefaultCalendar(companyId);
        // end of the base calendar.

        // threshold
        Float thresholdAsFloat = (Float) p_map
                .get(SchedulerConstants.WARNING_THRESHOLD);
        // creation date
        Long creationDate = (Long) p_map.get(SchedulerConstants.CREATED_DATE);
        // repeat count
        Integer repeatCount = (Integer) p_map
                .get(SchedulerConstants.REPEAT_COUNT);
        // listener class (i.e. EmailDispatcher, and etc.)
        Class listener = (Class) p_map.get(SchedulerConstants.LISTENER);
        // email info
        HashMap emailInfo = (HashMap) p_map.get(SchedulerConstants.EMAIL_INFO);
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
        TaskInfo taskInfo = (TaskInfo) p_map.get(SchedulerConstants.TASK_INFO);
        Date deadline = isAcceptType ? taskInfo.getAcceptByDate() : taskInfo
                .getCompleteByDate();
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

        Date deadlinePM = ServerProxy.getEventScheduler().determineDate(
                deadline, defaultCalendar, deadlineNotifyPM);

        long deadlineNotifyUser = taskInfo.getOverdueToUser();

        if (deadlineNotifyUser <= 0)
        {
            deadlineNotifyUser = 1 * 24 * 60 * 60 * 1000;
        }

        Date deadlineUser = ServerProxy.getEventScheduler().determineDate(
                deadline, defaultCalendar, deadlineNotifyUser);

        emailInfo.put(SchedulerConstants.EVENT_TYPE, eventTypePrefix);
        emailInfo.put(SchedulerConstants.DEADLINE_DATE, deadline);
        emailInfo.put(SchedulerConstants.DEADLINE_DATE_COMPLETION,
                expectedCompleteDate);

        // Here add two parameters to the emailInfo to show how many days be
        // overdue.
        long dhmPM[] = DateHelper.daysHoursMinutes(deadlineNotifyPM);
        long dhmUser[] = DateHelper.daysHoursMinutes(deadlineNotifyUser);

        emailInfo.put(SchedulerConstants.OVERDUE_PM, overdueString(dhmPM));
        emailInfo.put(SchedulerConstants.OVERDUE_USER, overdueString(dhmUser));

        String jobId = scheduleEvent(deadline, warningDate, repeatCount,
                listener, calendar, emailInfo, eventTypePrefix);
        emailInfo.put(SchedulerConstants.OVERDUE, Boolean.TRUE);
        String jobIdOverdue = scheduleEvent(deadlinePM, deadlineUser,
                repeatCount, listener, calendar, emailInfo, eventTypePrefix);

        // persist info in db
        persistEvent(p_map, eventType.intValue(), jobId);
        persistEvent(p_map, eventType.intValue(), jobIdOverdue);
    }

    private String overdueString(long dhm[])
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

    // save event info in database. This info will be used for stopping or
    // updating an existing event.
    private void persistEvent(HashMap p_map, int p_eventType, String p_jobId)
            throws Exception
    {
        try
        {
            Long objId = (Long) p_map
                    .get(SchedulerConstants.SCHEDULE_DOMAIN_OBJ_ID);
            Integer objType = (Integer) p_map
                    .get(SchedulerConstants.DOMAIN_OBJ_TYPE);
            FluxEventMap fem = new FluxEventMap();
            fem.setDomainObjectId(objId.longValue());
            fem.setDomainObjectType(objType.intValue());
            fem.setEventId(p_jobId);
            fem.setEventType(p_eventType);
            EventSchedulerHelper.createFluxEventMap(fem);
        }
        catch (Exception e)
        {
            s_logger.error("SchedulerMDB :: Failed to persist event "
                    + "with a schedule job id of:  " + p_jobId);
            try
            {
                // try to stop the job. if it fails, log the error
                Quartz.unschedule(p_jobId);
            }
            catch (Exception ex)
            {
                s_logger.error("SchedulerMDB :: Failed to "
                        + "unschedule event with id: " + p_jobId);
            }
        }
    }

    // Schedule the event in Quartz
    private String scheduleEvent(Date deadline, Date warningDate,
            Integer repeatCount, Class listener, BaseFluxCalendar calendar,
            HashMap emailInfo, String eventTypePrefix) throws Exception
    {
        // now that we have both warning and deadline dates, we need to generate
        // a time expression to go from warning to the deadline date
        long diff = deadline.getTime() - warningDate.getTime();
        // Quartz need diff(repeat intrerval) greater than 0.
        if (diff <= 0)
        {
            s_logger.error("Modify the data, due Quartz need Repeat Interval greater than 0.");
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

    // unschedule an event and remove it from db
    private void unschedule(FluxEventMap p_fluxEventMap) throws Exception
    {
        // unschedule event
        Quartz.unschedule(p_fluxEventMap.getEventId());
        // now remove it from our own db
        EventSchedulerHelper.removeFluxEventMap(p_fluxEventMap);
    }
}
