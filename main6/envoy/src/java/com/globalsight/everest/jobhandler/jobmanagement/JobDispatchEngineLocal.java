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

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.coti.util.COTIUtilEnvoy;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

public class JobDispatchEngineLocal implements JobDispatchEngine
{
    private static final String AUTOMATIC = Job.AUTOMATIC;
    private static final String PENDING_AUTOMATIC = Job.PENDING + "_"
            + AUTOMATIC;
    private static final String DISPATCHED = Job.DISPATCHED;
    private static final String READY = Job.READY_TO_BE_DISPATCHED + "_"
            + Job.MANUAL;

    private static final String PENDING_MANUAL = Job.PENDING + "_" + Job.MANUAL;

    private static final String PROCESSING_AUTOMATIC = Job.PROCESSING + "_"
            + AUTOMATIC;
    private static final String PROCESSING_MANUAL = Job.PROCESSING + "_"
            + Job.MANUAL;
    private static final String LEVERAGING_AUTOMATIC = Job.LEVERAGING + "_"
            + AUTOMATIC;
    private static final String LEVERAGING_MANUAL = Job.LEVERAGING + "_"
            + Job.MANUAL;
    private static final String ADDING_FILES_MANUAL = Job.ADD_FILE + "_"
            + Job.MANUAL;
    private static final String ADDING_FILES_AUTOMATIC = Job.ADD_FILE + "_"
            + AUTOMATIC;

    private Hashtable m_jobDispatchManager = null;

    private static final Logger c_category = Logger
            .getLogger(JobDispatcher.class.getName());

    private static HashMap<String, String> m_jobStateTransitions = new HashMap<String, String>();

    static
    {
        m_jobStateTransitions.put(PENDING_AUTOMATIC, DISPATCHED);
        m_jobStateTransitions.put(PENDING_MANUAL, READY);
        m_jobStateTransitions.put(READY, DISPATCHED);
        m_jobStateTransitions.put(PROCESSING_AUTOMATIC, DISPATCHED);
        m_jobStateTransitions.put(PROCESSING_MANUAL, READY);
        m_jobStateTransitions.put(LEVERAGING_AUTOMATIC, DISPATCHED);
        m_jobStateTransitions.put(LEVERAGING_MANUAL, READY);
        m_jobStateTransitions.put(ADDING_FILES_MANUAL, READY);
        m_jobStateTransitions.put(ADDING_FILES_AUTOMATIC, DISPATCHED);
    }

    public JobDispatchEngineLocal() throws JobException
    {
        m_jobDispatchManager = new Hashtable();
        // Load up all the jobs that are pending into JobDispatchManager
        // and create JobDispatchers
        Iterator it = null;
        try
        {
            String hql = "from JobImpl j where j.state = 'PENDING' or j.state = 'IMPORT_FAILED'";
            HibernateUtil.search(hql).iterator();
            it = HibernateUtil.search(hql).iterator();
        }
        catch (Exception pe)
        {
            throw new JobException(JobException.MSG_NO_JOBS_IN_PENDING_STATE,
                    null, pe, JobException.PROPERTY_FILE_NAME);
        }
        if (it != null)
        {
            while (it.hasNext())
            {
                Job job = null;
                try
                {
                    job = (Job) it.next();
                    JobDispatcher jobDispatcher = new JobDispatcher(job);
                    m_jobDispatchManager.put(new Long(job.getId()),
                            jobDispatcher);
                }
                catch (Throwable t)
                {
                    String jobName = null;
                    if (job != null)
                        jobName = job.getJobName();
                    c_category.warn(
                            "Failed to dispatch pending job " + jobName, t);
                }
            }
        }
    }

    public static HashMap<String, String> getJobStateTransitions()
    {
        return m_jobStateTransitions;
    }

    /**
     * This method is used to create a job dispatcher. When a job is created a
     * job dispatcher must also be created. A one to one relationship exists
     * between JobDispatcher and a Job.
     * 
     * @param Job
     *            p_job
     * @throws JobException
     *             , RemoteException
     */
    public void createDispatcher(Job p_job) throws JobException,
            RemoteException
    {
        Long jobId = getId(p_job);
        if (m_jobDispatchManager.get(jobId) == null)
        {
            if (c_category.isDebugEnabled())
            {
                c_category
                        .debug("Creating a job dispatcher for the job with Id"
                                + jobId);
            }
            m_jobDispatchManager.put(jobId, new JobDispatcher(p_job));
        }
    }

    /**
     * This method is used to dispatch a job manually. The preconditions are a
     * job dispatcher must exist. Secondly the job must be either in a pending
     * state or a ready to be dispatched state.
     * 
     * @param Job
     *            p_job
     * @throws JobException
     *             , RemoteException
     */
    public void dispatchJob(Job p_job) throws JobException, RemoteException
    {
        if (p_job.getState().equals(Job.PENDING))
        {
            JobDispatcher jobDispatcher = getDispatcher(p_job);
            jobDispatcher.dispatchJob(p_job);
            jobDispatcher.destroyTimer(p_job);
            m_jobDispatchManager.remove(jobDispatcher);
        }
        else
        {
            JobDispatcher jobDispatcher = new JobDispatcher(p_job);
            jobDispatcher.dispatchJob(p_job);
        }
    }

    /**
     * This method is used for canceling a job and ALL of its workflows. Not
     * called by the GUI so no session id is passed.
     * 
     * @param p_job
     *            - the job to cancel
     * @param p_reimport
     *            - Is this from reimport?
     */
    public void cancelJob(Job p_job, boolean p_reimport) throws JobException,
            RemoteException
    {
        cancelJob(User.SYSTEM_USER_ID, p_job, null, p_reimport);
    }

    /**
     * This method is used for canceling a job and ALL of its workflows. Not
     * called by the GUI so no session id is passed.
     * 
     * @param p_job
     *            - the job to cancel
     */
    public void cancelJob(Job p_job) throws JobException, RemoteException
    {
        cancelJob(p_job, false);
    }

    /**
     * @see JobDispatchEngine.cancelJob(String, String, Job, String, boolean)
     */
    public void cancelJob(String p_idOfUserRequestingCancel, Job p_job,
            String p_state, boolean p_reimport) throws JobException,
            RemoteException
    {
        if (p_job.getState().equals(Job.PENDING))
        {
            JobDispatcher jobDispatcher = getDispatcher(p_job);
            jobDispatcher.cancelJob(p_idOfUserRequestingCancel, p_job, p_state);
            jobDispatcher.destroyTimer(p_job);
            m_jobDispatchManager.remove(jobDispatcher);
        }
        else
        {
            JobDispatcher jobDispatcher = new JobDispatcher(p_job);
            jobDispatcher.cancelJob(p_idOfUserRequestingCancel, p_job, p_state,
                    p_reimport);
        }
        // do this after the cancel. If the cancel fails then an exception is
        // thrown and this part of the code won't be reached and the comment
        // reference
        // files won't be removed
        if (!p_reimport)
        {
            deleteCommentReferenceFiles(p_job, p_state);
        }
        
        // cancel COTI job if have
        COTIUtilEnvoy.cancelCOTIJob(p_job);
    }

    /**
     * This method is used for deleting comments of a job
     * 
     * @param p_job
     *            - the job to cancel
     * @param p_state
     *            - the state of the job
     */
    private void deleteCommentReferenceFiles(Job p_job, String p_state)
            throws JobException, RemoteException
    {
        // String jobState = p_job.getState();
        try
        {
            Iterator it = p_job.getWorkflows().iterator();
            while (it.hasNext())
            {
                Workflow curWF = (Workflow) it.next();

                // if the states are equal - or no state was specified so
                // cancel all workflows
                if (p_state == null || p_state.equals(curWF.getState()))
                {

                    // get the target locale
                    Hashtable allTasks = curWF.getTasks();
                    TreeMap sortedTasks = new TreeMap(allTasks);
                    Iterator sortedTaskIterator = sortedTasks.values()
                            .iterator();
                    List comments = null;
                    Task t = null;

                    while (sortedTaskIterator.hasNext())
                    {
                        t = (Task) sortedTaskIterator.next();
                        if (t != null)
                        {
                            comments = t.getTaskComments();
                        }
                        if (comments != null)
                        {
                            for (int y = 0; y < comments.size(); y++)
                            {
                                Comment tc = (Comment) comments.get(y);
                                // String f =
                                // CommentUpload.UPLOAD_BASE_DIRECTORY
                                // + CommentUpload.UPLOAD_DIRECTORY
                                // + new Long(tc.getId()).toString() + "/" ;
                                String f = AmbFileStoragePathUtils
                                        .getCommentReferenceDirPath()
                                        + File.separator
                                        + tc.getId()
                                        + File.separator;
                                String[] dirs =
                                { "General", "Restricted" };
                                for (int h = 0; h < dirs.length; h++)
                                {
                                    String dir = "";
                                    dir += f + "/" + dirs[h];
                                    File commentFile = new File(dir);
                                    File[] srcFiles = commentFile.listFiles();
                                    if (srcFiles != null)
                                    {
                                        for (int i = 0; i < srcFiles.length; ++i)
                                        {
                                            srcFiles[i].delete();
                                        }
                                    }
                                    commentFile.delete();
                                }
                                File commentFile = new File(f);
                                commentFile.delete();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new JobException(e);
        }
    }

    /**
     * This is a call back method which a Job TimerService will invoke to denote
     * the completion of a time interval. This method is used solely for jobs
     * marked with the automatic dispatch tag
     * 
     * @param Job
     *            p_job
     * @throws JobException
     *             , RemoteException
     */
    public void timerTriggerEvent(Job p_job) throws JobException,
            RemoteException
    {
        try
        {
            JobDispatcher jobDispatcher = getDispatcher(p_job);
            Job job = loadJobFromDbIntoCache(p_job.getId());
            jobDispatcher.timerTriggerEvent(job);
            jobDispatcher.destroyTimer(job);
            m_jobDispatchManager.remove(jobDispatcher);
        }
        catch (Exception e)
        {
            throw new JobException(e);
        }
    }

    private Job loadJobFromDbIntoCache(long p_jobId) throws Exception
    {
        return ServerProxy.getJobHandler().getJobById(p_jobId);
    }

    /**
     * This is a method called by JobAdditionEngine whenever a request is added
     * to a job
     * 
     * @param Job
     *            p_job. If the word count exceeds the word count in the
     *            dispatch criteria the job is dispatched
     * @param Job
     *            p_job
     * @throws JobException
     *             , RemoteException
     */
    public void wordCountIncreased(Job p_job) throws JobException,
            RemoteException
    {
        JobDispatcher jobDispatcher = getDispatcher(p_job);
        jobDispatcher.wordCountIncreased(p_job);
        Job j = null;
        try
        {
            Vector args = new Vector();
            args.add(getId(p_job));
            j = (JobImpl) HibernateUtil.get(JobImpl.class, getId(p_job));
        }
        catch (Exception pe)
        {
            String[] errorArgs = new String[1];
            errorArgs[0] = getId(p_job).toString();
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_ID,
                    errorArgs, pe, JobException.PROPERTY_FILE_NAME);
        }

        if (j.isWordCountReached())
        {
            jobDispatcher.destroyTimer(p_job);
            m_jobDispatchManager.remove(jobDispatcher);
        }
    }

    /**
     * This method is used to dispatch a batch job.
     * 
     * @param Job
     *            p_job
     * @throws JobException
     *             , RemoteException
     */
    public void dispatchBatchJob(Job p_job) throws JobException,
            RemoteException
    {
        JobDispatcher jobDispatcher = getDispatcher(p_job);
        jobDispatcher.dispatchBatchJob(p_job);
        if (p_job.getDispatchType().equals(AUTOMATIC))
        {
            m_jobDispatchManager.remove(jobDispatcher);
        }
    }

    /**
     * This method is used to make a job ready by moving it to the Ready State
     * from PENDING state
     * 
     * @param Job
     *            p_job
     * @throws JobException
     *             , RemoteException
     */
    public void makeReadyJob(Job p_job) throws JobException, RemoteException
    {
        Job job = null;
        try
        {
            job = loadJobFromDbIntoCache(p_job.getId());
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = new Long(job.getId()).toString();
            throw new JobException(JobException.MSG_FAILED_TO_MAKE_JOB_READY,
                    args, e, JobException.PROPERTY_FILE_NAME);
        }

        String dispatchType = job.getDispatchType();
        // do not automatic dispatch job for AEM jobs
        if (dispatchType.equals(Job.AUTOMATIC)
                && !"aem_gs_translator".equals(job.getJobType()))
        {
            JobDispatcher jobDispatcher = getDispatcher(job);
            jobDispatcher.dispatchJob(job);
        }
        else
        {
            // Change the state of the job and the associated workflows
            JobImpl jobImplClone = null;
            Session session = null;
            Transaction transaction = null;

            try
            {
                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();
                jobImplClone = (JobImpl) session.get(JobImpl.class, new Long(
                        job.getId()));

                Iterator it = jobImplClone.getWorkflows().iterator();
                while (it.hasNext())
                {
                    WorkflowImpl wf = (WorkflowImpl) it.next();
                    wf.setState(Job.READY_TO_BE_DISPATCHED);
                    session.update(wf);
                }
                jobImplClone.setState(Job.READY_TO_BE_DISPATCHED);
                session.update(jobImplClone);

                transaction.commit();
            }
            catch (Exception e)
            {
                if (transaction != null)
                {
                    transaction.rollback();
                }
                String[] args = new String[1];
                args[0] = new Long(p_job.getId()).toString();
                throw new JobException(
                        JobException.MSG_FAILED_TO_MAKE_JOB_READY, args, e,
                        JobException.PROPERTY_FILE_NAME);
            }
            finally
            {
                if (session != null)
                {
                    // session.close();
                }
            }

            JobDispatcher jobDispatcher = getDispatcher(p_job);
            jobDispatcher.destroyTimer(p_job);
        }
    }

    private Long getId(Job p_job)
    {
        JobImpl j = (JobImpl) p_job;
        return j.getIdAsLong();
    }

    private JobDispatcher getDispatcher(Job p_job)
    {

        JobDispatcher jd = (JobDispatcher) m_jobDispatchManager
                .get(getId(p_job));
        // if it can't find one then just create one for the job
        // this may occur on start-up when a BATCH_RESERVED job moves to
        // IMPORT_FAIL and
        // then DISPATCHED. One was never created for a BATCH_RESERVED job
        if (jd == null)
        {
            jd = new JobDispatcher(p_job);
            // add to the list - because any code that calls this also removes
            // it from the collection
            m_jobDispatchManager.put(getId(p_job), jd);
        }
        return jd;
    }
}
