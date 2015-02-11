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
import java.util.List;



/**
 * This class only represent a lightweight workflow instance that is
 * sent back to a client that needs to perform actions based on 
 * either the workflow state or the action type of the next workflow
 * task infos.
 */




public class WorkflowInstanceInfo implements Serializable
{
    
	private static final long serialVersionUID = -4296057389082238079L;
	private long m_id = -1;
    private int m_state = -1;
    private List<WfTaskInfo> m_nextTaskInfos = null;


    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
    * WorkflowInstanceInfo constructor used for setting the initial values.
    * @param p_id - The id of the workflow instance.
    * @param p_state - The workflow instance's current state.
    * @param p_nextTaskInfos - A list of next possible WfTaskInfo objects.
    */
    public WorkflowInstanceInfo(long p_id, int p_state, 
                                List<WfTaskInfo> p_nextTaskInfos)
    {        
        m_id = p_id;
        m_state = p_state;
        m_nextTaskInfos = p_nextTaskInfos;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
    * Get the id of the workflow instance.
    *
    * @return The workflow instance's id.
    */
    public long getId()
    {
        return m_id;
    }


    /**
    * Get a list of next possible WfTaskInfo objects.  
    *
    * @return A list of lightweigh workflow task instance objects (as
    * WfTaskInfo objects).  If there are no activity nodes, the retured
    * value is null.
    */
    public List<WfTaskInfo> getNextTaskInfos()
    {
        return m_nextTaskInfos;
    }

    /**
    * Get the state of the workflow instance.
    *
    * @return The workflow instance's current state.
    */
    public int getState()
    {
        return m_state;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////
}
