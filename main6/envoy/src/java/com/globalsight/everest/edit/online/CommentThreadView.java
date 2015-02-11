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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.util.SortUtil;

/**
 * A data object that holds all comment threads for all segments in a target
 * page.
 */
public class CommentThreadView implements Serializable
{
    static private final Comparator s_compBySegment = new CompBySegment();
    static private final Comparator s_compByPriority = new CompByPriority();
    static private final Comparator s_compByStatus = new CompByStatus();
    static private final Comparator s_compByTitle = new CompByTitle();
    static private final Comparator s_compByDate = new CompByDate();
    static private final Comparator s_compByUser = new CompByUser();
    // add comparator for category
    static private final Comparator s_compByCategory = new CompByCategory();

    static public final String SORT_SEGMENT_ASC = "SortSegAsc";
    static public final String SORT_SEGMENT_DESC = "SortSegDesc";
    static public final String SORT_PRIO_ASC = "SortPriAsc";
    static public final String SORT_PRIO_DESC = "SortPriDesc";
    static public final String SORT_STATUS_ASC = "SortStaAsc";
    static public final String SORT_STATUS_DESC = "SortStaDesc";
    static public final String SORT_TITLE_ASC = "SortTitAsc";
    static public final String SORT_TITLE_DESC = "SortTitDesc";
    static public final String SORT_DATE_ASC = "SortDateAsc";
    static public final String SORT_DATE_DESC = "SortDateDesc";
    static public final String SORT_USER_ASC = "SortUserAsc";
    static public final String SORT_USER_DESC = "SortUserDesc";
    // add category to comments
    static public final String SORT_CATEGORY_ASC = "SortCategoryAsc";
    static public final String SORT_CATEGORY_DESC = "SortCategoryDesc";

    /** The list of Issue objects representing our threads. */
    private ArrayList m_issues = null;

    private String m_sortedBy = "";

    //
    // Constructors
    //

    public CommentThreadView()
    {
        m_issues = new ArrayList();
    }

    public CommentThreadView(List p_issues)
    {
        m_issues = new ArrayList(p_issues);

        sortBySegment(false);
    }

    //
    // public APIs
    //

    public void setIssues(List p_issues)
    {
        m_issues = new ArrayList(p_issues);

        sortBySegment(false);
    }

    public ArrayList getIssues()
    {
        return m_issues;
    }

    public String getSortedBy()
    {
        return m_sortedBy;
    }

    public void sort(String p_arg)
    {
        if (p_arg.equals(SORT_SEGMENT_ASC))
        {
            sortBySegment(true);
        }
        else if (p_arg.equals(SORT_SEGMENT_DESC))
        {
            sortBySegment(false);
        }
        else if (p_arg.equals(SORT_PRIO_ASC))
        {
            sortByPriority(true);
        }
        else if (p_arg.equals(SORT_PRIO_DESC))
        {
            sortByPriority(false);
        }
        else if (p_arg.equals(SORT_STATUS_ASC))
        {
            sortByStatus(true);
        }
        else if (p_arg.equals(SORT_STATUS_DESC))
        {
            sortByStatus(false);
        }
        else if (p_arg.equals(SORT_TITLE_ASC))
        {
            sortByTitle(true);
        }
        else if (p_arg.equals(SORT_TITLE_DESC))
        {
            sortByTitle(false);
        }
        else if (p_arg.equals(SORT_DATE_ASC))
        {
            sortByDate(true);
        }
        else if (p_arg.equals(SORT_DATE_DESC))
        {
            sortByDate(false);
        }
        else if (p_arg.equals(SORT_USER_ASC))
        {
            sortByUser(true);
        }
        else if (p_arg.equals(SORT_USER_DESC))
        {
            sortByUser(false);
        }
        else if (p_arg.equals(SORT_CATEGORY_ASC))
        {
            sortByCategory(true);
        }
        else if (p_arg.equals(SORT_CATEGORY_DESC))
        {
            sortByCategory(false);
        }
    }

    /**
     * Sorts by category.
     * 
     * @param p_asc
     *            whether it is a asc order
     */
    private void sortByCategory(boolean p_asc)
    {
        m_sortedBy = SORT_CATEGORY_ASC;

        SortUtil.sort(m_issues, s_compByCategory);

        if (!p_asc)
        {
            m_sortedBy = SORT_CATEGORY_DESC;

            Collections.reverse(m_issues);
        }
    }

    public void sortBySegment(boolean p_asc)
    {
        m_sortedBy = SORT_SEGMENT_ASC;

        SortUtil.sort(m_issues, s_compBySegment);

        if (!p_asc)
        {
            m_sortedBy = SORT_SEGMENT_DESC;

            Collections.reverse(m_issues);
        }
    }

    public void sortByPriority(boolean p_asc)
    {
        m_sortedBy = SORT_PRIO_ASC;

        SortUtil.sort(m_issues, s_compByPriority);

        if (!p_asc)
        {
            m_sortedBy = SORT_PRIO_DESC;

            Collections.reverse(m_issues);
        }
    }

    public void sortByStatus(boolean p_asc)
    {
        m_sortedBy = SORT_STATUS_ASC;

        SortUtil.sort(m_issues, s_compByStatus);

        if (!p_asc)
        {
            m_sortedBy = SORT_STATUS_DESC;

            Collections.reverse(m_issues);
        }
    }

    public void sortByTitle(boolean p_asc)
    {
        m_sortedBy = SORT_TITLE_ASC;

        SortUtil.sort(m_issues, s_compByTitle);

        if (!p_asc)
        {
            m_sortedBy = SORT_TITLE_DESC;

            Collections.reverse(m_issues);
        }
    }

    public void sortByDate(boolean p_asc)
    {
        m_sortedBy = SORT_DATE_ASC;

        SortUtil.sort(m_issues, s_compByDate);

        if (!p_asc)
        {
            m_sortedBy = SORT_DATE_DESC;

            Collections.reverse(m_issues);
        }
    }

    public void sortByUser(boolean p_asc)
    {
        m_sortedBy = SORT_USER_ASC;

        SortUtil.sort(m_issues, s_compByUser);

        if (!p_asc)
        {
            m_sortedBy = SORT_USER_DESC;

            Collections.reverse(m_issues);
        }
    }

    //
    // Private classes
    //

    static public class CompBySegment implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Issue i1 = (Issue) o1;
            Issue i2 = (Issue) o2;

            String[] key1 = i1.getLogicalKey().split(CommentHelper.SEPARATOR);
            String[] key2 = i2.getLogicalKey().split(CommentHelper.SEPARATOR);

            // TU ID
            int res = (int) (Long.parseLong(key1[1]) - Long.parseLong(key2[1]));
            if (res == 0)
            {
                // TUV ID
                res = (int) (Long.parseLong(key1[2]) - Long.parseLong(key2[2]));
                if (res == 0)
                {
                    // SUB ID
                    res = (int) (Long.parseLong(key1[3]) - Long
                            .parseLong(key2[3]));
                }
            }

            return res;
        }
    }

    static public class CompByPriority implements Comparator
    {
        static public final List s_priorities = Arrays.asList(new String[]
        { Issue.PRI_LOW, Issue.PRI_MEDIUM, Issue.PRI_HIGH, Issue.PRI_URGENT, });

        public int compare(Object o1, Object o2)
        {
            Issue i1 = (Issue) o1;
            Issue i2 = (Issue) o2;

            String p1 = i1.getPriority();
            String p2 = i2.getPriority();

            return s_priorities.indexOf(p1) - s_priorities.indexOf(p2);
        }
    }

    static public class CompByStatus implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Issue i1 = (Issue) o1;
            Issue i2 = (Issue) o2;

            String s1 = i1.getStatus();
            String s2 = i2.getStatus();

            return s1.compareTo(s2);
        }
    }

    static public class CompByTitle implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Issue i1 = (Issue) o1;
            Issue i2 = (Issue) o2;

            String s1 = i1.getComment();
            String s2 = i2.getComment();

            return s1.compareToIgnoreCase(s2);
        }
    }

    static public class CompByDate implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Issue i1 = (Issue) o1;
            Issue i2 = (Issue) o2;

            Date d1 = i1.getCreatedDateAsDate();
            Date d2 = i2.getCreatedDateAsDate();

            return d1.compareTo(d2);
        }
    }

    static public class CompByUser implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Issue i1 = (Issue) o1;
            Issue i2 = (Issue) o2;

            String s1 = i1.getCreatorId();
            String s2 = i2.getCreatorId();

            return s1.compareToIgnoreCase(s2);
        }
    }

    static public class CompByCategory implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Issue i1 = (Issue) o1;
            Issue i2 = (Issue) o2;

            String s1 = i1.getCategory();
            String s2 = i2.getCategory();

            return s1.compareToIgnoreCase(s2);
        }
    }
}
