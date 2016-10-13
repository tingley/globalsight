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

import java.util.Collection;

import java.rmi.RemoteException;

/** A service interface for performing CRUD operations for DBConnections **/
public interface DBConnectionPersistenceManager
{
    public static final String SERVICE_NAME = "DBConnectionPersistenceManager";

    /**
     * Creates a new DBConnection object in the data store
     * @return the newly created object
     */
    public DBConnection createDBConnection(DBConnection p_dbconnection)
    throws DBConnectionEntityException, RemoteException;

    /**
     * Reads the DBConnection object from the datastore
     * @return DBConnection with the given id
     */
    public DBConnection readDBConnection(long p_id)
    throws DBConnectionEntityException, RemoteException;

    /**
     * Deletes a DBConnection object from the datastore
     */
    public void deleteDBConnection(DBConnection p_dbconnection)
    throws DBConnectionEntityException, RemoteException;

    /**
     * Update the DBConnection object in the datastore
     * @return the newly updated object
     */
    public DBConnection updateDBConnection(DBConnection p_dbconnection)
    throws DBConnectionEntityException, RemoteException;

    /**
     * Get a list of all existing DBConnections objects in the datastore
     * @return a vector of the DBConnections objects
     */
    public Collection getAllDBConnections()
    throws DBConnectionEntityException, RemoteException;

    /**
     * Creates a new DBDispatch object in the data store
     * @return the newly created object
     */
    public DBDispatch createDBDispatch(DBDispatch p_dbdispatch)
    throws DBConnectionEntityException, RemoteException;

    /**
     * Reads the DBDispatch object from the datastore
     * @return DBDispatch with the given id
     */
    public DBDispatch readDBDispatch(long p_id)
    throws DBConnectionEntityException, RemoteException;

    /**
     * Deletes a DBDispatch object from the datastore
     */
    public void deleteDBDispatch(DBDispatch p_dbdispatch)
    throws DBConnectionEntityException, RemoteException;

    /**
     * Update the DBDispatch object in the datastore
     * @return the newly updated object
     */
    public DBDispatch updateDBDispatch(DBDispatch p_dbdispatch)
    throws DBConnectionEntityException, RemoteException;

    /**
     * Get a list of all existing DBDispatches objects in the datastore
     * @return a vector of the DBDispatches objects
     */
    public Collection getAllDBDispatches()
    throws DBConnectionEntityException, RemoteException;
}

