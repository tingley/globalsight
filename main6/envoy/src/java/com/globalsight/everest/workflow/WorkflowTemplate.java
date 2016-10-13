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


import java.util.Vector;
import java.io.Serializable;

/**
 * WorkflowTemplate class is basically a wrapper for iflow's Plan.  It can be 
 * instantiated by passing iflow's plan as paramter to the constructor.  
 * Therefore, every method of this class delegates the task to the Plan object.
 *
 * Note: Since this class need to be exposed to Java grid applet which runs 
 * within JDK 1.1, all collection classes are choosen from JDK 1.1.
 *
 * @version     1.0
 * @author Tomy A. Doomany
 */


/*
 * MODIFIED     MM/DD/YYYY
 * TomyD        08/31/2000   Initial version.
 */




public class WorkflowTemplate implements Serializable
{
    
	private static final long serialVersionUID = -3178703558591548782L;
	// wf instance id
    private long m_wfTemplateId = -1;
    // wf instance name
    private String m_wfTemplateName = null;
    // wf instance description
    private String m_wfTemplateDescription = null;
    // wf instance tasks
    private Vector<WorkflowTask> m_wfTemplateTasks = null;

    private int m_maxSequence=0;


    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Constructor
    ////////////////////////////////////////////////////////////////////////////////// 
    /**
    * WorkflowTemplate constructor.
    * @param p_templateId - The template's id.
    * @param p_templateName - The name of the template.
    * @param p_templateDescription - The description of the template.
    * @param p_templateTasks - The tasks of the template.
    */
    //public WorkflowTemplate(Plan p_plan)
    public WorkflowTemplate(long p_templateId, 
                            String p_templateName, 
                            String p_templateDescription,
                            Vector<WorkflowTask> p_templateTasks)
    {
        m_wfTemplateId = p_templateId;
        m_wfTemplateName = p_templateName;
        m_wfTemplateDescription = p_templateDescription;
        m_wfTemplateTasks = p_templateTasks;
    }

    /**
     * Default Constructor (used from UI during template creation).
     */
    public WorkflowTemplate()
    {
        m_wfTemplateTasks = new Vector<WorkflowTask>();
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Constructor
    ////////////////////////////////////////////////////////////////////////////////// 


    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Helper Methods
    ////////////////////////////////////////////////////////////////////////////////// 
    /**
    * Get the template's id.
    * @return The template's id.
    */
    public long getId()
    {
        return m_wfTemplateId;
    }


    /**
    * Get the template's name.
    * @return The name of this template.
    */
    public String getName()
    {
        return m_wfTemplateName;
    }


    /**
    * Get the template's description.
    * @return The description of this template.
    */
    public String getDescription()
    {
        return m_wfTemplateDescription;
    }


    /**
    * Get a list of workflow tasks.
    * @return A list of workflow tasks.        
    */
    public Vector<WorkflowTask> getWorkflowTasks()
    {
        return m_wfTemplateTasks;
    }

    /**
     * Create a new arrow and add it to this workflow template.
     * @param p_arrowName - The name of the arrow.
     * @param p_arrowType - The type of the arrow.
     * @param p_sourceNode - The node arrow coming from.
     * @param p_targetNode - The node arrow pointing to.
     */
    public WorkflowArrow addArrow(String p_arrowName, long p_arrowType,
                                  WorkflowTask p_sourceNode,
                                  WorkflowTask p_targetNode)
    {
        WorkflowArrow workflowArrow = new WorkflowArrow(p_arrowName,
                p_arrowType, p_sourceNode, p_targetNode);

        boolean isDefault = true;
        if (p_sourceNode.getType() == WorkflowConstants.CONDITION)
        {
            isDefault = p_sourceNode.getConditionSpec().addCondBranchSpecInfo(
                    p_arrowName, 0, "0", true);
        }
        workflowArrow.setDefault(isDefault);

        p_sourceNode.addOutgoingArrow(workflowArrow);
        p_targetNode.addIncomingArrow(workflowArrow);

        return workflowArrow;
    }

    /**
     * Add a task to this template.
     * @param p_workflowTask - The task to be added to this template.
     */
    public void addWorkflowTask(WorkflowTask p_workflowTask)
    {        
        m_wfTemplateTasks.add(p_workflowTask);        
    }

    /**
     * Create a new workflow task and add it to this template.
     * @param p_taskName - The name of the newly created task.
     * @param p_taskType - The task type.
     */
    public WorkflowTask addWorkflowTask(String p_taskName ,int p_taskType)
    {
        WorkflowTask m_workflowTask = new WorkflowTask(p_taskName,p_taskType);
        if (p_taskType==WorkflowConstants.CONDITION)
        {
            WorkflowConditionSpec m_workflowConditionSpec =new   WorkflowConditionSpec();
            m_workflowTask.setConditionSpec(m_workflowConditionSpec);
        }

        m_wfTemplateTasks.add(m_workflowTask); 

        return m_workflowTask;
    }


    /**
     * Remove a particular workflow task from this template.
     * @param p_workflowTask - The task to be removed.
     */
    public void removeWorkflowTask(WorkflowTask p_workflowTask)
    {
        int index = m_wfTemplateTasks.indexOf(p_workflowTask);
        if (index > -1)
        {
            WorkflowTask wft = (WorkflowTask)m_wfTemplateTasks.get(index);
            if (wft.getTaskId() == WorkflowTask.ID_UNSET)
            {
                m_wfTemplateTasks.remove(index);
            }
            else
            {
                wft.setStructuralState(WorkflowConstants.REMOVED);
            }
        }
    }

    /**
     * Set the name of this template
     * @param p_name - The template name to be set.
     */
    public void setName(String p_templateName)
    {
        m_wfTemplateName = p_templateName;
    }

    /**
     * Set the description of this template to the specified value.
     * @param p_description - The description to be set.
     */
    public void setDescription(String p_templateDescription)
    {
        m_wfTemplateDescription = p_templateDescription;
    }

    public int getMaxSequence()
    {
        return m_maxSequence;
    }

    public void setMaxSequence(int p_maxsequence)
    {
        m_maxSequence = p_maxsequence;
    }

    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Helper Methods
    //////////////////////////////////////////////////////////////////////////////////     

    // set the id of the template
    public void setId(long p_templateId)
    {
        m_wfTemplateId = p_templateId;
    }

    /**
    * Returns a string representation of the object (based on the object name).
    */
    public String toString()
    {
        return m_wfTemplateName;
    }


    /**
     * Return a string representation of the object appropriate 
     * for debugging
     * @return a string representation of the object appropriate 
     * for debugging
     */
    public String toDebugString()
    {
        return super.toString()
        + " m_wfTemplateId=" + Long.toString(m_wfTemplateId)
        + " m_wfTemplateName=" + (m_wfTemplateName != null?
                                  m_wfTemplateName:"null")
        + " m_wfTemplateDescription=" 
                + (m_wfTemplateDescription != null?
                   m_wfTemplateDescription:"null") 
        + " start m_wfTemplateTasks=" 
                + WorkflowHelper.toDebugString(m_wfTemplateTasks)
        + "\nend m_wfTemplateTasks"
                ;
    }
}
