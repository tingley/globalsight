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
package com.globalsight.everest.persistence.activity;

/**
 * Specifies the names of all the named queries for Activity.
 */
public interface ActivityQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all activities.
     * <p>
     * Arguments: none.
     */
    public static String ALL_ACTIVITIES = "getAllActivities";
    
    public static String ALL_DTP_ACTIVITIES = "getAllDtpActivities";
    
    public static String ALL_TRANS_ACTIVITIES = "getAllTransActivities";

    /**
     * A named query to return the activity specified by the given name.
     * The name must be passed in uppercase to be found.
     * <p>
     * Arguments: 1: Uppercased Activity Name.
     */
    public static String ACTIVITY_BY_NAME = "getActivityByName";

    /**
     * A named query to return the activity specified by the given id.
     * <p>
     * Arguments: 1 - activity id 
     */
    public static String ACTIVITY_BY_ID = "getActivityById";
}
