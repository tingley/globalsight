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
import java.util.ArrayList;
import java.util.Date;

// globalsight
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.date.DateHelper;

/**
 * This is strictly an entity object that holds information that is specific to a task.
 * This object is serializable and can be passed to a client.
 */
public class TaskInfo
    implements Serializable
{

    //=====================private data members==================================

    private long m_id;
    private String m_name;
    private int m_state;                    // the state of the task.
                                            // the various states are described in Task.java
    private Date m_acceptedDate = null;     // the date and time the task was accepted 
                                            // null if not accepted yet
    private Date m_completedDate = null;    // the date and time the task was completed
                                            // null if not completed yet
    private Date m_acceptByDate;            // the date the task SHOULD be accepted by
    private Date m_completeByDate;          // the date the task SHOULD be completed by
    
    private Date exportDate;


    //  list of assignees to the task
    private ArrayList m_taskAssignees = new ArrayList();

    // if the task has been accepted then the user id of the person 
    // who actually accepted the task
    private String m_acceptorUserId = null; 

    private int m_type = Task.TYPE_TRANSLATE;
    
    private long m_overdueToPM = 0;
    private long m_overdueToUser = 0;

    /**
     * Constructor accepts all values to create a TaskInfo object.
     * 
     * @param p_id      The id of the task.      
     * @param p_name    The name of the task.
     * @param p_state   The state of the task.
     * @param p_acceptByDate    The date the task should be accepted by.
     * @param p_completeByDate  The date the task should be completed by.
     * @param p_acceptedDate    The date the task was accepted.  NULL if it hasn't been accepted yet.
     * @param p_completedDate   The date the task was completed.  NULL if it hasn't been completed yet.
     * @param p_type    The task type.
     *
     */
    public TaskInfo(long p_id, String p_name, int p_state,
                    Date p_acceptByDate, Date p_completeByDate,
                    Date p_acceptedDate, Date p_completedDate,
                    int p_type)  
    {
        m_id = p_id;
        m_name = p_name;
        m_state = p_state;
        m_acceptByDate = p_acceptByDate;
        m_completeByDate = p_completeByDate;
        m_acceptedDate = p_acceptedDate;
        m_completedDate = p_completedDate;
        m_type=p_type;
    }


    /**
     * Add a possible task assignee to this task info object.
     * The assignee is determined based on the role associated with
     * the task.
     */
    public void addTaskAssignee(String p_userId, 
                                String p_userFullName, 
                                Date p_estimatedCompletionDate)
    {
        m_taskAssignees.add(new TaskAssignee(
            m_id, p_userId, p_userFullName, 
            p_estimatedCompletionDate));
    }

    /**
     * Get the id of this task.
     * @return long of The task's id.
     */
    public long getId()
    {
        return m_id;
    }

    /**
     * To get the name of the task.
     * @return String
     */
    public String getName()
    {
        return m_name;
    }
    
    /**
     * To get the display name of the task.
     * @return String
     */
    public String getTaskDisplayName()
    {
    	String displayName = getName();
    	try
    	{
    		Activity act = (Activity)ServerProxy.getJobHandler().getActivity(getName());
    		displayName = act.getDisplayName();
    	}
    	catch (Exception e)
    	{
    		
    	}
    	return displayName;
     }
    
    /**
     * To get the state of the task. The states are defined in Task.java as CONSTANTS.
     * @return int
     */
    public int getState()
    {
        return m_state;
    }
    
    public void setState(int state) {
        this.m_state = state;
    }

    public String getStateAsString()
    {
        return TaskImpl.getStateAsString(m_state);
    }

    /**
     * Get the possible task assignees associated with this task.
     */
    public ArrayList getTaskAssignees()
    {
        return m_taskAssignees;
    }

    /**
     * Get the "Accept by" time as a date object.
     * @return The time given for accepting this task.
     */
    public Date getAcceptByDate()
    {
        return m_acceptByDate;
    }

    /**
     * Get the time stamp the task must be accepted by the user as a String.
     * @return String of "accept by" time stamp.
     */
    public String getAcceptByAsString()
    {
        Date acceptBy = getAcceptByDate();
        if (acceptBy == null)
        {
            return "";
        }
        return DateHelper.getFormattedDateAndTime(acceptBy, null);
    }
    
    /**
     * Get the date that the task was accepted by the user.
     * @return a Date object.
     */
    public Date getAcceptedDate()
    {
        return m_acceptedDate;
    }

    /**
     * Get the date that the task was accepted by the user as a String.
     * @return String of The accepted date.
     */
    public String getAcceptedDateAsString()
    {
        Date accepted = getAcceptedDate();
        if (accepted == null)
        {
            return "";
        }
        return DateHelper.getFormattedDateAndTime(accepted, null);
    }

    /** 
     * Set the user id of the one who accepted the task.
     */
    public void setAcceptor(String p_userId)
    {
        m_acceptorUserId = p_userId;
    }
    
    /**
     * Returns the user id of the user who accepted the task
     * or NULL if not set yet.
     */
    public String getAcceptor()
    {
        return m_acceptorUserId;
    }

    /**
     * To get the complete by date (due date) of the task.
     * @return Date
     */
    public Date getCompleteByDate()
    {
        return m_completeByDate;
    }

    /**
     * Return the completd date of the task as String.
     * @return String
     */
    public String getCompleteByAsString()
    {
        Date completeBy = getCompleteByDate();
        if (completeBy == null)
        {
            return "";
        }
        return DateHelper.getFormattedDateAndTime(completeBy, null);
    }   

    /**
     * Get the date that the task was completed.
     * @return Date.
     */
    public Date getCompletedDate()
    {
        return m_completedDate;
    }

    /**
     * Get the time stamp the task was completed by the user as a String.
     * @return String of "Completed date" timestamp.
     */
    public String getCompletedDateAsString()
    {
        Date completed = getCompletedDate();
        if (completed == null)
        {
            return "";
        }
        return DateHelper.getFormattedDateAndTime(completed, null);
    }

    /**
     * Returns the type of the task. See Task.TYPE_TRANSLATE or
     * Task.TYPE_REVIEW
     * 
     * @return type
     */
    public int getType()
    {
        return m_type;
    }
  
    /** 
     * Print out the contents of the task as a string.
     */
    public String toString()
    {
        StringBuffer message = new StringBuffer("Task - ");
        message.append(" Id=");
        message.append(m_id);
        message.append(", Name="); 
        message.append(getName() != null ? getName() : "null");
        message.append(", State=");
        message.append(getState());
        message.append(", AcceptByDate=");
        message.append(getAcceptByAsString() != null ? getAcceptByAsString() : "null");
        message.append(", AcceptedDate=");
        message.append(getAcceptedDateAsString() != null ? getAcceptedDateAsString() : "null");
        message.append(", DueDate="); 
        message.append(getCompleteByAsString() != null ? getCompleteByAsString() : "null");
        message.append(", CompletedDate=");
        message.append(getCompletedDateAsString() != null ? getCompletedDateAsString() : "null");
        message.append(", Type=");
        message.append(m_type);

        return message.toString();
    }


    public Date getExportDate()
    {
        return exportDate;
    }


    public void setExportDate(Date exportDate)
    {
        this.exportDate = exportDate;
    }  
    
    /**
     * Get the overdue time to pm for this activity.
     */
    public long getOverdueToPM()
    {
        return m_overdueToPM;
    }

    /**
     * Get the overdue time to user for this activity.
     */
    public long getOverdueToUser()
    {
        return m_overdueToUser;
    }
    
    /**
     * Set the overdue time to pm for this activity.
     */
    public void setOverdueToPM(long p_time) {
        this.m_overdueToPM = p_time;
    }
    
    /**
     * Set the overdue time to user for this activity.
     */
    public void setOverdueToUser(long p_time) {
        this.m_overdueToUser = p_time;
    }
}

