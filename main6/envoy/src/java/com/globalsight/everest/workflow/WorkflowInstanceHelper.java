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

// Core Java classes
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;

import com.globalsight.everest.taskmanager.TaskPersistenceAccessor;
import com.globalsight.log.GlobalSightCategory;

/**
 * WorkflowInstanceHelper is a helper class responsible for perfoming some
 * complex actions on a workflow instance.
 * <p>
 */

public class WorkflowInstanceHelper extends WorkflowHelper
{

	private static final Logger c_category = Logger
			.getLogger(WorkflowInstanceHelper.class.getName());

	private static String s_iFlowRmiPort = WorkflowConstants.PORT;

	// a constant representing an active node
	static final String ACTIVE_NODE = "activeNode";

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	/**
     * WorkflowInstanceHelper Constructor.
     */
	public WorkflowInstanceHelper()
	{
		super();
	}

	// ////////////////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////////////////
	// Begin: Helper Methods
	// ////////////////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////////
	// End: Helper Methods
	// ////////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////////
	// Begin: Local Methods
	// ///////////////////////////////////////////////////////////////////////

	/**
     * Sort a list of WorkflowTaskInstance by its sequence number
     * <p>
     * Pre-condition: The list of WorkflowTaskInstance's are sequentially
     * ordered by its sequence number. The sequence number starts with 0.
     * <p>
     * 
     * @param List
     *            A list of WorkflowTaskInstance's
     * @return WorkflowTaskInstance[] A sorted array of WorkflowTaskInstance's
     */
	@SuppressWarnings("unchecked")
	public static WorkflowTaskInstance[] sortWorkflowTaskInstanceBySeq(
			List p_wftis)
	{
		Set treeSet = new TreeSet(new WorkflowTaskComparator());
		treeSet.addAll(p_wftis);
		int wftiCnt = treeSet.size();
		WorkflowTaskInstance[] wftis = new WorkflowTaskInstance[wftiCnt];
		Iterator iter = treeSet.iterator();
		int index = 0;
		while (iter.hasNext())
		{
			WorkflowTaskInstance aTask = (WorkflowTaskInstance) iter.next();
			wftis[index] = aTask;
			index++;
		}
		return wftis;
	}

	public static String getArrowName()
	{
		return WorkflowConstants.ARROW_NAME;
		// TomyD -- I commented out this code since iFlow needs
		// the arrow name + : + a generated integer as a combination
		// of workitem eventtype which is stored in the history table.
		// The arrow name was about 29 chars and adding ":+an integer" was
		// making it to be longer than 30 chars. Therefore, a truncated value
		// was stored in db.
		/*
         * int maxArrowNameLength = 30; // can't add an arrow with an existing
         * name String name = WorkflowConstants.ARROW_NAME + "_-" +
         * Long.toString(ARROW_NAME_INDEX++) + "_" + getCurrentDate(); if
         * (name.length() > maxArrowNameLength) { name = name.substring(0,
         * maxArrowNameLength); } return name;
         */
	}

	/**
     * Get Tasks (not WorkflowTask or WorkflowTaskInstance, but Task) for a
     * WorkflowInstance
     */
	@SuppressWarnings("unchecked")
	public static List getTasksForWorkflowInstance(
			WorkflowInstance p_workflowInstance) throws Exception
	{
		List workflowInstanceTasks = p_workflowInstance
				.getWorkflowInstanceTasks();
		Vector<Long> taskIds = new Vector<Long>(workflowInstanceTasks.size());
		for (int i = 0; i < workflowInstanceTasks.size(); i++)
		{
			taskIds.add(new Long(((WorkflowTaskInstance) workflowInstanceTasks
					.get(i)).getTaskId()));
		}
		Collection tasks = TaskPersistenceAccessor.getTasks(taskIds);
		return (tasks == null ? new ArrayList(0) : new ArrayList(tasks));
	}

	// Get the workflow instance for the specified instance id.
	public static ProcessInstance getProcessInstance(long p_workflowInstanceId)
			throws WorkflowException
	{
		ProcessInstance pi = null;
		JbpmContext context = null;
		try
		{
			context = WorkflowConfiguration.getInstance().getJbpmContext();
			pi = context.getProcessInstance(p_workflowInstanceId);
		}
		// WFServerException, WFInternalException, ModelInternalException
		catch (Exception se)
		{
			c_category.error("getProcessInstance: " + se.toString()
					+ GlobalSightCategory.getLineContinuation()
					+ " p_workflowInstanceId="
					+ Long.toString(p_workflowInstanceId), se);
			String args[] = { String.valueOf(p_workflowInstanceId) };
			throw new WorkflowException(
					WorkflowException.MSG_FAILED_TO_GET_WF_INSTANCE, args, se);
		}
		finally
		{
			context.close();
		}

		return pi;
	}

	static Properties getLoginProperties(String p_serverHost)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(WorkflowConstants.PROTOCOL);
		sb.append(p_serverHost);
		sb.append(/* WorkflowConstants.PORT */s_iFlowRmiPort);

		Properties props = new Properties();
		props.put(WorkflowConstants.TRANSPORT_TYPE, WorkflowConstants.RMI);
		props.put(WorkflowConstants.NAMING_PROVIDER, sb.toString());

		return props;
	}

	static String getNamingContext(String p_serverHost)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(WorkflowConstants.NAMING_CONTEXT_PREFIX);
		sb.append(p_serverHost);
		sb.append(WorkflowConstants.NAMING_CONTEXT_SUFFIX);

		return sb.toString();
	}

	/*
     * private static void setIFlowServer() throws Exception {
     * SystemConfiguration sc = SystemConfiguration.getInstance(); String
     * serverHost = sc.getStringParameter(WorkflowConstants.HOST); // iflow
     * server c_iFlowServer = serverHost + WorkflowConstants.FLOW; }
     */

}
