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
package com.globalsight.everest.comment;

import com.globalsight.util.date.DateHelper;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflow.Activity;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *  This class contains basic information about a task comment.
 *  It includes general information and does not allow for manipulation on the task comment.
 */
public class TaskCommentInfo 
    implements Serializable
{
    // private attributes
    private long m_commentId;
    private Date m_createDate;
    private String m_creatorId;
    private String m_comment;
   
    private String m_taskName;
    private Locale m_targetLocale;
    // a list of CommentFile objects or empty if there aren't any attachments
    private List m_attachments = new ArrayList();

    /**
     * Construct a TaskCommentInfo without attachments
     */
    public TaskCommentInfo(long p_commentId, 
                           Date p_createDate, String p_creatorId,
                           String p_comment, String p_taskName,
                           Locale p_targetLocale)
    {
        m_commentId = p_commentId;
        m_createDate = p_createDate;
        m_creatorId = p_creatorId;
        m_comment = p_comment;
        m_taskName = p_taskName;
        m_targetLocale = p_targetLocale;
    }

    /**
     * Construct a TaskCommentInfo with attachments
     */
    public TaskCommentInfo(long p_commentId,
                           Date p_createDate, String p_creatorId,
                           String p_comment, String p_taskName,
                           Locale p_targetLocale, List p_attachments)
    {
        m_commentId = p_commentId;
        m_createDate = p_createDate;
        m_creatorId = p_creatorId;
        m_comment = p_comment;
        m_taskName = p_taskName;
        m_targetLocale = p_targetLocale;
        if (p_attachments != null)
        {
            m_attachments = p_attachments;
        }                                 
    }

    /**
     * Get the id of the comment.
     */
    public long getCommentId()
    {
        return m_commentId;
    }

    /**
     * Get the date that the comment was created as a formatted string.
     */
    public String getCreatedDate()
    {
        return DateHelper.getFormattedDateAndTime(m_createDate);
    }

    /**
     * Get the date that the comment was created.
     */
    public Date getCreatedDateAsDate()
    {
        return m_createDate;
    }

    /**
     * Get the id (user name) of the creator of this comment.
     */
    public String getCreatorId()
    {
        return m_creatorId;
    }

    /**
     * Get the comment.
     */
    public String getComment()
    {
        return m_comment == null ? "" : m_comment;
    }

    /**
     * Get the target locale this task comment is associated to.
     */
    public Locale getTargetLocale()
    {
        return m_targetLocale;
    }

    /**
     * Get the name of the task the comment is associated with.
     */
    public String getTaskName()
    {
        return m_taskName;
    }  
    
    // just for Activity Name issue, the data of Activity Name in GlobalSight6.9.2
    // contains companyId postfix, when display it, the displayname should be used.
    /**
     * Get the diplayname of the task the comment is associated with.
     */
    public String getTaskDisplayName()
    {
        
    	String displayName = m_taskName;
    	try
    	{
    		Activity act = (Activity)ServerProxy.getJobHandler().getActivity(m_taskName);
    		displayName = act.getDisplayName();
    	}
    	catch (Exception e)
    	{
    		
    	}
    	return displayName;
    }  

    /**
     * Get the list of file attachments (CommentFile objects). 
     */
    public List getAttachments()
    {
        return m_attachments;
    }

    /** 
     * override - get the contents of the TaskCommentInfo as a string.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("{m_createDate=");
        sb.append(m_createDate.toString());
        sb.append(", m_creatorId=");
        sb.append(m_creatorId);
        sb.append(", m_comment=");
        sb.append(m_comment);
        sb.append(", m_taskName=");
        sb.append(m_taskName);
        sb.append(", m_targetLocale=");
        sb.append(m_targetLocale.toString());
        sb.append(", number of file attachments=");
        sb.append(m_attachments.size());
        sb.append("}");

        return sb.toString();
    }

    // package level methods
    void setAttachments(ArrayList p_list)
    {
        m_attachments = p_list;
    }

}

