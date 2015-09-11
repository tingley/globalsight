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
package com.globalsight.everest.edit.offline.page;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.globalsight.everest.comment.Issue;

/**
 * A data object holding the various "parts" of an issue. Issues are
 * represented in RTF as annotations.
 *
 * GlobalSight RTF Issues/Annotations contain special delimiters that
 * separate and identify the various internal parts of an GlobalSight
 * Issue. These parts are parsed by a regular expression and deposited
 * in this data object. The error checker later verifies their values
 * and reports any errors. After error checking, these values are used
 * to create a new issue or update an existing one.
 */
public class UploadIssue implements Issue, Serializable
{
    // The logical key stored in the database is composed of
    // targetpageid_tuid_tuvid_subid, so we need to store the
    // individual components.

	private static final long serialVersionUID = 5037032960389554756L;

	// This is the TU ID.
    private long m_tuId = -1;
    // TUV ID must be set manually since it's not stored in offline
    // files (only tu_sub is).
    private long m_tuvId = -1;
    // The Sub ID.
    private long m_subId = 0;

    private String m_title;
    private String m_status;
    // @see Issue for valid category
    private String m_category = CATEGORY_TYPE01;
    private String m_priority;
    private String m_comment;

    // This is nasty. OfflinePageData indexes uploaded segments by
    // their display ID instead of [TUID,SUBID]. To match uploaded
    // issues with uploaded segments, we need to use the display ID.
    private String m_displayId;

    /** Constructor. */
    public UploadIssue(String p_displayId, long p_tuId, long p_subId,
        String p_title, String p_status, String p_category,
        String p_priority, String p_comment)
    {
        m_displayId = p_displayId;

        m_tuId = p_tuId;
        m_subId = p_subId;

        m_title = p_title;
        m_status = p_status;
        m_category = p_category;
        m_priority = p_priority;
        m_comment = p_comment;
    }

    public String getDisplayId()
    {
        return m_displayId;
    }

    public long getLevelObjectId()
    {
        return m_tuvId;
    }

    public long getTuId()
    {
        return m_tuId;
    }

    public long getTuvId()
    {
        return m_tuvId;
    }

    public void setTuvId(long p_id)
    {
        m_tuvId = p_id;
    }

    public long getSubId()
    {
        return m_subId;
    }

    public String getStatus()
    {
        return m_status;
    }
    
    /**
     * @see Issue.getCategory()
     */
    public String getCategory()
    {
        return m_category;
    }

    /**
     * @see Issue.setCategory(String)
     */
    public void setCategory(String p_category)
    {
    	m_category = p_category;
    }

    public String getPriority()
    {
        return m_priority;
    }

    public String getTitle()
    {
        return m_title;
    }

    public String getComment()
    {
        return m_comment;
    }

    /** NOT USED IN UPLOAD */
    public long getId()
    {
        return -1;
    }

    /** NOT USED IN UPLOAD */
    public String getCreatedDate()
    {
        return null;
    }

    /** NOT USED IN UPLOAD */
    public Date getCreatedDateAsDate()
    {
        return null;
    }

    /** NOT USED IN UPLOAD */
    public String getCreatorId()
    {
        return null;
    }

    /** NOT USED IN UPLOAD */
    public List getHistory()
    {
        return null;
    }

    /** NOT USED IN UPLOAD */
    public int getLevelObjectType()
    {
        return TYPE_SEGMENT;
    }

    /** NOT USED IN UPLOAD */
    public String getLogicalKey()
    {
        return null;
    }

    /** NOT USED IN UPLOAD */
    public com.globalsight.everest.foundation.WorkObject getWorkObject()
    {
        return null;
    }

    public void setComment(String p_comment)
    {
        m_comment = p_comment;
    }

    public void setPriority(String p_priority)
    {
        m_priority = p_priority;
    }

    public void setStatus(String p_status)
    {
        m_status = p_status;
    }

    public void setTitle(String p_title)
    {
        m_title = p_title;
    }

    /** NOT USED IN UPLOAD */
    public void setLogicalKey(String p_arg)
    {
    }

    /** NOT USED IN UPLOAD */
    public void setLevelObjectId(String p_arg)
    {
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("UploadIssue ");
        sb.append(m_tuId);
        sb.append(":");
        sb.append(m_tuvId);
        sb.append(":");
        sb.append(m_subId);
        sb.append(" prio=");
        sb.append(m_priority);
        sb.append(" status=");
        sb.append(m_status);
        sb.append(" category=");
        sb.append(m_category);
        sb.append(" title='");
        sb.append(m_title);
        sb.append("' comment= '");
        sb.append(m_comment);
        sb.append("'");

        return sb.toString();
    }

    @Override
    public boolean isOverwrite()
    {
        return false;
    }

    @Override
    public boolean isShare()
    {
        return false;
    }

    @Override
    public void setOverwrite(boolean overwrite)
    {
        
    }

    @Override
    public void setShare(boolean share)
    {
        
    }
}
