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
import java.util.List;
import java.util.ArrayList;


/**
* WorkflowConditionSpec class is a wrapper (only attributes) for iflow's ConditionSpec object.
*/

public class WorkflowConditionSpec implements Serializable
{
    
	private static final long serialVersionUID = -5062185175599748995L;
	
	private List<WorkflowBranchSpec> m_workflowBranchSpecs = null;
    private String m_attributeName = null;
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    public WorkflowConditionSpec()
    {
        m_workflowBranchSpecs = new ArrayList<WorkflowBranchSpec>();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the workflow branch spec based on the given arrow label.
     * @return The branch spec that has the given arrow label (null if not found).
     */
    public WorkflowBranchSpec getBranchSpec(String p_arrowLabel)
    {
        WorkflowBranchSpec branchSpec = null;
         int size = m_workflowBranchSpecs == null ? 0 : 
             m_workflowBranchSpecs.size();
         boolean isSame = false;
         for (int i=0; !isSame && i<size; i++)
         {
             WorkflowBranchSpec workflowBranchSpec = 
                 (WorkflowBranchSpec)m_workflowBranchSpecs.get(i);
             
             isSame = workflowBranchSpec.getArrowLabel().equals(p_arrowLabel);
             if (isSame)
             {
                 branchSpec = (WorkflowBranchSpec)m_workflowBranchSpecs.get(i);
             }
         }
        return branchSpec;
    }

    /**
     * Get the workflow branch specs.
     * @return The array of branch specs.
     */
    // newely added 
    public List getBranchSpecs()
    {
        return m_workflowBranchSpecs;
    }

    /**
     * Get the condition attribute name.
     * @return The condition attribute name.
     */
    public String getConditionAttribute()
    {
        return m_attributeName;
    }    


    /**
     * Set the condition attribute name to be the specified value.
     * @param p_attributeName - The name to be set.
     */
    public void setConditionAttribute(String p_attributeName)
    {
        m_attributeName = p_attributeName;
    }

    /**
     * Set the info of a branch spec based on the given parameters.
     *
     * @param p_arrowLabel - the arrow label.
     * @param p_operation - The branch operation constant.
     * @param p_value - The value used in the branch comparison.
     * @param p_isDefault - The flag that determines whether the new 
     *                      branch is the default.
     */
    public WorkflowBranchSpec setCondBranchSpecInfo(String p_arrowLabel, 
                                                    int p_operation,
                                                    String p_value, 
                                                    boolean p_isDefault)
    {
        WorkflowBranchSpec workflowBranchSpec= null;
        int sz = m_workflowBranchSpecs.size();
        boolean isSame = false;
        for (int i=0; !isSame && i<sz; i++)
        {
            workflowBranchSpec= 
                (WorkflowBranchSpec)m_workflowBranchSpecs.get(i);
            isSame = 
                workflowBranchSpec.getArrowLabel().equals(p_arrowLabel);
            if (isSame)
            {
                workflowBranchSpec.setComparisonOperator(p_operation);
                workflowBranchSpec.setValue(p_value);
                workflowBranchSpec.setDefault(p_isDefault);                
            }
        }
        return workflowBranchSpec;
    }

    /**
     * Set the ordered array of branch specs to be the specified array.
     * @param p_branchSpecs An array of ordered branches.
     */
    public void setEvalOrder(List<WorkflowBranchSpec> p_branchSpecs)
    {
        m_workflowBranchSpecs = p_branchSpecs;
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////     

    //////////////////////////////////////////////////////////////////////
    //  Begin: Package Level Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Adds a condition branch spec info to the list.
     * <p>
     * Returns boolean value - the branch is default or not.
     */
    boolean addCondBranchSpecInfo(String p_arrowLabel, int p_operation,
            String p_value, boolean p_isDefault)
    {
        p_isDefault = determineDefaultBranch();

        WorkflowBranchSpec branchSpec = new WorkflowBranchSpec();
        branchSpec.setArrowLabel(p_arrowLabel);
        branchSpec.setComparisonOperator(p_operation);
        branchSpec.setValue(p_value);
        branchSpec.setDefault(p_isDefault);

        m_workflowBranchSpecs.add(branchSpec);

        return p_isDefault;
    }


    // Remove a branch spec based on the given arrow name and state.
    void removeBranchSpec(String p_arrowName , int p_state)
    {
        boolean isDefaultRemoved = false;
        int i = m_workflowBranchSpecs.size() - 1;
        while (i >= 0)
        {
            WorkflowBranchSpec branchSpec =
                (WorkflowBranchSpec)m_workflowBranchSpecs.get(i--);
            
            if (branchSpec.getArrowLabel().equals(p_arrowName))
            {
                isDefaultRemoved = branchSpec.isDefault();
                if (p_state == WorkflowConstants.REMOVED)
                {
                    // remove the branch spec from the collection since
                    // it's not an existing one (it was part of a newly
                    // added arrow).
                    m_workflowBranchSpecs.remove(
                        m_workflowBranchSpecs.indexOf(branchSpec)) ;
                }
                else
                {               
                    // since this branch spec is part of an existing condition spec
                    // (basically the arrow is an existing one), we should just
                    // update the state of it until the mapping for persistence happens.
                    branchSpec.setStructuralState(WorkflowConstants.
                                                  REMOVED);                    
                }

                // If the "default" branch spec is removed, we need to continue
                // thru the list and set the next one to be the default.  Otherwise,
                // break out of the loop.
                if (isDefaultRemoved)
                {
                    //reset the index in case the default was not the first
                    // branch spec in the list (go thru all)
                    i = i < 0 ? (m_workflowBranchSpecs.size() - 1) : i;
                    continue;
                }
                else
                {                
                    break;
                }
            }
            
            // if the removed branchSpec was the default one, we
            // need to set the next one as the default spec now.
            if (isDefaultRemoved && 
                branchSpec.getStructuralState() != 
                WorkflowConstants.REMOVED)
            {
                branchSpec.setDefault(true);
                break;
            }
        }        
    }    

    //This method will update the branch spec s' Arrow lable with new ArrowLabel
    void updateBranchSpec(String p_oldArrowLabel ,String p_newArrowLabel){
      int size = m_workflowBranchSpecs.size();
      boolean isSameLabel = false;
      for (int i=0; !isSameLabel && i<size; i++)
      {
          WorkflowBranchSpec branchSpec =
              (WorkflowBranchSpec)m_workflowBranchSpecs.get(i);
          isSameLabel =  branchSpec.getArrowLabel().equals(p_oldArrowLabel);
          if (isSameLabel)
          {
            branchSpec.setArrowLabel(p_newArrowLabel);          
          }
      }
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Package Level Methods
    //////////////////////////////////////////////////////////////////////

    // determine whether an newly added branch should be the default one.
    // Note that ONLY removal of newly added arrows will result in removal 
    // of the branch spec from the m_workflowBranchSpecs colleciton.  Otherwise,
    // the branch spec will only have a state of REMOVED.
    private boolean determineDefaultBranch()
    {
        int size = m_workflowBranchSpecs.size();
        // for a 0 size collection, the first branch spec is "default" one.
        if (size == 0)
        {
            return true;
        }
        else
        {
            boolean isDefault = false;
            // check the collection and see if there's already a default
            // branch spec.
            for (int i = 0; !isDefault && i < size; i++)
            {
                WorkflowBranchSpec branchSpec = 
                    (WorkflowBranchSpec)m_workflowBranchSpecs.get(i);

                isDefault = branchSpec.isDefault() && 
                    branchSpec.getStructuralState() != 
                    WorkflowConstants.REMOVED;
            }

            return !isDefault;
        }
    }
}
