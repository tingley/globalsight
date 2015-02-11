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

/**
 * TaskTuvDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the TaskTuv descriptor.
 */
public class TaskTuvDescriptorModifier

implements TuvQueryConstants
{

    public static final String PREVIOUS_TASK_TUV_BY_WORKFLOW_SQL = "select t0.* "
            + "from TASK_TUV t0, TASK_INFO t1 "
            + "where t0.task_id = t1.task_id and t1.workflow_id = :WORKFLOW_ARG";
    public static final String PREVIOUS_TASK_TUV_BY_TASK_ID = "select * "
            + "from task_tuv where task_id =:" + TASK_ID_ARG
            + " order by version desc";

}
