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

import java.rmi.RemoteException;

public interface JobDispatchEngine
{
    public static final String SERVICE_NAME = "JobDispatchEngine";
    /**
    * This method is used to create a JobDispatcher when
    * a job is created
    * @param Job p_job
    * @throws JobException, RemoteException
    */
    public void createDispatcher(Job p_job) throws JobException, RemoteException;
    /**
    * This method is used to notify JobDispatcher the word count has
    * increased due to a addition of a request to an existing job
    * or the addition of a request to a new job
    * @param Job p_job
    * @throws JobException, RemoteException
    */
    public void wordCountIncreased(Job p_job) throws JobException,RemoteException;

    /**
    * This method is used to let JobDispatchEngine that this batch job can now
    * be dispatched
    * @param Job p_job
    * @throws JobException, RemoteException
    */
    public void dispatchBatchJob(Job p_job) throws JobException, RemoteException;

    /**
    * This method is used to dispatch jobs by word count and timers
    * @param Job p_job
    */
    public void timerTriggerEvent(Job p_job) throws JobException, RemoteException;

    /**
    * This method is used for manual dispatch only
    * @param Job p_job
    * @throws JobException, RemoteException
    */
    public void dispatchJob(Job p_job) throws JobException,RemoteException;

    /**
     * This method is used for canceling a job and ALL of its workflows.
     * Not called by the GUI so no session id is passed.
     *
     * @param p_job - the job to cancel
     */
    public void cancelJob(Job p_job) throws JobException, RemoteException;

    /**
     * This method is used for canceling a job and ALL of its workflows.
     * Not called by the GUI so no session id is passed.
     *
     * @param p_job - the job to cancel
     */
    public void cancelJob(Job p_job, boolean p_reimport) throws JobException, RemoteException;

    /**
    * This method is used for canceling a job's workflows with the specified state.
    *
    * @param p_idOfUserRequestingCancel
    * @param p_job
    * @param p_state  The specified state of workflows to cancel or NULL if all workflows should be cancelled.
    * @throws JobException, RemoteException
    */
    public void cancelJob(String p_idOfUserRequestingCancel,
                          Job p_job, String p_state, boolean p_reimport) 
    throws JobException, RemoteException;
    
    /**
    * This method is used to make a job ready-in other words move it from the 'PENDING'
    * state to the 'READY' state.
    * @param Job p_job
    * @throws JobException, RemoteException
    */
    public void makeReadyJob(Job p_job) throws JobException, RemoteException;
}
