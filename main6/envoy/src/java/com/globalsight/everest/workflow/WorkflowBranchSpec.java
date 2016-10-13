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

//JDK
import java.io.Serializable;


/**
* WorkflowBranchSpec class is a wrapper (only attributes) for iflow's BranchSpec object.
*/




public class WorkflowBranchSpec implements Serializable
{
    
	private static final long serialVersionUID = 3164669718779565321L;
	
	private int m_comparisonOperator = -1;
    private String m_value = "";
    private String m_arrowLabel = "";
    private boolean m_isDefault = false;
    private int m_structuralState = -1;
    
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    public WorkflowBranchSpec()
    {
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the label (name) of the arrow.
     * @return The arrow's label.
     */
    public String getArrowLabel()
    {
        return m_arrowLabel;
    }

    /**
     * Get the comparison operator for this branch spec.
     * @return The branch spec's comparison operator.
     */
    public int getComparisonOperator()
    {
        return m_comparisonOperator;
    }

    /**
     * Get the structural state of this branch  (i.e. new, deleted, edited, or unchanged).
     * @return The structural state of this branch.
     */    
    public int getStructuralState()
    {
        return m_structuralState;
    }

    /**
     * Get the value of this branch spec.
     * @return The branch spec's value.
     */
    public String getValue()
    {
        return m_value;
    }

    /**
     * Determines whether this is the default branch spec.
     * @return True if this is the default branch spec.  Otherwise, 
     *         return false.
     */
    public boolean isDefault()
    {
        return m_isDefault;
    }


    /**
     * Set the arrow label to be the specified value.
     * @param p_arrowLabel - The arrow name to be set.
     */
    public void setArrowLabel(String p_arrowLabel)
    {
        m_arrowLabel = p_arrowLabel;
    }

    /**
     * Set the comparison operator to be the specified value.
     * @param p_comparisonOperator - The operator to be set.
     */
    public void setComparisonOperator(int p_comparisonOperator)
    {
        m_comparisonOperator = p_comparisonOperator;
    }

    /**
     * Set the flag which determines whether or not this is the default
     * branch spec.
     * @param p_isDefault - The value to be set.
     */
    public void setDefault(boolean p_isDefault)
    {
        m_isDefault = p_isDefault;
    }

    /**
     * Set the structural state to be the specified value.
     * @param p_structuralState - The structural state to be set.
     */
    public void setStructuralState(int p_structuralState)
    {
        m_structuralState = p_structuralState;
    }

    /**
     * Set the value of this branch spec.
     * @param p_value - The value to be set.
     */
    public void setValue(String p_value)
    {
        m_value = p_value;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////    
}
