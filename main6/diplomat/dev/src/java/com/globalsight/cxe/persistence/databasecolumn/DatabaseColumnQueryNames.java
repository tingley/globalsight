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
package com.globalsight.cxe.persistence.databasecolumn;

/**
 * Specifies the names of all the named queries for DatabaseColumn.
 */
public interface DatabaseColumnQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return the database column with the given id.
     * <p>
     * Arguments: Database Column id.
     */
    public static String DATABASE_COLUMN_BY_ID = "getDatabaseColumnById";

    /**
     * A named query to return the database columns with the given profile id.
     * <p>
     * Arguments: Database Profile id.
     */
    public static String DATABASE_COLUMNS_BY_PROFILE_ID = 
        "getDatabaseColumnsByProfileId";
}
