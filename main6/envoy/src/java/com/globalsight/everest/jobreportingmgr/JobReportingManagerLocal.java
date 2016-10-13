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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.criterion.Expression;

import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * JobReportingManagerLocal implements JobReportingManager and is responsible
 * for handling and delegating read only processes for obtaining information
 * about job.
 */
public class JobReportingManagerLocal implements JobReportingManager
{
    /**
     * Get a list of job objects based on a particular state(such as: 'readay',
     * 'in progresss' and 'completed'). This method is used when UI state is one
     * to one mapping to server side state mapping
     * <p>
     * 
     * @param p_state -
     *            The state of the job.
     * @return A vector of jobs that have the state specified.
     * 
     * @exception JobException
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Collection getJobsByState(String p_state) throws RemoteException,
            JobException
    {
        return getJobsByState(p_state, "", "");
    }

    /**
     * Get a list of job objects based on two particular states(such as:
     * 'readay', 'in progresss' and 'completed'). This method is used when UI
     * job state is one to many (one to two) mapping to server side job state
     * <p>
     * 
     * @param p_state -
     *            The state of the job.
     * @return A vector of only jobs that match either state.
     * 
     * @exception JobException
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Collection getJobsByState(String p_state, String p_anotherState)
            throws RemoteException, JobException
    {
        return getJobsByState(p_state, p_anotherState, "");
    }

    /**
     * Get a list of job objects based on three different states (such as:
     * 'readay', 'in progresss' and 'completed'). This method is used when UI
     * job state is one to many (one to 3) mapping to server side job state
     * <p>
     * 
     * @param p_state -
     *            The state of the job.
     * @return A vector of jobs that match one of the three states.
     * 
     * @exception JobException
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Collection getJobsByState(String p_state, String p_anotherState,
            String p_otherState) throws RemoteException, JobException
    {
        Collection jobs = null;
        try
        {
            HashSet set = new HashSet();
            set.add(p_state);
            set.add(p_anotherState);
            set.add(p_otherState);
            jobs = getJobsByState(set);
        }
        catch (Exception pe)
        {
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    null, pe, JobException.PROPERTY_FILE_NAME);
        }
        return jobs;
    }

    private Collection getJobsByState(Set states)
    {
        Collection jobs = null;
        try
        {
            jobs = HibernateUtil.getSession().createCriteria(JobImpl.class)
                    .add(Expression.in("state", states)).list();
        }
        catch (Exception pe)
        {
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_STATE,
                    null, pe, JobException.PROPERTY_FILE_NAME);
        }
        return jobs;
    }

    /**
     * Get a list of workflow objects based on a particular job id.
     * <p>
     * 
     * @param p_jobId -
     *            The id of the job (also is the foreign key in the wrokflow).
     * @return A vector of only ONE workflow (always a vector is returned since
     *         TOPLink is unaware of querying for one or more objects.)
     * @exception JobException
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    @SuppressWarnings("unchecked")
    public Collection<Workflow> getWorkflowsByJobId(long p_jobId) throws RemoteException,
            JobException
    {
        Collection<Workflow> workflows = null;
        try
        {
            String hql = "from WorkflowImpl w where w.job.id = :jId";
            HashMap<String, Long> map = new HashMap<String, Long>();
            map.put("jId", new Long(p_jobId));
            workflows = (Collection<Workflow>) HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            String[] args = new String[1];
            args[0] = new Long(p_jobId).toString();
            throw new JobException(
                    JobException.MSG_FAILED_TO_GET_WORKFLOW_BY_JOBID, args, pe,
                    JobException.PROPERTY_FILE_NAME);
        }
        return workflows;
    }
}
