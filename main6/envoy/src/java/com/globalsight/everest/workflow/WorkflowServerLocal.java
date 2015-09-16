/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.workflow;

// java
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.PooledActor;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.ArrorInfo;
import com.globalsight.everest.workflowmanager.DefaultPathTasks;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Entry;
import com.globalsight.util.StringUtil;
import com.globalsight.util.XmlParser;
import com.globalsight.util.mail.MailerHelper;
import com.globalsight.util.mail.MailerWLRemote;

/**
 * <code>WorkflowServerLocal</code> responsibles for communication with the
 * Jbpm's workflow engine. It provides support for workflow template and
 * workflow instance activities.
 * 
 */
public class WorkflowServerLocal implements WorkflowServer
{
    // PRIVATE STATIC VARIABLES
    private static final Logger s_logger = Logger
            .getLogger(WorkflowServerLocal.class.getName());

    private MailerWLRemote m_mailHandler = null;

    private String m_capLoginUrl = null;

    private String m_adminEmailAddress = null;

    private Timestamp m_timestamp = new Timestamp();

    private static Map completeWorkFlow = new HashMap();

    // system wide parameter that determines whether the notification action is
    // active.
    // The values are 0 or 1 (inactive and active respectively).
    private int m_isNotificationActive = -1;

    // the value of the warning threshold (system wide parameter).
    private Float m_threshold = null;

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    // For "amb-118 login task detail directly"
    private HashMap<String, String> taskDeatilUrlParam = new HashMap<String, String>();

    private static final String EXIT_NODE = "Exit";

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Construct a WorkflowServerLocal object.
     * 
     * @exception WorkflowException
     *                - Workflow related exception.
     */
    public WorkflowServerLocal()
    {

    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: WorkflowServer Implementation Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * 
     * @see com.globalsight.everest.workflow.WorkflowServer#acceptTask(com.globalsight.everest.workflowmanager.Workflow,
     *      java.lang.String, long,
     *      com.globalsight.everest.taskmanager.TaskInfo,
     *      com.globalsight.everest.workflow.TaskEmailInfo)
     */
    public void acceptTask(Workflow p_wfClone, String p_assignee,
            long p_nodeInstanceId, TaskInfo p_taskInfo,
            TaskEmailInfo p_emailInfo, boolean isSkipped)
            throws RemoteException, WorkflowException
    {

        JbpmContext ctx = null;

        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();

            ProcessInstance processInstance = ctx.getProcessInstance(p_wfClone
                    .getId());
            Node node = WorkflowJbpmUtil.getNodeById(
                    processInstance.getProcessDefinition(), p_nodeInstanceId);
            TaskInstance taskInstance = WorkflowJbpmUtil.getTaskInstanceByNode(
                    processInstance, node, true);

            startTaskInstance(taskInstance, p_assignee);

            // Set up job start date
            Job job = p_wfClone.getJob();
            if (job.getStartDate() == null)
            {
                job.setStartDate(new Date());
                HibernateUtil.update(job);
            }

            String companyId = String.valueOf(p_wfClone.getCompanyId());
            p_emailInfo.setCompanyId(companyId);
            //get Job Comments
            String comments = MailerHelper.getJobCommentsByJob(job);
            
            Object[] args =
            {
                    WorkflowJbpmUtil.getActivityNameWithArrowName(node, "_"
                            + companyId, processInstance,
                            WorkflowConstants.TASK_TYPE_ACC),
                    UserUtil.getUserNameById(p_assignee),
                    capLoginUrl(),
                    p_emailInfo.getPriorityAsString(),
                    p_emailInfo.getJobName(),
                    WorkflowHelper.localePair(p_emailInfo.getSourceLocale(),
                            p_emailInfo.getTargetLocale(), "en_US") ,comments};

            if (!isSkipped)
                sendTaskActionEmailToUser(p_assignee, p_emailInfo, null,
                        WorkflowMailerConstants.ACCEPT_TASK, args);

            // Add the assignee name to the emailInfo for overdue issue
            p_emailInfo.setAssigneesName(p_assignee);

            // Fist stop the "accept by" timer and then create a "complete by"
            // warning notification for this activity based on the accepted
            // time.
            if (isNotificationActive())
            {
                int actionType = SchedulerConstants.ACCEPT_ACTIVITY;
                if (isSkipped)
                    actionType = SchedulerConstants.SKIP_ACTIVITY;

                EventNotificationHelper.performSchedulingProcess(new Integer(
                        actionType), p_nodeInstanceId,
                        (Integer) SchedulerConstants.s_eventTypes
                                .get(SchedulerConstants.ACCEPT_TYPE), node,
                        p_taskInfo, EventNotificationHelper.getCurrentTime(),
                        (Integer) SchedulerConstants.s_eventTypes
                                .get(SchedulerConstants.COMPLETE_TYPE),
                        getWarningThreshold(), p_emailInfo);
            }
        }
        catch (WorkflowException wfe)
        {

            // this exception happens if jbpm could not create the work
            // item
            // so we know that the workflow has been discarded
            s_logger.error("acceptTask (workflow discarded) " + wfe.toString()
                    + GlobalSightCategory.getLineContinuation()
                    + " p_assignee=" + p_assignee + " p_nodeInstanceId="
                    + p_nodeInstanceId, wfe);
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_ACCEPT_CANCELED, null, wfe);

        }
        catch (Exception e)
        {
            s_logger.error(
                    "acceptTask " + e.toString()
                            + GlobalSightCategory.getLineContinuation()
                            + " p_assignee=" + p_assignee
                            + " p_nodeInstanceId=" + p_nodeInstanceId, e);
            throw new WorkflowException(WorkflowException.MSG_FAILED_TO_ACCEPT,
                    null, e);
        }
        finally
        {
            ctx.close();
        }
    }

    /**
     * @see WorkflowServer.advanceTask(String, String, long, String,
     *      DefaultPathTasks, TaskEmailInfo);
     */
    @SuppressWarnings("unchecked")
    public WorkflowInstanceInfo advanceTask(Workflow p_wfClone,
            String p_assignee, long p_nodeInstanceId, String p_arrowLabel,
            DefaultPathTasks p_taskInfos, TaskEmailInfo p_emailInfo,
            String skipping) throws RemoteException, WorkflowException
    {

        int state = -1;
        List<WfTaskInfo> nextTaskInfos = null;

        JbpmContext ctx = null;

        try
        {

            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance processInstance = ctx.getProcessInstance(p_wfClone
                    .getId());

            String skipActivity = WorkflowJbpmUtil
                    .getSkipActivity(processInstance);

            Node node = WorkflowJbpmUtil.getNodeById(
                    processInstance.getProcessDefinition(), p_nodeInstanceId);
            /* only get the not ended task */
            TaskInstance taskInstance = WorkflowJbpmUtil.getTaskInstanceByNode(
                    processInstance, node, true);

            WorkflowInstance workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(processInstance);

            // TODO getPreActivityName
            NextNodes nextNodes = nextNodeInstances(
                    node.getName(),
                    workflowInstance.getWorkflowInstanceTasks(),
                    p_arrowLabel,
                    getPreActivityName(skipActivity,
                            workflowInstance.getWorkflowInstanceTasks()));

            // get a list of next nodes used for email notification and event
            // scheduling. At this point, the UDA for a possible condition node
            // will also be updated.

            int sz = nextNodes.size();
            boolean isCompleted = sz == 0 || nextNodes.hasExitNode();

            // if the completed task was not the last one, set the assigness of
            // next node(s)
            if (!isCompleted)
            {
                setAssigneesOfNextNodes(processInstance, nextNodes,
                        p_taskInfos, p_emailInfo);
            }

            endTaskInstance(nextNodes, taskInstance, isCompleted,
                    processInstance, p_arrowLabel, skipActivity,
                    workflowInstance.getDefaultPathNode(), p_wfClone.getId(),
                    p_assignee);

            // For task complete, the finish email info need the node's
            // activity.
            p_emailInfo.setPreNode(node);
            if (isCompleted)
            {
                // only notify task completion
                advanceTaskNotification(p_assignee, p_nodeInstanceId, null,
                        p_emailInfo, null, processInstance, skipping,
                        WorkflowConstants.TASK_TYPE_COM);

                // TomyD -- since jBPM would not return the correct state of a
                // workflow
                // (due to some time delays for event related purposes), we'll
                // assume that
                // the workflow is completed once the next node is "Exit" node.
                state = WorkflowConstants.STATE_COMPLETED;
                // well,the p_arrowLabel is param come from next node,
                // if the p_arrowLabel is null
                // that means it have none,yes ,all the workflow have finished.
                p_emailInfo.setAssigneesName(p_assignee);
                completeWorkFlow.put(
                        workflowInstance.getId() + p_emailInfo.getJobName(),
                        p_emailInfo);

            }
            else
            {
                state = WorkflowConstants.STATE_RUNNING;
                nextTaskInfos = new ArrayList<WfTaskInfo>();

                for (int i = 0; i < sz; i++)
                {
                    // It's possible that after a condition node, the same node
                    // gets
                    // activated. Therefore, we need to consider it as the next
                    // node.
                    WorkflowTaskInstance nextNode = (WorkflowTaskInstance) nextNodes
                            .getNode(i);

                    nextNode = (nextNode.getType() == WorkflowConstants.ACTIVITY && (nextNode
                            .getTaskId() == p_nodeInstanceId || !nextNodes
                            .wasActive(nextNode))) ? nextNode : null;

                    // the notification is both for completion of a task and the
                    // starting
                    // of the next possible task(s). Since we're looping thru a
                    // list of
                    // possible next tasks, we should only send the completion
                    // email once.
                    p_assignee = i == 0 ? p_assignee : null;

                    ArrayList emailInfo = advanceTaskNotification(
                            p_assignee,
                            p_nodeInstanceId,
                            nextNode == null ? null : p_taskInfos
                                    .getTaskInfoById(nextNode.getTaskId()),
                            p_emailInfo, WorkflowJbpmUtil.getNodeByWfTask(
                                    processInstance, nextNode),
                            processInstance, skipping,
                            WorkflowConstants.TASK_TYPE_COM);
                    if (nextNode != null)
                    {
                        // now add it to the list of next WfTaskInfo objects
                        WfTaskInfo taskInfo = new WfTaskInfo(
                                nextNode.getTaskId(),
                                getSystemActionTypeForNode(nextNode,
                                        processInstance));

                        taskInfo.userEmail = emailInfo;
                        nextTaskInfos.add(taskInfo);
                    }
                }
            }
        }
        catch (Exception e)
        {
            s_logger.info("advanceTask " + e.toString()
                    + GlobalSightCategory.getLineContinuation()

                    + p_assignee + " p_emailInfo="
                    + (p_emailInfo != null ? p_emailInfo.toString() : "null")
                    + " p_nodeInstanceId=" + Long.toString(p_nodeInstanceId));
            String pm = p_emailInfo.getProjectManagerId();
            try
            {
                pm = getEmailInfo(pm).getEmailAddress();
            }
            catch (Exception e1)
            {
                // ignore...
            }
            String[] args =
            { String.valueOf(p_nodeInstanceId), pm };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_ADVANCE, args, e);
        }
        finally
        {
            ctx.close();
        }

        return new WorkflowInstanceInfo(p_wfClone.getId(), state, nextTaskInfos);
    }

    public void advanceWorkFlowNotification(String key, String state)
    {
        TaskEmailInfo p_emailInfo = (TaskEmailInfo) completeWorkFlow
                .remove(key);
        if (p_emailInfo == null)
        {

            return;
        }
        Object[] args =
        {
                p_emailInfo.getJobName(),
                p_emailInfo.getAssigneesName(),
                capLoginUrl(),
                p_emailInfo.getPriorityAsString(),
                WorkflowHelper.localePair(p_emailInfo.getSourceLocale(),
                        p_emailInfo.getTargetLocale(), "en_US"), state };
        sendTaskActionEmailToUser(p_emailInfo.getAssigneesName(), p_emailInfo,
                null, WorkflowMailerConstants.COMPLETED_WFL, args);
    }

    /**
     * Creates a new workflow instance based on the given template id.
     * 
     * @param p_wfTemplateId
     *            - The id of the template that will be used for the creation of
     *            a process.
     * 
     * @return The newly created workflow instance.
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public WorkflowInstance createWorkflowInstance(long p_wfTemplateId)
            throws RemoteException, WorkflowException
    {
        WorkflowInstance wfi = null;
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessDefinition pd = ctx.getGraphSession().getProcessDefinition(
                    p_wfTemplateId);

            /*
             * To support the modification of the workflow instance, we need to
             * copy a new version of processdefinition. The modification of the
             * instance will only affect the copy of the processdefiniton and
             * will not affect the original one. This copy will also generator a
             * new node id, this id will be used as the task id
             */

            /* Gets the original xml of the given processdefinition */
            FileInputStream in = new FileInputStream(AmbFileStoragePathUtils
                    .getWorkflowTemplateXmlDir().getAbsolutePath()
                    + File.separator
                    + pd.getName()
                    + WorkflowConstants.SUFFIX_XML);
            ProcessDefinition pdCopy = ProcessDefinition
                    .parseXmlInputStream(in);
            ctx.deployProcessDefinition(pdCopy);
            ProcessInstance pi = pdCopy.createProcessInstance();
            wfi = WorkflowProcessAdapter.getProcessInstance(pi);
        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_wfTemplateId) };
            s_logger.error("Failed to create wf instance. " + e.toString(), e);
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_CREATE_WF_INSTANCE, args, e);
        }
        finally
        {
            ctx.close();
        }
        return wfi;
    }

    /**
     * Create a new workflow template.
     * 
     * @param p_wfTemplate
     *            - The template to be created.
     * @param p_worklfowOwners
     *            - The owner (s) of the workflow instances.
     * 
     * @return The created template with a valid id.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public WorkflowTemplate createWorkflowTemplate(
            WorkflowTemplate p_wfTemplate, WorkflowOwners p_workflowOwners)
            throws RemoteException, WorkflowException
    {
        try
        {
            return WorkflowTemplateAdapter.createInstance()
                    .createWorkflowTemplate(p_wfTemplate, p_workflowOwners);
        }
        catch (Exception e)
        {
            s_logger.error("Unable to create a workflow template due to the exception "
                    + e);
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_CREATE_WF_TEMPLATE, null, e);
        }
    }

    /**
     * Import a workflow template.
     * 
     * @param p_wfTemplate
     *            - The template to be created.
     * @param doc
     *            The workflow xml template document.
     * 
     * @return The created template with a valid id.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public WorkflowTemplate importWorkflowTemplate(
            WorkflowTemplate p_wfTemplate, Document doc)
            throws RemoteException, WorkflowException
    {
        try
        {
            return WorkflowTemplateAdapter.createInstance()
                    .importWorkflowTemplate(p_wfTemplate, doc);
        }
        catch (Exception e)
        {
            s_logger.error("Unable to create a workflow template due to the exception "
                    + e);
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_CREATE_WF_TEMPLATE, null, e);
        }
    }

    /**
     * Gets a list of tasks that are assigned by a specified project manager
     * based on the task state.
     * <p>
     * 
     * @return map keyed by node.getId(), values are WorkflowTaskInstance
     * @param p_userId
     *            - The current user id.
     * @param p_taskState
     *            - The task state.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public Map filterTasksForPM(String p_userId, int p_taskState)
            throws RemoteException
    {
        JbpmContext ctx = null;
        Map<Long, WorkflowTaskInstance> taskList = new HashMap<Long, WorkflowTaskInstance>();
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            // first get the task instances that were assigned to PM.
            List<TaskInstance> taskInstances = WorkflowJbpmPersistenceHandler
                    .getTaskInstances(p_userId, p_taskState, false, ctx);
            // then add the task instances that are visible by PM.
            taskInstances.addAll(WorkflowJbpmPersistenceHandler
                    .getTaskInstances(p_userId, p_taskState, true, ctx));

            convertAndMap(taskInstances, taskList, p_userId, p_taskState);
        }
        finally
        {
            ctx.close();
        }

        return taskList;
    }

    /**
     * Get a list of currently active tasks of the specified workflow instance.
     * 
     * @return A Map of active tasks (WorkflowTaskInstance objects) of the
     *         specified workflow instance with the task id as the key.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public Map<Long, WorkflowTaskInstance> getActiveTasksForWorkflow(
            long p_workflowInstanceId) throws RemoteException,
            WorkflowException
    {
        try
        {
            WorkflowInstance wi = WorkflowProcessAdapter
                    .getProcessInstance(p_workflowInstanceId);

            Vector<WorkflowTaskInstance> wfTaskInstances = wi
                    .getWorkflowInstanceTasks();
            Map<Long, WorkflowTaskInstance> activeTasks = new HashMap<Long, WorkflowTaskInstance>(
                    wfTaskInstances.size());

            for (WorkflowTaskInstance wti : wfTaskInstances)
            {
                if (wti.getType() == WorkflowConstants.ACTIVITY
                        && wti.getTaskState() == WorkflowConstants.STATE_RUNNING)
                {
                    activeTasks.put(new Long(wti.getTaskId()), wti);
                    updateInstanceOwner(p_workflowInstanceId, wti);
                }
            }
            return activeTasks;

        }
        catch (Exception e)
        {
            s_logger.error("getActiveTaskForWorkflow " + e.toString()
                    + GlobalSightCategory.getLineContinuation()
                    + " p_workflowInstanceId=" + p_workflowInstanceId, e);
            String args[] =
            { String.valueOf(p_workflowInstanceId) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_GET_ACTIVE_TASKS, args, e);
        }
    }

    private void updateInstanceOwner(long p_workflowInstanceId,
            WorkflowTaskInstance wti) throws Exception
    {
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();

            ProcessInstance processInstance = ctx
                    .getProcessInstance(p_workflowInstanceId);

            TaskInstance taskInstance = WorkflowJbpmUtil.getTaskInstanceByNode(
                    processInstance, wti.getNodeName());

            wti.setAcceptUser(taskInstance.getActorId());

        }
        catch (Exception e)
        {
            s_logger.error("updateInstanceOwner " + e.toString()
                    + GlobalSightCategory.getLineContinuation()
                    + " p_workflowInstanceId=" + p_workflowInstanceId, e);
            String args[] =
            { String.valueOf(p_workflowInstanceId) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_GET_TASKS_ACCEPT_USER,
                    args, e);
        }
        finally
        {
            ctx.close();
        }

    }

    /**
     * Gets a list of tasks that are assigned to a specified user based on the
     * task state.
     * <p>
     * 
     * @return map keyed by node.getId(), values are WorkflowTaskInstance
     * @param p_userId
     *            - The current user id.
     * @param p_taskState
     *            - The task state.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public Map getTasksForUser(String p_userId, int p_taskState)
            throws RemoteException
    {
        JbpmContext ctx = null;
        Map<Long, WorkflowTaskInstance> taskList = new HashMap<Long, WorkflowTaskInstance>();
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            List<TaskInstance> taskInstances = WorkflowJbpmPersistenceHandler
                    .getTaskInstances(p_userId, p_taskState, false, ctx);

            convertAndMap(taskInstances, taskList, p_userId, p_taskState);
        }
        finally
        {
            ctx.close();
        }

        return taskList;
    }

    /**
     * Converts jbpm TaskInstance to WorkflowTaskInstance and put them in a map.
     * 
     * @param p_taskInstances
     *            a list of task instances.
     * @param p_taskList
     *            the map
     * @param p_userId
     *            the user id.
     * @param p_taskState
     *            the task state being searched.
     */
    private void convertAndMap(List<TaskInstance> p_taskInstances,
            Map<Long, WorkflowTaskInstance> p_taskList, String p_userId,
            int p_taskState)
    {
        List<Long> rejectTaskIds = WorkflowJbpmUtil.getRejectedTaskIds(
                p_taskInstances, p_userId);
        for (Iterator it = p_taskInstances.iterator(); it.hasNext();)
        {
            TaskInstance ti = (TaskInstance) it.next();
            String activityName = WorkflowJbpmUtil
                    .getActivityName(ti.getName());
            long id = ti.getTask().getTaskNode().getId();
            Long keyId = new Long(id);

            WorkflowTaskInstance wti = (WorkflowTaskInstance) p_taskList
                    .get(keyId);
            if (wti == null)
            {
                wti = new WorkflowTaskInstance(activityName,
                        WorkflowConstants.ACTIVITY);
                wti.setTaskId(id);
                wti.setActivity(new Activity(activityName));

                String config = WorkflowJbpmUtil.getConfigure(ti.getTask()
                        .getTaskNode());
                WorkflowNodeParameter param = WorkflowNodeParameter
                        .createInstance(config);
                String assignees = WorkflowJbpmUtil.getAssignees(ti, p_userId);
                String acceptedTime = param
                        .getAttribute(WorkflowConstants.FIELD_ACCEPTED_TIME);
                String completedTime = param
                        .getAttribute(WorkflowConstants.FIELD_COMPLETED_TIME);

                String overdueToPM = param
                        .getAttribute(WorkflowConstants.OVERDUETOPM);
                String overdueToUser = param
                        .getAttribute(WorkflowConstants.OVERDUETOUSER);
                wti.setAcceptedTime(Long.parseLong(acceptedTime));
                wti.setCompletedTime(Long.parseLong(completedTime));

                if (overdueToPM != null && !"".equals(overdueToPM.trim())
                        && overdueToUser != null
                        && !"".equals(overdueToUser.trim()))
                {
                    wti.setOverdueToPM(Long.parseLong(overdueToPM));
                    wti.setOverdueToUser(Long.parseLong(overdueToUser));
                }

                wti.setWorkItemAttributes(activityName, "", assignees, ti
                        .getCreate().getTime(), WorkflowJbpmUtil
                        .getStateFromTaskInstance(ti, p_userId, p_taskState,
                                rejectTaskIds));

                p_taskList.put(keyId, wti);
            }
        }
    }

    /**
     * Get a list of tasks (as WorkflowTask objects) for the specified workflow
     * instance. Note that all returned tasks are TYPE_ACTIVTY.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * 
     * @return A list of all tasks (as WorkflowTask objects) of the specified
     *         workflow instance.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps workflow's exceptions.
     */
    public Vector getTasksForWorkflow(long p_workflowInstanceId)
            throws RemoteException, WorkflowException
    {

        JbpmContext ctx = null;
        WorkflowInstance workflowInstance = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance processInstance = ctx
                    .getProcessInstance(p_workflowInstanceId);
            workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(processInstance);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            ctx.close();
        }
        return workflowInstance == null ? new Vector() : workflowInstance
                .getWorkflowInstanceTasks();
    }

    /**
     * @see WorkflowServer.getVisitedTasksForWOrkflow(WFSession, long)
     */
    @SuppressWarnings("unchecked")
    public List getVisitedTasksForWorkflow(long p_workflowInstanceId)
            throws RemoteException, WorkflowException
    {

        List<WorkflowTaskInstance> taskList = new ArrayList<WorkflowTaskInstance>();
        JbpmContext ctx = null;
        WorkflowInstance workflowInstance = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance processInstance = ctx
                    .getProcessInstance(p_workflowInstanceId);
            workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(processInstance);

            Vector tasks = workflowInstance.getWorkflowInstanceTasks();

            for (Enumeration<WorkflowTaskInstance> e = tasks.elements(); e
                    .hasMoreElements();)
            {
                WorkflowTaskInstance task = e.nextElement();
                if (task.getType() == WorkflowConstants.ACTIVITY
                        && task.getTaskState() != WorkflowConstants.STATE_INITIAL)
                {
                    taskList.add(task);
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            ctx.close();
        }
        return taskList;
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowTaskInstance> getUnVisitedTasksForWorkflow(
            long p_workflowInstanceId) throws RemoteException,
            WorkflowException
    {

        List<WorkflowTaskInstance> taskList = new ArrayList<WorkflowTaskInstance>();
        JbpmContext ctx = null;
        WorkflowInstance workflowInstance = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance processInstance = ctx
                    .getProcessInstance(p_workflowInstanceId);
            workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(processInstance);

            List<WorkflowTaskInstance> nodes = workflowInstance
                    .getDefaultPathNode();

            for (WorkflowTaskInstance task : nodes)
            {
                if (task.getType() == WorkflowConstants.ACTIVITY
                        && task.getTaskState() == WorkflowConstants.STATE_INITIAL)
                {
                    taskList.add(task);
                }
            }

            if (hasReachedExit(nodes.get(nodes.size() - 1)))
            {

                Vector tasks = workflowInstance.getWorkflowInstanceTasks();

                for (Enumeration<WorkflowTaskInstance> e = tasks.elements(); e
                        .hasMoreElements();)
                {
                    WorkflowTaskInstance task = e.nextElement();

                    if (task.getType() == WorkflowConstants.STOP
                            && !WorkflowConstants.START_NODE
                                    .equals(processInstance.getRootToken()
                                            .getNode().getName()))
                    {
                        taskList.add(task);
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            ctx.close();
        }
        return taskList;
    }

    private boolean hasReachedExit(WorkflowTaskInstance task)
    {
        Vector arrows = task.getOutgoingArrows();
        WorkflowArrowInstance arrow = (WorkflowArrowInstance) arrows.get(0);
        WorkflowTaskInstance endTask = (WorkflowTaskInstance) arrow
                .getTargetNode();

        if (endTask.getType() == WorkflowConstants.STOP)
        {
            return true;
        }

        if (endTask.getType() == WorkflowConstants.CONDITION)
        {

            Vector aarrows = endTask.getOutgoingArrows();
            for (Enumeration ee = aarrows.elements(); ee.hasMoreElements();)
            {
                WorkflowArrow aarrow = (WorkflowArrow) ee.nextElement();
                if (endTask.getConditionSpec().getBranchSpec(aarrow.getName())
                        .isDefault())
                {
                    return aarrow.getTargetNode().getType() == WorkflowConstants.STOP;
                }
            }

        }

        return false;
    }

    /**
     * Get a particular workflow instance based on the given id.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance to be retreived.
     * @return A WorkflowInstance object (if it exists).
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps workflow's exceptions.
     */
    public WorkflowInstance getWorkflowInstanceById(long p_workflowInstanceId)
            throws RemoteException, WorkflowException
    {
        return WorkflowProcessAdapter.getProcessInstance(p_workflowInstanceId);
    }

    /**
     * @see WorkflowServer.getWorkflowTaskInfo(long)
     */
    public WfTaskInfo getWorkflowTaskInfo(long p_workflowInstanceId,
            long p_taskId) throws RemoteException, WorkflowException
    {

        JbpmContext ctx = null;

        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance processInstance = ctx
                    .getProcessInstance(p_workflowInstanceId);
            ProcessDefinition processDefinition = processInstance
                    .getProcessDefinition();

            Node node = WorkflowJbpmUtil.getNodeById(processDefinition,
                    p_taskId);

            return new WfTaskInfo(p_taskId, getSystemActionTypeForNode(node,
                    processInstance));

        }
        catch (Exception e)
        {
            s_logger.error("Error occured when process the second target file",
                    e);
        }
        finally
        {
            ctx.close();
        }

        /* If error occured, return a null action type */
        return new WfTaskInfo(p_taskId, null);

    }

    /**
     * Gets a particular task of a workflow instance. This method is used when
     * retrieving a workflow task for a project manager.
     * 
     * @return A specified task (node) of a workflow instance.
     * @param p_userId
     *            - the pm id.
     * @param p_taskId
     *            - The id of the workflow instance task.
     * @param p_state
     *            - The task state.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public WorkflowTaskInstance getWorkflowTaskInstance(String p_userId,
            long p_taskId, int p_state) throws RemoteException,
            WorkflowException
    {
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                .getTaskInstance(p_taskId, ctx);
        WorkflowTaskInstance wti = null;
        try
        {
            Node node = taskInstance.getTask().getTaskNode();
            wti = WorkflowProcessAdapter.workflowTaskInstance(taskInstance,
                    p_userId);
            String assignees = WorkflowJbpmUtil.getAssignees(taskInstance,
                    p_userId);
            wti.setWorkItemAttributes(WorkflowJbpmUtil.getActivityName(node),
                    "", assignees, taskInstance.getCreate().getTime(),
                    WorkflowJbpmUtil.getStateFromTaskInstance(taskInstance,
                            p_userId, p_state));
        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_taskId) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_GET_WF_INSTANCE, args, e);
        }
        finally
        {
            ctx.close();
        }

        return wti;
    }

    /**
     * @see WorkflowServer.getWorkflowTaskInstance(long, long)
     */
    @SuppressWarnings("unchecked")
    public WorkflowTaskInstance getWorkflowTaskInstance(long p_wfId,
            long p_taskId) throws RemoteException, WorkflowException
    {

        WorkflowTaskInstance wti = null;
        WorkflowInstance workflowInstance = null;
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        try
        {
            ProcessInstance processInstance = ctx.getProcessInstance(p_wfId);
            workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(processInstance);

            Vector tasks = workflowInstance.getWorkflowInstanceTasks();

            for (Enumeration<WorkflowTaskInstance> e = tasks.elements(); e
                    .hasMoreElements();)
            {
                WorkflowTaskInstance task = e.nextElement();
                if (task.getTaskId() == p_taskId)
                {
                    return task;
                }
            }

        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_taskId) };
            s_logger.error("Error when get the WorkflowTaskInstance");
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_GET_WF_INSTANCE, args, e);
        }
        finally
        {
            ctx.close();
        }

        return wti;
    }

    /**
     * Gets a task of a workflow instance for a particular user. This method is
     * used when retrieving a workflow task for a translator/reviewer/etc. (for
     * a project manager use getWorkflowTaskInstance).
     * <p>
     * 
     * @return A the specified task (node) of a workflow instance.
     * @param p_assignee
     *            - The task's assignee.
     * @param p_taskId
     *            - The task node id.
     * @param p_state
     *            - The task state.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps workflow's exceptions.
     */
    public WorkflowTaskInstance getWorkflowTaskInstanceForAssignee(
            String p_assignee, long p_taskId, int p_state)
            throws RemoteException, WorkflowException
    {
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                .getTaskInstance(p_taskId, ctx);
        Set<String> actorIds = PooledActor.extractActorIds(taskInstance
                .getPooledActors());
        if (!actorIds.contains(p_assignee))
        {
            throw new WorkflowException(new Exception(p_assignee
                    + " do not have" + " the authority to operate the task."));
        }

        WorkflowTaskInstance wfTaskInstance = null;
        try
        {
            Node node = taskInstance.getTask().getTaskNode();
            wfTaskInstance = WorkflowProcessAdapter.workflowTaskInstance(
                    taskInstance, p_assignee);
            wfTaskInstance.setWorkItemAttributes(WorkflowJbpmUtil
                    .getActivityName(node), "", p_assignee, taskInstance
                    .getCreate().getTime(),
                    WorkflowJbpmUtil.getStateFromTaskInstance(taskInstance,
                            p_assignee, p_state));
        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_taskId) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_GET_WF_INSTANCE, args, e);
        }
        finally
        {
            ctx.close();
        }
        return wfTaskInstance;
    }

    /**
     * @see WorkflowServer.getWorkflowTemplateById(long)
     */
    public WorkflowTemplate getWorkflowTemplateById(long p_templateId)
            throws RemoteException, WorkflowException
    {
        try
        {

            return WorkflowTemplateAdapter.createInstance()
                    .getWorkflowTemplate(p_templateId);
        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_templateId) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_GET_WF_TEMPLATE, args, e);
        }

    }

    /**
     * Modify an existing workflow template. Since i-Flow does not allow the
     * modification of a template that has an active associated instance, we
     * create a new template and return the new id for updating the workflow
     * template info object.
     * 
     * @param p_wfTemplate
     *            - The template to be modified.
     * @param p_worklfowOwners
     *            - The owner (s) of the workflow instances.
     * @return The modified workflow template template.
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps workflow's exceptions.
     */
    public WorkflowTemplate modifyWorkflowTemplate(
            WorkflowTemplate p_wfTemplate, WorkflowOwners p_workflowOwners)
            throws RemoteException, WorkflowException
    {
        try
        {
            // TomyD -- NEED TO CREATE A NEW ONE INSTEAD OF MODIFYING THE
            // EXISTING
            // TEMPLATE. A template that has an associate process in an
            // active state can not be modified.
            return createWorkflowTemplate(p_wfTemplate, p_workflowOwners);

        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_wfTemplate.getId()) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_UPDATE_WF_TEMPLATE, args, e);
        }
    }

    /**
     * @see WorkflowServer.modifyWorkflowInstance(String, WorkflowInstance,
     *      DefaultPathTasks, TaskEmailInfo)
     */
    public Map modifyWorkflowInstance(String p_sessionId,
            WorkflowInstance p_wfInstance, DefaultPathTasks p_taskInfos,
            TaskEmailInfo p_emailInfo) throws RemoteException,
            WorkflowException
    {

        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        Map newAndDeleted = null;
        List originalTasks = new ArrayList(0);
        List persistedTasks = new ArrayList(0);

        try
        {
            // TomyD -- Temp for Customer Upload feature.

            ProcessInstance processInstance = ctx
                    .getProcessInstance(p_wfInstance.getId());
            WorkflowProcessAdapter processAdapter = WorkflowProcessAdapter
                    .createInstance();

            // TomyD -- Temp for Customer Upload feature.
            if (p_sessionId == null)
            {
                setAdminAsOwner(processInstance);
            }

            // determine the state of workflow before modification.
            boolean isWfCompleted = processInstance.hasEnded();

            // get the original workflow tasks (ONLY ActivityNodes before the
            // modification)
            originalTasks = processAdapter
                    .workflowTaskInstances(processInstance);

            // perform the structural edit.
            WorkflowProcessAdapter.updateWorkflowProcessInstance(p_wfInstance,
                    processInstance);

            // now get the list of updated activities (ONLY Activity Nodes)

            persistedTasks = processAdapter
                    .workflowTaskInstances(processInstance);

            // notify user(s) for a newly added and activate task for a workflow
            // that was in completed state
            if (isWfCompleted)
            {

                /* should never be here */

                // List activeNodes = piInfo.getActiveNodes();
                // int size = activeNodes == null ? 0 : activeNodes.size();
                // for (int i = 0; i < size; i++)
                // {
                // NodeInstance activeNode = (NodeInstance) activeNodes.get(i);
                // TaskInfo taskInfo = p_taskInfos.getTaskInfoById(activeNode
                // .getNodeInstanceId());
                //
                // if (m_systemNotificationEnabled)
                // {
                // notifyTaskIsAdvanced(null, null, wfSession,
                // p_emailInfo, activeNode, taskInfo);
                // }
                //
                // // // add the warning/deadline timer if feature is
                // // activated.
                // // EventNotificationHelper.scheduleNotificationForDispatch(
                // // activeNode, taskInfo, p_emailInfo,
                // // isNotificationActive(), getWarningThreshold());
                // }
            }
            // only perform node activation update for an active process
            else
            {
                performNodeActivation(persistedTasks, processInstance,
                        processInstance.getProcessDefinition(), originalTasks,
                        p_taskInfos, p_emailInfo);
            }

        }
        catch (WorkflowException wfe) // send back the appropriate workflow
                                      // exception
        {
            throw wfe;
        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_wfInstance.getId()) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_UPDATE_WF_INSTANCE, args, e);
        }
        finally
        {
            ctx.close();
        }

        updateWorkflowInstanceId(p_wfInstance, persistedTasks);
        try
        {
            newAndDeleted = newAndDeletedTasks(originalTasks, persistedTasks);
        }
        catch (Exception e)
        {
            String args[] =
            { String.valueOf(p_wfInstance.getId()) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_UPDATE_WF_INSTANCE, args, e);
        }

        return newAndDeleted;
    }

    /**
     * Sets the task id for the WorkflowTaskInstance when the node is new added.
     * 
     * @param p_wfInstance
     *            The WorkflowTaskInstance.
     * @throws Exception
     */
    public void updateWorkflowInstanceId(WorkflowInstance p_wfInstance,
            List persistedTasks)

    {
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance processInstance = ctx
                    .getProcessInstance(p_wfInstance.getId());
            ProcessDefinition processDefinition = processInstance
                    .getProcessDefinition();
            Vector tasks = p_wfInstance.getWorkflowInstanceTasks();

            for (Enumeration e = tasks.elements(); e.hasMoreElements();)
            {
                WorkflowTaskInstance taskInstance = (WorkflowTaskInstance) e
                        .nextElement();
                setTaskInstanceId(taskInstance, processDefinition);

            }

            for (Iterator it = persistedTasks.iterator(); it.hasNext();)
            {
                WorkflowTaskInstance taskInstance = (WorkflowTaskInstance) it
                        .next();
                setTaskInstanceId(taskInstance, processDefinition);
            }
        }
        finally
        {
            ctx.close();
        }
    }

    private void setTaskInstanceId(WorkflowTaskInstance p_wfTask,
            ProcessDefinition p_processDefinition)
    {
        if (p_wfTask.getType() != WorkflowConstants.START
                && p_wfTask.getType() != WorkflowConstants.STOP)
        {
            if (p_wfTask.getTaskId() <= 0)
            {
                /* set the task id */
                Node node = WorkflowJbpmUtil.getNodeByNodeName(
                        p_processDefinition, p_wfTask.getNodeName());
                p_wfTask.setTaskId(node.getId());
            }
        }
    }

    /**
     * @see WorkflowServer.rejectTask(String, String, long, TaskInfo,
     *      TaskEmailInfo)
     */
    public void rejectTask(String p_assignee, long p_nodeInstanceId,
            TaskInfo p_taskInfo, TaskEmailInfo p_emailInfo)
            throws RemoteException
    {
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        try
        {
            TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                    .getTaskInstance(p_nodeInstanceId, ctx);

            String config = WorkflowJbpmUtil.getConfigure(taskInstance
                    .getTask().getTaskNode());
            WorkflowNodeParameter param = WorkflowNodeParameter
                    .createInstance(config);
            // update the actorId or pooledActors in the task instance.
            WorkflowJbpmUtil.updateAssignees(taskInstance, p_assignee, param);
            String pm = param.getAttribute(WorkflowConstants.FIELD_PM);
            Set pooledActors = PooledActor.extractActorIds(taskInstance
                    .getPooledActors());
            if (!(p_assignee.equals(pm) && pooledActors.contains(p_assignee) && pooledActors
                    .size() == 1))
            {
                /* save the reject variable to the database */
                WorkflowJbpmPersistenceHandler.saveTaskVariable(ctx,
                        WorkflowConstants.VARIABLE_IS_REJECTED, p_assignee,
                        taskInstance);
            }

            WorkflowJbpmUtil.setPrivateValue(taskInstance, "start", null);

            int initialState = -1;
            initialState = WorkflowJbpmUtil
                    .getStateFromTaskInstance(taskInstance, p_assignee,
                            WorkflowConstants.TASK_ALL_STATES);
            Object[] args =
            {
                    WorkflowJbpmUtil.getActivityName(taskInstance.getTask()
                            .getTaskNode()),
                    UserUtil.getUserNameById(p_assignee),
                    p_emailInfo.getComments(),
                    capLoginUrl(),
                    p_emailInfo.getPriorityAsString(),
                    p_emailInfo.getJobName(),
                    WorkflowHelper.localePair(p_emailInfo.getSourceLocale(),
                            p_emailInfo.getTargetLocale(), "en_US") };
            sendTaskActionEmailToUser(p_assignee, p_emailInfo, null,
                    WorkflowMailerConstants.REJECT_TASK, args);

            // The base time for newly created "accpet by" timer should be
            // the creation date of the work item since we're dealing with
            // an existing work item.
            if (initialState == WorkflowConstants.TASK_ACCEPTED
                    && isNotificationActive())
            {
                EventNotificationHelper.performSchedulingProcess(new Integer(
                        SchedulerConstants.UNACCEPT_ACTIVITY),
                        p_nodeInstanceId,
                        (Integer) SchedulerConstants.s_eventTypes
                                .get(SchedulerConstants.COMPLETE_TYPE),
                        taskInstance.getTask().getTaskNode(), p_taskInfo,
                        new Long(taskInstance.getCreate().getTime()),
                        (Integer) SchedulerConstants.s_eventTypes
                                .get(SchedulerConstants.ACCEPT_TYPE),
                        getWarningThreshold(), p_emailInfo);
            }
        }
        finally
        {
            ctx.close();
        }
    }

    /**
     * @see WorkflowServer.startWorkflow(WFSession, long, DefaultPathTasks,
     *      TaskEmailInfo)
     */
    public List<WfTaskInfo> startWorkflow(long p_workflowInstanceId,
            DefaultPathTasks p_taskInfos, TaskEmailInfo p_emailInfo)
            throws RemoteException, WorkflowException
    {
        JbpmContext ctx = null;
        try
        {
            // must get a WF session that is the admin - since we
            // don't have the owner's (PM) userid and password for
            // this method.
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();

            ProcessInstance ppi = ctx.getProcessInstance(p_workflowInstanceId);
            WorkflowInstance workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(ppi);

            String skipActivity = WorkflowJbpmUtil.getSkipActivity(ppi);

            NextNodes nextNodes = nextNodeInstances(
                    null,
                    workflowInstance.getWorkflowInstanceTasks(),
                    null,
                    getPreActivityName(skipActivity,
                            workflowInstance.getDefaultPathNode()));

            // Gets all the assigees' names of this activity for overdue issue.
            String[] assignees = setAssigneesOfNextNodes(ppi, nextNodes,
                    p_taskInfos, p_emailInfo);
            String assigneesName = "";
            if (assignees != null)
            {
                int i = assignees.length;
                for (int n = 0; n < i; n++)
                {
                    if (n != 0)
                    {
                        assigneesName += ",";
                    }
                    assigneesName += assignees[n];
                }
            }
            p_emailInfo.setAssigneesName(assigneesName);

            // start the worklow and advance to the appropriate task
            ppi.signal();
            signalProcess(ppi, skipActivity,
                    workflowInstance.getDefaultPathNode(), p_workflowInstanceId);

            int sz = nextNodes.size();
            List<WfTaskInfo> nextTaskInfos = new ArrayList<WfTaskInfo>();

            for (int i = 0; i < sz; i++)
            {
                WorkflowTaskInstance nextNode = (WorkflowTaskInstance) nextNodes
                        .getNode(i);
                TaskInfo taskInfo = p_taskInfos.getTaskInfoById(nextNode
                        .getTaskId());
                if (nextNode.getType() == WorkflowConstants.ACTIVITY)
                {
                    ArrayList emailInfos = notifyTaskIsAdvanced(null,
                            p_emailInfo,
                            WorkflowJbpmUtil.getNodeByWfTask(ppi, nextNode),
                            taskInfo, ppi, null,
                            WorkflowConstants.TASK_TYPE_NEW);

                    // Only create a warning notification for the first activity
                    // (accpet timer)
                    EventNotificationHelper.scheduleNotificationForDispatch(
                            WorkflowJbpmUtil.getNodeByWfTask(ppi, nextNode),
                            taskInfo, p_emailInfo, isNotificationActive(),
                            getWarningThreshold());

                    // now add it to the list of next WfTaskInfo objects
                    WfTaskInfo wfTaskInfo = new WfTaskInfo(
                            nextNode.getTaskId(), getSystemActionTypeForNode(
                                    nextNode, ppi));
                    wfTaskInfo.userEmail = emailInfos;
                    nextTaskInfos.add(wfTaskInfo);
                }
            }
            return nextTaskInfos;
        }
        catch (Exception e)
        {
            s_logger.error(
                    "startWorkflow: "
                            + e.toString()
                            + GlobalSightCategory.getLineContinuation()
                            + " p_workflowInstanceId="
                            + Long.toString(p_workflowInstanceId)
                            + " p_emailInfo="
                            + (p_emailInfo != null ? p_emailInfo.toString()
                                    : "null"), e);
            throw new WorkflowException(WorkflowException.EX_START_ERROR,
                    WorkflowException.MSG_FAILED_TO_START_WORKFLOW, e);
        }
        finally
        {
            ctx.close();
        }
    }

    private String getPreActivityName(String skipActivity,
            List<WorkflowTaskInstance> tasks)
    {

        if (skipActivity == null)
        {
            return null;
        }

        WorkflowTaskInstance temTask = null;
        for (WorkflowTaskInstance task : tasks)
        {
            if (task.getActivityName().equals(skipActivity))
            {
                break;
            }
            temTask = task;
        }

        return temTask == null ? WorkflowConstants.START_NODE : temTask
                .getActivityName();
    }

    /**
     * Suspend the specified workflow instance. This process is done by
     * de-activating the currently active task.
     * <p>
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @param p_emailInfo
     *            Information to be used or included in any emails sent out.
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public void suspendWorkflow(long p_workflowInstanceId,
            TaskEmailInfo p_emailInfo) throws RemoteException
    {
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance pi = ctx.getProcessInstance(p_workflowInstanceId);
            Collection taskInstances = pi.getTaskMgmtInstance()
                    .getTaskInstances();
            String companyIdStr = MailerHelper.getCompanyId(p_emailInfo);

            pi.suspend();

            String localePair = WorkflowHelper.localePair(
                    p_emailInfo.getSourceLocale(),
                    p_emailInfo.getTargetLocale(), "en_US");
            ArrayList<String> assignees = null;

            // There is no taskInstance, when the task didn't dispatch.
            if (null == taskInstances || taskInstances.size() == 0)
            {
                Object[] arguments =
                {
                        "",
                        UserUtil.getUserNameById(p_emailInfo
                                .getProjectManagerId()), capLoginUrl(),
                        p_emailInfo.getJobName(), localePair };
                sendSingleTaskActionEmailToUser(
                        p_emailInfo.getProjectManagerId(), p_emailInfo,
                        p_emailInfo.getAccepterName(), assignees,
                        WorkflowMailerConstants.CANCEL_TASK, arguments);
            }
            else
            {
                for (Iterator it = taskInstances.iterator(); it.hasNext();)
                {
                    TaskInstance ti = (TaskInstance) it.next();
                    Node node = ti.getTask().getTaskNode();
                    assignees = new ArrayList<String>();
                    if (ti.getActorId() == null)
                    {
                        Set pa = ti.getPooledActors();
                        Set actors = PooledActor.extractActorIds(pa);
                        for (Iterator its = actors.iterator(); its.hasNext();)
                        {
                            assignees.add((String) its.next());
                        }
                    }
                    else
                    {
                        assignees.add(ti.getActorId());
                    }
                    Object[] args =
                    {
                            WorkflowJbpmUtil.getActivityNameWithArrowName(node,
                                    "_" + companyIdStr, pi, ""),
                            UserUtil.getUserNameById(p_emailInfo
                                    .getProjectManagerId()), capLoginUrl(),
                            p_emailInfo.getJobName(), localePair };

                    // Modify for GBS-1004
                    /*
                     * String[] a = new String[assignees.size()];
                     * sendTaskActionEmailToUsers
                     * (p_emailInfo.getProjectManagerId(), assignees.toArray(a),
                     * p_emailInfo.getProjectIdAsLong(), null,
                     * WorkflowMailerConstants.CANCEL_TASK, args);
                     */
                    sendSingleTaskActionEmailToUser(
                            p_emailInfo.getProjectManagerId(), p_emailInfo,
                            p_emailInfo.getAccepterName(), assignees,
                            WorkflowMailerConstants.CANCEL_TASK, args);

                    // Stop the "complete by" or "accept by" timer of this node.
                    if (isNotificationActive())
                    {
                        // since we don't have the WorkItem object, we don't
                        // really
                        // know what the state is. Therefore, we'll assume
                        // "complete"
                        // for now and we'll do another check for "accept"
                        // during
                        // the unscheduling process.
                        EventNotificationHelper
                                .performSchedulingProcess(new Integer(
                                        SchedulerConstants.CANCEL_WORKFLOW),
                                        node.getId(), null, null, null, null,
                                        null, getWarningThreshold(),
                                        p_emailInfo);
                    }
                }
            }
        }
        finally
        {
            ctx.close();
        }
    }

    /**
     * Notify (send email) to the participants of the active task of the
     * workflow specified.
     * 
     * @param p_WFSession
     *            - The WFSession object created through a singleton object.
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @param p_taskActionType
     *            - the action that is being done that an email should be sent
     *            out for. See WorkflowMailerConstants for the types.
     * @param p_emailInfo
     *            - The information to be inluded in any emails sent out. The
     *            arguments sent to be emailed must be in a certain order. See
     *            the email resource property.
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public void notifyTaskParticipants(long p_workflowInstanceId,
            int p_taskActionType, TaskEmailInfo p_emailInfo)
            throws RemoteException, WorkflowException
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        JbpmContext ctx = null;

        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();

            ProcessInstance processInstance = ctx
                    .getProcessInstance(p_workflowInstanceId);
            WorkflowProcessAdapter processAdapter = WorkflowProcessAdapter
                    .createInstance();
            List activityNodes = processAdapter
                    .workflowTaskInstances(processInstance);
            List<WorkflowTaskInstance> activeNodes = WorkflowProcessAdapter
                    .getActiveNodeInstances(activityNodes);

            for (WorkflowTaskInstance task : activeNodes)
            {

                Object[] args =
                { task.getName(), capLoginUrl(),
                        p_emailInfo.getPriorityAsString(),
                        p_emailInfo.getPageName(), p_emailInfo.getTime(),
                        p_emailInfo.getJobName(), p_emailInfo.getComments() };
                sendTaskActionEmailToUsers(
                        p_emailInfo.getProjectManagerId(),
                        getNodeAssignees(WorkflowJbpmUtil.getNodeById(
                                processInstance.getProcessDefinition(),
                                task.getTaskId()), processInstance),
                        p_emailInfo.getProjectIdAsLong(), null,
                        p_taskActionType, args);
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "notifyTaskParticipants: "
                            + e.toString()
                            + GlobalSightCategory.getLineContinuation()
                            + " p_workflowInstanceId="
                            + Long.toString(p_workflowInstanceId)
                            + " p_taskActionType="
                            + p_taskActionType
                            + " p_emailInfo="
                            + (p_emailInfo != null ? p_emailInfo.toString()
                                    : "null"), e);
        }
        finally
        {
            ctx.close();
        }
    }

    /**
     * Reassign all active/deactive activities of the given user to the project
     * manager.
     * 
     * @param p_userId
     *            - The user id of the assignee.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    @SuppressWarnings("unchecked")
    public void reassignUserActivitiesToPm(String p_userId)
            throws RemoteException, WorkflowException
    {
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            List taskInstances = WorkflowJbpmPersistenceHandler
                    .getNonRejectedTaskInstancesByAssignee(p_userId, ctx);

            for (Iterator it = taskInstances.iterator(); it.hasNext();)
            {
                TaskInstance ti = (TaskInstance) it.next();
                String pm = ti.getDescription();
                String actorId = ti.getActorId();
                if (actorId != null && p_userId.equals(actorId))
                {
                    // clear the actors
                    ti.setActorId(null);
                    // clear the start date
                    WorkflowJbpmUtil.setPrivateValue(ti, "start", null);
                }

                Set pooledActors = ti.getPooledActors();
                // add pm to new pooled actors
                if (pooledActors != null && !pooledActors.isEmpty())
                {
                    Set<String> actorIds = PooledActor
                            .extractActorIds(pooledActors);
                    if (actorIds != null && actorIds.contains(p_userId))
                    {
                        actorIds.remove(p_userId);
                        actorIds.add(pm);
                        ti.setPooledActors(WorkflowJbpmUtil
                                .toStringArray(actorIds));
                    }
                }
                // for GBS-1302, reassign interim activity
                TaskInterimPersistenceAccessor.reassignInterimActivity(ti);
            }
        }
        finally
        {
            ctx.close();
        }
    }

    /*
     * @see WorkflowServer.taskIdsInDefaultPath(long)
     */
    public long[] taskIdsInDefaultPath(long p_workflowInstanceId)
            throws WorkflowException, RemoteException
    {

        WorkflowInstance workflowInstance = WorkflowProcessAdapter
                .getProcessInstance(p_workflowInstanceId);

        // get the NodeInstances of TYPE_ACTIVITY
        List nodesInPath = ProcessImplDefaultPathFinder
                .activityNodesInDefaultPath(p_workflowInstanceId, -1, null,
                        WorkflowJbpmUtil.convertToArray(workflowInstance
                                .getWorkflowInstanceTasks()));

        // now loop through the nodes and get the task ids
        long[] taskIds = new long[nodesInPath.size()];
        for (int i = 0; i < nodesInPath.size(); i++)
        {
            taskIds[i] = ((WorkflowTaskInstance) nodesInPath.get(i))
                    .getTaskId();
        }

        return taskIds;
    }

    /**
     * @see WorkflowServer.timeDurationsInDefaultPath(long, long, String)
     * 
     */
    public List timeDurationsInDefaultPath(String p_destinationArrow,
            long p_workflowInstanceId, long p_startNodeId)
            throws WorkflowException, RemoteException
    {

        WorkflowInstance workflowInstance = WorkflowProcessAdapter
                .getProcessInstance(p_workflowInstanceId);

        // get the NodeInstances of TYPE_ACTIVITY
        List nodesInPath = workflowInstance.getDefaultPathNode();

        return convertToWfTaskInfos(nodesInPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.globalsight.everest.workflow.WorkflowServer#timeDurationsInDefaultPath
     * (long, long, java.lang.String,
     * com.globalsight.everest.workflow.WorkflowInstance)
     */
    public List timeDurationsInDefaultPath(long p_workflowInstanceId,
            long p_startNodeId, ArrorInfo p_destinationArrow,
            WorkflowInstance wfi) throws WorkflowException, RemoteException
    {

        List nodesInPath = wfi.getDefaultPathNode() == null
                || p_destinationArrow != null ? ProcessImplDefaultPathFinder
                .activityNodesInDefaultPath(p_workflowInstanceId,
                        p_startNodeId, p_destinationArrow, WorkflowJbpmUtil
                                .convertToArray(wfi.getWorkflowInstanceTasks()))
                : wfi.getDefaultPathNode();

        return convertToWfTaskInfos(nodesInPath);
    }

    /**
     * @see WorkflowServer.assignWorkflowOwners(long, String, String[])
     */
    public void reassignWorkflowOwners(long p_workflowInstanceId,
            String p_projectManagerId, String[] p_workflowOwners)
            throws RemoteException, WorkflowException
    {
        /*
         * In Jbpm implementation, we don't use the project currently. This
         * function can be refined or removed by further investigation
         */

    }

    public void signalProcess(ProcessInstance processInstance, String activity,
            List<WorkflowTaskInstance> taskInstances, long workflowId)
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();

            if (activity != null)
            {
                Token token = processInstance.getRootToken();
                for (WorkflowTaskInstance task : taskInstances)
                {

                    if (task.getActivityName().equals(activity))
                    {
                        break;
                    }

                    if (!task.getActivityName().equals(
                            WorkflowJbpmUtil.getTaskName(token.getNode()
                                    .getName())))
                    {
                        continue;
                    }

                    if (task.getActivityName().equals(
                            WorkflowJbpmUtil.getTaskName(token.getNode()
                                    .getName())))
                    {
                        String arrow = getArrowLabel(task);
                        TaskInstance taskInstance = WorkflowJbpmUtil
                                .getTaskInstanceByNode(processInstance,
                                        token.getNode());
                        if (StringUtil.isEmpty(arrow))
                        {
                            taskInstance.end();
                        }
                        else
                        {
                            taskInstance.setVariable(
                                    WorkflowConstants.VARIABLE_GOTO, arrow);
                            taskInstance.end();

                        }
                        try
                        {
                            // for GBS-1302, skip interim activity
                            TaskInterimPersistenceAccessor.skipInterimActivity(
                                    taskInstance.getTask().getTaskNode()
                                            .getId(), connection);
                        }
                        catch (Exception e)
                        {
                        }

                        WorkflowJbpmPersistenceHandler.saveSkipVariable(
                                taskInstance, workflowId);
                        continue;
                    }
                }

                removeSkip(processInstance);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Error found in signalProcess.", e);
        }
        finally
        {
            ConnectionPool.silentReturnConnection(connection);
        }
    }

    private void removeSkip(ProcessInstance processInstance)
    {
        ProcessDefinition processDefinition = processInstance
                .getProcessDefinition();
        Node exitNode = processDefinition.getNode(WorkflowConstants.END_NODE);
        WorkflowNodeParameter np = WorkflowNodeParameter
                .createInstance(exitNode);
        np.removeElement(WorkflowConstants.FIELD_SKIP);
        WorkflowJbpmUtil.setConfigure(exitNode, np.restore());
    }

    private String getArrowLabel(WorkflowTaskInstance task)
    {

        if (task.getOutgoingArrows().size() == 1
                && ((WorkflowArrow) task.getOutgoingArrows().get(0))
                        .getTargetNode().getType() != WorkflowConstants.CONDITION)
        {
            return null;
        }

        Vector arrows = task.getOutgoingArrows();

        for (Enumeration e = arrows.elements(); e.hasMoreElements();)
        {
            WorkflowArrow arrow = (WorkflowArrow) e.nextElement();
            WorkflowTaskInstance targetTask = (WorkflowTaskInstance) arrow
                    .getTargetNode();
            if (targetTask.getType() == WorkflowConstants.CONDITION)
            {
                Vector aarrows = targetTask.getOutgoingArrows();
                for (Enumeration ee = aarrows.elements(); ee.hasMoreElements();)
                {
                    WorkflowArrow aarrow = (WorkflowArrow) ee.nextElement();
                    if (targetTask.getConditionSpec()
                            .getBranchSpec(aarrow.getName()).isDefault())
                    {
                        return aarrow.getName();
                    }
                }
            }
        }

        return null;
    }

    public void setSkipActivity(List<Entry> list, String userId,
            boolean internal) throws RemoteException, WorkflowException
    {
        JbpmContext ctx = null;
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        Map<String, String> activityMap = new HashMap<String, String>();
        String taskName;

        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();

            for (Entry<String, String> entry : list)
            {

                String workflowId = entry.getKey();
                String activity = entry.getValue();
                ProcessInstance processInstance = ctx.getProcessInstance(Long
                        .valueOf(workflowId));

                boolean undispatched = processInstance.getRootToken().getNode()
                        .getName().equals(WorkflowConstants.START_NODE);

                if (!undispatched)
                {
                    taskName = WorkflowJbpmUtil.getTaskName(processInstance
                            .getRootToken().getNode().getName());
                    activityMap.put(workflowId, taskName);

                }

                map.put(workflowId, Boolean.valueOf(undispatched));

            }
        }
        finally
        {
            ctx.close();
        }

        for (Entry<String, String> entry : list)
        {
            String workflowId = entry.getKey();
            Workflow workflow = (Workflow) HibernateUtil.get(
                    WorkflowImpl.class, Long.valueOf(workflowId));
            try
            {
                int indexInDefaultPath = Integer.parseInt(entry.getHelp());
                for (int i = 0; i <= indexInDefaultPath; i++)
                {
                    if (!map.get(workflowId))
                    {
                        Iterator it = ServerProxy.getTaskManager()
                                .getCurrentTasks(workflow.getId()).iterator();
                        Task task = null;
                        if (it.hasNext())
                        {
                            task = (Task) (it.next());
                        }
                        else
                        {
                            return;
                        }
                        ServerProxy.getTaskManager().acceptTask(userId, task,
                                true);
                        if (i == indexInDefaultPath)
                        {
                            // i == indexInDefaultPath indicates this is the
                            // last
                            // activity to skip.
                            ServerProxy.getTaskManager().completeTask(userId,
                                    task, null, "LAST_SKIPPING");
                        }
                        else
                        {
                            ServerProxy.getTaskManager().completeTask(userId,
                                    task, null, "SKIPPING");
                        }
                        try
                        {
                            ctx = WorkflowConfiguration.getInstance()
                                    .getJbpmContext();
                            TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                                    .getTaskInstance(task.getId(), ctx);
                            WorkflowJbpmPersistenceHandler.saveSkipVariable(
                                    taskInstance, workflow.getId());
                        }
                        finally
                        {
                            ctx.close();
                        }
                    }
                }
            }
            finally
            {
                if (!"Exit".equals(entry.getValue()))
                {
                    // update workflow state back to DISPATCHED if it has not
                    // ended
                    JobCreationMonitor.updateWorkflowState(workflow,
                            Workflow.DISPATCHED);
                }
            }
        }

    }

    public void setSkipActivity(List<Entry> list, String userId)
            throws RemoteException, WorkflowException
    {
        JbpmContext ctx = null;
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        Map<String, String> activityMap = new HashMap<String, String>();
        String taskName;

        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();

            for (Entry<String, String> entry : list)
            {

                String workflowId = entry.getKey();
                String activity = entry.getValue();
                ProcessInstance processInstance = ctx.getProcessInstance(Long
                        .valueOf(workflowId));

                boolean undispatched = processInstance.getRootToken().getNode()
                        .getName().equals(WorkflowConstants.START_NODE);

                if (!undispatched)
                {
                    taskName = WorkflowJbpmUtil.getTaskName(processInstance
                            .getRootToken().getNode().getName());
                    activityMap.put(workflowId, taskName);
                }

                map.put(workflowId, Boolean.valueOf(undispatched));

            }
        }
        finally
        {
            ctx.close();
        }

        for (Entry<String, String> entry : list)
        {
            String workflowId = entry.getKey();
            String activity = entry.getValue();
            // int indexInDefaultPath = Integer.parseInt(request
            // .getParameter("activity_" + workflowId));
            // for (int i = 0; i <= indexInDefaultPath; i++)
            while (true)
            {
                try
                {
                    if (!map.get(workflowId))
                    {

                        Workflow workflow = (Workflow) HibernateUtil.get(
                                WorkflowImpl.class, Long.valueOf(workflowId));
                        Iterator it = ServerProxy.getTaskManager()
                                .getCurrentTasks(workflow.getId()).iterator();
                        Task task = null;
                        if (it.hasNext())
                        {
                            task = (Task) (it.next());
                            if (activity.equals(task.getTaskName()))
                                return;
                        }
                        else
                        {
                            return;
                        }
                        ServerProxy.getTaskManager().acceptTask(userId, task,
                                true);
                        if (task.getTaskName().equals("Exit"))
                        {
                            ServerProxy.getTaskManager().completeTask(userId,
                                    task, null, "LAST_SKIPPING");
                        }
                        else
                        {
                            ServerProxy.getTaskManager().completeTask(userId,
                                    task, null, "SKIPPING");
                        }
                        try
                        {
                            ctx = WorkflowConfiguration.getInstance()
                                    .getJbpmContext();
                            TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                                    .getTaskInstance(task.getId(), ctx);
                            WorkflowJbpmPersistenceHandler.saveSkipVariable(
                                    taskInstance, workflow.getId());
                        }
                        finally
                        {
                            ctx.close();
                        }
                    }
                }
                catch (Exception e)
                {
                    s_logger.error(e.getMessage(), e);
                }
            }
        }

    }

    private Task getTask(WorkflowImpl workflow, String activity)
    {
        Set<TaskImpl> taskSet = workflow.getTaskSet();

        for (TaskImpl task : taskSet)
        {
            if (activity.equals(task.getName()))
            {
                return task;
            }
        }

        return null;
    }

    private void setSkipActivityToNode(ProcessInstance processInstance,
            String activity)
    {
        ProcessDefinition processDefinition = processInstance
                .getProcessDefinition();
        Node exitNode = processDefinition.getNode(WorkflowConstants.END_NODE);
        WorkflowNodeParameter nodeParameter = WorkflowNodeParameter
                .createInstance(exitNode);
        nodeParameter.setAttribute(WorkflowConstants.FIELD_SKIP, activity);
        WorkflowJbpmUtil.setConfigure(exitNode, nodeParameter.restore());
    }

    // ////////////////////////////////////////////////////////////////////
    // End: WorkflowServer Implementation Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * ======= //
     * //////////////////////////////////////////////////////////////////// //
     * Begin: Package-scope Methods //
     * //////////////////////////////////////////////////////////////////// /**
     * >>>>>>> .r2615 Performs the necessary steps when the system start-up is
     * invoked.
     */
    void startup() throws SystemStartupException
    {
        s_logger.info("--- jBPM configuration loading... ---");
        WorkflowConfiguration.getInstance();
        s_logger.info("--- jBPM configuration loaded... ---");
    }

    /**
     * Closes the jBPM configuration when the system shutdown is invoked.
     * 
     * @throws SystemShutdownException
     */
    void shutdown()
    {
        WorkflowConfiguration.getInstance().logout();
        s_logger.info("--- jBPM configuration closed ---");
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Support Methods
    // ////////////////////////////////////////////////////////////////////

    /*
     * Convert a list of NodeInstance objects to WfTaskInfo objects.
     */
    @SuppressWarnings("unchecked")
    private List convertToWfTaskInfos(List p_nodesInPath)
    {

        List taskInfos = new ArrayList(p_nodesInPath.size());

        for (Iterator it = p_nodesInPath.iterator(); it.hasNext();)
        {
            WorkflowTaskInstance taskInstance = (WorkflowTaskInstance) it
                    .next();

            long acceptanceDuration = taskInstance.getAcceptTime();
            long completionDuration = taskInstance.getCompletedTime();

            String[] roles = taskInstance.getRoles();

            WfTaskInfo taskInfo = new WfTaskInfo(taskInstance.getTaskId(),
                    taskInstance.getName(), acceptanceDuration,
                    completionDuration, roles, taskInstance.getTaskState(),
                    isFollowedByExit(taskInstance));
            taskInfo.setOverdueToPM(taskInstance.getOverdueToPM());
            taskInfo.setOverdueToUser(taskInstance.getOverdueToUser());
            taskInfos.add(taskInfo);
        }

        return taskInfos;

    }

    private boolean isFollowedByExit(WorkflowTaskInstance taskInstance)
    {
        boolean followedByExit = false;
        Vector arrows = taskInstance.getOutgoingArrows();

        Enumeration e = arrows.elements();
        while (e.hasMoreElements())
        {
            WorkflowArrow arrow = (WorkflowArrow) e.nextElement();
            followedByExit = arrow.getTargetNode().getType() == WorkflowConstants.STOP;
            if (followedByExit)
            {
                break;
            }
        }

        return followedByExit;
    }

    /**
     * Activate a new task (and deactivate the old one).
     * 
     */

    private void activateTaskInstance(ProcessInstance p_processInstance,
            long p_taskId, String[] p_newRoles, String[] p_roles,
            String p_oldActivityName, DefaultPathTasks p_taskInfos,
            TaskEmailInfo p_emailInfo) throws Exception
    {
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            TaskInstance ti = WorkflowJbpmPersistenceHandler.getTaskInstance(
                    p_taskId, ctx);
            Integer unscheduledEventType = 0;
            String[] previousAssignees = new String[p_roles.length];
            for (int i = 0; i < p_roles.length; i++)
            {
                previousAssignees[i] = retrieveUserIdFromRole(p_roles[i]);
            }
            String[] newAssignees = new String[p_newRoles.length];
            for (int i = 0; i < p_newRoles.length; i++)
            {
                newAssignees[i] = retrieveUserIdFromRole(p_newRoles[i]);
            }

            ti.setActorId(null);
            ti.setPooledActors(newAssignees);
            WorkflowJbpmUtil.setPrivateValue(ti, "start", null);
            // for GBS-1302, reassign interim activity
            TaskInterimPersistenceAccessor.reassignInterimActivity(ti);
            unscheduledEventType = (Integer) SchedulerConstants.s_eventTypes
                    .get(SchedulerConstants.ACCEPT_TYPE);

            TaskInfo taskInfo = p_taskInfos.getTaskInfoById(p_taskId);
            // after a reassign we have to stop
            // existing timer and create a new one
            if (isNotificationActive())
            {
                EventNotificationHelper.performSchedulingProcess(new Integer(
                        SchedulerConstants.MODIFY_WORKFLOW), p_taskId,
                        unscheduledEventType, ti.getTask().getTaskNode(),
                        taskInfo, EventNotificationHelper.getCurrentTime(),
                        (Integer) SchedulerConstants.s_eventTypes
                                .get(SchedulerConstants.COMPLETE_TYPE),
                        getWarningThreshold(), p_emailInfo);
            }

            String companyIdStr = p_emailInfo.getCompanyId();
            String pmIdStr = p_emailInfo.getProjectManagerId();

            Object[] deactiveArgs =
            {
                    p_oldActivityName,
                    UserUtil.getUserNameById(pmIdStr),
                    capLoginUrl(),
                    p_emailInfo.getJobName(),
                    WorkflowHelper.localePair(p_emailInfo.getSourceLocale(),
                            p_emailInfo.getTargetLocale(), "en_US"),
                    p_emailInfo.getPriorityAsString() };
            Object[] activeArgs =
            {
                    WorkflowJbpmUtil
                            .getActivityName(ti.getTask().getTaskNode()),
                    capLoginUrl(),
                    p_emailInfo.getPriorityAsString(),
                    taskInfo.getAcceptByDate(),
                    taskInfo.getCompleteByDate(),
                    p_emailInfo.getJobName(),
                    WorkflowHelper.localePair(p_emailInfo.getSourceLocale(),
                            p_emailInfo.getTargetLocale(), "en_US") };

            // notify deactivated and activated task assignees
            sendTaskActionEmailToUsers(pmIdStr, previousAssignees,
                    p_emailInfo.getProjectIdAsLong(), null,
                    WorkflowMailerConstants.CANCEL_TASK, deactiveArgs);

            sendTaskActionEmailToUsers(pmIdStr, newAssignees,
                    p_emailInfo.getProjectIdAsLong(), null,
                    WorkflowMailerConstants.REASSIGN_TASK, activeArgs);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "commitTaskActivation: " + e.toString()
                            + GlobalSightCategory.getLineContinuation()
                            + " p_processInstance="
                            + WorkflowHelper.toDebugString(p_processInstance)
                            + " node=" + WorkflowHelper.toDebugString(p_taskId)
                            + " p_emailInfo="
                            + WorkflowHelper.toDebugString(p_emailInfo), e);
            String args[] =
            { String.valueOf(p_taskId) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_COMMIT_TASK_ACTIVATION,
                    args, e);
        }
        finally
        {
            ctx.close();
        }
    }

    private String retrieveUserIdFromRole(String p_role)
    {
        String[] ss = p_role.split(" ");
        StringBuilder sb = new StringBuilder();
        // 1000 Translation1_1000 en_US de_DE leo anyone
        int nameSize = ss.length - 4;
        for (int i = 0; i < nameSize; i++)
        {
            sb.append(ss[i + 4]);
            if (i + 1 < nameSize)
            {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /* Return a Map of WorkflowTaskInstances from the process instance. The */
    /* key of the map is WorkflowConstant.IS_NEW or IS_DELETED. The value for */
    /*
     * >>>>>>> .r2615 each key would be a list of newly added tasks and deleted
     * ones repectively
     */
    private Map newAndDeletedTasks(List p_originalTasks, List p_modifiedTasks)
            throws Exception
    {
        int size = p_originalTasks.size();
        Map<String, List> returnMap = new HashMap<String, List>(size);

        Map originalMap = WorkflowProcessAdapter
                .makeWorkflowTaskInstanceMap(p_originalTasks);

        Map modifiedMap = WorkflowProcessAdapter
                .makeWorkflowTaskInstanceMap(p_modifiedTasks);

        List<WorkflowTaskInstance> deletedWorkflowTaskInstances = new ArrayList<WorkflowTaskInstance>(
                size);

        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance original = (WorkflowTaskInstance) p_originalTasks
                    .get(i);
            WorkflowTaskInstance modified = (WorkflowTaskInstance) modifiedMap
                    .get(new Long(original.getTaskId()));
            if (modified == null)
            {
                deletedWorkflowTaskInstances.add(original);
            }
        }
        // Loop through modifieds and find ones not in originals
        // Mark these as IS_NEW.
        size = p_modifiedTasks.size();
        List<WorkflowTaskInstance> newWorkflowTaskInstances = new ArrayList<WorkflowTaskInstance>(
                size);
        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance modified = (WorkflowTaskInstance) p_modifiedTasks
                    .get(i);
            if (originalMap.get(new Long(modified.getTaskId())) == null)
            {
                newWorkflowTaskInstances.add(modified);
            }
        }
        returnMap.put(WorkflowConstants.IS_DELETED,
                deletedWorkflowTaskInstances);
        returnMap.put(WorkflowConstants.IS_NEW, newWorkflowTaskInstances);
        return returnMap;
    }

    private void performNodeActivation(List p_updatedTasks,
            ProcessInstance p_processInstance,
            ProcessDefinition p_processDefinition, List p_wfTaskInstances,
            DefaultPathTasks p_taskInfos, TaskEmailInfo p_emailInfo)
            throws Exception
    {
        List activeNodes = WorkflowProcessAdapter
                .getActiveNodeInstances(p_updatedTasks);

        Map map = addEmailInfo(p_processInstance, p_wfTaskInstances,
                activeNodes, p_emailInfo);

        int mapSize = map.size();
        Integer listSize = (Integer) map.get(WorkflowConstants.LIST_SIZE);
        int size = listSize.intValue();

        // at least one of the durations of active node has changed
        if (mapSize == size)
        { // TomyD -- TBD - need to know the create or
          // accepted date here.
            /*
             * Map durations = (Map)map.get(WorkflowConstants.UPDATE_EVENT);
             * EventNotificationHelper.determineEventUpdate(durations,
             * p_emailInfo, isNotificationActive(), getWarningThreshold());
             */
        }
        else if (map.size() > size)
        {

            for (int i = 0; i < size; i++)
            {
                Boolean isNew = (Boolean) map.get(WorkflowConstants.IS_NEW + i);
                if (isNew == null)
                {
                    continue;
                }

                String[] newRoles = null;
                String[] roles = null;
                String oldActivityName = null;
                long taskId = ((Long) map.get(WorkflowConstants.TASK_ID + i))
                        .longValue();

                if (!isNew.booleanValue())
                {
                    newRoles = (String[]) map
                            .get(WorkflowConstants.ROLE_FOR_ACTIVATE + i);
                    roles = (String[]) map
                            .get(WorkflowConstants.ROLE_FOR_DEACTIVATE + i);

                    oldActivityName = (String) map
                            .get(WorkflowConstants.NAME_FOR_DEACTIVATE + i);
                }
                activateTaskInstance(p_processInstance, taskId, newRoles,
                        roles, oldActivityName, p_taskInfos, p_emailInfo);
            }
        }
    }

    /**
     * Adds the info required for sending email for reroute/reassign
     * 
     * @param p_wfTaskInstances
     *            The original {@WorkflowTaskInstance}
     *            list.
     * @param p_activeNodes
     *            The new {@code WorkflowTaskInstance} list.
     */
    private Map addEmailInfo(ProcessInstance p_processInstance,
            List p_wfTaskInstances, List p_activeNodes,
            TaskEmailInfo p_emailInfo) throws Exception
    {
        // Get original active nodes as a hash map (id, task).
        Map originalActiveTasks = WorkflowProcessAdapter
                .getActiveWfTaskInstances(p_wfTaskInstances);

        Map<String, Object> map = new HashMap<String, Object>(4);
        // Loop thru the current nodes
        int size = p_activeNodes.size();
        map.put(WorkflowConstants.LIST_SIZE, new Integer(size));
        String companyIdStr = p_emailInfo.getCompanyId();
        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance workflowTaskInstance = (WorkflowTaskInstance) p_activeNodes
                    .get(i);

            long activeNodeId = workflowTaskInstance.getTaskId();
            WorkflowTaskInstance wfti = (WorkflowTaskInstance) originalActiveTasks
                    .get(new Long(activeNodeId));

            if (wfti != null)
            {
                String[] newRoles = workflowTaskInstance.getRoles();
                String[] oriRoles = wfti.getRoles();
                if (!workflowTaskInstance.getRoleType())
                {
                    // not user role (to get container roles)
                    newRoles = getAllQualifiedUserRoles(workflowTaskInstance);
                }

                if (!wfti.getRoleType())
                {
                    // not user role (to get container roles)
                    oriRoles = getAllQualifiedUserRoles(wfti);
                }

                if (haveRolesChanged(newRoles, oriRoles))
                {
                    map.put(WorkflowConstants.IS_NEW + i, Boolean.FALSE);
                    map.put(WorkflowConstants.TASK_ID + i, new Long(
                            activeNodeId));
                    map.put(WorkflowConstants.ROLE_FOR_ACTIVATE + i, newRoles);
                    map.put(WorkflowConstants.ROLE_FOR_DEACTIVATE + i, oriRoles);
                    Node node = WorkflowJbpmUtil.getNodeByWfTask(
                            p_processInstance, wfti);
                    map.put(WorkflowConstants.NAME_FOR_DEACTIVATE + i, wfti
                            .getActivity() != null ? wfti.getActivity()
                            .getActivityName() : wfti.getName());
                }

                // we need to add info for change in durations or role. This
                // info is used on event notification update.
                Map durationChangeMap = EventNotificationHelper
                        .durationChangeMap(workflowTaskInstance,
                                wfti.getAcceptTime(), wfti.getCompletedTime());
                // it's possible that at least one duration (accept/complete) is
                // updated
                map.put(WorkflowConstants.UPDATE_EVENT + i, durationChangeMap);
            }
            else
            // we have a new active node
            {
                map.put(WorkflowConstants.IS_NEW + i, Boolean.TRUE);
                map.put(WorkflowConstants.TASK_ID + i, new Long(activeNodeId));
            }
        }

        return map;
    }

    /**
     * Gets the user roles based on the all qualified role selection.
     */
    private String[] getAllQualifiedUserRoles(WorkflowTaskInstance wfti)
            throws Exception
    {
        Activity activity = wfti.getActivity();
        // 1000 Translation1_1000 en_US de_DE
        String roleAsString = wfti.getRolesAsString();
        String sourceLocale = roleAsString.split(" ")[2];
        String targetLocale = roleAsString.split(" ")[3];
        List userInfos = ServerProxy.getUserManager().getUserInfos(
                activity.getActivityName(), sourceLocale, targetLocale);

        String[] userRoles = new String[userInfos.size()];
        for (int i = 0; i < userInfos.size(); i++)
        {
            UserInfo ui = (UserInfo) userInfos.get(i);
            // make the user role like
            // "1000 Translation1_1000 en_US de_DE leoanyone"
            // to make sure retrieveUserIdFromRole() read the correct user name
            // in next step
            userRoles[i] = "roleId activityName sourceLocale targetLocale "
                    + ui.getUserId();
        }
        return userRoles;
    }

    /* Return the login URL for CAP */
    private String capLoginUrl()
    {
        if (m_capLoginUrl == null)
        {
            m_capLoginUrl = getSystemConfigValue(SystemConfigParamNames.CAP_LOGIN_URL);
        }
        return m_capLoginUrl;
    }

    /**
     * Determines whether the two string arrays have different set of strings.
     * This is done to find out whether the current roles are different from the
     * previous ones.
     */
    private boolean haveRolesChanged(String[] p_currentRoles,
            String[] p_previousRoles)
    {
        if (p_currentRoles.length != p_previousRoles.length)
        {
            return true;
        }
        for (int i = 0; i < p_currentRoles.length; i++)
        {
            String newUserId = retrieveUserIdFromRole(p_currentRoles[i]);
            String oriUserId = retrieveUserIdFromRole(p_previousRoles[i]);
            if (!newUserId.equals(oriUserId))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the next node in the workflow, from Task. If there is no next, then
     * return NULL.
     * 
     * @param p_task
     *            Currenct task
     * @param p_arrowLabel
     *            Next label name(optional)
     * @param p_skipToAcitivity
     *            activity name(optional)
     * @return
     * @throws Exception
     */
    public WorkflowTaskInstance nextNodeInstances(Task p_task,
            String p_arrowLabel, String p_skipToAcitivity) throws Exception
    {
        WorkflowTaskInstance wti = null;
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance processInstance = ctx.getProcessInstance(p_task
                    .getWorkflow().getId());
            Node node = WorkflowJbpmUtil.getNodeById(
                    processInstance.getProcessDefinition(), p_task.getId());

            NextNodes nextNodes = nextNodeInstances(node.getName(),
                    WorkflowProcessAdapter.getProcessInstance(processInstance)
                            .getWorkflowInstanceTasks(), p_arrowLabel,
                    p_skipToAcitivity);
            if (nextNodes.size() > 0)
            {
                wti = (WorkflowTaskInstance) nextNodes.getNode(0);
            }
        }
        finally
        {
            ctx.close();
        }

        return wti;
    }

    /**
     * Return the node instances that will be activated after the current one.
     * Note that no Exit/Stop node will be returned
     * 
     * @param p_workItem
     *            - The current active work item.
     * @param p_nodeInstances
     *            - An array of node instances.
     * @param p_arrowLabel
     *            - The possible outgoing arrow label of a condition node (if
     *            any).
     */

    private NextNodes nextNodeInstances(String p_nodeName,
            Vector p_nodeInstances, String p_arrowLabel, String skipToAcitivity)
    {

        WorkflowTaskInstance taskInstance = null;

        if (skipToAcitivity != null)
        {
            taskInstance = WorkflowJbpmUtil.getTaskInstanceByActivity(
                    p_nodeInstances, skipToAcitivity);
        }
        else if (StringUtil.isEmpty(p_nodeName))
        {
            // if work item is null, we're starting a workflow (find start node)
            taskInstance = WorkflowJbpmUtil.getStartNode(p_nodeInstances);
        }
        else
        {
            taskInstance = WorkflowJbpmUtil.getCurrentTaskInstance(
                    p_nodeInstances, p_nodeName);
        }

        return WorkflowProcessAdapter.nextNodeInstances(taskInstance,
                p_arrowLabel);
    }

    //

    private String getSystemActionTypeForNode(WorkflowTaskInstance p_node,
            ProcessInstance p_pi)
    {
        Node node = WorkflowJbpmUtil.getNodeByWfTask(p_pi, p_node);
        return getSystemActionTypeForNode(node, p_pi);
    }

    private String getSystemActionTypeForNode(Node p_node, ProcessInstance p_pi)
    {

        WorkflowNodeParameter nodeParameter = WorkflowNodeParameter
                .createInstance(WorkflowJbpmUtil.getConfigure(p_node));

        return nodeParameter.getAttribute(WorkflowConstants.FIELD_ACTION_TYPE,
                WorkflowTaskInstance.NO_ACTION);
    }

    /*
     * Return a formatted string for the given timestamp, base on the given
     * locale
     */
    private String format(Date p_date, Locale p_locale, TimeZone p_timeZone)
    {
        m_timestamp.setDate(p_date);
        m_timestamp.setLocale(p_locale);
        m_timestamp.setTimeZone(p_timeZone);
        return m_timestamp.toString();
    }

    // get a system configuration value for a given name.
    private String getSystemConfigValue(String p_paramName)
    {
        String value = null;
        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            value = config.getStringParameter(p_paramName);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "WorkflowServerLocal :: getSystemConfigValue for parameter name: "
                            + p_paramName, e);
        }

        return value;
    }

    /**
     * Perform an enactment edit and update the Assignee UDA value for the
     * process instance.
     * 
     */
    private String[] setAssigneesOfNextNodes(ProcessInstance p_pi,
            NextNodes p_nextNodes, DefaultPathTasks p_taskInfos,
            TaskEmailInfo p_emailInfo)
    {

        String[] assignees = null;
        try
        {
            Date newDate = new Date();
            int sz = p_nextNodes.size();

            for (int i = 0; i < sz; i++)
            {
                WorkflowTaskInstance node = (WorkflowTaskInstance) p_nextNodes
                        .getNode(i);

                if (node.getType() != WorkflowConstants.ACTIVITY)
                {
                    continue;
                }

                TaskInfo taskInfo = p_taskInfos.getTaskInfoById(node
                        .getTaskId());

                String roles = node.getRolesAsString();
                String preference = node.getRolePreference();

                // get the user ids of all the members of the roles
                String[] userIds = AssigneeFilter.getAssigneeList(roles
                        .split(","), p_emailInfo.getProjectIdAsLong()
                        .longValue());

                // not only get the assignees based on the role preference (if
                // any)
                assignees = WorkflowProcessAdapter.updateAssignees(userIds,
                        p_pi, node, newDate, taskInfo, preference);

                boolean shouldNotify = assignees == null
                        && WorkflowConstants.AVAILABLE_ROLE_PREFERENCE
                                .equals(preference);
                if (shouldNotify)
                {
                    notifyResourceUnavailability(p_emailInfo, node.getName(),
                            taskInfo);
                }
            }

        }
        catch (Exception e)
        {
            try
            {

            }
            catch (Exception ex)
            {
                s_logger.error("Failed to cancel editing of process with id: "
                        + p_pi.getId());
            }
            s_logger.error(
                    "Failed to set node assignees of workflow: " + p_pi.getId(),
                    e);
        }

        return assignees;
    }

    /**
     * @param p_node
     * @param p_pi
     * @return
     */
    private String[] getNodeAssignees(Node p_node, ProcessInstance p_pi)
    {
        WorkflowNodeParameter nodeParameter = WorkflowNodeParameter
                .createInstance(WorkflowJbpmUtil.getConfigure(p_node));
        return nodeParameter.getArrayAttribute(WorkflowConstants.FIELD_ROLE_ID);
    }

    /*
     * TomyD -- Temp for Customer Upload feature. Add iFlow's admin user as the
     * process owner so he can take more actions on a process instance or work
     * item.
     */
    private void setAdminAsOwner(ProcessInstance p_processInstance)
            throws Exception
    {
        /* It seems the owner never be used in workflow related operation */
    }

    /**
     * Sends the import initiator an email that this review-only task is ready
     * for acceptance. This sends an email to the person that initiated the
     * first file in the job.
     */
    private void notifyImportInitiator(TaskInfo p_taskInfo,
            TaskEmailInfo p_emailInfo, int p_mailerMessageType, Object p_args[])
    {
        XmlParser xmlParser = null;
        try
        {
            Task task = (Task) ServerProxy.getTaskManager().getTask(
                    p_taskInfo.getId());
            String companyIdStr = String.valueOf(task.getCompanyId());
            List sourcePages = task.getSourcePages();
            SourcePage sp1 = (SourcePage) sourcePages.get(0);
            String efxml = sp1.getRequest().getEventFlowXml();

            // parse the efxml for the import initiator id
            xmlParser = XmlParser.hire();

            Document document = xmlParser.parseXml(efxml);
            Element root = document.getRootElement();
            org.dom4j.Node node = root
                    .selectSingleNode("/eventFlowXml/source/@importInitiatorId");
            if (node != null)
            {
                String importInitatorId = node.getText();
                if (importInitatorId != null && importInitatorId.length() > 0)
                {
                    EmailInformation emailInfo = getEmailInfo(importInitatorId);
                    s_logger.debug("Emailing about review-only task to import initator "
                            + importInitatorId);
                    String subject = getEmailSubject(p_mailerMessageType);
                    String message = getEmailMessage(p_mailerMessageType);
                    sendMail(importInitatorId, emailInfo, subject, message,
                            p_args, companyIdStr);
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to send e-mail to the import initiator about a review-only task.",
                    e);
        }
        finally
        {
            if (xmlParser != null)
            {
                XmlParser.fire(xmlParser);
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Private Support Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Email notification methods
    // ////////////////////////////////////////////////////////////////////
    /* Return the administrator's email address */
    private String adminEmailAddress()
    {
        if (m_adminEmailAddress == null)
        {
            m_adminEmailAddress = getSystemConfigValue(SystemConfigParamNames.ADMIN_EMAIL);
        }
        return m_adminEmailAddress;
    }

    /*
     * Perform the advance task notification. Also remove a completion event
     * that was scheduled (if it's not already been fired) and create new event
     * for the next (if any) task.
     */
    private ArrayList advanceTaskNotification(String p_assignee,
            long p_nodeInstanceId, TaskInfo p_taskInfo,
            TaskEmailInfo p_emailInfo, Node p_node,
            ProcessInstance p_processInstance, String skipping,
            String p_taskType)
    {
        ArrayList emailinfo = notifyTaskIsAdvanced(p_assignee, p_emailInfo,
                p_node, p_taskInfo, p_processInstance, skipping, p_taskType);

        // Fist stop the "complete by" timer of the node that was finished.
        // Then create a "accpet by" timer for the next activity.
        if (isNotificationActive())
        {
            int actionType = SchedulerConstants.FINISH_ACTIVITY;
            if (skipping != null)
            {
                if (skipping.startsWith("LAST"))
                {
                    actionType = SchedulerConstants.SKIP_FINISH_ACTIVITY;
                }
                else
                {
                    actionType = SchedulerConstants.SKIP_ACTIVITY;
                }
            }
            EventNotificationHelper.performSchedulingProcess(new Integer(
                    actionType), p_nodeInstanceId,
                    (Integer) SchedulerConstants.s_eventTypes
                            .get(SchedulerConstants.COMPLETE_TYPE), p_node,
                    p_taskInfo, EventNotificationHelper.getCurrentTime(),
                    (Integer) SchedulerConstants.s_eventTypes
                            .get(SchedulerConstants.ACCEPT_TYPE),
                    getWarningThreshold(), p_emailInfo);
        }

        return emailinfo;
    }

    /* Get email address of a user based on the user name */
    private EmailInformation getEmailInfo(String p_userName) throws Exception
    {
        return ServerProxy.getUserManager().getEmailInformationForUser(
                p_userName);
    }

    /* Retrieve the appropriate email subject key for the particular task. */
    private String getEmailSubject(int p_messageType)
    {
        String subject = null;
        switch (p_messageType)
        {
            case WorkflowMailerConstants.ACCEPT_TASK:
                subject = WorkflowMailerConstants.ACCEPT_TASK_SUBJECT;
                break;
            case WorkflowMailerConstants.ACTIVATE_TASK:
                subject = WorkflowMailerConstants.ACTIVATE_TASK_SUBJECT;
                break;
            case WorkflowMailerConstants.ACTIVATE_REVIEW_TASK:
                subject = WorkflowMailerConstants.ACTIVATE_REVIEW_TASK_SUBJECT;
                break;
            case WorkflowMailerConstants.CANCEL_TASK:
                subject = WorkflowMailerConstants.CANCEL_TASK_SUBJECT;
                break;
            case WorkflowMailerConstants.COMPLETED_TASK:
                subject = WorkflowMailerConstants.COMPLETED_TASK_SUBJECT;
                break;
            case WorkflowMailerConstants.COMPLETED_WFL:
                subject = WorkflowMailerConstants.COMPLETED_WFL_SUBJECT;
                break;
            case WorkflowMailerConstants.COMPLETED_JOB:
                subject = WorkflowMailerConstants.COMPLETED_JOB_SUBJECT;
                break;
            case WorkflowMailerConstants.NO_AVAILABLE_RESOURCE:
                subject = WorkflowMailerConstants.NO_AVAILABLE_RESOURCE_SUBJECT;
                break;
            case WorkflowMailerConstants.PAGE_EXPORTED_FOR_UPDATE:
                subject = WorkflowMailerConstants.PAGE_EXPORTED_FOR_UPDATE_SUBJECT;
                break;
            case WorkflowMailerConstants.PAGE_REIMPORTED:
                subject = WorkflowMailerConstants.PAGE_REIMPORTED_SUBJECT;
                break;
            case WorkflowMailerConstants.REASSIGN_TASK:
                subject = WorkflowMailerConstants.REASSIGN_TASK_SUBJECT;
                break;
            case WorkflowMailerConstants.REJECT_TASK:
                subject = WorkflowMailerConstants.REJECT_TASK_SUBJECT;
                break;
        }
        return subject;
    }

    /*
     * Retrieve the appropriate email message key according for the particular
     * task.
     */
    private String getEmailMessage(int p_messageType)
    {
        String message = null;
        switch (p_messageType)
        {
            case WorkflowMailerConstants.ACCEPT_TASK:
                message = WorkflowMailerConstants.ACCEPT_TASK_MESSAGE;
                break;
            case WorkflowMailerConstants.ACTIVATE_TASK:
                message = WorkflowMailerConstants.ACTIVATE_TASK_MESSAGE;
                break;
            case WorkflowMailerConstants.ACTIVATE_REVIEW_TASK:
                message = WorkflowMailerConstants.ACTIVATE_REVIEW_TASK_MESSAGE;
                break;
            case WorkflowMailerConstants.CANCEL_TASK:
                message = WorkflowMailerConstants.CANCEL_TASK_MESSAGE;
                break;
            case WorkflowMailerConstants.COMPLETED_TASK:
                message = WorkflowMailerConstants.COMPLETED_TASK_MESSAGE;
                break;
            case WorkflowMailerConstants.COMPLETED_WFL:
                message = WorkflowMailerConstants.COMPLETED_WFL_MESSAGE;
                break;
            case WorkflowMailerConstants.COMPLETED_JOB:
                message = WorkflowMailerConstants.COMPLETED_JOB_MESSAGE;
                break;
            case WorkflowMailerConstants.NO_AVAILABLE_RESOURCE:
                message = WorkflowMailerConstants.NO_AVAILABLE_RESOURCE_MESSAGE;
                break;
            case WorkflowMailerConstants.PAGE_EXPORTED_FOR_UPDATE:
                message = WorkflowMailerConstants.PAGE_EXPORTED_FOR_UPDATE_MESSAGE;
                break;
            case WorkflowMailerConstants.PAGE_REIMPORTED:
                message = WorkflowMailerConstants.PAGE_REIMPORTED_MESSAGE;
                break;
            case WorkflowMailerConstants.REASSIGN_TASK:
                message = WorkflowMailerConstants.REASSIGN_TASK_MESSAGE;
                break;
            case WorkflowMailerConstants.REJECT_TASK:
                message = WorkflowMailerConstants.REJECT_TASK_MESSAGE;
                break;
        }
        return message;
    }

    /* Return the remote reference of the mailer interface. */
    private MailerWLRemote getMailer() throws Exception
    {
        if (m_mailHandler == null)
        {
            m_mailHandler = ServerProxy.getMailer();
        }
        return m_mailHandler;
    }

    /**
     * Notify PM and WFM that no resources were available to work on a
     * particular task (for a 'available' role preference). Therefore, by
     * default all it'll be assigned to all assignees.
     */
    private void notifyResourceUnavailability(TaskEmailInfo p_emailInfo,
            String p_activityName, TaskInfo p_taskInfo)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        Object[] args =
        {
                p_activityName,
                p_emailInfo.getPriorityAsString(),
                p_emailInfo.getJobName(),
                WorkflowHelper.localePair(p_emailInfo.getSourceLocale(),
                        p_emailInfo.getTargetLocale(), "en_US"), capLoginUrl() };
        sendTaskActionEmailToUser(null, p_emailInfo, null,
                WorkflowMailerConstants.NO_AVAILABLE_RESOURCE, args);
    }

    /**
     * notify a project manager about a completed task, and then let the users
     * of the next task know about it. This method is also used when a workflow
     * instance has started.
     * 
     * @param p_taskType
     *            task/activity type(new/accept/complete)
     */
    private ArrayList notifyTaskIsAdvanced(String p_assignee,
            TaskEmailInfo p_emailInfo, Node p_node, TaskInfo p_taskInfo,
            ProcessInstance processInstance, String skipping, String p_taskType)
    {
        ArrayList emailInfos = new ArrayList();
        if (!m_systemNotificationEnabled)
        {
            return null;
        }

        try
        {
            // The args don't include restricted attachment informations.
            String companyId = CompanyThreadLocal.getInstance().getValue();
            String companyName = ServerProxy.getJobHandler()
                    .getCompanyById(Long.parseLong(companyId)).getName();
            p_emailInfo.setCompanyId(companyId);
           
            if (p_assignee != null)
            {
                Object[] args =
                {
                        WorkflowJbpmUtil.getActivityNameWithArrowName(
                                p_emailInfo.getPreNode(), "_" + companyId,
                                processInstance, p_taskType),
                        UserUtil.getUserNameById(p_assignee),
                        capLoginUrl(),
                        p_emailInfo.getPriorityAsString(),
                        p_emailInfo.getJobName(),
                        WorkflowHelper.localePair(
                                p_emailInfo.getSourceLocale(),
                                p_emailInfo.getTargetLocale(), "en_US"), 
                                p_emailInfo.getComments()};
                if (skipping == null)
                {
                    sendTaskActionEmailToUser(p_assignee, p_emailInfo, null,
                            WorkflowMailerConstants.COMPLETED_TASK, args);

                }
            }

            if (p_node == null)
            {

                return null;
            }

            // notify the appropriate users about the next task (if there is
            // any)
            // -1 is the one before the first node at sequence 0

            if (p_node != null)
            {
                String jobName = p_emailInfo.getJobName();
                if (jobName != null)
                {
                    jobName = jobName.replaceAll(" ", "%20");
                }

                String linkUrl = capLoginUrl() + "/exports?"
                        + "activityName=downloadXliff&xliff=true"
                        + "&companyName=" + companyName + "&file=" + jobName
                        + "_" + p_emailInfo.getSourceLocale() + "_"
                        + p_emailInfo.getTargetLocale() + ".zip";

                String activityName = WorkflowJbpmUtil
                        .getActivityNameWithArrowName(p_node, "_" + companyId,
                                processInstance,
                                WorkflowConstants.TASK_TYPE_NEW);

                Object[] args =
                {
                        activityName,
                        capLoginUrl(),
                        p_emailInfo.getPriorityAsString(),
                        p_taskInfo.getAcceptByDate(),
                        p_taskInfo.getCompleteByDate(),
                        p_emailInfo.getJobName(),
                        WorkflowHelper.localePair(
                                p_emailInfo.getSourceLocale(),
                                p_emailInfo.getTargetLocale(), "en_US"),
                        String.valueOf(p_emailInfo
                                .getSegmentTmMatchesWordCount()),
                        String.valueOf(p_emailInfo.getLeverageMatchThreshold()),
                        String.valueOf(p_emailInfo
                                .getTotalFuzzyMatchesWordCount()),
                        String.valueOf(p_emailInfo.getNoMatchesWordCount()),
                        String.valueOf(p_emailInfo.getRepetitionsWordCount()),
                        p_emailInfo.getJobId(), p_emailInfo.getProjectName(),
                        String.valueOf(p_emailInfo.getTotalWordCount()),
                        p_emailInfo.getComments(), p_emailInfo.getAttachment(),
                        linkUrl };

                // The args include restricted attachment informations.
                Object[] restrictArgs =
                {
                        activityName,
                        capLoginUrl(),
                        p_emailInfo.getPriorityAsString(),
                        p_taskInfo.getAcceptByDate(),
                        p_taskInfo.getCompleteByDate(),
                        p_emailInfo.getJobName(),
                        WorkflowHelper.localePair(
                                p_emailInfo.getSourceLocale(),
                                p_emailInfo.getTargetLocale(), "en_US"),
                        String.valueOf(p_emailInfo
                                .getSegmentTmMatchesWordCount()),
                        String.valueOf(p_emailInfo.getLeverageMatchThreshold()),
                        String.valueOf(p_emailInfo
                                .getTotalFuzzyMatchesWordCount()),
                        String.valueOf(p_emailInfo.getNoMatchesWordCount()),
                        String.valueOf(p_emailInfo.getRepetitionsWordCount()),
                        p_emailInfo.getJobId(), p_emailInfo.getProjectName(),
                        String.valueOf(p_emailInfo.getTotalWordCount()),
                        p_emailInfo.getRestrictComments(),
                        p_emailInfo.getRestrictAttachment(), linkUrl };

                // The args don't include job comment informations.
                Object[] noJobCommentArgs =
                {
                        activityName,
                        capLoginUrl(),
                        p_emailInfo.getPriorityAsString(),
                        p_taskInfo.getAcceptByDate(),
                        p_taskInfo.getCompleteByDate(),
                        p_emailInfo.getJobName(),
                        WorkflowHelper.localePair(
                                p_emailInfo.getSourceLocale(),
                                p_emailInfo.getTargetLocale(), "en_US"),
                        String.valueOf(p_emailInfo
                                .getSegmentTmMatchesWordCount()),
                        String.valueOf(p_emailInfo.getLeverageMatchThreshold()),
                        String.valueOf(p_emailInfo
                                .getTotalFuzzyMatchesWordCount()),
                        String.valueOf(p_emailInfo.getNoMatchesWordCount()),
                        String.valueOf(p_emailInfo.getRepetitionsWordCount()),
                        p_emailInfo.getJobId(), p_emailInfo.getProjectName(),
                        String.valueOf(p_emailInfo.getTotalWordCount()), "",
                        new ArrayList(), linkUrl };
                // For "amb-118 login task detail directly"
                initTaskDeatilUrlParam(p_taskInfo);

                String[] toUsers = getNodeAssignees(p_node, processInstance);
                emailInfos = (ArrayList) getMailer().getEmailAddresses(toUsers);
                int ActionType = WorkflowMailerConstants.ACTIVATE_TASK;

                if (toUsers != null)
                {
                    String from = p_emailInfo.getProjectManagerId();
                    Long projectId = p_emailInfo.getProjectIdAsLong();
                    PermissionManager pm = Permission.getPermissionManager();

                    for (int i = 0; i < toUsers.length; i++)
                    {
                        String to = toUsers[i];
                        PermissionSet perSet = pm.getPermissionSetForUser(to);

                        Object[] messageArgs;
                        boolean viewJobCommentPerm = perSet
                                .getPermissionFor(Permission.ACTIVITIES_COMMENTS_JOB);
                        boolean restrictCommentPerm = perSet
                                .getPermissionFor(Permission.COMMENT_ACCESS_RESTRICTED);

                        // How to show job comments according to permission.
                        if (restrictCommentPerm)
                        {
                            messageArgs = restrictArgs;
                        }
                        else if (viewJobCommentPerm)
                        {
                            messageArgs = args;
                        }
                        else
                        {
                            messageArgs = noJobCommentArgs;
                        }

                        if (skipping == null
                                || (skipping != null && skipping
                                        .startsWith("LAST")))
                            sendTaskActionEmailToUsers(from, new String[]
                            { to }, projectId, null, ActionType, messageArgs);
                    }
                }

                boolean b_isDell = false;
                try
                {
                    SystemConfiguration sc = SystemConfiguration.getInstance();
                    b_isDell = sc
                            .getBooleanParameter(SystemConfigParamNames.IS_DELL);
                }
                catch (Exception ex)
                {
                    s_logger.error("Failed to read IS_DELL param", ex);
                }

                if (b_isDell)
                {
                    if (Task.TYPE_REVIEW == p_taskInfo.getType()
                            && (skipping == null || (skipping != null && skipping
                                    .startsWith("LAST"))))
                    {
                        notifyImportInitiator(p_taskInfo, p_emailInfo,
                                WorkflowMailerConstants.ACTIVATE_REVIEW_TASK,
                                args);
                    }
                }
            }
        }
        catch (Exception ne)
        {
            s_logger.error("notifyTaskIsAdvanced " + ne.toString(), ne);
        }

        return emailInfos;
    }

    /**
     * For "amb-118 login task detail directly" Make a link which logon task
     * detail directly for task accepter
     * 
     * @param p_taskInfo
     *            for get some info eg.task id etc.
     * @return link
     */
    private String makeLoginToTaskUrl()
    {
        // make login url from email
        String paramStr = getUrlParamStrBaseParamMap(taskDeatilUrlParam);
        String url = addParameterToUrl(capLoginUrl(), paramStr);
        return url;
    }

    /**
     * Add a parameter to a url link
     * 
     * @param p_rootUrl
     *            p_rootUrl current url, please ensure the root url have exsit
     *            any parameter,else must add "?" first
     * @param p_paramName
     * @param p_paramValue
     * @return
     */

    @SuppressWarnings("deprecation")
    private void initTaskDeatilUrlParam(TaskInfo p_taskInfo)
    {
        String taskPageParam = "?linkName=detail&pageName=TK1A&taskAction=getTask&taskId="
                + p_taskInfo.getId() + "&state=-10";
        String forwardUrl = "/ControlServlet" + taskPageParam;
        forwardUrl = URLEncoder.encode(forwardUrl, "UTF-8");
        taskDeatilUrlParam.put("linkName", "detail");
        taskDeatilUrlParam.put("pageName", "TK1A");
        taskDeatilUrlParam.put("taskAction", "getTask");
        taskDeatilUrlParam.put("taskId", Long.toString(p_taskInfo.getId()));
        taskDeatilUrlParam.put(WebAppConstants.LOGIN_FROM,
                WebAppConstants.LOGIN_FROM_EMAIL);
        taskDeatilUrlParam.put("forwardUrl", forwardUrl);
        taskDeatilUrlParam.put("state", "-10");// all task status
    }

    private String getUrlParamStrBaseParamMap(HashMap p_paramMap)
    {
        StringBuilder paramStr = new StringBuilder("");
        if (p_paramMap == null)
        {
            return "";
        }
        Set keySet = p_paramMap.keySet();

        Iterator iterator = keySet.iterator();

        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            String value = (String) p_paramMap.get(key);
            if (key != null && !"".equals(key))
            {
                paramStr.append("&").append(key).append("=").append(value);
            }
        }
        return paramStr.toString();
    }

    /**
     * Add a parameter string to a url link
     * 
     * @param p_rootUrl
     *            current url , note: please ensure the root url have exsit any
     *            parameter,else must add "?" first
     * @param p_paramStr
     * @return
     */
    private String addParameterToUrl(String p_rootUrl, String p_paramStr)
    {
        // check this root url have no parameter
        if (p_rootUrl.indexOf("?") == -1)
        {
            p_rootUrl = p_rootUrl + "?temp=";
        }
        if (p_paramStr != null && !"".equals(p_paramStr))
        {
            return p_rootUrl + p_paramStr;
        }
        else
        {
            return p_rootUrl;
        }
    }

    /* send the email... */
    private void sendMail(String p_fromUserId, EmailInformation p_to,
            String p_subjectKey, String p_messageKey, Object[] p_messageArgs,
            String p_companyIdStr)
    {
        try
        {
            Locale locale = p_to.getEmailLocale();
            String[] args = new String[p_messageArgs.length];
            for (int i = 0; i < p_messageArgs.length; i++)
            {
                if (p_messageArgs[i] instanceof Date)
                {
                    args[i] = format((Date) p_messageArgs[i], locale,
                            p_to.getUserTimeZone());
                }
                if (p_messageArgs[i] instanceof List)
                {
                    List attachment = (List) p_messageArgs[i];
                    StringBuilder strBuff = new StringBuilder();
                    if (attachment.size() == 0)
                    {
                        continue;
                    }
                    for (Iterator iter = attachment.iterator(); iter.hasNext();)
                    {
                        File attFile = (File) iter.next();
                        strBuff.append(attFile.getAbsolutePath());
                        strBuff.append(",");
                    }
                    args[i] = strBuff.toString();
                }
                else
                {
                    args[i] = p_messageArgs[i].toString();
                }
            }
            getMailer().sendMail(p_fromUserId, p_to, p_subjectKey,
                    p_messageKey, args, p_companyIdStr);
        }
        catch (Exception e)
        {
            String address = "<null!>";
            if (p_to != null)
                address = p_to.getEmailAddress();
            s_logger.error("Failed to send email to " + address, e);
        }
    }

    // send an email to users, for notify the task/activity cancel action
    private void sendSingleTaskActionEmailToUser(String p_fromUserId,
            TaskEmailInfo p_emailInfo, String p_accepter,
            List<String> p_assignee, int p_taskActionType,
            Object[] p_messageArgs)
    {
        try
        {
            if (!m_systemNotificationEnabled)
            {
                return;
            }

            List<?> wfmUserNames = p_emailInfo.getWorkflowManagerIds();
            int size = wfmUserNames.size();
            boolean notifyPm = p_emailInfo.notifyProjectManager();
            List<String> receiverList = new ArrayList<String>();
            String receiver = null;

            // do not send email if not required
            if (size == 0 && !notifyPm)
            {
                return;
            }

            String companyIdStr = MailerHelper.getCompanyId(p_emailInfo);
            String subject = getEmailSubject(p_taskActionType);
            String message = getEmailMessage(p_taskActionType);

            // notify the accepter or all users
            if (null != p_accepter && p_accepter.length() > 0)
            {
                receiver = p_accepter;
                if (null != receiver && !receiverList.contains(receiver))
                {
                    receiverList.add(receiver);
                }
            }
            else
            {
                if (null != p_assignee && p_assignee.size() > 0)
                {
                    receiverList = p_assignee;
                }
            }

            // notify PM
            if (notifyPm)
            {
                receiver = p_emailInfo.getProjectManagerId();
                if (null != receiver && !receiverList.contains(receiver))
                {
                    receiverList.add(receiver);
                }
            }

            // notify all workflow managers (if any)
            for (int i = 0; i < size; i++)
            {
                receiver = (String) wfmUserNames.get(i);
                if (null != receiver && !receiverList.contains(receiver))
                {
                    receiverList.add(receiver);
                }
            }

            // send email
            for (int i = 0; i < receiverList.size(); i++)
            {
                sendMail(p_fromUserId, getEmailInfo(receiverList.get(i)),
                        subject, message, p_messageArgs, companyIdStr);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to send email notification.", e);
        }
    }

    /* send an email to a user based on a workflow action (i.e. advancing */
    /* or rejecting a task) */
    public void sendJobActionEmailToUser(String p_fromUserId,
            TaskEmailInfo p_emailInfo, int p_taskActionType)
    {
        Object[] p_messageArgs =
        { p_emailInfo.getJobName(), capLoginUrl(),
                p_emailInfo.getAssigneesName(),
                p_emailInfo.getPriorityAsString() };
        sendTaskActionEmailToUser(p_fromUserId, p_emailInfo, null,
                p_taskActionType, p_messageArgs);
    }

    public void sendTaskActionEmailToUser(String p_fromUserId,
            TaskEmailInfo p_emailInfo, String p_cc, int p_taskActionType,
            Object[] p_messageArgs)
    {
        try
        {
            if (!m_systemNotificationEnabled)
            {
                return;
            }

            boolean notifyPm = p_emailInfo.notifyProjectManager();
            String projectManager = p_emailInfo.getProjectManagerId();
            List<String> wfmUserNames = p_emailInfo.getWorkflowManagerIds();
            // do not send email if not required
            if (!notifyPm && wfmUserNames.size() == 0)
            {
                return;
            }

            // Delete the ignored receipt, Details in GBS-2461&2462.
            Set<String> ignoredReceipt = p_emailInfo.getIgnoredReceipt();
            if (ignoredReceipt != null && ignoredReceipt.size() > 0)
            {
                if (ignoredReceipt.contains(projectManager))
                    notifyPm = false;

				for (Iterator<String> it = wfmUserNames.iterator(); it.hasNext();)
                {
                    if (ignoredReceipt.contains(it.next()))
                    {
                    	it.remove();
                    }
                }
            }

            String companyIdStr = MailerHelper.getCompanyId(p_emailInfo);
            String subject = getEmailSubject(p_taskActionType);
            String message = getEmailMessage(p_taskActionType);

            if (notifyPm)
            {
				sendMail(p_fromUserId, getEmailInfo(projectManager), subject,
						message, p_messageArgs, companyIdStr);
            }

            // notify all workflow managers (if any)
            for (int i = 0; i < wfmUserNames.size(); i++)
            {
				if (notifyPm
						&& projectManager.equalsIgnoreCase(wfmUserNames.get(i)))
            		continue;

				sendMail(p_fromUserId, getEmailInfo(wfmUserNames.get(i)),
						subject, message, p_messageArgs, companyIdStr);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to send email notification.", e);
        }
    }

    /*
     * send an email to a group of users (filtered by role and project) based on
     * a workflow action
     */
    private void sendTaskActionEmailToUsers(String p_initiator,
            String[] userIds, Long p_projectId, String p_cc,
            int p_taskActionType, Object[] p_messageArgs)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        try
        {
            Project proj = ServerProxy.getProjectHandler().getProjectById(
                    p_projectId);
            String companyIdStr = String.valueOf(proj.getCompanyId());
            String from = getEmailInfo(p_initiator).getEmailAddress();
            if (from != null)
            {
                List emailInfos = getMailer().getEmailAddresses(userIds);
                int size = emailInfos == null ? 0 : emailInfos.size();
                for (int i = 0; i < size; i++)
                {
                    EmailInformation to = (EmailInformation) emailInfos.get(i);
                    if (to != null)
                    {
                        taskDeatilUrlParam.put(
                                WebAppConstants.LOGIN_NAME_FIELD,
                                UserUtil.getUserNameById(to.getUserId()));
                        p_messageArgs[1] = makeLoginToTaskUrl();
                        sendMail(p_initiator, to,
                                getEmailSubject(p_taskActionType),
                                getEmailMessage(p_taskActionType),
                                p_messageArgs, companyIdStr);
                    }
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to send email notification to a group of users.", e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Email notification methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Event notification support
    // ////////////////////////////////////////////////////////////////////

    // get the warning threshold for scheduling timers.
    private Float getWarningThreshold()
    {
        if (m_threshold == null)
        {
            try
            {
                String threshold = getSystemConfigValue(SystemConfigParamNames.TIMER_THRESHOLD);
                m_threshold = Float.valueOf(threshold);
            }
            catch (NumberFormatException e)
            {
                s_logger.error(
                        "WorkflowServerLocal :: invalid warning threshold. It's been reset to 75%",
                        e);
                m_threshold = Float.valueOf(".75");
            }
        }
        return m_threshold;
    }

    // determines whether the notification feature is active
    private boolean isNotificationActive()
    {
        if (m_isNotificationActive == -1)
        {
            try
            {
                SystemConfiguration config = SystemConfiguration.getInstance();
                m_isNotificationActive = config
                        .getIntParameter(SystemConfigParamNames.USE_WARNING_THRESHOLDS);
            }
            catch (Exception e)
            {
                s_logger.error("WorkflowServerLocal :: isNotificationActive. ",
                        e);
            }
        }
        return m_isNotificationActive == 1;
    }

    /**
     * Ends the taskinstance.
     * 
     * @param p_nextNodes
     * @param p_taskInstance
     * @param isCompleted
     * @param p_processInstance
     * @param p_arrowLabel
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void endTaskInstance(NextNodes p_nextNodes,
            TaskInstance p_taskInstance, boolean isCompleted,
            ProcessInstance p_processInstance, String p_arrowLabel,
            String activity, List<WorkflowTaskInstance> taskInstances,
            long workflowId, String assignee) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException
    {
        if (!StringUtil.isEmpty(p_arrowLabel))
        {
            /* set the variable for the condition branch */
            p_taskInstance.setVariable(WorkflowConstants.VARIABLE_GOTO,
                    p_arrowLabel);
        }
        else
        {
            // if the arrow is null, it indicates this is a skip operation
            p_taskInstance.setVariable(WorkflowConstants.VARIABLE_GOTO,
                    WorkflowConstants.VARIABLE_GOTO_SKIP);
        }

        /* end the task */

        p_taskInstance.end();
        // for GBS-1302, end interim activity
        TaskInterimPersistenceAccessor.endInterimActivity(p_taskInstance);

        if (!StringUtil.isEmpty(activity))
        {
            WorkflowJbpmPersistenceHandler.saveSkipVariable(p_taskInstance,
                    workflowId);
        }

        signalProcess(p_processInstance, activity, taskInstances, workflowId);

        if (!isCompleted)
        {
            /*
             * for loop situation, there will be more than one taskinstance in
             * jpbm for same node name. We set the start date to null of the
             * original taskinstance to mark the task has been activited.
             */
            p_taskInstance = WorkflowJbpmUtil.getTaskInstanceByNode(
                    p_processInstance, ((WorkflowTaskInstance) p_nextNodes
                            .getNode(0)).getNodeName());

            // WorkflowJbpmUtil.setPrivateValue(p_taskInstance, "start", null);

        }
    }

    /**
     * Starts the task instance.
     * 
     * @param p_taskInstance
     *            The task instance.
     * @param p_assignee
     *            The name of the assignee.
     */
    private void startTaskInstance(TaskInstance p_taskInstance,
            String p_assignee)

    {
        if (p_taskInstance.getStart() == null)
        {
            /*
             * Judges the data of the taskinstance in case of the refresh of the
             * page.
             */
            p_taskInstance.start();
        }
        p_taskInstance.setActorId(p_assignee);

        // for GBS-1302, accept interim activity
        TaskInterimPersistenceAccessor.acceptInterimActivity(p_taskInstance);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Event notification support
    // ////////////////////////////////////////////////////////////////////
}
