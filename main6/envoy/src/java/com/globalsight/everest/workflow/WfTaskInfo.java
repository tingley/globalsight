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

import com.globalsight.everest.servlet.util.ServerProxy;
import java.util.ArrayList;



/**
 * This class only represent a lightweight workflow task that is
 * sent back to a client that needs to perform actions based on 
 * the action type of a particular task. 
 */




public class WfTaskInfo implements Serializable
{
    
	private static final long serialVersionUID = 846870546594361604L;
	private boolean m_followedByExit = false;
    private int m_state = 0;
    private long m_id = -1;
    private long m_acceptanceDuration = 0;
    private long m_completionDuration = 0;
    private long m_overdueToPM = 0;
    private long m_overdueToUser = 0;
    private String m_actionType = null;
    private String m_name = null;
    private String[] m_roles = null;
    //For automatic action, the download xliff file need the task assignee's email info
    public ArrayList userEmail = new ArrayList();



    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
    * WfTaskInfo constructor used for setting the initial values.
    * @param p_id - The id of the workflow task instance.
    * @param p_actionType - The system action type of this task instance.
    */
    public WfTaskInfo(long p_id, String p_actionType)
    {        
        m_id = p_id;
        m_actionType = p_actionType;
    }

    /**
     * Constructor used for setting durations.
     */
    public WfTaskInfo(long p_id, String p_name,
                      long p_acceptanceDuration, 
                      long p_completionDuration, 
                      String[] p_roles, int p_state,
                      boolean p_followedByExit)
    {
        m_id = p_id;
        m_name = p_name;
        m_acceptanceDuration = p_acceptanceDuration;
        m_completionDuration = p_completionDuration;
        m_roles = p_roles;
        m_state = p_state;
        m_followedByExit = p_followedByExit;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Determines whether at least one of the outgoing arrows of this node
     * is attached to the exit node (an indication of this task being
     * the last one in the workflow).
     *
     * @return True if one of the outgoing arrows points to exit node.
     * Otherwise, return false.
     */
    public boolean followedByExitNode()
    {
        return m_followedByExit;
    }
    /**
     * Get the state of the node.  The state only specifies whether
     * the node is active, deactived, or completed.
     * @return The node's state.
     */
    public int getState()
    {
        return m_state;
    }

    /**
     * Get the acceptance duration for this activity.
     * @return The acceptance duration.
     */
    public long getAcceptanceDuration()
    {
        return m_acceptanceDuration;
    }

    /**
     * Get the completion duration for this activity.
     * @return The completion duration.
     */
    public long getCompletionDuration()
    {
        return m_completionDuration;
    }
    
    /**
     * Get the overdue time to pm for this activity.
     */
    public long getOverdueToPM()
    {
        return m_overdueToPM;
    }

    /**
     * Get the overdue time to user for this activity.
     */
    public long getOverdueToUser()
    {
        return m_overdueToUser;
    }
    
    /**
     * Set the overdue time to pm for this activity.
     */
    public void setOverdueToPM(long p_time) {
        this.m_overdueToPM = p_time;
    }
    
    /**
     * Set the overdue time to user for this activity.
     */
    public void setOverdueToUser(long p_time) {
        this.m_overdueToUser = p_time;
    }

    /**
    * Get the id of the task instance..
    * @return The task's id.
    */
    public long getId()
    {
        return m_id;
    }

    /**
     * Get the activity name.
     */
    public String getName()
    {
        return m_name;
    }
    
    /**
     * Get the activity display name.
     */
    public String getDisplayName()
    {
    	String displayName = getName();
    	try
    	{
    		Activity act = (Activity)ServerProxy.getJobHandler().getActivity(getName());
    		displayName = act.getDisplayName();
    	}
    	catch (Exception e)
    	{
    		
    	}
    	return displayName;
    }

    /**
     * Get the roles associated with this task.
     */
    public String[] getRoles()
    {
        return m_roles;
    }

    /**
     * Get the estimated total duration for this activity.
     * This duration is the combination of acceptance and 
     * completion duration.
     * @return The total duration for this activity.
     */
    public long getTotalDuration()
    {
        return m_acceptanceDuration + m_completionDuration;
    }

    /**
    * Get the system action type for this task instance.  Note that
    * the action type will be used as a key to the localization 
    * property files.  A null action type means that there's no system
    * action associated with this task.
    * @return The system action type associated with this task.
    */
    public String getActionType()
    {
        return m_actionType;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////
}
