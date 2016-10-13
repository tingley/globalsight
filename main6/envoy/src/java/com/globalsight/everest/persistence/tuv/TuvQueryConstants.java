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
 * TuvQueryConstants provides constants for building queries.
 */
public interface TuvQueryConstants
{
    public static final String TU_TABLE_PLACEHOLDER = "\uE000" + "_TRANSLATION_UNIT_TABLE_" + "\uE000";
    public static final String TUV_TABLE_PLACEHOLDER = "\uE000" + "_TRANSLATION_UNIT_VARIANT_TABLE_" + "\uE000";
    public static final String LM_TABLE_PLACEHOLDER = "\uE000" + "_LEVERAGE_MATCH_TABLE_" + "\uE000";
    public static final String LM_EXT_TABLE_PLACEHOLDER = "\uE000" + "_LEVERAGE_MATCH_EXT_TABLE_" + "\uE000";
    public static final String TEMPLATE_PART_TABLE_PLACEHOLDER = "\uE000" + "_TEMPLATE_PART_TABLE_" + "\uE000";
    public static final String TU_TUV_ATTR_TABLE_PLACEHOLDER = "\uE000" + "_TRANSLATION_TU_TUV_ATTR_TABLE_" + "\uE000";

    public static final String TRANSLATION_UNIT_TABLE = "TRANSLATION_UNIT";
    public static final String TRANSLATION_UNIT_VARIANT_TABLE = "TRANSLATION_UNIT_VARIANT";
    public static final String LEVERAGE_MATCH_TABLE = "LEVERAGE_MATCH";
    public static final String LEVERAGE_MATCH_EXT_TABLE = "LEVERAGE_MATCH_EXT";
    public static final String TEMPLATE_PART_TABLE = "TEMPLATE_PART";

    // TaskTuv
    public static final String TASK_ID_ARG = "taskId";

    public static final String PREVIOUS_TASK_TUV_BY_TASK_ID = "select * "
            + "from task_tuv where task_id =:" + TASK_ID_ARG
            + " order by version desc";

}
