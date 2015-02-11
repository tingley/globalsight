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



/**
* WorkflowArrowInstance class is a wrapper for iflow's ArrowInstance object.
*/


public class WorkflowArrowInstance extends WorkflowArrow
{
    
	private static final long serialVersionUID = 8066361310082617529L;
	private long m_id = -1;
    private boolean m_isActive = false;
    private int m_state = -1;

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Default constructor.
     */
    public WorkflowArrowInstance()
    {
    }


    /**
     * Constructor that takes all the basic info.
     * @param p_arrowName - The name of the arrow.
     * @param p_arrowType - The arrow type.
     * @param p_sourceNode - The source node (where the arrow is coming from).
     * @param p_targetNode - The target node (where arrow is pointing to).
     */
    public WorkflowArrowInstance(String p_arrowName,
                                 long p_arrowType,
                                 WorkflowTask p_sourceNode,
                                 WorkflowTask p_targetNode)

    {
        super(p_arrowName,p_arrowType,p_sourceNode,p_targetNode);
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the id of this arrow instance.
     * @return The arrow instance's unique id.
     */
    public long getArrowInstanceId()
    {
        return m_id;
    }

    /**
     * Determines whether this arrow instance is active.
     * @return True is the arrow instance is active.  Otherwise, return false.
     */
    public boolean isActive()
    {
        return m_isActive;
    }

    /**
     * Get the state of this arrow instance.
     * @return The arrow instance's state.
     */
    public int getState()
    {
        return m_state;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////



    //////////////////////////////////////////////////////////////////////
    //  Begin: Package Level Methods
    //////////////////////////////////////////////////////////////////////
    // set the id of this arrow instance to the specified value.
    void setArrowInstanceId(long p_id)
    {
        m_id = p_id;
    }             

    // Sets the active flag to the specified value.
    void isActive(boolean p_isActive)
    {
        m_isActive = p_isActive;
    }

    // set the arrow state
    void setState(int p_state)
    {
        m_state = p_state;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Package Level Methods
    //////////////////////////////////////////////////////////////////////
}

