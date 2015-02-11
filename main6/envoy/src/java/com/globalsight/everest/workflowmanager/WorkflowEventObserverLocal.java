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
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.entity.exportlocation.ExportLocation;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.persistence.exportlocation.ExportLocationPersistenceManager;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobEditionInfo;
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
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.terminology.indexer.IIndexManager;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.webservices.client.Ambassador;
import com.globalsight.webservices.client.WebServiceClientHelper;

public class WorkflowEventObserverLocal implements WorkflowEventObserver
{
    static private final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger(WorkflowEventObserverLocal.class);

    /**
     * This method is set to state of workflow to be in PENDING state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException,
     *             RemoteException
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
     * @throws WorkflowManagerException,
     *             RemoteException
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
     * @throws WorkflowManagerException,
     *             RemoteException
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
     * @throws WorkflowManagerException,
     *             RemoteException
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
     * @throws WorkflowManagerException,
     *             RemoteException
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
            possiblyUpdateJobForExport(wfClone, Workflow.EXPORTED);
            
            // re-index the new added entry
            FileProfile fileProfile = wfClone.getJob().getFileProfile();

            if (fileProfile.getTerminologyApproval() == 1)
            {
                String termbaseName = wfClone.getJob().getL10nProfile()
                        .getProject().getTermbaseName();
                Termbase tb = TermbaseList.get(termbaseName);

                try
                {
                    if (!tb.isIndexing())
                    {
                        ITermbaseManager s_manager = ServerProxy
                                .getTermbaseManager();
                        ITermbase itb = s_manager.connect(termbaseName, wfClone
                                .getJob().getL10nProfile().getProject()
                                .getProjectManagerId(), "");
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
                            .getDesktopIconExportedDir();
                    File jobDir = new File(diExportedDir, job.getJobName());
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
                map.put(CompanyWrapper.CURRENT_COMPANY_ID, p_workflow
                        .getCompanyId());
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

    private void possiblyUpdateJobForExport(Workflow p_wf,
            String p_wfState) throws Exception
    {
        Collection workflows = p_wf.getJob().getWorkflows();
        
        if (checkStateOfWorkflows(workflows, p_wfState))
        {
            JobImpl jobClone = (JobImpl) p_wf.getJob();
            
            String jobCompanyName = null;
            try {
            	long jobCompanyId = Long.parseLong(jobClone.getCompanyId());
                jobCompanyName = ServerProxy.getJobHandler().getCompanyById(jobCompanyId).getCompanyName();            	
            } catch (Exception ex) {
            	
            }
            
            //added by Walter, for sending back the job to GS Edition server.
            JobEditionInfo je = getGSEditionJobByJobID(jobClone.getId());
            if(je != null) {
                if(!je.getSendingBackStatus().equals("sending_back_edition_finished")) {
                    ExportLocationPersistenceManager mgr =
                        ServerProxy.getExportLocationPersistenceManager();
                    ExportLocation eLoc = mgr.getDefaultExportLocation();
                    String exportLocation =  eLoc.getLocation();
                    if (jobCompanyName != null && !exportLocation.endsWith(jobCompanyName)) {
                    	exportLocation = exportLocation + File.separator + jobCompanyName;
                    }
                    String wsdl = je.getUrl();
                    Ambassador ambassador = 
                        WebServiceClientHelper.getClientAmbassador(wsdl, je.getUserName(), je.getPassword());
                    
                    String fullAccessToken = 
                        ambassador.login(je.getUserName(), je.getPassword());
                    
                    String realAccessToken = 
                        WebServiceClientHelper.getRealAccessToken(fullAccessToken);
                    
                    sendingBackEditionJob(p_wf, ambassador, realAccessToken, exportLocation, je);
                }
            }
            
            jobClone.setState(p_wfState);
            HibernateUtil.update(jobClone);

            // update the source page and the TUVs since the Job is being
            // updated
            getPageEventObserver().notifyAllSourcePagesExportedEvent(
                    jobClone.getSourcePages());

            // delete in-progress TM data for the job
            deleteInProgressTmData(jobClone);
        }
    }
    
    private JobEditionInfo getGSEditionJobByJobID(long jobID) {
        JobEditionInfo je = new JobEditionInfo();
        
        try {
            String hql = "from JobEditionInfo a where a.jobId = :id";
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("id", Long.toString(jobID));
            Collection servers = HibernateUtil.search(hql, map);
            Iterator i = servers.iterator();
            je = i.hasNext() ? (JobEditionInfo) i.next() : null;
        }
        catch (Exception pe) {
            //s_logger.error("Persistence Exception when retrieving JobEditionInfo", pe);
        }
        
        return je;
    }
    
    private void sendingBackEditionJob(Workflow workflow, 
            Ambassador ambassador, 
            String realAccessToken, 
            String exportLocation,
            JobEditionInfo je) 
    {
        try {
            Iterator iter = workflow.getTargetPages().iterator();
            
            while (iter.hasNext())
            {
                TargetPage tp = (TargetPage) iter.next();
                String exportingFileName = tp.getExternalPageId();
                int index = exportingFileName.indexOf(File.separator);
                exportingFileName = exportingFileName.substring(index + 1);
                exportingFileName =  exportLocation + File.separator +
                          tp.getGlobalSightLocale().getLanguage() + "_" +
                          tp.getGlobalSightLocale().getCountry() + 
                          File.separator + exportingFileName;
                File finalFile = new File(exportingFileName);
                
                if (finalFile.exists() && finalFile.isFile())
                {
                    FileInputStream is = new FileInputStream(finalFile);
                    byte[] bytes = new byte[(int)finalFile.length()];
                    is.read(bytes,0,bytes.length);
                    is.close();
                    
                    String pagename =  
                    exportingFileName.substring(exportingFileName.lastIndexOf(File.separator) + 1);
                    
                    ambassador.uploadEditionFileBack(realAccessToken, 
                                          je.getOriginalTaskId(), 
                                          pagename, bytes);                	
                }
                else
                {
                	String msg = "The final file does not exist or is not a file : " + finalFile.getAbsolutePath();
                	s_logger.error(msg);
                }
            }
            
            ambassador.importOfflineTargetFiles(realAccessToken, je.getOriginalTaskId());
            
            Session HibSession = HibernateUtil.getSession();
            Transaction tx = HibSession.beginTransaction();
            je.setSendingBackStatus("sending_back_edition_finished");
            tx.commit();
        }
        catch(Exception e) {
        	s_logger.error(e);
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
     * @throws WorkflowManagerException,
     *             RemoteException
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
                //session.close();
            }
        }
    }

    /**
     * This method is set to state of workflow to be in CANCELLED state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException,
     *             RemoteException
     */
    public void notifyWorkflowCancelledEvent(Workflow p_workflow)
            throws WorkflowManagerException, RemoteException
    {
        try
        {
            p_workflow.setState(Workflow.CANCELLED);
            WorkflowPersistenceAccessor.updateWorkflowState(p_workflow);
            Collection workflows = getWorkflows(p_workflow);

            getPageEventObserver().notifyWorkflowCancelEvent(
                    p_workflow.getTargetPages());

            if (workflowsHaveState(workflows, Workflow.CANCELLED))
            {
                getJobEventObserver().notifyJobCancelledEvent(
                        p_workflow.getJob());
            }
        }
        catch (Exception je)
        {
            s_logger.error(
                    "WorkflowEventObserver::notifyWorkflowExportFailedEvent",
                    je);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_CANCEL_WORKFLOW,
                    null, je);
        }
    }

    /**
     * This method is set to state of workflow to be in ARCHIVE state.
     * 
     * @param Workflow
     *            p_workflow
     * @throws WorkflowManagerException,
     *             RemoteException
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

    private Collection getWorkflows(Workflow p_workflow)
    {
        Job j = p_workflow.getJob();
        Collection c = j.getWorkflows();
        return c;
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
