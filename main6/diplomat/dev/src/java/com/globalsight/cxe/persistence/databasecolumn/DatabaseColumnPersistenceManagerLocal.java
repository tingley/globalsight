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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.globalsight.cxe.entity.databasecolumn.DatabaseColumn;
import com.globalsight.cxe.entity.databasecolumn.DatabaseColumnImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implementation of Database column perrsistence Manager,
 */
public class DatabaseColumnPersistenceManagerLocal implements
        DatabaseColumnPersistenceManager
{
    /**
     * Creates new DatabaseColumnPersistenceManagerLocal TODO: Remove throws
     * clause.
     */
    public DatabaseColumnPersistenceManagerLocal()
            throws DatabaseColumnEntityException
    {
        super();
    }

    /**
     * Delete specified database column from data store.
     * 
     * @param p_column
     *            Database column object to delete
     * @throws RemoteException
     *             Application Server Error
     * @throws DatabaseColumnEntityException
     *             Error deleting database column from data store.
     */
    public void deleteDatabaseColumn(DatabaseColumn p_column)
            throws RemoteException, DatabaseColumnEntityException
    {
        try
        {
            HibernateUtil.delete((DatabaseColumnImpl) p_column);
        }
        catch (Exception e)
        {
            throw new DatabaseColumnEntityException(e);
        }
    }

    /**
     * Persist a new database column object in data store.
     * 
     * @param p_column
     *            Database column object to persist in data store
     * @throws RemoteException
     *             Application Server Error
     * @throws DatabaseColumnEntityException
     *             Error creating database column in data store.
     * @return Newly created database column object
     */
    public DatabaseColumn createDatabaseColumn(DatabaseColumn p_column)
            throws RemoteException, DatabaseColumnEntityException
    {
        try
        {
            HibernateUtil.save((DatabaseColumnImpl) p_column);
            return getDatabaseColumn(p_column.getId());
        }
        catch (Exception e)
        {
            throw new DatabaseColumnEntityException(e);
        }
    }

    /**
     * Retrieve a specified database column object with passed id
     * 
     * @param p_id
     *            Id of database column to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws DatabaseColumnEntityException
     *             Error retrieving a specific database column.
     * @return Database column object with matching id
     */
    public DatabaseColumn getDatabaseColumn(long p_id) throws RemoteException,
            DatabaseColumnEntityException
    {
        try
        {
            return (DatabaseColumnImpl) HibernateUtil.get(
                    DatabaseColumnImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new DatabaseColumnEntityException(e);
        }
    }

    /**
     * Return all database column from the database that have the given profile
     * id.
     * 
     * @param p_dbProfileId
     *            database profile id
     * 
     * @throws RemoteException
     *             Application Server Error
     * @throws DatabaseColumnEntityException
     *             Error getting collection of database columns from data store.
     * 
     * @return Collection of all database columns for specified database profile
     *         id
     */
    public Collection getDatabaseColumns(long p_dbProfileId)
            throws RemoteException, DatabaseColumnEntityException
    {
        try
        {
            String hql = "from DatabaseColumnImpl d where d.dbProfileId = :dId order by d.columnNo";
            Map map = new HashMap();
            map.put("dId", new Long(p_dbProfileId));
            HibernateUtil.search(hql, map);
            return HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            throw new DatabaseColumnEntityException(e);
        }
    }

    /**
     * Update specified database column in data store.
     * 
     * @param p_column
     *            Database column object to modify
     * @throws RemoteException
     *             Application Server Error
     * @throws DatabaseColumnEntityException
     *             Error updating database column in data store.
     * @return Modified database column object
     */
    public DatabaseColumn updateDatabaseColumn(DatabaseColumn p_column)
            throws RemoteException, DatabaseColumnEntityException
    {
        try
        {
            HibernateUtil.update((DatabaseColumnImpl) p_column);
        }
        catch (Exception e)
        {
            throw new DatabaseColumnEntityException(e);
        }
        return p_column;
    }
}
