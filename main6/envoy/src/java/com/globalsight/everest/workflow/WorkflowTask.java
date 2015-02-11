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

import java.awt.Point;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import com.globalsight.util.DateUtil;

/**
 * WorkflowTask contains information about a task (node) of a workflow. A
 * WorkflowTask is initially created through it's constructor and provides
 * support for reading it's attributes. This object has a couple constructors
 * which one of them would be used as the primary one for our use:
 * 
 * public WorkflowTask(Activity p_activity, int p_sequence, Vector p_timerDefs)
 * 
 * 
 * Note: Since this class need to be exposed to Java grid applet which runs
 * within JDK 1.1, all collection classes are choosen from JDK 1.1.
 */
public class WorkflowTask implements Serializable
{
    /**
	 * 
	 */
    private static final long serialVersionUID = 5667945350967193558L;
    //
    // PUBLIC CONSTANTS
    //
    public static final int ID_UNSET = -3;
    public static final long NO_RATE = -1;
    public static final String NO_ACTION = "lb_no_action";
    public static final String DEFAULT_ROLE_NAME = "All qualified users";

    //
    // PRIVATE CONSTANTS
    //
    private static final String[] TIMER_NAMES =
    { WorkflowConstants.ACCEPT, WorkflowConstants.COMPLETE };

    //
    // PRIVATE MEMBER VARIABLES
    //
    private Activity m_activity = null;
    private int m_taskType = -1;
    private long m_taskId = ID_UNSET;
    private int m_sequence = -9;
    private Vector m_timerDefs = null;
    private String[] m_roles = null;
    private String m_rolesAsString = null;
    private String m_rolePreference = null;
    private boolean m_roleType = false;
    private long m_expenseRateId = NO_RATE; // invalid - no rate specified yet
    private long m_revenueRateId = NO_RATE; // invalid - no rate specified yet
    private int m_rateSelectionCriteria = WorkflowConstants.USE_ONLY_SELECTED_RATE;
    private Hashtable m_timers = null;
    private WorkflowConditionSpec m_conditionSpec = null;
    private WorkflowDataItem[] m_wfDataItems = null;
    private String m_epilogueScript = ""; // to avoid NullPointerException in
                                          // i-Flow
    private String m_prologueScript = "";// to avoid NullPointerException in
                                         // i-Flow
    private Vector<WorkflowArrow> m_incomingArrows = new Vector<WorkflowArrow>();
    private Vector<WorkflowArrow> m_outgoingArrows = new Vector<WorkflowArrow>();
    private int m_structuralState = -1;
    private Point m_position = null;
    private String m_name = null;
    private String m_nodeName = null;

    private String m_desc = "";
    private long m_acceptTime = -1;
    private long m_completedTime = -1;
    private long m_overduetoPM = 0;
    private long m_overduetoUser = 0;
    private String m_roleName = DEFAULT_ROLE_NAME;
    private String m_systemActionType = NO_ACTION;
    private int m_reportUploadCheck = 0;//0: Not check . 1: Check.

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructors
    // ////////////////////////////////////////////////////////////////////
    /**
     * General WorkflowTask constructor.
     * 
     * @param p_name
     *            The name of this workflow task.
     * @param p_taskType
     *            The type of this task.
     */
    public WorkflowTask(String p_name, int p_taskType)
    {
        m_name = p_name;
        m_taskType = p_taskType;
    }

    /**
     * WorkflowTask constructor used only when creating a new workflow template
     * from UI.
     * 
     * @param p_activity
     *            The activity used for getting the task name.
     * @param p_sequence
     *            The sequence of this task.
     * @param p_timerDefs
     *            A list of TimerDefinition objects.
     * @param p_roles
     *            The name of the user or container roles.
     * @param p_roleType
     *            This specifies the type of the role. If the role is a userRole
     *            or containerRole. True for userRole.
     */
    public WorkflowTask(Activity p_activity, int p_sequence,
            Vector p_timerDefs, String[] p_roles, boolean p_roleType)
    {
        this(-1, p_activity, WorkflowConstants.ACTIVITY, p_sequence,
                p_timerDefs, p_roles, p_roleType, NO_RATE, NO_RATE,
                WorkflowConstants.USE_ONLY_SELECTED_RATE);
    }

    /**
     * WorkflowTask constructor used only when creating a new workflow template
     * from UI.
     * 
     * @param p_activity
     *            The activity used for getting the task name.
     * @param p_sequence
     *            The sequence of this task.
     * @param p_timerDefs
     *            A list of TimerDefinition objects.
     * @param p_roles
     *            The name user or container roles.
     * @param p_roleType
     *            This specifies the type of the role. If the role is a userRole
     *            or containerRole. True for userRole.
     * @param p_expenseRateId
     *            The id of the expense rate to associate with this workflow.
     * @param p_revenueRateId
     *            The id of the revenue rate to associate with this workflow.
     * @param p_rateSelectionCriteria
     *            The selection criteria for the rate
     */
    public WorkflowTask(Activity p_activity, int p_sequence,
            Vector p_timerDefs, String[] p_roles, boolean p_roleType,
            long p_expenseRateId, long p_revenueRateId,
            int p_rateSelectionCriteria)
    {
        this(-1, p_activity, WorkflowConstants.ACTIVITY, p_sequence,
                p_timerDefs, p_roles, p_roleType, p_expenseRateId,
                p_revenueRateId, p_rateSelectionCriteria);
    }

    /**
     * WorkflowTask constructor used only when creating a workflow template.
     * 
     * @param p_taskId
     *            The id of the template's task.
     * @param p_activity
     *            The activity used for getting the task name.
     * @param p_sequence
     *            The sequence of this task.
     * @param p_timerDefs
     *            A list of TimerDefinition objects.
     * @param p_roles
     *            The name user or container roles.
     * @param p_roleType
     *            This specifies the type of the role. If the role is a userRole
     *            or containerRole. True for user.
     */
    public WorkflowTask(long p_taskId, Activity p_activity, int p_sequence,
            Vector p_timerDefs, String[] p_roles, boolean p_roleType)
    {
        this(p_taskId, p_activity, WorkflowConstants.ACTIVITY, p_sequence,
                p_timerDefs, p_roles, p_roleType, NO_RATE, NO_RATE,
                WorkflowConstants.USE_ONLY_SELECTED_RATE);
    }

    /**
     * General WorkflowTask constructor.
     * 
     * @param p_taskId
     *            The id of the template's task.
     * @param p_activity
     *            The activity used for getting the task name.
     * @param p_taskType
     *            The type of this workflow task.
     * @param p_sequence
     *            The sequence of this task.
     * @param p_timerDefs
     *            A list of TimerDefinition objects.
     * @param p_roles
     *            The name user or container roles.
     * @param p_roleType
     *            This specifies the type of the role. If the role is a userRole
     *            or containerRole. True for user.
     */
    public WorkflowTask(long p_taskId, Activity p_activity, int p_taskType,
            int p_sequence, Vector p_timerDefs, String[] p_roles,
            boolean p_roleType)
    {
        this(p_taskId, p_activity, p_taskType, p_sequence, p_timerDefs,
                p_roles, p_roleType, NO_RATE, NO_RATE,
                WorkflowConstants.USE_ONLY_SELECTED_RATE);
    }

    /**
     * General WorkflowTask constructor.
     * 
     * @param p_taskId
     *            The id of the template's task.
     * @param p_activity
     *            The activity used for getting the task name.
     * @param p_taskType
     *            The type of this workflow task.
     * @param p_sequence
     *            The sequence of this task.
     * @param p_timerDefs
     *            A list of TimerDefinition objects.
     * @param p_roles
     *            The name user or container roles.
     * @param p_roleType
     *            This specifies the type of the role. If the role is a userRole
     *            or containerRole. True for user.
     * @param p_expenseRateId
     *            The id of the expense rate this task is associated with. Could
     *            be NO_RATE if a rate hasn't been specified.
     * @param p_revenueRateId
     *            The id of the revenue rate this task is associated with. Could
     *            be NO_RATE if a rate hasn't been specified.
     * @param p_rateSelectionCriteria
     *            The selection criteria for expense rate
     */
    public WorkflowTask(long p_taskId, Activity p_activity, int p_taskType,
            int p_sequence, Vector p_timerDefs, String[] p_roles,
            boolean p_roleType, long p_expenseRateId, long p_revenueRateId,
            int p_rateSelectionCriteria)
    {
        m_taskId = p_taskId;
        m_activity = p_activity;
        m_taskType = p_taskType;
        m_sequence = p_sequence;
        m_timerDefs = p_timerDefs;
        m_roles = p_roles;
        m_roleType = p_roleType;
        m_rateSelectionCriteria = p_rateSelectionCriteria;
        m_expenseRateId = p_expenseRateId;
        m_revenueRateId = p_revenueRateId;
        m_timers = new Hashtable();
        setDates();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructors
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Helper Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Add the arrow as an incoming arrow for this task.
     * 
     * @param p_arrow
     *            The incoming arrow to be added.
     */
    public void addIncomingArrow(WorkflowArrow p_arrow)
    {
        m_incomingArrows.add(p_arrow);
    }

    /**
     * Add the arrow as an outgoing arrow for this task.
     * 
     * @param p_arrow
     *            The outgoing arrow to be added.
     */
    public void addOutgoingArrow(WorkflowArrow p_arrow)
    {
        m_outgoingArrows.add(p_arrow);
    }

    /**
     * Get the relative time by which the task should be accepted.
     * 
     * @return The period by which a task should be accepted (in ms).
     */
    public long getAcceptTime()
    {
        // TomyD -- to avoid breaking some existing code,
        // need to call namedTimeValue
        return m_acceptTime > -1 ? m_acceptTime
                : namedTimeValue(WorkflowConstants.ACCEPT);
    }

    /**
     * Get the syste action type for this task instance. Note that the action
     * type will be used as a key to the localization property files. A null
     * action type means that there's no system action associated with this
     * task.
     * 
     * @return The system action type associated with this task.
     */
    public String getActionType()
    {
        return m_systemActionType;
    }

    /**
     * Get the activity object of this task.
     * 
     * @return The activity object.
     */
    public Activity getActivity()
    {
        return m_activity;
    }

    /**
     * Get the activity name of this task.
     * 
     * @return The activity name.
     */
    public String getActivityName()
    {
        return m_taskType == WorkflowConstants.ACTIVITY ? (m_activity != null ? m_activity
                .getActivityName() : null)
                : m_name;
    }

    /**
     * Get the activity name of this task.
     * 
     * @return The activity name.
     */
    public String getActivityDisplayName()
    {
        return m_taskType == WorkflowConstants.ACTIVITY ? (m_activity != null ? m_activity
                .getDisplayName() : null)
                : m_name;
    }

    /**
     * Get the relative time by which the task should be completed.
     * 
     * @return The period by which a task should be completed (in ms).
     */
    public long getCompletedTime()
    {
        // TomyD -- to avoid breaking existing code, need to call namedTimeValue
        return m_completedTime > -1 ? m_completedTime
                : namedTimeValue(WorkflowConstants.COMPLETE);
    }

    /**
     * Gets the duration from accepted to completed.
     * 
     * @return the duration from accepted to completed.
     */
    public String getDurationString()
    {
        return DateUtil.formatDuration(getDuration());
    }

    /**
     * Gets the duration in long format. Note: will return a long number larger
     * than 0.
     * 
     * @return the duration
     */
    public long getDuration()
    {
        long duration = m_completedTime - m_acceptTime;
        if (m_acceptTime > -1 && m_completedTime > -1 && duration > 0)
        {
            return duration;
        }
        return 0;
    }

    /**
     * Get the condition spec for this not (only for conditional node)
     * 
     * @return The condition spec for this task.
     */
    public WorkflowConditionSpec getConditionSpec()
    {
        return m_conditionSpec;
    }

    /**
     * Get an array of workflow data items.
     * 
     * @return An array of workflow data items.
     */
    public WorkflowDataItem[] getDataItemRefs()
    {
        return m_wfDataItems;
    }

    /**
     * Get the description of this task.
     * 
     * @return The description of the task.
     */
    public String getDesc()
    {
        return m_desc;
    }

    /**
     * Get the display role name.
     * 
     * @return The displayable role name.
     */
    public String getDisplayRoleName()
    {
        return m_roleName;
    }

    /**
     * Get the epilogue script for this task.
     * 
     * @return The task's epilogue script.
     */
    public String getEpilogueScript()
    {
        return m_epilogueScript;
    }

    /**
     * Get the form name used for this task.
     * 
     * @return The form name.
     */
    public String getFormName()
    {
        return WorkflowConstants.ROLE;
    }

    /**
     * Get the form path used for this task. The path will be a user role if the
     * value of m_roleType is true. Otherwise, it's a container role.
     * 
     * @return The form path.
     */
    public String getFormPath()
    {
        return getRoleType() ? WorkflowConstants.USER_ROLE
                : WorkflowConstants.CONTAINER_ROLE;
    }

    /**
     * Get a list of incoming arrows.
     * 
     * @return A list of incoming arrows.
     */
    public Vector getIncomingArrows()
    {
        return m_incomingArrows;
    }

    /**
     * Get a list of outgoing arrows.
     * 
     * @return A list of outgoing arrows.
     */
    public Vector getOutgoingArrows()
    {
        return m_outgoingArrows;
    }

    /**
     * Get the position of this task for displaying purposes.
     * 
     * @return The task's display position.
     */
    public Point getPosition()
    {
        return m_position;
    }

    /**
     * Get the prologue script for this task.
     * 
     * @return The task's prologue script.
     */
    public String getPrologueScript()
    {
        return m_prologueScript;
    }

    /**
     * Get the roles of the task.
     * 
     * @return The task's current roles.
     */
    public String[] getRoles()
    {
        return m_roles;
    }

    /**
     * Get a comma-delimited string of roles.
     */
    public String getRolesAsString()
    {
        if (m_rolesAsString == null)
        {
            int size = m_roles == null ? 0 : m_roles.length;
            StringBuilder sb = new StringBuilder();

            if (size > 0)
            {
                sb.append(m_roles[0]);
            }
            for (int i = 1; i < size; i++)
            {
                sb.append(",");
                sb.append(m_roles[i]);
            }

            m_rolesAsString = sb.toString();
        }
        return m_rolesAsString;
    }

    /**
     * Get the role preference of the task. The preference is associated with a
     * container role (i.e. fastest resources, available resources)
     * 
     * @return The task's current role preference.
     */
    public String getRolePreference()
    {
        return m_rolePreference;
    }

    /**
     * Get the role or user type of the role for this task.
     * 
     * @return The roleType. True if the role type is userRole. False if the
     *         role type is containerRole.
     */
    public boolean getRoleType()
    {
        return m_roleType;
    }

    /**
     * Set the id of the expense rate this task is assigned to.
     * 
     * @param p_rateId
     *            - The id of the rate it is assigned to or NO_RATE if it isn't
     *            assigned to a rate.
     */
    public void setExpenseRateId(long p_rateId)
    {
        m_expenseRateId = p_rateId;
    }

    /**
     * Get the id of the expense rate that is assigned to this task.
     * 
     * @return The id of the rate or NO_RATE if none has been specified.
     */
    public long getExpenseRateId()
    {
        return m_expenseRateId;
    }

    /**
     * Set the Rate Selection Criteria for the expense rate
     * 
     * @param p_criteria
     *            - Rate Selection Criteria
     */
    public void setRateSelectionCriteria(int p_criteria)
    {
        m_rateSelectionCriteria = p_criteria;
    }

    /**
     * Get the rate selection criteria for the expense rate of this task.
     * 
     * @return int rate selection criteria.
     */
    public int getRateSelectionCriteria()
    {
        return m_rateSelectionCriteria;
    }

    /**
     * Set the id of the revenue rate this task is assigned to.
     * 
     * @param p_rateId
     *            - The id of the rate it is assigned to or NO_RATE if it isn't
     *            assigned to a rate.
     */
    public void setRevenueRateId(long p_rateId)
    {
        m_revenueRateId = p_rateId;
    }

    /**
     * Get the id of the expense rate that is assigned to this task.
     * 
     * @return The id of the rate or NO_RATE if none has been specified.
     */
    public long getRevenueRateId()
    {
        return m_revenueRateId;
    }

    /**
     * Get the sequence number of this task (used for ordering the template
     * tasks).
     * 
     * @return The sequence of the task.
     */
    public int getSequence()
    {
        return m_sequence;
    }

    /**
     * Get the structural state of this task (i.e. new, deleted, edited, or
     * unchanged).
     * 
     * @return The structural state of this task.
     */
    public int getStructuralState()
    {
        return m_structuralState;
    }

    /**
     * Get the id of the task (a task belongs to a template).
     * 
     * @return The task's id.
     */
    public long getTaskId()
    {
        return m_taskId;
    }

    /**
     * Get the relative due date for each workflow task.
     * 
     * @return The duration for each task.
     */
    public Vector getTimerDefinitions()
    {
        return m_timerDefs;
    }

    /**
     * Get the workflow template's task id (used by the task instance object for
     * retreiving the task id of the template).
     * 
     * @return The task's id.
     */
    /*
     * public long getTemplateTaskId() { return m_taskId; }
     */

    /**
     * Get the task type (the types are all predefined within this class).
     * 
     * @return The task type.
     */
    public int getType()
    {
        return m_taskType;
    }

    /**
     * Check only those outgoing arrows whose structural edit state is not
     * removed
     */
    public boolean hasValidOutgoingArrows()
    {
        int size = m_outgoingArrows.size();
        for (int i = 0; i < size; i++)
        {
            WorkflowArrow p_Arrow = (WorkflowArrow) m_outgoingArrows
                    .elementAt(i);
            if (p_Arrow.getStructuralState() != WorkflowConstants.REMOVED)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the time value for the timer with the given name; -1 if none.
     * 
     * @param p_name
     *            the name of the timer to use.
     */
    public long namedTimeValue(String p_name)
    {
        long v = 0;
        String s = (String) m_timers.get(prefix(p_name));
        if (s != null)
        {
            try
            {
                v = Long.parseLong(s);
            }
            catch (NumberFormatException nfe)
            {
                // ignore
            }
        }
        return v;
    }

    /**
     * Set the structural state of the arrow to "Removed".
     * 
     * @param p_arrow
     *            - The arrow to be removed.
     */
    public void removeArrow(WorkflowArrow p_arrow)
    {
        int indexInOutGoing = m_outgoingArrows.indexOf(p_arrow);
        int index = indexInOutGoing > -1 ? indexInOutGoing : m_incomingArrows
                .indexOf(p_arrow);

        if (index > -1)
        {
            WorkflowArrow arrow = (WorkflowArrow) m_outgoingArrows.get(index);
            if (arrow.getArrowId() == -1)
            {
                m_outgoingArrows.remove(index);
            }
            else
            {
                arrow.setStructuralState(WorkflowConstants.REMOVED);
            }
        }
    }

    /**
     * Set the structural state of the arrow to "Removed".
     * 
     * @param p_arrow
     *            - The arrow to be removed.
     */
    // not using the removeArrow method .. later delete that method
    public void removeIncommingArrow(WorkflowArrow p_arrow)
    {
        int index = m_incomingArrows.indexOf(p_arrow);
        if (index > -1)
        {

            WorkflowArrow arrow = (WorkflowArrow) m_incomingArrows.get(index);
            if (arrow.getArrowId() == -1)
            {
                m_incomingArrows.remove(index);

            }
            else
            {
                arrow.setStructuralState(WorkflowConstants.REMOVED);
            }
        }
    }

    /**
     * Remove the arror by setting its structural state to "Removed".
     * 
     * @param p_arrow
     *            - The arrow to be removed.
     */
    public void removeOutgoingArrow(WorkflowArrow p_arrow)
    {
        int index = m_outgoingArrows.indexOf(p_arrow);
        if (index > -1)
        {

            WorkflowArrow arrow = (WorkflowArrow) m_outgoingArrows.get(index);
            long id = 0;
            // determine arrow id
            if (arrow instanceof WorkflowArrowInstance)
            {
                id = ((WorkflowArrowInstance) arrow).getArrowInstanceId();
            }
            else if (arrow instanceof WorkflowArrow)
            {
                id = arrow.getArrowId();
            }

            // If the arrow is new, remove it from the list. Otherwise, just
            // change the state to "Removed".
            if (id == -1)
            {
                m_outgoingArrows.remove(index);
            }
            else
            {
                arrow.setStructuralState(WorkflowConstants.REMOVED);
            }

            if (m_taskType == WorkflowConstants.CONDITION)
            {
                getConditionSpec().removeBranchSpec(arrow.getName(),
                        WorkflowConstants.REMOVED);
            }
        }
    }

    /**
     * Set the accepted time to be the specified value.
     * 
     * @param p_acceptedTime
     *            - The accepted time in milliseconds.
     */
    public void setAcceptedTime(long p_acceptedTime)
    {
        m_acceptTime = p_acceptedTime;
    }

    /**
     * Set the system action type of this task to be the specified value.
     * 
     * @param p_systemActionType
     *            - The system action type to be set.
     */
    public void setActionType(String p_systemActionType)
    {
        m_systemActionType = p_systemActionType;
    }

    /**
     * Set the activity to be the specified value.
     * 
     * @param p_activity
     *            - The activity to be set.
     */
    public void setActivity(Activity p_activity)
    {
        m_activity = p_activity;
    }

    /**
     * Set the completed time to be the specified value.
     * 
     * @param p_completedTime
     *            - The completed time in milliseconds.
     */
    public void setCompletedTime(long p_completedTime)
    {
        m_completedTime = p_completedTime;
    }

    /**
     * Set the condition spec for the conditional task to be this value.
     * 
     * @param p_conditionSpec
     *            - the condition spec to be set.
     */
    public void setConditionSpec(WorkflowConditionSpec p_conditionSpec)
    {
        m_conditionSpec = p_conditionSpec;
    }

    /**
     * Set the data items for this task.
     * 
     * @param p_wfDataItems
     *            - The data items to be set.
     */
    public void setDataItemRefs(WorkflowDataItem[] p_wfDataItems)
    {
        m_wfDataItems = p_wfDataItems;
    }

    /**
     * Set the description of the task to be the specified value.
     * 
     * @param p_desc
     *            - the description to be set.
     */
    public void setDesc(String p_desc)
    {
        m_desc = p_desc;
    }

    /**
     * Set the display role name to be the specified value. Note that this role
     * name will be stored in i-Flow as a user defined attribute of a workflow
     * (for a particular task).
     * 
     * @param p_roleName
     *            - The role name to be set.
     */
    public void setDisplayRoleName(String p_roleName)
    {
        m_roleName = p_roleName;
    }

    /**
     * Set the epilogue script for this task.
     * 
     * @param p_epilogueScript
     *            - The epilogue script to be set.
     */
    public void setEpilogueScript(String p_epilogueScript)
    {
        m_epilogueScript = p_epilogueScript;
    }

    /**
     * Set the name of a non-activity node (i.e. condition node, and etc.)
     * 
     * @param p_name
     *            - The name to be set.
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set the display position of this task.
     * 
     * @param p_postion
     *            - The position to be set.
     */
    public void setPosition(Point p_position)
    {
        m_position = p_position;
    }

    /**
     * Set the prologue script for this task.
     * 
     * @param p_prologueScript
     *            - The prologue script to be set.
     */
    public void setPrologueScript(String p_prologueScript)
    {
        m_prologueScript = p_prologueScript;
    }

    /**
     * Set the roles of this task to be the specified value.
     * 
     * @param p_roles
     *            - The roles to be set.
     */
    public void setRoles(String[] p_roles)
    {
        m_roles = p_roles;
        // reset the comma-delimited value of roles
        m_rolesAsString = null;
    }

    /**
     * Set the role preference of this task to be the specified value. This
     * preference is associated with a container role (all qualified users) and
     * can be either 'fastest resources' or 'available resources'.
     * 
     * @param p_rolePreference
     *            - The role preference to be set.
     */
    public void setRolePreference(String p_rolePreference)
    {
        m_rolePreference = p_rolePreference;
    }

    /**
     * Set the role or user type of the role for this task.
     * 
     * @param p_user
     *            - True if the role type is userRole. False if the role type is
     *            containerRole.
     */
    public void setRoleType(boolean p_roleType)
    {
        m_roleType = p_roleType;
    }

    /**
     * Set the sequence to be the specified value.
     * 
     * @param p_sequence
     *            - The sequence to be set.
     */
    public void setSequence(int p_sequence)
    {
        m_sequence = p_sequence;
    }

    /**
     * Set the structural state to be the specified value.
     * 
     * @param p_structuralState
     *            - The structural state to be set.
     */
    public void setStructuralState(int p_structuralState)
    {
        m_structuralState = p_structuralState;
    }

    /**
     * Set the timer definitions to be the specified value.
     * 
     * @param p_timerDefs
     *            - The timer definition to be set.
     */
    public void setTimerDefinitions(Vector p_timerDefs)
    {
        m_timerDefs = p_timerDefs;
        if (m_timerDefs != null)
        {
            setDates();
        }
    }

    /**
     * Set the activity type to be the specified value.
     * 
     * @param p_type
     *            - A valid type (i.e. Activity, And, Or,...).
     */
    public void setType(int p_type)
    {
        m_taskType = p_type;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Helper Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package Level Methods
    // ////////////////////////////////////////////////////////////////////
    // set the id for an existing task.
    void setTaskId(long p_taskId)
    {
        m_taskId = p_taskId;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Package Level Methods
    // ////////////////////////////////////////////////////////////////////

    //
    // PUBLIC OBJECT OVERRIDES
    //
    /**
     * @see java.lang.Comparator#compare
     */
    public int compareTo(Object p_obj)
    {
        WorkflowTask t = (WorkflowTask) p_obj;
        return (getSequence() == t.getSequence() ? 0 : (getSequence() < t
                .getSequence() ? -1 : 1));
    }

    /**
     * @see java.lang.Comparator#equals
     */
    public boolean equals(Object p_obj)
    {
        boolean isEqual = false;
        try
        {
            isEqual = m_taskType == WorkflowConstants.ACTIVITY ? (compareTo((WorkflowTask) p_obj) == 0)
                    : super.equals(p_obj);
        }
        catch (ClassCastException cce)
        {
            // ignore
        }
        return isEqual;
    }

    /**
     * Returns a string representation of the object (based on the object name).
     */
    public String toString()
    {
        return getActivityName();
    }

    /**
     * Return a string representation of the object appropriate for debugging.
     * 
     * @return a string representation of the object appropriate for debugging.
     */
    public String toDebugString()
    {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(", m_activity=");
        sb.append(m_activity);
        sb.append(", m_taskType=");
        sb.append(m_taskType);
        sb.append(", m_taskId=");
        sb.append(m_taskId);
        sb.append(", m_sequence=");
        sb.append(m_sequence);
        sb.append(", m_timerDefs=");
        sb.append(m_timerDefs);
        sb.append(", m_roles=");
        sb.append(getRolesAsString());
        sb.append(", m_acceptTime=");
        sb.append(getAcceptTime());
        sb.append(", m_completeTime=");
        sb.append(getCompletedTime());
        sb.append(", m_roleType=");
        sb.append(m_roleType);
        sb.append(", m_expenseRateId=");
        sb.append(m_expenseRateId);
        sb.append(", m_revenueRateId=");
        sb.append(m_revenueRateId);
        return sb.toString();
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* set the date values in milliseconds. */
    @SuppressWarnings("unchecked")
    private void setDates()
    {
        int size = m_timerDefs == null ? 0 : m_timerDefs.size();
        for (int j = 0; j < size; j++)
        {
            TimerDefinition td = (TimerDefinition) m_timerDefs.elementAt(j);
            if (isTimerName(prefix(td.getTimerPrefix())))
            {
                Vector items = td.getDataItemRefs();
                for (int i = 0; i < items.size(); i++)
                {
                    WorkflowDataItem di = (WorkflowDataItem) items.elementAt(i);
                    m_timers.put(prefix(di.getName()), di.getValue());
                }
            }
        }
    }

    /* Return true if the given name is the recognized name of a timer */
    private boolean isTimerName(String p_name)
    {
        boolean isTimerName = false;
        for (int i = 0; !isTimerName && i < TIMER_NAMES.length; i++)
        {
            isTimerName = (p_name.equals(TIMER_NAMES[i]));
        }
        return isTimerName;
    }

    private String prefix(String p_string)
    {
        String p = p_string;
        int index = p_string.indexOf("_");
        if (index > 0)
        {
            p = p_string.substring(0, index);
        }
        return p;
    }

    // check if node contains incoming arrow and outgoing arrows
    /**
     * This method returns true if node contains Incomming arrow and outgoing
     * arrows.
     */

    private boolean hasArrows()
    {
        // return(m_incomingArrows.size() > 0 ) && (m_outgoingArrows.size() > 0
        // );
        return (hasValidIncommingArrows()) && (hasValidOutgoingArrows());
    }

    /**
     * This method returns true if node is valid.
     */
    public boolean isValid()
    {
        int type = getType();

        if (type == WorkflowConstants.START)
            return hasValidOutgoingArrows();

        else if (type == WorkflowConstants.STOP)
            return hasValidIncommingArrows();

        else if (type == WorkflowConstants.CONDITION)
            return ((getConditionSpec().getBranchSpecs().size() > 0) && (hasArrows()));

        else if (type == WorkflowConstants.OR || type == WorkflowConstants.AND)
            return ((getActivityName().length() > 0) && (hasArrows()));

        else if (type == WorkflowConstants.ACTIVITY)
            return ((m_acceptTime > -1) && (m_completedTime > -1))
                    && (hasArrows());
        else
            return false;
    }

    /**
     * Checks if it is the Exit node.
     */
    public boolean isExit()
    {
        return m_taskType == WorkflowConstants.STOP;
    }

    // check only those incomming arrows whose structural edit state is not
    // removed
    private boolean hasValidIncommingArrows()
    {
        int size = m_incomingArrows.size();
        for (int i = 0; i < size; i++)
        {
            WorkflowArrow p_Arrow = (WorkflowArrow) m_incomingArrows
                    .elementAt(i);
            if (p_Arrow.getStructuralState() != WorkflowConstants.REMOVED)
            {
                return true;
            }
        }
        return false;
    }

    public String getNodeName()
    {
        return m_nodeName;
    }

    public String getName()
    {
        return m_name;
    }

    public void setNodeName(String p_nodeName)
    {
        m_nodeName = p_nodeName;
    }

    public long getOverdueToPM()
    {
        return m_overduetoPM;
    }

    public long getOverdueToUser()
    {
        return m_overduetoUser;
    }

    public void setOverdueToPM(long p_time)
    {
        m_overduetoPM = p_time;
    }

    public void setOverdueToUser(long p_time)
    {
        m_overduetoUser = p_time;
    }

	public void setReportUploadCheck(int p_reportUploadCheck) {
		this.m_reportUploadCheck = p_reportUploadCheck;
	}

	public int getReportUploadCheck() {
		return m_reportUploadCheck;
	}
}
