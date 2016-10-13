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
package com.globalsight.config;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import com.globalsight.everest.util.system.RemoteServer;

public class UserParameterPersistenceManagerWLRMIImpl extends RemoteServer
        implements UserParameterPersistenceManagerWLRemote
{
    private UserParameterPersistenceManager m_localReference = null;

    public UserParameterPersistenceManagerWLRMIImpl() throws RemoteException,
            UserParameterEntityException
    {
        super(UserParameterPersistenceManager.SERVICE_NAME);

        m_localReference = new UserParameterPersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public UserParameter updateUserParameter(UserParameter param1)
            throws RemoteException, UserParameterEntityException
    {
        return m_localReference.updateUserParameter(param1);
    }

    public UserParameter getUserParameter(String param1, String param2)
            throws RemoteException, UserParameterEntityException
    {
        return m_localReference.getUserParameter(param1, param2);
    }

    public UserParameter getUserParameter(long param1) throws RemoteException,
            UserParameterEntityException
    {
        return m_localReference.getUserParameter(param1);
    }

    public Collection getUserParameters(String param1) throws RemoteException,
            UserParameterEntityException
    {
        return m_localReference.getUserParameters(param1);
    }

    public HashMap getUserParameterMap(String param1) throws RemoteException,
            UserParameterEntityException
    {
        return m_localReference.getUserParameterMap(param1);
    }
}
