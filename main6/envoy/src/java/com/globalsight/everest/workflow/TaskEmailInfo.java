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

import com.globalsight.everest.foundation.Timestamp;

// JDK
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.graph.def.Node;

/**
 * The information needed for sending out emails about tasks 
 * (start, advance, reassign, cancel, etc.) 
 */
public class TaskEmailInfo
    implements java.io.Serializable
{
    
	private static final long serialVersionUID = 9146331513634643757L;
	//
    // PRIVATE MEMBER VARIABLES
    //
    private String m_projectManagerId = null;
    private boolean m_notifyPm = false;
    private List m_workflowManagerIds = new ArrayList();
    private int m_priority = -1;
    private String m_comments = "";
    private Timestamp m_timeEventWillTakePlace = null;
    private String m_pageName = "";
    private String m_jobName = "";
    private int m_levMatchThreshold = 0;
    private int m_wordTotal = 0;
    private int m_noMatches = 0;
    private int m_repetitions = 0;
    private int m_segmentMatches = 0;
    private int m_fuzzyMatches = 0;
    private String m_jobId = "";
    private String m_projectName = "";

    // These values are used for the event notification (emails).
    private Long m_projectIdAsLong = null;
    private Long m_wfIdAsLong = null;
    private String m_sourceLocale = null;
    private String m_targetLocale = null;
    
    //Assigns' names for the overdue notification.
    private String m_assigneesName = null;

    //added attachment information
    private List m_attachment = null;

    //Comments that include restricted attachment information.
    private String m_restrictComments = "";
    
    //Attachments that include restricted attachment.
    private List m_restrictAttachment = null;
    //For task complete, when send mail, 
    //the email's activity need be the prenode's activity.
    private Node prenode = null;
    
    //Accepter Name, For Example, Activity accepter name
    private String m_accepterName = null;
    
    private String m_companyId = null;
    
    // The ignored recipient of email.
    private Set<String> ignoredReceipt;

	//
    // PUBLIC CONSTRUCTORS
    //
    public TaskEmailInfo()
    {
    }

    /**
     * Create the information without any comments.
     */
    public TaskEmailInfo(String p_projectManagerId,
                         List p_workflowManagerIds,
                         boolean p_notifyPm,
                         int p_priority)
    {   
        this(p_projectManagerId, p_workflowManagerIds, p_notifyPm, 
             p_priority, "");
    }

    /**
     * Create the information with comments.
     */
    @SuppressWarnings("unchecked")
	public TaskEmailInfo(String p_projectManagerId,
                         List p_workflowManagerIds,
                         boolean p_notifyPm,
                         int p_priority,
                         String p_comments)
    {   
        m_projectManagerId = p_projectManagerId;
        m_notifyPm = p_notifyPm;
        m_priority = p_priority;
        m_comments = p_comments;

        if (p_workflowManagerIds != null)
        {
            m_workflowManagerIds.addAll(p_workflowManagerIds);
        }
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the comments associated with this task to be
     * sent in the email.
     * @return  Comments
     */
    public String getComments()
    {
        return m_comments;
    }
    

    /**
     * Return the job id.
     */
    public String getJobId()
    {
        return m_jobId;
    }
    
    /**
     * Return the job name.
     */
    public String getJobName()
    {
        return m_jobName;
    }

    /**
     * Get the leverage match threshold for this task which is
     * set at job level.
     */
    public int getLeverageMatchThreshold()
    {
        return m_levMatchThreshold;
    }

    /**
     * Get the workflow level no matches word count.
     */
    public int getNoMatchesWordCount()
    {
        return m_noMatches;
    }
    
    /**
     * Get the total word count.
     */
    public int getTotalWordCount()
    {
        return m_wordTotal;
    }

    /**
     * Return the name of the page that may be affected 
     * or NULL if one isn't set.
     */
    public String getPageName()
    {
        return m_pageName;
    }

    /**
     * Get the priority of the task.
     * @return The priority of the task (same as job priority).
     */
    public int getPriority()
    {
        return m_priority;
    }

    /**
     * Get the priority of the task as a string.
     * @return The priority of the task as a string(same as job priority).
     */
    public String getPriorityAsString()
    {
        return String.valueOf(m_priority);                                                        
    }

    /**
     * Get the project id as a Long object.
     * @return The Long representation of the project id.
     */
    public Long getProjectIdAsLong()
    {
        return m_projectIdAsLong;
    }

    /**
     * Get the project name
     */
    public String getProjectName()
    {
        return m_projectName;
    }

    /**
     * Get the project manager id.
     * @return The project manager id.
     */
    public String getProjectManagerId()
    {
        return m_projectManagerId;
    }

    /**
     * Get the workflow level repetitions word count.
     */
    public int getRepetitionsWordCount()
    {
        return m_repetitions;
    }

    /**
     * Get the workflow level segment tm matches word count.
     */
    public int getSegmentTmMatchesWordCount()
    {
        return m_segmentMatches;
    }

    /**
     * Get the string representation of the source locale.
     * @return The string representation of the job's source locale.
     */
    public String getSourceLocale()
    {
        return m_sourceLocale;
    }

    /**
     * Get the string representation of the target locale.
     * @return The string representation of the workflow's target locale.
     */
    public String getTargetLocale()
    {
        return m_targetLocale;
    }
    
    /**
     * Get the assignees' names of this activity.
     * @return The string representation of the workflow's target locale.
     */
    public String getAssigneesName()
    {
    	return m_assigneesName;
    }
    
    /**
     * Return the time the event will take place
     * or NULL if one isn't set.
     */
    public Timestamp getTime()
    {
        return m_timeEventWillTakePlace;
    }

    /**
     * Get the workflow level total fuzzy matches word count
     * based on the leverage match threshold.
     */
    public int getTotalFuzzyMatchesWordCount()
    {
        return m_fuzzyMatches;
    }

    /**
     * Get the workflow id as a Long object.
     * @return The Long representation of the workflow id.
     */
    public Long getWfIdAsLong()
    {
        return m_wfIdAsLong;
    }

    /**
     * Get a list of possible workflow manager ids.
     * @return The workflow manager ids.
     */
    public List getWorkflowManagerIds()
    {
        return m_workflowManagerIds;
    }
    
    /**
     * Determines whether an email notification should be sent to PM.
     * @return True if email should be sent to PM.  Otherwise, returns false.
     */
    public boolean notifyProjectManager()
    {
        return m_notifyPm;
    }

    /**
     * Set the job id that this task is associated with.
     */
    public void setJobId(String p_jobId)
    {
	m_jobId = p_jobId;
    }

    /**
     * Set the job name that this task is associated with.
     */
    public void setJobName(String p_jobName)
    {
        m_jobName = p_jobName;
    }

    /**
     * Set the name of the page that may be affected.
     */
    public void setPageName(String p_pageName)
    {
        m_pageName = p_pageName;
    }

    /**
     * Sets the project name
     */
    public void setProjectName(String p_projectName)
    {
        m_projectName = p_projectName;
    }

    /**
     * Set the project id to be the specified value.
     * @param p_projectIdAsLong - The project id to be set.
     */
    public void setProjectIdAsLong(Long p_projectIdAsLong)
    {
        m_projectIdAsLong = p_projectIdAsLong;
    }

    /**
     * Set the source locale to be the specified value.
     * @param p_sourceLocale - The source locale of the job.
     */
    public void setSourceLocale(String p_sourceLocale)
    {
        m_sourceLocale = p_sourceLocale;
    }
    
    /**
     * Set the target locale to be the specified value.
     * @param p_targetLocale - The target locale of the workflow.
     */
    public void setTargetLocale(String p_targetLocale)
    {
        m_targetLocale = p_targetLocale;
    }
    
    /**
     * Set the assignees' names to be the specified value.
     * @param p_assigneesName - The assignees' names of this activity.
     */
    public void setAssigneesName(String p_assigneesName)
    {
    	m_assigneesName = p_assigneesName;
    }
    
    /**
     * Set the time the event associated with this task will take place.
     */
    public void setTime(Timestamp p_time)
    {
        m_timeEventWillTakePlace = p_time;
    }

    /**
     * Set the workflow id to be the specified value.
     * @param p_wfIdAsLong - The workflow id to be set.
     */
    public void setWfIdAsLong(Long p_wfIdAsLong)
    {
        m_wfIdAsLong = p_wfIdAsLong;
    }


    /**
     * Set the word count details needed for email notification.  This
     * word count info is at workflow level and based on the leverage
     * match threshold.
     */
    public void setWordCountDetails(
        int p_levMatchThreshold, int p_noMatches, int p_repetitions, 
        int p_totalFuzzyMatches, int p_segmentMatches,int p_wordTotal)
    {
        m_levMatchThreshold = p_levMatchThreshold;
        m_noMatches = p_noMatches;
        m_repetitions = p_repetitions;
        m_fuzzyMatches = p_totalFuzzyMatches;
        m_segmentMatches = p_segmentMatches;
        m_wordTotal = p_wordTotal;
    }

    /**
     * Sets the attachment
     * @return
     */
    public List getAttachment()
    {
        return m_attachment;
    }

    /**
     * Returns the attachment
     * @param m_attachment
     */
    public void setAttachment(List p_attachment)
    {
        m_attachment = p_attachment;
    }

    public String getRestrictComments()
    {
        return m_restrictComments;
    }

    public void setRestrictComments(String comments)
    {
        m_restrictComments = comments;
    }

    public List getRestrictAttachment()
    {
        return m_restrictAttachment;
    }

    public void setRestrictAttachment(List attachment)
    {
        m_restrictAttachment = attachment;
    }
    
    /**
     * Returns a string representation of the object (based on the object name).
     */
    public String toString()
    { 
        StringBuilder sb = new StringBuilder(super.toString());
        if (getProjectManagerId() != null)
        {
            sb.append(", Project manager id=");
            sb.append(getProjectManagerId());
        }
        sb.append(", Priority=");
        sb.append(getPriority());
        sb.append(", Comments={");
        sb.append(getComments());
        sb.append("}");
        if (getTime() != null)
        {
            sb.append(", Timestamp=");
            sb.append(getTime());
        }
        sb.append(", PageName=");
        sb.append(getPageName());
        return sb.toString();
    } 
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////
    
    public Node getPreNode() {
        return this.prenode;
    }
    
    public void setPreNode(Node m_node) {
        this.prenode = m_node;
    }
    
    public String getAccepterName() {
		return m_accepterName;
	}

	public void setAccepterName(String accepter) {
		m_accepterName = accepter;
	}

    public String getCompanyId()
    {
        return m_companyId;
    }

    public void setCompanyId(String m_companyId)
    {
        this.m_companyId = m_companyId;
    }

    public Set<String> getIgnoredReceipt()
    {
        return ignoredReceipt;
    }

    public void setIgnoredReceipt(Set<String> p_ignoredReceipt)
    {
        ignoredReceipt = p_ignoredReceipt;
    }
    
    public void setIgnoredReceipt(String p_ignoredReceipt)
    {
        if(ignoredReceipt == null)
        {
            ignoredReceipt = new HashSet<String>();
        }
        
        ignoredReceipt.add(p_ignoredReceipt);
    }
    
    
}       
