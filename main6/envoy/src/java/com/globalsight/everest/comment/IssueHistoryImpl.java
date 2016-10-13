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

// 
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.date.DateHelper;
import com.globalsight.util.edit.EditUtil;

//
import java.util.Calendar;
import java.util.Date;

/**
 * Implements the IssueHistory interface.
 */
public class IssueHistoryImpl extends PersistentObject implements IssueHistory
{
    private static final long serialVersionUID = 4189548225885003241L;

    public final static String DATE_REPORTED = "m_dateReported";

    // private attributes
    private Date m_dateReported;
    private String m_reportedBy;
    private String m_comment;

    // backpointer to the issue this is history about.
    private Issue m_issue;
    
    private Long dbId;

    /**
     * Default constructor.
     */
    public IssueHistoryImpl()
    {
    }

    /**
     * Constructor
     */
    public IssueHistoryImpl(Issue p_issue, String p_reportedBy,
            String p_commentText)
    {
        m_issue = p_issue;
        m_reportedBy = p_reportedBy;
        m_dateReported = Calendar.getInstance().getTime();

        setComment(p_commentText);
    }
    
    /**
     * @see IssueHistory.getId()
     */
    public long getId()
    {
        return m_issue.getId() + m_dateReported.getTime();
    }

    /**
     * @see IssueHistory.dateReported()
     */
    public String dateReported()
    {
        return DateHelper.getFormattedDateAndTime(m_dateReported);
    }

    /**
     * @see IssueHistory.dateReportedAsDate()
     */
    public Date dateReportedAsDate()
    {
        return m_dateReported;
    }

    /**
     * @see IssueHistory.reportedBy()
     */
    public String reportedBy()
    {
        return m_reportedBy;
    }

    /**
     * @see IssueHistory.getComment()
     */
    public String getComment()
    {
        return m_comment;
    }

    /**
     * 
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append("{m_dateRepoted=");
        sb.append(dateReported());
        sb.append(", m_reportedBy=");
        sb.append(m_reportedBy);
        sb.append(", m_comment=");
        sb.append(m_comment);
        sb.append("}");

        return sb.toString();
    }

    /**
     * Compares this IssueHistory to another based on date <br>
     * 
     * @param o --
     *            another IssueHistoryImpl
     * @return -1, 0, or 1
     */
    public int compareTo(Object o)
    {
        if (o == null)
            return 1;
        else
        {
            IssueHistoryImpl ih = (IssueHistoryImpl) o;
            return this.m_dateReported.compareTo(ih.m_dateReported);
        }
    }

    /**
     * 
     */
    public void dateReported(Date p_date)
    {
        m_dateReported = p_date;
    }

    /**
     * Set the comment text to what is specified. From the UI it should already
     * be less than 4000 characters but truncate for other cases.
     */
    public void setComment(String p_comment)
    {
        m_comment = p_comment;
        if (p_comment != null)
        {
            m_comment = EditUtil.truncateUTF8Len(p_comment, 4000);
        }
    }

    public Date getDateReported()
    {
        return m_dateReported;
    }

    public void setDateReported(Date reported)
    {
        m_dateReported = reported;
    }

    public String getReportedBy()
    {
        return m_reportedBy;
    }

    public void setReportedBy(String by)
    {
        m_reportedBy = by;
    }

    public Issue getIssue()
    {
        return m_issue;
    }

    public void setIssue(Issue m_issue)
    {
        this.m_issue = m_issue;
    }

    public Long getDbId()
    {
        return dbId;
    }

    public void setDbId(Long dbId)
    {
        this.dbId = dbId;
    }
}
