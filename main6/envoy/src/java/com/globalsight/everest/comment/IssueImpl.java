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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * This class holds comments that users make that pertain to a particular task.
 */
public class IssueImpl extends CommentImpl implements Issue, Comparable
{
    private static final long serialVersionUID = 4425479989725750197L;

    public final static String ISSUE_HISTORY = "m_issueHistory";
    public final static String OBJECT_TYPE = "m_levelObjectType";
    public final static String LOGICAL_KEY = "m_logicalKey";

    // @see Issue for valid priorities
    private String m_priority;
    // @see Issue for valid status
    private String m_status = STATUS_OPEN;
    // @see Issue for valid category
    private String m_category = CATEGORY_TYPE01;

    // the list of any comment text that has been added to the issue.
    // list of IssueHistoryImpl objects
    private List m_issueHistory = new ArrayList();
    private boolean share = false;
    private boolean overwrite = false;

    /**
     * String stores key information separated by "_" to specify what higher
     * level group it is part of
     * 
     * ie. PageId_TuId_TuvId_SubId (if work object type = SEGMENT)
     * JobId_WfId_TaskId (if work object type = TASK)
     */
    private String m_logicalKey = null;

    private long targetPageId = 0;

    // contains the type and id of the object the issue is assigned to
    private int m_levelObjectType = TYPE_SEGMENT; // default is segment
    private long m_levelObjectId;

    private static Map PRIORITY_MAP;
    static
    {
        PRIORITY_MAP = new HashMap();
        PRIORITY_MAP.put("U", "urgent");
        PRIORITY_MAP.put("L", "low");
        PRIORITY_MAP.put("M", "medium");
        PRIORITY_MAP.put("H", "high");
    }

    /**
     * Default constructor.
     */
    public IssueImpl()
    {
    }

    /**
     * Construct a IssueImpl with priority and status and category
     */
    public IssueImpl(int p_levelObjectType, long p_levelObjectId,
            String p_title, String p_priority, String p_status,
            String p_category, String p_creatorId, String p_commentText)
    {
        super(Calendar.getInstance().getTime(), p_creatorId, p_title, null);
        m_levelObjectType = p_levelObjectType;
        m_levelObjectId = p_levelObjectId;
        setTitle(p_title);
        setPriority(p_priority);
        setStatus(p_status);
        setCategory(p_category);
        addHistory(p_creatorId, p_commentText);
    }

    /**
     * Construct a IssueImpl with priority and status and category and a logical
     * key
     */
    public IssueImpl(int p_levelObjectType, long p_levelObjectId,
            String p_title, String p_priority, String p_status,
            String p_category, String p_creatorId, String p_commentText,
            String p_logicalKey)
    {
        this(p_levelObjectType, p_levelObjectId, p_title, p_priority, p_status,
                p_category, p_creatorId, p_commentText);
        setLogicalKey(p_logicalKey);
        if (p_logicalKey != null && p_logicalKey.indexOf("_") > -1)
        {
            int index = p_logicalKey.indexOf("_");
            setTargetPageId(Long.parseLong(p_logicalKey.substring(0, index)));
        }
    }

    /**
     * Returns the level as a one char - string to signify the level type. "S" =
     * segment/TUV
     */
    public static String getLevelTypeAsString(int p_levelObjectType)
    {
        String objectType = null;
        if (p_levelObjectType == Issue.TYPE_SEGMENT)
        {
            objectType = "S";
        }
        return objectType;
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

    /**
     * @see Issue.getPriority()
     */
    public String getPriority()
    {
        return m_priority;
    }

    /**
     * @see Issue.setPriority(String)
     */
    public void setPriority(String p_priority)
    {
        m_priority = p_priority;
    }

    public String getPriorityInDb()
    {
        String priority = m_priority;
        if (priority != null && priority.length() > 1)
        {
            priority = priority.substring(0, 1).toUpperCase();
        }
        return priority;
    }

    public void setPriorityInDb(String p_priority)
    {
        m_priority = (String) PRIORITY_MAP.get(p_priority);
    }

    /**
     * @see Issue.getTitle()
     */
    public String getTitle()
    {
        return getName();
    }

    /**
     * @see Issue.setTitle(String)
     */
    public void setTitle(String p_title)
    {
        p_title = EditUtil.truncateUTF8Len(p_title, 200);
        setName(p_title);
    }

    /**
     * @see Issue.getStatus().
     */
    public String getStatus()
    {
        return m_status;
    }

    /**
     * @see Issue.setStatus(String)
     */
    public void setStatus(String p_status)
    {
        m_status = p_status;
    }

    /**
     * @see Issue.getLevelObjectType()
     */
    public int getLevelObjectType()
    {
        return m_levelObjectType;
    }

    /**
     * @see Issue.getLevelObjectId()
     */
    public long getLevelObjectId()
    {
        return m_levelObjectId;
    }

    /**
     * @see Issue.getLogicalKey()
     */
    public String getLogicalKey()
    {
        return m_logicalKey;
    }

    /**
     * @see Issue.setLogicalKey(String)
     */
    public void setLogicalKey(String p_key)
    {
        m_logicalKey = p_key;
    }

    public long getTargetPageId()
    {
        return this.targetPageId;
    }

    public void setTargetPageId(long p_targetPageId)
    {
        this.targetPageId = p_targetPageId;
    }

    /**
     * @see Issue.getHistory()
     */
    public List getHistory()
    {
        SortUtil.sort(m_issueHistory, new IssueHistoryComparator());
        return m_issueHistory;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Override Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Compares this issue to another based on date <br>
     * 
     * @param o
     *            -- another IssueImpl
     * @return -1, 0, or 1
     */
    public int compareTo(Object o)
    {
        if (o == null)
        {
            return 1;
        }
        else
        {
            IssueImpl otc = (IssueImpl) o;
            return this.m_createDate.compareTo(otc.m_createDate);
        }
    }

    /**
     * Returns the issue's title. To get the comment text use getHistory()
     */
    public String getComment() throws RuntimeException
    {
        return getTitle();
    }

    /**
     * Sets the issue's title. To set or add comment text use getHistory() and
     * add to the IssueHistory.
     */
    public void setComment(String p_comment) throws RuntimeException
    {
        setTitle(p_comment);
    }

    /**
     * This method is not supported. The objects don't know about the Issues,
     * yet the issues know about what object they are associated to
     * (m_levelObjectType, m_levelObjectId)
     */
    public WorkObject getWorkObject()
    {
        throw new UnsupportedOperationException(
                "This method is not supported.  Use getLevelObjectType() and getLevelObjectId()");
    }

    /**
     * Returns 'true' if the ids of the Issue objects are equal, 'false' if they
     * aren't.
     * 
     * @param p_comment
     *            The Issue object to compare with
     * @return 'true' if the issue objects have the same ids. Otherwise return
     *         'false'
     */
    public boolean equals(Object p_issue)
    {
        if (p_issue instanceof Issue)
        {
            return (getId() == ((Issue) p_issue).getId());
        }

        return false;
    }

    /**
     * Override - write out the contents of the issue into a string.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append("{m_priority=");
        sb.append(m_priority);
        sb.append(", m_category=");
        sb.append(m_category);
        sb.append(", m_status=");
        sb.append(m_status);
        sb.append(", m_logicalKey=");
        sb.append(m_logicalKey);
        sb.append(", m_levelObjectType=");
        sb.append(getLevelTypeAsString(m_levelObjectType));
        sb.append(", m_levelObjectId=");
        sb.append(m_levelObjectId);
        sb.append(", m_issueHistory={");
        sb.append(m_issueHistory.toString());
        sb.append("}}");

        return sb.toString();
    }

    // ---override package methods

    /**
     * This method is not suppored since there are IssueHistorys that have their
     * own reportedBy userid.
     */
    void setModifierId(String p_modifierId)
    {
        throw new UnsupportedOperationException(
                "This method is not supported.  Use addHistory()");
    }

    /**
     * This method is not suppored since there are IssueHistorys that have their
     * own dateReported.
     */
    void setModifiedDate(Date p_date)
    {
        throw new UnsupportedOperationException(
                "This method is not supported.  Use addHistory()");
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Override Methods
    // ////////////////////////////////////////////////////////////////////

    //
    // package methods
    //
    public IssueHistory addHistory(String p_reportedBy, String p_commentText)
    {
        IssueHistory ih = new IssueHistoryImpl(this, p_reportedBy,
                p_commentText);
        // just add to the end of the list - the list will
        // be sorted when history is retrieved.
        m_issueHistory.add(ih);
        return ih;
    }

    public String getLevelObjectTypeAsString()
    {
        return getLevelTypeAsString(m_levelObjectType);
    }

    public void setLevelObjectTypeAsString(String objectType)
    {
        if ("S".equalsIgnoreCase(objectType))
        {
            m_levelObjectType = 4;
        }
    }

    public void setLevelObjectId(long objectId)
    {
        m_levelObjectId = objectId;
    }

    public Set getIssueHistory()
    {
        Set issueHistory = null;
        if (m_issueHistory != null)
        {
            issueHistory = new HashSet(m_issueHistory);
        }
        return issueHistory;
    }

    public void setIssueHistory(Set history)
    {
        if (history == null)
        {
            m_issueHistory = new ArrayList();

        }
        else
        {
            m_issueHistory = new ArrayList(history);
        }
    }

    public boolean isShare()
    {
        return share;
    }

    public void setShare(boolean share)
    {
        this.share = share;
    }

    public boolean isOverwrite()
    {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite)
    {
        this.overwrite = overwrite;
    }
}
