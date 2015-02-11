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

import java.util.Collection;

import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.everest.util.system.RemoteServer;

public class DBConnectionPersistenceManagerWLRMIImpl 
    extends RemoteServer 
    implements DBConnectionPersistenceManagerWLRemote
{
    DBConnectionPersistenceManager m_localReference;

    public DBConnectionPersistenceManagerWLRMIImpl()
        throws java.rmi.RemoteException, DBConnectionEntityException
    {
        super(DBConnectionPersistenceManager.SERVICE_NAME);
        m_localReference = new DBConnectionPersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public  com.globalsight.cxe.entity.dbconnection.DBConnection createDBConnection(com.globalsight.cxe.entity.dbconnection.DBConnection param1) throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException,java.rmi.RemoteException
    {
        return m_localReference.createDBConnection(param1);
    }

    public  void deleteDBConnection(com.globalsight.cxe.entity.dbconnection.DBConnection param1) throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException,java.rmi.RemoteException
    {
        m_localReference.deleteDBConnection(param1);
    }

    public  com.globalsight.cxe.entity.dbconnection.DBConnection readDBConnection(long param1) throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException,java.rmi.RemoteException
    {
        return m_localReference.readDBConnection(param1);
    }

    public  com.globalsight.cxe.entity.dbconnection.DBConnection updateDBConnection(com.globalsight.cxe.entity.dbconnection.DBConnection param1) throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException,java.rmi.RemoteException
    {
        return m_localReference.updateDBConnection(param1);
    }

    public Collection getAllDBConnections() throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException, java.rmi.RemoteException
    {
        return m_localReference.getAllDBConnections();
    }

    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: DB Dispatch  //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    public  com.globalsight.cxe.entity.dbconnection.DBDispatch createDBDispatch(com.globalsight.cxe.entity.dbconnection.DBDispatch param1) throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException,java.rmi.RemoteException
    {
        return m_localReference.createDBDispatch(param1);
    }

    public  void deleteDBDispatch(com.globalsight.cxe.entity.dbconnection.DBDispatch param1) throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException,java.rmi.RemoteException
    {
        m_localReference.deleteDBDispatch(param1);
    }

    public  com.globalsight.cxe.entity.dbconnection.DBDispatch readDBDispatch(long param1) throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException,java.rmi.RemoteException
    {
        return m_localReference.readDBDispatch(param1);
    }

    public  com.globalsight.cxe.entity.dbconnection.DBDispatch updateDBDispatch(com.globalsight.cxe.entity.dbconnection.DBDispatch param1) throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException,java.rmi.RemoteException
    {
        return m_localReference.updateDBDispatch(param1);
    }

    public java.util.Collection getAllDBDispatches() throws com.globalsight.cxe.persistence.dbconnection.DBConnectionEntityException, java.rmi.RemoteException
    {
        return m_localReference.getAllDBDispatches();
    }

    //////////////////////////////////////////////////////////////////////////////
    //  END: DB Dispatch  ////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
}
