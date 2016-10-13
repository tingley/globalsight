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
package com.globalsight.persistence.scheduling;

/**
 * FluxEventMapDescriptorModifier extends DescriptorModifier by providing
 * amendment methods unique to the TimeExpression descriptor.
 */
public class FluxEventMapDescriptorModifier
{
    private static final String AND_EVENT_TYPE = "and fm.event_type = :ET";

    private static StringBuffer sb = new StringBuffer();
    static
    {
        sb.append("select fm.* from ");
        sb.append("FLUX_GS_MAP fm ");
        sb.append("where fm.domain_obj_id = :DOID ");
        sb.append("and fm.domain_obj_type = :DOT ");
        // sb.append("and fm.event_type = #ET");
    }

    public static final String FLUX_EVENT_MAPS_SQL = sb.toString();

    public static final String FLUX_EVENT_MAP_SQL = sb.toString()
            + AND_EVENT_TYPE;

}
