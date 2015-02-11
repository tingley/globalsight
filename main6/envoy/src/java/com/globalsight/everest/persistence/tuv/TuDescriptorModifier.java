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

import com.globalsight.everest.persistence.tuv.TuvQueryConstants;

/**
 * TuDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Tu descriptor.
 */
public class TuDescriptorModifier implements TuvQueryConstants
{
    // Public Constants
    public static final String ID = "tuId";
    public static final String COUNT = "tuCount";

    private static final String TUS = "select tu.* from ";
    private static final String TU_IDS = "select tu.id from ";
    private static final String TU_COUNT = "select count(*) from ";
    private static String commonString = null;
    private static String orderByString = null;

    static
    {
        // common condition
        StringBuffer sb = new StringBuffer();
        sb.append(TRANSLATION_UNIT_TABLE);
        sb.append(" tu, ");
        sb.append(SOURCE_PAGE_LEVERAGE_GROUP_TABLE);
        sb.append(" splg");
        sb.append(" where tu.");
        sb.append(TRANSLATION_UNIT_LEVERAGE_GROUP_ID_COLUMN);
        sb.append(" = splg.");
        sb.append(SOURCE_PAGE_LEVERAGE_GROUP_LG_ID_COLUMN);
        sb.append(" and splg.");
        sb.append(SOURCE_PAGE_LEVERAGE_GROUP_SP_ID_COLUMN);
        sb.append(" =:");
        sb.append(SOURCE_PAGE_ID_ARG);

        commonString = sb.toString();

        // common string plus order by condition
        StringBuffer orderBy = new StringBuffer();
        orderBy.append(commonString);
        orderBy.append(" order by ");
        orderBy.append(TRANSLATION_UNIT_ORDER_COLUMN);
        orderBy.append(" asc");

        orderByString = orderBy.toString();
    }

    public static final String TU_BY_SOURCE_PAGE_ID_SQL = TUS + orderByString;
    public static final String TUIDS_BY_SOURCE_PAGE_ID_SQL = TU_IDS
            + orderByString;
    public static final String TU_COUNT_BY_SOURCE_PAGE_ID_SQL = TU_COUNT
            + commonString;

}
