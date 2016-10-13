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

// globalsight
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.workflowmanager.ArrorInfo;

/**
 * This class is used to find all the nodes in the default path of a process
 * instance. The nodes are returned in a list and are not in order - since the
 * workflow is not necessarily linear.
 * 
 * A "default" process impl path includes all activity nodes within an AND/OR
 * path and all the activity nodes within the default branch of a Conditional
 * node. If the process implementation is running then it looks at the
 * conditional branches that have been chosen over the default ones.
 */
public class ProcessImplDefaultPathFinder
{
    // PRIVATE STATIC VARIABLES
    private static final Logger s_logger = Logger
            .getLogger(ProcessImplDefaultPathFinder.class.getName());

    // -----------PACKAGE METHODS--------------------------

    /**
     * Return a list of all ACTIVITY Node Instances that are in the default path
     * of the specified workflow.
     * <p>
     * Note: there is possibly a loop in the default path which never goes to
     * the exit of the workflow. To fix this problem, for this case, stop the
     * list at the nearest Exit node.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @param p_startNodeId
     *            - The id of the node considered as the starting node in the
     *            path. If it's less than zero, we'll start from the START node.
     * @param p_destinationArrow
     *            - The outgoing arrow name for a condition node which
     *            determines the next node.
     * @param p_nodes
     *            - The nodes of the workflow.
     */
    static List<Object> activityNodesInDefaultPath2(long p_workflowInstanceId,
            long p_startNodeId, ArrorInfo p_destinationArrow,
            WorkflowTaskInstance[] p_nodes) throws WorkflowException,
            RemoteException
    {
        List<Object> nodesInPath = new ArrayList<Object>();
        try
        {
            WorkflowTaskInstance startNode = p_startNodeId > 0 ? findStartNode(
                    p_startNodeId, p_nodes) : findStartNode(p_nodes);

            // calls method to process the next node(s) in the path.
            // this is called recursively and moves down the path
            // adding to the array list of nodes in the path.
            nextNodeInDefaultPath2(startNode, p_destinationArrow, nodesInPath);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Error occurred when finding the default path of workflow "
                            + p_workflowInstanceId, e);
            String args[] =
            { Long.toString(p_workflowInstanceId) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_FIND_DEFAULT_PATH, args, e);

        }
        return nodesInPath;
    }

    /**
     * Return a list of all ACTIVITY Node Instances that are in the default path
     * of the specified workflow.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @param p_startNodeId
     *            - The id of the node considered as the starting node in the
     *            path. If it's less than zero, we'll start from the START node.
     * @param p_destinationArrow
     *            - The outgoing arrow name for a condition node which
     *            determines the next node.
     * @param p_nodes
     *            - The nodes of the workflow.
     */
    static List activityNodesInDefaultPath(long p_workflowInstanceId,
            long p_startNodeId, ArrorInfo p_destinationArrow,
            WorkflowTaskInstance[] p_nodes) throws WorkflowException,
            RemoteException
    {
        List nodesInPath = new ArrayList();// WorkflowTaskInstance list
        try
        {
            WorkflowTaskInstance startNode = p_startNodeId > 0 ? findStartNode(
                    p_startNodeId, p_nodes) : findStartNode(p_nodes);

            // calls method to process the next node(s) in the path.
            // this is called recursively and moves down the path
            // adding to the array list of nodes in the path.
            nextNodeInDefaultPath(startNode, p_destinationArrow, nodesInPath);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Error occurred when finding the default path of workflow "
                            + p_workflowInstanceId, e);
            String args[] =
            { Long.toString(p_workflowInstanceId) };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_FIND_DEFAULT_PATH, args, e);

        }
        return nodesInPath;
    }

    /**
     * Returns the node in this workflow instance which would be considered as a
     * start point for the rest of the default path.
     * 
     * @param p_nodes
     *            All the nodes of a particular process instance.
     * @return The node considered a starting point in the workflow's default
     *         path. This node is not necessarily the very first node of the
     *         path.
     */
    static WorkflowTaskInstance findStartNode(long p_startNodeId,
            WorkflowTaskInstance[] p_nodes)
    {
        WorkflowTaskInstance startNode = null;
        // loop through till the start node is found
        for (int i = 0; (startNode == null) && i < p_nodes.length; i++)
        {
            if (p_nodes[i].getTaskId() == p_startNodeId)
            {
                startNode = p_nodes[i];
            }
        }
        return startNode;
    }

    /**
     * Returns the start node in this workflow instance.
     * 
     * @param p_nodes
     *            All the nodes of a particular process instance.
     * @return The start node.
     */
    static WorkflowTaskInstance findStartNode(WorkflowTaskInstance[] p_nodes)
    {
        WorkflowTaskInstance startNode = null;
        // loop through till the start node is found
        for (int i = 0; (startNode == null) && i < p_nodes.length; i++)
        {
            if (p_nodes[i].getType() == WorkflowConstants.START)
            {
                startNode = p_nodes[i];
            }
        }
        return startNode;
    }

    // -----------------PRIVATE METHODS----------------------------------

    /**
     * @see nextNodeInDefaultPath(WorkflowTaskInstance, ArrorInfo, List).
     */
    private static void nextNodeInDefaultPath2(WorkflowTaskInstance p_ni,
            ArrorInfo p_destinationArrow, List<Object> p_nodesInPath)
            throws WorkflowException
    {
        Vector arrows = p_ni.getOutgoingArrows();

        WorkflowTaskInstance currentNode = null;
        // go through all the branches under the node passed in

        for (Enumeration e = arrows.elements(); e.hasMoreElements();)
        {
            WorkflowArrowInstance ai = (WorkflowArrowInstance) e.nextElement();
            currentNode = (WorkflowTaskInstance) ai.getTargetNode();
            if (currentNode.isExit())
            {
                p_nodesInPath.add(ai);
                return;
            }

            WorkflowTaskInstance ni = evaluateNode2(currentNode,
                    p_destinationArrow, p_nodesInPath);

            if (ni != null) // not at the end so keep searching down
            {
                // recursively
                nextNodeInDefaultPath2(ni, p_destinationArrow, p_nodesInPath);
            }

        }

    }

    /**
     * @see evaluateNode(WorkflowTaskInstance, ArrorInfo, List).
     */
    private static WorkflowTaskInstance evaluateNode2(
            WorkflowTaskInstance p_ni, ArrorInfo p_arrowInfo,
            List<Object> p_nodesInPath) throws WorkflowException
    {
        WorkflowTaskInstance returnNode = null;

        switch (p_ni.getType())
        {
            case WorkflowConstants.STOP:
                break;
            case WorkflowConstants.ACTIVITY:
                if (!alreadyInPath(p_nodesInPath, p_ni))
                {
                    p_nodesInPath.add(p_ni);
                    returnNode = p_ni;
                }
                break;
            case WorkflowConstants.AND:
            case WorkflowConstants.OR:
                returnNode = p_ni;
                break;
            case WorkflowConstants.CONDITION:
                String destinationArrow = null;
                if (p_arrowInfo != null)
                {
                    Vector<WorkflowArrowInstance> arrows = p_ni
                            .getIncomingArrows();
                    for (WorkflowArrowInstance arrow : arrows)
                    {
                        if (arrow.getSourceNode().getTaskId() == p_arrowInfo
                                .getSourceId())
                        {
                            destinationArrow = p_arrowInfo.getArrorName();
                            break;
                        }
                    }
                }
                Object o = handleConditionNode2(p_ni, destinationArrow);
                if (o instanceof WorkflowArrow)
                {
                    // the arrow going to Exit node
                    p_nodesInPath.add(o);
                }
                else if (o instanceof WorkflowTaskInstance)
                {
                    // recursively handle the default or chosen node
                    returnNode = evaluateNode2((WorkflowTaskInstance) o,
                            p_arrowInfo, p_nodesInPath);
                }
                break;
            default:
                // log error message and keep going
                s_logger.error("The node type " + p_ni.getType()
                        + " is not supported.");
                break;
        }

        return returnNode;
    }

    /**
     * @see handleConditionNode(WorkflowTaskInstance, String)
     */
    private static Object handleConditionNode2(WorkflowTaskInstance p_cni,
            String p_destinationArrow) throws WorkflowException
    {
        return getDefaultOne2(p_cni);
    }

    /**
     * @see getDefaultOne(WorkflowTaskInstance).
     */
    private static Object getDefaultOne2(WorkflowTaskInstance p_task)
    {
        Vector arrows = p_task.getOutgoingArrows();
        Enumeration e = arrows.elements();
        WorkflowTaskInstance defaultTask = null;
        while (e.hasMoreElements())
        {
            WorkflowArrow arrow = (WorkflowArrow) e.nextElement();
            WorkflowTaskInstance targetTask = (WorkflowTaskInstance) arrow
                    .getTargetNode();
            if (targetTask.isExit())
            {
                return arrow;
            }
            if (p_task.getConditionSpec().getBranchSpec(arrow.getName())
                    .isDefault())
            {
                defaultTask = targetTask;
            }
        }

        return defaultTask;
    }

    /**
     * Go through the next node(s) that follow the one passed in.
     * 
     * @param p_ni
     *            The node instance that has been processed and its target
     *            node(s) should be processed too.
     * @param p_destinationArrow
     *            - The outgoing arrow name for a condition node which
     *            determines the next node.
     * @param p_nodesInPath
     *            This is a return variable that will contain all the activity
     *            nodes in the default path from the one passed in.
     * @return The param p_nodesInPath holds return value.
     */
    @SuppressWarnings("unchecked")
    private static void nextNodeInDefaultPath(WorkflowTaskInstance p_ni,
            ArrorInfo p_destinationArrow, List p_nodesInPath)
            throws WorkflowException
    {
        Vector arrows = p_ni.getOutgoingArrows();

        WorkflowTaskInstance currentNode = null;
        // go through all the branches under the node passed in

        for (Enumeration e = arrows.elements(); e.hasMoreElements();)
        {

            WorkflowArrowInstance ai = (WorkflowArrowInstance) e.nextElement();
            currentNode = (WorkflowTaskInstance) ai.getTargetNode();

            WorkflowTaskInstance ni = evaluateNode(currentNode,
                    p_destinationArrow, p_nodesInPath);

            if (ni != null) // not at the end so keep searching down
            {
                // recursively
                nextNodeInDefaultPath(ni, p_destinationArrow, p_nodesInPath);
            }

        }

    }

    /**
     * Evaluate the node to determine if it is in the default path. If it is and
     * it is an activity node then add to the array list passed in. If
     * conditional must determine what path to proceed on and evaluate the next
     * node.
     * 
     * @param p_ni
     *            The node to evaluate as part of the path.
     * @param p_destinationArrow
     *            - The outgoing arrow name for a condition node which
     *            determines the next node.
     * @param p_nodesInPath
     *            The array list to add to if it is to be part of the path.
     *            (return value too)
     * @return NodeInstance Returns the node instance that has been evaluated.
     *         May be the same as the one that came in OR may be one lower (for
     *         conditional) or may be NULL if an EXIT node or the node couldn't
     *         be evaluated. This node returned should be used to look at the
     *         NEXT target node instances from it (if not NULL).
     * 
     */
    private static WorkflowTaskInstance evaluateNode(WorkflowTaskInstance p_ni,
            ArrorInfo p_arrowInfo, List<WorkflowTaskInstance> p_nodesInPath)
            throws WorkflowException
    {
        WorkflowTaskInstance returnNode = null;

        switch (p_ni.getType())
        {
            case WorkflowConstants.STOP:
                // leave the return node null - at end of the path
                break;
            case WorkflowConstants.ACTIVITY:
                // check that this node isn't already in the array
                // of nodes already in the path.
                // if so...
                // We don't allow infinite looping - just take the loop once
                // and stop.
                // or if paths converge - only allow the node once
                if (!alreadyInPath(p_nodesInPath, p_ni))
                {
                    p_nodesInPath.add(p_ni);
                    returnNode = p_ni;
                }
                // else - leave the return node null
                break;
            // treated the same since all path(s) could have been taken in the
            // OR
            // case - just like ANDs
            case WorkflowConstants.AND:
            case WorkflowConstants.OR:
                returnNode = p_ni;
                break;
            case WorkflowConstants.CONDITION:
                // find the default way or if the node is active or already been
                // processed find
                // the one that has been chosen and procced (only go down one
                // way)

                String destinationArrow = null;
                if (p_arrowInfo != null)
                {
                    Vector<WorkflowArrowInstance> arrows = p_ni
                            .getIncomingArrows();
                    for (WorkflowArrowInstance arrow : arrows)
                    {
                        if (arrow.getSourceNode().getTaskId() == p_arrowInfo
                                .getSourceId())
                        {
                            destinationArrow = p_arrowInfo.getArrorName();
                            break;
                        }
                    }
                }
                p_ni = handleConditionNode(p_ni, destinationArrow);
                // recursively handle the default or chosen node
                returnNode = evaluateNode(p_ni, p_arrowInfo, p_nodesInPath);
                break;
            default:
                // log error message and keep going
                s_logger.error("The node type " + p_ni.getType()
                        + " is not supported.");
                break;
        }
        return returnNode;
    }

    /**
     * This method was created because the ArrayList.contains() method wasn't
     * working correctly anymore. It returned false and the recursion ran
     * rampant. Couldn't figure out why it was failing....it worked in System4
     * 4.4.7, but failed in 5.1
     * 
     * Replaced it with this method that tested to see if the node is already in
     * the list and returns "true" if it is.
     */
    private static boolean alreadyInPath(List p_nodesInPath,
            WorkflowTaskInstance p_ni)
    {
        boolean found = false;

        Iterator it = p_nodesInPath.iterator();

        while (it.hasNext())
        {
            WorkflowTaskInstance node = (WorkflowTaskInstance) it.next();
            if (node.equals(p_ni))
            {
                found = true;
                break;
            }
        }

        return found;

    }

    /**
     * Find the path to follow from the condition node and return it. Either
     * returns the default node or the node that has been chosen if this
     * workflow instance is running and passed this condition node.
     * 
     * @param p_cni
     *            - The condition node.
     * @param p_destinationArrow
     *            - The outgoing arrow name for a condition node which
     *            determines the next node.
     */
    private static WorkflowTaskInstance handleConditionNode(
            WorkflowTaskInstance p_cni, String p_destinationArrow)
            throws WorkflowException
    {
        WorkflowTaskInstance chosenNode = null;

        // This condition applies when the destination node following
        // the given condition node is determined.
        if (p_destinationArrow != null)
        {
            chosenNode = WorkflowProcessAdapter.targetNodeInstanceByArrowLabel(
                    p_cni, p_destinationArrow);
        }
        // if the conditional node has already been encountered and
        // a choice made.
        else if (p_cni.getTaskState() == WorkflowConstants.STATE_COMPLETED)
        {
            // look through the ones it points to and finds the one that was
            // chosen

            chosenNode = getDefaultOneByState(p_cni);

        }

        if (chosenNode == null) // pick the default one
        {

            chosenNode = getDefaultOne(p_cni);
        }

        return chosenNode;
    }

    /**
     * Gets the default one by the state of the arrow and the taskinstance.
     * 
     * @param p_task
     * @return
     */
    private static WorkflowTaskInstance getDefaultOneByState(
            WorkflowTaskInstance p_task)
    {
        Vector arrows = p_task.getOutgoingArrows();
        Enumeration e = arrows.elements();
        while (e.hasMoreElements())
        {
            WorkflowArrowInstance arrow = (WorkflowArrowInstance) e
                    .nextElement();
            if (arrow.getState() != WorkflowConstants.STATE_INITIAL
                    && ((WorkflowTaskInstance) (arrow.getTargetNode()))
                            .getTaskState() != WorkflowConstants.STATE_INITIAL)
            {
                return ((WorkflowTaskInstance) (arrow.getTargetNode()));
            }
        }

        return null;
    }

    /**
     * Gets the default one by BranchSpec.
     * 
     * @param p_task
     * @return
     */
    public static WorkflowTaskInstance getDefaultOne(WorkflowTaskInstance p_task)
    {
        Vector arrows = p_task.getOutgoingArrows();
        Enumeration e = arrows.elements();
        WorkflowTaskInstance targetTask = null;
        while (e.hasMoreElements())
        {
            WorkflowArrow arrow = (WorkflowArrow) e.nextElement();
            targetTask = (WorkflowTaskInstance) arrow
                    .getTargetNode();
            if (p_task.getConditionSpec().getBranchSpec(arrow.getName())
                    .isDefault())
            {
                return targetTask;
            }
        }

        return targetTask;
    }

    /**
     * Gets the default one by BranchSpec.
     * 
     * @param p_task
     * @return
     */
    public static String getDefaultArrowName(WorkflowTaskInstance p_task)
    {
        Vector arrows = p_task.getOutgoingArrows();
        Enumeration e = arrows.elements();
        while (e.hasMoreElements())
        {
            WorkflowArrow arrow = (WorkflowArrow) e.nextElement();
            if (p_task.getConditionSpec().getBranchSpec(arrow.getName())
                    .isDefault())
            {
                return arrow.getName();
            }
        }

        return null;
    }
}
