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

import java.rmi.RemoteException;
import java.util.Collection;

import com.globalsight.cxe.entity.databaseprofile.DatabaseProfile;
import com.globalsight.cxe.entity.databaseprofile.DatabaseProfileImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implementation of Database profile persistence Manager,
 */
public class DatabaseProfilePersistenceManagerLocal  
    implements DatabaseProfilePersistenceManager
{
    /**
     * Default Constructor
     * TODO: Remove the throws clause
     */
    public DatabaseProfilePersistenceManagerLocal()
    throws DatabaseProfileEntityException
    {
        super();
    }

    /**
     * Delete the given database profile from the database.
     *
     * @param p_profile Database profile object to delete
     *
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error deleting database profile from data
     * store.
     */
    public void deleteDatabaseProfile(DatabaseProfile p_profile)
    throws RemoteException, DatabaseProfileEntityException
    {
        try
        {
            HibernateUtil.delete((DatabaseProfileImpl)p_profile);
        }
        catch (Exception e)
        {
            throw new DatabaseProfileEntityException(e);
        }
    }

    /**
     * Persist a new database profile object in data store.
     *
     * @param p_profile Database profile object to persist in data store
     *
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error creating database profile in data
     * store.
     *
     * @return newly created database profile object
     */
    public DatabaseProfile createDatabaseProfile(DatabaseProfile p_profile)
    throws RemoteException, DatabaseProfileEntityException
    {
        try
        {
            HibernateUtil.save((DatabaseProfileImpl)p_profile);
            return getDatabaseProfile(p_profile.getId());
        }
        catch (Exception e)
        {
            throw new DatabaseProfileEntityException(e);
        }
    }

    /**
     * Retrieve a specified database profile object with passed id
     *
     * @param p_id Id of database profile to retreive
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error retrieving a specific database
     * profile.
     * @return Database profile object with matching id
     */
    public DatabaseProfile getDatabaseProfile(long p_id)
    throws RemoteException, DatabaseProfileEntityException
    {
        try
        {
            return (DatabaseProfileImpl)HibernateUtil.get(DatabaseProfileImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new DatabaseProfileEntityException(e);
        }
    }

    /**
     * Return all database profile objects from data store.
     *
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error getting collection of database
     * profiles from data store.
     * @return Collection of all database profile objects in system
     */
    public Collection getAllDatabaseProfiles()
    throws RemoteException, DatabaseProfileEntityException
    {
        try
        {
            String hql = "from DatabaseProfileImpl d order by d.name";
            return HibernateUtil.search(hql);
        }
        catch (Exception e)
        {
            throw new DatabaseProfileEntityException(e);
        }
    }

    /**
     * Update specified database profile in data store.
     *
     * @param p_profile Database profile object to modify
     * @throws RemoteException Application Server Error
     * @throws DatabaseProfileEntityException Error updating database profile in data
     * store.
     * @return Modified database profile object
     */
    public DatabaseProfile updateDatabaseProfile(DatabaseProfile p_profile)
    throws RemoteException, DatabaseProfileEntityException
    {
        try
        {
            HibernateUtil.update((DatabaseProfileImpl)p_profile);
        }
        catch (Exception e)
        {
            throw new DatabaseProfileEntityException(e);
        }
        return p_profile;
    }
}
