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
package com.globalsight.cxe.persistence.databaseprofile;

/**
 * Specifies the names of all the named queries for DatabaseProfile.
 */
public interface DatabaseProfileQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all database profiles.
     * <p>
     * Arguments: none.
     */
    public static String ALL_DATABASE_PROFILES = "getAllDatabaseProfiles";

    /**
     * A named query to return the database profile specified by the given id.
     * <p>
     * Arguments: 1: Database Profile id.
     */
    public static String DATABASE_PROFILE_BY_ID = "getDatabaseProfileById";
    
    /**
     * A named query to return all database profiles associated with a specific
     * l10n profile id.
     * <p>
     * Arguments: 1: L10n Profile id.
     */
    public static String DATABASE_PROFILES_BY_L10NPROFILE_ID = "getDatabaseProfilesByL10nProfileId";    
}
