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

import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;

/**
 * TuvQueryConstants provides constants for building queries.
 */
public interface TuvQueryConstants
{
    public static final Class TASK_TUV_CLASS = TaskTuv.class;
    public static final Class TUV_CLASS = TuvImpl.class;
    public static final Class TU_CLASS = TuImpl.class;
    public static final Class TASK_CLASS = Task.class;

    //
    // Names of parameters to queries
    //

    // Tuv
    public static final String SOURCE_PAGE_ID_ARG =
        "sourcePageIdParameter";
    public static final String TARGET_PAGE_ID_ARG =
        "targetPageIdParameter";
    public static final String LOCALE_ID_ARG =
        "localeIdParameter";
    public static final String TUV_ID_ARG = 
        "tuvId";

    // TaskTuv
    public static final String TUV_ID_LIST_ARG =
        "tuvIdsParameter";
    public static final String TASK_TUV_TUV_ID_ARG =
        "taskTuvTuvIdParameter";
    public static final String TASK_ID_ARG = "taskId";

    public static final String TASK_TUV_MAX_RESULTS_ARG =
        "taskTuvMaxResultsParameter";

    //
    // Names of tables and columns
    //

    public static final String TRANSLATION_UNIT_VARIANT_TABLE =
        "TRANSLATION_UNIT_VARIANT";
    public static final String TRANSLATION_UNIT_VARIANT_ID_COLUMN =
        "ID";
    public static final String TRANSLATION_UNIT_VARIANT_TU_ID_COLUMN =
        "TU_ID";
    public static final String TRANSLATION_UNIT_VARIANT_SEGMENT_COLUMN =
        "SEGMENT_CLOB";
    public static final String TRANSLATION_UNIT_VARIANT_STATE_COLUMN =
        "STATE";
    public static final String TRANSLATION_UNIT_VARIANT_LOCALE_ID_COLUMN =
        "LOCALE_ID";
    public static final String TRANSLATION_UNIT_VARIANT_SEGMENT_CLOB_COLUMN =
        "SEGMENT_CLOB";
    public static final String TRANSLATION_UNIT_VARIANT_ORDER_COLUMN =
        "ORDER_NUM";
       

    public static final String TASK_TUV_TABLE =
        "TASK_TUV";
    public static final String TASK_TUV_CURRENT_TUV_ID_COLUMN =
        "CURRENT_TUV_ID";
    public static final String TASK_TUV_PREVIOUS_TUV_ID_COLUMN =
        "PREVIOUS_TUV_ID";
    public static final String TASK_TUV_TASK_ID_COLUMN =
        "TASK_ID";
    public static final String TASK_TUV_VERSION_COLUMN =
        "VERSION";

    public static final String TRANSLATION_UNIT_TABLE =
        "TRANSLATION_UNIT";
    public static final String TRANSLATION_UNIT_ID_COLUMN =
        "ID";
    public static final String TRANSLATION_UNIT_LEVERAGE_GROUP_ID_COLUMN =
        "LEVERAGE_GROUP_ID";
     public static final String TRANSLATION_UNIT_ORDER_COLUMN =
        "ORDER_NUM";
     
    public static final String SOURCE_PAGE_LEVERAGE_GROUP_TABLE =
        "SOURCE_PAGE_LEVERAGE_GROUP";
    public static final String SOURCE_PAGE_LEVERAGE_GROUP_LG_ID_COLUMN =
        "LG_ID";
    public static final String SOURCE_PAGE_LEVERAGE_GROUP_SP_ID_COLUMN =
        "SP_ID";

    public static final String TARGET_PAGE_TABLE =
        "TARGET_PAGE";
    public static final String TARGET_PAGE_ID_COLUMN =
        "ID";
    public static final String TARGET_PAGE_SOURCE_PAGE_ID_COLUMN =
        "SOURCE_PAGE_ID";
    public static final String TAGET_PAGE_WORKFLOW_INSTANCE_ID_COLUMN =
        "WORKFLOW_IFLOW_INSTANCE_ID";


    public static final String TARGET_PAGE_LEVERAGE_GROUP_TABLE =
        "TARGET_PAGE_LEVERAGE_GROUP";
    public static final String TARGET_PAGE_LEVERAGE_GROUP_LG_ID_COLUMN =
        "LG_ID";
    public static final String TARGET_PAGE_LEVERAGE_GROUP_TP_ID_COLUMN =
        "TP_ID";

    public static final String WORKFLOW_TABLE
        = "WORKFLOW";
    public static final String WORKFLOW_IFLOW_INSTANCE_ID_COLUMN
        = "IFLOW_INSTANCE_ID";
    public static final String WORKFLOW_TARGET_LOCALE_ID_COLUMN
        = "TARGET_LOCALE_ID";
    
    public static final String TARGET_LOCALE_ID_ARG = "target_local_id";
    
}
