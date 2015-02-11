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
package com.globalsight.everest.projecthandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.l10nprofile.WorkflowTemplateInfoDescriptorModifier;
import com.globalsight.everest.persistence.workflow.WorkflowDescriptorModifier;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
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
 * This is a listener that's invoked via JMS and is responsible for updating all
 * existing workflow instances and templates. The update process performed by
 * this listener happens after the user changes the project manager from project
 * UI. After updating the PM, all of the workflow instances and templates would
 * have the new PM as the owner. At the end of this process, an email is sent to
 * the project modifier and the newly selected project manager to inform them of
 * the update result. Note that a list of ids for both workflow templates and
 * instances that possibly failed to get updated would be part of the email.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_PROJECT_UPDATE_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class ProjectUpdateMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = 0L;
    private List m_failedInstances = new ArrayList();
    private List m_failedTemplates = new ArrayList();
    private String m_companyIdStr = null;

    // used for logging errors
    private static final Logger s_category = Logger
            .getLogger(ProjectUpdateMDB.class.getName());

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * ProjectUpdateMDB default constructor.
     */
    public ProjectUpdateMDB()
    {
        super(s_category);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: MessageListener Implementation
    // ////////////////////////////////////////////////////////////////////
    /**
     * Start the scheduling related process as a separate thread. This method is
     * not a public API and is ONLY invoked by it's consumer for updating the
     * workflow instances/templates (if the PM has changed). Upon an exception,
     * a notification will be sent to the modifier.
     * 
     * @param p_message
     *            - The message to be passed.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_message)
    {
        ProjectUpdateMessage msg = null;
        try
        {
            msg = (ProjectUpdateMessage) ((ObjectMessage) p_message)
                    .getObject();

            Project project = ServerProxy.getProjectHandler().getProjectById(
                    msg.getProjectId().longValue());
            m_companyIdStr = String.valueOf(project.getCompanyId());
            CompanyThreadLocal.getInstance().setIdValue(m_companyIdStr);

            performUpdateProcess(msg);
        }
        catch (Exception e)
        {
            s_category.error("Failed to update workflow instances "
                    + "and templates: " + e);
            if (msg != null)
            {
                try
                {
                    String[] messageArguments = new String[3];
                    messageArguments[0] = msg.getProjectId().toString();
                    messageArguments[1] = UserUtil.getUserNameById(msg
                            .getCurrentProjectManager());
                    messageArguments[2] = e.toString();
                    notifyUser(messageArguments, msg.getProjectModifier(),
                            MailerConstants.WF_PM_CHANGE_FAILED_SUBJECT,
                            "message_wfUpdateFailed");
                }
                catch (Exception ex)
                {
                    s_category.error("Failed to notify user that no workflow "
                            + "templates/instances got updated" + e);
                }
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: MessageListener Implementation
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////
    /*
     * Assign the new workflow instance owners.
     * 
     * @param p_workflowId - The id of the workflow that the new owners should
     * be assigned. @param p_newPmId - The username of the newly assigned PM.
     * 
     * @param p_newWorkflowOwners - A list of new workflow owners to be
     * assigned.
     */
    private void assignWorkflowInstanceOwners(long p_workflowId,
            String p_newPmId, List p_newWorkflowOwners) throws Exception
    {
        // get all of the owners now and pass the array of ids to iFlow
        int size = p_newWorkflowOwners.size();
        String[] ownerIds = new String[size];
        for (int i = 0; i < size; i++)
        {
            WorkflowOwner wfo = (WorkflowOwner) p_newWorkflowOwners.get(i);
            ownerIds[i] = wfo.getOwnerId();
        }
        // update the process name, and owners
        ServerProxy.getWorkflowServer().reassignWorkflowOwners(p_workflowId,
                p_newPmId, ownerIds);
    }

    /*
     * Update and replace the owner of the workflow to be the newly selected
     * project manager. If the process fails, add the workflow id to the list
     * that would be included in the email notification.
     */
    private void changeProjectManagerOfWfInstance(Workflow p_wf,
            String p_newPmId)
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            Workflow wfClone = (Workflow) session.get(Workflow.class,
                    p_wf.getIdAsLong());

            List previousOnwers = wfClone.getWorkflowOwners();
            int listSize = previousOnwers.size();

            // first delete the PM (previous PM)
            boolean isPm = false;
            for (int i = 0; (!isPm && i < listSize); i++)
            {
                WorkflowOwner owner = (WorkflowOwner) previousOnwers.get(i);
                isPm = Permission.GROUP_PROJECT_MANAGER.equals(owner
                        .getOwnerType());

                if (isPm)
                {
                    wfClone.removeWorkflowOwner(owner);
                    session.delete(owner);
                }
            }

            // now add the newly assigned project manager
            wfClone.addWorkflowOwner(new WorkflowOwner(p_newPmId,
                    Permission.GROUP_PROJECT_MANAGER));

            // set the new owners in the jbpm's process instance
            assignWorkflowInstanceOwners(p_wf.getId(), p_newPmId,
                    wfClone.getWorkflowOwners());

            session.update(wfClone);

            // commit the uow
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            // keep in list for email notification...
            m_failedInstances.add(p_wf.getIdAsLong());
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /*
     * Change the project manager as the main owner of the workflow template to
     * be the new PM associated with the updated project.
     */
    private void changeProjectManagerOfWfTemplate(
            WorkflowTemplateInfo p_wfTemplateInfo)
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            long oldTemplateId = p_wfTemplateInfo.getWorkflowTemplateId();

            WorkflowTemplateInfo clone = (WorkflowTemplateInfo) session.get(
                    WorkflowTemplateInfo.class, p_wfTemplateInfo.getIdAsLong());

            WorkflowTemplate template = ServerProxy.getWorkflowServer()
                    .getWorkflowTemplateById(oldTemplateId);

            String[] wfMgrIds = new String[clone.getWorkflowManagerIds().size()];
            wfMgrIds = (String[]) clone.getWorkflowManagerIds().toArray(
                    wfMgrIds);

            // modify the template by setting new PM
            WorkflowTemplate wft = ServerProxy.getWorkflowServer()
                    .modifyWorkflowTemplate(
                            template,
                            new WorkflowOwners(clone.getProjectManagerId(),
                                    wfMgrIds));

            // set the object with the created id (will set the id within wfti).
            clone.setWorkflowTemplate(wft);
            // commit the uow
            transaction.commit();

            // now try removing the old template (to cleanup template table)
            /*
             * We cannot remove the template in jbpm implementation.
             * removeWorkflowTemplate(oldTemplateId);
             */
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            // keep in list for email notification...
            m_failedTemplates.add(p_wfTemplateInfo.getIdAsLong());
        }
        finally
        {
            if (session != null)
            {
                // //session.close();
            }
        }
    }

    /*
     * Notify the project modifier and the new PM about the state of the update
     * (both instances and templates).
     */
    private void notifyEndOfProcess(Long p_projectId, String p_modifier,
            String p_newPm)
    {
        String[] messageArguments = new String[4];
        messageArguments[0] = p_projectId.toString();
        messageArguments[1] = UserUtil.getUserNameById(p_newPm);
        messageArguments[2] = m_failedInstances.toString();
        messageArguments[3] = m_failedTemplates.toString();

        // notify the modifier about the completion process
        notifyUser(messageArguments, p_modifier,
                MailerConstants.WF_PM_CHANGE_COMPLETED_SUBJECT,
                "message_wfUpdateCompleted");

        // notify the new PM about new responsibilities
        notifyUser(messageArguments, p_newPm,
                MailerConstants.WF_PM_CHANGE_COMPLETED_SUBJECT,
                "message_wfUpdateCompleted");

    }

    /*
     * Send email notification to the user based on the given info.
     */
    private void notifyUser(String[] p_messageArgs, String p_recipientUserId,
            String p_subjectKey, String p_messageKey)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }
        try
        {
            // notify the modifier about the completion process
            EmailInformation emailInfo = ServerProxy.getUserManager()
                    .getEmailInformationForUser(p_recipientUserId);

            ServerProxy.getMailer().sendMail((EmailInformation) null,
                    emailInfo, p_subjectKey, p_messageKey, p_messageArgs,
                    m_companyIdStr);
        }
        catch (Exception e)
        {
            s_category.error(
                    "Failed to notify recipient: " + p_recipientUserId, e);
        }

    }

    /*
     * Perform the update process by updating the workflow instances and then
     * templates. Finally notify the modifier and the new PM.
     */
    private void performUpdateProcess(ProjectUpdateMessage p_message)
            throws Exception
    {
        String modifier = p_message.getProjectModifier();
        String originalPm = p_message.getPreviousProjectManager();
        String newPm = p_message.getCurrentProjectManager();
        Long projectId = p_message.getProjectId();

        // update workflow instances
        updateWorkflowInstances(originalPm, newPm, projectId);

        // update the workflow templates
        updateWorkflowTemplates(originalPm, newPm, projectId);

        // now notify the modifier and new PM...
        notifyEndOfProcess(projectId, modifier, newPm);
    }

    /*
     * Update the existing workflow instances.
     */
    private void updateWorkflowInstances(String p_originalPm, String p_newPm,
            Long p_projectId) throws Exception
    {
        Map map = new HashMap();
        map.put("projectId", p_projectId);
        map.put("pmUserId", p_originalPm);

        String sql = WorkflowDescriptorModifier.WORKFLOW_BY_OWNER_AND_TYPE_SQL;
        Collection c = HibernateUtil
                .searchWithSql(sql, map, WorkflowImpl.class);

        if (c == null)
        {
            s_category.info("no workflow instances to be updated for new PM: "
                    + UserUtil.getUserNameById(p_newPm));
            return;
        }

        Object[] wfs = c.toArray();
        // loop thru them and update one by one
        for (int i = 0; i < wfs.length; i++)
        {
            Workflow wf = (Workflow) wfs[i];
            changeProjectManagerOfWfInstance(wf, p_newPm);
        }
    }

    /*
     * Update the existing workflow instances.
     */
    private void updateWorkflowTemplates(String p_originalPm, String p_newPm,
            Long p_projectId) throws Exception
    {
        Map map = new HashMap();
        map.put("projectId", p_projectId);

        String sql = WorkflowTemplateInfoDescriptorModifier.TEMPLATE_BY_PROJECT_ID_SQL;
        Collection c = HibernateUtil.searchWithSql(sql, map,
                WorkflowTemplateInfo.class);

        if (c == null)
        {
            s_category.info("no workflow templates to be updated for new PM: "
                    + UserUtil.getUserNameById(p_newPm));
            return;
        }

        Object[] wfTemplateInfos = c.toArray();
        // loop thru them and update one by one
        for (int i = 0; i < wfTemplateInfos.length; i++)
        {
            WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) wfTemplateInfos[i];
            changeProjectManagerOfWfTemplate(wfti);
        }
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////
}
