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

import java.rmi.RemoteException;

public interface JobEventObserver
{
    public static final String SERVICE_NAME = "JobEventObserverServer";

    /**
     * Job Batch Reserved State Change
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobBatchReservedEvent(Job p_job) throws JobException,
            RemoteException;

    /**
     * Job Pending State Change
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobPendingEvent(Job p_job) throws JobException,
            RemoteException;

    /**
     * Job Ready To Be Dispatched State Change
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobReadyToBeDispatchedEvent(Job p_job)
            throws JobException, RemoteException;

    /**
     * Job Dispatch State Change
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobDispatchEvent(Job p_job) throws JobException,
            RemoteException;

    /**
     * Job Localized State Change
     * 
     * @param Job
     *            p_workflow
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobLocalizedEvent(Job p_job) throws JobException,
            RemoteException;

    /**
     * Job Exported State Change
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobExportedEvent(Job p_job) throws JobException,
            RemoteException;

    /**
     * Job Export Failed State Change
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobExportFailedEvent(Job p_job) throws JobException,
            RemoteException;

    /**
     * Job Import Failed State Change
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobImportFailedEvent(Job p_job) throws JobException,
            RemoteException;

    /**
     * Job Archive State Change
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , JobException
     */
    public void notifyJobArchiveEvent(Job p_job) throws JobException,
            RemoteException;
}
