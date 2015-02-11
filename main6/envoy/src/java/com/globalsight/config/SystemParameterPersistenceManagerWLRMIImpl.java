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

import com.globalsight.config.SystemParameterChangeListener;
import com.globalsight.everest.util.system.RemoteServer;

public class SystemParameterPersistenceManagerWLRMIImpl 
    extends RemoteServer
    implements SystemParameterPersistenceManagerWLRemote
{
    SystemParameterPersistenceManager m_localReference;

    public SystemParameterPersistenceManagerWLRMIImpl() 
        throws java.rmi.RemoteException, SystemParameterEntityException
    {
        super(SystemParameterPersistenceManager.SERVICE_NAME);
        m_localReference = new SystemParameterPersistenceManagerLocal();
        // notify any listeners that the system paramters are ready now.
        //((SystemParameterPersistenceManagerLocal)m_localReference).notifyListeners();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public  com.globalsight.config.SystemParameter updateSystemParameter(com.globalsight.config.SystemParameter param1) throws java.rmi.RemoteException,com.globalsight.config.SystemParameterEntityException
    {
        return m_localReference.updateSystemParameter(param1);
    }
    
    public SystemParameter updateAdminSystemParameter(SystemParameter p_sysParm)
    throws RemoteException, SystemParameterEntityException{
        return m_localReference.updateAdminSystemParameter(p_sysParm);        
    }

    public  com.globalsight.config.SystemParameter getSystemParameter(java.lang.String param1) throws java.rmi.RemoteException,com.globalsight.config.SystemParameterEntityException
    {
        return m_localReference.getSystemParameter(param1);
    }

    public  com.globalsight.config.SystemParameter getSystemParameter(java.lang.String paramName, String p_companyId) throws java.rmi.RemoteException,com.globalsight.config.SystemParameterEntityException
    {
        return m_localReference.getSystemParameter(paramName, p_companyId);
    }
    
    public SystemParameter getAdminSystemParameter(java.lang.String param1) throws java.rmi.RemoteException,com.globalsight.config.SystemParameterEntityException
    {
        return m_localReference.getAdminSystemParameter(param1);
    }

    public  com.globalsight.config.SystemParameter getSystemParameter(long param1) throws java.rmi.RemoteException,com.globalsight.config.SystemParameterEntityException
    {
        return m_localReference.getSystemParameter(param1);
    }

    public  java.util.Collection getSystemParameters() throws java.rmi.RemoteException,com.globalsight.config.SystemParameterEntityException
    {
        return m_localReference.getSystemParameters();
    }


    public void registerListener(SystemParameterChangeListener p_listener,
                                 String p_systemParameterName)
    {
        m_localReference.registerListener(p_listener, p_systemParameterName);
    }

    public void clearDirty() {
        m_localReference.clearDirty();
    }

    public void setDirty() {
        m_localReference.setDirty();
    }
}
