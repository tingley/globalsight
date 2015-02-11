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

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.node.Decision;
import org.jbpm.graph.node.EndState;
import org.jbpm.graph.node.StartState;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.instantiation.Delegation;
import org.jbpm.taskmgmt.exe.PooledActor;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * <code>WorkflowJbpmUtil</code> provides the basic method for Jbpm conversion
 * in work flow process. <br>
 * 
 * @see com.globalsight.everest.workflow.WorkflowConstants
 * @version 1.2
 */
public class WorkflowJbpmUtil
{
    private static final Logger s_logger = Logger
            .getLogger(WorkflowJbpmUtil.class.getName());

    private static final int ERROR_CODE = -1;

    private static final String TASK_NODE_REG = "node_(\\d{1,})_";

    private static final String TASK_REG = "task_";

    public static int getNodeIndex(String name)
    {
        Pattern p = Pattern.compile(TASK_NODE_REG);
        Matcher m = p.matcher(name);
        if (m.find())
        {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    /**
     * Gets the type of the node. <br>
     * The type includes:
     * <ul>
     * <li>{@code com.globalsight.everest.workflow.WorkflowConstants#START}</li>
     * <li>{@code com.globalsight.everest.workflow.WorkflowConstants#ACTIVITY}</li>
     * <li>{@code com.globalsight.everest.workflow.WorkflowConstants#STOP}</li>
     * <li>{@code com.globalsight.everest.workflow.WorkflowConstants#CONDITION}</li>
     * <li>{@code com.globalsight.everest.workflow.WorkflowConstants#AND}</li>
     * <li>{@code com.globalsight.everest.workflow.WorkflowConstants#OR}</li>
     * </ul>
     * Note: the type of <b>and</b> and <b>or</b> have not been implemented.
     * 
     * 
     * @param node
     *            The jbpm node.
     * @return the type of the node.
     */
    public static int getNodeType(Node node)
    {

        if (node == null)
        {
            throw new IllegalArgumentException("The node should not be null");
        }

        /* convert */
        node = (Node) getImplementation(node);

        if (node instanceof StartState)
        {
            /* for the start state node */
            return WorkflowConstants.START;
        }
        else if (node instanceof TaskNode)
        {
            /* for the task node */
            return WorkflowConstants.ACTIVITY;
        }
        else if (node instanceof EndState)
        {
            /* for the endstate node */
            return WorkflowConstants.STOP;
        }
        else if (node instanceof Decision)
        {
            /* for the decision node */
            return WorkflowConstants.CONDITION;
        }
        else
        {
            s_logger.error("There is no node match with the legal node ");
            s_logger.error("The node is " + node);
            return ERROR_CODE;
        }
    }

    /**
     * Judges whether the node is condition node.
     * 
     * @param node
     *            {@code Node} the target node.
     * @return <code>true</code> when the node is condition node.
     */
    public static boolean isConditionNode(Node node)
    {
        return getNodeType(node) == WorkflowConstants.CONDITION;
    }

    /**
     * Judges whether the node is the stop node.
     * 
     * @param node
     *            {@code Node} the target node.
     * @return <code>true</code> when the node is condition node.
     */
    public static boolean isExitNode(Node node)
    {
        return getNodeType(node) == WorkflowConstants.STOP;
    }

    /**
     * Judges whether the node is activity.
     * 
     * @param node
     *            {@code Node} the target node.s
     * @return <code>true</code> when the node is condition node.
     */
    public static boolean isActivityNode(Node node)
    {
        return getNodeType(node) == WorkflowConstants.ACTIVITY;
    }

    /**
     * Gets the configuration value of the different node.
     * 
     * @param node
     *            The node.
     * @return The configuration value.
     */
    public static String getConfigure(Node node)
    {

        Delegation delegation = getDelegation(node);

        if (getImplementation(node) instanceof StartState)
        {
            return null;
        }

        if (delegation == null)
        {
            throw new IllegalArgumentException("The node has no delegation ");
        }

        return delegation.getConfiguration();

    }

    /**
     * Gets the configuration value of the different node.
     * 
     * @param node
     *            The node.
     * @return The configuration value.
     */
    public static void setConfigure(Node node, String value)
    {
        Delegation delegation = getDelegation(node);

        if (delegation == null)
        {
            throw new IllegalArgumentException("The node has no delegation ");
        }

        delegation.setConfiguration(value);
    }

    /**
     * Gets the task name by the node name. <br>
     * The fromat of the node name is like:
     * 
     * <pre>
     * 	node_[index]_activityname
     * </pre>
     * 
     * And the task name is like :
     * 
     * <pre>
     * task_activityname
     * </pre>
     * 
     * @param node
     *            The node.
     * @return the task name.
     */
    public static String getTaskName(Node node)
    {
        StringBuilder sb = new StringBuilder(WorkflowConstants.NAME_TASK)
                .append(WorkflowConstants.NAME_SEPARATOR);
        sb.append(getActivityName(node));

        return sb.toString();
    }

    /**
     * Gets the activityName of the node.
     * 
     * @param node
     *            The
     * @{code Node}
     * @return The name of the activity.
     */
    public static String getActivityName(Node node)
    {
        if (node == null)
        {
            throw new IllegalArgumentException("The node cannot be null ");
        }
        return getTaskName(node.getName());
    }

    /**
     * Gets the activity name of the node with arrow name, using for Email.
     * 
     * @param p_node
     * @param p_suffix
     *            suffix string, which will split from activity name
     * @param p_pi
     *            process Instance
     * @param p_type
     *            task/activity type(new/accept/complete)
     * @return ativityName(arrowName)
     */
    public static String getActivityNameWithArrowName(Node p_node,
            String p_suffix, ProcessInstance p_pi, String p_type)
    {
        if (p_node == null)
        {
            throw new IllegalArgumentException("The node cannot be null ");
        }

        String result = getTaskName(p_node.getName());
        result = StringUtil.delSuffix(result, p_suffix);
        Set<Transition> trans = p_node.getArrivingTransitions();
        if (trans == null)
        {
            return result;
        }
        Transition arrTrans = null;
        if (trans.size() == 1)
        {
            arrTrans = (Transition) trans.iterator().next();
        }
        else
        {
            TaskInstance ti = getLastTaskInstance(p_pi, p_type);
            if (ti == null)
            {
                return result;
            }

            String tiName = getActivityName(ti.getName());
            for (Transition tTran : trans)
            {
                Node tNode = tTran.getFrom();

                if (isActivityNode(tNode))
                {
                    if (tiName.equals(getTaskName(tNode.getName())))
                    {
                        arrTrans = tTran;
                        break;
                    }
                }
                else if (isConditionNode(tNode))
                {
                    Node node1 = null;
                    do
                    {
                        node1 = ((Transition) tNode.getArrivingTransitions()
                                .iterator().next()).getFrom();
                    } while (!isActivityNode(node1));

                    if (tiName.equals(getTaskName(node1.getName())))
                    {
                        arrTrans = tTran;
                        break;
                    }
                }
                else
                {
                    arrTrans = tTran;
                }
            }
        }

        if (arrTrans != null)
        {
            result = result + "(" + arrTrans.getName() + ")";
        }
        return result.trim();
    }

    /**
     * Get the last TaskInstance.
     * 
     * @param p_pi
     * @param p_type
     *            task/activity type(new/accept/complete)
     * @return
     */
    public static TaskInstance getLastTaskInstance(ProcessInstance p_pi,
            String p_type)
    {
        Collection<TaskInstance> tiSet = p_pi.getTaskMgmtInstance()
                .getTaskInstances();
        int len = tiSet.size();
        if (len == 1
                || (WorkflowConstants.TASK_TYPE_COM.equals(p_type) && len == 2))
        {
            return new TaskInstance("Start");
        }

        List<TaskInstance> tiList = new ArrayList<TaskInstance>(tiSet);
        SortUtil.sort(tiList, new java.util.Comparator<TaskInstance>()
        {
            /**
             * Sort the object into descending order.
             */
            @Override
            public int compare(TaskInstance ti1, TaskInstance ti2)
            {
                Date tiDate = ti1.getCreate();
                if (tiDate == null || tiDate.before(ti2.getCreate()))
                {
                    return 1;
                }
                else if (tiDate.after(ti2.getCreate()))
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        });

        if (WorkflowConstants.TASK_TYPE_COM.equals(p_type) && len > 2)
        {
            return tiList.get(2);
        }
        else
        {
            return tiList.get(1);
        }
    }

    /**
     * Gets the task name by the node name.
     * 
     * @param nodeName
     * @return
     */
    public static String getTaskName(String nodeName)
    {
        return nodeName.replaceFirst(TASK_NODE_REG, StringUtil.EMPTY_STRING);
    }

    /**
     * Gets the task name by the taskName.
     * 
     * @param p_nodeName
     * @return
     */
    public static String getActivityName(String p_nodeName)
    {
        return p_nodeName.replaceFirst(TASK_REG, StringUtil.EMPTY_STRING);
    }

    /**
     * Gets the {@code WorkflowTaskInstance} by the taskinstance 's id.
     * 
     * @param p_wfTaskInstances
     *            The list of the {@code WorkflowTaskInstance}.
     * @param id
     *            The id of the instance.
     * @return The {@code WorkflowTaskInstance}
     */
    public static WorkflowTaskInstance getTaskInstanceById(
            List p_wfTaskInstances, long id)
    {
        Object o = getTaskById(p_wfTaskInstances, id);
        return getTaskById(p_wfTaskInstances, id) == null ? null
                : (WorkflowTaskInstance) o;
    }

    /**
     * Gets the {@code WorkflowTask} by the task 's id.
     * 
     * @param p_wfTask
     *            The list of the {@code WorkflowTask}.
     * @param id
     *            The id of the task.
     * @return The {@code WorkflowTask}
     */
    public static WorkflowTask getTaskById(List p_wfTask, long id)
    {
        Iterator it = p_wfTask.iterator();

        while (it.hasNext())
        {
            WorkflowTask task = (WorkflowTask) it.next();
            if (task.getTaskId() == id)
            {
                return task;
            }
        }

        return null;
    }

    /**
     * Gets the processdefinition by the id of the template.
     * 
     * @param p_id
     *            The id of the template.
     * @return {@code ProcessDefinition}
     */
    public static ProcessDefinition getProcessDefinition(long p_id)
    {
        JbpmContext context = null;
        ProcessDefinition pd = null;
        try
        {
            context = WorkflowConfiguration.getInstance().getJbpmContext();
            pd = context.getGraphSession().getProcessDefinition(p_id);
        }
        finally
        {
            context.close();
        }

        return pd;
    }

    /**
     * Gets the real object extends by <code>cglib</code>
     * 
     * @param obj
     *            The extends object.
     * @return The real obj.
     */
    public static Object getImplementation(Object obj)
    {
        if (obj instanceof HibernateProxy)
        {
            return ((HibernateProxy) obj).getHibernateLazyInitializer()
                    .getImplementation();
        }
        else
        {
            return obj;
        }

    }

    /**
     * Gets the private filed by reflection.
     * 
     * @param obj
     *            The obj you want to retrieve.
     * @param fieldName
     *            The field name.
     * @return The field value.
     */
    public static Object getPrivateValue(Object obj, String fieldName)
    {
        try
        {
            Field field = setFieldAssisable(obj, fieldName);
            return field.get(obj);
        }
        catch (SecurityException e)
        {
            s_logger.error("The Security check is on, you cannot set the assessible ");
            s_logger.error("error in security check ", e);
        }
        catch (IllegalArgumentException e)
        {
            s_logger.error("The argument is error");
            s_logger.error("the obj is " + obj);
            s_logger.error("error", e);
        }
        catch (IllegalAccessException e)
        {
            s_logger.error("You cannot get the private field");
            s_logger.error("error", e);
        }

        /* return null if error occured */
        return null;
    }

    /**
     * Sets the private filed by reflection.
     * 
     * @param obj
     *            The obj you want to retrieve.
     * @param fieldName
     *            The field name.
     * @return The field value.
     */
    public static void setPrivateValue(Object obj, String fieldName,
            Object value)
    {
        try
        {
            Field field = setFieldAssisable(obj, fieldName);
            field.set(obj, value);
        }
        catch (SecurityException e)
        {
            s_logger.error("The Security check is on, you cannot set the assessible ");
            s_logger.error("error in security check ", e);
        }
        catch (IllegalArgumentException e)
        {
            s_logger.error("The argument is error");
            s_logger.error("the obj is " + obj);
            s_logger.error("error", e);
        }
        catch (IllegalAccessException e)
        {
            s_logger.error("You cannot get the private field");
            s_logger.error("error", e);
        }

    }

    /**
     * Sets the private/protected field acceessible.
     * 
     * @param obj
     * @param fieldName
     * @return
     */
    public static Field setFieldAssisable(Object obj, String fieldName)
    {
        Field field = null;
        try
        {
            field = obj.getClass().getDeclaredField(fieldName);
        }
        catch (SecurityException e)
        {
            s_logger.error("The Security check is on, you cannot set the assessible ");
            s_logger.error("error in security check ", e);
        }
        catch (NoSuchFieldException e)
        {
            s_logger.error("There is no field named " + fieldName
                    + "for the class " + obj.getClass());
            s_logger.error("error in no field", e);
        }
        field.setAccessible(true);
        return field;
    }

    /**
     * Converts the tasks to the
     * 
     * @{code WorkflowInstance} array.
     * 
     * @param tasks
     *            The vector of the {@code WorkflowInstance}
     * @return
     */
    public static WorkflowTaskInstance[] convertToArray(Vector tasks)
    {
        WorkflowTaskInstance[] taskInstances = new WorkflowTaskInstance[tasks
                .size()];

        int i = 0;
        for (Enumeration e = tasks.elements(); e.hasMoreElements();)
        {
            taskInstances[i++] = (WorkflowTaskInstance) e.nextElement();
        }

        return taskInstances;
    }

    /**
     * Gets the <code>Node</code> by the <code>WorkflowTask</code> and the
     * <code>ProcessInstance</code>.
     * 
     * @param p_pi
     *            The processinstance.
     * @param p_task
     *            The workflowtask.
     * @return The node.
     */
    public static Node getNodeByWfTask(ProcessInstance p_pi, WorkflowTask p_task)
    {
        return getNodeByWfTask(p_pi.getProcessDefinition(), p_task);
    }

    /**
     * Gets the <code>Node</code> by the <code>WorkflowTask</code> and the
     * <code>ProcessDefinition</code>.
     * 
     * @param p_processDefinition
     *            The {@code ProcessDefinition}
     * @param p_task
     *            The {@WorkflowTask}
     * @return The node.
     */
    public static Node getNodeByWfTask(ProcessDefinition p_processDefinition,
            WorkflowTask p_task)
    {
        if (p_task.getType() == WorkflowConstants.STOP)
        {
            return p_processDefinition.getNode(WorkflowConstants.END_NODE);
        }

        return p_processDefinition.getNode(p_task.getNodeName());
    }

    /**
     * Gets the <code>Node</code> by the given <code>ProcessDefinition</code>
     * and the id of the <code>ProcessDefinition</code>.
     * 
     * @param processDefinition
     *            {@code ProcessDefinition}
     * @param p_id
     *            The id of the processDefinition.
     * @return The node.
     */
    public static Node getNodeById(ProcessDefinition processDefinition,
            long p_id)
    {

        List nodes = processDefinition.getNodes();

        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            Node node = (Node) it.next();
            if (node.getId() == p_id)
            {
                return node;
            }
        }

        /* No id found, should never be here */
        s_logger.error("There is no node found from the processdefinition, the id of the node is "
                + p_id);
        return null;

    }

    /**
     * Gets the node by the node name.
     * 
     * @param processDefinition
     *            The process Definition.
     * @param name
     *            the name of the node.
     * @return The node.
     */
    public static Node getNodeByNodeName(ProcessDefinition processDefinition,
            String name)
    {
        Map map = processDefinition.getNodesMap();
        return (Node) map.get(name);
    }

    /**
     * Gets the end node.
     * 
     * @param p_processDefinition
     *            {@code ProessDefinition}
     * @return The end node.
     */
    public static Node getExitNode(ProcessDefinition p_processDefinition)
    {
        return p_processDefinition.getNode(WorkflowConstants.END_NODE);
    }

    /**
     * Gets the taskinstance by the given <code>Node</code>
     * 
     * @param p_processInstance
     *            {@code TaskInstance}
     * @param p_node
     *            {@code Node}
     * @return {@TaskInstance}
     */
    public static TaskInstance getTaskInstanceByNode(
            ProcessInstance p_processInstance, Node p_node)
    {
        return getTaskInstanceByNode(p_processInstance, p_node.getName());
    }

    /**
     * Gets the task instance by node
     * 
     * @param p_processInstance
     *            {@code ProcessInstance}
     * @param p_node
     *            {@Node}
     * @param p_flag
     *            the flag indicates whether get the unstarted task.
     * @return The taskInstance.
     */
    public static TaskInstance getTaskInstanceByNode(
            ProcessInstance p_processInstance, Node p_node, boolean p_flag)
    {
        return getTaskInstanceByNode(p_processInstance, p_node.getName(),
                p_flag);
    }

    /**
     * Gets the taskinstance by the given <code>Node</code>
     * 
     * @param p_processInstance
     *            {@code TaskInstance}
     * @param p_node
     *            the name of the {@code Node}
     * @return {@TaskInstance}
     */
    public static TaskInstance getTaskInstanceByNode(
            ProcessInstance p_processInstance, String p_node)
    {
        return getTaskInstanceByNode(p_processInstance, p_node, false);
    }

    /**
     * Gets the taskinstance by the given <code>Node</code>
     * 
     * @param p_processInstance
     *            {@code TaskInstance}
     * @param p_node
     *            the name of the {@code Node}
     * @param p_flag
     *            true when only want to get the not ended taksinstance. false
     *            gets all the taskinstance.
     * @return {@TaskInstance}
     */
    public static TaskInstance getTaskInstanceByNode(
            ProcessInstance p_processInstance, String p_node, boolean p_flag)
    {
        Collection tasks = p_processInstance.getTaskMgmtInstance()
                .getTaskInstances();

        for (Iterator it = tasks.iterator(); it.hasNext();)
        {
            TaskInstance taskInstance = (TaskInstance) it.next();
            if (taskInstance.getTask().getTaskNode().getName().equals(p_node))
            {
                if (p_flag && taskInstance.hasEnded())
                {
                    /* only get the taskintance which has not ended. */
                    /* special for the loop situtation. */
                    continue;
                }
                return taskInstance;
            }
        }
        return null;
    }

    /**
     * Gets the <code>WorkflowTaskInstance</code> by the node. <br>
     * This method will only get the
     * 
     * @param node
     *            The {@code Node}.
     * @return the workflowtaskinstance.
     */
    public static WorkflowTaskInstance getCompactWfTaskInstance(Node p_node)
    {
        WorkflowTaskInstance wfTaskInstance = new WorkflowTaskInstance(
                getActivityName(p_node), WorkflowConstants.ACTIVITY);
        wfTaskInstance.setNodeName(p_node.getName());
        wfTaskInstance.setTaskId(p_node.getId());
        return wfTaskInstance;
    }

    /**
     * Gets the startNode from the <code>Vector</code> of the object
     * <code>WorkflowTaskInstance</code>
     * 
     * @param tasks
     *            The {@code WorkflowTaskInstance} vector.
     * @return
     */
    public static WorkflowTaskInstance getStartNode(Vector tasks)
    {
        for (Enumeration e = tasks.elements(); e.hasMoreElements();)
        {
            WorkflowTaskInstance task = (WorkflowTaskInstance) e.nextElement();
            if (task.getType() == WorkflowConstants.START)
            {
                return task;
            }
        }

        throw new IllegalArgumentException("The vector contains no start node ");
    }

    public static WorkflowTaskInstance getTaskInstanceByActivity(Vector tasks,
            String activity)
    {
        for (Enumeration e = tasks.elements(); e.hasMoreElements();)
        {
            WorkflowTaskInstance task = (WorkflowTaskInstance) e.nextElement();
            if (task.getActivityName().equals(activity))
            {
                return task;
            }
        }

        throw new IllegalArgumentException(
                "The vector contains no task with the activity name "
                        + activity);
    }

    /**
     * Gets the <code>TaskInstance</code> by the name of the task.
     * 
     * @param tasks
     *            The vectors of the {@code WorkflowTaskInstance}
     * @param name
     *            The name of the node.
     * @return The {@WorkflowTaskInstance}
     */
    public static WorkflowTaskInstance getCurrentTaskInstance(Vector tasks,
            String name)
    {

        for (Enumeration e = tasks.elements(); e.hasMoreElements();)
        {
            WorkflowTaskInstance task = (WorkflowTaskInstance) e.nextElement();
            if (task.getNodeName().equals(name))
            {
                return task;
            }
        }

        throw new IllegalArgumentException("The vector contains no start node ");

    }

    // /////////////////////////////////////////////////////////
    // Private Static method
    // /////////////////////////////////////////////////////////
    /**
     * Gets the delegation of the node.
     * 
     * @param node
     *            The node.
     * @return The delegation.
     */
    private static Delegation getDelegation(Node node)
    {

        Node no = (Node) getImplementation(node);

        Delegation delegation = null;

        if (no instanceof StartState)
        {
            /* for start state, there is no configure */
        }
        else if (no instanceof EndState)
        {
            /* use the ActionDelegation */
            delegation = node.getAction().getActionDelegation();

        }
        else if (no instanceof TaskNode)
        {
            /* use the AssignmentDelegation */
            delegation = ((TaskNode) no).getTask(getTaskName(node))
                    .getAssignmentDelegation();
        }
        else if (no instanceof Decision)
        {
            /*
             * use the action delegation temporary, handlerDelegation doesn't
             * support
             */
            delegation = (Delegation) getPrivateValue(((Decision) no),
                    "decisionDelegation");

        }

        return delegation;
    }

    public static List<Long> getRejectedTaskIds(List<TaskInstance> tasks,
            String p_userId)
    {
        JbpmContext ctx = WorkflowConfiguration.getInstance()
                .getCurrentContext();

        List<Long> allIds = new ArrayList<Long>();
        if (tasks == null || tasks.size() == 0)
        {
            return allIds;
        }

        for (TaskInstance task : tasks)
        {
            allIds.add(task.getId());
        }

        String idsString = allIds.toString();
        idsString = idsString.substring(1, idsString.length() - 1);

        StringBuffer sql = new StringBuffer("select vi.TASKINSTANCE_ID ");
        sql.append("from JBPM_GS_VARIABLE vi ");
        sql.append("where vi.NAME = :name and vi.VALUE = :value ");
        sql.append("and vi.CATEGORY = 'reject' and vi.TASKINSTANCE_ID in (:ids);");

        SQLQuery query = null;
        Session session = ctx.getSession();
        query = session.createSQLQuery(sql.toString());
        query.setString("name", WorkflowConstants.VARIABLE_IS_REJECTED);
        query.setString("value", p_userId);
        query.setParameterList("ids", allIds);

        List<Long> ids = new ArrayList<Long>();
        for (Object id : query.list())
        {
            // id is BigInteger
            ids.add(Long.parseLong(id.toString()));
        }
        return ids;
    }

    public static int getStateFromTaskInstance(TaskInstance p_ti,
            String p_userId, int p_taskState)
    {
        return getStateFromTaskInstance(p_ti, p_userId, p_taskState, null);
    }

    private static boolean isUserRejected(JbpmContext ctx, String userId,
            TaskInstance ti, List<Long> rejectTaskIds)
    {
        if (rejectTaskIds == null)
            return WorkflowJbpmPersistenceHandler.isUserRejected(ctx, userId,
                    ti);

        return rejectTaskIds.indexOf(ti.getId()) > -1;
    }

    /**
     * Gets the state from task instance.
     * 
     * @param p_ti
     *            the task instance.
     * @param p_userId
     *            the user id.
     * @param p_taskState
     *            the task state being searched.
     * 
     * @return the state.
     */
    public static int getStateFromTaskInstance(TaskInstance p_ti,
            String p_userId, int p_taskState, List<Long> rejectTaskIds)
    {

        JbpmContext ctx = WorkflowConfiguration.getInstance()
                .getCurrentContext();

        int state = WorkflowConstants.TASK_DECLINED;

        String pm = p_ti.getDescription();

        if (WorkflowConstants.TASK_ALL_STATES == p_taskState
                && p_userId.equals(pm))
        {
            /*
             * for the all status task, we need to judge the task status
             * paricular
             */
            if (WorkflowJbpmPersistenceHandler.isTaskRejected(ctx, p_ti,
                    p_userId))
            {
                return state;
            }
        }
        /* judge whether the user has reject the task */

        // for the pm, he can see the rejected activities if he likes.
        // || for the user who rejected the activity, he can see it as well.
        if (pm != null && p_userId.equals(pm)
                && p_taskState == WorkflowConstants.TASK_DECLINED)
        {
            return state;
        }
        else
        {
            /* for the non pm user */
            boolean isRejected = isUserRejected(ctx, p_userId, p_ti,
                    rejectTaskIds);

            if (isRejected)
            {
                // fix for GBS-1470
                boolean isRejectedForReassign = WorkflowJbpmPersistenceHandler
                        .isUserRejectedForReassign(ctx, p_userId, p_ti);

                if (!isRejectedForReassign)
                {
                    return state;
                }
            }
        }

        if (p_ti.getStart() == null)
        {
            state = WorkflowConstants.TASK_ACTIVE;
        }
        else if (p_ti.getStart() != null && !p_ti.hasEnded())
        {
            state = WorkflowConstants.TASK_ACCEPTED;
        }
        else if (p_ti.hasEnded())
        {
            state = WorkflowConstants.TASK_COMPLETED;
        }
        return state;
    }

    /**
     * Updates the assignees in the task instance when it is rejected.
     * 
     * @param p_ti
     *            the task instance.
     * @param p_param
     *            the WorkflowNodeParameter of the task node.
     * @param p_userId
     *            the user who rejected the task instance.
     * 
     */
    public static void updateAssignees(TaskInstance p_ti, String p_userId,
            WorkflowNodeParameter p_param)
    {
        String[] pm =
        { p_param.getAttribute(WorkflowConstants.FIELD_PM) };
        String actorId = p_ti.getActorId();
        if (actorId != null && p_userId.equals(actorId))
        {
            // set the actor id to null if the user accepted and rejected this
            // task instance.
            p_ti.setActorId(null);
        }
        Set pooledActors = p_ti.getPooledActors();
        // add the project manager to the assignees if the task was rejected
        // and there is no other assignee
        // so that the owners can accept it.
        if (pooledActors != null && !pooledActors.isEmpty())
        {
            Set actorIds = PooledActor.extractActorIds(pooledActors);
            if (actorIds != null && actorIds.contains(p_userId))
            {
                actorIds.remove(p_userId);
                p_ti.setPooledActors(toStringArray(actorIds));
                if (actorIds.isEmpty())
                {
                    p_ti.setPooledActors(pm);
                }
            }
        }
        // for GBS-1302, reject interim activity
        TaskInterimPersistenceAccessor.rejectInterimActivity(p_ti);
    }

    /**
     * Converts the object array to a String array.
     * 
     * @param p_actorIds
     *            the set of the pooled actor ids.
     * 
     * @return the String array.
     */
    public static String[] toStringArray(Set p_actorIds)
    {
        Object[] actors = p_actorIds.toArray();
        String[] strActors = new String[actors.length];
        for (int i = 0; i < actors.length; i++)
        {
            strActors[i] = actors[i].toString();
        }
        return strActors;
    }

    /**
     * Gets assignees and pm of a task.
     * 
     * @param taskId
     *            the id of the task.
     * @return assignees.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getAssignees(long taskId)
    {
        List<String> assignees = new ArrayList<String>();
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        try
        {
            TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                    .getTaskInstance(taskId, ctx);

            // Adds pm
            if (taskInstance.getDescription() != null)
            {
                assignees.add(taskInstance.getDescription());
            }

            if (taskInstance.getActorId() != null)
            {
                assignees.add(taskInstance.getActorId());
            }
            else
            {
                Set pooledActors = taskInstance.getPooledActors();
                Set actorIds = PooledActor.extractActorIds(pooledActors);
                assignees.addAll(actorIds);
            }
        }
        finally
        {
            ctx.close();
        }

        return assignees;
    }

    /**
     * Gets the assignees in the task instance.
     * 
     * @param p_ti
     *            the task instance.
     * @param p_userId
     *            the user id.
     * 
     * @return the String of the assignees.
     */
    public static String getAssignees(TaskInstance p_ti, String p_userId)
    {
        StringBuilder assignees = new StringBuilder();
        String actorId = p_ti.getActorId();
        if (actorId != null)
        {
            assignees.append(actorId);
        }
        else
        {
            Set pooledActors = p_ti.getPooledActors();
            if (pooledActors != null && !pooledActors.isEmpty())
            {
                Set actorIds = PooledActor.extractActorIds(pooledActors);
                Object[] ais = actorIds.toArray();
                for (int i = 0; i < ais.length; i++)
                {
                    assignees.append((String) ais[i]);
                    if (i != ais.length - 1)
                    {
                        assignees.append(",");
                    }
                }
            }
        }
        return assignees.toString();
    }

    /**
     * Gets the state of the <code>TaskInstance</code> by the given node. <br>
     * Only three state will be retrevied from this method :
     * <ul>
     * <li> {@code WorkflowConstants#STATE_INITIAL}</li>
     * <li> {@code WorkflowConstants#STATE_RUNNING}</li>
     * <li> {@code WorkflowConstants#STATE_COMPLETED}</li>
     * </ul>
     * 
     * @param p_processInstance
     * @param p_node
     * @return
     */
    public static int getTaskState(ProcessInstance p_processInstance,
            Node p_node)
    {
        TaskInstance taskInstance = getTaskInstanceByNode(p_processInstance,
                p_node);

        if (taskInstance == null)
        {
            /*
             * When the taskInstance is null, it means has not reached this
             * point
             */
            return WorkflowConstants.STATE_INITIAL;
        }
        else if (taskInstance.hasEnded())
        {
            /* The task has bean completed */
            return WorkflowConstants.STATE_COMPLETED;
        }
        else
        {
            /* The task is still running */
            return WorkflowConstants.STATE_RUNNING;
        }
    }

    /**
     * Gets the element name of the arrow in the process definition file. The
     * name will be leaving/arrving + <i>arrowName</i>. <br>
     * Note: the arrowName here has stripped the white space.
     * 
     * @param prefix
     *            The prefix of the arrow : leaving/arriving
     * @param name
     *            The name of the arrow.
     */
    public static String getArrowName(String prefix, String name)
    {
        return prefix + WorkflowConstants.NAME_SEPARATOR
                + name.replaceAll("\\s", StringUtil.EMPTY_STRING);
    }

    /**
     * Generate the node name by the activity name and the index.
     * 
     * @param name
     *            The activity name.
     * @param index
     *            The index of the node.
     * @return the node name.
     */
    public static String generateNodeName(String name, int index)
    {
        return WorkflowConstants.NAME_NODE + WorkflowConstants.NAME_SEPARATOR
                + index + WorkflowConstants.NAME_SEPARATOR + name;
    }

    /**
     * Generates the task name by the activity name and the index.
     * 
     * @param name
     *            The name of the task.
     * @return The task name in jbpm process definition file.
     */
    public static String generateTaskName(String name)
    {
        return WorkflowConstants.NAME_TASK + WorkflowConstants.NAME_SEPARATOR
                + name;
    }

    /**
     * Converts the <code>Point</code> value to the String type value. <br>
     * The format of the value is :<br>
     * X:Y
     * 
     * @param point
     *            The point.
     * @return The String type value.
     */
    public static String generatePointValue(Point point)
    {
        return point.getX() + WorkflowConstants.POINT_SEPARATOR + point.getY();
    }

    /**
     * Gets the skip activity.
     * 
     * @param processInstance
     *            {@code ProcessInstance}
     * @return
     */
    public static String getSkipActivity(ProcessInstance processInstance)
    {
        String activity = null;
        ProcessDefinition processDefinition = processInstance
                .getProcessDefinition();
        Node exitNode = processDefinition.getNode(WorkflowConstants.END_NODE);
        WorkflowNodeParameter np = WorkflowNodeParameter
                .createInstance(exitNode);
        activity = np.getAttribute(WorkflowConstants.FIELD_SKIP, null);
        return activity;
    }
}
