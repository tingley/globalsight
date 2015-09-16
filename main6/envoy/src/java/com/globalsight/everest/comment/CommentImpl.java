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
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflowmanager.Workflow;

import java.util.Date;

/**
 * This class holds comments that users make that pertain to a particular task.
 */
public class CommentImpl extends PersistentObject implements Comment,
        Comparable
{
    private static final long serialVersionUID = 4924147655847439462L;
    protected Date m_createDate = null;
    protected String m_creatorId = null;
    protected String m_commentString = null;

    // points to the object the comment is associated with.
    protected WorkObject m_object = null;

    /**
     * Default constructor.
     */
    public CommentImpl()
    {
    }

    /**
     * Construct a CommentImpl.
     */
    public CommentImpl(Date p_createDate, String p_creatorId, String p_comment,
            WorkObject p_object)
    {
        m_createDate = p_createDate;
        m_creatorId = p_creatorId;
        setComment(p_comment);
        setObject(p_object);
    }

    /**
     * Get the date that the comment was created.
     * 
     * @return The accepted date.
     */
    public String getCreatedDate()
    {
        return DateHelper.getFormattedDateAndTime(m_createDate);
    }

    /**
     * Get the date that the comment was created.
     * 
     * @return The accepted date.
     */
    public Date getCreatedDateAsDate()
    {
        return m_createDate;
    }

    /**
     * Get the id (user name) of the creator of this comment.
     * 
     * @return The comment creator's id.
     */
    public String getCreatorId()
    {
        return m_creatorId;
    }

    /**
     * Get the comment.
     * 
     * @return The comment.
     */
    public String getComment()
    {
        return m_commentString == null ? "" : m_commentString;
    }

    /**
     * Compares this task comment Impl to another based on date <br>
     * 
     * @param o --
     *            another CommentImpl
     * @return -1, 0, or 1
     */
    public int compareTo(Object o)
    {
        if (o == null)
            return 1;
        else
        {
            CommentImpl otc = (CommentImpl) o;
            return this.m_createDate.compareTo(otc.m_createDate);
        }
    }

    /**
     * Set the correct comment variable.
     */
    public void setComment(String p_comment)
    {
        if (p_comment != null && p_comment.length() > 0)
        {
            m_commentString = p_comment;
        }
    }

    /**
     * @see Comment.getWorkfObject()
     */
    public WorkObject getWorkObject()
    {
        return m_object;
    }

    public WorkObject getObject()
    {
        return m_object;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Set the modifier's user id to be the specified value.
     * 
     * @param p_modifierId -
     *            The username of this comment's modifier.
     */
    void setModifierId(String p_modifierId)
    {
        m_creatorId = p_modifierId;
    }

    /**
     * Set the date which this comment was modified to be the specified value.
     * 
     * @param p_date -
     *            The date this comment was modified.
     */
    void setModifiedDate(Date p_date)
    {
        m_createDate = p_date;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////
    // Begin: Override Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Returns 'true' if the ids of the Comment objects are equal, 'false' if
     * they aren't.
     * 
     * @param p_comment
     *            The Comment object to compare with
     * @return 'true' if the comment objects have the same ids. Otherwise return
     *         'false'
     */
    public boolean equals(Object p_comment)
    {
        if (p_comment instanceof Comment) { return (getId() == ((Comment) p_comment)
                .getId()); }
        return false;
    }

    /**
     * The hashCode method is overridden to support the 'equals' method. If two
     * Comment objects are equal according to the equals(Object) method, then
     * calling the hashCode method on each of the two objects will produce the
     * same integer result.
     * 
     * @return the comment id's hashCode.
     */
    public int hashCode()
    {
        return getIdAsLong().hashCode();
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("{m_createDate=");
        sb.append(m_createDate != null ? m_createDate.toString() : null);
        sb.append(", m_creatorId=");
        sb.append(m_creatorId);
        sb.append(", m_commentString=");
        sb.append(m_commentString);
        sb.append(", m_object=");
        if (m_object == null)
        {
            sb.append("null");
        }
        else if (m_object instanceof Job)
        {
            sb.append("Job: ");
            sb.append(((Job) m_object).getId());
        }
        else if (m_object instanceof Workflow)
        {
            sb.append("Workflow: ");
            sb.append(((Workflow) m_object).getId());
        }
        else if (m_object instanceof Task)
        {
            sb.append("Task: ");
            sb.append(((Task) m_object).getId());
        }
        else
        {
            sb.append("Unknown");
        }
        sb.append("}");

        return sb.toString();
    }

    public Date getCreateDate()
    {
        return m_createDate;
    }

    public void setCreateDate(Date date)
    {
        m_createDate = date;
    }

    public String getCommentString()
    {
        return m_commentString;
    }

    public void seCommentString(String string)
    {
        m_commentString = string;
    }

    public void setCreatorId(String id)
    {
        m_creatorId = id;
    }

    public void setCommentString(String string)
    {
        m_commentString = string;
    }

    public void setObject(WorkObject m_object)
    {
        this.m_object = m_object;
    }
    
    // ////////////////////////////////////////////////////////////////////
    // End: Override Methods
    // ////////////////////////////////////////////////////////////////////
}
