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
/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

// java
import java.util.Collection;

import com.globalsight.cxe.entity.databasecolumn.DatabaseColumn;

import java.rmi.RemoteException;

/**
 * Persistence manager for data access of database column objects.
 *
 */
public interface DatabaseColumnPersistenceManager
{
    /**
     * Database Column Persistence Service Name
     *
     */
    public static final String SERVICE_NAME = "DatabaseColumnPersistenceManager";

    /**
     * Persist a new database column object in data store.
     *
     * @param p_databaseColumn Database column object to persist in data store
     * @throws RemoteException Application Server Error
     * @throws DatabaseColumnEntityException Error creating database column in data
     *         store.
     * @return Newly created database column object
     */
    public DatabaseColumn createDatabaseColumn(DatabaseColumn p_databaseColumn)
        throws RemoteException, DatabaseColumnEntityException;

    /**
     * Delete specified database column from data store.
     *
     * @param p_databaseColumn Database column object to delete
     * @throws RemoteException Application Server Error
     * @throws DatabaseColumnEntityException Error deleting database column from data
     *         store.
     */
    public void deleteDatabaseColumn(DatabaseColumn p_databaseColumn)
        throws RemoteException, DatabaseColumnEntityException;

    /**
     * Update specified database column in data store.
     *
     * @param p_databaseColumn Database column object to modify
     * @throws RemoteException Application Server Error
     * @throws DatabaseColumnEntityException Error updating database column in data
     *         store.
     * @return Modified database column object
     */
    public DatabaseColumn updateDatabaseColumn(DatabaseColumn p_databaseColumn)
        throws RemoteException, DatabaseColumnEntityException;

    /**
     * Retrieve a specified database column object with passed id
     *
     * @param p_id Id of database column to retreive
     * @throws RemoteException Application Server Error
     * @throws DatabaseColumnEntityException Error retrieving a specific database
     *         column.
     * @return  Database column object with matching id
     */
    public DatabaseColumn getDatabaseColumn(long p_id)
        throws RemoteException, DatabaseColumnEntityException;

    /**
     * Return all database column objects from data store for specified database
     * profile id.
     *
     * @param p_dbProfileId Database profile id
     * @throws RemoteException Application Server Error
     * @throws DatabaseColumnEntityException Error getting collection of database
     *        columns from data store.
     * @return Collection of all database columns for specified database profile id
     */
    public Collection getDatabaseColumns(long p_dbProfileId)
        throws RemoteException, DatabaseColumnEntityException;
}
