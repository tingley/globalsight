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
package com.globalsight.everest.edit.online;

import com.globalsight.everest.comment.Issue;

import java.util.ArrayList;
import java.io.Serializable;

/**
 * A data object that holds all comments for a single segment.
 */
public class CommentView
    implements Serializable
{
    private long m_tuId;
    private long m_tuvId;
    private long m_subId;

    /** The Issue object with its IssueHistory objects. */
    private Issue m_comment = null;

    //////////////////////////////////////////////////////////////////
    // Constructors
    //////////////////////////////////////////////////////////////////

    public CommentView(long p_tuId, long p_tuvId, long p_subId)
    {
        m_tuId = p_tuId;
        m_tuvId = p_tuvId;
        m_subId = p_subId;
    }

    public CommentView(long p_tuId, long p_tuvId, long p_subId, Issue p_comment)
    {
        m_tuId = p_tuId;
        m_tuvId = p_tuvId;
        m_subId = p_subId;
        m_comment = p_comment;
    }

    //////////////////////////////////////////////////////////////////
    // Public APIs
    //////////////////////////////////////////////////////////////////

    public long getTuId()
    {
        return m_tuId;
    }

    public long getTuvId()
    {
        return m_tuvId;
    }

    public long getSubId()
    {
        return m_subId;
    }

    public void setComment(Issue p_comment)
    {
        m_comment = p_comment;
    }

    public Issue getComment()
    {
        return m_comment;
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append("CommentView segment=");
        result.append(m_tuId).append('_');
        result.append(m_tuvId).append('_');
        result.append(m_subId);

        if (m_comment != null)
        {
            result.append(" issue_id=");
            result.append(m_comment.getId());
        }

        return result.toString();
    }
}
