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


// GlobalSight
import com.globalsight.everest.taskmanager.TaskInfo;
// JDK
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * The DefaultPathTasks contains a list of TaskInfo objects.  The
 * objects are stored in a HashMap where you can obtain a desired
 * object for a given task id.
 */

public class DefaultPathTasks implements Serializable
{
    
	private static final long serialVersionUID = 8869978703967748022L;
	
	private HashMap<Long, TaskInfo> m_map = null;
    
    
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Constructor
     */
    public DefaultPathTasks()
    {
        super();
        m_map = new HashMap<Long, TaskInfo>(1);
    }

    
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the task info object based on the given task id.
     * @param p_taskInfoId - The task id.
     * @return The TaskInfo object based on the given task id.
     */
    public TaskInfo getTaskInfoById(long p_taskInfoId)
    {
        return (TaskInfo)m_map.get(new Long(p_taskInfoId));
    }

    /**
     * Get a list of all TaskInfo objects within the workflow's 
     * default path.
     * @return A list of TaskInfo objects.
     */
    @SuppressWarnings("unchecked")
	public List getAllTaskInfos()
    {
        return new ArrayList(m_map.values());
    }

    /**
     * Returns the number of TaskInfo objects in the workflow's 
     * default path.
     */
    public int size()
    {
        return m_map.size();
    }
    
    //////////////////////////////////////////////////////////////////////
    //  End: Public Helper Methods
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: Package Level Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Add the specified task info object to the list of default path tasks.
     * @param p_taskInfo - The task info object to be added.
     */
    void addTaskInfo(TaskInfo p_taskInfo)
    {
        m_map.put(new Long(p_taskInfo.getId()), p_taskInfo);
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Package Level Methods
    //////////////////////////////////////////////////////////////////////
}
