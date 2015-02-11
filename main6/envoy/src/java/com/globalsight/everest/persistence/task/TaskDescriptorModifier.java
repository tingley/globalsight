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
package com.globalsight.everest.persistence.task;

import com.globalsight.everest.taskmanager.Task;

/**
 * TaskDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Task descriptor.
 */
public class TaskDescriptorModifier
{

    // table and field name
    private static final String TABLE_NAME = "TASK_INFO";
    public static final String TASK_NAME_ARG = "taskName";
    public static final String TASK_USER_ID_ARG = "taskUserId";
    public static final String WORKFLOW_ID_ARG = "workflowId";
    public static final String RATE_TYPE_ARG = "rateType";
    public static final String JOB_ID_ARG = "jobId";
    public static final String ACTIVITY_COMPLETION_TIME_SQL = "SELECT "
            + "IFLOW_INSTANCE_ID, MIN(C.COMPLETED_DATE) "
            + "  FROM WORKFLOW B, " + TABLE_NAME + " C "
            + "  WHERE B.JOB_ID = :" + JOB_ID_ARG
            + "  AND   C.WORKFLOW_ID = B.IFLOW_INSTANCE_ID "
            + "  GROUP BY (B.IFLOW_INSTANCE_ID) ";
    public static final String COMPLETED_TASKS_IN_WORKFLOW_SQL = "select * from "
            + TABLE_NAME
            + " where workflow_id = :"
            + WORKFLOW_ID_ARG
            + " and completed_date is not null";

    public static String ACCEPTED_TASK_BY_USER_ID_SQL = null;
    static
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM " + TABLE_NAME + " WHERE USER_ID = :");
        sb.append(TASK_USER_ID_ARG);
        sb.append(" AND STATE = \'ACCEPTED\'");
        ACCEPTED_TASK_BY_USER_ID_SQL = sb.toString();
    }

    public static String TASKS_BY_NAME_AND_JOB_ID_SQL = null;
    static
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT t.* FROM " + TABLE_NAME + " t, WORKFLOW w");
        sb.append(" WHERE t.WORKFLOW_ID = w.IFLOW_INSTANCE_ID");
        sb.append(" AND w.JOB_ID = :").append(JOB_ID_ARG);

        TASKS_BY_NAME_AND_JOB_ID_SQL = sb.toString();
    }
    
    public static String ACTIVE_TASKS_BY_JOB_ID_SQL = null;
    static
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ti.* FROM " + TABLE_NAME + " ti, WORKFLOW w");
        sb.append(" WHERE ti.WORKFLOW_ID = w.IFLOW_INSTANCE_ID");
        sb.append(" AND w.JOB_ID = :").append(JOB_ID_ARG);
        sb.append(" AND ti.STATE = \'ACTIVE\'");
        ACTIVE_TASKS_BY_JOB_ID_SQL = sb.toString();
    }

    public static String CURRENT_TASKS_BY_WORKFLOW_ID_SQL = null;
    static
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ");
        sb.append(TABLE_NAME);
        sb.append(" WHERE WORKFLOW_ID = :");
        sb.append(WORKFLOW_ID_ARG);
        sb.append(" AND STATE IN (\'ACTIVE\', \'ACCEPTED\',\'" 
                + Task.STATE_DISPATCHED_TO_TRANSLATION_STR 
                + "\',\'" + Task.STATE_IN_TRANSLATION_STR
                + "\',\'" + Task.STATE_DISPATCHED_TO_TRANSLATION_STR
                + "\',\'" + Task.STATE_TRANSLATION_COMPLETED_STR
                + "\')");
        CURRENT_TASKS_BY_WORKFLOW_ID_SQL = sb.toString();
    }
    
}
