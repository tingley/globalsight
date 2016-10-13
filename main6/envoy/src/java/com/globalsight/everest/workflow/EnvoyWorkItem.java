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

//import com.globalsight.everest.foundation.Timestamp;


/**
 * EnvoyWorkItem is a class that represent the iflow's WorkItem (association
 * of a node instance and an assignee).  Note that EnvoyWorkItem should only
 *  be instantiated via  WorkflowTaskInstance object.
 */



public class EnvoyWorkItem  implements Serializable
{
    
	private static final long serialVersionUID = 6814879570649471464L;
	// workitem attributes
    private String m_workItemName   = null;
    private String m_assignee       = null;
    private int m_workItemState     = -1;

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Constructs a package level work item.
     * @param p_workItemName - The name of the work item.
     * @param p_assignee - The work item's assignee.
     * @param p_workItemState - The state of the work item.
     */
    EnvoyWorkItem(String p_workItemName, String p_assignee, 
                  int p_workItemState)
    {
        m_workItemName = p_workItemName;
        m_assignee = p_assignee;
        m_workItemState = p_workItemState;
    }

    /**
    * Get the WorkItem name.
    * @return The work item's name.
    */
    public String getWorkItemName()
    {
        return m_workItemName;
    }

    /**
    * Get the assignee of the work item.
    * @return The assignee of the work item.
    */
    public String getAssignee()
    {
        return m_assignee;
    }

    /**
    * Get the work item's state.
    * @return The state of the work item.
    */
    public int getWorkItemState()
    {
        return m_workItemState;
    }


    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        return super.toString()
                + " m_workItemName=" + (m_workItemName != null?
                m_workItemName:"null")
                + " m_assignee=" + (m_assignee != null?
                m_assignee:"null")
                + " m_workItemState=" + Integer.toString(m_workItemState)
                ;
    }
}
