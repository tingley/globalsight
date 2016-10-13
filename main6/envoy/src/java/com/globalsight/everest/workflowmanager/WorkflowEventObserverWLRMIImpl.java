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

package com.globalsight.everest.workflowmanager;

import com.globalsight.everest.util.system.RemoteServer;

public class WorkflowEventObserverWLRMIImpl extends RemoteServer implements
        WorkflowEventObserverWLRemote
{
    WorkflowEventObserver m_localReference;

    public WorkflowEventObserverWLRMIImpl() throws java.rmi.RemoteException
    {
        super(WorkflowEventObserver.SERVICE_NAME);
        m_localReference = new WorkflowEventObserverLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public void notifyWorkflowDispatchEvent(
            com.globalsight.everest.workflowmanager.Workflow param1)
            throws com.globalsight.everest.workflowmanager.WorkflowManagerException,
            java.rmi.RemoteException
    {
        m_localReference.notifyWorkflowDispatchEvent(param1);
    }

    public void notifyWorkflowExportFailedEvent(
            com.globalsight.everest.workflowmanager.Workflow param1)
            throws com.globalsight.everest.workflowmanager.WorkflowManagerException,
            java.rmi.RemoteException
    {
        m_localReference.notifyWorkflowExportFailedEvent(param1);
    }

    public void notifyWorkflowExportedEvent(
            com.globalsight.everest.workflowmanager.Workflow param1)
            throws com.globalsight.everest.workflowmanager.WorkflowManagerException,
            java.rmi.RemoteException
    {
        m_localReference.notifyWorkflowExportedEvent(param1);
    }

    public void notifyWorkflowLocalizedEvent(
            com.globalsight.everest.workflowmanager.Workflow param1)
            throws com.globalsight.everest.workflowmanager.WorkflowManagerException,
            java.rmi.RemoteException
    {
        m_localReference.notifyWorkflowLocalizedEvent(param1);
    }

    public void notifyWorkflowPendingEvent(
            com.globalsight.everest.workflowmanager.Workflow param1)
            throws com.globalsight.everest.workflowmanager.WorkflowManagerException,
            java.rmi.RemoteException
    {
        m_localReference.notifyWorkflowPendingEvent(param1);
    }

    public void notifyWorkflowMakeReadyEvent(Workflow p_workflow)
            throws WorkflowManagerException, java.rmi.RemoteException
    {
        m_localReference.notifyWorkflowMakeReadyEvent(p_workflow);
    }

    public void notifyWorkflowArchiveEvent(
            com.globalsight.everest.workflowmanager.Workflow param1)
            throws com.globalsight.everest.workflowmanager.WorkflowManagerException,
            java.rmi.RemoteException
    {
        m_localReference.notifyWorkflowArchiveEvent(param1);
    }
}
