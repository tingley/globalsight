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
 * CostDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Cost object's DescriptorModifier.
 */
public class CostDescriptorModifier 
{
    private static final String COST_RATE_TYPE_ARG = "costRateType";

    // SQL
    public static final String COST_BY_JOB_ID_SQL = "select * from cost "
            + "where costable_object_type = 'J'"
            + " and costable_object_id = :oId and rate_type = :"
            + COST_RATE_TYPE_ARG;

    public static final String COST_BY_WORKFLOW_ID_SQL = "select * from cost "
            + "where costable_object_type = 'W'"
            + " and costable_object_id = :oId and rate_type = :"
            + COST_RATE_TYPE_ARG;

    public static final String COST_BY_TASK_ID_SQL = "select * from cost "
            + "where costable_object_type = 'T'"
            + " and costable_object_id = :oId and rate_type = :"
            + COST_RATE_TYPE_ARG;
}
