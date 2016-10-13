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

import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.util.system.RemoteServer;

import java.rmi.RemoteException;

/**
 * JobReportingManager remote implementation. All methods are just pass throughs to the
 * real JobReportingManager.
 */
public class JobReportingManagerWLRMIImpl
    extends RemoteServer 
    implements JobReportingManagerWLRemote
{
    JobReportingManager m_localReference;

    public JobReportingManagerWLRMIImpl() 
        throws RemoteException
    {
        super(JobReportingManager.SERVICE_NAME);
        m_localReference = new JobReportingManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public Collection getJobsByState(String p_param1) 
    throws RemoteException, JobException
    {
        return m_localReference.getJobsByState(p_param1);
    }

    public Collection getJobsByState(String p_param1, String p_param2) 
    throws RemoteException, JobException
    {
        return m_localReference.getJobsByState(p_param1, p_param2);
    }

    public Collection getJobsByState(String p_param1, String p_param2, String p_param3) 
    throws RemoteException, JobException
    {
        return m_localReference.getJobsByState(p_param1, p_param2, p_param3);
    }

    public Collection getWorkflowsByJobId(long p_param1) 
    throws RemoteException, JobException
    {
        return m_localReference.getWorkflowsByJobId(p_param1);
    }
}
