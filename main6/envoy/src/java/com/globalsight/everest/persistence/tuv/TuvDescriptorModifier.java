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

import com.globalsight.everest.tuv.TuvState;

/**
 * TuvDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Tuv descriptor.
 */
public class TuvDescriptorModifier implements TuvQueryConstants
{
    private static final String TUV_OBJECT = "select tuv.* from ";
    private static final String TUV_ID = "select /*+ INDEX "
            + "(splg idx_splg_sp_lg) */ tuv.id from ";
    // common suffix for an sql statement
    public static final String COMMON_SUFFIX = TRANSLATION_UNIT_VARIANT_TABLE
            + " tuv, " + TRANSLATION_UNIT_TABLE + " tu, "
            + SOURCE_PAGE_LEVERAGE_GROUP_TABLE + " splg " + " where tuv."
            + TRANSLATION_UNIT_VARIANT_LOCALE_ID_COLUMN + " = :"
            + LOCALE_ID_ARG + " and tuv."
            + TRANSLATION_UNIT_VARIANT_TU_ID_COLUMN + " = tu."
            + TRANSLATION_UNIT_ID_COLUMN + " and tu."
            + TRANSLATION_UNIT_LEVERAGE_GROUP_ID_COLUMN + " = splg."
            + SOURCE_PAGE_LEVERAGE_GROUP_LG_ID_COLUMN + " and splg."
            + SOURCE_PAGE_LEVERAGE_GROUP_SP_ID_COLUMN + " = :"
            + SOURCE_PAGE_ID_ARG + " order by tuv."
            + TRANSLATION_UNIT_VARIANT_ORDER_COLUMN + " asc";

    public static final String TUV_BY_SOURCE_PAGE_SQL = TUV_OBJECT
            + COMMON_SUFFIX;
    public static final String TUV_ID_BY_SOURCE_PAGE_SQL = TUV_ID
            + COMMON_SUFFIX;

    public static final String TUV_BY_TARGET_PAGE_SQL = "select tuv.* from "
            + TRANSLATION_UNIT_VARIANT_TABLE + " tuv, "
            + TRANSLATION_UNIT_TABLE + " tu, "
            + TARGET_PAGE_LEVERAGE_GROUP_TABLE + " tplg " + " where tuv."
            + TRANSLATION_UNIT_VARIANT_LOCALE_ID_COLUMN + " = :"
            + LOCALE_ID_ARG + " and tuv."
            + TRANSLATION_UNIT_VARIANT_STATE_COLUMN + " != '"
            + TuvState.OUT_OF_DATE.getName() + "'" + " and tuv."
            + TRANSLATION_UNIT_VARIANT_TU_ID_COLUMN + " = tu."
            + TRANSLATION_UNIT_ID_COLUMN + " and tu."
            + TRANSLATION_UNIT_LEVERAGE_GROUP_ID_COLUMN + " = tplg."
            + TARGET_PAGE_LEVERAGE_GROUP_LG_ID_COLUMN + " and tplg."
            + TARGET_PAGE_LEVERAGE_GROUP_TP_ID_COLUMN + " = :"
            + TARGET_PAGE_ID_ARG + " order by tuv."
            + TRANSLATION_UNIT_VARIANT_ORDER_COLUMN + " asc";

    public static final String TARGET_TUV_BY_SOURCE_PAGE_SQL = "select tuv.* "
            + "from "
            + TRANSLATION_UNIT_VARIANT_TABLE
            + " tuv, "
            + TRANSLATION_UNIT_TABLE
            + " tu, "
            + SOURCE_PAGE_LEVERAGE_GROUP_TABLE
            + " splg "
            + " where tuv."
            + TRANSLATION_UNIT_VARIANT_LOCALE_ID_COLUMN
            + " = :"
            + LOCALE_ID_ARG
            + " and tuv."
            + TRANSLATION_UNIT_VARIANT_STATE_COLUMN
            + " != '"
            + TuvState.OUT_OF_DATE.getName()
            + "'"
            + " and tuv."
            + TRANSLATION_UNIT_VARIANT_TU_ID_COLUMN
            + " = tu."
            + TRANSLATION_UNIT_ID_COLUMN
            + " and tu."
            + TRANSLATION_UNIT_LEVERAGE_GROUP_ID_COLUMN
            + " = splg."
            + SOURCE_PAGE_LEVERAGE_GROUP_LG_ID_COLUMN
            + " and splg."
            + SOURCE_PAGE_LEVERAGE_GROUP_SP_ID_COLUMN
            + " = :"
            + SOURCE_PAGE_ID_ARG
            + " order by tuv."
            + TRANSLATION_UNIT_VARIANT_ORDER_COLUMN + " asc";

    public static final String TUV_BY_TU_LOCALE_SQL = "select tuv.* from "
            + TRANSLATION_UNIT_VARIANT_TABLE + " tuv " + "where tuv.tu_id = :"
            + TUV_ID_ARG + " and tuv.locale_id = :" + TARGET_LOCALE_ID_ARG
            + " and tuv.state != '" + TuvState.OUT_OF_DATE.getName() + "'";

}
