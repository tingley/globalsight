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
package com.globalsight.ling.tm2.persistence;


/**
 * An interface to define methods of retrieving Tu objects from
 * the TM (or any other table from the database)
 */

interface TuRetriever
{

    /**
     * Query the database. Implementations of this method queries the
     * database using criteria that are set to the class before the
     * method is called.
     *
     * The query this method runs should return Tu and Tuv data in one
     * row and it must be sorted on Tu.id so all Tuv data that belong
     * to a Tu are returned next to each other.
     *
     * @return SegmentQueryResult that holds ResultSet(s) that are
     * returned from this query.
     */
    SegmentQueryResult query()
        throws Exception;
    

    /**
     * Release resources used to query database
     */
    void close()
        throws Exception;


}
