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

import com.globalsight.everest.util.system.RemoteServer;

public class DatabaseProfilePersistenceManagerWLRMIImpl
    extends RemoteServer implements DatabaseProfilePersistenceManagerWLRemote
{
    DatabaseProfilePersistenceManager m_localReference;

    public DatabaseProfilePersistenceManagerWLRMIImpl() 
        throws java.rmi.RemoteException, DatabaseProfileEntityException
    {
        super(DatabaseProfilePersistenceManager.SERVICE_NAME);
        m_localReference = new DatabaseProfilePersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public  com.globalsight.cxe.entity.databaseprofile.DatabaseProfile createDatabaseProfile(com.globalsight.cxe.entity.databaseprofile.DatabaseProfile param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databaseprofile.DatabaseProfileEntityException
    {
        return m_localReference.createDatabaseProfile(param1);
    }

    public  void deleteDatabaseProfile(com.globalsight.cxe.entity.databaseprofile.DatabaseProfile param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databaseprofile.DatabaseProfileEntityException
    {
        m_localReference.deleteDatabaseProfile(param1);
    }

    public  com.globalsight.cxe.entity.databaseprofile.DatabaseProfile updateDatabaseProfile(com.globalsight.cxe.entity.databaseprofile.DatabaseProfile param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databaseprofile.DatabaseProfileEntityException
    {
        return m_localReference.updateDatabaseProfile(param1);
    }

    public  com.globalsight.cxe.entity.databaseprofile.DatabaseProfile getDatabaseProfile(long param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databaseprofile.DatabaseProfileEntityException
    {
        return m_localReference.getDatabaseProfile(param1);
    }

    public  java.util.Collection getAllDatabaseProfiles() throws java.rmi.RemoteException,com.globalsight.cxe.persistence.databaseprofile.DatabaseProfileEntityException
    {
        return m_localReference.getAllDatabaseProfiles();
    }
}
