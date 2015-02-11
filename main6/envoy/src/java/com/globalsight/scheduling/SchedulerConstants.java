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

import java.util.HashMap;

import com.globalsight.calendar.ReservedTime;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.request.reimport.DelayedImportRequest;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.terminology.scheduler.TermbaseReindexExpression;

/**
 * String constants used in scheduling.
 */
public class SchedulerConstants
{
    // Constants for Quartz
    /**
     * Special Job data parameter name.
     */
    public static final String KEY_PARAM = "key_param";

    /**
     * Default group name for Quartz Job and triggers.
     */
    public static final String DEFAULT_GROUP = "globalsight";

    public static final String CALENDAR_TYPE_HOLIDAY = "holiday";
    public static final String CALENDAR_TYPE_WORKINGHOUR = "working_hour";
    public static final String CALENDAR_TYPE_RESERVEDTIME = "reserved_time";
    public static final String CALENDAR_TYPE_PERSONALRESERVED = "personal_reserved";
    public static final String CALENDAR_TYPE_OTHER = "other_calendar";

    // ////////////////////////////////////////////////////////////////////
    // Begin: Event Types
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constant representing an Accept event type. This type is used for
     * activation (creation) or deactivation of an event based on a
     * "Accepted by" date.
     */
    public static final String ACCEPT_TYPE = WorkflowConstants.ACCEPT;

    /**
     * Constant representing a Complete event type. This type is used for
     * activation (creation) or deactivation of an event based on a
     * "Completed by" date.
     */
    public static final String COMPLETE_TYPE = WorkflowConstants.COMPLETE;

    /**
     * Constant for ReservedTime
     */
    public static final String RESERVED_TIME_TYPE = "ReservedTime";

    /**
     * The event type for export source
     */
    public static final String EXPORT_SOURCE_TYPE = "ExportSource";

    /**
     * The event type for delayed reimport
     */
    public static final String DELAYED_REIMPORT_TYPE = "DelayedReimport";

    /**
     * The event type for timed job dispatch
     */
    public static final String TIMED_JOB_DISPATCH_TYPE = "TimedJobDispatch";

    /**
     * The event type to reindex a termbase.
     */
    public static final String REINDEX_TERMBASE_TYPE = "reindex-termbase";

    // ////////////////////////////////////////////////////////////////////
    // End: Event Types
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Action Types
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constant representing a "skip" action type (i.e. skipping an activity).
     */
    public static final int SKIP_ACTIVITY = 0;

    /**
     * Constant representing an "accept" action type (i.e. accepting an
     * activity).
     */
    public static final int ACCEPT_ACTIVITY = 1;

    /**
     * Constant representing a "cancel" action type (i.e. cancelling workflow).
     */
    public static final int CANCEL_WORKFLOW = 2;

    /**
     * Constant representing a dispatch action type (i.e. workflow dispatch).
     */
    public static final int DISPATCH_WORKFLOW = 3;

    /**
     * Constant representing a "finish" action type (i.e. finishing an
     * activity).
     */
    public static final int FINISH_ACTIVITY = 4;

    /**
     * Constant representing a "modify" action type (i.e. modify workflow).
     */
    public static final int MODIFY_WORKFLOW = 5;

    /**
     * Constant representing an "un-accept/reject" action type (i.e.
     * unaccepting/rejecting an "already accepted" activity).
     */
    public static final int UNACCEPT_ACTIVITY = 6;

    /**
     * Constant representing a "skip" action type (i.e. skipping the last
     * activity).
     */
    public static final int SKIP_FINISH_ACTIVITY = 7;
    // ////////////////////////////////////////////////////////////////////
    // End: Action Types
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Variable Manager Keys associated with FlowChart (Job)
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constant used as a key for toggling between events occuring more than
     * once.
     */
    static final String EVENT_KEY = "EVENT_KEY";

    /**
     * Constant used as a key for the period of time (in days) used for
     * calendaring clean-up process.
     */
    public static final String NUM_OF_DAYS = "num_of_days";

    /**
     * Constant used as a key for the start time (in hours) used for calendaring
     * clean-up process.
     */
    public static final String START_TIME = "startTime";

    /**
     * Constant used as a key for the recurrance expression used for calendaring
     * clean-up process.
     */
    public static final String RECURRANCE = "recurrance";
    // ////////////////////////////////////////////////////////////////////
    // End: Variable Manager Keys associated with FlowChart (Job)
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: HashMap's values for scheduling/unscheduling
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constant used as a value for an "Acceptance" event type. This value is
     * basically used as a prefix for email's subject and message body key.
     */
    public static final String ACCEPTANCE = "acceptance";

    /**
     * Constant used as a value for an "Completion" event type. This value is
     * basically used as a prefix for email's subject and message body key.
     */
    public static final String COMPLETION = "completion";

    /**
     * Constant used as a flag key for checking a possible update of an
     * "accept by" duration.
     */
    public static final String ACCEPT_CHANGED = "isAcceptChanged";

    /**
     * Constant used as a flag key for checking a possible update of a
     * "complete by" duration
     */
    public static final String COMPLETE_CHANGED = "isCompleteChanged";

    /**
     * Constant used as a key for an active node.
     */
    public static final String ACTIVE_NODE = "activeNode";

    /**
     * Constant used as a key for a TaskInfo object.
     */
    public static final String TASK_INFO = "taskInfo";
    // ////////////////////////////////////////////////////////////////////
    // End: HashMap's values for scheduling/unscheduling
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: HashMap's key for scheduling/unscheduling
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constant used as a key which represents an action type (i.e. dispatch,
     * accept, and etc.)
     */
    public static final String ACTION_TYPE = "actionType";

    /**
     * Constant used as a key for the business calendar
     */
    public static final String BIZ_CALENDAR = "bizCalendar";

    /**
     * Constant used as a key for the creation date (i.e. activity's creation)
     */
    public static final String CREATED_DATE = "createdDate";

    /**
     * Constant used as a key for the deadline date (i.e. acceptance or
     * completion)
     */
    public static final String DEADLINE_DATE = "deadlineDate";

    // Adds five parameters for the over due issue
    /**
     * Constant used as a key for the deadline completion date.
     */
    public static final String DEADLINE_DATE_COMPLETION = "deadlineDateCompletion";

    /**
     * Constant used as a key for the days that the task overdue to notify pm.
     */
    public static final String OVERDUE_PM = "overduePm";

    /**
     * Constant used as a key for the days that the task overdue to notify user.
     */
    public static final String OVERDUE_USER = "overdueUser";

    /**
     * Constant used as a key for the task's assignees' names.
     */
    public static final String ASSIGNEES_NAME = "assigneesName";

    /**
     * Constant used as a key for the flag for the overdue email.
     */
    public static final String OVERDUE = "overdue";
    // Ends the overdue issue

    /**
     * Constant used as a key for the domain object's type.
     */
    public static final String DOMAIN_OBJ_TYPE = "domainObjType";

    /**
     * Constant used as a key for an "Accept by" or "Complete by" duration.
     */
    public static final String DURATION = "duration";

    /**
     * Constant used as a key for the email notification info.
     */
    public static final String EMAIL_INFO = "emailInfo";

    /**
     * Constant used as a key for an "Acceptance" or "Completion" event type.
     */
    public static final String EVENT_TYPE = "eventType";

    /**
     * Constant used as a key for an event listener for scheduling.
     */
    public static final String LISTENER = "listener";

    /**
     * Constant used as a key for timer's repeat count.
     */
    public static final String REPEAT_COUNT = "repeatCount";

    /**
     * Constant used as a key for the domain object's id (for scheduling an
     * event).
     */
    public static final String SCHEDULE_DOMAIN_OBJ_ID = "scheduleDomainObjId";

    /**
     * Constant used as a key for the scheduling event type (i.e. accept or
     * complete).
     */
    public static final String SCHEDULE_EVENT_TYPE = "scheduleEventType";

    /**
     * Constant used as a key for the domain object's id (for unscheduling an
     * event).
     */
    public static final String UNSCHEDULE_DOMAIN_OBJ_ID = "unscheduleDomainObjId";

    /**
     * Constant used as a key for the unscheduling event type (i.e. accept or
     * complete).
     */
    public static final String UNSCHEDULE_EVENT_TYPE = "unScheduleEventType";

    /**
     * Constant used as a key for the warning threshold.
     */
    public static final String WARNING_THRESHOLD = "warningThreshold";
    // ////////////////////////////////////////////////////////////////////
    // End: HashMap's key for scheduling/unscheduling
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Email Info keys
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constant used as a key for the project id.
     */
    public static final String PROJECT_ID = "projectID";

    /**
     * Constant used as a key for the Workflow id.
     */
    public static final String WF_ID = "wfID";

    /**
     * Constant used as a key for the job name.
     */
    public static final String JOB_NAME = "jobName";

    /**
     * Constant used as a key for the activity name.
     */
    public static final String ACTIVITY_NAME = "activityName";

    /**
     * Constant used as a key for the source locale.
     */
    public static final String SOURCE_LOCALE = "sourceLocale";

    /**
     * Constant used as a key for the target locale.
     */
    public static final String TARGET_LOCALE = "targetLocale";

    /**
     * Constant used as a suffix for the "deadline approaching" email subject's
     * key.
     */
    public static final String DEADLINE_APPROACH_SUBJECT = "DeadlineApproachingSubject";

    /**
     * Constant used as a suffix for the "deadline passed" email subject's key.
     */
    public static final String DEADLINE_PASSED_SUBJECT = "DeadlinePassedSubject";

    /**
     * Constant used as a suffix for the "deadline approaching" email
     * message-body's key.
     */
    public static final String DEADLINE_APPROACH_BODY = "DeadlineApproachingBody";

    /**
     * Constant used as a suffix for the "deadline passed" email message-body's
     * key.
     */
    public static final String DEADLINE_PASSED_BODY = "DeadlinePassedBody";

    // From here is for the over due issue
    /**
     * Constant used as a suffix for the "deadline approaching" email subject's
     * key.
     */
    public static final String NOTIFY_USER_OVERDUE_SUBJECT = "NotifyUserOverdueSubject";

    /**
     * Constant used as a suffix for the "deadline passed" email subject's key.
     */
    public static final String NOTIFY_PM_OVERDUE_SUBJECT = "NotifyPmOverdueSubject";

    /**
     * Constant used as a suffix for the "deadline approaching" email
     * message-body's key.
     */
    public static final String NOTIFY_USER_OVERDUE_BODY = "NotifyUserOverdueBody";

    /**
     * Constant used as a suffix for the "deadline passed" email message-body's
     * key.
     */
    public static final String NOTIFY_PM_OVERDUE_BODY = "NotifyPmOverdueBody";

    // end the over due

    /**
     * Constant used as a suffix for the quotation email subject's key.
     */
    public static final String NOTIFY_QUOTE_PERSON_SUBJECT = "NotifyQuotePersonSubject";

    /**
     * Constant used as a suffix for the quotation email message-body's key.
     */
    public static final String NOTIFY_QUOTE_PERSON_BODY = "NotifyQuotePersonBody";

    /**
     * Constant used as a suffix for the quotation email subject's key for
     * quoting project manage the person entered a PO Number.
     */
    public static final String NOTIFY_PONUMBER_SUBJECT = "NotifyPONumberSubject";

    /**
     * Constant used as a suffix for the quotation email message-body's
     * keyConstant used as a suffix for quoting project manage the person
     * entered a PO Number.
     */
    public static final String NOTIFY_PONUMBER_BODY = "NotifyPONumberBody";

    /**
     * Constant used as a suffix for approving quote subject.
     */
    public static final String NOTIFY_QUOTEAPPROVED_SUBJECT = "NotifyQuoteApprovedSubject";

    /**
     * Constant used as a suffix for approving quote body.
     */
    public static final String NOTIFY_QUOTEAPPROVED_BODY = "NotifyQuoteApprovedBody";

    /**
     * Constant used as a key for scheduling failure email subject.
     */
    public static final String SCHEDULING_FAILED_SUBJECT = "schedulingFailedSubject";

    /**
     * Constant used as a key for scheduling failure email message body.
     */
    public static final String SCHEDULING_FAILED_BODY = "schedulingFailedMessage";
    // ////////////////////////////////////////////////////////////////////
    // End: Email Info keys
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // NUM_OF_DAYS_INTERVAL and NUM_OF_DAYS_EXPRESSION should both
    // have the same value
    // ////////////////////////////////////////////////////////////////////
    /**
     * The constant used for getting interval ranges for a month. This is used
     * for UI to make sure the ranges for a given month is returned.
     */
    public static final int NUM_OF_DAYS_INTERVAL = 32;

    /**
     * The expression used as duration of a business interval range (to get the
     * ranges for total of 32 days where the last day is excluded)
     */
    public static final String NUM_OF_DAYS_EXPRESSION = "+32d";

    // ///////////////////////////////////////////////////////////////////
    // Key for current company id.
    // ///////////////////////////////////////////////////////////////////
    public static final String CURRENT_COMPANY_ID = "current_company_id";

    // Temp values (should be populated from db).
    public static final HashMap s_objectTypes = new HashMap();
    public static final HashMap s_eventTypes = new HashMap();
    public static final HashMap s_eventKeys = new HashMap();

    /**
     * Keeps the event keys and event types maps in synch.
     * 
     * @param p_type
     *            String type
     * @param p_key
     *            Integer key
     */
    static public void addToKeysAndTypes(String p_type, Integer p_key)
    {
        s_eventTypes.put(p_type, p_key);
        s_eventKeys.put(p_key, p_type);
    }

    static
    {
        // Note: these constants distinguish objects in a category
        // from each other; the "object types" and "events" categories
        // are not related, they just reuse the same constants.
        Integer vals[] = new Integer[10];
        for (int i = 0; i < vals.length; i++)
        {
            vals[i] = new Integer(i);
        }

        // object types used in the FLUX_GS_MAP_OBJECT_TYPE table.
        int a = 0;
        s_objectTypes.put(WorkflowTask.class, vals[a++]);
        s_objectTypes.put(ReservedTime.class, vals[a++]);
        s_objectTypes.put(Job.class, vals[a++]);
        s_objectTypes.put(DelayedImportRequest.class, vals[a++]);
        s_objectTypes.put(TermbaseReindexExpression.class, vals[a++]);

        int b = 0;
        addToKeysAndTypes(ACCEPT_TYPE, vals[b++]);
        addToKeysAndTypes(COMPLETE_TYPE, vals[b++]);
        addToKeysAndTypes(RESERVED_TIME_TYPE, vals[b++]);
        addToKeysAndTypes(EXPORT_SOURCE_TYPE, vals[b++]);
        addToKeysAndTypes(DELAYED_REIMPORT_TYPE, vals[b++]);
        addToKeysAndTypes(TIMED_JOB_DISPATCH_TYPE, vals[b++]);
        addToKeysAndTypes(REINDEX_TERMBASE_TYPE, vals[b++]);
    }

    /**
     * Used to query the key value used for a given type String
     * 
     * @param p_type
     * @return
     */
    static public Integer getKeyForType(String p_type)
    {
        return (Integer) s_eventTypes.get(p_type);
    }

    /**
     * Returns the object type key used for the given class
     * 
     * @param p_class
     * @return
     */
    static public Integer getKeyForClass(Class p_class)
    {
        return (Integer) s_objectTypes.get(p_class);
    }
}
