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
package com.globalsight.everest.taskmanager;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.ReservedTime;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.comment.CommentImpl;
import com.globalsight.everest.edit.offline.OfflineFileUploadStatus;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageEventObserver;
import com.globalsight.everest.page.PageStateValidator;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.task.TaskDescriptorModifier;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EnvoyWorkItem;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowException;
import com.globalsight.everest.workflow.WorkflowHelper;
import com.globalsight.everest.workflow.WorkflowJbpmPersistenceHandler;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.everest.workflow.WorkflowNodeParameter;
import com.globalsight.everest.workflow.WorkflowServer;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowManager;
import com.globalsight.everest.workflowmanager.WorkflowManagerException;
import com.globalsight.log.OperationLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.modules.Modules;
import com.globalsight.util.resourcebundle.LocaleWrapper;

/**
 * TaskManagerLocal implements TaskManager interface. It is responsible for
 * handling and delegating task related processes.
 */
public class TaskManagerLocal implements TaskManager
{
    // For logging use
    private static final Logger CATEGORY = Logger
            .getLogger(TaskManagerLocal.class.getName());

    // the server handles access to IFlow
    private WorkflowServer m_workflowServer = null;

    private UserManager m_userManager = null;

    private PageEventObserver m_pageEventObserver = null;

    private WorkflowManager m_workflowManager = null;

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    public TaskManagerLocal() throws TaskException
    {
        init();
    }

    public TaskManagerLocal(UserManager p_userManager,
            WorkflowServer p_workflowServer,
            PageEventObserver p_pageEventObserver,
            WorkflowManager p_workflowManager)
    {
        m_userManager = p_userManager;
        m_workflowServer = p_workflowServer;
        m_pageEventObserver = p_pageEventObserver;
        m_workflowManager = p_workflowManager;
    }

    /**
     * To accept a Task. It invokes Iflow to accept a task, also saved accepted
     * time to database task_info table.
     * 
     * @param p_userId
     *            the Id of the user who accepts the task.
     * @param p_task
     *            The task to be accepted.
     * @param isSkipped
     *            If this task is being skipped.
     * 
     * @throws RemoteException
     *             , TaskException
     */
    public void acceptTask(String p_userId, Task p_task, boolean isSkipped)
            throws RemoteException, TaskException
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("isSkipped", isSkipped);
        acceptTask(p_userId, p_task, data);
    }

    /**
     * To accept a Task. It invokes Iflow to accept a task, also saved accepted
     * time to database task_info table.
     * 
     * @param p_userId
     *            the Id of the user who accepts the task.
     * @param p_task
     *            The task to be accepted.
     * @param data
     *            Keeps the data for this method.
     * 
     * @throws RemoteException
     *             , TaskException
     */
    public void acceptTask(String p_userId, Task p_task,
            Map<String, Object> data) throws RemoteException, TaskException
    {
        Task task = validateTaskForAcceptance(p_userId, p_task);
        // this would be the base date in case of an exception
        Date originalCompletedBy = task.getEstimatedCompletionDate();
        TaskInfo taskInfo = null;
        List wfTaskInfos = null;
        Workflow wfClone = null;

        // remove all proposed reserved times from user calendars
        removeReservedTimes(p_task.getId());

        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            TaskImpl task_temp = (TaskImpl) session.get(TaskImpl.class,
                    new Long(task.getId()));
            task_temp.setWorkflowTask(((TaskImpl) task).getWorkflowTask());
            wfClone = task_temp.getWorkflow();

            taskInfo = updateAcceptedTask(getCurrentTime(), task_temp,
                    p_userId, session);
            // -1 indicates that the default path would be from the
            // beginning of the workflow (form START node)
            wfTaskInfos = m_workflowServer.timeDurationsInDefaultPath(null,
                    wfClone.getId(), -1);

            // now update the estimated acceptance/completion for the rest
            // of the tasks in the default path (following the accepted
            // task) and the workflow's completion time.
            updateDefaultPathTasks(taskInfo.getCompleteByDate(), wfTaskInfos,
                    wfClone, task_temp.getId(), session);

            tx.commit();

            // For sla issue
            if (wfClone.isEstimatedTranslateCompletionDateOverrided())
            {
                m_workflowManager.updateEstimatedTranslateCompletionDate(
                        wfClone.getId(),
                        wfClone.getEstimatedTranslateCompletionDate());
            }
        }
        catch (Exception te)
        {
            tx.rollback();
            String[] msgArgs =
            { "TopLink access error!" };
            CATEGORY.error("acceptTask(): " + te.toString() + " p_task="
                    + WorkflowHelper.toDebugString(p_task), te);
            throw new TaskException(TaskException.MSG_FAILED_TO_ACCEPT_TASK,
                    msgArgs, te);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }

        try
        {
            TaskEmailInfo emailInfo = createTaskEmailInfo(wfClone.getJob(),
                    p_task, null);
            // printDebuggingInfo(p_task);
            if (data.get("ignoredReceipt") != null)
            {
                HashSet<String> ignoredReceipt = (HashSet) data
                        .get("ignoredReceipt");
                emailInfo.setIgnoredReceipt(ignoredReceipt);
            }
            boolean isSkipped = false;
            if (data.get("isSkipped") != null)
            {
                isSkipped = (Boolean) data.get("isSkipped");
            }

            m_workflowServer.acceptTask(wfClone, p_userId, p_task.getId(),
                    taskInfo, emailInfo, isSkipped);
        }
        catch (WorkflowException we)
        {
            // rollback the above DB action for transaction consistence
            try
            {
                unacceptTask(task, wfTaskInfos, originalCompletedBy, p_userId,
                        null);
            }
            catch (Exception te)
            {
                String[] msgArgs =
                { "TopLink access error!" };
                CATEGORY.error("acceptTask(): " + te.toString() + " p_task="
                        + WorkflowHelper.toDebugString(p_task), te);
                throw new TaskException(
                        TaskException.MSG_FAILED_TO_ACCEPT_TASK, msgArgs, te);
            }

            // throw jBPM exception
            String[] msgArgs =
            { "WorkflowServer error!" };
            CATEGORY.error("acceptTask(): " + we.toString() + " p_task="
                    + WorkflowHelper.toDebugString(p_task), we);
            throw new TaskException(we.getMessageKey(),
                    we.getMessageArguments(), we, we.getPropertyFileName());
        }
    }

    /**
     * To complete a Task. It invokes Iflow to complete a task, also saved
     * completed time to database task_info table.
     * 
     * @param p_userId
     *            the Id of the user who completes the task.
     * @param p_task
     *            The task to be completed.
     * @param p_destinationArrow
     *            - The name of the outgoing arrow of a condition node (if the
     *            next node of this task is a condition node). This is used for
     *            making decision.
     * @param skipping
     *            Indicates this task is being skipped.
     * 
     * @throws RemoteException
     *             , TaskException
     */
    public void completeTask(String p_userId, Task p_task,
            String p_destinationArrow, String skipping) throws RemoteException,
            TaskException
    {
        // Justify whether the task can been completed.
        if (!TaskHelper.canCompleteTask(p_task))
        {
            String hql = "update TaskImpl t set t.stateStr = :state where t.id = :taskId";
            Map map = new HashMap();
            map.put("state", Task.STATE_ACCEPTED_STR);
            map.put("taskId", p_task.getId());
            HibernateUtil.excute(hql, map);
            return;
        }
        
        validateStateOfPages(p_task);        
        try
        {
            m_workflowManager.setTaskCompletion(p_userId, p_task,
                    p_destinationArrow, skipping);
        }
        catch (WorkflowManagerException wfe)
        {
            throw new TaskException(wfe.getMessageKey(),
                    wfe.getMessageArguments(), wfe, wfe.getPropertyFileName());
        }
        catch (Exception e)
        {
            throw new TaskException(TaskException.MSG_FAILED_TO_COMPLETE_TASK,
                    null, e);
        }

        OfflineFileUploadStatus status = OfflineFileUploadStatus.getInstance();
        if (status.getFileStates(p_task.getId()) != null)
        {
            status.clearTaskFileState(p_task.getId());
        }
        
		OperationLog.log(p_userId, OperationLog.EVENT_COMPLETE,
				OperationLog.COMPONET_TASK, p_task.getJobName() + "-"
						+ p_task.getTargetLocale().getDisplayName() + "-"
						+ p_task.getTaskDisplayName());
    }

    /**
     * To retrieve a Task by Id for the given user.
     * 
     * @param p_userId
     *            the Id of the user.
     * @param p_taskId
     *            The id of the target task.
     * @param p_state
     *            - The state of the task to be retrieved. Since a list of
     *            activities are displayed based on their state, it's important
     *            to pass the state in order to get the appropriate details
     *            info.
     * 
     * @return a Task object
     * @throws RemoteException
     *             , TaskException
     */
    public Task getTask(String p_userId, long p_taskId, int p_state)
            throws RemoteException, TaskException
    {
        // get the WorkflowTaskInstance from IFLow
        WorkflowTaskInstance wfTask;
        try
        {
            if (canManageProjects(p_userId))
            {
                wfTask = m_workflowServer.getWorkflowTaskInstance(p_userId,
                        p_taskId, p_state);
            }
            else
            {
                wfTask = m_workflowServer.getWorkflowTaskInstanceForAssignee(
                        p_userId, p_taskId, p_state);
            }
        }
        catch (WorkflowException we)
        {
            String[] msgArgs =
            { "" + p_taskId };
            CATEGORY.error(
                    "getTask(): " + we.toString() + " p_taskId="
                            + Long.toString(p_taskId), we);

            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_TASK_IN_TRANSIT, msgArgs,
                    we);
        }

        // Get a task from Toplink
        Task task;
        try
        {
            task = TaskPersistenceAccessor.getTask(p_taskId, true);
        }
        catch (TaskException te)
        {
            String[] msgArgs =
            { "TopLink access error!" };
            CATEGORY.error("getTask(): " + te.toString() + " wfTask="
                    + WorkflowHelper.toDebugString(wfTask) + " p_taskId="
                    + Long.toString(p_taskId), te);
            throw new TaskException(TaskException.MSG_FAILED_TO_GET_TASK,
                    msgArgs, te);
        }

        // Judge IFlow and DB data consistency
        if ((wfTask == null && task != null) || (wfTask != null)
                && (task == null))
        {
            String[] msgArgs =
            { "Task data is not synchronized between IFlow and Database!" };
            CATEGORY.error("getTask(): " + Arrays.asList(msgArgs).toString()
                    + " wfTask=" + WorkflowHelper.toDebugString(wfTask)
                    + " p_taskId=" + Long.toString(p_taskId));
            throw new TaskException(TaskException.MSG_FAILED_TO_GET_TASK,
                    msgArgs, null);
        }

        if (task != null)
        {
            // set WorkflowTaskInstance to Task
            task.setWorkflowTask(wfTask);

            // set project manager name to Task
            if (!"".equals(task.getProjectManagerId()))
            {
                String projName = getProjectManagerName(task
                        .getProjectManagerId());
                task.setProjectManagerName(projName);
            }
        }

        return task;
    }

    /**
     * To retrieve activity completion time
     * 
     * @param p_jobid
     *            The id of the job in question
     * 
     * @return a hashtable containing workflowIds as key and task completed time
     *         as value
     * @throws RemoteException
     *             , TaskException
     */
    public Collection getFirstCompletedActivityTime(long p_jobId)
            throws RemoteException, TaskException
    {
        // Get a task from Toplink
        Collection taskInfo;
        try
        {
            taskInfo = TaskPersistenceAccessor
                    .getFirstCompletedActivityTime(p_jobId);
        }
        catch (TaskException te)
        {
            String[] msgArgs =
            { "TopLink access error!" };
            CATEGORY.error("getFirstCompletedActivityTime(): " + te.toString(),
                    te);
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_TASK_COMPLETION_TIME,
                    msgArgs, te);
        }
        return taskInfo;
    }

    /**
     * @see TaskManager.getTask(long)
     */
    public Task getTask(long p_taskId) throws RemoteException, TaskException
    {
        return getTask(p_taskId, false);
    }

    /**
     * @see TaskManager.getTask(long, boolean)
     */
    public Task getTask(long p_taskId, boolean p_isEditable)
            throws RemoteException, TaskException
    {
        // Get a task from Toplink
        Task task = null;
        try
        {
            task = TaskPersistenceAccessor.getTask(p_taskId, p_isEditable);
        }
        catch (TaskException te)
        {
            String[] msgArgs =
            { "TopLink access error!" };
            CATEGORY.error(
                    "getTask(): " + te.toString() + " p_taskId="
                            + Long.toString(p_taskId), te);
            throw new TaskException(TaskException.MSG_FAILED_TO_GET_TASK,
                    msgArgs, te);
        }
        return task;
    }

    /**
     * Gets Workflow Tasks from jBPM
     * 
     * @param p_user
     * @param p_taskState
     * @return
     * @exception RemoteException
     * @exception TaskException
     */
    public Map getWFTasks(User p_user, int p_taskState) throws RemoteException,
            TaskException
    {
        String userId = p_user.getUserId();
        boolean isProjManager = isProjectManager(p_user);
        // get Map of taskNodeId and WorkflowTaskInstance from jBPM.
        Map wfTasks = null;
        try
        {
            if (isProjManager)
            {
                wfTasks = m_workflowServer
                        .filterTasksForPM(userId, p_taskState);

            }
            else
            {
                wfTasks = m_workflowServer.getTasksForUser(userId, p_taskState);
            }
        }
        catch (WorkflowException we)
        {
            String[] msgArgs =
            { "WorkflowServer error!" };
            CATEGORY.error("getTasks(): " + we.toString() + " p_userId="
                    + WorkflowHelper.toDebugString(userId), we);
            throw new TaskException(TaskException.MSG_FAILED_TO_GET_TASKS,
                    msgArgs, we);
        }
        return wfTasks;
    }

    /**
     * Judges whether the user is a project manager.
     * 
     * @param p_user
     * 
     * @return true of false.
     */
    private boolean isProjectManager(User p_user)
    {
        boolean isProjectManager = false;
        try
        {
            // get the projects whose manager is this user.
            Collection projects = ServerProxy.getProjectHandler()
                    .getProjectsByProjectManager(p_user);
            if (projects != null && !projects.isEmpty())
            {
                isProjectManager = true;
            }
        }
        catch (Exception e)
        {
            throw new TaskException(e);
        }
        return isProjectManager;
    }

    /**
     * To retrieve tasks of certain state for the given user.
     * 
     * @param p_userId
     *            the Id of the user.
     * @param p_taskState
     *            The state of the target tasks.
     * 
     * @return a Map of taskId and Task key value pair
     * @throws RemoteException
     *             , TaskException
     */
    public List getTasks(String p_userId, int p_taskState)
            throws RemoteException, TaskException
    {
        List tasks = null;
        Map wfTasks = getWFTasks(
                ServerProxy.getUserManager().getUser(p_userId), p_taskState);

        if (wfTasks != null && wfTasks.size() != 0)
        {
            // get the keys of the map and convert them to array of long type
            Vector taskIds = new Vector(wfTasks.keySet());
            int SIZE_DIVISOR = 500;
            int sizeOfTaskIds = taskIds.size();
            float fraction = sizeOfTaskIds / SIZE_DIVISOR;
            int iterations = (int) fraction + 1;
            // Get tasks from Toplink
            try
            {
                tasks = new ArrayList();
                int ibeg = 0;
                int iend = 499;
                int taskSize = 0;
                for (int i = 0; i < iterations; i++)
                {
                    taskSize = taskIds.size();
                    if (taskSize < iend)
                    {
                        iend = taskSize;
                    }
                    Collection tempTasks = getTasksForIteration(taskIds
                            .subList(ibeg, iend));
                    taskIds.subList(ibeg, iend).clear();
                    tasks.addAll(tempTasks);
                }
            }
            catch (Exception te)
            {
                String[] msgArgs =
                { "TopLink access error!" };
                CATEGORY.error("getTasks(): " + te.toString() + " p_userId="
                        + WorkflowHelper.toDebugString(p_userId), te);
                throw new TaskException(TaskException.MSG_FAILED_TO_GET_TASK,
                        msgArgs, te);
            }

            linkWorkflowTasksWithTaskInfos(wfTasks, tasks);
        }
        return tasks;
    }

    /**
     * Links each Workflow Task from IFLOW with the corresponding TaskInfo from
     * TOPLink
     * 
     * @param p_wfTasks
     * @param p_tasks
     */
    public void linkWorkflowTasksWithTaskInfos(Map p_wfTasks, List p_tasks)
            throws RemoteException, TaskException
    {
        HashMap<String, String> pmIdName = new HashMap<String, String>();

        Iterator iter = p_tasks.iterator();
        HashMap activities = new HashMap();
        // Link each WorkflowTaskInstance with its corresponding Task
        while (iter.hasNext())
        {
            Task aTask = (Task) iter.next();
            Long taskId = new Long(aTask.getId());
            WorkflowTaskInstance wfTask = null;
            if (p_wfTasks.containsKey(taskId))
            {
                wfTask = (WorkflowTaskInstance) p_wfTasks.get(taskId);

                // need to set the Activity object since in the WorkflowServer
                // we're just using new Activity(name)
                Activity activity = (Activity) activities.get(wfTask
                        .getActivityName());
                if (activity == null)
                {
                    try
                    {
                        activity = ServerProxy.getJobHandler().getActivity(
                                wfTask.getActivityName());

                        activities.put(wfTask.getActivityName(), activity);
                    }
                    catch (Exception e)
                    {
                        activity = wfTask.getActivity();
                    }
                }
                wfTask.setActivity(activity);

                aTask.setWorkflowTask(wfTask);

                if ("".equals(aTask.getProjectManagerId()))
                {
                    iter.remove();
                }
                else
                {
                    String id = aTask.getProjectManagerId();
                    String name = pmIdName.get(id);
                    if (name == null)
                    {
                        name = getProjectManagerName(id);
                        pmIdName.put(id, name);
                    }
                    String projName = name;
                    aTask.setProjectManagerName(projName);
                }
            }
            else
            {
                CATEGORY.debug("Unmappable task, removing it: " + taskId);
                iter.remove();
            }
        }
    }

    private Collection getTasksForIteration(List p_List) throws Exception
    {
        return TaskPersistenceAccessor.getTasks(new Vector(p_List));
    }

    /**
     * Searchs for tasks based on the given criteria
     * 
     * @param p_criteria
     * @return
     */
    public Collection getTasks(TaskSearchParameters p_searchParameters)
            throws RemoteException, TaskException
    {
        Map params = p_searchParameters.getParameters();
        User user = (User) params.get(new Integer(TaskSearchParameters.USER));
        Integer state = (Integer) params.get(new Integer(
                TaskSearchParameters.STATE));
        Map wfTasks = getWFTasks(user, state.intValue());

        if (wfTasks.isEmpty())
        {
            return new ArrayList();
        }

        p_searchParameters.setIds(wfTasks.keySet());
        List tasks = (List) TaskPersistenceAccessor
                .getTasks(p_searchParameters);
        linkWorkflowTasksWithTaskInfos(wfTasks, tasks);
        return tasks;
    }

    /**
     * @see TaskManager.getTasks(String, long)
     */
    public Collection getTasks(String p_taskName, long p_jobId)
            throws RemoteException, TaskException
    {
        boolean attachWorkflowTaskInstance = true;
        return getTasks(p_taskName, p_jobId, attachWorkflowTaskInstance);
    }

    /**
     * @see TaskManager.getTasks(String, long, boolean)
     */
    public Collection getTasks(String p_taskName, long p_jobId,
            boolean p_attachWorkflowTaskInstance) throws RemoteException,
            TaskException
    {
        Collection tasks = null;
        try
        {
            HashMap map = new HashMap();
            map.put(TaskDescriptorModifier.JOB_ID_ARG, new Long(p_jobId));
            String sql = TaskDescriptorModifier.TASKS_BY_NAME_AND_JOB_ID_SQL;
            if (p_taskName != null && p_taskName.trim().length() > 0)
            {
                sql = sql + " AND t.NAME = :taskName";
                map.put(TaskDescriptorModifier.TASK_NAME_ARG, p_taskName);
            }

            tasks = HibernateUtil.searchWithSql(sql, map, TaskImpl.class);
        }
        catch (Exception pe)
        {
            String args[] =
            { p_taskName, Long.toString(p_jobId) };
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_TASKS_BY_NAME_AND_JOB_ID,
                    args, pe);
        }

        if (p_attachWorkflowTaskInstance)
        {
            for (Iterator ti = tasks.iterator(); ti.hasNext();)
            {
                // get the WorkflowTaskInstance from IFLow
                Task t = (Task) ti.next();
                WorkflowTaskInstance wfTask = null;
                try
                {
                    wfTask = m_workflowServer.getWorkflowTaskInstance(t
                            .getWorkflow().getId(), t.getId());
                    t.setWorkflowTask(wfTask);
                }
                catch (WorkflowException we)
                {
                    String[] msgArgs =
                    { "" + t.getId() };
                    CATEGORY.error("getTask(): " + we.toString() + " p_taskId="
                            + Long.toString(t.getId()), we);
                    throw new TaskException(
                            TaskException.MSG_FAILED_TO_GET_TASK_IN_TRANSIT,
                            msgArgs, we);
                }
            }
        }

        return tasks;
    }

    /**
     * @see TaskManager.getCurrentTasks(long)
     */
    public Collection getCurrentTasks(long p_workflowId)
            throws RemoteException, TaskException
    {
        Collection tasks = null;
        try
        {
            HashMap map = new HashMap();
            map.put(TaskDescriptorModifier.WORKFLOW_ID_ARG, new Long(
                    p_workflowId));
            tasks = HibernateUtil.searchWithSql(
                    TaskDescriptorModifier.CURRENT_TASKS_BY_WORKFLOW_ID_SQL,
                    map, TaskImpl.class);
        }
        catch (Exception pe)
        {
            String args[] =
            { Long.toString(p_workflowId) };
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_CURRENT_TASKS, args, pe);
        }

        return tasks;
    }

    /**
     * Retrieves all the tasks in a job that are associated with a particular
     * rate type (like HOURLY, PAGE_COUNT, etc..)
     */
    public Collection getTasks(long p_jobId, Integer p_rateType,
            Integer p_costType) throws RemoteException, TaskException
    {
        return TaskPersistenceAccessor
                .getTasks(p_jobId, p_rateType, p_costType);
    }

    /**
     * @see TaskManager.getTasksForRating(long)
     */
    public List getTasksForRating(long p_workflowId) throws RemoteException,
            TaskException
    {
        List tasksForRating = new ArrayList();
        try
        {
            Collection tasks = TaskPersistenceAccessor
                    .getCompletedTasks(p_workflowId);

            for (Iterator ti = tasks.iterator(); ti.hasNext();)
            {
                Task t = (Task) ti.next();
                // if ratings already specified
                if (t.getRatings() != null && t.getRatings().size() > 0)
                {
                    tasksForRating.add(t);
                }
                else
                {
                    String userId = t.getAcceptor();
                    Vendor v = ServerProxy.getVendorManagement()
                            .getVendorByUserId(userId);
                    // if there is a vendor associated with the user then the
                    // task can be rated - so add to the list
                    if (v != null)
                    {
                        tasksForRating.add(t);
                    }
                    // else - don't add to the list
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get the tasks for rating in workflow "
                    + p_workflowId, e);
            String[] args =
            { Long.toString(p_workflowId) };
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_TASKS_FOR_RATING, args, e);
        }
        return tasksForRating;
    }

    /**
     * @see TaskManager.getAcceptedTaskInfosInWorkflow(long)
     * 
     */
    public Collection getAcceptedTaskInfosInWorkflow(long p_workflowId)
            throws RemoteException, TaskException
    {

        Collection tasks = null;
        try
        {
            // get all the tasks from iflow that are associated with the
            // workflow
            // and have been visited (either complete or accepted)
            WorkflowServer ws = ServerProxy.getWorkflowServer();
            List tasksFromIflow = ws.getVisitedTasksForWorkflow(p_workflowId);

            Vector taskIds = new Vector(tasksFromIflow.size());
            for (int i = 0; i < tasksFromIflow.size(); i++)
            {
                long tId = ((WorkflowTaskInstance) tasksFromIflow.get(i))
                        .getTaskId();
                taskIds.add(new Long(tId));
            }

            // get from DB all the specified tasks
            tasks = TaskPersistenceAccessor.getTasks(taskIds);

            // loop through and match them up
            for (Iterator it = tasks.iterator(); it.hasNext();)
            {
                TaskImpl task = (TaskImpl) it.next();
                boolean found = false;
                for (int j = 0; !found && j < tasksFromIflow.size(); j++)
                {
                    WorkflowTaskInstance wfTask = (WorkflowTaskInstance) tasksFromIflow
                            .get(j);
                    if (task.getId() == wfTask.getTaskId())
                    {
                        found = true;
                        task.setWorkflowTask(wfTask);
                        // remove from collection so it isn't checked against
                        // again
                        // it won't loop through the collection anymore
                        tasksFromIflow.remove(wfTask);
                    }
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to retrieve the accepted tasks of workflow "
                    + p_workflowId, e);
            String args[] =
            { Long.toString(p_workflowId) };
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_WF_ACCEPTED_TASKS, args, e);
        }

        Collection taskInfos = new ArrayList(tasks.size());
        // loop through and create the TaskInfo objects
        for (Iterator i = tasks.iterator(); i.hasNext();)
        {
            TaskImpl task = (TaskImpl) i.next();
            // only add the ones that have already been accepted
            if (task.getAcceptedDate() != null)
            {
                TaskInfo tInfo = new TaskInfo(task.getId(), task.getTaskName(),
                        task.getState(), task.getEstimatedAcceptanceDate(),
                        task.getEstimatedCompletionDate(),
                        task.getAcceptedDate(), task.getCompletedDate(),
                        task.getType());
                String acceptor = task.getAcceptor();
                if (acceptor != null && acceptor.length() > 0)
                {
                    tInfo.setAcceptor(acceptor);
                }

                taskInfos.add(tInfo);
            }
        }
        return taskInfos;
    }

    /**
     * @see TaskManager.removeUserAsTaskAcceptor(String)
     */
    public void removeUserAsTaskAcceptor(String p_userId)
            throws RemoteException, TaskException
    {
        // first reassign the user
        try
        {
            ServerProxy.getWorkflowServer()
                    .reassignUserActivitiesToPm(p_userId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to reassign the user activities of user "
                    + p_userId + " to the Project Manager.", e);
            return;
        }

        // now reset the acceptor, accepted_date, and task state
        // for all accepted task by this user
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            SQLQuery query = session
                    .createSQLQuery(TaskDescriptorModifier.ACCEPTED_TASK_BY_USER_ID_SQL);
            query.addEntity(TaskImpl.class);
            query.setParameter(TaskDescriptorModifier.TASK_USER_ID_ARG,
                    p_userId);
            Collection col = query.list();

            if (col == null)
            {
                return;
            }

            Object[] tasks = col.toArray();

            for (int i = 0, size = tasks.length; i < size; i++)
            {
                Task task = (Task) tasks[i];
                task.setAcceptor(null);
                task.setAcceptedDate(null);
                task.setState(Task.STATE_ACTIVE);
                session.update(task);
            }
            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            CATEGORY.error(
                    "Failed to reset task acceptor and state for the removed user "
                            + p_userId + ".", e);
            return;
        }
    }

    /**
     * @see TaskManager.updateStfCreationState(long, String)
     */
    public void updateStfCreationState(long p_taskId, String p_stfCreationState)
            throws RemoteException, TaskException
    {

        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        // Task task = TaskPersistenceAccessor.getTask(p_taskId, false);
        Task task = (Task) session.get(TaskImpl.class, new Long(p_taskId));

        try
        {
            // UnitOfWork uow = PersistenceService.getInstance().
            // acquireUnitOfWork();

            // Task clone =
            // (Task)uow.registerObject(task);

            task.setStfCreationState(p_stfCreationState);
            //for GBS-3331: createSTF regard as exporting
            if(p_stfCreationState.equals("COMPLETED"))
            {
            	WorkflowExportingHelper.setAsNotExporting(task.getWorkflow().getId()); 
            }
            
            session.saveOrUpdate(task);
            // uow.commit();
            tx.commit();

            // notify the PM if the stf creation failed
            if (Task.FAILED.equals(p_stfCreationState))
            {
                notifyProjectManager(task);
            }
        }
        catch (Exception e)
        {
            tx.rollback();
            throw new TaskException(TaskException.MSG_FAILED_TO_UPDATE_TASK,
                    null, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////
    // Local methods
    // ///////////////////////////////////////////////////////////////////

    /*
     * Create and add a reserved time based on the specifed type to the given
     * user calendar.
     */
    private void addReservedTimeToUserCal(TaskImpl p_clonedTask,
            String p_reservedTimeType, Session p_session,
            UserFluxCalendar p_calClone, Date p_baseDate, Date p_estimatedDate)
    {
        // if calendaring module is not installed, don't create reserved times.
        if (!Modules.isCalendaringInstalled())
        {
            return;
        }

        TimeZone tz = p_calClone.getTimeZone();
        Timestamp start = new Timestamp(tz);
        start.setDate(p_baseDate);
        Timestamp end = new Timestamp(tz);
        end.setDate(p_estimatedDate);

        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(p_clonedTask.getTaskName());
        sb.append("]");
        sb.append("[");
        sb.append(p_clonedTask.getJobName());
        sb.append("]");
        sb.append("[");
        sb.append(p_clonedTask.getProjectManagerId());
        sb.append("]");

        String taskName = sb.toString();

        ReservedTime rt = new ReservedTime(taskName, p_reservedTimeType, start,
                start.getHour(), start.getMinute(), end, end.getHour(),
                end.getMinute(), null, p_clonedTask.getIdAsLong());

        p_calClone.addReservedTime(rt);
        p_session.save(rt);

        // now add the buffer (if not set to zero)
        if (p_calClone.getActivityBuffer() > 0)
        {
            Timestamp bufferEnd = new Timestamp(tz);
            bufferEnd.setDate(end.getDate());
            bufferEnd.add(Timestamp.HOUR, p_calClone.getActivityBuffer());
            ReservedTime buffer = new ReservedTime(taskName,
                    ReservedTime.TYPE_BUFFER, end, end.getHour(),
                    end.getMinute(), bufferEnd, bufferEnd.getHour(),
                    bufferEnd.getMinute(), null, p_clonedTask.getIdAsLong());

            p_calClone.addReservedTime(buffer);
            p_session.save(buffer);
        }
    }

    /**
     * Do initialization.
     */
    private void init() throws TaskException
    {
        // initialize the WorkflowServer instance.
        try
        {
            m_workflowServer = ServerProxy.getWorkflowServer();
        }
        catch (GeneralException ge)
        {
            String[] messageArgument =
            { "Couldn't find the WorkflowServer." };

            CATEGORY.error("TaskException is thrown from: "
                    + "TaskManagerLocal::init(): " + messageArgument, ge);

            throw new TaskException(TaskException.MSG_FAILED_TO_INIT_SERVER,
                    messageArgument, ge);
        }

        // initialize the UserManager instance.
        try
        {
            m_userManager = ServerProxy.getUserManager();
        }
        catch (GeneralException ge)
        {
            String[] messageArgument =
            { "Couldn't find the UserManager." };

            CATEGORY.error("TaskException is thrown from: "
                    + "TaskManagerLocal::init(): " + messageArgument, ge);

            throw new TaskException(TaskException.MSG_FAILED_TO_INIT_SERVER,
                    messageArgument, ge);
        }

        // initialize the WorkflowManager instance.
        try
        {
            m_workflowManager = ServerProxy.getWorkflowManager();
        }
        catch (GeneralException ge)
        {
            String[] messageArgument =
            { "Couldn't find the WorkflowManager." };

            CATEGORY.error("TaskException is thrown from: "
                    + "TaskManagerLocal::init(): " + messageArgument, ge);

            throw new TaskException(TaskException.MSG_FAILED_TO_INIT_SERVER,
                    messageArgument, ge);
        }

        // initialize the PageEventObserver instance.
        try
        {
            m_pageEventObserver = ServerProxy.getPageEventObserver();
        }
        catch (GeneralException ge)
        {
            String[] messageArgument =
            { "Couldn't find the PageEventObserver." };

            CATEGORY.error("TaskException is thrown from: "
                    + "TaskManagerLocal::init(): " + messageArgument, ge);

            throw new TaskException(TaskException.MSG_FAILED_TO_INIT_SERVER,
                    messageArgument, ge);
        }
    }

    /*
     * Send an email from admin to the Project Manager in regards to stf
     * creation failure.
     */
    private void notifyProjectManager(Task p_task)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        try
        {
            GlobalSightLocale targetLocale = p_task.getTargetLocale();
            Job job = p_task.getWorkflow().getJob();
            String companyIdStr = String.valueOf(job.getCompanyId());

            WorkflowTemplateInfo wfti = job.getL10nProfile()
                    .getWorkflowTemplateInfo(targetLocale);

            // Job -> name (id)
            StringBuffer jobInfo = new StringBuffer();
            jobInfo.append(p_task.getJobName());
            jobInfo.append(" (");
            jobInfo.append(p_task.getJobId());
            jobInfo.append(")");

            // task -> name (id)
            StringBuffer taskInfo = new StringBuffer();
            taskInfo.append(p_task.getTaskName());
            taskInfo.append(" (");
            taskInfo.append(p_task.getId());
            taskInfo.append(")");

            SystemConfiguration config = SystemConfiguration.getInstance();
            String capLoginUrl = config
                    .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

            String[] messageArguments = new String[4];
            messageArguments[0] = jobInfo.toString();
            messageArguments[1] = taskInfo.toString();
            messageArguments[2] = capLoginUrl;

            User user = null;
            List wfManagerIds = p_task.getWorkflow().getWorkflowOwnerIdsByType(
                    Permission.GROUP_WORKFLOW_MANAGER);
            int size = wfManagerIds.size();
            // notify workflow managers (if any)
            for (int i = 0; i < size; i++)
            {
                user = m_userManager.getUser((String) wfManagerIds.get(i));
                Locale userLocale = LocaleWrapper.getLocale(user
                        .getDefaultUILocale());
                messageArguments[3] = targetLocale.getDisplayName(userLocale);
                ServerProxy.getMailer().sendMailFromAdmin(user,
                        messageArguments,
                        MailerConstants.STF_CREATION_FAILED_SUBJECT,
                        "message_stf_creation_failed", companyIdStr);
            }

            if (wfti.notifyProjectManager())
            {
                String userId = wfti.getProjectManagerId();
                user = m_userManager.getUser(userId);
                Locale userLocale = LocaleWrapper.getLocale(user
                        .getDefaultUILocale());
                messageArguments[3] = targetLocale.getDisplayName(userLocale);
                ServerProxy.getMailer().sendMailFromAdmin(user,
                        messageArguments,
                        MailerConstants.STF_CREATION_FAILED_SUBJECT,
                        "message_stf_creation_failed", companyIdStr);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Failed to notify Project Manager about stf creation failure.",
                    e);
        }
    }

    /*
     * private void rollBackDBTaskCompletion(Task p_task, Date p_originalDate)
     * throws TaskException { p_task.setCompletedDate(p_originalDate); try {
     * TaskPersistenceAccessor.updateTask(p_task); } catch (TaskException te) {
     * String[] msgArgs = { "TopLink access error!" };
     * CATEGORY.error("rollBackDBTaskCompletion(): " + te.toString() + "
     * p_task=" + WorkflowHelper.toDebugString(p_task) + " p_originalDate=" +
     * WorkflowHelper.toDebugString(p_originalDate), te); throw new
     * TaskException(TaskException.MSG_FAILED_TO_COMPLETE_TASK, msgArgs, te); }
     * }
     */

    /**
     * To get a Date object representing the current time.
     */
    private Date getCurrentTime()
    {
        return new Date();
    }

    /**
     * This method checks to see if the person has the permission to get task
     * without assignee check.
     */
    private boolean canManageProjects(String p_userId) throws RemoteException,
            TaskException
    {
        boolean result;
        try
        {
            // get the person's permissions and check if they can manage
            // projects
            PermissionSet perms = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_userId);
            result = perms.getPermissionFor(Permission.PROJECTS_MANAGE)
                    || perms.getPermissionFor(Permission.ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY);
        }
        catch (Exception e)
        {
            throw new TaskException(e);
        }

        return result;
    }

    /**
     * To get the name of the project manager by the given id.
     */
    private String getProjectManagerName(String p_prjManagerUserId)
            throws RemoteException, TaskException
    {
        String name = null;

        if (p_prjManagerUserId != null)
        {
            try
            {
                name = m_userManager.getUser(p_prjManagerUserId).getUserName();
            }
            catch (GeneralException ge)
            {
                String[] messageArgument =
                { "Failed to get name for the project manager: "
                        + p_prjManagerUserId };

                CATEGORY.error("TaskException is thrown from: "
                        + "TaskManagerLocal::getProjectManagerName(): "
                        + messageArgument, ge);

                throw new TaskException(TaskException.MSG_FAILED_TO_GET_TASK,
                        messageArgument, ge);
            }
        }

        return name;
    }

    /*
     * Create a reserved time along with a possible buffer and add it to the
     * acceptor's calendar.
     */
    private Date createReservedTime(Date p_baseDate, TaskImpl p_clonedTask,
            long p_taskDuration, String p_reservedTimeType, String p_userId,
            Session p_session) throws Exception
    {
        UserFluxCalendar userCalendar = ServerProxy.getCalendarManager()
                .findUserCalendarByOwner(p_userId);
        UserFluxCalendar calClone = (UserFluxCalendar) p_session.get(
                UserFluxCalendar.class, userCalendar.getIdAsLong());

        Date estimatedDate = ServerProxy.getEventScheduler().determineDate(
                p_baseDate, userCalendar, p_taskDuration);
        // if calendaring module is not installed, don't create reserved times.
        if (Modules.isCalendaringInstalled())
        {
            addReservedTimeToUserCal(p_clonedTask, p_reservedTimeType,
                    p_session, calClone, p_baseDate, estimatedDate);
        }
        p_session.saveOrUpdate(calClone);
        return estimatedDate;
    }

    // get the task email info for the given job.
    private TaskEmailInfo createTaskEmailInfo(Job p_job, Task p_task,
            String p_rejectComment)
    {
        Workflow wf = p_task.getWorkflow();
        WorkflowTemplateInfo wfti = p_job.getL10nProfile()
                .getWorkflowTemplateInfo(p_task.getTargetLocale());

        TaskEmailInfo emailInfo = new TaskEmailInfo(
                p_task.getProjectManagerId(),
                wf.getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                wfti.notifyProjectManager(), p_job.getPriority(),
                p_rejectComment);

        emailInfo.setJobId(new Long(p_job.getId()).toString());
        emailInfo.setJobName(p_task.getJobName());
        String projectName = p_job.getL10nProfile().getProject().getName();
        emailInfo.setProjectName(projectName);
        emailInfo.setProjectIdAsLong(new Long(p_job.getL10nProfile()
                .getProjectId()));
        emailInfo.setWfIdAsLong(wf.getIdAsLong());
        emailInfo.setSourceLocale(p_job.getSourceLocale().toString());
        emailInfo.setTargetLocale(p_task.getTargetLocale().toString());

        return emailInfo;
    }

    private void printDebuggingInfo(Task p_task)
    {
        Workflow workflow = p_task.getWorkflow();
        Job job = workflow.getJob();

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Task is: " + p_task.toString());
            CATEGORY.debug("Task debug string is:"
                    + WorkflowHelper.toDebugString(p_task));
            CATEGORY.debug("Task name is: " + p_task.getTaskName());
            CATEGORY.debug("TargetLocale is: " + workflow.getTargetLocale());
            CATEGORY.debug("JobName is: " + job.getJobName());            
        }
    }

    /*
     * If the user had already accepted the task, perform the unacceptance
     * process. Otherwise, just set the task comments and clear the acceptor
     * name.
     */
    public void rejectTask(String p_userId, Task p_task, String p_rejectComment)
            throws RemoteException, TaskException
    {
        TaskInfo taskInfo = null;
        try
        {
            taskInfo = rejectTask(p_task, p_userId, p_rejectComment);
        }
        catch (Exception te)
        {
            String[] msgArgs =
            { "TopLink access error!" };
            CATEGORY.error("rejectTask(): " + te.toString() + " p_task="
                    + WorkflowHelper.toDebugString(p_task), te);
            throw new TaskException(TaskException.MSG_FAILED_TO_REJECT_TASK,
                    msgArgs, te);
        }

        // IFlow rejects task
        try
        {
            Job job = p_task.getWorkflow().getJob();
            TaskEmailInfo emailInfo = createTaskEmailInfo(job, p_task,
                    p_rejectComment);
            printDebuggingInfo(p_task);

            m_workflowServer.rejectTask(p_userId, p_task.getId(), taskInfo,
                    emailInfo);
        }
        catch (WorkflowException we)
        {
            try
            {
                // rollback the above DB action for transaction consistence
                Comment taskComment = new CommentImpl(new Date(), p_userId,
                        p_rejectComment, p_task);
                p_task.removeTaskComment(taskComment);
                TaskPersistenceAccessor.updateTask(p_task);
            }
            catch (TaskException te)
            {
                String[] msgArgs =
                { "TopLink access error!" };
                CATEGORY.error("rejectTask(): " + te.toString() + " p_task="
                        + WorkflowHelper.toDebugString(p_task), te);
                throw new TaskException(
                        TaskException.MSG_FAILED_TO_REJECT_TASK, msgArgs, te);
            }

            // throw IFlow Exception
            String[] msgArgs =
            { "WorkflowServer error!" };
            CATEGORY.error("rejectTask(): " + we.toString() + " p_task="
                    + WorkflowHelper.toDebugString(p_task), we);
            ;
            throw new TaskException(we.getMessageKey(),
                    we.getMessageArguments(), we, we.getPropertyFileName());
        }
    }

    /*
     * If the user had already accepted the task, perform the unacceptance
     * process. Otherwise, just set the task comments and clear the acceptor
     * name.
     */
    private TaskInfo rejectTask(Task p_task, String p_rejectorId,
            String p_rejectComment) throws Exception
    {

        if (p_task.getState() == Task.STATE_ACCEPTED)
        {
            // -1 indicates that the default path would be from the
            // beginning of the workflow (form START node)
            List wfTaskInfos = m_workflowServer.timeDurationsInDefaultPath(
                    null, p_task.getWorkflow().getId(), -1);

            unacceptTask(p_task, wfTaskInfos, null, p_rejectorId,
                    p_rejectComment);
        }
        else
        {
            // Save reject comment to DB
            Comment taskComment = new CommentImpl(new Date(), p_rejectorId,
                    p_rejectComment, p_task);
            p_task.addTaskComment(taskComment);
            p_task.setAcceptor(null);
            p_task.setAcceptedDate(null);
            TaskPersistenceAccessor.updateTask(p_task);

            // remove the proposed reserved time.
            removeScheduledActivity(p_task.getId(), p_rejectorId);
        }

        return new TaskInfo(p_task.getId(), p_task.getTaskName(),
                p_task.getState(), p_task.getEstimatedAcceptanceDate(),
                p_task.getEstimatedCompletionDate(), null, null,
                p_task.getType());
    }

    /*
     * Remove all the reserved times with the given task id.
     */
    private void removeReservedTimes(long p_taskId)
    {
        try
        {
            ServerProxy.getCalendarManager()
                    .removeScheduledActivities(p_taskId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not remove reserved times for task id "
                    + p_taskId, e);
            throw new GeneralException(e);
        }
    }

    /**
     * Remove the reserved times from the specified user's calendar.
     */
    private void removeScheduledActivity(long p_taskId, String p_userId)
    {
        try
        {
            ServerProxy.getCalendarManager().removeScheduledActivity(p_taskId,
                    p_userId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Could not remove reserved time for task id "
                    + p_taskId, e);
        }
    }

    /*
     * Unaccept a task that was previously accepted by the same user by reseting
     * the task attributes to the pre-acceptance values (i.e. the estimated
     * dates, acceptor, acceptance date, and so on).
     */
    private void unacceptTask(Task task, List wfTaskInfos, Date p_completedby,
            String p_userId, String p_rejectComment) throws Exception
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            TaskImpl task_temp = (TaskImpl) session.get(TaskImpl.class,
                    new Long(task.getId()));
            task_temp.setWorkflowTask(((TaskImpl) task).getWorkflowTask());
            task_temp.setAcceptor(null);
            task_temp.setAcceptedDate(null);
            task_temp.setState(Task.STATE_ACTIVE);

            if (p_completedby == null)
            {
                p_completedby = ServerProxy.getEventScheduler().determineDate(
                        task_temp.getEstimatedAcceptanceDate(),
                        ServerProxy.getCalendarManager().findDefaultCalendar(
                                String.valueOf(task_temp.getCompanyId())),
                        task_temp.getTaskDuration());
            }
            task_temp.setEstimatedCompletionDate(p_completedby);
            // only during a rejection of a task
            if (p_rejectComment != null)
            {
                Comment taskComment = new CommentImpl(new Date(), p_userId,
                        p_rejectComment, task_temp);
                task_temp.addTaskComment(taskComment);
            }

            Workflow wf = (Workflow) session.get(WorkflowImpl.class, task_temp
                    .getWorkflow().getIdAsLong());
            // now update the estimated acceptance/completion for the rest
            // of the tasks in the default path (following the accepted
            // task) and the workflow's completion time.
            updateDefaultPathTasks(p_completedby, wfTaskInfos, wf,
                    task_temp.getId(), session);

            // at this point we should create a proposed reserved time for
            // assigness
            // with a 'de-active' state (since the work item will be assigned to
            // them).
            WorkflowTaskInstance wft = m_workflowServer
                    .getWorkflowTaskInstance(p_userId, task_temp.getId(),
                            WorkflowConstants.TASK_ALL_STATES);
            Vector workItems = wft.getWorkItems();
            Date baseDate = new Date();

            int size = workItems == null ? -1 : workItems.size();
            for (int i = 0; i < size; i++)
            {
                EnvoyWorkItem ewi = (EnvoyWorkItem) workItems.get(i);
                if (ewi.getWorkItemState() == WorkflowConstants.TASK_DEACTIVE)
                {
                    createReservedTime(
                            baseDate,
                            task_temp,
                            task_temp.getTaskAcceptDuration()
                                    + task_temp.getTaskDuration(),
                            ReservedTime.TYPE_PROPOSED, ewi.getAssignee(),
                            session);
                }
            }

            session.saveOrUpdate(wf);
            session.saveOrUpdate(task_temp);
            tx.commit();
            // now remove the reserved time from the user's calendar.
            // This does not need to be part of the same transaction
            removeScheduledActivity(task_temp.getId(), p_userId);
        }
        catch (Exception e)
        {
            tx.rollback();
            // e.printStackTrace();
            CATEGORY.error(e.getMessage(), e);
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
     * Update the attributes of the accepted task by setting the acceptance
     * date, acceptor's user id, and estimated completion date.
     */
    private TaskInfo updateAcceptedTask(Date p_baseDate, TaskImpl p_clonedTask,
            String p_userId, Session p_session) throws Exception
    {
        if (Task.STATE_FINISHING != p_clonedTask.getState())
        {
            p_clonedTask.setState(Task.STATE_ACCEPTED);
        }

        p_clonedTask.setAcceptor(p_userId);

        // retrieve the overdue value from the jbpm task instance config xml.
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                    .getTaskInstance(p_clonedTask.getId(), ctx);
            String config = WorkflowJbpmUtil.getConfigure(taskInstance
                    .getTask().getTaskNode());
            WorkflowNodeParameter param = WorkflowNodeParameter
                    .createInstance(config);
            long overdueToPM = param.getLongAttribute(
                    WorkflowConstants.FIELD_OVERDUE_PM_TIME,
                    WorkflowTaskInstance.NO_RATE);
            long overdueToUser = param.getLongAttribute(
                    WorkflowConstants.FIELD_OVERDUE_USER_TIME,
                    WorkflowTaskInstance.NO_RATE);

            Date estimatedDate = createReservedTime(p_baseDate, p_clonedTask,
                    p_clonedTask.getTaskDuration(), ReservedTime.TYPE_ACTIVITY,
                    p_userId, p_session);
            p_clonedTask.setAcceptedDate(p_baseDate);
            p_clonedTask.setEstimatedCompletionDate(estimatedDate);

            TaskInfo taskInfo = new TaskInfo(p_clonedTask.getId(),
                    p_clonedTask.getTaskName(), p_clonedTask.getState(), null,
                    estimatedDate, p_baseDate, null, p_clonedTask.getType());
            taskInfo.setOverdueToPM(overdueToPM);
            taskInfo.setOverdueToUser(overdueToUser);

            return taskInfo;
        }
        finally
        {
            ctx.close();
        }
    }

    /*
     * Update the default path tasks by setting their estimated acceptance, and
     * completion times. Also set the estimated completion time for the
     * workflow. Note that the id for p_acceptedTaskId indicates that only the
     * task with the given id and the ones after it will be updated.
     * 
     * @param p_baseDate - The based date used for finding dates based on
     * durations. @param p_wfTaskInfos - The tasks (as WfTaskInfo objects) in
     * default path. @param p_wfClone - The workflow that needs to be udpated.
     * 
     * @param p_completedTaskId - The id of the base task. @param p_uow - Unit
     * of work for persistance purposes.
     */
    private void updateDefaultPathTasks(Date p_baseDate, List p_wfTaskInfos,
            Workflow p_wfClone, long p_acceptedTaskId, Session p_session)
            throws Exception
    {
        int size = p_wfTaskInfos.size();
        Hashtable ht = p_wfClone.getTasks();
        FluxCalendar calendar = ServerProxy.getCalendarManager()
                .findDefaultCalendar(String.valueOf(p_wfClone.getCompanyId()));
        Date estimatedDate = p_baseDate;
        boolean found = false;
        // loop thru the tasks following the given start task for updating
        // the estimated dates for the tasks and possibly workflow.
        for (int i = 0; i < size; i++)
        {
            WfTaskInfo wfTaskInfo = (WfTaskInfo) p_wfTaskInfos.get(i);

            TaskImpl task = (TaskImpl) ht.get(new Long(wfTaskInfo.getId()));
            if (!found)
            {
                found = p_acceptedTaskId == wfTaskInfo.getId();
            }
            else
            {
                TaskImpl task_temp = (TaskImpl) p_session.get(TaskImpl.class,
                        task.getIdAsLong());
                Date acceptBy = ServerProxy.getEventScheduler().determineDate(
                        estimatedDate, calendar,
                        wfTaskInfo.getAcceptanceDuration());
                task_temp.setEstimatedAcceptanceDate(acceptBy);

                Date completeBy = ServerProxy.getEventScheduler()
                        .determineDate(estimatedDate, calendar,
                                wfTaskInfo.getTotalDuration());

                task_temp.setEstimatedCompletionDate(completeBy);
                estimatedDate = completeBy;
                p_session.saveOrUpdate(task_temp);
            }
        }

        // For sla report issue
        if (!p_wfClone.isEstimatedCompletionDateOverrided())
        {
            p_wfClone.setEstimatedCompletionDate(estimatedDate);
        }
        // For sla report issue
        p_wfClone.updateTranslationCompletedDates();
    }

    /**
     * Make sure no page is in UPDATING state.
     */
    private void validateStateOfPages(Task p_task) throws TaskException
    {
        try
        {
            PageStateValidator.validateStateOfPagesInTask(p_task);
        }
        catch (Exception e)
        {
            throw new TaskException(e);
        }
    }

    /*
     * Check the state of the task before performing the acceptance process.
     */
    private Task validateTaskForAcceptance(String p_userId, Task p_task)
            throws TaskException, RemoteException
    {
        // first check and make sure none of the pages of this task
        // are in the updating process
        validateStateOfPages(p_task);

        Task task = getTask(p_userId, p_task.getId(),
                WorkflowConstants.TASK_ALL_STATES);
        // check if this task has already been accepted.
        if (task.getState() == Task.STATE_DEACTIVE)
        {
            String[] msgArgs =
            { "" + p_task.getId() };
            CATEGORY.error("acceptTask(): " + Arrays.asList(msgArgs).toString()
                    + " p_task=" + WorkflowHelper.toDebugString(p_task));
            throw new TaskException(TaskException.MSG_FAILED_TO_ACCEPT_TASK,
                    msgArgs, null);
        }

        return task;
    }

    /**
     * Get all active tasks by Job ID.
     */
    public List<TaskImpl> getActivieTasks(long p_jobId)
    {
        Map params = new HashMap();
        params.put(TaskDescriptorModifier.JOB_ID_ARG, p_jobId);
        return HibernateUtil.searchWithSql(
                TaskDescriptorModifier.ACTIVE_TASKS_BY_JOB_ID_SQL, params,
                TaskImpl.class);
    }
}
