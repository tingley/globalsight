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

import org.apache.log4j.Logger;

// globalsight
import com.globalsight.everest.request.Request;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;

import java.rmi.RemoteException;

public class JobCreatorWLRMIImpl extends RemoteServer implements
        JobCreatorWLRemote
{
    private static final Logger c_logger = Logger
            .getLogger(JobCreatorWLRMIImpl.class);

    private JobCreator m_localReference;

    public JobCreatorWLRMIImpl() throws RemoteException
    {
        super(JobCreator.SERVICE_NAME);

        try
        {
            m_localReference = new JobCreatorLocal();
        }
        catch (JobCreationException jce)
        {
            c_logger.error("Failed to create JobCreatorLocal", jce);

            throw new RemoteException();
        }
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public void addRequestToJob(Request param1) throws RemoteException,
            JobCreationException
    {
        m_localReference.addRequestToJob(param1);
    }

    public void init() throws SystemStartupException
    {
        super.init();
        // from GBS-2137
        // JobCreationMonitor.cleanupIncompleteJobs();
    }
}
