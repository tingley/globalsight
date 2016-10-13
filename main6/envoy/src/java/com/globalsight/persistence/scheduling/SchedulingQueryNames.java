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
 * Specifies the names of all the registered named queries.
 */
public interface SchedulingQueryNames
{
    
    /////////////////////////////////////////////
    //  Flux_Event_Map object (flux_gs_map table)
    /////////////////////////////////////////////
    /**
     * A named query to return a flux event map object specified 
     * by the given arguments:
     * <p>
     * Arguments:   1. Event Type
     *              2. Domain Object Id
     *              3. Domain Object Type
     */
    public static String FLUX_EVENT_MAP_BY_ARGS = 
        "fluxEventMapByArgs";

    /**
     * A named query to return all flux event map objects specified 
     * by the given arguments:
     * <p>
     * Arguments:   1. Domain Object Id
     *              2. Domain Object Type
     */
    public static String FLUX_EVENT_MAPS_BY_ARGS = 
        "fluxEventMapsByArgs";
}
