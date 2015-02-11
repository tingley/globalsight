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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * WorkflowInstance class is basically a wrapper for iflow's ProcessInstance. It
 * can be instantiated by passing iflow's processInstance information as
 * paramters to the constructor.
 * 
 */

public class WorkflowInstance implements Serializable
{
	
	private static final long serialVersionUID = -8949875886503748611L;

	// wf instance id
	private long m_wfInstanceId = -1;

	// wf instance name
	private String m_wfInstanceName = null;

	// wf instance description
	private String m_wfInstanceDescription = null;

	// wf instance tasks
	private Vector<WorkflowTaskInstance> m_wfInstanceTasks = null;

	// max sequence num (used for user defined attributes)
	private int m_maxSequence = 0;

	// A map of workflow task instances with id as the key
	private Map<Long,WorkflowTaskInstance> m_taskMap = null;

	/*
     * The defaultPathNode store the sorted nodes. It will improve the
     * efficience and can be used to set the state of the node
     */
	private List defaultPathNode = null;

	// /////////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// /////////////////////////////////////////////////////////////////////////
	/**
     * Default Constructor (used from UI during process creation).
     */
	public WorkflowInstance()
	{
		m_wfInstanceTasks = new Vector<WorkflowTaskInstance>();
	}

	/**
     * WorkflowInstance constructor.
     * 
     * @param p_wfInstanceId -
     *            The id of the workflow instance.
     * @param p_wfInstanceName -
     *            The name of the workflow instance.
     * @param p_wfInstanceDescription -
     *            The description of the workflow instance.
     * @param p_wfInstanceTasks -
     *            The tasks of the workflow instance.
     */
	public WorkflowInstance(long p_wfInstanceId, String p_wfInstanceName,
			String p_wfInstanceDescription, Vector<WorkflowTaskInstance> p_wfInstanceTasks)
	{
		m_wfInstanceId = p_wfInstanceId;
		m_wfInstanceName = p_wfInstanceName;
		m_wfInstanceDescription = p_wfInstanceDescription;
		m_wfInstanceTasks = p_wfInstanceTasks;
	}

	// ////////////////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////////////////
	// Begin: Helper Methods
	// ////////////////////////////////////////////////////////////////////////////////

	/**
     * Create a new workflow task instance and add it to this workflow.
     * 
     * @param p_taskName -
     *            The name of the task.
     * @param p_taskType -
     *            The type of the task (i.e. condition, activity, etc.).
     */
	public WorkflowTaskInstance addWorkflowTaskInstance(String p_taskName,
			int p_taskType)
	{
		WorkflowTaskInstance m_workflowTaskInstance = new WorkflowTaskInstance(
				p_taskName, p_taskType);
		if (p_taskType == WorkflowConstants.CONDITION)
		{
			WorkflowConditionSpec m_workflowConditionSpec = new WorkflowConditionSpec();
			m_workflowTaskInstance.setConditionSpec(m_workflowConditionSpec);
		}
		m_wfInstanceTasks.add(m_workflowTaskInstance);

		return m_workflowTaskInstance;
	}

	/**
     * Add the specified workflow task instance to the list of task within this
     * workflow.
     * 
     * @param p_wfTaskInstance -
     *            The task to be added.
     */
	public void addWorkflowTaskInstance(WorkflowTaskInstance p_wfTaskInstance)
	{
		m_wfInstanceTasks.add(p_wfTaskInstance);
	}

	/**
     * Add an arrow instance as a connection between two nodes.
     * 
     * @param p_arrowName -
     *            The name of the arrow.
     * @param p_arrowType -
     *            The type of the arrow.
     * @param p_sourceNode -
     *            The node arrow coming from.
     * @param p_targetNode -
     *            The node arrow pointing to.
     */
	public WorkflowArrowInstance addArrowInstance(String p_arrowName,
			long p_arrowType, WorkflowTask p_sourceNode,
			WorkflowTask p_targetNode)
    {
        WorkflowArrowInstance workflowArrowInstance = new WorkflowArrowInstance(
                p_arrowName, p_arrowType, p_sourceNode, p_targetNode);

        boolean isDefault = true;
        if (p_sourceNode.getType() == WorkflowConstants.CONDITION)
        {
            isDefault = p_sourceNode.getConditionSpec().addCondBranchSpecInfo(
                    p_arrowName, 0, "0", true);
        }
        workflowArrowInstance.setDefault(isDefault);

        p_sourceNode.addOutgoingArrow(workflowArrowInstance);
        p_targetNode.addIncomingArrow(workflowArrowInstance);

        return workflowArrowInstance;
    }

	/**
     * Remove the specified arrow instance.
     * 
     * @param p_wfArrowInstance -
     *            The arrow instance to be removed.
     */
	public void removeWorkflowArrowInstance(
			WorkflowArrowInstance p_wfArrowInstance)
	{
		p_wfArrowInstance.getSourceNode()
				.removeOutgoingArrow(p_wfArrowInstance);
		p_wfArrowInstance.getTargetNode().removeIncommingArrow(
				p_wfArrowInstance);
	}

	/**
     * Remove the task instance from this workflow.
     * 
     * @param p_wfTaskInstance -
     *            The task instance to be removed.
     */
	public void removeWorkflowTaskInstance(WorkflowTaskInstance p_wfTaskInstance)
	{
		int index = m_wfInstanceTasks.indexOf(p_wfTaskInstance);
		if (index > -1)
		{
			WorkflowTaskInstance wft = (WorkflowTaskInstance) m_wfInstanceTasks
					.get(index);
			if (wft.getTaskId() == WorkflowTaskInstance.ID_UNSET)
			{
				m_wfInstanceTasks.remove(index);
			}
			else
			{
				wft.setStructuralState(WorkflowConstants.REMOVED);
			}
		}
	}

	/**
     * Get the template's id.
     * 
     * @return The template's id.
     */
	public long getId()
	{
		return m_wfInstanceId;
	}

	/**
     * Get the template's name.
     * 
     * @return The name of this template.
     */
	public String getName()
	{
		return m_wfInstanceName;
	}

	/**
     * Get the template's description.
     * 
     * @return The description of this template.
     */
	public String getDescription()
	{
		return m_wfInstanceDescription;
	}

	/**
     * Get the maximum sequence number. The sequence number is used for defining
     * a unique user defined attribute in i-Flow.
     * 
     * @return The maximum sequence number.
     */
	public int getMaxSequence()
	{
		return m_maxSequence;
	}

	/**
     * Get a list of workflow instance tasks.
     * 
     * @return A list of workflow instance tasks.
     */
	public Vector<WorkflowTaskInstance> getWorkflowInstanceTasks()
	{
		return m_wfInstanceTasks;
	}

	/**
     * Reassing a task to the specified role.
     * 
     * @param p_taskId -
     *            The id of the reassigned task.
     * @param p_roles -
     *            The role to be set.
     */
	public void reassignTask(long p_taskId, String[] p_roles)
	{
		// loop thru current tasks and update the task
		int size = m_wfInstanceTasks.size();

		for (int i = 0; i < size; i++)
		{
			WorkflowTaskInstance wft = (WorkflowTaskInstance) m_wfInstanceTasks
					.get(i);

			if (wft.getTaskId() == p_taskId)
			{
				wft.setRoles(p_roles);
				return; // return as soon as the role is set.
			}
		}
	}

	
	/**
     * Get a workflow task instance object by its id. This method should only be
     * used when a workflow is retrieved. If the tasks are updated this map
     * would not be updated.
     */
	public WorkflowTaskInstance getWorkflowTaskById(long p_taskId)
	{
		int size = m_wfInstanceTasks.size();

		if (m_taskMap == null || m_taskMap.size() != size)
		{
			m_taskMap = new HashMap<Long,WorkflowTaskInstance>(size);
			for (int i = 0; i < size; i++)
			{
				WorkflowTaskInstance wft = (WorkflowTaskInstance) m_wfInstanceTasks
						.get(i);
				m_taskMap.put(new Long(wft.getTaskId()), wft);
			}
		}

		return (WorkflowTaskInstance) m_taskMap.get(new Long(p_taskId));
	}

	/**
     * Set the maximum sequence number within this workflow.
     * 
     * @param p_maxsequence -
     *            The max sequence number to be set.
     */
	public void setMaxSequence(int p_maxsequence)
	{
		m_maxSequence = p_maxsequence;
	}

	/**
     * Set the durations for a task to be the specified value.
     * 
     * @param p_taskId -
     *            The id of the task which the timer definitions should be set.
     * @param p_timerDefs -
     *            A list of timer definitions for a task.
     */
	public void setWorkflowTaskDurations(long p_taskId, Vector p_timerDefs)
	{
		int size = m_wfInstanceTasks.size();

		for (int i = 0; i < size; i++)
		{
			WorkflowTaskInstance wft = (WorkflowTaskInstance) m_wfInstanceTasks
					.get(i);
			if (wft.getTaskId() == p_taskId)
			{
				wft.setTimerDefinitions(p_timerDefs);
				return; // return as soon as the instance is removed.
			}
		}
	}

	/**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
	public String toString()
	{
		return super.toString()
				+ " m_wfInstanceId="
				+ Long.toString(m_wfInstanceId)
				+ " m_wfInstanceName="
				+ (m_wfInstanceName != null ? m_wfInstanceName : "null")
				+ " m_wfInstanceDescription="
				+ (m_wfInstanceDescription != null ? m_wfInstanceDescription
						: "null") + " start m_wfInstanceTasks="
				+ WorkflowHelper.toDebugString(m_wfInstanceTasks)
				+ "\nend m_wfInstanceTasks";
	}

	// ////////////////////////////////////////////////////////////////////////
	// End: Public Methods
	// ////////////////////////////////////////////////////////////////////////

	/**
     * Set the id of this workflow instance to be the specified id.
     * 
     * @param p_wfinstanceId -
     *            The id to be set.
     */
	void setId(long p_wfinstanceId)
	{
		m_wfInstanceId = p_wfinstanceId;
	}

	/**
     * Set the workflow instance's name.
     * 
     * @param The
     *            name of this workflow instance.
     */
	void setName(String p_wfInstanceName)
	{
		m_wfInstanceName = p_wfInstanceName;
	}

	/**
     * Set the workflow instance tasks to be the specified tasks.
     * 
     * @param p_workflowInstanceTasks -
     *            The tasks to be set.
     */
	void setWorkflowInstanceTasks(Vector<WorkflowTaskInstance> p_workflowInstanceTasks)
	{
		m_wfInstanceTasks = p_workflowInstanceTasks;
	}

	/**
     * Get the workflow instance's description.
     * 
     * @param The
     *            description of this workflow instance.
     */
	void setDescription(String p_wfInstanceDescription)
	{
		m_wfInstanceDescription = p_wfInstanceDescription;
	}

	public List getDefaultPathNode()
	{
		return defaultPathNode;
	}

	public void setDefaultPathNode(List defaultPathNode)
	{
		this.defaultPathNode = defaultPathNode;
	}
}
