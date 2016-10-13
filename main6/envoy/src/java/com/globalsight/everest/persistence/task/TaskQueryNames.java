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


import com.globalsight.everest.taskmanager.TaskImpl;

/**
 * Specifies the names of all the named queries for Activity.
 */
public interface TaskQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return the accepted tasks specified by the 
     * given user id.
     * <p>
     * Arguments: 1. Task's acceptor user id
     */
    public final static String ACCEPTED_TASK_BY_USER_ID = "acceptedTaskByUserId";

    /**
     * A named query to return the task specified by the given id.
     * <p>
     * Arguments: 1. Task id
     */
    public final static String TASK_BY_ID = "getTaskById";

    /**
     * A named query to return the tasks specified by the given list of ids.
     * <p>
     * Arguments: 1. Task id list
     */
    public final static String TASKS_BY_IDS = "getTasksByIds";

    /**
     * A named query to return a Task based on the 
     * Task ID.
     * <p>
     * Arguments: 1: A Task ID
     */
    public static final String TASK_BY_PK = TASK_BY_ID; 

    /**
     * A named query to return all Tasks in a job
     * associated with a particular rate type.
     * <p>
     * Arguments: 1 - rate type
     *            2 - job id
     */
    public static final String TASKS_IN_JOB_BY_RATE_TYPE = "getTasksInJobByRateType";
    public static final String TASKS_IN_JOB_BY_RATE_TYPE_FOR_REVENUE = "getTasksInJobByRateTypeForRevenue";
    /**
     * A named query to return activity completion time of the first completed act
     * 
     * <p>
     * Arguments: 1 - job id
     */
    public static final String FIRST_COMPLETED_TASK_TIME = "getFirstCompletedTaskTime";

    /**
     * A named query to return all the completed tasks in a workflow.
     * <p>
     * Arguments: 1 - workflow id
     */
    public static final String COMPLETED_TASKS = "getCompletedTasks";

    /**
     * A named query to return all tasks for the given name and job id.
     * <p>
     * Arguments: 1 - task name
     *            2 - job id
     */
    public static final String TASKS_BY_NAME_AND_JOB_ID = 
        "getTasksByNameAndJobId";

    /**
     * A named query to return all current tasks for the given workflow id.
     * <p>
     * Arguments: 1 - Workflow Id     
     */
    public static final String CURRENT_TASKS_BY_WORKFLOW_ID = 
        "getCurrentTasksByWorkflowId";
}
