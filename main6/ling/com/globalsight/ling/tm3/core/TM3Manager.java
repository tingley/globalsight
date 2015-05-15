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
package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

/**
 * Top-level interface for interacting with TM3.
 * 
 */
public interface TM3Manager
{

    /**
     * Create a new shared pool for storing shared TMs.
     * <p>
     * <b>Note that this method is non-transactional, because table creation is
     * involved.</b>
     * 
     * @param conn
     *            Active JDBC connection. Unlike other TM3 methods, a connection
     *            is used here, because only table creation is performed.
     * @param id
     *            ID of this pool. Note that these IDs must be externally
     *            managed.
     * @return true if the pool was created. False if it already existed.
     * @throws TM3Exception
     *             if an error occurred
     */
    public boolean createStoragePool(Connection conn, long id,
            Set<TM3Attribute> inlineAttributes) throws TM3Exception;

    /**
     * Permanentaly remove a shared pool and any TMs stored in it.
     * <p>
     * <b>Note that this method is non-transactional, because table removal is
     * involved.</b>
     * 
     * @param conn
     *            Active JDBC connection. Unlike other TM3 methods, a connection
     *            is used here, because only table deletion is performed.
     * @param id
     *            ID of this pool
     * @return true if the pool was removed. False if it did not exist.
     * @throws TM3Exception
     *             if an error occurred
     */
    public boolean removeStoragePool(Connection conn, long id)
            throws TM3Exception;

    /**
     * Get a list of all TMs in the database. This method should be used with
     * care, since it will attach a single data factory to every instance.
     * 
     * @param factory
     * @return
     * @throws TM3Exception
     */
    public <T extends TM3Data> List<TM3Tm<T>> getAllTms(
            TM3DataFactory<T> factory) throws TM3Exception;

    /**
     * Load a TM from the database.
     * 
     * @param factory
     *            factory object to deserialize TUV data
     * @param id
     *            TM id
     * @return TM, or null if no TM with that id exists
     * @throws TM3Exception
     *             If an error occurs while the TM is being loaded.
     */
    public <T extends TM3Data> TM3Tm<T> getTm(TM3DataFactory<T> factory, long id)
            throws TM3Exception;

    /**
     * Create a new multilingual TM, using shared storage identified by
     * <tt>sharedStorageId</tt>.
     * 
     * @param factory
     * @param sharedStorageId
     * @return
     * @throws TM3Exception
     */
    public <T extends TM3Data> TM3SharedTm<T> createMultilingualSharedTm(
            TM3DataFactory<T> factory, Set<TM3Attribute> inlineAttributes,
            long sharedStorageId) throws TM3Exception;

    /**
     * Remove a tm and all associated metadata, including segment (TU/TUV) data,
     * attributes, and event history.
     * 
     * @param tm
     * @throws TM3Exception
     */
    public <T extends TM3Data> void removeTm(TM3Tm<T> tm) throws TM3Exception;

}
