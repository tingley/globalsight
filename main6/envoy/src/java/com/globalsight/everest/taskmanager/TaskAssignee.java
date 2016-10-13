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
package com.globalsight.everest.taskmanager;

// java
import java.io.Serializable;
import java.util.Date;

/**
 * This is strictly an entity object that holds information about an 
 * assignee of a task based on the role associated with the task.
 * Note that a TaskAssignee is only a member of the task's role and
 * does not necessarily represent the individual that's either working
 * on the task or will work on the task.  
 */
public class TaskAssignee
    implements Serializable
{

    // Private variables
    private long m_taskId;
    private String m_userId;
    private String m_userFullName;
    private Date m_estimatedCompletionDate = null;
  


    /**
     * Default constructor used to create a task assignee.
     * 
     * @param p_taskId - The id of the task that the assignee is 
     *                   responsible for.
     * @param p_userId - The user id of the assignee.
     * @param p_userFullName - The assignee's full name.
     * @param p_estimatedCompletionDate - The estimated completion date.
     */
    TaskAssignee(long p_taskId, String p_userId, String p_userFullName, 
                 Date p_estimatedCompletionDate)
    {
        m_taskId = p_taskId;
        m_userId = p_userId;
        m_userFullName = p_userFullName;
        m_estimatedCompletionDate = p_estimatedCompletionDate;
    }

    /**
     * Get the id of the task that the assignee is responsible for.
     * @return The task id.
     */
    public long getTaskId()
    {
        return m_taskId;
    }

    /**
     * Get the user id for the assignee.
     * @return The assignee's user id.
     */
    public String getUserId()
    {
        return m_userId;
    }

    /**
     * Get the full name for the task assignee.
     * @return The assignee's full name.
     */
    public String getUserFullName()
    {
        return m_userFullName;
    }
    
    /**
     * Get the estimated completion date based on the
     * assignee's calendar.
     * @return The estimated completion date for the task.
     */
    public Date getEstimatedCompletionDate()
    {
        return m_estimatedCompletionDate;
    }
}

