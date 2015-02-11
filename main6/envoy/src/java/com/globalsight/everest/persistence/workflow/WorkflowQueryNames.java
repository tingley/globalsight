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
package com.globalsight.everest.persistence.workflow;

/**
 * Specifies the names of all the named queries for Workflow.
 */
public interface WorkflowQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return the workflow with the given id.
     * <p>
     * Arguments: 1: Workflow id.
     */
    public final static String WORKFLOW_BY_ID = "getWorkflowById";

    /**
     * A named query to return all workflows associated with a particular job id.
     *
     * <p>
     * Arguemnts: 1: job id
     */
    public static final String WORKFLOWS_BY_JOB_ID = 
        "getWorkflowsByJobId";

    /**
     * Named query to return the workflow owners for the given workflow id.
     * <p>
     * Arguments: 1: Workflow id.
     */
    public final static String WORKFLOW_OWNERS_BY_WFID = "wfOwnersByWfId";

    /**
     * Named query to return the workflow owners based on the given 
     * workflow id and owner type (i.e. PM, WFM).
     * <p>
     * Arguments: 1: Workflow id.
     *            2: Workflow owner type.
     */
    public final static String WORKFLOW_OWNERS_BY_WFID_AND_OWNER_TYPE = 
        "wfOwnersByWfIdAndOwnerType";

    /**
     * Named query to return the workflows based on the given 
     * project manager.
     * <p>
     * Arguments: 1: Project Manager user id.     
     */
    public final static String WORKFLOW_BY_OWNER_AND_TYPE = "wfByOwner";
}
