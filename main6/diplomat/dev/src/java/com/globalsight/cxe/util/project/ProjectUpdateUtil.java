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
package com.globalsight.cxe.util.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.l10nprofile.WorkflowTemplateInfoDescriptorModifier;
import com.globalsight.everest.persistence.workflow.WorkflowDescriptorModifier;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.WorkflowOwners;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowOwner;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.mail.MailerConstants;

/**
 * Class {@code ProjectUpdateUtil} is used for updating all existing workflow
 * instances and templates without using JMS.
 * 
 * @since GBS-4400
 */
public class ProjectUpdateUtil
{
    static private final Logger logger = Logger.getLogger(ProjectUpdateUtil.class);

    private static boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    /**
     * Performs the update process on workflow instances and templates
     * asynchronously with thread instead of JMS.
     */
    static public void performUpdateProcessWithThread(Map<String, Object> data)
    {
        ProjectUpdateRunnable runnable = new ProjectUpdateRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Performs the update process on workflow instances and templates
     * synchronously.
     */
    static public void performUpdateProcess(Map<String, Object> p_data)
    {
        String modifierId = (String) p_data.get("modifierId");
        String originalPm = (String) p_data.get("originalPm");
        String newPm = (String) p_data.get("newPm");
        long projectId = (Long) p_data.get("projectId");
        long companyId = (Long) p_data.get("companyId");

        try
        {
            List<Long> failedWfInstances = new ArrayList<Long>();
            // update workflow instances
            updateWorkflowInstances(originalPm, newPm, projectId, failedWfInstances);

            List<Long> failedWfTemplates = new ArrayList<Long>();
            // update the workflow templates
            updateWorkflowTemplates(originalPm, newPm, projectId, failedWfTemplates);

            // now notify the modifier and new PM
            notifyEndOfProcess(projectId, modifierId, newPm, companyId, failedWfInstances,
                    failedWfTemplates);
        }
        catch (Exception e)
        {
            logger.error("Failed to update workflow instances and templates.", e);
            try
            {
                String[] messageArguments = new String[3];
                messageArguments[0] = String.valueOf(projectId);
                messageArguments[1] = UserUtil.getUserNameById(newPm);
                messageArguments[2] = e.toString();
                notifyUser(messageArguments, modifierId, companyId,
                        MailerConstants.WF_PM_CHANGE_FAILED_SUBJECT, "message_wfUpdateFailed");
            }
            catch (Exception ex)
            {
                logger.error(
                        "Failed to notify user that no workflow templates or instances got updated.",
                        ex);
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Notifies the project modifier and the new PM about the state of the
     * update (both instances and templates).
     */
    private static void notifyEndOfProcess(long p_projectId, String p_modifier, String p_newPm,
            long p_companyId, List<Long> p_failedWfInstances, List<Long> p_failedWfTemplates)
    {
        String[] messageArguments = new String[4];
        messageArguments[0] = String.valueOf(p_projectId);
        messageArguments[1] = UserUtil.getUserNameById(p_newPm);
        messageArguments[2] = p_failedWfInstances.toString();
        messageArguments[3] = p_failedWfTemplates.toString();

        // notify the modifier about the completion process
        notifyUser(messageArguments, p_modifier, p_companyId,
                MailerConstants.WF_PM_CHANGE_COMPLETED_SUBJECT, "message_wfUpdateCompleted");

        // notify the new PM about new responsibilities
        notifyUser(messageArguments, p_newPm, p_companyId,
                MailerConstants.WF_PM_CHANGE_COMPLETED_SUBJECT, "message_wfUpdateCompleted");

    }

    /**
     * Updates the existing workflow instances.
     */
    private static void updateWorkflowInstances(String p_originalPm, String p_newPm,
            long p_projectId, List<Long> p_failedWfInstances) throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("projectId", p_projectId);
        map.put("pmUserId", p_originalPm);

        String sql = WorkflowDescriptorModifier.WORKFLOW_BY_OWNER_AND_TYPE_SQL;
        List<WorkflowImpl> wfs = HibernateUtil.searchWithSql(sql, map, WorkflowImpl.class);

        if (wfs == null)
        {
            logger.info("There are no workflow instances to be updated for new PM: "
                    + UserUtil.getUserNameById(p_newPm));
            return;
        }
        for (WorkflowImpl wf : wfs)
        {
            changeProjectManagerOfWfInstance(wf, p_newPm, p_failedWfInstances);
        }
    }

    /**
     * Updates the existing workflow templates.
     */
    private static void updateWorkflowTemplates(String p_originalPm, String p_newPm,
            long p_projectId, List<Long> p_failedWfTemplates) throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("projectId", p_projectId);

        String sql = WorkflowTemplateInfoDescriptorModifier.TEMPLATE_BY_PROJECT_ID_SQL;
        List<WorkflowTemplateInfo> wtis = HibernateUtil.searchWithSql(sql, map,
                WorkflowTemplateInfo.class);

        if (wtis == null)
        {
            logger.info("There are no workflow templates to be updated for new PM: "
                    + UserUtil.getUserNameById(p_newPm));
            return;
        }
        for (WorkflowTemplateInfo wti : wtis)
        {
            changeProjectManagerOfWfTemplate(wti, p_failedWfTemplates);
        }
    }

    private static void changeProjectManagerOfWfInstance(Workflow p_wf, String p_newPmId,
            List<Long> p_failedWfInstances) throws Exception
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Workflow wfClone = (Workflow) session.get(Workflow.class, p_wf.getIdAsLong());
            List<WorkflowOwner> previousOnwers = wfClone.getWorkflowOwners();
            int listSize = previousOnwers.size();
            // first delete the PM (previous PM)
            boolean isPm = false;
            for (int i = 0; (!isPm && i < listSize); i++)
            {
                WorkflowOwner owner = (WorkflowOwner) previousOnwers.get(i);
                isPm = Permission.GROUP_PROJECT_MANAGER.equals(owner.getOwnerType());
                if (isPm)
                {
                    wfClone.removeWorkflowOwner(owner);
                    session.delete(owner);
                }
            }
            // now add the newly assigned project manager
            wfClone.addWorkflowOwner(
                    new WorkflowOwner(p_newPmId, Permission.GROUP_PROJECT_MANAGER));

            session.update(wfClone);

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            p_failedWfInstances.add(p_wf.getId());
            throw new Exception(e);
        }
    }

    private static void changeProjectManagerOfWfTemplate(WorkflowTemplateInfo p_wfTemplateInfo,
            List<Long> p_failedWfTemplates) throws Exception
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            long oldTemplateId = p_wfTemplateInfo.getWorkflowTemplateId();
            WorkflowTemplateInfo clone = (WorkflowTemplateInfo) session
                    .get(WorkflowTemplateInfo.class, p_wfTemplateInfo.getIdAsLong());

            WorkflowTemplate template = ServerProxy.getWorkflowServer()
                    .getWorkflowTemplateById(oldTemplateId);

            String[] wfMgrIds = new String[clone.getWorkflowManagerIds().size()];
            wfMgrIds = (String[]) clone.getWorkflowManagerIds().toArray(wfMgrIds);

            // modify the template by setting new PM
            WorkflowTemplate wft = ServerProxy.getWorkflowServer().modifyWorkflowTemplate(template,
                    new WorkflowOwners(clone.getProjectManagerId(), wfMgrIds));

            clone.setWorkflowTemplate(wft);

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            p_failedWfTemplates.add(p_wfTemplateInfo.getId());
            throw new Exception(e);
        }
    }

    private static void notifyUser(String[] p_messageArgs, String p_recipientUserId,
            long p_companyId, String p_subjectKey, String p_messageKey)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }
        try
        {
            EmailInformation emailInfo = ServerProxy.getUserManager()
                    .getEmailInformationForUser(p_recipientUserId);

            ServerProxy.getMailer().sendMail((EmailInformation) null, emailInfo, p_subjectKey,
                    p_messageKey, p_messageArgs, String.valueOf(p_companyId));
        }
        catch (Exception e)
        {
            logger.error("Failed to notify recipient: " + p_recipientUserId, e);
        }

    }
}
