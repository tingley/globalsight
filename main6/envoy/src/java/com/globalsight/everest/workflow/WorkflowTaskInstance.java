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
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import com.globalsight.everest.foundation.Timestamp;

/**
 * WorkflowTaskInstance is a subclass of WorkflowTask and contain the
 * information about the task of a workflow instance.
 * 
 * Note: Since this class need to be exposed to Java grid applet which runs
 * within JDK 1.1, all collection classes are choosen from JDK 1.1.
 */
public class WorkflowTaskInstance extends WorkflowTask implements Serializable
{
    private static final long serialVersionUID = -7048278139866486606L;

    private static final long MILLIS_PER_SEC = 1000;

    private long m_taskInstanceId = ID_UNSET;

    private String m_initiator = null;

    private int m_wfTaskInstanceState = -1;

    private long m_creationTime = -1;

    private Vector<EnvoyWorkItem> m_workItems = null;

    private boolean m_isFirstTime = false;

    private String m_assignees = null;

    private Vector<String> m_allAssignees = null;

    private Vector m_conditionNodeTargetInfos = null;

    private String[] m_initialRoles = null;

    /* the user accept the task */
    private String m_acceptUser = null;

    /**
     * Constructor used in UI for creating a new Task Instance.
     * 
     * @param p_name
     *            - The default name of the taks instance. Used for
     *            non_ActivityNode.
     * @param p_taskType
     *            - The type of this task.
     */
    public WorkflowTaskInstance(String p_name, int p_taskType)
    {
        super(p_name, p_taskType);
        initialize();
    }

    /**
     * WorkflowTaskInstance constructor. Used when info is populated from UI for
     * new a new task.
     * 
     * @param p_roles
     *            The name user or container roles.
     * @param p_roleType
     *            this specifies the type of the role. If the role is a userRole
     *            or containerRole. True for user.
     * @param p_activity
     *            the activity used for getting the task name.
     * @param p_taskType
     *            the type of this workflow task.
     * @param p_sequence
     *            the sequence of this task.
     * @param p_timerDefs
     *            a vector of TimerDefinition objects.
     */
    public WorkflowTaskInstance(String[] p_roles, boolean p_roleType,
            Activity p_activity, int p_taskType, int p_sequence,
            Vector p_timerDefs, int p_wfTaskInstanceState)
    {
        this(ID_UNSET, p_roles, p_roleType, ID_UNSET, p_activity, p_taskType,
                p_sequence, p_timerDefs, p_wfTaskInstanceState);
    }

    /**
     * WorkflowTaskInstance constructor. Used when info is populated from UI for
     * new a new task.
     * 
     * @param p_roles
     *            The name user or container roles.
     * @param p_roleType
     *            this specifies the type of the role. If the role is a userRole
     *            or containerRole. True for user.
     * @param p_activity
     *            the activity used for getting the task name.
     * @param p_taskType
     *            the type of this workflow task.
     * @param p_sequence
     *            the sequence of this task.
     * @param p_timerDefs
     *            a vector of TimerDefinition objects.
     * @param p_wfTaskInstanceState
     * @param p_expenseRateId
     *            The id of the expense rate this workflow instance is
     *            associated with.
     * @param p_revenueRateId
     *            The id of the revenue rate this workflow instance is
     *            associated with.
     * @param p_rateSelectionCriteria
     *            The Rate Selection Criteria for expense rate
     */
    public WorkflowTaskInstance(String[] p_roles, boolean p_roleType,
            Activity p_activity, int p_taskType, int p_sequence,
            Vector p_timerDefs, int p_wfTaskInstanceState,
            long p_expenseRateId, long p_revenueRateId,
            int p_rateSelectionCriteria)
    {
        this(ID_UNSET, p_roles, p_roleType, ID_UNSET, p_activity, p_taskType,
                p_sequence, p_timerDefs, p_wfTaskInstanceState,
                p_expenseRateId, p_revenueRateId, p_rateSelectionCriteria);
    }

    /**
     * WorkflowTaskInstance constructor used when task info is populated from
     * iflow server.
     * 
     * @param p_taskInstanceId
     *            the id of the task instance.
     * @param p_roles
     *            The name user or container roles.
     * @param p_roleType
     *            this specifies the type of the role. If the role is a userRole
     *            or containerRole. True for user.
     * @param p_taskId
     *            the id of the template's task.
     * @param p_activity
     *            the activity used for getting the task name.
     * @param p_taskType
     *            the type of this workflow task.
     * @param p_sequence
     *            the sequence of this task.
     * @param p_timerDefs
     *            a vector of TimerDefinition objects.
     */
    public WorkflowTaskInstance(long p_taskInstanceId, String[] p_roles,
            boolean p_roleType, long p_taskId, Activity p_activity,
            int p_taskType, int p_sequence, Vector p_timerDefs,
            int p_wfTaskInstanceState)
    {
        this(p_taskInstanceId, p_roles, p_roleType, p_taskId, p_activity,
                p_taskType, p_sequence, p_timerDefs, p_wfTaskInstanceState,
                NO_RATE, NO_RATE, WorkflowConstants.USE_ONLY_SELECTED_RATE);
    }

    /**
     * WorkflowTaskInstance constructor used when task info is populated from
     * iflow server.
     * 
     * @param p_taskInstanceId
     *            the id of the task instance.
     * @param p_roles
     *            The name user or container roles.
     * @param p_roleType
     *            this specifies the type of the role. If the role is a userRole
     *            or containerRole. True for user.
     * @param p_taskId
     *            the id of the template's task.
     * @param p_activity
     *            the activity used for getting the task name.
     * @param p_taskType
     *            the type of this workflow task.
     * @param p_sequence
     *            the sequence of this task.
     * @param p_timerDefs
     *            a vector of TimerDefinition objects.
     * @param p_expenseRateId
     *            The id of the expense rate this workflow instance is
     *            associated with.
     * @param p_revenueRateId
     *            The id of the revenue rate this workflow instance is
     *            associated with.
     * @param p_rateSelectionCriteria
     *            The Rate Selection Criteria for expense rate
     */
    public WorkflowTaskInstance(long p_taskInstanceId, String[] p_roles,
            boolean p_roleType, long p_taskId, Activity p_activity,
            int p_taskType, int p_sequence, Vector p_timerDefs,
            int p_wfTaskInstanceState, long p_expenseRateId,
            long p_revenueRateId, int p_rateSelectionCriteria)
    {
        super(p_taskId, p_activity, p_taskType, p_sequence, p_timerDefs,
                p_roles, p_roleType, p_expenseRateId, p_revenueRateId,
                p_rateSelectionCriteria);
        m_taskInstanceId = p_taskInstanceId;
        m_wfTaskInstanceState = p_wfTaskInstanceState;
        m_initialRoles = p_roles;
        initialize();
    }

    /**
     * Get the id of the task (a task belongs to a template).
     * 
     * @return The task's id.
     */
    public long getTaskId()
    {
        return m_taskInstanceId;
    }

    /**
     * Get a collection of all work items associated with this task instance.
     * 
     * @return A vector of work item objects as EnvoyWorkItem.
     */
    public Vector getWorkItems()
    {
        return m_workItems;
    }

    /**
     * Get the initiator of the process to which this work item belongs.
     * 
     * @return The initiator of the process.
     */
    public String getInitiator()
    {
        return m_initiator;
    }

    /**
     * Get the creation time of the work item.
     * 
     * @return The creation time of the work item.
     */
    public long getCreationTime()
    {
        return m_creationTime;
    }

    /**
     * Get the state of this task.
     * 
     * @return the state of this task.
     */
    public int getTaskState()
    {
        return m_wfTaskInstanceState;
    }

    /**
     * Get the state of a task that has started. This is basically the state of
     * iFlow's WorkItem which is a task assigned to an individual.
     * 
     * @return The state of a task assigned to an individual.
     */
    public int getTaskStateForAssignee()
    {
        return determineState();
    }

    /**
     * Returns the username that accepted the task. Returns null if the task has
     * not been accepted yet.
     */
    public String getAccepter()
    {
        String user = null;
        if (m_workItems.size() > 0)
        {
            for (int i = 0; i < m_workItems.size(); i++)
            {
                EnvoyWorkItem item = (EnvoyWorkItem) m_workItems.elementAt(i);
                if (item.getWorkItemState() == WorkflowConstants.TASK_ACCEPTED)
                {
                    user = item.getAssignee();
                    break;
                }
            }
        }

        return user;
    }

    /**
     * Get the date by which the task should be accepted.
     * 
     * @return The date by which a task should be accepted.
     */
    public Timestamp getToBeAcceptedDate()
    {
        return getConvertedTime(getAcceptTime(), null);
    }

    /**
     * Get the date by which the task should be completed. This is actually the
     * sum of the accept time and complete time, since the task cannot be
     * started until it has been accepted.
     * 
     * @return The date by which a task should be completed.
     */
    public Timestamp getDueDate(Date p_baseDate)
    {
        long completionDuration = p_baseDate == null ? getCompletedTime()
                + getAcceptTime() : getCompletedTime();
        return getConvertedTime(completionDuration, p_baseDate);
    }

    /**
     * Get a list of all assignees.
     * 
     * @return A list of assignees.
     */
    public Vector<String> getAllAssignees()
    {
        return m_allAssignees;
    }

    /**
     * Get a list of assignees separated by comma.
     * 
     * @return A list of assignees as one string.
     */
    public String getAllAssigneesAsString()
    {
        if (m_assignees == null)
        {
            // int sz = m_workItems.size();
            int sz = m_allAssignees.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sz; i++)
            {
                sb.append(m_allAssignees.elementAt(i));
                if (i < sz - 1)
                {
                    sb.append("<BR>");
                }
            }
            m_assignees = sb.toString();
        }
        return m_assignees;
    }

    /**
     * Get the list of target node info for a condition node ONLY.
     * 
     * @return A list of ConditionNodeTargetInfo objects for a condition node.
     *         Otherwise, returns null.
     */
    public Vector getConditionNodeTargetInfos()
    {
        return m_conditionNodeTargetInfos;
    }

    /**
     * Return whether or not the task has been reassigned.
     * 
     * @return True if it has been reassigned.
     */
    public boolean isReassigned()
    {

        return m_initialRoles != null
                && !areSameRoles(m_initialRoles, getRoles());
    }

    public boolean isAccepted()
    {
        return m_acceptUser != null;
    }

    /**
     * Return a string representation of the object appropriate for logging.
     * 
     * @return a string representation of the object appropriate for logging.
     */
    public String toDebugString()
    {
        StringBuilder sb = new StringBuilder(super.toDebugString());
        sb.append(", m_taskInstanceId=");
        sb.append(m_taskInstanceId);
        sb.append(", m_initiator=");
        sb.append(m_initiator);
        sb.append(", m_wfTaskInstanceState=");
        sb.append(WorkflowHelper
                .toDebugStringWorkflowTaskInstanceState(m_wfTaskInstanceState));
        sb.append(", getTaskStateForAssignee=");
        sb.append(WorkflowHelper
                .toDebugStringWorkflowTaskInstanceState(getTaskStateForAssignee()));
        sb.append(", m_creationTime=");
        sb.append(m_creationTime);
        sb.append(", m_workItems=[");
        sb.append(m_workItems);
        sb.append("], m_isFirstTime=");
        sb.append(m_isFirstTime);
        return sb.toString();
    }

    /**
     * Set the role of this task to be the specified value.
     * 
     * @param p_roles
     *            - The roles to be set.
     */
    @Override
    public void setRoles(String[] p_roles)
    {
        if (m_initialRoles == null)
        {
            m_initialRoles = p_roles;
        }
        super.setRoles(p_roles);
    }

    /**
     * Set the required iflow's WorkItem attributes. A WorkItem represents the
     * association of a particular workflow participant(s) (or members of a
     * role) with a workflow task instance.
     */
    void setWorkItemAttributes(String p_workItemName, String p_initiator,
            String p_assignees, long p_creationTime, int p_workItemState)
    {

        m_initiator = p_initiator;
        m_creationTime = p_creationTime;
        String[] assignees = p_assignees.split(",");
        for (int i = 0; i < assignees.length; i++)
        {
            String assignee = assignees[i];
            if (!m_allAssignees.contains(assignee))
            {
                m_allAssignees.addElement(assignee);
                m_workItems.addElement(new EnvoyWorkItem(p_workItemName,
                        assignee, p_workItemState));
            }
        }
    }

    /**
     * set the task id during the NodeInstance to WorkflowTaskInstance
     * conversion.
     */
    void setTaskId(long p_taskId)
    {
        m_taskInstanceId = p_taskId;
    }

    /**
     * Set the task state
     */
    void setTaskState(int p_wfTaskInstanceState)
    {
        m_wfTaskInstanceState = p_wfTaskInstanceState;
    }

    /**
     * set the flag that determines whether one of the possible target nodes of
     * this one is a condition node.
     * 
     * @param p_conditionNodeTargetInfos
     */
    void setConditionNodeTargetInfos(Vector p_conditionNodeTargetInfos)
    {
        m_conditionNodeTargetInfos = p_conditionNodeTargetInfos;
    }

    /**
     * Determines whether the two array of roles contain the same set of role
     * names.
     */
    private boolean areSameRoles(String[] p_workflowRoles,
            String[] p_selectedRoles)
    {
        // First need to sort since Arrays.equals() requires
        // the parameters to be sorted
        Arrays.sort(p_workflowRoles);
        Arrays.sort(p_selectedRoles);
        return Arrays.equals(p_workflowRoles, p_selectedRoles);
    }

    /**
     * determine the state of the task (work item vs. node instance).
     */
    private int determineState()
    {
        int size = m_workItems.size();
        int workItemState = m_wfTaskInstanceState;

        // since work items are filtered based on their states, get the state of
        // the first one.
        if (size > 0)
        {
            workItemState = m_workItems.elementAt(0).getWorkItemState();
        }

        return workItemState;
    }

    /**
     * Convert the created date from milli seconds to a Timestamp object and
     * then add the relative date to it.
     * 
     * @param p_time
     * @param p_baseDate
     * @return
     */
    private Timestamp getConvertedTime(long p_time, Date p_baseDate)
    {
        Timestamp ts = new Timestamp(Timestamp.DATE);
        ts.setDate(p_baseDate == null ? new Date(m_creationTime) : p_baseDate);
        ts.add(Timestamp.SECOND, new Long(p_time / MILLIS_PER_SEC).intValue());
        return ts;
    }

    /**
     * Initialize local variables
     */
    private void initialize()
    {
        m_isFirstTime = true;
        m_workItems = new Vector<EnvoyWorkItem>();
        m_allAssignees = new Vector<String>();
    }

    public String getAcceptUser()
    {
        return m_acceptUser;
    }

    public void setAcceptUser(String acceptUser)
    {
        m_acceptUser = acceptUser;
    }
}
