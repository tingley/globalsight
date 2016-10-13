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
package com.globalsight.everest.jobhandler;

import com.globalsight.everest.util.system.RemoteServer;

import java.rmi.RemoteException;

public class JobEventObserverWLRMIImpl extends RemoteServer implements
        JobEventObserverWLRemote
{
    JobEventObserver m_localReference;

    public JobEventObserverWLRMIImpl() throws RemoteException
    {
        super(JobEventObserver.SERVICE_NAME);
        m_localReference = new JobEventObserverLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public void notifyJobPendingEvent(Job param1) throws JobException,
            RemoteException
    {
        m_localReference.notifyJobPendingEvent(param1);
    }

    public void notifyJobDispatchEvent(Job param1) throws JobException,
            RemoteException
    {
        m_localReference.notifyJobDispatchEvent(param1);
    }

    public void notifyJobBatchReservedEvent(Job param1) throws JobException,
            RemoteException
    {
        m_localReference.notifyJobBatchReservedEvent(param1);
    }

    public void notifyJobLocalizedEvent(Job param1) throws JobException,
            RemoteException
    {
        m_localReference.notifyJobLocalizedEvent(param1);
    }

    public void notifyJobExportedEvent(Job param1) throws JobException,
            RemoteException
    {
        m_localReference.notifyJobExportedEvent(param1);
    }

    public void notifyJobExportFailedEvent(Job param1) throws JobException,
            RemoteException
    {
        m_localReference.notifyJobExportFailedEvent(param1);
    }

    public void notifyJobReadyToBeDispatchedEvent(Job param1)
            throws JobException, RemoteException
    {
        m_localReference.notifyJobReadyToBeDispatchedEvent(param1);
    }

    public void notifyJobImportFailedEvent(Job param1) throws JobException,
            RemoteException
    {
        m_localReference.notifyJobImportFailedEvent(param1);
    }

    public void notifyJobArchiveEvent(Job param1) throws JobException,
            RemoteException
    {
        m_localReference.notifyJobArchiveEvent(param1);
    }
}
