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

import java.rmi.RemoteException;

public interface WorkflowEventObserver
{
    public static final String SERVICE_NAME = "WorkflowEventObserverServer";

    /**
     * Workflow Pending State Change
     * 
     * @param Workflow
     *            p_workflow
     * @throws RemoteException, WorkflowManagerException
     */
    public void notifyWorkflowPendingEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException;

    public void notifyWorkflowMakeReadyEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException;

    /**
     * Workflow Dispatch State Change
     * 
     * @param Workflow
     *            p_workflow
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void notifyWorkflowDispatchEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException;

    /**
     * Workflow Localized State Change
     * 
     * @param Workflow
     *            p_workflow
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void notifyWorkflowLocalizedEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException;

    /**
     * Workflow Exported State Change
     * 
     * @param Workflow
     *            p_workflow
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void notifyWorkflowExportedEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException;

    /**
     * Workflow Export Failed State Change
     * 
     * @param Workflow
     *            p_workflow
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void notifyWorkflowExportFailedEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException;

    /**
     * Workflow archive state change
     * 
     * @param Workflow
     *            p_workflow
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void notifyWorkflowArchiveEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException;
}
