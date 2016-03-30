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

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobEventObserverWLRemote;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.DataSourceType;
import com.globalsight.everest.page.PageEventObserverWLRemote;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.ling.inprogresstm.InProgressTmManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.terminology.indexer.IIndexManager;
import com.globalsight.util.AmbFileStoragePathUtils;

public class WorkflowEventObserverLocal implements WorkflowEventObserver
{
    static private final Logger s_logger = Logger
            .getLogger(WorkflowEventObserverLocal.class);

    /**
     * This method is set to state of workflow to be in PENDING state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException
     *             , RemoteException
     */
    public void notifyWorkflowPendingEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException
    {
        // TopLink code for updating the state of workflow
        WorkflowPersistenceAccessor.updateWorkflowState(p_workflow);
    }

    /**
     * This method is set to state of workflow to be in PENDING state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException
     *             , RemoteException
     */
    public void notifyWorkflowMakeReadyEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException
    {
        // TopLink code for updating the state of workflow
        WorkflowPersistenceAccessor.updateWorkflowState(p_workflow);
    }

    /**
     * This method is set to state of workflow to be in DISPATCHED state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException
     *             , RemoteException
     */
    public void notifyWorkflowDispatchEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException
    {
        try
        {
            String currentState = p_workflow.getState();
            if (!currentState.equals(Workflow.CANCELLED)
                    && !currentState.equals(Workflow.IMPORT_FAILED))
            {
                p_workflow.setState(Workflow.DISPATCHED);
                p_workflow.setDispatchedDate(new Date());

                WorkflowPersistenceAccessor.updateWorkflowState(p_workflow);

                getPageEventObserver().notifyWorkflowDispatchEvent(
                        p_workflow.getJob().getSourcePages(),
                        p_workflow.getTargetPages());
            }
            Collection workflows = getWorkflows(p_workflow);
            if (workflowsHaveState(workflows, Workflow.DISPATCHED))
            {
                getJobEventObserver().notifyJobDispatchEvent(
                        p_workflow.getJob());
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "WorkflowEventObserver::notifyWorkflowDispatchEvent", e);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_WORKFLOW,
                    null, e);
        }
    }

    /**
     * This method is set to state of workflow to be in LOCALIZED state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException
     *             , RemoteException
     */
    public void notifyWorkflowLocalizedEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException
    {
        try
        {
            p_workflow.setState(Workflow.LOCALIZED);
            p_workflow.setCompletedDate(new Date());
            WorkflowPersistenceAccessor.updateWorkflowState(p_workflow);
            Collection workflows = getWorkflows(p_workflow);
            if (workflowsHaveState(workflows, Workflow.LOCALIZED))
            {
                getJobEventObserver().notifyJobLocalizedEvent(
                        p_workflow.getJob());
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "WorkflowEventObserver::notifyWorkflowLocalizedEvent", e);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_WORKFLOW,
                    null, e);
        }
    }

    /**
     * Returns true if all workflows are in the given state. If the given state
     * is "CANCELLED" or "IMPORT_FAILED", check ALL workflows agains it;
     * otherwise, only check the given state against non-CANCELLED or
     * non-IMPORT_FAILED workflows.
     */
    private boolean workflowsHaveState(Collection p_workflows, String p_state)
    {
        boolean result = true;

        Iterator it = p_workflows.iterator();
        if (p_state.equals(Workflow.CANCELLED)
                || p_state.equals(Workflow.IMPORT_FAILED))
        {
            while (result && it.hasNext())
            {
                Workflow wf = (Workflow) it.next();
                result &= wf.getState().equals(p_state);
            }
        }
        else
        {
            while (result && it.hasNext())
            {
                String wfState = ((Workflow) it.next()).getState();
                if (!wfState.equals(Workflow.CANCELLED)
                        && !wfState.equals(Workflow.IMPORT_FAILED))
                {
                    result &= wfState.equals(p_state);
                }
            }
        }

        return result;
    }

    /**
     * This method is set to state of workflow to be in EXPORTED state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException
     *             , RemoteException
     */
    public void notifyWorkflowExportedEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException
    {
        try
        {
            WorkflowImpl wfClone = (WorkflowImpl) HibernateUtil.get(
                    WorkflowImpl.class, p_workflow.getIdAsLong());
            wfClone.setState(Workflow.EXPORTED);
            wfClone.setExportDate(new Date());
            WorkflowPersistenceAccessor.updateWorkflowState(p_workflow);
            possiblyUpdateJobForExport(wfClone, Workflow.EXPORTED);

            // re-index the new added entry
            FileProfile fileProfile = wfClone.getJob().getFileProfile();
            String companyId = String.valueOf(fileProfile.getCompanyId());

            if (fileProfile.getTerminologyApproval() == 1)
            {
                String termbaseName = wfClone.getJob().getL10nProfile()
                        .getProject().getTermbaseName();
                Termbase tb = TermbaseList.get(companyId, termbaseName);

                try
                {
                    if (!tb.isIndexing())
                    {
                        ITermbaseManager s_manager = ServerProxy
                                .getTermbaseManager();
                        ITermbase itb = s_manager.connect(termbaseName, wfClone
                                .getJob().getL10nProfile().getProject()
                                .getProjectManagerId(), "", companyId);
                        IIndexManager indexer = itb.getIndexer();
                        indexer.doIndex();
                    }
                }
                catch (Exception e)
                {
                }
            }

            /*
             * for desktop icon download Ambassador.getDownloadableJobs(...)
             */
            String dataSourceType = ((TargetPage) wfClone.getTargetPages()
                    .iterator().next()).getDataSourceType();
            boolean isAutoImport = dataSourceType
                    .equals(DataSourceType.FILE_SYSTEM_AUTO_IMPORT);
            if (isAutoImport)
            {
                Job job = wfClone.getJob();
                if (job.getState().equals(Workflow.EXPORTED))
                {
                    File diExportedDir = AmbFileStoragePathUtils
                            .getDesktopIconExportedDir(job.getCompanyId());
                    File jobDir = new File(diExportedDir, String.valueOf(job
                            .getJobId()));
                    if (!jobDir.exists())
                    {
                        jobDir.mkdirs();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            s_logger.error(
                    "WorkflowEventObserver::notifyWorkflowLocalizedEvent", ex);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_WORKFLOW,
                    null, ex);
        }
        finally
        {
            try
            {
                // send a JMS Message so that task tuv deleter can delete
                // asynchronously
                // TaskTuvDeleter.deleteTaskTuvs(p_workflow.getId());
                HashMap map = new HashMap();
                map.put(CompanyWrapper.CURRENT_COMPANY_ID,
                        String.valueOf(p_workflow.getCompanyId()));
                map.put("command", "DeleteTaskTuvs");
                map.put("workflowId", p_workflow.getIdAsLong());
                JmsHelper.sendMessageToQueue(map,
                        JmsHelper.JMS_TRASH_COMPACTION_QUEUE);
            }
            catch (Exception ex)
            {
                s_logger.error("Unable to delete task tuvs", ex);
            }
        }
    }

    private void possiblyUpdateJobForExport(Workflow p_wf, String p_wfState)
            throws Exception
    {
        Collection workflows = p_wf.getJob().getWorkflows();

        if (checkStateOfWorkflows(workflows, p_wfState))
        {
            JobImpl jobClone = (JobImpl) p_wf.getJob();
            
            if (Job.EXPORTED.equals(p_wfState))
            {
            	possibllyCompleteBlaiseEntry(jobClone);
            }

            jobClone.setState(p_wfState);
            HibernateUtil.update(jobClone);

            // update the source page and the TUVs since the Job is being
            // updated
            getPageEventObserver().notifyAllSourcePagesExportedEvent(
                    jobClone.getSourcePages());

            // delete in-progress TM data for the job
            deleteInProgressTmData(jobClone);

            if (p_wfState.equals(Job.EXPORTED))
            {
                File diExportedDir = AmbFileStoragePathUtils
                        .getDesktopIconExportedDir(p_wf.getCompanyId());
                File jobDir = new File(diExportedDir, String.valueOf(p_wf
                        .getJob().getId()));
                if (!jobDir.exists())
                {
                    jobDir.mkdirs();
                }
            }
        }
    }

    /**
	 * If current job is a Blaise job, and it has been in EXPORTED state, invoke
	 * Blaise API to complete it.
	 * 
	 * @param job
	 */
	private void possibllyCompleteBlaiseEntry(JobImpl job)
    {
		BlaiseConnectorJob bcj = BlaiseManager
				.getBlaiseConnectorJobByJobId(job.getJobId());
		try
        {
        	if (bcj != null)
        	{
				BlaiseConnector blc = BlaiseManager.getBlaiseConnectorById(bcj
						.getBlaiseConnectorId());
				if (blc != null) {
	                BlaiseHelper helper = new BlaiseHelper(blc);
					// If this entry has been completed, it will be removed from
					// Blaise inbox entries, this will throw "object with id xxx
					// not found" exception. Ignore this exception.
	                helper.complete(bcj.getBlaiseEntryId(), job.getId());
				}
            }
        }
        catch (Exception ignore)
        {
			s_logger.warn("Error when possiblly complete entry: "
					+ bcj.getBlaiseEntryId() + ", " + ignore.getMessage());
        }
    }

	private boolean checkStateOfWorkflows(Collection p_workflows, String p_state)
    {
        boolean result = false;
        int i = 0;
        int size = p_workflows.size();
        for (Iterator it = p_workflows.iterator(); it.hasNext();)
        {
            Workflow wf = (Workflow) it.next();
            if (WorkflowTypeConstants.TYPE_DTP.equals(wf.getWorkflowType()))
            {
                size--;
                continue;
            }
            String wfState = wf.getState();

            if (wfState.equals(Workflow.EXPORTED)
                    || wfState.equals(Workflow.ARCHIVED)
                    || wfState.equals(Workflow.CANCELLED)
                    || wfState.equals(Workflow.IMPORT_FAILED))
            {
                i++;
            }
        }

        if (i == size)
        {
            result = true;
        }

        return result;
    }

    /**
     * This method is set to state of workflow to be in EXPORTED_FAILED state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException
     *             , RemoteException
     */
    public void notifyWorkflowExportFailedEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException
    {
        // Even if one workflow fails the state of the job must be set
        // to export_fail.
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            if (p_workflow.getId() < 1)
            {
                session.save(p_workflow);
            }
            else
            {
                p_workflow = (WorkflowImpl) session.get(WorkflowImpl.class,
                        p_workflow.getIdAsLong());
            }

            JobImpl jobClone = (JobImpl) p_workflow.getJob();
            p_workflow.setState(Workflow.EXPORT_FAILED);
            jobClone.setState(Workflow.EXPORT_FAILED);

            if (workflowsHaveState(jobClone.getWorkflows(),
                    Workflow.EXPORT_FAILED))
            {
                Collection sourcePages = jobClone.getSourcePages();

                for (Iterator it = sourcePages.iterator(); it.hasNext();)
                {
                    SourcePage sp = (SourcePage) it.next();
                    sp.setPageState(PageState.EXPORT_FAIL);
                }
            }

            session.update(p_workflow);
            transaction.commit();
            // delete in-progress TM data for the job
            deleteInProgressTmData(jobClone);
        }
        catch (Exception je)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            s_logger.error(
                    "WorkflowEventObserver::notifyWorkflowExportFailedEvent",
                    je);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_EXPORT_WORKFLOW,
                    null, je);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * This method is set to state of workflow to be in ARCHIVE state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException
     *             , RemoteException
     */
    public void notifyWorkflowArchiveEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException
    {
        try
        {
            p_workflow.setState(Workflow.ARCHIVED);
            WorkflowPersistenceAccessor.updateWorkflowState(p_workflow);

            Collection workflows = getWorkflows(p_workflow);
            if (workflowsHaveState(workflows, Workflow.ARCHIVED))
            {
                getJobEventObserver()
                        .notifyJobArchiveEvent(p_workflow.getJob());
            }
        }
        catch (Exception je)
        {
            s_logger.error("WorkflowEventObserver::notifyWorkflowArchiveEvent",
                    je);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_ARCHIVE_WORKFLOW,
                    null, je);
        }
    }

    private Collection<Workflow> getWorkflows(Workflow p_workflow)
    {
    	return p_workflow.getJob().getWorkflows();
    }

    private JobEventObserverWLRemote getJobEventObserver() throws Exception
    {
        return ServerProxy.getJobEventObserver();
    }

    private PageEventObserverWLRemote getPageEventObserver() throws Exception
    {
        return ServerProxy.getPageEventObserver();
    }

    private void deleteInProgressTmData(Job p_job)
            throws WorkflowManagerException
    {
        try
        {
            InProgressTmManager mgr = LingServerProxy.getInProgressTmManager();
            mgr.deleteSegments(p_job.getId());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new WorkflowManagerException(e);
        }
    }
}
