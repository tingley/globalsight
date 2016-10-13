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
package com.globalsight.everest.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.persistence.workflow.JbpmVariable;

/**
 * 
 * <code>WorkflowJbpmPersistenceHandler</code> deals with queries from jbpm
 * database with the hibernate session in JbpmContext.
 * 
 * @version 1.2
 */
public class WorkflowJbpmPersistenceHandler
{
	private static final Logger c_logger = Logger
			.getLogger(WorkflowJbpmPersistenceHandler.class.getName());

	// Variable constants used for the sql/hql parameters.
	private final static String ACTOR_ID = "actorId";

	private final static String VARIABLE_NAME = "variableName";

	private final static String CATEGORY = "category";

	private final static String USER_ID = "userId";

	private final static String TASK_ID = "taskId";
    
    private final static String WORKFLOW_ID = "workflowId";

	private final static String IN_EXPRESSION = "@INEXPRESSION@";

	private final static String PM = "PM";

	private final static String VARIABLE_CATEGORY_REJECT = "reject";
    
    private final static String VARIABLE_CATEGORY_SKIP = "skip";

	// Order by JBPM_TASKINSTANCE.ID_ desc.
	private final static String ORDER_DESC = " order by ti.ID_ desc";

	// Used for querying the accepted task instances from jbpm database.
	private final static String ACCEPTED_TASK_INSTANCES = "select ti.* from JBPM_TASKINSTANCE ti where ti.ACTORID_ = :"
			+ ACTOR_ID
			+ " and ti.START_ is not null and ti.END_ is null"
			+ ORDER_DESC;

	// Used for querying the accepted task instances for PM from jbpm database.
	private final static String ACCEPTED_TASK_INSTANCES_PM = "select ti.* from JBPM_TASKINSTANCE ti where ti.DESCRIPTION_ =:"
			+ PM
			+ " and ti.START_ is not null and ti.END_ is null"
			+ ORDER_DESC;

	// Used for querying the completed task instances from jbpm database.
	private final static String COMPLETED_TASK_INSTANCES = "select ti.* from JBPM_TASKINSTANCE ti where ti.ACTORID_ = :"
			+ ACTOR_ID + " and ti.END_ is not null" + ORDER_DESC;

	// Used for querying the completed task instances for PM from jbpm database.
	private final static String COMPLETED_TASK_INSTANCES_PM = "select ti.* from JBPM_TASKINSTANCE ti where ti.DESCRIPTION_ =:"
			+ PM + " and ti.END_ is not null" + ORDER_DESC;

	// Used for querying the task instances in all status for PM from jbpm
	// database.
	private final static String ALL_TASK_INSTANCES_PM = "select ti.* from JBPM_TASKINSTANCE ti where ti.DESCRIPTION_ =:"
			+ PM + ORDER_DESC;

	// Used for querying the task instance from jbpm database by task node id.
	private final static String TASK_INSTANCE_BY_ID = "select distinct ti.* from JBPM_TASKINSTANCE ti inner join JBPM_TASK t on ti.TASK_ = t.ID_ inner join JBPM_NODE n on t.TASKNODE_ = n.ID_ where n.ID_ = :"
			+ TASK_ID + ORDER_DESC;

	/**
     * Gets a list of the task instances that belong to the user id based on
     * different task states.
     * 
     * @param p_userId
     *            the user id.
     * @param p_taskState
     *            the task state.
     * @param p_pm
     *            is pm or not (true:PM|false:user).
     * @param p_ctx
     *            the JbpmContext.
     * 
     * @return a list of the task instances.
     */
	public static List<TaskInstance> getTaskInstances(String p_userId,
			int p_taskState, boolean p_pm, JbpmContext p_ctx)
	{
		Session session = p_ctx.getSession();
		switch (p_taskState)
		{
			// rejected activities
			case WorkflowConstants.TASK_DECLINED:
				return rejectedTaskInstances(p_userId, session, p_pm);
				// available activities
			case WorkflowConstants.TASK_ACTIVE:

				return activeTaskInstances(p_userId, session, p_pm);

            // in progress activities
			case WorkflowConstants.TASK_ACCEPTED:
				return acceptedTaskInstances(p_userId, session, p_pm);
			
			case WorkflowConstants.TASK_READEAY_DISPATCH_GSEDTION:
                return acceptedTaskInstances(p_userId, session, p_pm);
                
			case WorkflowConstants.TASK_DISPATCHED_TO_TRANSLATION:
                return acceptedTaskInstances(p_userId, session, p_pm);
                
			case WorkflowConstants.TASK_IN_TRANSLATION:
                return acceptedTaskInstances(p_userId, session, p_pm);
                
			case WorkflowConstants.TASK_TRANSLATION_COMPLETED:
                return acceptedTaskInstances(p_userId, session, p_pm);
            
			case WorkflowConstants.TASK_GSEDITION_IN_PROGESS:
                return acceptedTaskInstances(p_userId, session, p_pm);
                
				// finished activities
			case WorkflowConstants.TASK_COMPLETED:
				return completedTaskInstances(p_userId, session, p_pm);
				// activities in all states
			case WorkflowConstants.TASK_ALL_STATES:
				return allTaskInstances(p_userId, session, p_pm);
		}
		return new ArrayList<TaskInstance>();
	}

	/**
     * Gets the task instances in all status.
     * 
     * @param p_userId
     *            the user id.
     * @param p_session
     *            the hibernate session.
     * @param p_pm
     *            is pm or not (true:PM|false:user).
     * 
     * @return the task instances in all status.
     */
	@SuppressWarnings("unchecked")
	private static List<TaskInstance> allTaskInstances(String p_userId,
			Session p_session, boolean p_pm)
	{
		SQLQuery query = null;
		if (p_pm)
		{
			query = p_session.createSQLQuery(ALL_TASK_INSTANCES_PM);
			query.setString(PM, p_userId);
		}
		else
		{
			query = p_session.createSQLQuery(SqlHolder.allTask);
			query.setString(ACTOR_ID, p_userId);
			query.setString(VARIABLE_NAME,
					WorkflowConstants.VARIABLE_IS_REJECTED);
			query.setString(CATEGORY, VARIABLE_CATEGORY_REJECT);

		}
		query.addEntity(TaskInstance.class);

		return query.list();
	}

	/**
     * Gets the completed task instances.
     * 
     * @param p_userId
     *            the user id.
     * @param p_session
     *            the hibernate session.
     * @param p_pm
     *            is pm or not (true:PM|false:user).
     * 
     * @return the completed task instances.
     */
	@SuppressWarnings("unchecked")
	private static List<TaskInstance> completedTaskInstances(String p_userId,
			Session p_session, boolean p_pm)
	{
		SQLQuery query = null;
		if (p_pm)
		{
			query = p_session.createSQLQuery(COMPLETED_TASK_INSTANCES_PM);
			query.setString(PM, p_userId);
		}
		else
		{
			query = p_session.createSQLQuery(COMPLETED_TASK_INSTANCES);
			query.setString(ACTOR_ID, p_userId);
		}
		query.addEntity(TaskInstance.class);

		return query.list();
	}

	/**
     * Gets the accepted task instances.
     * 
     * @param p_userId
     *            the user id.
     * @param p_session
     *            the hibernate session.
     * @param p_pm
     *            is pm or not (true:PM|false:user).
     * 
     * @return the accepted task instances.
     */
	@SuppressWarnings("unchecked")
	private static List<TaskInstance> acceptedTaskInstances(String p_userId,
			Session p_session, boolean p_pm)
	{
		SQLQuery query = null;
		if (p_pm)
		{
			query = p_session.createSQLQuery(ACCEPTED_TASK_INSTANCES_PM);
			query.setString(PM, p_userId);
		}
		else
		{
			query = p_session.createSQLQuery(ACCEPTED_TASK_INSTANCES);
			query.setString(ACTOR_ID, p_userId);

		}
		query.addEntity(TaskInstance.class);

		return query.list();
	}

	/**
     * Gets the active(available) task instances.
     * 
     * @param p_userId
     *            the user id.
     * @param p_session
     *            the hibernate session.
     * @param p_pm
     *            is pm or not (true:PM|false:user).
     * 
     * @return the active(available) task instances.
     */
	@SuppressWarnings("unchecked")
	private static List<TaskInstance> activeTaskInstances(String p_userId,
			Session p_session, boolean p_pm)
	{
		SQLQuery query = null;
		if (p_pm)
		{
			query = p_session.createSQLQuery(SqlHolder.activeTaskPm);
			query.setString(PM, p_userId);
			if (c_logger.isDebugEnabled())
			{
				c_logger.debug("The query sql is " + SqlHolder.activeTaskPm);
			}
		}
		else
		{
			query = p_session.createSQLQuery(SqlHolder.activeTask);
			if (c_logger.isDebugEnabled())
			{
				c_logger.debug("The query sql is " + SqlHolder.activeTask);
			}
			query.setString(ACTOR_ID, p_userId);
		}

		query.setString(VARIABLE_NAME, WorkflowConstants.VARIABLE_IS_REJECTED);
		query.setString(CATEGORY, VARIABLE_CATEGORY_REJECT);
		query.addEntity(TaskInstance.class);

		return query.list();
	}

	/**
     * Gets the rejected task instances.
     * 
     * @param p_userId
     *            the user id.
     * @param p_session
     *            the hibernate session.
     * @param p_pm
     *            is pm or not (true:PM|false:user).
     * 
     * @return the rejected task instances.
     */
	@SuppressWarnings("unchecked")
	private static List<TaskInstance> rejectedTaskInstances(String p_userId,
			Session p_session, boolean p_pm)
	{
		SQLQuery query = null;
		if (p_pm)
		{
			query = p_session.createSQLQuery(SqlHolder.rejectTaskPm);
			query.setString(PM, p_userId);
		}
		else
		{
			query = p_session.createSQLQuery(SqlHolder.rejectTask);
			query.setString(USER_ID, p_userId);
		}
		query.setString(VARIABLE_NAME, WorkflowConstants.VARIABLE_IS_REJECTED);
		query.setString(CATEGORY, VARIABLE_CATEGORY_REJECT);
		query.addEntity(TaskInstance.class);

		return query.list();
	}

	/**
     * Gets the latest task instance with specified task node id.
     * 
     * @param p_taskId -
     *            The task node id.
     * @param p_ctx
     *            the JbpmContext.
     * 
     * @return the latest task instance.
     */
	public static TaskInstance getTaskInstance(long p_taskId, JbpmContext p_ctx)
	{
		List taskInstances = getTaskInstancesById(p_taskId, p_ctx);
		// The first task instance is the latest one because the list is ordered
		// by desc.
		if (taskInstances == null || taskInstances.size() == 0)
		{
		    return null;
		}
		else
		{
		    return (TaskInstance) taskInstances.get(0);
		}
	}

	/**
     * Saves the task variable to the databases.
     * 
     * @param ctx
     * @param name
     * @param value
     * @param taskInstance
     */
	public static void saveTaskVariable(JbpmContext ctx, String name,
			String value, TaskInstance taskInstance)
	{
		JbpmVariable taskVariable = new JbpmVariable();

		taskVariable.setName(name);
		taskVariable.setValue(value);
		taskVariable.setCategory(VARIABLE_CATEGORY_REJECT);
		taskVariable.setTaskInstance(taskInstance);

		ctx.getSession().save(taskVariable);
	}

    public static void saveSkipVariable(TaskInstance taskInstance, long workflowId) {
        JbpmVariable taskVariable = new JbpmVariable();
        taskVariable.setName(VARIABLE_CATEGORY_SKIP);
        taskVariable.setValue(String.valueOf(workflowId));
        taskVariable.setCategory(VARIABLE_CATEGORY_SKIP);
        taskVariable.setTaskInstance(taskInstance);
        
        WorkflowConfiguration.getInstance().getCurrentContext().getSession().save(taskVariable);
    }
    
	/**
     * Judges whether the user is rejected for the specified
     * <code>TaskInstance</code>. <br>
     * The rejected user will be stored in the table
     * <code>JBPM_GS_VARIABLE</code>.
     * 
     * @param ctx
     *            {@code JbpmContext}
     * @param userId
     *            the user id.
     * @param taskInstance
     *            {@code TaskInstance}
     * @return <code>true</code> when the user is reject by the specified
     *         taskinstance.
     */
	public static boolean isUserRejected(JbpmContext ctx, String userId,
			TaskInstance taskInstance)
	{

		SQLQuery query = null;
		Session session = ctx.getSession();

		query = session.createSQLQuery(SqlHolder.isUserReject);

		query.setString(VARIABLE_NAME, WorkflowConstants.VARIABLE_IS_REJECTED);
		query.setString(CATEGORY, VARIABLE_CATEGORY_REJECT);
		query.setString(ACTOR_ID, userId);
		query.setLong(TASK_ID, taskInstance.getId());
        
		Number num = (Number) query.uniqueResult();

		return num == null ? false : num.longValue() > 0;

	}

	/**
	 * Fix for GBS-1470
     * Judges whether the user is rejected for (This user Rejected the task first, then reassign to this user) 
     * 
     * @param ctx
     *            {@code JbpmContext}
     * @param userId
     *            the user id.
     * @param taskInstance
     *            {@code TaskInstance}
     * @return <code>true</code> The user reject, and not been reassigned.
     */
	public static boolean isUserRejectedForReassign(JbpmContext ctx, String userId,
			TaskInstance taskInstance)
	{

		SQLQuery query = null;
		Session session = ctx.getSession();

		query = session.createSQLQuery(SqlHolder.isUserRejectForReassign);

		query.setString(VARIABLE_NAME, WorkflowConstants.VARIABLE_IS_REJECTED);
		query.setString(CATEGORY, VARIABLE_CATEGORY_REJECT);
		query.setString(ACTOR_ID, userId);
		query.setLong(TASK_ID, taskInstance.getId());
        long id = taskInstance.getId();
		Number num = (Number) query.uniqueResult();

		return num == null ? false : num.longValue() > 0;

	}
	
	
	/**
     * Judges whether the task is rejected<br>
     * The rule for judges the task whether is rejected is as below:
     * <ul>
     * <li>If the lp reject the task, the task showed rejected by the lp</li>
     * <li>If the lp(s) reject the task and there is extra lp availabe for the
     * task, the task showed rejected for pm</li> *
     * <li>If the lp(s) reject the task and there is no extra lp availabe for
     * the task, the task showed availabe for pm</li>
     * </ul>
     * 
     * 
     * @param ctx
     *            {@code JbpmContext}
     * @param taskInstance
     *            {@code TaskInstance}
     * @param p_pm
     *            The name of the pm
     * @return <code>true</code> when the task is rejected
     */
	public static boolean isTaskRejected(JbpmContext ctx,
			TaskInstance taskInstance, String p_pm)
	{

		SQLQuery query = null;
		Session session = ctx.getSession();

		query = session.createSQLQuery(SqlHolder.isTaskReject);

		query.setString(VARIABLE_NAME, WorkflowConstants.VARIABLE_IS_REJECTED);
		query.setString(CATEGORY, VARIABLE_CATEGORY_REJECT);
		query.setString(PM, p_pm);
		query.setLong(TASK_ID, taskInstance.getId());

		Number num = (Number) query.uniqueResult();

		/* normally, the num should not be null */
		return num == null ? false : num.longValue() > 0;

	}

	/**
     * Gets the task instances with specified task node id.
     * 
     * @param p_taskId -
     *            The task node id.
     * @param p_ctx
     *            the JbpmContext.
     * 
     * @return a list of the task instances.
     */
	public static List getTaskInstancesById(long p_taskId, JbpmContext p_ctx)
	{
		Session session = p_ctx.getSession();
		SQLQuery query = session.createSQLQuery(TASK_INSTANCE_BY_ID);
		query.addEntity(TaskInstance.class);
		query.setLong(TASK_ID, p_taskId);
		List result = query.list();
		return result;
	}

	public static List getNonRejectedTaskInstancesByAssignee(String p_userId,
			JbpmContext p_ctx)
	{
		Session session = p_ctx.getSession();
		List<TaskInstance> taskInstances = new ArrayList<TaskInstance>();
		taskInstances.addAll(activeTaskInstances(p_userId, session, false));
		taskInstances.addAll(acceptedTaskInstances(p_userId, session, false));

		return taskInstances;
	}
    
    public static List<String> getSkippedTaskInstance(long workflowId, JbpmContext p_ctx) {
        Session session = p_ctx.getSession();
        SQLQuery query = session.createSQLQuery(SqlHolder.skippedTask);
        query.setString(WORKFLOW_ID, String.valueOf(workflowId));
        return query.list();
    }

	static class SqlHolder
	{
		static StringBuilder activeTaskSb = new StringBuilder();

		static StringBuilder activeTaskPmSb = new StringBuilder();

		static StringBuilder rejectTaskSb = new StringBuilder();

		static StringBuilder rejectTaskPmSb = new StringBuilder();

		static StringBuilder allTaskInstanceSb = new StringBuilder();

		static StringBuilder isUserRejectedSb = new StringBuilder();
		
		static StringBuilder isUserRejectedForReassignSb = new StringBuilder();

		static StringBuilder isAllUserRejectedSb = new StringBuilder();

		static StringBuilder isAvailableExistPmSb = new StringBuilder();

		static StringBuilder isTaskRejectedSb = new StringBuilder();
        
        static StringBuilder skippedTaskInstance = new StringBuilder();

		static
		{
			/* for the active task */
			activeTaskSb.append("select distinct ti.* ");
			activeTaskSb.append("from JBPM_TASKINSTANCE ti ");
			activeTaskSb.append("inner join JBPM_TASKACTORPOOL tap ");
			activeTaskSb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
			activeTaskSb.append("inner join JBPM_POOLEDACTOR pa ");
			activeTaskSb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
			activeTaskSb.append("left join JBPM_GS_VARIABLE vi  ");
			activeTaskSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			activeTaskSb.append("and vi.NAME = :").append(VARIABLE_NAME)
					.append(" ");
			activeTaskSb.append("and (vi.VALUE != :").append(ACTOR_ID).append(
					" ");
			activeTaskSb.append("or vi.VALUE is null) ");
			activeTaskSb.append("and vi.CATEGORY = :").append(CATEGORY).append(
					" ");
			activeTaskSb.append("where pa.ACTORID_ = :").append(ACTOR_ID)
					.append(" ");
			activeTaskSb.append("and ti.START_ is null ");
			activeTaskSb.append(ORDER_DESC);

			/* fro the active task PM */

			activeTaskPmSb.append("select distinct ti.* ");
			activeTaskPmSb.append("from JBPM_TASKINSTANCE ti ");
			activeTaskPmSb.append("inner join JBPM_TASKACTORPOOL tap ");
			activeTaskPmSb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
			activeTaskPmSb.append("inner join JBPM_POOLEDACTOR pa ");
			activeTaskPmSb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
			activeTaskPmSb.append("where ");
			activeTaskPmSb.append("( ");
			activeTaskPmSb.append("pa.ACTORID_ = :").append(PM).append(" ");
			activeTaskPmSb.append("or ti.DESCRIPTION_ = :").append(PM).append(
					" ");
			activeTaskPmSb.append(") and ti.start_ is null ");
			activeTaskPmSb.append("and ti.id_ not in ");
			activeTaskPmSb.append("( ");
			activeTaskPmSb.append(" select distinct ti.id_ ");
			activeTaskPmSb.append(" from JBPM_TASKINSTANCE ti ");
			activeTaskPmSb.append(" inner join JBPM_TASKACTORPOOL tap ");
			activeTaskPmSb.append(" on ti.ID_ = tap.TASKINSTANCE_ ");
			activeTaskPmSb.append(" inner join JBPM_POOLEDACTOR pa ");
			activeTaskPmSb.append(" on tap.POOLEDACTOR_ = pa.ID_ ");
			activeTaskPmSb.append(" inner join JBPM_GS_VARIABLE vi  ");
			activeTaskPmSb.append(" on ti.ID_ = vi.TASKINSTANCE_ID ");
			activeTaskPmSb.append(" and vi.NAME = :").append(VARIABLE_NAME)
					.append(" ");
			activeTaskPmSb.append(" and vi.VALUE != pa.actorid_ ");
			activeTaskPmSb.append(" and vi.CATEGORY = :").append(CATEGORY)
					.append(" ");
			activeTaskPmSb.append(" and pa.ACTORID_ != :").append(PM)
					.append("");
			activeTaskPmSb.append(" where ti.START_ is null ");
			activeTaskPmSb.append(")");

			/* Used for querying the rejected task instances from jbpm database. */
			rejectTaskSb.append("select ti.* ");
			rejectTaskSb.append("from JBPM_TASKINSTANCE ti inner join JBPM_GS_VARIABLE vi ");
			rejectTaskSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			rejectTaskSb.append("where vi.NAME = :").append(VARIABLE_NAME).append(" ");
			rejectTaskSb.append("and vi.VALUE = :").append(USER_ID).append(" ");
			rejectTaskSb.append("and vi.CATEGORY = :").append(CATEGORY).append(" ");
			rejectTaskSb.append("and ti.ID_ not in (");
			rejectTaskSb.append("select ti.ID_ ");
			rejectTaskSb.append("from JBPM_TASKINSTANCE ti join JBPM_TASKACTORPOOL tap ");
			rejectTaskSb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
			rejectTaskSb.append("inner join JBPM_POOLEDACTOR pa ");
			rejectTaskSb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
			rejectTaskSb.append("inner join JBPM_GS_VARIABLE vi ");
			rejectTaskSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			rejectTaskSb.append("and vi.NAME = :").append(VARIABLE_NAME).append(" ");
			rejectTaskSb.append("and vi.VALUE = :").append(USER_ID).append(" ");
			rejectTaskSb.append("and vi.CATEGORY = :").append(CATEGORY).append(" ");
			rejectTaskSb.append("where pa.ACTORID_ = :").append(USER_ID).append(" ");
			rejectTaskSb.append(")");
			rejectTaskSb.append(ORDER_DESC);
			
			

			/* Used for querying the rejected task instances for PM from jbpm */

			rejectTaskPmSb.append("select distinct ti.* ");
			rejectTaskPmSb.append("from JBPM_TASKINSTANCE ti ");
			rejectTaskPmSb.append("inner join JBPM_TASKACTORPOOL tap ");
			rejectTaskPmSb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
			rejectTaskPmSb.append("inner join JBPM_POOLEDACTOR pa ");
			rejectTaskPmSb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
			rejectTaskPmSb.append("inner join JBPM_GS_VARIABLE vi  ");
			rejectTaskPmSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			rejectTaskPmSb.append("and vi.NAME = :").append(VARIABLE_NAME)
					.append(" ");
			rejectTaskPmSb.append("and vi.VALUE != pa.actorid_ ");
			rejectTaskPmSb.append("and vi.CATEGORY = :").append(CATEGORY)
					.append(" ");
			rejectTaskPmSb.append("and pa.ACTORID_ != :").append(PM).append("");

			/* Used for querying the task instances in all status from jbpm */
			allTaskInstanceSb.append("select distinct ti.* ");
			allTaskInstanceSb
					.append("from JBPM_TASKINSTANCE ti inner join JBPM_TASKACTORPOOL tap ");
			allTaskInstanceSb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
			allTaskInstanceSb.append("inner join JBPM_POOLEDACTOR pa ");
			allTaskInstanceSb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
			allTaskInstanceSb.append("left join JBPM_GS_VARIABLE vi ");
			allTaskInstanceSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			allTaskInstanceSb.append("where (pa.ACTORID_ = :").append(ACTOR_ID)
					.append(" ");
			allTaskInstanceSb.append("and ti.ACTORID_ is null) ");
			allTaskInstanceSb.append("or ti.ACTORID_ = :").append(ACTOR_ID)
					.append(" ");
			allTaskInstanceSb.append("or (vi.NAME = :").append(VARIABLE_NAME)
					.append(" ");
			allTaskInstanceSb.append("and vi.VALUE = :").append(ACTOR_ID)
					.append(" ");
			allTaskInstanceSb.append("and vi.CATEGORY = :").append(CATEGORY)
					.append(") ");
			allTaskInstanceSb.append(ORDER_DESC);

			/* judge whether the user is rejected */
			isUserRejectedSb.append("select count(*) ");
			isUserRejectedSb
					.append("from JBPM_TASKINSTANCE ti inner join JBPM_GS_VARIABLE vi ");
			isUserRejectedSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			isUserRejectedSb.append("where vi.NAME = :").append(VARIABLE_NAME)
					.append(" ");
			isUserRejectedSb.append("and vi.VALUE = :").append(ACTOR_ID)
					.append(" ");
			isUserRejectedSb.append("and vi.CATEGORY = :").append(CATEGORY)
					.append(" ");
			isUserRejectedSb.append("and ti.id_ = :").append(TASK_ID).append(
					" ");
			
			isUserRejectedForReassignSb.append("select count(*) ");
			isUserRejectedForReassignSb.append("from JBPM_TASKINSTANCE ti join JBPM_TASKACTORPOOL tap ");
			isUserRejectedForReassignSb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
			isUserRejectedForReassignSb.append("inner join JBPM_POOLEDACTOR pa ");
			isUserRejectedForReassignSb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
			isUserRejectedForReassignSb.append("inner join JBPM_GS_VARIABLE vi ");
			isUserRejectedForReassignSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			isUserRejectedForReassignSb.append("and vi.NAME = :").append(VARIABLE_NAME).append(" ");
			isUserRejectedForReassignSb.append("and vi.VALUE = :").append(ACTOR_ID).append(" ");
			isUserRejectedForReassignSb.append("and vi.CATEGORY = :").append(CATEGORY).append(" ");
			isUserRejectedForReassignSb.append("where pa.ACTORID_ = :").append(ACTOR_ID).append(" ");
			isUserRejectedForReassignSb.append("and ti.id_ = :").append(TASK_ID).append(" ");

			/* judge whether all the user except the pm has rejected */
			isAllUserRejectedSb.append("select count(*) ");
			isAllUserRejectedSb
					.append("from JBPM_TASKINSTANCE ti inner join JBPM_GS_VARIABLE vi ");
			isAllUserRejectedSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			isAllUserRejectedSb.append("where vi.NAME = :").append(
					VARIABLE_NAME).append(" ");
			isAllUserRejectedSb.append("and vi.VALUE in ")
					.append(IN_EXPRESSION).append(" ");
			isAllUserRejectedSb.append("and vi.CATEGORY = :").append(CATEGORY)
					.append(" ");
			isAllUserRejectedSb.append("and ti.id_ = :").append(TASK_ID)
					.append(" ");

			/* Judges whether the availabe taskinstance exist */
			isAvailableExistPmSb.append("select distinct ti.* ");
			isAvailableExistPmSb.append("from JBPM_TASKINSTANCE ti ");
			isAvailableExistPmSb.append("inner join JBPM_TASKACTORPOOL tap ");
			isAvailableExistPmSb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
			isAvailableExistPmSb.append("inner join JBPM_POOLEDACTOR pa ");
			isAvailableExistPmSb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
			isAvailableExistPmSb.append("inner join JBPM_GS_VARIABLE vi  ");
			isAvailableExistPmSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			isAvailableExistPmSb.append("and vi.NAME = :")
					.append(VARIABLE_NAME).append(" ");
			isAvailableExistPmSb.append("and (vi.VALUE != :").append(ACTOR_ID)
					.append(" ");
			isAvailableExistPmSb.append("or vi.VALUE is null) ");
			isAvailableExistPmSb.append("and vi.CATEGORY = :").append(CATEGORY)
					.append(" ");
			isAvailableExistPmSb.append("where ( pa.ACTORID_ = :").append(
					ACTOR_ID).append(" ");
			isAvailableExistPmSb.append("or ti.DESCRIPTION_ = :").append(PM)
					.append(") ");
			isAvailableExistPmSb.append("and ti.START_ is null ");
			isAvailableExistPmSb.append(ORDER_DESC);

			/* judges whether the specified task is rejected */
			isTaskRejectedSb.append("select count(*) ");
			isTaskRejectedSb.append("from JBPM_TASKINSTANCE ti ");
			isTaskRejectedSb.append("inner join JBPM_TASKACTORPOOL tap ");
			isTaskRejectedSb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
			isTaskRejectedSb.append("inner join JBPM_POOLEDACTOR pa ");
			isTaskRejectedSb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
			isTaskRejectedSb.append("inner join JBPM_GS_VARIABLE vi  ");
			isTaskRejectedSb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
			isTaskRejectedSb.append("and vi.NAME = :").append(VARIABLE_NAME)
					.append(" ");
			isTaskRejectedSb.append("and vi.VALUE != pa.actorid_ ");
			isTaskRejectedSb.append("and vi.CATEGORY = :").append(CATEGORY)
					.append(" ");
			isTaskRejectedSb.append("and pa.ACTORID_ != :").append(PM).append(
					" ");
			isTaskRejectedSb.append("and ti.id_ = :").append(TASK_ID).append(
					" ");
            
            skippedTaskInstance.append("select t.NAME_ ");
            skippedTaskInstance.append("from JBPM_GS_VARIABLE vi ");
            skippedTaskInstance.append("inner join JBPM_TASKINSTANCE t ");
            skippedTaskInstance.append("on vi.TASKINSTANCE_ID = t.ID_ ");
            skippedTaskInstance.append("where vi.value = :").append(WORKFLOW_ID).append(" ");
            skippedTaskInstance.append("and vi.CATEGORY = '").append(VARIABLE_CATEGORY_SKIP).append("' ");

		}

		static String activeTask = activeTaskSb.toString();

		static String activeTaskPm = activeTaskPmSb.toString();

		static String rejectTask = rejectTaskSb.toString();

		static String rejectTaskPm = rejectTaskPmSb.toString();

		static String allTask = allTaskInstanceSb.toString();

		static String isUserReject = isUserRejectedSb.toString();
		
		static String isUserRejectForReassign = isUserRejectedForReassignSb.toString();

		static String allUserReject = isAllUserRejectedSb.toString();

		static String isTaskReject = isTaskRejectedSb.toString();
        
        static String skippedTask = skippedTaskInstance.toString();
	}
}
