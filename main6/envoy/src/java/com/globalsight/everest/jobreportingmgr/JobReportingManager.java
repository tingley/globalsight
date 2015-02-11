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
package com.globalsight.everest.jobreportingmgr;

import java.util.Collection;

import java.rmi.RemoteException;

import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.workflowmanager.Workflow;

/**
 * JobReportingManager is an interface used for handling reading process of the attributes of Jobs.
 */
public interface JobReportingManager
{
    public static final String SERVICE_NAME = "JobReportingManagerServer";

    /**
   * Get a list of job objects based on a particular state  
   * @param p_state - The state of the job.   
   * @exception java.rmi.RemoteException Network related exception.
   * @exception JobException Component related exception.
   */
    Collection getJobsByState(String p_state)
    throws RemoteException, JobException;     

    Collection getJobsByState(String p_state, String p_anotherState)
    throws RemoteException, JobException;

    Collection getJobsByState(String p_state, String p_anotherState, String p_otherState)
    throws RemoteException, JobException;

    Collection<Workflow> getWorkflowsByJobId(long p_jobId)
    throws RemoteException, JobException;
} 