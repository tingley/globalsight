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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.node.Decision;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.instantiation.Delegation;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.StringUtil;

/**
 * This class is responsible for the conversion of an JBPM ProcessInstance to
 * Globalsight's WorkflowInstance object and vice versa. It's basically
 * responsible for the whole process of a workflow instance structural edit and
 * getting a particular workflow instance based on a given id.
 */

public class WorkflowProcessAdapter extends WorkflowHelper
{
    private static final Logger s_logger = Logger
            .getLogger(WorkflowProcessAdapter.class.getName());

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package Level Methods
    // ////////////////////////////////////////////////////////////////////
    static WorkflowProcessAdapter createInstance()
    {
        return new WorkflowProcessAdapter();
    }

    /**
     * Gets a list of active "Activity Node" <code>WorkflowTaskInstance</code>
     * (STATE_RUNNING).
     * 
     * @param p_nodeInstances
     *            - An array of "activity node" {@code WorkflowTaskInstance}.
     * @return A list of "activity node" instance that are active (if any).
     */
    @SuppressWarnings("unchecked")
    static List<WorkflowTaskInstance> getActiveNodeInstances(List p_tasks)
    {
        List<WorkflowTaskInstance> activityTasks = new ArrayList<WorkflowTaskInstance>(
                1);

        for (Iterator it = p_tasks.iterator(); it.hasNext();)
        {
            WorkflowTaskInstance workflowTaskInstance = (WorkflowTaskInstance) it
                    .next();

            if (workflowTaskInstance.getTaskState() == WorkflowConstants.STATE_RUNNING)
            {
                activityTasks.add(workflowTaskInstance);
            }

        }

        return activityTasks;
    }

    /**
     * Get a list of active workflow task instance objects (STATE_RUNNING).
     * 
     * @param p_wfTaskInstances
     *            - A list of workflow task instances.
     * @return A HashMap of active workflow task instances (id as the key).
     */
    @SuppressWarnings("unchecked")
    static Map getActiveWfTaskInstances(List p_wfTaskInstances)
    {
        Map<Long, WorkflowTaskInstance> map = new HashMap<Long, WorkflowTaskInstance>(
                3);
        int size = p_wfTaskInstances.size();
        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance wft = (WorkflowTaskInstance) p_wfTaskInstances
                    .get(i);

            if (wft.getTaskState() == WorkflowConstants.STATE_RUNNING)
            {
                map.put(new Long(wft.getTaskId()), wft);
            }
        }

        return map;
    }

    /**
     * Gets a workflow instance object by converting the given jBPM process
     * instance object into a workflow instance object.
     * 
     * @param p_pi
     *            - the process instance to be converted to workflow instance
     *            object.
     */
    @SuppressWarnings("unchecked")
    public static WorkflowInstance getProcessInstance(ProcessInstance p_pi)
            throws Exception
    {

        List<WorkflowTaskInstance> wfTaskInstances = new ArrayList<WorkflowTaskInstance>();
        WorkflowInstance wfInst = new WorkflowInstance();
        /* The map contains the WorkflowNodeParameter */
        Map<String, WorkflowNodeParameter> map = new HashMap<String, WorkflowNodeParameter>();

        wfInst.setId(p_pi.getId());

        ProcessDefinition processDefinition = p_pi.getProcessDefinition();

        /* gets the nodes in the processdefinition */
        List nodeList = processDefinition.getNodes();

        // Create the WorkflowTaskInstances first
        createWorkflowTaskInstances(wfInst, nodeList, map, wfTaskInstances);

        // Now Add the arrows to each node
        addArrowToTaskInstance(nodeList, wfTaskInstances, wfInst, map);

        // now add the properties
        addPropertiesToTaskInstance(wfInst, wfTaskInstances, nodeList, map);

        // now get the max sequence
        wfInst.setMaxSequence(WorkflowAdapterHelper.getMaxSeq(wfTaskInstances));

        /* set the sorted default path node */
        wfInst.setDefaultPathNode(ProcessImplDefaultPathFinder
                .activityNodesInDefaultPath(p_pi.getId(), -1, null,
                        WorkflowJbpmUtil.convertToArray(wfInst
                                .getWorkflowInstanceTasks())));

        updateNodeState(p_pi, wfInst.getDefaultPathNode());

        return wfInst;
    }

    private static void updateNodeState(ProcessInstance p_processInstance,
            List p_nodeList)
    {
        boolean settled = false;
        boolean undispatched = p_processInstance.getRootToken().getNode()
                .getName().equals(WorkflowConstants.START_NODE);

        for (Iterator it = p_nodeList.iterator(); it.hasNext();)
        {
            WorkflowTaskInstance task = (WorkflowTaskInstance) it.next();
            if (!settled
                    && task.getTaskId() == p_processInstance.getRootToken()
                            .getNode().getId())
            {

                settled = true;
                task.setTaskState(WorkflowConstants.STATE_RUNNING);
                continue;

            }

            task.setTaskState((undispatched || settled) ? WorkflowConstants.STATE_INITIAL
                    : WorkflowConstants.STATE_COMPLETED);

        }
    }

    /**
     * Gets a workflow instance object by converting the given jBPM process
     * instance object into a workflow instance object.
     * 
     * @param p_pi
     *            - the process instance to be converted to workflow instance
     *            object.
     */
    public static WorkflowInstance getProcessInstance(long p_instanceId)
    {

        JbpmContext ctx = null;
        WorkflowInstance workflowInstance = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            ProcessInstance instance = ctx.getProcessInstance(p_instanceId);
            workflowInstance = getProcessInstance(instance);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "There is error in create the workflowinstance for the process instance id "
                            + p_instanceId, e);
        }
        finally
        {
            ctx.close();
        }

        return workflowInstance;

    }

    /**
     * Get a HashMap of workflow instance tasks as value and their id as the
     * key.
     */
    static Map makeWorkflowTaskInstanceMap(List p_wfTaskInstances)
            throws Exception
    {
        int size = p_wfTaskInstances.size();
        Map<Long, WorkflowTaskInstance> workflowTasks = new HashMap<Long, WorkflowTaskInstance>(
                size);

        // loop through the nodes and create the WorkflowTask object per node.
        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance task = (WorkflowTaskInstance) p_wfTaskInstances
                    .get(i);

            workflowTasks.put(new Long(task.getTaskId()), task);
        }
        return workflowTasks;
    }

    /**
     * Get a list of node instances following the specified node instance. Note
     * that no Exit/Stop node will be part of this list.
     * 
     * @param p_currentNode
     *            - The current source node.
     * @param p_arrowLabel
     *            - The possible outgoing arrow label of a condition node (if
     *            any).
     * 
     * @return A list of node instances (except for Exit/Stop) following the
     *         specified node instance.
     * 
     */
    static NextNodes nextNodeInstances(WorkflowTaskInstance p_currentNode,
            String p_arrowLabel)
    {
        NextNodes nextNodes = new NextNodes(4);
        return validActivityNodes(p_currentNode, nextNodes, p_arrowLabel);
    }

    /**
     * Returns a list of <code>WorkflowTaskInstance</code> following the
     * specified one.
     * <p>
     * For GBS-2092, complete workflow, stop the list at the nearest Exit node,
     * including the Exit node's incoming arrow in the list.
     * 
     * @param workflowInstanceId
     *            - The workflow instance id.
     * @param startNodeId
     *            - The specified workflow task id.
     */
    public static List<Object> nextNodesInDefaultPath(long workflowInstanceId,
            long startNodeId) throws Exception
    {
        WorkflowInstance workflowInstance = WorkflowProcessAdapter
                .getProcessInstance(workflowInstanceId);

        return ProcessImplDefaultPathFinder.activityNodesInDefaultPath2(
                workflowInstanceId, startNodeId, null, WorkflowJbpmUtil
                        .convertToArray(workflowInstance
                                .getWorkflowInstanceTasks()));
    }

    /**
     * Updates the existing workflow instance . <br>
     * This method will update the structure of the workflow includes add or
     * remove the node.
     * 
     * @param p_workflowInstance
     *            - The workflow instance to be updated.
     * @param p_wfSession
     *            - The WFSession object used for the update process.
     */

    @SuppressWarnings("unchecked")
    static ProcessInstanceInfo updateWorkflowProcessInstance(
            WorkflowInstance p_workflowInstance, ProcessInstance p_pi)
            throws Exception
    {

        try
        {
            // get an array of the ids for the last completed activities
            // that have outgoing arrows to the Exit node.
            long[] lastCompletedActivitiesId = getIdsOfLastCompletedTasks(p_pi);

            ProcessDefinition processDefinition = p_pi.getProcessDefinition();

            setDescription(processDefinition,
                    p_workflowInstance.getDescription());

            // Remove all the nodes which were marked as removed
            // Add all the new node instances which were added
            addRemoveNode(processDefinition, p_workflowInstance);

            updateArrowProperties(p_pi, processDefinition, p_workflowInstance);

            updateNodeNames(p_pi, processDefinition, p_workflowInstance);

            // before commit, activate task for a completed workflow
            List activeNodes = activateNewTasks(lastCompletedActivitiesId, p_pi);

            return new ProcessInstanceInfo(activeNodes);
        }
        catch (Exception e)
        {
            s_logger.error("error occured when update the workflow instance ",
                    e);
            throw e;
        }
    }

    /**
     * Updates the task|taskNode|taskInstance name if the activity is changed.
     */
    private static void updateNodeNames(ProcessInstance p_pi,
            ProcessDefinition p_pd, WorkflowInstance p_wfi)
    {
        for (Object o : p_wfi.getWorkflowInstanceTasks())
        {
            WorkflowTaskInstance wfTask = (WorkflowTaskInstance) o;
            String nodeName = wfTask.getNodeName();
            Node node = WorkflowJbpmUtil.getNodeByNodeName(p_pd, nodeName);
            if (node == null)
            {
                continue;
            }

            if (wfTask.getStructuralState() != WorkflowConstants.REMOVED
                    && wfTask.getStructuralState() != WorkflowConstants.NEW)
            {
                if (wfTask.getType() == WorkflowConstants.ACTIVITY)
                {
                    String activityName = wfTask.getActivityName();
                    // updating names only when the activity is changed
                    if (!activityName.equals(WorkflowJbpmUtil
                            .getActivityName(node)))
                    {
                        String newTaskName = WorkflowJbpmUtil
                                .generateTaskName(activityName);
                        Task task = ((TaskNode) node).getTask(WorkflowJbpmUtil
                                .getTaskName(node));
                        task.setName(newTaskName);
                        // update task instance name if created
                        TaskInstance ti = WorkflowJbpmUtil
                                .getTaskInstanceByNode(p_pi, nodeName);
                        if (ti != null)
                        {
                            ti.setName(newTaskName);
                        }
                        node.setName(WorkflowJbpmUtil.generateNodeName(
                                activityName,
                                WorkflowJbpmUtil.getNodeIndex(nodeName)));
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void updateArrowProperties(ProcessInstance p_pi,
            ProcessDefinition p_processDefinition,
            WorkflowInstance p_workflowInstance) throws Exception
    {

        Vector p_wfTaskInstances = p_workflowInstance
                .getWorkflowInstanceTasks();

        int size = p_wfTaskInstances == null ? 0 : p_wfTaskInstances.size();

        // Once all the nodes have been added ,update the properties
        // of existing and new node instances
        for (int j = 0; j < size; j++)
        {
            WorkflowTaskInstance p_wfTask = (WorkflowTaskInstance) p_wfTaskInstances
                    .get(j);

            String nodeName = p_wfTask.getNodeName() == null ? p_wfTask
                    .getName() : p_wfTask.getNodeName();

            Node node = WorkflowJbpmUtil.getNodeByNodeName(p_processDefinition,
                    nodeName);

            if (node == null)
            {
                /* the node has been deleted */
                continue;
            }

            WorkflowNodeParameter nodePara = WorkflowNodeParameter
                    .createInstance(node);
            if (p_wfTask.getStructuralState() == WorkflowConstants.NEW)
            {
                // add arrows
                addArrows(p_wfTask, node, p_processDefinition);
                updateProperties(p_wfTask, node, p_processDefinition, nodePara);
                WorkflowJbpmUtil.setConfigure(node, nodePara.restore());

            }
            // This means update existing node instance
            else if (p_wfTask.getStructuralState() != WorkflowConstants.REMOVED)
            {
                if (p_wfTask.getType() == WorkflowConstants.ACTIVITY)
                {
                    nodePara.setAttribute(WorkflowConstants.FIELD_ACTIVITY,
                            p_wfTask.getActivityName());
                }
                nodePara.setAttribute(WorkflowConstants.FIELD_POINT,
                        WorkflowJbpmUtil.generatePointValue(p_wfTask
                                .getPosition()));

                updateArrows(p_wfTask, node, p_processDefinition);
                updateProperties(p_wfTask, node, p_processDefinition, nodePara);

                if (p_wfTask.getType() != WorkflowConstants.START)
                {
                    /* The start node doesn't have the config */
                    WorkflowJbpmUtil.setConfigure(node, nodePara.restore());
                }

            }

        }

    }

    @SuppressWarnings("unchecked")
    private static void addRemoveNode(ProcessDefinition p_processDefinition,
            WorkflowInstance p_workflowInstance)
    {

        Vector p_wfTaskInstances = p_workflowInstance
                .getWorkflowInstanceTasks();

        int index = getMaxIndex(p_processDefinition);

        int size = p_wfTaskInstances == null ? 0 : p_wfTaskInstances.size();

        for (int j = 0; j < size; j++)
        {
            WorkflowTaskInstance p_wfTask = (WorkflowTaskInstance) p_wfTaskInstances
                    .get(j);

            // Remove all the arrows associated with this node which were
            // marked as deleted
            if (p_wfTask.getStructuralState() == WorkflowConstants.REMOVED
                    && !p_wfTask.getName().equals(WorkflowConstants.END_NODE))
            {
                // Get the corresponding Node instance from process instance
                Node node = WorkflowJbpmUtil.getNodeById(p_processDefinition,
                        p_wfTask.getTaskId());

                removeArrowForNode(p_processDefinition, node);

                p_processDefinition.removeNode(node);
            }
            // new process taskinstance added
            else if (p_wfTask.getTaskId() == WorkflowTask.ID_UNSET
                    && !p_wfTask.getName().equals(WorkflowConstants.END_NODE))
            {
                index++;
                Node node = createNewNode(index, p_wfTask);

                p_processDefinition.addNode(node);

                Point point = p_wfTask.getPosition();

                WorkflowNodeParameter nodePara = WorkflowNodeParameter
                        .createInstance(node);

                if (p_wfTask.getType() == WorkflowConstants.ACTIVITY)
                {
                    nodePara.setAttribute(WorkflowConstants.FIELD_ACTIVITY,
                            p_wfTask.getActivityName());
                }
                nodePara.setAttribute(WorkflowConstants.FIELD_POINT,
                        WorkflowJbpmUtil.generatePointValue(point));
                nodePara.setAttribute(WorkflowConstants.FIELD_SEQUENCE,
                        String.valueOf(p_wfTask.getSequence()));

                WorkflowJbpmUtil.setConfigure(node, nodePara.restore());

                p_wfTask.setStructuralState(WorkflowConstants.NEW);
                /*
                 * set the name of the workflowTask for the arrow update
                 * operation
                 */
                p_wfTask.setNodeName(node.getName());
                p_wfTask.setName(WorkflowJbpmUtil.getActivityName(node));

            }
        }

        setMaxIndex(p_processDefinition, index);

    }

    /**
     * Gets the max index of the nodes.
     * 
     * @param p_processDefinition
     *            {@code ProcessDefinition}
     * @param endNodePara
     *            {@WorkflowNodeParameter}
     * @return The max index
     */
    private static int getMaxIndex(ProcessDefinition p_processDefinition)
    {
        Node endNode = WorkflowJbpmUtil.getExitNode(p_processDefinition);
        WorkflowNodeParameter endNodePara = WorkflowNodeParameter
                .createInstance(WorkflowJbpmUtil.getConfigure(endNode));
        return endNodePara.getIntAttribute(WorkflowConstants.FIELD_MAX_NODE_ID);
    }

    /**
     * Sets the max index of the nodes.
     * 
     * @param p_processDefinition
     *            {@code ProcessDefinition}
     * @param endNodePara
     *            {@WorkflowNodeParameter}
     * @return The max index
     */
    private static void setMaxIndex(ProcessDefinition p_processDefinition,
            int index)
    {
        Node endNode = WorkflowJbpmUtil.getExitNode(p_processDefinition);
        WorkflowNodeParameter endNodePara = WorkflowNodeParameter
                .createInstance(WorkflowJbpmUtil.getConfigure(endNode));

        WorkflowJbpmUtil.setConfigure(endNode, endNodePara.restore(endNodePara
                .setAttribute(WorkflowConstants.FIELD_MAX_NODE_ID,
                        String.valueOf(index))));

    }

    /**
     * Create a new node for the new added activity in the applet.
     * 
     * @param index
     *            The index of the node.
     * @param p_wfTask
     *            The {@code WorkflowTaskInstance}
     * @return The new created Node.
     */
    private static Node createNewNode(int index, WorkflowTaskInstance p_wfTask)
    {
        Node node = null;

        switch (p_wfTask.getType())
        {

            case WorkflowConstants.CONDITION:
                node = createConditionNode(p_wfTask.getActivityName(), index);
                break;
            case WorkflowConstants.ACTIVITY:
                node = createTaskNode(p_wfTask.getActivityName(), index);
                break;
            default:
                break;
        }

        return node;
    }

    /**
     * Creates the task node.
     * 
     * @param name
     *            The name of the activity.
     * @param index
     *            The index of hte task.
     * @return The node.
     */
    private static Node createTaskNode(String name, int index)
    {
        TaskNode node = new TaskNode(WorkflowJbpmUtil.generateNodeName(name,
                index));
        Delegation assignment = new Delegation(WorkflowAssignment.class);
        assignment.setClassName(WorkflowAssignment.class.getName());
        Task task = new Task(WorkflowJbpmUtil.generateTaskName(name));
        task.setAssignmentDelegation(assignment);
        // task.setTaskController(createTaskController());
        node.addTask(task);
        return node;
    }

    /**
     * Creates the condition node.
     * 
     * @param name
     *            the name of the activity.
     * @param index
     *            the index of the node.
     * @return the condition node.
     */
    private static Node createConditionNode(String name, int index)
    {
        Decision decision = new Decision(WorkflowJbpmUtil.generateNodeName(
                name, index));

        Delegation delegation = new Delegation(WorkflowDecision.class);
        delegation.setClassName(WorkflowDecision.class.getName());

        /* set the delegation to the decision */
        WorkflowJbpmUtil.setPrivateValue(decision, "decisionDelegation",
                delegation);

        return decision;
    }

    /**
     * Removes arrows from the node.
     * 
     * @param p_processDefinition
     * @param p_node
     */
    private static void removeArrowForNode(
            ProcessDefinition p_processDefinition, Node p_node)
    {

        removeArrows(p_processDefinition, p_node.getLeavingTransitions(), true);

        removeArrows(p_processDefinition, p_node.getArrivingTransitions(),
                false);
    }

    /**
     * Removes the arrows
     * 
     * @param p_processDefinition
     * @param collection
     */
    @SuppressWarnings("unchecked")
    private static void removeArrows(ProcessDefinition p_processDefinition,
            Collection collection, boolean isLeaving)
    {
        for (Iterator it = collection.iterator(); it.hasNext();)
        {
            Transition transition = (Transition) it.next();

            Node sourceNode = transition.getFrom();
            Node targetNode = transition.getTo();

            it.remove();
            if (isLeaving)
            {
                targetNode.removeArrivingTransition(transition);
            }
            else
            {
                sourceNode.removeLeavingTransition(transition);
            }

        }
    }

    /**
     * Sets the description of the ProcessDefinition.
     * 
     * @param p_processDefinition
     *            ProcessDefinition.
     * @param p_description
     *            the value of the description.
     */
    private static void setDescription(ProcessDefinition p_processDefinition,
            String p_description)
    {

        if (StringUtil.isEmpty(p_description))
        {
            return;
        }

        Node node = p_processDefinition.getNode(WorkflowConstants.END_NODE);
        WorkflowNodeParameter exitNodeP = WorkflowNodeParameter
                .createInstance(WorkflowJbpmUtil.getConfigure(node));
        WorkflowNodeParameter subNodeP = exitNodeP
                .getsubNodeParameter(WorkflowConstants.FIELD_START_STATE);

        String startValue = subNodeP.restore(subNodeP.setAttribute(
                WorkflowConstants.FIELD_DESCRIPTION, p_description));
        String value = exitNodeP.restore(exitNodeP.setAttribute(
                WorkflowConstants.FIELD_START_STATE, startValue));
        WorkflowJbpmUtil.setConfigure(node, value);
    }

    /**
     * Creates a WorkflowTaskInstance object for the given TaskInstance object.
     * 
     * @param p_taskInstance
     *            - The task instance object to be converted to
     *            WorkflowTaskInstance.
     * @param p_userId
     *            the user id.
     * @return a WorkflowTaskInstance object populated with all the required
     *         fields.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    static WorkflowTaskInstance workflowTaskInstance(
            TaskInstance p_taskInstance, String p_userId) throws Exception
    {
        Node taskNode = p_taskInstance.getTask().getTaskNode();
        WorkflowNodeParameter param = WorkflowNodeParameter
                .createInstance(WorkflowJbpmUtil.getConfigure(taskNode));
        WorkflowTaskInstance wti = new WorkflowTaskInstance(
                WorkflowJbpmUtil.getActivityName(taskNode),
                WorkflowConstants.ACTIVITY);
        wti.setAcceptUser(p_taskInstance.getActorId());
        wti.setTaskId(taskNode.getId());
        wti.setNodeName(taskNode.getName());
        wti.setDesc(param.getAttribute(WorkflowConstants.FIELD_SEQUENCE));
        wti.setPosition(param.getPointAttribute(WorkflowConstants.FIELD_POINT));
        wti.setTaskState(WorkflowJbpmUtil.getStateFromTaskInstance(
                p_taskInstance, p_userId, WorkflowConstants.TASK_ALL_STATES));
        List outgoingArrows = taskNode.getLeavingTransitions();
        boolean hasConditionNode = false;
        for (int i = 0; !hasConditionNode && i < outgoingArrows.size(); i++)
        {
            Transition transition = (Transition) outgoingArrows.get(i);
            Node targetNode = transition.getTo();
            hasConditionNode = WorkflowJbpmUtil.getNodeType(targetNode) == WorkflowConstants.CONDITION;
            if (hasConditionNode)
            {
                List outgoingArs = targetNode.getLeavingTransitions();
                int size = outgoingArs.size();
                Vector<ConditionNodeTargetInfo> conditionNodeTargetInfos = new Vector<ConditionNodeTargetInfo>(
                        size);
                for (int j = 0; j < size; j++)
                {
                    Transition arrow = (Transition) outgoingArs.get(j);
                    Node node = arrow.getTo();
                    conditionNodeTargetInfos.add(new ConditionNodeTargetInfo(
                            arrow.getName(), WorkflowJbpmUtil
                                    .getActivityName(node)));
                }
                wti.setConditionNodeTargetInfos(conditionNodeTargetInfos);
            }
        }
        addActivityNodeProperties(param, wti);
        return wti;
    }

    private static void addActivityNodeProperties(WorkflowNodeParameter param,
            WorkflowTaskInstance wti) throws Exception
    {
        addActivityNodeProperties(null, param, wti);
    }

    /**
     * Adds the required properties of the activity node.
     * 
     * @param p_param
     *            the related WorkflowNodeParameter
     * @param p_wti
     *            the WorkflowTaskInstance.
     * 
     * @throws Exception
     */
    private static void addActivityNodeProperties(WorkflowInstance p_wfi,
            WorkflowNodeParameter p_param, WorkflowTaskInstance p_wti)
            throws Exception
    {
        Activity activity = ServerProxy.getJobHandler().getActivity(
                p_param.getAttribute(WorkflowConstants.FIELD_ACTIVITY));

        String acceptor = null;
        if (p_wfi != null)
        {
            JbpmContext ctx = null;
            try
            {
                ctx = WorkflowConfiguration.getInstance().getJbpmContext();
                ProcessInstance processInstance = ctx.getProcessInstance(p_wfi
                        .getId());
                if (processInstance != null)
                {
                    TaskInstance taskInstance = WorkflowJbpmUtil
                            .getTaskInstanceByNode(processInstance,
                                    p_wti.getNodeName());
                    if (taskInstance != null)
                    {
                        acceptor = taskInstance.getActorId();
                    }
                }
            }
            finally
            {
                ctx.close();
            }
        }

        // sequence of the node
        int sequence = WorkflowAdapterHelper.parseInt(p_wti.getDesc(), -9);

        /*
         * the role type represents select all or specied user in activity
         * properties
         */
        boolean isUserRole = p_param
                .getBooleanAttribute(WorkflowConstants.FIELD_ROLE_TYPE);

        if (WorkflowAdapterHelper.isCostingEnabled())
        {
            // expense rate selection criteria
            int rateSelectionCriteria = p_param.getIntAttribute(
                    WorkflowConstants.FIELD_RATE_SELECTION_CRITERIA,
                    WorkflowConstants.USE_ONLY_SELECTED_RATE);
            // expense rate id
            long expenseRateId = p_param.getLongAttribute(
                    WorkflowConstants.FIELD_EXPENSE_RATE_ID,
                    WorkflowTaskInstance.NO_RATE);

            p_wti.setRateSelectionCriteria(rateSelectionCriteria);
            p_wti.setExpenseRateId(expenseRateId);

            if (WorkflowAdapterHelper.isRevenueEnabled())
            {
                // revenue rate id
                long revenueRateId = p_param.getLongAttribute(
                        WorkflowConstants.FIELD_REVENUE_RATE_ID,
                        WorkflowTaskInstance.NO_RATE);
                p_wti.setRevenueRateId(revenueRateId);
            }
        }

        long timeToAccept = p_param.getLongAttribute(
                WorkflowConstants.FIELD_ACCEPTED_TIME,
                WorkflowTaskInstance.NO_RATE);

        long timeToComplete = p_param.getLongAttribute(
                WorkflowConstants.FIELD_COMPLETED_TIME,
                WorkflowTaskInstance.NO_RATE);

        long overduePM = p_param.getLongAttribute(
                WorkflowConstants.FIELD_OVERDUE_PM_TIME,
                WorkflowTaskInstance.NO_RATE);

        long overdueUser = p_param.getLongAttribute(
                WorkflowConstants.FIELD_OVERDUE_USER_TIME,
                WorkflowTaskInstance.NO_RATE);

        String displayRoleName = p_param.getAttribute(
                WorkflowConstants.FIELD_ROLE_NAME,
                WorkflowTaskInstance.DEFAULT_ROLE_NAME);

        // system action type
        String actionType = p_param.getAttribute(
                WorkflowConstants.FIELD_ACTION_TYPE,
                WorkflowTaskInstance.NO_ACTION);
        
        int reportUploadCheck = p_param.getIntAttribute(
                WorkflowConstants.FIELD_REPORT_UPLOAD_CHECK,
                WorkflowConstants.REPORT_UPLOAD_CHECK);

        String rolePreference = p_param
                .getAttribute(WorkflowConstants.FIELD_ROLE_PREFERENCE);

        String[] roles = p_param
                .getArrayAttribute(WorkflowConstants.FIELD_ROLES);

        // Role Preference
        p_wti.setRolePreference(rolePreference);
        p_wti.setActionType(actionType);
        p_wti.setActivity(activity);
        p_wti.setAcceptUser(acceptor);
        p_wti.setSequence(sequence);
        p_wti.setRoleType(isUserRole);
        p_wti.setRoles(roles);
        p_wti.setAcceptedTime(timeToAccept);
        p_wti.setCompletedTime(timeToComplete);
        p_wti.setOverdueToPM(overduePM);
        p_wti.setOverdueToUser(overdueUser);
        p_wti.setReportUploadCheck(reportUploadCheck);
        p_wti.setDisplayRoleName(UserUtil.getUserNamesByIds(displayRoleName));
    }

    /**
     * Get a collection of workflow instance tasks (as WorkflowTaskInstance
     * objects).
     * 
     * @param p_process
     *            - The process instance used to get its tasks.
     * @param p_nodes
     *            - The nodes (i-Flow tasks) of the process instance.
     * @return A collection of WorkflowTaskInstance objects.
     * @exception Exception
     *                - An i-Flow related exception.
     */
    @SuppressWarnings("unchecked")
    List workflowTaskInstances(ProcessInstance p_process) throws Exception
    {
        List<WorkflowTaskInstance> workflowTasks = new ArrayList<WorkflowTaskInstance>();
        ProcessDefinition processDefinition = p_process.getProcessDefinition();
        List nodes = processDefinition.getNodes();

        // loop through the nodes and create the WorkflowTask object per node.
        for (Iterator it = nodes.iterator(); it.hasNext();)
        {
            Node node = (Node) it.next();
            WorkflowTaskInstance task = workflowTaskInstance(node, p_process);
            TaskInstance taskInstance = WorkflowJbpmUtil.getTaskInstanceByNode(
                    p_process, node.getName());
            if (taskInstance != null)
            {
                task.setAcceptUser(taskInstance.getActorId());
            }
            if (task != null)
            {
                workflowTasks.add(task);
            }
        }

        return workflowTasks;
    }

    /**
     * Return the target node from the passed in node, whose arrow is labeled
     * with the name passed in.
     * 
     * @param p_ni
     *            The node to find the target from.
     * @param p_arrowName
     *            The arrow to follow to the target node.
     * 
     * @return The target node from the condition node followed by the arrow.
     */
    static WorkflowTaskInstance targetNodeInstanceByArrowLabel(
            WorkflowTaskInstance p_ni, String p_arrowLabel)
    {
        // get outgoing arrow instances
        Vector outGoingArrows = p_ni.getOutgoingArrows();

        WorkflowTaskInstance targetNode = null;

        Enumeration eu = outGoingArrows.elements();

        while (eu.hasMoreElements())
        {
            WorkflowArrowInstance ai = (WorkflowArrowInstance) eu.nextElement();
            if (ai.getName().equals(p_arrowLabel))
            {
                targetNode = (WorkflowTaskInstance) ai.getTargetNode();
                break;
            }
        }

        return targetNode;
    }

    /**
     * Provides access to the UDA that stores the assignees. Updates the UDA
     * with the correct up-to-date users. This UDA is used by the role script to
     * assign a task to the correct users.
     */
    static String[] updateAssignees(String[] p_userIds, ProcessInstance p_pi,
            WorkflowTaskInstance task, Date p_baseDate, TaskInfo p_taskInfo,
            String p_preference) throws WorkflowException
    {

        try
        {
            String assignees[] = AssigneeFilter
                    .getAssigneeListByRolePreference(task, p_userIds,
                            p_baseDate, p_preference, p_taskInfo);

            boolean allUserIds = assignees == null
                    && WorkflowConstants.AVAILABLE_ROLE_PREFERENCE
                            .equals(p_preference);
            Node node = p_pi.getProcessDefinition().getNode(task.getNodeName());
            WorkflowNodeParameter nodePara = WorkflowNodeParameter
                    .createInstance(WorkflowJbpmUtil.getConfigure(node));

            String assigneeValue = allUserIds ? AssigneeFilter
                    .assigneeListAsString(p_userIds) : AssigneeFilter
                    .assigneeListAsString(assignees);

            String updatedValue = nodePara.restore(nodePara.setAttribute(
                    WorkflowConstants.FIELD_ROLE_ID, assigneeValue));

            WorkflowJbpmUtil.setConfigure(node, updatedValue);

            return assignees;
        }
        catch (Exception e)
        {
            s_logger.error("Failed to update the assignees for task "
                    + task.getTaskId() + " in process instance " + p_pi.getId());
            String args[] =
            { Long.toString(task.getTaskId()), Long.toString(p_pi.getId()) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_UPDATE_ASSIGNEES_FOR_TASK,
                    args, e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Helper Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Activate the task(s) right after the last completed task for a workflow
     * that is in completed state. Basically, we're reactivating a completed
     * workflow by activating the newly added task (it's possible to have more
     * than one new task but we'll activate ones after the last completed
     * activity). THIS METHOD IS ONLY USED AFTER AN EXPORT FAILURE (where the PM
     * can add one or more tasks for fixing the export issue).
     * 
     * @param p_lastActivityId
     *            - The id of the last completed activity.
     * @param p_processInstance
     *            - The process instance to be edited (before commit).
     * 
     * @return A list of possible newly activated nodes.
     * 
     */
    private static List activateNewTasks(long[] p_lastActivitiesId,
            ProcessInstance p_processInstance) throws Exception
    {
        List activeNodes = null;
        if (p_processInstance.hasEnded())
        {

            /*
             * because in curren system, the user cannot edit the workflow
             * instance when the job is complete, so it should never be here.
             */

            // activeNodes = new ArrayList(1);
            // for (int i = 0; i < p_lastActivitiesId.length; i++)
            // {
            // NodeInstance lastCompletedNode = p_processInstance
            // .getNodeInstance(p_lastActivitiesId[i]);
            //
            // ArrowInstance[] outgoingArrows = lastCompletedNode
            // .getOutgoingArrowInstances();
            //
            // for (int j = 0; j < outgoingArrows.length; j++)
            // {
            // NodeInstance ni = outgoingArrows[j].getTargetNodeInstance();
            // if (ni.getState() == NodeInstance.STATE_INITIAL)
            // {
            // ni.activate();
            // activeNodes.add(ni);
            // }
            // }
            // }
        }

        // Workaround - due to iFlow's time delay after activating a node,
        // getActiveNodeInstances does not return the active nodes.
        // Therefore, we'll put the activated node in a list and
        // return it to the parties interested in the active node
        // (i.e. email notification process)
        return activeNodes;
    }

    /**
     * Creates the workflow instance.
     * 
     * @param p_wfInst
     *            The workflow instance.
     * @param p_nodeList
     *            The node list.
     * @param p_map
     *            The map of the {@code WorkfowNodeParameter}
     * @param p_taskList
     *            The node instance task list.
     */
    private static void createWorkflowTaskInstances(WorkflowInstance p_wfInst,
            List p_nodeList, Map<String, WorkflowNodeParameter> p_map,
            List<WorkflowTaskInstance> p_taskList)
    {

        /*
         * because Jbpm cannot store the data in the startstate, we put the
         * content in the endstate to store the startstate related parameter.
         */
        /* record the parameter in the endstate for startstate */
        WorkflowNodeParameter startNodeParameter = null;
        /* record the task of the statrt state */
        WorkflowTaskInstance startInstance = null;

        for (Iterator it = p_nodeList.iterator(); it.hasNext();)
        {
            Node node = (Node) it.next();
            WorkflowNodeParameter workflowNodeParameter = WorkflowNodeParameter
                    .createInstance(WorkflowJbpmUtil.getConfigure(node));

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("The content of the node "
                        + WorkflowJbpmUtil.getTaskName(node) + " is "
                        + WorkflowJbpmUtil.getConfigure(node));
            }

            /* build the taskinstance */
            WorkflowTaskInstance wfTaskInst = p_wfInst.addWorkflowTaskInstance(
                    WorkflowJbpmUtil.getActivityName(node),
                    WorkflowJbpmUtil.getNodeType(node));
            wfTaskInst.setNodeName(node.getName());
            wfTaskInst.setTaskId(node.getId());

            if (StringUtil.isEmpty(WorkflowJbpmUtil.getConfigure(node)))
            {
                /* the node is startstate */
                p_taskList.add(wfTaskInst);
                startInstance = wfTaskInst;
                continue;
            }

            wfTaskInst.setDesc(workflowNodeParameter
                    .getAttribute(WorkflowConstants.FIELD_SEQUENCE));
            wfTaskInst.setPosition(workflowNodeParameter
                    .getPointAttribute(WorkflowConstants.FIELD_POINT));
            wfTaskInst.setTaskState(WorkflowConstants.STATE_INITIAL);
            /* add the task to the list */
            p_taskList.add(wfTaskInst);
            /* add the parameter to the map */
            p_map.put(node.getName(), workflowNodeParameter);

            if (wfTaskInst.getType() == WorkflowConstants.STOP)
            {
                /* The node is end state */
                startNodeParameter = workflowNodeParameter
                        .getsubNodeParameter(WorkflowConstants.FIELD_START_STATE);
            }

        }

        /* set the parameter for the start node */
        startInstance.setDesc(startNodeParameter
                .getAttribute(WorkflowConstants.FIELD_SEQUENCE));
        startInstance.setPosition(startNodeParameter
                .getPointAttribute(WorkflowConstants.FIELD_POINT));
        p_map.put(WorkflowConstants.START_NODE, startNodeParameter);
    }

    /**
     * Adds the required properties of the activity node.
     * 
     * @param p_map
     *            The map contains the {@code WorkflowNodeParameter}.
     * @param p_task
     *            The {@code WorkflowTaskInstance}.
     * @param p_node
     *            The {@code Node}.
     * @throws Exception
     */
    private static void addActivityNodeProperties(WorkflowInstance p_wfi,
            Map p_map, WorkflowTaskInstance p_task, Node p_node)
            throws Exception
    {
        String nodeName = p_node.getName();
        /* Gets the nodeParameter */
        WorkflowNodeParameter nodeParameter = (WorkflowNodeParameter) p_map
                .get(nodeName);
        addActivityNodeProperties(p_wfi, nodeParameter, p_task);
    }

    /*
     * This function adds all the incoming arrows and outgoing arrows to the
     * nodeInstances
     */
    private static void addArrowToTaskInstance(List p_nodeList,
            List p_wfTaskInstances, WorkflowInstance wfInst, Map p_map)
            throws Exception
    {

        Iterator it = p_nodeList.iterator();

        while (it.hasNext())
        {
            /* iterator the node */
            Node node = (Node) it.next();

            List transitionList = node.getLeavingTransitions();

            if (transitionList == null)
            {
                continue;
            }
            for (int i = 0; i < transitionList.size(); i++)
            {
                /* iterator the leaving transition */
                Transition transition = (Transition) transitionList.get(i);
                Node sourceNode = transition.getFrom();
                Node targetNode = transition.getTo();
                WorkflowTaskInstance wfSourceNode = WorkflowJbpmUtil
                        .getTaskInstanceById(p_wfTaskInstances,
                                sourceNode.getId());
                WorkflowTaskInstance wfTargetNode = WorkflowJbpmUtil
                        .getTaskInstanceById(p_wfTaskInstances,
                                targetNode.getId());

                WorkflowArrowInstance p_outgoingArrow = wfInst
                        .addArrowInstance(transition.getName(),
                                WorkflowConstants.REGULAR_ARROW, wfSourceNode,
                                wfTargetNode);

                WorkflowNodeParameter sourceParameter = (WorkflowNodeParameter) p_map
                        .get(sourceNode.getName());
                WorkflowNodeParameter targetParameter = (WorkflowNodeParameter) p_map
                        .get(targetNode.getName());

                setPropertiesForArrow(p_outgoingArrow, sourceParameter,
                        targetParameter, i);

            }

        }

    }

    /**
     * Sets the properties for the arrow.
     * 
     * @param p_outgoingArrow
     *            The arrow.
     * @param p_sourceParameter
     *            The parameter object for the source node.
     * @param p_targetParameter
     *            The parameter object for the target node.
     * @param p_arrowNumber
     *            The number of the arrow.
     */
    private static void setPropertiesForArrow(
            WorkflowArrowInstance p_outgoingArrow,
            WorkflowNodeParameter p_sourceParameter,
            WorkflowNodeParameter p_targetParameter, int p_arrowNumber)
    {
        p_outgoingArrow.setStartPoint(new Point(0, 0));
        p_outgoingArrow.setEndPoint(new Point(0, 0));
        p_outgoingArrow.setPoints(new Point[0]);
        p_outgoingArrow.isActive(true);
        p_outgoingArrow.setState(WorkflowConstants.STATE_INITIAL);
    }

    /*
     * Add condition properties to the condition node instance
     */
    private static void addConditionNodeProperties(
            WorkflowTaskInstance p_WfTask, Node p_node, Map p_map)
            throws Exception
    {
        WorkflowNodeParameter nodeParameter = ((WorkflowNodeParameter) p_map
                .get(p_node.getName()))
                .getsubNodeParameter(WorkflowConstants.FIELD_WORKFLOW_CONDITION_SPEC);

        WorkflowConditionSpec p_wfCondSpec = p_WfTask.getConditionSpec();

        p_wfCondSpec.setConditionAttribute(nodeParameter
                .getAttribute(WorkflowConstants.FIELD_CONDITION_ATTRIBUTE));

        List<WorkflowBranchSpec> m_workflowBranchSpecs = new ArrayList<WorkflowBranchSpec>();
        List transitionList = p_node.getLeavingTransitions();

        for (int i = 0; i < transitionList.size(); i++)
        {
            String name = WorkflowConstants.FIELD_WORKFLOW_BRANCH_SPEC
                    + WorkflowConstants.NAME_SEPARATOR + i;
            WorkflowNodeParameter subNodeParameter = nodeParameter
                    .getsubNodeParameter(name);
            WorkflowBranchSpec p_workflowBranchSpec = p_wfCondSpec
                    .setCondBranchSpecInfo(
                            subNodeParameter
                                    .getAttribute(WorkflowConstants.FIELD_ARROW_LABEL),
                            subNodeParameter
                                    .getIntAttribute(WorkflowConstants.FIELD_COMPARISON_OPERATOR),
                            subNodeParameter
                                    .getAttribute(WorkflowConstants.FIELD_BRANCH_VALUE),
                            subNodeParameter
                                    .getBooleanAttribute(WorkflowConstants.FIELD_IS_DEFAULT));
            m_workflowBranchSpecs.add(p_workflowBranchSpec);
        }

        p_wfCondSpec.setEvalOrder(m_workflowBranchSpecs);
        p_WfTask.setConditionSpec(p_wfCondSpec);
    }

    /**
     * Adds properties to the task instances.
     */
    /**
     * Adding properties to the task instances
     */
    private static void addPropertiesToTaskInstance(
            WorkflowTaskInstance p_WfTask, WorkflowNodeParameter p_nodeParameter)
            throws Exception
    {
        addActivityNodeProperties(p_nodeParameter, p_WfTask);
    }

    /**
     * Adds properties to the task instances.
     */
    private static void addPropertiesToTaskInstance(WorkflowInstance p_wfi,
            List p_wfTaskInstances, List p_nodeList, Map p_map)
            throws Exception

    {
        Iterator it = p_nodeList.iterator();
        while (it.hasNext())
        {
            Node node = (Node) it.next();
            WorkflowTaskInstance wfTask = WorkflowJbpmUtil.getTaskInstanceById(
                    p_wfTaskInstances, node.getId());

            switch (wfTask.getType())
            {
                case WorkflowConstants.ACTIVITY:
                    addActivityNodeProperties(p_wfi, p_map, wfTask, node);
                    break;
                case WorkflowConstants.CONDITION:
                    addConditionNodeProperties(wfTask, node, p_map);
                    break;

                case WorkflowConstants.OR:
                case WorkflowConstants.AND:
                    /* the and/or node will not be supported in current system. */
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Add the valid activity node to the list. For non-activity node types,
     * need to recurse and find one (if any exist)
     */
    private static void addToActiveNodeList(WorkflowTaskInstance p_node,
            NextNodes p_nextNodes, String p_arrowLabel)
    {
        // if activity type, add to list
        if (p_node != null && p_node.getType() == WorkflowConstants.ACTIVITY)
        {
            p_nextNodes.addNode(p_node, new Integer(p_node.getTaskState()));
        }
        else
        {
            findValidActivityNode(p_node, p_nextNodes, p_arrowLabel);
        }
    }

    /**
     * Find a possible activity node that follows a Condition, OR, or AND node.
     * 
     */
    private static void findValidActivityNode(WorkflowTaskInstance p_node,
            NextNodes p_nextNodes, String p_arrowLabel)
    {
        if (p_node == null)
        {
            return;
        }

        switch (p_node.getType())
        {
            default:
                break;

            case WorkflowConstants.STOP:
                p_nextNodes.exitNode();
                break;

            case WorkflowConstants.CONDITION:
                WorkflowTaskInstance targetNode = targetNodeOfConditionNode(
                        p_node, p_arrowLabel);
                addToActiveNodeList(targetNode, p_nextNodes, p_arrowLabel);
                break;

            case WorkflowConstants.OR:
                /* The system don't need to support or */
                break;

            case WorkflowConstants.AND:
                /* The system don't need to support and */
                break;
        }
    }

    /**
     * Gets an array of ids for the last completed tasks (directly connected to
     * the Exit node).
     * 
     */
    private static long[] getIdsOfLastCompletedTasks(ProcessInstance p_pi)
    {
        long[] ids = new long[0];
        if (p_pi.hasEnded())
        {
            /*
             * Because in current system, the user cannot edit the
             * processInstance when it is ended. so we think part of code will
             * never be reached
             */
            List nodes = p_pi.getProcessDefinition().getNodes();
            boolean isExitNode = false;
            Set trans = null;

            for (Iterator it = nodes.iterator(); it.hasNext();)
            {
                Node n = (Node) it.next();
                isExitNode = WorkflowJbpmUtil.isExitNode(n);

                if (isExitNode)
                {
                    trans = n.getArrivingTransitions();
                }
            }

            ids = new long[trans.size()];
            int i = 0;
            // now that we have the incoming arrows, let's find the source nodes
            for (Iterator it = trans.iterator(); it.hasNext();)
            {
                ids[i++] = ((Transition) it.next()).getFrom().getId();
            }

        }
        /* should always return zero size array */
        return ids;
    }

    /**
     * Find the target node following the condition node. Update the UDA value
     * also.
     */
    private static WorkflowTaskInstance targetNodeOfConditionNode(
            WorkflowTaskInstance p_node, String p_arrowLabel)
    {
        // first update the UDA value
        String validArrowName = updateConditionNodeResult(p_node, p_arrowLabel);
        p_arrowLabel = validArrowName == null ? p_arrowLabel : validArrowName;

        return targetNodeInstanceByArrowLabel(p_node, p_arrowLabel);
    }

    /**
     * @param p_wfTask
     * @param p_node
     * @param p_processDefinition
     * @throws Exception
     */
    private static void updateArrows(WorkflowTaskInstance p_wfTask,
            Node p_node, ProcessDefinition p_processDefinition)
            throws Exception
    {

        /*
         * It seems that the p_wfTask will not record the deleted arrow, we need
         * to delete the arrow manually.
         */
        removeArrows(p_node, p_wfTask);

        Vector outgoingArrows = p_wfTask.getOutgoingArrows();
        int size = outgoingArrows == null ? 0 : outgoingArrows.size();
        for (int i = 0; i < size; i++)
        {
            WorkflowArrowInstance wfArrow = (WorkflowArrowInstance) outgoingArrows
                    .get(i);

            if (wfArrow.getStructuralState() == WorkflowConstants.REMOVED)
            {
                Transition transition = p_node.getLeavingTransition(wfArrow
                        .getName());
                if (transition != null)
                {
                    p_node.removeLeavingTransition(transition);
                }
            }
            else
            {
                if (wfArrow.getArrowInstanceId() == -1)
                {
                    /* new added arrow */
                    Node targetNode = WorkflowJbpmUtil.getNodeByWfTask(
                            p_processDefinition, wfArrow.getTargetNode());

                    addTransition(p_node, targetNode, wfArrow.getName());
                }
                else
                {
                    /* update existing arrow instance */
                    List transitions = p_node.getLeavingTransitions();
                    Transition transition = null;

                    for (Iterator it = transitions.iterator(); it.hasNext();)
                    {
                        Transition t = (Transition) it.next();
                        if (WorkflowJbpmUtil.getActivityName(
                                t.getTo().getName()).equals(
                                wfArrow.getTargetNode().getActivityName()))
                        {
                            transition = t;
                            break;
                        }
                    }

                    if (transition != null)
                    {
                        transition.setName(wfArrow.getName());
                    }

                }// END OF wfArrow.getArrowInstanceId()
            }// END OF wfArrow.getStructuralState()
        }
    }

    private static void removeArrows(Node p_node, WorkflowTaskInstance p_wfTask)
    {

        List trans = p_node.getLeavingTransitions();
        List<Transition> removeList = new ArrayList<Transition>(trans.size());
        for (Iterator it = trans.iterator(); it.hasNext();)
        {
            Transition transition = (Transition) it.next();
            if (!isArrowExist(transition, p_wfTask))
            {
                removeList.add(transition);
            }
        }

        removeTransitions(p_node, removeList);
    }

    private static void removeTransitions(Node p_node, List trans)
    {
        for (Iterator it = trans.iterator(); it.hasNext();)
        {
            Transition transition = (Transition) it.next();
            Node targetNode = transition.getTo();
            targetNode.removeArrivingTransition(transition);
            p_node.removeLeavingTransition(transition);
        }
    }

    private static boolean isArrowExist(Transition p_transition,
            WorkflowTaskInstance p_wfTask)
    {
        boolean isExist = false;

        Vector outgoingArrows = p_wfTask.getOutgoingArrows();

        for (Enumeration e = outgoingArrows.elements(); e.hasMoreElements();)
        {
            WorkflowArrowInstance wfArrow = (WorkflowArrowInstance) e
                    .nextElement();
            if (p_transition.getName().equals(wfArrow.getName()))
            {
                isExist = true;
                break;
            }
        }

        return isExist;
    }

    /**
     * Gets the label name of the arrow.
     * 
     */
    private static String updateConditionNodeResult(
            WorkflowTaskInstance p_node, String p_arrowLabel)
    {

        if (StringUtil.isEmpty(p_arrowLabel))
        {

            return ProcessImplDefaultPathFinder.getDefaultArrowName(p_node);
        }
        else
        {
            return p_arrowLabel;
        }

    }

    private static void updateProperties(WorkflowTaskInstance p_wfTask,
            Node p_existingNodeInst, ProcessDefinition p_processDefinition,
            WorkflowNodeParameter workflowNodeParameter) throws Exception
    {
        switch (p_wfTask.getType())
        {

            case WorkflowConstants.ACTIVITY:
                updateActivityNodeProperties(p_processDefinition,
                        p_existingNodeInst, p_wfTask, workflowNodeParameter);
                break;

            case WorkflowConstants.CONDITION:
                updateConditionNodeProperties(p_processDefinition,
                        p_existingNodeInst, p_wfTask, workflowNodeParameter);
                break;

            default:
                break;
        }
    }

    /**
     * Update the properties of an activity node in Jbpm
     * 
     */
    private static void updateActivityNodeProperties(
            ProcessDefinition p_processDefinition, Node p_node,
            WorkflowTaskInstance p_wfTask,
            WorkflowNodeParameter workflowNodeParameter) throws Exception
    {

        WorkflowNodeParameter nodePara = workflowNodeParameter == null ? WorkflowNodeParameter
                .createInstance(p_node) : workflowNodeParameter;

        int sequence = p_wfTask.getSequence();

        nodePara.setAttribute(WorkflowConstants.FIELD_SEQUENCE,
                String.valueOf(sequence));

        /* roles */
        nodePara.setAttribute(WorkflowConstants.FIELD_ROLES,
                p_wfTask.getRolesAsString());

        nodePara.setAttribute(WorkflowConstants.FIELD_ROLE_TYPE,
                String.valueOf(p_wfTask.getRoleType()));

        /* set the pm to the workflow taskinstance */
        setPmForNode(p_processDefinition, nodePara);

        // update the ASSIGNEES UDA
        nodePara.setAttribute(WorkflowConstants.FIELD_ROLE_ID, "0");

        // setRoleType(p_existingNodeInst, p_wfTask);

        if (WorkflowAdapterHelper.isCostingEnabled())
        {
            // ---- specify expense rate selection criteria ----
            int rateSelectionCriteria = p_wfTask.getRateSelectionCriteria();
            nodePara.setAttribute(
                    WorkflowConstants.FIELD_RATE_SELECTION_CRITERIA,
                    String.valueOf(rateSelectionCriteria));

            // ---- specify expense rate information ----
            long rateId = p_wfTask.getExpenseRateId();
            nodePara.setAttribute(WorkflowConstants.FIELD_EXPENSE_RATE_ID,
                    String.valueOf(rateId));

            if (WorkflowAdapterHelper.isRevenueEnabled())
            {
                // ---- specify revenue rate information ----
                rateId = p_wfTask.getRevenueRateId();
                nodePara.setAttribute(WorkflowConstants.FIELD_REVENUE_RATE_ID,
                        String.valueOf(rateId));

            }
        }
        // update UDA Time to accept
        long acceptTime = p_wfTask.getAcceptTime();
        nodePara.setAttribute(WorkflowConstants.FIELD_ACCEPTED_TIME,
                Long.toString(acceptTime));

        // update UDA Time to complete
        long completeTime = p_wfTask.getCompletedTime();
        nodePara.setAttribute(WorkflowConstants.FIELD_COMPLETED_TIME,
                Long.toString(completeTime));

        long overduePM = p_wfTask.getOverdueToPM();
        nodePara.setAttribute(WorkflowConstants.FIELD_OVERDUE_PM_TIME,
                Long.toString(overduePM));

        // update UDA Time to complete
        long overdueUser = p_wfTask.getOverdueToUser();
        nodePara.setAttribute(WorkflowConstants.FIELD_OVERDUE_USER_TIME,
                Long.toString(overdueUser));

        // update role name UDA
        String role_name = p_wfTask.getDisplayRoleName();
        nodePara.setAttribute(WorkflowConstants.FIELD_ROLE_NAME,
                UserUtil.getUserIdsByNames(role_name));

        // update system action type UDA
        String actionType = p_wfTask.getActionType();
        nodePara.setAttribute(WorkflowConstants.FIELD_ACTION_TYPE, actionType);

        // update system action type UDA
        String rolePreference = p_wfTask.getRolePreference() == null ? ""
                : p_wfTask.getRolePreference();
        nodePara.setAttribute(WorkflowConstants.FIELD_ROLE_PREFERENCE,
                rolePreference);
    }

    private static void setPmForNode(ProcessDefinition p_processDefinition,
            WorkflowNodeParameter p_workflowNodeParameter)
    {
        Node exitNode = WorkflowJbpmUtil.getExitNode(p_processDefinition);
        WorkflowNodeParameter startPara = WorkflowNodeParameter.createInstance(
                exitNode).subNodeparameter(WorkflowConstants.FIELD_START_STATE);
        p_workflowNodeParameter.setAttribute(WorkflowConstants.FIELD_PM,
                startPara.getAttribute(WorkflowConstants.FIELD_PM));
    }

    private static void updateConditionNodeProperties(
            ProcessDefinition p_processDefiniton, Node p_existingNodeInst,
            WorkflowTaskInstance p_wfTask,
            WorkflowNodeParameter workflowNodeParameter) throws Exception
    {
        // validateConditionUda(p_pi);

        WorkflowConditionSpec wfcondSpec = p_wfTask.getConditionSpec();

        List wfBranchSpecs = wfcondSpec.getBranchSpecs();
        WorkflowNodeParameter nodeParameter = workflowNodeParameter == null ? WorkflowNodeParameter
                .createInstance(p_existingNodeInst) : workflowNodeParameter;

        nodeParameter.setAttribute(WorkflowConstants.FIELD_SEQUENCE,
                String.valueOf(p_wfTask.getSequence()));

        /* clear all the workflow_branch_spec_? first */
        nodeParameter
                .removeElement(WorkflowConstants.FIELD_WORKFLOW_CONDITION_SPEC);

        /* create a new workflow_branch_spec */
        WorkflowNodeParameter condiPara = nodeParameter
                .subNodeparameter(WorkflowConstants.FIELD_WORKFLOW_CONDITION_SPEC);

        for (int i = 0; i < wfBranchSpecs.size(); i++)
        {
            WorkflowBranchSpec p_wfBranchSpec = (WorkflowBranchSpec) wfBranchSpecs
                    .get(i);

            WorkflowNodeParameter specPara = condiPara
                    .subNodeparameter(WorkflowConstants.FIELD_WORKFLOW_BRANCH_SPEC
                            + WorkflowConstants.NAME_SEPARATOR + i);
            if (p_wfBranchSpec.getStructuralState() != WorkflowConstants.REMOVED)
            {
                specPara.setAttribute(WorkflowConstants.FIELD_ARROW_LABEL,
                        p_wfBranchSpec.getArrowLabel());
                specPara.setAttribute(
                        WorkflowConstants.FIELD_COMPARISON_OPERATOR,
                        String.valueOf(p_wfBranchSpec.getComparisonOperator()));
                specPara.setAttribute(WorkflowConstants.FIELD_STRUCTUAL_STATE,
                        String.valueOf(-1));
                specPara.setAttribute(WorkflowConstants.FIELD_IS_DEFAULT,
                        String.valueOf(p_wfBranchSpec.isDefault()));
                specPara.setAttribute(WorkflowConstants.FIELD_BRANCH_VALUE,
                        p_wfBranchSpec.getValue());
            }

        }
        // condSpec.setEvalOrder(branchSpec);
        condiPara.setAttribute(WorkflowConstants.FIELD_CONDITION_ATTRIBUTE,
                WorkflowConstants.CONDITION_UDA);

        WorkflowJbpmUtil.setConfigure(p_existingNodeInst,
                nodeParameter.restore());

    }

    /**
     * Return a list of Activity Node Instance objects following the p_node.
     * This list is needed for email notificaiton of the next possible active
     * tasks.
     */
    private static NextNodes validActivityNodes(WorkflowTaskInstance p_node,
            NextNodes p_nextNodes, String p_arrowLabel)
    {

        if (p_node.getType() == WorkflowConstants.CONDITION)
        {
            Vector aarrows = p_node.getOutgoingArrows();
            for (Enumeration ee = aarrows.elements(); ee.hasMoreElements();)
            {
                WorkflowArrow aarrow = (WorkflowArrow) ee.nextElement();
                if (p_node.getConditionSpec().getBranchSpec(aarrow.getName())
                        .isDefault())
                {
                    WorkflowTaskInstance task = (WorkflowTaskInstance) aarrow
                            .getTargetNode();
                    if (task.getType() == WorkflowConstants.STOP)
                    {
                        break;
                    }
                    addToActiveNodeList(task, p_nextNodes, p_arrowLabel);
                    break;
                }
            }
        }
        else
        {

            Vector workflowArrows = p_node.getOutgoingArrows();

            for (Enumeration e = workflowArrows.elements(); e.hasMoreElements();)
            {
                WorkflowTaskInstance task = (WorkflowTaskInstance) ((WorkflowArrow) e
                        .nextElement()).getTargetNode();

                addToActiveNodeList(task, p_nextNodes, p_arrowLabel);
            }
        }

        return p_nextNodes;
    }

    /**
     * Add arrows to the node.
     * 
     * @param p_wfTask
     * @param p_node
     * @param p_processDefinition
     * @throws Exception
     */
    private static void addArrows(WorkflowTaskInstance p_wfTask, Node p_node,
            ProcessDefinition p_processDefinition) throws Exception
    {
        if (p_wfTask.getStructuralState() != WorkflowConstants.REMOVED)
        {
            Vector p_OutgoingArrows = p_wfTask.getOutgoingArrows();
            int size = p_OutgoingArrows == null ? 0 : p_OutgoingArrows.size();

            for (int i = 0; i < size; i++)
            {
                WorkflowArrowInstance p_OutgoingArrow = (WorkflowArrowInstance) p_OutgoingArrows
                        .get(i);
                if (p_OutgoingArrow.getStructuralState() != WorkflowConstants.REMOVED)
                {

                    Node targetNode = WorkflowJbpmUtil.getNodeByWfTask(
                            p_processDefinition,
                            p_OutgoingArrow.getTargetNode());

                    addTransition(p_node, targetNode, p_OutgoingArrow.getName());
                }
            }
        }
    }

    /**
     * Adds the transition to the source node and the target node.
     * 
     * @param p_sourceNode
     *            source node.
     * @param p_targetNode
     *            target node.
     * @param name
     *            the name of the transition.
     */
    private static void addTransition(Node p_sourceNode, Node p_targetNode,
            String name)
    {
        if (!isTransitionExist(p_sourceNode, p_targetNode))
        {

            Transition transition = new Transition(name);
            transition.setFrom(p_sourceNode);
            transition.setTo(p_targetNode);
            transition.setProcessDefinition(p_targetNode.getProcessDefinition());
            
            p_sourceNode.addLeavingTransition(transition);
            p_targetNode.addArrivingTransition(transition);

        }

    }

    /**
     * Judges whether the transition between the source node and the target node
     * has existed.
     * 
     * @param p_sourceNode
     *            The source node.
     * @param p_targetNode
     *            The target node.
     * @return whether exist.
     */
    private static boolean isTransitionExist(Node p_sourceNode,
            Node p_targetNode)
    {

        boolean isExist = false;

        List trans = p_sourceNode.getLeavingTransitions();

        if (trans == null)
        {
            return isExist;
        }

        for (Iterator it = trans.iterator(); it.hasNext();)
        {
            Transition t = (Transition) it.next();
            if (t.getTo().equals(p_targetNode))
            {
                isExist = true;
                break;
            }
        }

        return isExist;

    }

    /**
     * Converts the JBPM's Node object to WorkflowTaskInstance.
     */
    private WorkflowTaskInstance workflowTaskInstance(Node p_node,
            ProcessInstance p_pi) throws Exception
    {
        if (p_node == null || p_pi == null)
        {
            return null;
        }

        int nodeType = WorkflowJbpmUtil.getNodeType(p_node);

        // Only get the activity node.
        if (nodeType != WorkflowConstants.ACTIVITY)
        {
            return null;
        }

        WorkflowNodeParameter workflowNodeParameter = WorkflowNodeParameter
                .createInstance(WorkflowJbpmUtil.getConfigure(p_node));

        // Create the WorkflowTaskInstances first
        WorkflowTaskInstance wfTaskInst = getBasicWft(p_pi,
                workflowNodeParameter, p_node, nodeType);

        processConditionNode(p_node, wfTaskInst);

        // add the properties
        addPropertiesToTaskInstance(wfTaskInst, workflowNodeParameter);

        return wfTaskInst;
    }

    /**
     * Processes the workflow's arrow when the target node is condition node.
     * 
     * @param p_node
     *            The node.
     * @param wfTaskInstance
     *            WorkflowTaskInstance.
     */
    private void processConditionNode(Node p_node,
            WorkflowTaskInstance wfTaskInstance)
    {

        List leavingTrans = p_node.getLeavingTransitions();

        boolean hasConditionNode = false;

        for (Iterator it = leavingTrans.iterator(); it.hasNext();)
        {
            Transition transition = (Transition) it.next();
            Node toNode = transition.getTo();

            hasConditionNode = WorkflowJbpmUtil.isConditionNode(toNode);

            if (hasConditionNode)
            {

                /* if the target node is condition node */
                List cLeavingTrans = toNode.getLeavingTransitions();
                Vector<ConditionNodeTargetInfo> conditionNodeTargetInfos = new Vector<ConditionNodeTargetInfo>(
                        cLeavingTrans.size());
                for (Iterator cit = cLeavingTrans.iterator(); cit.hasNext();)
                {
                    Transition cl = (Transition) cit.next();
                    Node cToNode = cl.getTo();
                    conditionNodeTargetInfos.add(new ConditionNodeTargetInfo(cl
                            .getName(), WorkflowJbpmUtil
                            .getActivityName(cToNode)));
                }

                wfTaskInstance
                        .setConditionNodeTargetInfos(conditionNodeTargetInfos);

            }

        }
    }

    /**
     * Gets the <code>WorkflowTaskInstance</code> with the basic informatin.
     * 
     * @param p_pi
     *            ProcessInstance.
     * @param workflowNodeParameter
     *            {@code WorkflowNodeParameter}.
     * @param p_node
     *            {@code Node}.
     * @param nodeType
     *            The type of the node.
     * @return {@code WorkflowTaskInstance}
     */
    private WorkflowTaskInstance getBasicWft(ProcessInstance p_pi,
            WorkflowNodeParameter workflowNodeParameter, Node p_node,
            int nodeType)
    {
        WorkflowTaskInstance wfTaskInst = new WorkflowTaskInstance(
                WorkflowJbpmUtil.getActivityName(p_node), nodeType);

        wfTaskInst.setTaskId(p_node.getId());
        wfTaskInst.setDesc(workflowNodeParameter
                .getAttribute(WorkflowConstants.FIELD_SEQUENCE));
        wfTaskInst.setPosition(workflowNodeParameter
                .getPointAttribute(WorkflowConstants.FIELD_POINT));
        wfTaskInst.setTaskState(WorkflowJbpmUtil.getTaskState(p_pi, p_node));
        wfTaskInst.setNodeName(p_node.getName());

        return wfTaskInst;
    }
}
