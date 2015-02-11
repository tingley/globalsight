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
package com.globalsight.cxe.persistence.knownformattype;

/**
 * Specifies the names of all the named queries for KnownFormatType.
 */
public interface KnownFormatTypeQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all known format types.
     * <p>
     * Arguments: none.
     */
    public static String ALL_KNOWN_FORMAT_TYPES = "getAllKnownFormatTypes";

    /**
     * A named query to return the known format type specified by the given id.
     * <p>
     * Arguments: 1: Known Format Type Id
     */
    public static String KNOWN_FORMAT_TYPE_BY_ID = "getKnownFormatTypeById";
}
