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

package com.globalsight.everest.secondarytargetfile;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.SystemAction;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * The SecondaryTargetFileMgrLocal class provides an implementation of the
 * SecondaryTargetFileMgr.
 * <p>
 * It's responsible for all type of actions that are related to
 * SecondaryTargetFile object (i.e. creation, update, and removal).
 * 
 */
public final class SecondaryTargetFileMgrLocal implements
        SecondaryTargetFileMgr
{
    // for logging purposes
    private static final Logger s_category = Logger
            .getLogger(SecondaryTargetFileMgrLocal.class.getName());

    // object used to lock for synchronizing the update state process
    private Boolean m_stateUpdateLock = Boolean.TRUE;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constructor - construct an instance of SecondaryTargetFileMgrLocal.
     */
    public SecondaryTargetFileMgrLocal()
    {
        super();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: SecondaryTargetFileMgr Implementation
    // ////////////////////////////////////////////////////////////////////
    /**
     * @see SecondaryTargetFileMgr.createSecondaryTargetFile
     */
    public void createSecondaryTargetFile(String p_absolutePath,
            String p_relativePath, int p_sourcePageBomType, String p_eventFlowXml, long p_exportBatchId)
            throws SecondaryTargetFileException, RemoteException
    {
        ExportBatchEvent ebe = null;
        try
        {
            ebe = ServerProxy.getExportEventObserver().getExportBatchEventById(
                    p_exportBatchId, false);

            // since STF is created per workflow, there would be one
            // workflow id for an export batch event.
            Long wfId = (Long) ebe.getWorkflowIds().get(0);

            // a light workflow object without iFlow related attributes
            Workflow wf = (Workflow) HibernateUtil
                    .get(WorkflowImpl.class, wfId);

            String storagePath = constructRelativePath(p_relativePath, wf
                    .getJob().getId(), wfId);

            SecondaryTargetFile existingStf = getStfByWfIdAndPath(wfId,
                    storagePath);

            boolean replace = existingStf != null
                    && shouldReplaceExistingStf(wfId,ebe.getTaskId().longValue());

            SecondaryTargetFile stf = null;
            if (existingStf == null || replace)
            {
                // instantiate the STF object and set its values (need to
                // append jobId\wfId as a prefix of the relative path)
                stf = replace ? existingStf : createSecondaryTargetFile(ebe,
                        p_relativePath, p_eventFlowXml, wfId);
                if (!replace)
                {
                    stf.setWorkflow(wf);
                }

                // rename the temp file to the name given by relative path
                stf = ServerProxy.getNativeFileManager().moveFileToStorage(
                        p_absolutePath, stf, p_sourcePageBomType);
            }

            // persist the STF (for new STF only)
            if (existingStf == null)
            {
                persistStf(stf, wf);
            }
        }
        catch (Exception e)
        {
            try
            {
                // update task's stf_creation_state to 'failed'
                ServerProxy.getTaskManager().updateStfCreationState(
                        ebe.getTaskId().longValue(), Task.FAILED);
            }
            catch (Exception ex)
            {
                s_category.error(
                        "createSTF :: failed to update stf_creation_state of"
                                + " task for export event batch " + ebe, ex);
            }
            throw new SecondaryTargetFileException(e);
        }
    }

    /**
     * @see SecondaryTargetFileMgr.failedToCreateSecondaryTargetFile(long)
     */
    public void failedToCreateSecondaryTargetFile(long p_exportBatchId)
            throws SecondaryTargetFileException, RemoteException
    {
        try
        {
            ExportBatchEvent ebe = ServerProxy.getExportEventObserver()
                    .getExportBatchEventById(p_exportBatchId, false);

            // update task's stf_creation_state to 'failed'
            ServerProxy.getTaskManager().updateStfCreationState(
                    ebe.getTaskId().longValue(), Task.FAILED);
        }
        catch (Exception ex)
        {
            throw new SecondaryTargetFileException(ex);
        }
    }

    /**
     * @see SecondaryTargetFileMgr.getSecondaryTargetFile(long)
     */
    public SecondaryTargetFile getSecondaryTargetFile(long p_stfId)
            throws SecondaryTargetFileException, RemoteException
    {
        return getStfById(new Long(p_stfId));
    }

    /**
     * @see SecondaryTargetFileMgr.notifyExportFailEvent(p_stfId);
     */
    public void notifyExportFailEvent(Long p_stfId)
            throws SecondaryTargetFileException, RemoteException
    {
        SecondaryTargetFile stf = getStfById(p_stfId);
        Workflow wf = stf.getWorkflow();

        // an interim export can happen during dispatch without
        // updating any states.
        if (!Workflow.DISPATCHED.equals(wf.getState()))
        {
            stf = updateState(p_stfId, SecondaryTargetFileState.EXPORT_FAIL);

            notifyWorkflowExportFailedEvent(p_stfId, stf.getWorkflow());
        }
    }

    /**
     * @see SecondaryTargetFileMgr.notifyExportSuccessEvent(Long)
     */
    public void notifyExportSuccessEvent(Long p_stfId)
            throws SecondaryTargetFileException, RemoteException
    {
        SecondaryTargetFile stf = null;
        // Sychronize this block of code since more than one stf may come
        // through at the same time. This will ensure the state update
        // performed one at a time which results in valid state during
        // isExported call for notifying workflow observer.
        synchronized (m_stateUpdateLock)
        {
            stf = updateState(p_stfId, SecondaryTargetFileState.EXPORTED);
        }

        notifyWorkflowExportedEvent(p_stfId, stf.getWorkflow());
    }

    /**
     * @see SecondaryTargetFileMgr.removeSecondaryTargetFile(SecondaryTargetFile)
     */
    public void removeSecondaryTargetFile(SecondaryTargetFile p_stf)
            throws SecondaryTargetFileException, RemoteException
    {
        try
        {
            p_stf.deactivate();
            HibernateUtil.saveOrUpdate(p_stf);
            return;
        }
        catch (Exception e)
        {
            String[] args = { String.valueOf(p_stf.getId()) };
            throw new SecondaryTargetFileException(
                    SecondaryTargetFileException.MSG_FAILED_TO_DELETE_STF,
                    args, e);
        }
    }

    /**
     * @see SecondaryTargetFileMgr.updateSecondaryTargetFile(SecondaryTargetFile)
     */
    public void updateSecondaryTargetFile(SecondaryTargetFile p_stf)
            throws SecondaryTargetFileException, RemoteException
    {
        try
        {
            HibernateUtil.saveOrUpdate(p_stf);
        }
        catch (Exception e)
        {
            String[] args = { String.valueOf(p_stf.getId()) };
            throw new SecondaryTargetFileException(
                    SecondaryTargetFileException.MSG_FAILED_TO_UPDATE_STF,
                    args, e);
        }
    }

    /**
     * @see SecondaryTargetFileMgr.updateState(SecondaryTargetFile, String)
     */
    public SecondaryTargetFile updateState(Long p_stfId, String p_state)
            throws SecondaryTargetFileException, RemoteException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        try
        {
            SecondaryTargetFile stf = (SecondaryTargetFile) session.get(
                    SecondaryTargetFile.class, p_stfId);

            stf.setState(p_state);
            session.update(stf);

            tx.commit();

            return stf;
        }
        catch (Exception e)
        {
            String[] args = { String.valueOf(p_stfId) };
            throw new SecondaryTargetFileException(
                    SecondaryTargetFileException.MSG_FAILED_TO_UPDATE_STF,
                    args, e);
        }
        finally
        {
            //session.close();
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: SecondaryTargetFileMgr Implementation
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Methods
    // ////////////////////////////////////////////////////////////////////

    // get a SecondaryTargetFile object by id
    private SecondaryTargetFile getStfById(Long p_id)
            throws SecondaryTargetFileException
    {
        try
        {
            return (SecondaryTargetFile) HibernateUtil.get(
                    SecondaryTargetFile.class, p_id);
        }
        catch (Exception e)
        {
            String args[] = { String.valueOf(p_id) };
            throw new SecondaryTargetFileException(
                    SecondaryTargetFileException.MSG_FAILED_TO_GET_STF, args, e);
        }
    }

    // get a SecondaryTargetFile object by wf id and storage path
    private SecondaryTargetFile getStfByWfIdAndPath(Long p_id,
            String p_storagePath)
    {
        String hql = " from SecondaryTargetFile s"
                + " where s.workflow.id = :WF_ID and s.storagePath = :STORAGE_PATH ";

        Session session = HibernateUtil.getSession();

        try
        {
            return (SecondaryTargetFile) session.createQuery(hql).setLong(
                    "WF_ID", p_id.longValue()).setString("STORAGE_PATH",
                    p_storagePath).list().get(0);
        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            //session.close();
        }
    }

    // persist the secondary target file.
    private void persistStf(SecondaryTargetFile p_stf, Workflow p_workflow)
            throws SecondaryTargetFileException
    {
        try
        {
            if (!stfExists(p_workflow, p_stf))
            {
                p_workflow.addSecondaryTargetFile(p_stf);
                HibernateUtil.saveOrUpdate(p_workflow);
                HibernateUtil.saveOrUpdate(p_stf);
            }
        }
        catch (Exception e)
        {
            throw new SecondaryTargetFileException(
                    SecondaryTargetFileException.MSG_FAILED_TO_CREATE_STF,
                    null, e);
        }
    }

    // create a new STF object
    private SecondaryTargetFile createSecondaryTargetFile(
            ExportBatchEvent p_exportBatchEvent, String p_relativePath,
            String p_eventFlowXml, Long p_wfId)
            throws SecondaryTargetFileException
    {
        try
        {
            String storagePath = constructRelativePath(p_relativePath,
                    p_exportBatchEvent.getJob().getId(), p_wfId);
            // new instance of STF without setting workflow back-pointer
            SecondaryTargetFile stf = new SecondaryTargetFile(p_eventFlowXml,
                    p_exportBatchEvent.getResponsibleUserId(),
                    SecondaryTargetFileState.ACTIVE_JOB, storagePath);

            return stf;
        }
        catch (Exception e)
        {
            String args[] = { String.valueOf(p_wfId) };
            throw new SecondaryTargetFileException(
                    SecondaryTargetFileException.MSG_FAILED_TO_GET_WF, args, e);
        }
    }

    /*
     * Check to see if all stfs of a workflow have been exported...
     */
    private boolean isExported(Set<SecondaryTargetFile> p_stfs)
    {
        for (SecondaryTargetFile stf : p_stfs)
        {
            if (!SecondaryTargetFileState.EXPORTED.equals(stf.getState()))
            {
                return false;
            }
        }

        return true;
    }

    /*
     * First populates the TM and then notifies the page event observer to
     * update the page, and tuvs state and finally update the workflow state to
     * exported (once all target pages are set to exported).
     */
    private void notifyWorkflowExportedEvent(Long p_stfId, Workflow p_workflow)
            throws SecondaryTargetFileException
    {
        try
        {
            // Only populate TM when ALL STFs are in exported state
            if (isExported(p_workflow.getSecondaryTargetFiles()))
            {
                s_category.debug("All STFs of workflow with id "
                        + p_workflow.getId()
                        + "have been exported.  Now populate TM.");

                populateTm(p_workflow);
            }
        }
        catch (Exception e)
        {
            long wfId = p_workflow == null ? -1 : p_workflow.getId();
            String args[] = { String.valueOf(p_stfId), String.valueOf(wfId) };

            throw new SecondaryTargetFileException(
                    SecondaryTargetFileException.MSG_FAILED_TO_UPDATE_WF_STATE,
                    args, e);
        }
    }

    /*
     * Notifies the workflow event observer if an stf fails to export. This is
     * only called for a final export.
     */
    private void notifyWorkflowExportFailedEvent(Long p_stfId,
            Workflow p_workflow) throws SecondaryTargetFileException
    {
        try
        {
            ServerProxy.getWorkflowEventObserver()
                    .notifyWorkflowExportFailedEvent(p_workflow);
        }
        catch (Exception e)
        {
            long wfId = p_workflow == null ? -1 : p_workflow.getId();
            String args[] = { String.valueOf(p_stfId), String.valueOf(wfId) };

            throw new SecondaryTargetFileException(
                    SecondaryTargetFileException.MSG_FAILED_TO_UPDATE_WF_STATE,
                    args, e);
        }
    }

    /*
     * Populate TM once all STFs of a workflow are exported (since there's not a
     * direct relationship between stf and target page(s)).
     */
    private void populateTm(Workflow p_workflow) throws Exception
    {
        L10nProfile l10nProfile = p_workflow.getJob().getL10nProfile();

        LeveragingLocales leveragingLocales = l10nProfile
                .getLeveragingLocales();

        TranslationMemoryProfile tmProfile = l10nProfile
                .getTranslationMemoryProfile();

        LeverageOptions leverageOptions = new LeverageOptions(tmProfile,
                leveragingLocales);

        TmCoreManager tmCoreManager = LingServerProxy.getTmCoreManager();

        List targetPages = p_workflow.getTargetPages();
        long jobId = p_workflow.getJob().getId();
        int size = targetPages.size();

        for (int i = 0; i < size; i++)
        {
            TargetPage tp = (TargetPage) targetPages.get(i);

            tmCoreManager.populatePageByLocale(tp.getSourcePage(),
                    leverageOptions, tp.getGlobalSightLocale(), jobId);

            // also update page state along with its tuvs. Note that
            // workflow state will also be set to exported in this method.
            ServerProxy.getPageEventObserver().notifyExportSuccessEvent(tp);
        }
    }

    /*
     * Construct a relative path by appending the job id and workflow id to the
     * current path.
     */
    private String constructRelativePath(String p_relativePath, long p_jobId,
            Long p_wfId)
    {
        // make sure there are no slashed
        if (p_relativePath.startsWith("/") || p_relativePath.startsWith("\\"))
        {
            p_relativePath = p_relativePath.substring(1, p_relativePath
                    .length());
        }

        // append job id and workflow id to the beginning of the relative path
        StringBuffer sb = new StringBuffer();
        sb.append(p_jobId);
        sb.append(File.separator);
        sb.append(p_wfId);
        sb.append(File.separator);
        sb.append(p_relativePath);

        return sb.toString();
    }

    /**
     * Determines whether the existing STF (if any) should be replaced by a
     * newly created one.
     */
    private boolean shouldReplaceExistingStf(long p_workflowInstanceId, long p_taskId) throws Exception
    {
        WfTaskInfo wfti = ServerProxy.getWorkflowServer().getWorkflowTaskInfo(p_workflowInstanceId,
                p_taskId);

        String actionType = wfti.getActionType();

        return actionType != null && actionType.equals(SystemAction.RSTF);
    }

    /*
     * Make sure no duplicate stfs are created. This could happen when the first
     * time creation of stfs fails after a few have been created. Therefore, we
     * need to check here before adding the same file to the workflow.
     */
    private boolean stfExists(Workflow p_wf, SecondaryTargetFile p_stf)
    {
        boolean exists = false;

        for (SecondaryTargetFile stf : p_wf.getSecondaryTargetFiles())
        {
            exists = stf.getStoragePath().equals(p_stf.getStoragePath());
            if (exists)
            {
                break;
            }
        }

        return exists;
    }

    // ////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////
}
