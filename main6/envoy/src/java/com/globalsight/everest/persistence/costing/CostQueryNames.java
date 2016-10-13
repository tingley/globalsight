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
package com.globalsight.everest.persistence.costing;


/**
 * Specifies the names of all the named queries for Cost.
 */
public interface CostQueryNames
{
   
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    // 

    /**
     * A named query to return a cost by its id.
     * <p>
     * Arguments: The id of the cost.                          
     */
    public static String COST_BY_ID = "getCostById"; 

    /**
     * A named query to return the cost for a particular job.
     * <p>
     * Arguments:  The id of the job.
     */
    public static String COST_BY_JOB_ID = "getCostByJobId";

    /**
     * A named query to return the cost for a particular workflow.
     * <p>
     * Arguments: The id of the workflow.
     */
    public static String COST_BY_WORKFLOW_ID = "getCostByWorkflowId";

    /**
     * A named query to return the cost for a particular task.
     * <p>
     * Arguments: The id of the task.
     */
    public static String COST_BY_TASK_ID = "getCostByTaskId";
}
