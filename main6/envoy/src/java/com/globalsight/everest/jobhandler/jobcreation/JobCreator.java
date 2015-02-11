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

package com.globalsight.everest.jobhandler.jobcreation;

// globalsight
import com.globalsight.everest.request.Request;

//third party
import java.rmi.RemoteException;


/**
 * JobCreator is an interface used for importing a request, creating
 * jobs and adding requests to pending jobs.
 */
public interface JobCreator
{
    // The name bound to the remote object.
    public static final String SERVICE_NAME = "JobCreator";

    /**
     * Adds a Request to a Job.
     *
     * @param p_request The Request object that will be added to a job
     *
     * @exception JobCreationException will be returned for any errors
     * that occurred specific to adding the request to a job (business
     * logic, db access, etc.)
     * @exception RemoteException will be thrown if there is a network
     * issue
     */
    void addRequestToJob(Request p_request)
        throws RemoteException, JobCreationException;
}
