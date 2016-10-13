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
package com.globalsight.everest.workflowmanager;


import com.globalsight.everest.persistence.PersistentObject;


/**
 * WorkflowOwner contains the information about a user who has
 * the authority to modify (strucutral edit and reassignment) a
 * workflow that he's assigned as the owner.
 * Note that the owner type is either Project Manager or Workflow Manager.
 */

public class WorkflowOwner extends PersistentObject
{
	private static final long serialVersionUID = 1L;

	/** TOPLINK mapped field for owner id */
    public static final String M_OWNER_ID="m_ownerId";

    private Workflow m_workflow = null;
    private String m_ownerId = null;
    private String m_ownerType = null;
    
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Default constructor used by TOPLink.
     */
    public WorkflowOwner()
    {
        super();
    }

    /**
     * Construction used during creation of the owner.
     */
    public WorkflowOwner(String p_ownerId,
                         String p_ownerType)
    {        
        m_ownerId = p_ownerId;
        m_ownerType = p_ownerType;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the owner id (user name).
     * @return The owner id of the workflow.
     */
    public String getOwnerId()
    {
        return m_ownerId;
    }

    /**
     * Get the workflow owner's type.
     * @return The workflow onwer's type which could be either
     * ProjectManager or WorkflowManager.
     */
    public String getOwnerType()
    {
        return m_ownerType;
    }

    /**
     * Get the workflow that this owner belongs to.
     * @return The workflow object which this is one of its owners.
     */
    public Workflow getWorkflow()
    {
        return m_workflow;
    }
    
    /**
     * Set the workflow to the specified value.
     * @param p_workflow - The workflow to be set which this owner
     * will become on of it's owners.
     */
    public void setWorkflow(Workflow p_workflow)
    {
        m_workflow = p_workflow;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Public Helper Methods
    //////////////////////////////////////////////////////////////////////



	public void setOwnerId(String id)
	{
		m_ownerId = id;
	}

	public void setOwnerType(String type)
	{
		m_ownerType = type;
	}
}
