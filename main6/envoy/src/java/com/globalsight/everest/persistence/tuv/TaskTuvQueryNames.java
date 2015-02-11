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
package com.globalsight.everest.persistence.tuv;

import com.globalsight.everest.tuv.TuvPersistenceHelper;
import com.globalsight.everest.tuv.TaskTuv;


/**
 * Specifies the names of all the named queries for TaskTuv.
 */
public interface TaskTuvQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //

    /**
     * A named query to return a collection of TaskTuvs based on
     * the given TuvId with a maxResult count.
     * of ids.
     * <p>
     * Arguments: 1: A TUV id
     * Arguments: 2: A max results count
     */
    public static final String PREVIOUS_TASK_TUVS = "getPreviousTaskTuvs";

    /**
     * A named query to return a collection of TaskTuvs based on
     * the given Workflow id.
     * of ids.
     * <p>
     * Arguments: 1: workflow id
     */
    public static final String PREVIOUS_TASK_TUV_BY_WORKFLOW =
            "getPreviousTaskTuvsByWorkflow";

    public static final String PREVIOUS_TASK_TUV_BY_TASK_ID =
            "getPreviousTaskTuvsByTaskId";
}
