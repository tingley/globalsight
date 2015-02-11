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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.page.PageEventObserver;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.inprogresstm.InProgressTmManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.modules.Modules;

public class JobEventObserverLocal implements JobEventObserver
{
    private static final Logger CATEGORY = Logger
            .getLogger(JobEventObserverLocal.class);

    public JobEventObserverLocal()
    {
    }

    /**
     * This sets the state of the job to be PENDING
     * 
     * @param Job
     *            p_job
     * @throws JobException,
     *             RemoteException
     */
    public void notifyJobPendingEvent(Job p_job) throws JobException,
            RemoteException
    {
        p_job.setState(Job.PENDING);
        JobPersistenceAccessor.updateJobState(p_job);

        if (Modules.isCorpusInstalled())
        {
            try
            {
                // at this time, clean up the ms-office related information in
                // the corpus
                // all the ms-office pages in the same job use the same native
                // format document
                // but they're pointing to different copies of it.
                ServerProxy.getCorpusManager().cleanUpMsOfficeJobSourcePages(
                        p_job.getId());
            }
            catch (Exception e)
            {
                // don't let this exception affect the job
                CATEGORY
                        .error("Failed to clean ms-office data from corpus.", e);
            }
        }
    }

    /**
     * This sets the state of the job to be DISPATCHED
     * 
     * @param Job
     *            p_job
     * @throws JobException,
     *             RemoteException
     */
    public void notifyJobDispatchEvent(Job p_job) throws JobException,
            RemoteException
    {
        p_job.setState(Job.DISPATCHED);
        p_job.setWordCountReached(true);
        JobPersistenceAccessor.updateJobState(p_job);

        if (p_job.getL10nProfile().getNameOfJobCreationScript() != null)
        {
            String jobCreationScript = p_job.getL10nProfile()
                    .getNameOfJobCreationScript();

            try
            {
                Process ps = Runtime.getRuntime().exec(jobCreationScript);
                ps.getOutputStream();
            }
            catch (IOException ioe)
            {
                CATEGORY.error("Unable to run job creation script", ioe);
            }
        }
    }

    /**
     * This sets the state of the job to be LOCALIZED
     * 
     * @param Job
     *            p_job
     * @throws JobException,
     *             RemoteException
     */
    public void notifyJobLocalizedEvent(Job p_job) throws JobException,
            RemoteException
    {
        p_job.setState(Job.LOCALIZED);
        JobPersistenceAccessor.updateJobState(p_job);
    }

    /**
     * This sets the state of the job to be EXPORTED and delete in-progress TM
     * data
     * 
     * @param Job
     *            p_job
     * @throws JobException,
     *             RemoteException
     */
    public void notifyJobExportedEvent(Job p_job) throws JobException,
            RemoteException
    {
        p_job.setState(Job.EXPORTED);
        JobPersistenceAccessor.updateJobState(p_job);

        deleteInProgressTmData(p_job);
    }

    /**
     * This sets the state of the job to be EXPORT_FAILED and delete in-progress
     * TM data
     * 
     * @param Job
     *            p_job
     * @throws JobException,
     *             RemoteException
     */
    public void notifyJobExportFailedEvent(Job p_job) throws JobException,
            RemoteException
    {
        p_job.setState(Job.EXPORT_FAIL);
        JobPersistenceAccessor.updateJobState(p_job);

        deleteInProgressTmData(p_job);
    }

    /**
     * This sets the state of the job to be BATCH_RESERVED
     * 
     * @param Job
     *            p_job
     * @throws JobException,
     *             RemoteException
     */
    public void notifyJobBatchReservedEvent(Job p_job) throws JobException,
            RemoteException
    {
        p_job.setState(Job.BATCHRESERVED);
        JobPersistenceAccessor.updateJobState(p_job);
    }

    /**
     * This sets the state of the job to be READY_TO_BE_DISPATCHED
     * 
     * @param Job
     *            p_job
     * @throws JobException,
     *             RemoteException
     */
    public void notifyJobReadyToBeDispatchedEvent(Job p_job)
            throws JobException, RemoteException
    {
        JobImpl jobImplClone = null;
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            jobImplClone = (JobImpl) p_job;
            Iterator it = jobImplClone.getWorkflows().iterator();

            while (it.hasNext())
            {
                WorkflowImpl wf = (WorkflowImpl) it.next();
                // only change the state if not cancelled, import failed or
                // if already in the correct state
                if (!wf.getState().equals(Job.READY_TO_BE_DISPATCHED)
                        && !wf.getState().equals(Workflow.CANCELLED)
                        && !wf.getState().equals(Workflow.IMPORT_FAILED))
                {
                    WorkflowImpl wfClone = (WorkflowImpl) session.get(
                            WorkflowImpl.class, wf.getIdAsLong());
                    wfClone.setState(Job.READY_TO_BE_DISPATCHED);
                    session.update(wfClone);
                }
            }

            jobImplClone.setState(Job.READY_TO_BE_DISPATCHED);
            session.saveOrUpdate(jobImplClone);

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            throw new JobException(
                    JobException.MSG_FAILED_TO_READY_TO_BE_DISPATCHED, null, e);

        }
        finally
        {
            if (session != null)
            {
                //session.close();
            }
        }
    }

    public void notifyJobImportFailedEvent(Job p_job) throws JobException,
            RemoteException
    {
        p_job.setState(Job.IMPORTFAILED);
        JobPersistenceAccessor.updateJobState(p_job);
    }

    public void notifyJobArchiveEvent(Job p_job) throws JobException,
            RemoteException
    {
        p_job.setState(Job.ARCHIVED);
        JobPersistenceAccessor.updateJobState(p_job);
    }

    protected PageEventObserver getPageEventObserver()
    {
        PageEventObserver pageEventObserver = null;

        try
        {
            pageEventObserver = ServerProxy.getPageEventObserver();
        }
        catch (Exception e)
        {
            CATEGORY.error("PageEventObserver::getPageEventObserver", e);
        }

        return pageEventObserver;
    }

    private void deleteInProgressTmData(Job p_job) throws JobException
    {
        try
        {
            InProgressTmManager inProgressTmManager = LingServerProxy
                    .getInProgressTmManager();
            inProgressTmManager.deleteSegments(p_job.getId());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JobException(e);
        }
    }

}
