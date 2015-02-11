/**
 *  Copyright 2011 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.costing.AmountOfWork;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReviewersCommentsReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReviewersCommentsSimpleReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.TranslationsEditReportGenerator;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowJbpmPersistenceHandler;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.mail.MailerHelper;

public class TaskThread extends MultiCompanySupportedThread
{
    private static final Logger log = Logger.getLogger(TaskThread.class
            .getName());
    public static final String KEY_ACTION = "runAction";
    public static final String ACTION_AUTOACCEPT = "autoAcceptAction";

    private TaskImpl task;
    private Map<String, ?> dataMap;
    public static String roleName;
    
    public TaskThread(TaskImpl p_task, Map<String, ?> p_dataMap)
    {
        super();
        dataMap = p_dataMap;
        task = p_task;
        task.setRatings(new ArrayList(task.getRatings()));
        task.setTaskComments(new ArrayList(task.getTaskComments()));
        task.setWorkSet(new HashSet<AmountOfWork>(task.getWorkSet()));
        task.setTaskTuvs(new HashSet(task.getTaskTuvs()));
    }

    public void run()
    {
        super.run();
        String companyId = CompanyThreadLocal.getInstance().getValue();
        try
        {
            String action = (String) dataMap.get(KEY_ACTION);
            if (ACTION_AUTOACCEPT.equals(action))
            {
                autoAcceptTask(task);
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
        CompanyThreadLocal.getInstance().setIdValue(companyId);
    }

    /**
     * Auto accept the task/activity only when accepter is single and follows
     * the project permission.
     * 
     * @param p_task
     *            Current task
     */
    protected void autoAcceptTask(TaskImpl p_task)
    {
        long companyId = p_task.getCompanyId();
        String companyName = CompanyWrapper.getCompanyNameById(companyId);
        String projectName = p_task.getProjectName();

        ProjectImpl project = null;
        try
        {
            project = (ProjectImpl) ServerProxy.getProjectHandler()
                    .getProjectByNameAndCompanyId(projectName, companyId);
        }
        catch (Exception e)
        {
            log.error("Can't get data in autoAcceptTask.", e);
            return;
        }

        if (project.getReviewOnlyAutoAccept() || project.getAutoAcceptPMTask())
        {
            JbpmContext ctx = null;
            try
            {
                WorkflowTaskInstance wti = (WorkflowTaskInstance) ServerProxy
                        .getWorkflowServer().getWorkflowTaskInstance(
                                p_task.getWorkflow().getId(), p_task.getId());
                ctx = WorkflowConfiguration.getInstance().getJbpmContext();
                ProcessInstance pi = ctx.getProcessInstance(p_task
                        .getWorkflow().getId());
                Node node = WorkflowJbpmUtil.getNodeByWfTask(pi, wti);
                TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                        .getTaskInstance(p_task.getId(), ctx);
                if (taskInstance == null)
                    return;
                String assigneesStr = WorkflowJbpmUtil.getAssignees(
                        taskInstance, null);
                Set<String> assignees = StringUtil.split(assigneesStr);
                if (assignees == null || assignees.size() != 1)
                {
                    return;
                }
                String acceptor = assignees.iterator().next();

                int type = p_task.getType();

                // Activity type is Review Only.
                if (Activity.TYPE_REVIEW == type)
                {
                    if (project.getReviewOnlyAutoAccept()
                            || canAutoAcceptByPM(project, acceptor))
                    {
                        autoAcceptAndSendEmail(type, acceptor, p_task, node,
                                pi, companyId, companyName, project);
                    }
                }
                else
                {
                    if (canAutoAcceptByPM(project, acceptor))
                    {
                        TaskHelper.acceptTask(acceptor, p_task);
                    }
                }
            }
            catch (Exception e)
            {
                log.error("Auto Accept Activity Error.", e);
            }
            finally
            {
                ctx.close();
            }
        }
    }

    /**
     * Justify whether the single available user can accept the task.
     */
    public static boolean canAutoAcceptByPM(ProjectImpl p_project,
            String p_acceptor)
    {
        return p_project.getAutoAcceptPMTask()
                && p_project.getProjectManagerId().equals(p_acceptor);
    }

    /**
     * Auto-accept the task and Send auto accepted Email.
     * 
     * @param p_activityType
     *            Task type which used for generating report.
     */
    public static void autoAcceptAndSendEmail(int p_activityType,
            String p_acceptor, TaskImpl p_task, Node p_node,
            ProcessInstance p_pi, long p_companyId, String p_companyName,
            ProjectImpl p_project) throws Exception
    {
        Map<String, Object> data = new HashMap<String, Object>();
        Set<String> iReceipt = new HashSet<String>();
        iReceipt.add(p_acceptor);
        data.put("ignoredReceipt", iReceipt);
        ServerProxy.getTaskManager().acceptTask(p_acceptor, p_task, data);

        String activityName = WorkflowJbpmUtil.getActivityNameWithArrowName(
                p_node, "_" + p_companyId, p_pi,
                WorkflowConstants.TASK_TYPE_ACC);
        EmailInformation sender = ServerProxy.getUserManager()
                .getEmailInformationForUser(p_project.getProjectManagerId());
        EmailInformation recipient = ServerProxy.getUserManager()
                .getEmailInformationForUser(p_acceptor);
        String localePair = MailerHelper.getLocalePair(
                p_task.getSourceLocale(), p_task.getTargetLocale(),
                recipient.getEmailLocale());
        String wordCount = p_task.getWorkflow().getJob().getWordCount() + "";
        // get Job comments
        String comments = MailerHelper.getJobCommentsByJob(p_task.getWorkflow().getJob());
    
        // Email attachments
        File[] files = null;
        if (p_project.getReviewOnlyAutoSend())
        {
        	roleName = p_acceptor;
            List<Long> jobIDS = new ArrayList<Long>();
            jobIDS.add(p_task.getJobId());
            List<GlobalSightLocale> targetLocales = new ArrayList<GlobalSightLocale>();
            targetLocales.add(p_task.getTargetLocale());
            ReportGenerator generator = null;
            if (p_activityType == Activity.TYPE_REVIEW)
            {
            	PermissionSet perms = Permission.getPermissionManager().getPermissionSetForUser(p_acceptor);
            	if(!perms.getPermissionFor(Permission.REPORTS_LANGUAGE_SIGN_OFF) &&
            			perms.getPermissionFor(Permission.REPORTS_LANGUAGE_SIGN_OFF_SIMPLE))
            	{
            		generator = new ReviewersCommentsSimpleReportGenerator(p_companyName);
            		((ReviewersCommentsSimpleReportGenerator)generator).setIncludeCompactTags(
            				p_project.isReviewReportIncludeCompactTags());
            	}
            	else 
            	{
            		generator = new ReviewersCommentsReportGenerator(p_companyName);
            		((ReviewersCommentsReportGenerator)generator).setIncludeCompactTags(
            				p_project.isReviewReportIncludeCompactTags());
				}
            }
            else
            {
                generator = new TranslationsEditReportGenerator(p_companyName);
            }
            files = generator.generateReports(jobIDS, targetLocales);
            roleName = null;
        }

        String[] messageArguments =
        { activityName, p_acceptor, TaskHelper.getTaskURL(p_task),
                String.valueOf(p_task.getPriority()), p_task.getJobName(),
                localePair, wordCount,comments};

        sendAutoAcceptMail(sender, recipient, files,
                MailerConstants.AUTO_ACCEPT_SUBJECT,
                MailerConstants.AUTO_ACCEPT_MESSAGE, messageArguments,
                p_companyId);
    }

    /**
     * Sends Auto Accept Email
     * 
     * @param p_sender
     *            sender
     * @param p_recipient
     *            recipient
     * @param p_files
     *            attachment files
     * @param p_emailSubjectKey
     *            Email Subject Key
     * @param p_emailMessageKey
     *            Email Message Key
     * @param p_messageArguments
     *            email message arguments
     * @param p_company
     *            company
     */
    private static void sendAutoAcceptMail(EmailInformation p_sender,
            EmailInformation p_recipient, File[] p_files,
            String p_emailSubjectKey, String p_emailMessageKey,
            String[] p_messageArguments, long p_companyId)
    {
        String[] attachments = null;
        if (p_files != null)
        {
            attachments = new String[p_files.length];
            for (int i = 0; i < p_files.length; i++)
            {
                attachments[i] = p_files[i].getAbsolutePath();
            }
        }

        try
        {
            ServerProxy.getMailer().sendMail(p_sender, p_recipient,
                    p_emailSubjectKey, p_emailMessageKey, p_messageArguments,
                    attachments, p_companyId);
        }
        catch (Exception e)
        {
            log.error("sendAutoAcceptMail Error.", e);
        }
    }

}
