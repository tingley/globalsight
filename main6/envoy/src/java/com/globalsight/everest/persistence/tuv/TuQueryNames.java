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

import com.globalsight.everest.tuv.TuImpl;


/**
 * Specifies the names of all the named queries for Tu.
 */
public interface TuQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //

    /**
     * A named query to return a collection of Tus
     * based on
     * the given source page id.
     * <p>
     * Arguments: 1: A SourcePage id

     */
    public static final String TUS_BY_SOURCE_PAGE_ID =
            "getTusBySourcePageId";


    /**
     * <p>Names a named query that returns a collection of tu ids that
     * belong to the given source page identifier.
     * Arguments: 1: A SourcePage id </p>
     */
    public static final String TUIDS_BY_SOURCE_PAGE_ID =
        "getTuIdsBySourcePageId";

    /**
     * <p>Names a named query that returns an integer representing
     * the number of tus for a given source page id.
     * Arguments: 1: A SourcePage id </p>
     */
    public static final String TU_COUNT_BY_SOURCE_PAGE_ID =
        "getTuCountBySourcePageId";
}
