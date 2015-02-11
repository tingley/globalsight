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
package com.globalsight.cxe.persistence.dbconnection;

/**
 * Specifies the names of all the named queries for DBConnection.
 */
public interface DBConnectionQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all db connections.
     * <p>
     * Arguments: none.
     */
    public static String ALL_DB_CONNECTIONS = "getAllDBConnections";

    /**
     * A named query to return the db connection specified by the given id.
     * <p>
     * Arguments: 1: DB Connection id.
     */
    public static String DB_CONNECTION_BY_ID = "getDBConnectionById";
}
