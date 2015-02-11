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

import com.globalsight.everest.util.system.RemoteServer;

import java.rmi.RemoteException;

public class DatabaseColumnPersistenceManagerWLRMIImpl 
    extends RemoteServer implements DatabaseColumnPersistenceManagerWLRemote
{
    DatabaseColumnPersistenceManager m_localReference;

    public DatabaseColumnPersistenceManagerWLRMIImpl() 
        throws RemoteException, DatabaseColumnEntityException
    {
        super(DatabaseColumnPersistenceManager.SERVICE_NAME);
        m_localReference = new DatabaseColumnPersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public  com.globalsight.cxe.entity.databasecolumn.DatabaseColumn createDatabaseColumn(com.globalsight.cxe.entity.databasecolumn.DatabaseColumn param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databasecolumn.DatabaseColumnEntityException
    {
        return m_localReference.createDatabaseColumn(param1);
    }

    public  void deleteDatabaseColumn(com.globalsight.cxe.entity.databasecolumn.DatabaseColumn param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databasecolumn.DatabaseColumnEntityException
    {
        m_localReference.deleteDatabaseColumn(param1);
    }

    public  com.globalsight.cxe.entity.databasecolumn.DatabaseColumn updateDatabaseColumn(com.globalsight.cxe.entity.databasecolumn.DatabaseColumn param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databasecolumn.DatabaseColumnEntityException
    {
        return m_localReference.updateDatabaseColumn(param1);
    }

    public  com.globalsight.cxe.entity.databasecolumn.DatabaseColumn getDatabaseColumn(long param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databasecolumn.DatabaseColumnEntityException
    {
        return m_localReference.getDatabaseColumn(param1);
    }

    public  java.util.Collection getDatabaseColumns(long param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databasecolumn.DatabaseColumnEntityException
    {
        return m_localReference.getDatabaseColumns(param1);
    }
}
