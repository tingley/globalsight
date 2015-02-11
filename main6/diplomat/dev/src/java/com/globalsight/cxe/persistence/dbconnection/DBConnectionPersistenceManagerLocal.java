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

import com.globalsight.cxe.entity.dbconnection.DBConnection;
import com.globalsight.cxe.entity.dbconnection.DBConnectionImpl;
import com.globalsight.cxe.entity.dbconnection.DBDispatch;
import com.globalsight.cxe.entity.dbconnection.DBDispatchImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

import java.util.Collection;

import java.rmi.RemoteException;

/**
 * Implements the service interface for performing CRUD operations for DBConnections
 */
public class DBConnectionPersistenceManagerLocal
    implements DBConnectionPersistenceManager
{
    /**
     * Default constructor
     * TODO: remove throws clause
     */
    public DBConnectionPersistenceManagerLocal()
    throws DBConnectionEntityException, RemoteException
    {
        super();
    }

    /**
     * Creates a new DBConnection object in the data store
     * @return the newly created object
     */
    public DBConnection createDBConnection(DBConnection p_dbConn)
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            HibernateUtil.save((DBConnectionImpl)p_dbConn);
            return p_dbConn;
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("CreateDBConnection", null, e);
        }
    }

    /**
     * Reads the DBConnection object from the datastore
     * @return DBConnection with the given id
     */
    public DBConnection readDBConnection(long p_id)
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            return(DBConnectionImpl)HibernateUtil.get(DBConnectionImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("ReadDBConnection", null, e);
        }
    }

    /**
     * Deletes a DBConnection object from the datastore
     */
    public void deleteDBConnection(DBConnection p_dbConn)
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            HibernateUtil.delete((DBConnectionImpl)p_dbConn);
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("DeleteDBConnection", null, e);
        }
    }

    /**
     * Update the DBConnection object in the datastore
     * @return the newly updated object
     */
    public DBConnection updateDBConnection(DBConnection p_dbConn)
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            HibernateUtil.update((DBConnectionImpl)p_dbConn);
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("UpdateDBConnection", null, e);
        }
        return p_dbConn;
    }

    /**
     * Get a list of all existing DBConnections objects in the datastore.
     * The complete list is expected to be less than 10 connections, so make
     * them all editable on the first pass.
     *
     * @return a vector of the DBConnections objects
     */
    public Collection getAllDBConnections()
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            String hql = "from DBConnectionImpl conn order by conn.name";
            return HibernateUtil.search(hql,null);          
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("GetAllDBConnections", null, e);
        }
    }

    /**
     * Creates a new DBDispatch object in the data store
     * @return the newly created object
     */
    public DBDispatch createDBDispatch(DBDispatch p_dbDisp)
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            HibernateUtil.save((DBDispatchImpl)p_dbDisp);
            return p_dbDisp;
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("CreateDBDispatch", null, e);
        }
    }

    /**
     * Reads the DBDispatch object from the datastore
     * @return DBDispatch with the given id
     */
    public DBDispatch readDBDispatch(long p_id)
    throws DBConnectionEntityException, RemoteException
    {
        try
        {           
            return(DBDispatchImpl)HibernateUtil.get(DBDispatchImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("ReadDBDispatch", null, e);
        }
    }

    /**
     * Deletes a DBDispatch object from the datastore
     */
    public void deleteDBDispatch(DBDispatch p_dbDisp)
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            HibernateUtil.delete((DBDispatchImpl)p_dbDisp);
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("DeleteDBDispatch", null, e);
        }
    }

    /**
     * Update the DBDispatch object in the datastore
     * @return the newly updated object
     */
    public DBDispatch updateDBDispatch(DBDispatch p_dbDisp)
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            HibernateUtil.update((DBDispatchImpl)p_dbDisp);
        }
        catch (Exception pe)
        {
            throw new DBConnectionEntityException("UpdateDBDispatch", null, pe);
        }
        return p_dbDisp;
    }

    /**
     * Get a list of all existing dbDispatches objects in the datastore
     * The complete list is expected to be less than 10 dispatches, so make
     * them all editable on the first pass.
     *
     * @return a vector of the dbDispatches objects
     */
    public Collection getAllDBDispatches()
    throws DBConnectionEntityException, RemoteException
    {
        try
        {
            String hql = "from DBDispatchImpl d order by d.name";
            return HibernateUtil.search(hql,null);      
        }
        catch (Exception e)
        {
            throw new DBConnectionEntityException("GetAllDBDispatches", null, e);
        }
    }
}

