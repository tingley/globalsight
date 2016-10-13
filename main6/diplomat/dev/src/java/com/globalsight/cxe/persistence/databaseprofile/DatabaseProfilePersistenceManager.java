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

import com.globalsight.cxe.entity.databaseprofile.DatabaseProfile;

import java.rmi.RemoteException;

/**
 * Persistence manager for data access of database profile objects.
 *
 */
public interface DatabaseProfilePersistenceManager
{
    /**
     * Database Profile Persistence Service Name
     *
     */
    public static final String SERVICE_NAME = "DatabaseProfilePersistenceManager";

    /**
     * Persist a new database profile object in data store.
     *
     * @param p_databaseProfile Database profile object to persist in data store
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error creating database profile in data
     *         store.
     * @returns Newly created database profile object
     */
    public DatabaseProfile createDatabaseProfile(DatabaseProfile p_databaseProfile)
        throws RemoteException, DatabaseProfileEntityException;

    /**
     * Delete specified database profile from data store.
     *
     * @param p_databaseProfile Database profile object to delete
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error deleting database profile from data
     *         store.
     */
    public void deleteDatabaseProfile(DatabaseProfile p_databaseProfile)
        throws RemoteException, DatabaseProfileEntityException;

    /**
     * Update specified database profile in data store.
     *
     * @param p_databaseProfile Database profile object to modify
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error updating database profile in data
     *         store.
     * @return Modified database profile object
     */
    public DatabaseProfile updateDatabaseProfile(DatabaseProfile p_databaseProfile)
        throws RemoteException, DatabaseProfileEntityException;

    /**
     * Retrieve a specified database profile  object with passed id
     *
     * @param p_id Id of database profile to retreive
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error retrieving a specific database
     *         profile.
     * @return  Database profile object with matching id
     */
    public DatabaseProfile getDatabaseProfile(long p_id)
        throws RemoteException, DatabaseProfileEntityException;

    /**
     * Return all database profile objects from data store.
     *
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error getting collection of database
     *        profiles from data store.
     * @return Collection of all database profile  objects in system
     */
    public Collection getAllDatabaseProfiles()
        throws RemoteException, DatabaseProfileEntityException;
}

