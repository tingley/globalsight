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


import java.util.ArrayList;
import java.util.List;

/**
 * WorkflowOwners contains the owners of a workflow.  Currently,
 * the owners are:
 * 1. Project Manager
 * 2. Workflow Managers (almost as a PM helper).  There can be
 *    multiple WM's (0 - n)
 */

public class WorkflowOwners implements java.io.Serializable
{
    
	private static final long serialVersionUID = -8004488131387048853L;
	private String m_pmId = null;
    private String[] m_wfmIds = null;
    private Object[] m_owners = null;
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    public WorkflowOwners(String p_pmId,
                          String[] p_wfmIds)
    {
        List<String> list = new ArrayList<String>();
        list.add( p_pmId);
        if (p_wfmIds != null)
        {
            for (int i = 0 ; i < p_wfmIds.length ; i++)
            {
                list.add(p_wfmIds[i]);
            }
        }
        
        m_owners = list.toArray();
        m_pmId = p_pmId;
        m_wfmIds = p_wfmIds;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the project manager id (user name).
     * @return The project manager's id.
     */
    public String getProjectManagerId()
    {
        return m_pmId;
    }

    /**
     * Get the workflow manager ids (user name).
     * @return The workflow manager's id.
     */
    public String[] getWorkflowManagerIds()
    {
        return m_wfmIds;
    }

    /**
     * Get an array of workflow owner's user names. This is used during
     * the creation of workflow template.
     * @return An array of user names.  Note that the array values are string.
     */
    public Object[] getOwners()
    {
        return m_owners;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Public Helper Methods
    //////////////////////////////////////////////////////////////////////
}
