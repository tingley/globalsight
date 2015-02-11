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
package com.globalsight.everest.persistence.comment;

// globalsight
import java.util.List;

/**
 * IssueUnnamedQueries. This class contains and builds the queries that can't be
 * be built ahead with a name.
 * 
 */
public class IssueUnnamedQueries
{
    public static final String TYPE_ARG = "objectType";
    public static final String KEY_ARG = "logicalKey";

    // count attr to be used by result handler
    static final String ISSUE_COUNT_ATTR = "issueCount";

    // sql string to get issues by type
    private static String ISSUES_BY_TYPE_SQL = "select * from issue where issue_object_type = :"
            + TYPE_ARG;

    // sql string to get the count of issues by type and logical key
    private static String ISSUE_COUNT_BY_TYPE_AND_KEY_SQL = "select count(*) from issue where issue_object_type = :"
            + TYPE_ARG + " and logical_key like :" + KEY_ARG;

    // sql string to get the count of issues by type and logical key
    private static String ISSUE_COUNT_BY_TYPE_STATUS_AND_KEY_SQL = "select count(*) from issue where issue_object_type = :"
            + TYPE_ARG;

    //
    // PUBLIC STATIC METHODS
    //

    /**
     * Creates the report query that returns the number of issues that are
     * associated with the object type, logical key and in the states provided
     * in the list. If no states are provided then it looks at all states.
     */
    public static String getIssueCountByTypeKeyAndState(List p_states)
    {
        StringBuffer sql = new StringBuffer(ISSUE_COUNT_BY_TYPE_AND_KEY_SQL);
        // build a list of states if they are specified
        if (p_states != null && p_states.size() > 0)
        {
            sql.append(" and status in ('");
            sql.append((String) p_states.get(0));
            for (int i = 1; i < p_states.size(); i++)
            {
                sql.append("','");
                sql.append((String) p_states.get(i));
            }
            sql.append("')");
        }

        return sql.toString();
    }

    /**
     * Creates the read all query that returns the issues that are associated
     * with the object type and the object ids specified. If not ids are
     * specified then it returns all issues of the particular object type.
     * 
     * @param p_objectIds
     *            The list of object ids that the issues are associated with.
     *            This list should not be contain more sthan 1000 ids. If there
     *            is more this query will fail since it uses an "IN" clause
     *            which can only hold up to 1000 items in its list.
     */
    public static String getIssuesByTypeAndObjectId(List p_objectIds)
    {
        StringBuffer sql = new StringBuffer(ISSUES_BY_TYPE_SQL);

        // build a list of ids if they are specified
        if (p_objectIds != null && p_objectIds.size() > 0)
        {
            sql.append(" and issue_object_id in (");
            sql.append((Long) p_objectIds.get(0));
            for (int i = 1; i < p_objectIds.size(); i++)
            {
                sql.append(", ");
                sql.append((Long) p_objectIds.get(i));
            }
            sql.append(")");
        }

        return sql.toString();
    }

    /**
     * Creates the read all query that returns the issues that are associated
     * with the object type and the object ids specified. If not ids are
     * specified then it returns all issues of the particular object type.
     * 
     * @param p_objectIds
     *            The list of object ids that the issues are associated with.
     *            This list should not be contain more sthan 1000 ids. If there
     *            is more this query will fail since it uses an "IN" clause
     *            which can only hold up to 1000 items in its list.
     */
    public static String getIssueCountByTypeStatusAndPageId(List p_states,
            List p_pageIds)
    {
        StringBuffer sql = new StringBuffer(
                ISSUE_COUNT_BY_TYPE_STATUS_AND_KEY_SQL);

        // build a list of states if they are specified
        if (p_states != null && p_states.size() > 0)
        {
            sql.append(" and status in ('");
            sql.append((String) p_states.get(0));
            for (int i = 1; i < p_states.size(); i++)
            {
                sql.append("','");
                sql.append((String) p_states.get(i));
            }
            sql.append("')");
        }
        // build a list of ids if they are specified
        if (p_pageIds != null && p_pageIds.size() > 0)
        {
            sql.append(" and ( logical_key like '");
            sql.append((String) p_pageIds.get(0));
            if (!((String) p_pageIds.get(0)).endsWith("%"))
            {
                sql.append("%");
            }
            for (int i = 1; i < p_pageIds.size(); i++)
            {
                sql.append("' or logical_key like '");
                sql.append((String) p_pageIds.get(i));
                if (!((String) p_pageIds.get(i)).endsWith("%"))
                {
                    sql.append("%");
                }
            }
            sql.append("')");
        }

        return sql.toString();
    }
}
