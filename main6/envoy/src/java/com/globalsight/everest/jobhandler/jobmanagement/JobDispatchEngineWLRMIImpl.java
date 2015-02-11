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
package com.globalsight.everest.jobhandler.jobmanagement;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;

import com.globalsight.everest.util.system.RemoteServer;

import java.rmi.RemoteException;

public class JobDispatchEngineWLRMIImpl 
    extends RemoteServer implements JobDispatchEngineWLRemote
{
    JobDispatchEngineLocal m_localReference;

    public JobDispatchEngineWLRMIImpl() throws RemoteException
    {
        super(JobDispatchEngine.SERVICE_NAME);
        try
        {
            m_localReference = new JobDispatchEngineLocal();
        }
        catch (JobException je)
        {

        }
    }
    public Object getLocalReference()
    {
        return m_localReference;
    }
    public  void createDispatcher(Job param1) 
        throws JobException,RemoteException
    {
        m_localReference.createDispatcher(param1);
    }
    public  void timerTriggerEvent(Job param1) 
        throws JobException,RemoteException
    {
        m_localReference.timerTriggerEvent(param1);
    }
    public void wordCountIncreased(Job param1)
        throws JobException, RemoteException
    {
        m_localReference.wordCountIncreased(param1);
    }
    public  void dispatchJob(Job param1) 
        throws JobException,RemoteException
    {
        m_localReference.dispatchJob(param1);
    }

    public void cancelJob(Job p_job) 
        throws JobException, RemoteException
    {
        m_localReference.cancelJob(p_job);
    }

    public void cancelJob(Job p_job, boolean p_reimport) 
        throws JobException, RemoteException
    {
        m_localReference.cancelJob(p_job, p_reimport);
    }

    public void cancelJob(String p_idOfUserRequestingCancel,
                          Job p_job, String p_state, boolean p_reimport)
        throws JobException, RemoteException
    {
        m_localReference.cancelJob(p_idOfUserRequestingCancel,
                                   p_job, p_state, p_reimport);
    }
    public void dispatchBatchJob(Job param1)
        throws JobException, RemoteException
    {
        m_localReference.dispatchBatchJob(param1);
    }
    public void makeReadyJob(Job param1)
        throws JobException, RemoteException
    {
        m_localReference.makeReadyJob(param1);
    }
}

