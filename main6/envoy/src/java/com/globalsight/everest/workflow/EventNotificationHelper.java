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
package com.globalsight.everest.workflow;

// java
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ProcessInstance;

import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.scheduling.ActivityEmailDispatcher;
import com.globalsight.scheduling.SchedulerConstants;

/**
 * This class provides package level static methods for Workflow component for
 * scheduling/unscheduling events. Note that all exceptions will only be logged
 * and not returned to Workflow.
 */

public class EventNotificationHelper
{
    // PRIVATE STATIC VARIABLES
    private static final Logger s_logger = Logger
            .getLogger(EventNotificationHelper.class.getName());

    private static final int INVALID_ID = -1;

    private static Boolean m_systemNotificationEnabled = null;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Event notification support
    // ////////////////////////////////////////////////////////////////////

    // This method will determine the update of any possible events (stop and
    // start)
    // after a workflow modification. If either of the durations
    // (accept/complete)
    // have been modified, we might need to unschedule and schedule an event. It
    // all
    // depends on which duration has been modified.
    /*
     * static void determineEventUpdate(Map p_durationMap, TaskEmailInfo
     * p_emailInfo, boolean p_isNotificationActive, Float p_threshold) { // map
     * content: activity, possibly accept or/and complete change flag. if
     * (p_durationMap.size() > 1 && p_isNotificationActive) { //TomyD --- how
     * should we get the right date? getCurrentTime is not valid.. NodeInstance
     * node = (NodeInstance)p_durationMap.get(SchedulerConstants .ACTIVE_NODE);
     * 
     * performSchedulingProcess(new Integer(SchedulerConstants
     * .MODIFY_WORKFLOW), node.getNodeInstanceId(), null, node,
     * getCurrentTime(), null, p_threshold, p_emailInfo, p_durationMap); } }
     */

    // get the current time in milliseconds (as a Long object)
    static Long getCurrentTime()
    {
        return new Long(System.currentTimeMillis());
    }

    @SuppressWarnings("unchecked")
    static Map durationChangeMap(WorkflowTaskInstance p_workflowTaskInstance,
            long p_originalAcceptDuration, long p_originalCompleteDuration)
    {
        long currentAcceptDuration = p_workflowTaskInstance.getAcceptTime();
        long currentCompleteDuration = p_workflowTaskInstance
                .getCompletedTime();

        Map map = new HashMap(3);
        map.put(SchedulerConstants.ACTIVE_NODE, p_workflowTaskInstance);
        // NOTE: need to only populate map if the result is TRUE.
        if (currentAcceptDuration != p_originalAcceptDuration)
        {
            map.put(SchedulerConstants.ACCEPT_CHANGED, Boolean.TRUE);
        }
        if (currentCompleteDuration != p_originalCompleteDuration)
        {
            map.put(SchedulerConstants.COMPLETE_CHANGED, Boolean.TRUE);
        }
        return map;
    }

    // Perform create/stop notification timer based on the info within the
    // HashMap.
    /**
     * @param p_actionType
     * @param p_unsheduleTaskId
     * @param p_unScheduleEventType
     * @param p_task
     * @param p_taskInfo
     * @param p_creationTime
     * @param p_scheduleEventType
     * @param p_threshold
     * @param p_emailInfo
     */
    public static void performSchedulingProcess(Integer p_actionType,
            long p_unsheduleTaskId, Integer p_unScheduleEventType, Node p_node,
            TaskInfo p_taskInfo, Long p_creationTime,
            Integer p_scheduleEventType, Float p_threshold,
            TaskEmailInfo p_emailInfo)
    {

        performSchedulingProcess(p_actionType, p_unsheduleTaskId,
                p_unScheduleEventType, p_node, p_taskInfo, p_creationTime,
                p_scheduleEventType, p_threshold, p_emailInfo, null);
    }

    // Perform create/stop notification timer based on the info within the
    // HashMap.
    /**
     * @param p_actionType
     * @param p_unsheduleTaskId
     * @param p_unScheduleEventType
     * @param p_task
     * @param p_taskInfo
     * @param p_creationTime
     * @param p_scheduleEventType
     * @param p_threshold
     * @param p_emailInfo
     * @param p_additionalInfo
     */
    @SuppressWarnings("unchecked")
    static void performSchedulingProcess(Integer p_actionType,
            long p_unsheduleTaskId, Integer p_unScheduleEventType, Node p_node,
            TaskInfo p_taskInfo, Long p_creationTime,
            Integer p_scheduleEventType, Float p_threshold,
            TaskEmailInfo p_emailInfo, Map p_additionalInfo)
    {
        try
        {
            // first get common hash map
            HashMap map = createSchedulingMap(p_actionType);
            if (p_unsheduleTaskId != INVALID_ID)
            {
                unschedulingNotificationInfo(map, new Long(p_unsheduleTaskId),
                        p_unScheduleEventType);
            }

            // for scheduling, make sure the task is valid. It's possible to
            // have a
            // null task if the workflow has been finished.
            if (p_scheduleEventType != null && p_node != null)
            {
                schedulingNotificationInfo(map, p_node, p_taskInfo,
                        p_creationTime, p_scheduleEventType, p_threshold,
                        p_emailInfo);
            }

            long projectId = p_emailInfo.getProjectIdAsLong().longValue();
            Project project = ServerProxy.getProjectHandler().getProjectById(
                    projectId);
            map.put(SchedulerConstants.CURRENT_COMPANY_ID,
                    String.valueOf(project.getCompanyId()));

            if (p_additionalInfo != null)
            {
                map.putAll(p_additionalInfo);
            }
            // now perform action
            ServerProxy.getEventScheduler().performSchedulingProcess(map);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to perform scheduling process.", e);
        }
    }

    // scheduling process for a dispatch, and a modification of a completed
    // workflow (after an export). Both of these have the same scheduling
    // process.
    /**
     * @param p_activeNode
     * @param p_taskInfo
     * @param p_emailInfo
     * @param p_isNotificationActive
     * @param p_threshold
     */
    static void scheduleNotificationForDispatch(Node p_activeNode,
            TaskInfo p_taskInfo, TaskEmailInfo p_emailInfo,
            boolean p_isNotificationActive, Float p_threshold)
    {
        if (p_isNotificationActive)
        {
            performSchedulingProcess(new Integer(
                    SchedulerConstants.DISPATCH_WORKFLOW), INVALID_ID, null,
                    p_activeNode, p_taskInfo, getCurrentTime(),
                    (Integer) SchedulerConstants.s_eventTypes
                            .get(SchedulerConstants.ACCEPT_TYPE), p_threshold,
                    p_emailInfo);
        }
    }

    // determines whether the system-wide notification is enabled
    /**
     * Determine whether the sytem-wide notification is enabled
     */
    public static boolean systemNotificationEnabled()
    {
        try
        {
            if (m_systemNotificationEnabled == null)
            {
                SystemConfiguration config = SystemConfiguration.getInstance();
                m_systemNotificationEnabled = new Boolean(
                        config.getStringParameter(SystemConfiguration.SYSTEM_NOTIFICATION_ENABLED));
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get system-wide notification flag. ", e);
            m_systemNotificationEnabled = Boolean.TRUE;
        }

        return m_systemNotificationEnabled.booleanValue();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Event notification support
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Methods
    // ////////////////////////////////////////////////////////////////////

    // this is the first method that should be called which will return the
    // common HashMap used for both creating and stopping a timer.
    @SuppressWarnings("unchecked")
    private static HashMap createSchedulingMap(Integer p_actionType)
    {
        HashMap map = new HashMap(16);
        map.put(SchedulerConstants.ACTION_TYPE, p_actionType);
        map.put(SchedulerConstants.DOMAIN_OBJ_TYPE,
                (Integer) SchedulerConstants.s_objectTypes
                        .get(WorkflowTask.class));

        return map;
    }

    //
    @SuppressWarnings("unchecked")
    private static HashMap emailInfo(TaskEmailInfo p_emailInfo,
            String p_activityName)
    {
        // email info
        HashMap emailMap = new HashMap(5);
        emailMap.put(SchedulerConstants.PROJECT_ID,
                p_emailInfo.getProjectIdAsLong());
        emailMap.put(SchedulerConstants.WF_ID, p_emailInfo.getWfIdAsLong());
        emailMap.put(SchedulerConstants.JOB_NAME, p_emailInfo.getJobName());
        emailMap.put(SchedulerConstants.ACTIVITY_NAME, p_activityName);
        emailMap.put(SchedulerConstants.SOURCE_LOCALE,
                p_emailInfo.getSourceLocale());
        emailMap.put(SchedulerConstants.TARGET_LOCALE,
                p_emailInfo.getTargetLocale());

        // Put the assignees' names to the emailMap, for the overdue issue
        emailMap.put(SchedulerConstants.ASSIGNEES_NAME,
                p_emailInfo.getAssigneesName());

        return emailMap;
    }

    @SuppressWarnings("unchecked")
    private static HashMap schedulingNotificationInfo(HashMap p_map,
            Node p_node, TaskInfo p_taskInfo, Long p_creationTime,
            Integer p_eventType, Float p_threshold, TaskEmailInfo p_emailInfo)
            throws Exception
    {
        // TomyD -- we might not need this...
        BaseFluxCalendar calendar = null;
        if (calendar != null)
        {
            p_map.put(SchedulerConstants.BIZ_CALENDAR, calendar);
        }

        long jobId = Long.valueOf(p_emailInfo.getJobId());
        Job job = ServerProxy.getJobHandler().getJobById(jobId);
        String companyIdStr = String.valueOf(job.getCompanyId());

        // Task info containing the estimated acceptance/completion dates.
        p_map.put(SchedulerConstants.TASK_INFO, p_taskInfo);

        // event type (accept, complete, ...)
        p_map.put(SchedulerConstants.SCHEDULE_EVENT_TYPE, p_eventType);
        // duration based on the event type (either AcceptTime or CompleteTime
        // in ms)
        /*
         * p_map.put(SchedulerConstants.DURATION, duration(p_eventType,
         * p_task));
         */
        // threshold
        p_map.put(SchedulerConstants.WARNING_THRESHOLD, p_threshold);
        // creation date
        p_map.put(SchedulerConstants.CREATED_DATE, p_creationTime);
        // repeat count (only once, for warning a deadline)
        p_map.put(SchedulerConstants.REPEAT_COUNT, new Integer(1));
        // listener class (i.e. EmailDispatcher, and etc.)
        p_map.put(SchedulerConstants.LISTENER, ActivityEmailDispatcher.class);
        // email information map
        long wfId = p_emailInfo.getWfIdAsLong();
        ProcessInstance pi = WorkflowConfiguration.getInstance()
                .getCurrentContext().getProcessInstance(wfId);
        String activityName = WorkflowJbpmUtil.getActivityNameWithArrowName(
                p_node, "_" + companyIdStr, pi, "");
        p_map.put(SchedulerConstants.EMAIL_INFO,
                emailInfo(p_emailInfo, activityName));
        // the id of the object for scheduling
        p_map.put(SchedulerConstants.SCHEDULE_DOMAIN_OBJ_ID,
                new Long(p_node.getId()));
        return p_map;
    }

    // populate the scheduling map with information required for stopping
    // an event notification.
    @SuppressWarnings("unchecked")
    private static HashMap unschedulingNotificationInfo(HashMap p_map,
            Long p_workflowTaskInstanceId, Integer p_eventType)
    {
        // accept, complete or etc. The event type can be null during workflow
        // cancellation. We don't want to go thru all work items in order to
        // find the work item for a given node instance as it'll be a very
        // expensive
        // process. Therefore, event type which is based on the work item state
        // is
        // not required all the time.
        if (p_eventType != null)
        {
            p_map.put(SchedulerConstants.UNSCHEDULE_EVENT_TYPE, p_eventType);
        }
        p_map.put(SchedulerConstants.UNSCHEDULE_DOMAIN_OBJ_ID,
                p_workflowTaskInstanceId);

        return p_map;
    }
}
