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
package com.globalsight.cxe.persistence.cms.teamsite.store;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
/**
 * Specifies the names of all the named queries for BackingStore.
 */
public interface BackingStoreQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all backing stores.
     * <p>
     * Arguments: none.
     */
    public static String ALL_BACKING_STORES = "getAllBackingStores";

    /**
     * A named query to return the backing store specified by the given id.
     * <p>
     * Arguments: 1: backing store id.
     */
    public static String BACKING_STORE_BY_ID = "getBackingStoreById";

    /**
     * A named query to return the backing stores specified by the given ids.
     * <p>
     * Arguments: 1: List of backing store ids.
     */
    public static String BACKING_STORES_BY_ID_LIST = "getBackingStoresByIdList";
}
