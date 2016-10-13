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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.task.TaskDescriptorModifier;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * TaskPersistenceAccessor handles TopLink access operations for TaskManager.
 */
public class TaskPersistenceAccessor
{
    private static final Logger c_category = Logger
            .getLogger(TaskPersistenceAccessor.class.getName());

    public TaskPersistenceAccessor()
    {
    }

    /**
     * Update a task record in database task_info table.
     * 
     * @param p_task
     *            The Task to be updated.
     */
    public static void updateTask(Task p_task) throws TaskException
    {
        try
        {
            // PersistenceService.getInstance().updateObject(
            // (PersistentObject)p_task);
            HibernateUtil.update(p_task);
        }
        catch (Exception pe)
        {
            c_category
                    .error("updateTask " + p_task.toString() + " "
                            + pe.toString(), pe);
            throw new TaskException(TaskException.MSG_FAILED_TO_UPDATE_TASK,
                    null, pe);
        }
    }

    /**
     * To retrieve a task object from TopLink by a given id.
     * 
     * @param p_taskId
     *            the id of the target task.
     * @param p_editable
     *            Specifies whether the object returned should be editable
     *            (cloned) or not.
     * @return a Task object.
     */
    public static Task getTask(long p_taskId, boolean p_editable)
            throws TaskException
    {
        Task task = null;
        try
        {
            task = (Task) HibernateUtil.get(TaskImpl.class, p_taskId);
        }
        catch (Exception pe)
        {
            throw new TaskException(TaskException.MSG_FAILED_TO_GET_TASK, null,
                    pe);
        }
        return task;
    }

    public static Collection getTasks(TaskSearchParameters p_searchParameters)
            throws TaskException
    {
        java.util.Map temp = p_searchParameters.getParameters();
        if (temp != null)
        {
            int state = ((Integer) temp.get(new Integer(
                    TaskSearchParameters.STATE))).intValue();
            if (state == WorkflowConstants.TASK_ALL_STATES
                    || state == WorkflowConstants.TASK_DECLINED)
            {
                temp.remove(new Integer(TaskSearchParameters.STATE));
            }
        }
        try
        {
            return new TaskSearchCriteria().search(p_searchParameters);
        }
        catch (Exception e)
        {
            c_category.error("Failed to get activities by criteria.", e);
            throw new TaskException(e);
        }
    }

    /**
     * To retrieve a list of task objects from TopLink by matching the given
     * ids.
     * 
     * @param p_taskIds
     *            the ids of the target tasks.
     * @return a List of Task objects.
     */
    public static Collection getTasks(Vector p_taskIds) throws TaskException
    {
        // Vector queryArgs = new Vector(1);
        // queryArgs.add(p_taskIds);

        Collection tasks = null;
        try
        {
            String sql = tasksByIds(p_taskIds);
            // ReadAllQuery query = createQuery(sql);
            // tasks = PersistenceService.getInstance().executeQuery(query,
            // false);
            tasks = HibernateUtil.searchWithSql(TaskImpl.class, sql);
        }
        catch (Exception pe)
        {
            c_category.error(
                    "getTasks " + p_taskIds.toString() + " " + pe.toString(),
                    pe);
            throw new TaskException(TaskException.MSG_FAILED_TO_GET_TASK, null,
                    pe);
        }
        return tasks;
    }

    /**
     * Retrieves all the tasks in a job that are associated with a particular
     * rate type (like HOURLY, PAGE_COUNT, etc..)
     */
    public static Collection getTasks(long p_jobId, Integer p_rateType,
            Integer p_costType) throws TaskException
    {
        Collection tasks = null;
        try
        {
            // PersistenceService ps = PersistenceService.getInstance();

            // find all tasks with rates of the particular unit of work
            // convert to the correct field in the DB
            // TOPLINK doesn't convert these correctly on a query
            // so must be done before.
            Vector args = new Vector();
            String rate_type = null;
            if (p_rateType.equals(Rate.UnitOfWork.HOURLY))
            {
                // args.add(new String("H"));
                rate_type = "H";
            }
            else if (p_rateType.equals(Rate.UnitOfWork.PAGE_COUNT))
            {
                // args.add(new String("P"));
                rate_type = "P";
            }
            else if (p_rateType.equals(Rate.UnitOfWork.WORD_COUNT))
            {
                // args.add(new String("W"));
                rate_type = "W";
            }
            else
            // assuming it is fixed
            {
                // args.add(new String("F"));
                rate_type = "F";
            }
            args.add(new Long(p_jobId));
            if (p_costType.intValue() == Cost.EXPENSE)
            {
                // tasks =
                // ps.executeNamedQuery(TaskQueryNames.TASKS_IN_JOB_BY_RATE_TYPE,
                // args, false);
                String hql = "from TaskImpl t where t.expenseRate.type=:rate_type and t.workflow.job.id=:job_id";
                HashMap map = new HashMap();
                map.put("rate_type", rate_type);
                map.put("job_id", new Long(p_jobId));
                tasks = HibernateUtil.search(hql, map);
            }
            else
            {
                // tasks =
                // ps.executeNamedQuery(TaskQueryNames.TASKS_IN_JOB_BY_RATE_TYPE_FOR_REVENUE,
                // args, false);
                String hql = "from TaskImpl t where t.revenueRate.type=:rate_type and t.workflow.job.id=:job_id";
                HashMap map = new HashMap();
                map.put("rate_type", rate_type);
                map.put("job_id", new Long(p_jobId));
                tasks = HibernateUtil.search(hql, map);
            }
        }
        catch (Exception pe)
        {
            c_category.error("Failed to get tasks of job " + p_jobId
                    + " with rate type " + p_rateType);
            String args[] =
            { Long.toString(p_jobId), p_rateType.toString() };
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_TASKS_OF_JOB_BY_RATE_TYPE,
                    args, pe);
        }
        return tasks;
    }

    /**
     * Retrieves WorkflowId and completion time based on jobid
     */
    public static Collection getFirstCompletedActivityTime(long p_jobId)
            throws TaskException
    {
        Collection tasks = new ArrayList();
        try
        {
            String sql = TaskDescriptorModifier.ACTIVITY_COMPLETION_TIME_SQL;
            Map map = new HashMap();
            map.put("jobId", new Long(p_jobId));
            List obs = HibernateUtil.searchWithSql(sql, map);
            for (int i = 0; i < obs.size(); i++)
            {
                Object[] objects = (Object[]) obs.get(i);

                TaskImpl task = new TaskImpl();
                WorkflowImpl w = new WorkflowImpl();
                Long wId = (Long) objects[0];
                w.setId(wId.longValue());
                task.setCompletedDate((Date) objects[1]);
                tasks.add(task);
            }
        }
        catch (PersistenceException pe)
        {
            c_category.error("Failed to get completions time " + p_jobId);
            String args[] =
            { Long.toString(p_jobId) };
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_TASK_COMPLETION_TIME, args,
                    pe);
        }
        return tasks;
    }

    /**
     * Return all the completed tasks in the workflow.
     */
    public static Collection getCompletedTasks(long p_workflowId)
            throws TaskException
    {
        Collection tasks = null;
        try
        {
            // PersistenceService ps = PersistenceService.getInstance();
            // Vector args = new Vector();
            // args.add(new Long(p_workflowId));
            // tasks =ps.executeNamedQuery(TaskQueryNames.COMPLETED_TASKS,
            // args, false);
            String sql = TaskDescriptorModifier.COMPLETED_TASKS_IN_WORKFLOW_SQL;
            HashMap map = new HashMap();
            map.put(TaskDescriptorModifier.WORKFLOW_ID_ARG, new Long(
                    p_workflowId));
            tasks = HibernateUtil.searchWithSql(sql, map, TaskImpl.class);
        }
        catch (PersistenceException pe)
        {
            c_category
                    .error("Failed to get all the completed tasks in workflow "
                            + p_workflowId);
            String args[] =
            { Long.toString(p_workflowId) };
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_COMPLETED_TASKS, args, pe);
        }
        return tasks;
    }

    // ------------------private methods-------------------------------------

    // private static ReadAllQuery createQuery(String p_sql)
    // {
    // ReadAllQuery query = new ReadAllQuery(TaskImpl.class);
    // query.setSQLString(p_sql);
    // query.bindAllParameters();
    // return query;
    // }

    private static String tasksByIds(Vector p_taskIds)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM TASK_INFO ");
        sb.append(" WHERE ");
        addTaskIds(sb, p_taskIds);
        return sb.toString();
    }

    private static void addTaskIds(StringBuffer p_sb, Vector p_ids)
    {
        p_sb.append("TASK_ID in (");
        for (int i = 0; i < p_ids.size(); i++)
        {
            p_sb.append(p_ids.elementAt(i));
            if (i < p_ids.size() - 1)
            {
                p_sb.append(", ");
            }
        }
        p_sb.append(")");

        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
            p_sb.append(" and COMPANY_ID = ");
            p_sb.append(Long.parseLong(companyId));
        }
    }
}
